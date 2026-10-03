from pathlib import Path
import re,subprocess,tempfile
R=Path(__file__).resolve().parents[1]
main=(R/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
gradle=(R/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle
assert 'presentedRendererName(report.vendor, report.renderer)' in main
assert 'presentedGlVendor(report.vendor, report.renderer)' in main
assert 'DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })' in main
assert 'DetailEvidenceRow("GL_VENDOR", report.vendor.ifBlank { "Unavailable" })' in main
assert '.put("name", r.renderer).put("vendor", r.vendor)' in main
assert 'val busy = state != 0' in main and 'state = if (success) 2 else 3' in main
assert 'enabled = !submissionInFlight && completeReportReady && networkAvailable' in main
assert 'submissionFailureLog = result.log' in main
assert 'delay(3000)' in main
mapping=main[main.index('private val gpuVendorArtworkRules'):main.index('\n@Composable\nprivate fun DatabaseReportVendorBadge(')]
ids=set(re.findall(r'R\.drawable\.(gpu_vendor_\w+)',mapping))
stubs='object R { object drawable { '+' '.join(f'const val {x}={i+1};' for i,x in enumerate(sorted(ids)))+' } }\n'
fixture=stubs+mapping+'''
fun main() {
    val rawVendor="Google Inc. (Qualcomm)"
    val rawRenderer="ANGLE (Qualcomm, Vulkan 1.3.284 (Adreno (TM) 710 (0x07010000)), Qualcomm Technologies Inc. Adreno Vulkan Driver-512.800.70)"
    check(presentedRendererName(rawVendor,rawRenderer)=="Adreno (TM) 710")
    check(presentedGlVendor(rawVendor,rawRenderer)=="Google LLC (Qualcomm)")
    check(vendorArtworkResource(rawVendor,rawRenderer)==R.drawable.gpu_vendor_qualcomm)
    check(vendorArtworkResource("Google Inc.","ANGLE (SwiftShader Device, Vulkan)")==R.drawable.gpu_vendor_unknown)
    check(vendorArtworkResource("Google Inc.","ANGLE (Unknown, Vulkan)")==R.drawable.gpu_vendor_unknown)
    check(vendorArtworkResource("Google Inc.","ANGLE (Qualcomm, Vulkan)")==R.drawable.gpu_vendor_unknown)
    check(presentedRendererName("Google Inc.","ANGLE (SwiftShader Device, Vulkan)")=="ANGLE (SwiftShader Device, Vulkan)")
    check(presentedGlVendor("Google Inc.","ANGLE (Unknown, Vulkan)")=="Google LLC")
    check(rawVendor=="Google Inc. (Qualcomm)")
    check(rawRenderer.contains("Vulkan 1.3.284"))
    println("3.0.2 ANGLE display, raw GL identity, Google LLC label and vendor artwork: PASS")
}
'''
with tempfile.TemporaryDirectory(prefix='og300-angle-') as d:
    src=Path(d)/'Angle.kt';src.write_text(fixture,encoding='utf-8')
    jar=Path(d)/'angle.jar'
    cp=subprocess.run(['kotlinc',str(src),'-include-runtime','-d',str(jar)],capture_output=True,text=True,timeout=95)
    assert cp.returncode==0,cp.stderr
    cp=subprocess.run(['java','-jar',str(jar)],capture_output=True,text=True,timeout=20)
    assert cp.returncode==0,cp.stderr
    print(cp.stdout.strip())
print('3.0.2 submission failure transient and 3.0.19 producer contract source checks: PASS')
