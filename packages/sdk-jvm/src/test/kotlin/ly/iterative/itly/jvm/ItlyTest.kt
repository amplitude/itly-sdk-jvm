package ly.iterative.itly.jvm

import org.junit.jupiter.api.*

class ItlyJvmTest {
    @Test
    fun SdkJvm_instantiation_succeeds() {
        val itly = Itly()
        Assertions.assertNotNull(itly)
    }
}
