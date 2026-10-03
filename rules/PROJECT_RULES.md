# OpenGLESScope Engineering Rules

This file is the single release-blocking engineering contract for OpenGLESScope. It applies the same evidence discipline, regression methodology, UI quality, lifecycle ownership, bounded-resource rules, accessibility requirements, deterministic packaging and clean-extract verification used by VulkanScope 3.0.12, while keeping OpenGL ES/EGL semantics truthful. API-specific Vulkan concepts are never invented merely to obtain superficial feature parity.

## Exact VulkanScope 3.0.12 methodology reference
- `rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md` is the byte-for-byte VulkanScope 3.0.12 `rules/PROJECT_RULES.md` reference and is immutable release evidence.
- Every API-neutral VulkanScope 3.0.12 rule class is mandatory here: correctness, provenance, lifecycle, crash containment, performance, storage, file-manager behavior, paging, update transfer, accessibility, Android TV/D-pad, desktop pointer behavior, edge-to-edge layout, deterministic packaging, negative mutation and clean-extract verification.
- API-specific Vulkan rules are adapted only when OpenGL ES/EGL has an authoritative equivalent. Otherwise the rule is explicitly Not applicable rather than imitated with fabricated data.
- `tests/golden/vulkanscope_3_0_12_rule_headings.txt` freezes the exact VulkanScope level-2 rule-section census. Methodology gates must verify the reference SHA-256 and heading snapshot.
- The OpenGLESScope contract may be stricter than the VulkanScope reference where GL/EGL safety or query legality requires it; it must never be weaker merely because the graphics API differs.
- The exact VulkanScope reference contains 199 level-2 rule sections; all 199 are accounted for by `tests/golden/vulkanscope_3_0_12_rule_applicability.json`.

## VulkanScope 3.0.12 complete methodology applicability census
- `tests/golden/vulkanscope_3_0_12_rule_applicability.json` is release evidence, not documentation decoration. It must contain exactly one entry for each of the 199 level-2 headings in the immutable VulkanScope 3.0.12 reference, in the same order and with the same heading text.
- `adopted` means the API-neutral rule class applies directly to OpenGLESScope.
- `adapted` means the rule class applies with OpenGL ES/EGL authoritative equivalents and must retain equivalent failure semantics, bounds and test strength.
- `api_specific_reference` means the Vulkan product feature itself is not fabricated in OpenGLESScope; only its API-neutral engineering lesson is inherited through an explicit current section.
- `historical_methodology_reference` means the historical release detail remains auditable evidence while the current OpenGLESScope contract supersedes it for implementation.
- Missing, duplicated, reordered or unclassified VulkanScope headings are release-blocking. An API-specific rule may be Not applicable as a product feature, but it may not be silently ignored as a methodology class.

## Non-negotiable
- Correctness, security, memory/resource safety, lifecycle safety, performance, accessibility, report integrity and usability are release-blocking.
- A reproducible crash, deadlock, infinite wait, stale-process mutation, unbounded allocation, silent report loss, fabricated canonical name, incorrect query gate, privacy leak or security bypass blocks release.
- Runtime support comes only from authoritative runtime evidence. GPU marketing name, SoC, Android version, device model, registry presence and extension registration are not runtime support evidence.
- Registry metadata and runtime evidence are separate evidence classes everywhere: UI, TXT, HTML, structured report, Database payload and Encyclopedia.
- Registry presence does not prove runtime support.
- Extension-name presence alone does not prove a query result.
- `Unknown / not queried` is preserved as a distinct evidence state when no legal runtime query has established a value.
- Missing, failed, unsupported, unavailable, not applicable, incomplete and unknown/not queried states remain distinguishable whenever their meanings differ.
- Unknown numeric enum values are preserved numerically/hexadecimally. A symbolic name is shown only when the locked current Khronos registry proves that exact name/value relationship.
- Historical or desktop-only aliases absent from the locked Android-relevant registry path must never be presented as current OpenGL ES/EGL canonical names.
- Complete report upload is explicit user action only. Silent/background report submission is forbidden.
- Static inspection, model tests, Android compilation, lint/unit tests, sanitizer/profiler runs and physical-device tests are separate evidence classes. NOT EXECUTED is never presented as PASS.

## Release evidence taxonomy
- `SPEC-MISSING`: authoritative registry/spec capability is materially missing from the collector.
- `QUERY-MISSING`: a legal runtime query is omitted or not executed behind its correct gate.
- `QUERY-ILLEGAL`: a query is executed for the wrong core version, extension scope, object type or attribute class.
- `REPORT-LOSS`: canonical collected evidence is dropped or semantically changed in export/submission/UI.
- `FABRICATED-NAME`: a GL_/EGL_ symbolic label is not canonical in the locked registry or a descriptive label masquerades as a token.
- `BUG`: deterministic implementation defect not covered by a more specific class.
- `CRASH/LIFECYCLE`: process, service, coroutine, JNI, EGL context or resource-ownership failure.
- `SECURITY`: origin, path, package, signing, input validation, privacy or permission defect.
- `MEMORY/RESOURCE`: unbounded allocation, leaked descriptor/object/thread/context or unsafe driver-controlled count.
- `PERFORMANCE`: avoidable repeated expensive work, unbounded Compose work, repeated native collection or pathological scan/query complexity.
- `ACCESSIBILITY/UX`: keyboard/TV/TalkBack/large-text/rotation/input/visual hierarchy defect.
- `TEST-GAP`: a claimed guarantee lacks a regression oracle, state model or negative mutation.

## ABI and Android platform
- Supported ABIs are arm64-v8a, armeabi-v7a and x86_64. Legacy x86 remains unsupported.
- minSdk is 31 for the current release line; older historical audits document previous minimums.
- compileSdk is Android API 37.2 and targetSdk is 37 (Android manifest targets major API levels).
- 16 KiB page compatibility and native hardening remain release-gated.
- Orientation and screen-size changes must not restart native collection merely to redraw the UI.

## Architecture
- UI is Kotlin + Jetpack Compose + Material 3 Expressive.
- Collection is C++20 behind a dedicated `:opengles_probe` Android service process and a deliberately small JNI boundary.
- The main process owns UI/report state. The probe process owns one terminal collection attempt and exits after atomic publication.
- UI recomposition never triggers native recollection.
- OpenGL ES core/GLSL ES, GL extensions, EGL core/runtime, EGL client extensions, EGL display extensions, Android Display/HDR and Android build/device provenance remain separate evidence layers.
- App-private analysis history and watched evidence are offline by default and never uploaded without explicit user action.

## Probe process and terminal publication
- Each collection attempt has a unique bounded result path in app cache and a separate terminal marker.
- A terminal marker is accepted only after a complete bounded JSON payload is atomically published and structurally valid.
- Partial JSON, oversized JSON, stale previous-run files and malformed terminal objects are failures, not success.
- The main process has a bounded timeout and performs bounded non-cancellable teardown on cancellation.
- A hard process watchdog terminates a stuck dedicated probe.
- One process owns one terminal publication; double publication and post-terminal mutation are forbidden.
- Service destruction tears down worker/watchdog state and the process terminates.
- Self-tests use the same isolation model and are attributed to the current report only when runtime GL identity matches.

## Native ownership and bounds
- EGLDisplay, EGLContext, EGLSurface, thread-local EGL state, shader/program objects and all heap containers have deterministic teardown.
- Every driver-controlled count is validated against an explicit ceiling before allocation or iteration.
- GL extension count, EGL config count, device count, DMA-BUF format/modifier counts, internal-format sample counts, program binaries, logs and runtime strings remain explicitly bounded.
- GL error draining is finite. EGL errors are intentionally consumed and recorded so stale errors cannot contaminate later evidence.
- JNI payloads remain bounded and small enough for safe publication.

## Khronos registry lock
- `registry/gl.xml` is the release-locked Combined OpenGL Registry source for OpenGL ES tokens, commands, extensions, aliases and core features.
- `registry/egl.xml` is the release-locked EGL API Registry source.
- Exact SHA-256 hashes are recorded in `registry/registry_lock.json`, manifests, CMake and release gates.
- A registry hash change requires regeneration of derived catalog/coverage artifacts plus query legality, canonical-name and report-surface review.
- Generated registry artifacts are never edited by hand to hide generator or source drift.

## Canonical-name and fabricated-name contract
- Every hard-coded string literal that syntactically claims to be `GL_*` or `EGL_*` must be one of: a canonical registry enum, a canonical registry extension name, or an explicitly allowlisted API string such as GL_VENDOR/GL_RENDERER/GL_VERSION/GL_SHADING_LANGUAGE_VERSION/GL_EXTENSIONS.
- A descriptive diagnostic must not invent a token-looking name. Example: timer query evidence uses `Query counter bits: GL_TIME_ELAPSED_EXT`, not a fabricated `GL_*_QUERY_COUNTER_BITS` token.
- Hard-coded numeric enum/name pairs must exactly match the locked registry value.
- Aliases are rendered as aliases; they never replace the canonical locked-registry name silently.
- Unknown future runtime values remain raw hex/numeric evidence.
- GL and EGL canonical-name audits are both release-blocking; auditing GL only is insufficient.

## OpenGL ES core baseline
- Current core baseline is OpenGL ES 3.2 and GLSL ES 3.20 unless authoritative upstream changes.
- GL_VENDOR, GL_RENDERER, GL_VERSION and GL_SHADING_LANGUAGE_VERSION are bounded runtime strings and are not rewritten.
- Parsed version and GL_MAJOR_VERSION/GL_MINOR_VERSION evidence are cross-checked where legal; a higher version is never inferred.
- Core query legality follows exact core version requirements.
- Default-framebuffer color/depth/stencil bit evidence comes from GL runtime state, not substituted EGLConfig metadata.
- Array/vector/indexed limits use the correct query form and preserve per-index evidence.
- Shader precision uses `glGetShaderPrecisionFormat` and preserves exact range/precision values.
- Count+enumeration APIs preserve both count-query status and enumerated values without double-counting capability state.

## Features and limits
- Core and extension implementation limits are collected only through legal GL/EGL runtime queries with exact version/extension ownership.
- Feature/limit names come from the locked registries; app-created convenience labels must be descriptive and must not mimic GL_/EGL_ tokens.
- UI summaries never replace the underlying complete values and diagnostics in reports.

## OpenGL ES core query census
- The registry-derived GLES 2.0–3.2 capability-like GetPName census is recalculated from `gl.xml` every release.
- A census change is an upstream-baseline event, not permission to update a golden number blindly.
- Every required core capability query must either be collected legally or have an explicit specification-backed exclusion.
- Query failure is Unavailable/diagnostic evidence and never a fabricated zero, false or Unsupported state.

## OpenGL ES extension contract
- Runtime extension enumeration is authoritative for extension presence.
- Extension-defined implementation queries execute only behind the exact owning extension gate unless an audited core promotion path makes the same query legal.
- Similar extension names do not imply interchangeable query legality.
- `eglGetProcAddress` results are null-checked before invocation.
- Extension query diagnostics remain separate from support counts.
- Current-registry extension GetPName candidates are regenerated and triaged each release: collect, explicitly exclude with specification evidence, or block release.
- Device/vendor-specific implementation queries are never inferred from vendor string alone; the owning extension must be present.

## OpenGL ES extension lookup performance
- Runtime extension lists are normalized once into deterministic sorted unique vectors.
- Repeated extension membership checks use logarithmic lookup over the normalized vector rather than repeated linear scans.
- Normalization must not fabricate, rename or discard distinct registered tokens; only exact duplicate strings may collapse.
- Extension output ordering is deterministic after normalization, improving report diff stability without changing evidence semantics.

## EGL core baseline
- Current EGL core baseline is EGL 1.5 unless authoritative upstream changes.
- Client extensions (`EGL_NO_DISPLAY`) and initialized-display extensions remain separate datasets.
- EGL vendor/version/client APIs, bound API, current context/display/surfaces and collector pbuffer evidence remain separate fields.
- EGLConfig attributes are queried directly and failed attributes retain error/provenance.
- Creation-only attributes are never presented as legal `eglQueryContext` runtime state.
- ES3 context creation requires EGL 1.5 or the exact `EGL_KHR_create_context` path; safe ES2 fallback remains independent.
- Extension scope is exact: client extensions gate client-scope entry points; display extensions gate initialized-display functionality.

## EGL query and configuration performance
- Invariant extension gates used for every EGLConfig are computed once per display, outside the per-config loop.
- Directory-like/config-like enumeration remains bounded before allocation.
- Repeated local metadata lookup must not dominate driver query cost when a direct indexed/local result is available.
- Performance changes must preserve every config row and every unavailable-attribute diagnostic; speed never justifies evidence loss.

