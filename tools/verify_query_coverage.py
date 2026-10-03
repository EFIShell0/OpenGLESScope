#!/usr/bin/env python3
from __future__ import annotations
import csv, re, xml.etree.ElementTree as ET
from gate_common import ROOT, require, main_guard

def verify():
    root=ET.parse(ROOT/'registry/gl.xml').getroot(); native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text()
    getp=set()
    for group in root.findall('enums'):
        for e in group.findall('enum'):
            if 'GetPName' in (e.get('group') or '').split(','): getp.add(e.get('name'))
    core=set()
    for f in root.findall('feature'):
        if f.get('api')!='gles2': continue
        if tuple(map(int,f.get('number').split('.')))>(3,2): continue
        for req in f.findall('require'):
            for e in req.findall('enum'): core.add(e.get('name'))
    cap_patterns=(r'^GL_MAX_',r'^GL_MIN_',r'^GL_NUM_',r'_BITS$',r'^GL_SUBPIXEL_BITS$',r'^GL_SHADER_COMPILER$')
    required=sorted(n for n in core&getp if any(re.search(p,n) for p in cap_patterns))
    missing=[n for n in required if n not in native]
    require(not missing,'missing core GLES 2.0-3.2 capability GetPName queries: '+', '.join(missing))
    require(len(required)==91,f'locked core capability census changed unexpectedly: {len(required)}')
    # Default collector framebuffer capability state is deliberately included, not inferred from EGL config metadata.
    for token in ['GL_RED_BITS','GL_GREEN_BITS','GL_BLUE_BITS','GL_ALPHA_BITS','GL_DEPTH_BITS','GL_STENCIL_BITS']:
        require(token in native,f'missing current collector framebuffer capability query {token}')
    # Extension-gated scalar/query coverage retained from audited prior releases.
    ext_tokens=[
      'GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT','GL_MAX_DEBUG_MESSAGE_LENGTH','GL_MAX_DEBUG_LOGGED_MESSAGES','GL_MAX_DEBUG_GROUP_STACK_DEPTH','GL_MAX_LABEL_LENGTH',
      'Query counter bits: GL_TIME_ELAPSED_EXT','Query counter bits: GL_TIMESTAMP_EXT','GL_MAX_DUAL_SOURCE_DRAW_BUFFERS_EXT','GL_MAX_VIEWS_OVR','GL_MAX_MULTIVIEW_BUFFERS_EXT',
      'GL_MAX_TEXTURE_BUFFER_SIZE_EXT','GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT','GL_MAX_CLIP_DISTANCES_EXT','GL_MAX_CULL_DISTANCES_EXT','GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES_EXT',
      'GL_SUBGROUP_SIZE_KHR','GL_SUBGROUP_SUPPORTED_STAGES_KHR','GL_SUBGROUP_SUPPORTED_FEATURES_KHR','GL_SUBGROUP_QUAD_ALL_STAGES_KHR','GL_MAX_WINDOW_RECTANGLES_EXT',
      'GL_MAX_VIEWPORTS_OES','GL_VIEWPORT_SUBPIXEL_BITS_OES','GL_VIEWPORT_BOUNDS_RANGE_OES','GL_VIEWPORT_INDEX_PROVOKING_VERTEX_OES',
      'GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_FAST_SIZE_EXT','GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_SIZE_EXT','GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_FAST_SIZE_EXT','GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_SIZE_EXT',
      'GL_MIN_SAMPLE_SHADING_VALUE_OES','GL_MAX_SPARSE_TEXTURE_SIZE_EXT','GL_MAX_SPARSE_3D_TEXTURE_SIZE_EXT','GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_EXT','GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_EXT',
      'GL_FRAGMENT_SHADING_RATE_WITH_SHADER_DEPTH_STENCIL_WRITES_SUPPORTED_EXT','GL_FRAGMENT_SHADING_RATE_WITH_SAMPLE_MASK_SUPPORTED_EXT','GL_FRAGMENT_SHADING_RATE_NON_TRIVIAL_COMBINERS_SUPPORTED_EXT',
      'GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT','GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT','GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_HEIGHT_EXT','GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_HEIGHT_EXT','GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_ASPECT_RATIO_EXT','GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_LAYERS_EXT','GL_FRAGMENT_SHADING_RATE_ATTACHMENT_WITH_DEFAULT_FRAMEBUFFER_SUPPORTED_EXT',
      'GL_NUM_DEVICE_UUIDS_EXT','GL_MAX_TIMELINE_SEMAPHORE_VALUE_DIFFERENCE_NV','GL_MAX_TASK_UNIFORM_BLOCKS_EXT','GL_MAX_MESH_UNIFORM_BLOCKS_EXT','GL_MAX_TASK_WORK_GROUP_TOTAL_COUNT_EXT','GL_MAX_MESH_WORK_GROUP_TOTAL_COUNT_EXT','GL_MESH_PREFERS_COMPACT_PRIMITIVE_OUTPUT_EXT','GL_MAX_TASK_WORK_GROUP_COUNT_EXT','GL_MAX_MESH_WORK_GROUP_SIZE_EXT']
    require(all(x in native for x in ext_tokens),'audited extension capability query set regressed')
    require('GL_EXT_fragment_shading_rate_attachment' not in native,'fabricated GL_EXT_fragment_shading_rate_attachment extension name must not be used')
    require('if (hasExt(glExt, "GL_EXT_fragment_shading_rate"))' in native and 'GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT' in native,'fragment shading-rate attachment queries must be gated by registered GL_EXT_fragment_shading_rate')
    require('GL_FRAGMENT_SHADING_RATE_PRIMITIVE_RATE_WITH_MULTI_VIEWPORT_SUPPORTED_EXT' not in native,'OpenGL ES must not query the desktop-only primitive multi-viewport state')
    require('glGetFragmentShadingRatesEXT' in native and 'addFragmentShadingRates(o, first, getFragmentShadingRatesExt, 1)' in native and 'addFragmentShadingRates(o, first, getFragmentShadingRatesExt, 4)' in native,'fragment shading-rate supported-rate enumeration missing')
    require('addLimit64(o, first, "GL_MAX_TIMELINE_SEMAPHORE_VALUE_DIFFERENCE_NV"' in native,'timeline semaphore 64-bit capability query regressed')
    require('const bool contextFlagsApplicable = glCode >= 320;' in native and 'contextFlagsApplicable = glCode >= 300' not in native,'GL_CONTEXT_FLAGS applicability must start at OpenGL ES 3.2')
    require('const bool resetCoreApplicable = glCode >= 320;' in native and 'queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY", 0x8256, true' in native,'OpenGL ES 3.2 reset notification strategy query missing')
    require('hasExt(glExt, "GL_KHR_robustness")' in native and 'queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_KHR", 0x8256, resetKhrApplicable' in native,'KHR robustness reset-strategy query missing for pre-3.2 contexts')
    require('queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_KHR", 0x90F3' in native,'KHR robust-access boolean state query missing')
    require('hasExt(glExt, "GL_EXT_robustness")' in native and 'queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_EXT", 0x8256, resetExtApplicable' in native and 'queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_EXT", 0x90F3, resetExtApplicable' in native,'EXT robustness query path missing')
    require('GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT' in native and 'GL_CONTEXT_FLAGS / GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT' in native,'core robust-access provenance missing')
    for token in ['GL_MESH_PREFERS_LOCAL_INVOCATION_VERTEX_OUTPUT_EXT','GL_MESH_PREFERS_LOCAL_INVOCATION_PRIMITIVE_OUTPUT_EXT','GL_MESH_PREFERS_COMPACT_VERTEX_OUTPUT_EXT','GL_MESH_PREFERS_COMPACT_PRIMITIVE_OUTPUT_EXT']:
        require(f'addBooleanLimit(o, first, "{token}"' in native,f'mesh-shader boolean query helper drift: {token}')
    for token in ['GL_MAX_TASK_WORK_GROUP_COUNT_EXT','GL_MAX_MESH_WORK_GROUP_COUNT_EXT','GL_MAX_TASK_WORK_GROUP_SIZE_EXT','GL_MAX_MESH_WORK_GROUP_SIZE_EXT']:
        require(f'addIndexedLimit(o, first, "{token}"' in native,f'mesh-shader indexed query helper drift: {token}')
    for token in ['GL_NUM_COMPRESSED_TEXTURE_FORMATS','GL_COMPRESSED_TEXTURE_FORMATS','GL_NUM_SHADER_BINARY_FORMATS','GL_SHADER_BINARY_FORMATS','GL_NUM_PROGRAM_BINARY_FORMATS','GL_PROGRAM_BINARY_FORMATS','glGetShaderPrecisionFormat']:
        require(token in native,f'enumeration/precision query missing: {token}')
    require('glGetInternalformativ(target, format.value, GL_NUM_SAMPLE_COUNTS, 1, &count);' in native,'internal-format NUM_SAMPLE_COUNTS call missing')
    require('glGetInternalformativ(target, format.value, GL_SAMPLES, count, samples.data());' in native,'internal-format SAMPLES call missing')
    for token in ['GL_NUM_SAMPLE_COUNTS','GL_SAMPLES','kRenderableInternalFormats','kMaxInternalFormatSampleCounts']:
        require(token in native,f'internal-format capability census missing: {token}')
    require(native.count('{GL_') >= 41,'renderable internal-format census unexpectedly small')
    require('GL_NV_internalformat_sample_query' in native and 'glGetInternalformatSampleivNV' in native,'NV per-sample internal-format detail path missing')
    require('glCode >= 310' in native and 'glCode >= 320 || hasExt(glExt, "GL_OES_texture_storage_multisample_2d_array")' in native,'internal-format target version/extension gating missing')
    matrix=ROOT/'PUBLIC_CAPABILITY_REFERENCE_MATRIX.csv'
    require(matrix.is_file(),'public capability matrix missing')
    with matrix.open(newline='',encoding='utf-8') as f: rows=list(csv.DictReader(f))
    floor=[r for r in rows if r.get('reference')=='External public OpenGL ES capability reference']
    extras=[r for r in rows if r.get('reference')=='OpenGLESScope additional query']
    require(len(floor)==145 and all(r.get('status')=='parity' and r.get('ui_txt_html_database')=='yes' for r in floor),'145-row public reference parity contract regressed')
    require(len(extras)==134,'134-row additional query contract regressed')
    # Registry-derived extension capability candidate gate. Any GLES2 extension GetPName token
    # that has an implementation-limit/capability-shaped name must be collected or the release
    # must explicitly change this gate with specification evidence. Mutable current-state tokens
    # intentionally do not match this candidate grammar.
    enum_groups={}
    for group in root.findall('enums'):
        for enum in group.findall('enum'):
            enum_groups.setdefault(enum.get('name'),set()).update((enum.get('group') or '').split(','))
    gles2_extension_enums=set()
    for extension in root.findall('./extensions/extension'):
        if 'gles2' not in (extension.get('supported') or '').split('|'): continue
        for req in extension.findall('require'):
            if req.get('api') not in (None,'gles2'): continue
            for enum in req.findall('enum'): gles2_extension_enums.add(enum.get('name'))
    candidate_pattern=re.compile(r'^(GL_(MAX|MIN|NUM)_.+|GL_.+_BITS(?:_.+)?|GL_.+_SIZE(?:_.+)?|GL_.+_ALIGNMENT(?:_.+)?|GL_.+_SUPPORTED(?:_.+)?)$')
    registry_candidates=sorted(name for name in gles2_extension_enums if name and 'GetPName' in enum_groups.get(name,set()) and candidate_pattern.match(name))
    registry_missing=[name for name in registry_candidates if name not in native]
    require(not registry_missing,'registry-derived GLES2 extension capability GetPName queries missing: '+', '.join(registry_missing))
    for token in ['GL_MAX_COLOR_ATTACHMENTS_NV','GL_SHADER_CORE_COUNT_ARM','GL_SHADER_CORE_ACTIVE_COUNT_ARM','GL_SHADER_CORE_MAX_WARP_COUNT_ARM','GL_SHADER_CORE_PIXEL_RATE_ARM','GL_SHADER_CORE_TEXEL_RATE_ARM','GL_SHADER_CORE_FMA_RATE_ARM','GL_MOTION_ESTIMATION_SEARCH_BLOCK_X_QCOM','GL_MOTION_ESTIMATION_SEARCH_BLOCK_Y_QCOM','GL_SHADING_RATE_IMAGE_TEXEL_WIDTH_NV','GL_SHADING_RATE_IMAGE_TEXEL_HEIGHT_NV','GL_SHADING_RATE_IMAGE_PALETTE_SIZE_NV','GL_MAX_COARSE_FRAGMENT_SAMPLES_NV','GL_SHADING_RATE_IMAGE_PALETTE_COUNT_NV','GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM']:
        require(token in native,f'current registry implementation-dependent query missing: {token}')
    require('addHexLimit64(o, first, "GL_SHADER_CORE_PRESENT_MASK_ARM"' in native,'ARM shader-core presence mask must use 64-bit bitfield evidence')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    block=main[main.index('private val OPENGL_ES_32_MINIMUMS'):main.index('private val GL_QUERY_DEPENDENCIES')]
    require(block.count('GlMinimum(')==104,'104-entry OpenGL ES 3.2 minimum/maximum reference set regressed')

if __name__=='__main__': main_guard('verify_query_coverage',verify)
