package ly.iterative.itly.core

import ly.iterative.itly.*

open class Options @JvmOverloads constructor(
    /**
     * Additional context properties to add to all events. Default is none.
     */
    val context: Properties? = null,

    /**
     * The current environment (development or production). Default is development.
     */
    val environment: Environment = Environment.DEVELOPMENT,

    /**
     * Extend the Itly SDK by adding plugins for common analytics trackers, validation and more.
     */
    val plugins: List<Plugin> = arrayListOf(),

    /**
     * Whether calls to the Itly SDK should be no-ops. Default is false.
     */
    val disabled: Boolean = false,

    /**
     * Configure validation handling. Default is to track invalid events in production, but throw in other environments.
     */
    val validation: ValidationOptions = ValidationOptions(
        trackInvalid = environment == Environment.PRODUCTION,
        errorOnInvalid = environment != Environment.PRODUCTION
    ),

    /**
     * Logger. Default is no logging.
     */
    val logger: Logger = Logger.NONE
)
