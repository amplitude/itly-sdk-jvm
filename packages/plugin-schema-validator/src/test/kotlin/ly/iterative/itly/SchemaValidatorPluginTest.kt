package ly.iterative.itly

import org.junit.jupiter.api.*

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
    enum class OptionalEnum(val value: String) {
        VALUE_1("Value 1"),
        VALUE_2("Value 2")
    }
}

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

class Identify(
        requiredNumber: Double,
        optionalArray: Array<String>? = null
) : Event(
    "identify",
    mapOf(
        *(if (optionalArray != null) arrayOf("optionalArray" to optionalArray) else arrayOf()),
        "requiredNumber" to requiredNumber
    )
)

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
}

class EventMaxIntForTest(
    intMax10: Int
) : Event(
    name = "EventMaxIntForTest",
    properties = mapOf(
        "intMax10" to intMax10
    )
)

lateinit var schemaValidatorPlugin: SchemaValidatorPlugin

class SchemaValidatorPluginTest {
    @BeforeEach fun beforeEach() {
        val validationOptions = ValidationOptions()

        schemaValidatorPlugin = SchemaValidatorPlugin(mapOf(
            "context" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/context\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Context\",\"description\":\"\",\"type\":\"object\",\"properties\":{\"requiredString\":{\"description\":\"description for context requiredString\",\"type\":\"string\"},\"optionalEnum\":{\"description\":\"description for context optionalEnum\",\"enum\":[\"Value 1\",\"Value 2\"]}},\"additionalProperties\":false,\"required\":[\"requiredString\"]}",
            "group" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/group\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Group\",\"description\":\"\",\"type\":\"object\",\"properties\":{\"requiredBoolean\":{\"description\":\"Description for group requiredBoolean\",\"type\":\"boolean\"},\"optionalString\":{\"description\":\"Description for group optionalString\",\"type\":\"string\"}},\"additionalProperties\":false,\"required\":[\"requiredBoolean\"]}",
            "identify" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/identify\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Identify\",\"description\":\"\",\"type\":\"object\",\"properties\":{\"optionalArray\":{\"description\":\"Description for identify optionalArray\",\"type\":\"array\",\"uniqueItems\":false,\"items\":{\"type\":\"string\"}},\"requiredNumber\":{\"description\":\"Description for identify requiredNumber\",\"type\":\"number\"}},\"additionalProperties\":false,\"required\":[\"requiredNumber\"]}",
            "Event With Optional Properties" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/00b99136-9d1a-48d8-89d5-25f165ff3ae0/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With Optional Properties\",\"description\":\"Event w optional properties description\",\"type\":\"object\",\"properties\":{\"optionalNumber\":{\"description\":\"\",\"type\":\"number\"},\"optionalArrayString\":{\"description\":\"\",\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"optionalArrayNumber\":{\"description\":\"\",\"type\":\"array\",\"items\":{\"type\":\"number\"}},\"optionalString\":{\"description\":\"Optional String property description\",\"type\":\"string\"},\"optionalBoolean\":{\"description\":\"\",\"type\":\"boolean\"}},\"additionalProperties\":false,\"required\":[]}",
            "Event No Properties" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/26af925a-be3a-40e5-947d-33da66a5352f/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event No Properties\",\"description\":\"Event w no properties description\",\"type\":\"object\",\"properties\":{},\"additionalProperties\":false,\"required\":[]}",
            "Event With Optional Array Types" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/2755da0e-a507-4b18-8f17-86d1d5c499ab/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With Optional Array Types\",\"description\":\"Description for event with optional array types\",\"type\":\"object\",\"properties\":{\"optionalStringArray\":{\"description\":\"Description for optional string array\",\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"optionalJSONArray\":{\"description\":\"Description for optional object array\",\"type\":\"array\",\"items\":{\"type\":\"object\"}},\"optionalBooleanArray\":{\"description\":\"Description for optional boolean array\",\"type\":\"array\",\"items\":{\"type\":\"boolean\"}},\"optionalNumberArray\":{\"description\":\"Description for optional number array\",\"type\":\"array\",\"items\":{\"type\":\"number\"}}},\"additionalProperties\":false,\"required\":[]}",
            "Event With All Properties" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/311ba144-8532-4474-a9bd-8b430625e29a/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With All Properties\",\"description\":\"Event w all properties description\",\"type\":\"object\",\"properties\":{\"requiredConst\":{\"description\":\"Event 2 Property - Const\",\"const\":\"some-const-value\"},\"requiredInteger\":{\"description\":\"Event 2 Property - Integer    *     * Examples:    * 5, 4, 3\",\"type\":\"integer\"},\"optionalString\":{\"description\":\"Event 2 Property - Optional String    *     * Examples:    * Some string, or another\",\"type\":\"string\"},\"requiredNumber\":{\"description\":\"Event 2 Property - Number\",\"type\":\"number\"},\"requiredString\":{\"description\":\"Event 2 Property - String\",\"type\":\"string\"},\"requiredArray\":{\"description\":\"Event 2 Property - Array\",\"type\":\"array\",\"minItems\":0,\"items\":{\"type\":\"string\"}},\"requiredEnum\":{\"description\":\"Event 2 Property - Enum\",\"enum\":[\"Enum1\",\"Enum2\"]},\"requiredBoolean\":{\"description\":\"Event 2 Property - Boolean\",\"type\":\"boolean\"}},\"additionalProperties\":false,\"required\":[\"requiredConst\",\"requiredInteger\",\"requiredNumber\",\"requiredString\",\"requiredArray\",\"requiredEnum\",\"requiredBoolean\"]}",
            "Event With Const Types" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/321b8f02-1bb3-4b33-8c21-8c55401d62da/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With Const Types\",\"description\":\"Description for event with const types\",\"type\":\"object\",\"properties\":{\"Integer Const\":{\"description\":\"Description for integer const\",\"const\":10},\"Boolean Const\":{\"description\":\"Description for boolean const type\",\"const\":true},\"String Int Const\":{\"description\":\"Description for string int const\",\"const\":0},\"Number Const\":{\"description\":\"Description for number const\",\"const\":2.2},\"String Const WIth Quotes\":{\"description\":\"Description for Int With Quotes\",\"const\":\"\\\"String \\\"Const With\\\" Quotes\\\"\"},\"String Const\":{\"description\":\"Description for string const\",\"const\":\"String-Constant\"}},\"additionalProperties\":false,\"required\":[\"Integer Const\",\"Boolean Const\",\"String Int Const\",\"Number Const\",\"String Const WIth Quotes\",\"String Const\"]}",
            "Event With Array Types" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/5ded19cd-6015-441b-a2be-f954425be1fe/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With Array Types\",\"description\":\"Description for event with Array Types\",\"type\":\"object\",\"properties\":{\"requiredBooleanArray\":{\"description\":\"description for required boolean array\",\"type\":\"array\",\"items\":{\"type\":\"boolean\"}},\"requiredStringArray\":{\"description\":\"description for required string array\",\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"requiredObjectArray\":{\"description\":\"Description for required object array\",\"type\":\"array\",\"items\":{\"type\":\"object\"}},\"requiredNumberArray\":{\"description\":\"Description for required number array\",\"type\":\"array\",\"items\":{\"type\":\"number\"}}},\"additionalProperties\":false,\"required\":[\"requiredBooleanArray\",\"requiredStringArray\",\"requiredObjectArray\",\"requiredNumberArray\"]}",
            "EventMaxIntForTest" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/aa0f08ac-8928-4569-a524-c1699e7da6f4/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"EventMaxIntForTest\",\"description\":\"Event to test schema validation\",\"type\":\"object\",\"properties\":{\"intMax10\":{\"description\":\"property to test schema validation\",\"type\":\"integer\",\"maximum\":10}},\"additionalProperties\":false,\"required\":[\"intMax10\"]}",
            "Event with Object and Object Array" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/aea72ecc-5a10-4bd7-99a6-81a464aabaed/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event Object Types\",\"description\":\"Event with Object and Object Array\",\"type\":\"object\",\"properties\":{\"requiredObject\":{\"description\":\"Property Object Type\",\"type\":\"object\"},\"requiredObjectArray\":{\"description\":\"Property Object Array Type\",\"type\":\"array\",\"items\":{\"type\":\"object\"}}},\"additionalProperties\":false,\"required\":[\"requiredObject\",\"requiredObjectArray\"]}",
            "Event With Enum Types" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/b4fc8366-b05d-40d3-b698-79795701624b/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Event With Enum Types\",\"description\":\"Description for event with enum types\",\"type\":\"object\",\"properties\":{\"required enum\":{\"description\":\"Description for optional enum\",\"enum\":[\"required enum 1\",\"required enum 2\"]},\"optional enum\":{\"description\":\"Description for required enum\",\"enum\":[\"optional enum 1\",\"optional enum 2\"]}},\"additionalProperties\":false,\"required\":[\"required enum\"]}",
            "event withDifferent_CasingTypes" to "{\"\$id\":\"https://iterative.ly/company/77b37977-cb3a-42eb-bce3-09f5f7c3adb7/event/fcb3d82d-208f-4bc2-b8e1-843683d9b595/version/1.0.0\",\"\$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"event withDifferent_CasingTypes\",\"description\":\"Description for case with space\",\"type\":\"object\",\"properties\":{\"EnumPascalCase\":{\"description\":\"DescirptionForEnumPascalCase\",\"enum\":[\"EnumPascalCase\"]},\"property with space\":{\"description\":\"Description for case with space\",\"type\":\"string\"},\"enum with space\":{\"description\":\"Description for enum with space\",\"enum\":[\"enum with space\"]},\"enum_snake_case\":{\"description\":\"description_for_enum_snake_case\",\"enum\":[\"enum_snake_case\"]},\"propertyWithCamelCase\":{\"description\":\"descriptionForCamelCase\",\"type\":\"string\"},\"PropertyWithPascalCase\":{\"description\":\"DescriptionForPascalCase\",\"type\":\"string\"},\"property_with_snake_case\":{\"description\":\"Description_for_snake_case\",\"type\":\"string\"},\"enumCamelCase\":{\"description\":\"descriptionForEnumCamelCase\",\"enum\":[\"enumCamelCase\"]}},\"additionalProperties\":false,\"required\":[\"EnumPascalCase\",\"property with space\",\"enum with space\",\"enum_snake_case\",\"propertyWithCamelCase\",\"PropertyWithPascalCase\",\"property_with_snake_case\",\"enumCamelCase\"]}"
        ), validationOptions)

        schemaValidatorPlugin.load(OptionsCore(
            context = Context(
                requiredString = "Required context string"
            ),
            validationOptions = validationOptions
        ))
    }

