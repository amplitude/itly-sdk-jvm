package ly.iterative.itly.test

import ly.iterative.itly.*
import ly.iterative.itly.core.Itly
import ly.iterative.itly.Options

class TestUtil {
    companion object {
        fun getItly(options: Options = Options()): Itly {
            return getItly(null, options)
        }

        fun getItly(context: Properties? = null, options: Options = Options()): Itly {
            val itly = Itly()
            itly.load(context, options)
            return itly
        }
    }
}
