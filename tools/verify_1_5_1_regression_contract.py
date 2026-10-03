#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json,re

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()

def expected_successor(c):
    out=dict(c['predecessorHashes'])
    for rel,spec in c['allowlistedChanges'].items(): out[rel]=spec['successorSha256']
    return out

def verify():
    p=ROOT/'tests/golden/1_5_1_compile_hotfix_contract.json'; require(p.is_file(),'1.5.1 regression contract missing')
    c=json.loads(p.read_text()); require(c['release']=='1.5.1' and c['predecessor']=='1.5.0','regression identity drift')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    successor_release='releaseVersionName = "1.6.0"' in gradle
    if successor_release:
        p160=ROOT/'tests/golden/1_6_0_shared_parity_contract.json'; require(p160.is_file(),'1.6.0 successor contract missing')
        c160=json.loads(p160.read_text())
        require(c160.get('predecessor')=='1.5.1','1.6.0 successor must chain from 1.5.1')
        require(c160.get('predecessorHashes')==expected_successor(c),'1.6.0 predecessor inventory is not the exact locked 1.5.1 production tree')
        return
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'predecessor production file removed: '+rel)
        if rel in allowed: require(sha(f)==allowed[rel]['successorSha256'],'allowlisted successor hash drift: '+rel)
        else: require(sha(f)==old,'non-allowlisted production regression: '+rel)
    current=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists(): current += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x)]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): current.append(rel)
    require(not sorted(set(current)-set(pre)),'new production files outside predecessor inventory')
if __name__=='__main__': main_guard('verify_1_5_1_regression_contract',verify)
