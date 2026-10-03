#!/usr/bin/env python3
"""Immutable 2.2.19 parent, EGL error provenance, lossless bounded DMA-BUF evidence."""
from __future__ import annotations
import hashlib, json, shutil, subprocess, tempfile
from pathlib import Path
from gate_common import ROOT, require, main_guard
NATIVE='app/src/main/cpp/openglesscope.cpp'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_20_release_regression_contract.json'
def sha(rel): return hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()
def source_oracle(s):
    markers=[
      'static std::string hexModifier(EGLuint64KHR value)',
      'static bool stableCount(EGLint expected, EGLint written) { return expected >= 0 && written == expected; }',
      'const EGLint error = eglGetError();\n        if (error != EGL_SUCCESS) return {nullptr, eglErrorDisplay(error)};',
      'if (!runtimeStringValid(raw)) return {nullptr, "eglQueryDeviceStringEXT returned null, malformed or oversized UTF-8 text"};',
      'const auto deviceExtText = queryDeviceText(device, EGL_EXTENSIONS);',
      'const auto renderer = queryDeviceText(device, EGL_RENDERER_EXT_VALUE);',
      'const auto driverName = queryDeviceText(device, EGL_DRIVER_NAME_EXT_VALUE);',
      'const auto extText = queryDeviceText(device, EGL_EXTENSIONS);',
      'extComplete ? (renderer.first ? renderer.first : "") : ""',
      'const EGLint deviceReadError = eglGetError();',
      'deviceRead == EGL_TRUE && deviceReadError == EGL_SUCCESS && rawDevice != 0',
      'const bool ok = readOk && stableCount(count, written);',
      'const bool formatsOk = formatsRead && stableCount(formatCount, writtenFormats);',
      'const bool modifiersOk = modifiersRead && stableCount(modifierCount, writtenModifiers);',
      'const bool ok = readOk && stableCount(count, written);',
      'Modifier count changed between enumeration passes; incomplete evidence rejected',
      'Format count changed between enumeration passes; incomplete evidence rejected',
      'Compression-rate count changed between enumeration passes; incomplete evidence rejected',
      'Device count changed between two enumeration passes; incomplete evidence rejected',
      'modifierDetail += hexModifier(modifiers[index]);',
      'modifierDetail += externalOnly[index] == EGL_TRUE ? "[externalOnly=true]" : "[externalOnly=false]";',
      'if (externalOnly[index] != EGL_TRUE && externalOnly[index] != EGL_FALSE) { modifierFlagsValid = false; break; }',
      'modifierFlagsValid ? "Available" : "Unavailable"',
      'raw DRM modifiers: " + modifierDetail',
      'const EGLint driverNameError = getDriverName ? eglGetError() : EGL_SUCCESS;',
      'const bool driverNameValid = driverNameError == EGL_SUCCESS && runtimeStringValid(driverName);',
      'kMaxDmaBufFormatCount = 128', 'kMaxDmaBufModifierCountPerFormat = 256', 'kMaxDmaBufModifierCountTotal = 4096',
    ]
    missing=[v for v in markers if v not in s]
    require(not missing, 'EGL provenance / enumeration / reporting regression: '+str(missing))
    require('raw DRM modifiers intentionally summarized' not in s,'dropping real DRM modifier values is report loss')
    require(s.count('stableCount(count, written)')>=2,'device and compression enumeration must be stable')
    require('if (count == 0 ||' not in s[s.index('const bool hasDeviceEnumeration'):s.index('if (hasExt(displayExt, "EGL_MESA_query_driver"')], 'zero-count handling must remain explicit')
    require(256*(18+22+2) <= 16384,'a single bounded modifier detail cannot fit in an analysis value')
