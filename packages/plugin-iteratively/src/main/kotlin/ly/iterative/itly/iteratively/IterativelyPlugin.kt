package ly.iterative.itly.iteratively

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.segment.backo.Backo
import ly.iterative.itly.*
import ly.iterative.itly.core.Options
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.Thread.MIN_PRIORITY
import java.net.ConnectException
import java.util.concurrent.*

val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

const val DEFAULT_THREAD_NAME = "plugin-iteratively-thread"
val DEFAULT_THREAD_FACTORY: ThreadFactory = ThreadFactory { r ->
    Thread({
        Thread.currentThread().priority = MIN_PRIORITY
        r.run()
    }, DEFAULT_THREAD_NAME)
}
fun newDefaultExecutorService(threadFactory: ThreadFactory): ExecutorService {
    return Executors.newSingleThreadExecutor(threadFactory)
}

internal class AuthInterceptor(apiKey: String) : Interceptor {
    private val authorization: String = "Bearer $apiKey"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
                .header("authorization", authorization)
                .build()
        return chain.proceed(authenticatedRequest)
    }
}

class IterativelyPlugin(
    apiKey: String,
    options: IterativelyOptions
): PluginBase(ID) {
    companion object {
        const val ID = "iteratively"
        const val LOG_TAG = "[plugin-$ID]"
        private val JSONObjectMapper = jacksonObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
            ).setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private val config: IterativelyOptions

    private val client: OkHttpClient
    private val retryPolicy: Backo
    private val queue: BlockingQueue<TrackModel>
    private val mainExecutor: ExecutorService
    private val networkExecutor: ExecutorService
    private val scheduledExecutor: ScheduledExecutorService
    private var isShutdown: Boolean
    private val isExternalNetworkExecutor: Boolean

    // Gets updated in load()
    private lateinit var logger: Logger

    init {
        // adjusts config values in accordance with provided environment value
        val disabled = if (options.environment === Environment.PRODUCTION) true
            else options.disabled

        this.config = options.copy(disabled = disabled)

        mainExecutor = newDefaultExecutorService(config.threadFactory)
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(config.threadFactory)
        networkExecutor = options.networkExecutor ?: newDefaultExecutorService(options.threadFactory)

        queue = LinkedBlockingQueue<TrackModel>()
        isExternalNetworkExecutor = (options.networkExecutor != null)
        isShutdown = false

        client = OkHttpClient.Builder()
//                .dispatcher(Dispatcher(networkExecutor))
                .addInterceptor(AuthInterceptor(apiKey))
                .build()
        retryPolicy = Backo.builder()
            .base(TimeUnit.MILLISECONDS, config.retryOptions.delayInitialMillis)
            .cap(TimeUnit.MILLISECONDS, config.retryOptions.delayMaximumMillis)
            .jitter(1)
            .build()
    }

    override fun load(options: Options) {
        logger = options.logger
        logger.info("$LOG_TAG load")

        if (this.config.disabled) {
            logger.info("$LOG_TAG disabled")
            return
        }

        mainExecutor.submit(PollTrackingQueue())
        scheduledExecutor.scheduleAtFixedRate(
            Runnable { flush() },
            config.flushIntervalMs,
            config.flushIntervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    override fun group(userId: String?, groupId: String, properties: Properties?) {
        this.push(this.toTrackModel(
            type = TrackType.group,
            properties = properties,
            validation = null
        ))
    }

    override fun identify(userId: String?, properties: Properties?) {
        this.push(this.toTrackModel(
            type = TrackType.identify,
            properties = properties,
            validation = null
        ))
    }

    override fun track(userId: String?, event: Event) {
        this.push(this.toTrackModel(
            type = TrackType.track,
            event = event,
            properties = event
        ))
    }

    override fun onValidationError(validation: ValidationResponse, event: Event) {
        val type = TrackType.fromEvent(event)
        this.push(this.toTrackModel(
            type = type,
            event = if (type == TrackType.track) event else null,
            properties = event,
            validation = validation
        ))
    }

    override fun flush() {
        push(TrackModel.newPoisonPill())
    }

    /**
     * Stops this instance from accepting further requests. In-flight events may not be uploaded right
     * away.
     */
    override fun shutdown() {
//        if (this === singleton) {
//            throw UnsupportedOperationException("Default singleton instance cannot be shutdown.")
//        }
        if (this.isShutdown) {
            return
        }

        this.isShutdown = true

        queue.clear()
        mainExecutor.shutdownNow()
        scheduledExecutor.shutdownNow()
        client.dispatcher.cancelAll()
        client.dispatcher.executorService.shutdownNow()
        client.connectionPool.evictAll()
        if (!isExternalNetworkExecutor && !networkExecutor.isShutdown) {
            networkExecutor.shutdownNow()
        }
    }

    private fun assertNotShutdown() {
        check(!this.isShutdown) { "plugin-iteratively was shutdown. Tracking is no longer operational." }
    }

    private fun toTrackModel(
        type: TrackType,
        event: Event? = null,
        properties: Properties? = null,
        validation: ValidationResponse? = null
    ): TrackModel {
        val valid = validation?.valid ?: true
        // Get sanitized info
        val details = if (config.omitValues) "" else validation?.message ?: ""
        val sanitizedProperties: Map<String, Any?>? = if (config.omitValues && properties != null)
            sanitizeValues(properties.properties) else properties?.properties

        return TrackModel(
            type = type,
            eventId = event?.id,
            eventSchemaVersion = event?.version,
            eventName = event?.name,
            properties = sanitizedProperties,
            valid = valid,
            validation = Validation(details)
        )
    }

    private fun sanitizeValues(properties: Map<String, Any?>): Map<String, Nothing?> {
        return properties.keys.associateWith { null }
    }

    private fun push(trackModel: TrackModel) {
        if (config.disabled) {
            return
        }

        logger.debug("$LOG_TAG Queueing '${trackModel.eventName}' type:'${trackModel.type}'")
        try {
            queue.put(trackModel)
        } catch (e: InterruptedException) {
            logger.error("$LOG_TAG Error: Queueing was interrupted for '${trackModel.type}'. ${e.message}")
        }
    }

    private fun getTrackModelJson(trackModels: List<TrackModel>): String {
        return "{\"objects\":${JSONObjectMapper.writeValueAsString(trackModels)}}"
    }

    /**
     * Continually polls queue for new TrackModels.
     *
     * Posts to server when:
     *  1) queue.size >= @flushQueueSize
     *  2) TrackModel.POISON is received from queue
     */
    inner class PollTrackingQueue: Runnable {
        override fun run() {
            var pending: MutableList<TrackModel> = mutableListOf()

            try {
                while (true) {
                    val track = queue.take()
                    val isPoisonPill = track.type == TrackType.POISON

                    if (!isPoisonPill) {
                        pending.add(track)
                    } else if (pending.size < 1) {
                        logger.debug("Flush received. No pending items.")
                        continue
                    }

                    if (pending.size >= config.flushQueueSize || isPoisonPill) {
                        logger.debug("$LOG_TAG Posting ${pending.size} track items to ${config.url}.")

                        // submit upload
                        networkExecutor.submit(Upload(pending))

                        // create a new batch
                        pending = mutableListOf()
                    }
                }
            } catch (e: InterruptedException) {
                logger.debug("$LOG_TAG Processing thread was interrupted.")
            }
        }
    }

    /**
     * Uploads a @batch of TrackModels to server
     */
    inner class Upload(private val batch: List<TrackModel>): Runnable {
        override fun run() {
            for(attempt in 0..config.retryOptions.maxRetries) {
                val retry = upload()
                if (!retry) return
                try {
                    logger.debug("$LOG_TAG waiting to retry (${attempt + 1})")
                    retryPolicy.sleep(attempt)
                } catch (e: InterruptedException) {
                    logger.debug(
                       "$LOG_TAG Thread interrupted waiting to retry upload after $attempt attempts."
                    )
                    return
                }
            }

            logger.error("$LOG_TAG Failed to upload ${batch.size} events. Maximum attempts exceeded.");
        }

        /**
         * Attempts to upload the current batch to server
         *
         * @return retry True if upload should be re-attempted, false otherwise
         */
        private fun upload(): Boolean {
            try {
                logger.debug("$LOG_TAG Post (batch1): ${batch}")
//                batch.forEach {
//                    logger.debug("$LOG_TAG Post (item): ${OrgJsonProperties.toJsonString(it as Object)}")
//                }
                val response = postJson(config.url, getTrackModelJson(batch))
                val code = response.code
                if (response.isSuccessful) {
                    // Upload succeeded, no need to retry
                    logger.debug("Upload complete.")
                    return false
                }
//                response.body?.close()
                if (response.code in 500..599) {
                    logger.debug("Upload received error response from server ($code).")
                    return true
                }
                if (response.code == 429) {
                    logger.debug("Upload rejected due to rate limiting ($code).")
                    return true
                }
                logger.debug("Upload failed due to unhandled HTTP error ($code).")
                return false
            } catch (e: InterruptedException) {
                logger.error("Thread was interrupted before upload could complete.")
                return false
            } catch(e: IOException) {
                if (e.message == "interrupted") {
                    logger.error("Thread was interrupted before upload could complete.(IOException).")
                    return false
                }
                logger.error("Upload failed due to IOException (${e.message}).")
                return true
            } catch(e: ConnectException) {
                logger.error("Error connecting to server.")
                return true
            } catch (e: Exception) {
                logger.error("A unhandled exception occurred. ${e.message}")
                return false
            }
        }

        /**
         * Posts @json to @url
         */
        @Throws(IOException::class)
        private fun postJson(url: String, json: String): Response {
            logger.debug("$LOG_TAG Post JSON: $json")
            val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)
            logger.debug("$LOG_TAG requestBody.contentLength: ${requestBody.contentLength()}")
            val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
            return client.newCall(request).execute()
        }
    }
}
