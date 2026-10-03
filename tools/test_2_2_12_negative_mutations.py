#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'; CHECK='verify_2_2_12_reference_parity.py'
def run(root):
 env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
 return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/CHECK)],cwd=root,env=env,capture_output=True,text=True).returncode
def verify():
 require(run(ROOT)==0,'unmutated 2.2.12 baseline fails')
 mutations=[
  (MAIN,'"Open Khronos specification"','"Open elsewhere"','Khronos-only action'),
  (MAIN,'CapabilityKeyValue("Dedicated query handler"','CapabilityKeyValue("Fake handler"','extension query disclosure'),
  (MAIN,'"Unknown · query evidence unavailable"','"Unsupported"','unknown is not unsupported'),
  (MAIN,'val evidenceValue = "ENUMERATED · $scopeName"','val evidenceValue = "ASSUMED · $scopeName"','runtime enumeration integrity'),
  (MAIN,'CapabilityKeyValue("Runtime evidence", "Exact extension token enumerated")','TransientActionButton("Share evidence", "injected", R.drawable.ic_share) { true }; CapabilityKeyValue("Runtime evidence", "Exact extension token enumerated")','extra action in details'),
  (MAIN,'Text(submitState, color = ComposeColor(0xFFAAAAAA)','Text("Always successful", color = ComposeColor(0xFFAAAAAA)','true submit status'),
  (MAIN,'enabled = !submissionInFlight && completeReportReady && networkAvailable','enabled = true','premature submission'),
  (MAIN,'TransientIconButton(idleIcon = R.drawable.ic_copy','IconButton(idleIcon = R.drawable.ic_copy','report ID transient copy'),
  (MAIN,'startupGateDelayMs?.let { gate ->','/* removed startup timing */ run {','cold timing'),
  (MAIN,'"OpenGL® ES™"','"OpenGL® ES"','ES trademark'),
  ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999','producer identity'),
  ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE','native probe drift'),
 ]
 for n,needle,replacement,label in mutations:
  with tempfile.TemporaryDirectory(prefix='ogles2212-neg-') as td:
   clone=Path(td);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
   p=clone/n;s=p.read_text();require(needle in s,'stale mutation: '+label);p.write_text(s.replace(needle,replacement,1))
   require(run(clone)!=0,'negative mutation incorrectly accepted: '+label)
if __name__=='__main__':main_guard('test_2_2_12_negative_mutations',verify)
