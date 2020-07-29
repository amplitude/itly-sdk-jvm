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
)

