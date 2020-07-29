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
