#!/usr/bin/env python3
from __future__ import annotations
import hashlib,subprocess,sys,json
from gate_common import ROOT, require, main_guard

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def verify():
    asset=ROOT/'app/src/main/assets/registry_catalog.json.gz'; man=ROOT/'registry/registry_catalog_manifest.json'; gen=ROOT/'tools/generate_registry_catalog.py'
    before=(sha(asset),sha(man)); cp=subprocess.run([sys.executable,'-B',str(gen)],cwd=ROOT,capture_output=True,text=True); require(cp.returncode==0,'registry catalog generator failed')
    after=(sha(asset),sha(man)); require(after==before,'registry catalog is not byte-reproducible')
    m=json.loads(man.read_text()); require(m['entries']==5261 and m['glEntries']==4196 and m['eglEntries']==1065 and m['eglExtensions']==167,'registry catalog census drift')
if __name__=='__main__': main_guard('verify_registry_catalog',verify)
