package ly.iterative.itly

data class Validation @JvmOverloads constructor(
    var valid: Boolean,
    var message: String? = null
)
