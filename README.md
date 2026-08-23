# OpenGLESScope 0.3.3

OpenGLESScope is an Android OpenGL ES, EGL and Display/HDR capability inspection and reporting application.

## 0.3.3

This release is a full correctness, reporting, update-lifecycle, security and resource-hygiene audit against the current Khronos OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5 baselines, Android API 37 Display/HDR semantics and OpenGLESScope Database 0.2.5.

Highlights:

- Application version 0.3.3, versionCode 303.
- Every attempted format count/list query now records its own query diagnostic, including compressed texture, shader binary and program binary format enumeration.
- `GL_NUM_EXTENSIONS` gets its own enumeration diagnostic on OpenGL ES 3.x instead of being represented only indirectly by the extension-list result.
- UI, TXT and HTML distinguish direct `GL_MAJOR_VERSION` / `GL_MINOR_VERSION` provenance from fallback parsing of `GL_VERSION`.
- Disabling Direct GitHub updates immediately cancels active metadata/download HTTP calls and update coroutines, clears pending update state and prevents a package installer launch after opt-out.
- APKs that fail package/signature/version validation are deleted from the private update cache.
- Native EGL/OpenGL ES cleanup, isolated probe-process limits, report bounds and all-or-nothing Database submission remain intact.

## Database 0.2.5 compatibility

The current payload remains schema-v2 / technicalReport-v1. OpenGLESScope Database 0.2.5 accepts the truthful producer mapping `0.3.3` / versionCode `303` under the compatible 0.x producer contract; no application-side schema mutation is required for this release. The Database, TXT and HTML outputs retain the complete technical datasets, including the more granular query diagnostics introduced here.

## Engineering baseline

- OpenGL ES: 3.2
- GLSL ES: 3.20
- EGL: 1.5
- Android compile/target API: 37
- Minimum Android API: 24
- Native language: C++20
- UI: Kotlin, Jetpack Compose and Material 3
- ABIs: arm64-v8a, armeabi-v7a, x86_64; x86 intentionally excluded

## Obtainium update management

The built-in direct GitHub updater is enabled by default on fresh installations so users receive release checks without setup. Users can disable it in Settings, especially when Obtainium is used as the external update manager; running both may produce duplicate update checks or notifications. A portable `obtainium-config.json` is included in the source tree.
