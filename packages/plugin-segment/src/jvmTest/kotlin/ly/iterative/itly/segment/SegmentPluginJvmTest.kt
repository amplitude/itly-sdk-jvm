package ly.iterative.itly

import ly.iterative.itly.segment.SegmentOptions
import ly.iterative.itly.segment.SegmentPlugin
import org.junit.jupiter.api.*

class SegmentPluginTest {
    @Test
    fun SegmentPluginJvm_instantiationWithoutSegmentOptions_succeeds() {
        val segmentPlugin = SegmentPlugin("write-key")
//        Assertions.assertNotNull(null)
        Assertions.assertNotNull(segmentPlugin)
    }

    @Test
    fun SegmentPluginJvm_instantiationWithSegmentOptions_succeeds() {
        val segmentPlugin = SegmentPlugin("write-key", SegmentOptions())
        Assertions.assertNotNull(segmentPlugin)
    }

    @Test
    fun SegmentPluginJvm_instantiationWithAnonymousIdAndSegmentOptions_succeeds() {
        val segmentPlugin = SegmentPlugin("write-key", "", SegmentOptions())
        Assertions.assertNotNull(segmentPlugin)
    }
}
