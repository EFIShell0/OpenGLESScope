#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root):
 env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
 return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_5_1_compile_hotfix.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def verify():
 with tempfile.TemporaryDirectory(prefix='ogles151-mut-',dir=ROOT.parent) as td:
  clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
  p=clone/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'; data=p.read_text(encoding='utf-8')
  good='${c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }}")'
  bad='${c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None"}")'
  require(good in data,'mutation fixture missing'); p.unlink(); p.write_text(data.replace(good,bad,1),encoding='utf-8')
  require(run(clone)!=0,'missing interpolation brace mutation was not rejected')
 with tempfile.TemporaryDirectory(prefix='ogles151-fp-',dir=ROOT.parent) as td:
  clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
  p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
  require(run(clone)==0,'unrelated changelog-only mutation rejected')
if __name__=='__main__': main_guard('test_1_5_1_negative_mutations',verify)