def compiled_oracle(s):
    compiler=shutil.which('g++') or shutil.which('clang++')
    require(bool(compiler),'host C++20 compiler not found')
    helpers=s[s.index('static std::string hexModifier('):s.index('static constexpr GLint kMaxGlEnumerationCount')]
    qblock=s[s.index('    auto queryDeviceText ='):s.index('    const bool hasDeviceQuery =',s.index('    auto queryDeviceText ='))]
    mblock=s[s.index('                        EGLint externalCount = 0;',s.index('const bool modifiersOk =')):s.index('                        addEglCapability("EGL dma-buf format "',s.index('const bool modifiersOk ='))]
    fixture=r'''#include <cassert>
#include <cstdint>
#include <cstring>
#include <functional>
#include <iomanip>
#include <sstream>
#include <string>
#include <tuple>
#include <vector>
using EGLint=int; using EGLBoolean=int; using EGLDeviceEXT=int;using EGLuint64KHR=unsigned long long;
static constexpr EGLint EGL_SUCCESS=0x3000, EGL_BAD_DEVICE_EXT=0x322B;
static constexpr EGLBoolean EGL_TRUE=1,EGL_FALSE=0;
''' + helpers + r'''
int main(){
    assert(hexModifier(0xFEDCBA9876543210ULL)=="0xFEDCBA9876543210");
    assert(hexModifier(0)=="0x0000000000000000");
    assert(stableCount(0,0)); assert(stableCount(3,3));
    assert(!stableCount(3,2)); assert(!stableCount(3,4)); assert(!stableCount(-1,-1));
    EGLint pending=EGL_SUCCESS, next=EGL_SUCCESS;
    const char* raw="Renderer One";
    std::function<const char*(EGLDeviceEXT,EGLint)> queryDeviceStringExt=[&](EGLDeviceEXT,EGLint)->const char*{pending=next;return raw;};
    auto eglGetError=[&](){EGLint result=pending;pending=EGL_SUCCESS;return result;};
    auto eglErrorDisplay=[](EGLint value){return std::to_string(value);};
    auto runtimeStringValid=[](const char* p){return p && std::strlen(p)<128 && static_cast<unsigned char>(p[0])<128;};
''' + qblock + r'''
    pending=EGL_BAD_DEVICE_EXT;
    assert(queryDeviceText(1,2).first!=nullptr);
    next=EGL_BAD_DEVICE_EXT;
    auto rejected=queryDeviceText(1,2);
    assert(rejected.first==nullptr && !rejected.second.empty());
    next=EGL_SUCCESS;raw=nullptr;
    assert(queryDeviceText(1,2).first==nullptr);
    raw="Renderer One";
    assert(queryDeviceText(1,2).first);
    auto record=[&](std::vector<EGLuint64KHR> modifiers,std::vector<EGLBoolean> externalOnly, EGLint writtenModifiers){
       bool modifiersOk=true;
''' + mblock + r'''
       return std::make_tuple(modifierFlagsValid,externalCount,modifierDetail);
    };
    const auto valid=record({0xFEDCBA9876543210ULL,0x5ULL},{EGL_TRUE,EGL_FALSE},2);
    assert(std::get<0>(valid) && std::get<1>(valid)==1);
    assert(std::get<2>(valid).find("0xFEDCBA9876543210[externalOnly=true]")!=std::string::npos);
    assert(std::get<2>(valid).find("0x0000000000000005[externalOnly=false]")!=std::string::npos);
    assert(!std::get<0>(record({0x1ULL},{27},1)));
    const auto empty=record({}, {}, 0);
    assert(std::get<0>(empty) && std::get<1>(empty)==0 && std::get<2>(empty).empty());
}
'''
    with tempfile.TemporaryDirectory(prefix='ogles220-') as tmp:
        src=Path(tmp)/'test.cpp'; exe=Path(tmp)/'test';src.write_text(fixture)
        result=subprocess.run([compiler,'-std=c++20','-O2','-Wall','-Wextra','-Werror',str(src),'-o',str(exe)],capture_output=True,text=True,timeout=30)
        require(result.returncode==0,'C++20 device/modifier fixture failed to compile: '+result.stderr[:3000])
        result=subprocess.run([str(exe)],capture_output=True,text=True,timeout=10)
        require(result.returncode==0,'C++20 device/modifier fixtures failed: '+result.stderr[:1000])
def verify():
    lock=json.loads((ROOT/CONTRACT).read_text())
    require(lock['release']=='2.2.20' and lock['predecessor']=='2.2.19','release identity drift')
    require(len(lock['predecessorHashes'])==160,'160 production files required')
    require(lock['predecessorZipSha256']=='57766d51306a42afc59e2554ccfbcfe91c6cc48ca886233eafa4a967cb2d2877','immutable predecessor ZIP hash drift')
    for rel,expected in lock['predecessorHashes'].items():
        if rel not in {NATIVE,GRADLE}:require(sha(rel)==expected,'unreviewed production mutation: '+rel)
    gradle=(ROOT/GRADLE).read_text()
    require('releaseVersionName = "2.2.20"' in gradle and 'releaseVersionCode = 2220' in gradle,'release identity drift')
    native=(ROOT/NATIVE).read_text()
    source_oracle(native); compiled_oracle(native)
    require(sha('registry/gl.xml')==json.loads((ROOT/'registry/registry_lock.json').read_text())['registrySha256'],'GL registry drift')
    require(sha('registry/egl.xml')==json.loads((ROOT/'registry/registry_lock.json').read_text())['eglRegistrySha256'],'EGL registry drift')
    print('verify_2_2_20_device_dma_integrity: PASS (160-source hash baseline; host C++20 device-error / stable-count / raw 64-bit modifier tests)')
if __name__=='__main__':main_guard('verify_2_2_20_device_dma_integrity',verify)
