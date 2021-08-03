package ly.iterative.itly

private val DEFAULT_OPTIONS = Options()

open class Options @JvmOverloads constructor(
    /**
     * The current environment (development or production). Default is development.
     */
    @JvmField
    val environment: Environment = Environment.DEVELOPMENT,

    /**
     * Extend the Itly SDK by adding plugins for common analytics trackers, validation and more.
     */
    @JvmField
    val plugins: List<Plugin<in PluginCallOptions, in PluginCallOptions, in PluginCallOptions, in PluginCallOptions>> = arrayListOf(),

    /**
     * Whether calls to the Itly SDK should be no-ops. Default is false.
     */
    @JvmField
    val disabled: Boolean = false,

    /**
     * Configure validation handling. Default is to track invalid events in production, but throw in other environments.
     */
    @JvmField
    val validation: ValidationOptions = defaultValidationOptionsForEnvironment(environment),

    /**
     * Logger. Default is no logging.
     */
    @JvmField
    val logger: Logger = Logger.NONE
) {
    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        private fun defaultValidationOptionsForEnvironment(environment: Environment): ValidationOptions {
            return ValidationOptions(
                trackInvalid = environment == Environment.PRODUCTION,
                errorOnInvalid = environment != Environment.PRODUCTION
            )
        }
    }

    /**
     * Returns new Options with the given overrides
     */
    @JvmOverloads
    fun withOverrides(
        environment: Environment? = null,
        plugins: List<Plugin<PluginCallOptions, PluginCallOptions, PluginCallOptions, PluginCallOptions>>? = null,
        disabled: Boolean? = null,
        validation: ValidationOptions? = null,
        logger: Logger? = null
    ): Options {
        return Options(
            environment = environment ?: this.environment,
            plugins = plugins ?: this.plugins,
            disabled = disabled ?: this.disabled,
            validation = validation ?: this.validation,
            logger = logger ?: this.logger
        )
    }

    data class Builder(
        private var environment: Environment = DEFAULT_OPTIONS.environment,
        private var disabled: Boolean = DEFAULT_OPTIONS.disabled,
        private var logger: Logger = DEFAULT_OPTIONS.logger,
        private var plugins: List<Plugin<in PluginCallOptions, in PluginCallOptions, in PluginCallOptions, in PluginCallOptions>> = DEFAULT_OPTIONS.plugins,
        private var validation: ValidationOptions = DEFAULT_OPTIONS.validation
    ) {
        fun environment(environment: Environment) = apply { this.environment = environment }
        fun disabled(disabled: Boolean) = apply { this.disabled = disabled }
        fun logger(logger: Logger) = apply { this.logger = logger }
        fun plugins(plugins: List<Plugin<PluginCallOptions, PluginCallOptions, PluginCallOptions, PluginCallOptions>>) = apply { this.plugins = plugins }
        fun validation(validation: ValidationOptions) = apply { this.validation = validation }

        fun build() = Options(
            environment = environment,
            plugins = plugins,
            disabled = disabled,
            logger = logger,
            validation = validation
        )
    }
}
