#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def inv():
    out=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists(): out += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x) and not x.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file(): out.append(rel)
    return sorted(set(out))

def verify():
    p=ROOT/'tests/golden/2_1_1_quality_parity_contract.json'; require(p.is_file(),'2.1.1 regression contract missing')
    c=json.loads(p.read_text(encoding='utf-8'))
    require(c.get('release')=='2.1.1' and c.get('predecessor')=='2.1.0','2.1.1 regression identity drift')
    require(c.get('predecessorZipSha256')=='6e2608ee7de86b43bc353d2531eb7f8609d28aa17d37a6a090cfe4f522ba42d0','2.1.0 predecessor ZIP hash drift')
    pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c.get('allowlistedNewFiles',{})
    require(not c.get('removedFiles'),'2.1.1 must not remove predecessor production files')
    require(set(inv())==set(pre)|set(added),'2.1.1 production inventory drift')
    require(set(allowed)=={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'2.1.1 changed-production allowlist widened')
    require(set(added)=={'app/src/main/res/drawable/ic_precision.xml','app/src/main/res/drawable/ic_configs.xml'},'2.1.1 new-production allowlist widened')
    for rel,old in pre.items():
        f=ROOT/rel; require(f.is_file(),'predecessor production file removed: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
            require(sha(f)==allowed[rel]['successorSha256'],'2.1.1 successor hash drift: '+rel)
        else: require(sha(f)==old,'non-allowlisted 2.1.0 production regression: '+rel)
    for rel,spec in added.items():
        require((ROOT/rel).is_file(),'allowlisted new production file missing: '+rel)
        require(sha(ROOT/rel)==spec['successorSha256'],'new production file hash drift: '+rel)
if __name__=='__main__': main_guard('verify_2_1_1_regression_contract',verify)
