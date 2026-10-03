#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text()
    service=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt').read_text()
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    for token in ['kMaxGlEnumerationCount = 16384','kMaxEglConfigCount = 4096','kMaxProgramBinaryBytes = 8 * 1024 * 1024','kMaxRuntimeStringBytes = 1024 * 1024','kMaxExtensionTokenBytes = 4096','kMaxInfoLogBytes = 1024 * 1024','kMaxInternalFormatSampleCounts = 64']:
        require(token in native,f'native resource ceiling missing: {token}')
    for token in ['Mutex','withLock','EXTRA_TERMINAL_PATH','fd.sync','Os.rename']:
        require(token in service or token in main,f'collection concurrency/publication contract missing: {token}')
    require('MAX_RESULT_BYTES = 8L * 1024L * 1024L' in service,'base-report size ceiling missing')
    require('20_000' in main or '20 * 1000' in main or 'SELF_TEST_TIMEOUT' in main,'self-test timeout ceiling missing')
    require('131072' in (ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text(),'JSON container-item ceiling missing')
if __name__=='__main__': main_guard('verify_concurrency_resource_contracts',verify)
