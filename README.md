<p><strong><span style="color:#FFFFFF">⚠ OpenGLESScope is not an official Khronos Group project.</span></strong></p>

# OpenGLESScope

**OpenGLESScope** is an advanced **OpenGL® ES™**, **EGL™**, and Android Display/HDR capability inspection and reporting tool for Android. It queries the active graphics implementation through a real EGL/OpenGL ES context and presents runtime GPU/implementation identity, core-version evidence, features, properties and limits, extensions, formats, shader precision, EGL runtime and configuration data, Android display information, query diagnostics, and dedicated analysis tools.

Database: https://efishell0.github.io/OpenGLESScope_database/

**Current version: 3.0.7** 

This app supports **Obtainium**; the official GitHub project/release URL can be used for update tracking.

> OpenGLESScope reports what the active OpenGL ES/EGL implementation actually exposes. It does not infer feature support from Android version, GPU model, renderer marketing text, an extension's presence alone, or a registry entry.

## Highlights

- Runtime OpenGL ES 2.0–3.2 inspection, with an **OpenGL ES 3.2 / GLSL ES 3.20** engineering baseline
- Independent **EGL 1.5** engineering baseline and runtime EGL version reporting
- Direct preservation of `GL_VENDOR`, `GL_RENDERER`, `GL_VERSION`, and `GL_SHADING_LANGUAGE_VERSION`
- Runtime GL extension enumeration and separate EGL client/display extension scopes
- Current checked-in Khronos `gl.xml` / `egl.xml` registry-locked metadata and canonical names
- **145** public capability-reference parity rows and **134** additional audited query rows in the checked-in coverage matrix
- **91** registry-derived core GLES 2.0–3.2 capability-like `GetPName` entries covered by the current core query contract
- Core and exact-extension-gated implementation features, properties, and limits
- **104** OpenGL ES 3.2 specification minimum/maximum reference checks, evaluated from actual evidence
- Compressed texture, internal-format, shader-binary, and program-binary inspection
- Vertex/fragment shader floating-point and integer precision with original queried ranges and values
- Detailed EGL runtime, current bindings, context, collector pbuffer, configurations, and diagnostics
- Applicable extension-backed EGL device, image, DMA-BUF, and configuration attributes
- Android Display/HDR and mode-level information kept separate from graphics API capabilities
- Local **Encyclopedia** backed by the bundled registry catalog; registry presence never means runtime support
- Dedicated **Analysis workspace**: evidence provenance, global search, comparison, diagnostics, minimums, dependency graph, raw report, public Database comparison, local history, watched evidence, sharing, and isolated self-tests
- Responsive, filtered and paginated evidence cards, with separate matching counts and actual **Showing** ranges
- Conservative ANGLE GPU attribution: the translation layer is not confused with a physical GPU vendor; raw driver strings remain unmodified
- TXT and self-contained HTML complete reports, plus explicit structured technical-report JSON export from Analysis
- User-initiated complete-report upload, canonical Database permalinks, and locally generated QR codes
- Secure GitHub-based direct update checking with user confirmation and Obtainium-compatible management
- Isolated native probe process, bounded collection/report resources, and persistent failure feedback
- Android TV/D-pad support, multi-ABI release builds, and a dark Material 3 Expressive interface
- Official OpenGL ES/EGL branding with the **`#BA2A8D`** OpenGLESScope accent

## UI

OpenGLESScope uses a dark Material 3 Expressive presentation modeled on the API-neutral interaction and accessibility methodology of the VulkanScope 3.0.12 reference, while retaining OpenGL ES/EGL terminology, evidence semantics, and its own brand color. Shared visual components are not a license to fabricate another API's capabilities.

The four primary floating-navigation destinations are **Overview, OpenGL ES, Display, and Extensions**. EGL is accessible within the OpenGL ES area and from Overview; Features, Limits/Diagnostics, Formats, Shader Precision, EGL Configs, Encyclopedia, Analysis, and Settings are dedicated destinations or nested inspection pages.

**Encyclopedia** and **Analysis workspace** open from compact Overview destination cards instead of placing all their content in Overview. Expensive reference searching and Analysis-only state are scoped to their active pages. Long technical rows use responsive evidence cards, bounded dialogs, monospace identifiers where needed, and actions that preserve the underlying complete values.

