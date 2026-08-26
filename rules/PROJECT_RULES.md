# OpenGLESScope Engineering Rules

## Non-negotiable
- Third-party comparison product names are forbidden in every shipped filename, source file, generated artifact, test, audit, UI string, report, database field and metadata. Neutral capability-reference terminology must be used instead.
- Dedicated packaged app-store metadata directory bundles are forbidden from source release archives.
- Root release.md files are forbidden from source release archives; release notes, when needed, are distributed separately from the source ZIP.
- README.md files are forbidden from source release archives; project documentation needed for a release must live in purpose-specific audit, rules or changelog files.
- Source-code comments are forbidden.
- Security, correctness, memory safety, performance and usability are never traded away for convenience.
- No known security vulnerability or memory leak may knowingly ship.
- No guessed GPU, OpenGL ES, EGL, display or HDR capability may be reported.
- Unknown, unsupported, unavailable and not-applicable are distinct states.
- Runtime OpenGL ES and EGL extension names are displayed exactly as returned by the implementation.
- OpenGL ES version, GLSL ES version, EGL version and Android platform version are distinct values.
- Android Display HDR data is distinct from OpenGL ES/EGL capability data.
- Wide-color support must not be represented as a measured physical gamut percentage.
- Native collection must never block the UI thread.
- Large capability collections must remain lazy/searchable in the UI.
- Official Khronos OpenGL ES/EGL and Android documentation are primary API-behavior sources.
- Device/display data stays on-device unless the user explicitly submits a complete report.
- Submission excludes IMEI, Android ID, serial, MAC addresses, account data, authentication tokens and private paths.
- No automatic/background report upload is allowed.
- Runtime network access is limited to explicit database submission and the official OpenGLESScope GitHub update check/download flow.
- HTTPS hostnames remain hostnames through TLS. IPv6 is preferred on dual-stack with IPv4 fallback retained.

## ABI
- armeabi-v7a required.
- arm64-v8a required.
- x86_64 required.
- x86 intentionally excluded.
- Release builds must produce four APK artifacts: universal, arm64-v8a, armeabi-v7a and x86_64.
- The universal APK must contain exactly arm64-v8a, armeabi-v7a and x86_64 native libraries and must not contain x86.
- Each split APK must contain only its declared ABI native library.

## Architecture
- Kotlin and Jetpack Compose for UI.
- Material 3 Expressive visual language.
- C++20 with EGL/OpenGL ES for native capability collection.
- JNI boundary remains small.
- No unrelated graphics-API loader or third-party driver bundle loading exists in OpenGLESScope.
- The active Android system EGL/OpenGL ES implementation is the authoritative runtime source.

## OpenGL ES collection
- GL_VENDOR, GL_RENDERER, GL_VERSION and GL_SHADING_LANGUAGE_VERSION are queried from a real current OpenGL ES context.
- OpenGL ES extensions are enumerated with glGetStringi when available and otherwise from the implementation extension string.
- Limits are queried with the corresponding OpenGL ES query and are never inferred from GPU model names.
- Compressed texture formats are reported only from GL_COMPRESSED_TEXTURE_FORMATS.
- Shader precision is reported from glGetShaderPrecisionFormat.
- Core-version feature state is derived only from the runtime GL_VERSION.
- Extension-backed feature state requires exact runtime extension-name evidence.

## EGL
- EGL vendor/version/client APIs and extensions are reported independently from OpenGL ES values.
- EGL configuration data comes from eglGetConfigs/eglGetConfigAttrib.
- EGL config attributes are raw implementation data; they must not be converted into unsupported display claims.

## Display and HDR
- Android Display HDR capabilities remain distinct from OpenGL ES and EGL.
- HDR luminance values are shown only when Android exposes them.
- An empty HDR type list is unavailable, not evidence that the GPU lacks HDR rendering capability.

## Database
- Application identity is OpenGLESScope with package com.efishell.openglesscope.
- Public web URL is https://efishell0.github.io/OpenGLESScope_database/.
- API base is https://openglesscope-database-api.openglesscope.workers.dev.
- Complete reports are all-or-nothing submissions; per-capability omission controls are forbidden.
- Request bodies are bounded to 2 MiB without truncation.
- Stored report IDs are SHA-256 hashes of stable canonical JSON.
- Pagination uses server-authored submitted_at/id ordering.

## Release 0.1.20
- Application version is 0.1.20 with versionCode 120. Database versioning is independent and changes only when the database is explicitly updated.
- Package/namespace is com.efishell.openglesscope.
- Official release repository is EFIShell0/OpenGLESScope.
- Official database repository is EFIShell0/OpenGLESScope_database.
- Primary UI accent is the official OpenGL ES brand tone #BA2A8D throughout the application and database.
- Only OpenGL ES, EGL and Android display capability paths are permitted.

## 0.1.19 audit additions
- OpenGL ES capability collection creates the highest context the system implementation can provide, then reports the actual runtime GL_VERSION without guessing.
- Android HDR type collection uses Display.Mode.supportedHdrTypes on API 34+ and the legacy HDR capability list only on older supported Android versions.
- Update checks follow the official release flow: startup performs a non-blocking metadata-only check, manual checks use the same path, and no APK is downloaded without explicit user confirmation.
- UI, TXT and HTML expose the complete collected OpenGL ES, EGL, display/HDR, limits, extensions, formats, shader precision, query diagnostics and EGL-config datasets. Database submission must remain compatible with the independently versioned accepted schema and includes the complete human-readable report snapshot.
- The update installer uses REQUEST_INSTALL_PACKAGES only for the explicit update flow and a non-exported FileProvider restricted to cache/updates.
- Official OpenGL ES artwork is the only OpenGL ES brand source; the application does not invent a replacement OpenGL ES mark.

- Shader and program binary format enumerations are reported only from the corresponding runtime GL queries.
- Version-gated OpenGL ES 3.0, 3.1 and 3.2 implementation limits are queried directly from the current context.

