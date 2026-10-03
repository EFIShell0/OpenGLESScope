#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def run(root,script):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(root);env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/script)],cwd=root,text=True,capture_output=True,env=env).returncode
def verify():
    ui='verify_2_2_9_reference_ui.py';hashgate='verify_2_2_9_release_contract.py'
    for gate in (ui,hashgate):require(run(ROOT,gate)==0,'current baseline failed '+gate)
    mutations=[
        (MAIN,'Box(Modifier.fillMaxSize().background(ComposeColor.Black).zIndex(50f))','Box(Modifier.fillMaxSize().zIndex(50f))',ui,'browser background'),
        (MAIN,'color = ComposeColor.Black,\n                contentColor = TextPrimary,','color = SurfaceDark,\n                contentColor = TextPrimary,',ui,'purple browser root'),
        (MAIN,'ExpressiveContainedIconTextButton("Open Khronos specification", R.drawable.ic_open_external,','ExpressiveActionButton("Open Khronos specification", R.drawable.ic_open_external,',ui,'external action shape'),
        (MAIN,'enabled = LocalValidatedNetwork.current) { uriHandler.openUri(url) }','enabled = true) { uriHandler.openUri(url) }',ui,'offline link action gate'),
        (MAIN,'onSizeChanged { bottomNavigationHeightPx = it.height }','.onSizeChanged { }',ui,'measured bottom navigation'),
        (MAIN,'bottom = bottomNavigationContentInset + 4.dp','bottom = 0.dp',ui,'bottom arrow clearance'),
        (MAIN,'bottom = bottomNavigationInset + 4.dp','bottom = 0.dp',ui,'OpenGL section arrow clearance'),
        (MAIN,'tint = ComposeColor.White\n                                    )','tint = BrandSoft\n                                    )',ui,'neutral EGL glyph'),
        (MAIN,'painter = painterResource(R.drawable.openglesscope_logo_horizontal_aligned)','painter = painterResource(R.drawable.openglesscope_logo_horizontal)',ui,'header logo artwork'),
        ('app/src/main/res/drawable-nodpi/openglesscope_logo_horizontal_aligned.png',None,None,hashgate,'aligned PNG immutable hash'),
        ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',hashgate,'immutable EGL native path'),
    ]
    for rel,old,new,gate,label in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles229-mut-') as d:
            clone=Path(d);shutil.copytree(ROOT,clone,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__'))
            p=clone/rel
            if old is None:
                buf=bytearray(p.read_bytes());buf[-12]^=0x01;p.write_bytes(buf)
            else:
                src=p.read_text();require(old in src,'stale mutation fixture: '+label);p.write_text(src.replace(old,new,1))
            require(run(clone,gate)!=0,'negative mutation was falsely accepted: '+label)
if __name__=='__main__':main_guard('test_2_2_9_negative_mutations',verify)
