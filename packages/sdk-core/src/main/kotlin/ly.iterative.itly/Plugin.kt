package ly.iterative.itly

import ly.iterative.itly.core.Options

abstract class Plugin(
    private val id: String
) {
    fun id(): String {
        return id
    }

    open fun load(options: Options) {}

    open fun validate(event: Event): ValidationResponse {
        return ValidationResponse.Valid
    }

    open fun alias(userId: String, previousId: String?) {}
    open fun postAlias(userId: String, previousId: String?) {}

    open fun identify(userId: String?, properties: Properties?) {}
    open fun postIdentify(userId: String?, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun group(userId: String?, groupId: String, properties: Properties?) {}
    open fun postGroup(userId: String?, groupId: String, properties: Properties?, validationResults: List<ValidationResponse>) {}

    open fun track(userId: String?, event: Event) {}
    open fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>) {}

    open fun reset() {}
    open fun flush() {}
    open fun shutdown() {}
}