- JNI entry points use stable non-mangled Java method names and are protected from release shrinking/renaming.
- Phone launcher, round launcher, adaptive launcher and Android TV banner preserve the established application shell layout geometry while using the official OpenGL ES artwork and SCOPE wordmark.
- Application shell quality parity includes icon-based navigation, back/settings/info affordances and landscape navigation behavior matching the established application shell, excluding API-specific pages and brand colors.
- Application-shell parity requires the established navigation geometry, spring page transitions, animated status banners, landscape focus behavior, card spacing/radii and expressive action-button alignment to remain intact unless a platform constraint requires a documented deviation.

## 0.1.19 collector-quality requirements
- Native OpenGL ES/EGL probing runs in the dedicated non-exported `:opengles_probe` process. The UI process must not load the collector native library.
- The probe process uses one worker thread and a process-wide native-probe lock.
- Probe results are complete snapshots published through private-cache temporary-file replacement; partial snapshots are not parsed as final reports.
- Probe result size is bounded to 8 MiB and probe duration to 20 seconds. Timeout/oversize handling terminates the probe process and reports Unavailable instead of retaining a stuck native worker.
- A native-library load failure, JNI failure or probe exception must not crash the main application process.
- Every attempted GL implementation query records an explicit Available or Unavailable diagnostic. Failed queries must not silently disappear.
- Queries that are not applicable to the actual core version or exact extension evidence are not executed merely to populate a field.
- OpenGL ES 2.0/3.0/3.1/3.2 implementation-limit families used by the comparison capability viewer are a minimum coverage floor; current Khronos core requirements remain authoritative.
- Known compressed/shader/program binary enums are displayed as symbolic name plus raw hexadecimal value. Unknown enumerants remain raw hexadecimal values.
- EGL config enumeration covers the EGL 1.5 core configuration attribute set and preserves per-attribute query failure as unavailable/null evidence.
- Current Android display mode, resolution and supported display modes are display evidence only and must not be interpreted as GL/EGL capability.
- UI, TXT and HTML exports must expose every collected dataset and query diagnostic. Export is disabled when the base capability report is unavailable.
- Database submission remains compatible with the independently versioned database schema. No application-only release may mutate the database schema or version.
- Database response parsing is bounded, database URL construction is restricted to the official HTTPS host, and report submission remains explicit and complete-report only.

## 0.1.19 UI and coverage requirements
- Settings is not a standalone destination. Report export and database actions live in Info. Collection policy and raw network-endpoint cards are not exposed as standalone Info sections.
- Primary page animation direction is derived from the visible navigation order, never enum declaration order.
- Search fields preserve the established rounded 22 dp geometry, spacing and placeholder presentation.
- Android-reported HDR types use the established HDR card/logo presentation where matching bundled artwork exists; unknown types remain text and are never guessed.
- Large LazyColumn datasets use stable keys where the collected key is intrinsically unique.
- external OpenGL ES capability reference OpenGL ES 2.0/3.0/3.1/3.2 information families are a minimum comparison floor, excluding sensors and unrelated Android inventory. Khronos core specifications remain authoritative.
- UI, TXT and HTML must continue to expose the same complete collected capability datasets and diagnostics.

## 0.1.19 audit additions
- Android TV D-pad, Enter/Center and Back navigation are first-class usability requirements. All actionable cards and navigation destinations must be focusable through Compose focus traversal and expose a visible focus state.
- Leanback launcher support and non-required touchscreen declarations remain present so television devices do not require touch input.
- Complete database payload parity includes query diagnostics, all collected EGL 1.5 configuration attributes and Android display mode evidence already present in UI/TXT/HTML. Database versioning remains independent.
- “All OpenGL ES queries” means all relevant implementation capability queries for the active core version and evidenced extensions, not mutable rendering state, object state, framebuffer state or synthetic feature tests.

## 0.1.19 export, database and update parity requirements
- TXT and HTML export use Android Storage Access Framework CreateDocument on phones and tablets.
- Android TV export, and document-provider launch failure, fall back to the public Downloads collection rather than silently failing.
- Android 9 and older request WRITE_EXTERNAL_STORAGE only when the Downloads fallback is actually required; the manifest permission is capped at API 28.
- SAF and Downloads writes report success or failure to the user and never silently discard an export error.
- TXT and HTML generation runs off the UI thread and concurrent export generation is blocked until the current snapshot is handed to the destination flow.
- Database submission exposes one in-flight operation at a time, remains disabled without a complete report, uses the fixed official HTTPS endpoint, sends the complete structured dataset plus canonical TXT snapshot, and never truncates the report to satisfy the 2 MiB transport bound.
- Database response materialization is bounded to 64 KiB and success/failure parsing must tolerate an absent report ID without producing misleading UI text.
- The HTTP resolver uses the Android platform DNS resolver, prefers IPv6 addresses when both families are available, retains IPv4 fallback, and never substitutes numeric address literals for HTTPS hostnames.
- Update checks remain asynchronous and metadata-only until explicit download confirmation. Manual and startup checks share the same code path and status-banner behavior.
- Update ABI selection is based on the installed native-library directory when possible, then falls back to Android supported ABIs.
- Release asset URLs are parsed and validated as HTTPS github.com paths under EFIShell0/OpenGLESScope/releases/download with no user-info, query or fragment before download.
- Downloaded update APKs remain bounded, private-cache confined, package-identity checked, signing-certificate checked and strictly newer by versionCode and versionName before the package installer is opened.

## 0.1.19 specification and evidence requirements
- The OpenGL ES 3.2 implementation-dependent-value tables are an explicit core coverage gate. The collector must include the applicable multisample line-width range and granularity, fragment interpolation offset bits, layer provoking vertex, primitive-restart-for-patches support and texture-buffer offset alignment queries in addition to the existing core limit families.
- A runtime extension list is evidence, not an inference source. Extension names must retain runtime spelling and enumeration order. Any extension-specific numeric query must be gated by the exact advertised extension that defines it.
- Feature UI must return Unknown, not Unsupported, when the relevant extension enumeration itself is unavailable.
- Query summaries must keep Available, Unavailable, Not applicable and Unknown separate.
- Android HDR constants may be named only when the platform API level exposes the corresponding official constant; unknown integer values remain identified by their Android HDR type value.
- Android TV release support requires armeabi-v7a and arm64-v8a APK coverage as well as the universal APK, with x86_64 retained for the required desktop/emulator ABI. Native libraries remain 16 KiB-page compatible.

