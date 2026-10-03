#!/usr/bin/env python3
"""Release 2.2.19: real host-compiled GL version/log safety and immutable source gate."""
from __future__ import annotations
import hashlib,json,re,shutil,subprocess,tempfile,zipfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
NATIVE='app/src/main/cpp/openglesscope.cpp'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_19_release_regression_contract.json'
def sha(rel):return hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()
def source_oracle(s):
    must=[
        'if (major != parsed.first || minor != parsed.second)',
        'diagnostic("Runtime GL version consistency", GL_INVALID_VALUE);',
        'return {0, -1};',
        'Self-test runtime GL version identity was inconsistent',
        'glGetShaderInfoLog(shader, length, &written, buffer.data());',
        'glGetProgramInfoLog(program, length, &written, buffer.data());',
        'std::string(buffer.data(), static_cast<size_t>(written))',
        'totalConfigs != configCapacity',
        'auto attr = [&](size_t index) -> EglAttrResult { return attrs[index].result; };'
    ]
    require(all(x in s for x in must),'GL identity/log/EGLConfig safety drift: '+str([x for x in must if x not in s]))
    require(s.count('written < 0 || written >= length')==2,'both shader and program log read-length bounds must remain intact')
    require(s.count('std::string(buffer.data(), static_cast<size_t>(written))')==2,'both logs must use explicit bounded output length')
    require('std::string(text.data())' not in s,'native info logs can read past buffers')
    require('auto attr = [&](const char* name)' not in s,'quadratic EGLConfig name lookup reintroduced')
    block=s[s.index('        const EglConfigQuery attrs[]'):s.index('        const auto recordable =',s.index('        const EglConfigQuery attrs[]'))]
    keys=re.findall(r'\{"([A-Za-z]+)", configAttr\(',block)
    require(len(keys)==31,'EGLConfig query census changed without spec audit')
    expected=['red','green','blue','alpha','depth','stencil','sampleBuffers','samples','surfaceType','renderableType','conformant','configCaveat','colorBufferType','level','nativeRenderable','nativeVisualId','minSwapInterval','maxSwapInterval','bufferSize','luminanceSize','alphaMaskSize','bindToTextureRgb','bindToTextureRgba','maxPbufferWidth','maxPbufferHeight','maxPbufferPixels','nativeVisualType','transparentType','transparentRed','transparentGreen','transparentBlue']
    require(keys==expected,'EGLConfig output positional semantics drifted')
    indices=re.findall(r'\battr\((\d+)\)',block)
    require(indices==[str(i) for i in range(31)],'every queried EGLConfig attribute must map to exactly one original output field')

