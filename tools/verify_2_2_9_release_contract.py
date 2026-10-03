#!/usr/bin/env python3
from __future__ import annotations
import json,hashlib
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
LOGO='app/src/main/res/drawable-nodpi/openglesscope_logo_horizontal_aligned.png'
CHANGED={'app/build.gradle.kts',MAIN}
PRE_SHA='1138fa2b2b71db24c567de884331d9d1898c4eed861bc90032bcbe9d1976e1dd'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def inventory():
    out=[]
    for base in ('app','gradle','registry'):
        out.extend(p.relative_to(ROOT).as_posix() for p in (ROOT/base).rglob('*') if p.is_file() and '/build/' not in p.as_posix() and p.suffix not in {'.pyc','.bak'} and '__pycache__' not in p.parts)
    out.extend(n for n in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat') if (ROOT/n).is_file())
    return sorted(set(out))
def verify():
    old=json.loads((ROOT/'tests/golden/2_2_8_release_regression_contract.json').read_text())
    c=json.loads((ROOT/'tests/golden/2_2_9_release_regression_contract.json').read_text())
    p={rel:old['allowlistedChanges'].get(rel,{}).get('successorSha256',h) for rel,h in old['predecessorHashes'].items()}
    p.update({rel:record['sha256'] for rel,record in old['allowlistedNewFiles'].items()})
    require(c['release']=='2.2.9' and c['predecessor']=='2.2.8' and c['predecessorZipSha256']==PRE_SHA,'exact immutable predecessor identity drift')
    require(c['predecessorHashes']==p,'historical 2.2.8 SHA-256 chain drift')
    changes=c['allowlistedChanges'];new=c['allowlistedNewFiles']
    require(set(changes)==CHANGED and set(new)=={LOGO} and not c.get('removedFiles'),'unexpected historical 2.2.9 allowlist')
    historical_successor=p.copy()
    for rel,h in changes.items():
        require(h['predecessorSha256']==p[rel] and h['reason'].strip(),'historical 2.2.9 predecessor change chain drift: '+rel)
        historical_successor[rel]=h['successorSha256']
    historical_successor.update({rel:record['sha256'] for rel,record in new.items()})
    require(new[LOGO]['reason'].strip(),'historical logo evidence reason drift')
    next_release=json.loads((ROOT/'tests/golden/2_2_10_release_regression_contract.json').read_text())
    require(next_release['predecessor']=='2.2.9' and next_release['predecessorHashes']==historical_successor,'2.2.9->2.2.10 immutable source chain drift')
    require(inventory()==sorted(historical_successor),'unexpected production additions/deletions')
    new_changes=next_release['allowlistedChanges']
    require(set(new_changes)==CHANGED and not next_release['allowlistedNewFiles'] and not next_release.get('removedFiles'),'unexpected 2.2.10 production allowlist')
    next_after=json.loads((ROOT/'tests/golden/2_2_11_release_regression_contract.json').read_text())
    require(next_after['predecessorHashes']=={n:new_changes.get(n,{}).get('successorSha256',h) for n,h in historical_successor.items()},'2.2.10 successor chain lost in 2.2.11')
    for rel,h in historical_successor.items():
        actual=sha(ROOT/rel)
        if rel in new_changes:
            rec=new_changes[rel];require(rec['predecessorSha256']==h and rec['successorSha256']==next_after['predecessorHashes'][rel] and rec['successorSha256']!=h and rec['reason'].strip(),'2.2.10 source not hash-allowlisted: '+rel)
        else:require(actual==h,'unrelated production mutation: '+rel)
if __name__=='__main__':main_guard('verify_2_2_9_release_contract',verify)
