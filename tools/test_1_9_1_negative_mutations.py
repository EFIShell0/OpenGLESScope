#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root):
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_9_1_compile_hotfix.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(old,new,label):
    with tempfile.TemporaryDirectory(prefix='ogles191-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(clone)!=0,label+' mutation was not rejected')

def verify():
    mutate('    val collecting = collectionStatus == CollectionStatus.COLLECTING\n','', 'collecting declaration removal')
    mutate('import androidx.compose.ui.semantics.role\n','', 'semantics role import removal')
    with tempfile.TemporaryDirectory(prefix='ogles191-fp-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
        require(run(clone)==0,'unrelated changelog-only mutation rejected')
if __name__=='__main__': main_guard('test_1_9_1_negative_mutations',verify)
