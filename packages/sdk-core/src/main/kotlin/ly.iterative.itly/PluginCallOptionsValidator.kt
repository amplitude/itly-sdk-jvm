package ly.iterative.itly;

class PluginCallOptionsValidator {

    companion object {
        fun <T: PluginCallOptions> validate(given: PluginCallOptions?): T? {
            if (given == null) return null
            return (given as? T) ?: throw IllegalArgumentException()
        }
    }

}