## Formats and binary evidence
- Compressed texture formats, shader binary formats and program binary formats use runtime count+enumeration evidence.
- Internal-format capability queries use legal targets and version/extension gates.
- Unknown format/binary values remain raw numeric evidence when no locked-registry symbolic name exists.
- Per-sample/internal-format details remain bounded.

## Android Display and HDR
- Android Display/HDR is platform evidence and never rewrites GL/EGL support state.
- Wide color gamut is a platform boolean/evidence state, not a fabricated measured gamut percentage.
- HDR luminance is shown only when Android supplies it.
- Current-mode HDR types and legacy HdrCapabilities provenance remain distinguishable where platform APIs differ.
- Unknown future integer HDR types remain explicit unknown values rather than mislabeled logos.

## Complete report contract
- Canonical structured evidence is lossless within defined safety ceilings.
- TXT, self-contained HTML, analysis snapshot and Database submission must not silently omit a canonical complete-report section.
- HTML escapes runtime/untrusted strings and includes restrictive local CSP.
- Structured/query state and human-readable text must agree semantically.
- Report generation never turns Unknown/Unavailable into Unsupported merely for presentation simplicity.

## Database submission
- Submission is explicit and complete-report gated.
- Endpoint is the fixed official HTTPS Worker origin; redirects are disabled.
- Request body is bounded to 2 MiB and never truncated to fit.
- Response reading is independently bounded.
- Success requires a canonical lowercase 64-hex report ID before permalink/success state is entered.
- Malformed success bodies are failures.
- Producer version/versionCode and technicalReport schema must match the companion Database contract exactly.
- When the app version ceiling advances, release is blocked until a companion Database release accepts that exact producer or Database submission is explicitly disabled for that app release.

## Privacy
- Reports and Database payloads never collect IMEI, serial, Android ID, MAC address, account identifiers, auth tokens, cookies or private filesystem paths.
- Android build/device metadata may include non-secret platform/build fields already required by the schema, but must not be extended with unique/private identifiers.

## Network policy
- Network behavior supports normal IPv4, IPv6 and dual-stack Android networks.
- No IPv4-only literals or address-family assumptions are allowed.
- Validated internet is checked for network-only user actions; offline inspection remains usable without network.

## Application updates
- Release discovery is limited to the official `EFIShell0/OpenGLESScope` GitHub Releases channel.
- Asset URL, package identity, signing identity, versionCode and exact selected versionName are verified.
- Trusted release metadata asset size is retained and compared to the completed download.
- Download size is bounded to 256 MiB.
- Disabling direct updates cancels active work and removes pending update artifacts.
- Update status distinguishes checking, offline/unavailable, up-to-date, available, downloading, paused, canceled, failed, verified and installer handoff states.
- The semantic update-check action uses the update-check icon, not a generic download icon.

## Shared-storage security
- Shared-storage browsing stays inside the canonical shared-storage root.
- Path traversal, symlink escape and non-canonical destinations are rejected.
- Folder scans are bounded to 4096 entries.
- Import extension and byte-size limits are validated before parsing.
- Export uses atomic temporary write/replace semantics and explicit overwrite confirmation.
- Legacy WRITE_EXTERNAL_STORAGE fallback and SAF-provider ambiguity do not return unless separately audited.

## File Manager parity
- The shared-storage File Manager follows the same interaction quality as VulkanScope 3.0.12 while retaining OpenGLESScope colors.
- Six view modes are available: List, Compact list, Detailed list, Grid, Dense grid and Large tiles.
- Six sort modes are available: Name A–Z, Name Z–A, Modified newest/oldest, Created newest/oldest.
- The most recently selected view/sort mode persists locally.
- Search is bounded and never causes an unbounded full-tree scan.
- Breadcrumb navigation exposes the canonical path hierarchy without showing an editable raw filesystem path.
- Back first dismisses the IME, then navigates to the parent folder, then closes at root.
- Import/export validation remains identical regardless of visual layout.
- Folder/file icons are semantic and stable; view-mode and sort-mode icons are not reused for unrelated actions.
- The File Manager remains usable in portrait, landscape, narrow windows, desktop pointer input and Android TV/D-pad navigation.

## Icon semantics
- API-neutral shared icons must remain byte-identical to the VulkanScope 3.0.12 assets unless an audited OpenGLESScope-specific semantic replacement is required.
- Vulkan-only icons are not copied merely to inflate parity.
- OpenGLESScope/OpenGL ES/EGL brand icons remain app-specific.
- One visible action must not use a misleading unrelated icon when a correct shared icon exists.
- Update check uses `ic_check_updates`; download/receive remain transfer semantics.
- Encyclopedia uses `ic_book`; reference search uses `ic_search`; evidence-boundary guidance uses `ic_shield`; opening-animation preference uses `ic_opening_animation_toggle`.
- Settings uses `ic_settings`; Info/About uses `ic_info`; Database submit/browse actions retain their dedicated database icons.
- Composite artwork is permitted only when a checked-in parity rule documents the semantic composition. Decorative invention that implies nonexistent capability is forbidden.

## Primary navigation parity
- The compact bottom navigation is used in portrait and landscape; a navigation rail must not reappear.
- The primary navigation has four API-appropriate destinations, matching VulkanScope 3.0.12 density: Overview, OpenGL ES, EGL and Extensions.
- Display & HDR, Features, Limits, Formats, Precision, EGL Configs, Encyclopedia, Analysis and Settings are secondary destinations reached from Overview/header or contextual actions.
- Secondary pages select Overview in primary navigation rather than inventing a fifth primary destination.
- Selection animation uses a rounded capsule and semantic icon/text state; no square highlight is allowed.
- Primary navigation stays above Android system navigation insets and remains usable in landscape.

## Settings information architecture
- Settings is entered from the header settings action and is not duplicated as a separate primary tab.
- Settings contains exactly the current meaningful areas: Info, Reports & Database, and Update/Startup preferences.
- A separate unreachable `Page.Info` destination is forbidden; Info is owned by the Settings information architecture.
- Update/startup settings include direct GitHub update preference and opening-animation preference.
- Settings subpage back behavior returns to Settings before leaving for Overview.
- Settings cards use the same Material 3 Expressive geometry, typography, spacing and semantic artwork as the rest of the app.

## Encyclopedia parity
- Encyclopedia is offline and generated only from the locked `gl.xml` + `egl.xml` catalogs.
- It has separate introductory/evidence-boundary guidance, Reference search, and How to read entries sections.
- Search is bounded to 160 input characters and 250 visible matches.
- Large Commands/Tokens/Types families require at least two query characters before enumeration into Compose UI.
- Catalog decompression is bounded to 2 MiB and entry count to 6000.
- Malformed individual catalog rows are skipped; invalid top-level schema fails closed without crashing the whole app process.
- Extension runtime state is derived only from exact runtime enumeration; non-extension registry entries are labeled registry reference, not runtime supported.
- Registry owners, alias/value/group/signature/definition fields are presented only when present in generated catalog data.

## Pagination and filters
- `COLLECTION_PAGE_SIZE` is 25 for heavy capability surfaces and filter popup paging.
- Page fields reject zero, leading-zero multi-digit forms and values beyond the current page count.
- Typing in a page field does not mutate the active page until commit/IME Done/focus loss.
- Arrow page changes update the field, clear focus and animate the displayed page number without corrupting in-progress keyboard input.
- Search/filter state is saveable across supported configuration changes.
- Heavy lists use lazy containers and bounded result windows.

## Opening sequence
- Opening animation preference is independent of direct-update preference.
- When enabled on a normal fresh launch, native collection starts only after the opening animation completes.
- A bounded watchdog releases startup if animation completion cannot be delivered.
- Disabling the animation allows immediate startup work; it does not change query semantics.
- Devices with unavailable GL/EGL evidence must not hang on the opening screen.

## Material 3 Expressive UI
- All destinations use one coherent dark Material 3 Expressive language; OpenGLESScope keeps its own magenta palette and logos.
- Edge-to-edge layout and system insets are handled without content hiding behind system UI.
- Long technical values wrap or use contained horizontal scrolling instead of clipping.
- Loading, failure, empty, unavailable and completed states are visually and semantically distinct.
- Actionable icons have accurate content descriptions; decorative icons do not create duplicate accessibility announcements.
- Interaction targets remain usable with touch, mouse, keyboard, TV remote, TalkBack, large text and increased display size.

## Android TV and desktop input
- Focusable rows/cards bring themselves into view when focused.
- D-pad traversal must not strand focus in clipped lists or dialogs.
- Long technical evidence remains copyable through supported long-press/keyboard/TV action paths where the UI exposes copying.
- Mouse-wheel/pointer scrolling remains usable on ChromeOS/Googlebook-style desktop Android environments.
- Desktop secondary-input behavior must not create duplicate/context menus where the platform contract disables them.

## Accessibility
- System font substitution, font scale, display scale, RTL/bidi and narrow landscape remain usable.
- Dynamic status announcements use appropriate semantics without repeated noisy announcements.
- Color is never the sole representation of evidence state.
- Large text may stack rows instead of truncating technical labels or action descriptions.

## Performance contract
- Native collection executes once per requested report, never once per recomposition/page.
- Extension membership lookup uses normalized sorted vectors and binary search.
- Invariant EGLConfig extension gates are hoisted outside per-config loops.
- Registry catalog loading is cached process-wide after bounded validation.
- Encyclopedia and filters cap visible work and use lazy containers.
- File Manager scans only the current folder and uses a 4096-entry ceiling.
- Network, hashing, package inspection, filesystem scans, registry decoding and report parsing do not block the main UI thread.
- A performance optimization is invalid if it drops rows, skips required query diagnostics, changes evidence state, reduces registry coverage or makes report output nondeterministic.

## Security contract
- Cleartext HTTP remains disabled.
- Official network origins are fixed/validated.
- Response bodies and local imports are bounded before materialization.
- APK install requires package/signature/version verification.
- HTML output escapes runtime strings.
- Shared-storage paths are canonicalized and root-confined.
- Sensitive-field rejection remains part of app/Database tests.

## Build and toolchain
- Current compile/target SDK baseline: 37.
- Current minSdk baseline: 31, matching the VulkanScope 3.0.12 Android platform floor and allowing the same Android 12+ window/splash behavior without compatibility-only branches.
- NDK remains explicitly locked by Gradle.
- Gradle/Kotlin/AGP/Compose/Material/OkHttp versions must form a build-compatible set verified by an actual Android build environment.
- Native compilation retains warning-as-error/hardening policy established by CMake.
- Experimental Compose/Material APIs require explicit compile-time opt-ins rather than suppression of real failures.

## Android build gate
- Release command is `./gradlew :app:assembleRelease :app:lintRelease :app:testReleaseUnitTest --offline --no-daemon --stacktrace` when dependencies are locally available.
- A Python/static quality gate cannot substitute for this build gate.
- If the Gradle distribution or dependencies are unavailable, record build/lint/unit as NOT EXECUTED.
- Any reached Kotlin/C++/resource/lint/unit failure blocks release until fixed and rerun.

## Static source and data gate
- Every Python tool must parse.
- Checked-in JSON/CSV/XML generated evidence must parse and satisfy schema/census/hash contracts.
- Registry source and generated artifacts must agree.
- Release source ZIP must not contain IDE/VCS/cache/build/credential/keystore/pyc/__pycache__ artifacts.

## Query coverage gate
- Core GLES capability census is derived from `gl.xml`.
- Extension capability candidates are derived from current GLES2 extension ownership and GetPName groups.
- EGL query legality is separately audited against `egl.xml` and API semantics.
- GL and EGL canonical-name audits both run.
- Query additions must be present end-to-end in canonical structured report, UI when applicable, TXT/HTML and Database compatibility when the schema carries them.

## Report integrity gate
- Structured report schema constants, parser, TXT, HTML, Database payload and Worker expectations are mutually consistent.
- Duplicate diagnostic keys are forbidden except for explicitly version-scoped historical compatibility exceptions.
- Complete-report readiness is a validated terminal state, not merely non-null JSON.

## Lifecycle/resource gate
- Probe timeout, cancellation, service destruction, terminal publication and stale-process teardown have source verifiers plus independent state-machine tests.
- Native bounded-resource rules have static checks and regression fixtures.

## UI parity gate
- Four primary destinations, no production navigation rail, no unreachable Page.Info and no fifth primary Display tab.
- Encyclopedia structure, filter-page input behavior, semantic icons, Settings structure and File Manager breadcrumb/view/sort behavior are release-gated.
- API-specific destination counts may differ only where OpenGL ES/EGL semantics require it; common interaction behavior must remain at VulkanScope 3.0.12 quality.

## Negative-mutation gate
- Every new release contract includes mutations that deliberately break version identity, registry/name checks, query coverage, performance contract, navigation/Settings/Encyclopedia/File Manager requirements and verifies the gate fails.
- A negative-mutation fixture that no longer exercises the intended defect is itself a failure.

