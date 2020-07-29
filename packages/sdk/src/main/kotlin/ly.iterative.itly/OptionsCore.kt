package ly.iterative.itly

class OptionsCore @JvmOverloads constructor(
    val context: Properties? = null,
    val environment: Environment = Environment.DEVELOPMENT,
    val plugins: List<Plugin> = arrayListOf(),
    val disabled: Boolean = false,
    val validationOptions: ValidationOptions = ValidationOptions(),
    val logger: Logger = Logger.NONE
)