## 0.1.19 Android TV browse and HTML-report requirements
- Android TV D-pad browsing must traverse read-only capability content as well as actionable controls. Read-only section, item, metric, HDR and key/value surfaces are focus targets on television devices and request bring-into-view when focused.
- D-pad Down/Up must be able to advance through capability evidence without requiring a touchscreen, mouse wheel or clickable control.
- Focusability added for television browsing must not turn read-only evidence into an action and Enter/Center must not mutate state on read-only cards.
- HTML report presentation quality tracks the established report shell: embedded application branding, hero metrics, responsive dark layout, section cards, readable wide tables, monospace technical identifiers and explicit query-status badges.
- HTML remains self-contained and does not load remote scripts, styles, fonts or tracking resources.
- TXT and HTML exports do not embed public database, database API or source-repository URLs; exported reports contain collected capability evidence and local report metadata only.
- Runtime extension enumeration is vendor-neutral and complete for the active implementation: every exact GL extension token reported by the implementation and every EGL display/client extension token is retained. Vendor-specific extensions are not guessed from GPU branding.
- Extension-specific numeric capability queries are executed only when the exact defining extension is advertised and the query is valid for the active context; vendor extensions that expose no implementation query are represented by their exact runtime extension token rather than synthetic values.

## 0.1.19 completion and artifact-cleanliness requirements
- Info must not expose standalone Collection policy or Network sections; fixed endpoint and transport policy remain implementation details unless needed for an actionable error or security disclosure.
- TXT export, HTML export and public-database submission remain disabled while capability collection is in progress, after an incomplete collection, and whenever a complete available report snapshot does not exist.
- A report action may become enabled only after the collection coroutine has completed and the parsed snapshot is marked available.
- The probe service accepts result paths only as direct children of the private cache/probe directory with the expected generated filename pattern.
- Release source, resources, documentation, archive entries and binary asset metadata contain no legacy graphics-project identifiers or external comparison-project identifiers.
- Coverage comparison is an engineering audit input only; comparison-project branding is never shipped in application artifacts.

## Release 0.1.20 update-channel requirements
- Application version is 0.1.20 with versionCode 120. Database versioning remains independent and is not changed by this application release.
- The official update source remains the public EFIShell0/OpenGLESScope GitHub repository only.
- Update metadata is obtained from the official GitHub Releases API release list so a repository whose newest published release is marked pre-release does not fail with GitHub's `/releases/latest` 404 behavior.
- Draft GitHub releases are never update candidates.
- Published stable and pre-release entries may be considered, but a candidate must have a parseable dotted numeric version and must be strictly newer than the installed application version.
- At most 20 recent official releases are materialized and the response body is bounded to 2 MiB.
- Candidate ordering is determined by parsed numeric version rather than trusting GitHub list order.
- APK selection retains installed-ABI matching with universal fallback.
- Release asset URLs remain restricted to HTTPS github.com paths under `/EFIShell0/OpenGLESScope/releases/download/` with no user-info, query or fragment.
- Startup update checks remain metadata-only and silent when up to date or when a background check fails. Manual checks use the same metadata path and surface actionable failures.
- No APK download starts until explicit user confirmation. Downloaded APK verification continues to require the expected package identity, signing certificate, strictly newer versionCode and strictly newer versionName before Android's installer is opened.


## Release 0.1.21 system navigation and HTML application parity requirements
- Application version is 0.1.21 with versionCode 121. Database versioning remains independent and is not changed by this application release.
- The Android system navigation-bar surface matches the established VulkanScope shell: #111111, dark-system-bar appearance, and Android 10+ navigation-bar contrast enforcement disabled. The status bar remains black.
- The HTML report Application section uses the same application-information hierarchy as the VulkanScope report while retaining OpenGLESScope identity and brand colors. It includes Version, Version code, Package, Application ABI, Supported device ABIs, Developer, Nickname and the EFIShell0 GitHub profile link.
- The HTML GitHub profile link is presentation/application metadata only. The 0.1.19 prohibition on public Database, Database API and source-repository URLs in TXT/HTML exports remains in force.
- Android/device information remains a separate HTML section and must not be merged into application identity.
- These parity changes must not modify OpenGL ES/EGL collection, capability semantics, diagnostics, report completeness, database submission schema or update security.


## Release 0.1.22 EGL branding and TXT application parity requirements
- Application version is 0.1.22 with versionCode 122. Database versioning remains independent and is not changed by this application release.
- EGL navigation and in-application destination icons use the supplied official EGL artwork, preserving its geometry and transparent silhouette while matching the established OpenGL ES icon sizing and alignment behavior.
- EGL Configs remains a distinct technical destination and does not masquerade as the EGL brand destination.
- TXT Application information follows the established VulkanScope hierarchy while retaining OpenGLESScope identity: Application, version, version code, package, installed application ABI, Developer, Nickname and the EFIShell0 GitHub profile.
- The TXT GitHub entry is application/developer profile metadata only. Database, Database API and source-repository URLs remain excluded from exported TXT/HTML metadata unless a later rule explicitly changes that policy.
- These presentation/export changes must not alter OpenGL ES/EGL capability collection, query semantics, diagnostics, submission completeness, database schema or update-channel security.