## Deterministic package and clean-extract gate
- Final source ZIP file order/timestamps/contents follow the deterministic packager contract.
- `files.txt` is regenerated from the final source tree and must match exactly.
- The finished ZIP is extracted to a new clean directory and the aggregate quality gate is rerun there.
- Clean-extract content hashes must match the packaged source tree.

## Immutable predecessor contract
- The exact predecessor release ZIP and SHA-256 are recorded in the 2.1.0 regression contract.
- Every production-source change is allowlisted with predecessor hash/state, successor hash/state and reason.
- Unrelated production drift blocks release.
- Test/rules/generated-artifact updates cannot be used to hide an unreviewed production change.

## Mandatory evidence workflow for every future change
1. Start from the exact immutable predecessor ZIP and record SHA-256/file census.
2. Read this complete rules file before editing production source.
3. Recheck current authoritative Khronos/toolchain sources when a claim can become stale.
4. Audit before patching and classify each finding using the evidence taxonomy.
5. Preserve a regression oracle before fixing a concrete defect whenever practical.
6. Make the smallest architecture-correct fix; never weaken evidence semantics or a gate to obtain PASS.
7. Update generated registry/report artifacts only from locked sources and deterministic generators.
8. Run source/state/negative-mutation/package gates.
9. Run the real Android build/lint/unit gate when the environment can resolve the locked toolchain.
10. Package deterministically, extract cleanly, rerun the aggregate gate and record any NOT EXECUTED evidence separately.

## Release 2.1.0 VulkanScope 3.0.12 rules/UI/query/performance parity audit
- Release identity is OpenGLESScope 2.1.0 / versionCode 2100.
- `PROJECT_RULES.md` is now a single authoritative document; the duplicate second engineering-rules root from 2.0.0 is removed.
- Exact VulkanScope 3.0.12 rules remain byte-for-byte locked as external methodology evidence.
- Production navigation contains exactly four primary destinations: Overview, OpenGL ES, EGL, Extensions.
- The unused legacy landscape navigation rail is removed; bottom navigation is the only primary navigation in both orientations.
- The unreachable duplicate `Page.Info` destination is removed; Info remains a Settings section.
- Heavy filter paging uses `COLLECTION_PAGE_SIZE` (25) and VulkanScope 3.0.12 focus/commit behavior.
- Encyclopedia now separates introduction/evidence boundary, Reference search and How to read entries.
- Semantic icon corrections include `ic_check_updates`, `ic_shield`, `ic_search`, `ic_question` and `ic_opening_animation_toggle` in their exact roles.
- File Manager adds canonical breadcrumb navigation while retaining six view and six sort modes, bounded current-directory scans and atomic import/export behavior.
- GL/EGL extension lists are normalized once; repeated membership gates use binary search.
- EGLConfig-invariant extension booleans are computed once per display rather than once per config row.
- Canonical-name audit expands from GL-only token checks to both `gl.xml` and `egl.xml`, including hard-coded enum/value agreement and fabricated-token rejection.
- Query coverage, report semantics, Database producer compatibility, security, lifecycle/resource, accessibility and deterministic package gates remain mandatory.
- Companion Database is OpenGLESScope Database 1.0.10; it accepts exact producer 2.1.0 / 2100 schema-5 evidence, retains exact 2.0.0 / 2000 as historical schema-5 evidence and keeps unaudited producer identities fail-closed.

## Historical release contracts
- Historical per-release audit documents under `rules/` remain immutable evidence for their released behavior.
- Historical rules are not duplicated inline in this file; the current contract above supersedes them where it is stricter.
- Existing regression contracts for 1.2.1 through 2.0.0 continue to protect released query/report/lifecycle behavior unless an explicit 2.1.0 allowlist documents a change.

## Release 2.1.1 full-report/UI/Analysis evidence parity audit
- Release identity is OpenGLESScope 2.1.1 / versionCode 2101; technicalReport remains schema 5 and submission schema remains 2.
- VulkanScope 3.0.12 remains the byte-locked API-neutral UI/interaction/quality methodology reference; Vulkan-only driver A/B and Vulkan Profiles behavior must never be fabricated for OpenGL ES/EGL.
- Every hard-coded GL_/EGL_ capability/query/token name must resolve against the locked `gl.xml`/`egl.xml` registries or be an explicitly audited state/query identifier covered by the canonical-name verifier.
- UI support state must be derived from exact runtime evidence. Registry ownership, an extension-name substring, absent data, failed query, Unknown or Unavailable evidence must never be promoted to Supported/Unsupported without the corresponding complete enumeration/query evidence.
- Features is generated from the complete OpenGLESScope query-gate catalog plus core-version milestones; it must not remain a hand-selected sample list.
- OpenGL ES, EGL, Limits, Precision, Formats, Configs and Extensions surfaces must expose diagnostic/provenance detail when the canonical report already contains that evidence; hiding retained query errors behind summary counts is a release blocker.
- Analysis contains the applicable VulkanScope-quality tools: Compare, Search, Diagnostics, Requirements, Minimums, Graph, Presentation, Raw JSON, Database, History, Watched, Quality, Share and Tests. Vulkan-only tools are identified as not applicable rather than simulated.
- Analysis Search distinguishes total matches from its bounded rendered window and discloses hidden matches. Raw JSON similarly reports total/matched/rendered counts and never truncates export data silently.
- Diagnostics exposes OpenGL ES runtime state, unavailable GL attributes, isolated probe timing/bounds/publication behavior, EGL current-binding evidence and query diagnostics. Per-query timing is shown only if actually recorded; it must not be invented.
- Requirements evaluates only checked-in scalar OpenGL ES 3.2 implementation minima/maxima that map to collected numeric evidence. Behavioral/conformance rules outside this resolver are stated as out of scope; missing source evidence is UNKNOWN.
- Minimum profiles use bounded local schemas and confirmation for save/load/delete operations; profile evaluation cannot mutate canonical capability evidence.
- Dependency graph explicitly separates runtime query-gate state from locked registry reference metadata; registry edges/owners do not imply runtime support.
- Presentation combines Android display evidence with EGL surface/runtime evidence and explicitly refuses end-to-end colorspace/HDR/presentation guarantees that the evidence cannot establish.
- Database analysis supports bounded recent-report browsing and exact 64-hex report lookup from the fixed HTTPS origin. Comparison is local and reports evidence differences, not GPU ranking/conformance/performance conclusions.
- History timestamps are user-readable while preserving private-storage size evidence; destructive history and watched-evidence actions require explicit confirmation.
- Collection integrity scoring is transparent: every audited check, maximum deduction and evidence is shown. The score is not conformance, certification, performance or device/vendor ranking.
- Self-test status has explicit PASS/FAIL/UNAVAILABLE semantics and never overwrites canonical feature-support state or collection-integrity scoring.
- Precision and EGL Configs have distinct semantic destination icons; unrelated Features/Surface artwork must not be reused for those destinations.
- Native/report correctness, canonical registries, File Manager safety/performance, updater security, lifecycle/resource, accessibility and deterministic packaging remain unchanged release blockers.
- Companion Database is OpenGLESScope Database 1.0.11 and accepts exact 2.1.1 / 2101 schema-5 evidence while preserving exact historical producer contracts and rejecting unaudited future identities.

## Release 2.1.2 current toolchain/spec/query/detail parity audit
- Release identity is OpenGLESScope 2.1.2 / versionCode 2102; submission schema remains 2 and technicalReport schema remains 5.
- Android platform parity is compileSdk 37, targetSdk 37 and minSdk 31, matching VulkanScope 3.0.12. `android.hardware.type.pc`, optional Leanback and optional touchscreen declarations remain aligned while the OpenGL ES requirement stays app-specific.
- Android Gradle Plugin is pinned to current stable 9.4.1 and uses AGP 9 built-in Kotlin; `org.jetbrains.kotlin.android` / `kotlin-android` must not be applied. The Compose Compiler Gradle plugin is pinned to current stable Kotlin 2.4.20. Gradle wrapper remains 9.7.1 to match VulkanScope 3.0.12 and is above AGP 9.4's minimum Gradle 9.6.0. Kotlin 2.4.20 documents full KGP support through Gradle 9.7.0 and permits newer Gradle releases with possible warnings; therefore 9.7.1 must not be described as fully supported by Kotlin and real build/lint/unit execution remains mandatory evidence.
- Android NDK is pinned to current LTS r30 / 30.0.16248370. ABI output remains arm64-v8a, armeabi-v7a, x86_64 and universal; legacy x86 remains excluded.
- AndroidX Core KTX is 1.19.1, Core SplashScreen is 1.2.0, Activity Compose is 1.13.0, Compose UI/Foundation/Animation are 1.12.1, Material 3 Expressive is 1.5.0-alpha28, Lifecycle Runtime Compose/KTX are 2.11.0, OkHttp is 5.5.0 and ZXing Core is 3.5.4.
- Material 3 remains on the current Expressive alpha line because production source uses `MaterialExpressiveTheme`, `MotionScheme.expressive`, `ShortNavigationBar`, `LoadingIndicator` and `LinearWavyProgressIndicator`; silently downgrading to a stable artifact that removes those APIs is forbidden.
- User-visible library/build-tool versions are generated from the pinned Gradle build configuration through BuildConfig constants; duplicate hand-maintained UI version strings are forbidden.
- Android platform splash uses the same API-neutral behavior as VulkanScope 3.0.12: `Theme.SplashScreen`, the app-specific foreground logo, bounded exit animation and `postSplashScreenTheme`. The in-app OpenGLESScope opening animation remains separately user-configurable and data collection still begins only after its startup gate opens.
- Current normative specification baseline is OpenGL ES 3.2, GLSL ES 3.20 revision 8 and EGL 1.5. Khronos still identifies OpenGL ES 3.2 and EGL 1.5 as current; later document publication timestamps do not imply a new API version.
- Locked `gl.xml` and `egl.xml` remain the canonical token/command/extension naming source. Every hard-coded GL_/EGL_ name must resolve to those registries or to an explicitly audited non-registry evidence-state identifier. Fabricated names, guessed aliases and substring-derived support are release blockers.
- Query coverage is independently release-blocked by the registry-derived GLES core census, current extension candidate triage, EGL registry coverage, EGL query-legality audit and GL GetPName disposition audit. A PASS means every in-scope candidate is collected or has an explicit specification-backed exclusion; it does not authorize inventing a runtime value.
- Runtime support remains evidence-only. Registry membership is reference metadata; failure/absence/Unknown/Unavailable are not converted into Unsupported, and extension ownership never becomes Supported without complete runtime enumeration/query evidence.
- OpenGL ES/EGL/Features/Limits/Precision/Formats/Configs/Extensions/Encyclopedia/Analysis screens must show retained query/provenance/error detail when the canonical report has it. Summary counts may accompany evidence but never replace it.
- Build-tool, library, Android API and Khronos baseline details in Settings > Info must be explicit enough to audit the shipped environment and must not rely on stale duplicated constants.
- Companion Database release is OpenGLESScope Database 1.0.12. It accepts exact 2.1.2 / 2102 schema-5 evidence, preserves exact audited historical producer contracts and rejects 2.1.3+ or any unaudited producer identity fail-closed.
- Release requires `verify_2_1_2_quality.py`, immutable predecessor verification against exact 2.1.1 ZIP SHA-256, negative mutation coverage, aggregate quality gate, deterministic package verification and clean-extract rerun.

## Release 2.1.3 NDK r30 native compile-correctness hotfix
- Release identity is OpenGLESScope 2.1.3 / versionCode 2103; submission schema remains 2 and technicalReport schema remains 5.
- OpenGLESScope 2.1.2 is the immutable predecessor. This hotfix changes only release identity and the audited EGLConfig extension-applicability identifier regression in `openglesscope.cpp`; registry/query/report/UI semantics must remain 2.1.2-equivalent.
- EGLConfig extension applicability must use the display-level extension booleans `hasRecordableConfigAttr`, `hasFramebufferTargetConfigAttr` and `hasFloatComponentsConfigAttr`. The stale undeclared identifiers `hasRecordable`, `hasFramebufferTarget` and `hasFloatComponents` are forbidden.
- `extensionApplies[]` is evidence gating only. It must describe whether the corresponding EGL configuration attribute is applicable from runtime extension enumeration; it must not be derived from the queried attribute values themselves.
- NDK r30 `-Wall -Wextra -Werror` compile correctness is release-blocking. Static gates may prove this exact regression absent, but Android `assembleRelease` remains separate execution evidence and must not be claimed unless actually run.
- Locked `gl.xml` / `egl.xml`, canonical-name/query coverage, EGL legality, report semantics, UI detail, Analysis, File Manager, security, lifecycle/resource, accessibility, toolchain and dependency contracts remain unchanged from 2.1.2.
- Companion Database release is OpenGLESScope Database 1.0.13. It accepts exact 2.1.3 / 2103 schema-5 evidence, preserves exact audited historical producer contracts and rejects 2.1.4+ or any unaudited producer identity fail-closed.
- Release requires `verify_2_1_3_compile_hotfix.py`, immutable predecessor verification against exact 2.1.2 ZIP SHA-256, dedicated negative-mutation coverage, aggregate quality gate, deterministic package verification and clean-extract rerun.

