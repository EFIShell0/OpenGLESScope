# OpenGLESScope 0.1.24 Capability Coverage Audit

OpenGLESScope 0.1.24 was re-audited against the current Khronos OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5 baselines and against current Android Display/HDR API behavior.

## Correctness fixes

- Added direct OpenGL ES 3.0 `GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT` collection.
- Added direct OpenGL ES 3.1+ `GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT` collection.
- Added diagnostics for GL runtime identity strings and version-number queries.
- Avoids querying `GL_MAJOR_VERSION` / `GL_MINOR_VERSION` on OpenGL ES 2.x; the parsed `GL_VERSION` remains the fallback source.
- Removed the false implication that `GL_VERSION` is a standardized standalone driver-version value.
- Treats pre-API-26 Android wide-color evidence as unavailable rather than unsupported.
- Ignores Android `HDR_TYPE_INVALID` while preserving unknown valid integer HDR values as raw Android evidence.
- Refreshes display evidence after Android display-change callbacks.

All collected values continue to flow to the UI, TXT report, self-contained HTML report and Database technical report. Missing or unavailable evidence is not synthesized as unsupported.
