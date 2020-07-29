/**
 * Configures Kotlin projects
 *
 * Based on https://github.com/AlainODea/gradle-com.example.hello-plugin
 */
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class JavaProjectPlugin : TestsPlugin() {
    override fun apply(project: Project) {
        super.apply(project)
    }
}
