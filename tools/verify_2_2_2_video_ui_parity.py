#!/usr/bin/env python3
from __future__ import annotations
from gate_common import ROOT,require,main_guard

def between(source,start,end='\n@Composable\n'):
    i=source.find(start);require(i>=0,'missing composable: '+start)
    j=source.find(end,i+len(start));return source[i:j if j>=0 else len(source)]

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require(any(f'releaseVersionName = "{version}"' in gradle and f'releaseVersionCode = {code}' in gradle for version,code in [('2.2.2',2202),('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'version drift')
    require('## Release 2.2.2 VulkanScope video-detail parity audit' in rules,'release rules absent')
    sheet=between(main,'private fun ExpressiveDetailDialog(')
    for token in ['DialogProperties(usePlatformDefaultWidth = false)','maxOf(280.dp','minOf(720.dp','landscape','BorderStroke(1.dp, BrandSoft.copy(alpha = 0.46f))','Modifier.fillMaxWidth().weight(1f)','desktopVerticalPointerScroll(scrollState)','dpadScrollableNavigation(scrollState)','verticalScroll(scrollState)','focusable().focusGroup()','ExpressiveScrollHints(scrollState','HorizontalDivider(color = OutlineVariant)','ExpressiveContainedIconTextButton("Close"']:
        require(token in sheet,'bounded accessible video-detail shell drift: '+token)
    rows=between(main,'private fun DetailEvidenceRow(')
    for token in ['BoxWithConstraints','value.length > 30','value.contains("\\n")','FontFamily.Monospace','mergeDescendants = true','weight(1.12f)']:
        require(token in rows,'long evidence text/semantics drift: '+token)
    ev=between(main,'private fun EvidenceCopyDialog(')
    for token in ['ExpressiveDetailDialog("Evidence provenance", onDismiss)','DetailEvidenceRow("Evidence key"','DetailEvidenceRow("Value"','DetailEvidenceRow("Evidence class"','DetailEvidenceRow("Source"','DetailEvidenceRow("Query/API path"','DetailEvidenceRow("Query group"','DetailEvidenceRow("Registry relationship"','TransientActionButton("Copy name + value"','ExpressiveActionButton("Share evidence"','TransientActionButton("Add to watched evidence"','ExpressiveActionButton("Open in Encyclopedia"','referenceToken.isNotBlank() && environment != null']:
        require(token in ev,'VulkanScope-style evidence detail regression: '+token)
    require('AlertDialog(' not in ev,'old compressed evidence AlertDialog returned')
    hero=between(main,'private fun HeroCard(')
    for token in ['showImplementationDetail','ExpressiveDetailDialog("Graphics implementation"','DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })','DetailEvidenceRow("GL_VENDOR", report.vendor.ifBlank { "Unavailable" })','DetailEvidenceRow("GL_VERSION", report.glVersion.ifBlank { "Unavailable" })','DetailEvidenceRow("GL_SHADING_LANGUAGE_VERSION", report.glslVersion.ifBlank { "Unavailable" })','DetailEvidenceRow("Initialized EGL version", report.egl.initializedVersion.ifBlank { "Unavailable" })','ChevronAffordance("Details"','A marketing GPU name, listed extension or registry token does not establish feature support']:
        require(token in hero,'runtime-derived overview implementation detail drift: '+token)
    ext=between(main,'private fun ExtensionsPage(')
    for token in ['COLLECTION_PAGE_SIZE','stickyCollectionPager(filtered.size','CapabilityKeyValue(ext, "ENUMERATED · $scopeName")','ChevronAffordance("Details"','ExpressiveDetailDialog(ext','Unknown · query evidence unavailable','extensionRegistryUrl(ext)','Open Khronos specification']:
        require(token in ext,'truthful extension detail UI regression: '+token)
    if 'releaseVersionName = "2.2.14"' in gradle:
        for token in ['CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Dedicated query handler"','"Open Khronos specification"','Unknown · query evidence unavailable']:
            require(token in ext,'2.2.12 VulkanScope reference detail regression: '+token)
        details=ext[ext.index('ExpressiveDetailDialog(ext'):]
        require('TransientActionButton("Copy name + value"' not in details and 'ExpressiveActionButton("Share evidence"' not in details,'superseded duplicate Details actions returned')
    elif any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.7','2.2.8','2.2.9','2.2.10','2.2.11')):
        for token in ['DetailEvidenceRow("Runtime evidence"','DetailEvidenceRow("Implemented query gates"','TransientActionButton("Copy name + value"','ExpressiveActionButton("Share evidence"','TransientActionButton("Add to watched evidence"','ExpressiveActionButton("Open in Encyclopedia"']:
            require(token in ext,'historic reference detail menu missing: '+token)
    else:
        for token in ['CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Implemented query gates"','TransientActionButton("Copy extension"']:
            require(token in ext,'historic video extension menu regression: '+token)
    fm=between(main,'private fun FormatsPage(')
    require('ExpressiveDetailDialog(' in fm and 'DetailEvidenceRow(' in fm,'formats detail must share the scrollable shell')
    for token in ['private fun CompactBottomNavigationBar(','private fun FileManagerBreadcrumbBar(','"Raw JSON" ->','"Watched" ->','"History" ->']:
        require(token in main,'existing VulkanScope common UI branch lost: '+token)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'API report schema drift')

if __name__=='__main__':main_guard('verify_2_2_2_video_ui_parity',verify)
