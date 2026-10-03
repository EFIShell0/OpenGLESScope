#!/usr/bin/env python3
"""Release 3.0.7 VulkanScope-like terminal failure presentation contract."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
s=(r/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
gradle=(r/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle
assert 'else if (!current.available) EmptyState(' not in s, 'global failure gate hides all page content'
assert 'PageContent(' in s and 'AnimatedContent(' in s
assert 'if (collecting) LoadingView()' in s
assert 'report = parseReport(JSONObject()' in s and '.put("status", "unavailable")' in s and '.put("reason", error.message?' in s
assert 'collectionStatus = CollectionStatus.FAILED' in s
start=s.index('private fun PageContent(')
end=s.index('@Composable\nprivate fun OverviewPage(',start)
section=s[start:end]
assert 'Page.Overview -> OverviewPage(report, display, onNavigate)' in section
assert 'Page.Display -> DisplayPage(display)' in section
assert 'Page.Encyclopedia -> RegistryEncyclopediaPage(' in section
assert 'Page.Settings -> SettingsPage(' in section
assert 'UnavailableCapabilityPage(page.title, report.reason)' in section
assert 'OpenGLESScopeLazyPage(' in section and 'This page requires a completed runtime report' in section
start=s.index('private fun OverviewPage(')
end=s.index('@Composable\nprivate fun HeroCard(',start)
overview=s[start:end]
assert 'if (!report.available)' in overview and 'item { ExploreCard(navigate) }' in overview
assert 'if (report.available) item {' in overview and 'CapabilitySectionCard("Runtime snapshot")' in overview
assert 'CapabilitySectionCard("Operating system")' in overview
assert 'if (report.available) shortGlVersion(report.glVersion) else "Unknown"' in overview
assert 'collectionReady = collectionStatus != CollectionStatus.FAILED && !collecting && current.available' in s
assert 'if (!report.available) {' in s[s.index('private fun HeroCard('):s.index('private val gpuVendorArtworkRules')]
assert 'Text("OpenGL® ES™ unavailable"' in s
assert s.count('private fun ExpressiveMetricGrid(')==1
# Mutation test: the historical dead gate is rejected and cannot silently return in a subsequent release.
mutated=s.replace('else if (current == null) EmptyState("No OpenGL® ES™ report")', 'else if (current == null) EmptyState("No OpenGL® ES™ report")\n                        else if (!current.available) EmptyState("Unavailable")',1)
assert 'else if (!current.available) EmptyState(' in mutated and 'else if (!current.available) EmptyState(' not in s
print('OpenGLESScope 3.0.7 failure-state page, navigation, truthful evidence and export gating: PASS')
