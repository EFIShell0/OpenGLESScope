#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json
from gate_common import ROOT, require, main_guard

MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory():
    output=[]
    for base in ('app','gradle','registry'):
        folder=ROOT/base
        if folder.exists(): output += [str(p.relative_to(ROOT)) for p in folder.rglob('*') if p.is_file() and '/build/' not in str(p).replace('\\','/') and not p.name.endswith('.bak')]
    for name in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat'):
        if (ROOT/name).is_file(): output.append(name)
    return sorted(set(output))
def verify():
    parent=json.loads((ROOT/'tests/golden/2_2_5_desktop_accessibility_regression_contract.json').read_text())
    current=json.loads((ROOT/'tests/golden/2_2_6_kotlin_compile_parity_regression_contract.json').read_text())
    require(current['release']=='2.2.6' and current['predecessor']=='2.2.5','2.2.6 predecessor/version identity wrong')
    require(current['predecessorZipSha256']=='6896534078df8074f98d05894d8b8cc0bb23e1d02e7fb745d01665a7b3041321','immutable predecessor ZIP changed')
    predecessor={name:parent['allowlistedChanges'].get(name,{}).get('successorSha256',value) for name,value in parent['predecessorHashes'].items()}
    require(current['predecessorHashes']==predecessor,'2.2.5 complete successor SHA-256 chain mismatch')
    require(inventory()==sorted(predecessor),'unreviewed production inventory additions or removals')
    require(set(current['allowlistedChanges'])=={'app/build.gradle.kts',MAIN},'source allowlist expanded')
    require(not current.get('allowlistedNewFiles') and not current.get('removedFiles'),'production file addition/deletion forbidden')
    for name,previous_hash in predecessor.items():
        actual=sha(ROOT/name)
        if name not in current['allowlistedChanges']:
            require(actual==previous_hash,'unreviewed production-source change: '+name)
        else:
            allowed=current['allowlistedChanges'][name]
            require(allowed['predecessorSha256']==previous_hash and allowed['reason'].strip(),'missing predecessor or reason: '+name)
            require(allowed['successorSha256']!=previous_hash and actual==allowed['successorSha256'],'2.2.6 successor hash mismatch: '+name)
if __name__=='__main__': main_guard('verify_2_2_6_regression_contract',verify)
