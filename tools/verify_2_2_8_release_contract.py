#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard
CHANGED={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'}
PRE_ZIP='02d6c1ac92bd0427d1d7ef578b0f1dc680f927fbbf68bebd2b2bf0c50effd1dc'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def inventory():
    out=[]
    for base in ('app','gradle','registry'):
        out += [p.relative_to(ROOT).as_posix() for p in (ROOT/base).rglob('*') if p.is_file() and '/build/' not in p.as_posix() and not p.name.endswith('.bak')]
    for name in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat'):
        if (ROOT/name).is_file():out.append(name)
    return sorted(set(out))
def verify():
    c=json.loads((ROOT/'tests/golden/2_2_8_release_regression_contract.json').read_text())
    p=json.loads((ROOT/'tests/golden/2_2_7_reference_ui_regression_contract.json').read_text())
    require(c['release']=='2.2.8' and c['predecessor']=='2.2.7' and c['predecessorZipSha256']==PRE_ZIP,'exact immutable 2.2.7 predecessor ZIP identity drift')
    predecessor={**{rel:p['allowlistedChanges'].get(rel,{}).get('successorSha256',h) for rel,h in p['predecessorHashes'].items()},**{rel:rec['sha256'] for rel,rec in p['allowlistedNewFiles'].items()}}
    require(c['predecessorHashes']==predecessor,'159 predecessor production hashes not chained to 2.2.7')
    changed=c['allowlistedChanges'];new=c['allowlistedNewFiles']; removed=c.get('removedFiles',[])
    require(set(changed)==CHANGED and not new and not removed,'unexpected production change allowlist')
    successor_229=json.loads((ROOT/'tests/golden/2_2_9_release_regression_contract.json').read_text())
    require(successor_229['predecessorHashes']=={n:changed.get(n,{}).get('successorSha256',h) for n,h in predecessor.items()},'2.2.8 historical successor hash not frozen')
    require(inventory()==sorted(set(predecessor) | set(successor_229['allowlistedNewFiles'])),'unexpected production file addition/removal')
    for rel,rec in successor_229['allowlistedNewFiles'].items():
        require(sha(ROOT/rel)==rec['sha256'],'later release added artifact drift: '+rel)
    for rel,old in predecessor.items():
        actual=sha(ROOT/rel)
        if rel in CHANGED:
            rec=changed[rel]
            require(rec['predecessorSha256']==old and rec['reason'].strip() and rec['successorSha256']!=old and successor_229['predecessorHashes'][rel]==rec['successorSha256'],'unreviewed historical successor bytes: '+rel)
        else:
            require(actual==old,'unrelated production file mutated: '+rel)
if __name__=='__main__':main_guard('verify_2_2_8_release_contract',verify)
