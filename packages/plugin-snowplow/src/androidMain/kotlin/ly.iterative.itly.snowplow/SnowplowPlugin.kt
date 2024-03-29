/**
 * Android SnowplowPlugin
 */
package ly.iterative.itly.snowplow

import android.content.Context
import com.snowplowanalytics.snowplow.tracker.Emitter
import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import ly.iterative.itly.*

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
    private lateinit var snowplow: Tracker

    val client: Tracker
        get() = this.snowplow

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-snowplow] load")
        if (config.tracker == null && (config.trackerUrl == null && config.appId == null)) {
            throw IllegalArgumentException("Either 'trackerUrl' and 'appId', or 'tracker' are required in SnowplowOptions")
        }
        this.snowplow = config.tracker ?: createDefaultTracker(
            config.androidContext, config.trackerUrl ?: "", config.appId ?: ""
        )
    }

    override fun identify(userId: String?, properties: Properties?, options: PluginCallOptions?) {
        logger.debug("[plugin-snowplow] identify(userId=$userId, properties=${properties?.properties})")
        this.snowplow.subject.identifyUser(userId)
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

    protected open fun createDefaultTracker(context: Context, trackerUrl: String, appId: String): Tracker {
        val emitter = Emitter.EmitterBuilder(trackerUrl, context).build()
        val subject = Subject.SubjectBuilder().context(context).build();

        return Tracker.init(
            Tracker.TrackerBuilder(emitter, "itly", appId, context).subject(subject).build()
        )
    }
}
