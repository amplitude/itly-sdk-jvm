package ly.iterative.itly

import ly.iterative.itly.core.Options

abstract class PluginBase(
    private val id: String
): Plugin {
    override fun id(): String {
        return id
    }
    override fun load(options: Options) {}
    override fun alias(userId: String, previousId: String?) {}
    override fun identify(userId: String?, properties: Properties?) {}
    override fun group(userId: String?, groupId: String, properties: Properties?) {}
    override fun track(userId: String?, event: Event) {}
    override fun reset() {}
    override fun validate(event: Event): ValidationResponse {
        return ValidationResponse(
            valid = true,
            pluginId = id()
        )
    }
    override fun onValidationError(validation: ValidationResponse, event: Event) {}
    override fun flush() {}
    override fun shutdown() {}
}
