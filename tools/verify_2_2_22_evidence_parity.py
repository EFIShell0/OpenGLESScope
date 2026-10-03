#!/usr/bin/env python3
"""2.2.22 immutable source, VulkanScope row geometry/hold and GL/EGL evidence integrity."""
from __future__ import annotations
import hashlib,json,re
from gate_common import ROOT, require, main_guard
from verify_2_2_21_gpu_navigation import compiled_vendor_oracle
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
LOCK='tests/golden/2_2_22_release_regression_contract.json'
def sha(path): return hashlib.sha256((ROOT/path).read_bytes()).hexdigest()
def section(s,a,b): return s[s.index(a):s.index(b,s.index(a)+len(a))]
def source_oracle(main: str, gradle: str):
    require('releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle,'wrong release identity')
    require(main.count('private val LocalDetailKeyValuePresentation = staticCompositionLocalOf { false }')==1,'detail-presentation switch absent/duplicated')
    dialog=section(main,'private fun ExpressiveDetailDialog(', '@Composable\nprivate fun DetailEvidenceRow(') if False else section(main,'private fun ExpressiveDetailDialog(', '@Composable\nprivate fun ExpressiveEvidenceRow(')
    require('CompositionLocalProvider(LocalDetailKeyValuePresentation provides true) { content() }' in dialog,'detail dialog did not provide column presentation')
    rows=section(main,'private fun ExpressiveEvidenceRow(', '@Composable\nprivate fun EvidenceCopyDialog(')
    require('private fun DetailEvidenceRow(key: String, value: String) = ExpressiveEvidenceRow(key, value)' in rows,'detail evidence no longer shares contained row')
    for token in [
        'val expandedTextLayout = preferExpandedTextLayout()', 'val detailPresentation = LocalDetailKeyValuePresentation.current',
        'expandedTextLayout || maxWidth < 420.dp || key.length > 22 || value.length > 30',
        'expandedTextLayout || maxWidth < 360.dp || key.length > 26 || value.length > 34',
        'val shape: Shape = if (detailPresentation) MaterialTheme.shapes.medium else RoundedCornerShape(16.dp)',
        'color = ComposeColor(0xFF211E1F)',
        'border = if (detailPresentation) androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant) else null',
        'Modifier.fillMaxWidth().then(tvBrowseModifier(shape)).semantics(mergeDescendants = true) { }',
        'if (detailPresentation) 13.dp else 14.dp', 'if (detailPresentation) 10.dp else 11.dp',
        'horizontalArrangement = Arrangement.spacedBy(14.dp)',
        'modifier = Modifier.weight(0.88f)', 'modifier = Modifier.weight(1.12f)',
        'MaterialTheme.typography.labelSmall', 'MaterialTheme.typography.labelMedium',
        'MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall',
        'trademarkApiDisplayText(key)', 'trademarkApiDisplayText(value.ifBlank { "Unavailable" })',
    ]: require(token in rows,'reference row property missing: '+token)
    require('private fun CapabilityKeyValueStatic(key: String, value: String) = ExpressiveEvidenceRow(key, value)' in main,'public rows reverted to uncontained/duplicated layout')
    press=section(main,'private fun CapabilityKeyValue(key: String, value: String)', '@Composable\nprivate fun CapabilityStatusBadge(')
    for token in [
        'val pressScale by animateFloatAsState(if (pressed) 0.985f else 1f, tween(110)',
        'val pressHighlightAlpha by animateFloatAsState(if (pressed) 0.58f else 0f, tween(110)',
        'val pressBorderAlpha by animateFloatAsState(if (pressed) 0.46f else 0f, tween(140, easing = FastOutSlowInEasing)',
        '.graphicsLayer(scaleX = pressScale, scaleY = pressScale)',
        '.background(BrandContainer.copy(alpha = pressHighlightAlpha), RoundedCornerShape(14.dp))',
        '.border(1.dp, BrandSoft.copy(alpha = pressBorderAlpha), RoundedCornerShape(14.dp))',
        'delay(550L)', 'onLongPress = { showActions = true }',
        '.consumeDesktopSecondaryMouseInput(suppressDesktopQuickMenu)',
        'showQuickMenu = true', 'CustomAccessibilityAction("Evidence actions")',
        'if (showActions) EvidenceCopyDialog(key, value, onDismiss = { showActions = false })',
    ]: require(token in press,'reference long-press/TV/desktop/accent effect missing: '+token)
    require(press.count('DropdownMenuItem(')==5,'quick evidence menu count changed')
    for action in ['Copy name + value','Share evidence','Add to watched evidence','Open in Encyclopedia','More details']:
        require(f'DropdownMenuItem(text = {{ Text("{action}") }}' in press,'quick-menu action missing: '+action)
    require('CapabilityKeyValueStatic(key, value)' in press,'long press no longer wraps the common contained surface')
    hero=section(main,'private fun HeroCard(', 'private val gpuVendorArtworkRules')
    require('Surface(color = ComposeColor(0xFF181516), shape = MaterialTheme.shapes.extraLargeIncreased' in hero,'GPU hero surface drift')
    require('Text("System Driver"' in hero, 'System Driver label drift')
    require('CapabilityKeyValue("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })' in hero,'EGL_VENDOR not independently queried or missing')
    require('no vendor/device ID or physical GPU model is inferred' in hero,'manufacturer provenance boundary missing')
    require('CapabilityKeyValue("Vendor ID"' not in hero,'numeric vendor ID falsely invented')
    nav=section(main,'private fun selectedNavigationPage(', 'private fun pageIcon(')
    require(re.findall(r'NavigationItem\(Page\.(\w+),\s*"[^"]+",\s*R\.drawable\.\w+\)',nav)==['Overview','OpenGLES','Display','Extensions'],'navigation/order drift')
    mapping=section(main,'private val gpuVendorArtworkRules', 'private fun QuickAccessCard(')
    require('declared = vendor.trim().lowercase(java.util.Locale.ROOT)' in mapping,'GPU identity artwork no longer GL_VENDOR-evidence based')
    return mapping

def verify():
    lock=json.loads((ROOT/LOCK).read_text())
    require(lock['release']=='2.2.22' and lock['predecessor']=='2.2.21','predecessor identity drift')
    require(len(lock['predecessorHashes'])==160 and set(lock['productionAllowlist'])=={MAIN,GRADLE}, 'immutable production census drift')
    release306Delta = {
        'app/src/main/cpp/openglesscope.cpp',
        'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt',
        'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt',
    }
    for path,digest in lock['predecessorHashes'].items():
        if path not in lock['productionAllowlist'] and path not in release306Delta:
            require(sha(path)==digest,'production drift outside historically allowed or 3.0.7 verified delta: '+path)
    require((ROOT/'tools/verify_3_0_6_emulator_probe.py').is_file(),'3.0.7 release-delta regression test missing')
    reg=json.loads((ROOT/'registry/registry_lock.json').read_text())
    require(sha('registry/gl.xml')==reg['registrySha256'] and sha('registry/egl.xml')==reg['eglRegistrySha256'],'registry SHA drift')
    mapping=source_oracle((ROOT/MAIN).read_text(),(ROOT/GRADLE).read_text())
    compiled_vendor_oracle(mapping)
    print('verify_2_2_22_evidence_parity: PASS (160 immutable sources; reference column/hold/TV/focus; independently queried EGL_VENDOR; Kotlin-compiled vendor attribution)')
if __name__=='__main__': main_guard('verify_2_2_22_evidence_parity',verify)
