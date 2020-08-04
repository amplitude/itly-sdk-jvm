# itly-sdk-jvm
Iteratively SDK for Android, Kotlin and Java

# Modules
### Cross-platform
 * sdk
 * plugin-iteratively
 * plugin-schema-validator
### Android (Kotlin & Java)
 * plugin-amplitude-android
 * plugin-mixpanel-android
 * plugin-segment-android
### JRE (Kotlin & Java)
 * plugin-mixpanel-android
 * plugin-segment-android


# Dependencies
## Android
```groovy
implementation 'ly.iterative.itly:sdk:1.0'
implementation 'ly.iterative.itly:plugin-iteratively:1.0'
implementation 'ly.iterative.itly:plugin-schema-validator:1.0'
implementation 'ly.iterative.itly:plugin-amplitude-android:1.0'
implementation 'ly.iterative.itly:plugin-mixpanel-android:1.0'
implementation 'ly.iterative.itly:plugin-segment-android:1.0'
```

## JRE
```groovy
implementation 'ly.iterative.itly:sdk:1.0'
implementation 'ly.iterative.itly:plugin-iteratively:1.0'
implementation 'ly.iterative.itly:plugin-schema-validator:1.0'
implementation 'ly.iterative.itly:plugin-mixpanel-jvm:1.0'
implementation 'ly.iterative.itly:plugin-segment-jvm:1.0'
```

# Project structure
The `packages` directory contains all itly modules.

The `examples` directory contains sample apps for Kotlin and Java.

# Build

### Setup `local.properties`
To build for Android you need to set the `ANDROID_ROOT_SDK` path.

Create a `local.properties` and set `sdk.dir` to be the path to the Android SDK on your machine.
```
# local.properties
sdk.dir=/Users/path/Library/Android/sdk
``` 

There is a `local.properties.example` that can be used for reference.

### Run `./gradlew build`
```
# Build all projects
./gradlew build

# Build a single project
./gradlew :packages:sdk:build
```

# Publish To Maven
```
# Local
./gradlew clean build publishToMavenLocal
```
