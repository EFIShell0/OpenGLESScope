#!/usr/bin/env python3
"""Every regressed surface, tooltip/press/column and provenance constraint must fail release oracle."""
from __future__ import annotations
from gate_common import ROOT, require, main_guard
from verify_2_2_22_evidence_parity import source_oracle
from pathlib import Path
MAIN=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
GRADLE=(ROOT/'app/build.gradle.kts').read_text()

def verify():
    source_oracle(MAIN,GRADLE)
    edits=[
        ('wrong producer',None,('releaseVersionCode = 3007','releaseVersionCode = 2999')),
        ('remove local dialog switch',('private val LocalDetailKeyValuePresentation = staticCompositionLocalOf { false }','private val LocalDetailKeyValuePresentation = staticCompositionLocalOf { true }'),None),
        ('remove dialog provider',('LocalDetailKeyValuePresentation provides true','LocalDetailKeyValuePresentation provides false'),None),
        ('remove neutral row surface',('color = ComposeColor(0xFF211E1F)','color = SurfaceTonal'),None),
        ('remove TV focus',('Modifier.fillMaxWidth().then(tvBrowseModifier(shape)).semantics','Modifier.fillMaxWidth().semantics'),None),
        ('break normal columns',('expandedTextLayout || maxWidth < 360.dp || key.length > 26 || value.length > 34','expandedTextLayout || maxWidth < 280.dp || key.length > 26 || value.length > 34'),None),
        ('break detail columns',('expandedTextLayout || maxWidth < 420.dp || key.length > 22 || value.length > 30','expandedTextLayout || maxWidth < 380.dp || key.length > 22 || value.length > 30'),None),
        ('break key weight',('Modifier.weight(0.88f)','Modifier.weight(0.50f)'),None),
        ('break value weight',('Modifier.weight(1.12f)','Modifier.weight(1.50f)'),None),
        ('remove detail border',('border = if (detailPresentation) androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant) else null','border = null'),None),
        ('discard shared capability row',('private fun CapabilityKeyValueStatic(key: String, value: String) = ExpressiveEvidenceRow(key, value)','private fun CapabilityKeyValueStatic(key: String, value: String) {}'),None),
        ('discard shared detail row',('private fun DetailEvidenceRow(key: String, value: String) = ExpressiveEvidenceRow(key, value)','private fun DetailEvidenceRow(key: String, value: String) {}'),None),
        ('remove press scale',('if (pressed) 0.985f else 1f','if (pressed) 1f else 1f'),None),
        ('remove pressed highlight',('if (pressed) 0.58f else 0f','if (pressed) 0f else 0f'),None),
        ('remove pressed border',('if (pressed) 0.46f else 0f','if (pressed) 0f else 0f'),None),
        ('remove TV hold',('delay(550L)','delay(0L)'),None),
        ('disable touch longpress',('onLongPress = { showActions = true }','onLongPress = { }'),None),
        ('remove secondary mouse policy',('.consumeDesktopSecondaryMouseInput(suppressDesktopQuickMenu)','.consumeDesktopSecondaryMouseInput(false)'),None),
        ('remove context action',('DropdownMenuItem(text = { Text("More details") }','DropdownMenuItem(text = { Text("Other") }'),None),
        ('wrong EGL data source',('CapabilityKeyValue("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })','CapabilityKeyValue("EGL_VENDOR", report.vendor.ifBlank { "Unavailable" })'),None),
        ('invent vendor ID',('Text("System Driver"','CapabilityKeyValue("Vendor ID", "0x0000")\n                    Text("System Driver"'),None),
        ('disable GPU evidence mapping',('declared = vendor.trim().lowercase(java.util.Locale.ROOT)','declared = renderer.trim().lowercase(java.util.Locale.ROOT)'),None),
        ('wrong navigation tab',('NavigationItem(Page.Display, "Display", R.drawable.ic_display)','NavigationItem(Page.EGL, "EGL", R.drawable.ic_egl)'),None),
    ]
    for name,main_edit,gradle_edit in edits:
        m=MAIN;g=GRADLE
        if main_edit:
            a,b=main_edit;require(a in m, 'inactive negative mutation: '+name);m=m.replace(a,b,1)
        if gradle_edit:
            a,b=gradle_edit;require(a in g, 'inactive version mutation: '+name);g=g.replace(a,b,1)
        try: source_oracle(m,g)
        except (AssertionError,ValueError): pass
        else: raise AssertionError('negative mutation survived oracle: '+name)
    print(f'test_2_2_22_negative_mutations: PASS ({len(edits)} independent regressions rejected)')
if __name__=='__main__': main_guard('test_2_2_22_negative_mutations',verify)
