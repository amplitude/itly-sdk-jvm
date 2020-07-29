/**
 * Configures Kotlin projects
 *
 * Based on https://github.com/AlainODea/gradle-com.example.hello-plugin
 */
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class KotlinProjectPlugin : TestsPlugin() {
    override fun apply(project: Project) {
        super.apply(project)

        project.afterEvaluate {
            dependencies {
//                "implementation"(kotlin("stdlib"))
                "implementation"(kotlin("stdlib-jdk8"))
            }

//            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//                kotlinOptions.jvmTarget = "1.8"
//            }
//            tasks.named("kotlinCompile") {
//                this.get("kotlinOptions").jvmTarget = "1.8"
//            }
        }
        // config JVM target to 1.8 for kotlin compilation tasks
        //tasks.withType<KotlinCompile>().configureEach {
        //    kotlinOptions.jvmTarget = "1.8"
        //}
    }
}
