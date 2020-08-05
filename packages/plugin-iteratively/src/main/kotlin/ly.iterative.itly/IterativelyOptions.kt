package ly.iterative.itly

import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory

data class IterativelyOptions @JvmOverloads constructor(
    val url: String,
    val environment: Environment = Environment.DEVELOPMENT,
    val omitValues: Boolean = false,
    val batchSize: Int = 100,
    val flushQueueSize: Long = 10,
    val flushIntervalMs: Long = 100,

    // TODO:
    //  Remove disabled here, use itly.disablePlugin() instead
    //  Do we need to stop anything else on disabled? Threads/scheduled tasks
    val disabled: Boolean = false,

    // Java/Android specific
    val threadFactory: ThreadFactory = DEFAULT_THREAD_FACTORY,
    val networkExecutor: ExecutorService = newDefaultExecutorService(threadFactory),
    val retryOptions: RetryOptions = RetryOptions()
)
