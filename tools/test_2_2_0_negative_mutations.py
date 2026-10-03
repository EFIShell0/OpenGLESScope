#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root:Path)->int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_2_2_0_ui_parity.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(rel:str,old:str,new:str,label:str):
    with tempfile.TemporaryDirectory(prefix='ogles220-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8'); require(run(clone)!=0,label+' mutation was not rejected')

def verify():
    require(run(ROOT)==0,'baseline 2.2.0 UI parity verifier does not pass')
    main='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
    mutate('app/build.gradle.kts','releaseVersionCode = 2200','releaseVersionCode = 2201','release identity')
    mutate(main,'painter = painterResource(R.drawable.openglesscope_scope_wordmark)','painter = painterResource(R.drawable.openglesscope_logo_horizontal)','header SCOPE branding')
    mutate(main,'NavigationItem(Page.Display, "Display", R.drawable.ic_tablet)','NavigationItem(Page.EGL, "EGL™", R.drawable.ic_egl_official)','EGL primary navigation')
    mutate(main,'R.drawable.ic_download, enabled = activity.directUpdatesEnabled','R.drawable.ic_check_updates, enabled = activity.directUpdatesEnabled','update semantic icon')
    mutate(main,'AndroidRenderEffect.createBlurEffect','AndroidRenderEffect.createOffsetEffect','live blur')
    mutate(main,'onLongPress = { showActions = true }','onLongPress = { }','long-press evidence actions')
    mutate(main,'.consumeDesktopSecondaryMouseInput(suppressDesktopQuickMenu)','.consumeDesktopSecondaryMouseInput(false)','desktop secondary evidence menu')
    mutate(main,'modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f)','modifier = Modifier','evidence dialog accent border')
    mutate(main,'NavigationItem(Page.OpenGLES, "OpenGL® ES"','NavigationItem(Page.OpenGLES, "OpenGL ES"','official OpenGL mark')
    mutate(main,'DatabaseSubmittedAt(row.submittedAt)','CapabilityKeyValue("Submitted", row.submittedAt)','Database submitted-time presentation')
    mutate(main,'ExpressiveFilterBar(listOf("Database list", "Report ID")','ExpressiveFilterBar(listOf("Reports")','Database lookup mode parity')
    mutate(main,'title.equals("Session History", true) || title.equals("Local session history", true) -> R.drawable.ic_history','title.equals("Session History", true) -> R.drawable.ic_history','session history semantic icon parity')
    mutate(main,'var submissionSuccessId by remember','var ignoredSubmissionSuccessId by remember','Database success report-id UI')
    mutate('rules/PROJECT_RULES.md','## Release 2.2.0 VulkanScope 3.0.12 visual/interaction parity contract','## Release 2.2.0 weakened parity contract','rules parity section')

if __name__=='__main__': main_guard('test_2_2_0_negative_mutations',verify)
