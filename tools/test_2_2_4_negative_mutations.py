#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_4_ui_parity.py'; CONTRACT='verify_2_2_4_regression_contract.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    cp=subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,env=env,capture_output=True,text=True)
    return cp.returncode

def verify():
    require(run(ROOT,UI)==0 and run(ROOT,CONTRACT)==0,'2.2.4 clean baseline gate failed')
    cases=[
      ('app/build.gradle.kts','releaseVersionCode = 2204','releaseVersionCode = 2203',UI,'version identity'),
      (MAIN,'NavigationItem(Page.EGL, "EGL™"','NavigationItem(Page.Display, "Display"',UI,'navigation order'),
      (MAIN,'OverlayCoordinationLead = 96.dp','OverlayCoordinationLead = 0.dp',UI,'pager overlay coordination'),
      (MAIN,'private fun ScrollBoundaryIndicators(','private fun BrokenScrollIndicator(',UI,'scroll arrows'),
      (MAIN,'optJSONObject("nextCursor")','optJSONObject("missingCursor")',UI,'report list cursor'),
      (MAIN,'filter { previous.add(it.id) }','filter { true }',UI,'duplicate report list rows'),
      (MAIN,'databaseRunning = true\n                            databaseBaseline = null\n                            val requestedId = databaseId','databaseRunning = true\n                            databaseBaseline = currentSnapshot\n                            val requestedId = databaseId',UI,'stale Analysis baseline'),
      (MAIN,'private fun FloatingStatusSurface(','private fun OldFloatingStatus(',UI,'popup host'),
      (MAIN,'targetState = phase','targetState = 0',UI,'report result animation'),
      (MAIN,'TECHNICAL_REPORT_SCHEMA_VERSION = 5','TECHNICAL_REPORT_SCHEMA_VERSION = 4',UI,'schema drift'),
      ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',CONTRACT,'unreviewed native source')]
    for path,before,after,script,label in cases:
        with tempfile.TemporaryDirectory(prefix='og224-negative-') as td:
            clone=Path(td);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('__pycache__','.gradle','build'))
            file=clone/path;text=file.read_text();require(before in text,'stale mutation fixture: '+label)
            file.write_text(text.replace(before,after,1))
            require(run(clone,script)!=0,'negative mutation escaped gate: '+label)
if __name__=='__main__':main_guard('test_2_2_4_negative_mutations',verify)
