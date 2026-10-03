#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import os,re,shutil,subprocess,tempfile

def verify():
 main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
 gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
 require(('releaseVersionName = "1.5.1"' in gradle and 'releaseVersionCode = 1501' in gradle) or ('releaseVersionName = "1.6.0"' in gradle and 'releaseVersionCode = 1600' in gradle),'1.5.1+ compiler-correction identity missing')
 good='${c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }}")'
 bad='${c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None"}")'
 require(good in main,'corrected unavailableAttributes interpolation missing')
 require(bad not in main,'known 1.5.0 missing interpolation brace remains')
 require(re.search(r'^private fun reportHtml\(context: Context, r: GlReport, d: DisplayInfo\): String \{',main,re.M) is not None,'reportHtml must remain a top-level private function')
 # Optional real Kotlin parser probe. Android/Compose symbols are intentionally unresolved
 # without the Android classpath; only parser/syntax diagnostics are release-blocking here.
 kotlinc=None if os.environ.get('OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE')=='1' else shutil.which('kotlinc')
 if kotlinc:
  with tempfile.TemporaryDirectory(prefix='ogles151-kotlinc-') as td:
   cp=subprocess.run([kotlinc,str(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'),'-d',str(os.path.join(td,'out.jar'))],text=True,capture_output=True)
   out=(cp.stdout or '')+'\n'+(cp.stderr or '')
   syntax_patterns=[r'Syntax error:',r'Expecting [\'\"}`)]',r'Unterminated',r'Unexpected tokens']
   for pattern in syntax_patterns:
    require(re.search(pattern,out,re.I) is None,'kotlinc syntax probe failed: '+pattern)
if __name__=='__main__': main_guard('verify_1_5_1_compile_hotfix',verify)
