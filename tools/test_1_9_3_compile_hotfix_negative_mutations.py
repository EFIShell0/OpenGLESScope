#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root):
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_9_3_compile_hotfix.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(old,new,label):
    with tempfile.TemporaryDirectory(prefix='ogles193-hotfix-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(clone)!=0,label+' mutation was not rejected')

def verify():
    mutate('e.optNullableString("surfaceGlColorspaceQuery")','runtime.optNullableString("surfaceGlColorspaceQuery")','GL colorspace query receiver regression')
    mutate('e.optNullableString("surfaceVgAlphaFormatQuery")','runtime.optNullableString("surfaceVgAlphaFormatQuery")','VG alpha query receiver regression')
    mutate('e.optNullableString("surfaceVgColorspaceQuery")','runtime.optNullableString("surfaceVgColorspaceQuery")','VG colorspace query receiver regression')
if __name__=='__main__': main_guard('test_1_9_3_compile_hotfix_negative_mutations',verify)
