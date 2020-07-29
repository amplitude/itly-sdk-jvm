private const val kotlinVersion = "1.3.72"
private const val orgJsonVersion = "20200518"
private const val okHttpVersion = "4.8.0"
private const val jUnitVersion = "5.6.2"
private const val androidGradleVersion = "3.0.1"

object Config {
    object BuildPlugins {
        val androidGradle = "com.android.tools.build:gradle:$androidGradleVersion"
        val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    }

    object Android {
        val buildToolsVersion = "27.0.3"
        val minSdkVersion = 19
        val targetSdkVersion = 27
        val compileSdkVersion = 27
        val applicationId = "com.antonioleiva.bandhookkotlin"
        val versionCode = 1
        val versionName = "0.1"
    }

    object Libs {
        val orgJson = "org.json:json:$orgJsonVersion"
        val okHttp = "com.squareup.okhttp3:okhttp:$okHttpVersion"
        val mockWebServer = "com.squareup.okhttp3:mockwebserver:$okHttpVersion"
        val jUnit = "org.junit.jupiter:junit-jupiter:$jUnitVersion"
    }
}

