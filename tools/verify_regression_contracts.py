#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def verify():
    p=ROOT/'tests/golden/1_4_1_build_hotfix_contract.json'; require(p.is_file(),'1.4.1 build-hotfix regression contract missing')
    c=json.loads(p.read_text()); require(c['release']=='1.4.1' and c['predecessor']=='1.4.0','regression identity drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']
    for rel,old_hash in pre.items():
        f=ROOT/rel; require(f.is_file(),'predecessor production file removed: '+rel)
        if rel in allowed:
            require(sha(f)==allowed[rel]['successorSha256'],'allowlisted successor hash drift: '+rel)
        else:
            require(sha(f)==old_hash,'non-allowlisted production regression: '+rel)
    for rel,entry in allowed.items():
        require(entry.get('reason'),'allowlisted change missing reason: '+rel)
    current=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists(): current.extend(str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x))
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): current.append(rel)
    extras=sorted(set(current)-set(pre))
    require(not extras,'new production files outside 1.4.0 predecessor inventory: '+', '.join(extras))
if __name__=='__main__': main_guard('verify_regression_contracts',verify)
