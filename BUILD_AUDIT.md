# OpenGLESScope 3.0.7 release audit

Version 3.0.7 / 3007. Paired Database 3.0.27; submission schema 2, technical report 5, normalizer 16 and locked GL/EGL registry inputs unchanged.

- Recognize the reported `Android Emulator OpenGL ES Translator (...)` as a translation layer alongside ANGLE. Under either layer, the Overview/Database badge may display only one unambiguous explicitly named GPU model from supported recognizable reported GL_RENDERER signatures. The vendor and name are presentation-only; canonical GL_VENDOR/GL_RENDERER, TXT/JSON and submission evidence are byte-preserved. This works for any recognized GPU family, not the reporter's particular NVIDIA device.
- Reject ambiguous or mixed-model, software/SwiftShader/llvmpipe/WARP and unsupported-only vendor strings for hardware-logo derivation. Unknown/missing official assets remain neutral; do not invent a numerical GPU ID, marketed device/model, advertised capability or fallback GL query. `GL_VERSION` and EGL version still reflect native queried data only.
- Updated compiled Kotlin and independent Node presentation regression for OpenGL ES Translator/ANGLE across manufacturer/model variants, raw evidence, software/ambiguity and source negative mutations. Historical registry snapshots and collection native code are unchanged from 3.0.6.
- Verification boundary: host source-quality and compiled Kotlin identity fixture are separate from Android Gradle build/emulator/physical-device runs; Android SDK/NDK compile, new APK on user's GPU and live Cloudflare deploy NOT EXECUTED here.

# OpenGLESScope 3.0.6 release audit

Version 3.0.6 / 3006. Companion Database 3.0.26 is required for NEW submissions. VulkanScope 3.0.12 remains reference for shared interaction methodology. Technical report schema 5, submission schema 2, normalizer 16, and locked Khronos GL/EGL registry inputs are unchanged.

- Reproduction evidence: Medium_Tablet x86_64 Android 37.2 16KiB, gfxstream, RTX 3050 Ti, guest reports ES 3.1 while host EGL translator logs ES 3.0; ES 3.2 configuration rejected with `EGL_BAD_CONFIG`, app loads x86_64 `libopenglesscope.so`, and base probe output was rejected as malformed/nonterminal. This log does not contain the original rejected JSON, so it cannot alone identify the exact failed field.
- Safe correction: raw index-based compressed/shader/program binary format enumerations remain bounded strings but duplicates no longer invalidate an otherwise complete report; extension identities, diagnostic identities, precision samples and EGL config identities retain unique/evidence validation. Repeated native diagnostic identities coalesce; inconsistent outcomes remain Unavailable rather than being treated as success. Invalid internal-format sample lists are explicitly marked Unavailable without fabricated sample counts. Empty indexed extension evidence is rejected, not silently omitted.
- On rejection, dedicated probe records a bounded field-class diagnostic and terminal explicit Unavailable reason, preserving the strict JSON and publish-then-marker lifecycle; neither a partially queried report nor unsupported EGL 3.2 context is promoted to Available. ES 3.2 / 3.1 / 3.0 / 2.0 context fallback already existed and is retained.
- Source verification: immutable predecessor inventory except explicit new release deltas; current-producer identity mutation checks, canonical names/symbol registry, EGL legality, GL query disposition, schema and lifetime checks plus new 3.0.6 focused negative contract. No new GL/EGL query names were invented; official locally pinned gl.xml/egl.xml remain byte-identical.
- Verification boundary: exact Windows/Android emulator run with NEW APK, Gradle assemble, Android unit/instrumented test, physical GPU and upstream registry re-fetch NOT performed here. Static source gate is not evidence of on-device repair. Preserve the user's runtime result and investigate named validation reason if still Unavailable.

# OpenGLESScope 3.0.5 release audit

Version 3.0.5 / 3005. Companion Database 3.0.25. Technical report schema 5, submission schema 2 and normalizer 16 remain unchanged. VulkanScope 3.0.12 supplies the API-neutral notification/interaction comparison.

