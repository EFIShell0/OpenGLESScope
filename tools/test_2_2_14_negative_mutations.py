#!/usr/bin/env python3
"""Independent source mutations must trip the new immutable UI and safe-data census."""
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
CHECK=ROOT/'tools/verify_2_2_14_update_database_parity.py'; MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def run(root):
 env=os.environ.copy();env.update(OPENGLESSCOPE_ROOT=str(root),PYTHONDONTWRITEBYTECODE='1')
 return subprocess.run([sys.executable,'-B',str(CHECK)],cwd=root,env=env,text=True,capture_output=True).returncode
def verify():
 require(run(ROOT)==0,'unmodified current source fails 2.2.14 oracle')
 mutations=[
 (MAIN,'height(14.dp)','height(3.dp)','VulkanScope progress thickness'),
 (MAIN,'progress.coerceIn(0f, 1f)','progress','bounded progress'),
 (MAIN,'tween(180, easing = FastOutSlowInEasing)','tween(0, easing = FastOutSlowInEasing)','animated percentage'),
 (MAIN,'ExpressiveDeterminateDownloadProgress(progressFraction, Modifier.fillMaxWidth())','ExpressiveLinearProgressIndicator(Modifier.fillMaxWidth())','determinate rather than indefinite transfer'),
 (MAIN,'"Download speed", formatUpdateSpeedDisplay(state.bytesPerSecond)','"Download speed", "—"','live speed'),
 (MAIN,'desktopVerticalPointerScroll(logState).tvRemoteLazyListNavigation(logState).focusable().focusGroup()','focusGroup()','mouse/TV navigation'),
 (MAIN,'databaseNextCursor != null && databaseRows.size < ANALYSIS_DATABASE_LIST_MAX','true','bounded next-page gate'),
 (MAIN,'DatabaseReportVendorBadge(row.vendor, row.gpuName)','VendorLogo("", "")','metadata badge not generic'),
 (MAIN,'"angle", "swiftshader", "mesa"','"angle", "swiftshader"','software/translation layer fallback'),
 (MAIN,'"Load more", R.drawable.ic_download','"Load more", R.drawable.ic_expand_more','list action icon'),
 ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999','producer identity'),
 ('app/src/main/AndroidManifest.xml','android.permission.INTERNET','android.permission.SYSTEM_ALERT_WINDOW','unrelated permission change'),
 ]
 for path,needle,repl,label in mutations:
  with tempfile.TemporaryDirectory(prefix='ogles2214-neg-') as td:
   c=Path(td);shutil.copytree(ROOT,c,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
   p=c/path;s=p.read_text();require(needle in s,'stale mutation fixture: '+label);p.write_text(s.replace(needle,repl,1))
   require(run(c)!=0,'not rejected: '+label)
 print(f'2.2.14 negative mutations: {len(mutations)}/{len(mutations)} correctly rejected')
if __name__=='__main__':main_guard('test_2_2_14_negative_mutations',verify)