Filters use contained, bounded single- or multi-select controls, appropriate search/clear actions, selected-state feedback, and pagination where needed. Capability lists show report-backed summary metrics: matched entries, applicable evidence-state breakdowns, and the actual interval displayed on the current page. Limits and query diagnostics are counted separately. The shared responsive metric grid follows one/two/three-column layout according to available width and text size; Shader Precision uses the shared **25-entry** pager in 3.0.3.

The floating navigation and scrolling content account for screen insets and transient status banners in portrait, landscape, freeform, and Android TV use. Query/collection status is independent from validated Android network status. Report-dependent actions remain disabled without a structurally complete collection; Internet-backed operations also require a validated connection.

Applicable evidence states retain distinct meanings:

- **Supported** — established by the relevant positive runtime evidence
- **Unsupported** — established by an authoritative negative result where that conclusion is valid
- **Available** — a requested value/query was obtained
- **Unavailable** — an applicable result could not be obtained
- **Incomplete** — the returned evidence was only partial
- **Not applicable** — prerequisites for this query path were not met
- **Unknown / not queried** — the available evidence cannot justify a conclusion

A missing value, a failed query, and a feature that is demonstrably unsupported are **not** interchangeable.

# Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/overview-6.jpg" width="200" alt="Overview">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/egl-configs-3.jpg" width="200" alt="EGL configurations">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/opengles-3.jpg" width="200" alt="OpenGL ES">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/egl-4.jpg" width="200" alt="EGL">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/display-3.jpg" width="200" alt="Android Display and HDR">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/extensions-3.jpg" width="200" alt="Extensions">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/androidstudio-landscape.png" width="500" alt="Virtualized Android example">
  <img src="https://raw.githubusercontent.com/EFIShell0/OpenGLESScope/main/screenshots/database-7.png" width="500" alt="OpenGLESScope Database website">
</p>

**NOTE: The first landscape screenshot was captured in Android Studio. Virtualized environments (emulators/hypervisors) such as BlueStacks, MuMuPlayer, LDPlayer, and QEMU are not supported.**

**NOTE 2: The last landscape photo is a screenshot of the database website.**

## OpenGL ES coverage

OpenGLESScope creates a real EGL/OpenGL ES context against the implementation selected by Android. The collector attempts the highest applicable context and uses compatible fallback paths for OpenGL ES 3.2, 3.1, 3.0, and 2.x; the reported core level comes from the actual runtime, **not** the version requested during context creation.

Coverage includes:

- Raw `GL_VENDOR`, `GL_RENDERER`, `GL_VERSION`, and `GL_SHADING_LANGUAGE_VERSION`
- Parsed OpenGL ES and GLSL ES versions with applicable consistency checks
- Core-version capability state and implementation-dependent GL queries
- Extension enumeration with explicit count/completeness evidence
- Internal limits, indexed limits, and applicable extension-gated properties
- Compressed/internal texture formats and binary-format enumerations
- Shader numeric precision and queried diagnostics
- Separately collected EGL and Android display/HDR evidence

OpenGL ES, GLSL ES, EGL, Android platform, and physical display identity are different domains; none substitutes for another. An Android system may expose an implementation backed by a translation layer, a software renderer, or vendor-provided graphics libraries.

The bundled Khronos Combined OpenGL Registry and EGL API Registry are release-locked in `registry/gl.xml`, `registry/egl.xml`, and `registry/registry_lock.json`. This provides canonical names and query-legality metadata; it is not a device-support database.

## Device and implementation information

The Overview and OpenGL ES runtime pages expose the available device/driver and actual GL/EGL implementation identity, including:

- Android device/model, OS and build provenance where reported by the platform
- Installed app ABI and platform-supported ABIs
- Raw GL implementation/vendor, renderer, runtime version, and GLSL version
- EGL vendor, version, initialized version, and applicable current-binding state
- System-driver presentation and independently queried implementation evidence
- Query-completeness and collection-status information
- Explicit detail views and copy/share/watch/Encyclopedia actions for supported evidence rows

