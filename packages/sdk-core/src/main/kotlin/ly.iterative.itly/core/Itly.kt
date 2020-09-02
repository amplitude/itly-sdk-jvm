package ly.iterative.itly.core

import ly.iterative.itly.*
import ly.iterative.itly.Properties
import ly.iterative.itly.internal.OrgJsonProperties
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

open class Itly {
    companion object {
        const val ID = "itly-core"
        const val LOG_TAG = "[$ID]"
    }

    private lateinit var config: Options

    private val pluginOptionsMap = Collections.synchronizedMap(HashMap<String, PluginOptions>())
    private val enabledPlugins = Collections.synchronizedList(ArrayList<Plugin>())
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

    fun id(): String {
        return ID
    }

    private fun enablePlugin(pluginId: String, enable: Boolean = true) {
        synchronized(pluginOptionsMap) {
            pluginOptionsMap[pluginId] = pluginOptionsMap[pluginId]?.copy(disabled = !enable)
                    ?: PluginOptions(disabled = !enable)
        }
        this.updateEnabledPlugins()
    }

    private fun disablePlugin(pluginId: String) {
        enablePlugin(pluginId, false)
    }

    private fun isPluginEnabled(pluginId: String): Boolean {
        synchronized(pluginOptionsMap) {
            return !(pluginOptionsMap[pluginId]?.disabled ?: false)
        }
    }

    private fun updateEnabledPlugins() {
        synchronized(enabledPlugins) {
            enabledPlugins.clear()
            enabledPlugins.addAll(config.plugins.filter {
                isPluginEnabled(it.id())
            })
        }
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

        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    it.load(config)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.load(). ${e.message}.")
                }
            }
        }

        // Validate Context
        val processedContext = process(Event("context", config.context?.properties))
        if (!processedContext.metadata.itly.validation.valid) {
            val message = processedContext.metadata.itly.validation.message
                    ?: "Invalid context=${OrgJsonProperties.toOrgJson(config.context)}"
            if (!(message.contains("No schema found") && config.context == null)) {
                throw IllegalArgumentException(message)
            }
        }
    }

    @Throws(IllegalStateException::class)
    fun alias(userId: String, previousId: String?) {
        if (this.disabled) {
            return
        }

        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    it.alias(userId, previousId)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.alias(). ${e.message}.")
                }
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
        processAndTrack(identify) { plugin, processedIdentify -> run {
            try {
                plugin.identify(userId, processedIdentify)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${plugin.id()}.identify(). ${e.message}.")
            }
        }}
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
        processAndTrack(group) { plugin, processedGroup -> run {
            try {
                plugin.group(userId, groupId, processedGroup)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${plugin.id()}.group(). ${e.message}.")
            }
        }}
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

        processAndTrack(event) { plugin, processedEvent -> run {
            try {
                plugin.track(userId, processedEvent)
            } catch (e: Exception) {
                config.logger.error("$LOG_TAG Error in ${plugin.id()}.track(${event.name}). ${e.message}.")
            }
        }}
    }

    fun reset() {
        if (this.disabled) {
            return
        }

        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    it.reset()
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.reset(). ${e.message}.")
                }
            }
        }
    }

    fun process(event: Event): Event {
        var processedEvent = event
        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    processedEvent = it.process(processedEvent)
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.process(). ${e.message}.")
                }
            }
        }
        return processedEvent
    }

    fun flush() {
        if (this.disabled) {
            return
        }

        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    it.flush()
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.flush(). ${e.message}.")
                }
            }
        }
    }

    @Synchronized fun shutdown() {
        if (this.disabled) {
            return
        }

        isShutdown.getAndSet(true)
        synchronized(enabledPlugins) {
            enabledPlugins.forEach {
                try {
                    it.shutdown()
                } catch (e: Exception) {
                    config.logger.error("$LOG_TAG Error in ${it.id()}.shutdown(). ${e.message}.")
                }
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processAndTrack(event: Event, trackMethod: (plugin: Plugin, processedEvent: Event) -> Unit) {
        val processedEvent = process(event)

        if (
            config.validation.disabled
            || processedEvent.metadata.itly.validation.valid
            || config.validation.trackInvalid
        ) {
            synchronized(enabledPlugins) {
                enabledPlugins.forEach {
                    trackMethod(it, processedEvent)
                }
            }
        }

        if (
            !config.validation.disabled
            && !processedEvent.metadata.itly.validation.valid
            && config.validation.errorOnInvalid
        ) {
            throw IllegalArgumentException(processedEvent.metadata.itly.validation.message)
        }
    }
}
