/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly

import android.content.Context
import com.snowplowanalytics.snowplow.controller.TrackerController
import ly.iterative.itly.snowplow.SnowplowOptions

actual class SnowplowOptions(
    androidContext: Context,
    vendor: String,
    trackerUrl: String? = null,
    tracker: TrackerController? = null
): SnowplowOptions(androidContext, vendor, trackerUrl, tracker)
