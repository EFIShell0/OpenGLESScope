#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

def run(root: Path, script: str) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def clone_tree(prefix: str):
    td=tempfile.TemporaryDirectory(prefix=prefix,dir=ROOT.parent)
    dst=Path(td.name)/'tree'
    shutil.copytree(ROOT,dst,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
    return td,dst

def mutate(rel: str, old: str, new: str, script: str, label: str):
    td,clone=clone_tree('ogles200-mut-')
    try:
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8')
        require(run(clone,script)!=0,label+' mutation was not rejected')
    finally: td.cleanup()

def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    regression_script='verify_2_1_0_regression_contract.py' if 'releaseVersionName = "2.1.0"' in gradle else 'verify_2_0_0_regression_contract.py'
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','private const val COLLECTION_PAGE_SIZE = 25','private const val COLLECTION_PAGE_SIZE = 50','verify_2_0_0_parity.py','paging-size drift')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','        enableEdgeToEdge()','        // enableEdgeToEdge removed','verify_2_0_0_parity.py','edge-to-edge removal')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','opening_animation_enabled','opening_animation_removed','verify_2_0_0_parity.py','opening preference loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','Downloaded package size does not match the official GitHub release metadata.','Downloaded package accepted despite metadata mismatch.','verify_2_0_0_parity.py','update final-size fail-open')
    mutate('registry/egl.xml','<registry>','<registry><!-- mutation -->','verify_2_0_0_parity.py','EGL registry lock drift')
    mutate('rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md','## Release 3.0.12 Turnip folder-scan Kotlin compile restoration requirements','## Release 3.0.12 mutated requirements','verify_2_0_0_parity.py','methodology reference drift')
    mutate('app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt','package com.efishell.openglesscope','package com.efishell.openglesscope\n// unauthorized production mutation',regression_script,'non-allowlisted production drift')
    td,clone=clone_tree('ogles200-extra-')
    try:
        extra=clone/'app/src/main/res/drawable/unauthorized_extra.xml'; extra.write_text('<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="1dp" android:height="1dp" android:viewportWidth="1" android:viewportHeight="1"/>',encoding='utf-8')
        require(run(clone,regression_script)!=0,'unauthorized production file was not rejected')
    finally: td.cleanup()
    td,clone=clone_tree('ogles200-fp-')
    try:
        p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
        require(run(clone,'verify_2_0_0_parity.py')==0,'unrelated changelog-only mutation rejected by parity verifier')
        require(run(clone,regression_script)==0,'unrelated changelog-only mutation rejected by production regression verifier')
    finally: td.cleanup()

if __name__=='__main__': main_guard('test_2_0_0_negative_mutations',verify)
