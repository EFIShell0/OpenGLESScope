#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json
from gate_common import ROOT,require,main_guard

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def inv():
    out=[]
    for base in ['app','gradle','registry']:
        d=ROOT/base
        if d.exists():out += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x).replace('\\','/') and not x.name.endswith('.bak')]
    for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        if (ROOT/rel).is_file():out.append(rel)
    return sorted(set(out))

def verify():
    f=ROOT/'tests/golden/2_2_2_video_ui_parity_regression_contract.json'
    require(f.is_file(),'missing video parity successor regression contract')
    c=json.loads(f.read_text(encoding='utf-8'))
    require(c.get('release')=='2.2.2' and c.get('predecessor')=='2.2.1','release predecessor identity drift')
    require(c.get('predecessorZipSha256')=='918b7b378aa5cbbc6bf15a210e1623b77169834005e813b0d67c9181e30b4f85','immutable 2.2.1 ZIP digest drift')
    old=c['predecessorHashes']; allowed=c['allowlistedChanges']; new=c.get('allowlistedNewFiles',{})
    require(not c.get('removedFiles') and not new,'2.2.2 cannot add/remove production files')
    require(set(old).issubset(set(inv())),'historical 2.2.2 production path removed in successor')
    require(set(allowed)=={'app/build.gradle.kts','app/src/main/java/com/efishell/openglesscope/MainActivity.kt'},'production change allowlist drift')
    for rel,h in old.items():
        require(len(h)==64 and all(x in '0123456789abcdef' for x in h),'malformed predecessor digest: '+rel)
        if rel in allowed:
            require(allowed[rel]['predecessorSha256']==h,'predecessor expected hash mismatch: '+rel)
            require(allowed[rel]['reason'].strip(),'allowlist without an audit reason: '+rel)
            nxt=allowed[rel]['successorSha256']
            require(len(nxt)==64 and all(x in '0123456789abcdef' for x in nxt) and nxt != h,'malformed historical successor digest: '+rel)
    # Do not compare immutable 2.2.2 hashes to a 2.2.3 working tree: that mistake
    # would either block all legitimate successors or encourage weakening old evidence.
    # New successor's full production inventory and hashes are checked by 2.2.3 gate.

if __name__=='__main__':main_guard('verify_2_2_2_regression_contract',verify)
