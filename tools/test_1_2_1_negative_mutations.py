#!/usr/bin/env python3
from __future__ import annotations
import os, shutil, subprocess, sys, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

CASES=[
 ('version drift','app/build.gradle.kts','releaseVersionName = "1.2.1"','releaseVersionName = "9.9.9"','verify_build_contract.py'),
 ('fabricated GL name','app/src/main/cpp/openglesscope.cpp','GL_COMPRESSED_LUMINANCE_LATC1_EXT','GL_COMPRESSED_LUMINANCE_LATC1_NV','verify_registry_canonical_names.py'),
 ('core query loss','app/src/main/cpp/openglesscope.cpp','addLimit(o, first, "GL_RED_BITS", GL_RED_BITS);','', 'verify_query_coverage.py'),
 ('terminal handshake loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt','const val EXTRA_TERMINAL_PATH = "terminal_path"','const val EXTRA_TERMINAL_PATH_BROKEN = "terminal_path"','verify_probe_lifecycle.py'),
 ('cleartext enabled','app/src/main/AndroidManifest.xml','android:usesCleartextTraffic="false"','android:usesCleartextTraffic="true"','verify_security_contracts.py'),
 ('database origin drift','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','https://openglesscope-database-api.openglesscope.workers.dev','https://example.invalid','verify_security_contracts.py'),
 ('update exact-version binding loss','app/src/main/java/com/efishell/openglesscope/MainActivity.kt','if (archiveVersion != update.version) error(','if (false && archiveVersion != update.version) error(','verify_security_contracts.py'),
 ('complete-report type validation loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt','!isEglConfigArray(root.opt("eglConfigs"))','false','verify_report_semantics.py'),
 ('internal-format query loss','app/src/main/cpp/openglesscope.cpp','glGetInternalformativ(target, format.value, GL_NUM_SAMPLE_COUNTS, 1, &count);','/* removed */','verify_query_coverage.py'),
 ('internal-format schema loss','app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt','!isInternalFormatArray(root.opt("internalFormats"))','false','verify_report_semantics.py'),
 ('registry hash corruption','registry/gl_registry_manifest.json','b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3','0'*64,'verify_registry_snapshot.py'),
]

def run_verifier(root: Path, verifier: str) -> int:
    env=os.environ.copy(); env['OPENGLESSCOPE_ROOT']=str(root)
    return subprocess.run([sys.executable,str(ROOT/'tools'/verifier)],cwd=root,env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode

def verify():
    for name,rel,old,new,verifier in CASES:
        with tempfile.TemporaryDirectory(prefix='ogles-mut-') as td:
            clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'))
            path=clone/rel; data=path.read_text(encoding='utf-8')
            require(old in data,f'negative mutation fixture missing for {name}')
            path.write_text(data.replace(old,new,1),encoding='utf-8')
            require(run_verifier(clone,verifier)!=0,f'negative mutation was not rejected: {name}')
    with tempfile.TemporaryDirectory(prefix='ogles-fp-') as td:
        clone=Path(td)/'tree'; shutil.copytree(ROOT,clone,ignore=shutil.ignore_patterns('.git','.gradle','build','__pycache__','*.pyc'))
        changelog=clone/'changelog.md'; changelog.write_text(changelog.read_text(encoding='utf-8')+'\n',encoding='utf-8')
        require(run_verifier(clone,'verify_security_contracts.py')==0,'unrelated false-positive control was rejected')

if __name__=='__main__': main_guard('test_1_2_1_negative_mutations',verify)
