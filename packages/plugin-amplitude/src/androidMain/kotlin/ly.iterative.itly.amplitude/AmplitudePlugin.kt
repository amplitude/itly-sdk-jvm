/**
 * Android AmplitudePlugin
 */
package ly.iterative.itly.amplitude

import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import ly.iterative.itly.*
import ly.iterative.itly.internal.OrgJsonProperties
import org.json.JSONArray
import org.json.JSONObject

open class AmplitudeCallOptions : PluginCallOptions() {}
class AmplitudeAliasOptions : AmplitudeCallOptions() {}
class AmplitudeGroupOptions : AmplitudeCallOptions() {}
class AmplitudeIdentifyOptions constructor(
        deviceId: String?,
        callback: (() -> Unit)?
) : AmplitudeCallOptions() {
    var deviceId: String? = deviceId
    var callback: (() -> Unit)? = callback
}
class AmplitudeTrackOptions constructor(
        insertId: String?,
        callback: (() -> Unit)?
) : AmplitudeCallOptions() {
    var insertId: String? = insertId
    var callback: (() -> Unit)? = callback
}

actual class AmplitudePlugin actual constructor(
    private val apiKey: String,
    options: AmplitudeOptions
) : Plugin<AmplitudeAliasOptions, AmplitudeIdentifyOptions, AmplitudeGroupOptions, AmplitudeTrackOptions>(ID) {
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
    }

    override fun identify(userId: String?, properties: Properties?, pluginCallOptions: AmplitudeIdentifyOptions?) {
        logger.debug("[plugin-${id()}] identify(userId=$userId, properties=${properties?.properties})")

        userId?.let {
            this.amplitude.userId = it
        }
        pluginCallOptions?.deviceId?.let {
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

        pluginCallOptions?.callback?.let { it() }
    }

    override fun track(userId: String?, event: Event, pluginCallOptions: AmplitudeTrackOptions?) {
        logger.debug("[plugin-${id()}] track(userId = $userId event=${event.name} properties=${event.properties})")
        val eventProps = OrgJsonProperties.toOrgJson(event)
        pluginCallOptions?.insertId?.let {
            eventProps?.put("insert_id", it)
        }
        amplitude.logEvent(event.name, eventProps)
        pluginCallOptions?.callback?.let { it() }
    }

    override fun reset() {
        logger.debug("[plugin-${id()}] reset")
        amplitude.userId = null
    }
}
