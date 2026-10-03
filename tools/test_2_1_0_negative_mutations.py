#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

SCRIPT='verify_2_1_0_quality.py'
def run(root: Path) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/SCRIPT)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def clone_tree(prefix='ogles210-mut-'):
    td=tempfile.TemporaryDirectory(prefix=prefix,dir=ROOT.parent); dst=Path(td.name)/'tree'
    shutil.copytree(ROOT,dst,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
    return td,dst

def mutate(rel,old,new,label):
    td,clone=clone_tree()
    try:
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8')
        require(run(clone)!=0,label+' mutation was not rejected')
    finally: td.cleanup()

def verify():
    require(run(ROOT)==0,'baseline 2.1.0 verifier does not pass')
    mutate('app/build.gradle.kts','releaseVersionCode = 2100','releaseVersionCode = 2101','release identity drift')
    mutate('rules/PROJECT_RULES.md','# OpenGLESScope Engineering Rules','# OpenGLESScope Engineering Rules\n\n# OpenGLESScope Engineering Rules','duplicate rules root')
    mutate('app/src/main/cpp/openglesscope.cpp','std::binary_search(extensions.begin(), extensions.end(), std::string(name))','std::find(extensions.begin(), extensions.end(), std::string(name)) != extensions.end()','linear extension lookup regression')
    mutate('app/src/main/cpp/openglesscope.cpp','GL_EXT_fragment_shading_rate','GL_EXT_fragment_shading_rate_attachment','fabricated extension name')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','private const val COLLECTION_PAGE_SIZE = 25','private const val COLLECTION_PAGE_SIZE = 50','paging drift')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','FileManagerOptionsChooser(','FileManagerOptionsDialog(','legacy File Manager chooser regression')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 250','private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 25000','unbounded Encyclopedia result regression')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','tvRemoteLazyGridNavigation(gridState)','tvRemoteLazyGridNavigationRemoved(gridState)','TV grid navigation regression')
    td,clone=clone_tree()
    try:
        icon=clone/'app/src/main/res/drawable/ic_check_updates.xml'; data=icon.read_text(encoding='utf-8'); icon.unlink(); icon.write_text(data+'\n<!-- mutation -->\n',encoding='utf-8')
        require(run(clone)!=0,'common semantic icon drift was not rejected')
    finally: td.cleanup()

if __name__=='__main__': main_guard('test_2_1_0_negative_mutations',verify)
