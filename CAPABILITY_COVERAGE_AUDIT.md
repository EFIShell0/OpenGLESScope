# OpenGLESScope 0.7.0 Capability Coverage Audit

OpenGLESScope 0.7.0 was re-audited against the current Khronos OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5 baselines and Android API 37 display/HDR behavior.

## Core OpenGL ES

The checked-in public capability-floor matrix retains all 145 observable external comparison reference OpenGL ES 2.0/3.0/3.1/3.2 implementation-limit rows. Core queries remain runtime-version-gated and direct; indexed work-group values remain independently queried.

## Additional implementation queries

49 OpenGLESScope-specific direct/core or exact-extension-gated implementation queries are tracked beyond the public floor. 0.7.0 adds 18 valid extension-backed query values covering shader subgroups, window rectangle limits, viewport arrays, pixel local storage, sample shading and sparse textures.

Mutable window-rectangle count state and the KHR parallel-shader-compile thread hint are intentionally excluded from capability-limit reporting because neither is an implementation maximum.

## EGL

EGL 1.5 configuration attributes remain enumerated with bounded `eglGetConfigs` / `eglGetConfigAttrib` handling. 0.7.0 adds current EGL API/context/surface evidence and extension-backed Android recordable/framebuffer-target and floating-component config attributes. Individual attribute failures retain their EGL errors.

## End-to-end evidence

Collected runtime identity, limits, GL/EGL extensions, formats, shader precision, diagnostics, EGL runtime/configs and Display/HDR data flow through in-app UI, TXT, HTML and schema-2/technicalReport-2 Database submission.

No capability is inferred from GPU/vendor identity. Unknown, unavailable, unsupported and not-applicable remain distinct.
