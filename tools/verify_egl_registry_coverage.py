#!/usr/bin/env python3
from __future__ import annotations
import json,re,xml.etree.ElementTree as ET
from gate_common import ROOT, require, main_guard

def verify():
    xml=ROOT/'registry/egl.xml'; root=ET.parse(xml).getroot(); native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(); main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    cov=json.loads((ROOT/'registry/generated/egl_query_coverage.json').read_text())
    ext=sorted(e.get('name') for e in root.findall('./extensions/extension')); commands={c.findtext('./proto/name') for c in root.findall('./commands/command') if c.find('./proto/name') is not None}
    require(len(ext)==167,'locked EGL extension census drift')
    require(len(commands)==158,'locked EGL command census drift')
    require(cov['schema']=='OpenGLESScopeEglQueryCoverage2','EGL coverage manifest schema drift')
    require(cov['registeredExtensions']==167 and cov['registeredCommands']==158,'EGL coverage manifest census drift')
    require([x['name'] for x in cov['extensions']]==ext,'EGL coverage manifest does not classify every registered extension')
    require({f.get('number') for f in root.findall('feature')}=={'1.0','1.1','1.2','1.3','1.4','1.5'},'EGL core feature census drift')
    require(len(cov['extensions'])==167 and len({x['name'] for x in cov['extensions']})==167,'every EGL extension must be classified exactly once')
    allowed_classes={'direct-collected','privacy-not-collected','creation-only-explained','window-surface-required','platform-not-applicable','object-or-action-required','runtime-enumeration-reference'}
    require(all(x.get('classification') in allowed_classes and x.get('reason') for x in cov['extensions']),'EGL extension classification/disposition is incomplete')
    for cmd in cov['queryCommands']: require(cmd in commands,f'EGL query command not registered: {cmd}')
    for exact in ['eglQueryDevicesEXT','eglQueryDisplayAttribEXT','eglQueryDeviceStringEXT','eglQueryDeviceAttribEXT','eglQuerySupportedCompressionRatesEXT','eglQueryDisplayAttribKHR','eglGetDisplayDriverName','eglQueryDmaBufFormatsEXT','eglQueryDmaBufModifiersEXT']:
        require(f'eglGetProcAddress("{exact}")' in native,f'exact EGL entry-point resolution missing: {exact}')
    patterns={
      'eglQueryDevicesEXT':r'queryDevicesExt\(', 'eglQueryDisplayAttribEXT':r'queryDisplayAttribExt\(', 'eglQueryDeviceStringEXT':r'queryDeviceStringExt\(',
      'eglQueryDeviceAttribEXT':r'queryDeviceAttribExt\(', 'eglQuerySupportedCompressionRatesEXT':r'eglGetProcAddress\("eglQuerySupportedCompressionRatesEXT"\)',
      'eglQueryDisplayAttribKHR':r'queryDisplayAttribKhr\(', 'eglGetDisplayDriverName':r'getDriverName\(', 'eglQueryDmaBufFormatsEXT':r'queryFormats\(', 'eglQueryDmaBufModifiersEXT':r'queryModifiers\(',
      'eglQueryContext':r'eglQueryContext\(', 'eglQuerySurface':r'eglQuerySurface\(', 'eglGetConfigAttrib':r'eglGetConfigAttrib\(', 'eglQueryAPI':r'eglQueryAPI\(', 'eglQueryString':r'eglQueryString\('
    }
    for cmd,pat in patterns.items(): require(re.search(pat,native),f'native EGL query path missing: {cmd}')
    for extname in cov['directRuntimeQueryExtensions']: require(extname in ext,f'direct EGL query extension is not registry-registered: {extname}')
    require(cov['classificationCounts'].get('direct-collected')==14,'direct EGL collection classification count drift')
    require(cov['classificationCounts'].get('runtime-enumeration-reference')==63,'EGL enumeration-reference classification count drift')
    for extname in ['EGL_ANDROID_recordable','EGL_ANDROID_framebuffer_target','EGL_EXT_pixel_format_float','EGL_KHR_gl_colorspace']:
        require(extname in cov['directRuntimeQueryExtensions'],f'direct EGL config/surface query extension missing from coverage policy: {extname}')
    for token in ['EGL_RECORDABLE_ANDROID_VALUE','EGL_FRAMEBUFFER_TARGET_ANDROID_VALUE','EGL_COLOR_COMPONENT_TYPE_EXT_VALUE']:
        require(token in native,f'direct EGL config attribute query constant missing: {token}')
    require('surfaceGlColorspaceApplicable = eglCode >= 150 || hasExt(displayExt, "EGL_KHR_gl_colorspace")' in native,'EGL_KHR_gl_colorspace direct query legality gate missing')
    enum_values={e.get('name'):e.get('value') for g in root.findall('enums') for e in g.findall('enum')}
    constants={
      'EGL_DEVICE_EXT':'0x322C','EGL_RENDERER_EXT':'0x335F','EGL_DRIVER_NAME_EXT':'0x335E','EGL_DEVICE_TYPE_EXT':'0x3590','EGL_BUFFER_AGE_KHR':'0x313D',
      'EGL_PROTECTED_CONTENT_EXT':'0x32C0','EGL_SURFACE_COMPRESSION_EXT':'0x34B0','EGL_TRACK_REFERENCES_KHR':'0x3352','EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_KHR':'0x31BD','EGL_CONTEXT_OPENGL_NO_ERROR_KHR':'0x31B3',
      'EGL_RECORDABLE_ANDROID':'0x3142','EGL_FRAMEBUFFER_TARGET_ANDROID':'0x3147','EGL_COLOR_COMPONENT_TYPE_EXT':'0x3339','EGL_GL_COLORSPACE':'0x309D','EGL_DISPLAY_SCALING':'10000'}
    for name,value in constants.items(): require(enum_values.get(name)==value,f'EGL registry value drift: {name}')
    require(enum_values.get('EGL_UNKNOWN') in {'EGL_CAST(EGLint,-1)','-1'},'EGL_UNKNOWN registry value drift')
    require('kMaxEglDeviceCount = 32' in native and 'kMaxEglCompressionRateCount = 32' in native,'EGL device/compression resource bounds missing')
    require('kMaxDmaBufFormatCount = 128' in native and 'kMaxDmaBufModifierCountPerFormat = 256' in native and 'kMaxDmaBufModifierCountTotal = 4096' in native,'EGL DMA-BUF query resource bounds missing')
    for extname in ['EGL_KHR_display_reference','EGL_MESA_query_driver','EGL_EXT_image_dma_buf_import_modifiers']:
        require(extname in cov['directRuntimeQueryExtensions'],f'new direct EGL query extension missing from coverage policy: {extname}')
    require('EGL_BUFFER_AGE_KHR", "Not applicable"' in native,'window-surface-only buffer age must not be fabricated from pbuffer collection')
    require('Persistent UUID fields are intentionally not collected' in native,'persistent EGL device identifier privacy boundary missing')
    require('RegistryEncyclopediaPage' in main and ('Offline Khronos OpenGL® ES™ and EGL™ registry reference' in main or 'Offline Khronos OpenGL® ES and EGL™ registry reference' in main),'EGL registry encyclopedia UI missing')
    require('EGL capability queries' in main and 'Presentation evidence' in main,'EGL runtime/presentation evidence UI missing')
if __name__=='__main__': main_guard('verify_egl_registry_coverage',verify)
