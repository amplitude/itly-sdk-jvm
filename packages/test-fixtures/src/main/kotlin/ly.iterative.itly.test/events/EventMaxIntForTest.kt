package ly.iterative.itly.test.events

import ly.iterative.itly.Event

class EventMaxIntForTest(
    intMax10: Int
) : Event(
    name = "EventMaxIntForTest",
    properties = mapOf(
        "intMax10" to intMax10
    )
) {
    companion object {
        val VALID = EventMaxIntForTest(
            intMax10 = 5
        )

        val INVALID_MAX_VALUE = EventMaxIntForTest(
            intMax10 = 20
        )

        const val INVALID_MAX_VALUE_ERROR_MESSAGE = "Error validating 'EventMaxIntForTest'. \$.intMax10: must have a maximum value of 10."
    }
}
