# OpenGLESScope 0.3.3

OpenGLESScope 0.3.3 is a full rules-driven correctness, reporting, security, lifecycle and resource-hygiene audit of the 0.3.2 codebase.

## Fixed

- Added separate query diagnostics for the count and value-array operations used by compressed texture, shader binary and program binary format enumeration.
- Added explicit `GL_NUM_EXTENSIONS` diagnostics to the OpenGL ES 3.x extension enumeration path.
- Corrected core-version presentation so UI, TXT and HTML state whether the version came from direct `GL_MAJOR_VERSION` / `GL_MINOR_VERSION` queries or from parsing the authoritative `GL_VERSION` string.
- Fixed a Direct GitHub updater lifecycle race: disabling the updater now cancels active metadata checks, APK downloads and their coroutines, clears pending state and prevents installation from continuing after opt-out.
- Failed APK package/signature/version validation now deletes the invalid cached target instead of leaving a potentially large stale file behind.
- Tightened signing-certificate rotation validation: the currently installed signer must be present in the candidate APK signing lineage; an older ancestor signer is not accepted merely because histories intersect.
- Updated README and release compatibility metadata to OpenGLESScope Database 0.2.5.

## Preserved

- Runtime-only OpenGL ES/EGL capability evidence; no GPU/vendor inference.
- OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5 engineering baselines.
- Exact runtime extension tokens and raw hexadecimal preservation for unknown format enums.
- Full limits, extensions, formats, shader precision, diagnostics, EGL Config and Display/HDR datasets in UI, TXT, HTML and Database technical reports.
- Non-exported isolated native probe process, 8 MiB result bound and 20-second probe timeout.
- Explicit all-or-nothing HTTPS Database submission with a 2 MiB payload bound.

Version: `0.3.3`  
versionCode: `303`