## Release 0.1.23 Overview GPU-logo parity requirements
- Application version is 0.1.23 with versionCode 123. Database versioning remains independent and is not changed by this application release.
- The Overview hero GPU vendor artwork uses the same geometry as VulkanScope: an 82 dp outer card, 18 dp corner radius, #111111 card surface, 8 dp internal image padding and ContentScale.Fit.
- Vendor artwork remains presentation-only and must never infer OpenGL ES/EGL capability support from branding.
- This release must not alter OpenGL ES/EGL collection, query semantics, diagnostics, report completeness, database schema/submission behavior, update security or ABI policy.


## Release 0.1.24 end-to-end producer/database and specification-audit requirements
- Application version is 0.1.24 with versionCode 124. OpenGLESScope Database remains independently versioned; this release is validated against Database 0.1.19 without mutating its D1 schema or Worker version.
- Database submission must remain accepted by the Database 0.1.19 exact schema. Top-level display evidence and technicalReport.display remain byte-semantically equivalent after JSON materialization, including nullable mode and wide-color evidence.
- The driver.version database field must not reuse GL_VERSION. OpenGL ES exposes implementation/version identity through GL_VERSION but no standardized standalone graphics-driver version query; absent standardized evidence is reported explicitly as unavailable.
- Android display evidence that cannot be queried on the running API level is null/unavailable, never fabricated as false/unsupported. In particular, wide-color-gamut evidence is unavailable before API 26.
- Android HDR_TYPE_INVALID is never presented as an HDR capability. API 34+ mode-specific HDR types remain authoritative for the active Display.Mode; other unknown non-invalid integer values remain visible as raw Android HDR type values.
- Display/HDR evidence is refreshed when Android reports display addition, removal or change so UI, TXT, HTML and Database submission do not silently retain a stale mode snapshot.
- OpenGL ES 3.0 core implementation-dependent coverage includes GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT. OpenGL ES 3.1+ coverage includes GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT. Both are queried directly only when their core version applies and receive query diagnostics.
- GL_VENDOR, GL_RENDERER, GL_VERSION and GL_SHADING_LANGUAGE_VERSION successful runtime queries receive diagnostics. GL_MAJOR_VERSION and GL_MINOR_VERSION are queried only for OpenGL ES 3.0+ and fall back to the parsed GL_VERSION core version when unavailable.
- UI labels must distinguish parsed core-version fields from direct GL_MAJOR_VERSION/GL_MINOR_VERSION query evidence.
- EGL Config search covers every displayed configuration attribute rather than only ID/renderable/surface fields.
- TXT, HTML, UI and Database payload retain the same collected datasets: runtime identity, limits, exact GL/EGL extension tokens, format enumerations, precision, diagnostics, EGL configs and Android display/HDR evidence. Newly collected core alignment values flow through all four paths without a presentation-only omission.
- Generated HTML remains self-contained, escapes report-derived strings and declares a restrictive local CSP plus no-referrer policy; no remote script, style, font or tracking dependency is permitted.
- Update version ordering ignores SemVer build metadata for precedence and orders a stable release above a pre-release with the same numeric core. Numeric pre-release identifiers compare numerically and build metadata never turns a newer release into an older one.
- Source release archives exclude transient Python bytecode/cache directories and other generated build caches.
- Current Khronos OpenGL ES 3.2 / GLSL ES 3.20 and EGL 1.5 specifications plus current Android Display/HDR API documentation remain the primary specification references. No marketing/vendor database substitutes for runtime evidence.

## Release 0.1.25 full application/specification/database-parity requirements
- Application version is 0.1.25 with versionCode 125. OpenGLESScope Database remains independently versioned and this application release is validated against Database 0.1.21 without changing its exact submission schema or D1 storage.
- Application ABI and Android platform evidence remain present in UI, TXT and self-contained HTML. Database 0.1.21 obtains ABI evidence from the canonical report text; the application must not add unaccepted top-level or technical-report JSON keys merely to duplicate that metadata.
- Current Khronos baselines remain OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5. Runtime evidence and exact extension strings remain authoritative over device/GPU naming.
- The external OpenGL ES capability reference graphics-capability families are a comparison floor only: versions, exact extensions, compressed formats, shader/program binary formats, implementation limits and EGL information must remain covered, while sensors and unrelated Android inventory remain out of scope.
- GL_KHR_debug implementation limits are queried on pre-3.2 contexts only when the exact GL_KHR_debug extension token is present. On OpenGL ES 3.2 they continue to use the corresponding core queries.
- GL_EXT_disjoint_timer_query query-counter bit depths for TIME_ELAPSED_EXT and TIMESTAMP_EXT are collected only when the exact extension token is present and glGetQueryivEXT resolves. Missing entry points or query failures are recorded as Unavailable diagnostics rather than guessed values.
- Every newly attempted extension-backed query must flow through the same limits dataset and query-diagnostic dataset consumed by UI, TXT, HTML and Database submission.
- No query is executed solely because a GPU vendor/model is known. No unsupported, unavailable or unknown state may be converted to Supported.
- Database submission remains all-or-nothing, explicit, bounded to 2 MiB, and restricted to the official HTTPS API hostname.
- Source release validation must check version identity, ABI/report metadata presence, Database 0.1.21 contract compatibility, the new extension-backed query gates and absence of transient build/cache artifacts.

