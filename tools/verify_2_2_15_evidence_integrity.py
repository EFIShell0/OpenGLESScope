#!/usr/bin/env python3
"""2.2.15 GL/EGL source, immutable predecessor, and compiled native-helper oracle."""
from __future__ import annotations
import hashlib, json, re, shutil, subprocess, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard

NATIVE = 'app/src/main/cpp/openglesscope.cpp'
GRADLE = 'app/build.gradle.kts'
CONTRACT = 'tests/golden/2_2_15_release_regression_contract.json'

def sha(path: str) -> str:
    return hashlib.sha256((ROOT/path).read_bytes()).hexdigest()

def native_oracle(s: str) -> None:
    required = [
        'static std::string esc(const std::string& s)', 'cp <= 0x10FFFFu',
        'unicodeEscape(0xD800u + (cp >> 10))', 'unicodeEscape(0xDC00u + (cp & 0x3FFu))',
        'static bool runtimeStringValid(const char* s)', 'cp > 0x10FFFFu',
        'static std::vector<std::string> splitExt(const char* s, bool* complete = nullptr)',
        'if (out.size() != received) return {}', 'if (complete) *complete = true',
        'static std::vector<std::string> glExtensions(int glCode, bool& complete)',
        'if (!glExtComplete)', 'if (!displayExtComplete)', 'if (clientExtensionText && !clientExtComplete)',
        'eglGetError();\n    const char* rawClientExtensionText = eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);',
        'eglCode < 150 ? "Not applicable" : "Unavailable"',
        'mandatoryEnumerationsComplete = false', 'if (!mandatoryEnumerationsComplete)',
        'std::numeric_limits<GLfloat>::max_digits10', '!std::isfinite(v)',
        '!std::isfinite(v[0]) || !std::isfinite(v[1])',
        'A required GL format/binary enumeration failed',
        'GL extension enumeration failed or exceeded a safety bound',
        'glExtensions(glCode, selfTestGlExtComplete)',
        'if (!selfTestDisplayExtComplete)',
        'if (!selfTestGlExtComplete)',
        'if (!deviceExtComplete)',
        'extComplete ? \"Available\" : \"Unavailable\"',
    ]
    require(all(x in s for x in required), 'native collection / JSON source oracle drift: '+', '.join(x for x in required if x not in s))
    require('if (c < 0x20 || c >= 0x80)' not in s, 'old per-UTF-8-byte Unicode corruptor returned')
    require(s.count('std::numeric_limits<GLfloat>::max_digits10') >= 2, 'both scalar/vector floating-point reports must remain lossless')
    require(s.count('mandatoryEnumerationsComplete = false') >= 2, 'both compressed and binary format failures must be fatal to an available report')
    require('out = splitExt(extensionString, &complete);' in s, 'GLES2 extension parsing lacks completeness evidence')
    require('complete = true;' in s[s.index('static std::vector<std::string> glExtensions'):s.index('static bool chooseConfig')], 'successful GLES3 enumeration cannot be distinguished from failed enumeration')

