#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
from pathlib import Path
import hashlib

GL_SHA='b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3'
EGL_SHA='3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8'
VULKAN_RULES_SHA='9c89153fb34352f567ccdec7b3685ac1feab56c45b46b22f320375dfed66e7ca'

def sha(path: Path): return hashlib.sha256(path.read_bytes()).hexdigest()

def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text()
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    cmake=(ROOT/'app/src/main/cpp/CMakeLists.txt').read_text()
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require((('releaseVersionName = "2.0.0"' in gradle and 'releaseVersionCode = 2000' in gradle) or ('releaseVersionName = "2.1.0"' in gradle and 'releaseVersionCode = 2100' in gradle)),'2.0.0+ release identity drift')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA,'gl.xml release lock drift')
    require(sha(ROOT/'registry/egl.xml')==EGL_SHA,'egl.xml supplied snapshot lock drift')
    require(EGL_SHA in cmake,'CMake EGL registry lock drift')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'VulkanScope 3.0.12 methodology reference drift')
    headings=[x for x in ref.read_text().splitlines() if x.startswith('## ')]
    require(len(headings)==199,'VulkanScope reference heading census drift')
    require('private const val COLLECTION_PAGE_SIZE = 25' in main,'25-row paging contract drift')
    require(main.count('CollectionPager(')>=7,'shared paging not applied broadly enough')
    require('enableEdgeToEdge()' in main,'edge-to-edge shell missing')
    require('CompactBottomNavigationBar(' in main,'compact bottom navigation missing')
    # An old helper may remain for predecessor compatibility, but production scaffold must not select it.
    require(('releaseVersionName = "2.0.0"' in gradle and main.count('CompactNavigationRail(')==1 and 'useRail' not in main) or ('releaseVersionName = "2.1.0"' in gradle and 'CompactNavigationRail' not in main),'landscape navigation contract drift')
    require('prefs.getBoolean("opening_animation_enabled", true)' in main and 'prefs.edit().putBoolean("opening_animation_enabled", enabled).apply()' in main and 'OpenGLESScopeOpeningAnimation' in main,'opening gate preference/animation missing')
    require('delay(3200)' in main or '3_200' in main,'opening watchdog missing')
    require('Show opening animation' in main,'opening-animation preference UI missing')
    for token in ['LIST(', 'COMPACT_LIST(', 'DETAILED_LIST(', 'GRID(', 'DENSE_GRID(', 'LARGE_TILES(']:
        require(token in main,'six-view file manager contract missing: '+token)
    for token in ['NAME_ASC(', 'NAME_DESC(', 'MODIFIED_NEWEST(', 'MODIFIED_OLDEST(', 'CREATED_NEWEST(', 'CREATED_OLDEST(']:
        require(token in main,'six-sort file manager contract missing: '+token)
    require(('FileManagerOptionsDialog(' in main) or ('FileManagerOptionsChooser(' in main),'file-manager unified view/sort chooser missing')
    require('assetSizeBytes: Long?' in main and 'selected.optLong("size"' in main,'trusted release asset size metadata missing')
    require('Update package size does not match the official GitHub release metadata.' in main and 'Downloaded package size does not match the official GitHub release metadata.' in main,'asset-size mismatch fail-closed checks missing')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report/submission schema drift')
    require('Database' in rules and ('1.0.9' in rules or '1.0.10' in rules or '2.1.0 / 2100' in rules),'2.0.0+ companion database contract missing')
    require((('releaseVersionName = "2.0.0"' in gradle and 'Release 2.0.0 VulkanScope 3.0.12 full shared-quality parity and dual-registry lock' in rules) or ('releaseVersionName = "2.1.0"' in gradle and 'Release 2.1.0 VulkanScope 3.0.12 rules/UI/query/performance parity audit' in rules)),'current parity rules section missing')

if __name__=='__main__': main_guard('verify_2_0_0_parity',verify)
