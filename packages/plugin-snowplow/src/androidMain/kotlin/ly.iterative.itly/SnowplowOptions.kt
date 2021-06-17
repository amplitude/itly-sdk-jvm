/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly

import android.content.Context
import ly.iterative.itly.snowplow.SnowplowOptions

actual class SnowplowOptions(
    androidContext: Context,
    tracker: Tracker?
): SnowplowOptions(androidContext, tracker)
