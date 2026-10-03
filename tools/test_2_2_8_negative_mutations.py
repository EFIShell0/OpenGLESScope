#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
UI='verify_2_2_8_reference_search_blur.py'; HASH='verify_2_2_9_release_contract.py'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,text=True,capture_output=True,env=env).returncode
def verify():
    for gate in (UI,HASH):require(run(ROOT,gate)==0,'current release baseline does not pass '+gate)
    mutations=[
        ('app/build.gradle.kts','releaseVersionCode = 2214','releaseVersionCode = 9999',UI,'producer identity'),
        ('app/build.gradle.kts','minorApiLevel = 2','minorApiLevel = 1',UI,'SDK 37.2 minor gate'),
        ('app/build.gradle.kts','targetSdk = 37','targetSdk = 36',UI,'target SDK 37 contract'),
        (MAIN,'painter = painterResource(R.drawable.openglesscope_logo_foreground)','painter = painterResource(R.drawable.openglesscope_scope_wordmark)',UI,'version badge original full logo'),
        (MAIN,'contentScale = ContentScale.Fit,\n                        modifier = Modifier.size(46.dp)','contentScale = ContentScale.Fit,\n                        modifier = Modifier.size(62.dp)',UI,'version badge reference dimensions'),
        (MAIN,'chromeSourceLayer.record { this@drawWithContent.drawContent() }','drawContent()',UI,'actually recorded global fallback'),
        (MAIN,'chromeBlurredLayer.record { drawLayer(chromeSourceLayer) }','chromeBlurredLayer.record { }',UI,'nonempty global blur layer'),
        (MAIN,'val chromeBlurRadiusPx = with(appDensity) { 24.dp.toPx() }','val chromeBlurRadiusPx = with(appDensity) { 8.dp.toPx() }',UI,'24dp blur geometry'),
        (MAIN,'if (pageChromeBackdrop?.layer === layer) pageChromeBackdrop = null','pageChromeBackdrop = null',UI,'outgoing page stale blur race'),
        (MAIN,'ChromeBackdropSource(chromeBlurredLayer, Offset.Zero)','ChromeBackdropSource(chromeSourceLayer, Offset.Zero)',UI,'reference fallback assigned to both chrome bars'),
        (MAIN,'catalog = withContext(Dispatchers.IO) { RegistryCatalog.load(context) }','catalog = RegistryCatalog.load(context)',UI,'off-main catalog work'),
        (MAIN,'catch (cancelled: CancellationException) {\n            throw cancelled','catch (cancelled: CancellationException) {\n            catalog = emptyList()',UI,'cancellation propagation'),
        (MAIN,'val safePage = page.coerceIn(0, pageCount - 1)','val safePage = page',UI,'reduced-result page bounds'),
        (MAIN,'onValueChange = { query = it.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = 0 }','onValueChange = { query = it.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH) }',UI,'filter state stale page'),
        (MAIN,'metrics.chunked(columns)','metrics.chunked(1)',UI,'responsive card structure'),
        (MAIN,'"${BuildConfig.COMPILE_SDK_LEVEL}.${BuildConfig.COMPILE_SDK_MINOR_LEVEL}"','"${BuildConfig.COMPILE_SDK_LEVEL}"',UI,'accurate SDK Info'),
        (MAIN,'"Encyclopedia unavailable"','"Turnip Encyclopedia unavailable"',UI,'foreign product UI copy'),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',HASH,'native probe immutable provenance'),
    ]
    for rel,old,new,gate,label in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles228-mutate-') as d:
            clone=Path(d);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','__pycache__','.gradle','build'))
            p=clone/rel;value=p.read_text(encoding='utf-8');require(old in value,'negative fixture stale: '+label)
            p.write_text(value.replace(old,new,1),encoding='utf-8')
            require(run(clone,gate)!=0,'negative mutation falsely accepted: '+label)
if __name__=='__main__':main_guard('test_2_2_8_negative_mutations',verify)
