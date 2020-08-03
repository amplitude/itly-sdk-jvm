/**
 * JVM SegmentPlugin
 */
package ly.iterative.itly.segment

import ly.iterative.itly.*
import ly.iterative.itly.core.*
import com.segment.analytics.Analytics
import com.segment.analytics.messages.AliasMessage
import com.segment.analytics.messages.GroupMessage
import com.segment.analytics.messages.IdentifyMessage
import com.segment.analytics.messages.TrackMessage
import kotlin.collections.HashMap

actual data class SegmentOptions @JvmOverloads constructor(
    val builder: Analytics.Builder? = null
)

actual class SegmentPlugin actual constructor(
    private val writeKey: String,
    options: SegmentOptions
) : PluginBase() {
    companion object {
        @JvmField
        val ID = "segment"
    }

    private var config: SegmentOptions = options
    private lateinit var logger: Logger
    private lateinit var segment: Analytics

    val client: Analytics
        get() = this.segment

    constructor(writeKey: String): this(writeKey, SegmentOptions())

    override fun id(): String {
        return ID
    }

    override fun load(options: Options) {
        logger = options.logger
        val builder = config.builder ?: Analytics.builder(writeKey)
        segment = builder.build()
    }

    override fun alias(userId: String, previousId: String?) {
        logger.info("[plugin-segment-jvm] alias(userId=$userId, previousId=$previousId)")
        segment.enqueue(AliasMessage.builder(previousId)
                .userId(userId))
    }

    override fun identify(userId: String?, properties: Properties?) {
        logger.info("[plugin-segment-jvm] identify(userId=$userId, properties=${properties?.properties})")
        val traits = HashMap(properties?.properties)
        val message = IdentifyMessage.builder().userId(userId)
        if (traits.isNotEmpty()) {
            message.traits(traits)
        }
        segment.enqueue(message)
    }

    override fun group(userId: String?, groupId: String, properties: Properties?) {
        logger.info("[plugin-segment-jvm] group(userId=$userId, groupId=$groupId, properties=${properties?.properties})")
        val traits = HashMap(properties?.properties)
        val message = GroupMessage.builder(groupId).userId(userId)
        if (traits.isNotEmpty()) {
            message.traits(traits)
        }
        segment.enqueue(message)
    }

    override fun track(userId: String?, event: Event) {
        logger.info("[plugin-segment-jvm] track(userId=$userId, event=${event.name}, properties=${event.properties})")
        val properties = HashMap(event.properties)
        val message = TrackMessage.builder(event.name).userId(userId)
        if (properties.isNotEmpty()) {
            message.properties(properties)
        }
        segment.enqueue(message)
    }

    override fun shutdown() {
        logger.info("[plugin-segment-jvm] shutdown");
        segment.shutdown()
    }
}
