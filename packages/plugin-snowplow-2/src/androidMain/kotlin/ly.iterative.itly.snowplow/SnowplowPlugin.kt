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
import java.lang.IllegalArgumentException

open class SnowplowCallOptions : PluginCallOptions()
open class SnowplowAliasOptions : SnowplowCallOptions()
open class SnowplowGroupOptions : SnowplowCallOptions()
open class SnowplowIdentifyOptions : SnowplowCallOptions()
open class SnowplowTrackOptions constructor(
    var callback: (() -> Unit)? = null,
    var context: MutableList<SelfDescribingJson>? = null
) : SnowplowCallOptions()

actual open class SnowplowPlugin actual constructor(
    options: SnowplowOptions
) : Plugin(ID) {
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
        if (config.tracker == null && config.trackerUrl == null) {
            throw IllegalArgumentException("At least one of 'tracker' or 'trackerUrl' in SnowplowOptions is required")
        }
        this.snowplow = config.tracker ?: Snowplow.createTracker(config.androidContext,
                "itly", config.trackerUrl ?: "", HttpMethod.POST);
    }

    override fun identify(userId: String?, properties: Properties?, options: PluginCallOptions?) {
        logger.debug("[plugin-snowplow] identify(userId=$userId, properties=${properties?.properties})")
        val subject = this.snowplow.subject
        subject.userId = userId
    }

    override fun track(userId: String?, event: Event, options: PluginCallOptions?) {
        val castedOptions = getTypedOptions<SnowplowTrackOptions>(options)
        logger.debug("[plugin-snowplow] track(userId = $userId event=${event.name} properties=${event.properties})")
        val schemaVer = event.version?.replace(Regex("/\\./g"), "-")
        val schema = "iglu:${config.vendor}/${event.name}/jsonschema/${schemaVer}"
        val selfDescribingEvent = SelfDescribingJson(schema, event.properties)
        val builder = SelfDescribing.builder()
        if (castedOptions?.context != null) {
            builder.contexts(castedOptions.context!!)
        }
        this.snowplow.track(builder.eventData(selfDescribingEvent).build())
        castedOptions?.callback?.let { it() }
    }
}
