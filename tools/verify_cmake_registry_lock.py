#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

def verify():
    lock=json.loads((ROOT/'registry/registry_lock.json').read_text()); cmake=(ROOT/'app/src/main/cpp/CMakeLists.txt').read_text()
    for pathkey,hashkey,label in [('bundledRegistryPath','registrySha256','OpenGL'),('eglBundledRegistryPath','eglRegistrySha256','EGL')]:
        data=(ROOT/lock[pathkey]).read_bytes(); digest=hashlib.sha256(data).hexdigest(); require(digest==lock[hashkey],f'{label} registry lock SHA mismatch'); require(lock[hashkey] in cmake,f'CMake does not hard-lock {label} registry SHA')
    for x in ['GL_ES_VERSION_3_2','GL_EXT_fragment_shading_rate','GL_EXT_mesh_shader','GL_ARM_shader_core_properties','EGL_VERSION_1_5','EGL_EXT_device_query','EGL_EXT_device_enumeration','EGL_EXT_surface_compression','eglQuerySupportedCompressionRatesEXT']: require(x in cmake,f'CMake registry sentinel missing: {x}')
    require('Bundled OpenGL registry snapshot hash mismatch' in cmake and 'Bundled EGL registry snapshot hash mismatch' in cmake,'CMake registry mismatch must fail build')
if __name__=='__main__': main_guard('verify_cmake_registry_lock',verify)
