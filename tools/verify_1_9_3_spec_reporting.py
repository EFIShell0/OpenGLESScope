#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, re, xml.etree.ElementTree as ET
from gate_common import ROOT, require, main_guard

SURFACE_ATTRS = [
    'EGL_GL_COLORSPACE','EGL_VG_ALPHA_FORMAT','EGL_VG_COLORSPACE','EGL_CONFIG_ID','EGL_HEIGHT',
    'EGL_HORIZONTAL_RESOLUTION','EGL_LARGEST_PBUFFER','EGL_MIPMAP_TEXTURE','EGL_MIPMAP_LEVEL',
    'EGL_MULTISAMPLE_RESOLVE','EGL_PIXEL_ASPECT_RATIO','EGL_RENDER_BUFFER','EGL_SWAP_BEHAVIOR',
    'EGL_TEXTURE_FORMAT','EGL_TEXTURE_TARGET','EGL_VERTICAL_RESOLUTION','EGL_WIDTH'
]
JSON_FIELDS = [
    'surfaceGlColorspace','surfaceGlColorspaceQuery','surfaceVgAlphaFormat','surfaceVgAlphaFormatQuery','surfaceVgColorspace','surfaceVgColorspaceQuery','surfaceConfigId','surfaceHeight',
    'surfaceHorizontalResolution','surfaceLargestPbuffer','surfaceMipmapTexture','surfaceMipmapLevel',
    'surfaceMultisampleResolve','surfacePixelAspectRatio','surfaceRenderBuffer','surfaceSwapBehavior',
    'surfaceTextureFormat','surfaceTextureTarget','surfaceVerticalResolution','surfaceWidth'
]

