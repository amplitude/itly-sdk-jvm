/**
 * Configures a projects 'test' task to support JUnit 5 tests + Jacoco coverage
 *
 * Based on https://github.com/AlainODea/gradle-com.example.hello-plugin
 */
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

open class TestsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            apply(plugin = "jacoco")

            val jUnitImplementation = properties["jUnitImplementation"] as String

            dependencies {
                "testImplementation"(jUnitImplementation)
            }

            tasks.named<Test>("test") {
                useJUnitPlatform()
                testLogging {
                    events("passed", "skipped", "failed")
                }
                finalizedBy(tasks.getByName("jacocoTestReport"))
            }
        }
    }
}
