package ly.iterative.itly

import ly.iterative.itly.core.Options

abstract class PluginBase: Plugin {
    override fun load(options: Options) {}
    override fun alias(userId: String, previousId: String?) {}
    override fun identify(userId: String?, properties: Properties?) {}
    override fun group(userId: String?, groupId: String, properties: Properties?) {}
    override fun track(userId: String?, event: Event) {}
    override fun reset() {}
    override fun validate(event: Event): ValidationResponse {
        return ValidationResponse(
            valid = true,
            pluginId = this.id()
        )
    }
    override fun onValidationError(validation: ValidationResponse, event: Event) {}
    override fun flush() {}
    override fun shutdown() {}
}
