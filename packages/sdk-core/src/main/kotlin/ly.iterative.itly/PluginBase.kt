package ly.iterative.itly

import ly.iterative.itly.core.Options

abstract class PluginBase(
    private val id: String
): Plugin {
    override fun id(): String {
        return id
    }
    override fun load(options: Options) {}

    override fun validate(event: Event): ValidationResponse {
        return ValidationResponse.Valid
    }

    override fun alias(userId: String, previousId: String?) {}
    override fun postAlias(userId: String, previousId: String?) {}

    override fun identify(userId: String?, properties: Properties?) {}
    override fun postIdentify(userId: String?, properties: Properties?, validationResults: List<ValidationResponse>) {}

    override fun group(userId: String?, groupId: String, properties: Properties?) {}
    override fun postGroup(userId: String?, groupId: String, properties: Properties?, validationResults: List<ValidationResponse>) {}

    override fun track(userId: String?, event: Event) {}
    override fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>) {}

    override fun reset() {}
    override fun flush() {}
    override fun shutdown() {}
}
