#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(); changelog=(ROOT/'changelog.md').read_text(); main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(); native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text()
    for heading in ['Non-negotiable','Mandatory evidence workflow for every future change','Build gate','Current upstream and specification baseline','Evidence-state semantics','Crash / lifecycle / terminal-publication contract','Registry and canonical-name contract','Negative-mutation gate','Deterministic package and clean-extract gate','Release 1.2.1']:
        require(heading in rules,f'PROJECT_RULES missing methodology section: {heading}')
    require('OpenGLESScope 1.2.1' in changelog,'changelog missing 1.2.1')
    require('GL_RED_BITS' in native and 'GL_STENCIL_BITS' in native,'new framebuffer bit queries missing')
    require('glGetInternalformativ' in native and 'internalFormats' in main,'internal-format sample reporting missing')
    require('GL_COMPRESSED_LUMINANCE_LATC1_EXT' in native and 'GL_COMPRESSED_LUMINANCE_LATC1_NV' not in native,'canonical LATC correction missing')
    require('EGL_CONTEXT_LOST' in native,'canonical EGL_CONTEXT_LOST error mapping missing')
    require('archiveVersion != update.version' in main,'exact update release binding missing')
    require('Database returned a malformed report ID' in main,'strict DB report-id success gate missing')
    require('.put("schemaVersion", 3)' in main and '.put("internalFormats", internalFormats)' in main,'technicalReport v3 internal-format Database transport missing')
    require((ROOT/'rules/1.2.1_FULL_CORRECTNESS_SECURITY_SPEC_AND_METHOD_AUDIT.md').is_file(),'1.2.1 method audit missing')

if __name__=='__main__': main_guard('verify_1_2_1',verify)
