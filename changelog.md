# 0.7.2

- Fixed release compilation of the Analysis self-test loading indicator by applying the required Material 3 Expressive opt-in to the Analysis composable, matching the shared VulkanScope implementation pattern.
- Kept the existing OpenGL ES/EGL Analysis behavior and evidence semantics unchanged; this is a compile-correctness fix rather than a capability inference change.
- Audited the companion Database Compare presentation against the shared VulkanScope interaction/layout architecture.
- Source archive policy continues to exclude README.md, root release.md, dedicated packaged app-store metadata directories and forbidden third-party comparison product naming.

# 0.7.1

- Closed remaining shared application-quality gaps against VulkanScope where an OpenGL ES/EGL equivalent exists.
- Added full EGL runtime/config evidence to Analysis snapshots and EGL query gates to the Analysis graph.
- Separated query diagnostics from implementation-limit totals.
- Added search and query provenance to Formats and search to Shader Precision.
- Reorganized EGL runtime detail into identity, current binding/context, collector pbuffer and exact failure evidence.
- Added Khronos registry navigation and exact implemented query-gate evidence to extension detail.
- Expanded Android OS/device provenance across UI, TXT and HTML.
- Added installed ABI and supported device ABI metadata to Database submissions.
- Source ZIP policy now excludes README.md in addition to existing clean-archive restrictions.

# 0.7.0

- Full OpenGL ES/EGL correctness, specification, reporting, security, memory-safety and optimization audit.
- Added EGL runtime/context/surface evidence and expanded extension-gated EGL Config attributes with exact error preservation.
- Added 18 valid extension-backed GL implementation queries; public external comparison reference floor remains 145/145 with 49 OpenGLESScope extras.
- Removed misleading mutable/hint states from capability-limit reporting.
- Hardened native cleanup, GL error draining, runtime-string/token/info-log/program-binary bounds and second-stage enumeration validation.
- Expanded Analysis dependency coverage while retaining 104 OpenGL ES 3.2 Spec minimum checks.
- Advanced current Database technicalReport to schema 2 while retaining top-level schema 2.

# 0.4.1

- Expanded Analysis to Compare, Spec minimums, Graph, Quality, Watched, Share and isolated Tests with VulkanScope-quality UX adapted to OpenGL ES/EGL evidence semantics.
- Expanded lossless bounded snapshots to 8 MiB / 32,768 entries and added evidence-kind/state filters plus completeness-aware regression candidates.
- Expanded the OpenGL ES 3.2 implementation-requirement evaluator to 104 directly queried minimum/maximum requirements.
- Added exact runtime extension-to-query dependency visualization and diagnostic evidence-quality analysis without capability inference.
- Added Matched/Missing watched filtering, canonical Database permalink copy/share and local QR generation.
- Hardened Database submission against redirects and isolated self-test attribution against GL runtime identity mismatch.
- Updated AGP 9.3.2 and Gradle 9.7.1 while retaining API 37, NDK r29, C++20 and three release ABIs.

# 0.4.0

- Added offline local report comparison, regression-candidate analysis, spec-minimum checks, watched capabilities, detailed format/extension inspection and isolated optional runtime tests.
- Analysis data is local-only and does not alter canonical capability or Database evidence.

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

- Audited OpenGLESScope 0.3.1 against the current Khronos OpenGL ES registry and the public external OpenGL ES capability reference reporting floor.
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
## 0.7.0 source-package cleanup

- Removed all third-party comparison product naming from shipped source, filenames, audits and release tooling.
- Removed packaged app-store metadata bundles.
- Removed the root release.md file from the source ZIP.
- Added permanent project rules preventing these items from returning.

