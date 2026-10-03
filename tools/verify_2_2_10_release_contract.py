#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
PRE='b575307b6acac5a14672fd01fa73246a37430368287ec4af8495df3c427f0098'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def inventory():
    out=[]
    for base in ('app','gradle','registry'):
        out.extend(p.relative_to(ROOT).as_posix() for p in (ROOT/base).rglob('*') if p.is_file() and '/build/' not in p.as_posix() and p.suffix not in {'.pyc','.bak'} and '__pycache__' not in p.parts)
    out.extend(n for n in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat') if (ROOT/n).is_file())
    return sorted(set(out))
def verify():
    c=json.loads((ROOT/'tests/golden/2_2_10_release_regression_contract.json').read_text())
    p=json.loads((ROOT/'tests/golden/2_2_9_release_regression_contract.json').read_text())
    previous={n:p['allowlistedChanges'].get(n,{}).get('successorSha256',h) for n,h in p['predecessorHashes'].items()}
    previous.update({n:record['sha256'] for n,record in p['allowlistedNewFiles'].items()})
    require(c['release']=='2.2.10' and c['predecessor']=='2.2.9' and c['predecessorZipSha256']==PRE,'immutable predecessor ZIP identity changed')
    require(c['predecessorHashes']==previous,'2.2.9 source successor SHA-256 chain mismatch')
    changes=c['allowlistedChanges']
    require(set(changes)=={MAIN,GRADLE} and not c.get('removedFiles') and not c.get('allowlistedNewFiles'),'unexpected production modification')
    require(inventory()==sorted(previous),'source additions/removals not reviewed')
    successor=json.loads((ROOT/'tests/golden/2_2_11_release_regression_contract.json').read_text())
    require(successor['predecessorHashes']=={n:changes.get(n,{}).get('successorSha256',h) for n,h in previous.items()},'2.2.10 successor not frozen in 2.2.11')
    for name,old_hash in previous.items():
        actual=sha(ROOT/name)
        if name in changes:
            entry=changes[name]
            require(entry['predecessorSha256']==old_hash and entry['successorSha256']==successor['predecessorHashes'][name] and entry['successorSha256']!=old_hash and entry['reason'].strip(),'changed source is not SHA-allowlisted: '+name)
        else:require(actual==old_hash,'unrelated production drift: '+name)
    g=(ROOT/GRADLE).read_text()
    require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g,'current producer identity mismatched')
    require('minorApiLevel = 2' in g and 'targetSdk = 37' in g,'locked API 37.2 build baseline lost')
if __name__=='__main__':main_guard('verify_2_2_10_release_contract',verify)
