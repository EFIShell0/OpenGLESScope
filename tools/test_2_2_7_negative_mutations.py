#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_7_reference_ui.py';HASH='verify_2_2_7_regression_contract.py';OLD='verify_2_2_6_compile_and_parity.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,text=True,capture_output=True,env=env).returncode
def verify():
    for script in (UI,HASH,OLD):require(run(ROOT,script)==0,'2.2.7 real source baseline failed: '+script)
    mutations=[
        ('app/build.gradle.kts','releaseVersionCode = 2208','releaseVersionCode = 9999',UI,'true version identity'),
        (MAIN,'ColorFilter.tint(BrandSoft)','ColorFilter.tint(ComposeColor.Red)',UI,'EGL official-artwork theme tint'),
        ('app/src/main/res/drawable/ic_android_brand.xml','android:fillColor="#F06BC7"','android:fillColor="#E2676A"',UI,'android theme tint'),
        (MAIN,'TransientActionButton("Copy name + value", "Copy exact extension and runtime evidence"','TransientActionButton("Copy extension", "Copy exact extension and runtime evidence"',UI,'extension evidence actions parity'),
        (MAIN,'DetailEvidenceRow("Runtime evidence", "Exact token enumerated in the current $scopeName")','CapabilityKeyValue("Runtime evidence", "Exact token enumerated in the current $scopeName")',UI,'extension detail static card regression'),
        (MAIN,'Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { TransientActionButton("Add to watch list"','Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TransientActionButton("Add to watch list"',UI,'watched action unbounded width'),
        (MAIN,'DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)','DialogProperties(usePlatformDefaultWidth = true, dismissOnBackPress = false, dismissOnClickOutside = false)',UI,'full-screen dialog width'),
        (MAIN,'val landscapeLayout = maxWidth > maxHeight && maxWidth >= 700.dp','val landscapeLayout = false',UI,'landscape reference layout'),
        (MAIN,'FileManagerViewMode.DENSE_GRID -> 92.dp','FileManagerViewMode.DENSE_GRID -> 100.dp',UI,'dense grid reference dimensions'),
        (MAIN,'bottomNavigationContentInset + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp','bottomNavigationContentInset + 4.dp',UI,'bottom scroll arrow navigation overlap'),
        (MAIN,'BackHandler(enabled = !busy) {','BackHandler(enabled = false) {',UI,'D-pad TV dialog back handling'),
        (MAIN,'import androidx.compose.animation.expandHorizontally','// import removed',UI,'reintroduced Kotlin unresolved animation import'),
        (MAIN,'import androidx.compose.runtime.rememberUpdatedState','// import removed',OLD,'regressed real 2.2.6 Kotlin compile fix'),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'unaudited GL/EGL probe source drift'),
    ]
    for rel,old,new,gate,label in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles227-mutate-') as t:
            d=Path(t);shutil.copytree(ROOT,d,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','__pycache__','.gradle','build'))
            f=d/rel;text=f.read_text(encoding='utf-8');require(old in text,'stale negative-mutation fixture: '+label)
            f.write_text(text.replace(old,new,1),encoding='utf-8')
            require(run(d,gate)!=0,'negative mutation wrongly passes: '+label)
if __name__=='__main__': main_guard('test_2_2_7_negative_mutations',verify)
