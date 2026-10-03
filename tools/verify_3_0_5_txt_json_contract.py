from pathlib import Path
import re, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
s=(root/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
g=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in g and 'releaseVersionCode = 3007' in g
report=s[s.index('private fun reportText('):s.index('private fun reportHtml(')]
for label,field in [('config ID','surfaceConfigId'),('GL colorspace','surfaceGlColorspace'),('VG alpha format','surfaceVgAlphaFormat'),('VG colorspace','surfaceVgColorspace'),('horizontal resolution','surfaceHorizontalResolution'),('largest pbuffer','surfaceLargestPbuffer'),('pixel aspect ratio','surfacePixelAspectRatio'),('vertical resolution','surfaceVerticalResolution')]:
    assert re.search(r'appendLine\("Pbuffer '+re.escape(label)+r': \${runtimeQueryEvidence\(r, r\.eglRuntime\.'+field+r"",report),label
assert report.count('appendLine("Pbuffer: config=') == 1
assert 'eglConfigAnalysisValue(r, c)' in report
config=s[s.index('private fun eglConfigAnalysisValue('):s.index('private fun glAnalysisEntries(')]
for key in ('recordableAndroid','framebufferTargetAndroid','colorComponentTypeExt','unavailableAttributes'):
    assert f'"{key}=${{' in config, key
assert 'GL_TIME_ELAPSED_EXT_QUERY_COUNTER_BITS' not in s and 'GL_TIMESTAMP_EXT_QUERY_COUNTER_BITS' not in s
print('3.0.7 source-derived TXT/JSON pbuffer + per-config serialization: PASS')
