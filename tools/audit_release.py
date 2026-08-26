from pathlib import Path
import csv
import json
import re
import sys

root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
root_gradle = (root / "build.gradle.kts").read_text(encoding="utf-8")
wrapper = (root / "gradle/wrapper/gradle-wrapper.properties").read_text(encoding="utf-8")
main = (root / "app/src/main/java/com/efishell/openglesscope/MainActivity.kt").read_text(encoding="utf-8")
service = (root / "app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt").read_text(encoding="utf-8")
native = (root / "app/src/main/cpp/openglesscope.cpp").read_text(encoding="utf-8")
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
rules = (root / "rules/PROJECT_RULES.md").read_text(encoding="utf-8")
qr = (root / "app/src/main/java/com/efishell/openglesscope/OpenGLESQrCode.kt").read_text(encoding="utf-8")
graph = (root / "app/src/main/java/com/efishell/openglesscope/OpenGLESDependencyGraph.kt").read_text(encoding="utf-8")
minimum_block = main[main.index("private val OPENGL_ES_32_MINIMUMS"):main.index("private val GL_QUERY_DEPENDENCIES")]
new_extension_queries = [
    "GL_SUBGROUP_SIZE_KHR", "GL_SUBGROUP_SUPPORTED_STAGES_KHR", "GL_SUBGROUP_SUPPORTED_FEATURES_KHR", "GL_SUBGROUP_QUAD_ALL_STAGES_KHR",
    "GL_MAX_WINDOW_RECTANGLES_EXT", "GL_MAX_VIEWPORTS_OES", "GL_VIEWPORT_SUBPIXEL_BITS_OES", "GL_VIEWPORT_BOUNDS_RANGE_OES", "GL_VIEWPORT_INDEX_PROVOKING_VERTEX_OES",
    "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_FAST_SIZE_EXT", "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_SIZE_EXT", "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_FAST_SIZE_EXT", "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_SIZE_EXT",
    "GL_MIN_SAMPLE_SHADING_VALUE_OES", "GL_MAX_SPARSE_TEXTURE_SIZE_EXT", "GL_MAX_SPARSE_3D_TEXTURE_SIZE_EXT", "GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_EXT", "GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_EXT",
]
checks = {
    "versionName": 'val releaseVersionName = "0.7.2"' in gradle,
    "versionCode": "val releaseVersionCode = 702" in gradle,
    "compileTarget37": "compileSdk = 37" in gradle and "targetSdk = 37" in gradle,
    "minSdk24": "minSdk = 24" in gradle,
    "ndk29": 'ndkVersion = "29.0.14206865"' in gradle,
    "agp932": 'version "9.3.2"' in root_gradle,
    "gradle971": "gradle-9.7.1-bin.zip" in wrapper,
    "abis": all(x in gradle for x in ["arm64-v8a", "armeabi-v7a", "x86_64"]) and 'include("x86")' not in gradle,
    "material3Expressive": "androidx.compose.material3:material3:1.5.0-alpha26" in gradle and "MotionScheme.expressive()" in main,
    "analysisExpressiveOptIn": "@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun AnalysisPage" in main,
    "zxing": "com.google.zxing:core:3.5.4" in gradle and "QRCodeWriter" in qr,
    "databaseHost": "https://openglesscope-database-api.openglesscope.workers.dev" in main,
    "databaseBodyLimit": "2 * 1024 * 1024" in main,
    "databaseNoRedirect": ".followRedirects(false)" in main and ".followSslRedirects(false)" in main,
    "schema2": 'put("schemaVersion", 2)' in main,
    "technicalSchema2": 'put("technicalReport", JSONObject()' in main and '.put("schemaVersion", 2)' in main and "technical report 2" in main,
    "probeProcess": 'android:process=":opengles_probe"' in manifest and 'android:exported="false"' in manifest,
    "probeBound": "8 * 1024 * 1024" in service and "TimeUnit.SECONDS.toNanos(20)" in main,
    "probeExecutorShutdown": "worker.shutdownNow()" in service,
    "atomicProbePublish": "Os.rename(temp.absolutePath, file.absolutePath)" in service and "java.nio.file.Files" not in service,
    "runtimeIdentity": all(x in native for x in ["GL_VENDOR", "GL_RENDERER", "GL_VERSION", "GL_SHADING_LANGUAGE_VERSION"]),
    "runtimeStringBounds": all(x in native for x in ["kMaxRuntimeStringBytes", "kMaxExtensionTokenBytes", "runtimeStringValid", "kMaxInfoLogBytes"]),
    "boundedGlErrorDrain": "for (int i = 0; i < 16; ++i)" in native and "while (glGetError()" not in native,
    "eglCleanup": "releaseEgl" in native and "eglReleaseThread()" in native and "eglTerminate(d)" in native,
    "eglRuntime": all(x in native for x in ["eglQueryAPI", "eglGetCurrentContext", "eglGetCurrentDisplay", "eglGetCurrentSurface", "EGL_CONTEXT_CLIENT_TYPE", "EGL_CONTEXT_CLIENT_VERSION", "EGL_SWAP_BEHAVIOR"]),
    "eglRuntimeReport": all(x in main for x in ["EglRuntimeInfo", 'section("EGL runtime"', '.put("eglRuntime", JSONObject()']),
    "eglConfigBounds": "kMaxEglConfigCount = 4096" in native and "totalConfigs > configCapacity" in native,
    "eglConfigExtensions": all(x in native for x in ["EGL_ANDROID_recordable", "EGL_ANDROID_framebuffer_target", "EGL_EXT_pixel_format_float", "unavailableAttributes"]),
    "extensionQueries": all(x in native for x in new_extension_queries),
    "noMisleadingStateQueries": "GL_NUM_WINDOW_RECTANGLES_EXT" not in native and "GL_MAX_SHADER_COMPILER_THREADS_KHR" not in native,
    "selfTestIdentity": all(x in main for x in ["runOpenGlesSelfTests(expected: GlReport)", "expected.vendor", "expected.renderer", "expected.glVersion"]),
    "selfTestBounds": all(x in native for x in ["kMaxProgramBinaryBytes", "kMaxInfoLogBytes", "written > 0 && written <= length", "GL_DEBUG_OUTPUT_SYNCHRONOUS"]),
    "runtimeFormatQueries": all(x in native for x in ["GL_NUM_COMPRESSED_TEXTURE_FORMATS", "GL_COMPRESSED_TEXTURE_FORMATS", "GL_NUM_SHADER_BINARY_FORMATS", "GL_SHADER_BINARY_FORMATS", "GL_NUM_PROGRAM_BINARY_FORMATS", "GL_PROGRAM_BINARY_FORMATS"]),
    "reportDatasets": all(x in main for x in ["OPENGL ES LIMITS", "OPENGL ES EXTENSIONS", "EGL DISPLAY EXTENSIONS", "EGL CLIENT EXTENSIONS", "COMPRESSED TEXTURE FORMATS", "SHADER BINARY FORMATS", "PROGRAM BINARY FORMATS", "SHADER PRECISION", "QUERY DIAGNOSTICS", "EGL CONFIGS", "EGL runtime"]),
    "analysisTabs": all(x in main for x in ['"Compare"', '"Spec minimums"', '"Graph"', '"Quality"', '"Watched"', '"Share"', '"Tests"']),
    "analysisSnapshot8MiB": "private const val ANALYSIS_MAX_SNAPSHOT_BYTES = 8 * 1024 * 1024" in main,
    "analysisBounded": all(x in main for x in ["ANALYSIS_MAX_ENTRIES = 32768", "ANALYSIS_MAX_KEY_LENGTH = 1024", "ANALYSIS_MAX_VALUE_LENGTH = 16384", "ANALYSIS_MAX_WATCHED = 256"]),
    "analysisFullEvidence": all(x in main for x in ["limit/", "extension/gl/", "extension/egl-display/", "extension/egl-client/", "format/compressed/", "format/shader-binary/", "format/program-binary/", "precision/", "egl-runtime/", "eglconfig/", "query/", "display/"]) and all(x in main for x in ["recordableAndroid=", "framebufferTargetAndroid=", "colorComponentTypeExt=", "unavailableAttributes="]),
    "analysisCompleteness": "regression candidate" in main and "enumeration" in main.lower(),
    "specMinimumCount": minimum_block.count("GlMinimum(") == 104,
    "specMinimumDirection": all(x in minimum_block for x in ['GlMinimum("GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_MIN_PROGRAM_TEXEL_OFFSET", -8.0, "≤ -8", "maximum")']),
    "dependencyGraph": all(x in main for x in ["GL_QUERY_DEPENDENCIES", "EGL_QUERY_DEPENDENCIES", "QUERY_DEPENDENCIES", "EGL_ANDROID_recordable", "EGL_EXT_pixel_format_float", "GL_KHR_shader_subgroup", "GL_EXT_sparse_texture", "GL_OES_viewport_array"]) and "OpenGLESDependencyGraph" in main and "Canvas" in graph,
    "quality": "Heuristic diagnostic evidence score" in main and "not an OpenGL ES conformance result" in main,
    "watched": 'putStringSet("watched"' in main and "Matched" in main and "Missing" in main,
    "canonicalPermalink": "#reports/" in main and "/Overview" in main and "last_database_report_id" in main,
    "localQr": "OpenGLESQrCode" in main,
    "htmlCsp": "Content-Security-Policy" in main and "default-src 'none'" in main,
    "securityPatch": 'put("securityPatch", Build.VERSION.SECURITY_PATCH)' in main and 'Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" }' in main and 'matches(Regex("\\\\d{4}-\\\\d{2}-\\\\d{2}"))' in main,
    "rules072": "## Release 0.7.2 compile correctness and shared-quality parity" in rules,
    "cleanArchivePolicy": "Third-party comparison product names are forbidden" in rules and "Root release.md files are forbidden" in rules and "README.md files are forbidden" in rules,
    "audit072": (root / "rules/0.7.2_COMPILE_CORRECTNESS_AND_SHARED_QUALITY_PARITY_AUDIT.md").is_file(),
    "noReadme": not any(x.is_file() and x.name.lower() == "readme.md" for x in root.rglob("*")),
    "separateLimitDiagnostics": "Query diagnostics are counted separately" in main or "Implementation limits and query diagnostics are counted separately" in main,
    "formatSearch": "Search formats…" in main and "Enumeration query" in main,
    "precisionSearch": "Search shader precision…" in main,
    "eglStructured": all(x in main for x in ["EGL identity", "Current EGL binding and context", "Collector pbuffer", "EGL runtime query failures"]),
    "extensionRegistry": "extensionRegistryUrl" in main and "Khronos spec" in main and "Implemented query gates" in main,
    "fullAndroidMetadata": all(x in main for x in ["Build.BRAND", "Build.DEVICE", "Build.BOARD", "Build.HARDWARE", "Build.VERSION.CODENAME", "Build.ID", "Build.VERSION.INCREMENTAL", "Build.FINGERPRINT"]),
    "submissionApplicationAbi": all(x in main for x in ['put("applicationAbi", detectInstalledAbi(context))', 'put("supportedDeviceAbis", JSONArray(Build.SUPPORTED_ABIS.toList()))']),
}
for path in (root / "app/src").rglob("*"):
    if path.suffix in {".kt", ".cpp", ".c", ".h", ".hpp"}:
        text = path.read_text(encoding="utf-8", errors="ignore")
        if re.search(r"(^|\s)//(?!/)", text, re.MULTILINE) or "/*" in text:
            checks[f"noSourceComments:{path.relative_to(root)}"] = False
