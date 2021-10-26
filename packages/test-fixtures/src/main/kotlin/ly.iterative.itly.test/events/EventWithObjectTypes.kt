package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class EventWithObjectTypes(
    requiredObject: Any,
    requiredObjectArray: Array<Any>
) : Event(
    "Event with Object and Object Array",
    mapOf(
        "requiredObject" to requiredObject,
        "requiredObjectArray" to requiredObjectArray
    )
)
