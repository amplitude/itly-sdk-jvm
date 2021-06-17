/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import ly.iterative.itly.*

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.controller.TrackerController
import com.snowplowanalytics.snowplow.event.SelfDescribing
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

        if (config.tracker != null) {
            this.snowplow = config.tracker
        }
        else {
            this.snowplow = Snowplow.getDefaultTracker()
        }
    }

    override fun identify(userId: String?, properties: Properties?) {
        logger.debug("[plugin-snowplow] identify(userId=$userId, properties=${properties?.properties})")
        if (userId == null) {
            return
        }
        val subject = this.snowplow?.subject
        subject?.userId = userId
    }

    override fun track(userId: String?, event: Event) {
        logger.debug("[plugin-snowplow] track(userId = $userId event=${event.name} properties=${event.properties})")
        val schemaVer = event.version?.replace(Regex("/\\./g"), "-")
        val schema = "iglu:com.snowplowanalytics/${event.name}/jsonschema/${schemaVer}"
        val selfDescribingEvent = SelfDescribingJson(schema, event.properties)
        this.snowplow?.track(SelfDescribing.builder().eventData(selfDescribingEvent).build())
    }

}
