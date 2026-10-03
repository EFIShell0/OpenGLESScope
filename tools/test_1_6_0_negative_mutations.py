#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

def run(root: Path):
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_6_0_quality.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(rel, old, new):
    with tempfile.TemporaryDirectory(prefix='ogles160-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+old); p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8')
        require(run(clone)!=0,'mutation was not rejected: '+old)

def verify():
    mutate('app/build.gradle.kts','releaseVersionCode = 1600','releaseVersionCode = 1601')
    mutate('app/src/main/AndroidManifest.xml','<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />','')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','MAX_UPDATE_REDIRECTS = 3','MAX_UPDATE_REDIRECTS = 2')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','packageSigningCertificatesMatch(installed, archive)','true')
    with tempfile.TemporaryDirectory(prefix='ogles160-fp-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
        require(run(clone)==0,'unrelated changelog mutation rejected')
if __name__=='__main__': main_guard('test_1_6_0_negative_mutations',verify)
