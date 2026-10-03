#!/usr/bin/env python3
from __future__ import annotations
import re,struct
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def between(t,a,b):
    x=t.find(a);require(x>=0,'missing section '+a);y=t.find(b,x+len(a));require(y>x,'missing section end '+b);return t[x:y]
def verify():
    s=(ROOT/MAIN).read_text();g=(ROOT/'app/build.gradle.kts').read_text();r=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g,'current release identity mismatch')
    require('## Release 2.2.9 File Manager, Khronos, viewport and brand alignment parity' in r,'new engineering contract missing')
    browser=between(s,'private fun SharedStorageBrowserDialog(','\n@Composable\nprivate fun SoftScrollIntersectionShadows(')
    require('Box(Modifier.fillMaxSize().background(ComposeColor.Black).zIndex(50f))' in browser and 'color = ComposeColor.Black,' in browser,'browser exposes purple background instead of reference black')
    require('color = SurfaceDark,' not in browser,'non-reference purple browser root')
    require('BackHandler(enabled = !busy)' in browser and 'screenVisible = false' in browser,'full-screen/TV exit regression')
    ext=between(s,'private fun ExtensionsPage(','\n@Composable\nprivate fun PrecisionPage(')
    needed = ['extensionRegistryUrl(ext)?.let { url ->','ExpressiveContainedIconTextButton(','"Open Khronos specification"','R.drawable.ic_open_external','enabled = LocalValidatedNetwork.current','uriHandler.openUri(url)']
    if 'releaseVersionName = "2.2.14"' not in g: needed.append('modifier = Modifier.fillMaxWidth()')
    for x in needed:
        require(x in ext,'reference Khronos contained action missing: '+x)
    require('ExpressiveActionButton("Open Khronos specification"' not in ext,'stale extension menu action shape')
    if 'releaseVersionName = "2.2.14"' in g: require('modifier = Modifier.fillMaxWidth()' not in ext[ext.index('ExpressiveDetailDialog(ext'):], 'reference Khronos action must not stretch')
    app=between(s,'private fun OpenGLESScopeApp(','\nprivate fun navigationItems()')
    for x in ('var bottomNavigationHeightPx by remember { mutableIntStateOf(0) }','val bottomNavigationContentInset = with(appDensity) { bottomNavigationHeightPx.toDp() } + PrimaryNavigationContentGap','LocalBottomNavigationContentInset provides bottomNavigationContentInset','onSizeChanged { bottomNavigationHeightPx = it.height }'):
        require(x in app,'viewport-measured bottom navigation inset missing: '+x)
    page=between(s,'private fun OpenGLESScopeLazyPage(','\nprivate fun LazyListScope.stickyCollectionPager(')
    for x in ('bottom = bottomNavigationContentInset','bottom = bottomNavigationContentInset + 4.dp'):
        require(x in page,'last content/scroll arrow can go behind actual navigation: '+x)
    require('bottomNavigationContentInset + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp' not in page,'double system navigation inset returned')
    sections=between(s,'private fun OpenGlesSectionCards(','\n@Composable\nprivate fun OpenGlesDestinationPage(')
    require('bottom = bottomNavigationInset + 4.dp' in sections,'OpenGL section arrow double counts Android navigation inset')
    nav=between(s,'private fun CompactBottomNavigationBar(','\n@Composable\nprivate fun HeaderActionButton(')
    for x in ('if (item.page == Page.EGL)','EglBrandArtwork(','tint = ComposeColor.White','contentAlignment = Alignment.Center','compactIconSize - 2.dp'):
        require(x in nav,'neutral white and vertically centered original EGL artwork missing: '+x)
    egl=between(s,'private fun EglBrandArtwork(','\n@Composable\nprivate fun SemanticArtwork(')
    require('painterResource(R.drawable.ic_egl_official)' in egl and 'ColorFilter.tint(tint)' in egl,'official EGL image not preserved/theme routable')
    header=between(s,'private fun AppHeader(','\n@Composable\nprivate fun PageContent(')
    require('R.drawable.openglesscope_logo_horizontal_aligned' in header and 'else 126.dp' in header and '.height(30.dp)' in header,'header logo reference drawing bounds absent')
    image=ROOT/'app/src/main/res/drawable-nodpi/openglesscope_logo_horizontal_aligned.png'
    raw=image.read_bytes();require(raw[:8]==bytes.fromhex('89504e470d0a1a0a'),'aligned logo is not PNG')
    require(struct.unpack('>II',raw[16:24])==(546,84),'header logo frame != reference 546 x 84')
    require('R.drawable.ic_egl_official' in s,'official EGL branding removed')
if __name__=='__main__':main_guard('verify_2_2_9_reference_ui',verify)
