#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

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
 p=ROOT/'tests/golden/1_7_0_ui_parity_contract.json'; require(p.is_file(),'1.7.0 regression contract missing')
 c=json.loads(p.read_text())
 require(c.get('release')=='1.7.0' and c.get('predecessor')=='1.6.0','release identity drift')
 require(c.get('predecessorZipSha256')=='51527cfdbc3ae6eec9b9efd41e9748457bd12599b494dcba436b66dfbad5506d','predecessor ZIP hash drift')
 pre=c['predecessorHashes']; allowed=c['allowlistedChanges']; added=c['allowlistedNewFiles']
 for rel,old in pre.items():
  f=ROOT/rel; require(f.is_file(),'production file removed: '+rel)
  if rel in allowed:
   require(allowed[rel]['predecessorSha256']==old,'predecessor hash mismatch: '+rel)
   require(sha(f)==allowed[rel]['successorSha256'],'successor hash drift: '+rel)
  else: require(sha(f)==old,'non-allowlisted production regression: '+rel)
 require(set(inv())==set(pre)|set(added),'production inventory drift')
 for rel,spec in added.items(): require(sha(ROOT/rel)==spec['successorSha256'],'new file hash drift: '+rel)
if __name__=='__main__': main_guard('verify_1_7_0_regression_contract',verify)
