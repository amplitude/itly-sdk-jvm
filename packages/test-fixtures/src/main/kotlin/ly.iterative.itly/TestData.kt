package ly.iterative.itly

data class User(
    val id: String = "user-id",
    val companyId: String = "company-id",
    val groupId: String = "group-id",
    val apiKey: String = "api-key"
)

data class TrackerUrl(
    val user: User,
    val host: String = "http://localhost",
    val port: Int = 4201,
    val path: String = "/t/version/${user.companyId}",
    val url: String = "$host:$port$path"
)
