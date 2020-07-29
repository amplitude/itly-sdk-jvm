package ly.iterative.itly

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

class SchemaValidatorPluginTest {
    @Test
    fun validate_contextWithProperties_valid() {
        val validation = TestUtil.getSchemaValidator().validate(Context.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_groupWithProperties_valid() {
        val validation = TestUtil.getSchemaValidator().validate(Group.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_identifyWithProperties_valid() {
        val validation = TestUtil.getSchemaValidator().validate(Identify.VALID_ALL_PROPS)
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_eventWithAllProperties_valid() {
        val validation = TestUtil.getSchemaValidator().validate(EventWithAllProperties.VALID_ALL_PROPS);
        Assertions.assertEquals(validation.valid, true)
    }

    @Test
    fun validate_invalidEvent_notValid() {
        val validation = TestUtil.getSchemaValidator().validate(invalidEvent)
        Assertions.assertEquals(validation.valid, false)
        Assertions.assertEquals(invalidEventExpectedErrorMessage, validation.message)
    }

    @Test
    fun validate_invalidEventWithErrorOnInvalid_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getSchemaValidator(OPTIONS_ERROR_ON_INVALID).validate(invalidEvent)
        }

        Assertions.assertEquals(invalidEventExpectedErrorMessage, exception.message)
    }

    @Test
    fun itlyLoad_invalidContextWithErrorOnInvalid_throwsError() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestUtil.getItly(OptionsCore(
                plugins = arrayListOf(
                    TestUtil.getSchemaValidator(OPTIONS_ERROR_ON_INVALID)
                ),
                validationOptions = OPTIONS_ERROR_ON_INVALID
            ))
        }

        Assertions.assertEquals(
            Context.ERROR_MESSAGE_REQUIRED_STRING_MISSING,
            exception.message
        )
    }

    @Test
    fun itlyTrack_invalidEventWithErrorOnInvalid_throwsError() {
        val itly = TestUtil.getItly(OptionsCore(
            context = Context.VALID_ALL_PROPS,
            plugins = arrayListOf(
                TestUtil.getSchemaValidator(OPTIONS_ERROR_ON_INVALID)
            ),
            validationOptions = OPTIONS_ERROR_ON_INVALID
        ))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }

        Assertions.assertEquals(
            EventMaxIntForTest.INVALID_MAX_VALUE_ERROR_MESSAGE,
            exception.message
        )
    }
}
