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

        fun getSchemaValidator(validationOptions: ValidationOptions = ValidationOptions()): SchemaValidatorPlugin {
            val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA, validationOptions)

            schemaValidatorPlugin.load(OptionsCore(
                context = Context.VALID_ONLY_REQUIRED_PROPS,
                validationOptions = validationOptions
            ))

            return schemaValidatorPlugin
        }
    }
}
