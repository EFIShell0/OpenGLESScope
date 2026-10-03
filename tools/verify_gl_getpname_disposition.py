#!/usr/bin/env python3
from __future__ import annotations
import xml.etree.ElementTree as ET
from gate_common import ROOT, require, main_guard

# These registry GetPName tokens are deliberately not implementation-capability evidence.
# They are mutable/current state or persistent identifiers. Keeping this list explicit makes
# newly-added registry query tokens fail closed until a spec audit classifies them.
MUTABLE_STATE={
 'GL_ALPHA_TEST_FUNC_QCOM','GL_ALPHA_TEST_QCOM','GL_ALPHA_TEST_REF_QCOM',
 'GL_BLEND','GL_BLEND_DST_ALPHA','GL_BLEND_DST_RGB','GL_BLEND_EQUATION_ALPHA','GL_BLEND_EQUATION_RGB','GL_BLEND_SRC_ALPHA','GL_BLEND_SRC_RGB',
 'GL_CLIP_DEPTH_MODE_EXT','GL_CLIP_ORIGIN_EXT','GL_COLOR_WRITEMASK','GL_DEPTH_RANGE','GL_DRAW_BUFFER_EXT','GL_FETCH_PER_SAMPLE_ARM',
 'GL_FRAMEBUFFER_FETCH_NONCOHERENT_QCOM','GL_PACK_ROW_LENGTH_NV','GL_PACK_SKIP_PIXELS_NV','GL_PACK_SKIP_ROWS_NV','GL_READ_BUFFER_EXT','GL_READ_BUFFER_NV',
 'GL_SAMPLER_BINDING','GL_SCISSOR_BOX','GL_SCISSOR_TEST','GL_SHADING_RATE_EXT','GL_SHADING_RATE_IMAGE_PER_PRIMITIVE_NV','GL_SHADING_RATE_QCOM',
 'GL_TEXTURE_2D','GL_VIEWPORT','GL_TIMESTAMP_EXT'
}
PRIVACY_IDENTIFIERS={'GL_DEVICE_LUID_EXT','GL_DEVICE_NODE_MASK_EXT','GL_DEVICE_UUID_EXT','GL_DRIVER_UUID_EXT'}
IMPLEMENTATION_CAPABILITIES={'GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM','GL_RESET_NOTIFICATION_STRATEGY_KHR','GL_CONTEXT_ROBUST_ACCESS_KHR','GL_RESET_NOTIFICATION_STRATEGY_EXT','GL_CONTEXT_ROBUST_ACCESS_EXT'}

def verify():
    xml=ET.parse(ROOT/'registry/gl.xml').getroot()
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    owners={}
    for ext in xml.findall('./extensions/extension'):
        if 'gles2' not in (ext.get('supported') or '').split('|'): continue
        for req in ext.findall('require'):
            if req.get('api') not in (None,'gles2'): continue
            for e in req.findall('enum'): owners.setdefault(e.get('name'),set()).add(ext.get('name'))
    groups={}
    for block in xml.findall('enums'):
        for e in block.findall('enum'): groups.setdefault(e.get('name'),set()).update((e.get('group') or '').split(','))
    all_ext_getp={n for n in owners if 'GetPName' in groups.get(n,set())}
    require(len(all_ext_getp)==84,f'GLES extension GetPName census drifted: {len(all_ext_getp)}')
    excluded=MUTABLE_STATE|PRIVACY_IDENTIFIERS
    require(excluded <= all_ext_getp,'classified extension GetPName name disappeared from current registry')
    capability=all_ext_getp-excluded
    require(len(MUTABLE_STATE)==31,f'mutable/volatile extension GetPName baseline drift: {len(MUTABLE_STATE)}')
    require(len(PRIVACY_IDENTIFIERS)==4,'privacy identifier baseline drift')
    require(len(capability)==49,f'implementation/capability extension GetPName baseline drift: {len(capability)}')
    for n in capability:
        require(f'\"{n}\"' in native,f'implementation/capability GetPName token has no explicit report/query identity: {n}')
    for n in MUTABLE_STATE:
        require(f'\"{n}\"' not in native,f'mutable/volatile state must not be reported as an implementation capability: {n}')
    for n in IMPLEMENTATION_CAPABILITIES:
        require(f'\"{n}\"' in native,f'implementation-dependent capability must be collected: {n}')
    require('addBooleanLimit(o, first, "GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM"' in native,'ARM MRT framebuffer-fetch capability must use boolean query semantics')
    require('queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_KHR", 0x8256, resetKhrApplicable' in native,'KHR reset notification strategy must use integer query semantics')
    require('queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_KHR", 0x90F3' in native,'KHR robust-access state must use boolean query semantics')
    require('queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_EXT", 0x8256, resetExtApplicable' in native,'EXT reset notification strategy must use integer query semantics')
    require('queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_EXT", 0x90F3, resetExtApplicable' in native,'EXT robust-access state must use boolean query semantics')
    require('const bool contextFlagsApplicable = glCode >= 320;' in native,'GL_CONTEXT_FLAGS must be ES 3.2 gated')
    require(r'\"resetNotificationStrategy\":' in native and r'\"resetNotificationStrategyQuery\":' in native and r'\"robustAccessQuery\":' in native,'GL runtime strategy/provenance evidence must be serialized')
    for n in PRIVACY_IDENTIFIERS:
        require(n not in native,f'persistent GL device identifier must not be collected: {n}')

if __name__=='__main__': main_guard('verify_gl_getpname_disposition',verify)
