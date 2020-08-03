package ly.iterative.itly.core

import ly.iterative.itly.*
import java.lang.Error
import java.lang.IllegalStateException
import java.util.HashMap

class Itly: Plugin {
    private lateinit var config: Options
    private val pluginOptionsMap = HashMap<String, PluginOptions>()
    private val enabledPlugins: ArrayList<Plugin> = arrayListOf()

    private val disabled: Boolean
        @Throws(IllegalStateException::class)
        get() {
            if (this::config.isInitialized) {
                return config.disabled
            }
            throw IllegalStateException("Itly is not initialized. Call Itly.load(Options(...))")
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

    override fun load(options: Options) {
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
        validate(Event("context", config.context?.properties));
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
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    fun alias(userId: String) = alias(userId, null)

    override fun identify(userId: String?, properties: Properties?) {
        if (this.disabled) {
            return
        }

        val identify = Event("identify", properties?.properties)
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
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    fun identify(userId: String?) = identify(userId, null)

    override fun group(userId: String?, groupId: String, properties: Properties?) {
        if (this.disabled) {
            return
        }

        val group = Event("group", properties?.properties)
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
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    fun group(userId: String?, groupId: String) = group(userId, groupId, null)

    override fun track(userId: String?, event: Event) {
        if (this.disabled) {
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

    @Throws(java.lang.IllegalArgumentException::class)
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

            if (config.validation.errorOnInvalid) {
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
        if (!config.validation.disabled) {
            shouldTrack = validate(event).valid;
            if (config.validation.trackInvalid) {
                shouldTrack = true;
            }
        }
        return shouldTrack
    }
}
