#!/usr/bin/env python3
"""Release-locked network/collection, Database gate and bottom-navigation parity."""
from __future__ import annotations
import hashlib, json
from gate_common import ROOT,require,main_guard
from verify_2_2_15_evidence_integrity import native_oracle,host_compiled_oracle
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_16_release_regression_contract.json'
def segment(s,a,b):
    i=s.index(a);j=s.index(b,i+len(a));return s[i:j]
def ui_oracle(s):
    db=segment(s,'CapabilitySectionCard("OpenGLESScope Database")','\n@Composable\nprivate fun CapabilityListPage(')
    for t in (
      'enabled = !submissionInFlight && completeReportReady && networkAvailable',
      'if (submissionInFlight || !completeReportReady || !networkAvailable) false else',
      'idleTrailingIcon = R.drawable.ic_upload',
      'if (!completeReportReady && !networkAvailable) {',
      'Database submission is locked for two independent reasons:',
      'When internet returns during collection, the network lock clears immediately;',
      'Database upload and public report browsing are disabled until Android reports a validated internet connection.',
      'ExpressiveExternalLinkRow("Open OpenGLESScope Database"',
      'enabled = networkAvailable',
      'if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log',
      'Compatibility notice:',
    ): require(t in db,'Database conditional/notice/detail parity missing: '+t)
    require('readableDisabledTrailing = true' not in db,'submit arrow misleadingly appears enabled offline')
    require('Database upload and public report browsing are disabled' in db and 'Text(submitState,' in db,'no independent network explanatory line/status')
    btn=segment(s,'private fun ExpressiveActionButton(','\n@Composable\nprivate fun ExpressiveIdentityBlock(')
    for t in ('!enabled -> accent','disabledContainerColor = trailingContainerColor','disabledContentColor = trailingContentColor','enabled = enabled','tint = trailingContentColor'):
        require(t in btn,'disabled action visually/semantically enabled: '+t)
    require('else -> trailingTint ?: accent' in btn,'success/failure tint lost on enabled transient actions')
    host=segment(s,'private fun TransientStatusOverlayHost(','\n@Composable\nprivate fun UpdateAvailableIcon(')
    for t in ('CollectionStatusBanner(collectionStatus)','ConnectivityStatusHost(collectionStatus, networkStateKnown, networkAvailable, networkBannerState)','UpdateStatusBanner(updateStatus, onInstallUpdate)',
      'OfflineFeatureAvailabilityBanner(collectionStatus == CollectionStatus.COLLECTING)',
      'R.drawable.ic_info','Internet features are unavailable',
      'Internet-dependent actions remain locked by network state, while report-dependent actions also remain locked until collection completes.',
      'Database submission/fetching, web links and update checks remain disabled until Android reports a validated internet connection.',
      'networkStateKnown && !networkAvailable && transitionState == NetworkBannerState.HIDDEN',
      'liveRegion = LiveRegionMode.Polite'):
        require(t in host,'VulkanScope equivalent notification lifecycle missing: '+t)
    require(host.index('CollectionStatusBanner(collectionStatus)') < host.index('ConnectivityStatusHost(') < host.index('UpdateStatusBanner(updateStatus, onInstallUpdate)'), 'popup ordering drift')
    nav=segment(s,'private fun CompactBottomNavigationBar(','\n@Composable\nprivate fun HeaderActionButton(')
    for t in (
      'compactMaxWidth = if (landscape) 384.dp else 348.dp',
      'val artworkSlot = if (landscape) 24.dp else 28.dp',
      'val eglArtworkWidth = if (landscape) 21.dp else 26.dp',
      'val glesArtworkWidth = if (landscape) 23.dp else 28.dp',
      'modifier = Modifier.size(artworkSlot)', 'contentAlignment = Alignment.Center',
      'Page.EGL -> EglBrandArtwork(', 'tint = ComposeColor.White',
      'Page.OpenGLES -> Image(', 'R.drawable.ic_opengles_gl_es',
      'else -> SemanticArtwork(', 'modifier = Modifier.size(compactIconSize)',
      'modifier = Modifier.fillMaxWidth()', 'textAlign = TextAlign.Center',
      'maxLines = if (accessibilityScale && !landscape) 2 else 1',
      'val accessibilityScale = configuration.fontScale >= 1.3f',
      '.weight(1f)', 'frostedChromeBackdrop(',
    ): require(t in nav,'4-slot portrait/landscape/RTL/scale parity missing: '+t)
    require('if (item.page == Page.EGL)' not in nav,'old offset artwork path returned')
    require('NavigationItem(Page.OpenGLES, "OpenGL® ES™"' in s and 'NavigationItem(Page.EGL, "EGL™"' in s,'API trademark/identity drift')
    require('if (collectionInProgress) "OpenGL® ES™/EGL™ collection continues offline.' in host,'collecting+offline dual lock regressed')
def verify():
    c=json.loads((ROOT/CONTRACT).read_text())
    require(c['release']=='2.2.16' and c['predecessor']=='2.2.15','release identity')
    require(c['predecessorZipSha256']=='1c06fe63a7f6a8f3cf1e01744adb03f9ef442e359cf3a01cd22f3b306c947fa2','wrong immutable predecessor')
    require(len(c['predecessorHashes'])==160 and set(c['allowlistedChanges'])=={MAIN,GRADLE},'production source census drift')
    for path,old in c['predecessorHashes'].items():
        now=hashlib.sha256((ROOT/path).read_bytes()).hexdigest()
        if path in c['allowlistedChanges']:
            change=c['allowlistedChanges'][path]
            require(change['predecessorSha256']==old and change['successorSha256']==now and old!=now and change['reason'],'unreviewed change: '+path)
        else: require(old==now,'unrelated production mutation: '+path)
    g=(ROOT/GRADLE).read_text();require('releaseVersionName = "2.2.16"' in g and 'releaseVersionCode = 2216' in g,'Gradle identity wrong')
    require('minorApiLevel = 2' in g and 'targetSdk = 37' in g,'Android API 37.2 / target 37 drift')
    s=(ROOT/MAIN).read_text();ui_oracle(s)
    n=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text();native_oracle(n);host_compiled_oracle(n)
    require('## Release 2.2.16 validated-network and bottom navigation parity' in (ROOT/'rules/PROJECT_RULES.md').read_text(),'release rules missing')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'report schema drift')
    print('verify_2_2_16_status_navigation: PASS (160-path production lineage, offline/collection/Database/nav, compiled native helper)')
if __name__=='__main__':main_guard('verify_2_2_16_status_navigation',verify)
