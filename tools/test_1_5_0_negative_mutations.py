#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard
CASES=[
 ('stale schema label','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','"$SUBMISSION_SCHEMA_VERSION · technical report $TECHNICAL_REPORT_SCHEMA_VERSION"','"2 · technical report 2"'),
 ('prerelease admitted','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','!it.optBoolean("draft", false) && !it.optBoolean("prerelease", false)','!it.optBoolean("draft", false)'),
 ('automatic redirects re-enabled','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','.followRedirects(false)','.followRedirects(true)'),
 ('redirect host broadened','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','"release-assets.githubusercontent.com", "objects.githubusercontent.com" -> true','else -> true'),
 ('digest verification removed','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','val actual = sha256FileHex(temp)','val actual = expected'),
 ('TXT config evidence dropped','app/src/main/java/com/efishell/openglesscope/MainActivity.kt',' | recordableAndroid ${c.recordableAndroid?.let { eglBooleanLabel(it) } ?: "N/A"}',''),
 ('HTML EGL runtime evidence dropped','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','"Pbuffer mipmap level" to (r.eglRuntime.surfaceMipmapLevel?.toString() ?: "Unavailable"),',''),
]
def run(root):
 env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'
 return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_5_0_quality.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def verify():
 for name,rel,old,new in CASES:
  with tempfile.TemporaryDirectory(prefix='ogles150-mut-',dir=ROOT.parent) as td:
   clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
   p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+name); p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8')
   require(run(clone)!=0,'negative mutation not rejected: '+name)
 with tempfile.TemporaryDirectory(prefix='ogles150-fp-',dir=ROOT.parent) as td:
  clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
  p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
  require(run(clone)==0,'unrelated changelog-only mutation rejected')
if __name__=='__main__': main_guard('test_1_5_0_negative_mutations',verify)
