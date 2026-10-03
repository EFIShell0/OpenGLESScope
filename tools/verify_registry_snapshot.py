#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json,tempfile,xml.etree.ElementTree as ET
from pathlib import Path
from gate_common import ROOT, require, main_guard

def verify():
    glp=ROOT/'registry/gl.xml'; glmp=ROOT/'registry/gl_registry_manifest.json'
    eglp=ROOT/'registry/egl.xml'; eglmp=ROOT/'registry/egl_registry_manifest.json'
    require(glp.is_file() and glmp.is_file(),'OpenGL registry/manifest missing')
    require(eglp.is_file() and eglmp.is_file(),'EGL registry/manifest missing')
    gr=ET.parse(glp).getroot(); gm=json.loads(glmp.read_text()); er=ET.parse(eglp).getroot(); em=json.loads(eglmp.read_text())
    require(hashlib.sha256(glp.read_bytes()).hexdigest()==gm['sha256'],'gl.xml SHA-256 drift'); require(glp.stat().st_size==gm['bytes'],'gl.xml byte-size drift')
    require(len(gr.findall('./commands/command'))==gm['commands'],'OpenGL command census drift'); require(sum(len(g.findall('enum')) for g in gr.findall('enums'))==gm['enumEntries'],'OpenGL enum census drift'); require(len(gr.findall('./extensions/extension'))==gm['registeredExtensions'],'OpenGL extension census drift')
    gles_ext=sum(1 for e in gr.findall('./extensions/extension') if any(x.startswith('gles') for x in (e.get('supported') or '').split('|'))); require(gles_ext==gm['glesExtensionEntries'],'GLES extension census drift')
    feats={(f.get('api'),f.get('name'),f.get('number')) for f in gr.findall('feature') if f.get('api') in {'gles1','gles2'}}; required={('gles1','GL_VERSION_ES_CM_1_0','1.0'),('gles2','GL_ES_VERSION_2_0','2.0'),('gles2','GL_ES_VERSION_3_0','3.0'),('gles2','GL_ES_VERSION_3_1','3.1'),('gles2','GL_ES_VERSION_3_2','3.2')}; require(required<=feats,'required OpenGL ES feature blocks missing')
    require(hashlib.sha256(eglp.read_bytes()).hexdigest()==em['sha256'],'egl.xml SHA-256 drift'); require(eglp.stat().st_size==em['bytes'],'egl.xml byte-size drift')
    require(len(er.findall('./commands/command'))==em['commands']==158,'EGL command census drift'); require(sum(len(g.findall('enum')) for g in er.findall('enums'))==em['enumEntries']==694,'EGL enum census drift'); require(len(er.findall('./extensions/extension'))==em['registeredExtensions']==167,'EGL extension census drift')
    egl_features=[{'api':f.get('api'),'name':f.get('name'),'number':f.get('number')} for f in er.findall('feature')]; require(egl_features==em['features'],'EGL core feature manifest drift'); require([x['number'] for x in egl_features]==['1.0','1.1','1.2','1.3','1.4','1.5'],'EGL 1.0-1.5 feature sequence drift')
    lock=json.loads((ROOT/'registry/registry_lock.json').read_text()); require(lock['registrySha256']==gm['sha256'] and lock['eglRegistrySha256']==em['sha256'],'combined registry lock drift')
    catalog=json.loads((ROOT/'registry/registry_catalog_manifest.json').read_text()); require(catalog['glXmlSha256']==gm['sha256'] and catalog['eglXmlSha256']==em['sha256'],'registry catalog source hash drift'); require(catalog['entries']==5261 and catalog['glEntries']==4196 and catalog['eglEntries']==1065 and catalog['eglExtensions']==167,'offline encyclopedia census drift')
    require(gm.get('currentOpenGLESCore')=='3.2' and gm.get('currentGLSLES')=='3.20' and gm.get('currentEGL')=='1.5' and em.get('currentEGL')=='1.5','current spec baseline metadata drift')
if __name__=='__main__': main_guard('verify_registry_snapshot',verify)
