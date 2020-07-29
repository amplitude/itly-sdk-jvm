package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class Group(
    requiredBoolean: Boolean,
    optionalString: String? = null
) : Event(
    "group",
    mapOf(
        *(if (optionalString != null) arrayOf("optionalString" to optionalString) else arrayOf()),
        "requiredBoolean" to requiredBoolean
    )
) {
    companion object {
        val VALID_ALL_PROPS = Group(
                requiredBoolean = false,
                optionalString = "I'm optional!"
        )
    }
}

