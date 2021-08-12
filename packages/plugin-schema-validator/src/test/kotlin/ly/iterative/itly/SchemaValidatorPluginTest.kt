package ly.iterative.itly

import ly.iterative.itly.core.Itly
import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*
import org.junit.jupiter.api.*
import java.lang.IllegalArgumentException

val user = User.DEFAULT
val invalidEvent = EventMaxIntForTest.INVALID_MAX_VALUE
const val invalidEventExpectedErrorMessage = EventMaxIntForTest.INVALID_MAX_VALUE_ERROR_MESSAGE
val VALIDATION_OPTIONS_ERROR_ON_INVALID = ValidationOptions(
    errorOnInvalid = true
)

fun loadDefaultSchemaValidator(options: Options? = null): SchemaValidatorPlugin {
    val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT)

    val opts = options ?: Options(environment = Environment.PRODUCTION)
    schemaValidatorPlugin.load(PluginLoadOptions(opts))

    return schemaValidatorPlugin
}

class SchemaValidatorPluginTest {
    @Test
    fun validate_contextWithProperties_valid() {
        val validation = loadDefaultSchemaValidator().validate(Context.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_groupWithProperties_valid() {
        val validation = loadDefaultSchemaValidator().validate(Group.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_identifyWithProperties_valid() {
        val validation = loadDefaultSchemaValidator().validate(Identify.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_eventWithAllProperties_valid() {
        val validation = loadDefaultSchemaValidator().validate(EventWithAllProperties.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_eventWithConstTypes_valid() {
        val validation = loadDefaultSchemaValidator().validate(EventWithConstTypes.VALID)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_invalidEvent_notValid() {
        val validation = loadDefaultSchemaValidator().validate(invalidEvent)
        Assertions.assertEquals(validation.valid, false)
        Assertions.assertEquals(invalidEventExpectedErrorMessage, validation.message)
    }

    @Test
    fun validate_invalidEventWithErrorOnInvalid_throwsError() {
        val itly: Itly = TestUtil.getItly(Context.VALID_ALL_PROPS, Options(
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT)),
            validation = VALIDATION_OPTIONS_ERROR_ON_INVALID
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(null, invalidEvent)
        }

        Assertions.assertEquals(invalidEventExpectedErrorMessage, exception.message)
    }

    @Test
    fun itlyLoad_invalidContext_succeeds() {
        Assertions.assertDoesNotThrow {
            TestUtil.getItly(Context.INVALID_NO_PROPS, Options(
                plugins = arrayListOf((SchemaValidatorPlugin(Schemas.DEFAULT))
            )))
        }
    }

    @Test
    fun itlyTrack_invalidContext_throwsError() {
        val itly: Itly = TestUtil.getItly(Context.INVALID_NO_PROPS, Options(
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(user.id, EventNoProperties())
        }

        Assertions.assertEquals(
                Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
                exception.message
        )
    }

    @Test
    fun itlyTrack_invalidEventWithErrorOnInvalid_throwsError() {
        val itly: Itly = TestUtil.getItly(Context.VALID_ALL_PROPS, Options(
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT)),
            validation = VALIDATION_OPTIONS_ERROR_ON_INVALID
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }

        Assertions.assertEquals(
            EventMaxIntForTest.INVALID_MAX_VALUE_ERROR_MESSAGE,
            exception.message
        )
    }

    @Test
    fun itlyTrack_withDefaultValidationInDevelopment_throwsError() {
        val itly: Itly = TestUtil.getItly(Context.INVALID_NO_PROPS, Options(
            environment = Environment.DEVELOPMENT,
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(user.id, EventNoProperties())
        }

        Assertions.assertEquals(
            Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
            exception.message
        )
    }

    @Test
    fun itly_nullContextWithNoContextSchema_succeed() {
        val itly: Itly = TestUtil.getItly(Options(
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.NO_CONTEXT))
        ))

        Assertions.assertDoesNotThrow {
            itly.track(user.id, EventNoProperties())
        }
    }

    @Test
    fun itlyTrack_nullContextWithContextSchema_succeed() {
        val itly: Itly = TestUtil.getItly(Options(
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
        ))

        Assertions.assertDoesNotThrow {
            itly.track(user.id, EventNoProperties())
        }
    }

    @Test
    fun itlyTrack_contextWithNoContextSchema_throwsError() {
        val itly: Itly = TestUtil.getItly(
            Properties(mapOf(
                "prop" to "value"
            )),
            Options(
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.NO_CONTEXT))
            )
        )

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(user.id, EventNoProperties())
        }

        Assertions.assertEquals(
            "No schema found for 'context'. Received context={\"prop\":\"value\"}",
            exception.message
        )
    }
}
