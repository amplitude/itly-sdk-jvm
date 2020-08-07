package ly.iterative.itly.core

import ly.iterative.itly.*
import ly.iterative.itly.internal.OrgJsonProperties
import java.lang.Error
import java.util.HashMap
import kotlin.IllegalStateException

open class Itly {
    companion object {
        const val ID = "itly-core"
        const val LOG_TAG = "[$ID]"
    }

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

    fun id(): String {
        return ID
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

    @Throws(IllegalStateException::class)
    fun load(options: Options) {
        if (this::config.isInitialized) {
            throw Error("Itly is already initialized. Itly.load() should only be called once.")
        }

        if (options.disabled) {
            config.logger.info("$LOG_TAG disabled = true")
            return
        }

        this.config = options;
        config.logger.debug("$LOG_TAG load")

        updateEnabledPlugins();

        config.logger.debug("$LOG_TAG ${enabledPlugins.size} plugins enabled")

        enabledPlugins.forEach {
            try {
                it.load(config)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.load(). ${e.message}.")
            }
        }

        // Validate Context
        try {
            validate(Event("context", config.context?.properties))
        } catch (e: NoSuchElementException) {
            val contextPropertyCount = config.context?.properties?.size ?: 0
            if (contextPropertyCount > 0) {
                throw IllegalArgumentException(
                    "Error validating 'context'. Schema not found but received context=${OrgJsonProperties.toOrgJson(config.context)}"
                )
            }
        }
    }

    @Throws(IllegalStateException::class)
    fun alias(userId: String, previousId: String?) {
        if (this.disabled) {
            return
        }

        enabledPlugins.forEach {
            try{
                it.alias(userId, previousId)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.alias(). ${e.message}.")
            }
        }
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun alias(userId: String) = alias(userId, null)

    @Throws(IllegalStateException::class)
    fun identify(userId: String?, properties: Properties?) {
        if (this.disabled) {
            return
        }

        val identify = Event("identify", properties?.properties)
        if (shouldBeTracked(identify)) {
            enabledPlugins.forEach {
                try {
                    it.identify(userId, identify)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.identify(). ${e.message}.")
                }
            }
        }
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun identify(userId: String?) = identify(userId, null)

    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String, properties: Properties?) {
        if (this.disabled) {
            return
        }

        val group = Event("group", properties?.properties)
        if(shouldBeTracked(group)) {
            enabledPlugins.forEach {
                try{
                    it.group(userId, groupId, group)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.group(). ${e.message}.")
                }
            }
        }
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String) = group(userId, groupId, null)

    @Throws(IllegalArgumentException::class)
    fun track(userId: String?, event: Event) {
        if (this.disabled) {
            return
        }

        if (shouldBeTracked(event)) {
            config.logger.error("$LOG_TAG track:post-validate")
            enabledPlugins.forEach {
                try {
                    it.track(userId, event)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.track(${event.name}). ${e.message}.")
                }
            }
        }
    }

    fun reset() {
        enabledPlugins.forEach {
            try {
                it.reset()
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.reset(). ${e.message}.")
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    fun validate(event: Event): ValidationResponse {
        // Loop over plugins and stop if valid === false
        val validationResponses = arrayListOf<ValidationResponse>()
        this.enabledPlugins.forEach {
            val pluginValidation: ValidationResponse = try {
                it.validate(event)
            } catch (e: Error) {
                ValidationResponse(
                    valid = false,
                    pluginId = it.id(),
                    message = e.message
                )
            }
            validationResponses.add(pluginValidation)
        }

        // Get first invalid response or return valid=true
        val validation = validationResponses.firstOrNull {
            !it.valid
        } ?: ValidationResponse(
            valid = true,
            pluginId = this.id()
        )

        // If validation failed call validationError hook
        if (!validation.valid) {
            enabledPlugins.forEach {
                try {
                    it.validationError(validation, event)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.validationError(). ${e.message}.")
                }
            }

            if (config.validation.errorOnInvalid) {
                throw IllegalArgumentException(validation.message)
            }
        }

        return validation;
    }

    fun flush() {
        enabledPlugins.forEach {
            try {
                it.flush()
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.flush(). ${e.message}.")
            }
        }
    }

    fun shutdown() {
        enabledPlugins.forEach {
            try {
                it.shutdown()
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.shutdown(). ${e.message}.")
            }
        }
    }

    @Throws(IllegalStateException::class)
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