def host_compiled_oracle(s: str) -> None:
    compiler = shutil.which('clang++') or shutil.which('g++')
    require(bool(compiler), 'host C++ compiler is required for source helper tests')
    esc = s[s.index('static std::string esc('):s.index('static std::string hexv(')]
    extension = s[s.index('static size_t boundedCStringLength('):s.index('static bool hasExt(')]
    flt = s[s.index('static void addFloatLimit('):s.index('static void addBooleanLimit(')]
    harness = r'''#include <algorithm>
#include <cctype>
#include <cmath>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <limits>
#include <sstream>
#include <string>
#include <vector>
using GLenum=unsigned int; using GLfloat=float;
static constexpr unsigned int GL_NO_ERROR=0, GL_INVALID_VALUE=0x0501;
static constexpr int kMaxGlEnumerationCount=16384;
static constexpr size_t kMaxRuntimeStringBytes=1024*1024, kMaxExtensionTokenBytes=4096;
''' + esc + extension + r'''
static bool errorSeen=false;
static GLfloat nextFloat=1.23456789f;
static void clearGlErrors() {}
static GLenum glGetError(){return GL_NO_ERROR;}
static void glGetFloatv(GLenum,GLfloat *result){result[0]=nextFloat;result[1]=nextFloat;}
static void diagnostic(const char*, GLenum error){if(error!=GL_NO_ERROR) errorSeen=true;}
''' + flt + r'''
int main(){
    const std::string nonascii = std::string("Caf\xC3\xA9 ") + "\xF0\x9F\x98\x80";
    std::cout << q(nonascii) << "\n";
    std::cout << runtimeStringValid(nonascii.c_str()) << " "
              << runtimeStringValid("\xC0\xAF") << " "
              << runtimeStringValid("\xED\xA0\x80") << "\n";
    bool complete = false;
    auto a=splitExt("",&complete);std::cout<<complete<<" "<<a.size()<<"\n";
    auto b=splitExt("GL_A GL_B GL_A",&complete);std::cout<<complete<<" "<<b.size()<<"\n";
    auto c=splitExt(std::string(4097,'X').c_str(),&complete);std::cout<<complete<<" "<<c.size()<<"\n";
    auto d=splitExt("GL_Z GL_A",&complete);std::cout<<complete<<" "<<d.size()<<" "<<d[0]<<"\n";
    std::ostringstream value; bool first=true;
    addFloatLimit(value,first,"GL_MAX_TEXTURE_LOD_BIAS",0);
    std::cout<<"["<<value.str()<<"]"<<"\n";
    nextFloat=std::numeric_limits<float>::infinity();
    std::ostringstream bad;first=true;errorSeen=false;
    addFloatLimit(bad,first,"GL_MAX_TEXTURE_LOD_BIAS",0);
    std::cout<<bad.str().size()<<" "<<errorSeen<<"\n";
    nextFloat=2.5f; std::ostringstream pair;first=true;
    addFloatLimit2(pair,first,"GL_ALIASED_POINT_SIZE_RANGE",0);
    std::cout<<"["<<pair.str()<<"]"<<"\n";
}
'''
    with tempfile.TemporaryDirectory(prefix='ogles-utf8-') as temp:
        src=Path(temp)/'oracle.cpp'; out=Path(temp)/'oracle';src.write_text(harness,encoding='utf-8')
        cp=subprocess.run([compiler,'-std=c++20','-Wall','-Wextra','-Werror','-O2',str(src),'-o',str(out)],text=True,capture_output=True,timeout=40)
        require(cp.returncode==0,'native helper compile failed: '+cp.stderr[:2500])
        proc=subprocess.run([str(out)],text=True,capture_output=True,timeout=10)
        require(proc.returncode==0,'native helper execution failed: '+proc.stderr[:1000])
        lines=proc.stdout.strip().splitlines()
        require(len(lines)==9,'host oracle output unexpectedly short: '+proc.stdout)
        require(json.loads(lines[0])=='Café 😀','UTF-8 round-trip corrupted actual driver identity: '+lines[0])
        require(lines[1]=='1 0 0','valid UTF-8 accepted wrongly, or malformed UTF-8 not rejected: '+lines[1])
        require(lines[2]=='1 0' and lines[3]=='0 0' and lines[4]=='0 0' and lines[5]=='1 2 GL_A','empty/failed/duplicate/oversized extension conflation: '+str(lines[2:6]))
        value=json.loads(lines[6]) if lines[6].startswith('[') else None
        # The final finite/nonfinite check is printed on an additional line.
        require(value is not None and len(value)==1 and value[0]['name']=='GL_MAX_TEXTURE_LOD_BIAS' and abs(float(value[0]['value'])-1.23456789)<1e-7,'finite float data lost')
        require(lines[7]=='0 1','nonfinite float must never be exported as a capability value')
        require('2.5 … 2.5' in json.loads(lines[8])[0]['value'], 'float vector evidence must retain precision')

def verify() -> None:
    c=json.loads((ROOT/CONTRACT).read_text(encoding='utf-8'))
    old=json.loads((ROOT/'tests/golden/2_2_14_release_regression_contract.json').read_text(encoding='utf-8'))
    lineage=dict(old['predecessorHashes'])
    lineage.update({k:v['successorSha256'] for k,v in old['allowlistedChanges'].items()})
    lineage.update({k:v['sha256'] for k,v in old.get('allowlistedNewFiles',{}).items()})
    require(c['release']=='2.2.15' and c['predecessor']=='2.2.14' and len(lineage)==160 and c['predecessorHashes']==lineage,'release lineage drift')
    require(c['predecessorZipSha256']=='519bc7e06c8c0d78c2b8b3ab3a2d232aad61f31f5528c09648019db9bbd405c6','wrong source predecessor zip')
    require(set(c['allowlistedChanges'])=={NATIVE,GRADLE} and not c['allowlistedNewFiles'] and not c['removedFiles'],'unreviewed production changes')
    for path, prev in lineage.items():
        if path in c['allowlistedChanges']:
            change=c['allowlistedChanges'][path]
            require(change['predecessorSha256']==prev and change['successorSha256']==sha(path) and sha(path)!=prev and bool(change['reason'].strip()),'untracked production mutation: '+path)
        else: require(sha(path)==prev,'unrelated production mutation: '+path)
    gradle=(ROOT/GRADLE).read_text(encoding='utf-8')
    require('releaseVersionName = "2.2.15"' in gradle and 'releaseVersionCode = 2215' in gradle and 'minorApiLevel = 2' in gradle and 'targetSdk = 37' in gradle,'release identity / SDK mismatch')
    n=(ROOT/NATIVE).read_text(encoding='utf-8')
    native_oracle(n)
    host_compiled_oracle(n)
    lock=json.loads((ROOT/'registry/registry_lock.json').read_text(encoding='utf-8'))
    require(hashlib.sha256((ROOT/'registry/gl.xml').read_bytes()).hexdigest()==lock['registrySha256'] and hashlib.sha256((ROOT/'registry/egl.xml').read_bytes()).hexdigest()==lock['eglRegistrySha256'],'locked current registry source modified')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    for x in ('TECHNICAL_REPORT_SCHEMA_VERSION = 5','SUBMISSION_SCHEMA_VERSION = 2','put("technicalReport", JSONObject()','section("EGL runtime"','QUERY DIAGNOSTICS','INTERNAL FORMAT SAMPLE SUPPORT'):
        require(x in main,'full-report sink missing: '+x)
    require('## Release 2.2.15 complete evidence-integrity / current-registry audit' in (ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8'),'release engineering contract missing')
    print('verify_2_2_15_evidence_integrity: PASS (160-path source hash, compiled C++ Unicode/extension/float tests, registry locks)')

if __name__=='__main__':main_guard('verify_2_2_15_evidence_integrity',verify)