    @Test
    fun validate_ContextWithProperties_valid() {
        val validation = schemaValidatorPlugin.validate(Context(
            requiredString = "Required context string."
        ))
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_GroupWithProperties_valid() {
        val validation = schemaValidatorPlugin.validate(Group(
            requiredBoolean = false,
            optionalString = "I'm optional!"
        ))
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_IdentifyWithProperties_valid() {
        val validation = schemaValidatorPlugin.validate(Identify(
            requiredNumber = 2.0,
            optionalArray = arrayOf("optional")
        ))
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_EventWithAllProperties_valid() {
        val event = EventWithAllProperties(
            requiredArray = arrayOf("required", "strings"),
            requiredBoolean = true,
            requiredEnum = EventWithAllProperties.RequiredEnum.ENUM_1,
            requiredInteger = 42,
            requiredNumber = 2.0,
            requiredString = "don't forget this. it's required."
        )
        val validation = schemaValidatorPlugin.validate(event);
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_EventMaxIntForTest_notValid() {
        val event = EventMaxIntForTest(
            intMax10 = 20
        )
        val validation = schemaValidatorPlugin.validate(event)
        Assertions.assertEquals(validation.valid, false)
        Assertions.assertEquals(
            validation.message,
            "(Itly) Error validating event EventMaxIntForTest " +
            "(\$.intMax10: must have a maximum value of 10)."
        )
    }
}
