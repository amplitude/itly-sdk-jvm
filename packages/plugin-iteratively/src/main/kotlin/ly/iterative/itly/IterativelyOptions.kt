package ly.iterative.itly

import ly.iterative.itly.iteratively.RetryOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

class IterativelyOptions(
    /**
     * The server endpoint to send messages.
     * @default: https://data.us-east-2.iterative.ly/t
     */
    url: String? = DEFAULT.url,

    /**
     * Tracking plan branch name (e.g. feature/demo).
     */
    branch: String? = DEFAULT.branch,

    /**
     * Tracking plan version number (e.g. 1.0.0).
     */
    version: String? = DEFAULT.version,

    /**
     * Remove all property values and validation error details from messages before enqueueing.
     * @default: false
     */
    omitValues: Boolean = DEFAULT.omitValues,

    /**
     * The maximum number of messages grouped together into a single network request.
     * @default: 100
     */
    batchSize: Int = DEFAULT.batchSize,

    /**
     * The number of messages that triggers unconditional queue flushing.
     * It works independently from flushInterval.
     * @default: 10
     */
    flushQueueSize: Long = DEFAULT.flushQueueSize,

    /**
     * Time in milliseconds to wait before flushing the queue.
     * @default: 10000
     */
    flushIntervalMs: Long = DEFAULT.flushIntervalMs,

    disabled: Boolean? = DEFAULT.disabled,

    // Java/Android specific
    retryOptions: RetryOptions = DEFAULT.retryOptions,
    threadFactory: ThreadFactory = DEFAULT.threadFactory,
    networkExecutor: ExecutorService? = DEFAULT.networkExecutor
): ly.iterative.itly.iteratively.IterativelyOptions(
    url, branch, version, omitValues, batchSize, flushQueueSize,
    flushIntervalMs, disabled, retryOptions, threadFactory, networkExecutor
) {
    companion object {
        @JvmStatic
        fun builder(): IBuild<IterativelyOptions> {
            return Builder(createInstance = { b: Builder<IterativelyOptions> ->
                IterativelyOptions(b)
            })
        }
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

    override fun copy(
        url: String?,
        branch: String?,
        version: String?,
        omitValues: Boolean?,
        batchSize: Int?,
        flushQueueSize: Long?,
        flushIntervalMs: Long?,
        disabled: Boolean?,
        threadFactory: ThreadFactory?,
        networkExecutor: ExecutorService?,
        retryOptions: RetryOptions?
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
}