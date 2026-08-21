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
