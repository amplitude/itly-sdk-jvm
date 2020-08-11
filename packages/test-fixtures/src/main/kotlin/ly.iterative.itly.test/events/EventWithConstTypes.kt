package ly.iterative.itly.test.events

import ly.iterative.itly.Event

/**
 * Description for event with const types
 *
 * Owner: Test codegen
 */
class EventWithConstTypes() : Event(
    name = "Event With Const Types",
    id = "https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/321b8f02-1bb3-4b33-8c21-8c55401d62da/version/1.0.0",
    properties = mapOf(
        "Boolean Const" to true,
        "Integer Const" to 10,
        "Number Const" to 2.2,
        "String Const" to "String-Constant",
        "String Const WIth Quotes" to "\"String \"Const With\" Quotes\"",
        "String Int Const" to 0
    )
) {
    companion object {
        val VALID = EventWithConstTypes()
    }
}
