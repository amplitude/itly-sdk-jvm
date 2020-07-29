package ly.iterative.itly.test

data class TrackerUrl(
    val user: User,
    val host: String = "http://localhost",
    val port: Int = 4201,
    val path: String = "/t/version/${user.companyId}",
    val url: String = "$host:$port$path"
)
