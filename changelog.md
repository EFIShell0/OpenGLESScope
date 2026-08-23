# 0.3.4

- Fixed duplicate query-diagnostic records introduced by 0.3.3 when count queries were reused by Limits and runtime-format enumeration.
- Canonical diagnostic names are now unique; repeated internal reads merge into one conservative evidence record instead of producing duplicate names rejected by Database validation.
- Preserves separate count and array diagnostics for extension, compressed texture, shader binary and program binary enumeration.
- Restores OpenGLESScope Database complete-report submission compatibility.

# 0.3.3

- Completed a full rules-driven correctness, reporting, security and resource-hygiene audit.
- Added per-query diagnostics for format count/value enumeration and `GL_NUM_EXTENSIONS`.
- Added accurate direct-vs-parsed OpenGL ES core-version provenance to UI, TXT and HTML.
- Fixed Direct GitHub updater cancellation/opt-out races and invalid APK cache cleanup.
- Tightened directional APK signing-lineage validation for certificate rotation.
- Updated Database compatibility metadata to Database 0.2.5.

# 0.3.2

- Audited OpenGLESScope 0.3.1 against the current Khronos OpenGL ES registry and the public OpenGL ES CapsViewer reporting floor.
- Added symbolic decoding for the 2026 Khronos-registered HUAWEI shader/program binary formats (`0x9770` and `0x9771`).
- Preserved raw hexadecimal fallback for unknown future enumerants and runtime-only capability evidence.
- Corrected stale README version metadata.
- Kept Database schema, Material 3 Expressive UI, update behavior, probe isolation and ABI policy unchanged.

# 0.3.1

- Verified runtime collection of compressed texture, shader binary and program binary formats against the OpenGL ES capability-viewer coverage floor.
- Added symbolic decoding for EXT BPTC, EXT RGTC and sRGB S3TC compressed formats while preserving raw hexadecimal values.
- Added symbolic decoding for MESA and DMP program binary formats while preserving unknown enumerants as raw hexadecimal values.
- No capability is inferred from GPU/vendor identity; all format lists remain sourced from the corresponding runtime OpenGL ES queries.

# 0.3.0

- Completed the application-wide Material 3 Expressive UI transition while preserving OpenGLESScope's established visual identity.
- Added expressive theme/motion, short portrait navigation, component state/shape treatments, dialogs, banners and progress/loading presentation.
- Aligned shared functional icons with VulkanScope 0.35.0 while preserving official OpenGL ES/EGL, GPU-vendor and HDR artwork.
- Preserved capability collection, reports, Database integration, update security and Android TV/landscape navigation behavior.
