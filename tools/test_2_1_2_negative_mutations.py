#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
SCRIPT='verify_2_1_2_quality.py'
def run(root:Path)->int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/SCRIPT)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode
def clone_tree():
    td=tempfile.TemporaryDirectory(prefix='ogles212-mut-',dir=ROOT.parent); dst=Path(td.name)/'tree'
    shutil.copytree(ROOT,dst,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link); return td,dst
def mutate(rel,old,new,label):
    td,c=clone_tree()
    try:
        p=c/rel; data=p.read_text(encoding='utf-8'); require(old in data,'fixture missing: '+label); p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(c)!=0,label+' mutation was not rejected')
    finally: td.cleanup()
def verify():
    require(run(ROOT)==0,'baseline 2.1.2 verifier does not pass')
    mutate('app/build.gradle.kts','releaseVersionCode = 2102','releaseVersionCode = 2103','release identity')
    mutate('build.gradle.kts','version "9.4.1"','version "9.4.0"','stale AGP')
    mutate('app/build.gradle.kts','minSdk = 31','minSdk = 24','Android API parity regression')
    mutate('app/build.gradle.kts','ndkVersion = "30.0.16248370"','ndkVersion = "29.0.14206865"','stale NDK')
    mutate('app/build.gradle.kts','androidx.core:core-ktx:1.19.1','androidx.core:core-ktx:1.19.0','stale Core KTX')
    mutate('app/build.gradle.kts','androidx.compose.ui:ui:1.12.1','androidx.compose.ui:ui:1.12.0','stale Compose UI')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','BuildConfig.CORE_KTX_VERSION','"1.19.1"','duplicated UI dependency version')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','do not prove runtime support','prove runtime support','registry/runtime evidence confusion')
    mutate('rules/PROJECT_RULES.md','## Release 2.1.2 current toolchain/spec/query/detail parity audit','## Release 2.1.2 weakened audit','rules release contract')
if __name__=='__main__': main_guard('test_2_1_2_negative_mutations',verify)
