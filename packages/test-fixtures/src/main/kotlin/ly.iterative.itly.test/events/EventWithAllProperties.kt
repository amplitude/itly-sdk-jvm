package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class EventWithAllProperties(
    requiredArray: Array<String>,
    requiredBoolean: Boolean,
    requiredEnum: EventWithAllProperties.RequiredEnum,
    requiredInteger: Int,
    requiredNumber: Double,
    requiredString: String,
    optionalString: String? = null
) : Event(
    "Event With All Properties",
    mapOf(
        *(if (optionalString != null) arrayOf("optionalString" to optionalString) else arrayOf()),
        "requiredArray" to requiredArray,
        "requiredBoolean" to requiredBoolean,
        "requiredConst" to "some-const-value",
        "requiredEnum" to requiredEnum.value,
        "requiredInteger" to requiredInteger,
        "requiredNumber" to requiredNumber,
        "requiredString" to requiredString
    )
) {
    enum class RequiredEnum(val value: String) {
        ENUM_1("Enum1"),
        ENUM_2("Enum2")
    }

    companion object {
        val VALID_ALL_PROPS = EventWithAllProperties(
            requiredArray = arrayOf("required", "strings"),
            requiredBoolean = true,
            requiredEnum = EventWithAllProperties.RequiredEnum.ENUM_1,
            requiredInteger = 42,
            requiredNumber = 2.0,
            requiredString = "don't forget this. it's required."
        )
    }
}
