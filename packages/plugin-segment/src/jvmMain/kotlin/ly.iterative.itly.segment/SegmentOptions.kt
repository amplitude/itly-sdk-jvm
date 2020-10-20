/**
 * JVM SegmentPlugin
 */
package ly.iterative.itly.segment

import com.segment.analytics.Analytics

actual open class SegmentOptions @JvmOverloads constructor(
    val builder: Analytics.Builder? = null
)
