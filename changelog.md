# 0.2.8

- Aligned the Settings and Info top-app-bar icons with VulkanScope's Material 3 Expressive vector language.
- Matched VulkanScope action placement exactly: Info left, Settings right, with both hidden on Settings/Info destinations.
- Preserved Direct GitHub updates, Obtainium guidance and all OpenGL ES/EGL/Display/HDR collection and reporting behavior.

# 0.2.7

- Direct GitHub updates are now enabled by default on fresh installations so users receive release checks without prior setup.
- Existing users' saved update preference is preserved; disabling Direct GitHub updates still stops startup checks.
- Obtainium remains an optional external update manager and its informational guidance and portable configuration are retained.
- The Add to Obtainium button remains removed.

# 0.2.6

- Removed the Add to Obtainium action from Settings to keep update controls minimal.
- Kept Direct GitHub updates opt-in and disabled by default.
- Kept Obtainium guidance as informational text and retained the portable obtainium-config.json.

# 0.2.5

- Replaced IzzyOnDroid-specific update messaging with Obtainium-oriented update management.
- Added one-tap Obtainium import configured to select the universal APK from official GitHub Releases.
- Kept the built-in updater opt-in and disabled by default.
- Updated first-install and consent messaging to avoid duplicate update-manager notifications.
- Added `obtainium-config.json`.

# 0.2.4

- Added a one-time seven-second first-install information banner for the default-disabled direct GitHub updater, reusing the existing update-status banner design.
- Direct GitHub updates remain opt-in and disabled by default; IzzyOnDroid bypass consent remains mandatory.

## 0.2.2

- Added Android `Build.VERSION.SECURITY_PATCH` to the in-app Android information surface.
- Added canonical `Android security patch: YYYY-MM-DD` and `Security patch: YYYY-MM-DD` TXT evidence.
- Added Android security patch to self-contained HTML Device information.
- Added `device.securityPatch` to complete Database submissions, matching VulkanScope producer behavior and OpenGLESScope Database 0.2.2 validation.
- Preserved security-patch data as Android platform metadata only; no graphics capability is inferred from it.
- Updated release audits and version metadata to 0.2.2 / versionCode 202.

# 0.2.1

- Version bumped to 0.2.1 / versionCode 201.
- Full OpenGL ES 3.2 / GLSL ES 3.20 / EGL 1.5 source audit.
- Full Android Display/HDR audit.
- HDR desired luminance human-readable output verified as cd/m² in UI, TXT and HTML.
- HDR_TYPE_INVALID filtering corrected for every supported Android API path.
- Display listener work reduced for unrelated displays.
- UI/TXT/HTML/Database technical-report dataset parity re-audited.
- Native-probe lifecycle, memory/resource cleanup and network/update security re-audited.
- Database 0.1.26 producer-version incompatibility with truthful 0.2.1 submissions documented rather than bypassed.

## 0.2.3
- Made the direct GitHub self-updater opt-in and disabled by default.
- Added a Settings consent gate describing the official APK source and IzzyOnDroid screening/verification bypass.
- Added a root MIT license and Fastlane metadata for IzzyOnDroid repository ingestion.
- Preserved OpenGL ES/EGL capability and complete-report behavior.
