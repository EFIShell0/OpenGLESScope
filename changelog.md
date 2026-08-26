# 0.7.5

- Fixed OpenGLESScope 0.7.2 report rejection when the producer preserves an explicit unavailable EGL current-binding result.
- Current EGL binding booleans now cross-check the `EGL current bindings` diagnostic instead of being forced to all-true.
- Canonical TXT binding values now cross-check the structured binding booleans fail-closed.
- Added regression coverage for accepted unavailable binding evidence plus diagnostic/TXT contradiction rejection.
- Current producer remains OpenGLESScope 0.7.2 / 702; schema 2, technicalReport 2, normalizer 10 and D1 storage are unchanged.
- No D1 migration or stored-report rewrite.

# 0.7.4

- Reworked shared database presentation geometry against VulkanScope Database 0.39.8 while preserving OpenGL ES/EGL semantics and branding.
- Rebuilt Compare control hierarchy to eliminate desktop whitespace/layout regressions and match the shared compact picker/subfilter structure.
- Unified custom-select, navigation, hero, card-grid, table-scroll, page-button and responsive filter geometry.
- Clear filters now stays hidden until a visible filter or global search is active.
- Added shared UI parity release tests.
- Hardened static-index metadata gating so CI builds 0.7.4 / OpenGLESScope 0.7.2 before source audit.
- Repository repair now removes transient dependency/cache/build directories plus forbidden root README.md/release.md automatically.
- No D1 migration or stored-report rewrite.

# 0.7.3

- Fixed the Compare workspace layout regression where checkbox toggles inherited generic form-control styles and expanded into tall vertical cards.
- Restored VulkanScope-quality Compare control hierarchy: report selectors and compact toggles on the primary row, section/field filters on a dedicated secondary row, and the share action outside the picker group.
- Added dedicated compact/responsive Compare CSS, keyboard-visible toggle states and mobile grid behavior while preserving exact comparison semantics.
- Added OpenGLESScope 0.7.2 / versionCode 702 producer support; schema 2, technicalReport 2, normalizer 10 and D1 storage remain unchanged.
- Source archive policy continues to exclude README.md, root release.md, dedicated packaged app-store metadata directories and forbidden third-party comparison product naming.

# 0.7.2

- Added the OpenGLESScope 0.7.1 / 701 producer contract.
- Current submissions carry structured installed ABI and Android-supported ABI metadata with exact TXT agreement checks.
- Historical 0.7.0 and older compatible producer payloads retain their released schema contracts.
- Removed invalid Worker requirements for non-capability state/control query names.
- Preserved Statistics, filtering, routing, Compare and D1 behavior from 0.7.1.
- Source ZIP policy now excludes README.md in addition to the existing clean-archive restrictions.

# 0.7.1

- Added a first-party Statistics workspace with interactive local SVG/CSS distributions and submission timelines.
- Added exact cohort filters for EGL version, driver mode/version, Android version, ABI, application version and exact runtime extension token.
- Added Clear filters and Display/HDR filter isolation so irrelevant GPU/GL/EGL/driver/extension filters do not distort display evidence.
- Added report and comparison permalinks with Share/Copy controls.
- Added canonical Statistics hash routing with the historical trends alias accepted for navigation.
- Added Compare section filtering and field-name search while preserving Differences only, Technical differences only and Common evidence only semantics.
- Added extension enumeration ranking with GL/EGL scope, namespace, minimum loaded-share and token search controls.
- Statistics percentages explicitly describe only loaded/filtered submissions and are never presented as market share.
- Current producer remains OpenGLESScope 0.7.0 / 700; schema 2, technicalReport 2, normalizer 10 and D1 storage remain unchanged.

# 0.7.0

- Added OpenGLESScope 0.7.0 / 700 producer support with current technicalReport schema 2 and normalizer 10.
- Added fail-closed EGL runtime/config validation and frontend Report Detail/Compare coverage.
- Removed synthetic vendor-ID presentation and retained raw submitted vendor/renderer provenance.
- Advanced current Pages assets to app.v070.js / site.v070.css.
- Preserved historical compatible technicalReport-1 reports, D1 schema/data and canonical report IDs.

