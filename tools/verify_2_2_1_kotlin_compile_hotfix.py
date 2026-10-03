#!/usr/bin/env python3
from __future__ import annotations
import hashlib,re
from pathlib import Path
from gate_common import ROOT,require,main_guard
GL_SHA='b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3'
EGL_SHA='3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8'
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()
def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require(any(f'releaseVersionName = "{n}"' in gradle and f'releaseVersionCode = {c}' in gradle for n,c in [('2.2.1',2201),('2.2.2',2202),('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'2.2.1+ version identity drift')
    require('## Release 2.2.1 Kotlin/Compose parity compile-correctness hotfix' in rules,'2.2.1 rules contract missing')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA and sha(ROOT/'registry/egl.xml')==EGL_SHA,'locked Khronos registry drift')
    require('import androidx.compose.ui.graphics.layer.drawLayer' in main,'canonical drawLayer import missing')
    require('import androidx.compose.ui.graphics.drawscope.drawLayer' not in main,'invalid drawscope.drawLayer import returned')
    require('import androidx.compose.animation.core.animateDpAsState' in main,'animateDpAsState import missing')
    require('import androidx.compose.foundation.layout.FlowRow' in main,'FlowRow import missing')
    require('import androidx.compose.foundation.ScrollState' in main,'ScrollState import missing')
    require('private fun ExpressiveScrollHints(scrollState: ScrollState' in main,'ScrollState ExpressiveScrollHints overload missing')
    require('scrollState.value > 0' in main and 'scrollState.value < scrollState.maxValue' in main,'ScrollState boundary evidence missing')
    require('private fun ExpressiveToggleRow(' in main,'ExpressiveToggleRow helper missing')
    require('private fun ExpressiveMetric(' in main,'ExpressiveMetric helper missing')
    require('ExpressiveSwitch(checked = checked, onCheckedChange = onCheckedChange)' in main,'ExpressiveToggleRow switch semantics drift')
    require('modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))' in main,'ExpressiveMetric animation/theme parity drift')
    # Exact compiler call sites remain resolved by those definitions/imports.
    for token in ['drawLayer(sourceLayer)','drawLayer(backdropLayer)','animateDpAsState(targetValue = topOverlayInset','ExpressiveScrollHints(\n            scrollState','ExpressiveToggleRow("Show unchanged"','FlowRow(horizontalArrangement','ExpressiveMetric("Differences"']:
        require(token in main,'reported compiler call site drift: '+token)
    # Previous compile hotfixes and 2.2.0 parity must remain intact.
    require('import androidx.compose.foundation.layout.matchParentSize' not in main,'invalid matchParentSize import returned')
    require('import androidx.compose.animation.animateContentSize' in main,'animateContentSize import missing')
    require('import androidx.compose.animation.core.FastOutSlowInEasing' in main,'FastOutSlowInEasing import missing')
    for branch in ['Requirements','Minimums','Graph','Presentation','Raw JSON','Database','History','Watched','Quality','Share','Tests']:
        require(f'"{branch}" ->' in main,'Analysis implementation branch missing: '+branch)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
if __name__=='__main__': main_guard('verify_2_2_1_kotlin_compile_hotfix',verify)
