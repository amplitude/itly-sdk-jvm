package ly.iterative.itly.core

import ly.iterative.itly.*

data class Options @JvmOverloads constructor(
    /**
     * Additional context properties to add to all events. Default is none.
     */
    val context: Properties? = null,

    /**
     * The current environment (development or production). Default is development.
     */
    val environment: Environment = Environment.DEVELOPMENT,

    /**
     * Extend the itly sdk by adding plugins for common analytics trackers, validation and more
     */
    val plugins: List<Plugin> = arrayListOf(),

    /**
     * Whether calls to the Itly SDK should be no-ops. Default is false.
     */
    val disabled: Boolean = false,

    /**
     * Configure validation handling
     */
    val validation: ValidationOptions = ValidationOptions(
        errorOnInvalid = environment != Environment.PRODUCTION
    ),

    /**
     * Logger. Default is no logging.
     */
    val logger: Logger = Logger.NONE
)
