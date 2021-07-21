package ly.iterative.itly.iteratively

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.segment.backo.Backo
import java.io.IOException
import java.lang.Thread.MIN_PRIORITY
import java.net.ConnectException
import java.util.concurrent.*
import ly.iterative.itly.*
import ly.iterative.itly.Properties
import okhttp3.*
import java.util.*

val JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8")
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

open class IterativelyCallOptions : PluginCallOptions()
class IterativelyAliasOptions : IterativelyCallOptions()
class IterativelyGroupOptions : IterativelyCallOptions()
class IterativelyIdentifyOptions : IterativelyCallOptions()
class IterativelyTrackOptions : IterativelyCallOptions()

class IterativelyPlugin(
    apiKey: String,
    options: IterativelyOptions = IterativelyOptions()
): Plugin<IterativelyAliasOptions, IterativelyIdentifyOptions, IterativelyGroupOptions, IterativelyTrackOptions>(ID) {
    companion object {
        const val ID = "iteratively"
        const val LOG_TAG = "[plugin-$ID]"
        private val JSONObjectMapper = jacksonObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
            ).setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        @JvmField
        val DEFAULT_DATAPLANE_URL = "https://data.us-east-2.iterative.ly/t"
    }

    class Config(
        val url: String,
        val branch: String?,
        val version: String?,
        val omitValues: Boolean,
        val batchSize: Int,
        val flushQueueSize: Long,
        val flushIntervalMs: Long,
        var disabled: Boolean?,
        val threadFactory: ThreadFactory,
        val networkExecutor: ExecutorService,
        val retryOptions: RetryOptions
    )

    val config: Config = Config(
        url = options.url ?: DEFAULT_DATAPLANE_URL,
        branch = options.branch,
        version = options.version,
        omitValues = options.omitValues ?: false,
        batchSize = options.batchSize ?: 100,
        flushQueueSize = options.flushQueueSize ?: 10,
        flushIntervalMs = options.flushIntervalMs ?: 10000,
        disabled = options.disabled,
        threadFactory = options.threadFactory ?: DEFAULT_THREAD_FACTORY,
        networkExecutor = options.networkExecutor ?: newDefaultExecutorService(
            options.threadFactory ?: DEFAULT_THREAD_FACTORY
        ),
        retryOptions = options.retryOptions ?: RetryOptions()
    )

    private var disabled: Boolean = config.disabled ?: false
    private val client: OkHttpClient
    private val retryPolicy: Backo
    private val queue: BlockingQueue<TrackModel>
    private val mainExecutor: ExecutorService
    private val scheduledExecutor: ScheduledExecutorService
    private val isExternalNetworkExecutor: Boolean
    private var isShutdown: Boolean

    // Gets updated in load()
    private lateinit var logger: Logger

    init {
        mainExecutor = newDefaultExecutorService(config.threadFactory)
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(config.threadFactory)

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

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.info("$LOG_TAG load")

        // adjusts config values in accordance with provided environment value
        disabled = config.disabled ?: (options.environment === Environment.PRODUCTION)

        if (disabled) {
            logger.info("$LOG_TAG disabled")
            return
        }

        mainExecutor.submit(PollTrackingQueue())
        scheduledExecutor.scheduleAtFixedRate(
            { flush() },
            config.flushIntervalMs,
            config.flushIntervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    override fun postGroup(userId: String?, groupId: String, properties: Properties?, validationResults: List<ValidationResponse>) {
        this.push(this.toTrackModel(
            type = TrackType.group,
            properties = properties,
            validation = validationResults.find { !it.valid }
        ))
    }

    override fun postIdentify(userId: String?, properties: Properties?, validationResults: List<ValidationResponse>) {
        this.push(this.toTrackModel(
            type = TrackType.identify,
            properties = properties,
            validation = validationResults.find { !it.valid }
        ))
    }

    override fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>) {
        this.push(this.toTrackModel(
            type = TrackType.track,
            event = event,
            properties = event,
            validation = validationResults.find { !it.valid }
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
        client.dispatcher().cancelAll()
        client.dispatcher().executorService().shutdownNow()
        client.connectionPool().evictAll()
        if (!isExternalNetworkExecutor && !config.networkExecutor.isShutdown) {
            config.networkExecutor.shutdownNow()
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
            messageId = UUID.randomUUID().toString(),
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
        if (disabled) {
            return
        }

        if (trackModel.type != TrackType.POISON) {
            logger.debug("$LOG_TAG Queueing '${trackModel.eventName}' type:'${trackModel.type}'")
        }
        try {
            queue.put(trackModel)
        } catch (e: InterruptedException) {
            logger.error("$LOG_TAG Error: Queueing was interrupted for '${trackModel.type}'. ${e.message}")
        }
    }

    private fun getTrackModelJson(trackModels: List<TrackModel>): String {
        val tp = if (config.version != null) "\"trackingPlanVersion\":\"${config.version}\"," else ""
        val bn = if (config.branch != null) "\"branchName\":\"${config.branch}\"," else ""

        return "{${tp}${bn}\"objects\":${JSONObjectMapper.writeValueAsString(trackModels)}}"
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
                        logger.debug("$LOG_TAG Flush received. No pending items.")
                        continue
                    }

                    if (pending.size >= config.flushQueueSize || isPoisonPill) {
                        logger.debug("$LOG_TAG Posting ${pending.size} track items to ${config.url}.")

                        // submit upload
                        config.networkExecutor.submit(Upload(pending))

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

            logger.error("$LOG_TAG Failed to upload ${batch.size} events. Maximum attempts exceeded.")
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
                response.close()

                val code = response.code()
                if (response.isSuccessful) {
                    // Upload succeeded, no need to retry
                    logger.debug("Upload complete.")
                    return false
                }
//                response.body?.close()
                if (code in 500..599) {
                    logger.debug("Upload received error response from server ($code).")
                    return true
                }
                if (code == 429) {
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
            val requestBody = RequestBody.create(JSON_MEDIA_TYPE, json)
            val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
            return client.newCall(request).execute()
        }
    }

    fun disabled(): Boolean {
        return disabled
    }
}
