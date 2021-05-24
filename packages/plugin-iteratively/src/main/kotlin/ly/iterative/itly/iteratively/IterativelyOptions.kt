package ly.iterative.itly.iteratively

import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

val DEFAULT_ITERATIVELY_OPTIONS = ly.iterative.itly.iteratively.IterativelyOptions()

data class IterativelyOptions @JvmOverloads constructor(
    /**
     * The server endpoint to send messages.
     * @default: https://data.us-east-2.iterative.ly/t
     */
    val url: String? = null,

    /**
     * Tracking plan branch name (e.g. feature/demo).
     */
    val branch: String? = null,

    /**
     * Tracking plan version number (e.g. 1.0.0).
     */
    val version: String? = null,

    /**
     * Remove all property values and validation error details from messages before enqueueing.
     * @default: false
     */
    val omitValues: Boolean = false,

    /**
     * The maximum number of messages grouped together into a single network request.
     * @default: 100
     */
    val batchSize: Int = 100,

    /**
     * The number of messages that triggers unconditional queue flushing.
     * It works independently from flushInterval.
     * @default: 10
     */
    val flushQueueSize: Long = 10,

    /**
     * Time in milliseconds to wait before flushing the queue.
     * @default: 10000
     */
    val flushIntervalMs: Long = 10000,

    // TODO:
    //  Remove disabled here, use itly.disablePlugin() instead
    //  Do we need to stop anything else on disabled? Threads/scheduled tasks
    val disabled: Boolean? = null,

    // Java/Android specific
    val retryOptions: RetryOptions = RetryOptions(),
    val threadFactory: ThreadFactory = DEFAULT_THREAD_FACTORY,
    val networkExecutor: ExecutorService? = null
) {
    companion object {
        @JvmStatic
        fun builder(): IBuild {
            return Builder()
        }
    }

    constructor(builder: Builder) : this(
        url = builder.url,
        omitValues = builder.omitValues,
        batchSize = builder.batchSize,
        flushQueueSize = builder.flushQueueSize,
        flushIntervalMs = builder.flushIntervalMs,
        disabled = builder.disabled,
        threadFactory = builder.threadFactory,
        networkExecutor = builder.networkExecutor,
        retryOptions = builder.retryOptions
    )

    // For Java :)
    class Builder internal constructor (
        internal var url: String? = DEFAULT_ITERATIVELY_OPTIONS.url,
        internal var omitValues: Boolean = DEFAULT_ITERATIVELY_OPTIONS.omitValues,
        internal var batchSize: Int = DEFAULT_ITERATIVELY_OPTIONS.batchSize,
        internal var flushQueueSize: Long = DEFAULT_ITERATIVELY_OPTIONS.flushQueueSize,
        internal var flushIntervalMs: Long = DEFAULT_ITERATIVELY_OPTIONS.flushIntervalMs,
        internal var disabled: Boolean? = DEFAULT_ITERATIVELY_OPTIONS.disabled,
        internal var threadFactory: ThreadFactory = DEFAULT_ITERATIVELY_OPTIONS.threadFactory,
        internal var networkExecutor: ExecutorService? = DEFAULT_ITERATIVELY_OPTIONS.networkExecutor,
        internal var retryOptions: RetryOptions = DEFAULT_ITERATIVELY_OPTIONS.retryOptions
    ) : IBuild {
        override fun url(url: String) = apply { this.url = url }
        override fun omitValues(omitValues: Boolean) = apply { this.omitValues = omitValues }
        override fun batchSize(batchSize: Int) = apply { this.batchSize = batchSize }
        override fun flushQueueSize(flushQueueSize: Long) = apply { this.flushQueueSize = flushQueueSize }
        override fun flushIntervalMs(flushIntervalMs: Long) = apply { this.flushIntervalMs = flushIntervalMs }
        override fun disabled(disabled: Boolean) = apply { this.disabled = disabled }
        override fun threadFactory(threadFactory: ThreadFactory) = apply { this.threadFactory = threadFactory }
        override fun networkExecutor(networkExecutor: ExecutorService) = apply { this.networkExecutor = networkExecutor }
        override fun retryOptions(retryOptions: RetryOptions) = apply { this.retryOptions = retryOptions }

        override fun build(): IterativelyOptions {
            return IterativelyOptions(this)
        }
    }

    interface IBuild {
        fun url(url: String): IBuild
        fun omitValues(omitValues: Boolean): IBuild
        fun batchSize(batchSize: Int): IBuild
        fun flushQueueSize(flushQueueSize: Long): IBuild
        fun flushIntervalMs(flushIntervalMs: Long): IBuild
        fun disabled(disabled: Boolean): IBuild
        fun threadFactory(threadFactory: ThreadFactory): IBuild
        fun networkExecutor(networkExecutor: ExecutorService): IBuild
        fun retryOptions(retryOptions: RetryOptions): IBuild
        fun build(): IterativelyOptions
    }

    /**
     * For backwards compatibility with codgen for versions <= 1.2.5
     */
    // TODO: Mark Deprecated after dataplane goes GA
    // @Deprecated("Update your source with `itly pull`", ReplaceWith("", ""))
    fun getPluginOptions(url: String): IterativelyOptions {
        return IterativelyOptions(
                url = url,
                omitValues = omitValues,
                batchSize = batchSize,
                flushQueueSize = flushQueueSize,
                flushIntervalMs = flushIntervalMs,
                disabled = disabled,
                threadFactory = threadFactory,
                networkExecutor = networkExecutor,
                retryOptions = retryOptions
        )
    }
}
