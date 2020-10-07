package ly.iterative.itly

class PluginLoadOptions(
    /**
     * The current environment (development or production).
     */
    val environment: Environment,

    /**
     * Itly logger
     */
    val logger: Logger
) {
    constructor(options: Options) : this(
        environment = options.environment,
        logger = options.logger
    )
}
