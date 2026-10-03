#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
CHECK='verify_2_2_13_database_failure_parity.py'
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def verify():
 def run(r):
  env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(r);env['PYTHONDONTWRITEBYTECODE']='1'
  return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/CHECK)],cwd=r,env=env,text=True,capture_output=True).returncode
 require(run(ROOT)==0,'unmodified current source does not satisfy 2.2.13 contract')
 mutations=[
  (MAIN,'heightIn(max = 360.dp).desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState)', 'heightIn(max = 360.dp).verticalScroll(scrollState)','reference ChromeOS/TV scroll behavior'),
  (MAIN,'heightIn(max = 360.dp)', 'heightIn(max = 99999.dp)','bounded dialog height'),
  (MAIN,'ExpressiveContainedIconTextButton("Copy all", R.drawable.ic_copy)', 'ExpressiveContainedIconTextButton("Dismiss", R.drawable.ic_copy)','copy all control'),
  (MAIN,'submissionFailureLog?.let { log -> DatabaseSubmissionFailureDialog(log = log, onDismiss = { submissionFailureLog = null }) }', '/* failure popup silently removed */','user-visible failure details'),
  (MAIN,'if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log', 'submissionSuccessId = result.reportId','server rejection opens dialog'),
  (MAIN,'submissionFailureLog = databaseSubmissionExceptionLog("unexpected", error)', 'submissionFailureLog = null','unexpected error log'),
  (MAIN,'Submission failed (HTTP ${res.code}): $message', 'Submission failed: $message','HTTP code in short status'),
  (MAIN,'"http-response", message, res.code, body','"http-response", "unknown", null, null','server response diagnostics'),
  (MAIN,'val limit = 96 * 1024', 'val limit = Int.MAX_VALUE','bounded log resource'),
  (MAIN,'if (error is CancellationException) throw error','/* cancellation silently swallowed */','lifecycle cancellation'),
  (MAIN,'enabled = !submissionInFlight && completeReportReady && networkAvailable','enabled = true','user initiated complete-report gate'),
  ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999','producer version identity'),
  ('app/src/main/AndroidManifest.xml','android.permission.INTERNET','android.permission.SYSTEM_ALERT_WINDOW','permission drift')]
 for n,needle,replacement,label in mutations:
  with tempfile.TemporaryDirectory(prefix='ogles2213-neg-') as td:
   clone=Path(td);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
   p=clone/n;s=p.read_text();require(needle in s,'stale negative mutation fixture: '+label);p.write_text(s.replace(needle,replacement,1))
   require(run(clone)!=0,'negative mutation NOT detected: '+label)
 print('2.2.13 negative mutations: 13/13 correctly rejected')
if __name__=='__main__':main_guard('test_2_2_13_negative_mutations',verify)