The text in `GL_VENDOR` identifies the **GL implementation**, not necessarily the chip manufacturer. `GL_RENDERER` may describe a translation or software layer. OpenGL ES does not standardize a query for a physical GPU vendor/device ID or a universally usable graphics-driver version, so OpenGLESScope does not invent one.

### ANGLE and physical GPU presentation

ANGLE is a graphics translation layer and **is not itself a GPU brand**. When an ANGLE renderer explicitly names an underlying recognizable physical GPU, OpenGLESScope may show its model and matching bundled vendor artwork in the presentation layer. This matching covers applicable clearly reported families such as Adreno, Mali/Immortalis/Maleoon, PowerVR, Xclipse, NVIDIA, AMD, Intel, VideoCore, and Vivante.

A backend API name by itself, an ambiguous or contradictory renderer, a software renderer such as SwiftShader, or missing official artwork **must not** create a guessed physical GPU name/logo. `Google LLC` is a normalized *display label* for a historical `Google Inc.` implementation identity; the original `GL_VENDOR` and `GL_RENDERER` strings remain unchanged in technical reports, TXT/HTML/JSON, and Database submission. Hardware recognition is never used to infer capability support.

## Extensions

OpenGLESScope enumerates exact runtime-reported OpenGL ES extension tokens. The enumeration method follows the real context:

- **OpenGL ES 3.x:** indexed extension enumeration with `glGetStringi`
- **OpenGL ES 2.x:** extension-string enumeration through `glGetString(GL_EXTENSIONS)`

EGL client and initialized-display extensions are queried separately and are not mixed into the GL extension list. Unknown registered or vendor-specific runtime tokens remain visible by their reported names. Extension support is derived from successful, complete enumeration where required; a failed or partial enumeration is not silently presented as a complete empty set.

### Exact-extension-gated implementation queries

An extension-defined implementation query is attempted only when the exact owning extension is advertised and the query is legal for the current context. Applicable query families include, among others:

- `GL_EXT_texture_filter_anisotropic`
- `GL_EXT_blend_func_extended`
- `GL_OVR_multiview` / `GL_OVR_multiview2`
- `GL_EXT_texture_buffer` and clip/cull-distance limits
- `GL_EXT_draw_buffers` / `GL_NV_draw_buffers`
- Multisampled-render-to-texture variants
- `GL_KHR_shader_subgroup`
- `GL_EXT_window_rectangles` / `GL_OES_viewport_array`
- Shader pixel local storage, sample shading, and sparse texture query paths
- Applicable fragment-shading-rate, mesh/task, robustness, and vendor query paths

The registry and the source code establish legality; the **driver query result** establishes the value. An extension name alone does not prove the value of an associated numeric limit or behavior.

## Features

Core capability state is evaluated against the reported runtime OpenGL ES version for levels **2.0, 3.0, 3.1, and 3.2**. Extension-backed rows remain separately gated on the exact reported extension prerequisites and successful corresponding queries.

The Features view presents implementation-backed, searchable evidence with explicit matching/availability summaries. Its catalog describes the application's audited query coverage, not every possible feature concept in every GL/EGL specification. Driver, vendor, marketing GPU name, and Android version never stand in for a missing result.

## Properties & limits

OpenGLESScope queries applicable implementation-dependent values directly and retains precise query diagnostics independently from ordinary limit/property rows.

Representative categories include:

- Texture, cube-map, array, renderbuffer, framebuffer, and viewport dimensions
- Vertex attributes, uniforms, varyings, and image/texture units
- Draw buffers, color attachments, shader storage and uniform blocks
- Atomic counters, transform feedback, and compute work groups
- Geometry/tessellation-related limits where the runtime makes them legal
- Per-index and multi-component limits such as compute work-group X/Y/Z
- Alignment, multisampling, internal-format, and applicable extension-defined limits
- Extension-defined vendor state only when its exact owning prerequisites are present

The locked core query audit covers **91** capability-like GLES 2.0–3.2 `GetPName` entries. The local requirements evaluator includes **104** OpenGL ES 3.2 implementation minimum/maximum reference checks. Neither number is an assertion about what every connected device supports.

