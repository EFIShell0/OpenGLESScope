#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
SCRIPT='verify_2_1_1_quality.py'
def run(root:Path)->int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/SCRIPT)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode
def clone_tree():
    td=tempfile.TemporaryDirectory(prefix='ogles211-mut-',dir=ROOT.parent); dst=Path(td.name)/'tree'
    shutil.copytree(ROOT,dst,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link); return td,dst
def mutate(rel,old,new,label):
    td,c=clone_tree()
    try:
        p=c/rel; data=p.read_text(encoding='utf-8'); require(old in data,'fixture missing: '+label); p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(c)!=0,label+' mutation was not rejected')
    finally: td.cleanup()
def verify():
    require(run(ROOT)==0,'baseline 2.1.1 verifier does not pass')
    mutate('app/build.gradle.kts','releaseVersionCode = 2101','releaseVersionCode = 2102','release identity')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT = 250','ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT = 25000','unbounded Analysis render')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','Per-query timings are not fabricated','Per-query timings are estimated','fabricated timing semantics')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','QUERY_DEPENDENCIES.keys.sorted().forEach','listOf("GL_EXT_fake_capability").forEach','hand-selected/fake feature catalog')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','Page.Precision -> R.drawable.ic_precision','Page.Precision -> R.drawable.ic_features','semantic icon regression')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','OpenGL ES runtime diagnostics','OpenGL ES runtime summary','diagnostic detail regression')
    mutate('rules/PROJECT_RULES.md','## Release 2.1.1 full-report/UI/Analysis evidence parity audit','## Release 2.1.1 weakened audit','rules release contract')
if __name__=='__main__': main_guard('test_2_1_1_negative_mutations',verify)
