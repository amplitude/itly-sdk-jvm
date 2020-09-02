package ly.iterative.itly

import org.jetbrains.annotations.Nullable

// TODO: Convert to data class?
open class Event @JvmOverloads constructor(
    val name: String,
    properties: Map<String, Any?>? = null,
    val id: String? = null,
    val version: String? = null,
    val metadata: EventMetadata = EventMetadata()
) : Properties(
    properties
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }
        other as Event

        return other.name == this.name &&
                other.id == this.id &&
                other.version == this.version &&
                super.equals(other as Properties)
    }

    override fun hashCode(): Int {
        var code = 0 + this.name.hashCode() + super.hashCode()

        if (this.id != null) code += this.id.hashCode()
        if (this.version != null) code += this.version.hashCode()

        return code
    }
}