The Limits/Diagnostics explorer keeps collected implementation values separate from safety/query diagnostics. For both modes, the 3.0.3 interface shows real filtered counts and bounded page intervals; diagnostics never inflate the implementation-limit total. Failed queries keep their actual status and available error detail instead of disappearing or being turned into zero.

## Formats

Format information is derived from the active implementation's legal GL queries and enumerations, not inferred from renderer or extension marketing names.

### Compressed texture formats

Enumerates reported compressed format values, preserving the canonical name and raw numeric/hex value when available. Future or unknown enumerants retain their original raw value instead of receiving a fabricated GL token.

### Internal-format and sample evidence

Applicable `glGetInternalformativ` results retain supported sample-count evidence and query availability, with exact version/extension guards and bounded enumeration. Applicable vendor-specific per-sample information is kept as separate runtime evidence rather than assumed for every format.

### Shader binary formats

Reported by the implementation's shader-binary format enumeration only; a zero result and a failed enumeration remain distinguishable.

### Program binary formats

Reported when the active core or exact extension makes the corresponding query valid. Known values receive their canonical symbolic name; unknown values remain numeric/raw.

Formats pages have search, detail actions, actual matched counts, and **Showing** ranges using the common bounded pager.

## Shader precision

OpenGLESScope queries numeric shader precision using `glGetShaderPrecisionFormat`, covering applicable **vertex** and **fragment** shader stages with low/medium/high floating-point and integer precision categories.

Each result preserves the driver-returned minimum/maximum exponent range and precision values. The app does not infer precision from GPU family or specification tables. The shared explorer offers stage/type filtering, search, actual result counts, and **25 entries per page** in version 3.0.3. It does not manufacture other shader stages for this API query.

## EGL runtime

EGL evidence is independent from OpenGL ES evidence and may include:

- `EGL_VENDOR`, `EGL_VERSION`, and initialized major/minor version
- Supported client APIs and bound client API
- Initialized-display extension list
- Client extensions queried with `EGL_NO_DISPLAY` when legal
- Current display, context, draw surface, and read surface validation
- Current context/configuration identification and applicable context attributes
- Context/pbuffer creation and collector surface evidence
- Applicable platform/device and extension-backed capabilities
- EGL error/status information with exact provenance

Current-bindings, collector-created pbuffer, and queried EGL configuration attributes are kept separate. Creation-only attributes are not misreported as legal current-context queries; failed EGL calls remain explicit unavailable/diagnostic evidence. An EGL extension is never silently reclassified as an OpenGL ES extension or vice versa.

## EGL configurations

The collector enumerates EGL configurations using `eglGetConfigs` and queries legal per-configuration attributes through `eglGetConfigAttrib`, with bounds and explicit query-error handling.

Available attributes can include:

- Config ID; color, depth, stencil, luminance, and alpha-mask bits
- Sample buffers and sample count
- Surface/renderable/conformant type flags and config caveat
- Color buffer type, native visual and renderability metadata
- Texture binding capability and transparency attributes
- Maximum pbuffer width, height, and pixels
- Minimum/maximum swap intervals
- `EGL_ANDROID_recordable` when its owning extension is reported
- `EGL_ANDROID_framebuffer_target` when its exact prerequisite is reported
- Applicable floating-point color-component evidence from `EGL_EXT_pixel_format_float`

Additional capability queries may expose independently bounded EGL device, supported DMA-BUF format/modifier and image metadata where the exact extension/entry-point prerequisites are satisfied. For example, the retained modifier audit bounds queried DMA-BUF data to **128 formats, 256 modifiers per format, and 4,096 total modifiers** while preserving raw 64-bit values and `externalOnly` state.

An unavailable attribute is not silently filled in. An EGL configuration's properties are implementation data: they do not independently prove physical Android display capabilities.

## Android Display & HDR

Android display information is collected independently of EGL and OpenGL ES runtime support. Where available from public Android APIs, it can include:

- Current display and active mode
- Resolution and refresh rate
- Supported display modes
- Android-reported HDR type information and query status
- Desired maximum/average/minimum luminance
- Wide-color evidence where exposed by the platform

