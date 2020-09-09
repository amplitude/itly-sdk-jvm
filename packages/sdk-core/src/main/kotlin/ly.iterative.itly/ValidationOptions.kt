package ly.iterative.itly

val DEFAULT_VALIDATION_OPTIONS = ValidationOptions()

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
) {
    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    // For Java :)
    data class Builder(
        private var disabled: Boolean = DEFAULT_VALIDATION_OPTIONS.disabled,
        private var trackInvalid: Boolean = DEFAULT_VALIDATION_OPTIONS.trackInvalid,
        private var errorOnInvalid: Boolean = DEFAULT_VALIDATION_OPTIONS.errorOnInvalid
    ) {
        fun disabled(disabled: Boolean) = apply { this.disabled = disabled }
        fun trackInvalid(trackInvalid: Boolean) = apply { this.trackInvalid = trackInvalid }
        fun errorOnInvalid(errorOnInvalid: Boolean) = apply { this.errorOnInvalid = errorOnInvalid }

        fun build() = ValidationOptions(
            disabled = disabled,
            trackInvalid = trackInvalid,
            errorOnInvalid = errorOnInvalid
        )
    }
}
