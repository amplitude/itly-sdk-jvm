package ly.iterative.itly

interface Logger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)

    companion object {
        @JvmField
        val STD_OUT_AND_ERR: Logger = object : Logger {
            override fun debug(message: String) {
                println(message)
            }
            override fun info(message: String) {
                println(message)
            }
            override fun warn(message: String) {
                println(message)
            }
            override fun error(message: String) {
                System.err.println(message)
            }
        }

        @JvmField
        val NONE: Logger = object : Logger {
            override fun debug(message: String) {}
            override fun info(message: String) {}
            override fun warn(message: String) {}
            override fun error(message: String) {}
        }
    }
}
