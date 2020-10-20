package ly.iterative.itly.iteratively

import ly.iterative.itly.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

data class IterativelyOptions @JvmOverloads constructor(
    val url: String,
    val omitValues: Boolean = false,
    val batchSize: Int = 100,
    val flushQueueSize: Long = 10,
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
        fun builder(): IUrl {
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
        internal var url: String = "",
        internal var omitValues: Boolean = DEFAULT_ITERATIVELY_OPTIONS.omitValues,
        internal var batchSize: Int = DEFAULT_ITERATIVELY_OPTIONS.batchSize,
        internal var flushQueueSize: Long = DEFAULT_ITERATIVELY_OPTIONS.flushQueueSize,
        internal var flushIntervalMs: Long = DEFAULT_ITERATIVELY_OPTIONS.flushIntervalMs,
        internal var disabled: Boolean? = DEFAULT_ITERATIVELY_OPTIONS.disabled,
        internal var threadFactory: ThreadFactory = DEFAULT_ITERATIVELY_OPTIONS.threadFactory,
        internal var networkExecutor: ExecutorService? = DEFAULT_ITERATIVELY_OPTIONS.networkExecutor,
        internal var retryOptions: RetryOptions = DEFAULT_ITERATIVELY_OPTIONS.retryOptions
    ) : IUrl, IBuild {
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

    interface IUrl {
        fun url(url: String): IBuild
    }

    interface IBuild {
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
}
