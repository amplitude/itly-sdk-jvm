package ly.iterative.itly.android

import ly.iterative.itly.*

class Options @JvmOverloads constructor(
    /**
     * Additional context properties to add to all events. Default is none.
     */
    context: Properties? = null,

    /**
     * The current environment (development or production). Default is development.
     */
    environment: Environment = Environment.DEVELOPMENT,

    /**
     * Extend the itly sdk by adding plugins for common analytics trackers, validation and more
     */
    plugins: List<Plugin> = arrayListOf(),

    /**
     * Whether calls to the Itly SDK should be no-ops. Default is false.
     */
    disabled: Boolean = false,

    /**
     * Configure validation handling
     */
    validation: ValidationOptions = ValidationOptions(
        errorOnInvalid = environment != Environment.PRODUCTION
    ),

    /**
     * Logger. Default is no logging.
     */
    logger: Logger = Logger.NONE
): ly.iterative.itly.core.Options(
    context = context,
    environment = environment,
    plugins = plugins,
    disabled = disabled,
    validation = validation,
    logger = logger
)
