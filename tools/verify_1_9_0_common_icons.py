#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import hashlib, json, re

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()

def verify():
    p=ROOT/'tests/golden/1_9_0_common_icon_hashes.json'
    require(p.is_file(),'1.9.0 common icon manifest missing')
    c=json.loads(p.read_text())
    require(c.get('reference')=='VulkanScope 1.4.3','icon reference drift')
    shared=c.get('sharedIcons',{})
    require(len(shared)==75,'expected 75 shared API-neutral icons')
    for name,expected in shared.items():
        f=ROOT/'app/src/main/res/drawable'/name
        require(f.is_file(),'shared VulkanScope icon missing: '+name)
        require(sha(f)==expected,'shared VulkanScope icon byte drift: '+name)
    expected_excluded=['ic_memory.xml','ic_memory_heap.xml','ic_memory_type.xml','ic_queues.xml','ic_surface_khr.xml','ic_surface_search.xml','ic_video.xml','ic_zip_download.xml']
    require(c.get('excludedVulkanOnlyIcons')==expected_excluded,'Vulkan-only exclusion list drift')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    # Icons that are common and have an actual OpenGLESScope action counterpart must be wired, not merely bundled.
    for icon in ['ic_add','ic_baseline','ic_profile','ic_action_import','ic_export','ic_save','ic_folder','ic_delete','ic_copy','ic_search','ic_qr','ic_share','ic_upload','ic_close','ic_chevron_right']:
        require(f'R.drawable.{icon}' in main,'common icon is not wired to any OpenGLESScope action: '+icon)
    # Turnip-only view modes and Vulkan-only queue/memory/video/surface helper icons are intentionally not required.

if __name__=='__main__': main_guard('verify_1_9_0_common_icons',verify)