On applicable Android versions, mode-level HDR information is used when present; older supported platforms follow the appropriate Android capability path. If a query is unavailable, OpenGLESScope reports that absence explicitly rather than fabricating a result. Android's declared HDR support is **not** proof of a particular GL/EGL rendering or presentation pipeline, and wide-color capability is not a measured gamut percentage.

Virtualized and translated Android environments may expose synthetic or incomplete GPU/display evidence; these results should not be presented as direct measurements of an underlying physical device.

## Encyclopedia

The **Encyclopedia** is a local reference surface opened from Overview. It combines a compact glossary with bounded offline registry-backed search and does **not** treat registered symbols as runtime capabilities.

The bundled Khronos GL/EGL registry catalog makes API tokens and metadata discoverable through category filters, search, result cards and paging. An entry's existence in the catalog means that the symbol exists in the locked reference, not that the active device supports it. Canonical `GL_*` and `EGL_*` names retain their exact spellings; descriptive notes must not imitate fabricated technical tokens.

Search stays on the Encyclopedia page, updates only the necessary result/pager content, preserves field focus while typing, and maintains bounded display work. Evidence from the current runtime can be opened or searched separately without rewriting the canonical report.

## Analysis

The separate **Analysis workspace** operates on a completed report and keeps its own state, tools, and bounded local evidence independently from normal collection. Analysis actions do not alter the canonical GL/EGL report just to make a visualization or comparison convenient.

The workspace contains dedicated tools for **Compare, global Search, Diagnostics, Requirements/Spec minimums, Graph, Quality, Database comparison, local History, Watched evidence, Share, raw structured reporting, and isolated Tests**.

### Evidence provenance and global search

Evidence rows can expose their original key/value, collection/query provenance, and applicable actions (copy, share, add to watch list, and Encyclopedia lookup). Long labels and values have bounded, responsive detail surfaces; Android TV/keyboard focus is considered separately from whether a row performs an action.

The local report search covers the collected identity, features, extensions, limits, formats, precision, EGL runtime/configurations, display/HDR, and diagnostics without silently discarding unavailable rows. Counts and visible-page ranges are derived from the filtered result set.

### Snapshot comparison

A saved local evidence baseline can be compared against the current completed collection. Missing-on-one-side information remains **Unknown / Not reported** rather than falsely becoming unsupported. Incomplete enumeration is tracked so it does not manufacture a removed-feature conclusion.

### Diagnostics and requirement resolution

Collection/query diagnostics preserve explicit success, absence, failure, or non-applicability with available details. Requirements are evaluated from the exact runtime core/extension/query prerequisites, not from a device name or unrelated capability. The evaluator presents evidence, not a conformance or performance score.

### Specification minimums

The bounded evaluator includes **104** checked-in OpenGL ES 3.2 implementation minimum/maximum reference entries. Correct minimum/maximum direction and alignment/offset semantics are applied. If the required query evidence is missing, the result remains **Unknown** instead of an artificial fail or pass.

### Extension dependency graph

The graph relates canonical GL/EGL extension prerequisites to the query paths implemented by OpenGLESScope. Registry ownership and dependencies remain reference data, not runtime support or proof of a numeric capability.

### Raw structured report and Database comparison

Analysis can inspect the exact local **schema-5 `technicalReport`** as a searchable, read-only tree and explicitly export bounded JSON. A validated public report ID may be used to fetch a public Database report from the fixed official HTTPS origin and compare the returned evidence locally. Such reads are user-initiated and subject to validated-network gating.

### Local session history and watched evidence

Completed Analysis snapshots can be retained privately and reused as comparison baselines. The analysis resource contract caps an uncompressed snapshot at **8 MiB** and **32,768 evidence entries**, with up to **8** retained history records. The local watch list contains at most **256** entries; matched/missing states remain distinct.

### Quality and self-tests

Quality reports explicit collection/query anomalies; it is not a GPU ranking, driver performance benchmark, or official conformance result. Optional minimal self-tests run through an isolated native-probe path, and their outcome is associated with the currently inspected report only if the relevant runtime identity matches. Tests do not silently rewrite the capability report.

### Local sharing and shared-storage actions

