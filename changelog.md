# 0.1.20

- Fixed GitHub update checks returning HTTP 404 when the newest OpenGLESScope GitHub release is marked as a pre-release.
- Replaced the `/releases/latest` metadata dependency with a bounded official release-list query.
- Draft releases are excluded.
- Stable and pre-release entries are version-sorted locally and only a strictly newer dotted numeric version can become an update candidate.
- Retained ABI-specific APK selection with universal fallback.
- Retained strict official GitHub release-asset URL validation, explicit download confirmation, download bounds, package/signature verification and newer versionCode/versionName checks.
- No OpenGL ES, EGL, Display/HDR, report or database schema behavior changed.
