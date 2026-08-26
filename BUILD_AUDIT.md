# OpenGLESScope 0.7.2 Build / Release Audit

- Version: 0.7.2
- versionCode: 702
- Compile / target API: 37
- Minimum API: 24
- Android Gradle Plugin: 9.3.2
- Gradle wrapper: 9.7.1
- NDK: 29.0.14206865
- Release ABIs: arm64-v8a, armeabi-v7a, x86_64
- Submission schema: 2
- technicalReport schema: 2
- Companion Database: 0.7.3

The 0.7.2 release verifier gates complete EGL Analysis evidence, OpenGL ES/EGL query-graph parity, searchable formats/precision, separated limit/diagnostic accounting, expanded Android provenance, application ABI submission metadata, clean source-archive policy and the Material 3 Expressive opt-in required by the Analysis loading indicator.

## User build regression reproduced from evidence

The reported release build completed native CMake work for arm64-v8a, armeabi-v7a and x86_64 and reached `:app:compileReleaseKotlin`, where `MainActivity.kt` failed because `LoadingIndicator()` is an experimental Material 3 Expressive API. The Analysis composable now carries `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`, matching the shared application implementation pattern. No capability/evidence behavior was changed by this fix.

## Build attempt in the release environment

A real `./gradlew :app:assembleRelease --no-daemon` attempt was made after the source correction. The wrapper could not obtain Gradle 9.7.1 because `services.gradle.org` could not be resolved (`java.net.UnknownHostException`). The Android compile phase therefore could not be re-run in this isolated environment and no local full APK PASS is claimed here. Source release verification and archive hygiene passed independently.
