#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory():
    result=[]
    for base in ('app','gradle','registry'):
        folder=ROOT/base
        if folder.exists():result += [str(p.relative_to(ROOT)) for p in folder.rglob('*') if p.is_file() and '/build/' not in str(p).replace('\\','/') and not p.name.endswith('.bak')]
    for rel in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat'):
        if (ROOT/rel).is_file():result.append(rel)
    return sorted(set(result))

def verify():
    previous=json.loads((ROOT/'tests/golden/2_2_4_paging_analysis_report_regression_contract.json').read_text())
    current=json.loads((ROOT/'tests/golden/2_2_5_desktop_accessibility_regression_contract.json').read_text())
    require(current['release']=='2.2.5' and current['predecessor']=='2.2.4','release/predecessor identity mismatch')
    require(current['predecessorZipSha256']=='c10772b9f32ce7ec73341f6a4e6d60aed6a1a27797980896d5eef0adc88ccee8','immutable predecessor ZIP SHA drift')
    expected={name:previous['allowlistedChanges'].get(name,{}).get('successorSha256',digest) for name,digest in previous['predecessorHashes'].items()}
    require(current['predecessorHashes']==expected,'2.2.4 successor hash chain broken')
    require(set(inventory())==set(expected),'unauthorized production file addition/removal')
    allow=current['allowlistedChanges'];require(set(allow)=={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'unaudited production changes')
    require(not current.get('allowlistedNewFiles') and not current.get('removedFiles'),'source inventory mutation')
    for name,old in expected.items():
        actual=digest(ROOT/name)
        if name in allow:
            change=allow[name]
            require(change['predecessorSha256']==old and change['reason'].strip(),'missing review evidence for '+name)
            require(change['successorSha256']!=old and actual==change['successorSha256'],'successor source drift: '+name)
        else:require(actual==old,'unreviewed source drift: '+name)
if __name__=='__main__':main_guard('verify_2_2_5_regression_contract',verify)
