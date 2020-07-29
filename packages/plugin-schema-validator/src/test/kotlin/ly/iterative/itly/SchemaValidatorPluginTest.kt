package ly.iterative.itly

import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*
import org.junit.jupiter.api.*

lateinit var schemaValidatorPlugin: SchemaValidatorPlugin

val context = Context(
    requiredString = "Required context string"
)

class SchemaValidatorPluginTest {
    @BeforeEach fun beforeEach() {
        val validationOptions = ValidationOptions()

        schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA, validationOptions)

        schemaValidatorPlugin.load(OptionsCore(
            context = context,
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
