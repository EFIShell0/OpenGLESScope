#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib,json

def verify():
    p=ROOT/'tests/golden/1_8_0_vulkanscope_common_icon_hashes.json'
    require(p.is_file(),'1.8.0 common icon hash manifest missing')
    data=json.loads(p.read_text(encoding='utf-8'))
    require(data.get('reference')=='VulkanScope 1.4.3','icon reference drift')
    hashes=data.get('sha256') or {}
    require(data.get('count')==72 and len(hashes)==72,'expected 72 shared VulkanScope/OpenGLESScope icons')
    base=ROOT/'app/src/main/res/drawable'
    for name,expected in hashes.items():
        f=base/name
        require(f.is_file(),'shared icon missing: '+name)
        actual=hashlib.sha256(f.read_bytes()).hexdigest()
        require(actual==expected,'shared VulkanScope icon drift: '+name)
if __name__=='__main__': main_guard('verify_1_8_0_common_icons',verify)
