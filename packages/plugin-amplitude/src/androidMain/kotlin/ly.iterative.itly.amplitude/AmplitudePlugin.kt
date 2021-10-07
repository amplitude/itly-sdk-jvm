/**
 * Android AmplitudePlugin
 */
package ly.iterative.itly.amplitude

import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.amplitude.api.Plan
import ly.iterative.itly.*
import ly.iterative.itly.internal.OrgJsonProperties
import org.json.JSONArray
import org.json.JSONObject

open class AmplitudeCallOptions : PluginCallOptions()
open class AmplitudeAliasOptions : AmplitudeCallOptions()
open class AmplitudeGroupOptions : AmplitudeCallOptions()
open class AmplitudeIdentifyOptions constructor(
    var deviceId: String? = null,
    var callback: (() -> Unit)? = null
) : AmplitudeCallOptions()
open class AmplitudeTrackOptions constructor(
    var insertId: String? = null,
    var callback: (() -> Unit)? = null
) : AmplitudeCallOptions()

actual open class AmplitudePlugin actual constructor(
    private val apiKey: String,
    options: AmplitudeOptions
) : Plugin(ID) {
    companion object {
        @JvmField
        val ID = "amplitude"
    }

    private var config: AmplitudeOptions = options
    private lateinit var logger: Logger
    private lateinit var amplitude: AmplitudeClient

    val client: AmplitudeClient
        get() = this.amplitude

    override fun load(options: PluginLoadOptions) {
        logger = options.logger
        logger.debug("[plugin-${id()}] load")

        amplitude = Amplitude.getInstance()
        amplitude.initialize(config.androidContext, apiKey)
        amplitude.setPlan(Plan().setBranch(config.planBranch).setSource(config.planSource).setVersion(config.planVersion))
    }

    override fun identify(userId: String?, properties: Properties?, options: PluginCallOptions?) {
        val castedOptions = getTypedOptions<AmplitudeIdentifyOptions>(options)

        logger.debug("[plugin-${id()}] identify(userId=$userId, properties=${properties?.properties})")

        userId?.let {
            this.amplitude.userId = it
        }
        castedOptions?.deviceId?.let {
            this.amplitude.deviceId = it
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

        castedOptions?.callback?.let { it() }
    }

    override fun track(userId: String?, event: Event, options: PluginCallOptions?) {
        val castedOptions = getTypedOptions<AmplitudeTrackOptions>(options)
        logger.debug("[plugin-${id()}] track(userId = $userId event=${event.name} properties=${event.properties})")
        val eventProps = OrgJsonProperties.toOrgJson(event)
        castedOptions?.insertId?.let {
            eventProps?.put("insert_id", it)
        }
        amplitude.logEvent(event.name, eventProps)
        castedOptions?.callback?.let { it() }
    }

    override fun reset() {
        logger.debug("[plugin-${id()}] reset")
        amplitude.userId = null
    }
}
