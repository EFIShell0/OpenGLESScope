#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT, require, main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
NEW_ICON='app/src/main/res/drawable/ic_android_brand.xml'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def inventory():
    out=[]
    for base in ('app','gradle','registry'):
        out += [p.relative_to(ROOT).as_posix() for p in (ROOT/base).rglob('*') if p.is_file() and '/build/' not in p.as_posix() and not p.name.endswith('.bak')]
    for name in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat'):
        if (ROOT/name).is_file():out.append(name)
    return sorted(set(out))
def verify():
    c=json.loads((ROOT/'tests/golden/2_2_7_reference_ui_regression_contract.json').read_text())
    p=json.loads((ROOT/'tests/golden/2_2_6_kotlin_compile_parity_regression_contract.json').read_text())
    require(c['release']=='2.2.7' and c['predecessor']=='2.2.6','2.2.7 predecessor release identity drift')
    require(c['predecessorZipSha256']=='a1e2f69e864766cc5c47dac21819fc40051fc5f5552ac6c85cc4beab2866376d','immutable exact 2.2.6 predecessor ZIP not recorded')
    predecessor={rel:p['allowlistedChanges'].get(rel,{}).get('successorSha256',h) for rel,h in p['predecessorHashes'].items()}
    require(c['predecessorHashes']==predecessor,'158 source predecessor hashes no longer chain from 2.2.6')
    changed=c['allowlistedChanges'];new=c['allowlistedNewFiles']
    next_release=json.loads((ROOT/'tests/golden/2_2_8_release_regression_contract.json').read_text())
    require(next_release['predecessor']=='2.2.7' and next_release['predecessorHashes']=={**{rel:changed.get(rel,{}).get('successorSha256',h) for rel,h in predecessor.items()},**{rel:rec['sha256'] for rel,rec in new.items()}},'historical 2.2.7 source chain not frozen in successor')
    require(set(changed)=={'app/build.gradle.kts',MAIN},'unreviewed production change allowlist')
    require(set(new)=={NEW_ICON} and not c.get('removedFiles'),'only theme palette Android drawable may be introduced')
    require(inventory()==sorted(set(predecessor)|set(new)),'unreviewed production file addition or removal')
    for rel,old in predecessor.items():
        actual=sha(ROOT/rel)
        if rel in changed:
            rec=changed[rel]
            require(rec['predecessorSha256']==old and rec['reason'].strip() and rec['successorSha256']!=old and next_release['predecessorHashes'][rel]==rec['successorSha256'],'unreviewed successor diff or wrong audit: '+rel)
        else:require(actual==old,'immutable unrelated production drift: '+rel)
    for rel,rec in new.items():
        require(rec['reason'].strip() and next_release['predecessorHashes'][rel]==rec['sha256'] and sha(ROOT/rel)==rec['sha256'],'new palette logo hash/reason mismatch')
if __name__=='__main__':main_guard('verify_2_2_7_regression_contract',verify)
