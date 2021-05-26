package ly.iterative.itly.iteratively

import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

//interface IIterativelyOptions {
//    val url: String?
//    val branch: String?
//    val version: String?
//    val omitValues: Boolean
//    val batchSize: Int
//    val flushQueueSize: Long
//    val flushIntervalMs: Long
//    val disabled: Boolean?
//    val retryOptions: RetryOptions
//    val threadFactory: ThreadFactory
//    val networkExecutor: ExecutorService?
//    fun copy(
//        url: String? = null,
//        branch: String? = null,
//        version: String? = null,
//        omitValues: Boolean? = null,
//        batchSize: Int? = null,
//        flushQueueSize: Long? = null,
//        flushIntervalMs: Long? = null,
//        disabled: Boolean? = null,
//        threadFactory: ThreadFactory? = null,
//        networkExecutor: ExecutorService? = null,
//        retryOptions: RetryOptions? = null
//    ): IIterativelyOptions
//}

//data class IterativelyOptionsOverrides(
//    val url: String? = null,
//    val branch: String? = null,
//    val version: String? = null,
//    val omitValues: Boolean? = null,
//    val batchSize: Int? = null,
//    val flushQueueSize: Long? = null,
//    val flushIntervalMs: Long? = null,
//    val disabled: Boolean? = null,
//    val threadFactory: ThreadFactory? = null,
//    val networkExecutor: ExecutorService? = null,
//    val retryOptions: RetryOptions? = null
//)

open class IterativelyOptions @JvmOverloads constructor(
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
        fun builder(): IBuild<IterativelyOptions> {
            return Builder(createInstance = { b: Builder<IterativelyOptions> ->
                IterativelyOptions(b)
            })
        }

        @JvmField
        val DEFAULT = IterativelyOptions()
    }

    constructor(builder: Builder<IterativelyOptions>) : this(
        url = builder.url,
        branch = builder.branch,
        version = builder.version,
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
    class Builder<T : IterativelyOptions> internal constructor (
        internal val createInstance: (builder: Builder<T>) -> T,
        internal var url: String? = DEFAULT.url,
        internal var branch: String? = DEFAULT.branch,
        internal var version: String? = DEFAULT.version,
        internal var omitValues: Boolean = DEFAULT.omitValues,
        internal var batchSize: Int = DEFAULT.batchSize,
        internal var flushQueueSize: Long = DEFAULT.flushQueueSize,
        internal var flushIntervalMs: Long = DEFAULT.flushIntervalMs,
        internal var disabled: Boolean? = DEFAULT.disabled,
        internal var threadFactory: ThreadFactory = DEFAULT.threadFactory,
        internal var networkExecutor: ExecutorService? = DEFAULT.networkExecutor,
        internal var retryOptions: RetryOptions = DEFAULT.retryOptions
    ) : IBuild<T> {
//        override fun copy(options: IterativelyOptions) = apply {
//            this.url = url
//            this.branch = branch
//            this.version = version
//            this.omitValues = omitValues
//            this.batchSize = batchSize
//            this.flushQueueSize = flushQueueSize
//            this.flushIntervalMs = flushIntervalMs
//            this.disabled = disabled
//            this.threadFactory = threadFactory
//            this.networkExecutor = networkExecutor
//            this.retryOptions = retryOptions
//        }
        override fun url(url: String) = apply { this.url = url }
        override fun branch(branch: String) = apply { this.branch = branch }
        override fun version(version: String) = apply { this.version = version }
        override fun omitValues(omitValues: Boolean) = apply { this.omitValues = omitValues }
        override fun batchSize(batchSize: Int) = apply { this.batchSize = batchSize }
        override fun flushQueueSize(flushQueueSize: Long) = apply { this.flushQueueSize = flushQueueSize }
        override fun flushIntervalMs(flushIntervalMs: Long) = apply { this.flushIntervalMs = flushIntervalMs }
        override fun disabled(disabled: Boolean) = apply { this.disabled = disabled }
        override fun threadFactory(threadFactory: ThreadFactory) = apply { this.threadFactory = threadFactory }
        override fun networkExecutor(networkExecutor: ExecutorService) = apply { this.networkExecutor = networkExecutor }
        override fun retryOptions(retryOptions: RetryOptions) = apply { this.retryOptions = retryOptions }

        override fun build(): T {
            return createInstance(this)
        }
    }

    interface IBuild<T : IterativelyOptions> {
//        fun copy(options: IterativelyOptions): IBuild<T>
        fun url(url: String): IBuild<T>
        fun branch(branch: String): IBuild<T>
        fun version(version: String): IBuild<T>
        fun omitValues(omitValues: Boolean): IBuild<T>
        fun batchSize(batchSize: Int): IBuild<T>
        fun flushQueueSize(flushQueueSize: Long): IBuild<T>
        fun flushIntervalMs(flushIntervalMs: Long): IBuild<T>
        fun disabled(disabled: Boolean): IBuild<T>
        fun threadFactory(threadFactory: ThreadFactory): IBuild<T>
        fun networkExecutor(networkExecutor: ExecutorService): IBuild<T>
        fun retryOptions(retryOptions: RetryOptions): IBuild<T>
        fun build(): T
    }

    /**
     * For backwards compatibility with codgen for versions <= 1.2.5
     */
    // TODO: Mark Deprecated after dataplane goes GA
    // @Deprecated("Update your source with `itly pull`", ReplaceWith("", ""))
    fun getPluginOptions(url: String): IterativelyOptions {
        return IterativelyOptions(
            url = url,
            branch = branch,
            version = version,
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

    open fun copy(
        url: String? = null,
        branch: String? = null,
        version: String? = null,
        omitValues: Boolean? = null,
        batchSize: Int? = null,
        flushQueueSize: Long? = null,
        flushIntervalMs: Long? = null,
        disabled: Boolean? = null,
        threadFactory: ThreadFactory? = null,
        networkExecutor: ExecutorService? = null,
        retryOptions: RetryOptions? = null
    ): IterativelyOptions {
        return IterativelyOptions(
            url = url ?: this.url,
            branch = branch ?: this.branch,
            version = version ?: this.version,
            omitValues = omitValues ?: this.omitValues,
            batchSize = batchSize ?: this.batchSize,
            flushQueueSize = flushQueueSize ?: this.flushQueueSize,
            flushIntervalMs = flushIntervalMs  ?: this.flushIntervalMs,
            disabled = disabled  ?: this.disabled,
            threadFactory = threadFactory ?: this.threadFactory,
            networkExecutor = networkExecutor ?: this.networkExecutor,
            retryOptions = retryOptions ?: this.retryOptions
        )
    }

//    open fun copy(overrides: IterativelyOptionsOverrides = IterativelyOptionsOverrides()): IterativelyOptions {
//        return IterativelyOptions(
//            url = overrides.url ?: this.url,
//            branch = overrides.branch ?: this.branch,
//            version = overrides.version ?: this.version,
//            omitValues = overrides.omitValues ?: this.omitValues,
//            batchSize = overrides.batchSize ?: this.batchSize,
//            flushQueueSize = overrides.flushQueueSize ?: this.flushQueueSize,
//            flushIntervalMs = overrides.flushIntervalMs  ?: this.flushIntervalMs,
//            disabled = overrides.disabled  ?: this.disabled,
//            threadFactory = overrides.threadFactory ?: this.threadFactory,
//            networkExecutor = overrides.networkExecutor ?: this.networkExecutor,
//            retryOptions = overrides.retryOptions ?: this.retryOptions
//        )
//    }
}
