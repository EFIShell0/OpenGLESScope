# OpenGLESScope 0.2.7 Build Audit

- Application version: 0.2.7
- versionCode: 207
- Package: com.efishell.openglesscope
- OpenGL ES baseline: 3.2
- GLSL ES baseline: 3.20
- EGL baseline: 1.5
- Release audit: PASS
- Manifest parse: PASS
- Obtainium config static verification: PASS
- Add to Obtainium runtime action: removed
- Obtainium informational guidance: retained
- Universal-APK filter: `(?i).*universal.*\.apk$`
- Obtainium architecture auto-filter: disabled
- Built-in direct updater: enabled by default on fresh installs
- Disabled-state first-install information banner logic is retained; a normal fresh install with the default-enabled updater proceeds directly to the non-blocking update check
- Current runtime source contains no IzzyOnDroid-specific messaging
- Existing package/signature/version/ABI update verification remains present
- OpenGL ES/EGL capability/report/Database behavior was not changed by this release

A full Gradle Kotlin/native compilation is not claimed unless completed by the validation environment.
- Gradle assembleRelease attempt: NOT COMPLETED because the wrapper could not resolve services.gradle.org in this environment
