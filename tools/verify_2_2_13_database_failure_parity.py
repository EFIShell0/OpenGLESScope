#!/usr/bin/env python3
"""Release 2.2.13 immutable source and VulkanScope 3.0.12 Database rejection UI oracle."""
from __future__ import annotations
import hashlib,json
from gate_common import ROOT, require, main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_13_release_regression_contract.json'
PRE_SHA='243846045b784b4544afc19d7c12f5df7bcef3fe110df803f62878d060de075d'
def sha(name):return hashlib.sha256((ROOT/name).read_bytes()).hexdigest()
def section(source,a,b):
 i=source.index(a);j=source.index(b,i+len(a));return source[i:j]
def verify():
 c=json.loads((ROOT/CONTRACT).read_text());old=json.loads((ROOT/'tests/golden/2_2_12_release_regression_contract.json').read_text());nxt=json.loads((ROOT/'tests/golden/2_2_14_release_regression_contract.json').read_text())
 old_successor={n:old['allowlistedChanges'].get(n,{}).get('successorSha256',h) for n,h in old['predecessorHashes'].items()}
 old_successor.update({n:v['sha256'] for n,v in old['allowlistedNewFiles'].items()})
 require(c['predecessor']=='2.2.12' and c['release']=='2.2.13' and c['predecessorZipSha256']==PRE_SHA and c['predecessorHashes']==old_successor,'immutable previous release source chain changed')
 require(len(old_successor)==160 and set(c['allowlistedChanges'])=={MAIN,GRADLE} and not c['allowlistedNewFiles'] and not c['removedFiles'],'production scope or inventory drift')
 require(nxt['predecessorHashes']=={n:c['allowlistedChanges'].get(n,{}).get('successorSha256',h) for n,h in old_successor.items()},'immutable 2.2.13 -> 2.2.14 lineage mismatch')
 for n,h in old_successor.items():
  actual=sha(n)
  if n in c['allowlistedChanges']:
   change=c['allowlistedChanges'][n]
   require(change['predecessorSha256']==h and change['successorSha256']==nxt['predecessorHashes'][n] and actual==nxt['allowlistedChanges'][n]['successorSha256'] and change['successorSha256']!=h and change['reason'].strip(),'allowlisted successor mismatch: '+n)
  else:require(actual==h,'unrelated source/native/manifest/registry modification: '+n)
 g=(ROOT/GRADLE).read_text();s=(ROOT/MAIN).read_text()
 require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g and 'minorApiLevel = 2' in g and 'targetSdk = 37' in g,'app identity / SDK drift')
 require('## Release 2.2.13 VulkanScope-equivalent Database failure log dialog audit' in (ROOT/'rules/PROJECT_RULES.md').read_text(),'new release methodology missing')
 dialog=section(s,'private fun DatabaseSubmissionFailureDialog(', '\n@Composable\nprivate fun DatabaseFailureDialogTitle()')
 for token in (
  'AlertDialog(', 'onDismissRequest = onDismiss','modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge)',
  'shape = MaterialTheme.shapes.extraLarge','containerColor = SurfaceRaised','tonalElevation = 0.dp',
  'title = { DatabaseFailureDialogTitle() }','The Database submission did not complete successfully.',
  'heightIn(max = 360.dp).desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).padding(14.dp)',
  'fontFamily = FontFamily.Monospace','ExpressiveContainedIconTextButton("Copy all", R.drawable.ic_copy)',
  'copyEvidenceText(context, "OpenGLESScope Database submission log", log)',
  'ExpressiveContainedIconTextButton("Close", R.drawable.ic_close, onClick = onDismiss)'):
  require(token in dialog,'VulkanScope 3.0.12 Database rejection dialog parity missing: '+token)
 title=section(s,'private fun DatabaseFailureDialogTitle()','\nprivate fun copyEvidenceText(')
 require('Text("Database submission failed"' in title and 'R.drawable.ic_database_submit' in title,'reference failure title/icon missing')
 info=section(s,'private fun InfoPage(','\n@Composable\nprivate fun CapabilityListPage(')
 for token in (
  'var submissionFailureLog by remember { mutableStateOf<String?>(null) }',
  'submissionFailureLog?.let { log -> DatabaseSubmissionFailureDialog(log = log, onDismiss = { submissionFailureLog = null }) }',
  'submissionFailureLog = null',
  'if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log',
  'submissionFailureLog = databaseSubmissionExceptionLog("unexpected", error)',
  'enabled = !submissionInFlight && completeReportReady && networkAvailable',
  'Text(submitState, color = ComposeColor(0xFFAAAAAA)'):
  require(token in info,'failure handling or submit gate disconnected: '+token)
 helper=section(s,'private fun boundedDatabaseSubmissionLog(','\nprivate fun submissionJson(')
 for token in (
  'val limit = 96 * 1024','[log truncated at $limit characters]',
  'result=failure','phase=$phase','httpStatus=$httpCode','detail=$detail','responseBody:',
  'if (!hasValidatedInternet(context))','if (!report.available)',
  'Submission failed (HTTP ${res.code}): $message',
  'databaseSubmissionFailure("Submission failed (HTTP ${res.code}): $message", "http-response", message, res.code, body)',
  'databaseSubmissionFailure("Submission failed: Database returned a malformed report ID."',
  'if (error is CancellationException) throw error',
  'databaseSubmissionExceptionLog("network-request", error)',
  'return DatabaseSubmissionResult(false, null, summary, log)'):
  require(token in helper,'bounded log/error route missing: '+token)
 require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'report producer schema changed')
if __name__=='__main__':main_guard('verify_2_2_13_database_failure_parity',verify)
