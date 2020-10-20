/**
 * JVM SegmentPlugin
 */
package ly.iterative.itly

import com.segment.analytics.Analytics

actual class SegmentOptions @JvmOverloads constructor(
    builder: Analytics.Builder? = null
): ly.iterative.itly.segment.SegmentOptions(builder)