CORE_MUTABLE_OR_OBJECT_STATE = {
    'GL_ACTIVE_TEXTURE',
    'GL_ARRAY_BUFFER_BINDING',
    'GL_BLEND',
    'GL_BLEND_COLOR',
    'GL_BLEND_DST_ALPHA',
    'GL_BLEND_DST_RGB',
    'GL_BLEND_EQUATION',
    'GL_BLEND_EQUATION_ALPHA',
    'GL_BLEND_EQUATION_RGB',
    'GL_BLEND_SRC_ALPHA',
    'GL_BLEND_SRC_RGB',
    'GL_COLOR_CLEAR_VALUE',
    'GL_COLOR_WRITEMASK',
    'GL_CULL_FACE',
    'GL_CULL_FACE_MODE',
    'GL_CURRENT_PROGRAM',
    'GL_DEBUG_GROUP_STACK_DEPTH',
    'GL_DEPTH_CLEAR_VALUE',
    'GL_DEPTH_FUNC',
    'GL_DEPTH_RANGE',
    'GL_DEPTH_TEST',
    'GL_DEPTH_WRITEMASK',
    'GL_DISPATCH_INDIRECT_BUFFER_BINDING',
    'GL_DITHER',
    'GL_DRAW_FRAMEBUFFER_BINDING',
    'GL_ELEMENT_ARRAY_BUFFER_BINDING',
    'GL_FRAGMENT_SHADER_DERIVATIVE_HINT',
    'GL_FRONT_FACE',
    'GL_LINE_WIDTH',
    'GL_PACK_ALIGNMENT',
    'GL_PACK_ROW_LENGTH',
    'GL_PACK_SKIP_PIXELS',
    'GL_PACK_SKIP_ROWS',
    'GL_PIXEL_PACK_BUFFER_BINDING',
    'GL_PIXEL_UNPACK_BUFFER_BINDING',
    'GL_POLYGON_OFFSET_FACTOR',
    'GL_POLYGON_OFFSET_FILL',
    'GL_POLYGON_OFFSET_UNITS',
    'GL_PROGRAM_PIPELINE_BINDING',
    'GL_READ_BUFFER',
    'GL_READ_FRAMEBUFFER_BINDING',
    'GL_RENDERBUFFER_BINDING',
    'GL_SAMPLER_BINDING',
    'GL_SAMPLE_COVERAGE_INVERT',
    'GL_SAMPLE_COVERAGE_VALUE',
    'GL_SCISSOR_BOX',
    'GL_SCISSOR_TEST',
    'GL_SHADER_STORAGE_BUFFER_BINDING',
    'GL_SHADER_STORAGE_BUFFER_SIZE',
    'GL_SHADER_STORAGE_BUFFER_START',
    'GL_STENCIL_BACK_FAIL',
    'GL_STENCIL_BACK_FUNC',
    'GL_STENCIL_BACK_PASS_DEPTH_FAIL',
    'GL_STENCIL_BACK_PASS_DEPTH_PASS',
    'GL_STENCIL_BACK_REF',
    'GL_STENCIL_BACK_VALUE_MASK',
    'GL_STENCIL_BACK_WRITEMASK',
    'GL_STENCIL_CLEAR_VALUE',
    'GL_STENCIL_FAIL',
    'GL_STENCIL_FUNC',
    'GL_STENCIL_PASS_DEPTH_FAIL',
    'GL_STENCIL_PASS_DEPTH_PASS',
    'GL_STENCIL_REF',
    'GL_STENCIL_TEST',
    'GL_STENCIL_VALUE_MASK',
    'GL_STENCIL_WRITEMASK',
    'GL_TEXTURE_BINDING_2D',
    'GL_TEXTURE_BINDING_2D_ARRAY',
    'GL_TEXTURE_BINDING_2D_MULTISAMPLE',
    'GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY',
    'GL_TEXTURE_BINDING_3D',
    'GL_TEXTURE_BINDING_BUFFER',
    'GL_TEXTURE_BINDING_CUBE_MAP',
    'GL_TRANSFORM_FEEDBACK_BUFFER_BINDING',
    'GL_TRANSFORM_FEEDBACK_BUFFER_SIZE',
    'GL_TRANSFORM_FEEDBACK_BUFFER_START',
    'GL_UNIFORM_BUFFER_BINDING',
    'GL_UNIFORM_BUFFER_SIZE',
    'GL_UNIFORM_BUFFER_START',
    'GL_UNPACK_ALIGNMENT',
    'GL_UNPACK_IMAGE_HEIGHT',
    'GL_UNPACK_ROW_LENGTH',
    'GL_UNPACK_SKIP_IMAGES',
    'GL_UNPACK_SKIP_PIXELS',
    'GL_UNPACK_SKIP_ROWS',
    'GL_VERTEX_ARRAY',
    'GL_VERTEX_ARRAY_BINDING',
    'GL_VERTEX_BINDING_DIVISOR',
    'GL_VERTEX_BINDING_OFFSET',
    'GL_VERTEX_BINDING_STRIDE',
}


EXTENSION_MUTABLE_OR_NONCAPABILITY_STATE = {
    'GL_ALPHA_TEST_FUNC_QCOM','GL_ALPHA_TEST_QCOM','GL_ALPHA_TEST_REF_QCOM',
    'GL_BLEND','GL_BLEND_DST_ALPHA','GL_BLEND_DST_RGB','GL_BLEND_EQUATION_ALPHA','GL_BLEND_EQUATION_RGB','GL_BLEND_SRC_ALPHA','GL_BLEND_SRC_RGB',
    'GL_CLIP_DEPTH_MODE_EXT','GL_CLIP_ORIGIN_EXT','GL_COLOR_WRITEMASK','GL_DEPTH_RANGE','GL_DRAW_BUFFER_EXT','GL_FETCH_PER_SAMPLE_ARM',
    'GL_FRAMEBUFFER_FETCH_NONCOHERENT_QCOM','GL_PACK_ROW_LENGTH_NV','GL_PACK_SKIP_PIXELS_NV','GL_PACK_SKIP_ROWS_NV','GL_READ_BUFFER_EXT','GL_READ_BUFFER_NV',
    'GL_SAMPLER_BINDING','GL_SCISSOR_BOX','GL_SCISSOR_TEST','GL_SHADING_RATE_EXT','GL_SHADING_RATE_IMAGE_PER_PRIMITIVE_NV','GL_SHADING_RATE_QCOM',
    'GL_TEXTURE_2D','GL_VIEWPORT','GL_TIMESTAMP_EXT'
}
EXTENSION_PRIVACY_OR_NONANDROID_IDENTIFIERS = {
    'GL_DEVICE_LUID_EXT','GL_DEVICE_NODE_MASK_EXT','GL_DEVICE_UUID_EXT','GL_DRIVER_UUID_EXT'
}

