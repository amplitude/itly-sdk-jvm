/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import ly.iterative.itly.*

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.controller.TrackerController
import com.snowplowanalytics.snowplow.event.SelfDescribing
import com.snowplowanalytics.snowplow.network.HttpMethod
import com.snowplowanalytics.snowplow.payload.SelfDescribingJson

open class SnowplowCallOptions : PluginCallOptions() {
    var callback: (() -> Unit)? = null;
    var context: MutableList<SelfDescribingJson>? = null;
}
class SnowplowAliasOptions : SnowplowCallOptions() {}
class SnowplowGroupOptions : SnowplowCallOptions() {}
class SnowplowIdentifyOptions : SnowplowCallOptions() {}
class SnowplowTrackOptions : SnowplowCallOptions() {}

actual class SnowplowPlugin actual constructor(
    options: SnowplowOptions
) : Plugin<SnowplowAliasOptions, SnowplowGroupOptions, SnowplowIdentifyOptions, SnowplowTrackOptions>(ID) {
    companion object {
        @JvmField
        val ID = "snowplow"
    }

    private var config: SnowplowOptions = options
    private lateinit var logger: Logger
    private lateinit var snowplow: TrackerController

    val client: TrackerController?
        get() = this.snowplow

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-snowplow] load")
        this.snowplow = config.tracker ?: Snowplow.createTracker(config.androidContext,
                "itly", config.trackerUrl, HttpMethod.POST);
    }

    override fun identify(userId: String?, properties: Properties?, pluginCallOptions: SnowplowGroupOptions?) {
        logger.debug("[plugin-snowplow] identify(userId=$userId, properties=${properties?.properties})")
        val subject = this.snowplow.subject
        subject?.userId = userId
        pluginCallOptions?.callback?.let { it() }
    }

    override fun track(userId: String?, event: Event, pluginCallOptions: SnowplowTrackOptions?) {
        logger.debug("[plugin-snowplow] track(userId = $userId event=${event.name} properties=${event.properties})")
        val schemaVer = event.version?.replace(Regex("/\\./g"), "-")
        val schema = "iglu:${config.vendor}/${event.name}/jsonschema/${schemaVer}"
        val selfDescribingEvent = SelfDescribingJson(schema, event.properties)
        if (pluginCallOptions?.context != null)
            this.snowplow.track(SelfDescribing.builder().contexts(pluginCallOptions.context!!).eventData(selfDescribingEvent).build())
        else
            this.snowplow.track(SelfDescribing.builder().eventData(selfDescribingEvent).build())
    }

}
