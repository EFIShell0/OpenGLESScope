#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import os,re,shutil,subprocess,tempfile

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    require(any((f'releaseVersionName = "{name}"' in gradle and f'releaseVersionCode = {code}' in gradle) for name,code in [('1.9.3',1903),('2.0.0',2000),('2.1.0',2100),('2.1.1',2101),('2.1.2',2102),('2.1.3',2103),('2.1.4',2104),('2.2.0',2200),('2.2.1',2201),('2.2.2',2202),('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.12',2212),('2.2.13',2213),('2.2.14',2214)]),'1.9.3+ compile-hotfix identity drift')
    expected=[
        'entries["egl-runtime/surfaceGlColorspaceQuery"] = e.optNullableString("surfaceGlColorspaceQuery") ?: "Not applicable"',
        'entries["egl-runtime/surfaceVgAlphaFormatQuery"] = e.optNullableString("surfaceVgAlphaFormatQuery") ?: "Not applicable"',
        'entries["egl-runtime/surfaceVgColorspaceQuery"] = e.optNullableString("surfaceVgColorspaceQuery") ?: "Not applicable"',
    ]
    for line in expected: require(line in main,'EGL runtime query provenance analysis binding missing: '+line)
    require(re.search(r'\bruntime\.optNullableString\("surface(?:GlColorspace|VgAlphaFormat|VgColorspace)Query"\)',main) is None,'undefined runtime receiver restored in EGL runtime analysis mapping')
    kotlinc=None if os.environ.get('OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE')=='1' else shutil.which('kotlinc')
    if kotlinc:
        with tempfile.TemporaryDirectory(prefix='ogles193-kotlinc-') as td:
            cp=subprocess.run([kotlinc,str(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'),'-d',os.path.join(td,'out.jar')],text=True,capture_output=True)
            out=(cp.stdout or '')+'\n'+(cp.stderr or '')
            require(re.search(r'unresolved reference[: ]+runtime\b',out,re.I) is None,'runtime remains unresolved in Kotlin compiler probe')
            for pattern in [r'Syntax error:',r'Unterminated',r'Unexpected tokens']:
                require(re.search(pattern,out,re.I) is None,'Kotlin parser probe failed: '+pattern)
if __name__=='__main__': main_guard('verify_1_9_3_compile_hotfix',verify)
