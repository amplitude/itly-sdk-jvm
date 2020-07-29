package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class Identify(
    requiredNumber: Double,
    optionalArray: Array<String>? = null
) : Event(
    "identify",
    mapOf(
        *(if (optionalArray != null) arrayOf("optionalArray" to optionalArray) else arrayOf()),
        "requiredNumber" to requiredNumber
    )
) {
    companion object {
        val VALID_ALL_PROPS = Identify(
            requiredNumber = 2.0,
            optionalArray = arrayOf("optional")
        )
    }
}
