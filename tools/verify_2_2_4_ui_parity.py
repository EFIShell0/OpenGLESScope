#!/usr/bin/env python3
from __future__ import annotations
import re
from gate_common import ROOT,require,main_guard

def block(source,start,end):
    a=source.index(start); b=source.index(end,a+len(start));return source[a:b]

def verify():
    g=(ROOT/'app/build.gradle.kts').read_text();m=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text();rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    require(any(f'releaseVersionName = "{v}"' in g and f'releaseVersionCode = {c}' in g for v,c in [('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'wrong current release')
    require('## Release 2.2.4 paging/Analysis/status/report source parity audit' in rules,'release contract not documented')
    nav=block(m,'private fun navigationItems()','private fun pageTransitionIndex')
    for t in ('Page.Overview','Page.OpenGLES','Page.EGL','Page.Extensions'):require('NavigationItem('+t in nav,'bottom destination absent: '+t)
    require('NavigationItem(Page.Display' not in nav,'Display incorrectly primary')
    require(nav.index('Page.Overview')<nav.index('Page.OpenGLES')<nav.index('Page.EGL')<nav.index('Page.Extensions'),'bottom order drift')
    require('Page.Display, Page.Features' in m,'Display child routing lost')
    lazy=block(m,'private fun OpenGLESScopeLazyPage(','private fun LazyListScope.stickyCollectionPager(')
    for t in ('pagerRegistrations','pagerMeasuredHeights','CollectionPagerVisualState','LocalPinnedPagerContentInset','reportStickyPagerState','activePagerVisual','drawWithContent','sourceLayer','blurredLayer','clipToBounds','ExpressiveScrollHints','coordinatedTransientOverlayInset'):
        require(t in lazy,'measured single sticky page overlay missing: '+t)
    for t in ('OverlayCoordinationLead = 96.dp','OverlayCoordinationMotionMillis = 120','LocalStickyPagerStateReporter provides stickyPagerStateReporter','headerContentInset + coordinatedPinnedPagerInset','private fun LazyListScope.stickyCollectionPager('):require(t in m,'overlay coordination regression: '+t)
    require(len(re.findall(r'\bstickyCollectionPager\(',m))>=9,'heavy collections not page controlled')
    require('item { CollectionPager(' not in m,'old disconnected pager returned')
    require('private fun ScrollBoundaryIndicators(listState: LazyListState' in m and 'listState.firstVisibleItemScrollOffset' in m,'scroll-offset-aware lazy arrows missing')
    require('private fun ScrollBoundaryIndicators(gridState: LazyGridState' in m and 'private fun ScrollBoundaryIndicators(scrollState: ScrollState' in m,'other scroll-arrow overloads missing')
    require('REPORT_LIST_CURSOR_TIMESTAMP' in m and 'REPORT_LIST_CURSOR_ID' in m,'unbounded cursor')
    for t in ('addQueryParameter("beforeSubmittedAt", cursor.submittedAt)','addQueryParameter("beforeId", cursor.id)','optJSONObject("nextCursor")','AnalysisReportPage(rows, next)','ANALYSIS_DATABASE_LIST_MAX','filter { previous.add(it.id) }','databaseNextCursor = result.nextCursor','"Load more", R.drawable.ic_download','databaseListPage = 0'):
        require(t in m,'report cursor paging broken: '+t)
    require('databaseRunning = true\n                            databaseBaseline = null\n                            val requestedId = databaseId' in m,'exact report selection must invalidate stale baseline before asynchronous request')
    for t in ('withContext(Dispatchers.Default)','rawTechnicalStatus = "Preparing bounded technical evidence…"','databaseBaseline = null','if (requestedId == databaseId)','finally { databaseRunning = false }'):
        require(t in m,'Analysis stale/locking regression: '+t)
    for t in ('private fun TransientStatusOverlayHost(','modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (overlayVisible) 8.dp else 0.dp)','Arrangement.spacedBy(6.dp)','private fun FloatingStatusSurface(','widthIn(max = 520.dp)','RoundedCornerShape(22.dp)','padding(horizontal = 13.dp, vertical = 8.dp)','liveRegion = LiveRegionMode.Polite'):
        require(t in m,'status popup geometry/accessibility drift: '+t)
    if 'releaseVersionName = "2.2.14"' in g:
        for t in ('private fun TransientActionButton(','state = if (success) 2 else 3','delay(3000)','Text(submitState, color = ComposeColor(0xFFAAAAAA)','TransientIconButton(idleIcon = R.drawable.ic_copy','if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log','databaseSubmissionExceptionLog('): require(t in m,'reference submit animation/status/server confirmation regressed: '+t)
        require('private fun DatabaseSubmissionStateCard(' not in m,'duplicated report state card returned')
    else:
        for t in ('private fun DatabaseSubmissionStateCard(','targetState = phase','reportSubmissionStateTransition','visible = submitting','submissionSuccessId = result.reportId','if (result.success) submissionSuccessId','databaseSubmissionExceptionLog('):require(t in m,'historical submission state regression: '+t)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in m and 'SUBMISSION_SCHEMA_VERSION = 2' in m,'report schema changed')
    require(re.search(r'(?i)\b(?:turnip|vulkanscope|vulkan)\b',m) is None,'Vulkan product prose leaked into UI')
if __name__=='__main__':main_guard('verify_2_2_4_ui_parity',verify)
