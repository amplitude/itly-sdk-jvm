package ly.iterative.itly

import java.lang.Error
import java.util.HashMap

class ItlyCore: Plugin {
    private lateinit var config: OptionsCore
    private val pluginOptionsMap = HashMap<String, PluginOptions>()
    private val enabledPlugins: ArrayList<Plugin> = arrayListOf()

    private val disabled: Boolean
        get() {
            if (!this::config.isInitialized) {
                throw Error("Itly is not initialized. Call Itly.load(Options(...))")
            }
            return config.disabled
        }

    override fun id(): String {
        return "itly-core"
    }

    fun enablePlugin(pluginId: String, enable: Boolean = true) {
        pluginOptionsMap[pluginId] = pluginOptionsMap[pluginId]?.copy(disabled = !enable)
                ?: PluginOptions(disabled = !enable)
        this.updateEnabledPlugins()
    }

    fun disablePlugin(pluginId: String) {
        enablePlugin(pluginId, false)
    }

    fun isPluginEnabled(pluginId: String): Boolean {
        return !(pluginOptionsMap[pluginId]?.disabled ?: false)
    }

    private fun updateEnabledPlugins() {
        this.enabledPlugins.clear()
        this.enabledPlugins.addAll(config.plugins.filter {
            isPluginEnabled(it.id())
        })
    }

    override fun load(options: OptionsCore) {
        if (this::config.isInitialized) {
            throw Error("Itly is already initialized. Itly.load() should only be called once.")
        }

        if (options.disabled) {
            config.logger.info("[sdk] disabled = true")
            return
        }

        this.config = options;
        config.logger.debug("[sdk] load")

        updateEnabledPlugins();

        config.logger.debug("[sdk] ${enabledPlugins.size} plugins enabled")

        enabledPlugins.forEach {
            try {
                it.load(config)
            } catch (e: Exception) {
                val message = "Error in ${it.id()}.load(). ${e.message}."
                config.logger.error(message)
            }
        }

        // Validate Context
        validate(Event(
            name = "context",
            properties = config.context?.properties,
            id = "context",
            version = "0-0-0"
        ));
    }

    override fun alias(userId: String, previousId: String?) {
        if (this.disabled) {
            return
        }

        enabledPlugins.forEach {
            try{
                it.alias(userId, previousId)
            } catch (e: Exception) {
                val message = "Error in ${it.id()}.alias(). ${e.message}."
                config.logger.error(message)
            }
        }
    }

    override fun identify(userId: String?, properties: Properties?) {
        if (config.disabled) {
            return
        }

        val identify = Event(
            name = "identify",
            properties = properties?.properties
        )

        if (shouldBeTracked(identify)) {
            enabledPlugins.forEach {
                try {
                    it.identify(userId, identify)
                } catch (e: Exception) {
                    val message = "Error in ${it.id()}.identify(). ${e.message}."
                    config.logger.error(message)
                }
            }
        }
    }

    override fun group(userId: String?, groupId: String, properties: Properties?) {
        if (config.disabled) {
            return
        }

        val group = Event(
            name = "group",
            properties = properties?.properties
        )

        if(shouldBeTracked(group)) {
            enabledPlugins.forEach {
                try{
                    it.group(userId, groupId, group)
                } catch (e: Exception) {
                    val message = "Error in ${it.id()}.group(). ${e.message}."
                    config.logger.error(message)
                }
            }
        }
    }

    override fun track(userId: String?, event: Event) {
        if (config.disabled) {
            return
        }

        if (shouldBeTracked(event)) {
            enabledPlugins.forEach {
                try {
                    it.track(userId, event)
                } catch (e: Exception) {
                    val message = "Error in ${it.id()}.track(${event.name}). ${e.message}."
                    config.logger.error(message)
                }
            }
        }
    }

    override fun reset() {
        enabledPlugins.forEach {
            try {
                it.reset()
            } catch (e: Exception) {
                val message = "Error in ${it.id()}.reset(). ${e.message}."
                config.logger.error(message)
            }
        }
    }

    override fun validate(event: Event): ValidationResponse {
        var pluginId = this.id();

        // Default to true
        var validation = ValidationResponse(
            valid = true,
            pluginId = pluginId
        )

        // Loop over plugins and stop if valid === false
        try {
            this.enabledPlugins.all {
                pluginId = it.id()
                validation = it.validate(event)
                return validation
            }
        } catch (e: Error) {
            // Catch errors in validate() method
            validation = ValidationResponse(
                valid = false,
                pluginId = pluginId,
                message = e.message
            )
        }

        // If validation failed call validationError hook
        if (!validation.valid) {
            enabledPlugins.forEach {
                try {
                    it.validationError(validation, event)
                } catch (e: Exception) {
                    val message = "Error in ${it.id()}.validationError(). ${e.message}."
                    config.logger.error(message)
                }
            }

            if (config.validationOptions.errorOnInvalid) {
                throw IllegalArgumentException("Validation Error: ${validation.message}");
            }
        }

        return validation;
    }

    override fun validationError(validation: ValidationResponse, event: Event) {
        TODO("Not yet implemented")
    }

    override fun flush() {
        enabledPlugins.forEach {
            try {
                it.flush()
            } catch (e: Exception) {
                val message = "Error in ${it.id()}.flush(). ${e.message}."
                config.logger.error(message)
            }
        }
    }

    override fun shutdown() {
        enabledPlugins.forEach {
            try {
                it.shutdown()
            } catch (e: Exception) {
                val message = "Error in ${it.id()}.shutdown(). ${e.message}."
                config.logger.error(message)
            }
        }
    }

    private fun shouldBeTracked(event: Event): Boolean {
        var shouldTrack = true;
        if (!config.validationOptions.disabled) {
            shouldTrack = validate(event).valid;
            if (config.validationOptions.trackInvalid) {
                shouldTrack = true;
            }
        }
        return shouldTrack
    }
}
