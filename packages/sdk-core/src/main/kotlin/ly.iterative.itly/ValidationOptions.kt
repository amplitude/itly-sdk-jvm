package ly.iterative.itly

data class ValidationOptions @JvmOverloads constructor(
    /**
     * Whether to disable data validation. Default is always false.
     */
    val disabled: Boolean = false,

    /**
     * Whether to allow tracking of invalid data (e.g. events, user traits). Default is true.
     * Note: default is false if you do not explicitly provide ValidationOptions and environment is not production.
     */
    val trackInvalid: Boolean = true,

    /**
     * Whether to throw an exception on invalid data (e.g. events, user traits). Default is false.
     * Note: default is true if you do not explicitly provide ValidationOptions and environment is not production.
     */
    val errorOnInvalid: Boolean = false
)
