#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_10_interaction_regressions.py';HASH='verify_2_2_10_release_contract.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,text=True,capture_output=True,env=env).returncode
def verify():
    require(run(ROOT,UI)==0 and run(ROOT,HASH)==0,'source baseline fails new release gates')
    mutations=[
        (MAIN,'ChevronAffordance("License", "Open ${library.name} license")','ChevronAffordance("Broken", "Open ${library.name} license")',UI,'accessible license'),
        (MAIN,'ChevronAffordance("License"','ChevronAffordance("Missing"',UI,'license arrow visibility'),
        (MAIN,'CapabilityKeyValue("Runtime evidence", "Exact extension token enumerated")','CapabilityKeyValue("Runtime evidence", "Fabricated")','verify_2_2_12_reference_parity.py','extension evidence disclosure'),
        (MAIN,'"Open Khronos specification"','"Non-reference action"','verify_2_2_12_reference_parity.py','reference-only Khronos action'),
        (MAIN,'CapabilityKeyValue("Dedicated query handler"','CapabilityKeyValue("Fake handler"','verify_2_2_12_reference_parity.py','query-gate evidence'),
        (MAIN,'"Unknown · query evidence unavailable"','"Unsupported"','verify_2_2_12_reference_parity.py','no unsupported fabrication'),
        (MAIN,'chooserColumns = if (maxWidth < 310.dp','chooserColumns = if (maxWidth < 380.dp',UI,'three column reference layout'),
        (MAIN,'color = ComposeColor(0xFF181516),\n                    contentColor = TextPrimary,','color = SurfaceRaised,\n                    contentColor = TextPrimary,',UI,'neutral modal surface'),
        (MAIN,'navigationBarColor = android.graphics.Color.BLACK','navigationBarColor = android.graphics.Color.TRANSPARENT',UI,'modal system-bar color'),
        (MAIN,'Compatibility notice: when a newer OpenGLESScope','Deprecated notice: when a newer OpenGLESScope',UI,'Database compatibility notice'),
        (MAIN,'runtimeQueryEvidence(report, report.glRuntime.contextFlags','"Unavailable" + runtimeQueryEvidence(report, report.glRuntime.contextFlags',UI,'GPU runtime evidence integrity'),
        ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999',HASH,'genuine producer identity'),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'unchanged native EGL probe')
    ]
    for file,needle,replacement,script,label in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles2210-neg-') as d:
            clone=Path(d);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
            p=clone/file;src=p.read_text();require(needle in src,'stale 2.2.10 negative fixture: '+label)
            p.write_text(src.replace(needle,replacement,1))
            require(run(clone,script)!=0,'negative mutation incorrectly accepted: '+label)
if __name__=='__main__':main_guard('test_2_2_10_negative_mutations',verify)
