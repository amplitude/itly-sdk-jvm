package ly.iterative.itly

import io.mockk.mockk
import io.mockk.verify
import ly.iterative.itly.core.Itly
import ly.iterative.itly.core.Options
import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

val user = User.DEFAULT

class ItlyTest {
    @Test
    fun load_defaultOptions_succeeds() {
        val itly = TestUtil.getItly(Options())

        val exception = Assertions.assertDoesNotThrow {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }

        Assertions.assertEquals(Unit, exception)
    }

    @Test
    fun load_trackWhenDisabled_doesntCallPlugins() {
        val plugin = mockk<Plugin>(relaxed = true)

        val itly = TestUtil.getItly(Options(
            plugins = listOf(plugin),
            disabled = true
        ))

        itly.track(user.id, EventMaxIntForTest.VALID)

        verify(exactly = 0) { plugin.track(user.id, EventMaxIntForTest.VALID) }
    }

    @Test
    fun alias_beforeLoad_throwsError() {
        val itly = Itly()
        Asserts.assertThrowsErrorNotInitialized {
            itly.alias(user.id, "previous-id")
        }
    }

    @Test
    fun group_beforeLoad_throwsError() {
        val itly = Itly()
        Asserts.assertThrowsErrorNotInitialized {
            itly.group(user.id, user.groupId, Identify.VALID_ALL_PROPS)
        }
    }

    @Test
    fun identity_beforeLoad_throwsError() {
        val itly = Itly()
        Asserts.assertThrowsErrorNotInitialized {
            itly.identify(user.id, Identify.VALID_ALL_PROPS)
        }
    }

    @Test
    fun track_beforeLoad_throwsError() {
        val itly = Itly()
        Asserts.assertThrowsErrorNotInitialized {
            itly.track(user.id, EventMaxIntForTest.INVALID_MAX_VALUE)
        }
    }

    @Test
    fun identify_withoutProperties_succeeds() {
        val itly = Itly()
        itly.load(Options())

        Assertions.assertDoesNotThrow {
            itly.identify(user.id)
        }
    }

    @Test
    fun identify_withProperties_succeeds() {
        val itly = Itly()
        itly.load(Options())

        Assertions.assertDoesNotThrow {
            itly.identify(user.id, Identify(
                requiredNumber = 42.0
            ))
        }
    }

    @Test
    fun group_withoutProperties_succeeds() {
        val itly = Itly()
        itly.load(Options())

        Assertions.assertDoesNotThrow {
            itly.group(user.id, user.groupId)
        }
    }

    @Test
    fun group_withProperties_succeeds() {
        val itly = Itly()
        itly.load(Options())

        Assertions.assertDoesNotThrow {
            itly.group(user.id, user.groupId, Group(
                requiredBoolean = true
            ))
        }
    }
}