## Release 0.2.1 full application, reporting, security and specification audit requirements
- Application version is 0.2.1 with versionCode 201. Database versioning remains independent.
- OpenGLESScope Database 0.1.26 is the compatibility reference for schema-v2 / technicalReport-v1 structure, report completeness, Display/HDR evidence, canonical TXT identity and transport/security behavior.
- Database 0.1.26 currently accepts only producer versions matching 0.1.x. OpenGLESScope 0.2.1 must never falsify its application version to bypass that server-side producer gate. A Database release that explicitly accepts 0.2.x is required before 0.2.1 submissions can be accepted.
- Android HDR luminance values are physical luminance metadata in cd/m² and must show that unit in the Display/HDR UI, TXT and HTML whenever a value is available. Structured Database fields remain numeric values whose field semantics are luminance in cd/m².
- HDR_TYPE_INVALID is filtered from Android HDR-type evidence on every supported Android API level where the constant exists; it is never reported as a capability.
- Display-change refresh work is limited to the activity display for addition/change callbacks while display removal still forces a refresh, avoiding unnecessary recomposition for unrelated displays.
- Current Khronos baselines remain OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5. Exact runtime strings and exact runtime extension tokens remain authoritative.
- No GPU model, vendor branding, Android version or marketing data may be used to infer OpenGL ES/EGL support.
- UI, TXT, HTML and Database technicalReport payload must continue to expose the same collected runtime identity, exact extensions, limits, runtime formats, shader precision, EGL configuration, query-diagnostic and Android Display/HDR datasets without selective omission.
- Native probe process isolation, 8 MiB probe-result bound, 20-second timeout, HTTPS-only official endpoints, explicit all-or-nothing submission, 2 MiB submission bound, update-package identity/signing/version validation, and non-exported FileProvider/service protections remain mandatory.
- Source release archives must contain no build caches, Python bytecode, Gradle caches, IDE state, APK outputs or other transient generated artifacts.


## Release 0.2.2 Android security-patch end-to-end reporting
- Application version is 0.2.2 with versionCode 202. Database versioning remains independent.
- Android `Build.VERSION.SECURITY_PATCH` is authoritative system patch-level evidence and is collected directly from Android; it is never inferred from SDK, release, device model, submission time or GPU identity.
- The security patch is exposed in the Android information UI, canonical TXT report, self-contained HTML Device section and Database schema-2 `device.securityPatch` field.
- Canonical TXT includes both the top-level `Android security patch: YYYY-MM-DD` evidence line and the DEVICE-section `Security patch: YYYY-MM-DD` line.
- The Database payload and canonical TXT must carry the same patch level.
- Android security-patch evidence is platform metadata only and must never alter OpenGL ES, EGL, Display/HDR, extension, limit or format capability state.
- `Build.VERSION.SECURITY_PATCH` is available from API 23; the application minimum SDK remains 24, so no compatibility fallback or privileged property access is required.
- Current Android API documentation and Android compatibility requirements define the platform security patch as a user-visible date-based patch level.
- Existing native probe isolation, report completeness, export escaping, explicit upload, HTTPS host restriction, 2 MiB submission bound, update verification and ABI policy remain mandatory.

## Release 0.2.3 IzzyOnDroid direct-update policy
- Application version is 0.2.3 with versionCode 203.
- The built-in GitHub self-updater is opt-in and disabled by default for every installation. No startup update discovery request may run until the user has explicitly enabled direct updates.
- Enabling direct updates must require an explicit confirmation that names the official EFIShell0/OpenGLESScope GitHub Releases source and states that APKs installed through this path bypass IzzyOnDroid repository scanning and verification.
- Disabling direct updates must immediately stop future automatic checks and hide pending update UI. Existing package/signing/version/ABI validation remains mandatory whenever direct updates are enabled.
- Settings owns the direct-update opt-in state. Info may expose manual update checking only while direct updates are enabled.
- IzzyOnDroid repository identity must not be inferred from Android installer package identity because repository clients can install packages from multiple repositories. The policy is implemented safely by keeping the self-updater disabled by default independent of installer identity.
- The source release must carry an explicit MIT LICENSE.
- OpenGL ES 3.2, GLSL ES 3.20, EGL 1.5 capability coverage and complete UI/TXT/HTML/Database reporting must not regress because of this release.

## Release 0.2.4 IzzyOnDroid first-install notice
- Application version is 0.2.4 with versionCode 204.
- Direct GitHub updates remain opt-in and disabled by default for every installation.
- A genuine first installation with direct updates disabled shows one seven-second non-modal information banner using the existing update-status banner visual language.
- The notice states only that direct GitHub updates are disabled by default and can optionally be enabled in Settings; it must not imply that IzzyOnDroid updates are disabled.
- The one-time notice must not perform a network request and must not appear on ordinary application upgrades.
- The separate consent that names the official GitHub Releases source and explains the IzzyOnDroid repository scanning/verification bypass remains mandatory before direct updates can be enabled.
- OpenGL ES 3.2, GLSL ES 3.20, EGL 1.5 collection, report completeness, Database submission and capability-state semantics must not regress.


## Release 0.2.5 Obtainium update-management integration
- Application version is 0.2.5 with versionCode 205.
- The built-in direct GitHub updater remains disabled by default and performs no startup network request until explicitly enabled.
- Runtime UI must not claim an IzzyOnDroid-specific security or repository relationship. Obtainium is presented only as an optional external update manager.
- Settings exposes an Add to Obtainium action using Obtainium's documented `obtainium://app/` import path.
- The generated Obtainium configuration targets the official EFIShell0/OpenGLESScope GitHub repository and filters release assets to filenames containing `universal` and ending in `.apk`; architecture auto-filtering is disabled so the universal release asset is selected deterministically.
- If Obtainium is unavailable, the action falls back to the official Obtainium project page instead of silently failing.
- Enabling the built-in updater must explain that, when Obtainium is also used, both update managers can check the same official GitHub Releases source and duplicate update notifications.
- The first-install seven-second information banner keeps the established update-banner visual language and now recommends Obtainium as the external update-management path without performing a network request.
- Existing APK package/signing/version/ABI validation, explicit installation consent, OpenGL ES 3.2 / GLSL ES 3.20 / EGL 1.5 collection and complete UI/TXT/HTML/Database reporting must not regress.

## Release 0.2.6 Obtainium UI simplification
- Application version is 0.2.6 with versionCode 206.
- The Direct GitHub updates setting remains present, opt-in and disabled by default.
- Settings must not expose an Add to Obtainium button or invoke an Obtainium deep link; Obtainium remains an optional external update manager described by informational text only.
- The portable obtainium-config.json remains in the source release and continues to target the official application GitHub repository with a universal-APK filter.
- When Direct GitHub updates are disabled, no startup update discovery request is performed by the application.
- Existing direct-update provenance, package identity, signing-certificate, version and ABI validation remains mandatory when the built-in updater is enabled.
- Graphics capability collection, reporting, export and Database behavior must not regress.

