#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_11_ui_permissions.py'; HASH='verify_2_2_11_release_contract.py'
def run(root, script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,text=True,capture_output=True,env=env).returncode
def verify():
    require(run(ROOT,UI)==0 and run(ROOT,HASH)==0,'unmutated 2.2.11 baseline fails')
    mutations=[
      (MAIN,'ExpressiveScrollHints(listState, Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp))','/* scroll hint accidentally removed */',UI,'missing license page scroll arrows'),
      (MAIN,'ChevronAffordance("License", "Open ${library.name} license")','ChevronAffordance("Old", "Open ${library.name} license")',UI,'license link presentation'),
      (MAIN,'itemsIndexed(lines, key = { index, _ -> "release-note:$index" })','itemsIndexed(lines, key = { index, _ -> "broken-note:$index" })',UI,'pointer/list-state mismatch'),
      (MAIN,'delay(3_000L)\n                        if (feedbackGeneration == generation) denied = false','delay(1_000L)\n                        if (feedbackGeneration == generation) denied = false',UI,'storage denial animation duration'),
      (MAIN,'label = "sharedStorageImportExportState"','label = "brokenState"',UI,'silent import export failure'),
      (MAIN,'"Import failed: ${it.message ?: it.javaClass.simpleName}"','"Unknown error"',UI,'missing attribution on import failure'),
      (MAIN,'readableDisabledTrailing = true','readableDisabledTrailing = false',UI,'invisible disabled Database submit arrow'),
      (MAIN,'enabled = !submissionInFlight && completeReportReady && networkAvailable','enabled = true',UI,'premature report submission'),
      ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999',HASH,'producer identity'),
      ('app/src/main/AndroidManifest.xml','android.permission.MANAGE_EXTERNAL_STORAGE','android.permission.WRITE_EXTERNAL_STORAGE',HASH,'unreviewed permission downgrade'),
      ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'native EGL probe mutation')
    ]
    for file,needle,replacement,script,label in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles2211-neg-') as d:
            clone=Path(d);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
            p=clone/file;src=p.read_text();require(needle in src,'stale negative mutation fixture: '+label)
            p.write_text(src.replace(needle,replacement,1))
            require(run(clone,script)!=0,'negative mutation incorrectly accepted: '+label)
if __name__=='__main__': main_guard('test_2_2_11_negative_mutations',verify)
