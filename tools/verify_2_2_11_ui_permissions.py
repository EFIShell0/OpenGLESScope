#!/usr/bin/env python3
from __future__ import annotations
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def segment(text,start,end):
    a=text.find(start);require(a>=0,'missing section '+start)
    b=text.find(end,a+len(start));require(b>a,'missing section boundary '+end)
    return text[a:b]
def verify():
    s=(ROOT/MAIN).read_text();rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require('## Release 2.2.11 license scroll arrows, shared storage feedback and submit affordance correction' in rules,'release-blocking rules absent')
    info=segment(s,'private fun InfoPage(','\n@Composable\nprivate fun CapabilityListPage(')
    require('ChevronAffordance("License", "Open ${library.name} license") { selectedLibraryLicense = library }' in info,'2.2.9 license link was not restored')
    require('Open ${library.name} license agreement' not in info,'incorrect 2.2.10 license redesign returned')
    notes=segment(s,'private fun ReleaseNotesContent(','\nprivate fun detectInstalledAbi(')
    for token in ('val listState = rememberLazyListState()','Box(modifier.padding(14.dp))','state = listState','desktopVerticalPointerScroll(listState)','tvRemoteLazyListNavigation(listState)','itemsIndexed(lines, key = { index, _ -> "release-note:$index" })','ExpressiveScrollHints(listState, Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp))'):
        require(token in notes,'license/release-note scroll arrows, pointer or D-pad missing: '+token)
    license_dialog=segment(s,'private fun LibraryLicenseDialog(','\n@OptIn(ExperimentalMaterial3ExpressiveApi::class)')
    require('ReleaseNotesContent(text, Modifier.fillMaxSize())' in license_dialog,'license dialog does not use corrected notes/scroll indicator renderer')
    require('private fun rememberScrollIndicatorVisibility(' in s and 'visible = visible && showDown' in s and 'visible = visible && showUp' in s,'shared boundary indicator missing')
    permission=segment(s,'private fun requestSharedStorageAccess(','\n    private fun showDirectUpdatesDisabledIntroIfFirstInstall(')
    for token in ('Environment.isExternalStorageManager()','ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION','ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION','pendingSharedStorageDenied = onDenied','showStoragePermissionDeniedFeedback()','onDenied()','Build.VERSION.SDK_INT < Build.VERSION_CODES.R'):
        require(token in permission,'non-parity or unsafe all-files permission lifecycle: '+token)
    permission_button=segment(s,'private fun SharedStoragePermissionActionButton(','\nprivate data class FileManagerBreadcrumb(')
    for token in ('AnimatedContent(targetState = denied, label = "sharedStoragePermissionFeedback")','if (permissionDenied) "Permission denied"','if (permissionDenied) "Shared-storage access was not granted"','scope.launch {','delay(3_000L)','if (feedbackGeneration == generation) denied = false'):
        require(token in permission_button,'3-second explicit storage permission failure animation lost: '+token)
    browser=segment(s,'private fun SharedStorageBrowserDialog(','\n@Composable\nprivate fun SoftScrollIntersectionShadows(')
    for token in ('label = "sharedStorageImportExportState"','AnimatedContent(','liveRegion = LiveRegionMode.Polite','"Import failed: ${it.message ?: it.javaClass.simpleName}"','"Export failed: ${it.message ?: it.javaClass.simpleName}"','"denied"','"unable"','result.onSuccess { status = it; requestClose() }','validatedSharedStorageDestination('):
        require(token in browser,'import/export bounded failure feedback or atomic destination lost: '+token)
    action=segment(s,'private fun TransientActionButton(','\n@Composable\nprivate fun ExpressiveLinearProgressIndicator(')
    button=segment(s,'private fun ExpressiveActionButton(','\n@Composable\nprivate fun ExpressiveIdentityBlock(')
    for token in ('readableDisabledTrailing: Boolean = false','readableDisabledTrailing = readableDisabledTrailing','enabled && !busy'):
        require(token in action,'submission disabled semantics or trailing contrast lost: '+token)
    for token in ('readableDisabledTrailing -> BrandContainer.copy(alpha = 0.80f)','BrandSoft.copy(alpha = 0.90f)','disabledContainerColor = trailingContainerColor','disabledContentColor = trailingContentColor','enabled = enabled','tint = trailingContentColor'):
        require(token in button,'disabled submit arrow must remain visibly tinted and unclickable: '+token)
    submit=info[info.index('CapabilitySectionCard("OpenGLESScope Database")'):]
    for token in ('enabled = !submissionInFlight && completeReportReady && networkAvailable','idleTrailingIcon = R.drawable.ic_upload,','readableDisabledTrailing = true','if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log'):
        require(token in submit,'Database submit gating/visual/server acknowledgement regression: '+token)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'report schema unexpectedly changed')
if __name__=='__main__':main_guard('verify_2_2_11_ui_permissions',verify)
