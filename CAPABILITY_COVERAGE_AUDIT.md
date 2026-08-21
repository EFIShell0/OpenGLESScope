# OpenGLESScope 0.2.1 Capability Coverage Audit

OpenGLESScope 0.2.1 was re-audited against the current Khronos OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5 baselines and current Android Display/HDR behavior.

The collector retains runtime GL_VENDOR, GL_RENDERER, GL_VERSION and GL_SHADING_LANGUAGE_VERSION evidence, parsed core version, exact runtime extension enumeration, version-gated core implementation limits, exact-extension-gated implementation queries, compressed texture formats, shader/program binary formats, vertex/fragment shader precision, EGL vendor/version/client APIs, EGL display/client extensions, EGL 1.5 configuration attributes and per-query diagnostics.

OpenGL ES 3.x extension enumeration uses glGetStringi and OpenGL ES 2.x uses GL_EXTENSIONS string enumeration. Runtime extension tokens are preserved exactly as returned.

Pre-3.2 GL_KHR_debug implementation limits remain exact-extension gated. GL_EXT_disjoint_timer_query counter-bit queries remain exact-extension gated and require a resolved glGetQueryivEXT entry point. Query failures remain explicit Unavailable diagnostics.

Android Display/HDR evidence stays separate from OpenGL ES/EGL capability evidence. API 34+ uses Display.Mode.supportedHdrTypes; older supported APIs use the legacy Display.HdrCapabilities list. HDR_TYPE_INVALID is filtered on all supported paths. Desired minimum, maximum and maximum-average luminance are represented in cd/m² in human-readable UI/TXT/HTML output.

No GPU vendor/model, Android release or marketing database is used to infer graphics capability support.
