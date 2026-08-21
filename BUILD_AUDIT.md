# OpenGLESScope 0.2.2 Build Audit

- Application version: 0.2.2
- versionCode: 201
- Package: com.efishell.openglesscope
- compileSdk/targetSdk: 37
- minSdk: 24
- NDK: 29.0.14206865
- Required release ABIs: arm64-v8a, armeabi-v7a, x86_64
- x86: intentionally excluded

Static source/release audit passes for version identity, ABI policy, HDR cd/m² presentation, report-path completeness markers, native-probe isolation/bounds, database transport bounds and official endpoint restrictions.

A full Gradle compile/lint result is not claimed in this environment because Gradle 9.7 is not locally cached and the wrapper cannot download services.gradle.org from the execution environment.