# 0.2.9

- Added OpenGLESScope 0.4.1 / versionCode 401 current-producer support without changing schema 2 / technicalReport 1.
- Added canonical direct report-section and Compare hash routes.
- Added Common evidence only, cross-producer/versionCode warnings and common/one-sided/visible Compare metrics.
- Preserved Unknown / Not reported for one-sided absence and diagnostic-authoritative states for queried evidence.
- Added route, Compare, Worker, source-audit, audit-hygiene and staged Pages artifact release gates.
- Switched Pages deployment to an exact allow-listed `_site` artifact and current cache-busted app.v042.js / site.v042.css assets.
- Preserved D1 schema, stored reports, canonical report hashes and Cloudflare identity pins without migration.

# 0.2.8

- Audited against OpenGLESScope 0.3.4 / versionCode 304.
- Fixed stale `/v1/reports` currentProducer metadata that still reported 0.3.2.
- Retains strict duplicate diagnostic rejection; 0.3.4 fixes the producer-side duplicate diagnostic regression instead of weakening Database evidence rules.
- No D1 migration or stored-report rewrite.

# 0.2.7

- Fixed Cloudflare Worker deployment failure caused by a future compatibility date.
- Set compatibility_date to 2026-08-23 for the observed deploy window.
- Added release-rule protection against local-timezone future-date regressions.
- Producer/schema/D1 contracts are unchanged.

# 0.2.7

- Restored canonical TXT compatibility with OpenGLESScope 0.3.3 after its core-version provenance reporting change.
- Updated current producer metadata and direct contract tests to OpenGLESScope 0.3.3 / versionCode 303.
- Kept older compatible `Parsed core version:` producer evidence valid while requiring the new 0.3.3 `Core version:` and `Core version provenance:` evidence.
- Bumped Worker normalizer metadata to 9 and compatibility date to 2026-08-24.
- Preserved schema 2 / technicalReport 1, stored reports and D1 schema without migration.

# 0.2.5

- Updated current producer audit metadata to OpenGLESScope 0.3.2 / versionCode 302.
- Added explicit 0.3.2 submission and versionCode-mismatch contract coverage.
- Updated registry audit date and Cloudflare Worker compatibility date to 2026-08-23.
- Kept schema 2, technicalReport schema 1, normalizer 8, 0.1.17+ compatible 0.x producer floor, and D1 schema unchanged.

# 0.2.4

- Compare plain metadata/scalar rows no longer show redundant Available badges.
- Missing report-side evidence remains Unknown / Not reported.
- Limits, shader precision and Diagnostics retain diagnostic-authoritative state badges.
- Wide-color and HDR-type state badges remain because they represent actual support/availability semantics.
- Technical-differences filtering from 0.2.3 is preserved.
- No schema, normalizer, stored-payload or D1 migration change.

## 0.2.4

- Added VulkanScope-parity `Technical differences only` to Compare using the existing toggle design.
- Enabled the technical filter by default.
- Excluded Application Version / Version code and Collection Status / Complete / Source only from the technical comparison universe.
- Preserved ABI, Android/device, driver, GL/EGL and all capability/query evidence as technical.
- Compare metrics now follow the active technical field universe.
- Bumped frontend cache asset to `app.v041.js`; Worker normalizer remains 8 and current producer remains OpenGLESScope 0.2.2.

## 0.2.2

- Updated current producer to OpenGLESScope 0.2.2 / versionCode 202.
- Requires canonical Android security-patch structured/TXT parity for producer 0.2.2 and newer.
- Keeps the field optional for compatible historical producers that never collected it.
- Normalizer version is 8.
- Retains VulkanScope-style `Patch YYYY-MM-DD` Reports presentation.

# Changelog

## 0.2.1

- Added VulkanScope-parity Android security-patch sublines to Reports OS cells when explicit evidence exists.
- Added backward-compatible optional schema-2 `device.securityPatch` validation in canonical `YYYY-MM-DD` form.
- Added runtime TXT fallback for explicit `Android security patch` / `Security patch` lines without guessing missing values.
- Bumped Worker normalizer to 7 and frontend cache-busting to `app.v038.js`.


