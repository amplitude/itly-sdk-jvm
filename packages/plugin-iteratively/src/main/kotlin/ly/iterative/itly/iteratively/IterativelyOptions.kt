package ly.iterative.itly.iteratively

import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

open class IterativelyOptions @JvmOverloads constructor(
    /**
     * The server endpoint to send messages.
     * @default: https://data.us-east-2.iterative.ly/t
     */
    open var url: String? = null,

    /**
     * Tracking plan branch name (e.g. feature/demo).
     */
    open var branch: String? = null,

    /**
     * Tracking plan version number (e.g. 1.0.0).
     */
    open var version: String? = null,

    /**
     * Remove all property values and validation error details from messages before enqueueing.
     * @default: false
     */
    open var omitValues: Boolean? = null,

    /**
     * The maximum number of messages grouped together into a single network request.
     * @default: 100
     */
    open var batchSize: Int? = null,

    /**
     * The number of messages that triggers unconditional queue flushing.
     * It works independently from flushInterval.
     * @default: 10
     */
    open var flushQueueSize: Long? = null,

    /**
     * Time in milliseconds to wait before flushing the queue.
     * @default: 10000
     */
    open var flushIntervalMs: Long? = null,

    // TODO:
    //  Remove disabled here, use itly.disablePlugin() instead
    //  Do we need to stop anything else on disabled? Threads/scheduled tasks
    open var disabled: Boolean? = null,

    // Java/Android specific
    open var retryOptions: RetryOptions? = null,
    open var threadFactory: ThreadFactory? = null,
    open var networkExecutor: ExecutorService? = null
) {
    companion object {
        @JvmStatic
        fun builder(): IBuild<IterativelyOptions> {
            return Builder(createInstance = { b: Builder<IterativelyOptions> ->
                IterativelyOptions(b)
            })
        }
    }

    constructor(other: IterativelyOptions) : this() {
        applyOverrides(other)
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
        internal var url: String? = null,
        internal var branch: String? = null,
        internal var version: String? = null,
        internal var omitValues: Boolean? = null,
        internal var batchSize: Int? = null,
        internal var flushQueueSize: Long? = null,
        internal var flushIntervalMs: Long? = null,
        internal var disabled: Boolean? = null,
        internal var threadFactory: ThreadFactory? = null,
        internal var networkExecutor: ExecutorService? = null,
        internal var retryOptions: RetryOptions? = null
    ) : IBuild<T> {
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
        return this.copy(IterativelyOptions(url = url))
    }

    /**
     * Returns a copy of this IterativelyOptions with @overrides
     */
    open fun copy(overrides: IterativelyOptions = IterativelyOptions()): IterativelyOptions {
        return IterativelyOptions(this).applyOverrides(overrides)
    }

    /**
     * Applies @overrides to this object
     */
    protected fun applyOverrides(overrides: IterativelyOptions): IterativelyOptions {
        url = overrides.url ?: url
        branch = overrides.branch ?: branch
        version = overrides.version ?: version
        omitValues = overrides.omitValues ?: omitValues
        batchSize = overrides.batchSize ?: batchSize
        flushQueueSize = overrides.flushQueueSize ?: flushQueueSize
        flushIntervalMs = overrides.flushIntervalMs  ?: flushIntervalMs
        disabled = overrides.disabled  ?: disabled
        threadFactory = overrides.threadFactory ?: threadFactory
        networkExecutor = overrides.networkExecutor ?: networkExecutor
        retryOptions = overrides.retryOptions ?: retryOptions

        return this
    }
}
