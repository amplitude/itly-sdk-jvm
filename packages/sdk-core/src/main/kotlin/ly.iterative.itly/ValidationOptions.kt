package ly.iterative.itly

val DEFAULT_VALIDATION_OPTIONS = ValidationOptions()

data class ValidationOptions @JvmOverloads constructor(
    val disabled: Boolean = false,
    val trackInvalid: Boolean = false,
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
