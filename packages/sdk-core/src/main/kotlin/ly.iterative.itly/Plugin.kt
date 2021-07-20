package ly.iterative.itly

abstract class Plugin<A: PluginCallOptions, I: PluginCallOptions, G: PluginCallOptions, T: PluginCallOptions> (
    private val id: String
) {
    fun id(): String {
        return id
    }

    open fun load(options: PluginLoadOptions) {}

    open fun validate(event: Event): ValidationResponse? {
        return null
    }

    open fun alias(userId: String, previousId: String?, pluginCallOptions: A?) {}
    open fun postAlias(userId: String, previousId: String?) {}

    open fun identify(userId: String?, properties: Properties?, pluginCallOptions: I?) {}
    open fun postIdentify(userId: String?, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun group(userId: String?, groupId: String, properties: Properties?, pluginCallOptions: G?) {}
    open fun postGroup(userId: String?, groupId: String, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun track(userId: String?, event: Event, pluginCallOptions: T?) {}
    open fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>) {}

    open fun reset() {}
    open fun flush() {}
    open fun shutdown() {}
}
