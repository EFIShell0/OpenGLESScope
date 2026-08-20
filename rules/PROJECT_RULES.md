# OpenGLESScope Engineering Rules

## Non-negotiable
- Source-code comments are forbidden.
- Security, correctness, memory safety, performance and usability are never traded away for convenience.
- No known security vulnerability or memory leak may knowingly ship.
- No guessed GPU, OpenGL ES, EGL, display or HDR capability may be reported.
- Unknown, unsupported, unavailable and not-applicable are distinct states.
- Runtime OpenGL ES and EGL extension names are displayed exactly as returned by the implementation.
- OpenGL ES version, GLSL ES version, EGL version and Android platform version are distinct values.
- Android Display HDR data is distinct from OpenGL ES/EGL capability data.
- Wide-color support must not be represented as a measured physical gamut percentage.
- Native collection must never block the UI thread.
- Large capability collections must remain lazy/searchable in the UI.
- Official Khronos OpenGL ES/EGL and Android documentation are primary API-behavior sources.
- Device/display data stays on-device unless the user explicitly submits a complete report.
- Submission excludes IMEI, Android ID, serial, MAC addresses, account data, authentication tokens and private paths.
- No automatic/background report upload is allowed.
- Runtime network access is limited to explicit database submission and the official OpenGLESScope GitHub update check/download flow.
- HTTPS hostnames remain hostnames through TLS. IPv6 is preferred on dual-stack with IPv4 fallback retained.

## ABI
- armeabi-v7a required.
- arm64-v8a required.
- x86_64 required.
- x86 intentionally excluded.

## Architecture
- Kotlin and Jetpack Compose for UI.
- Material 3 Expressive visual language.
- C++20 with EGL/OpenGL ES for native capability collection.
- JNI boundary remains small.
- No unrelated graphics-API loader or third-party driver bundle loading exists in OpenGLESScope.
- The active Android system EGL/OpenGL ES implementation is the authoritative runtime source.

## OpenGL ES collection
- GL_VENDOR, GL_RENDERER, GL_VERSION and GL_SHADING_LANGUAGE_VERSION are queried from a real current OpenGL ES context.
- OpenGL ES extensions are enumerated with glGetStringi when available and otherwise from the implementation extension string.
- Limits are queried with the corresponding OpenGL ES query and are never inferred from GPU model names.
- Compressed texture formats are reported only from GL_COMPRESSED_TEXTURE_FORMATS.
- Shader precision is reported from glGetShaderPrecisionFormat.
- Core-version feature state is derived only from the runtime GL_VERSION.
- Extension-backed feature state requires exact runtime extension-name evidence.

## EGL
- EGL vendor/version/client APIs and extensions are reported independently from OpenGL ES values.
- EGL configuration data comes from eglGetConfigs/eglGetConfigAttrib.
- EGL config attributes are raw implementation data; they must not be converted into unsupported display claims.

## Display and HDR
- Android Display HDR capabilities remain distinct from OpenGL ES and EGL.
- HDR luminance values are shown only when Android exposes them.
- An empty HDR type list is unavailable, not evidence that the GPU lacks HDR rendering capability.

## Database
- Application identity is OpenGLESScope with package com.efishell.openglesscope.
- Public web URL is https://efishell0.github.io/OpenGLESScope_database/.
- API base is https://openglesscope-database-api.openglesscope.workers.dev.
- Complete reports are all-or-nothing submissions; per-capability omission controls are forbidden.
- Request bodies are bounded to 2 MiB without truncation.
- Stored report IDs are SHA-256 hashes of stable canonical JSON.
- Pagination uses server-authored submitted_at/id ordering.

## Release 0.1.0
- Application and database version are 0.1.0.
- Package/namespace is com.efishell.openglesscope.
- Official release repository is EFIShell0/OpenGLESScope.
- Official database repository is EFIShell0/OpenGLESScope_database.
- Primary UI accent is blue throughout the application and database.
- Only OpenGL ES, EGL and Android display capability paths are permitted.
