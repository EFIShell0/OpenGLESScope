#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

CASES=[
 ('version drift','app/build.gradle.kts','releaseVersionName = "1.4.1"','releaseVersionName = "9.9.9"','verify_build_contract.py'),
 ('clang unused EGL constant regression','app/src/main/cpp/openglesscope.cpp','static constexpr EGLint EGL_PROTECTED_CONTENT_EXT_VALUE = 0x32C0;','static constexpr EGLint EGL_BUFFER_AGE_KHR_VALUE = 0x313D;\nstatic constexpr EGLint EGL_PROTECTED_CONTENT_EXT_VALUE = 0x32C0;','verify_1_4_1_compile_hotfix.py'),
 ('nullable evidence regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','CustomMinimumResult(rule, evidence, if(pass)"PASS" else "FAIL")','CustomMinimumResult(rule, raw, if(pass)"PASS" else "FAIL")','verify_1_4_1_compile_hotfix.py'),
 ('LazyListScope remember regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','val filtered = rawTechnicalEntries.entries.filter','val technical = remember(report, display) { submissionJson(context, report, display).getJSONObject("technicalReport") }; val filtered = rawTechnicalEntries.entries.filter','verify_1_4_1_compile_hotfix.py'),
]

def clone_tree(target: Path) -> Path:
    clone=target/'tree'
    shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
    return clone

def write_cow(path: Path, data: str) -> None:
    path.unlink(); path.write_text(data,encoding='utf-8')

def run_verifier(root: Path, verifier: str) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/verifier)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def verify():
    for name,rel,old,new,verifier in CASES:
        with tempfile.TemporaryDirectory(prefix='ogles141-mut-',dir=ROOT.parent) as td:
            clone=clone_tree(Path(td)); path=clone/rel; data=path.read_text(encoding='utf-8')
            require(old in data,f'negative mutation fixture missing for {name}')
            write_cow(path,data.replace(old,new,1))
            require(run_verifier(clone,verifier)!=0,f'negative mutation was not rejected: {name}')
    with tempfile.TemporaryDirectory(prefix='ogles141-fp-',dir=ROOT.parent) as td:
        clone=clone_tree(Path(td)); changelog=clone/'changelog.md'; write_cow(changelog,changelog.read_text(encoding='utf-8')+'\n')
        for verifier in ['verify_1_4_1_compile_hotfix.py','verify_build_contract.py','verify_security_contracts.py']:
            require(run_verifier(clone,verifier)==0,'unrelated false-positive control was rejected: '+verifier)
if __name__=='__main__': main_guard('test_1_4_1_negative_mutations',verify)
