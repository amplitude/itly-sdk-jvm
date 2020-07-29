package ly.iterative.itly

import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*
import org.junit.jupiter.api.*
import java.lang.IllegalArgumentException

val context = Context(
    requiredString = "Required context string"
)

fun getSchemaValidator(
    validationOptions: ValidationOptions = ValidationOptions()
): SchemaValidatorPlugin {
    val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA, validationOptions)

    schemaValidatorPlugin.load(OptionsCore(
        context = context,
        validationOptions = validationOptions
    ))

    return schemaValidatorPlugin
}

val invalidEvent = EventMaxIntForTest(
    intMax10 = 20
)
const val invalidEventExpectedErrorMessage = "(Itly) Error validating event EventMaxIntForTest (\$.intMax10: must have a maximum value of 10)."

class SchemaValidatorPluginTest {
    @Test
    fun validate_ContextWithProperties_valid() {
        val validation = getSchemaValidator().validate(Context(
            requiredString = "Required context string."
        ))
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_GroupWithProperties_valid() {
        val validation = getSchemaValidator().validate(Group(
            requiredBoolean = false,
            optionalString = "I'm optional!"
        ))
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_IdentifyWithProperties_valid() {
        val validation = getSchemaValidator().validate(Identify(
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
        val validation = getSchemaValidator().validate(event);
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_InvalidEvent_notValid() {
        val validation = getSchemaValidator().validate(invalidEvent)
        Assertions.assertEquals(validation.valid, false)
        Assertions.assertEquals(invalidEventExpectedErrorMessage, validation.message)
    }

    @Test
    fun validate_InvalidEventWithErrorOnInvalid_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            getSchemaValidator(ValidationOptions(
                errorOnInvalid = true
            )).validate(invalidEvent)
        }

        Assertions.assertEquals(invalidEventExpectedErrorMessage, exception.message)
    }
}
