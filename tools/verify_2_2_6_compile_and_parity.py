#!/usr/bin/env python3
from __future__ import annotations
import hashlib,re,json
from gate_common import ROOT,require,main_guard

def chunk(s,start,end):
    a=s.index(start);b=s.index(end,a+len(start));return s[a:b]
def verify():
    s=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    g=(ROOT/'app/build.gradle.kts').read_text()
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require(any(f'releaseVersionName = "{v}"' in g and f'releaseVersionCode = {c}' in g for v,c in [('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.14',2214)]),'wrong 2.2.6+ release identity')
    require('## Release 2.2.6 real Kotlin compile correction and UI parity re-audit' in rules,'compile correction contract missing')
    require(s.count('import androidx.compose.runtime.rememberUpdatedState')==1,'Compose rememberUpdatedState must have one correct import')
    require('import androidx.compose.runtime.remember\nimport androidx.compose.runtime.rememberUpdatedState\n' in s,'Compose import resolution changed')
    require('val latestOnPageChange = rememberUpdatedState(onPageChange)' in s,'sticky pager callback must follow latest lambda')
    require('R.drawable.ic_chevron_down' not in s and '"Load more", R.drawable.ic_download,' in s,'undefined report-list icon returned')
    available={p.stem for p in (ROOT/'app/src/main/res').rglob('*') if p.is_file() and p.parent.name.startswith(('drawable','mipmap'))}
    missing=sorted(set(re.findall(r'R\.drawable\.([A-Za-z_]\w*)',s))-available)
    require(not missing,'unresolved R.drawable resource references: '+repr(missing))
    if 'releaseVersionName = "2.2.14"' in g:
        analysis=chunk(s,'private fun AnalysisPage(','@Composable\nprivate fun DatabaseSubmissionFailureDialog(')
    else:
        analysis=chunk(s,'private fun AnalysisPage(','@Composable\nprivate fun DatabaseSubmissionStateCard(') if '@Composable\nprivate fun DatabaseSubmissionStateCard(' in s else chunk(s,'private fun AnalysisPage(','@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun DatabaseSubmissionStateCard(')
    require('val databaseComparisonRows = remember(databaseBaseline, currentSnapshot, includeUnchanged)' in analysis,'Analysis comparison not cached from a composable context')
    require(analysis.index('val databaseComparisonRows = remember(')<analysis.index('OpenGLESScopeLazyPage(verticalSpacing = 10.dp)'),'remember accidentally invoked in non-composable LazyListScope DSL')
    require('val dbRows = remember(' not in analysis,'obsolete non-composable remember invocation remains')
    require('databaseComparisonRows.count {' in analysis and 'items(databaseComparisonRows.take(ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT)' in analysis,'hoisted snapshot comparison not rendered')
    if 'releaseVersionName = "2.2.14"' in g:
        require('@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun AnalysisPage(' in s,'Analysis still needs explicit Material3 opt-in')
        require('private fun TransientActionButton(' in s and 'private fun DatabaseSubmissionStateCard(' not in s,'reference transient Database submit row missing or duplicate card returned')
        require('private fun TransientIconButton(' in s and 'AnimatedContent(' in s[s.index('private fun TransientIconButton('):s.index('private fun ExpressiveLinearProgressIndicator(')],'report ID animation missing')
    else:
        require('@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun DatabaseSubmissionStateCard(' in s,'Material3 experimental LoadingIndicator not explicitly opted into')
        dialog=chunk(s,'private fun DatabaseSubmissionStateCard(','@Composable\nprivate fun DatabaseSubmissionFailureDialog(')
        require('1 -> LoadingIndicator(color = BrandSoft' in dialog and 'targetState = phase' in dialog,'historic success/error animation drift')
    require('if (nextId != databaseId) {' in analysis and 'databaseId = nextId\n                                    databaseBaseline = null' in analysis,'typing a new report ID must invalidate stale Analysis comparison immediately')
    require('catch (error: Throwable) { if (requestedId == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }' in analysis,'stale exact-ID request may clobber newer Analysis state')
    require('catch (error: Throwable) { if (row.id == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }' in analysis,'stale selected-row request may clobber newer Analysis state')
    pager=chunk(s,'private fun LazyListScope.stickyCollectionPager(','@Composable\nprivate fun ScrollBoundaryIndicators(')
    normalized=re.sub(r'\s+','',pager).encode()
    require(hashlib.sha256(normalized).hexdigest()=='9b4fa68dfe39b3ba3554b4438d885ceb0de51971d25da57c3c6cf821112ddf5f','shared pager differs from frozen VulkanScope 3.0.12 reference')
    icons=json.loads((ROOT/'tests/golden/2_2_0_vulkanscope_common_drawable_hashes.json').read_text())['files']
    require(len(icons)==103,'common drawable census truncated')
    for name,expected in icons.items():
        p=ROOT/'app/src/main/res'/name
        require(p.is_file() and hashlib.sha256(p.read_bytes()).hexdigest()==expected,'VulkanScope common icon mismatch: '+name)
    for token in ('TECHNICAL_REPORT_SCHEMA_VERSION = 5','SUBMISSION_SCHEMA_VERSION = 2','private fun FloatingStatusSurface(','private fun SharedStorageBrowserDialog(','private fun EvidenceCopyDialog(','private fun HdrCapabilitiesCarousel(','private fun CompactBottomNavigationBar('):
        require(token in s,'existing shared surface or reporting schema disappeared: '+token)
if __name__=='__main__':main_guard('verify_2_2_6_compile_and_parity',verify)
