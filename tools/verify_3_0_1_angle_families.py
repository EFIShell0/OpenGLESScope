from pathlib import Path
import re,subprocess,tempfile
R=Path(__file__).resolve().parents[1]
main=(R/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
gradle=(R/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle
assert 'collectionStatus = if (parsed.available) CollectionStatus.COMPLETED else CollectionStatus.FAILED' in main
assert 'collectionReady = collectionStatus != CollectionStatus.FAILED && !collecting && current.available' in main
assert 'if (collectionStatus == CollectionStatus.COMPLETED)' in main
assert 'if (collectionStatus == CollectionStatus.FAILED) collectionStatus = CollectionStatus.IDLE' not in main
assert 'CollectionStatusBanner(collectionStatus)' in main and 'visible = status != CollectionStatus.IDLE' in main
assert 'completeReportReady && !exportPreparing' in main
assert 'enabled = !submissionInFlight && completeReportReady && networkAvailable' in main
assert 'state = if (success) 2 else 3' in main and 'delay(3000)' in main
assert '.put("name", r.renderer).put("vendor", r.vendor)' in main
assert 'DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })' in main
assert 'DetailEvidenceRow("GL_VENDOR", report.vendor.ifBlank { "Unavailable" })' in main
source=main[main.index('private val gpuVendorArtworkRules'):main.index('\n@Composable\nprivate fun DatabaseReportVendorBadge(')]
ids=set(re.findall(r'R\.drawable\.(gpu_vendor_\w+)',source))
stubs='object R { object drawable { '+' '.join(f'const val {x}={i+1};' for i,x in enumerate(sorted(ids)))+' } }\n'
fixture=stubs+source+'''
fun main(){
    val cases=listOf(
        Triple("ANGLE (Qualcomm, Vulkan 1.3.284 (Adreno (TM) 710 (0x07010000)), Driver-512.800.70)","Adreno (TM) 710",R.drawable.gpu_vendor_qualcomm),
        Triple("ANGLE (ARM, Mali-G78, Vulkan 1.3)","Mali-G78",R.drawable.gpu_vendor_arm),
        Triple("ANGLE (ARM, Immortalis-G715, Vulkan 1.3)","Immortalis-G715",R.drawable.gpu_vendor_arm),
        Triple("ANGLE (Imagination, PowerVR GE8320, Vulkan 1.2)","PowerVR GE8320",R.drawable.gpu_vendor_imagination),
        Triple("ANGLE (Samsung, Xclipse 920, Vulkan 1.3)","Xclipse 920",R.drawable.gpu_vendor_samsung),
        Triple("ANGLE (NVIDIA, GeForce RTX 4090, Direct3D11)","GeForce RTX 4090",R.drawable.gpu_vendor_nvidia),
        Triple("ANGLE (AMD, Radeon RX 7900 XT, Direct3D12)","Radeon RX 7900 XT",R.drawable.gpu_vendor_amd),
        Triple("ANGLE (Intel, Intel(R) UHD Graphics 620, D3D11)","Intel(R) UHD Graphics 620",R.drawable.gpu_vendor_intel),
        Triple("ANGLE (Broadcom, VideoCore VI, Vulkan)","VideoCore VI",R.drawable.gpu_vendor_broadcom),
        Triple("ANGLE (Vivante, Vivante GC7000, OpenGL ES)","Vivante GC7000",R.drawable.gpu_vendor_vivante),
        Triple("ANGLE (Huawei, Maleoon 910, Vulkan)","Maleoon 910",R.drawable.gpu_vendor_huawei),
        Triple("ANGLE (VeriSilicon, VeriSilicon VIP9000, OpenGL ES)","VeriSilicon VIP9000",R.drawable.gpu_vendor_unknown),
        Triple("ANGLE (Apple, Apple M2 Pro, Metal)","Apple M2 Pro",R.drawable.gpu_vendor_unknown)
    )
    for ((renderer,model,icon) in cases){
        check(presentedRendererName("Google Inc.",renderer)==model){"bad model: $renderer"}
        check(vendorArtworkResource("Google Inc.",renderer)==icon){"bad icon: $renderer"}
        check(presentedGlVendor("Google Inc.",renderer).startsWith("Google LLC ("))
    }
    for (renderer in listOf(
        "ANGLE (Google, Vulkan 1.3, GPU unavailable)",
        "ANGLE (Qualcomm, Vulkan 1.3)",
        "ANGLE (Google, SwiftShader, Adreno (TM) 710)",
        "ANGLE (NVIDIA, GeForce RTX 4090, AMD Radeon RX 7900)"
    )){
        check(presentedRendererName("Google Inc.",renderer)==renderer)
        check(vendorArtworkResource("Google Inc.",renderer)==R.drawable.gpu_vendor_unknown)
    }
    check(presentedGlVendor("Google Inc. (Qualcomm)",cases[0].first)=="Google LLC (Qualcomm)")
    println("3.0.2 multi-family ANGLE models, artwork, software/ambiguity fallbacks and persistent failure actions: PASS")
}
'''
with tempfile.TemporaryDirectory(prefix='og301-gpu-') as d:
    src=Path(d)/'Gpu.kt';src.write_text(fixture,encoding='utf-8')
    jar=Path(d)/'gpu.jar'
    cp=subprocess.run(['kotlinc',str(src),'-include-runtime','-d',str(jar)],capture_output=True,text=True,timeout=95)
    assert cp.returncode==0,cp.stderr
    cp=subprocess.run(['java','-jar',str(jar)],capture_output=True,text=True,timeout=20)
    assert cp.returncode==0,cp.stderr
    print(cp.stdout.strip())
