#!/usr/bin/env python3
from __future__ import annotations
import hashlib,re
from pathlib import Path
from gate_common import ROOT,require,main_guard

VULKAN_RULES_SHA='9c89153fb34352f567ccdec7b3685ac1feab56c45b46b22f320375dfed66e7ca'
GL_SHA='b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3'
EGL_SHA='3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8'
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()

def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8'); root_gradle=(ROOT/'build.gradle.kts').read_text(encoding='utf-8'); gradle_props=(ROOT/'gradle.properties').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8'); rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8'); styles=(ROOT/'app/src/main/res/values/styles.xml').read_text(encoding='utf-8')
    require('releaseVersionName = "2.1.2"' in gradle and 'releaseVersionCode = 2102' in gradle,'2.1.2/2102 identity drift')
    require(rules.count('# OpenGLESScope Engineering Rules')==1,'rules root must remain singular')
    require('## Release 2.1.2 current toolchain/spec/query/detail parity audit' in rules,'2.1.2 rules contract missing')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'; require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'VulkanScope 3.0.12 methodology reference drift')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA and sha(ROOT/'registry/egl.xml')==EGL_SHA,'locked Khronos registry drift')
    # Current Android/toolchain/dependency contract.
    for phrase in ['compileSdk = 37','minSdk = 31','targetSdk = 37','ndkVersion = "30.0.16248370"','androidx.core:core-ktx:1.19.1','androidx.core:core-splashscreen:1.2.0','androidx.compose.ui:ui:1.12.1','androidx.compose.foundation:foundation:1.12.1','androidx.compose.animation:animation:1.12.1','androidx.compose.material3:material3:1.5.0-alpha28']:
        require(phrase in gradle,'2.1.2 build/dependency drift: '+phrase)
    require('version "9.4.1"' in root_gradle,'AGP 9.4.1 drift')
    require('id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"' in root_gradle,'Compose compiler plugin 2.4.20 drift')
    require('org.jetbrains.kotlin.android' not in root_gradle and 'kotlin-android' not in root_gradle,'legacy Kotlin Android plugin must not be applied under AGP 9 built-in Kotlin')
    require('android.builtInKotlin=false' not in gradle_props.replace(' ',''),'AGP 9 built-in Kotlin must not be disabled')
    require('android.hardware.type.pc' in manifest and 'android:theme="@style/AppTheme.Starting"' in manifest,'Android platform parity/splash manifest drift')
    require('Theme.SplashScreen' in styles and '@drawable/openglesscope_logo_foreground' in styles,'platform splash style drift')
    require('SplashScreen.Companion.installSplashScreen' in main and 'val splashScreen = installSplashScreen()' in main and 'setOnExitAnimationListener' in main,'platform splash runtime parity missing')
    # UI never carries a second manually-maintained version set.
    for field in ['BuildConfig.CORE_KTX_VERSION','BuildConfig.SPLASHSCREEN_VERSION','BuildConfig.ACTIVITY_COMPOSE_VERSION','BuildConfig.COMPOSE_VERSION','BuildConfig.MATERIAL3_VERSION','BuildConfig.LIFECYCLE_VERSION','BuildConfig.OKHTTP_VERSION','BuildConfig.ZXING_VERSION','BuildConfig.AGP_VERSION','BuildConfig.KOTLIN_VERSION','BuildConfig.GRADLE_VERSION','BuildConfig.NDK_VERSION','BuildConfig.COMPILE_SDK_LEVEL','BuildConfig.MIN_SDK_LEVEL','BuildConfig.TARGET_SDK_LEVEL']:
        require(field in main,'generated Info/build version evidence missing: '+field)
    for stale in ['LibraryVersionInfo("AndroidX Core KTX", "1.19.0"','LibraryVersionInfo("Compose UI", "1.12.0"','CapabilityKeyValue("Android Gradle Plugin", "9.4.0")','CapabilityKeyValue("Android NDK", "29.0.14206865")']:
        require(stale not in main,'stale duplicated UI version string returned: '+stale)
    for phrase in ['Normative API baseline", "OpenGL ES 3.2 · GLSL ES 3.20 (spec revision 8) · EGL 1.5','Khronos OpenGL registry SHA-256','Khronos EGL registry SHA-256','Registry audit date']:
        require(phrase in main,'Khronos baseline/hash detail missing: '+phrase)
    # 2.1.1 full evidence/Analysis contract remains mandatory.
    expected=['Compare','Search','Diagnostics','Requirements','Minimums','Graph','Presentation','Raw JSON','Database','History','Watched','Quality','Share','Tests']
    for label in expected: require(f'"{label}"' in main,'Analysis tab missing: '+label)
    require('System↔Turnip A/B and Vulkan Profiles are N/A' in main,'Vulkan-only Analysis boundary missing')
    require(re.search(r'ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT\s*=\s*250\b',main) is not None,'Analysis visible bound drift')
    require('QUERY_DEPENDENCIES.keys.sorted().forEach' in main,'Features must derive extension rows from complete query-gate catalog')
    require('This screen is exhaustive for OpenGLESScope\'s query-gated feature catalog' in main,'Features scope disclosure missing')
    require('Unavailable GL runtime attributes' in main,'GL runtime unavailable attributes are not surfaced')
    require('diagnosticByName["${p.shader}/${p.type}"]' in main,'Precision query evidence missing')
    require('CapabilityKeyValue("Query evidence", diagnostic?.status ?: "Unknown")' in main,'limit/precision diagnostic evidence missing')
    # Existing architecture/safety/performance retained.
    require('private const val COLLECTION_PAGE_SIZE = 25' in main,'paging contract drift')
    require('FileManagerOptionsChooser(' in main and 'FileManagerOptionsDialog(' not in main,'File Manager parity drift')
    require('CompactNavigationRail' not in main,'legacy navigation rail returned')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
    require('collectionElapsedMs = ((System.nanoTime() - collectionStartedNanos) / 1_000_000L)' in main,'collection timing evidence missing')
    for phrase in ['Per-query timings are not fabricated','do not prove runtime support','not a conformance','performance benchmark']:
        require(phrase.lower() in main.lower(),'evidence boundary/detail missing: '+phrase)

if __name__=='__main__': main_guard('verify_2_1_2_quality',verify)
