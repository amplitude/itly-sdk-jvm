package ly.iterative.itly

class CallOptions @JvmOverloads constructor(
        pluginToOptions: Map<String, PluginCallOptions>
) {
    val pluginToOptions: Map<String, PluginCallOptions> = pluginToOptions;
}
