package ly.iterative.itly.test

import ly.iterative.itly.*
import ly.iterative.itly.core.Itly
import ly.iterative.itly.core.Options
import ly.iterative.itly.test.events.*

class TestUtil {
    companion object {
        fun getItly(options: Options = Options()): Itly {
            val itly = Itly()
            itly.load(options)
            return itly
        }

        fun getDefaultSchemaValidator(): SchemaValidatorPlugin {
            return SchemaValidatorPlugin(Schemas.DEFAULT_SCHEMA)
        }
    }
}
