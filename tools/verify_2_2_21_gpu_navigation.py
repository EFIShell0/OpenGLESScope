#!/usr/bin/env python3
"""2.2.21 immutable predecessor, GPU provenance, reference nav and EGL artwork rules."""
from __future__ import annotations
import hashlib, json, re, shutil, subprocess, tempfile
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
NATIVE='app/src/main/cpp/openglesscope.cpp'
LOCK='tests/golden/2_2_21_release_regression_contract.json'

def sha(rel): return hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()

def source_oracle(main: str, gradle: str):
    require('releaseVersionName = "2.2.21"' in gradle and 'releaseVersionCode = 2221' in gradle,'version identity drift')
    nav=main[main.index('private fun selectedNavigationPage('):main.index('private fun pageIcon(')]
    require('Page.EGL -> Page.OpenGLES' in nav,'nested EGL must select parent OpenGL ES tab')
    require('Page.Display -> Page.Overview' not in nav,'Display incorrectly selects Overview')
    matches=re.findall(r'NavigationItem\(Page\.(\w+),\s*"[^"]+",\s*R\.drawable\.\w+\)',nav)
    require(matches==['Overview','OpenGLES','Display','Extensions'],'primary destination/order mismatch: '+str(matches))
    require('Page.Display -> 2' in nav and 'Page.EGL -> 1' in nav,'navigation transition order mismatch')
    bar=main[main.index('private fun CompactBottomNavigationBar('):main.index('private fun HeaderActionButton(')]
    require('if (landscape) 340.dp else PrimaryNavigationMaxWidth' in bar and 'PrimaryNavigationMaxWidth = 310.dp' in main,'VulkanScope reference bar dimensions drift')
    require('.weight(1f)' in bar and 'contentAlignment = Alignment.Center' in bar and 'textAlign = TextAlign.Center' in bar,'four centered equal hit areas absent')
    require('16.dp + startSystemInset' in bar and '16.dp + endSystemInset' in bar and 'PrimaryNavigationBottomGap + bottomSystemInset' in bar,'landscape/portrait/RTL/system-inset contract missing')
    require('Page.EGL -> EglBrandArtwork' not in bar and 'NavigationItem(Page.Display, "Display", R.drawable.ic_display)' in nav,'EGL wrongly retained as a primary button')
    hero=main[main.index('private fun HeroCard('):main.index('private val gpuVendorArtworkRules')]
    require('Surface(color = ComposeColor(0xFF181516), shape = MaterialTheme.shapes.extraLargeIncreased' in hero, 'VulkanScope hero card color differs')
    require('Text("System Driver"' in hero and 'Text("System OpenGL' not in hero,'System Driver label drift')
    require('CapabilityKeyValue("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })' in hero, 'numeric vendor ID fabricated or EGL_VENDOR omitted')
    require('CapabilityKeyValue("OpenGL® ES™", shortGlVersion' not in hero,'redundant OpenGL ES row returned')
    require('no vendor/device ID or physical GPU model is inferred' in hero,'GPU identity evidence boundary absent')
    require('DetailEvidenceRow("GL_RENDERER"' in hero and 'DetailEvidenceRow("GL_VENDOR"' in hero,'raw GL renderer/vendor evidence omitted')
    egl=main[main.index('private enum class EglArtworkUse'):main.index('private fun ExpressiveDestinationCard(')]
    require('EglArtworkUse { IDENTITY, CONTEXT, PBUFFER }' in egl,'EGL logo variants missing')
    for token in ['EglBrandArtwork(', 'R.drawable.ic_cpu','R.drawable.ic_surface','Modifier.align(Alignment.BottomEnd)','ContentScale.Fit']:
        require(token in egl,'official EGL role art missing: '+token)
    require('painterResource(R.drawable.ic_egl_official)' in main[main.index('private fun EglBrandArtwork('):main.index('private fun SemanticArtwork(')], 'official EGL source asset missing')
    section=main[main.index('private fun SectionHeaderIcon('):main.index('private fun CapabilitySectionCard(')]
    for role, title in [('IDENTITY','EGL identity'),('CONTEXT','Current EGL binding and context'),('PBUFFER','Collector pbuffer')]:
        require(f'title.equals("{title}", true) -> EglSectionArtwork(EglArtworkUse.{role})' in section,'EGL section branding repeated or wrong: '+title)
    mapping=main[main.index('private val gpuVendorArtworkRules'):main.index('private fun QuickAccessCard(')]
    require('declared = vendor.trim().lowercase(java.util.Locale.ROOT)' in mapping,'GL_VENDOR not the source of branding')
    require('gpuVendorArtworkRules.firstOrNull { (pattern, _) -> pattern.containsMatchIn(declared) }?.second' in mapping,'vendor matching is not evidence-driven')
    require('val layers = listOf("angle", "swiftshader", "mesa", "freedreno", "panfrost"' in mapping,'GL implementation layer neutral artwork missing')
    require('|| listOf("angle", "swiftshader", "mesa", "freedreno", "panfrost"' in mapping, 'renderer software/translation layer guard missing')
    patterns=re.findall(r'Regex\("""(.*?)"""\) to R\.drawable\.gpu_vendor_',mapping)
    require(patterns and all(x.startswith('^') for x in patterns),'vendor labels must match the beginning of the declared GL_VENDOR string')
    require('gpu_vendor_vsi' not in mapping,'unverified VSI art aliases the Vivante bitmap')
    require('val icon = vendorArtworkResource(vendor, renderer)' in mapping and 'painter = painterResource(vendorArtworkResource(vendor, renderer))' in mapping,'hero/public report logo mapping diverges')
    require('contentDescription = artworkDescription' in mapping,'logo accessibility provenance absent')
    require('"adreno" in text' not in mapping and '"mali" in text' not in mapping,'renderer substring is not GL_VENDOR authority')
    return mapping

