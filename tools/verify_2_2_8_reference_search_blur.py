#!/usr/bin/env python3
from __future__ import annotations
import hashlib, re, gzip, json
from pathlib import Path
from gate_common import ROOT,require,main_guard
MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
def section(s,first,last):
    a=s.find(first);require(a>=0,'missing UI reference: '+first)
    b=s.find(last,a+len(first));require(b>a,'missing UI boundary: '+last)
    return s[a:b]
def verify():
    g=(ROOT/'app/build.gradle.kts').read_text();s=(ROOT/MAIN).read_text();r=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require('val releaseVersionName = "2.2.14"' in g and 'val releaseVersionCode = 2214' in g,'current producer identity drift')
    for t in ('compileSdk {','version = release(37) {','minorApiLevel = 2','targetSdk = 37','"COMPILE_SDK_LEVEL", "37"','"COMPILE_SDK_MINOR_LEVEL", "2"'):
        require(t in g,'minor API release 37.2 DSL/BuildConfig missing: '+t)
    require('targetSdk = 37.2' not in g and 'compileSdk = 37' not in g,'invalid fake SDK minor assignment')
    logo=section(s,'private fun ExpressiveVersionBlock(','\n@Composable\n')
    for t in ('Surface(shape = RoundedCornerShape(19.dp), color = BrandContainer)','painter = painterResource(R.drawable.openglesscope_logo_foreground)','contentScale = ContentScale.Fit','modifier = Modifier.size(46.dp)','Text("Version $version"'):
        require(t in logo,'Info Application brand/reference geometry missing: '+t)
    require('openglesscope_scope_wordmark' not in logo,'big cropped SCOPE logo returned to version card')
    src=(ROOT/'app/src/main/res/drawable-nodpi/openglesscope_logo_foreground.png')
    require(src.is_file(),'original brand image missing')
    require('## Release 2.2.8 full-logo, continuous blur, SDK 37.2 and Encyclopedia search parity audit' in r,'release rule section missing')
    app=section(s,'private fun OpenGLESScopeApp(','\nprivate fun navigationItems()')
    for t in ('val chromeSourceLayer = rememberGraphicsLayer()','val chromeBlurredLayer = rememberGraphicsLayer()','AndroidRenderEffect.createBlurEffect(chromeBlurRadiusPx, chromeBlurRadiusPx, Shader.TileMode.CLAMP)','val chromeBlurRadiusPx = with(appDensity) { 24.dp.toPx() }','SideEffect { chromeBlurredLayer.renderEffect = chromeRenderEffect }','chromeSourceLayer.record { this@drawWithContent.drawContent() }','chromeBlurredLayer.record { drawLayer(chromeSourceLayer) }','drawLayer(chromeSourceLayer)'):
        require(t in app,'global, actually drawn 24dp blurred fallback missing: '+t)
    require(app.count('ChromeBackdropSource(chromeBlurredLayer, Offset.Zero)')==2,'both top and bottom chrome must use real global fallback')
    require('if (pageChromeBackdrop?.layer === layer) pageChromeBackdrop = null' in app,'outgoing page may remove newer page registration')
    require('val fallbackChromeLayer = rememberGraphicsLayer()' not in app and 'ChromeBackdropSource(fallbackChromeLayer' not in app,'never recorded blur fallback returned')
    require('CompositionLocalProvider(LocalChromeBackdropReporter provides chromeBackdropReporter' in app,'page-specific chrome registration lost')
    require(app.index('chromeSourceLayer.record') < app.index('val navigationBackdrop'),'fallback includes self-referential navigation chrome')
    enc=section(s,'private fun RegistryEncyclopediaPage(', '\nprivate fun AnalysisPage(')
    for t in ('var page by rememberSaveable { mutableIntStateOf(0) }','query = initialQuery.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = 0; onSeedConsumed()','withContext(Dispatchers.IO) { RegistryCatalog.load(context) }','catch (cancelled: CancellationException) {','throw cancelled','catalogError = error.message?.take(240)','take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1)','val safePage = page.coerceIn(0, pageCount - 1)','matchedEntries.drop(safePage * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE)','onValueChange = { query = it.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = 0 }','ExpressiveMetricGrid(listOf(','stickyCollectionPager(matchedEntries.size, safePage, { page = it.coerceIn(0, pageCount - 1) })'):
        require(t in enc,'Encyclopedia responsive/cancellation/bounded-page regression: '+t)
    require('runCatching { RegistryCatalog.load' not in enc,'catalog cancellation must not be swallowed by runCatching')
    for t in ('{ category = categories[it]; page = 0 }','{ apiFilter = apis[it]; page = 0 }','{ runtimeFilter = runtimeStates[it]; page = 0 }'):
        require(t in enc,'filter edit fails to clear stale page: '+t)
    grid=section(s,'private fun ExpressiveMetricGrid(','\n@Composable\nprivate fun')
    for t in ('maxWidth < 300.dp','fontScale >= 1.55f','maxWidth < 760.dp','metrics.chunked(columns)','Modifier.weight(1f)','ExpressiveMetric(label, value, Modifier.weight(1f))'):
        require(t in grid,'responsive reference metric-card layout drift: '+t)
    require('"${BuildConfig.COMPILE_SDK_LEVEL}.${BuildConfig.COMPILE_SDK_MINOR_LEVEL}"' in s,'toolchain UI does not show actual API 37.2')
    for p in (ROOT/'app/src/main').rglob('*'):
        if p.is_file() and p.suffix.lower() in {'.kt','.java','.xml','.json','.html','.csv','.txt'}:
            require(not re.search(r'(?i)\b(?:vulkanscope|vulkan|turnip)\b',p.read_text(encoding='utf-8',errors='replace')),'foreign graphics-product copy: '+str(p.relative_to(ROOT)))
    src=(ROOT/'app/src/main/assets/registry_catalog.json.gz');raw=gzip.decompress(src.read_bytes())
    require(len(raw)<2*1024*1024,'expanded catalog > 2 MiB')
    data=json.loads(raw)
    require(isinstance(data,(list,dict)),'registry catalog not a JSON collection')
    require('private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 250' in s and 'private const val COLLECTION_PAGE_SIZE = 25' in s,'search/paging bounds drift')
if __name__=='__main__':main_guard('verify_2_2_8_reference_search_blur',verify)
