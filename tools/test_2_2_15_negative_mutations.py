#!/usr/bin/env python3
"""Demonstrate that the 2.2.15 native evidence contract catches actual regressions."""
from __future__ import annotations
from gate_common import ROOT,require,main_guard
from verify_2_2_15_evidence_integrity import native_oracle


def verify():
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    native_oracle(native)
    mutants=[
        ('utf8 surrogate loss', 'unicodeEscape(0xD800u + (cp >> 10))', 'unicodeEscape(cp)'),
        ('utf8 byte corruption', 'if (c < 0x80)', 'if (c < 0x20 || c >= 0x80)'),
        ('unicode nonfinite', '!std::isfinite(v)', 'false'),
        ('float precision loss', 'std::numeric_limits<GLfloat>::max_digits10', '6'),
        ('extension completeness lost', 'if (!glExtComplete)', 'if (glExt.empty())'),
        ('EGL display parse ignored', 'if (!displayExtComplete)', 'if (displayExt.empty())'),
        ('EGL client parse ignored', 'if (clientExtensionText && !clientExtComplete)', 'if (false)'),
        ('duplicate extension normalization', 'if (out.size() != received) return {}', 'if (false) return {}'),
        ('unbounded GL extension helper', 'static std::vector<std::string> glExtensions(int glCode, bool& complete)', 'static std::vector<std::string> glExtensions(int glCode)'),
        ('required format failure ignored', 'if (!mandatoryEnumerationsComplete)', 'if (false)'),
        ('no failure propagation', 'mandatoryEnumerationsComplete = false', 'mandatoryEnumerationsComplete = true'),
        ('legacy EGL false state', 'eglCode < 150 ? "Not applicable" : "Unavailable"', '"Available"'),
        ('invalid driver UTF8 trusted', 'static bool runtimeStringValid(const char* s)', 'static bool runtimeStringTrusted(const char* s)'),
        ('GLES2 parse not checked', 'out = splitExt(extensionString, &complete);', 'out = splitExt(extensionString);'),
        ('self-test uses obsolete overload', 'glExtensions(glCode, selfTestGlExtComplete)', 'glExtensions(glCode)'),
        ('self-test EGL missing list silent', 'if (!selfTestDisplayExtComplete)', 'if (false)'),
        ('self-test GL missing list silent', 'if (!selfTestGlExtComplete)', 'if (false)'),
        ('device extension missing list silent', 'if (!deviceExtComplete)', 'if (false)'),
        ('device extension falsely available', 'extComplete ? "Available" : "Unavailable"', '"Available"'),
    ]
    count=0
    for title,needle,replacement in mutants:
        require(needle in native, 'mutant fixture stale: '+title)
        changed=native.replace(needle,replacement,1)
        try:native_oracle(changed)
        except (AssertionError, SystemExit, ValueError):count+=1
        else: raise AssertionError('negative mutation escaped evidence audit: '+title)
    require(count==len(mutants),'not all negative mutations were executed')
    print(f'test_2_2_15_negative_mutations: PASS ({count} deliberate regressions rejected)')

if __name__=='__main__':main_guard('test_2_2_15_negative_mutations',verify)
