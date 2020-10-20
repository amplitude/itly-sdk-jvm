/**
 * Android Itly
 */
package ly.iterative.itly.android

import ly.iterative.itly.*
import ly.iterative.itly.Options

class Itly : IItly {
    companion object {
        const val ID = "itly-android"
        const val LOG_TAG = "[$ID]"
    }

    private val itly = ly.iterative.itly.core.Itly()

    /**
     * Initialize the Itly instance
     *
     * @param options
     */
    @Throws(IllegalStateException::class)
    fun load(options: Options = Options()) {
        load(null, options)
    }

    /**
     * Initialize the Itly instance
     *
     * @param context Additional context properties to add to all events. Default is none.
     * @param options
     */
    @Throws(IllegalStateException::class)
    fun load(context: Properties? = null, options: Options = Options()) {
        itly.load(context, options)
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
    override fun track(event: Event) {
        itly.track(null, event)
    }

    override fun reset() {
        itly.reset()
    }

    override fun flush() {
        itly.flush()
    }

    override fun shutdown() {
        itly.shutdown()
    }
}
