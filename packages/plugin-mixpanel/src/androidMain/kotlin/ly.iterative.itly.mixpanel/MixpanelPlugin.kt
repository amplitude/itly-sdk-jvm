package ly.iterative.itly.mixpanel

import ly.iterative.itly.*

import com.mixpanel.android.mpmetrics.MixpanelAPI
import ly.iterative.itly.internal.OrgJsonProperties

open class MixpanelCallOptions : PluginCallOptions()
class MixpanelAliasOptions : MixpanelCallOptions()
class MixpanelGroupOptions : MixpanelCallOptions()
class MixpanelIdentifyOptions : MixpanelCallOptions()
class MixpanelTrackOptions : MixpanelCallOptions()

actual class MixpanelPlugin actual constructor(
    private val token: String,
    options: MixpanelOptions
) : Plugin<MixpanelAliasOptions, MixpanelIdentifyOptions, MixpanelGroupOptions, MixpanelTrackOptions>(ID) {
    companion object {
        @JvmField
        val ID = "mixpanel"
    }

    private var config: MixpanelOptions = options
    private lateinit var logger: Logger
    private lateinit var mixpanel: MixpanelAPI

    val client: MixpanelAPI
        get() = this.mixpanel

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-${id()}] load")

        mixpanel = MixpanelAPI.getInstance(config.androidContext, token)
    }

    override fun alias(userId: String, previousId: String?, pluginCallOptions: MixpanelAliasOptions?) {
        logger.debug("[plugin-${id()}] alias(userId=$userId previousId=$previousId)")
        mixpanel.alias(userId, previousId)
    }

    override fun identify(userId: String?, properties: Properties?, pluginCallOptions: MixpanelIdentifyOptions?) {
        logger.debug("[plugin-${id()}] identify(userId=$userId, properties=${properties?.properties})")

        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
        properties?.let {
            this.mixpanel.people.set(OrgJsonProperties.toOrgJson(it))
        }
    }

    override fun track(userId: String?, event: Event, pluginCallOptions: MixpanelTrackOptions?) {
        logger.debug("[plugin-${id()}] track(userId = $userId event=${event.name} properties=${event.properties})")
        mixpanel.track(event.name, OrgJsonProperties.toOrgJson(event))
    }

    override fun reset() {
        logger.debug("[plugin-${id()}] reset")
        mixpanel.reset()
    }
}
