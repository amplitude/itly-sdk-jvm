# itly-sdk-jvm
Iteratively SDK for Android, Kotlin and Java

# Modules
### Android (Kotlin & Java)
 * sdk-android
 * plugin-amplitude-android
 * plugin-mixpanel-android
 * plugin-segment-android
### JRE (Kotlin & Java)
 * sdk-jvm
 * plugin-segment-jvm
### Cross-platform
 * plugin-iteratively
 * plugin-schema-validator


# Dependencies
## Android
```groovy
implementation 'ly.iterative.itly:sdk-android:1.0.1'
implementation 'ly.iterative.itly:plugin-iteratively:1.0.1'
implementation 'ly.iterative.itly:plugin-schema-validator:1.0.1'
implementation 'ly.iterative.itly:plugin-amplitude-android:1.0.1'
implementation 'ly.iterative.itly:plugin-mixpanel-android:1.0.1'
implementation 'ly.iterative.itly:plugin-segment-android:1.0.1'
```

## JRE
```groovy
implementation 'ly.iterative.itly:sdk-jvm:1.0.1'
implementation 'ly.iterative.itly:plugin-iteratively:1.0.1'
implementation 'ly.iterative.itly:plugin-schema-validator:1.0.1'
implementation 'ly.iterative.itly:plugin-segment-jvm:1.0.1'
```

# Project structure
The `packages` directory contains all itly modules.

The `examples` directory contains sample apps for Kotlin and Java.

# Build

### Setup `local.properties`
To build you will need to set some additional properties.

For Android you need to set the `ANDROID_ROOT_SDK` path, or `sdk.dir` in a `local.properties` file.

Signing and publishing also require user specific properties.

There is a `local.properties.example` that can be used for reference. Rename this file `local.properties` and set the required values.

Some properties in the `local.properties.example` need to be set in the user `gradle.properties` to work correctly.

### Run `./gradlew build`
```
# Build all projects
./gradlew build

# Build a single project
./gradlew :packages:sdk:build
```

# Set JAVA_HOME and MAVEN_LOCAL environment vars
```shell
export JAVA_HOME="/Users/<you>/Library/Java/JavaVirtualMachines/<jdk-X.Y.Z>/Contents/Home"
export MAVEN_LOCAL="/Users/<you>/.m2/repository"
export GRADLE_USER_HOME="/Users/<you>/.gradle/"
```

# Install Maven
```shell
brew install maven
mvn --version
```

# Publish To Maven
```shell
# Local
./gradlew clean build publishToMavenLocal
```

# Known Errors
Sometimes the `dokkaHtml` task fails. The workaround is to disable it for a run, which will fix it on subsequent runs.
```shell
./gradlew build -x dokkaHtml
./gradlew build
```