#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(); changelog=(ROOT/'changelog.md').read_text(); main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(); native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(); contract=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text()
    for heading in ['Non-negotiable','Mandatory evidence workflow for every future change','Build gate','Current upstream and specification baseline','Evidence-state semantics','Crash / lifecycle / terminal-publication contract','Registry and canonical-name contract','Negative-mutation gate','Deterministic package and clean-extract gate','Release 1.4.0']:
        require(heading in rules,f'PROJECT_RULES missing methodology section: {heading}')
    require('OpenGLESScope 1.4.0' in changelog,'changelog missing 1.4.0')
    require('createBestContext' in native and 'canCreateEs3 = eglCode >= 150 || hasKhrCreateContext' in native,'legal ES3 EGL context creation gate missing')
    for token in ['EGL_CONTEXT_MINOR_VERSION", "Not applicable"','EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY", "Not applicable"','EGL_CONTEXT_FLAGS_KHR", "Not applicable"','EGL_CONTEXT_OPENGL_NO_ERROR_KHR", "Not applicable"']:
        require(token in native,f'creation-only EGL evidence semantics missing: {token}')
    for token in ['eglQueryDmaBufFormatsEXT','eglQueryDmaBufModifiersEXT','EGL_TRACK_REFERENCES_KHR','eglGetDisplayDriverName','GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM']:
        require(token in native,f'1.4.0 specification/query evidence missing: {token}')
    require('kMaxEglCapabilityCount = 256' in native and 'eglCapabilityOverflow' in native,'EGL capability bounded fail-closed collector missing')
    for token in ['MAX_EGL_CAPABILITIES = 256','MAX_EGL_CONFIGS = 4096','MAX_LIMITS = 8192','uniqueObjectArray']:
        require(token in contract,f'strict producer report contract missing: {token}')
    require('MessageDigest.getInstance("SHA-256")' in main,'history SHA-256 identity missing')
    require('LaunchedEffect(currentSnapshot)' in main and 'LaunchedEffect(currentSnapshot.toString())' not in main,'large JSON Compose effect-key regression')
    require('history = withContext(Dispatchers.IO) { listAnalysisHistory(context) }' in main,'history metadata IO is not off-main')
    for token in ['GL_RESET_NOTIFICATION_STRATEGY','GL_RESET_NOTIFICATION_STRATEGY_KHR','GL_CONTEXT_ROBUST_ACCESS_KHR']:
        require(token in native,f'robustness runtime state evidence missing: {token}')
    require('class ActiveDiagnosticsScope' in native,'native diagnostic lifetime RAII guard missing')
    require('withContext(Dispatchers.IO) { runCatching { loadAnalysisHistory(record.file) } }' in main,'history body load must remain on IO dispatcher')
    require((ROOT/'tools/verify_gl_getpname_disposition.py').is_file(),'GL GetPName disposition gate missing')
    require((ROOT/'tools/verify_egl_query_legality.py').is_file(),'EGL legality gate missing')
    require((ROOT/'rules/1.4.0_FULL_CORRECTNESS_MEMORY_SPEC_SECURITY_AUDIT.md').is_file(),'1.4.0 full audit missing')
if __name__=='__main__': main_guard('verify_1_4_0',verify)
