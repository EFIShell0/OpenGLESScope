#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, re
from pathlib import Path
from gate_common import ROOT, require, main_guard

VULKAN_RULES_SHA='9c89153fb34352f567ccdec7b3685ac1feab56c45b46b22f320375dfed66e7ca'
GL_SHA='b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3'
EGL_SHA='3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8'

def sha(p: Path) -> str: return hashlib.sha256(p.read_bytes()).hexdigest()

def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    catalog=(ROOT/'app/src/main/java/com/efishell/openglesscope/RegistryCatalog.kt').read_text(encoding='utf-8')

    require('releaseVersionName = "2.1.0"' in gradle and 'releaseVersionCode = 2100' in gradle,'2.1.0/2100 identity drift')
    require(rules.count('# OpenGLESScope Engineering Rules')==1,'PROJECT_RULES must have exactly one canonical root contract')
    require('## Release 2.1.0 VulkanScope 3.0.12 rules/UI/query/performance parity audit' in rules,'2.1.0 rule section missing')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'exact VulkanScope 3.0.12 rules reference drift')
    require(len([x for x in ref.read_text(encoding='utf-8').splitlines() if x.startswith('## ')])==199,'VulkanScope methodology heading census drift')

    require(sha(ROOT/'registry/gl.xml')==GL_SHA,'gl.xml lock drift')
    require(sha(ROOT/'registry/egl.xml')==EGL_SHA,'egl.xml lock drift')

    # Navigation / Settings / shared chrome.
    require('CompactNavigationRail' not in main,'legacy landscape navigation rail remains')
    require('Page.Info' not in main,'legacy top-level Info page remains')
    require('Overview("Overview"), OpenGLES("OpenGL ES"), Display("Display & HDR"), EGL("EGL")' in main,'page topology drift')
    require('private fun selectedNavigationPage' in main and 'Page.Display, Page.Features, Page.Limits, Page.Formats, Page.Precision, Page.Configs, Page.Encyclopedia, Page.Analysis, Page.Settings -> Page.Overview' in main,'secondary-page primary-nav ownership drift')
    for token in ['INFO("Info"','REPORTS_DATABASE("Reports & Database"','UPDATE_PREFERENCES("Update Preferences"']:
        require(token in main,'Settings information architecture drift: '+token)
    for token in ['private fun HeaderActionButton(', 'private fun AnimatedHeaderActionButton(', 'private fun HeaderTitleBadge(', '.selectable(selected = selected', 'navigationIndicatorScale', 'navigationIconTint', 'navigationTextTint']:
        require(token in main,'shared VulkanScope chrome interaction missing: '+token)

    # Pagination, input and scroll parity.
    require('private const val COLLECTION_PAGE_SIZE = 25' in main,'25-item pager contract drift')
    require(main.count('CollectionPager(')>=7,'shared pager not used broadly enough')
    for token in ['val compactMaxWidth = if (landscape) 340.dp else 310.dp','val compactHeight = if (landscape) 46.dp else 54.dp','val compactIndicatorHeight = if (landscape) 35.dp else 42.dp','val compactIndicatorInset = if (landscape) 6.dp else 8.dp','bottom = 4.dp + bottomSystemInset']:
        require(token in main,'VulkanScope 3.0.12 primary-navigation geometry drift: '+token)
    for token in ['desktopVerticalPointerScroll(listState)','tvRemoteLazyListNavigation(listState)','tvRemoteLazyGridNavigation(gridState)','dpadScrollableNavigation(scrollState)','ExpressiveScrollHints(gridState']:
        require(token in main,'desktop/TV scroll parity missing: '+token)
    for token in ['val textOverflows =', 'val showLeadingFade = focused && textOverflows', 'val showTrailingFade = textOverflows', '.padding(start = 44.dp)', '.padding(end = 43.dp)']:
        require(token in main,'search-field overflow/fade parity missing: '+token)

    # File manager parity and security-oriented UX.
    for token in ['LIST(', 'COMPACT_LIST(', 'DETAILED_LIST(', 'GRID(', 'DENSE_GRID(', 'LARGE_TILES(']: require(token in main,'File Manager six-view mode drift: '+token)
    for token in ['NAME_ASC(', 'NAME_DESC(', 'MODIFIED_NEWEST(', 'MODIFIED_OLDEST(', 'CREATED_NEWEST(', 'CREATED_OLDEST(']: require(token in main,'File Manager six-sort mode drift: '+token)
    require('FileManagerOptionsChooser(' in main and 'FileManagerOptionsDialog(' not in main,'old File Manager options dialog path remains')
    fm=main[main.find('private fun FileManagerOptionsChooser'):main.find('private fun ExpressiveSearchField')]
    require('HeaderActionButton(' in fm and 'Close view and sort menu' in fm,'File Manager chooser close action is not using shared header-action chrome')
    require('FileManagerBreadcrumbBar(' in main,'File Manager breadcrumb parity missing')
    require('shared_view_mode' in main and 'shared_sort_mode' in main,'File Manager preference persistence missing')
    require('validatedSharedStorageDestination' in main and 'isCanonicalSharedStoragePath' in main,'File Manager canonical-path validation missing')

    # Encyclopedia must remain registry-backed, bounded and evidence-aware.
    require(re.search(r'private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT\s*=\s*250\b', main) is not None,'Encyclopedia visible-result bound drift')
    require('RegistryCatalog.load' in main or 'RegistryCatalog.' in main,'registry-backed Encyclopedia missing')
    for phrase in ['Extensions', 'Core versions', 'Commands', 'Tokens', 'Types', 'OpenGL ES', 'EGL', 'Runtime', 'Not enumerated', 'Reference']:
        require(phrase in main,'Encyclopedia filter/evidence surface missing: '+phrase)
    require('COLLECTION_PAGE_SIZE' in main[main.find('private fun RegistryEncyclopediaPage'):main.find('private fun AnalysisPage')],'Encyclopedia does not use shared paging')
    require('MAX_CATALOG_BYTES = 2 * 1024 * 1024' in catalog and 'MAX_CATALOG_ENTRIES = 6_000' in catalog and 'cached:' in catalog,'registry catalog bounds/cache drift')

    # Semantic icon parity: shared common icons are byte-identical to the frozen Vulkan 3.0.12 set.
    golden=json.loads((ROOT/'tests/golden/2_1_0_common_icon_hashes.json').read_text(encoding='utf-8'))
    files=golden.get('files',{})
    require(len(files)>=80,'common semantic icon census unexpectedly small')
    for rel,expected in files.items():
        p=ROOT/'app/src/main/res'/rel
        require(p.is_file(),f'common semantic icon missing: {rel}')
        require(sha(p)==expected,f'common semantic icon drift: {rel}')
    for mapping in ['"Check for updates"','R.drawable.ic_check_updates','"Opening animation"','R.drawable.ic_opening_animation_toggle','R.drawable.ic_self_test','R.drawable.ic_book','R.drawable.ic_evidence']:
        require(mapping in main,'semantic icon mapping missing: '+mapping)

    # Query correctness / fabricated-name / performance invariants.
    require('static void normalizeExtensionList' in native and 'std::sort(extensions.begin(), extensions.end())' in native and 'std::unique(extensions.begin(), extensions.end())' in native,'extension normalization drift')
    require('std::binary_search(extensions.begin(), extensions.end(), std::string(name))' in native,'extension membership regressed from logarithmic lookup')
    require('GL_EXT_fragment_shading_rate_attachment' not in native,'fabricated/unregistered fragment shading rate extension name reintroduced')
    for i in range(1,13): require(f'EGL_SURFACE_COMPRESSION_FIXED_RATE_{i}BPC_EXT' in native,f'canonical EGL compression token missing: {i}BPC')
    loop=native.find('for (EGLint i = 0; i < totalConfigs; ++i)')
    require(loop>0,'EGLConfig loop missing')
    for token in ['const bool hasRecordableConfigAttr','const bool hasFramebufferTargetConfigAttr','const bool hasFloatComponentsConfigAttr']:
        pos=native.find(token); require(0 <= pos < loop,f'EGLConfig invariant extension gate not hoisted: {token}')

    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
    require('assetSizeBytes: Long?' in main and 'Downloaded package size does not match the official GitHub release metadata.' in main,'updater trusted-size validation drift')

if __name__=='__main__': main_guard('verify_2_1_0_quality',verify)
