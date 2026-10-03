#!/usr/bin/env python3
"""2.2.18 immutable predecessor, VulkanScope notification and GL/EGL evidence contract."""
from __future__ import annotations
import hashlib,json,re,shutil,subprocess,tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
from verify_2_2_15_evidence_integrity import host_compiled_oracle
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
NATIVE='app/src/main/cpp/openglesscope.cpp'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_18_release_regression_contract.json'

def sha(path:str)->str:return hashlib.sha256((ROOT/path).read_bytes()).hexdigest()

def notification_oracle(s:str)->None:
    start=s.index('    LaunchedEffect(Unit) {\n        collectionStatus = CollectionStatus.COLLECTING',s.index('private fun OpenGLESScopeApp('))
    end=s.index('    val collecting = collectionStatus == CollectionStatus.COLLECTING',start)
    part=s[start:end]
    require('collectionStatus = if (parsed.available) CollectionStatus.COMPLETED else CollectionStatus.FAILED' in part,'unknown/unavailable evidence lost its terminal failure state')
    require('collectionStatus = CollectionStatus.FAILED' in part,'exception path does not publish failure')
    require('if (collectionStatus == CollectionStatus.COMPLETED) {\n            delay(2200L)\n            if (collectionStatus == CollectionStatus.COMPLETED) collectionStatus = CollectionStatus.IDLE' in part,'failed/unknown collection state must persist')
    require(not re.search(r'\n\s+delay\(2200\)\s+collectionStatus = CollectionStatus.IDLE',part),'unconditional failed-notification dismissal')
    require('collectionStatus = CollectionStatus.COLLECTING' in part[:120], 'retry must supersede old failure')
    host=s[s.index('private fun TransientStatusOverlayHost('):s.index('private fun UpdateStatusBadge(')]
    for literal in ['CollectionStatusBanner(collectionStatus)','ConnectivityStatusHost(collectionStatus, networkStateKnown, networkAvailable, networkBannerState)','UpdateStatusBanner(updateStatus, onInstallUpdate)','ic_info','ic_network_connected','ic_network_disconnected','ic_update_available','ic_download','240','360','380L','4_500L']:
        if literal=='4_500L':require(literal in s,'network transition lifetime drift')
        else:require(literal in host,'notification parity missing: '+literal)
    require('if (networkStateKnown && !networkAvailable && transitionState == NetworkBannerState.HIDDEN)' in host,'persistent offline alert must wait for known validated network state')
    require('if (collectionInProgress)' in host,'offline while collecting must retain separate explanatory text')
    require('if (connected) R.drawable.ic_network_connected else R.drawable.ic_network_disconnected' in host,'connected/disconnected glyph mismatch')
    require('painterResource(R.drawable.ic_info)' in host, 'offline availability must have VulkanScope information glyph')
    require('UpdateStatus.Checking -> { ExpressiveLinearProgressIndicator' in s,'update checking spinner drift')
    require('is UpdateStatus.Failed -> Text(status.message' in s,'update failure status missing')
    snap=json.loads((ROOT/'tests/golden/2_2_18_notification_drawable_reference.json').read_text())
    require(snap['reference']=='VulkanScope 3.0.12 immutable drawable SHA-256 snapshot','wrong visual reference')
    require(len(snap['sharedIcons'])==10,'notification icon census drift')
    for name,expected in snap['sharedIcons'].items():require(sha('app/src/main/res/drawable/'+name+'.xml')==expected,'VulkanScope drawable drift: '+name)

def egl_oracle(s:str)->None:
    required=['eglGetError();\n    const char* rawDisplayExtensionText = eglQueryString(d, EGL_EXTENSIONS);\n    const EGLint displayExtensionError = eglGetError();',
              'displayExtensionError == EGL_SUCCESS && runtimeStringValid(rawDisplayExtensionText)',
              'auto queryEglDisplayString = [&](EGLint token, const char*& raw, EGLint& error) -> const char*',
              'error = eglGetError();\n        return error == EGL_SUCCESS && runtimeStringValid(raw) ? raw : nullptr;',
              'queryEglDisplayString(EGL_VENDOR, rawEglVendorText, eglVendorError)',
              'queryEglDisplayString(EGL_VERSION, rawEglVersionText, eglVersionError)',
              'queryEglDisplayString(EGL_CLIENT_APIS, rawEglClientApisText, eglClientApisError)',
              'clientDiscoveryNotApplicable = eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr',
              'if (!eglVendorText || !eglVersionText || (eglCode >= 120 && !eglClientApisText))',
              'eglCode >= 120 && !eglClientApisText',
              'Required EGL_VENDOR identity query failed:', 'Required EGL_VERSION identity query failed:', 'Required EGL_CLIENT_APIS identity query failed:',
              'releaseEgl(d, s, c);\n        return env->NewStringUTF((std::string(',
              'const EGLint selfTestExtensionsError = eglGetError();',
              'selfTestExtensionsError == EGL_SUCCESS ? splitExt(selfTestDisplayExtensions, &selfTestDisplayExtComplete)']
    require(all(x in s for x in required),'EGL correctness drift: '+str([x for x in required if x not in s]))
    require('eglCode < 150 ? "Not applicable" : "Unavailable"' not in s,'non-EGL_BAD_DISPLAY failures cannot be classified as not applicable')
    require(s.count('eglGetError();')>=20,'per-query EGL error checking regressed')