- Adopted the shared responsive, accent-colored metric grids on all filtered capability listing views: Features, Limits/Diagnostics, Formats, Extensions, Shader Precision, EGL Configs and common report/analysis search. Counts are derived only from current, filtered producer evidence; Showing is the actual bounded page interval, with no Vulkan field or inferred GL/EGL query. Shader Precision is now capped by the shared 25-entry pager.
- Corrected explicit multi-family ANGLE GPU name and official bundled vendor-artwork identity; retained raw GL_VENDOR/GL_RENDERER strings in reports and evidence inspection. Apple GPU name is displayed only when explicitly reported; missing Apple artwork is never fabricated.
- A failed/incomplete collection leaves a persistent failure banner and locks report-dependent TXT/HTML export and Database upload. Submission and copy result feedback retain the three-second lock.
- Host Kotlin fixture, source/regression gates and clean-package reproducibility are required. This environment has no Android SDK/Gradle distribution for a full Android build; no APK compilation or physical GPU recognition is claimed.
- D1 changes, registry symbol changes and synthetic report backfills: none.

# OpenGLESScope 3.0.3 Kotlin release compile hotfix (same version)

- Reproduced from user Windows `:app:compileReleaseKotlin` log: two private `ExpressiveMetricGrid(metrics: List<Pair<String, String>>, modifier: Modifier = Modifier)` composables generated overload-resolution ambiguity at twelve call sites.
- Retained the original responsive implementation that delegates to `ExpressiveMetric`, matching VulkanScope 3.0.12 shared metric-card implementation. Removed only the duplicate second grid; preserved the separate `MetricCard` used by other report pages.
- Both current metric parity and compile regression verification require exactly one such composable; unchanged 3.0.3 / 3003 identity, schema 2/5, and companion Database 3.0.20.
- Source quality gate is not equivalent to Android Gradle compile; device/Windows release build still requires execution with Android SDK and pinned AGP/NDK.

# Historical 2.2.21 release audit

Version 2.2.21 / 2221. Exact immutable predecessor OpenGLESScope 2.2.20; the 160 production-source predecessor SHA-256 inventory and source ZIP hash are in `tests/golden/2_2_21_release_regression_contract.json`. VulkanScope 3.0.12 engineering methodology governs shared safety, lifecycle, privacy, bounded resources and testing. Companion Database 3.0.17 acceptance of 2.2.21 is a separate server deployment; no producer spoofing or silent upload is introduced.

Inherited 2.2.20 collector findings (unchanged in 2.2.21):
- REPORT-LOSS: `EGL_EXT_image_dma_buf_import_modifiers` previously retained format/modifier counts but dropped every queried 64-bit DRM modifier and `externalOnly` flag. Now all values are preserved as exact-width hexadecimal raw numeric evidence in existing `eglCapabilities[].detail`, bounded by 128 formats / 256 modifiers per format / 4096 total. The exact status/value/detail propagates through UI, technicalReport schema 5, TXT, HTML, analysis and Database submission schema 2 without a schema change. Malformed flags are Unavailable, not false.
- QUERY/REPORT: `eglQueryDeviceStringEXT` and `eglGetDisplayDriverName` previously could treat a non-null text pointer accompanied by an EGL error as Available. Every query now independently clears and consumes EGL errors and verifies bounded UTF-8. `eglQueryDisplayAttribEXT` must return both EGL_TRUE and EGL_SUCCESS for a valid device handle.
- QUERY/REPORT: Two-pass EGL Device, DMA-BUF format/modifier and compression rate counts must agree; otherwise that dataset is Unavailable rather than a complete shortened list. A device missing a legal renderer query is not fabricated as named `Device N`.
- MEMORY/PERFORMANCE: The previous 8 MiB probe publication limit, `kMaxEglCapabilityCount=256`, upper 4096 modifier census, per-format bound, dedicated probe-process timeout/teardown and GL/EGL release paths are retained. The extra detail worst case per format remains under the 16,384-character analysis evidence cap.
- SECURITY: No manifest/permission, app transport, import/export, privacy metadata or report-schema code changed. GL and EGL registry names/values were not added manually.

