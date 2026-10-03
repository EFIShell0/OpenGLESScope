#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt';UI='verify_2_2_5_input_layout_accessibility.py';HASH='verify_2_2_5_regression_contract.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,env=env,capture_output=True,text=True).returncode

def verify():
    require(run(ROOT,UI)==0 and run(ROOT,HASH)==0,'2.2.5 baseline regression gate failed')
    cases=[
      ('app/build.gradle.kts','releaseVersionCode = 2205','releaseVersionCode = 2204',UI,'release identity'),
      (MAIN,'viewConfiguration.touchSlop','0f',UI,'desktop drag slop'),
      (MAIN,'if (!event.buttons.isPrimaryPressed) {','if (!event.buttons.isSecondaryPressed) {',UI,'desktop drag button'),
      (MAIN,'requester.bringIntoView()','Unit',UI,'focused browse row visibility'),
      (MAIN,'maxWidth > maxHeight && maxWidth >= 700.dp','false',UI,'landscape split pane'),
      (MAIN,'Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(10.dp)','Modifier.fillMaxSize().padding(0.dp).padding(10.dp)',UI,'system inset coverage'),
      (MAIN,'chunked(chooserColumns)','chunked(3)',UI,'adaptive high DPI chooser'),
      (MAIN,'Surface(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }, shape = MaterialTheme.shapes.medium, color = if (warning)', 'Surface(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }, shape = MaterialTheme.shapes.medium, color = if (warning)',UI,'polite status'),
      (MAIN,'if (isRtl) "‹" else "›"','">"',UI,'RTL breadcrumb'),
      (MAIN,'if (rtl) R.drawable.ic_chevron_left else R.drawable.ic_chevron_right','R.drawable.ic_chevron_right',UI,'RTL folder arrow'),
      (MAIN,'TECHNICAL_REPORT_SCHEMA_VERSION = 5','TECHNICAL_REPORT_SCHEMA_VERSION = 4',UI,'unrelated schema'),
      ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'unreviewed native source'),
    ]
    for rel,old,new,gate,label in cases:
        with tempfile.TemporaryDirectory(prefix='og225-mut-') as tmp:
            dest=Path(tmp)
            shutil.copytree(ROOT,dest,dirs_exist_ok=True,ignore=shutil.ignore_patterns('__pycache__','.gradle','build'))
            path=dest/rel;before=path.read_text();require(old in before,'stale negative mutation: '+label)
            path.write_text(before.replace(old,new,1))
            require(run(dest,gate)!=0,'mutation escaped gate: '+label)
if __name__=='__main__':main_guard('test_2_2_5_negative_mutations',verify)