## Release 2.1.4 Kotlin/Compose compile-correctness hotfix
- Release identity is OpenGLESScope 2.1.4 / versionCode 2104; submission schema remains 2 and technicalReport schema remains 5.
- OpenGLESScope 2.1.3 is the immutable predecessor. This hotfix may change only release identity and the audited Kotlin/Compose compile-correctness surface in `MainActivity.kt`; GL/EGL registry/query/report semantics, native collector behavior, File Manager, security, Android API and dependency/toolchain pins remain unchanged.
- `Modifier.matchParentSize()` is a BoxScope member extension and must not be imported as `androidx.compose.foundation.layout.matchParentSize`. Usage is permitted only from a valid BoxScope receiver.
- `animateContentSize` must resolve from `androidx.compose.animation.animateContentSize`; `FastOutSlowInEasing` must resolve from `androidx.compose.animation.core.FastOutSlowInEasing`. Missing or guessed import paths are release blockers.
- Internal evidence models must not expose private-in-file types. `FeatureEvidenceRow` and `EvidenceState` must have compatible visibility.
- Composable state APIs (`remember`, `rememberSaveable`, `LaunchedEffect`, etc.) must run only from composable scope. They must never be invoked directly from `LazyListScope` / `LazyGridScope` builders outside an `item`/`items` composable lambda.
- Analysis Graph registry-reference memoization must be computed in `AnalysisPage` composable scope before the lazy-list DSL and passed into lazy items as immutable values. Registry metadata remains reference-only and cannot be promoted into runtime support.
- The 2.1.3 NDK r30 native compile-correctness rules remain mandatory, including the exact EGLConfig applicability booleans and prohibition of stale undeclared native identifiers.
- Release requires a dedicated 2.1.4 Kotlin/Compose compile verifier, immutable predecessor verification against exact 2.1.3 ZIP SHA-256, negative mutation coverage for every reported compile failure class, aggregate quality gate, deterministic packaging and clean-extract rerun.
- Companion Database release is OpenGLESScope Database 1.0.14. It accepts exact 2.1.4 / 2104 schema-5 evidence, preserves audited historical producer contracts and rejects 2.1.5+ or any unaudited producer identity fail-closed.


## Release 2.2.0 VulkanScope 3.0.12 visual/interaction parity contract
- Release identity is OpenGLESScope 2.2.0 / versionCode 2200. Submission schema remains 2 and technicalReport schema remains 5; this release is a UI/interaction parity release and must not silently change collected capability semantics.
- VulkanScope 3.0.12 remains the frozen API-neutral UI/interaction methodology reference. OpenGLESScope may differ only where OpenGL ES/EGL capability semantics require different content, and in OpenGLESScope branding/accent color. Shared chrome, navigation motion, spacing, blur behavior, dialogs, copy actions, File Manager interaction, Database result presentation and accessibility must follow the VulkanScope reference.
- The global app header and Application/About section artwork use the standalone SCOPE wordmark. The full OpenGLESScope wordmark remains allowed only for OpenGLESScope-specific branding surfaces such as launch/report branding; it must not replace SCOPE in shared VulkanScope-equivalent chrome.
- API-neutral common icons remain byte-locked to the frozen VulkanScope 3.0.12 semantic icon set. OpenGL ES/EGL-specific official artwork is rendered as artwork without accent tint. Application, update, precision and EGL-config actions must use their reviewed semantic icons; a generic or unrelated glyph is a release blocker.
- The primary bottom navigation is exactly Overview / OpenGL® ES / Display / Extensions. EGL™ is a nested OpenGL® ES destination, analogous to Display being a nested Surface destination in VulkanScope; EGL™ must not reappear as a primary bottom-navigation item.
- User-facing API names use current Khronos mark presentation: OpenGL® ES, EGL™ and SPIR-V™. Raw registry tokens, extension names, query keys and serialized report field names are never rewritten with trademark glyphs.
- Header, bottom navigation and system-navigation chrome use the same live page-backdrop blur model as the VulkanScope reference: page content is recorded, blurred, sampled behind translucent chrome, and content insets prevent headers/status overlays/navigation from obscuring evidence rows.
- Evidence key/value rows support long-press actions, Android TV OK/Enter long-hold, desktop secondary-button quick actions, explicit copy/share actions and an accent-bordered detail dialog. Copy/share actions never mutate collected evidence.
- Alert dialogs and custom dialogs use OpenGLESScope accent-colored borders matching the shared VulkanScope dialog treatment. Full-screen/shared-storage dialogs remain bounded, focus-safe and navigation-safe.
- Analysis Database list results use the VulkanScope evidence presentation pattern: identity summary, API/version pills, localized submitted date/time/time-zone evidence, exact report ID, explicit compare action, and bounded explicit remote fetch. Successful Database submission shows the exact validated 64-hex report ID in a bordered monospaced field with copy and Open report actions.
- Database result/UI differences must never be hidden through truncation or fabricated fields. Unknown/unavailable evidence stays explicit and raw report identifiers remain exact.
- Release verification must include a dedicated 2.2.0 UI parity verifier, negative mutations for every release-critical parity contract, immutable predecessor verification against the exact 2.1.4 release ZIP, aggregate quality gate, deterministic packaging and clean-extract rerun.
- The 2.2.0 UI parity audit is recorded in `rules/2.2.0_VULKANSCOPE_3.0.12_VISUAL_INTERACTION_PARITY_AUDIT.md`; all 103 common drawable resources in the locked census must remain byte-identical to VulkanScope 3.0.12.
- Companion Database release is OpenGLESScope Database 1.0.15. It accepts exact 2.2.0 / 2200 schema-5 evidence, preserves audited historical producer contracts and rejects 2.2.1+ or any unaudited producer identity fail-closed.


## Release 2.2.1 Kotlin/Compose parity compile-correctness hotfix
- Release identity is OpenGLESScope 2.2.1 / versionCode 2201; submission schema remains 2 and technicalReport schema remains 5.
- OpenGLESScope 2.2.0 is the immutable predecessor. This hotfix may change only release identity and the audited Kotlin/Compose compile-correctness surface in `MainActivity.kt`; GL/EGL registry/query/report semantics, native collector behavior, UI parity intent, Android API and dependency/toolchain pins remain unchanged.
- `drawLayer` must resolve from `androidx.compose.ui.graphics.layer.drawLayer`; the obsolete/incorrect `androidx.compose.ui.graphics.drawscope.drawLayer` import is forbidden.
- `animateDpAsState` must resolve from `androidx.compose.animation.core.animateDpAsState`.
- `FlowRow` must resolve from `androidx.compose.foundation.layout.FlowRow`.
- Scroll-hint parity must include `LazyListState`, `LazyGridState` and plain `ScrollState`; a `ScrollState` must never be passed to a lazy-only overload.
- Shared Analysis helpers `ExpressiveToggleRow` and `ExpressiveMetric` must remain real composables, use OpenGLESScope theme semantics, and must not be replaced by unresolved placeholders.
- Kotlin compile regressions reported by real `assembleRelease` evidence are release-blocking. Static verification may prove the exact reported failure classes absent, but a real Android build must not be claimed PASS unless it actually runs.
- The 2.2.0 VulkanScope visual/interaction parity contract remains mandatory, including SCOPE artwork, nested EGL™, blur, evidence actions, Database presentation and complete Analysis branches.
- Companion Database release is OpenGLESScope Database 1.0.16. It accepts exact 2.2.1 / 2201 schema-5 evidence, preserves audited historical producer contracts and rejects 2.2.2+ or any unaudited producer identity fail-closed.
- Release requires `verify_2_2_1_kotlin_compile_hotfix.py`, immutable predecessor verification against exact 2.2.0 ZIP SHA-256, dedicated negative-mutation coverage, aggregate quality gate, deterministic packaging and clean-extract rerun.

## Release 2.2.2 VulkanScope video-detail parity audit
- Release identity is OpenGLESScope 2.2.2 / versionCode 2202, with schema 2 submission and schema 5 technicalReport unchanged. The immutable predecessor is the exact OpenGLESScope 2.2.1 ZIP; its hash and complete production inventory are recorded in `tests/golden/2_2_2_video_ui_parity_regression_contract.json`.
- Only `app/build.gradle.kts` (release identity) and `app/src/main/java/com/efishell/openglesscope/MainActivity.kt` (reviewed UI/detail/evidence presentation) may change among production sources. Everything else retains the SHA-256 bytes from the immutable predecessor.
- The October 1 VulkanScope reference video demonstrates a bounded, accent-bordered, scrollable detail sheet with fixed header and Close and explicit Copy, Share, Watch and Encyclopedia controls. Common UI routes must provide equivalent interaction and long-token display using collected GL/EGL evidence. Vulkan-only device/profiles/driver-slots are Not applicable, and no registry token can be presented as proof of an unrelated runtime query.
- Exact run-time `GlReport` evidence is the only source for renderer/vendor/GL/GLSL/EGL implementation details. Extension detail distinguishes enumeration from separate query-gate availability, failures, or unknown. Copy/Share/Watch are explicit user actions and may not mutate canonical evidence.
- The new detail shell must remain within screen bounds in portrait and landscape, expose scrolling hints, respect Android TV D-pad/desktop pointer, have a clear Close action, and stack long tokens rather than truncate canonical query names.
- The 103 common VulkanScope drawables, SCOPE branding, navigation destinations and screen architecture, all GL/EGL native sources, manifests, registry locks, schema constants, toolchain pins and privacy/security/probe lifecycle conditions must remain unchanged from 2.2.1.
- Database 1.0.16 strictly accepts the audited 2.2.1/2201 producer. This 2.2.2 app remains a source release requiring a separate Database admission update before new-version remote submission. Do not fabricate the older producer identity, change its versionCode or weaken the companion Worker fail-closed validation.
- Release requires `verify_2_2_2_video_ui_parity.py`, `verify_2_2_2_regression_contract.py`, dedicated negative-mutation tests, aggregate quality gate, deterministic ZIP proof and clean-extract recheck. Real Android compile/lint/unit gates remain required and must be marked NOT EXECUTED, never PASS, if dependencies cannot resolve.

## Release 2.2.3 full branding/copy/HDR/extension interaction parity audit
- Release identity is OpenGLESScope 2.2.3 / versionCode 2203; predecessor is the exact immutable OpenGLESScope 2.2.2 ZIP, SHA-256 `be81f683a410d8436d1d0479142b021f70cff5547e5c45a035569f1b6e4699c0`. All 158 production source paths are frozen in `tests/golden/2_2_3_full_ui_regression_contract.json`.
- Production-source allowlist: `app/build.gradle.kts` for identity and `app/src/main/java/com/efishell/openglesscope/MainActivity.kt` for UI corrections. No changes to native collector, Khronos registries, GL/EGL query legality, data schema, manifests, assets, file manager, submission privacy or network transport. All other production hashes must remain byte-identical to 2.2.2.
- Main header uses the actual full horizontal OpenGLESScope product logo, not the historical SCOPE-only crop. Historical `openglesscope_scope_wordmark.png` is retained without modification for its documented About and version-block uses. Exact VulkanScope common icon assets and their locked SHA-256 hashes are preserved.
- Clipboard action must place exact key/value or token bytes on Android's native clipboard and must not show an additional duplicate `Copied` toast; Android owns its platform clipboard confirmation. Successful explicit action feedback inside the evidence sheet remains a separate interaction state, not an OS Toast.
- Fixed user-facing strings must contain no unrelated Vulkan/Turnip/VulkanScope claims, driver-slot explanations, or product comparison. Actual GL/EGL runtime strings are always preserved verbatim even when an active third-party implementation identifies itself with an unexpected term. Historical rules/reference filenames are immutable methodology artifacts, not UI resources.
- HDR cards follow the common reference: accurate `None reported`/`Unknown / not exposed`/`Unavailable` states, original Android `HdrCapabilities`/current-display-mode provenance and luminance, official logos where corresponding assets exist, textual HLG/HLG+, scrollable bounded horizontal carousel with accessible left/right buttons. A logo never manufactures HDR support.
- Long canonical GL/EGL keys and values switch to stacked full-width evidence rows, avoiding truncated or badly wrapped tokens in standard portrait and landscape widths.
- An extension token row uses the same shared `CapabilityKeyValue` press/TV/secondary-pointer/accessibility action implementation as other evidence rows. Its Details route keeps exact runtime enumeration scope separate from registry metadata and independently observed query gates; all related detail rows also expose shared evidence actions.
- Small EGL destinations/section/quick-access marks use a unified branded typographic glyph instead of a hard-red official bitmap shrunk or incorrectly tinted inside an accent-colored container. Existing official raster remains immutable; opening-animation icon uses the common tinted semantic icon treatment.
- Release gates: `verify_2_2_3_full_ui.py`, `verify_2_2_3_regression_contract.py`, `test_2_2_3_negative_mutations.py`, aggregate static/data/source/security/lifecycle/report/packaging checks, deterministic source ZIP and clean extraction. Reached Android build/lint/unit failures block production release; unavailable locked Android toolchain is `NOT EXECUTED`, never implied PASS.
- Companion Database 1.0.16 has not been updated to admit producer 2.2.3/2203. Do not spoof 2.2.1 or weaken fail-closed remote validation. A separate Database release must authorize 2.2.3 before production remote submission.

