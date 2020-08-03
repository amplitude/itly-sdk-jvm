/**
 * JVM SegmentPlugin
 */
package ly.iterative.itly.mixpanel

import ly.iterative.itly.*
import ly.iterative.itly.core.*

actual data class MixpanelOptions @JvmOverloads constructor(
    val builder: Any? = null
)

actual class MixpanelPlugin actual constructor(
    private val token: String,
    options: MixpanelOptions
) : PluginBase() {
    companion object {
        @JvmField
        val ID = "mixpanel"
    }

    private var config: MixpanelOptions = options
    private lateinit var logger: Logger

    override fun id(): String {
        return ID
    }

    override fun load(options: Options) {
        logger = options.logger
        logger.info("[plugin-${id()}] load")
    }

    // TODO: Implement Java only MixpanelPlugin
}