A canonical permalink is retained only after a successful Database response with a validated 64-hex report ID. QR generation happens locally rather than through a tracking service. Bounded Analysis snapshot, minimum-profile and technical-report JSON import/export use the app's shared-storage browser after explicit user action, with canonical path confinement and appropriate overwrite confirmation.

## Registry-driven query system

The project bundles the Khronos Combined OpenGL Registry and EGL API Registry locally. Generated coverage/catalog files are tied to the checked-in SHA-256 locks and validated by source quality gates; these files are not refreshed from the Internet at runtime.

The current release's reference contract includes:

- OpenGL ES **3.2** / GLSL ES **3.20** and EGL **1.5** engineering baselines
- **91** core GLES capability-like `GetPName` entries in the locked census
- **145** public parity rows plus **134** additional audited query rows in `PUBLIC_CAPABILITY_REFERENCE_MATRIX.csv`
- **104** OpenGL ES 3.2 minimum/maximum checks
- Exact `GL_*` / `EGL_*` canonical naming and owning-extension query validation

An upstream registry change requires deliberate regeneration and review of catalog, legality, evidence, and report surfaces. No guessed enumerant, undocumented alias, missing `eglGetProcAddress` entry point, or arbitrary `GetPName` token may be introduced merely for apparent coverage.

## Query safety and evidence semantics

Driver-controlled counts, lengths, strings, and return values are treated as untrusted input. Multi-stage collection paths revalidate actual returned counts before indexing or allocating, enforce resource bounds, and retain query-failure diagnostics. Distinct extension enumeration completeness, GL and EGL errors, unknown future numeric values, and exact-width 64-bit extension evidence are preserved where applicable.

A successful query answers only the question it actually asked. A missing extension after *complete authoritative* enumeration may have a different evidence meaning from a missing extension after a failed enumeration. An EGL configuration, Android HDR report, model name, or registry dependency must not be promoted into unrelated API support.

### Isolated native probe

The native collector runs in a separate, non-exported Android service process named `:opengles_probe` through a bounded C++20/JNI boundary. The main UI process does not directly load the collector native library.

Core protections include:

- Dedicated terminal collection attempt with a unique private result path
- Single native worker and process-wide probe synchronization
- Private cache, complete snapshot publication, and atomic temporary-file replacement
- **8 MiB** probe-result ceiling and a normal **20-second** probe timeout
- Bounded output parsing and explicit terminal state
- Watchdog/process termination for timeout or oversized output
- Deterministic cleanup of EGL displays, contexts, surfaces, threads, and native objects
- No UI recomposition-triggered native recollection

A native-library load failure, JNI exception, probe crash, timeout, invalid result, or incomplete terminal publication becomes explicit failed/unavailable collection evidence rather than an apparently successful partial report. A collection failure keeps its user-visible notification until an appropriate state change; report-dependent export/upload controls remain disabled.

## Reports

OpenGLESScope can export the complete collected report as:

- **TXT** — canonical, human-readable technical evidence
- **HTML** — self-contained report with embedded presentation, no remotely loaded scripts/styles/fonts/trackers
- **Structured JSON** — explicit schema-5 `technicalReport` export from Analysis

Report content may include application/version metadata, Android device/ABI information, raw GL/EGL implementation identity, core states, features, limits, GL extensions, EGL client/display extensions, shader precision, binary/internal/compressed formats, EGL context/current-binding/configuration data, Android display/HDR modes, query diagnostics, and available provenance.

The same completed capability model is used for UI, TXT, HTML, structured Analysis, and Database submission. Reports are not silently truncated or populated with invented values to make them look complete. TXT/HTML export and Database upload stay unavailable while collection is running or structurally incomplete.

## OpenGLESScope Database

OpenGLESScope can explicitly submit the complete technical report to the public companion Database:

**https://efishell0.github.io/OpenGLESScope_database/**

Submission is **opt-in**: no capability report is uploaded until the user initiates it. No per-capability exclusion control produces a misleadingly complete partial report. Network use is confined to the official HTTPS service and guarded by the Android validated-network state.

The current submission contract is:

- Application **OpenGLESScope 3.0.3** / **versionCode 3003**
- Companion Database **3.0.20** for new submissions
- Top-level submission **schema 2**
- Nested **`technicalReport` schema 5**
- Database normalizer contract **16**
- **2 MiB** maximum request body; oversized reports fail rather than being truncated
- All-or-nothing, bounded response handling and no automatic HTTP redirects for upload
- Canonical permalink recorded only for server-confirmed success and a validated 64-hex report ID

The companion Database 3.0.20 admits **only** the audited 3.0.3/3003 producer for *new* POST requests. Earlier stored reports remain readable; a rejected submission is displayed as an error, never as a successful upload. Publishing the app and publishing the companion Worker/Pages are separate operations.

The user-facing compatibility notice follows the VulkanScope reference wording with the project name adapted, without inserting version numbers or additional warnings into that locked paragraph:

> Compatibility notice: when a newer OpenGLESScope release raises the Database submission floor, reports from older app versions are rejected by the server. A rejection is returned as a submission error and is shown here instead of being treated as a successful upload.

Sensitive identifiers such as IMEI, Android ID, device serials, MAC addresses, authentication tokens, account data, and private file paths are not part of the intended public capability-report payload.

## Update system

OpenGLESScope can check the official repository's GitHub Releases for application updates:

**https://github.com/EFIShell0/OpenGLESScope**

**Direct GitHub updates** are enabled by default and can be disabled from Settings when Obtainium is used as the sole external update manager. Startup discovery is metadata-only and non-blocking; disabling the option stops built-in update discovery/download rather than affecting local GL/EGL collection.

Network-backed update actions require a validated Android default network. Download requires explicit review/confirmation; candidate package identity, release/version progression, APK/signature compatibility, size, and relevant ABI fallback are checked before Android's installer is invoked. Downloaded files remain in private cache and are shared only through the non-exported update `FileProvider`. Release-note text is bounded and treated as inert display content.

Obtainium can follow the official GitHub releases and universal APK independently of built-in direct updates.

## Security & privacy

Capability collection is performed **locally**. Network access is reserved for explicit functionality, including Database submission/public comparison, official update checks/downloads, and user-opened external links.

Relevant release safeguards include:

- No automatic or background upload of graphics capability reports
- Fixed HTTPS production endpoints; cleartext traffic disabled
- Android application backup disabled
- Non-exported native probe service and update `FileProvider`
- Bounded native strings, enumerations, EGL configuration/device counts, query logs, and JSON output
- **8 MiB** probe-result / Analysis snapshot bounds and a **2 MiB** Database request limit
- **32,768** Analysis evidence-entry and **256** watched-entry limits
- Canonical-path confined, bounded, user-initiated shared-storage import/export
- Package identity, signing, version and size checks for official direct updates
- Stack protection, hidden native symbols, RELRO/NOW, and **16 KiB** page-size linker compatibility
- Explicit `GL_*`/`EGL_*` registry/name/enum validation rather than invented tokens
- Exact-extension and version prerequisites for implementation-specific queries
- No capability inference from a vendor logo, driver marketing string, or unrelated Android API
- Explicit distinction among successful, failed, incomplete, unavailable and not-applicable evidence
- No remote QR-generation service and no analytics dependency in the reporting path

A source/static quality-gate PASS is **not** a substitute for an actual Android release compile, Android instrumentation/lint run, or physical-device driver verification. These require the corresponding Android toolchain/device.

## Android TV and responsive layouts

OpenGLESScope provides an Android TV / Leanback launcher entry and supports D-pad-based use rather than assuming a touchscreen. Navigable controls have visible focus; long technical screens can be traversed with directional keys, and compatible evidence actions are available to the focused item where implemented. Scroll-to-focused-content, reduced motion, accessibility labels, and compact portrait/landscape/freeform presentation are part of the shared UI contract.

The bottom navigation consists of **Overview, OpenGL ES, Display, Extensions** in that order. EGL remains a nested inspection destination instead of an extra top-level tab. Temporary status overlays reserve content clearance so neither the app bar nor the floating bottom bar has to obscure the current evidence row.

## Supported ABIs

Native Android release outputs are configured for:

| ABI | Status |
|---|---|
| `arm64-v8a` | Supported |
| `armeabi-v7a` | Supported |
| `x86_64` | Supported |
| `x86` | Intentionally excluded |