## Release 2.2.4 paging/Analysis/status/report source parity audit
- Release identity is OpenGLESScope 2.2.4 / versionCode 2204; immutable predecessor is OpenGLESScope 2.2.3 ZIP, SHA-256 `b238e6ac4db108f404afc3291604c8b51833d16e0c450440f9cf316645edd9b9`. All 158 production source hashes are checked by `tests/golden/2_2_4_paging_analysis_report_regression_contract.json`; only `app/build.gradle.kts` and `MainActivity.kt` may change.
- VulkanScope 3.0.12 is an API-neutral **behavioral code reference** for measured sticky pagination (single live chip; animated header join/detach; focus-safe page number commit; top overlay, pager, scroll-arrow and bottom-navigation z-order), transient popup geometry, status animations and report-list UX. Native OpenGL ES/EGL inspection stays authoritative and Vulkan-only data is forbidden in product text and reports.
- Exactly four bottom-navigation destinations in portrait and landscape, in order Overview, OpenGL ES, EGL, Extensions. Display is an Overview child, never an independent bottom tab. EGL retains both its own screen and optional inner OpenGL ES navigator route for backward navigation compatibility.
- All collections use 25 rows per in-app page. An actual measured list item supplies the pager's only visible control. The floating pager must never be duplicated, overlap the top bar or cover validated transient overlays. Page arrow motion, scroll arrow visibility, hardware/D-pad, input focus, invalid-number commit and accessibility mirror the reference implementation; invalid pages cannot be selected.
- Analysis Raw JSON generation runs on a bounded background dispatcher, explicitly indicates loading and parsing failure and does not mutate submission data. A Database comparison clears previous baseline before a new exact-ID request, avoids an outdated response changing a different selected ID and restores action-state flags on both success and failure.
- Database report list uses the published `nextCursor` from Database 1.0.16. Requests are bounded to 50 public summaries, with a maximum of 200 retained in-app rows, de-duplication by exact 64-hex ID, safe ISO8601/id validation and explicit Load more; list-page control operates only on loaded entries. Invalid/empty cursors do not fabricate further results. No implicit uploads.
- Connectivity, collection and update banner cards use a common 520-dp maximum-width, 22-dp frosted floating status surface, shared transition timings, 13x8 internal padding, 30-dp state artwork, correct z-order below any merged pager, and polite accessibility status announcements.
- Database submission exposes distinct idle/uploading/accepted/failed accessible animated states. Accepted ID and Open report are shown only after a real validated successful server reply; any failure retains an explicitly accessible bounded error log. Source 2.2.4 is NOT yet authorized by Database 1.0.16's producer allowlist; client must never spoof a lower version or show success for HTTP 400.
- No native collection, registry, schema, storage security, manifest, permissions, Android build-tool locks, existing logo assets or release artifact hashes may be modified. Static/source negative-mutation and clean-extract quality gates are required; real Android build/lint/unit tests are NOT EXECUTED if the locked distribution/SDK cannot resolve.

## Release 2.2.5 input, File Manager and responsive accessibility parity audit
- Release OpenGLESScope 2.2.5 / versionCode 2205 is based on byte-locked OpenGLESScope 2.2.4 ZIP SHA-256 `c10772b9f32ce7ec73341f6a4e6d60aed6a1a27797980896d5eef0adc88ccee8`; production changes are limited to app/build.gradle.kts and MainActivity.kt with 158 predecessor hashes fully checked by the dedicated 2.2.5 regression contract.
- VulkanScope 3.0.12 serves as an API-neutral source implementation reference: graphics-layer 24-dp true blur/backdrop, wheel plus mouse-primary drag (slop-aware), keyboard/TV/card focus and bring-into-view. Retain OpenGLESScope's palette and GL/EGL evidence truth; do not include Vulkan-only runtime surfaces.
- Desktop secondary click is consumed in the File Manager on ChromeOS, Android PC and freeform-window Android; on supported touch/pointer platforms, evidence actions retain their bounded, existing long-press semantics.
- Wide landscape File Manager (at least 700 dp width and wider than height) separates a scrollable controls column and an independently scrollable list/grid. Narrow, large-font, portrait and split screen use a bounded single-column path. Insets protect status, navigation and desktop window edges.
- File Manager options use 2 columns below 380 dp or fontScale >= 1.3, otherwise 3; menu width is never forced wider than the parent. View/sort background transitions are animated, and decorative mode icons do not duplicate spoken labels.
- All actionable browse rows focus and bring into view for keyboard/TV/desktop; disabled and current breadcrumb targets are not focusable. RTL reverses breadcrumb glyph, folder arrow and logical search feathering, not raw filenames/paths. File Manager statuses use polite live regions; system-font-scale navigation reserves extra label height.
- Collection, schema-5 technical reports, submission schema-2, registry, native API queries, manifests, security, asset hashes and remote Database admission policy remain unchanged. Actual Android build/lint/unit and device-level visual/semantics checks are mandatory when a compatible environment is available and must be marked NOT EXECUTED rather than PASS otherwise.
- Mandatory release gates: predecessor source-hash chain, 2.2.5 UI/source checks, independent negative mutation, original shared quality/security/data suite, deterministic packaging and clean-extract recheck.

## Release 2.2.6 real Kotlin compile correction and UI parity re-audit
- Release OpenGLESScope 2.2.6 / versionCode 2206 is based on the byte-locked exact 2.2.5 source ZIP, SHA-256 `6896534078df8074f98d05894d8b8cc0bb23e1d02e7fb745d01665a7b3041321`. The 158 production paths and allowlist of only `app/build.gradle.kts` and `MainActivity.kt` are captured in `tests/golden/2_2_6_kotlin_compile_parity_regression_contract.json`.
- Four actual Windows `:app:compileReleaseKotlin` diagnostics are release-blocking: unresolved Compose `rememberUpdatedState`, nonexistent `R.drawable.ic_chevron_down`, non-composable `remember` inside the `LazyListScope` Analysis builder, and missing explicit `ExperimentalMaterial3ExpressiveApi` opt-in for the Database submission `LoadingIndicator`.
- Resolve the import from `androidx.compose.runtime`, use an existing reviewed semantic expand icon, hoist Analysis comparison state into its nearest actual composable with all three source keys and retain bounded rendered results, and annotate only the specific experimental composable instead of suppressing compiler diagnostics.
- Changing a typed report ID must immediately clear an unrelated previous comparison. A failed/old exact-ID or selected-Database-row asynchronous read must not erase a newer ID's Analysis baseline or status. Validate identity in the error branch just as in the success branch; terminal progress indicators may still exit in `finally`.
- An independent current-version gate must check all referenced drawable names against packaged Android resources; the shared sticky pager must equal the frozen VulkanScope 3.0.12 normalized implementation, and all 103 API-neutral common drawables must preserve the approved reference hashes. Different product-specific TV banners are intentional branding, not a reference parity failure.
- Preserve header, blur, File Manager, Display/HDR, collection paging, report statuses, GL/EGL runtime semantics, Khronos registry, all native sources, security/privacy, schema-5 technicalReport, schema-2 submission, toolchain/SDK and Database 1.0.16 fail-closed producer admission. Never spoof 2.2.1 to bypass Database version policy.
- The next source archive requires the predecessor-hash contract, dedicated compiler/error regression gate, deliberately failing negative mutations, complete existing static quality/data/security suite, deterministic package, clean extraction and rerun. Static success is not proof of Android compilation; actual Gradle build/lint/unit must be labeled NOT EXECUTED when the locked Gradle distribution or Android SDK cannot resolve.

## Release 2.2.7 official artwork, reference evidence menu, overlay and full-screen File Manager audit
- Version OpenGLESScope 2.2.7 / 2207 derives from immutable 2.2.6 ZIP SHA-256 `a1e2f69e864766cc5c47dac21819fc40051fc5f5552ac6c85cc4beab2866376d`; lock all predecessor production paths and allow changes only to MainActivity.kt, app/build.gradle.kts and a new theme-colored `ic_android_brand.xml` resource. Original shared 103 drawable hashes and original official EGL PNG are immutable.
- Restore official EGL raster silhouette rendered through Compose `ColorFilter.tint(BrandSoft)` (not substitute typography and not a literal red logo). Android robot body must use the exact `BrandSoft` palette hex `#F06BC7`, retaining the separately rendered dark eye detail. Both marks must be visible in Overview/section cards using correct semantic artwork routing.
- Extensions detail is a full-width `ExpressiveDetailDialog` showing source, runtime enumeration separately from query legality, relevant diagnostics, and the actual consistent four evidence actions: Copy name + value, Share evidence, Add to watched evidence, Open in Encyclopedia. A Khronos specification button may be additional. Extension-row long press still exposes standard accessibility/pointer/TV evidence actions.
- Watched evidence actions must never sit inside an unconstrained horizontal scroll container when action buttons use fillMaxWidth. Use a bounded fillMaxWidth vertical action group with meaningful keyboard/TV semantics.
- Reference shared-storage File Manager as of VulkanScope 3.0.12 must use a separate platform full-screen dialog above primary navigation, 180/220-ms entrance and 160/210-ms exit, animated back/header/search, 40/60 independently scrollable wide-landscape controls/content layout, portrait/split-window single-column layout, true list/grid/dense/large-tile mode spacing (92/156/228 dp adaptive cells), bounded directory scan and canonical-path validation, atomic export/import, genuine sort/filter, and deferred overwrite confirmation. TV back handling must belong to the dialog window; status must be a polite accessibility live region. Soft scroll boundary fade and navigation animations remain real, not mock overlays.
- Collection up/down overlays must reserve BOTH the bottom-navigation content inset and Android system navigation inset plus separation, including landscape and enlarged font conditions. Never allow bottom arrow to appear within/below primary navigation.
- No GL/EGL native query, Khronos registry file, report schema, producer identity spoofing, permissions, toolchain lock or existing image may change. Database 1.0.16 has not authorized 2.2.7 producer submissions; server 4xx must be a real failure UI state.
- Current 2.2.7 mandatory gates: full hash chain, existing historical API-neutral tests updated only where legitimately superseded, dedicated source and resource checks plus deliberate negative mutations, reproducible ZIP and clean-extract gate. Actual Gradle assemble/lint/unit and instrumented portrait/landscape/TV tests are NOT EXECUTED without Android toolchain/emulator; static PASS must not be called compiled/running PASS.

## Release 2.2.8 full-logo, continuous blur, SDK 37.2 and Encyclopedia search parity audit
- OpenGLESScope 2.2.8 / versionCode 2208 derives only from the exact immutable 2.2.7 source ZIP, SHA-256 `02d6c1ac92bd0427d1d7ef578b0f1dc680f927fbbf68bebd2b2bf0c50effd1dc`. Production-source allowlist is solely `app/build.gradle.kts` and `app/src/main/java/com/efishell/openglesscope/MainActivity.kt`; every other production asset/native file retains its predecessor hash.
- Application Info version badge uses the complete official OpenGLESScope foreground artwork within the same 46-dp brand container and position as the reference VulkanScope 3.0.12 version component. The small independent historical SCOPE wordmark remains permitted in compact section-specific artwork, not the central version badge.
- A continuously recorded graphics-layer fallback, with a real 24-dp shader blur, must be available during loading, collection and AnimatedContent transitions. The outgoing page may clear its backdrop registration only when its own layer is still the active registration. Never draw header/navigation back into the fallback source or substitute an empty never-rendered layer for it.
- `compileSdk` uses AGP's stable minor-level release DSL: `version = release(37) { minorApiLevel = 2 }`. Because Android `targetSdkVersion` targets an integer API level (rather than an SDK minor), `targetSdk = 37` remains correct. Report compile SDK as `37.2`; do not invent `targetSdk = 37.2` or misrepresent a minor-version manifest target. Preserve AGP 9.4.1, Kotlin 2.4.20, NDK r30, SDK min31, manifest, ABIs and pinned dependencies.
- Encyclopedia follows VulkanScope responsive metric-grid structure while truthfully labeling GL and EGL data. Seed/query/category/API/runtime filter edits synchronously reset page; clamping guarantees stable, bounded paging even after filtering reduces results. Khronos catalog loading is off-main and cancellation is rethrown instead of rendered as an error; bounded search and no remote queries preserved.
- Production fixed strings/resources must not contain Vulkan/Turnip/VulkanScope product language; immutable reference rule evidence and historical regressions must never be rewritten to erase provenance. Actual runtime GL/EGL renderer/vendor names are never falsified or filtered.
- Dedicated current release source/inventory verifier, negative mutations, full historical static/data/security/report gates, deterministic packaging, clean extraction, and honest actual Android compile/lint/unit evidence are mandatory. Source PASS alone does not assert APK/build/device PASS. The companion Database 1.0.16 does not admit unaudited 2.2.8 producer; fail closed without version spoofing.


