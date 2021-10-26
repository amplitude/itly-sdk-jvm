package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class EventWithArrayTypes(
    requiredBooleanArray:Array<Boolean>,
    requiredNumberArray:Array<Double>,
    requiredObjectArray:Array<Any>,
    requiredStringArray:Array<String>
) : Event(
"Event With Array Types",
    mapOf(
        "requiredBooleanArray" to requiredBooleanArray,
        "requiredNumberArray" to requiredNumberArray,
        "requiredObjectArray" to requiredObjectArray,
        "requiredStringArray" to requiredStringArray
    )
)
