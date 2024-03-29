/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly

import android.content.Context
import com.snowplowanalytics.snowplow.tracker.Tracker
import ly.iterative.itly.snowplow.SnowplowOptions

actual class SnowplowOptions(
    androidContext: Context,
    vendor: String,
    trackerUrl: String? = null,
    appId: String? = null,
    tracker: Tracker? = null
): SnowplowOptions(androidContext, vendor, trackerUrl, appId, tracker)
