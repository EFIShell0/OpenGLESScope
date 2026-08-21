# OpenGLESScope 0.1.24

OpenGLESScope 0.1.24 is an end-to-end capability correctness, reporting, Database compatibility, usability, update-ordering and security-hardening release.

## Changes

- Re-audited the producer contract against OpenGLESScope Database 0.1.19.
- Added `GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT` for OpenGL ES 3.0+.
- Added `GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT` for OpenGL ES 3.1+.
- Added explicit diagnostics for GL runtime identity/version queries and stopped querying GL 3.x major/minor state on OpenGL ES 2.x.
- Corrected the Database driver-version field so `GL_VERSION` is not mislabeled as a standardized standalone driver version.
- Corrected Android wide-color semantics on API 24-25 from false/unsupported to unavailable.
- Filters Android `HDR_TYPE_INVALID` from mode HDR capability presentation.
- Refreshes display/HDR evidence when Android reports display changes.
- Expanded EGL Config search to every displayed configuration attribute.
- Added clearer driver/provenance information to OpenGL ES UI, TXT and HTML output.
- Added a restrictive local CSP and no-referrer policy to generated HTML reports.
- Hardened version ordering for stable/pre-release and SemVer build-metadata cases.
- Removed transient Python bytecode/cache content from the source release.

## Compatibility

- Application: `0.1.24`
- versionCode: `124`
- Package: `com.efishell.openglesscope`
- OpenGL ES baseline: `3.2`
- GLSL ES baseline: `3.20`
- EGL baseline: `1.5`
- Validated Database contract: `OpenGLESScope Database 0.1.19`
