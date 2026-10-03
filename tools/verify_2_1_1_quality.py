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
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require('releaseVersionName = "2.1.1"' in gradle and 'releaseVersionCode = 2101' in gradle,'2.1.1/2101 identity drift')
    require(rules.count('# OpenGLESScope Engineering Rules')==1,'rules root must remain singular')
    require('## Release 2.1.1 full-report/UI/Analysis evidence parity audit' in rules,'2.1.1 rules contract missing')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'VulkanScope 3.0.12 methodology reference drift')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA and sha(ROOT/'registry/egl.xml')==EGL_SHA,'locked Khronos registry drift')

    # Applicable Analysis parity and explicit N/A boundary.
    expected=['Compare','Search','Diagnostics','Requirements','Minimums','Graph','Presentation','Raw JSON','Database','History','Watched','Quality','Share','Tests']
    for label in expected: require(f'"{label}"' in main,'Analysis tab missing: '+label)
    require('System↔Turnip A/B and Vulkan Profiles are N/A' in main,'Vulkan-only Analysis boundary missing')
    require(re.search(r'ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT\s*=\s*250\b',main) is not None,'Analysis visible bound drift')
    for phrase in ['Global OpenGL ES/EGL report search','Matches", matching.size.toString()','Rendered", "${filtered.size} / ${matching.size}"','additional matching evidence field(s) are intentionally not composed']:
        require(phrase in main,'bounded Search disclosure missing: '+phrase)
    for phrase in ['OpenGL ES runtime diagnostics','Probe and scheduler timing','App-observed collection','Probe timeout budget','Probe result bound','Dedicated :opengles_probe process','Per-query timings are not fabricated','EGL binding diagnostics']:
        require(phrase in main,'Diagnostics parity/detail missing: '+phrase)
    for phrase in ['OpenGL ES 3.2 scalar implementation requirements','Non-scalar language/API rules','missing evidence remains UNKNOWN','Direct numeric comparison only']:
        require(phrase in main,'Requirements evidence semantics missing: '+phrase)
    for phrase in ['Saved minimum profiles','pendingMinimumProfileSave','pendingMinimumProfileLoad','Save minimum profile?','Load minimum profile?','Delete saved minimum profile?']:
        require(phrase in main,'Minimum-profile parity/confirmation missing: '+phrase)
    for phrase in ['Dependency graph explorer','Interactive query-gate map','Registry references','do not prove runtime support']:
        require(phrase in main,'Graph evidence-boundary detail missing: '+phrase)
    for phrase in ['Presentation evidence','Surface config ID','Surface render buffer','Surface swap behavior','Surface GL colorspace','end-to-end guarantee']:
        require(phrase in main,'Presentation detail/provenance missing: '+phrase)
    for phrase in ['Raw structured technicalReport','Rendered','no evidence was truncated','Local session history','analysisHistoryLabel(record)','Private app storage']:
        require(phrase in main,'Raw/History transparency missing: '+phrase)
    for phrase in ['OpenGLESScope Database lookup','ANALYSIS_DATABASE_LIST_LIMIT = 50','ANALYSIS_DATABASE_LIST_MAX = 200','Exact 64-hex report ID','Compare this report','evidence difference only']:
        require(phrase in main,'Database analysis parity missing: '+phrase)
    for phrase in ['Collection integrity score','Scoring method','maximum deduction','What this score does not mean','not a conformance','performance benchmark']:
        require(phrase.lower() in main.lower(),'Quality-score transparency missing: '+phrase)
    for phrase in ['Database permalink & QR','The QR payload is exactly the permalink shown above','OpenGL ES active self-tests','PASS','FAIL','UNAVAILABLE','Result semantics','do not contribute to the Collection integrity score']:
        require(phrase in main,'Share/Test semantics missing: '+phrase)
    require('Displayed", "10 / ${matches.size} matches' in main,'Watched evidence truncation disclosure missing')

    # Full evidence surfaces, no hand-picked Features sample.
    require('QUERY_DEPENDENCIES.keys.sorted().forEach' in main,'Features must derive extension rows from complete query-gate catalog')
    require('This screen is exhaustive for OpenGLESScope\'s query-gated feature catalog' in main,'Features scope disclosure missing')
    require('glEnumerationAvailable' in main and 'eglDisplayEnumerationAvailable && eglClientEnumerationAvailable' in main,'Features conservative enumeration semantics missing')
    require('Unavailable GL runtime attributes' in main,'GL runtime unavailable attributes are not surfaced')
    require('diagnosticByName["${p.shader}/${p.type}"]' in main,'Precision query evidence missing')
    require('CapabilityKeyValue("Query evidence", diagnostic?.status ?: "Unknown")' in main,'limit/precision diagnostic evidence missing')
    require('Page.Precision -> R.drawable.ic_precision' in main and 'Page.Configs -> R.drawable.ic_configs' in main,'Precision/Configs semantic destination icons missing')
    for icon in ['ic_precision.xml','ic_configs.xml']:
        require((ROOT/'app/src/main/res/drawable'/icon).is_file(),'semantic icon resource missing: '+icon)

    # Existing architecture/safety/performance retained.
    require('private const val COLLECTION_PAGE_SIZE = 25' in main,'paging contract drift')
    require('FileManagerOptionsChooser(' in main and 'FileManagerOptionsDialog(' not in main,'File Manager parity drift')
    require('CompactNavigationRail' not in main,'legacy navigation rail returned')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
    require('collectionElapsedMs = ((System.nanoTime() - collectionStartedNanos) / 1_000_000L)' in main,'collection timing evidence missing')

if __name__=='__main__': main_guard('verify_2_1_1_quality',verify)
