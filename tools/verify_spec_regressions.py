#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import json

def verify():
    lock=json.loads((ROOT/'registry/registry_lock.json').read_text())
    require(lock['apiBaseline']=='OpenGL ES 3.2','OpenGL ES baseline drift')
    require(lock['glslEsBaseline']=='3.20','GLSL ES baseline drift')
    require(lock['eglBaseline']=='1.5','EGL baseline drift')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text()
    # Internal-format support must be runtime-query evidence, not inferred from API/version/format name.
    exact='glGetInternalformativ(target, format.value, GL_NUM_SAMPLE_COUNTS, 1, &count);'
    require(exact in native,'internal-format sample-count query call missing')
    require('glGetInternalformativ(target, format.value, GL_SAMPLES, count, samples.data());' in native,'internal-format sample enumeration call missing')
    require('GL_FRAGMENT_SHADING_RATE_PRIMITIVE_RATE_WITH_MULTI_VIEWPORT_SUPPORTED_EXT' not in native,'desktop-only fragment shading-rate state leaked into GLES')
    require('GL_EXT_fragment_shading_rate_attachment' not in native,'fabricated fragment-shading-rate attachment extension must not return')
    require('if (hasExt(glExt, "GL_EXT_fragment_shading_rate"))' in native and 'GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT' in native,'attachment fragment shading-rate queries must remain under registered GL_EXT_fragment_shading_rate')
    require('GL_NV_internalformat_sample_query' in native and 'glGetInternalformatSampleivNV' in native,'NV internal-format detail path missing')
    require('glCode >= 310' in native and 'glCode >= 320 || hasExt(glExt, "GL_OES_texture_storage_multisample_2d_array")' in native,'internal-format target API dependency gates missing')
    # Runtime strings and extension presence are evidence, never capability substitutes.
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    for phrase in ['Registry presence does not prove runtime support','Extension-name presence alone does not prove a query result','Unknown / not queried']:
        require(phrase in rules,f'spec evidence rule missing: {phrase}')
if __name__=='__main__': main_guard('verify_spec_regressions',verify)
