package ly.iterative.itly

open class PluginCallOptions @JvmOverloads constructor(
        properties: Map<String, Any?>? = null
) {
    val properties: Map<String, Any?> = properties ?: mapOf()
}
