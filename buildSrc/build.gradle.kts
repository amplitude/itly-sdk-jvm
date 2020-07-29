import org.gradle.kotlin.dsl.`kotlin-dsl`

// The classpath below doesnt work
//buildscript {
//    repositories {
//        google()
//        jcenter()
//        mavenCentral()
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:3.5.3")
//    }
//}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
//    id("com.android.library") apply false
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

// FIXME: These dependencies also apply to the subprojects...
//dependencies {
//    compileOnly(gradleApi())
//
//    implementation("com.android.tools.build:gradle:3.5.3")
//    implementation(kotlin("gradle-plugin", "1.3.72"))
//    implementation(kotlin("android-extensions"))
//    implementation(kotlin("stdlib-jdk8", "1.3.72"))
////    implementation("org.jacoco:org.jacoco.core:0.8.4")
//}


