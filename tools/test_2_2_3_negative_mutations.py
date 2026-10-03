#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
UI='verify_2_2_3_full_ui.py'; CONTRACT='verify_2_2_4_regression_contract.py';MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def gate(path,tool):
    env=os.environ.copy();env['OPENGLESSCOPE_ROOT']=str(path);env['PYTHONDONTWRITEBYTECODE']='1'
    result=subprocess.run([sys.executable,'-B',str(ROOT/'tools'/tool)],cwd=path,env=env,capture_output=True,text=True)
    return result.returncode

def verify():
    require(gate(ROOT,UI)==0 and gate(ROOT,CONTRACT)==0,'clean baseline gate failed')
    cases=[
       ('app/build.gradle.kts','releaseVersionCode = 2204','releaseVersionCode = 2202',UI,'version identity'),
       (MAIN,'painter = painterResource(R.drawable.openglesscope_logo_horizontal),\n                    contentDescription = \"OpenGLESScope\"','painter = painterResource(R.drawable.openglesscope_scope_wordmark),\n                    contentDescription = \"SCOPE\"',UI,'header logo'),
       (MAIN,'    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(trademarkApiDisplayText(label), text))','    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(trademarkApiDisplayText(label), text)); android.widget.Toast.makeText(context, "Copied", android.widget.Toast.LENGTH_SHORT).show()',UI,'clipboard toast'),
       (MAIN,'Vendor logos are decorative, not evidence of capabilities.','Vulkan driver tools and Turnip details.',UI,'foreign product copy'),
       (MAIN,'HdrCapabilitiesCarousel(d.hdrTypes)','Row { d.hdrTypes.forEach { HdrTypeCard(it) } }',UI,'HDR carousel'),
       (MAIN,'icon == R.drawable.ic_egl_official -> EglBrandArtwork(modifier, contentDescription)','icon == R.drawable.ic_egl_official -> Icon(painterResource(icon), null)',UI,'EGL badge'),
       (MAIN,'Icon(painter = painterResource(R.drawable.ic_opening_animation_toggle), contentDescription = null, tint = BrandSoft, modifier = Modifier.size(22.dp))','Image(painter = painterResource(R.drawable.ic_opening_animation_toggle), contentDescription = null)',UI,'opening icon'),
       (MAIN,'CapabilityKeyValue(ext, "ENUMERATED · $scopeName")','Text(ext)',UI,'extension long press'),
       (MAIN,'key.length > 28 || value.length > 52','false',UI,'long token wrapping'),
       (MAIN,'CapabilityKeyValue("Implemented query gates"','DetailEvidenceRow("Implemented query gates"',UI,'extension inner evidence'),
       ('app/src/main/cpp/openglesscope.cpp','EGL_NONE','EGL_FALSE',CONTRACT,'unreviewed native drift')
    ]
    for rel,old,new,script,label in cases:
        with tempfile.TemporaryDirectory(prefix='ogles-223-negative-') as temp:
            dest=Path(temp)
            shutil.copytree(ROOT,dest,dirs_exist_ok=True,ignore=shutil.ignore_patterns('__pycache__','.gradle','build'))
            file=dest/rel;content=file.read_text(encoding='utf-8')
            require(old in content,'stale mutation fixture: '+label)
            file.write_text(content.replace(old,new,1),encoding='utf-8')
            require(gate(dest,script)!=0,'deliberate defect escaped verifier: '+label)
if __name__=='__main__':main_guard('test_2_2_3_negative_mutations',verify)
