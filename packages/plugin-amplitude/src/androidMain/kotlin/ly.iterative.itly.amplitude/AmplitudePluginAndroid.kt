/**
 * Android AmplitudePlugin
 */
package ly.iterative.itly.amplitude

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import ly.iterative.itly.*
import org.json.JSONArray
import org.json.JSONObject

actual data class AmplitudeOptions(
    val androidContext: Context
)

actual class AmplitudePlugin actual constructor(
    private val apiKey: String,
    options: AmplitudeOptions
) : PluginBase() {
    companion object {
        @JvmField
        val ID = "amplitude"
    }

    private var config: AmplitudeOptions = options
    private lateinit var logger: Logger
    private lateinit var amplitude: AmplitudeClient

    val client: AmplitudeClient
        get() = this.amplitude

    override fun id(): String {
        return ID
    }

    override fun load(options: OptionsCore) {
        logger = options.logger
        logger.debug("[plugin-${id()}] load")

        amplitude = Amplitude.getInstance()
        amplitude.initialize(config.androidContext, apiKey)
    }

    override fun identify(userId: String?, properties: Properties?) {
        logger.debug("[plugin-${id()}] identify(userId=$userId, properties=${properties?.properties})")

        userId?.let {
            this.amplitude.userId = it
        }

        properties?.let {
            val identify = Identify()
            it.properties.forEach {
                val key = it.key
                when (val value = it.value) {
                    is Int -> {
                        identify.set(key, value)
                    }
                    is Long -> {
                        identify.set(key, value)
                    }
                    is Float -> {
                        identify.set(key, value)
                    }
                    is Double -> {
                        identify.set(key, value)
                    }
                    is IntArray -> {
                        identify.set(key, value)
                    }
                    is String -> {
                        identify.set(key, value)
                    }
                    is Boolean -> {
                        identify.set(key, value)
                    }
                    is LongArray -> {
                        identify.set(key, value)
                    }
                    is FloatArray -> {
                        identify.set(key, value)
                    }
                    is DoubleArray -> {
                        identify.set(key, value)
                    }
                    is BooleanArray -> {
                        identify.set(key, value)
                    }
                    is JSONArray -> {
                        identify.set(key, value)
                    }
                    is JSONObject -> {
                        identify.set(key, value)
                    }
                    is Array<*> -> {
                        if (value.isArrayOf<String>()) {
                            @Suppress("UNCHECKED_CAST")
                            identify.set(key, value as Array<String>)
                        }
                    }
                    else -> {
                        System.err.println("Invalid type encountered for object: $value")
                    }
                }
            }
            this.amplitude.identify(identify)
        }
    }

    override fun track(userId: String?, event: Event) {
        logger.debug("[plugin-${id()}] track(userId = $userId event=${event.name} properties=${event.properties})")
        amplitude.logEvent(event.name, OrgJsonProperties.toOrgJson(event))
    }

    override fun reset() {
        logger.debug("[plugin-${id()}] reset")
        amplitude.userId = null
    }
}
