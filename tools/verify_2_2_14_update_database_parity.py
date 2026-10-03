#!/usr/bin/env python3
"""OpenGLESScope 2.2.14: immutable production census and VulkanScope update/list UI oracle."""
import hashlib,json
from gate_common import ROOT, require, main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt';GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_14_release_regression_contract.json'
PRE_SHA='bd9368661f2ff9089d8f30af3a288d377fb33ac8512b63b85cf9af9e1e81b159'
def sha(p):return hashlib.sha256((ROOT/p).read_bytes()).hexdigest()
def chunk(s,a,b):
 i=s.find(a); require(i>=0,'missing '+a);j=s.find(b,i+len(a));require(j>i,'missing boundary '+b);return s[i:j]
def verify():
 c=json.loads((ROOT/CONTRACT).read_text()); prev=json.loads((ROOT/'tests/golden/2_2_13_release_regression_contract.json').read_text())
 lineage={n:prev['allowlistedChanges'].get(n,{}).get('successorSha256',h) for n,h in prev['predecessorHashes'].items()}
 lineage.update({n:v['sha256'] for n,v in prev['allowlistedNewFiles'].items()})
 require(c['release']=='2.2.14' and c['predecessor']=='2.2.13' and c['predecessorZipSha256']==PRE_SHA and c['predecessorHashes']==lineage,'predecessor ZIP or immutable source chain changed')
 require(len(lineage)==160 and set(c['allowlistedChanges'])=={MAIN,GRADLE} and not c['allowlistedNewFiles'] and not c['removedFiles'],'unreviewed production scope')
 for name,old in lineage.items():
  actual=sha(name)
  if name in c['allowlistedChanges']:
   entry=c['allowlistedChanges'][name]
   require(entry['predecessorSha256']==old and entry['successorSha256']==actual and actual!=old and entry['reason'].strip(),'changed source not allowlisted '+name)
  else: require(actual==old,'unrelated production drift '+name)
 s=(ROOT/MAIN).read_text(); g=(ROOT/GRADLE).read_text()
 require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g and 'minorApiLevel = 2' in g and 'targetSdk = 37' in g,'truthful release identity/SDK drift')
 helper=chunk(s,'private fun ExpressiveDeterminateDownloadProgress(','\n@Composable\nprivate fun UpdateTransferDialog(')
 for x in ('progress.coerceIn(0f, 1f)','animateFloatAsState(','tween(180, easing = FastOutSlowInEasing)','RoundedCornerShape(999.dp)','height(14.dp)','fillMaxWidth(animatedProgress)','Brush.horizontalGradient(','target * 100f','FontFamily.Monospace'):
  require(x in helper,'VulkanScope determinate bar drift '+x)
 transfer=chunk(s,'private fun UpdateTransferDialog(','\n@Composable\nprivate fun UpdateCancelConfirmationDialog(')
 for x in ('state.totalBytes?.takeIf { it > 0L } ?: state.update.assetSizeBytes?.takeIf { it > 0L }','state.phase == UpdateTransferPhase.COMPLETED || state.phase == UpdateTransferPhase.VERIFYING -> 1f','(state.bytesDownloaded.toDouble() / progressTotal.toDouble()).coerceIn(0.0, 1.0).toFloat()','state.phase !in setOf(UpdateTransferPhase.CANCELED, UpdateTransferPhase.FAILED)','ExpressiveDeterminateDownloadProgress(progressFraction, Modifier.fillMaxWidth())','"Download speed", formatUpdateSpeedDisplay(state.bytesPerSecond)','"Downloaded", progressText','desktopVerticalPointerScroll(logState).tvRemoteLazyListNavigation(logState).focusable().focusGroup()','ExpressiveScrollHints(logState','UpdateTransferPhase.CONNECTING, UpdateTransferPhase.DOWNLOADING -> ExpressiveTextButton("Pause"','UpdateTransferPhase.PAUSED -> ExpressiveTextButton("Resume"','UpdateTransferPhase.COMPLETED -> ExpressivePrimaryIconTextButton("Install"'):
  require(x in transfer,'update transfer reference functionality missing: '+x)
 require('ExpressiveLinearProgressIndicator(Modifier.fillMaxWidth())' not in transfer,'old indeterminate line returned')
 for x in ('private fun UpdateConfirmationDialog(','"Installed version"','"Download ABI"','"Release notes"','"Downloaded versionCode"','private fun UpdateCancelConfirmationDialog(','private fun formatUpdateSpeedDisplay(','bytesPerSecond','expectedSha256'):
  require(x in s,'update verification/UI field absent: '+x)
 dbfetch=chunk(s,'private suspend fun fetchDatabaseReportPage(','\nprivate fun analysisHistoryLabel(')
 for x in ('ANALYSIS_DATABASE_LIST_LIMIT.toString()','hasValidatedInternet(context)','REPORT_LIST_CURSOR_TIMESTAMP.matches','REPORT_LIST_CURSOR_ID.matches','JSONObject(body)','nextCursor','if (nextJson != null && next == null) error(','minOf(array.length(), ANALYSIS_DATABASE_LIST_LIMIT)'):
  require(x in dbfetch,'bounded cursor parsing lost: '+x)
 vendor=chunk(s,'private fun vendorArtworkResource(','\n@Composable\nprivate fun DatabaseReportVendorBadge(')
 for x in ('"angle", "swiftshader", "mesa"','R.drawable.gpu_vendor_unknown','R.drawable.gpu_vendor_qualcomm','R.drawable.gpu_vendor_arm','R.drawable.gpu_vendor_imagination','R.drawable.gpu_vendor_nvidia','R.drawable.gpu_vendor_intel','R.drawable.gpu_vendor_amd','R.drawable.gpu_vendor_broadcom','R.drawable.gpu_vendor_samsung'):
  require(x in vendor,'vendor badge source mapping lost: '+x)
 badge=chunk(s,'private fun DatabaseReportVendorBadge(','\n@Composable\nprivate fun VendorLogo(')
 for x in ('Modifier.size(50.dp)','BrandContainer','BrandSoft.copy(alpha = 0.28f)','painterResource(vendorArtworkResource(vendor, renderer))','contentDescription = "Reported graphics vendor artwork"'):
  require(x in badge,'vendor badge parity lost: '+x)
 db=chunk(s,'"Database" -> {','"History" -> {')
 for x in ('Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {','"Load reports" else "Refresh"','"Load more", R.drawable.ic_download','modifier = Modifier.weight(1f)','databaseNextCursor != null && databaseRows.size < ANALYSIS_DATABASE_LIST_MAX','databaseRows = (databaseRows + additions).take(ANALYSIS_DATABASE_LIST_MAX)','stickyCollectionPager(visibleRows.size','DatabaseReportVendorBadge(row.vendor, row.gpuName)','"OpenGL® ES™", row.openGlesVersion','"EGL™", row.eglVersion','"Compare this report"','fetchDatabaseAnalysisSnapshot(context, row.id)'):
  require(x in db,'Database list reference parity lost '+x)
 require('painterResource(R.drawable.ic_opengles_gl_es), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).size(26.dp)' not in db,'duplicated generic GLES artwork returned')
 for x in ('TECHNICAL_REPORT_SCHEMA_VERSION = 5','SUBMISSION_SCHEMA_VERSION = 2','update.installedAbi','update.downloadAbi'):
  require(x in s,'runtime identity or report schema drift: '+x)
 require('## Release 2.2.14 VulkanScope update-transfer and Database report-list parity' in (ROOT/'rules/PROJECT_RULES.md').read_text(),'engineering rules not updated')
 print('2.2.14 update/database parity: PASS (160 production paths; only MainActivity and Gradle changed)')
if __name__=='__main__':main_guard('verify_2_2_14_update_database_parity',verify)
