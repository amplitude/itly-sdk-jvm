/**
 * Android MParticlePlugin
 */
package ly.iterative.itly.mparticle

import com.mparticle.MPEvent
import ly.iterative.itly.*
import com.mparticle.MParticle

open class MParticleCallOptions : PluginCallOptions()
class MParticleAliasOptions : MParticleCallOptions()
class MParticleGroupOptions : MParticleCallOptions()
class MParticleIdentifyOptions : MParticleCallOptions()
class MParticleTrackOptions : MParticleCallOptions()

actual class MParticlePlugin actual constructor(
    private val apiKey: String,
    options: MParticleOptions
) : Plugin<MParticleAliasOptions, MParticleIdentifyOptions, MParticleGroupOptions, MParticleTrackOptions>(ID) {
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

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("$LOG_TAG load")
        MParticle.start(com.mparticle.MParticleOptions
                .builder(config.androidContext)
                .credentials(apiKey, config.apiSecret)
                .build())
    }

//    fun screen() {
//        MParticle.getInstance().logScreen("Screen name")
//        MParticle.getInstance().logScreen("name", properties)
//        MParticle.getInstance().logScreen(mpEvent)
//    }

    override fun track(userId: String?, event: Event, pluginCallOptions: MParticleTrackOptions?) {
        logger.debug("$LOG_TAG track(userId = $userId event=${event.name} properties=${event.properties})")

        val mpMetadata = event.metadata?.get(ID)
        val metaEventType = mpMetadata?.get("eventType")
        val metaCustomFlags = mpMetadata?.get("customFlags")

        val mpeBuilder = MPEvent.Builder(
            event.name,
            if (metaEventType != null) metaEventType as MParticle.EventType
            else MParticle.EventType.Other
        )
        .customAttributes(event.properties.mapValues { it.value.toString() }.plus(
            "\$itly" to "audit"
        ))

        if (metaCustomFlags != null && metaCustomFlags is Map<*, *>) {
            metaCustomFlags.forEach{
                mpeBuilder.addCustomFlag(it.key.toString(), it.value.toString())
            }
        }

        val mpe = mpeBuilder.build()
        logger.debug("$LOG_TAG mpEvent=${mpe.eventName} type=${mpe.type} attr=${mpe.customAttributes} flags=${mpe.customFlags}")

        MParticle.getInstance()?.logEvent(mpe)
    }
}
