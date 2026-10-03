#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json,re
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'; GRADLE='app/build.gradle.kts'
def sha(name): return hashlib.sha256((ROOT/name).read_bytes()).hexdigest()
def section(s,a,b):
 i=s.find(a);require(i>=0,'missing section start: '+a)
 j=s.find(b,i+len(a));require(j>i,'missing section boundary: '+b)
 return s[i:j]
def verify():
 g=(ROOT/GRADLE).read_text();s=(ROOT/MAIN).read_text();rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
 prev=json.loads((ROOT/'tests/golden/2_2_11_release_regression_contract.json').read_text())
 c=json.loads((ROOT/'tests/golden/2_2_12_release_regression_contract.json').read_text())
 lineage={n:prev['allowlistedChanges'].get(n,{}).get('successorSha256',h) for n,h in prev['predecessorHashes'].items()}
 lineage.update({n:v['sha256'] for n,v in prev['allowlistedNewFiles'].items()})
 require(c['predecessorHashes']==lineage and c['release']=='2.2.12' and c['predecessor']=='2.2.11' and c['predecessorZipSha256']=='058bbdb1afbd804ea511f691134c2c6c589d720ed76d4d386150c7ba6cb3e761','release predecessor chain drift')
 changes=c['allowlistedChanges'];require(set(changes)=={MAIN,GRADLE} and not c['allowlistedNewFiles'] and not c['removedFiles'],'production scope drift')
 next_release=json.loads((ROOT/'tests/golden/2_2_13_release_regression_contract.json').read_text())
 expected_successor={n:changes.get(n,{}).get('successorSha256',h) for n,h in lineage.items()}
 require(next_release['predecessor']=='2.2.12' and next_release['predecessorHashes']==expected_successor,'historical 2.2.12 -> current source lineage drift')
 for n,h in lineage.items():
  if n in changes:
   ch=changes[n];require(ch['predecessorSha256']==h and ch['successorSha256']==next_release['predecessorHashes'][n] and ch['reason'].strip() and ch['successorSha256']!=h,'invalid allowlisted source: '+n)
  else:require(sha(n)==h,'unrelated / native / registry production source drift: '+n)
 require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g and 'minorApiLevel = 2' in g and 'targetSdk = 37' in g,'version/SDK identity mismatch')
 require('## Release 2.2.12 reference Extension Details, Database submission, trademark and cold-start timing correction' in rules,'release methodology missing')
 ext=section(s,'private fun ExtensionsPage(','\n@Composable\nprivate fun PrecisionPage(')
 dialog=section(ext,'ExpressiveDetailDialog(ext, onDismiss = { selected = null }) {','\n        }\n    }\n}')
 for marker in ('CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Scope"','CapabilityKeyValue("Namespace"','CapabilityKeyValue("Registry baseline"','CapabilityKeyValue("Dedicated query handler"','QUERY_DEPENDENCIES[ext]','"Unknown · query evidence unavailable"','extensionRegistryUrl(ext)?.let','"Open Khronos specification"','enabled = LocalValidatedNetwork.current','uriHandler.openUri(url)','Runtime enumeration, registry metadata'):
  require(marker in dialog,'reference details lost: '+marker)
 require('modifier = Modifier.fillMaxWidth()' not in dialog and dialog.count('"Open Khronos specification"')==1,'Khronos button stretched/duplicated')
 for forbidden in ('TransientActionButton(','ExpressiveActionButton(','"Copy name + value"','"Share evidence"','"Add to watched evidence"','"Open in Encyclopedia"'):
  require(forbidden not in dialog,'extra, non-reference details action returned: '+forbidden)
 require('val evidenceValue = "ENUMERATED · $scopeName"' in ext,'extension enumeration evidence value may not be fabricated')
 require('CapabilityKeyValue("Runtime evidence", "Exact extension token enumerated")' in dialog,'reference runtime evidence must be exact')
 require('CapabilityKeyValue(ext, "ENUMERATED · $scopeName")' in ext,'token common long-press evidence actions removed')
 info=section(s,'private fun InfoPage(','\n@Composable\nprivate fun CapabilityListPage(')
 submit=section(info,'CapabilitySectionCard("OpenGLESScope Database")','Text("Submission is explicit and user-initiated.')
 for t in ('TransientActionButton(','idleTrailingIcon = R.drawable.ic_upload','enabled = !submissionInFlight && completeReportReady && networkAvailable','Compatibility notice: when a newer OpenGLESScope release','Text(submitState, color = ComposeColor(0xFFAAAAAA)','TransientIconButton(idleIcon = R.drawable.ic_copy','runCatching { copyEvidenceText(context, "OpenGLESScope report ID", reportId) }.isSuccess','if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log','"Open report"'):
  require(t in submit,'missing reference Database transition: '+t)
 require('DatabaseSubmissionStateCard(' not in s,'duplicated report animation card still present')
 transient=section(s,'private fun TransientActionButton(','\n@Composable\nprivate fun TransientIconButton(')
 require('delay(3000)' in transient and 'state = if (success) 2 else 3' in transient,'submit transient states not bounded')
 copy=section(s,'private fun TransientIconButton(','\n@Composable\nprivate fun ExpressiveLinearProgressIndicator(')
 for t in ('AnimatedContent(','scaleIn(tween(220), initialScale = 0.72f)','scaleOut(tween(150), targetScale = 0.82f)','R.drawable.ic_check','R.drawable.ic_close','delay(3000L)','LiveRegionMode' if False else 'IconButton('):require(t in copy,'report ID transient copy drift: '+t)
 require('OPENGL_ES_TRADEMARK_DISPLAY_REGEX.replace(text, "OpenGL® ES™")' in s and 'OpenGL(?:®)?\\s+ES(?!™' in s,'idempotent ® ES™ display transform missing')
 require('"OpenGL® ES™"' in s and 'OpenGL® ES™™' not in s,'bad OpenGL ® ES™ presentation')
 for t in ('startupActivityStartNanos = System.nanoTime()','startupTimingColdLaunch = savedInstanceState == null','startupGateDelayMs by mutableStateOf<Long?>(null)','if (startupTimingColdLaunch && startupGateDelayMs == null)','System.nanoTime() - startupActivityStartNanos','Page.Analysis -> AnalysisPage(activity, report, display, collectionElapsedMs, activity.startupGateDelayMs)','startupGateDelayMs?.let { gate ->','"Opening / startup gate"','formatAnalysisElapsedTime(gate)','String.format(java.util.Locale.ROOT, "%.3f s (%d ms)"'):
  require(t in s,'cold startup diagnostic time parity missing: '+t)
 require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'report/submission schema changed')
if __name__=='__main__':main_guard('verify_2_2_12_reference_parity',verify)