def compiled_vendor_oracle(mapping: str):
    mapping=mapping[:mapping.index('\n@Composable')]
    compiler=shutil.which('kotlinc')
    require(bool(compiler),'host Kotlin compiler unavailable')
    ids=re.findall(r'R\.drawable\.(gpu_vendor_\w+)',mapping)
    ids=sorted(set(ids))
    stubs='object R { object drawable { '+ ' '.join(f'const val {x} = {i+1};' for i,x in enumerate(ids))+' } }\n'
    fixture=stubs+mapping+'''\nfun main() {
    fun same(v:String,r:String,k:Int) = check(vendorArtworkResource(v,r)==k) { "incorrect branding for $v / $r" }
    same("Qualcomm", "Adreno 740", R.drawable.gpu_vendor_qualcomm)
    same("ARM", "Mali-G78", R.drawable.gpu_vendor_arm)
    same("NVIDIA Corporation", "Tegra", R.drawable.gpu_vendor_nvidia)
    same("Intel", "Intel UHD", R.drawable.gpu_vendor_intel)
    same("Imagination Technologies", "PowerVR", R.drawable.gpu_vendor_imagination)
    same("Mesa", "Adreno 740", R.drawable.gpu_vendor_unknown)
    same("Google Inc. (Qualcomm)", "ANGLE (Qualcomm, Vulkan 1.3.284 (Adreno (TM) 710))", R.drawable.gpu_vendor_qualcomm)
    same("Google Inc.", "ANGLE (SwiftShader Device, Vulkan)", R.drawable.gpu_vendor_unknown)
    same("Google Inc.", "ANGLE (Qualcomm generic Vulkan)", R.drawable.gpu_vendor_unknown)
    same("Qualcomm", "SwiftShader Device", R.drawable.gpu_vendor_unknown)
    same("Unknown", "Adreno 740", R.drawable.gpu_vendor_unknown)
    same("Unavailable", "Mali-G78", R.drawable.gpu_vendor_unknown)
    same("Harman Audio", "Mali-G78", R.drawable.gpu_vendor_unknown)
    same("Adreno", "Qualcomm", R.drawable.gpu_vendor_unknown)
    same("Samsung", "Mali-G78", R.drawable.gpu_vendor_samsung)
    same("Google Inc. (Qualcomm)", "Adreno", R.drawable.gpu_vendor_unknown)
    same("VeriSilicon", "GC7000", R.drawable.gpu_vendor_unknown)
    same("Qualcomm", "Mesa Freedreno Adreno", R.drawable.gpu_vendor_unknown)
}\n'''
    with tempfile.TemporaryDirectory(prefix='ogl221-gpu-') as d:
        f=Path(d)/'Vendor.kt'; jar=Path(d)/'vendor.jar'; f.write_text(fixture)
        cp=subprocess.run([compiler,str(f),'-include-runtime','-d',str(jar)],capture_output=True,text=True,timeout=65)
        require(cp.returncode==0, 'Kotlin vendor matcher failed to compile: '+cp.stderr[:2000])
        cp=subprocess.run(['java','-jar',str(jar)],capture_output=True,text=True,timeout=15)
        require(cp.returncode==0, 'Kotlin GPU attribution fixture failed: '+cp.stderr[:2000])

def verify():
    lock=json.loads((ROOT/LOCK).read_text())
    require(lock['release']=='2.2.21' and lock['predecessor']=='2.2.20','release chain drift')
    require(lock['predecessorZipSha256']=='a2bf1c9469bfd5bf8b6056f04be9908e89cdd8076537e8b0ff0495de985156f9','exact 2.2.20 ZIP drift')
    require(len(lock['predecessorHashes'])==160,'predecessor production census drift')
    allow=set(lock['productionAllowlist']);require(allow=={MAIN,GRADLE},'unreviewed production allowlist')
    for rel,digest in lock['predecessorHashes'].items():
        if rel not in allow:require(sha(rel)==digest, 'unreviewed production drift: '+rel)
    require(sha('registry/gl.xml')==json.loads((ROOT/'registry/registry_lock.json').read_text())['registrySha256'],'GL registry drift')
    require(sha('registry/egl.xml')==json.loads((ROOT/'registry/registry_lock.json').read_text())['eglRegistrySha256'],'EGL registry drift')
    require(sha('app/src/main/res/drawable-nodpi/gpu_vendor_vsi.png')==sha('app/src/main/res/drawable-nodpi/gpu_vendor_vivante.png'), 'VSI asset alias evidence changed; reassess branding')
    require(sha('app/src/main/res/drawable-nodpi/ic_egl_official.png')==lock['predecessorHashes']['app/src/main/res/drawable-nodpi/ic_egl_official.png'],'official EGL PNG mutated')
    main=(ROOT/MAIN).read_text();mapping=source_oracle(main,(ROOT/GRADLE).read_text());compiled_vendor_oracle(mapping)
    print('verify_2_2_21_gpu_navigation: PASS (160-source predecessor; Kotlin-compiled GL_VENDOR attribution; reference nav and official EGL variants)')
if __name__=='__main__': main_guard('verify_2_2_21_gpu_navigation',verify)
