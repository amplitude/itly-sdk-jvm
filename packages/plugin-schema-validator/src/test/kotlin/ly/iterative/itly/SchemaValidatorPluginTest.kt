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

fun loadSchemaValidator(schema: Map<String, String> = Schemas.DEFAULT): SchemaValidatorPlugin {
    val schemaValidatorPlugin = SchemaValidatorPlugin(schema)
    val opts = Options(environment = Environment.PRODUCTION)
    schemaValidatorPlugin.load(PluginLoadOptions(opts))

    return schemaValidatorPlugin
}

class SchemaValidatorPluginTest {
    @Test
    fun validate_contextWithProperties_valid() {
        val validation = loadSchemaValidator().validate(Context.VALID_ALL_PROPS)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_groupWithProperties_valid() {
        val validation = loadSchemaValidator().validate(Group.VALID_ALL_PROPS)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_identifyWithProperties_valid() {
        val validation = loadSchemaValidator().validate(Identify.VALID_ALL_PROPS)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_identifyWithoutProperties_invalid() {
        val validation = loadSchemaValidator().validate(Identify.INVALID_NO_PROPS)
        Assertions.assertEquals(false, validation.valid)
    }

    @Test
    fun validate_identifyWithoutPropertiesNoSchema_valid() {
        val validation = loadSchemaValidator(Schemas.defaultWithout("identify")).validate(Identify.INVALID_NO_PROPS)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_identifyWithPropertiesNoSchema_invalid() {
        val validation = loadSchemaValidator(Schemas.defaultWithout("identify")).validate(Identify.VALID_ALL_PROPS)
        Assertions.assertEquals(false, validation.valid)
        Assertions.assertEquals("No schema found for 'identify'. Received identify={\"optionalArray\":[\"optional\"],\"requiredNumber\":2.0}", validation.message)
    }

    @Test
    fun validate_eventWithAllProperties_valid() {
        val validation = loadSchemaValidator().validate(EventWithAllProperties.VALID_ALL_PROPS)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_eventWithConstTypes_valid() {
        val validation = loadSchemaValidator().validate(EventWithConstTypes.VALID)
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_eventWithArrayTypes_valid() {
        val validation = loadSchemaValidator().validate(EventWithArrayTypes(
            arrayOf(true, false),
            arrayOf(1.0, 2.0, 3.0),
            arrayOf(mapOf("a" to 1, "b" to 2.0, "c" to "xyz"), mapOf("x" to "a", "y" to 2.0, "z" to "abc")),
            arrayOf("a", "bc")
        ))
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_eventObjectTypes_valid() {
        val validation = loadSchemaValidator().validate(EventWithObjectTypes(
            mapOf("a" to 1, "b" to 2.0, "c" to "xyz"),
            arrayOf(mapOf("a" to 1, "b" to 2.0, "c" to "xyz"), mapOf("x" to "a", "y" to 2.0, "z" to "abc"))
        ))
        Assertions.assertEquals(true, validation.valid)
    }

    @Test
    fun validate_invalidEvent_notValid() {
        val validation = loadSchemaValidator().validate(invalidEvent)
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
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.DEFAULT))
            ))
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
            plugins = arrayListOf(SchemaValidatorPlugin(Schemas.defaultWithout("context")))
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
                plugins = arrayListOf(SchemaValidatorPlugin(Schemas.defaultWithout("context")))
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
