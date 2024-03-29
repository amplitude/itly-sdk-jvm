/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import android.content.Context
import com.snowplowanalytics.snowplow.tracker.Tracker

actual open class SnowplowOptions(
    val androidContext: Context,
    var vendor: String,
    var trackerUrl: String? = null,
    var appId: String? = null,
    var tracker: Tracker? = null
)