## 0.2.0
- Added OpenGLESScope 0.2.1 producer support without weakening the schema 2 / technicalReport 1 contract.
- Fixed 0.1.x-only producer parsing that rejected 0.2.1 and bypassed strict current-producer validation logic for non-0.1 versions.
- Added current producer versionCode and cd/m² TXT/structured luminance parity validation.
- Added explicit unsupported producer errors and compatible-producer visibility.
- Updated current producer metadata to OpenGLESScope 0.2.1 and normalizer version to 6.
- Retained current OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5 specification baselines.


## 0.1.26

- Fixed the remaining custom horizontal scrollbar endpoint defect by replacing the SVG scrollbar track with an HTML track/thumb mapped in CSS pixels.
- The thumb now clamps to the exact right edge when native table scrolling reaches its maximum.
- Applied the VulkanScope-compatible sticky table header geometry and neutral header surface globally across every tabular destination.
- Removed the source CSS comment and accidental literal escaped newline that violated project source rules in 0.1.25.
- Refreshed the full static security/correctness audit and Khronos OpenGL ES/EGL specification audit.
- Updated frontend cache-busting to `app.v036.js` and `site.v036.css`.
- Database version is 0.1.26; current producer target intentionally remains OpenGLESScope 0.1.25.

## 0.1.25

- Matched navigation and Reports filter control geometry to VulkanScope Database 0.35.8.
- Prevented Driver mode labels such as `System driver` from breaking inside the Reports table.
- Added `cd/m²` to available Display/HDR luminance values.
- Hid the native table horizontal scrollbar so only the synchronized custom scrollbar is visible.
- Cache-busted changed frontend assets to v034.

## 0.1.23
- Renamed the Reports table GPU header to Device to match VulkanScope presentation.
- Added VulkanScope-style single-line version chips for complete OpenGL ES and EGL runtime version strings.
- Matched Reports header color, size and weight to the VulkanScope reference.
- Matched Report ID monospace size and normal weight to VulkanScope.
- Preserved synchronized horizontal table scrolling so long complete version strings remain readable without vertical wrapping.
- Kept capability evidence, filtering, sorting, pagination, report detail, Worker schema and D1 storage semantics unchanged.

## 0.1.22
- Audited every OpenGLESScope Database destination against OpenGLESScope 0.1.25 and VulkanScope Database 0.35.8 shared quality behavior.
- Added Driver identity to Reports and driver sorting.
- Added report-detail tab counts and richer report hero metrics.
- Made Extensions and runtime Formats enumeration-diagnostic aware so unavailable enumeration is not mislabeled Not listed.
- Restricted Limits aggregation to real GL implementation-limit queries and preserved diagnostic state per loaded report.
- Made Precision aggregation use all loaded reports and retain failed/missing query evidence.
- Expanded Display/HDR aggregate evidence with mode count and luminance values.
- Replaced CSP-sensitive inline coverage widths with semantic progress elements.
- Moved custom horizontal scrollbar geometry to SVG attributes while preserving drag, keyboard, touch/trackpad, edge-shadow and resize behavior.
- Raised Worker normalizer to 5 and current producer target to OpenGLESScope 0.1.25.
- Added current-producer duplicate/evidence consistency checks for limits, diagnostics, runtime enumerations, precision, EGL configs, KHR_debug and EXT_disjoint_timer_query.
- Hardened CORS preflight with the normal API security-header set.
- Retained OpenGLESScope 0.1.25 current-header and 0.1.17 legacy compatibility.
- No D1 migration or stored-report rewrite.

