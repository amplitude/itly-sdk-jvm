package ly.iterative.itly

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import ly.iterative.itly.core.Options

class SchemaValidatorPlugin @JvmOverloads constructor(
    private val schemas: Map<String, String>
): PluginBase() {
    companion object {
        const val ID = "schema-validator"
        private const val LOG_TAG = "[plugin-$ID]"
    }

    private lateinit var validators: Map<String, JsonSchema>
    private lateinit var logger: Logger

    override fun id(): String { return ID }

    override fun load(options: Options) {
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

        val validator = this.validators.getValue(getSchemaKey(event))
        logger.debug("$LOG_TAG validator=$validator")

        val errors = validator.validate(JacksonProperties.toJackson(event))
        logger.debug("$LOG_TAG errors=$errors")

        if (errors.size > 0) {
            val builder = StringBuilder()
            errors.forEach {
                builder.append(it.message)
            }

            return ValidationResponse(
                valid = false,
                message = "Error validating event '${event.name}'. $builder.",
                pluginId = this.id()
            )
        }

        return ValidationResponse(
            valid = true,
            pluginId = this.id()
        )
    }

    fun getSchemaKey(event: Event): String {
        return event.name;
    }
}
