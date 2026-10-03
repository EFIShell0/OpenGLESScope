from pathlib import Path
import re,subprocess,tempfile
R=Path(__file__).resolve().parents[1]
main=(R/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
gradle=(R/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle
assert 'private val emulatorTranslatorPrefix' in main and 'reportedTranslationLayer(renderer) != null' in main
assert 'pattern.findAll(renderer)' in main and 'return candidates.singleOrNull()' in main
assert '.put("name", r.renderer).put("vendor", r.vendor)' in main
assert 'DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })' in main
assert 'DetailEvidenceRow("GL_VENDOR", report.vendor.ifBlank { "Unavailable" })' in main
source=main[main.index('private val gpuVendorArtworkRules'):main.index('\n@Composable\nprivate fun DatabaseReportVendorBadge(')]
ids=set(re.findall(r'R\.drawable\.(gpu_vendor_\w+)',source))
stubs='object R { object drawable { '+' '.join(f'const val {x}={i+1};' for i,x in enumerate(sorted(ids)))+' } }\n'
fixture=stubs+source+'''
fun main(){
    val examples=listOf(
        Triple("Android Emulator OpenGL ES Translator (NVIDIA GeForce RTX 3050 Ti Laptop GPU/PCIe/SSE2)","NVIDIA GeForce RTX 3050 Ti Laptop GPU",R.drawable.gpu_vendor_nvidia),
        Triple("Android Emulator OpenGL ES Translator (AMD Radeon RX 7900 XT/PCIe)","AMD Radeon RX 7900 XT",R.drawable.gpu_vendor_amd),
        Triple("Android Emulator OpenGL ES Translator (Intel(R) Iris Xe Graphics)","Intel(R) Iris Xe Graphics",R.drawable.gpu_vendor_intel),
        Triple("Android Emulator OpenGL ES Translator (Adreno (TM) 710)","Adreno (TM) 710",R.drawable.gpu_vendor_qualcomm),
        Triple("Android Emulator OpenGL ES Translator (Mali-G78)","Mali-G78",R.drawable.gpu_vendor_arm),
        Triple("Android Emulator OpenGL ES Translator (Immortalis-G715)","Immortalis-G715",R.drawable.gpu_vendor_arm),
        Triple("Android Emulator OpenGL ES Translator (PowerVR GE8320)","PowerVR GE8320",R.drawable.gpu_vendor_imagination),
        Triple("Android Emulator OpenGL ES Translator (Xclipse 920)","Xclipse 920",R.drawable.gpu_vendor_samsung),
        Triple("Android Emulator OpenGL ES Translator (VideoCore VI)","VideoCore VI",R.drawable.gpu_vendor_broadcom),
        Triple("Android Emulator OpenGL ES Translator (Vivante GC7000)","Vivante GC7000",R.drawable.gpu_vendor_vivante),
        Triple("Android Emulator OpenGL ES Translator (Maleoon 910)","Maleoon 910",R.drawable.gpu_vendor_huawei),
        Triple("Android Emulator OpenGL ES Translator (VeriSilicon VIP9000)","VeriSilicon VIP9000",R.drawable.gpu_vendor_unknown),
        Triple("Android Emulator OpenGL ES Translator (Apple M2 Pro)","Apple M2 Pro",R.drawable.gpu_vendor_unknown),
        Triple("ANGLE (NVIDIA, GeForce RTX 4090, Vulkan 1.3)","GeForce RTX 4090",R.drawable.gpu_vendor_nvidia)
    )
    for ((raw,model,icon) in examples){
        check(presentedRendererName("Google (NVIDIA Corporation)",raw)==model){"wrong model: $raw"}
        check(vendorArtworkResource("Google (NVIDIA Corporation)",raw)==icon){"wrong logo: $raw"}
        check(reportedTranslationLayer(raw)!=null)
    }
    val unknown=listOf(
        "Android Emulator OpenGL ES Translator (NVIDIA Corporation)",
        "Android Emulator OpenGL ES Translator (Unknown GPU)",
        "Android Emulator OpenGL ES Translator (SwiftShader NVIDIA GeForce RTX 3050 Ti Laptop GPU)",
        "Android Emulator OpenGL ES Translator (Microsoft Basic Render Driver, Radeon RX 7900 XT)",
        "Android Emulator OpenGL ES Translator (NVIDIA GeForce RTX 3050 Ti, AMD Radeon RX 7900 XT)",
        "Android Emulator OpenGL ES Translator (GeForce RTX 4090 and GeForce RTX 3050 Ti)",
        "ANGLE (NVIDIA, GeForce RTX 4090, SwiftShader)",
        "Bare unrecognized GPU"
    )
    for (raw in unknown){
        check(presentedRendererName("Google (NVIDIA Corporation)",raw)==raw){"invented model: $raw"}
        check(vendorArtworkResource("Google (NVIDIA Corporation)",raw)==R.drawable.gpu_vendor_unknown){"invented logo: $raw"}
    }
    check(vendorArtworkResource("NVIDIA Corporation","GeForce RTX 4090")==R.drawable.gpu_vendor_nvidia)
    check(vendorArtworkResource("Google (NVIDIA Corporation)","Bare unknown renderer")==R.drawable.gpu_vendor_unknown)
    println("3.0.7 Kotlin translator identity + multi-family/ambiguous/software evidence: PASS")
}
'''
with tempfile.TemporaryDirectory(prefix='og307-gpu-') as td:
    src=Path(td)/'Gpu.kt';src.write_text(fixture,encoding='utf-8')
    jar=Path(td)/'gpu.jar'
    cp=subprocess.run(['kotlinc',str(src),'-include-runtime','-d',str(jar)],capture_output=True,text=True,timeout=95)
    assert cp.returncode==0,cp.stderr
    cp=subprocess.run(['java','-jar',str(jar)],capture_output=True,text=True,timeout=25)
    assert cp.returncode==0,cp.stderr
    print(cp.stdout.strip())
for needle in ['private val emulatorTranslatorPrefix','pattern.findAll(renderer)','return candidates.singleOrNull()','reportedTranslationLayer(renderer) != null']:
    assert needle in source or needle in main
    assert needle not in main.replace(needle,'',1)
print('3.0.7 translation-layer source mutation guards: PASS')
