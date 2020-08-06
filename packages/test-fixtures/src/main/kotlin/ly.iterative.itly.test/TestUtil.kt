package ly.iterative.itly.test

import ly.iterative.itly.*
import ly.iterative.itly.core.Itly
import ly.iterative.itly.core.Options

class TestUtil {
    companion object {
        fun getItly(options: Options = Options()): Itly {
            val itly = Itly()
            itly.load(options)
            return itly
        }
    }
}
