#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory():
    result=[]
    for base in ['app','gradle','registry']:
        source=ROOT/base
        if source.exists():result += [str(p.relative_to(ROOT)) for p in source.rglob('*') if p.is_file() and '/build/' not in str(p).replace('\\','/') and not p.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file():result.append(rel)
    return sorted(set(result))
def verify():
    c=json.loads((ROOT/'tests/golden/2_2_4_paging_analysis_report_regression_contract.json').read_text())
    p=json.loads((ROOT/'tests/golden/2_2_3_full_ui_regression_contract.json').read_text())
    require(c['release']=='2.2.4' and c['predecessor']=='2.2.3','release identity regression')
    require(c['predecessorZipSha256']=='b238e6ac4db108f404afc3291604c8b51833d16e0c450440f9cf316645edd9b9','archive SHA lock changed')
    expected={path:p['allowlistedChanges'].get(path,{}).get('successorSha256',old) for path,old in p['predecessorHashes'].items()}
    require(c['predecessorHashes']==expected,'immutable predecessor hash chain broken')
    require(set(inventory())==set(expected),'unreviewed production path addition/removal')
    allow=c['allowlistedChanges'];require(set(allow)=={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'unexpected allowlist')
    require(not c.get('allowlistedNewFiles') and not c.get('removedFiles'),'production file addition/removal')
    for path,old in expected.items():
        file=ROOT/path;require(file.is_file(),'missing production path: '+path)
        if path in allow:
            change=allow[path]
            require(change['predecessorSha256']==old and change['reason'].strip(),'unreviewed reason/hash: '+path)
            require(change['successorSha256']!=old and digest(file)==change['successorSha256'],'unauthorized successor modification: '+path)
        else:require(digest(file)==old,'unreviewed production drift: '+path)
if __name__=='__main__':main_guard('verify_2_2_4_regression_contract',verify)
