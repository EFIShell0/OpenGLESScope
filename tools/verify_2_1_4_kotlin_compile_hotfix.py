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
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require('releaseVersionName = "2.1.4"' in gradle and 'releaseVersionCode = 2104' in gradle,'2.1.4/2104 identity drift')
    require(rules.count('# OpenGLESScope Engineering Rules')==1,'rules root must remain singular')
    require('## Release 2.1.4 Kotlin/Compose compile-correctness hotfix' in rules,'2.1.4 rules contract missing')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'VulkanScope 3.0.12 methodology reference drift')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA and sha(ROOT/'registry/egl.xml')==EGL_SHA,'locked Khronos registry drift')

    # Exact Kotlin/Compose failures from the real assembleRelease evidence.
    require('import androidx.compose.foundation.layout.matchParentSize' not in main,'invalid matchParentSize top-level import returned')
    require('Modifier.matchParentSize()' in main,'BoxScope matchParentSize usage unexpectedly removed')
    require('import androidx.compose.animation.animateContentSize' in main,'animateContentSize canonical import missing')
    require('import androidx.compose.animation.core.FastOutSlowInEasing' in main,'FastOutSlowInEasing canonical import missing')
    require('internal data class FeatureEvidenceRow' in main and 'internal enum class EvidenceState' in main,'internal evidence-model visibility mismatch')
    require('private enum class EvidenceState' not in main,'internal FeatureEvidenceRow exposes private EvidenceState')

    analysis_start=main.index('private fun AnalysisPage(')
    analysis_end=main.index('\n@Composable\n',analysis_start+1) if '\n@Composable\n' in main[analysis_start+1:] else len(main)
    analysis=main[analysis_start:analysis_end]
    memo=analysis.find('val graphRegistryReferences = remember(context, graphSelectedRoot)')
    lazy=analysis.find('OpenGLESScopeLazyPage(verticalSpacing = 10.dp)')
    graph=analysis.find('"Graph" -> {')
    presentation=analysis.find('"Presentation" -> {',graph)
    require(memo>=0 and lazy>=0 and memo<lazy,'Graph registry remember must execute in AnalysisPage composable scope before LazyListScope DSL')
    require(graph>=0 and presentation>graph,'Analysis Graph branch missing')
    require('remember(' not in analysis[graph:presentation],'Composable remember invoked directly from LazyListScope Graph branch')
    require('val graphRuntimeExtensions = remember(' in analysis and 'val graphDiagnosticByName = remember(' in analysis,'Graph derived state memoization missing')

    # 2.1.3 NDK r30 compile hotfix must remain intact.
    require('const bool extensionApplies[] = {hasRecordableConfigAttr, hasFramebufferTargetConfigAttr, hasFloatComponentsConfigAttr};' in native,'EGLConfig extension applicability mapping drift')
    for stale in ['hasRecordable','hasFramebufferTarget','hasFloatComponents']:
        require(re.search(r'\b'+re.escape(stale)+r'\b',native) is None,'stale undeclared native identifier returned: '+stale)

    # Current toolchain/spec/report/UI contracts are unchanged.
    for phrase in ['compileSdk = 37','minSdk = 31','targetSdk = 37','ndkVersion = "30.0.16248370"','androidx.compose.ui:ui:1.12.1','androidx.compose.foundation:foundation:1.12.1','androidx.compose.animation:animation:1.12.1','androidx.compose.material3:material3:1.5.0-alpha28']:
        require(phrase in gradle,'2.1.4 build/dependency drift: '+phrase)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
    expected=['Compare','Search','Diagnostics','Requirements','Minimums','Graph','Presentation','Raw JSON','Database','History','Watched','Quality','Share','Tests']
    for label in expected: require(f'"{label}"' in main,'Analysis tab missing: '+label)
    require('QUERY_DEPENDENCIES.keys.sorted().forEach' in main,'Features must remain query-gate driven')
    require('FileManagerOptionsChooser(' in main and 'FileManagerOptionsDialog(' not in main,'File Manager parity drift')

if __name__=='__main__': main_guard('verify_2_1_4_kotlin_compile_hotfix',verify)
