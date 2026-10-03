#!/usr/bin/env python3
from __future__ import annotations
import gzip, hashlib, json, re, xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
GL=ROOT/'registry/gl.xml'; EGL=ROOT/'registry/egl.xml'
OUT=ROOT/'app/src/main/assets/registry_catalog.json.gz'
MAN=ROOT/'registry/registry_catalog_manifest.json'

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def text(el): return ''.join(el.itertext()).strip() if el is not None else ''

def build(path:Path, api_name:str):
    root=ET.parse(path).getroot(); owners=defaultdict(list); relevant_ext=[]; relevant_features=[]
    if api_name=='OpenGL ES':
        feat_ok=lambda f: f.get('api') in {'gles1','gles2'}
        ext_ok=lambda e: any(x in (e.get('supported') or '').split('|') for x in ('gles1','gles2'))
    else:
        feat_ok=lambda f: f.get('api')=='egl'
        ext_ok=lambda e: 'egl' in (e.get('supported') or '').split('|')
    for f in root.findall('feature'):
        if not feat_ok(f): continue
        name=f.get('name') or ''; relevant_features.append({'name':name,'number':f.get('number') or '','api':f.get('api') or ''})
        for req in f.findall('require'):
            for child in req:
                n=child.get('name');
                if n: owners[n].append(name)
    exts=root.find('extensions')
    if exts is not None:
        for e in exts.findall('extension'):
            if not ext_ok(e): continue
            name=e.get('name') or ''; relevant_ext.append(name)
            for req in e.findall('require'):
                api=req.get('api')
                if api_name=='OpenGL ES' and api and api not in {'gles1','gles2'}: continue
                for child in req:
                    n=child.get('name');
                    if n: owners[n].append(name)
    entries=[]; seen=set()
    # Enums. Keep symbols referenced by ES/EGL features/extensions, plus API-prefixed public tokens.
    for group in root.findall('enums'):
        ns=group.get('namespace') or ''
        for e in group.findall('enum'):
            n=e.get('name')
            if not n or n in seen: continue
            if api_name=='OpenGL ES':
                # gl.xml is a combined desktop/OpenGL ES registry. Only symbols actually owned by
                # an ES core feature or an extension whose supported set includes gles1/gles2 belong
                # in the OpenGLESScope encyclopedia.
                if not n.startswith('GL_') or n not in owners: continue
            else:
                if not n.startswith('EGL_'): continue
            seen.add(n)
            entries.append({'name':n,'kind':'token','api':api_name,'value':e.get('value') or '', 'alias':e.get('alias') or '', 'group':e.get('group') or group.get('group') or ns, 'owners':sorted(set(owners.get(n,[])))[:48]})
    commands=root.find('commands')
    if commands is not None:
        for c in commands.findall('command'):
            proto=c.find('proto'); n=proto.findtext('name') if proto is not None else None
            if not n or n in seen: continue
            prefix='gl' if api_name=='OpenGL ES' else 'egl'
            if not n.startswith(prefix): continue
            if api_name=='OpenGL ES' and n not in owners: continue
            seen.add(n)
            signature=text(proto)+'('+', '.join(text(p) for p in c.findall('param'))+')'
            entries.append({'name':n,'kind':'command','api':api_name,'signature':signature,'alias':(c.find('alias').get('name') if c.find('alias') is not None else ''),'owners':sorted(set(owners.get(n,[])))[:48]})
    referenced_types=set()
    if commands is not None:
        for c in commands.findall('command'):
            proto=c.find('proto'); cn=proto.findtext('name') if proto is not None else None
            if not cn or (api_name=='OpenGL ES' and cn not in owners): continue
            for pt in c.findall('.//ptype'):
                if pt.text: referenced_types.add(pt.text.strip())
    types=root.find('types')
    if types is not None:
        for t in types.findall('type'):
            n=t.findtext('name') or t.get('name')
            if not n or n in seen: continue
            if api_name=='OpenGL ES':
                if not (n.startswith('GL') or n.startswith('PFNGL')) or (n not in owners and n not in referenced_types): continue
            else:
                if not (n.startswith('EGL') or n.startswith('PFNEGL')): continue
            seen.add(n)
            definition=re.sub(r'\s+',' ',text(t))[:512]
            entries.append({'name':n,'kind':'type','api':api_name,'definition':definition,'owners':sorted(set(owners.get(n,[])))[:48]})
    for ext in sorted(set(relevant_ext)):
        entries.append({'name':ext,'kind':'extension','api':api_name,'owners':[ext]})
    for f in relevant_features:
        entries.append({'name':f['name'],'kind':'core-version','api':api_name,'value':f['number'],'owners':[f['name']]})
    return entries, relevant_features, sorted(set(relevant_ext))

gl_entries,gl_features,gl_exts=build(GL,'OpenGL ES')
egl_entries,egl_features,egl_exts=build(EGL,'EGL')
entries=sorted(gl_entries+egl_entries,key=lambda x:(x['api'],x['kind'],x['name']))
payload={'schema':'OpenGLESScopeRegistryCatalog1','generatedFrom':['registry/gl.xml','registry/egl.xml'],'entries':entries,'counts':{'entries':len(entries),'glEntries':len(gl_entries),'eglEntries':len(egl_entries),'glExtensions':len(gl_exts),'eglExtensions':len(egl_exts),'glCoreVersions':len(gl_features),'eglCoreVersions':len(egl_features)}}
OUT.parent.mkdir(parents=True,exist_ok=True)
raw=json.dumps(payload,separators=(',',':'),ensure_ascii=False).encode()
with gzip.GzipFile(filename='',mode='wb',fileobj=OUT.open('wb'),compresslevel=9,mtime=0) as z:z.write(raw)
manifest={'schema':'OpenGLESScopeRegistryCatalogManifest1','glXmlSha256':sha(GL),'eglXmlSha256':sha(EGL),'catalogSha256':sha(OUT),'catalogUncompressedBytes':len(raw),**payload['counts']}
MAN.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
print(json.dumps(manifest,indent=2))
