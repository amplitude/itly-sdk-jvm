package ly.iterative.itly

data class RetryOptions @JvmOverloads constructor(
    val maxRetries: Int = 25, // ~1 day
    val delayInitialSeconds: Long = 10, // 10 seconds
    val delayMaximumSeconds: Long = 1 * 60 * 60 // = 1 hr
)
