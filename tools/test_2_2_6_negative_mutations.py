#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_6_compile_and_parity.py'
HASH='verify_2_2_6_regression_contract.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    result=subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,env=env,text=True,capture_output=True)
    return result.returncode
def verify():
    require(run(ROOT,UI)==0 and run(ROOT,HASH)==0,'2.2.6 baseline static and hash gates failed')
    cases=[
        ('app/build.gradle.kts','releaseVersionCode = 2206','releaseVersionCode = 9999',UI,'release version regression'),
        (MAIN,'import androidx.compose.runtime.rememberUpdatedState\n','',UI,'missing Compose import'),
        (MAIN,'"Load more reports", R.drawable.ic_expand_more,','"Load more reports", R.drawable.ic_chevron_down,',UI,'undefined drawable resource'),
        (MAIN,'val databaseComparisonRows = remember(databaseBaseline, currentSnapshot, includeUnchanged)', 'val dbRows = remember(databaseBaseline, currentSnapshot, includeUnchanged)',UI,'remember snapshot out of legal scope'),
        (MAIN,'@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun DatabaseSubmissionStateCard(', '@Composable\nprivate fun DatabaseSubmissionStateCard(',UI,'experimental Material3 opt-in'),
        (MAIN,'if (nextId != databaseId) {','if (false) {',UI,'editing report ID retains wrong comparison'),
        (MAIN,'catch (error: Throwable) { if (requestedId == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }','catch (error: Throwable) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" }',UI,'stale exact Analysis failure'),
        (MAIN,'catch (error: Throwable) { if (row.id == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }','catch (error: Throwable) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" }',UI,'stale selected-row Analysis failure'),
        (MAIN,'initialPagerHeightPx = with(density) { 72.dp.roundToPx() }','initialPagerHeightPx = with(density) { 56.dp.roundToPx() }',UI,'shared VulkanScope pager drift'),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'unreviewed native GL/EGL drift'),
    ]
    for rel,old,new,gate,label in cases:
        with tempfile.TemporaryDirectory(prefix='ogles226-mut-') as tmp:
            dest=Path(tmp)
            shutil.copytree(ROOT,dest,dirs_exist_ok=True,ignore=shutil.ignore_patterns('__pycache__','.gradle','build'))
            path=dest/rel
            before=path.read_text();require(old in before,'stale negative-mutation fixture: '+label)
            path.write_text(before.replace(old,new,1))
            require(run(dest,gate)!=0,'negative mutation unexpectedly passed: '+label)
if __name__=='__main__': main_guard('test_2_2_6_negative_mutations',verify)
