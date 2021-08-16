/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import android.content.Context
import com.snowplowanalytics.snowplow.controller.TrackerController

actual open class SnowplowOptions(
        val androidContext: Context,
        var vendor: String,
        var trackerUrl: String? = null,
        var tracker: TrackerController? = null
        )
