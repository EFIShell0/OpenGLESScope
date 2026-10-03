#!/usr/bin/env python3
from pathlib import Path
import json, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
xml=ROOT/'registry/egl.xml'; out=ROOT/'registry/generated/egl_query_coverage.json'
r=ET.parse(xml).getroot()
extensions=sorted(e.get('name') for e in r.findall('./extensions/extension'))
commands=sorted(c.findtext('./proto/name') for c in r.findall('./commands/command') if c.find('./proto/name') is not None)

# This classification is about what OpenGLESScope can truthfully collect in its isolated,
# Android pbuffer-based diagnostic context. It is not a support classification.
direct_collected={
'EGL_ANDROID_recordable','EGL_ANDROID_framebuffer_target','EGL_EXT_pixel_format_float','EGL_KHR_gl_colorspace',
'EGL_EXT_protected_content','EGL_EXT_device_query','EGL_EXT_device_base','EGL_EXT_device_enumeration','EGL_EXT_device_query_name','EGL_EXT_device_type',
'EGL_EXT_surface_compression','EGL_KHR_display_reference','EGL_MESA_query_driver','EGL_EXT_image_dma_buf_import_modifiers',
}
privacy_not_collected={'EGL_EXT_device_persistent_id'}
creation_only={'EGL_KHR_create_context','EGL_KHR_create_context_no_error','EGL_EXT_create_context_robustness'}
window_surface={
'EGL_EXT_buffer_age','EGL_KHR_partial_update','EGL_ANDROID_get_frame_timestamps','EGL_ANDROID_presentation_time','EGL_ANDROID_front_buffer_auto_refresh',
'EGL_EXT_swap_buffers_with_damage','EGL_KHR_swap_buffers_with_damage','EGL_KHR_lock_surface','EGL_KHR_lock_surface2','EGL_KHR_lock_surface3',
'EGL_NV_post_sub_buffer','EGL_NV_stream_consumer_gltexture_yuv','EGL_EXT_present_opaque','EGL_EXT_bind_to_front',
}
platform_non_android={n for n in extensions if any(k in n.lower() for k in ['platform_x11','platform_xcb','platform_wayland','platform_gbm','wayland','x11','xcb'])}
object_keywords=('stream','sync','output','image','fence','debug','blob','dma_buf','native_client_buffer','native_fence','interop')

def classify(name:str):
    if name in direct_collected: return 'direct-collected','Collector executes a bounded legal query only when runtime extension and entry-point evidence allow it.'
    if name in privacy_not_collected: return 'privacy-not-collected','Support is enumerated, but persistent UUID-style identifiers are intentionally not retained.'
    if name in creation_only: return 'creation-only-explained','Creation attributes affect context construction; they are not fabricated as queryable runtime state.'
    if name in window_surface: return 'window-surface-required','The isolated collector intentionally owns a pbuffer, so window/presentation-only evidence is not fabricated.'
    if name in platform_non_android: return 'platform-not-applicable','Platform-specific X11/XCB/Wayland/GBM behavior is outside the Android diagnostic surface.'
    if any(k in name.lower() for k in object_keywords): return 'object-or-action-required','Meaningful evidence requires a transient extension object/action not created solely for capability enumeration.'
    return 'runtime-enumeration-reference','Runtime extension enumeration is authoritative support evidence; no additional safe implementation-capability query is defined for this collector.'

entries=[]
for n in extensions:
    c,reason=classify(n); entries.append({'name':n,'classification':c,'reason':reason})
query_commands=[
'eglQueryString','eglQueryAPI','eglQueryContext','eglQuerySurface','eglGetConfigAttrib','eglGetCurrentContext','eglGetCurrentDisplay','eglGetCurrentSurface',
'eglQueryDevicesEXT','eglQueryDisplayAttribEXT','eglQueryDeviceStringEXT','eglQueryDeviceAttribEXT','eglQuerySupportedCompressionRatesEXT',
'eglQueryDisplayAttribKHR','eglGetDisplayDriverName','eglQueryDmaBufFormatsEXT','eglQueryDmaBufModifiersEXT'
]
obj={
'schema':'OpenGLESScopeEglQueryCoverage2','eglCore':'1.5','registeredExtensions':len(extensions),'registeredCommands':len(commands),
'extensions':entries,
'directRuntimeQueryExtensions':sorted(direct_collected),
'privacyExcludedExtensions':sorted(privacy_not_collected),
'queryCommands':query_commands,
'classificationCounts':{k:sum(1 for e in entries if e['classification']==k) for k in sorted({e['classification'] for e in entries})},
'notes':[
'Every registered EGL extension is classified exactly once; registry presence never proves runtime support.',
'Direct queries are executed only when the relevant runtime extension/core version, object type and entry point make the query legal.',
'Creation-only, window-surface-only, object/action-required, privacy-excluded and platform-not-applicable evidence remain explicit rather than being guessed.'
]
}
out.write_text(json.dumps(obj,indent=2,sort_keys=True)+'\n')
print(out)
