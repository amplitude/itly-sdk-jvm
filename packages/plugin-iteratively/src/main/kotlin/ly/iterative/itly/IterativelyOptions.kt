package ly.iterative.itly

import ly.iterative.itly.iteratively.RetryOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

val DEFAULT_ITERATIVELY_OPTIONS = ly.iterative.itly.iteratively.IterativelyOptions("")

/**
 * User configurable options for IterativelyPlugin
 */
data class IterativelyOptions(
    val omitValues: Boolean = DEFAULT_ITERATIVELY_OPTIONS.omitValues,
    val batchSize: Int = DEFAULT_ITERATIVELY_OPTIONS.batchSize,
    val flushQueueSize: Long = DEFAULT_ITERATIVELY_OPTIONS.flushQueueSize,
    val flushIntervalMs: Long = DEFAULT_ITERATIVELY_OPTIONS.flushIntervalMs,
    val disabled: Boolean = DEFAULT_ITERATIVELY_OPTIONS.disabled,
    val threadFactory: ThreadFactory = DEFAULT_ITERATIVELY_OPTIONS.threadFactory,
    val networkExecutor: ExecutorService? = DEFAULT_ITERATIVELY_OPTIONS.networkExecutor,
    val retryOptions: RetryOptions = DEFAULT_ITERATIVELY_OPTIONS.retryOptions
) {
    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    // For Java :)
    data class Builder(
        private var omitValues: Boolean = DEFAULT_ITERATIVELY_OPTIONS.omitValues,
        private var batchSize: Int = DEFAULT_ITERATIVELY_OPTIONS.batchSize,
        private var flushQueueSize: Long = DEFAULT_ITERATIVELY_OPTIONS.flushQueueSize,
        private var flushIntervalMs: Long = DEFAULT_ITERATIVELY_OPTIONS.flushIntervalMs,
        private var disabled: Boolean = DEFAULT_ITERATIVELY_OPTIONS.disabled,
        private var threadFactory: ThreadFactory = DEFAULT_ITERATIVELY_OPTIONS.threadFactory,
        private var networkExecutor: ExecutorService? = DEFAULT_ITERATIVELY_OPTIONS.networkExecutor,
        private var retryOptions: RetryOptions = DEFAULT_ITERATIVELY_OPTIONS.retryOptions
    ) {

        fun omitValues(omitValues: Boolean) = apply { this.omitValues = omitValues }
        fun batchSize(batchSize: Int) = apply { this.batchSize = batchSize }
        fun flushQueueSize(flushQueueSize: Long) = apply { this.flushQueueSize = flushQueueSize }
        fun flushIntervalMs(flushIntervalMs: Long) = apply { this.flushIntervalMs = flushIntervalMs }
        fun disabled(disabled: Boolean) = apply { this.disabled = disabled }
        fun threadFactory(threadFactory: ThreadFactory) = apply { this.threadFactory = threadFactory }
        fun networkExecutor(networkExecutor: ExecutorService) = apply { this.networkExecutor = networkExecutor }
        fun retryOptions(retryOptions: RetryOptions) = apply { this.retryOptions = retryOptions }

        fun build() = IterativelyOptions(
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
