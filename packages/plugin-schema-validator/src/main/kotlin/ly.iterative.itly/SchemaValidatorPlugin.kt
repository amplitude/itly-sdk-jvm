package ly.iterative.itly

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import ly.iterative.itly.core.Options

//import java.util.concurrent.ExecutorService

//data class SchemaValidatorOptions @JvmOverloads constructor(
//    val executorService: ExecutorService? = null
//)

class SchemaValidatorPlugin @JvmOverloads constructor(
    private val schemas: Map<String, String>,
    options: ValidationOptions = ValidationOptions()
): PluginBase() {
    companion object {
        @JvmField
        val ID = "schema-validator"
    }

    private val config: ValidationOptions
    private lateinit var validators: Map<String, JsonSchema>
    private lateinit var logger: Logger

    init {
        this.config = options
    }

    override fun id(): String { return ID }

    override fun load(options: Options) {
        // Get a reference to the SDK logger
        logger = options.logger
        logger.debug("[plugin-schema-validator] load")

        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        this.validators = schemas.map {
            it.key to factory.getSchema(it.value)
        }.toMap()
    }

    override fun validate(event: Event): ValidationResponse {
        logger.debug("[validate] event: $event")
        val validator = this.validators.getValue(event.name)
        logger.debug("validator: $validator")
        val errors = validator.validate(JacksonProperties.toJackson(event))
        logger.debug("errors: $errors")
        if (errors.size > 0) {
            val builder = StringBuilder()
            errors.forEach {
                builder.append(it.message)
            }

            val message = "(Itly) Error validating event ${event.name} ($builder)."

            if (this.config.errorOnInvalid) {
                throw IllegalArgumentException(message)
            }

            return ValidationResponse(
                valid = false,
                message = message,
                pluginId = this.id()
            )
        }

        return ValidationResponse(
            valid = true,
            pluginId = this.id()
        )
    }

//    override fun validationError(validationResponse: ValidationResponse, event: Event) {
//        val schemaKey = this.getSchemaKey(event);
//        this.validationErrorHandler(validation, event, this.schemas[schemaKey]);
//    }

    fun getSchemaKey(event: Event): String {
        return event.name;
    }
}

//import Ajv from 'ajv';
//import {
//Event,
//PluginBase,
//ValidationResponse,
//} from '@itly/sdk';
//
//export type ValidationResponseHandler = (
//validation: ValidationResponse,
//event: Event,
//schema: any
//) => any;
//
//export type SchemaMap = { [schemaKey: string]: any };
//
//const SYSTEM_EVENTS = ['identify', 'context', 'group', 'page'];
//function isSystemEvent(name: string) {
//    return SYSTEM_EVENTS.includes(name);
//}
//
//function isEmpty(obj: any) {
//    return obj === undefined || Object.keys(obj).length === 0;
//}
//
//export default class SchemaValidatorPlugin extends PluginBase {
//    static ID: string = 'schema-validator';
//
//    private ajv?: Ajv.Ajv;
//
//    private validators?: { [schemaKey: string]: Ajv.ValidateFunction };
//
//    constructor(
//            private schemas: SchemaMap,
//            private validationErrorHandler: ValidationResponseHandler = () => {},
//    ) {
//        super();
//    }
//
//    id = () => SchemaValidatorPlugin.ID;
//
//    load() {
//        this.ajv = new Ajv();
//        this.validators = {};
//    }
//
//    validate(event: Event): ValidationResponse {
//        const schemaKey = this.getSchemaKey(event);
//        // Check that we have a schema for this event
//        if (!this.schemas[schemaKey]) {
//            if (isSystemEvent(schemaKey)) {
//                // pass system events by default
//                if (isEmpty(event.properties)) {
//                    return {
//                        valid: true,
//                        pluginId: this.id(),
//                    };
//                }
//
//                return {
//                    valid: false,
//                    message: `'${event.name}' schema is empty but properties were found. properties=${JSON.stringify(event.properties)}`,
//                    pluginId: this.id(),
//                };
//            }
//
//            return {
//                valid: false,
//                message: `Event ${event.name} not found in tracking plan.`,
//                pluginId: this.id(),
//            };
//        }
//
//        // Compile validator for this event if needed
//        const validators = this.validators!;
//        if (!validators[schemaKey]) {
//            validators[schemaKey] = this.ajv!.compile(this.schemas[schemaKey]);
//        }
//
//        const validator = validators[schemaKey]!;
//        if (event.properties && !(validator(event.properties) === true)) {
//            const errorMessage = validator.errors
//            ? validator.errors.map((e: any) => {
//                let extra = '';
//                if (e.keyword === 'additionalProperties') {
//                    extra = ` (${e.params.additionalProperty})`;
//                }
//                return `\`properties${e.dataPath}\` ${e.message}${extra}.`;
//            }).join(' ')
//            : 'An unknown error occurred during validation.';
//
//            return {
//                valid: false,
//                message: `Passed in ${event.name} properties did not validate against your tracking plan. ${errorMessage}`,
//                pluginId: this.id(),
//            };
//        }
//
//        return {
//            valid: true,
//            pluginId: this.id(),
//        };
//    }
//
//    validationError(validation: ValidationResponse, event: Event) {
//        const schemaKey = this.getSchemaKey(event);
//        this.validationErrorHandler(validation, event, this.schemas[schemaKey]);
//    }
//
//    getSchemaKey(event: Event) {
//        return event.name;
//    }
//}
