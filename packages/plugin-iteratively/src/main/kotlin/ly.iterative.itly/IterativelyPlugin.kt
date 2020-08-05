package ly.iterative.itly

import ly.iterative.itly.core.Options
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import net.jodah.failsafe.function.CheckedRunnable
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Thread.MIN_PRIORITY
import java.net.ConnectException
import java.time.temporal.ChronoUnit
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
): PluginBase() {
    companion object {
        @JvmField
        val ID = "iteratively"
    }

    private val config: IterativelyOptions

    private val client: OkHttpClient
    private val queue: BlockingQueue<TrackModel>
    private val mainExecutor: ExecutorService
    private val scheduledExecutor: ScheduledExecutorService
    private val retryPolicy: RetryPolicy<Any>
    private var isShutdown: Boolean

    // Gets updated in load()
    private lateinit var logger: Logger

    init {
        // adjusts config values in accordance with provided environment value
        val disabled = if (options.environment === Environment.PRODUCTION) true
            else options.disabled

        this.config = options.copy(disabled = disabled)

        client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(apiKey))
                .build()
        queue = LinkedBlockingQueue<TrackModel>()
        mainExecutor = newDefaultExecutorService(config.threadFactory)
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(config.threadFactory)
        isShutdown = false
        retryPolicy = RetryPolicy<Any>()
                .handle(ConnectException::class.java)
                .handle(IOException::class.java)
                .onRetry { logger.warn("Retrying upload...") }
                .withBackoff(
                    config.retryOptions.delayInitialSeconds,
                    config.retryOptions.delayMaximumSeconds,
                    ChronoUnit.SECONDS
                )
                .withJitter(1.0)
                .withMaxRetries(config.retryOptions.maxRetries)
    }

    override fun id(): String { return ID }

    override fun load(options: Options) {
        logger = options.logger
        logger.info("[plugin-iteratively] load")

        if (this.config.disabled) {
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

    override fun validationError(validation: ValidationResponse, event: Event) {
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

        queue.clear()
        mainExecutor.shutdownNow()
        scheduledExecutor.shutdownNow()
        config.networkExecutor.shutdown()
        this.isShutdown = true
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
        val sanitizedProperties = if (config.omitValues && properties != null)
            Properties.sanitizeValues(properties) else properties

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

    private fun push(trackModel: TrackModel) {
        if (config.disabled) {
            return
        }

        try {
            queue.put(trackModel)
        } catch (e: InterruptedException) {
            logger.error("Error: Queueing was interrupted for '${trackModel.type}'. ${e.message}")
        }
    }

    private fun getTrackModelJson(trackModels: List<TrackModel>): JSONObject {
        return JSONObject("{}").put("objects", JSONArray(trackModels))
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
                        logger.debug("Posting ${pending.size} track items.")
                        // submit upload
                        Failsafe.with(retryPolicy)
                                .with(config.networkExecutor)
                                .run(Upload(pending))

                        // create a new batch
                        pending = mutableListOf()
                    }
                }
            } catch (e: InterruptedException) {
                logger.debug("Processing thread was interrupted.")
            }
        }
    }

    /**
     * Uploads a @batch of TrackModels to server
     */
    inner class Upload(private val batch: List<TrackModel>): CheckedRunnable {
//        @Throws(IOException::class.java)
        override fun run() {
            var success = false;
            try {
                val response = postJson(config.url, getTrackModelJson(batch))
                success = response.isSuccessful
            } catch (e: Error) {
                logger.error("RequestError: " + e.message)
            }

            if (!success) {
                logger.error("Upload failed: ${config.url}")
                throw IOException("Server request unsuccessful.")
            } else {
                logger.debug("Upload Success: ${config.url}")
            }
        }

        /**
         * Posts @json to @url
         */
        @Throws(IOException::class)
        private fun postJson(url: String, json: JSONObject): Response {
            val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

            return client.newCall(request).execute()
        }
    }
}
