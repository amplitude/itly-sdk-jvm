package ly.iterative.itly

//import java.util.concurrent.ExecutorService

data class ValidationOptions(
    val disabled: Boolean = false,
    val trackInvalid: Boolean = false,
    val errorOnInvalid: Boolean = false
//    val executorService: ExecutorService? = null
)
