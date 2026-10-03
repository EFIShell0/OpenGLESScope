#!/usr/bin/env python3
from __future__ import annotations
import os,shutil,subprocess,sys,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard

def run(root: Path) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools/verify_1_9_3_spec_reporting.py')],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def mutate(rel: str, old: str, new: str, label: str):
    with tempfile.TemporaryDirectory(prefix='ogles193-mut-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/rel; data=p.read_text(encoding='utf-8'); require(old in data,'mutation fixture missing: '+label)
        p.unlink(); p.write_text(data.replace(old,new,1),encoding='utf-8')
        require(run(clone)!=0,label+' mutation was not rejected')

def verify():
    mutate('app/src/main/cpp/openglesscope.cpp','querySurfaceAttr(d, s, EGL_CONFIG_ID)','querySurfaceAttr(d, s, EGL_WIDTH)','EGL surface query loss')
    mutate('app/src/main/cpp/openglesscope.cpp','surfaceLargestPbuffer.value == EGL_TRUE ? "true" : "false"','std::to_string(surfaceLargestPbuffer.value)','EGL boolean misreport')
    mutate('app/src/main/cpp/openglesscope.cpp','const bool contextFlagsApplicable = glCode >= 320;','const bool contextFlagsApplicable = glCode >= 300;','GL_CONTEXT_FLAGS wrong ES 3.0 applicability')
    mutate('app/src/main/cpp/openglesscope.cpp','hasExt(glExt, "GL_EXT_robustness")','hasExt(glExt, "GL_EXT_robustness_REMOVED")','GL_EXT_robustness path loss')
    mutate('app/src/main/cpp/openglesscope.cpp','const bool surfaceVgApplicable = eglCode >= 120;','const bool surfaceVgApplicable = eglCode >= 130;','EGL 1.2 VG alias applicability loss')
    mutate('app/src/main/cpp/openglesscope.cpp','EGL_ALPHA_FORMAT (EGL 1.2 alias of EGL_VG_ALPHA_FORMAT)','EGL_VG_ALPHA_FORMAT (provenance removed)','EGL 1.2 VG alias query provenance loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','put("egl-runtime/surfaceVgAlphaFormatQuery", report.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable")','put("egl-runtime/surfaceVgAlphaFormatQuery_REMOVED", report.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable")','EGL query provenance report loss')
    mutate('app/src/main/cpp/openglesscope.cpp','EGL_GL_COLORSPACE_BT2020_PQ_EXT (0x3340)','EGL_GL_COLORSPACE_BT2020_PQ_REMOVED (0x3340)','EGL colorspace symbolic detail loss')
    mutate('app/src/main/cpp/openglesscope.cpp','glResetStrategyDisplay(GLint value, const char* queryName)','glResetStrategyDisplay(GLint value, const char* ignoredQueryName)','reset strategy provenance-aware label loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','private fun runtimeQueryEvidence(r: GlReport','private fun removedRuntimeQueryEvidence(r: GlReport','query state distinction loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','val technicalQueryDiagnostics = tech.optJSONArray("queryDiagnostics")','val technicalQueryDiagnostics = tech.optJSONArray("queryDiagnostics_REMOVED")','Database query-state provenance loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','put("gl-runtime/robustAccessQuery", report.glRuntime.robustAccessQuery ?: "Not applicable")','put("gl-runtime/robustAccessQuery_REMOVED", report.glRuntime.robustAccessQuery ?: "Not applicable")','robust access provenance report loss')
    mutate('app/src/main/java/com/efishell/openglesscope/MainActivity.kt','tech.optJSONObject("glRuntime")?.let { g ->','tech.optJSONObject("glRuntime_REMOVED")?.let { g ->','Database GL runtime analysis loss')
    mutate('registry/gl_registry_manifest.json','"verifiedAt": "2026-09-30"','"verifiedAt": "2026-09-29"','stale registry verification date')
    with tempfile.TemporaryDirectory(prefix='ogles193-fp-',dir=ROOT.parent) as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
        p=clone/'changelog.md'; p.unlink(); p.write_text((ROOT/'changelog.md').read_text(encoding='utf-8')+'\n',encoding='utf-8')
        require(run(clone)==0,'unrelated changelog-only mutation rejected')
if __name__=='__main__': main_guard('test_1_9_3_negative_mutations',verify)
