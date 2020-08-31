/**
 * Android SegmentPlugin
 */
package ly.iterative.itly.mixpanel

import ly.iterative.itly.*

import com.mixpanel.android.mpmetrics.MixpanelAPI
import android.content.Context
import ly.iterative.itly.core.Options
import ly.iterative.itly.internal.OrgJsonProperties

actual data class MixpanelOptions(
    val androidContext: Context
)

actual class MixpanelPlugin actual constructor(
    private val token: String,
    options: MixpanelOptions
) : PluginBase(ID) {
    companion object {
        @JvmField
        val ID = "mixpanel"
    }

    private var config: MixpanelOptions = options
    private lateinit var logger: Logger
    private lateinit var mixpanel: MixpanelAPI

    val client: MixpanelAPI
        get() = this.mixpanel

    override fun load(options: Options) {
        logger = options.logger
        logger.debug("[plugin-${id()}] load")

        mixpanel = MixpanelAPI.getInstance(config.androidContext, token)
    }

    override fun alias(userId: String, previousId: String?) {
        logger.debug("[plugin-${id()}] alias(userId=$userId previousId=$previousId)")
        mixpanel.alias(userId, previousId)
    }

    override fun identify(userId: String?, properties: Properties?) {
        logger.debug("[plugin-${id()}] identify(userId=$userId, properties=${properties?.properties})")

        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
        properties?.let {
            this.mixpanel.people.set(OrgJsonProperties.toOrgJson(it))
        }
    }

    override fun track(userId: String?, event: Event) {
        logger.debug("[plugin-${id()}] track(userId = $userId event=${event.name} properties=${event.properties})")
        mixpanel.track(event.name, OrgJsonProperties.toOrgJson(event))
    }

    override fun reset() {
        logger.debug("[plugin-${id()}] reset")
        mixpanel.reset()
    }
}
