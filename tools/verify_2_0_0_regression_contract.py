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

def verify():
    p=ROOT/'tests/golden/2_0_0_vulkanscope_3_0_12_parity_contract.json'
    require(p.is_file(),'2.0.0 regression contract missing')
    c=json.loads(p.read_text(encoding='utf-8'))
    require(c.get('release')=='2.0.0' and c.get('predecessor')=='1.9.3-fixed','2.0.0 regression identity drift')
    require(c.get('predecessorZipSha256')=='5527888f8dd5c1b2b689f81a2595924c468f38ff547dfe2f4a707197f9616f6e','1.9.3-fixed predecessor ZIP hash drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c.get('allowlistedNewFiles',{})
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    if 'releaseVersionName = "2.1.0"' in gradle:
        p210=ROOT/'tests/golden/2_1_0_quality_parity_contract.json'; require(p210.is_file(),'2.1.0 successor contract missing')
        c210=json.loads(p210.read_text(encoding='utf-8'))
        expected=dict(pre)
        for rel,spec in allowed.items(): expected[rel]=spec['successorSha256']
        for rel,spec in added.items(): expected[rel]=spec['successorSha256']
        require(c210.get('predecessor')=='2.0.0','2.1.0 successor must chain from 2.0.0')
        require(c210.get('predecessorHashes')==expected,'2.1.0 predecessor inventory is not the exact locked 2.0.0 production tree')
        return
    require(set(inv())==set(pre)|set(added),'2.0.0 production inventory drift')
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'2.0.0 successor hash drift: '+rel)
        else:
            require(sha(f)==old,'non-allowlisted 1.9.3-fixed production regression: '+rel)
    for rel,spec in added.items():
        require((ROOT/rel).is_file(),'allowlisted new production file missing: '+rel)
        require(sha(ROOT/rel)==spec['successorSha256'],'new production file hash drift: '+rel)
if __name__=='__main__': main_guard('verify_2_0_0_regression_contract',verify)