## Release 2.2.9 File Manager, Khronos, viewport and brand alignment parity
- Release identity is OpenGLESScope 2.2.9 / 2209, compiled against locked SDK 37.2 while targeting API 37. Schema 5 technical reports and submission schema 2 are unchanged.
- Immutable predecessor is the exact OpenGLESScope 2.2.8 ZIP SHA-256 1138fa2b2b71db24c567de884331d9d1898c4eed861bc90032bcbe9d1976e1dd. Only MainActivity.kt, app/build.gradle.kts, and newly derived aligned logo PNG may differ in the production source tree. Golden source SHA-256 chaining is release-blocking.
- The full-screen shared-storage File Manager has the exact reference VulkanScope black (0xFF000000) outer background, including status/navigation inset area rather than OpenGLESScope's tinted main-screen SurfaceDark. Content-card colors retain OpenGLESScope product palette. Keep search, sort, canonical path bounds, atomic writes, back handling and keyboard/TV input unchanged.
- The Open Khronos specification action in extension evidence uses the reference contained text-and-external-icon button, spans available width and is disabled without validated network; URL is an authoritative registry-specification URL, never fabricated for unknown extensions.
- Derive real bottom navigation height from `onSizeChanged` and provide it to all scrolling pages through LocalBottomNavigationContentInset. Last cards must remain reachable at the actual bottom independent of Android navigation-bars inset, display orientation or font scale; scroll arrows sit above the measured navigation rather than double-counting system insets.
- Render official EGL artwork white *in the bottom navigation only*, in the same centered icon slot as OpenGL ES; other EGL surfaces may use the brand tint according to semantic context. Never replace the official EGL glyph with a generated text symbol.
- Header uses genuine OpenGLESScope horizontal logo alpha-framed to VulkanScope 3.0.12 aligned artwork geometry (546x84 PNG), rendered 126x30 dp on normal widths, reduced only for truly narrow layouts/very large text. Original artwork remains available as immutable source.
- New UI regression and negative-mutation gates must detect root background, external button shape/network state, measured inset, EGL alignment/tint, header image size, and accidental native probe changes. Repeat aggregate gate on a clean extraction; real Android compile/lint/unit/on-device remain separately classified if unavailable.


## Release 2.2.10 license, Extensions evidence, File Manager chooser, Database and GPU detail correction
- Release identity OpenGLESScope 2.2.10 / 2210; compileSdk 37.2, targetSdk 37, schema 5 technical report and submission schema 2 are unchanged. Exact immutable predecessor OpenGLESScope 2.2.9 ZIP SHA-256: b575307b6acac5a14672fd01fa73246a37430368287ec4af8495df3c427f0098. The only allowlisted production changes are MainActivity.kt and app/build.gradle.kts. Every other source SHA-256 must equal its predecessor; changes to the registry, native GL/EGL probes and transport are forbidden.
- Each Libraries license agreement is a single full-width accessible action with a visible 22dp right chevron, a bounded label and existing packaged license document; do not rely on an off-screen nested trailing button.
- The Extensions Details window initially presents Copy name + value, Share evidence, Add to watched evidence and Open in Encyclopedia before any unbounded query-gate diagnostics. The network-validated Open Khronos specification action remains a full-width contained control with the authoritative extension registry URL and no synthetic link for unknown entries. Long GL/EGL metadata scrolls below the actions.
- File Manager view/sort chooser defaults to three reference-sized columns at ordinary portrait widths, only reducing to two for extremely narrow/large-font windows. Surface uses the VulkanScope neutral #181516 backdrop and product accent; consistent dim scrim covers full window, navigation bar is black with contrast enforcement disabled while the chooser is displayed. Preserve navigation/sort choices and bounded scanning/export semantics.
- Database compatibility notice explicitly explains that the server rejects old or unaudited producer identities and never presents a rejected upload as success. No producer spoofing; Database 1.0.16 acceptance of 2.2.10 must be separately audited/deployed.
- GPU Details shows direct GL_RENDERER, GL_VENDOR, GL_VERSION, GLSL, GL context flags/reset/robust access query provenance, runtime EGL identity/context/config/surface evidence, unknown/unavailable attributes and bounded category/diagnostic totals. It must not invent standardized driver versions or infer capability support from branding.
- Current-source/UI/hash-chain/negative-mutation/clean-extract gates block release. Static PASS does not assert real Android compile, TalkBack, TV or device visual PASS.

## Release 2.2.11 license scroll arrows, shared storage feedback and submit affordance correction
- Release identity is OpenGLESScope 2.2.11 / 2211. Exact immutable predecessor OpenGLESScope 2.2.10 ZIP SHA-256 `f59cbabbac4cd753c019be54af8be154da827eec25b460b6469c018785d8b2f6`; only `app/src/main/java/com/efishell/openglesscope/MainActivity.kt` and `app/build.gradle.kts` may change. All native GL/EGL probes, registry, report schema, transport, manifest and drawable resources are immutable against this predecessor.
- Supersedes only the 2.2.10 **license link presentation** change: restore the explicit 2.2.9 License `ChevronAffordance`, as requested. The actual missing *page scroll* arrows were caused by `ReleaseNotesContent` not providing `ExpressiveScrollHints`. Both license agreement and update release notes use the same lazy list/keyboard-D-pad navigation and visible-on-scroll boundary indicators as the VulkanScope 3.0.12 reference. Do not confuse link chevrons with in-content scroll indicators.
- Manifest permission census remains the same as VulkanScope 3.0.12: INTERNET, ACCESS_NETWORK_STATE, REQUEST_INSTALL_PACKAGES, MANAGE_EXTERNAL_STORAGE. The additionally required OpenGL ES 2.0 uses-feature is an API-specific graphics capability, **not a permission**. Do not broaden storage access or request it automatically; retain explicit user initiation, scoped installation permission on demand, and the existing 3-second animated permission-denied action feedback.
- Shared storage import/export failure status now has an animated, spoken warning with explicit Import failed / Export failed attribution and a non-dismissible-by-error retry path; exceptions do not turn into silent success. Keep bound checks, safe canonical paths and atomic export unchanged.
- Database submit's trailing upload arrow retains sufficient theme-accent contrast while disabled, *without enabling submission*. Retain readiness, user initiation, validated internet, lifecycle and server producer-identity admission gates; success is only shown after actual server acknowledgement. Database 1.0.16 still needs separate 2.2.11 producer compatibility deployment.
- Dedicated regression verifier and negative mutation gate are release-blocking; all other earlier contract expectations remain binding except the explicitly superseded 2.2.10 inline-license-row presentation.

## Release 2.2.12 reference Extension Details, Database submission, trademark and cold-start timing correction
- The exact predecessor is OpenGLESScope 2.2.11; immutable ZIP SHA-256 and all production hashes are locked in `tests/golden/2_2_12_release_regression_contract.json`. Only `MainActivity.kt` and app release identity may change. Earlier historical reference evidence remains immutable.
- Explicit Extension Details uses VulkanScope 3.0.12's evidence-first arrangement: no duplicated copy/share/watch/Encyclopedia action cards (the row's existing long-press evidence menu remains intact), a single content-sized contained Khronos specification action after the evidence entries, followed by an API-truthful interpretation boundary. Never fabricate GL/EGL-specific spec metadata to match Vulkan structures.
- Database submission mirrors the supplied VulkanScope interaction video and reference: the single TransientActionButton owns three-second pending/success/failure feedback; no redundant second DatabaseSubmissionStateCard. Preserve strict complete-report and validated-internet gates, disabled icon legibility, server-confirmed success only, separate rejection details, result status label, and animated report-ID copy button. Reports remain schema 5 and submissions schema 2; producer admission needs a separate Database deployment.
- OpenGL® ES™ is the user-visible trademark form. Canonical GL/EGL driver strings, registry tokens, on-wire schema identifiers and logged raw evidence are not altered merely for presentation. Idempotent display normalization must not double-suffix ™.
- Cold-launch Activity-to-opening-gate delay is captured with monotonic `System.nanoTime()`, published only once after actual opening sequence completes or watchdog releases it, and exposed under Analysis → Diagnostics as seconds with exact milliseconds. It is kept distinct from the dedicated GLES probe collection interval. Configuration-change recreation must not be misrepresented as cold-launch timing. No fabricated per-query instrumentation.
- New negative-mutation tests must fail on extra details actions, stretched Khronos button, duplicate report-animation card, incorrect report-ID transition, absent ™, removed startup timing, disabled submission gating, and native/manifest drift. Re-run aggregate quality, clean extraction and deterministic packaging. Real Gradle assemble/lint/unit and device behavior must be recorded separately from static checks.

## Release 2.2.13 VulkanScope-equivalent Database failure log dialog audit
- Exact immutable predecessor: OpenGLESScope 2.2.12 ZIP SHA-256 `243846045b784b4544afc19d7c12f5df7bcef3fe110df803f62878d060de075d`. `tests/golden/2_2_13_release_regression_contract.json` locks all 160 production paths. Only MainActivity.kt and app release identity may change. No report/schema/native/manifest/permission/registry drift.
- The separate bounded failure dialog was already present in 2.2.12 and MUST remain visible on a genuine failed submission (server rejection, malformed ID, serialization/network/unexpected error), never on success or automatically without a user-initiated submission.
- Match VulkanScope 3.0.12 DatabaseSubmissionFailureDialog structure, size and actions with product-specific accent: title, bounded scrollable monospaced log (360 dp vertical cap), 96 Ki-character maximum, Copy all and Close; add reference desktop pointer-wheel and TV D-pad scroll modifiers in the exact sequence.
- HTTP non-success must show the HTTP code and bounded server message in the submit status, and preserve phase=http-response, httpStatus, detail and capped responseBody in the separate diagnostic log. Unknown failure cannot be rendered as upload success. Keep cancellation propagation and server-acknowledgement gate.
- Dedicated positive/negative source tests, exact predecessor hash chain, source/data/privacy gates, deterministic packaging and clean-extract verification are release-blocking. Android build/lint/unit and device observation are separate evidence classes; never claim PASS when unavailable.

## Release 2.2.14 VulkanScope update-transfer and Database report-list parity
- Exact predecessor: immutable OpenGLESScope 2.2.13 ZIP and its 160-path production census, with the new allowlist restricted to MainActivity.kt and app versionName/versionCode. SDK 37.2, target 37, permissions, native GL/EGL, reporting schema and locked registries remain unchanged.
- The Direct Updates consent, confirmation, ABI/release provenance, bounded transfer, pause/resume/cancel and verified-install flow remain enabled only through explicit user action. Replace the indeterminate transfer line with the VulkanScope 3.0.12 14 dp, 180 ms eased, 0–100% bounded determinate track and continuously visible numeric percentage; use authoritative progress totals (transfer length or signed release asset metadata). Do not pretend a missing total proves finished bytes. Verify completed APK before installer handoff. Preserve per-second speed, received/total bytes, state and scrollable bounded live log with desktop wheel/TV D-pad controls.
- Public Database list keeps 50-summary cursor pagination, 200-unique-summary memory bound, explicit request, validated network and exact-ID comparison. Display equal-weight Refresh/Load more controls and a 50 dp GPU vendor badge using the existing OpenGLESScope runtime vendor/renderer logo resource mapping. It is purely illustrative; GL/EGL runtime evidence is never inferred from artwork. Unknown/translation-layer renderer metadata stays the unknown artwork.
- 2.2.14-specific positive and negative mutation tests are release-blocking. Preserve all past immutable contract files and do not weaken report/privacy/network verification for UI parity. Distinguish static checks from Android compile/lint/unit/device testing.

