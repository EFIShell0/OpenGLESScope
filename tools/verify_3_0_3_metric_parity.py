from pathlib import Path
R=Path(__file__).resolve().parents[1]
s=(R/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
v=(R/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in v and 'releaseVersionCode = 3007' in v
ref=(R/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md').read_text(encoding='utf-8')
assert 'VulkanScope' in ref
assert 'maxWidth < 300.dp || (fontScale >= 1.55f && maxWidth < 420.dp) -> 1' in s
assert 'maxWidth < 760.dp -> 2' in s and 'else -> 3' in s
assert 'metrics.chunked(columns)' in s and 'ExpressiveMetric(label, value, Modifier.weight(1f))' in s
# Build-blocking Kotlin regression: only ONE reference-equivalent composable may own this signature.
assert s.count('private fun ExpressiveMetricGrid(metrics: List<Pair<String, String>>, modifier: Modifier = Modifier)') == 1, 'duplicate ExpressiveMetricGrid causes Kotlin OVERLOAD_RESOLUTION_AMBIGUITY'
assert 'private fun MetricCard(title: String, value: String, modifier: Modifier)' in s, 'preserve independent Overview metric card'
names=['FeaturesPage','LimitsPage','FormatsPage','ExtensionsPage','PrecisionPage','ConfigsPage','SearchRows']
for name in names:
 a=s.index(f'private fun {name}(')
 b=s.find('\n@Composable',a+1)
 if b<0:b=len(s)
 source=s[a:b]
 assert 'ExpressiveMetricGrid(' in source, f'{name}: no responsive metric grid'
 assert 'Showing' in source and 'COLLECTION_PAGE_SIZE' in source, f'{name}: missing true pager ranges'
 assert 'CapabilityKeyValue("Matches",' not in source, f'{name}: old plain Matches counter remains'
assert '"Limits" to filtered.size.toString()' in s
assert '"Evidence rows" to filtered.size.toString()' in s
assert 'diagnosticByName[name]?.status.equals("Available", true)' in s
assert '"Matching formats" to filtered.size.toString()' in s
assert '"Matching extensions" to filtered.size.toString()' in s
assert '"Shader stages" to rows.map { it.shader }.distinct().size.toString()' in s
assert 'items(visibleRows, key = { "${it.shader}|${it.type}" })' in s
assert 'stickyCollectionPager(rows.size, page, { page = it })' in s
assert s.count('"Showing" to if (') >= 7
assert 'Compatibility notice: when a newer OpenGLESScope release raises the Database submission floor' in s
print('OpenGLESScope 3.0.3 responsive, truthful filtered/paged metric-card parity: PASS')
