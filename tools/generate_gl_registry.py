#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, xml.etree.ElementTree as ET
from pathlib import Path
from gate_common import ROOT

src=ROOT/'registry/gl.xml'
root=ET.parse(src).getroot()
features=[]
for f in root.findall('feature'):
    if f.get('api') in {'gles1','gles2'}:
        features.append({'api':f.get('api'),'name':f.get('name'),'number':f.get('number')})
manifest={
    'source':'Khronos Combined OpenGL Registry gl.xml; release-locked snapshot revalidated for OpenGLESScope 2.0.0',
    'canonicalRegistry':'https://registry.khronos.org/OpenGL/xml/gl.xml',
    'verifiedAt':'2026-09-30',
    'sha256':hashlib.sha256(src.read_bytes()).hexdigest(),
    'bytes':src.stat().st_size,
    'commands':len(root.findall('./commands/command')),
    'enumEntries':sum(len(group.findall('enum')) for group in root.findall('enums')),
    'registeredExtensions':len(root.findall('./extensions/extension')),
    'glesExtensionEntries':sum(1 for e in root.findall('./extensions/extension') if any(x.startswith('gles') for x in (e.get('supported') or '').split('|'))),
    'features':features,
    'currentOpenGLESCore':'3.2',
    'currentGLSLES':'3.20',
    'currentEGL':'1.5',
}
out=ROOT/'registry/gl_registry_manifest.json'
out.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n',encoding='utf-8')
print(out)
