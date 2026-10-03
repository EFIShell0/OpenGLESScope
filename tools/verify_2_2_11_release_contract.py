#!/usr/bin/env python3
from __future__ import annotations
import hashlib,json,xml.etree.ElementTree as ET
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
PRE_SHA='f59cbabbac4cd753c019be54af8be154da827eec25b460b6469c018785d8b2f6'
PERMISSIONS={'android.permission.INTERNET','android.permission.ACCESS_NETWORK_STATE','android.permission.REQUEST_INSTALL_PACKAGES','android.permission.MANAGE_EXTERNAL_STORAGE'}
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory():
    paths=[]
    for base in ('app','gradle','registry'):
        paths.extend(x.relative_to(ROOT).as_posix() for x in (ROOT/base).rglob('*') if x.is_file() and x.suffix not in ('.pyc','.bak') and '__pycache__' not in x.parts and '/build/' not in x.as_posix())
    paths.extend(x for x in ('build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat') if (ROOT/x).is_file())
    return sorted(set(paths))
def verify():
    previous=json.loads((ROOT/'tests/golden/2_2_10_release_regression_contract.json').read_text())
    c=json.loads((ROOT/'tests/golden/2_2_11_release_regression_contract.json').read_text())
    lineage={name:previous['allowlistedChanges'].get(name,{}).get('successorSha256',h) for name,h in previous['predecessorHashes'].items()}
    lineage.update({name:v['sha256'] for name,v in previous['allowlistedNewFiles'].items()})
    require(c['release']=='2.2.11' and c['predecessor']=='2.2.10' and c['predecessorZipSha256']==PRE_SHA,'predecessor identity drift')
    require(c['predecessorHashes']==lineage,'2.2.10 immutable SHA-256 successor chain drift')
    changes=c['allowlistedChanges'];require(set(changes)=={MAIN,GRADLE} and not c.get('allowlistedNewFiles') and not c.get('removedFiles'),'unreviewed production source scope')
    successor=json.loads((ROOT/'tests/golden/2_2_12_release_regression_contract.json').read_text())
    current_lineage={n:changes.get(n,{}).get('successorSha256',h) for n,h in lineage.items()}
    require(successor['predecessorHashes']==current_lineage and successor['predecessor']=='2.2.11','immutable 2.2.11 -> 2.2.12 hash chain drift')
    require(inventory()==sorted(lineage),'unreviewed production file addition/removal')
    latest=json.loads((ROOT/'tests/golden/2_2_13_release_regression_contract.json').read_text())
    current=json.loads((ROOT/'tests/golden/2_2_14_release_regression_contract.json').read_text())
    require(latest['predecessorHashes']==successor['predecessorHashes'] | {n: successor['allowlistedChanges'][n]['successorSha256'] for n in successor['allowlistedChanges']},'2.2.12 -> 2.2.13 lineage changed')
    for name,prev_hash in lineage.items():
        actual=sha(ROOT/name)
        if name in changes:
            change=changes[name]
            require(change['predecessorSha256']==prev_hash and change['successorSha256']==current_lineage[name] and prev_hash!=current_lineage[name] and change['reason'].strip(),'invalid historical source change allowlist: '+name)
            require(latest['allowlistedChanges'][name]['successorSha256']==current['predecessorHashes'][name] and actual==current['allowlistedChanges'][name]['successorSha256'],'current 2.2.14 source fails exact historical chain: '+name)
        else:require(actual==prev_hash,'unrelated/native/registry/resource/manifest drift: '+name)
    gradle=(ROOT/GRADLE).read_text()
    require('val releaseVersionName = "2.2.14"' in gradle and 'val releaseVersionCode = 2214' in gradle,'version identity drift')
    require('minorApiLevel = 2' in gradle and 'targetSdk = 37' in gradle,'SDK 37.2 baseline drift')
    manifest=ET.parse(ROOT/'app/src/main/AndroidManifest.xml').getroot()
    permission_attr='{http://schemas.android.com/apk/res/android}name'
    current=[node.get(permission_attr) for node in manifest.findall('uses-permission')]
    require(len(current)==len(PERMISSIONS) and set(current)==PERMISSIONS,'VulkanScope 3.0.12 permission census mismatches or duplicate permission')
    feature=manifest.find('uses-feature')
    require(feature is not None and feature.get('{http://schemas.android.com/apk/res/android}glEsVersion')=='0x00020000','required OpenGL ES 2.0 feature incorrectly removed as a permission')
if __name__=='__main__':main_guard('verify_2_2_11_release_contract',verify)