Historical native evidence remains intact; 2.2.21 adds a 160-source exact-2.2.20 ZIP hash contract with two allowlisted production changes, Kotlin-compiled GPU artwork identity fixtures, and 20 new negative UI/branding mutations. Current Khronos registry-lock, GetPName/EGL query legality, report schema, lifecycle, memory and security static gates must still pass. Deterministic clean ZIP extraction/source SHA equivalence is required.

Khronos current public core baseline is OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5, distinct from verification of the newest extension-registry snapshot. Live latest upstream gl.xml/egl.xml byte-by-byte refresh, real Android build/lint/unit, physical-device driver comparisons, ASan/HWASan/LeakCanary and sustained RAM profiling are NOT EXECUTED unless independently documented by an actual run. A static source PASS never asserts these.

## 2.2.21 evidence boundary
Native GL/EGL collector, technicalReport/submission schema, registry snapshots, permissions and existing PNG art remain byte-identical to 2.2.20. Hero presentation uses the VulkanScope 3.0.12 neutral surface and System Driver title, with EGL_VENDOR (textual implementation identity) in lieu of an invented numeric vendor ID. GPU artwork is selected conservatively from GL_VENDOR and neutral on software/translation layers. Four bottom tabs are Overview / OpenGL ES / Display / Extensions; EGL is still available under OpenGL ES and Overview. EGL identity/context/surface use the unchanged official mark with semantic companion variants. Static checks cannot establish Android runtime pixel parity or physical GPU attribution; Android assemble/lint/unit and physical device checks remain NOT EXECUTED until actually run.

## Release 3.0.1 producer and GPU presentation contract
- versionName 3.0.2 / versionCode 3002; schema 2 and technicalReport schema 5 retained.
- Explicit ANGLE Qualcomm/Adreno renderer identity is presented with Qualcomm artwork; translation or software renderers without explicit evidence remain unknown.
- Historical GL_VENDOR/GL_RENDERER, report TXT and submission JSON preserve the queried source bytes. Google Inc. becomes Google LLC only in presentation.
- Transient action and icon buttons remain locked through working/success/failure feedback for 3 seconds and disable on network/collection prerequisites as the VulkanScope reference.
- Database 3.0.27 permits new report submissions from exactly 3.0.2/3002; earlier stored reports remain readable.

## Release 3.0.2 locked Database notice parity
- Exact yellow warning matches the VulkanScope 3.0.12 Database notice, with only VulkanScope replaced by OpenGLESScope. No extra required producer/version text is included in the user-facing compatibility warning. Companion Database 3.0.19 accepts only 3.0.2/3002 for new submissions; old reports remain readable.
- The introductory Database privacy sentence follows VulkanScope wording with the project name adjusted; canonical GL/EGL report data, permissions, and collection remain unchanged.

## Release 3.0.5 unavailable-collection parity

- A failed base probe stays a terminal explicit Unavailable report; the Compose navigation and Overview remain rendered rather than falling back to a single global EmptyState.
- Throwing probes retain their failure reason as unavailable evidence; no fabricated GL/EGL capability values.
- Report-only sections show titled, scrollable unavailable evidence cards; Display, Encyclopedia and Settings still work.
- Complete report submission, TXT/HTML exports, and report-dependent actions remain locked without an authoritative available snapshot.
- Companion Database 3.0.25 accepts only new 3.0.5/3005 submissions.

## 3.0.5 TXT/JSON hotfix
- Restored eight explicit EGL pbuffer evidence lines in canonical TXT without changing native queries or structured JSON.
- Matched EGL Configs field separators with Database 3.0.25 and added a mutation-sensitive contract.
