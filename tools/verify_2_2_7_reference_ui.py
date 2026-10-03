#!/usr/bin/env python3
from __future__ import annotations
import hashlib,re,xml.etree.ElementTree as ET
from gate_common import ROOT,require,main_guard

MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def block(s,start,end):
    a=s.find(start);require(a>=0,'missing UI definition: '+start)
    b=s.find(end,a+len(start));require(b>a,'missing end boundary: '+end)
    return s[a:b]
def verify():
    s=(ROOT/MAIN).read_text(encoding='utf-8');g=(ROOT/'app/build.gradle.kts').read_text();r=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require('releaseVersionName = "2.2.14"' in g and 'releaseVersionCode = 2214' in g,'wrong actual app producer identity')
    require('## Release 2.2.7 official artwork, reference evidence menu, overlay and full-screen File Manager audit' in r,'new release contract missing')
    egl=block(s,'private fun EglBrandArtwork(', '\n@Composable\nprivate fun SemanticArtwork(')
    for t in ('Image(','painterResource(R.drawable.ic_egl_official)','ContentScale.Fit','ColorFilter.tint(tint)','modifier = modifier'):
        require(t in egl,'missing actual, theme-tinted official EGL artwork: '+t)
    golden_egl='945b590aebc7f48abb6a9d6cad16fe1e479568ec16d3ea8519f92f8698024b3e'
    require(hashlib.sha256((ROOT/'app/src/main/res/drawable-nodpi/ic_egl_official.png').read_bytes()).hexdigest()==golden_egl,'official EGL logo mutated')
    require('icon == R.drawable.ic_egl_official -> EglBrandArtwork(modifier, contentDescription)' in s and 'sectionIcon == R.drawable.ic_egl_official ->' in s,'EGL routing is still typographic or hard-red')
    android=(ROOT/'app/src/main/res/drawable/ic_android_brand.xml');require(android.is_file(),'new Android brand artwork missing')
    ET.parse(android);xml=android.read_text()
    require('android:fillColor="#F06BC7"' in xml and 'android:fillColor="#351719"' in xml,'Android artwork not exactly theme-toned while preserving eyes')
    require('R.drawable.ic_android_brand' in s and 'R.drawable.ic_android\n' not in s,'old off-palette Android asset still used')
    extension=block(s,'private fun ExtensionsPage(','\n@Composable\nprivate fun PrecisionPage(')
    if 'releaseVersionName = "2.2.14"' in g:
        for t in ('CapabilityKeyValue(ext, "ENUMERATED · $scopeName")','ChevronAffordance("Details"','ExpressiveDetailDialog(ext','CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Dedicated query handler"','Unknown · query evidence unavailable','extensionRegistryUrl(ext)','"Open Khronos specification"'):
            require(t in extension,'2.2.12 reference details evidence missing: '+t)
        require('TransientActionButton("Copy name + value"' not in extension and 'ExpressiveActionButton("Share evidence"' not in extension,'reference has no duplicate action cards')
    else:
        for t in ('CapabilityKeyValue(ext, "ENUMERATED · $scopeName")','ChevronAffordance("Details"','ExpressiveDetailDialog(ext','DetailEvidenceRow("Runtime evidence"','DetailEvidenceRow("Implemented query gates"','Unknown · query evidence unavailable','TransientActionButton("Copy name + value"','ExpressiveActionButton("Share evidence"','TransientActionButton("Add to watched evidence"','ExpressiveActionButton("Open in Encyclopedia"','extensionRegistryUrl(ext)'):
            require(t in extension,'historical 2.2.7 menu missing: '+t)
    require('TransientActionButton("Copy extension"' not in extension,'stale inconsistent Details button returned')
    watched=block(s,'            "Watched" -> {','            "Share" -> {')
    require('Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { TransientActionButton("Add to watch list"' in watched,'watched action does not have bounded vertical width')
    require('Row(Modifier.horizontalScroll(rememberScrollState())' not in watched,'unbounded row returned in watched action group')
    fm=block(s,'private fun SharedStorageBrowserDialog(','\n@Composable\nprivate fun SoftScrollIntersectionShadows(')
    for t in ('DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false','Dialog(','BackHandler(enabled = !busy)','Box(Modifier.fillMaxSize().background(ComposeColor.Black).zIndex(50f))','AnimatedVisibility(','enter = fadeIn(tween(180)) + slideInVertically(tween(220','exit = fadeOut(tween(160)) + slideOutVertically(tween(210','shape = RoundedCornerShape(0.dp)','screenVisible = false','delay(220L)','BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp))','maxWidth > maxHeight && maxWidth >= 700.dp','.weight(0.4f)','.weight(0.6f)','desktopVerticalPointerScroll(controlsScrollState)','dpadScrollableNavigation(controlsScrollState)','SharedStorageBrowserListing(','pendingOverwrite?.let','validatedSharedStorageDestination(','scanSharedStorageDirectory(scanRoot, target, request.allowedExtensions' ,'liveRegion = LiveRegionMode.Polite'):
        require(t in fm,'file manager reference parity missing: '+t)
    require(fm.index('BackHandler(enabled = !busy)')>fm.index('Dialog(\n'),'Dialog back handler outside window: Android TV regression')
    require('onDismissRequest = { }' not in fm,'full-screen browser dismissal must not silently eat back')
    require('max = 360.dp' in block(s,'private fun FileManagerOptionsChooser(','\n@Composable\nprivate fun ExpressiveSearchField('),'File Manager mode selector grows unbounded')
    listings=block(s,'private fun SharedStorageBrowserListing(','\n@Composable\nprivate fun SharedStorageFolderRow(')
    for t in ('AnimatedContent(','sharedStorageDirectoryTransition','LazyVerticalGrid(','LazyColumn(','FileManagerViewMode.DENSE_GRID -> 92.dp','FileManagerViewMode.LARGE_TILES -> 228.dp','else -> 156.dp','SoftScrollIntersectionShadows(showTopFade, showBottomFade','ExpressiveScrollHints(gridState','ExpressiveScrollHints(listState','SharedStorageFolderGridCard(','SharedStorageFileGridCard(','focusGroup()'):
        require(t in listings,'File Manager real transition/modes missing: '+t)
    for name in ['expandHorizontally','shrinkHorizontally','slideInVertically','slideOutVertically']:
        require('import androidx.compose.animation.'+name in s,'new imported Compose animation missing: '+name)
    require('private fun SoftScrollIntersectionShadows(' in s and 'Brush.verticalGradient(' in s,'file manager fades missing')
    require('bottom = bottomNavigationContentInset + 4.dp' in s and 'onSizeChanged { bottomNavigationHeightPx = it.height }' in s,'lazy bottom arrow is not above measured navigation bar')
    require('bottom = bottomNavigationInset + 4.dp' in s and 'LocalBottomNavigationContentInset provides bottomNavigationContentInset' in s,'OpenGL section arrow not coordinated with measured navigation')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'GL report schema changed')
    res={p.stem for p in (ROOT/'app/src/main/res').rglob('*') if p.is_file() and p.parent.name.startswith(('drawable','mipmap'))}
    missing=sorted(set(re.findall(r'R\.drawable\.([A-Za-z_]\w*)',s))-res)
    require(not missing,'undefined Android R.drawable references: '+str(missing))
if __name__=='__main__':main_guard('verify_2_2_7_reference_ui',verify)
