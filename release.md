# OpenGLESScope 0.1.20

Version name: `0.1.20`  
Version code: `120`

## Fixed

- Fixed update checking when the newest GitHub release is published as a pre-release.
- GitHub's `/releases/latest` endpoint excludes pre-releases and can return 404 when no stable release exists; OpenGLESScope now queries the bounded official release list instead.
- Draft releases are ignored.
- Published release candidates are sorted by parsed numeric version and must be newer than the installed application.

## Preserved update security

- Official repository only: `EFIShell0/OpenGLESScope`
- HTTPS GitHub release assets only
- ABI-specific asset selection with universal fallback
- Explicit confirmation before download
- Bounded APK download
- Package identity validation
- Signing-certificate validation
- Strictly newer `versionCode`
- Strictly newer `versionName`

OpenGL ES/EGL capability collection, reporting and Database compatibility are unchanged.
