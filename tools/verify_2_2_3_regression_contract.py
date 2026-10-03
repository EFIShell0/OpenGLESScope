#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory():
    out=[]
    for base in ['app','gradle','registry']:
        directory=ROOT/base
        if directory.exists():
            out += [str(p.relative_to(ROOT)) for p in directory.rglob('*') if p.is_file() and '/build/' not in str(p).replace('\\','/') and not p.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file():out.append(rel)
    return sorted(set(out))
def verify():
    contract=json.loads((ROOT/'tests/golden/2_2_3_full_ui_regression_contract.json').read_text())
    historic=json.loads((ROOT/'tests/golden/2_2_2_video_ui_parity_regression_contract.json').read_text())
    require(contract['release']=='2.2.3' and contract['predecessor']=='2.2.2','incorrect predecessor identity')
    require(contract['predecessorZipSha256']=='be81f683a410d8436d1d0479142b021f70cff5547e5c45a035569f1b6e4699c0','exact predecessor ZIP digest drift')
    prior=contract['predecessorHashes'];allowed=contract['allowlistedChanges']
    require(not contract.get('allowlistedNewFiles') and not contract.get('removedFiles'),'unexpected production additions/removals')
    require(set(inventory())==set(prior),'production file inventory drift')
    require(set(allowed)=={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'production allowlist changed')
    # 2.2.2 successor hash census must be exactly the 2.2.3 predecessor census:
    # this protects archived source evidence while allowing reviewed version changes.
    for rel,old in historic['predecessorHashes'].items():
        expected=historic['allowlistedChanges'][rel]['successorSha256'] if rel in historic['allowlistedChanges'] else old
        require(prior[rel]==expected,'predecessor contract chain broken: '+rel)
    for rel,expected in prior.items():
        p=ROOT/rel
        require(p.is_file(),'predecessor file removed: '+rel)
        if rel in allowed:
            entry=allowed[rel]
            require(entry['predecessorSha256']==expected and entry['reason'].strip(),'change not correctly justified: '+rel)
            require(sha(p)==entry['successorSha256'],'successor production mutation or drift: '+rel)
            require(sha(p)!=expected,'no-op allowlist entry: '+rel)
        else:
            require(sha(p)==expected,'unreviewed production drift: '+rel)
if __name__=='__main__': main_guard('verify_2_2_3_regression_contract',verify)
