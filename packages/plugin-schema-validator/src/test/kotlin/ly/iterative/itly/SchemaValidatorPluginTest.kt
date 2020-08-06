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
val OPTIONS_ERROR_ON_INVALID = ValidationOptions(
    errorOnInvalid = true
)

fun loadDefaultSchemaValidator(options: Options? = null): SchemaValidatorPlugin {
    val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA)

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
        val validation = loadDefaultSchemaValidator().validate(EventWithAllProperties.VALID_ALL_PROPS);
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
        val itly: Itly = TestUtil.getItly(Options(
            context = Context.VALID_ALL_PROPS,
            plugins = arrayListOf(TestUtil.getDefaultSchemaValidator()),
            validation = OPTIONS_ERROR_ON_INVALID
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.validate(invalidEvent)
        }

        Assertions.assertEquals(invalidEventExpectedErrorMessage, exception.message)
    }

    @Test
    fun itlyLoad_invalidContextWithErrorOnInvalid_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(Options(
                plugins = arrayListOf(TestUtil.getDefaultSchemaValidator()),
                validation = OPTIONS_ERROR_ON_INVALID
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
            plugins = arrayListOf(TestUtil.getDefaultSchemaValidator())
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
                plugins = arrayListOf(TestUtil.getDefaultSchemaValidator())
            ))
        }

        Assertions.assertEquals(
            Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
            exception.message
        )
    }

    @Test
    fun itly_withDefaultValidation_doesNotThrowErrorInProduction() {
        Assertions.assertDoesNotThrow {
            TestUtil.getItly(Options(
                environment = Environment.PRODUCTION,
                plugins = arrayListOf(TestUtil.getDefaultSchemaValidator())
            ))
        }
    }
}
