#!/usr/bin/env python3
"""3.0.6 x86_64 gfxstream-style EGL fallback and nonterminal-evidence safeguards."""
from gate_common import ROOT,require,main_guard
from pathlib import Path

CPP='app/src/main/cpp/openglesscope.cpp'
CONTRACT='app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt'
SERVICE='app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt'

def verify_sources(cpp,contract,service):
    require('const int minors[] = {2, 1, 0};' in cpp and 'EGL_CONTEXT_MINOR_VERSION_KHR' in cpp and 'for (size_t i = 0; i < 3; ++i)' in cpp,
            'ES 3.2/3.1/3.0 fallback removed')
    require('EGL_CONTEXT_CLIENT_VERSION, 2' in cpp and 'EGL_OPENGL_ES2_BIT' in cpp,
            'ES 2 fallback removed')
    require('reconcileDiagnosticIdentities(diagnostics);' in cpp and
            'previous->status = "Unavailable"' in cpp,
            'conflicting diagnostic identity can remain successful/duplicated')
    require('Repeated query identity returned inconsistent status or error evidence' in cpp,
            'conflicting query status is not explained')
    require('GL_SAMPLES returned a duplicate sample count' in cpp and 'samples.clear();' in cpp and
            'status = "Unavailable";' in cpp,
            'malformed internal-format sample vector could be reported available')
    require('diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE)' in cpp,
            'invalid GL extension enumeration must not silently disappear')
    for key in ('compressedFormats','shaderBinaryFormats','programBinaryFormats'):
        require(f'!isBoundedStringArray(root.opt("{key}"), MAX_ENUMERATION_ITEMS)' in contract,
                f'raw repeated driver {key} incorrectly turns terminal report unavailable')
        require(f'"{key}" to isBoundedStringArray(root.opt("{key}"), MAX_ENUMERATION_ITEMS)' in contract,
                f'{key} failure provenance missing')
    require('!isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS)' in contract and
            '!isUniqueStringArray(egl.opt("extensions"), MAX_ENUMERATION_ITEMS)' in contract and
            '!isDiagnosticArray(root.opt("diagnostics"))' in contract,
            'strict extension/diagnostic uniqueness weakened')
    require('array.length() > maxItems' in contract and 'array.opt(index) !is String' in contract,
            'raw format enumeration bounds/types bypassed')
    require('if (!hasStrictJsonGrammar(candidate)) return "strict JSON grammar or duplicate object key"' in contract,
            'duplicate JSON keys could be accepted')
    require('val validationIssue = runCatching {' in service and
            'OpenGLESProbeContract.terminalValidationIssue(requestedResult.readText(Charsets.UTF_8), selfTest)' in service and
            'Log.e("OpenGLESProbeWork", "Native probe evidence rejected: $validationIssue")' in service and
            'Native probe report validation failed: $validationIssue' in service,
            'nonterminal probe failure must publish actionable reason')
    require('terminalPayloadValid && writeResult(requestedTerminal, "done")' in service,
            'invalid result could be marked terminal')


def verify():
    cpp=(ROOT/CPP).read_text(encoding='utf-8')
    contract=(ROOT/CONTRACT).read_text(encoding='utf-8')
    service=(ROOT/SERVICE).read_text(encoding='utf-8')
    verify_sources(cpp,contract,service)
    mutations=[
        ('remove diagnostic reconciliation','cpp','reconcileDiagnosticIdentities(diagnostics);',''),
        ('mark conflicts Available','cpp','previous->status = "Unavailable"','previous->status = "Available"'),
        ('report invalid format available','cpp','GL_SAMPLES returned a duplicate sample count','GL_SAMPLES reported Available despite duplicate counts'),
        ('remove raw compressed enumeration support','contract','!isBoundedStringArray(root.opt("compressedFormats"), MAX_ENUMERATION_ITEMS)','!isUniqueStringArray(root.opt("compressedFormats"), MAX_ENUMERATION_ITEMS)'),
        ('disable strict extension uniqueness','contract','!isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS)','!isBoundedStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS)'),
        ('disable grammar check','contract','if (!hasStrictJsonGrammar(candidate)) return "strict JSON grammar or duplicate object key"','if (false) return "strict JSON grammar or duplicate object key"'),
        ('erase failure reason','service','Native probe report validation failed: $validationIssue','Native probe report validation failed'),
        ('allow invalid completion marker','service','terminalPayloadValid && writeResult(requestedTerminal, "done")','published && writeResult(requestedTerminal, "done")'),
    ]
    for label,which,before,after in mutations:
        inp={'cpp':cpp,'contract':contract,'service':service}
        require(before in inp[which], 'inactive mutation: '+label)
        inp[which]=inp[which].replace(before,after,1)
        try: verify_sources(**inp)
        except AssertionError: pass
        else: raise AssertionError('negative mutation survived: '+label)
    print('verify_3_0_6_emulator_probe: PASS (8 independent regressions rejected; bounded evidence, ordered ES fallback, terminal publication)')

if __name__=='__main__': main_guard('verify_3_0_6_emulator_probe',verify)
