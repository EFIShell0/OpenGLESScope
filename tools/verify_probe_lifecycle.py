#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    svc=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt').read_text()
    for token in ['probeMutex.withLock','ensureOpenGlesProbeProcessQuiescent','runningOpenGlesProbePids','EXTRA_TERMINAL_PATH','EXTRA_TIMEOUT_MS','terminalFile','strictOpenGlesProbeTerminalCandidate','withTimeout(timeoutMs)','TimeoutCancellationException','CancellationException','NonCancellable','maxResultBytes','Recovered an atomic OpenGL ES probe publication at the timeout boundary']:
        require(token in main,f'main probe lifecycle contract missing: {token}')
    require('const val EXTRA_TERMINAL_PATH = "terminal_path"' in svc,'service terminal-path extra contract drift')
    for token in ['AtomicBoolean','hardTimeoutWatchdog','timeoutMs + 100L','validateTerminalResult','EXTRA_TERMINAL_PATH','requestedTerminal.path != requestedResult.path + ".done"','output.fd.sync()','Os.rename','terminalPayloadValid','writeResult(requestedTerminal, "done")','terminateDedicatedProcess','Process.killProcess(Process.myPid())','worker.shutdownNow()']:
        require(token in svc,f'service one-shot terminal contract missing: {token}')
    contract=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text()
    require('OpenGLESProbeContract.isTerminalJson(candidate, selfTest)' in main,'main shared strict terminal contract missing')
    require('OpenGLESProbeContract.isTerminalJson(file.readText(Charsets.UTF_8), selfTest)' in svc,'service shared strict terminal contract missing')
    for token in ['hasStrictJsonGrammar','!names.add(name)','isCompleteAvailableReport','isEglConfigArray','isUnavailableAttributeArray','MAX_JSON_DEPTH','MAX_JSON_CONTAINER_ITEMS']:
        require(token in contract,f'shared strict terminal schema contract missing: {token}')
    require('resultFile.isFile && resultFile.length() > maxResultBytes' in main,'oversize result termination guard missing')
    require('selfTest = true, timeoutMs = 20_000L, maxResultBytes = 1024L * 1024L' in main,'self-test bounded one-shot path missing')

if __name__=='__main__': main_guard('verify_probe_lifecycle',verify)
