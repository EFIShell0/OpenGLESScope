#!/usr/bin/env python3
from __future__ import annotations
import re
from gate_common import ROOT, require, main_guard

LEGAL_CONTEXT_ATTRS={
    'EGL_CONFIG_ID',
    'EGL_CONTEXT_CLIENT_TYPE',
    'EGL_CONTEXT_CLIENT_VERSION',
    'EGL_RENDER_BUFFER',
    'EGL_PROTECTED_CONTENT_EXT_VALUE',
}
FORBIDDEN_CONTEXT_ATTRS={
    'EGL_CONTEXT_MINOR_VERSION',
    'EGL_CONTEXT_MINOR_VERSION_KHR',
    'EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_VALUE',
    'EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_KHR',
    'EGL_CONTEXT_FLAGS_KHR',
    'EGL_CONTEXT_OPENGL_NO_ERROR_KHR_VALUE',
    'EGL_CONTEXT_OPENGL_NO_ERROR_KHR',
}

def verify():
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    calls=re.findall(r'queryContextAttr\(d,\s*c,\s*([A-Za-z0-9_]+)\)',native)
    require(calls,'no EGL context-state query calls found')
    illegal=sorted(set(calls)-LEGAL_CONTEXT_ATTRS)
    require(not illegal,'illegal/unaudited eglQueryContext attribute(s): '+', '.join(illegal))
    for token in FORBIDDEN_CONTEXT_ATTRS:
        require(not re.search(rf'queryContextAttr\(d,\s*c,\s*{re.escape(token)}\s*\)',native),f'creation-only EGL attribute illegally queried at runtime: {token}')
    for token in ['EGL_CONFIG_ID','EGL_CONTEXT_CLIENT_TYPE','EGL_CONTEXT_CLIENT_VERSION','EGL_RENDER_BUFFER']:
        require(token in calls,f'legal core eglQueryContext evidence missing: {token}')
    require('if (hasExt(displayExt, "EGL_EXT_protected_content"))' in native and 'queryContextAttr(d, c, EGL_PROTECTED_CONTENT_EXT_VALUE)' in native,'protected-content context query must be exact-extension gated')

    # ES3 creation is legal on EGL 1.5 core or EGL_KHR_create_context only.
    require('const bool canCreateEs3 = eglCode >= 150 || hasKhrCreateContext;' in native,'ES3 EGL creation legality gate missing')
    es3_start=native.index('if (canCreateEs3 && chooseConfig(d, EGL_OPENGL_ES3_BIT_KHR, cfg))')
    fallback=native.index('if (chooseConfig(d, EGL_OPENGL_ES2_BIT, cfg))',es3_start)
    es3=native[es3_start:fallback]
    require('EGL_CONTEXT_MAJOR_VERSION_KHR' in es3 and 'EGL_CONTEXT_MINOR_VERSION_KHR' in es3,'ES3 major/minor creation request missing')
    require('EGL_OPENGL_ES3_BIT_KHR' in es3,'ES3 renderable-bit request missing')
    es2=native[fallback:native.index('return EGL_NO_CONTEXT;',fallback)]
    require('EGL_CONTEXT_CLIENT_VERSION, 2' in es2,'safe EGL core ES2 fallback missing')
    for forbidden in ['EGL_CONTEXT_MAJOR_VERSION_KHR','EGL_CONTEXT_MINOR_VERSION_KHR','EGL_OPENGL_ES3_BIT_KHR']:
        require(forbidden not in es2,f'ES2 fallback illegally contains ES3/KHR creation attribute: {forbidden}')

    # Creation-only fields must be reported honestly rather than synthesized as runtime state.
    for label in ['EGL_CONTEXT_MINOR_VERSION','EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY','EGL_CONTEXT_FLAGS_KHR','EGL_CONTEXT_OPENGL_NO_ERROR_KHR']:
        require(f'addEglCapability("{label}", ' in native,f'creation-only EGL evidence explanation missing: {label}')
    require('Creation attribute, not legal EGL 1.5 eglQueryContext runtime state' in native,'minor-version legality explanation missing')
    require('Creation-only attribute with no EGL queryable state' in native,'no-error legality explanation missing')

    # Extension scope is part of the contract: client extensions are advertised on EGL_NO_DISPLAY; display extensions on the initialized display.
    require('const bool hasDeviceQuery = hasExt(clientExt, "EGL_EXT_device_query") || hasExt(clientExt, "EGL_EXT_device_base");' in native,'EGL_EXT_device_query/base must be gated by the client extension string only')
    require('if (hasExt(clientExt, "EGL_KHR_display_reference"))' in native,'EGL_KHR_display_reference must be gated by the client extension string')
    require('if (hasExt(displayExt, "EGL_MESA_query_driver"))' in native,'EGL_MESA_query_driver must be gated by the initialized-display extension string')
    require('hasExt(displayExt, "EGL_EXT_image_dma_buf_import_modifiers")' in native,'dma-buf modifier queries must be gated by the display extension string')

if __name__=='__main__': main_guard('verify_egl_query_legality',verify)
