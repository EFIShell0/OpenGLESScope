#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib, json

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def production_inventory():
    current=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists():
            current += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x)]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): current.append(rel)
    return sorted(set(current))

def verify():
    p=ROOT/'tests/golden/1_6_0_shared_parity_contract.json'
    require(p.is_file(),'1.6.0 regression contract missing')
    c=json.loads(p.read_text())
    require(c.get('release')=='1.6.0' and c.get('predecessor')=='1.5.1','1.6.0 regression identity drift')
    require(c.get('predecessorZipSha256')=='13d4007bb2cfe7daeea6ea789152d28156bb7b3b3541e0e2e00647038d2d04de','1.5.1 predecessor ZIP hash drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c['allowlistedNewFiles']
    for rel,old in pre.items():
        f=ROOT/rel
        require(f.is_file(),'predecessor production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel].get('predecessorSha256')==old,'allowlisted predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'allowlisted successor hash drift: '+rel)
        else:
            require(sha(f)==old,'non-allowlisted production regression: '+rel)
    current=set(production_inventory())
    expected=set(pre)|set(added)
    require(current==expected,'production inventory drift: '+str(sorted(current^expected)))
    for rel,spec in added.items():
        f=ROOT/rel
        require(f.is_file(),'allowlisted new production file missing: '+rel)
        require(sha(f)==spec['successorSha256'],'allowlisted new production file hash drift: '+rel)

if __name__=='__main__':
    main_guard('verify_1_6_0_regression_contract',verify)
