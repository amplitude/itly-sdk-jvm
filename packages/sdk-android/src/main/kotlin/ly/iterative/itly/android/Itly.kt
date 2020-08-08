/**
 * Android Itly
 */
package ly.iterative.itly.android

import ly.iterative.itly.*
import ly.iterative.itly.core.Options

class Itly {
    companion object {
        const val ID = "itly-android"
        const val LOG_TAG = "[$ID]"
    }

    private val itly = ly.iterative.itly.core.Itly()

    @Throws(IllegalStateException::class)
    fun load(options: Options) {
        itly.load(options)
    }

    //ALIAS
    @Throws(IllegalStateException::class)
    fun alias(userId: String, previousId: String?) {
        itly.alias(userId, previousId)
    }
    @Throws(IllegalStateException::class)
    fun alias(userId: String) = alias(userId, null)


    // IDENTIFY
    @Throws(IllegalStateException::class)
    fun identify(userId: String?, properties: Properties?) {
        itly.identify(userId, properties)
    }
    @Throws(IllegalStateException::class)
    fun identify(userId: String?) = identify(userId, null)

    @Throws(IllegalStateException::class)
    fun identify(properties: Properties?) = identify(null, properties)


    // GROUP
    @Throws(IllegalStateException::class)
    fun group(groupId: String, properties: Properties?) {
        itly.group(null, groupId, properties)
    }

    @Throws(IllegalStateException::class)
    fun group(groupId: String) = group(groupId, null)

    // TRACK
    @Throws(IllegalArgumentException::class)
    fun track(event: Event) {
        itly.track(null, event)
    }

    fun reset() {
        itly.reset()
    }

    @Throws(IllegalArgumentException::class)
    fun validate(event: Event): ValidationResponse {
        return itly.validate(event)
    }

    fun flush() {
        itly.flush()
    }

    fun shutdown() {
        itly.shutdown()
    }
}