## Release 2.2.15 complete evidence-integrity / current-registry audit
- Immutable predecessor is exact OpenGLESScope 2.2.14 ZIP SHA-256 `519bc7e06c8c0d78c2b8b3ab3a2d232aad61f31f5528c09648019db9bbd405c6`. Production allowlist: `app/src/main/cpp/openglesscope.cpp` and `app/build.gradle.kts`; all 158 other production files remain identical by SHA-256.
- GL/EGL renderer/vendor/version strings are validated as bounded UTF-8 and serialized as complete Unicode scalar values in JSON, including surrogate pairs for non-BMP characters. Encoding individual UTF-8 bytes as unrelated `\u00XX` code points is forbidden; invalid source bytes cannot become invented metadata.
- Explicitly distinguish an empty successful extension enumeration from failed, duplicated, oversized or incomplete GL / EGL display / EGL client enumeration. A mandatory GL extension or binary/compressed format query failure blocks an Available terminal report rather than silently emitting an apparently valid empty capability list.
- EGL_NO_DISPLAY/EGL_EXTENSIONS query failure on pre-1.5 EGL is Not applicable unless EGL_EXT_client_extensions makes it available; a valid empty returned string remains Available. All client/device/display extension membership is strictly scope-specific.
- Optional EGL device extension lists and self-test extension enumeration must propagate incomplete/invalid reads to Unavailable rather than invented zero-extension evidence. The self-test must use the current glExtensions completeness signature.
- GL float limits serialize finite full binary32 round-trip precision (`max_digits10`); no NaN/Infinity is misreported as a usable numeric capability. Raw driver-provided unknown numeric enumerants remain numerically visible instead of fabricated canonical names.
- `registry/gl.xml` and `registry/egl.xml` remain byte-locked; only their verified exact SHA-256 values are claimed. Upstream live download and Android driver/device tests are separate required evidence, not inferred from an offline registry mirror or static PASS.
- New positive, host-compiled C++ property tests and negative mutation regressions are release-blocking; report schema 5 and Database submission schema 2 are unchanged. Server acceptance of producer identity 2.2.15 requires separate Database deployment.


## Release 2.2.16 validated-network and bottom navigation parity
- The exact predecessor is immutable OpenGLESScope 2.2.15 ZIP SHA-256 `1c06fe63a7f6a8f3cf1e01744adb03f9ef442e359cf3a01cd22f3b306c947fa2`; the release contract locks all 160 production paths. Only `MainActivity.kt` and app Gradle version identity may change. Registry, native collector, manifests, permissions, technicalReport schema 5, submission schema 2 and security bounds remain unmodified.
- The offline status overlay follows VulkanScope 3.0.12's verified state semantics and order: collection, connectivity, update. An offline info badge remains visually and semantically distinguishable from the short transition notification. While collecting offline, network and incomplete-report locks are explicitly explained independently; inspection still works without network. Online recovery must not falsely unlock incomplete reports.
- Database area shows the reference blue explanatory line when validated connectivity is missing and two distinct lines when both network and complete-report evidence are missing. Distinctness must be preserved in the UI and accessibility tree, alongside the existing compatibility/admission notice and separately logged server failures.
- **Disabled means disabled**: Submit complete report and public Database link must both be non-activatable without validated internet; Submit additionally requires a terminal complete report and no active upload. The upload icon/background/label use the same muted disabled treatment as the public Database link. Re-check the gates on activation; do not display an active-colored trailing icon in any unavailable state. With all requirements satisfied, submission remains explicit and user-initiated; never silently disable all real reporting or bypass acknowledgement.
- All four bottom destinations keep equal hit widths and a shared centered artwork slot, including aspect-ratio-correct official GLES and EGL artwork, with white EGL branding and brand-neutral unselected labels. Account for OpenGL® ES™ label length by bounded responsive bar width, portrait/landscape sizing, two-line high-font-scale support and centered text. No fabricated GL/EGL support is derived from artwork. Test RTL and keyboard/D-pad semantics without inventing a fifth destination.
- A new positive release/source-hash verifier and at least 18 independent negative UI mutations are mandatory. The compiled native UTF-8/extensions/float regression oracle remains active; historical source-version-bound verifiers are retained on disk as immutable evidence, not falsely run against a different release identity. Clean extraction, manifest and deterministic package verification are release-blocking. Actual Android assembly, lint, unit and physical device rendering are separate evidence and must never be reported PASS without execution.

## Release 2.2.17 Kotlin TextUnit compile hotfix
- Release identity is OpenGLESScope 2.2.17 / versionCode 2217 and derives from the exact OpenGLESScope 2.2.16 source ZIP. The predecessor ZIP SHA-256 and all 160 production-source hashes are frozen in `tests/golden/2_2_17_release_regression_contract.json`.
- The Windows `:app:compileReleaseKotlin` failure at `MainActivity.kt` line 3300 is release-blocking: Compose `TextUnit` does not provide the `minus` operator used by `compactLabelFontSize - 0.5.sp` in the locked toolchain.
- Preserve the intended navigation geometry without arithmetic on `TextUnit`: the non-accessibility OpenGL ES label uses explicit 7.5 sp landscape / 8.5 sp portrait sizes, equivalent to the previous intended 0.5 sp reduction from 8/9 sp.
- Only `app/build.gradle.kts` and `MainActivity.kt` may change in production source. Native GL/EGL collection, registry evidence, report schemas, permissions, Database behavior, blur, accessibility, RTL, TV/desktop input and all 2.2.16 navigation semantics remain unchanged.
- A dedicated compile-hotfix verifier must reject reintroduction of `TextUnit` subtraction, missing explicit OpenGL ES label size selection, version drift, unrelated production mutation or predecessor-hash drift.
- Static/source gates do not substitute for a real Android build. The user's reached Kotlin compiler failure must be considered fixed only after the corrected source reaches compilation; any new compiler diagnostic remains release-blocking.
- Deterministic package, clean extraction and rerun of the current aggregate quality gate are mandatory.

## Release 2.2.18 notification-state and EGL identity provenance audit
- Immutable predecessor is OpenGLESScope 2.2.17; its ZIP hash and every production path are fixed in `tests/golden/2_2_18_release_regression_contract.json`. Only `MainActivity.kt`, native `openglesscope.cpp`, and `app/build.gradle.kts` may change. All checked-in Khronos XML snapshots, generated catalogues, schema-5 technicalReport / schema-2 submission producers, assets, permissions, transports and product branding remain unchanged.
- Compare the offline (`ic_info`), online/offline transition (`ic_network_connected` / `ic_network_disconnected`), update-available (`ic_update_available`) and collection-success/failure glyphs against VulkanScope 3.0.12 byte-for-byte. Preserve the source-owned overlay ordering, colors, fade timings, and live-region semantics; never substitute a Wi-Fi-off glyph for the persistent informational offline message.
- Match VulkanScope's terminal failure lifetime: when runtime probe fails, returns incomplete evidence or cannot establish support (Unknown / Unavailable), its failure banner stays until a new collection rather than being hidden by the success-only timer. Successful completion alone may auto-hide. Do not infer network validation when the platform cannot establish it.
- EGL display string queries must clear/read eglGetError for *each* EGL_VENDOR, EGL_VERSION and EGL_CLIENT_APIS query, including the initial display extension query. Neither a non-null string returned together with an EGL error nor malformed UTF-8 is authoritative. Required EGL identity failure must make the complete technical report Unavailable with an explicit error, never a fabricated value or a successful empty/partial identity. EGL_CLIENT_APIS is required starting at EGL 1.2.
- EGL_NO_DISPLAY/EGL_EXTENSIONS is Not applicable for a legacy pre-1.5 implementation only when the driver specifically returns EGL_BAD_DISPLAY with null text. Other errors remain Unavailable. The self-test's EGL display extension enumeration must likewise check the EGL error and completeness gate. Query results from runtime and registry metadata remain separate; all values and unknown enums must retain their real provenance.
- The Khronos Combined GL current core baseline is ES 3.2, and the Khronos EGL current core baseline is 1.5. Registry-lock hashes, generated catalogues, canonical-name tests, GetPName and EGL-query legality, query coverage, JSON/TXT/HTML/Database schema, security, bounded resources, lifecycle, host-compiled negative fixtures and deterministic clean-extract checks are mandatory. Do not claim newest extension-registry snapshot, NDK build, physical device, or full runtime support without corresponding external evidence.
- `tools/verify_2_2_18_state_and_evidence.py` and `tools/test_2_2_18_negative_mutations.py` are release-blocking. Earlier per-version immutable rule/test evidence is retained; current tests supersede earlier exact-source expectations only for the three allowlisted production changes.


## Release 2.2.19 specification, memory and performance correction
- Exact immutable predecessor is OpenGLESScope 2.2.18; all 160 production-source hashes and predecessor ZIP SHA-256 are recorded in `tests/golden/2_2_19_release_regression_contract.json`. Only native `openglesscope.cpp` and `app/build.gradle.kts` may differ in production.
- If GL_VERSION reports an ES 3.x version, both GL_MAJOR_VERSION and GL_MINOR_VERSION must agree exactly before the collector uses version-specific query gates. Contradictory driver responses invalidate the report and self-test, never authorize newer-core queries or falsely normalize reported values.
- Shader and program info-log extraction must query/check actual GL errors, bound driver-controlled allocations to 1 MiB, use the returned `GLsizei` written length and reject negative / out-of-capacity lengths. Never search for a presumed NUL outside valid driver-written bytes.
- EGLConfig two-pass enumeration must reject a changed count rather than silently lose rows. All 31 canonical fields retain existing output order, per-query error data and API legality, but use constant-time indexed access rather than 31 repeated linear name lookups per config.
- The active release verifier, host-compiled fixtures and at least 12 mutation tests must enforce these changes. Existing 2.2.18 verifier files are immutable historical evidence, not falsely run as current-source hashes. Native resource lifecycle, process timeout, 8 MiB probe publication bound, explicit Database submission and schema 5/2 remain in effect.
- Khronos public ES 3.2 / EGL 1.5 baseline index checks are not an assertion of byte-for-byte newest extension snapshot. Exact upstream registry refresh, Android build/lint/unit, physical device and sanitizer/heap profiling results must be recorded separately as NOT EXECUTED unless actually performed. Clean deterministic ZIP and source hash equivalence are mandatory.

## Release 2.2.20 EGL query and bounded DMA-BUF report integrity
- Exact immutable predecessor OpenGLESScope 2.2.19 ZIP and all 160 production hashes are locked in `tests/golden/2_2_20_release_regression_contract.json`. Only the C++ collector and Gradle release identity may change. Existing historical source-specific tests remain archived, not falsely run against a different release identity.
- Never accept a non-null EGL Device/renderer/driver-name string as Available unless its own `eglGetError` returns EGL_SUCCESS and its text passes bounded UTF-8 validation. Device-handle discovery must also verify EGL_SUCCESS. Do not manufacture `Device N` as an actual renderer name if the query is inapplicable or failed.
- All two-pass device, DMA-BUF format/modifier and supported-compression-rate enumerations require exact agreement between expected and written counts. Changed counts produce explicit Unavailable evidence and cannot silently omit entries.
- Every returned 64-bit DMA-BUF modifier and exact `externalOnly` flag must be preserved in existing bounded EGL capability detail; counts alone are insufficient. Unknown numeric modifiers are rendered as raw hexadecimal values without fabricated canonical names. Invalid boolean flags invalidate the individual capability evidence. No schema-5/schema-2 report changes or capability fabrications.
- Preserve the 128 format, 256 modifier/format, 4096 total modifier and 256 capability safety bounds; preserve complete TXT, HTML, analysis, structured report and Database propagation. No change to permissions, privacy, network submission, lifecycle ownership or registry sources.
- `tools/verify_2_2_20_device_dma_integrity.py` host-compiled C++20 fixtures and `tools/test_2_2_20_negative_mutations.py` are active release blockers. Current and historical spec/registry, query-coverage, report, memory, security, accessibility and deterministic-package gates must pass. Newest upstream XML refresh, Android assemble/lint/unit, real GPU and long-term memory/sanitizer results remain separate evidence classes and are not inferred from static tests.


