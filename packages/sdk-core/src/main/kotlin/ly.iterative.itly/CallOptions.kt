package ly.iterative.itly

import java.util.HashMap

open class CallOptions constructor(map: HashMap<String, PluginCallOptions?>? = HashMap()) {
    val map: HashMap<String, PluginCallOptions?>? = map

    fun get(pluginName: String): PluginCallOptions? {
        return map?.get(pluginName)
    }
    fun put(pluginName: String, options: PluginCallOptions?) {
        map?.put(pluginName, options)
    }
}
