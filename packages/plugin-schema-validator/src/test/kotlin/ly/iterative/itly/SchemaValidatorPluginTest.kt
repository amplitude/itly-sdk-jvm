package ly.iterative.itly

import ly.iterative.itly.core.Itly
import ly.iterative.itly.core.Options
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

    val opts = options ?: Options(
        environment = Environment.PRODUCTION,
        context = Context.VALID_ONLY_REQUIRED_PROPS
    )
    schemaValidatorPlugin.load(opts)

    return schemaValidatorPlugin
}

class SchemaValidatorPluginTest {
    @Test
    fun validate_contextWithProperties_valid() {
        val context = loadDefaultSchemaValidator().process(Context.VALID_ALL_PROPS)
        Assertions.assertEquals(context.metadata.itly.validation.valid, true)
    }

    @Test
    fun validate_groupWithProperties_valid() {
        val group = loadDefaultSchemaValidator().process(Group.VALID_ALL_PROPS)
        Assertions.assertEquals(group.metadata.itly.validation.valid, true)
    }

    @Test
    fun validate_identifyWithProperties_valid() {
        val identify = loadDefaultSchemaValidator().process(Identify.VALID_ALL_PROPS)
        Assertions.assertEquals(identify.metadata.itly.validation.valid, true)
    }

    @Test
    fun validate_eventWithAllProperties_valid() {
        val event = loadDefaultSchemaValidator().process(EventWithAllProperties.VALID_ALL_PROPS)
        Assertions.assertEquals(event.metadata.itly.validation.valid, true)
    }

    @Test
    fun validate_eventWithConstTypes_valid() {
        val event = loadDefaultSchemaValidator().process(EventWithConstTypes.VALID);
        Assertions.assertEquals(event.metadata.itly.validation.valid, true)
    }

    @Test
    fun validate_invalidEvent_notValid() {
        val event = loadDefaultSchemaValidator().process(invalidEvent)
        Assertions.assertEquals(event.metadata.itly.validation.valid, false)
        Assertions.assertEquals(invalidEventExpectedErrorMessage, event.metadata.itly.validation.message)
    }

    @Test
    fun validate_invalidEventWithErrorOnInvalid_throwsError() {
        val itly: Itly = TestUtil.getItly(Options(
            context = Context.VALID_ALL_PROPS,
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT)),
            validation = VALIDATION_OPTIONS_ERROR_ON_INVALID
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(null, invalidEvent)
        }

        Assertions.assertEquals(invalidEventExpectedErrorMessage, exception.message)
    }

    @Test
    fun itlyLoad_invalidContext_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(Options(
                context = Context.INVALID_NO_PROPS,
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
            ))
        }

        Assertions.assertEquals(
            Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
            exception.message
        )
    }

    @Test
    fun itlyTrack_invalidEventWithErrorOnInvalid_throwsError() {
        val itly: Itly = TestUtil.getItly(Options(
            context = Context.VALID_ALL_PROPS,
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
    fun itly_withDefaultValidation_throwsErrorInDevelopment() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(Options(
                environment = Environment.DEVELOPMENT,
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
            ))
        }

        Assertions.assertEquals(
            Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
            exception.message
        )
    }

    @Test
    fun itly_nullContextWithNoContextSchema_succeed() {
        Assertions.assertDoesNotThrow {
            TestUtil.getItly(Options(
                context = null,
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.NO_CONTEXT))
            ))
        }
    }

    @Test
    fun itly_nullContextWithContextSchema_throwsError() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(Options(
                context = null,
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
            ))
        }
    }


    @Test
    fun itly_contextWithNoContextSchema_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(Options(
                context = Properties(mapOf(
                    "prop" to "value"
                )),
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.NO_CONTEXT))
            ))
        }

        Assertions.assertEquals(
            "No schema found for 'context'. Received context={\"prop\":\"value\"}",
            exception.message
        )
    }
}
