package ly.iterative.itly.iteratively

const val MS_IN_S: Long = 1000
const val S_IN_M: Long = 60
const val M_IN_H: Long = 60

data class RetryOptions @JvmOverloads constructor(
    val maxRetries: Int = 25, // ~1 day
    val delayInitialMillis: Long = 10 * MS_IN_S, // 10 seconds
    val delayMaximumMillis: Long = 1 * MS_IN_S * S_IN_M * M_IN_H // = 1 hr
)