## 0.1.21
- Added Android release/API and application ABI to the Reports table using loaded report detail instead of nonexistent summary columns.
- Fixed Android-version sorting to use authoritative loaded device metadata.
- Added Application ABI and Supported device ABIs to report Summary, hero context, Compare and global search.
- Added read-time `runtimeMetadata` extraction for current and historical stored reports without rewriting D1 rows.
- Hardened current 0.1.25 TXT/structured cross-checks for GPU, driver mode, OpenGL ES, Android and ABI evidence.
- Raised the canonical complete-report TXT minimum to 1000 bytes.
- Added explicit Khronos OpenGL ES 3.2, GLSL ES 3.20 and EGL 1.5 provenance metadata to Worker health/list responses and the live footer.
- Fixed reduced-motion behavior for main navigation and report detail-tab bring-into-view scrolling.
- Audited shared frontend, Worker, security, report, responsive and error-state behavior against VulkanScope Database 0.35.8.
- No D1 schema migration or stored-report rewrite.
- Active frontend JavaScript is `assets/app.v031.js`; CSS remains `assets/site.v030.css` because no stylesheet semantics changed.



## 0.1.20

- Fixed the custom horizontal table scrollbar so its thumb width and position track the real table viewport and scroll offset.
- Added pointer dragging and keyboard Arrow/Home/End control to the table scrollbar track.
- Added animated left/right table edge shadows that reflect hidden content and update while scrolling.
- Synchronized table scroll affordances across touch, trackpad, native scrolling, arrow buttons, resize changes and dynamically rendered report-detail tables.
- Hide custom table controls when no horizontal overflow exists.
- Preserved reduced-motion behavior and all OpenGL ES/EGL report semantics.
- Added v030 cache-busted JavaScript and CSS assets.
- No D1 migration or stored-report rewrite.

## 0.1.19

- Full database correctness/security/performance audit against OpenGLESScope 0.1.23 and the VulkanScope Database quality reference.
- Fixed current TXT header compatibility while preserving the OpenGLESScope 0.1.17+ compatibility floor.
- Added exact-object Worker validation, separator-safe sensitive-key checks, canonical diagnostic-state validation and duplicated display consistency.
- Fixed limit/diagnostic coverage denominators and Android HDR empty-list semantics.
- Removed silent empty-index outage fallback and surfaced report-detail load failures.
- Expanded technical global search and reduced structured detail response duplication.
- Added Worker contract tests and refreshed automated audit coverage to app.v029.js/site.v028.css.
- Hardened GitHub Pages deployment with pre-upload index/audit validation and current Pages action versions.
- No D1 migration or stored-report rewrite.

## 0.1.18

- Switched all branded EGL runtime surfaces to the bundled white monochrome EGL mark.
- Added the exact application GL|ES artwork to OpenGL ES navigation, view heading, report-detail tab and runtime section heading.
- Kept EGL Configs configuration-specific rather than using the EGL brand mark as capability evidence.
- Added v028 cache-busted frontend assets.
- No D1, report-schema or report-semantic changes.

## 0.1.16

- Matched the VulkanScope Database hero information hierarchy with OpenGL ES-native wording.
- Renamed the hero to **OpenGL ES Hardware Database** and aligned its implementation-data description and search copy.
- Replaced the previous generic hero cards with **Reports**, **GPU models**, **OpenGL ES extensions**, **Normalized fields**, and **Producer/query baseline**.
- Added a server-authored OpenGLESScope/OpenGL ES/GLSL ES/EGL query baseline and retained bounded detail loading for aggregate metrics.
- Preserved report semantics, D1 schema, raw canonical report access, and the 0.1.15 report-detail interaction parity work.

## 0.1.15
- Matched VulkanScope report-detail tab animation, control geometry, keyboard behavior and Raw report presentation.
- Added v025 frontend cache busting without schema or D1 changes.

## 0.1.14

- Removed the Overview destination and made Reports the default/root database view.
- Matched VulkanScope Database report-list hierarchy and responsive toolbar behavior.
- Kept the report-index cursor batch at 500 rows and now follows all returned cursor pages without an arbitrary 200-page truncation cap.
- Added repeated-cursor detection so a broken pagination chain fails explicitly instead of silently presenting a partial database.
- Kept user-selectable visible page sizes at exactly 10, 25, and 50 rows with a hard 50-row maximum.
- Preserved deterministic filter/sort-before-pagination behavior and exact server-authored submission timestamps.
- Added v024 cache-busted frontend assets.
- No D1 migration or report-schema change.

## 0.1.13

