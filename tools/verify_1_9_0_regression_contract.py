#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib, json

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def inv():
    out=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists():
            out += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x) and not x.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): out.append(rel)
    return sorted(set(out))

def expected_successor(c):
    out=dict(c['predecessorHashes'])
    for rel,spec in c['allowlistedChanges'].items(): out[rel]=spec['successorSha256']
    for rel,spec in c.get('allowlistedNewFiles',{}).items(): out[rel]=spec['successorSha256']
    return out

def verify():
    p=ROOT/'tests/golden/1_9_0_vulkanscope_ui_storage_parity_contract.json'
    require(p.is_file(),'1.9.0 regression contract missing')
    c=json.loads(p.read_text())
    require(c.get('release')=='1.9.0' and c.get('predecessor')=='1.8.0','release identity drift')
    require(c.get('predecessorZipSha256')=='678b5bac87dd3b7afbcbdd5acfe550cb5de934870c3162765214f78aa6c2a950','predecessor ZIP hash drift')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    if any(f'releaseVersionName = "{v}"' in gradle for v in ['1.9.1','1.9.2','1.9.3','2.0.0','2.1.0']):
        p191=ROOT/'tests/golden/1_9_1_compile_hotfix_contract.json'; require(p191.is_file(),'1.9.1 successor contract missing')
        c191=json.loads(p191.read_text())
        require(c191.get('predecessor')=='1.9.0','1.9.1 successor must chain from 1.9.0')
        require(c191.get('predecessorHashes')==expected_successor(c),'1.9.1 predecessor inventory is not the exact locked 1.9.0 production tree')
        if 'releaseVersionName = "1.9.2"' in gradle or 'releaseVersionName = "1.9.3"' in gradle or ('releaseVersionName = "2.0.0"' in gradle or 'releaseVersionName = "2.1.0"' in gradle):
            p192=ROOT/'tests/golden/1_9_2_ui_encyclopedia_contract.json'; require(p192.is_file(),'1.9.2 successor contract missing')
            c192=json.loads(p192.read_text())
            require(c192.get('predecessor')=='1.9.1','1.9.2 successor must chain from 1.9.1')
            require(c192.get('predecessorHashes')==expected_successor(c191),'1.9.2 predecessor inventory is not the exact locked 1.9.1 production tree')
        return
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c['allowlistedNewFiles']
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'successor hash drift: '+rel)
        else:
            require(sha(f)==old,'non-allowlisted production regression: '+rel)
    require(set(inv())==set(pre)|set(added),'production inventory drift')
    for rel,spec in added.items():
        require((ROOT/rel).is_file(),'new allowlisted production file missing: '+rel)
        require(sha(ROOT/rel)==spec['successorSha256'],'new file hash drift: '+rel)

if __name__=='__main__': main_guard('verify_1_9_0_regression_contract',verify)
