package ly.iterative.itly.test

import ly.iterative.itly.*
import ly.iterative.itly.test.events.*

class TestUtil {
    companion object {
        fun getItly(options: OptionsCore = OptionsCore()): ItlyCore {
            val itly = ItlyCore()
            itly.load(options)
            return itly
        }

        fun loadDefaultSchemaValidator(validation: ValidationOptions = ValidationOptions()): SchemaValidatorPlugin {
            val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA, validation)

            schemaValidatorPlugin.load(OptionsCore(
                environment = Environment.PRODUCTION,
                context = Context.VALID_ONLY_REQUIRED_PROPS,
                validation = validation
            ))

            return schemaValidatorPlugin
        }
    }
}