for forbidden in [".gradle", "build", ".idea", "__pycache__"]:
    checks[f"noTransient:{forbidden}"] = not any(p.name == forbidden for p in root.rglob("*"))
obtainium = root / "obtainium-config.json"
checks["obtainiumConfig"] = obtainium.is_file()
if obtainium.is_file():
    data = json.loads(obtainium.read_text(encoding="utf-8"))
    settings = json.loads(data["apps"][0]["additionalSettings"])
    checks["obtainiumUniversal"] = settings.get("apkFilterRegEx") == r"(?i).*universal.*\.apk$" and settings.get("autoApkFilterByArch") is False
matrix_path = root / "PUBLIC_CAPABILITY_REFERENCE_MATRIX.csv"
checks["referenceMatrix"] = matrix_path.is_file() and (root / "PUBLIC_CAPABILITY_REFERENCE_AUDIT.md").is_file()
if matrix_path.is_file():
    with matrix_path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle))
    floor = [row for row in rows if row.get("reference") == "External public OpenGL ES capability reference"]
    extras = [row for row in rows if row.get("reference") == "OpenGLESScope additional query"]
    checks["reference145"] = len(floor) == 145 and all(row.get("status") == "parity" and row.get("ui_txt_html_database") == "yes" for row in floor)
    checks["reference49Extras"] = len(extras) == 49
    checks["referenceNativeTokens"] = all(re.sub(r"\[\d+\]$", "", row.get("capability", "")) in native for row in rows)

forbidden_product = ("caps" + "viewer").lower()
checks["noForbiddenProductName"] = not any(forbidden_product in x.read_text(encoding="utf-8", errors="ignore").lower() for x in root.rglob("*") if x.is_file()) and not any(forbidden_product in str(x.relative_to(root)).lower() for x in root.rglob("*"))
checks["noPackagedStoreMetadata"] = not any(x.is_dir() and x.name.lower() == "fastlane" for x in root.rglob("*"))
checks["noRootReleaseMd"] = not (root / "release.md").exists()
failed = [k for k, v in checks.items() if not v]
if failed:
    print("OpenGLESScope 0.7.2 audit: FAIL")
    for key in failed:
        print(key)
    sys.exit(1)
print("OpenGLESScope 0.7.2 audit: PASS")
print(f"minimums={minimum_block.count('GlMinimum(')} referenceFloor=145 extras=49 analysisMaxBytes={8 * 1024 * 1024} schema=2 technicalReport=2")