def compiled_oracle(s):
    compiler=shutil.which('g++') or shutil.which('clang++')
    require(bool(compiler),'host C++ compiler unavailable')
    version=s[s.index('static std::pair<int, int> runtimeGlVersion('):s.index('static void addLimit(',s.index('static std::pair<int, int> runtimeGlVersion('))]
    logs=s[s.index('static std::string shaderInfoLog('):s.index('static thread_local bool selfTestDebugCallbackSeen')]
    source=r'''#include <algorithm>
#include <cassert>
#include <string>
#include <utility>
#include <vector>
using GLint=int; using GLuint=unsigned int; using GLenum=unsigned int; using GLsizei=int;
static constexpr GLenum GL_NO_ERROR=0, GL_INVALID_VALUE=0x501, GL_MAJOR_VERSION=0x821B, GL_MINOR_VERSION=0x821C, GL_INFO_LOG_LENGTH=0x8B84;
static constexpr GLint kMaxInfoLogBytes=1024*1024;
static int realMajor=3,realMinor=2,logLength=5,reportedLength=4;
static bool logError=false,mismatch=false;
static void clearGlErrors(){}
static GLenum glGetError(){return logError?GL_INVALID_VALUE:GL_NO_ERROR;}
static void diagnostic(const char* name, GLenum err){if(std::string(name)=="Runtime GL version consistency"&&err!=GL_NO_ERROR)mismatch=true;}
static std::pair<int,int> parseGlVersion(const char* text){return std::string(text)=="OpenGL ES 3.1"?std::pair<int,int>{3,1}:std::string(text)=="OpenGL ES 2.0"?std::pair<int,int>{2,0}:std::pair<int,int>{3,2};}
static int versionCode(std::pair<int,int> value){return value.first*100+value.second*10;}
static void glGetIntegerv(GLenum e,GLint* out){*out=e==GL_MAJOR_VERSION?realMajor:realMinor;}
static void glGetShaderiv(GLuint,GLenum,GLint* out){*out=logLength;}
static void glGetProgramiv(GLuint,GLenum,GLint* out){*out=logLength;}
static void glGetShaderInfoLog(GLuint,GLsizei n,GLsizei* written,char* out){for(int i=0;i<std::min(n,4);++i)out[i]="ABCD"[i];*written=reportedLength;}
static void glGetProgramInfoLog(GLuint,GLsizei n,GLsizei* written,char* out){for(int i=0;i<std::min(n,4);++i)out[i]="WXYZ"[i];*written=reportedLength;}
''' +version+logs+r'''
int main(){
    assert(runtimeGlVersion("OpenGL ES 3.2")==std::make_pair(3,2));
    realMinor=1;mismatch=false;
    assert(runtimeGlVersion("OpenGL ES 3.2")==std::make_pair(0,-1) && mismatch);
    assert(runtimeGlVersion("OpenGL ES 3.1")==std::make_pair(3,1));
    assert(runtimeGlVersion("OpenGL ES 2.0")==std::make_pair(2,0));
    assert(shaderInfoLog(1)=="ABCD" && programInfoLog(1)=="WXYZ");
    reportedLength=5;
    assert(shaderInfoLog(1)=="Shader info log read failed or returned an invalid length");
    assert(programInfoLog(1)=="Program info log read failed or returned an invalid length");
    reportedLength=-1;assert(shaderInfoLog(1)=="Shader info log read failed or returned an invalid length");
    logLength=kMaxInfoLogBytes+1;assert(shaderInfoLog(1)=="Shader info log exceeded the 1 MiB safety bound");
    logLength=5;reportedLength=4;logError=true;
    assert(shaderInfoLog(1)=="Shader info log length query failed");
    assert(programInfoLog(1)=="Program info log length query failed");
}
'''
    with tempfile.TemporaryDirectory(prefix='ogl219-host-') as tmp:
        src=Path(tmp)/'oracle.cpp'; exe=Path(tmp)/'oracle';src.write_text(source)
        p=subprocess.run([compiler,'-std=c++20','-O2','-Wall','-Wextra','-Werror',str(src),'-o',str(exe)],capture_output=True,text=True,timeout=30)
        require(p.returncode==0,'host-compiled version/log oracle: '+p.stderr[:2000])
        p=subprocess.run([str(exe)],capture_output=True,text=True,timeout=10)
        require(p.returncode==0,'host-compiled runtime assertions failed: '+p.stderr[:1000])

def verify():
    lock=json.loads((ROOT/CONTRACT).read_text())
    require(lock['release']=='2.2.19' and lock['predecessor']=='2.2.18','release regression identity drift')
    require(len(lock['predecessorHashes'])==160,'predecessor production census drift')
    require(lock['predecessorZipSha256']=='1a09c0f81e9436a1915b0f483848455f909bd36401c3a8ff449be63019ee7265','predecessor zip hash drift')
    allow={NATIVE,GRADLE}
    for rel,h in lock['predecessorHashes'].items():
        if rel not in allow:require(sha(rel)==h,'unreviewed production mutation: '+rel)
    require('releaseVersionName = "2.2.19"' in (ROOT/GRADLE).read_text() and 'releaseVersionCode = 2219' in (ROOT/GRADLE).read_text(),'release identity drift')
    s=(ROOT/NATIVE).read_text()
    source_oracle(s);compiled_oracle(s)
    print('verify_2_2_19_integrity_security_performance: PASS (160 production sources; compiled GL version and bounded GPU log fixtures; 31 EGLConfig fields)')
if __name__=='__main__':main_guard('verify_2_2_19_integrity_security_performance',verify)
