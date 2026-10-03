# OpenGLESScope 3.0.7

### Fixed
- Display explicitly reported GPU family/model and bundled manufacturer artwork for Android Emulator OpenGL ES Translator as well as ANGLE, across supported model families without altering native GL_VENDOR/GL_RENDERER or exported report evidence.
- Keep software, ambiguous, conflicting and unknown renderer strings on neutral artwork rather than inventing hardware identity.

### Changed
- New application version 3.0.7 / 3007; companion Database 3.0.27 for new submissions.

# OpenGLESScope 3.0.6

### Fixed
- Preserve bounded driver-native compressed and shader/program binary enumeration evidence even if a driver reports repeated format tokens, without relaxing independent extension-name checks.
- Reconcile repeated query diagnostic identities without claiming a successful query from conflicting evidence; classify invalid internal-format samples as explicitly Unavailable.
- Identify the specific rejected JSON field category in isolated probe logs and terminal failure reason instead of generic Unknown.
- Preserve spec-backed OpenGL ES 3.2→3.1→3.0→2.0 context fallback and complete-report gating.

### Changed
- Release 3.0.6 / 3006; Database 3.0.26 companion required before new submissions.

# OpenGLESScope 3.0.5

### Fixed
- Keep Overview, navigation, Android Display, Encyclopedia and Settings visible after an unavailable/failed OpenGL ES/EGL collection, matching VulkanScope failure-state presentation.
- Show explicit unavailable evidence on report-only pages instead of a blank workspace; preserve real probe failure reasons without inventing capability data.
- Retain complete-report gating for exports and Database submission.

# OpenGLESScope 3.0.3 — release Kotlin compile hotfix (same version)

- Fixed: Removed a duplicate `ExpressiveMetricGrid` composable that caused `:app:compileReleaseKotlin` overload-resolution ambiguity. Retains VulkanScope 3.0.12 responsive `ExpressiveMetric` implementation and keeps existing Overview `MetricCard`.
- Added: Release-blocking source assertions for a unique metric-grid signature so this exact Kotlin regression cannot silently pass `QUALITY_GATE`. No versionCode, reporting schema, renderer, or Database contract changes.

# OpenGLESScope 3.0.3

- Changed: Shared VulkanScope responsive metric-card typography, spacing, grid and filtered/paged counts applied to capability lists and analysis search; OpenGLESScope brand color retained.
- Changed: Features, Limits/Diagnostics, Formats, Extensions, Shader Precision, EGL Configs and general search/list views use truthful evidence metrics and explicit Showing page ranges; Shader Precision uses the bounded shared pager.
- Preserved: API-specific GL/EGL queries and counts, raw reports, the locked database compatibility warning, schema 2/5, and ANGLE physical-GPU presentation.
- Changed: Producer identity 3.0.3/3003, paired only with Database 3.0.20.

# OpenGLESScope 3.0.2

- Changed: Restored the exact VulkanScope Database compatibility notice with OpenGLESScope branding only.
- Changed: Matched the Database privacy introduction to the reference.
- Changed: Producer version 3.0.2 / 3002 for Database 3.0.19.

# OpenGLESScope 3.0.1

- Fixed: ANGLE physical GPU family/model detection and matching available vendor logo for explicitly reported Qualcomm, Arm, Imagination, Samsung, NVIDIA, AMD, Intel, Broadcom, Vivante and Huawei. VeriSilicon model evidence is retained but its incorrectly aliased Vivante logo is not used. Unknown, software and conflicting signatures do not fabricate a GPU.
- Preserved: Driver-reported GL_VENDOR / GL_RENDERER in TXT, JSON, history and Database submission; Google LLC is display-only normalization.
- Changed: New report producer version to 3.0.1 / 3001, matching Database 3.0.17.
- Verified: Persistent collection-failure banner and disabled report-dependent actions; transient failure/success lock remains three seconds.

# OpenGLESScope 3.0.1

- Changed: Application release identity to 3.0.1 (3001) for Database 3.0.17.
- Fixed: Explicit ANGLE Qualcomm/Adreno renderer presentation, manufacturer display name, and vendor artwork without changing canonical GL evidence.
- Verified: Transient action success/failure lock and upload prerequisites remain aligned with shared reference.

# OpenGLESScope 2.2.22

## Fixed
- Match VulkanScope's contained, responsive two-column evidence rows everywhere instead of uncontained static text, including long-press inset/focus and dialog presentation.
- Present EGL_VENDOR in the System Driver hero with the same type hierarchy, dimensions and focus treatment as VulkanScope's Vendor ID, retaining the exact queried textual value.

## Verified
- Immutable 2.2.21 native collector, Khronos registry and report code; registry/name/query/report, security, resource and lifecycle source gates. No invented vendor ID or runtime capability.
- Device profiling, newest upstream full XML comparison and real Android build are separate verification tasks.

# Historical 2.2.21

## Fixed
- Reject EGL Device renderer/name text when `eglQueryDeviceStringEXT` reports an EGL error or malformed UTF-8; preserve error provenance rather than accepting a non-null driver pointer as success.
- Stop reporting synthetic `Device 0`/`Device 1` placeholders as actual renderer values when no authoritative device renderer has been queried.
- Reject unstable two-pass EGL Device, DMA-BUF format/modifier and surface-compression enumeration counts instead of presenting partial results as complete.
- Preserve real 64-bit DMA-BUF modifiers and each `externalOnly` flag in bounded EGL capability details, rather than reporting counts alone. Invalid flags explicitly invalidate the per-format query evidence.
- Require successful EGL error verification for current-display device-handle and MESA driver-name queries.

## Verified
- GL/EGL locked registry hashes, canonical names, GL/EGL core/extension query legality and existing structured/TXT/HTML/Database schema alignment; VulkanScope 3.0.12 API-neutral resource/security methodology.
- Android APK/lint/unit, physical-driver stress, long-duration RAM/heap profiling, sanitizers and newest upstream registry byte-level comparison are separate release qualifications; they are not implied by static PASS.

## 2.2.21
- Changed: GPU hero card neutral surface, System Driver label and EGL_VENDOR identity evidence; no synthetic numeric vendor ID.
- Changed: Bottom tabs now Overview / OpenGL ES / Display / Extensions in portrait/landscape. EGL remains accessible through OpenGL ES.
- Changed: Official EGL artwork remains unchanged and is used with distinguishable identity/context/pbuffer companion marks.
- Fixed: GL_VENDOR-based GPU artwork attribution; uncertain/translation-layer identities now use neutral artwork.

- Fixed canonical report text (EGL pbuffer/detail and EGL Config output) to match the strict Database submission contract; retained raw report and native evidence.