- Report-index sort/per-page/date parity.
- View-specific state filters.
- Extension, format and precision subfilters.
- Display/HDR filter isolation.
- Coverage and footer visual parity.

## 0.1.11
- Added GPU vendor artwork parity and dominant-percentage coverage styling based on the VulkanScope Database reference.
- Preserved OpenGL ES/EGL runtime-evidence semantics.

## 0.1.10
- Added the full GitHub repository mark/icon treatment to the hero repository action.
- Matched repository-card geometry, hover treatment and chevron behavior to the reference-quality database UI.
- Prevented unintended root-level horizontal overflow while preserving intentional table and navigation scrolling.
- Tightened responsive hero and footer geometry.
- Preserved report semantics, Worker schema, D1 schema and Cloudflare account isolation.

## 0.1.10

- Added repository `.gitignore` coverage for Node dependencies, Wrangler local state, environment files, logs, Python caches and OS metadata.
- Pinned the production Worker to the dedicated OpenGLESScope Cloudflare `account_id` so an unrelated authenticated account cannot be used accidentally.
- Pinned the production D1 binding to the dedicated OpenGLESScope D1 UUID.
- Added Wrangler auth-profile helper scripts to `worker/package.json` for one-time profile creation/activation and normal status/deploy/migration workflows.
- Pinned Wrangler to 4.124.0 for reproducible local behavior matching the deployed environment.
- Updated the Worker health response to database version 0.1.10.
- Removed Python bytecode/cache artifacts from the release package.
- Frontend behavior, report schema, D1 schema, title behavior and logo assets are unchanged from 0.1.7.

## 0.1.7

- Audited compatibility against OpenGLESScope 0.1.18 while retaining independent database versioning.
- Added application version and versionCode to server-side report summaries through a forward D1 migration.
- Hardened Worker validation for device, OpenGL ES, EGL, display, EGL config, diagnostic and report-text structure.
- Added cross-consistency checks between top-level runtime extension sets and the structured technical report.
- Raised Worker normalizer metadata to version 3.
- Added bounded and timeout-controlled frontend JSON materialization.
- Added active-navigation bring-into-view, navigation edge fades and mouse-wheel horizontal navigation.
- Added explicit newest/oldest ordering to Display & HDR evidence.
- Added state-semantic coverage visualization for implementation limits and diagnostics without coloring non-dominant containers as dominant evidence.
- Added report producer version visibility and global-search coverage.
- Improved failure-state presentation and retry flow.
- Added PNG, ICO and Apple touch icon cache-busted assets for the release.
- Preserved 50-row pagination, bounded concurrent detail fetching, exact comparison and raw canonical TXT access.

- Frontend canonical TXT compatibility fallback is retained for older stored reports when structured normalization is unavailable.


Branding/title parity in 0.1.7:
- The web header horizontal logo is copied directly from the OpenGLESScope application asset with identical bytes.
- Browser icons use the application GL|ES artwork centered on opaque black.
- Reports uses the base browser title; every other main destination prefixes its navigation label.
- Report detail titles use GPU name, active detail-tab label, then OpenGLESScope Database.


## 0.1.10 UI parity
- Added semantic local SVG icons to every main navigation destination.
- Added compact icon-bearing custom filters with selected-option checkmarks and viewport-aware listboxes.
- Matched filter height, spacing, mobile layout, focus visibility, detail tabs, pagination and table-scroll affordances to the project quality baseline.
- Added Windows-safe fail-closed Cloudflare account verification before production D1 and deploy operations.


## 0.1.13
- Matched Compare control density, GPU-name emphasis and differences-only control to the VulkanScope Database quality reference.
- Matched coverage bar/percentage hierarchy while preserving explicit OpenGL ES/EGL state labels and count denominators.
- Fixed diagnostic dominant-percentage coloring so only a unique maximum is emphasized and ties remain neutral.
- Added v022 cache-busted frontend assets.
## 0.7.0 source-package cleanup

- Removed all third-party comparison product naming from shipped source, filenames, audits and release tooling.
- Removed packaged app-store metadata bundles.
- Removed the root release.md file from the source ZIP.
- Added permanent project rules preventing these items from returning.

