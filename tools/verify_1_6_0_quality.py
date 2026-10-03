#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require('releaseVersionName = "1.6.0"' in gradle and 'releaseVersionCode = 1600' in gradle,'1.6.0 release identity missing')
    require('13d4007bb2cfe7daeea6ea789152d28156bb7b3b3541e0e2e00647038d2d04de' in rules,'1.5.1 predecessor hash missing')
    require('95b0f34218f2a653a2660d0e3597feea50ec05d3193f17e2ff811505d146b9ac' in rules,'VulkanScope 1.4.3 reference hash missing')
    require('android.permission.ACCESS_NETWORK_STATE' in manifest,'ACCESS_NETWORK_STATE missing')
    for token in ['registerDefaultNetworkCallback','NetworkStatusBanner','OfflineFeatureAvailabilityBanner','NET_CAPABILITY_VALIDATED','LocalValidatedNetwork']:
        require(token in main,'validated-network parity missing: '+token)
    for token in ['UpdateTransferState','UpdateTransferPhase','pauseUpdateDownload','resumeUpdateDownload','requestCancelUpdateDownload','confirmCancelUpdateDownload','UpdateTransferDialog','UpdateCancelConfirmationDialog']:
        require(token in main,'update transfer parity missing: '+token)
    for token in ['MAX_UPDATE_REDIRECTS = 3','isAllowedOfficialUpdateRedirect','sha256FileHex(temp)','if (!packageSigningCertificatesMatch(installed, archive))','archive.packageName != packageName','archiveCode <= installedCode','archiveVersion != update.version','cleanupStaleUpdateArtifacts()']:
        require(token in main,'updater security regression: '+token)
    require('.followRedirects(true)' not in main and '.followSslRedirects(true)' not in main,'automatic redirects re-enabled')
    require('contentDescription = "Clear search"' in main and 'trailingIcon' in main,'search clear affordance missing')
    for icon in ['ic_analysis.xml','ic_compare.xml','ic_book.xml','ic_self_test.xml','ic_check_updates.xml','ic_update_available.xml','ic_network_connected.xml','ic_network_disconnected.xml']:
        require((ROOT/'app/src/main/res/drawable'/icon).is_file(),'semantic icon missing: '+icon)
    require('SUBMISSION_SCHEMA_VERSION = 2' in main and 'TECHNICAL_REPORT_SCHEMA_VERSION = 4' in main,'report schema drift')
    require('android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden"' in manifest,'rotation no-recollection contract missing')
    require('Vulkan-only' in rules and 'must not be fabricated' in rules,'API-specific applicability boundary missing')
    audit=ROOT/'rules/1.6.0_VULKANSCOPE_SHARED_APPLICATION_PARITY_AUDIT.md'
    require(audit.is_file(),'1.6.0 audit document missing')
if __name__=='__main__': main_guard('verify_1_6_0_quality',verify)
