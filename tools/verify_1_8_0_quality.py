#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require('releaseVersionName = "1.8.0"' in gradle and 'releaseVersionCode = 1800' in gradle,'1.8.0 identity missing')
    # Shared VulkanScope dialog/action geometry and semantic controls.
    for token in [
        'private fun QuestionDialogTitle(text: String)',
        'private fun SemanticDialogTitle(text: String, icon: Int)',
        'private fun ExpressiveCancelButton(',
        'private fun ExpressiveCloseButton(',
        'private fun ExpressiveContainedIconTextButton(',
        'private fun ExpressivePrimaryIconTextButton(',
        'private fun UpdateDialogKeyValue(',
        'shape = MaterialTheme.shapes.extraLarge',
        'ExpressiveContainedIconTextButton("Cancel download", R.drawable.ic_close',
        'ExpressivePrimaryIconTextButton("Download update", R.drawable.ic_download_update',
        'ExpressivePrimaryIconTextButton("Install", R.drawable.ic_download_update',
    ]: require(token in main,'shared VulkanScope dialog/button parity missing: '+token)
    # About/Application parity and local library-license presentation.
    require(main.count('ExpressiveActionButton("Check for updates"') == 1,'manual update action must exist only once in Info/Application')
    for token in [
        'CapabilitySectionCard("Application")',
        'CapabilitySectionCard("Libraries")',
        'CapabilitySectionCard("Build toolchain")',
        'CapabilitySectionCard("About")',
        'OpenGLESScope is not an official Khronos Group project.',
        'OpenGLESScope is an OpenGL ES and EGL capability and device inspection utility for Android.',
        'LibraryLicenseDialog(library = library',
        'context.assets.open(library.licenseAsset)',
        'licenses/apache_2_0.md',
    ]: require(token in main,'Info/About parity missing: '+token)
    require((ROOT/'app/src/main/assets/licenses/apache_2_0.md').is_file(),'local/offline Apache 2.0 license missing')
    # Destructive Analysis operations require explicit confirmation.
    for token in [
        'QuestionDialogTitle("Delete analysis history snapshot?")',
        'QuestionDialogTitle("Delete all analysis history?")',
        'QuestionDialogTitle("Remove watched evidence?")',
        'QuestionDialogTitle("Delete all watched evidence?")',
        'pendingHistoryDeleteAll = true',
        'pendingWatchDeleteAll = true',
        'pendingWatchDelete = token',
    ]: require(token in main,'destructive confirmation parity missing: '+token)
    # Race-safe updater state ownership and stronger OpenGLESScope stable-channel security.
    for token in [
        'updateCheckInFlight by mutableStateOf(false)',
        'updateStatusHideJob: Job? = null',
        'if (updateCheckInFlight || updateDownloadJob != null || updateTransferState != null) return',
        'updateStatusHideJob?.cancel()',
        'if (!updateCheckInFlight && updateStatus == displayedStatus',
        '!it.optBoolean("draft", false) && !it.optBoolean("prerelease", false)',
        'followRedirects(false)',
        'MAX_UPDATE_REDIRECTS = 3',
        'sha256FileHex(temp)',
        'packageSigningCertificatesMatch(installed, archive)',
        'pending != null && pending.exists() && directUpdatesEnabled',
    ]: require(token in main,'shared updater/security parity missing: '+token)
    # Database submission remains bounded/fixed-origin and failure detail is copyable.
    for token in [
        'private fun boundedDatabaseSubmissionLog(value: String)',
        'val limit = 96 * 1024',
        'payloadBytes.size > 2 * 1024 * 1024',
        'readResponseTextLimited(res.body, 64 * 1024)',
        'base.host != "openglesscope-database-api.openglesscope.workers.dev"',
        'DatabaseSubmissionFailureDialog(log = log',
        'ExpressiveContainedIconTextButton("Copy all", R.drawable.ic_copy',
    ]: require(token in main,'Database failure/security parity missing: '+token)
    # Retain 1.7 Encyclopedia crash guards.
    for token in ['ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 100','ENCYCLOPEDIA_MAX_QUERY_LENGTH = 160','largeFamilyNeedsQuery','Dispatchers.IO','take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1)']:
        require(token in main,'Encyclopedia stability regression: '+token)
    require((ROOT/'rules/1.8.0_VULKANSCOPE_COMMON_DIALOG_SECURITY_PARITY_AUDIT.md').is_file(),'1.8.0 audit missing')
    require('stable-channel security contract' in rules and 'Companion Database release 1.0.4' in rules,'1.8.0 rules contract incomplete')

if __name__=='__main__': main_guard('verify_1_8_0_quality',verify)
