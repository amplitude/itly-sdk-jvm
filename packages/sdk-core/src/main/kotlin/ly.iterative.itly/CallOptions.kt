package ly.iterative.itly

import java.util.HashMap

open class CallOptions constructor(private val map: HashMap<String, PluginCallOptions?>? = HashMap()) {
    fun get(pluginName: String): PluginCallOptions? {
        return map?.get(pluginName)
    }
    fun put(pluginName: String, options: PluginCallOptions?) {
        map?.put(pluginName, options)
    }
}
