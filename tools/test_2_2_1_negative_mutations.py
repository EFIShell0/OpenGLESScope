#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
VER=ROOT/'tools/verify_2_2_1_kotlin_compile_hotfix.py'
def run(root):
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(VER)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode
def mutate(rel,old,new,label):
    with tempfile.TemporaryDirectory(prefix='ogles221-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(clone)!=0,label+' mutation was not rejected')
def verify():
    require(run(ROOT)==0,'baseline 2.2.1 compile verifier does not pass')
    main='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
    mutate('app/build.gradle.kts','releaseVersionCode = 2201','releaseVersionCode = 2202','release identity')
    mutate(main,'import androidx.compose.ui.graphics.layer.drawLayer','import androidx.compose.ui.graphics.drawscope.drawLayer','wrong drawLayer package')
    mutate(main,'import androidx.compose.animation.core.animateDpAsState\n','', 'missing animateDpAsState import')
    mutate(main,'import androidx.compose.foundation.layout.FlowRow\n','', 'missing FlowRow import')
    mutate(main,'private fun ExpressiveScrollHints(scrollState: ScrollState','private fun ExpressiveScrollHints(scrollState: LazyListState','ScrollState overload regression')
    mutate(main,'private fun ExpressiveToggleRow(','private fun MissingExpressiveToggleRow(','missing ExpressiveToggleRow')
    mutate(main,'private fun ExpressiveMetric(','private fun MissingExpressiveMetric(','missing ExpressiveMetric')
    mutate('rules/PROJECT_RULES.md','## Release 2.2.1 Kotlin/Compose parity compile-correctness hotfix','## Release 2.2.1 weakened compile hotfix','rules compile contract')
if __name__=='__main__': main_guard('test_2_2_1_negative_mutations',verify)