def host_egl_oracle(s:str)->None:
    compiler=shutil.which('clang++') or shutil.which('g++')
    require(bool(compiler),'host C++ compiler missing')
    begin=s.index('    auto queryEglDisplayString =')
    query=s[begin:s.index('    };',begin)+7]
    begin=s.index('    auto eglStringDetail =')
    detail=s[begin:s.index('    };',begin)+7]
    harness=r'''#include <cassert>
#include <cstring>
#include <string>
#include <iostream>
using EGLint=int;
static constexpr EGLint EGL_SUCCESS=0x3000,EGL_BAD_DISPLAY=0x3008,EGL_BAD_PARAMETER=0x300C;
static constexpr EGLint EGL_VENDOR=0x3053,EGL_VERSION=0x3054,EGL_CLIENT_APIS=0x308D;
static int d=1, pending=EGL_SUCCESS, emitted=EGL_SUCCESS;
static const char* textResult="Vendor";
static EGLint eglGetError(){ auto e=pending;pending=EGL_SUCCESS;return e; }
static const char* eglQueryString(int,EGLint){pending=emitted;return textResult;}
static bool runtimeStringValid(const char* value){return value && value[0]!='!' && std::strlen(value)<1024;}
static std::string eglErrorDisplay(EGLint error){return "EGL error "+std::to_string(error);}
int main(){
''' + query + '\n' + detail + r'''
    const char* raw=nullptr; EGLint error=EGL_SUCCESS;
    emitted=EGL_SUCCESS;textResult="Vendor";
    const char* valid=queryEglDisplayString(EGL_VENDOR,raw,error);
    assert(valid && std::string(valid)=="Vendor" && error==EGL_SUCCESS);
    emitted=EGL_BAD_PARAMETER;textResult="FabricatedVendor";
    valid=queryEglDisplayString(EGL_VENDOR,raw,error);
    assert(!valid && error==EGL_BAD_PARAMETER && eglStringDetail(raw,valid,error).find("12300")!=std::string::npos);
    emitted=EGL_SUCCESS;textResult="!invalid";
    valid=queryEglDisplayString(EGL_VERSION,raw,error);
    assert(!valid && eglStringDetail(raw,valid,error).find("invalid UTF-8")!=std::string::npos);
    emitted=EGL_SUCCESS;textResult=nullptr;
    valid=queryEglDisplayString(EGL_CLIENT_APIS,raw,error);
    assert(!valid && eglStringDetail(raw,valid,error)=="eglQueryString returned null");
    bool clientExtComplete=false;const char* rawClientExtensionText=nullptr;
    int eglCode=140,clientExtError=EGL_BAD_DISPLAY;
    bool clientDiscoveryNotApplicable = eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr;
    assert(clientDiscoveryNotApplicable);
    clientExtError=EGL_BAD_PARAMETER;
    clientDiscoveryNotApplicable = eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr;
    assert(!clientDiscoveryNotApplicable);
    eglCode=150;clientExtError=EGL_BAD_DISPLAY;
    clientDiscoveryNotApplicable = eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr;
    assert(!clientDiscoveryNotApplicable);
    assert(!clientExtComplete);
    std::cout<<"EGL QUERY HOST PASS";
}
'''
    with tempfile.TemporaryDirectory(prefix='ogles-egl-query-') as t:
        source=Path(t)/'egl.cpp';binary=Path(t)/'egl';source.write_text(harness)
        cp=subprocess.run([compiler,'-std=c++20','-O2','-Wall','-Wextra','-Werror',str(source),'-o',str(binary)],capture_output=True,text=True,timeout=40)
        require(cp.returncode==0,'EGL host compiler failed: '+cp.stderr[:1800])
        run=subprocess.run([str(binary)],capture_output=True,text=True,timeout=10)
        require(run.returncode==0 and 'EGL QUERY HOST PASS' in run.stdout,'EGL host semantic fixture failed: '+run.stderr[:1800])

def verify()->None:
    c=json.loads((ROOT/CONTRACT).read_text())
    require(c['release']=='2.2.18' and c['predecessor']=='2.2.17','wrong predecessor identity')
    require(c['predecessorZipSha256']=='21992266509b5cd28338bb443503349f76fcdfd0c3fed840319cb5bb7de909d5','predecessor ZIP SHA drift')
    require(len(c['predecessorHashes'])==160 and set(c['allowlistedChanges'])=={MAIN,NATIVE,GRADLE},'160 path locked production census/allowlist missing')
    for path,old in c['predecessorHashes'].items():
        now=sha(path)
        if path in c['allowlistedChanges']:
            ch=c['allowlistedChanges'][path]
            require(ch['predecessorSha256']==old and ch['successorSha256']==now and old!=now and ch['reason'],'unreviewed changed production file: '+path)
        else:require(now==old,'unrelated production file drift: '+path)
    require('releaseVersionName = "2.2.18"' in (ROOT/GRADLE).read_text() and 'releaseVersionCode = 2218' in (ROOT/GRADLE).read_text(),'version identity not bumped correctly')
    main=(ROOT/MAIN).read_text();native=(ROOT/NATIVE).read_text()
    notification_oracle(main);egl_oracle(native);host_egl_oracle(native);host_compiled_oracle(native)
    for required in ['## Release 2.2.18 notification-state and EGL identity provenance audit']:
        require(required in (ROOT/'rules/PROJECT_RULES.md').read_text(),'current project rules not updated')
    print('verify_2_2_18_state_and_evidence: PASS (160 hash-locked files, ten byte-identical VulkanScope icons, persistent failures, EGL query semantics and compiled C++ fixtures)')

if __name__=='__main__':main_guard('verify_2_2_18_state_and_evidence',verify)
