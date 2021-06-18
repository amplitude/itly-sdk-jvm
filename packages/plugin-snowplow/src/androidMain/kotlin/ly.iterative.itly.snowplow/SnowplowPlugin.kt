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

actual class SnowplowPlugin actual constructor(
    options: SnowplowOptions
) : Plugin(ID) {
    companion object {
        @JvmField
        val ID = "snowplow"
    }

    private var config: SnowplowOptions = options
    private lateinit var logger: Logger
    private var snowplow: TrackerController? = null

    val client: TrackerController?
        get() = this.snowplow

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-snowplow] load")
        this.snowplow = config.tracker ?: Snowplow.createTracker(config.androidContext,
                "appTracker", config.trackerUrl, HttpMethod.POST);
    }

    override fun identify(userId: String?, properties: Properties?) {
        logger.debug("[plugin-snowplow] identify(userId=$userId, properties=${properties?.properties})")
        val subject = this.snowplow?.subject
        subject?.userId = userId
    }

    override fun track(userId: String?, event: Event) {
        logger.debug("[plugin-snowplow] track(userId = $userId event=${event.name} properties=${event.properties})")
        val schemaVer = event.version?.replace(Regex("/\\./g"), "-")
        val schema = "iglu:${config.vendor}/${event.name}/jsonschema/${schemaVer}"
        val selfDescribingEvent = SelfDescribingJson(schema, event.properties)
        this.snowplow?.track(SelfDescribing.builder().eventData(selfDescribingEvent).build())
    }

}