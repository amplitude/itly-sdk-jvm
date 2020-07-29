package ly.iterative.itly

import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

val user = User.DEFAULT

class ItlyCoreTest {
    @Test
    fun load_defaultOptions_succeeds() {
        val itly = TestUtil.getItly(OptionsCore())

        val exception = Assertions.assertDoesNotThrow {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }

        Assertions.assertEquals(Unit, exception)
    }

    @Test
    fun alias_beforeLoad_throwsError() {
        val itly = ItlyCore()
        Asserts.assertThrowsErrorNotInitialized {
            itly.alias(user.id, "previous-id")
        }
    }

    @Test
    fun group_beforeLoad_throwsError() {
        val itly = ItlyCore()
        Asserts.assertThrowsErrorNotInitialized {
            itly.group(user.id, user.groupId, Identify.VALID_ALL_PROPS)
        }
    }

    @Test
    fun identity_beforeLoad_throwsError() {
        val itly = ItlyCore()
        Asserts.assertThrowsErrorNotInitialized {
            itly.identify(user.id, Identify.VALID_ALL_PROPS)
        }
    }

    @Test
    fun track_beforeLoad_throwsError() {
        val itly = ItlyCore()
        Asserts.assertThrowsErrorNotInitialized {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }
    }
}
