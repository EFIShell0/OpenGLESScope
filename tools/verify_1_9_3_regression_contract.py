#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def inv():
    out=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists(): out += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x) and not x.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): out.append(rel)
    return sorted(set(out))
def verify():
    p=ROOT/'tests/golden/1_9_3_spec_reporting_contract.json'; require(p.is_file(),'1.9.3 regression contract missing')
    c=json.loads(p.read_text()); require(c.get('release')=='1.9.3' and c.get('predecessor')=='1.9.2','1.9.3 regression identity drift')
    require(c.get('predecessorZipSha256')=='541a7a6b18cf6ca851bd624e6e0418a32589f98319db5c42913785dfd39c4c4b','1.9.2 predecessor ZIP hash drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c.get('allowlistedNewFiles',{})
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    if 'releaseVersionName = "2.0.0"' in gradle or 'releaseVersionName = "2.1.0"' in gradle:
        p200=ROOT/'tests/golden/2_0_0_vulkanscope_3_0_12_parity_contract.json'; require(p200.is_file(),'2.0.0 successor contract missing')
        c200=json.loads(p200.read_text(encoding='utf-8'))
        expected=dict(pre)
        for rel,spec in allowed.items(): expected[rel]=spec['successorSha256']
        for rel,spec in added.items(): expected[rel]=spec['successorSha256']
        require(c200.get('predecessor')=='1.9.3-fixed','2.0.0 successor must chain from 1.9.3-fixed')
        require(c200.get('predecessorHashes')==expected,'2.0.0 predecessor inventory is not the exact locked 1.9.3-fixed production tree')
        return
    require(set(inv())==set(pre)|set(added),'production inventory drift')
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'successor hash drift: '+rel)
        else: require(sha(f)==old,'non-allowlisted production regression: '+rel)
    for rel,spec in added.items():
        require((ROOT/rel).is_file(),'allowlisted new production file missing: '+rel)
        require(sha(ROOT/rel)==spec['successorSha256'],'new production file hash drift: '+rel)
if __name__=='__main__': main_guard('verify_1_9_3_regression_contract',verify)
