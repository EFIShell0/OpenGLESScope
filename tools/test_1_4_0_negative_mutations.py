#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

CASES=[
 ('version drift','app/build.gradle.kts','releaseVersionName = "1.4.0"','releaseVersionName = "9.9.9"','verify_build_contract.py'),
 ('fabricated GL name','app/src/main/cpp/openglesscope.cpp','GL_COMPRESSED_LUMINANCE_LATC1_EXT','GL_COMPRESSED_LUMINANCE_LATC1_NV','verify_registry_canonical_names.py'),
 ('core query loss','app/src/main/cpp/openglesscope.cpp','addLimit(o, first, "GL_RED_BITS", GL_RED_BITS);','', 'verify_query_coverage.py'),
 ('terminal handshake loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt','const val EXTRA_TERMINAL_PATH = "terminal_path"','const val EXTRA_TERMINAL_PATH_BROKEN = "terminal_path"','verify_probe_lifecycle.py'),
 ('cleartext enabled','app/src/main/AndroidManifest.xml','android:usesCleartextTraffic="false"','android:usesCleartextTraffic="true"','verify_security_contracts.py'),
 ('database origin drift','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','https://openglesscope-database-api.openglesscope.workers.dev','https://example.invalid','verify_security_contracts.py'),
 ('update exact-version binding loss','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','if (archiveVersion != update.version) error(','if (false && archiveVersion != update.version) error(','verify_security_contracts.py'),
 ('complete-report type validation loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt','!isEglConfigArray(root.opt("eglConfigs"))','false','verify_report_semantics.py'),
 ('internal-format query loss','app/src/main/cpp/openglesscope.cpp','glGetInternalformativ(target, format.value, GL_NUM_SAMPLE_COUNTS, 1, &count);','', 'verify_query_coverage.py'),
 ('internal-format schema loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt','!isInternalFormatArray(root.opt("internalFormats"))','false','verify_report_semantics.py'),
 ('EGL registry hash corruption','registry/egl_registry_manifest.json','32c90fd4160ea1a6f43d772b1a955f415756583b610d6f5344693adfd178bb82','0'*64,'verify_registry_snapshot.py'),
 ('EGL device query loss','app/src/main/cpp/openglesscope.cpp','eglGetProcAddress("eglQueryDevicesEXT")','eglGetProcAddress("eglQueryDevicesEXT_BROKEN")','verify_egl_registry_coverage.py'),
 ('registry hash corruption','registry/gl_registry_manifest.json','b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3','0'*64,'verify_registry_snapshot.py'),
 ('illegal creation-only EGL query','app/src/main/cpp/openglesscope.cpp','addEglCapability("EGL_CONTEXT_MINOR_VERSION", "Not applicable"','queryContextAttr(d, c, EGL_CONTEXT_MINOR_VERSION_KHR); addEglCapability("EGL_CONTEXT_MINOR_VERSION", "Not applicable"','verify_egl_query_legality.py'),
 ('EGL extension scope drift','app/src/main/cpp/openglesscope.cpp','hasExt(clientExt, "EGL_KHR_display_reference")','hasExt(displayExt, "EGL_KHR_display_reference")','verify_egl_query_legality.py'),
 ('ARM framebuffer fetch capability loss','app/src/main/cpp/openglesscope.cpp','addBooleanLimit(o, first, "GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM", GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM_VALUE);','', 'verify_gl_getpname_disposition.py'),
 ('history eager inflate regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','AnalysisHistoryRecord(file, savedAt, "Session snapshot", file.length())','loadAnalysisHistory(file); AnalysisHistoryRecord(file, savedAt, "Session snapshot", file.length())','verify_memory_resource_contracts_1_4_0.py'),
 ('history hash collision regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','MessageDigest.getInstance("SHA-256")','MessageDigest.getInstance("MD5")','verify_memory_resource_contracts_1_4_0.py'),
 ('producer EGL bound loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt','MAX_EGL_CAPABILITIES = 256','MAX_EGL_CAPABILITIES = 999999','verify_report_contract_alignment_1_4_0.py'),
 ('native EGL capability bound loss','app/src/main/cpp/openglesscope.cpp','kMaxEglCapabilityCount = 256','kMaxEglCapabilityCount = 999999','verify_report_contract_alignment_1_4_0.py'),
 ('robustness strategy query loss','app/src/main/cpp/openglesscope.cpp','addLimit(o, first, "GL_RESET_NOTIFICATION_STRATEGY", 0x8256);','', 'verify_query_coverage.py'),
 ('robust access query loss','app/src/main/cpp/openglesscope.cpp','addBooleanLimit(o, first, "GL_CONTEXT_ROBUST_ACCESS_KHR", 0x90F3);','', 'verify_gl_getpname_disposition.py'),
 ('large snapshot effect-key regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','LaunchedEffect(currentSnapshot) {','LaunchedEffect(currentSnapshot.toString()) {','verify_memory_resource_contracts_1_4_0.py'),
 ('history main-thread IO regression','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','history = withContext(Dispatchers.IO) { listAnalysisHistory(context) }','history = listAnalysisHistory(context)','verify_memory_resource_contracts_1_4_0.py'),
 ('diagnostic RAII loss','app/src/main/cpp/openglesscope.cpp','ActiveDiagnosticsScope diagnosticsScope(diagnostics);','activeDiagnostics = &diagnostics;','verify_memory_resource_contracts_1_4_0.py'),
]

def clone_tree(target: Path) -> Path:
    clone=target/'tree'
    shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'),copy_function=os.link)
    return clone

def write_cow(path: Path, data: str) -> None:
    # Break the hard link before writing so the immutable source tree is never mutated.
    path.unlink()
    path.write_text(data,encoding='utf-8')

def run_verifier(root: Path, verifier: str) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root); env['PYTHONDONTWRITEBYTECODE']='1'
    return subprocess.run([sys.executable,'-B',str(ROOT/'tools'/verifier)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def verify():
    for name,rel,old,new,verifier in CASES:
        with tempfile.TemporaryDirectory(prefix='ogles140-mut-',dir=ROOT.parent) as td:
            clone=clone_tree(Path(td))
            path=clone/rel; data=path.read_text(encoding='utf-8')
            require(old in data,f'negative mutation fixture missing for {name}')
            write_cow(path,data.replace(old,new,1))
            require(run_verifier(clone,verifier)!=0,f'negative mutation was not rejected: {name}')
    with tempfile.TemporaryDirectory(prefix='ogles140-fp-',dir=ROOT.parent) as td:
        clone=clone_tree(Path(td))
        changelog=clone/'changelog.md'; write_cow(changelog,changelog.read_text(encoding='utf-8')+'\n')
        for verifier in ['verify_security_contracts.py','verify_egl_query_legality.py','verify_gl_getpname_disposition.py','verify_memory_resource_contracts_1_4_0.py','verify_report_contract_alignment_1_4_0.py']:
            require(run_verifier(clone,verifier)==0,'unrelated false-positive control was rejected: '+verifier)
if __name__=='__main__': main_guard('test_1_4_0_negative_mutations',verify)