## Release 2.2.21 GPU card, navigation and official EGL brand variants
- Exact immutable predecessor is OpenGLESScope 2.2.20, SHA-256 and 160 production hashes recorded in `tests/golden/2_2_21_release_regression_contract.json`. Only MainActivity.kt and app/build.gradle.kts may differ in production. 2.2.20 native queries, registry, reports and schemas remain byte-identical.
- GPU hero card matches the neutral VulkanScope 3.0.12 `#181516` surface, keeps the truthful `System Driver` heading, and replaces the redundant OpenGL ES version row with the independently queried textual `EGL_VENDOR`. OpenGL ES/EGL core defines no Vulkan-style numeric hardware vendor ID. GL_VENDOR and EGL_VENDOR are implementation identifiers, not guarantees of physical silicon.
- GPU logo attribution is presentation only: match anchored, bounded recognized GL_VENDOR strings, not arbitrary interior substrings or GL_RENDERER/model marketing text. Existing `gpu_vendor_vsi.png` is byte-identical to `gpu_vendor_vivante.png`; until a real independently verified VeriSilicon logo is provided, VSI artwork remains neutral rather than showing a different brand. Unknown, ambiguous, Mesa/software and translation paths use neutral artwork. A logo never supplies evidence for runtime support, a numeric ID, or a specific hardware model. The accessibility description must describe only the artwork actually selected. Database and local hero use the same mapping.
- Primary destinations are exactly Overview, OpenGL ES, Display, Extensions in that order with equal-width centered items, matched VulkanScope portrait/landscape max widths (310/340 dp) and full system insets, RTL, TalkBack and TV focus. EGL remains reachable through the OpenGL ES destination and Overview; selecting the nested EGL page retains OpenGL ES as the highlighted primary tab. No fifth primary destination or unreachable content.
- Official EGL asset is immutable and is displayed with ContentScale.Fit, never redrawn into an unofficial imitation or falsely recolored at the pixel source. Identity uses the plain official EGL icon, current-context uses the same mark with a semantic context overlay, and collector pbuffer uses a surface overlay; the companions communicate different meanings. All are accessible through the parent section heading rather than duplicate TalkBack announcements.
- Dedicated active positive verifier and >=20 independent negative mutations are release blockers; historical 2.2.20 tests remain immutable evidence. Actual Android build, rendering and physical GPU identity validation remain separate evidence classes.


## Release 2.2.22 VulkanScope evidence-column and long-hold parity + native/report security audit
- Immutable predecessor is the exact OpenGLESScope 2.2.21 ZIP with the SHA-256 recorded in `tests/golden/2_2_22_release_regression_contract.json`; all 160 production files are individually locked. Only `app/src/main/java/com/efishell/openglesscope/MainActivity.kt` and `app/build.gradle.kts` may change. No GL/EGL registry, native collector, privacy/permission, structured report, TXT, HTML, history or Database payload alteration is permitted in this release.
- Common capability rows use VulkanScope 3.0.12's bounded `ExpressiveEvidenceRow` Surface rather than uncontained Row/Column text. This applies to GPU `EGL_VENDOR` and every `CapabilityKeyValue` surface throughout Overview, Features, Limits, Display, EGL and Extensions. Row geometry uses 16dp normal corners, 360dp normal stacking, 420dp bordered dialog stacking, 0.88/1.12 two-column widths, 14/13dp horizontal inset and 11/10dp vertical inset, with font-size/length/line-wrap responsiveness. The neutral row background is `#211E1F` (reference); the product-specific magenta is retained for long-hold inset/accent, TV focus and quick-action controls.
- A long-hold retains the reference 0.985 scale (110ms), accent highlight (110ms), border glow (140ms), 550ms Android TV OK/Enter long hold, bounded context action set and platform-aware desktop secondary-button handling. Explicit evidence inspector dialogs use the identical contained evidence row and `LocalDetailKeyValuePresentation` provider. No additional popup is manufactured for renderer identity.
- The GPU hero `EGL_VENDOR` row must show the independently queried `report.egl.vendor` value in exactly this reference column component, retaining the plain-text string/provenance distinction from `GL_VENDOR`. OpenGL ES/EGL do not define Vulkan-style numeric hardware vendor IDs; no ID inference or renderer marketing guess is introduced.
- Existing canonical-name, current locked Khronos registry, query-coverage, source lifecycle, bounded memory, security, report schema/TXT/HTML/Database and accessibility gates remain release blocking. All unchanged native/report sources are verified bytewise against the 2.2.21 predecessor, not claimed independently fixed. Upstream live registry-byte comparison, actual Android assemble/lint/unit, physical GPU comparisons, sanitizer/heap profiling and on-device pixel tests are separately NOT EXECUTED until observed.
- `tools/verify_2_2_22_evidence_parity.py` and `tools/test_2_2_22_negative_mutations.py` are active release blockers; historical 2.2.21 tests are retained bytewise as predecessor evidence.

## Release 3.0.0 — validated producer and ANGLE attribution
- Application release identity is 3.0.0 / 3000, paired with Database 3.0.15. Existing report schema 2 and technicalReport schema 5 are unchanged.
- GL_VENDOR, GL_RENDERER and canonical export/submission payloads retain exact driver-provided values. Google LLC is a presentation name only for historical Google Inc. display labels.
- ANGLE is a translation layer, not a physical GPU vendor. Qualcomm artwork and an Adreno display title require explicit Qualcomm and Adreno evidence inside the reported ANGLE renderer, without a software-renderer marker.
- Unknown ANGLE backends and software renderers keep unknown artwork. There is no inferred vendor or device ID.
- Temporary success/failure controls remain noninteractive for three seconds and upload is disabled until complete collection and validated network are both available.
- New Database POST accepts the exact 3.0.0/3000 audited producer; older stored reports are still readable.

## Release 3.0.1 — physical GPU identity under ANGLE and persistent failure reporting
- Show physical GPU name/vendor/artwork only from an explicit recognized model in the reported GL_RENDERER. The ANGLE translation layer or its Vulkan/Direct3D/Metal backend alone is not a GPU identity. Software backends, conflicting signatures and missing logo assets must not receive a fabricated hardware logo.
- Preserve canonical GL_VENDOR/GL_RENDERER in technical reports, TXT, JSON and Database uploads. The Google LLC name is a presentation normalization only.
- Collection failure remains visible until a validated collection changes status. Export and upload depend on complete collected evidence and are disabled on failure. Transient submit/copy action results keep the reference three-second interaction lock.
- New producer identity is OpenGLESScope 3.0.1 / 3001, paired with Database 3.0.17.

## Release 3.0.2 immutable Database warning contract
- The Database compatibility notice is the VulkanScope 3.0.12 sentence with only VulkanScope replaced by OpenGLESScope. Do not add release numbers, unofficial-app assertions, or historical storage commentary to that yellow warning. Verified by `tools/verify_3_0_2_database_notice.py`.
- New uploads must identify exact OpenGLESScope 3.0.2 / versionCode 3002 and Database 3.0.19; earlier reports remain GET-readable.

## Release 3.0.3 responsive capability metric parity
- Match VulkanScope 3.0.12's shared responsive metric grid in OpenGLESScope brand color: 1 column below 300 dp or large text on narrow screens, 2 columns below 760 dp, 3 otherwise. Use the VulkanScope-equivalent shared ExpressiveMetric card in exactly one ExpressiveMetricGrid composable, preserving the independent Overview MetricCard; do not duplicate a Kotlin signature or fall back to muted key/value Matches counters.
- Features, Limits/Diagnostics, Formats, Extensions, Precision, EGL Configs, common list/search and bounded analysis evidence have exact report-backed metric counts. Filters alter matching counts; Showing is derived from the actual visible page, never invented or overstated.
- Keep limit vs diagnostic totals disjoint, preserve explicit Available/Unavailable/Not applicable/Unknown semantics and all canonical values. Shader Precision now applies the 25-entry pager. No Vulkan-only sections or fields may be copied into GL/EGL reports.
- Current exact report submission producer is OpenGLESScope 3.0.3 / versionCode 3003 for Database 3.0.20; older/newer apps cannot POST. Previous reports remain read-only. Schema 2, technical schema 5, normalizer 16 and the locked exact Database warning text do not change.
- Source tests and clean package reproducibility must gate the new metric presentation and release handshake.

## Release 3.0.4 failed-collection shell and Database producer contract

- Match VulkanScope terminal failure behavior: failure is an explicit unavailable evidence state and must never replace the entire page content with one global EmptyState.
- Keep Overview hero, detailed red failure card, Explore, current Android Display, Quick Access, offline Encyclopedia and Settings available even without a complete GL/EGL probe.
- Report-only sections must show a nonempty titled Unavailable page; never infer Unsupported or zero capabilities from missing or failed data. The runtime-count snapshot requires a completed authoritative report.
- Catch isolated probe exceptions into a terminal unavailable GlReport carrying the actual bounded error reason; do not silently leave report null.
- Complete report TXT/HTML export and Database submit controls remain locked unless an available complete report exists; failed collection notification stays visible as in the VulkanScope reference.
- Current app identity is OpenGLESScope 3.0.4/versionCode 3004 and companion Database 3.0.21 accepts this exact pair for new POST. Earlier stored records are preserved and remain readable.
- tools/verify_3_0_4_failure_navigation.py is a required negative/regression gate and the exact-one ExpressiveMetricGrid compile fix is retained.


## OpenGLESScope 3.0.5 — canonical TXT/JSON submission contract (release-blocking)
- Producer identity is 3.0.5 / 3005, submission schema 2 / technicalReport schema 5. Database 3.0.25 is the companion; prior producers are read-only in the Database.
- `reportText()` preserves the existing compact `Pbuffer:` summary and additionally emits eight explicit `Pbuffer ...:` runtime evidence lines. Values come from the same `GlReport.eglRuntime` as `submissionJson()`, never placeholders invented to satisfy a validator.
- `eglConfigAnalysisValue()` serializes exact `recordableAndroid=`, `framebufferTargetAndroid=`, `colorComponentTypeExt=` and `unavailableAttributes=` fields per actual EGL config. An empty valid EGL config list has no fabricated configuration rows.
- No invented `GL_*`/`EGL_*` query aliases may be added to repair TXT comparison. Preserve exact native queries and the locked Khronos gl.xml/egl.xml symbols.
- Database submission failure is a failure: never report HTTP 400/403 as successful, and never upload an incomplete collection.
- Release gate must assert this producer/Worker canonical format agreement and reject removal of any retained pbuffer field.


## Release 3.0.6 real-emulator probe-integrity and producer pairing
- Preserve explicit failed EGL context evidence and attempt ordered supported ES contexts without claiming ES 3.2 is available. Android's advertised ES version and host gfxstream logger are not a substitute for actual queried GL_VERSION.
- Bounded indexed native format results are raw enumeration evidence and can contain repeated driver-provided format tokens; validate each exact original string, but do not invent, deduplicate, infer, sort or drop format rows. Strict unique identities remain mandatory for GL/EGL extensions, diagnostics, limits, EGL configs and per-(target,internalFormat) rows.
- Repeated diagnostic query identities are reconciled before serialization; conflicting status/error evidence must become an explicit Unavailable record. Invalid sample enumeration becomes Unavailable, never silently shortened or fabricated as valid.
- Any non-terminal native output is rejected with a bounded precise validation category; log error provenance in dedicated process, persist a terminal Unavailable reason and completion marker. Do not bypass JSON grammar, field validation, self-test or complete-report upload/export gating.
- Retain locked locally auditable Khronos GL/EGL core and extension XML SHA-256, standard OpenGL ES 3.2 / GLSL ES 3.20 / EGL 1.5 limits, legal query guards and no synthetic API names. No claim of newer upstream byte-match without an actual re-fetch.
- App 3.0.6 / 3006 must be paired with Database 3.0.26 for any new report upload; old reports remain readable without reattributing their source version.
- Required verification: clean source and manifest reproduction, audit, prior regression gates and 3.0.6 focused negative mutations; Android 37.2 x86_64 16 KiB emulator and device evidence require separate execution outside a host-only code review.

## Release 3.0.7 — evidence-only translator family identity
- `Android Emulator OpenGL ES Translator (...)` and `ANGLE (...)` are rendering translation layers, not physical hardware vendors. Presentation-only identity extraction is permitted solely when the actual bounded `GL_RENDERER` contains exactly one unambiguous, recognized explicit GPU model signature; the same vendor/model/logo rules apply to all supported families and models, not a particular test card or brand.
- Use only bundled appropriately attributed manufacturer artwork. Do not infer a logo or GPU from `GL_VENDOR` implementation branding, the translation-layer name, operating system, Vulkan/Direct3D backends or marketing assumptions. Software backends, duplicate/conflicting GPU models and unrecognized/ambiguous strings retain neutral artwork and their raw reported renderer text.
- The canonical `GL_VENDOR`, `GL_RENDERER`, EGL provenance, technical TXT/JSON and database submissions remain byte-for-byte runtime evidence. Display names and manufacturer artwork are not hardware attestation, a physical vendor/device ID, feature support, query results or conformance evidence.
- Source and executable Kotlin/Node regression tests cover cross-family model names, exact raw preservation, emulator translation, ANGLE, software, ambiguity, and missing artwork; app and Database versions must be paired before new POST, retaining historical read-only records and the unchanged cache-first pipeline.
