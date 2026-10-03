#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root):
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_2_1_3_compile_hotfix.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(rel,old,new,label):
    with tempfile.TemporaryDirectory(prefix='ogles213-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(clone)!=0,label+' mutation was not rejected')

def verify():
    require(run(ROOT)==0,'baseline 2.1.3 compile-hotfix verifier does not pass')
    mutate('app/build.gradle.kts','releaseVersionCode = 2103','releaseVersionCode = 2104','release identity')
    mutate('app/src/main/cpp/openglesscope.cpp','{hasRecordableConfigAttr, hasFramebufferTargetConfigAttr, hasFloatComponentsConfigAttr}','{hasRecordable, hasFramebufferTarget, hasFloatComponents}','stale EGLConfig extension identifiers')
    mutate('app/src/main/cpp/openglesscope.cpp','hasRecordableConfigAttr ? queryConfigAttr','hasRecordable ? queryConfigAttr','stale recordable applicability identifier')
    mutate('rules/PROJECT_RULES.md','## Release 2.1.3 NDK r30 native compile-correctness hotfix','## Release 2.1.3 weakened compile hotfix','rules compile-hotfix contract')
if __name__=='__main__': main_guard('test_2_1_3_negative_mutations',verify)
