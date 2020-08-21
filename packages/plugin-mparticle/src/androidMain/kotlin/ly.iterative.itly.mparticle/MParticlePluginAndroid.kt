/**
 * Android MParticlePlugin
 */
package ly.iterative.itly.mparticle

import com.mparticle.MPEvent
import ly.iterative.itly.*
import com.mparticle.MParticle
import ly.iterative.itly.core.Options

typealias MP = com.mparticle.MParticle

actual typealias MParticleOptions = com.mparticle.MParticleOptions

actual class MParticlePlugin actual constructor(
    options: MParticleOptions
) : PluginBase() {
    companion object {
        @JvmField
        val ID = "mparticle"
        val LOG_TAG = "[plugin-$ID]"
    }

    private var config: MParticleOptions = options
    private lateinit var logger: Logger
    private lateinit var mParticle: MParticle

    val client: MParticle
        get() = this.mParticle

    override fun id(): String {
        return ID
    }

    override fun load(options: Options) {
        logger = options.logger
        logger.debug("$LOG_TAG load")
        MParticle.start(config)
    }

//    fun screen() {
//        MParticle.getInstance().logScreen("Screen name")
//        MParticle.getInstance().logScreen("name", properties)
//        MParticle.getInstance().logScreen(mpEvent)
//    }

    override fun track(userId: String?, event: Event) {
        logger.debug("$LOG_TAG track(userId = $userId event=${event.name} properties=${event.properties})")

        val mpMetadata = event.metadata?.get(ID)

//        meta?.eventType || Mparticle.EventType.Other,
//        {
//            $itly: this.$itly,
//            ...event.properties,
//        },
//        meta?.customFlags,
        val metaEventType = mpMetadata?.get("eventType")
        val metaCustomFlags = mpMetadata?.get("customFlags")

        logger.debug("$LOG_TAG HEHREHREHREHRHEREHRHERHEREREHRHEHRERE!!!")
        val mpeBuilder = MPEvent.Builder(
            event.name,
            if (metaEventType != null) metaEventType as MParticle.EventType
            else MParticle.EventType.Other
        )
        .customAttributes(event.properties.mapValues { it.value.toString() }.plus(
            "\$itly" to "audit"
        ))
//        .addCustomFlag("\$itly", "audit")
//
//        if (metaCustomFlags != null) {
//            (metaCustomFlags as Map<String, String>).forEach{
//                mpeBuilder.addCustomFlag(it.key, it.value)
//            }
//        }

        val mpe = mpeBuilder.build()
        logger.debug("$LOG_TAG event=${mpe.eventName} type=${mpe.type} attr=${mpe.customAttributes} flags=${mpe.customFlags}")

        MParticle.getInstance()?.logEvent(mpe)
    }
}
