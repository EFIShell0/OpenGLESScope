# 0.1.24

- Full application/Database 0.1.19 compatibility, capability, report, UI, security and specification audit.
- Added OpenGL ES 3.0 uniform-buffer and 3.1 shader-storage-buffer offset-alignment queries.
- Corrected GL query diagnostics, driver-version provenance, Android wide-color unavailable semantics and invalid HDR type handling.
- Added live display evidence refresh, full-field EGL Config search, HTML CSP/no-referrer hardening and stronger update version precedence.

# 0.1.23

- Matched Overview GPU vendor-logo container geometry to VulkanScope.
- No capability, report, Database, update-security or ABI-policy changes.

# 0.1.22

- Matched the Android system navigation-bar surface to VulkanScope using #111111 with dark navigation-bar icons and Android 10+ contrast enforcement disabled.
- Reworked the HTML report Application section to match VulkanScope information parity: version, versionCode, package, application ABI, supported device ABIs, developer, nickname and GitHub profile.
- Kept Android/device information in its own HTML section.
- Preserved the 0.1.19 export rule that excludes Database, Database API and source-repository URLs.
- No OpenGL ES, EGL, display/HDR, database-schema or capability-semantic changes.
