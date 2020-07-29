package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class EventMaxIntForTest(
    intMax10: Int
) : Event(
    name = "EventMaxIntForTest",
    properties = mapOf(
            "intMax10" to intMax10
    )
)
