package ly.iterative.itly.core

import ly.iterative.itly.*
import ly.iterative.itly.Properties
import java.util.concurrent.atomic.AtomicBoolean

open class Itly {
    companion object {
        const val LOG_TAG = "[itly-core]"
    }

    private lateinit var config: Options
    private lateinit var context: Event

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

    @Throws(IllegalStateException::class)
    fun load(options: Options) {
        if (this::config.isInitialized) {
            throw Error("Itly is already initialized. Itly.load() should only be called once.")
        }

        if (options.disabled) {
            config.logger.info("$LOG_TAG disabled = true")
            return
        }

        config = options;
        context = Event("context", config.context?.properties)

        config.logger.debug("$LOG_TAG load")

        config.logger.debug("$LOG_TAG ${config.plugins.size} plugins enabled")
        runOnAllPlugins("load") { it.load(config) }
    }

    @Throws(IllegalStateException::class)
    fun alias(userId: String, previousId: String?) {
        if (disabled) {
            return
        }

        runOnAllPlugins("alias") { it.alias(userId, previousId) }
        runOnAllPlugins("postAlias") { it.postAlias(userId, previousId) }
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun alias(userId: String) = alias(userId, null)

    @Throws(IllegalStateException::class)
    fun identify(userId: String?, properties: Properties?) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "identify",
            Event("identify", properties?.properties),
            false,
            { plugin, data -> plugin.identify(userId, data) },
            { plugin, data, validationResponses -> plugin.postIdentify(userId, data, validationResponses) }
        )
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun identify(userId: String?) = identify(userId, null)

    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String, properties: Properties?) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "group",
            Event("group", properties?.properties),
            false,
            { plugin, data -> plugin.group(userId, groupId, data) },
            { plugin, data, validationResponses -> plugin.postGroup(userId, groupId, data, validationResponses) }
        )
    }
    // NOTE: Can't use @JvmOverload above since it is an interface method
    // NOTE: Need to manually override method instead
    @Throws(IllegalStateException::class)
    fun group(userId: String?, groupId: String) = group(userId, groupId, null)

    @Throws(IllegalArgumentException::class)
    fun track(userId: String?, event: Event) {
        if (disabled) {
            return
        }

        validateAndRunOnAllPlugins(
            "track",
            event,
            true,
            { plugin, data -> plugin.track(userId, data) },
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
                val response = it.validate(event)
                if (response != null) {
                    validationResponses.add(response)
                }
            }
        }

        return validationResponses
    }

    @Throws(IllegalArgumentException::class)
    private fun validateAndRunOnAllPlugins(
        op: String, event: Event,
        includeContext: Boolean,
        method: (plugin: Plugin, event: Event) -> Unit,
        postMethod: (plugin: Plugin, event: Event, validationResponses: List<ValidationResponse>) -> Unit
    ) {
        val contextValidationResponses = if (includeContext) validate(context) else listOf()
        val isContextValid = contextValidationResponses.all { it.valid }

        val eventValidationResponses = validate(event)
        val isEventValid = eventValidationResponses.all { it.valid }

        val combinedEvent = if (includeContext)
            Event(
                event.name,
                context.properties + event.properties,
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
