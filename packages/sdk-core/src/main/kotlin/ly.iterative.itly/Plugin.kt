package ly.iterative.itly

abstract class Plugin (
    private val id: String
) {
    fun id(): String {
        return id
    }

    open fun load(options: PluginLoadOptions) {}

    open fun validate(event: Event): ValidationResponse? {
        return null
    }

    open fun alias(userId: String, previousId: String?, options: PluginCallOptions?) {}
    open fun postAlias(userId: String, previousId: String?) {}

    open fun identify(userId: String?, properties: Properties?, options: PluginCallOptions?) {}
    open fun postIdentify(userId: String?, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun group(userId: String?, groupId: String, properties: Properties?, options: PluginCallOptions?) {}
    open fun postGroup(userId: String?, groupId: String, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun track(userId: String?, event: Event, options: PluginCallOptions?) {}
    open fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>) {}

    open fun reset() {}
    open fun flush() {}
    open fun shutdown() {}

    protected companion object {
        inline fun <reified T: PluginCallOptions> getTypedOptions(given: PluginCallOptions?): T? {
            if (given == null) return null
            return (given as? T) ?: throw IllegalArgumentException("Invalid PluginCallOptions. Given ${given.javaClass}, expected ${T::class}")
        }
    }
}
