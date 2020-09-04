package ly.iterative.itly

import ly.iterative.itly.core.Options

interface Plugin {
    // Plugin methods
    fun id(): String
    fun load(options: Options)

    // Validation methods
    fun validate(event: Event): ValidationResponse

    // Tracking methods
    fun alias(userId: String, previousId: String? = null)
    fun postAlias(userId: String, previousId: String? = null)

    fun identify(userId: String?, properties: Properties? = null)
    fun postIdentify(userId: String?, properties: Properties? = null, validationResults: List<ValidationResponse>)

    fun group(userId: String?, groupId: String, properties: Properties? = null)
    fun postGroup(userId: String?, groupId: String, properties: Properties? = null, validationResults: List<ValidationResponse>)

    fun track(userId: String?, event: Event)
    fun postTrack(userId: String?, event: Event, validationResults: List<ValidationResponse>)

    fun reset()
    fun flush()
    fun shutdown()
}
