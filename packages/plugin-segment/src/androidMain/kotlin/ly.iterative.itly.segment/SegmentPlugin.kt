/**
 * Android SegmentPlugin
 */
package ly.iterative.itly.segment

import ly.iterative.itly.*
import com.segment.analytics.Analytics
import com.segment.analytics.Traits

typealias SegmentProperties = com.segment.analytics.Properties

open class SegmentCallOptions : PluginCallOptions()
class SegmentAliasOptions : SegmentCallOptions()
class SegmentGroupOptions : SegmentCallOptions()
class SegmentIdentifyOptions : SegmentCallOptions()
class SegmentTrackOptions : SegmentCallOptions()

actual class SegmentPlugin actual constructor(
    private val writeKey: String,
    options: SegmentOptions
) : Plugin<SegmentAliasOptions, SegmentIdentifyOptions, SegmentGroupOptions, SegmentTrackOptions>(ID) {
    companion object {
        @JvmField
        val ID = "segment"
    }

    private var config: SegmentOptions = options
    private lateinit var logger: Logger
    private lateinit var segment: Analytics

    val client: Analytics
        get() = this.segment

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-segment] load")

        segment = Analytics.Builder(config.androidContext, writeKey).build()
        Analytics.setSingletonInstance(this.segment)
    }

    override fun alias(userId: String, previousId: String?, pluginCallOptions: SegmentAliasOptions?) {
        logger.debug("[plugin-segment] alias(userId=$userId previousId=$previousId)")
        this.segment.alias(userId)
    }

    override fun identify(userId: String?, properties: Properties?, pluginCallOptions: SegmentIdentifyOptions?) {
        logger.debug("[plugin-segment] identify(userId=$userId, properties=${properties?.properties})")
        if (userId == null) {
            return
        }

        var segmentTraits: Traits? = null
        if (properties != null && properties.properties.isNotEmpty()) {
            segmentTraits = Traits()
            properties.properties.forEach {
                segmentTraits.putValue(it.key, it.value)
            }
        }

        if (segmentTraits == null) {
            this.segment.identify(userId)
        } else {
            this.segment.identify(userId, segmentTraits, null)
        }
    }

    override fun group(userId: String?, groupId: String, properties: Properties?, pluginCallOptions: SegmentGroupOptions?) {
        logger.debug("[plugin-segment] group(userId = $userId, groupdId=$groupId properties=${properties?.properties})")
        var segmentTraits: Traits? = null
        if (properties != null && properties.properties.isNotEmpty()) {
            segmentTraits = Traits()
            properties.properties.forEach {
                segmentTraits.putValue(it.key, it.value)
            }
        }

        this.segment.group(groupId, segmentTraits, null)
    }

    override fun track(userId: String?, event: Event, pluginCallOptions: SegmentTrackOptions?) {
        logger.debug("[plugin-segment] track(userId = $userId event=${event.name} properties=${event.properties})")
        var segmentProperties: SegmentProperties? = null
        if (event.properties.isNotEmpty()) {
            segmentProperties = SegmentProperties()
            event.properties.forEach {
                segmentProperties[it.key] = it.value
            }
        }

        this.segment.track(event.name, segmentProperties, null)
    }

    override fun reset() {
        logger.debug("[plugin-segment] reset")
        this.segment.reset()
    }

    override fun flush() {
        logger.debug("[plugin-segment] flush")
        this.segment.flush()
    }

    override fun shutdown() {
        logger.debug("[plugin-segment] shutdown")
        // TODO: Check if it is the "default instance"
//        this.segment.shutdown()
    }
}
