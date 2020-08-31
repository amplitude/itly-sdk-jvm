package ly.iterative.itly

import java.util.Arrays

open class Properties @JvmOverloads constructor(
    properties: Map<String, Any?>? = null
) {
    val properties: Map<String, Any?> = properties ?: mapOf();

    companion object {
        @JvmStatic
        fun combine(properties: Map<String, Any?>): Properties {
            return Properties(properties)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }
        other as Properties

        if (this.properties.keys != other.properties.keys) {
            return false
        }

        this.properties.entries.forEach { entry ->
            val otherValue = other.properties[entry.key]

            if (entry.value === otherValue) {
                return@forEach
            }

            if (entry.value?.javaClass != otherValue?.javaClass) {
                return false
            }

            if (!when (val value = entry.value) {
                is Array<*> -> Arrays.equals(value, otherValue as Array<*>)
                else -> (value == otherValue)
            }) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var code = 0
        this.properties.entries.forEach { entry ->
            code += entry.key.hashCode() + when (val value = entry.value) {
                is Array<*> -> Arrays.hashCode(value)
                else -> value.hashCode()
            }
        }

        return code
    }
}
