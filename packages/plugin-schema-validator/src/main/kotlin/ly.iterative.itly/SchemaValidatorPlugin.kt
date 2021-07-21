package ly.iterative.itly

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.lang.Exception

open class SchemaValidatorCallOptions : PluginCallOptions()
class SchemaValidatorAliasOptions : SchemaValidatorCallOptions()
class SchemaValidatorGroupOptions : SchemaValidatorCallOptions()
class SchemaValidatorIdentifyOptions : SchemaValidatorCallOptions()
class SchemaValidatorTrackOptions : SchemaValidatorCallOptions()

class SchemaValidatorPlugin constructor(
    private val schemas: Map<String, String>
): Plugin<SchemaValidatorAliasOptions, SchemaValidatorIdentifyOptions, SchemaValidatorGroupOptions, SchemaValidatorTrackOptions>(ID) {
    companion object {
        const val ID = "schema-validator"
        private const val LOG_TAG = "[plugin-$ID]"
    }

    private lateinit var validators: Map<String, JsonSchema>
    private lateinit var logger: Logger

    override fun load(options: PluginLoadOptions) {
        // Get a reference to the SDK logger
        logger = options.logger
        logger.debug("$LOG_TAG load")

        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        this.validators = schemas.map {
            it.key to factory.getSchema(it.value)
        }.toMap()
    }

    override fun validate(event: Event): ValidationResponse {
        logger.debug("$LOG_TAG validate(event=${event.name})")

        var errorMessage: String? = null
        try {
            val validator = this.validators.getValue(getSchemaKey(event))
            val errors = validator.validate(JacksonProperties.toJackson(event))
            logger.debug("$LOG_TAG errors=$errors")
            if (errors.size > 0) {
                val builder = StringBuilder()
                errors.forEach {
                    builder.append(it.message)
                }
                errorMessage = "Error validating '${event.name}'. $builder."
            }
        } catch (e: NoSuchElementException) {
            errorMessage = "No schema found for '${event.name}'. Received ${event.name}=${JacksonProperties.toJackson(event)}"
        } catch (e: Exception) {
            errorMessage = "Unhandled exception validating '${event.name}'. ${e.message}"
        }

        if (errorMessage != null) {
            return ValidationResponse(
                valid = false,
                message = errorMessage,
                pluginId = this.id()
            )
        }

        return ValidationResponse(
            valid = true,
            pluginId = this.id()
        )
    }

    fun getSchemaKey(event: Event): String {
        return event.name
    }
}
