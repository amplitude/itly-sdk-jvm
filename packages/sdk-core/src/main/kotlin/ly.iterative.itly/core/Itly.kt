package ly.iterative.itly.core

import ly.iterative.itly.*
import java.util.concurrent.atomic.AtomicBoolean

open class Itly {
    companion object {
        const val LOG_TAG = "[itly-core]"
    }

    private lateinit var config: Options
    private var context: Event? = null

    private var isShutdown: AtomicBoolean = AtomicBoolean(false)

    private val disabled: Boolean
        @Throws(IllegalStateException::class)
        get() {
            if (isShutdown.get()) {
                throw IllegalStateException("Itly is shutdown. No more requests are possible.")
            }
            if (this::config.isInitialized) {
                return config.disabled
            }
            throw IllegalStateException("Itly is not initialized. Call Itly.load(Options(...))")
        }

    /**
     * Initialize the Itly instance
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
        if (this::config.isInitialized) {
            throw Error("Itly is already initialized. Itly.load() should only be called once.")
        }

        config = options
        config.logger.debug("$LOG_TAG load")
        if (config.disabled) {
            config.logger.info("$LOG_TAG disabled = true")
            return
        }

        if (context != null) {
            this.context = Event("context", context.properties)
        }

        config.logger.debug("$LOG_TAG ${config.plugins.size} plugins enabled")
        val pluginLoadOptions = PluginLoadOptions(config)
        runOnAllPlugins("load") { it.load(pluginLoadOptions) }
    }

    @Throws(IllegalStateException::class)
    fun alias(userId: String, previousId: String?, callOptions: CallOptions?) {
        if (disabled) {
            return
        }

        runOnAllPlugins("alias") { plugin -> plugin.alias(userId, previousId, callOptions?.get(plugin.id())) }
        runOnAllPlugins("postAlias") { it.postAlias(userId, previousId) }
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun alias(userId: String) = alias(userId, null, null)

    @Throws(IllegalStateException::class)
    fun identify(userId: String?, properties: Properties?, callOptions: CallOptions?) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "identify",
            Event("identify", properties?.properties),
            false,
            { plugin, data -> plugin.identify(userId, data, callOptions?.get(plugin.id())) },
            { plugin, data, validationResponses -> plugin.postIdentify(userId, data, validationResponses) }
        )
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun identify(userId: String?) = identify(userId, null, null)

    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String, properties: Properties?, callOptions: CallOptions?) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "group",
            Event("group", properties?.properties, "0"),
            false,
            { plugin, data -> plugin.group(userId, groupId, data, callOptions?.get(plugin.id())) },
            { plugin, data, validationResponses -> plugin.postGroup(userId, groupId, data, validationResponses) }
        )
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String) = group(userId, groupId, null, null)

    @Throws(IllegalArgumentException::class)
    fun track(userId: String?, event: Event, callOptions: CallOptions?) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "track",
            event,
            true,
            { plugin, data -> plugin.track(userId, data, callOptions?.get(plugin.id())) },
            { plugin, data, validationResponses -> plugin.postTrack(userId, data, validationResponses) }
        )
    }

    fun reset() {
        if (disabled) {
            return
        }

        runOnAllPlugins("reset") { it.reset() }
    }

    fun flush() {
        if (disabled) {
            return
        }

        runOnAllPlugins("flush") { it.flush() }
    }

    @Synchronized fun shutdown() {
        if (disabled) {
            return
        }

        isShutdown.getAndSet(true)
        runOnAllPlugins("shutdown") { it.shutdown() }
    }

    private fun runOnAllPlugins(op: String, name: String = "", method: (plugin: Plugin) -> Unit) {
        config.plugins.forEach {
            try {
                method(it)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${it.id()}.${op}(${name}). ${e.message}.")
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun validate(event: Event): List<ValidationResponse> {
        val validationResponses: MutableList<ValidationResponse> = mutableListOf()

        if (!config.validation.disabled) {
            runOnAllPlugins("validate") {
                val validation = it.validate(event)
                // Only add invalid validation responses
                if (validation != null && !validation.valid) {
                    validationResponses.add(validation)
                }
            }
        }

        return validationResponses
    }

    @Throws(IllegalArgumentException::class)
    private fun validateAndRunOnAllPlugins(
        op: String,
        event: Event,
        includeContext: Boolean,
        method: (plugin: Plugin, event: Event) -> Unit,
        postMethod: (plugin: Plugin, event: Event, validationResponses: List<ValidationResponse>) -> Unit
    ) {
        val contextValidationResponses = if (includeContext && context != null) validate(context!!) else listOf()
        val isContextValid = contextValidationResponses.all { it.valid }

        val eventValidationResponses = validate(event)
        val isEventValid = eventValidationResponses.all { it.valid }

        val combinedEvent = if (includeContext)
            Event(
                event.name,
                (context?.properties ?: mapOf()) + event.properties,
                event.id,
                event.version,
                event.metadata
            )
        else
            event

        if (
            (isContextValid && isEventValid)
            || config.validation.trackInvalid
        ) {
            runOnAllPlugins(op) { method(it, combinedEvent) }
        }

        val combinedValidationResponses = contextValidationResponses + eventValidationResponses
        runOnAllPlugins(op) { postMethod(it, combinedEvent, combinedValidationResponses) }

        if (
            (!isContextValid || !isEventValid)
            && config.validation.errorOnInvalid
        ) {
            throw IllegalArgumentException(combinedValidationResponses.find { !it.valid }?.message)
        }
    }
}
