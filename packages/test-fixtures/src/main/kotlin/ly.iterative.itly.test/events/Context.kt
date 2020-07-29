package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class Context(
    requiredString: String,
    optionalEnum: Context.OptionalEnum? = null
) : Event(
    "context",
    mapOf(
        *(if (optionalEnum != null) arrayOf("optionalEnum" to optionalEnum.value) else arrayOf()),
        "requiredString" to requiredString
    )
) {
    companion object {
        val VALID_ONLY_REQUIRED_PROPS = Context(
            requiredString = "Required context string"
        )

        val VALID_ALL_PROPS = Context(
            requiredString = "Required context string",
            optionalEnum = OptionalEnum.VALUE_1
        )
    }
    enum class OptionalEnum(val value: String) {
        VALUE_1("Value 1"),
        VALUE_2("Value 2")
    }
}