def verify():
    build=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    require(any((f'releaseVersionName = "{name}"' in build and f'releaseVersionCode = {code}' in build) for name,code in [('1.9.3',1903),('2.0.0',2000),('2.1.0',2100),('2.1.1',2101),('2.1.2',2102),('2.1.3',2103),('2.1.4',2104),('2.2.0',2200),('2.2.1',2201),('2.2.2',2202),('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.12',2212),('2.2.13',2213),('2.2.14',2214)]),'1.9.3+ schema-5 release identity missing')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    contract=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text(encoding='utf-8')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main, '1.9.3 technical report schema 5 missing')
    require('.put("glRuntime", JSONObject()' in main, 'technical report schema 5 drops glRuntime')

    # Registry snapshots remain physically present and release-locked.
    for fn,mf in [('gl.xml','gl_registry_manifest.json'),('egl.xml','egl_registry_manifest.json')]:
        p=ROOT/'registry'/fn; require(p.is_file(),f'{fn} missing')
        man=json.loads((ROOT/'registry'/mf).read_text(encoding='utf-8'))
        require(man['sha256']==hashlib.sha256(p.read_bytes()).hexdigest(),f'{fn} SHA-256 manifest mismatch')
        require(man['bytes']==p.stat().st_size,f'{fn} byte-count manifest mismatch')
        require(man.get('verifiedAt')=='2026-09-30',f'{fn} current-main verification date missing')
    glroot=ET.parse(ROOT/'registry/gl.xml').getroot(); eglroot=ET.parse(ROOT/'registry/egl.xml').getroot()
    require(len(glroot.findall('./commands/command'))==3301,'gl.xml command census drift')
    require(len(glroot.findall('./extensions/extension'))==863,'gl.xml extension census drift')
    require(len(eglroot.findall('./commands/command'))==158,'egl.xml command census drift')
    require(len(eglroot.findall('./extensions/extension'))==167,'egl.xml extension census drift')


    # Full core GLES GetPName disposition through ES 3.2. The project intentionally
    # excludes mutable renderer/object/binding state from a capability report; every
    # remaining registry-classified core GetPName token must be represented by the collector.
    groups = {}
    for enum_block in glroot.findall('enums'):
        for enum in enum_block.findall('enum'):
            groups.setdefault(enum.get('name'), set()).update(x for x in (enum.get('group') or '').split(',') if x)
    core_getp = set()
    for feature in glroot.findall('feature'):
        if feature.get('api') != 'gles2' or float(feature.get('number') or 999) > 3.2:
            continue
        for req in feature.findall('require'):
            if req.get('api') not in (None, 'gles2'):
                continue
            for enum in req.findall('enum'):
                name = enum.get('name')
                if 'GetPName' in groups.get(name, set()):
                    core_getp.add(name)
        for rem in feature.findall('remove'):
            if rem.get('api') not in (None, 'gles2'):
                continue
            for enum in rem.findall('enum'):
                core_getp.discard(enum.get('name'))
    require(len(core_getp) == 199, f'GLES 2.0-3.2 core GetPName census drift: {len(core_getp)}')
    uncollected_core = {name for name in core_getp if name not in native}
    require(uncollected_core == CORE_MUTABLE_OR_OBJECT_STATE,
            'core GetPName disposition drift; unclassified=' + ','.join(sorted(uncollected_core - CORE_MUTABLE_OR_OBJECT_STATE)) +
            ' unexpectedly-collected=' + ','.join(sorted(CORE_MUTABLE_OR_OBJECT_STATE - uncollected_core)))
    require(len(core_getp - CORE_MUTABLE_OR_OBJECT_STATE) == 109,
            'implementation/capability core GetPName coverage baseline drift')

    # Every GLES2 extension GetPName token in current gl.xml is triaged. Implementation/capability
    # state must have an explicit collector identity; mutable/object/volatile state and device identifiers
    # are excluded deliberately rather than disappearing from the audit.
    extension_getp = set()
    for ext in glroot.findall('./extensions/extension'):
        if 'gles2' not in (ext.get('supported') or '').split('|'):
            continue
        for req in ext.findall('require'):
            if req.get('api') not in (None, 'gles2'):
                continue
            for enum in req.findall('enum'):
                name = enum.get('name')
                if 'GetPName' in groups.get(name, set()):
                    extension_getp.add(name)
    extension_excluded = EXTENSION_MUTABLE_OR_NONCAPABILITY_STATE | EXTENSION_PRIVACY_OR_NONANDROID_IDENTIFIERS
    require(len(extension_getp) == 84, f'GLES2 extension GetPName census drift: {len(extension_getp)}')
    require(extension_excluded <= extension_getp, 'extension disposition contains names absent from current gl.xml')
    extension_capability = extension_getp - extension_excluded
    require(len(extension_capability) == 49, f'extension implementation/capability GetPName disposition drift: {len(extension_capability)}')
    for name in sorted(extension_capability):
        require(f'"{name}"' in native, f'queryable extension capability has no collector identity: {name}')
    require(EXTENSION_PRIVACY_OR_NONANDROID_IDENTIFIERS == {'GL_DEVICE_LUID_EXT','GL_DEVICE_NODE_MASK_EXT','GL_DEVICE_UUID_EXT','GL_DRIVER_UUID_EXT'},
            'device/driver identifier privacy exclusion drift')

    # EGL 1.5 table 3.5: all 17 queryable surface attributes must be queried on the collector pbuffer.
    for token in SURFACE_ATTRS:
        require(token in native,f'EGL 1.5 queryable surface attribute missing: {token}')
    query_block=native[native.index('const auto surfaceGlColorspace'):native.index('const EglRuntimeQuery eglRuntimeAttributes')]
    for token in SURFACE_ATTRS:
        require(f'querySurfaceAttr(d, s, {token})' in query_block,f'eglQuerySurface path missing: {token}')
    require('surfaceGlColorspaceApplicable = eglCode >= 150 || hasExt(displayExt, "EGL_KHR_gl_colorspace")' in native,'EGL_GL_COLORSPACE legality gate drift')
    require('surfaceVgApplicable = eglCode >= 120' in native,'EGL VG surface-attribute legality gate drift')
    require('EGL_ALPHA_FORMAT alias before EGL 1.3' in native and 'EGL_COLORSPACE alias before EGL 1.3' in native,'EGL 1.2 VG alias provenance missing')
    for evidence in [
        'EGL_GL_COLORSPACE (EGL 1.5 core)', 'EGL_GL_COLORSPACE_KHR (EGL_KHR_gl_colorspace)',
        'EGL_VG_ALPHA_FORMAT (EGL 1.3+ canonical)', 'EGL_ALPHA_FORMAT (EGL 1.2 alias of EGL_VG_ALPHA_FORMAT)',
        'EGL_VG_COLORSPACE (EGL 1.3+ canonical)', 'EGL_COLORSPACE (EGL 1.2 alias of EGL_VG_COLORSPACE)']:
        require(evidence in native, f'EGL successful-query provenance missing: {evidence}')
    require('surfaceMultisampleApplicable = eglCode >= 140' in native,'EGL multisample-resolve legality gate drift')
    require('surfaceResolutionApplicable = eglCode >= 120' in native,'EGL 1.2 surface-attribute legality gate drift')
    require('surfaceTextureApplicable = eglCode >= 110' in native,'EGL 1.1 pbuffer texture-attribute legality gate drift')
    require('eglQuerySurface(d, surface, attr, &value)' in native and 'eglGetError()' in native,'eglQuerySurface error capture missing')

    # The full EGL surface dataset must survive native JSON -> strict parser -> UI/analysis/TXT/HTML/Database.
    for field in JSON_FIELDS:
        require(f'\\"{field}\\":' in native,f'native eglRuntime JSON field missing: {field}')
        require(f'"{field}"' in contract,f'strict report contract missing EGL runtime field: {field}')
        require(field in main,f'Kotlin report pipeline missing EGL runtime field: {field}')
    for key in [
        'egl-runtime/surfaceConfigId','egl-runtime/surfaceSize','egl-runtime/surfaceGlColorspace','egl-runtime/surfaceGlColorspaceQuery','egl-runtime/surfaceVgAlphaFormat','egl-runtime/surfaceVgAlphaFormatQuery',
        'egl-runtime/surfaceVgColorspace','egl-runtime/surfaceVgColorspaceQuery','egl-runtime/surfaceHorizontalResolution','egl-runtime/surfaceVerticalResolution',
        'egl-runtime/surfacePixelAspectRatio','egl-runtime/surfaceLargestPbuffer','egl-runtime/surfaceRenderBuffer',
        'egl-runtime/surfaceSwapBehavior','egl-runtime/surfaceTextureFormat','egl-runtime/surfaceTextureTarget',
        'egl-runtime/surfaceMipmapTexture','egl-runtime/surfaceMipmapLevel','egl-runtime/surfaceMultisampleResolve']:
        require(main.count(f'"{key}"') >= 2,f'local/Database analysis parity key missing: {key}')
    require('Pbuffer: config=' in main and 'Pbuffer GL colorspace' in main and 'Pbuffer multisample resolve' in main,'TXT/HTML EGL surface reporting incomplete')
    require('surfaceLargestPbuffer.value == EGL_TRUE ? "true" : "false"' in native, 'EGL_LARGEST_PBUFFER boolean is mis-serialized')
    require('surfaceMipmapTexture.value == EGL_TRUE ? "true" : "false"' in native, 'EGL_MIPMAP_TEXTURE boolean is mis-serialized')
    require('!isNullableBoolean(runtime, "surfaceLargestPbuffer")' in contract and '!isNullableBoolean(runtime, "surfaceMipmapTexture")' in contract, 'EGL surface boolean contract type drift')
    require('private fun eglScaledSurfaceEvidence' in main and 'EGL_UNKNOWN (-1)' in main and 'EGL_DISPLAY_SCALING' in main, 'scaled EGL surface semantics are not preserved in human-readable reports')

    # Current registered colorspace values must retain symbolic detail instead of collapsing to raw hex.
    for name, value in [
        ('EGL_GL_COLORSPACE_DEFAULT_EXT','0x314D'),('EGL_GL_COLORSPACE_BT2020_LINEAR_EXT','0x333F'),
        ('EGL_GL_COLORSPACE_BT2020_PQ_EXT','0x3340'),('EGL_GL_COLORSPACE_SCRGB_LINEAR_EXT','0x3350'),
        ('EGL_GL_COLORSPACE_SCRGB_EXT','0x3351'),('EGL_GL_COLORSPACE_DISPLAY_P3_LINEAR_EXT','0x3362'),
        ('EGL_GL_COLORSPACE_DISPLAY_P3_EXT','0x3363'),('EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT','0x3490'),
        ('EGL_GL_COLORSPACE_BT2020_HLG_EXT','0x3540')]:
        require(name in native and value in native, f'current EGL colorspace symbolic mapping missing: {name}')

    # Human-readable and compare/report surfaces must not collapse Not applicable and Unavailable.
    require('private fun runtimeQueryEvidence(r: GlReport' in main, 'runtime query-state presentation helper missing')
    require('Unknown / not queried' in main, 'unknown/not-queried runtime state is not preserved')
    runtime_region = main[main.index('private fun OpenGLESPage'):main.index('@Composable\nprivate fun DisplayPage')]
    require('Not applicable / unavailable' not in runtime_region, 'OpenGL ES runtime UI conflates Not applicable and Unavailable')
    egl_region = main[main.index('@Composable\nprivate fun EglPage'):main.index('@Composable\nprivate fun FeaturesPage')]
    require('Not applicable / unavailable' not in egl_region, 'EGL runtime UI conflates Not applicable and Unavailable')
    require('val technicalQueryDiagnostics = tech.optJSONArray("queryDiagnostics")' in main and 'technicalRuntimeEvidence' in main, 'Database compare conversion does not retain query-state provenance')

    # GL reset strategy is core in ES 3.2 and extension-gated before 3.2; it must not be silently dropped into a legacy limits-only path.
    require('const bool contextFlagsApplicable = glCode >= 320;' in native,'GL_CONTEXT_FLAGS must be queried only on OpenGL ES 3.2+')
    require('contextFlagsApplicable = glCode >= 300' not in native,'GL_CONTEXT_FLAGS must not be queried on ES 3.0/3.1')
    require('const bool resetCoreApplicable = glCode >= 320;' in native,'OpenGL ES 3.2 reset-strategy applicability gate missing')
    require('queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY", 0x8256, true' in native,'core reset-strategy GetIntegerv path missing')
    require('queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_KHR", 0x8256, resetKhrApplicable' in native,'KHR robustness reset-strategy path missing')
    require('queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_KHR", 0x90F3' in native,'KHR robust-access boolean path missing')
    require('hasExt(glExt, "GL_EXT_robustness")' in native and 'queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_EXT", 0x8256, resetExtApplicable' in native,'EXT robustness reset-strategy path missing')
    require('queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_EXT", 0x90F3, resetExtApplicable' in native,'EXT robust-access boolean path missing')
    require('GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT' in native and 'glContextFlagsRaw' in native,'ES 3.2 core robust-access derivation from GL_CONTEXT_FLAGS missing')
    require('robustAccessQuery' in native and 'GL_CONTEXT_FLAGS / GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT' in native,'robust-access query provenance missing')
    require('glResetStrategyDisplay(GLint value, const char* queryName)' in native, 'reset-strategy value labels are not query-provenance aware')
    require('GL_LOSE_CONTEXT_ON_RESET") + suffix' in native and 'GL_NO_RESET_NOTIFICATION") + suffix' in native,
            'KHR/EXT reset-strategy value suffix preservation missing')
    require('addLimit(o, first, "GL_RESET_NOTIFICATION_STRATEGY"' not in native,'reset strategy must not be duplicated/misreported as a generic limit')
    for key in ['gl-runtime/contextFlags','gl-runtime/resetNotificationStrategy','gl-runtime/resetNotificationStrategyQuery','gl-runtime/robustAccess','gl-runtime/robustAccessQuery']:
        require(main.count(f'"{key}"') >= 2,f'local/Database GL runtime analysis parity key missing: {key}')

    # Database-fetched compare snapshots must be lossless relative to local compare snapshots.
    dbfun=main[main.index('private fun databasePayloadToAnalysisSnapshot'):main.index('private data class CustomMinimumResult')]
    require('tech.optJSONObject("glRuntime")' in dbfun,'Database analysis conversion drops glRuntime')
    for key in ['surfaceConfigId','surfaceGlColorspace','surfaceVgAlphaFormat','surfaceVgColorspace','surfaceHorizontalResolution','surfaceVerticalResolution','surfacePixelAspectRatio','surfaceLargestPbuffer']:
        require(f'egl-runtime/{key}' in dbfun,f'Database analysis conversion drops EGL runtime field: {key}')
    require('nvSampleProperties' in dbfun and 'conformant=' in dbfun,'Database analysis conversion drops NV internal-format sample evidence')

if __name__=='__main__': main_guard('verify_1_9_3_spec_reporting',verify)
