#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def verify():
 p=ROOT/'tests/golden/1_5_0_quality_contract.json'; require(p.is_file(),'1.5.0 regression contract missing')
 c=json.loads(p.read_text()); require(c['release']=='1.5.0' and c['predecessor']=='1.4.1','regression identity drift')
 pre=c['predecessorHashes']; allowed=c['allowlistedChanges']
 for rel,old in pre.items():
  f=ROOT/rel; require(f.is_file(),'predecessor production file removed: '+rel)
  if rel in allowed: require(sha(f)==allowed[rel]['successorSha256'],'allowlisted successor hash drift: '+rel)
  else: require(sha(f)==old,'non-allowlisted production regression: '+rel)
 current=[]
 for base in ['app','gradle','registry']:
  d=ROOT/base
  if d.exists(): current += [str(x.relative_to(ROOT)) for x in d.rglob('*') if x.is_file() and '/build/' not in str(x)]
 for rel in ['build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
  if (ROOT/rel).is_file(): current.append(rel)
 require(not sorted(set(current)-set(pre)),'new production files outside predecessor inventory')
if __name__=='__main__': main_guard('verify_1_5_0_regression_contract',verify)