## Release 0.2.7 default-enabled direct GitHub updates
- Application version is 0.2.7 with versionCode 207.
- This release supersedes the default-off/opt-in-by-default clauses of releases 0.2.3 through 0.2.6; their security validation and explicit APK install-action requirements remain in force.
- Direct GitHub updates are enabled by default only when no saved `direct_updates_enabled` preference exists. Existing users' explicit saved choice must be preserved across upgrades.
- A fresh installation may perform the existing non-blocking startup update check against the fixed official EFIShell0/OpenGLESScope GitHub Releases API endpoint.
- Disabling Direct GitHub updates must persist `false`, stop future startup update discovery, hide pending update UI and prevent update APK download until the user explicitly enables the setting again.
- Obtainium remains optional external update-management guidance only; no Add to Obtainium runtime button or deep-link action is reintroduced.
- The portable `obtainium-config.json` remains source metadata and continues to select the official universal APK release asset.
- Update APK download and installation remain explicit user actions. Existing official-release provenance, HTTPS, package identity, signing-certificate, versionCode/versionName and ABI validation requirements remain mandatory.
- OpenGL ES/EGL collection, report/export completeness, Database submission, Display/HDR semantics and capability-state semantics must not regress.

## Release 0.2.8 Settings/Info Material 3 Expressive parity
- Application version is 0.2.8 with versionCode 208. Database versioning remains independent.
- The top-app-bar Info and Settings affordances must match VulkanScope's established Material 3 Expressive vector assets exactly, preserving 24 dp geometry, tint behavior, touch targets and accessibility labels.
- On normal destinations, Info is the left action and Settings is the rightmost action, matching VulkanScope exactly. Settings and Info destinations do not expose either top-app-bar action, matching VulkanScope's hierarchy and preventing cross-destination ordering drift.
- This release is UI-only for the Settings/Info top-bar affordances. OpenGL ES/EGL collection, Display/HDR semantics, report/export completeness, Database submission and update behavior must not regress.
- Direct GitHub updates remain default-enabled for fresh installs subject to the 0.2.7 preference-preservation and update-security rules; Obtainium remains informational only and no Add to Obtainium runtime action is reintroduced.


## Release 0.3.0 full Material 3 Expressive application parity
- Application version is 0.3.0 with versionCode 300. Database versioning remains independent and is not changed by this visual release.
- AndroidX Compose Material3 is 1.5.0-alpha26 and the application shell uses `MaterialExpressiveTheme` with `MotionScheme.expressive()` while retaining OpenGLESScope's established black/dark surfaces and official brand accent `#BA2A8D` with `#F06BC7` as the existing soft accent.
- Existing OpenGLESScope visual identity, official OpenGL ES/EGL artwork, GPU-vendor logos, HDR artwork, information hierarchy, destination order, page geometry, card spacing and technical-content density remain intact; Material 3 Expressive may refine component state, shape and motion but must not redesign the product into a different visual identity.
- Portrait primary navigation uses the Material 3 Expressive short navigation bar/item family. Selected/unselected semantics and destination ordering remain unchanged. Android TV/landscape compact navigation rail focus acquisition, bring-into-view behavior, destination order and destination semantics must not regress.
- Top-app-bar back, Info and Settings actions use expressive icon-button shape/state behavior while preserving the 0.2.8 Info-left/Settings-right ordering, 24 dp icon geometry, 48 dp touch targets and accessibility labels.
- Search fields retain the established OpenGLESScope 22 dp geometry required by the 0.1.19 UI rules while adopting expressive tonal containers, focus colors and the search affordance.
- Format/extension selectors, Explore actions and collection-state chips use expressive filter/assist chip state and shape language without changing filtering semantics or technical labels.
- The Direct GitHub updates toggle uses expressive switch state treatment while preserving the 0.2.7 default-enabled preference behavior and all update-security requirements.
- Update confirmation, direct-update consent, update/collection banners, update actions and progress/loading indicators use expressive dialog/button/container/loading/progress language while preserving all existing actions, timing, provenance checks and user consent boundaries.
- Remaining shared functional navigation/action vector icons use the same rounded Material 3 Expressive geometry established by VulkanScope 0.35.0. Official OpenGL ES/EGL, GPU-vendor and HDR artwork are data/brand assets and must not be redrawn as generic Material icons.
- Experimental Material 3 Expressive APIs are opted into only at the smallest wrapper/composable scope that directly requires them.
- OpenGL ES/EGL native collection, probe isolation/timeouts, Android Display/HDR semantics, UI/TXT/HTML completeness, Database payload/schema compatibility, Obtainium metadata, GitHub update behavior and release ABI policy are functionally unchanged by this visual release.
- `ShortNavigationBarItemDefaults.colors` must use the Material3 1.5.0-alpha26 parameters `selectedTextColorTopIconPosition`, `selectedTextColorStartIconPosition` and `selectedIndicatorColor`; removed/legacy color parameter names are forbidden.


## Release 0.3.1 binary/compressed format enumeration completeness
- Application version is 0.3.1 with versionCode 301. Database versioning remains independent and is not changed by this application release.
- Compressed texture formats must continue to come only from `GL_NUM_COMPRESSED_TEXTURE_FORMATS` and `GL_COMPRESSED_TEXTURE_FORMATS`; extension strings or GPU identity must never synthesize entries.
- Shader binary formats must continue to come only from `GL_NUM_SHADER_BINARY_FORMATS` and `GL_SHADER_BINARY_FORMATS`.
- Program binary formats are queried only when OpenGL ES 3.0+ or exact `GL_OES_get_program_binary` runtime evidence makes the query applicable, and values come only from `GL_NUM_PROGRAM_BINARY_FORMATS` / `GL_PROGRAM_BINARY_FORMATS`.
- Known Khronos/Android registry enumerants used by these three runtime lists are displayed as canonical symbolic name plus raw hexadecimal value. Unknown or future enumerants remain raw hexadecimal values and are never guessed.
- UI, TXT, HTML, diagnostics and Database submission continue to consume the same collected arrays without omission or schema mutation.
- Material 3 Expressive UI behavior from 0.3.0, probe-process isolation, bounds, HTTPS restrictions, explicit upload policy and update verification remain unchanged.


