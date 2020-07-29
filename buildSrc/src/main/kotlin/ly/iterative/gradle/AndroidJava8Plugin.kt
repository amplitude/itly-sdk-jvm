// FIXME: Need to fix classpath in build.gradle.kts and apply com.android.library
package ly.iterative.gradle

//import org.gradle.api.JavaVersion
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//class AndroidJava8Plugin : Plugin<Project> {
//    override fun apply(project: Project) {
//        // Configure common android build parameters.
//        val androidExtension = project.extensions.getByName("android")
//        if (androidExtension is BaseExtension) {
//            androidExtension.apply {
//                // Java 8
//                compileOptions {
//                    sourceCompatibility = JavaVersion.VERSION_1_8
//                    targetCompatibility = JavaVersion.VERSION_1_8
//                }
//                project.tasks.withType<KotlinCompile>(KotlinCompile::class.java).configureEach {
//                    kotlinOptions {
//                        jvmTarget = "1.8"
//                    }
//                }
//            }
//        }
//    }
//}
