package ly.iterative.itly

import ly.iterative.itly.iteratively.RetryOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

open class IterativelyOptions(
    /**
     * The server endpoint to send messages.
     * @default: https://data.us-east-2.iterative.ly/t
     */
    url: String? = null,

    /**
     * Tracking plan branch name (e.g. feature/demo).
     */
    branch: String? = null,

    /**
     * Tracking plan version number (e.g. 1.0.0).
     */
    version: String? = null,

    /**
     * Remove all property values and validation error details from messages before enqueueing.
     * @default: false
     */
    omitValues: Boolean? = null,

    /**
     * The maximum number of messages grouped together into a single network request.
     * @default: 100
     */
    batchSize: Int? = null,

    /**
     * The number of messages that triggers unconditional queue flushing.
     * It works independently from flushInterval.
     * @default: 10
     */
    flushQueueSize: Long? = null,

    /**
     * Time in milliseconds to wait before flushing the queue.
     * @default: 10000
     */
    flushIntervalMs: Long? = null,

    disabled: Boolean? = null,

    // Java/Android specific
    retryOptions: RetryOptions? = null,
    threadFactory: ThreadFactory? = null,
    networkExecutor: ExecutorService? = null
): ly.iterative.itly.iteratively.IterativelyOptions(
    url = url, branch = branch, version = version, omitValues = omitValues, batchSize = batchSize,
    flushQueueSize = flushQueueSize, flushIntervalMs = flushIntervalMs, disabled = disabled,
    retryOptions = retryOptions, threadFactory = threadFactory, networkExecutor = networkExecutor
) {
    companion object {
        @JvmStatic
        fun builder(): IBuild<IterativelyOptions> {
            return Builder(createInstance = { b: Builder<IterativelyOptions> ->
                IterativelyOptions(b)
            })
        }
    }

    constructor(other: ly.iterative.itly.iteratively.IterativelyOptions) : this() {
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

    override fun copy(overrides: ly.iterative.itly.iteratively.IterativelyOptions): IterativelyOptions {
        return IterativelyOptions(IterativelyOptions(this).applyOverrides(overrides))
    }
}