## Release 0.3.2 current-registry binary-format maintenance
- Application version is 0.3.2 with versionCode 302. Database versioning remains independent and is not changed by this application release.
- The 0.3.1 runtime-only format-enumeration rules remain mandatory.
- Current Khronos OpenGL ES Extension Registry binary-format enumerants must be decoded when a canonical registered name exists; as of the 2026 registry this includes `GL_SHADER_BINARY_HUAWEI` value `0x9770` and `GL_PROGRAM_BINARY_HUAWEI` value `0x9771`.
- Symbolic decoding never creates capability evidence. A symbolic name is applied only to a numeric value actually returned by the corresponding runtime format array query.
- Unknown or future enumerants remain raw hexadecimal values. No GPU vendor/model inference is permitted.
- external comparison reference remains a comparison floor for relevant OpenGL ES/EGL capability presentation, while Khronos core/extension specifications are authoritative when the two differ or the registry gains newer enumerants.
- Release metadata in README, Gradle, release notes, changelog, release metadata and audit tooling must agree on versionName/versionCode.


## Release 0.3.3 full correctness, reporting, security and resource-hygiene audit
- Application version is 0.3.3 with versionCode 303. Database versioning remains independent; the compatibility reference for this producer is OpenGLESScope Database 0.2.5.
- Runtime capability evidence must remain authoritative and implementation-derived. No GPU/vendor identity, model table, allowlist or guessed capability may create OpenGL ES, EGL, Display or HDR support claims.
- Each attempted OpenGL ES implementation query used to enumerate extensions or format arrays must leave explicit diagnostic evidence for the actual query operation. Count queries and value-array queries are separate attempted operations and therefore receive separate diagnostics. A value-array operation that is not executed because its authoritative count is zero must be represented as not applicable rather than as an attempted successful query.
- OpenGL ES core-version presentation must retain provenance. On contexts where direct `GL_MAJOR_VERSION` and `GL_MINOR_VERSION` queries are valid and successful, UI/TXT/HTML must identify direct-query provenance; fallback parsing from `GL_VERSION` must be labeled as parsed runtime evidence.
- Disabling Direct GitHub updates is an immediate runtime opt-out: active release-metadata HTTP calls, APK HTTP calls and update coroutines must be cancelled, pending update state must be cleared, and no package installer may be opened by work that completed after the opt-out.
- A downloaded APK that fails package identity, signing-certificate, versionCode, versionName or archive validation must not remain as a validated target in the private update cache. Temporary partial files must also be removed.
- On Android 9+ single-signer key rotation, signing compatibility is directional: the currently installed APK signer must be present in the candidate APK signing lineage. A candidate signed only by an older ancestor certificate must not pass merely because the two histories intersect. Multi-signer and legacy cases require exact current signer-set equality.
- Query-diagnostic additions must flow through the existing UI Diagnostics destination, TXT report, HTML report and Database technical report without weakening the all-or-nothing report requirement or mutating the schema merely for presentation detail.
- UI, TXT, HTML and Database reports must continue to expose limits, exact GL/EGL extension datasets, compressed texture formats, shader/program binary formats, shader precision, query diagnostics, EGL Configs and Android Display/HDR evidence without silently dropping collected technical data.
- Native EGL/OpenGL ES probing must continue to release the current context, surface, context and display resources on all normal completion paths; service worker shutdown, isolated process behavior, 8 MiB result bound and 20-second timeout remain mandatory.
- Release metadata, README, changelog and static audit identities must agree on 0.3.3 / 303.


## Release 0.3.4

- Application version is 0.3.4 with versionCode 304.
- Query diagnostic names in a complete report are canonical and unique.
- Repeated internal reads of the same named query must not create duplicate diagnostic entries.
- If repeated reads disagree, the single canonical diagnostic must preserve the conservative unavailable/inconsistent result rather than hide the conflict.
- Count and array enumeration evidence remain distinct by their actual GL query names.
- Database submission must pass the strict duplicate-diagnostic rejection contract.

## Release 0.4.0 local analysis and optional tests

- Application version is 0.4.0 with versionCode 400.
- Analysis functionality is local-only and must not mutate the canonical TXT, HTML or Database technical report schema or capability evidence.
- Portable analysis snapshots use the separate `OpenGLESScopeAnalysisSnapshot1` schema and are not accepted as Database technical reports.
- Snapshot import is explicit through Android SAF, is bounded to 2 MiB, performs no background network access and rejects other snapshot schemas.
- Report diff output describes submitted/runtime evidence changes only. A regression candidate is not a claim that a capability or driver is defective.
- OpenGL ES minimum comparisons use only verified Khronos OpenGL ES 3.2 minimum values embedded by the release. Missing or non-numeric runtime values remain Unknown and are never treated as failures.
- Watched capability tokens are local preferences only and do not alter report collection or submission.
- Extension detail is based on exact enumerated runtime tokens plus the embedded Khronos engineering baseline. Promotion, dependency or support state must not be inferred when corresponding registry/runtime evidence is absent.
- Format detail preserves canonical symbolic names and raw values where registered; unknown future values remain raw evidence.
- Optional active tests require explicit user action and execute in the existing isolated probe process with a 20 second client timeout. Test PASS/FAIL/UNAVAILABLE/NOT_APPLICABLE states are test evidence only and never rewrite capability state.
- Program-binary self-test data is bounded to 16 MiB and temporary test objects are destroyed before the probe returns.
- The self-test creates and destroys its own EGL context/surface, shaders and programs and terminates EGL on every completed path.
- Canonical Database schema remains schema 2 / technicalReport 1.
- OpenGLESScope Database 0.2.8 remains the audited compatible Database release for the unchanged canonical report contract.


