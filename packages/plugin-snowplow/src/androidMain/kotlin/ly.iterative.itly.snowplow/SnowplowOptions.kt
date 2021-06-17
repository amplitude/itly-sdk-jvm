/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import android.content.Context

actual open class SnowplowOptions(
        val androidContext: Context,
        var tracker: Tracker?
)
