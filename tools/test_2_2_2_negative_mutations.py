#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
SCRIPTS=['verify_2_2_2_video_ui_parity.py','verify_2_2_9_release_contract.py']
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def run(p,name):
    e=os.environ.copy();e['OPENGLESSCOPE_ROOT']=str(p);e['PYTHONDONTWRITEBYTECODE']='1'
    cp=subprocess.run([sys.executable,'-B',str(ROOT/'tools'/name)],cwd=p,env=e,text=True,capture_output=True)
    return cp.returncode

def verify():
    for n in SCRIPTS:require(run(ROOT,n)==0,'original baseline fails '+n)
    mutations=[
        ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999','version identity',SCRIPTS[0]),
        (MAIN,'ExpressiveDetailDialog("Evidence provenance", onDismiss)','AlertDialog(', 'video evidence dialog regression',SCRIPTS[0]),
        (MAIN,'Modifier.fillMaxWidth().desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).focusable().focusGroup()','Modifier.fillMaxWidth().desktopVerticalPointerScroll(scrollState).verticalScroll(scrollState).focusable().focusGroup()', 'TV scrolling regression',SCRIPTS[0]),
        (MAIN,'CapabilityKeyValue(ext, "ENUMERATED · $scopeName")','CapabilityKeyValue(ext, "SUPPORTED · $scopeName")','fabricated extension support',SCRIPTS[0]),
        (MAIN,'Unknown · query evidence unavailable','Supported', 'unknown query evidence fabricated',SCRIPTS[0]),
        (MAIN,'DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })','Text(report.renderer)', 'overview source-derived row removed',SCRIPTS[0]),
        (MAIN,'DetailEvidenceRow("Value", value.ifBlank','Text(value.ifBlank', 'provenance value row removed',SCRIPTS[0]),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE','unreviewed native drift',SCRIPTS[1]),
    ]
    for rel,old,new,label,script in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles-222-mutate-') as t:
            clone=Path(t)
            shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('__pycache__','.gradle','build'))
            path=clone/rel
            source=path.read_text(encoding='utf-8')
            require(old in source,'mutation fixture stale: '+label)
            path.write_text(source.replace(old,new,1),encoding='utf-8')
            require(run(clone,script)!=0,'deliberate regression escaped gate: '+label)
if __name__=='__main__':main_guard('test_2_2_2_negative_mutations',verify)
