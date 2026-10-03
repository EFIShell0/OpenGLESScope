#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT, require, main_guard

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def inv():
    out=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists():
            out += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x) and not x.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): out.append(rel)
    return sorted(set(out))

def verify():
    p=ROOT/'tests/golden/2_1_0_quality_parity_contract.json'
    require(p.is_file(),'2.1.0 regression contract missing')
    c=json.loads(p.read_text(encoding='utf-8'))
    require(c.get('release')=='2.1.0' and c.get('predecessor')=='2.0.0','2.1.0 regression identity drift')
    require(c.get('predecessorZipSha256')=='4502f803444b997e1cc07af58022b67e282325730a554fa2879fec03346a51f5','2.0.0 predecessor ZIP hash drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c.get('allowlistedNewFiles',{})
    require(not c.get('removedFiles'),'2.1.0 must not remove predecessor production files')
    require(set(inv())==set(pre)|set(added),'2.1.0 production inventory drift')
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'2.1.0 successor hash drift: '+rel)
        else:
            require(sha(f)==old,'non-allowlisted 2.0.0 production regression: '+rel)
    for rel,spec in added.items():
        require((ROOT/rel).is_file(),'allowlisted new production file missing: '+rel)
        require(sha(ROOT/rel)==spec['successorSha256'],'new production file hash drift: '+rel)
    require(set(allowed)=={'app/build.gradle.kts','app/src/main/cpp/openglesscope.cpp','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'2.1.0 changed-production allowlist widened unexpectedly')
    require(set(added)=={'app/src/main/res/drawable-nodpi/ic_opening_animation_toggle.png'},'2.1.0 new-production allowlist widened unexpectedly')
if __name__=='__main__': main_guard('verify_2_1_0_regression_contract',verify)
