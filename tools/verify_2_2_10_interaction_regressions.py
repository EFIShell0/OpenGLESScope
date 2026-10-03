#!/usr/bin/env python3
from __future__ import annotations
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def section(s,start,end):
    a=s.find(start);require(a>=0,'missing UI start '+start)
    b=s.find(end,a+len(start));require(b>a,'missing UI boundary '+end)
    return s[a:b]
def verify():
    s=(ROOT/MAIN).read_text(encoding='utf-8');g=(ROOT/'app/build.gradle.kts').read_text();r=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g,'current producer identity missing')
    require('## Release 2.2.10 license, Extensions evidence, File Manager chooser, Database and GPU detail correction' in r,'current release rules section missing')
    license=section(s,'private fun InfoPage(','\n@Composable\nprivate fun CapabilityListPage(')
    # The 2.2.11 contract explicitly reverts 2.2.10's link presentation, without removing license navigation.
    require('ChevronAffordance("License", "Open ${library.name} license") { selectedLibraryLicense = library }' in license,'requested historical License control not restored')
    require('Text(library.licenseName, color = TextSecondary' in license and 'LibraryLicenseDialog(' in license,'license entry/content missing')
    ext=section(s,'private fun ExtensionsPage(','\n@Composable\nprivate fun PrecisionPage(')
    dialog=section(ext,'ExpressiveDetailDialog(ext, onDismiss = { selected = null }) {','\n        }\n    }\n}')
    if 'releaseVersionName = "2.2.14"' in g:
        dialog=section(ext,'ExpressiveDetailDialog(ext, onDismiss = { selected = null }) {','\n        }\n    }\n}')
        for token in ('CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Scope"','CapabilityKeyValue("Dedicated query handler"','"Open Khronos specification"','enabled = LocalValidatedNetwork.current','extensionRegistryUrl(ext)?.let'):
            require(token in dialog,'2.2.12 reference details missing: '+token)
        require(not any(x in dialog for x in ('TransientActionButton(', 'ExpressiveActionButton(', '"Copy name + value"','"Add to watched evidence"')),'superseded duplicate Extensions action reintroduced')
    else:
        actions=('TransientActionButton("Copy name + value"','ExpressiveActionButton("Share evidence"','TransientActionButton("Add to watched evidence"','ExpressiveActionButton("Open in Encyclopedia"','"Open Khronos specification"')
        for action in actions:require(dialog.count(action)==1,'missing/duplicated historical Extensions option '+action)
        require(max(dialog.index(x) for x in actions) < dialog.index('DetailEvidenceRow("Scope", scopeName)'), 'long registry/query rows obscure historical actions')
        require('enabled = LocalValidatedNetwork.current' in dialog and 'extensionRegistryUrl(ext)?.let' in dialog,'historical online gate regression')
        require('runCatching { copyEvidenceText(context' in dialog and 'shareEvidenceText(context' in dialog and 'environment?.addWatch?.invoke(referenceToken)' in dialog and 'environment?.openEncyclopedia?.invoke(referenceToken)' in dialog,'historical actions disconnected')
    chooser=section(s,'private fun FileManagerOptionsChooser(','\n@Composable\nprivate fun ExpressiveSearchField(')
    for needle in ('chooserColumns = if (maxWidth < 310.dp || LocalConfiguration.current.fontScale >= 1.65f) 2 else 3','FileManagerViewMode.entries.chunked(chooserColumns)','widthIn(min = 292.dp, max = 360.dp)','modifier = Modifier.size(27.dp)','height(3.dp)','ComposeColor(0xFF181516)','ComposeColor.Black.copy(alpha = 0.52f)','decorFitsSystemWindows = false','DialogWindowProvider','navigationBarColor = android.graphics.Color.BLACK','isNavigationBarContrastEnforced = false','statusBarsPadding().navigationBarsPadding()','dpadScrollableNavigation(scrollState)','onViewMode(mode)','onSortMode(mode)'):
        require(needle in chooser,'file manager layout or modal blur/system bar parity lost: '+needle)
    gpu=section(s,'private fun HeroCard(report: GlReport)','\n@Composable\nprivate fun VendorLogo(')
    for needle in ('"GL_RENDERER"','"GL_VENDOR"','"GL_VERSION"','coreVersionProvenance(report)','"GL_CONTEXT_FLAGS"','runtimeQueryEvidence(report, report.glRuntime.contextFlags','"Reset strategy query"','"Robust access query"','report.glRuntime.unavailableAttributes','"EGL_VENDOR"','"EGL_VERSION"','report.eglRuntime.boundApi','report.eglRuntime.currentContext','report.eglRuntime.configId','report.eglRuntime.surfaceWidth','"EGL display extensions"','"EGL configurations"','"Query diagnostics"','report.diagnostics.count'):
        require(needle in gpu,'GPU Details lacks collected, defensible GL/EGL evidence: '+needle)
    require('DetailEvidenceRow("GL_CONTEXT_FLAGS", runtimeQueryEvidence(report, report.glRuntime.contextFlags' in gpu,'GPU context flags must use authoritative runtime query, not fabricated prefix')
    require(('Driver build/version is unavailable through a standardized OpenGL® ES™ query.' in gpu or 'Driver build/version is unavailable through a standardized OpenGL® ES query.' in gpu),'driver-version evidence boundary lost')
    info=section(s,'private fun InfoPage(','\n@Composable\nprivate fun CapabilityListPage(')
    require('Compatibility notice: when a newer OpenGLESScope release raises the Database submission floor' in info,'missing Database compatibility notice')
    require(info.index('Compatibility notice: when a newer OpenGLESScope') < info.index('Text(submitState,') if 'releaseVersionName = "2.2.14"' in g else info.index('Compatibility notice: when a newer OpenGLESScope') < info.index('DatabaseSubmissionStateCard('),'Database rejection notice is hidden below status')
    require('rejected by the server' in info and 'instead of being treated as a successful upload' in info,'server rejection/success evidence is misleading')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'report schema changed')
if __name__=='__main__':main_guard('verify_2_2_10_interaction_regressions',verify)