The release APK verifier expects exactly **four APK artifacts**: universal, `arm64-v8a`, `armeabi-v7a`, and `x86_64`. The universal APK contains precisely the three supported native ABIs; each split contains only its declared ABI. Unexpected or accidental `x86` inclusion is rejected.

## Android and build baseline

OpenGLESScope **3.0.3** uses the project-pinned configuration:

- **Minimum SDK:** Android 12 / API **31**
- **Compile SDK:** Android API **37.2**
- **Target SDK:** Android API **37**
- **Android Gradle Plugin:** **9.4.1**
- **Gradle wrapper:** **9.7.1**
- **Kotlin Compose plugin:** **2.4.20**
- **JDK:** **17+** (subject to the Android Gradle Plugin's installed-toolchain requirements)
- **NDK:** **30.0.16248370**
- **CMake minimum:** **3.22.1**
- **Native language standard:** **C++20**
- **UI:** Kotlin, Jetpack Compose, Material 3 Expressive
- **Native graphics APIs:** EGL and OpenGL ES

CMake builds the dedicated native collector with warnings-as-errors, `-fstack-protector-strong`, hidden visibility, RELRO/NOW, and 16 KiB page linker settings.

### Build

From the project root on Linux/macOS:

```bash
./gradlew :app:assembleRelease
```

On Windows / PowerShell:

```powershell
.\gradlew.bat :app:assembleRelease
```

The release verifier checks the native-library ABI layout and copies the resulting APK set into the configured release-artifact output directory. An Android SDK/NDK installation matching the pinned project configuration is required; this source README does not claim a signed or device-tested APK is included in the source ZIP.

## Requirements

- Android **12 / API 31** or later
- Working OpenGL ES 2.0-capable implementation and EGL context creation path
- A supported device ABI and compatible Android graphics environment
- A physical device is recommended for meaningful GPU/display characterization

The exact available data depends on the active OpenGL ES/EGL driver or translation layer, Android framework, firmware, device ABI, and display stack. In an emulator or virtualized Android environment, a renderer may describe the graphics translation/software environment rather than a physical host GPU; results are not automatically authoritative hardware evidence.

## Branding

OpenGLESScope uses the approved repository artwork and official OpenGL ES/EGL visual assets, with the magenta primary brand accent:

**`#BA2A8D`**

User-facing API labels retain the intended OpenGL® ES™ and EGL™ marks; raw registry tokens, queried strings, serialized fields, and on-wire identifiers are **never** rewritten for presentation or trademark decoration. Vendor artwork selection is a conservative identity hint, not capability proof. A missing or mismatched logo is not replaced by an unrelated brand's artwork.

Application shell, launcher, Android TV banner, TXT/HTML report presentation, and the companion Database keep their own OpenGLESScope identity. Capability/status colors remain semantically distinct from the brand accent.

## Open source

OpenGLESScope is open-source software and is **not affiliated with, sponsored by, or an official project of the Khronos Group**.

Application source: **https://github.com/EFIShell0/OpenGLESScope**

Companion Database source: **https://github.com/EFIShell0/OpenGLESScope_database**

Bug reports, tested device results, specification-backed corrections, and contributions are welcome. See the repository's `LICENSE`, `rules/PROJECT_RULES.md`, and versioned audit documents for the applicable terms and release requirements.

## Third-party components

The current app Gradle release pins include:

- AndroidX Core KTX **1.19.1**
- AndroidX Core SplashScreen **1.2.0**
- AndroidX Activity Compose **1.13.0**
- Compose UI / Foundation / Animation **1.12.1**
- Material 3 **1.5.0-alpha28**
- Lifecycle Runtime Compose / KTX **2.11.0**
- OkHttp **5.5.0**
- ZXing Core **3.5.4**

Build-time tools (AGP, Gradle, Kotlin Compose plugin, NDK, and CMake) are identified separately from libraries packaged for runtime. The app's **Info → Libraries** view and the bundled license files provide the applicable component/notice context. Every third-party component remains subject to its own license and upstream terms.

---

## OpenGLESScope

**Inspect your GPU. Inspect your driver. Inspect your OpenGL® ES™ / EGL™ implementation.**