## Release 0.4.1 VulkanScope-quality feature parity
- Application version is 0.4.1 with versionCode 401.
- Analysis must expose Compare, Spec minimums, Graph, Quality, Watched, Share and isolated Tests using OpenGL ES/EGL-native evidence semantics.
- Analysis snapshots are bounded to 8 MiB, 32,768 entries, 1,024-character keys and 16,384-character values; overflow fails instead of truncating evidence.
- Watched evidence is bounded to 256 entries and supports Matched/Missing filtering.
- OpenGL ES 3.2 implementation-dependent requirement evaluation uses direct runtime queries and preserves minimum-versus-maximum direction; missing evidence stays Unknown.
- Dependency graph edges come only from exact runtime extension/query gates implemented by OpenGLESScope and never from GPU/vendor inference.
- Evidence-quality scoring uses explicit diagnostics/safety anomalies only and is never presented as conformance, benchmark or driver-quality evidence.
- Canonical Database permalinks use `#reports/<64-hex-id>/Overview`; local QR generation must not require a remote service.
- Database submission must not follow HTTP or HTTPS redirects and remains explicit complete-report only.
- Isolated self-test results may be attributed only when GL_VENDOR, GL_RENDERER and GL_VERSION match the selected report.
- Vulkan-only APIs and tuple-state models are not synthesized in OpenGLESScope. Parity applies to quality architecture, evidence-state semantics, analysis, reporting, safety and usability.
- Current companion Database is OpenGLESScope Database 0.2.9; canonical report schema remains 2 and technicalReport schema remains 1.

## Release 0.7.0 full correctness, security, EGL and reporting audit
- Application version is 0.7.0 with versionCode 700.
- Current companion Database is OpenGLESScope Database 0.7.0.
- Submission schema remains 2; OpenGLESScope 0.7.0 technicalReport schema is 2.
- Current Khronos engineering baselines remain OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5.
- The checked-in external public capability floor remains 145/145 and 49 additional OpenGLESScope implementation queries are release-gated.
- Exact-extension-gated implementation queries added in 0.7.0 must retain explicit runtime extension prerequisites and end-to-end UI/TXT/HTML/Database paths.
- Mutable state or API control hints must not be mislabeled as implementation capability limits; specifically GL_NUM_WINDOW_RECTANGLES_EXT and GL_MAX_SHADER_COMPILER_THREADS_KHR are excluded from limit reporting.
- EGL runtime binding/context/surface evidence and bounded EGL Config extension attributes must preserve unavailable/error provenance rather than fabricating support.
- Driver-controlled enumeration counts, runtime strings, extension tokens, info logs and self-test binaries remain bounded.
- Native GL error draining must be finite and EGL teardown must release thread-local EGL state.
- Probe publication on minSdk 24 must not depend on java.nio.file.Files APIs unavailable on that platform floor.
- Full release requires the 0.7.0 source verifier, syntax/data checks and source-package hygiene to pass. Full Android compilation is claimed only when actually completed by the toolchain.

## Release 0.7.1 VulkanScope-quality application parity and source-package cleanup
- Application version is 0.7.1 with versionCode 701. The audited companion Database is OpenGLESScope Database 0.7.2.
- Quality parity with VulkanScope applies only where an OpenGL ES, EGL, Android display or shared application architecture equivalent exists. Vulkan-specific capability models must never be synthesized.
- Analysis snapshots must include all current EGL runtime binding/context/surface evidence, every current EGL Config extension-backed attribute and exact unavailable-attribute error provenance.
- Implementation limits and query diagnostics are separate evidence classes with separate counts and search surfaces; diagnostics must never inflate limit totals.
- Formats and shader-precision evidence must be independently searchable without truncating the collected dataset.
- EGL runtime presentation must separate identity, current context/binding, collector pbuffer and explicit unavailable-attribute error evidence.
- Extension detail may navigate to the authoritative Khronos registry document and may show exact OpenGLESScope query gates, but registry navigation must never infer runtime support, promotion or dependency state.
- The Analysis query graph covers exact implemented OpenGL ES and EGL query gates and keeps runtime enumeration separate from query-result evidence.
- Diagnostic quality scoring incorporates explicit EGL current-binding failures while remaining a collection-evidence heuristic, never a conformance, performance or driver-quality verdict.
- Android runtime/device provenance exposed in UI, TXT and HTML includes manufacturer, brand, model, product, device, board, hardware, Android release/codename/SDK, build ID, incremental build, security patch and fingerprint. Empty platform strings are represented as Unavailable.
- Database submissions from 0.7.1 include the installed application ABI and Android-supported device ABI list in application metadata. Submission schema remains 2 and technicalReport schema remains 2.
- README.md, root release.md, dedicated packaged app-store metadata directories and forbidden third-party comparison product naming are absent from the source release archive.
- Release verification must fail if any of these parity, provenance, archive-hygiene or evidence-separation requirements regress.
## Release 0.7.2 compile correctness and shared-quality parity
- Application version is 0.7.2 with versionCode 702. The audited companion Database is OpenGLESScope Database 0.7.3.
- Any use of experimental Material 3 or Material 3 Expressive APIs must carry the exact required compile-time opt-in at the narrowest appropriate composable/function scope; release compilation may not rely on suppressed or ignored experimental-API diagnostics.
- Analysis self-test loading behavior follows the shared VulkanScope Material 3 Expressive implementation pattern and must compile under the pinned Material 3 dependency.
- A release that reaches Kotlin compilation and fails on an experimental API is a release-blocking defect even when native compilation and resource processing succeed.
- Existing OpenGL ES/EGL evidence, Analysis, report, export, submission and query-state semantics remain unchanged by a compile-only correction.
- README.md, root release.md, dedicated packaged app-store metadata directories and forbidden third-party comparison product naming remain absent from the source release archive.
- Release verification must explicitly gate the Analysis Material 3 Expressive opt-in so this compile regression cannot recur.

