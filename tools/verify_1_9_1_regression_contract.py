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
    p=ROOT/'tests/golden/1_9_1_compile_hotfix_contract.json'; require(p.is_file(),'1.9.1 regression contract missing')
    c=json.loads(p.read_text()); require(c.get('release')=='1.9.1' and c.get('predecessor')=='1.9.0','1.9.1 regression identity drift')
    require(c.get('predecessorZipSha256')=='c0d9381f1d841171b4354c950c0ced10a83acddb17143d797ea9e211ae99350c','1.9.0 predecessor ZIP hash drift')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    if 'releaseVersionName = "1.9.2"' in gradle or 'releaseVersionName = "1.9.3"' in gradle or ('releaseVersionName = "2.0.0"' in gradle or 'releaseVersionName = "2.1.0"' in gradle):
        p192=ROOT/'tests/golden/1_9_2_ui_encyclopedia_contract.json'; require(p192.is_file(),'1.9.2 successor contract missing')
        c192=json.loads(p192.read_text())
        expected=dict(c['predecessorHashes'])
        for rel,spec in c['allowlistedChanges'].items(): expected[rel]=spec['successorSha256']
        for rel,spec in c.get('allowlistedNewFiles',{}).items(): expected[rel]=spec['successorSha256']
        require(c192.get('predecessor')=='1.9.1','1.9.2 successor must chain from 1.9.1')
        require(c192.get('predecessorHashes')==expected,'1.9.2 predecessor inventory is not the exact locked 1.9.1 production tree')
        return
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c.get('allowlistedNewFiles',{})
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'successor hash drift: '+rel)
        else: require(sha(f)==old,'non-allowlisted production regression: '+rel)
    require(set(inv())==set(pre)|set(added),'production inventory drift')
if __name__=='__main__': main_guard('verify_1_9_1_regression_contract',verify)
