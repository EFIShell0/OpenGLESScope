#!/usr/bin/env python3
from __future__ import annotations
from gate_common import ROOT,require,main_guard

def chunk(s,start,end):
    a=s.index(start);b=s.index(end,a+len(start));return s[a:b]
def verify():
    s=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    gradle=(ROOT/'app/build.gradle.kts').read_text();rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require(any(f'releaseVersionName = "{v}"' in gradle and f'releaseVersionCode = {c}' in gradle for v,c in [('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'wrong 2.2.5+ identity')
    require('## Release 2.2.5 input, File Manager and responsive accessibility parity audit' in rules,'release contract missing')
    wheel=chunk(s,'private fun Modifier.desktopVerticalPointerScroll(state: ScrollableState, wheelScalePx: Float): Modifier =','@Composable\nprivate fun Modifier.tvRemoteLazyListNavigation')
    for t in ('PointerEventType.Scroll','dispatchRawDelta(axis * wheelScalePx)','viewConfiguration.touchSlop','PointerType.Mouse','event.buttons.isPrimaryPressed','if (!event.buttons.isPrimaryPressed) {','if (!event.buttons.isPrimaryPressed || !mouse.pressed) {','accumulatedY','activeId','state.dispatchRawDelta(-overSlop)','state.dispatchRawDelta(-deltaY)','mouse.consume()'):
        require(t in wheel,'VulkanScope desktop pointer gesture drift: '+t)
    focus=chunk(s,'private fun tvBrowseModifier(shape: Shape, enabled: Boolean = true): Modifier','@Composable\nprivate fun ExpressiveIconButton')
    for t in ('BringIntoViewRequester()','requester.bringIntoView()','.onFocusChanged','.focusable(enabled = enabled)','BrandSoft'):
        require(t in focus,'keyboard/TV/desktop focused row regression: '+t)
    require('if (!isTelevision) return Modifier' not in focus,'desktop/keyboard browse focus path disabled')
    browser=chunk(s,'private fun SharedStorageBrowserDialog(','@Composable\nprivate fun SharedStorageFolderRow(')
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        for t in ('DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false','BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp))','maxWidth > maxHeight && maxWidth >= 700.dp','.weight(0.4f)','.weight(0.6f)','desktopVerticalPointerScroll(controlsScrollState)','dpadScrollableNavigation(controlsScrollState)','SharedStorageBrowserListing(','consumeDesktopSecondaryMouseInput(suppressDesktopSecondaryInput)','liveRegion = LiveRegionMode.Polite'):
            require(t in browser,'new full-screen landscape/inset/file manager a11y regression: '+t)
    else:
        for t in ('BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp))','maxWidth > maxHeight && maxWidth >= 700.dp','Modifier.weight(0.42f).fillMaxHeight()','Modifier.weight(0.58f).fillMaxHeight()','desktopVerticalPointerScroll(controlsScrollState)','dpadScrollableNavigation(controlsScrollState)','listingContent(Modifier.weight(1f).fillMaxWidth())','statusBarsPadding().navigationBarsPadding()','consumeDesktopSecondaryMouseInput(isChromeOsRuntime(context)','val listingContent: @Composable (Modifier) -> Unit','liveRegion = LiveRegionMode.Polite'):
            require(t in browser,'historical landscape/inset/file manager a11y regression: '+t)
    fm=chunk(s,'private fun FileManagerOptionsChooser(','@Composable\nprivate fun ExpressiveSearchField(')
    for t in ('val chooserColumns = if (maxWidth < 310.dp || LocalConfiguration.current.fontScale >= 1.65f) 2 else 3','Modifier.widthIn(min = 292.dp, max = 360.dp)','chunked(chooserColumns)','repeat(chooserColumns - rowModes.size)','fileManagerViewTileColor','fileManagerSortRowColor','tvBrowseModifier(tileShape, enabled)','tvBrowseModifier(rowShape, enabled)','contentDescription = null'):
        require(t in fm,'small-window/chooser animation/semantics regression: '+t)
    breadcrumb=chunk(s,'private fun FileManagerBreadcrumbBar(','@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun SharedStorageBrowserDialog(')
    require('LocalLayoutDirection.current' in breadcrumb and 'if (isRtl) "‹" else "›"' in breadcrumb and 'tvBrowseModifier(shape, enabled && !active)' in breadcrumb,'RTL/current crumb focus error')
    arrow=chunk(s,'private fun FileManagerNavigateArrow(','@Composable\nprivate fun SharedStoragePermissionActionButton(')
    require('if (rtl) R.drawable.ic_chevron_left else R.drawable.ic_chevron_right' in arrow,'file RTL navigation icon missing')
    search=chunk(s,'private fun ExpressiveSearchField(','@Composable\nprivate fun rememberFilterScrollBoundaryConnection(')
    require('val rtl = LocalLayoutDirection.current' in search and search.count('if (rtl) listOf(')>=2,'RTL search fade directions not mirrored')
    nav=chunk(s,'private fun CompactBottomNavigationBar(','@Composable\nprivate fun HeaderActionButton(')
    for t in ('accessibilityScale = configuration.fontScale >= 1.3f','compactHeight = if (accessibilityScale)','compactIndicatorHeight = if (accessibilityScale)','compactLabelFontSize = if (accessibilityScale)','backdropRootOffset','frostedChromeBackdrop'):
        require(t in nav,'high font scale / mosaic nav regression: '+t)
    blur=chunk(s,'private fun OpenGLESScopeLazyPage(','private fun LazyListScope.stickyCollectionPager(')
    for t in ('24.dp.toPx()','AndroidRenderEffect.createBlurEffect','drawLayer(sourceLayer)','drawLayer(blurredLayer)','GlassTint'):
        require(t in blur,'actual blur rather than simulated overlay missing: '+t)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in s and 'SUBMISSION_SCHEMA_VERSION = 2' in s,'unrelated report drift')
if __name__=='__main__':main_guard('verify_2_2_5_input_layout_accessibility',verify)
