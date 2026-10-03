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
    "versionName": 'val releaseVersionName = "3.0.7"' in gradle,
    "versionCode": "val releaseVersionCode = 3007" in gradle,
    "compileTarget37": "compileSdk {" in gradle and "version = release(37)" in gradle and "minorApiLevel = 2" in gradle and "targetSdk = 37" in gradle,
    "minSdk31": "minSdk = 31" in gradle,
    "ndk30": 'ndkVersion = "30.0.16248370"' in gradle,
    "agp941": 'version "9.4.1"' in root_gradle,
    "gradle971": "gradle-9.7.1-bin.zip" in wrapper,
    "abis": all(x in gradle for x in ["arm64-v8a", "armeabi-v7a", "x86_64"]) and 'include("x86")' not in gradle,
    "material3Expressive": "androidx.compose.material3:material3:1.5.0-alpha28" in gradle and "MotionScheme.expressive()" in main,
    "currentCoreKtx": "androidx.core:core-ktx:1.19.1" in gradle,
    "currentSplash": "androidx.core:core-splashscreen:1.2.0" in gradle and "installSplashScreen()" in main and 'android:theme="@style/AppTheme.Starting"' in manifest,
    "currentCompose": all(x in gradle for x in ["androidx.compose.ui:ui:1.12.1","androidx.compose.foundation:foundation:1.12.1","androidx.compose.animation:animation:1.12.1"]),
    "generatedInfoVersions": all(x in main for x in ["BuildConfig.CORE_KTX_VERSION","BuildConfig.SPLASHSCREEN_VERSION","BuildConfig.COMPOSE_VERSION","BuildConfig.MATERIAL3_VERSION","BuildConfig.AGP_VERSION","BuildConfig.NDK_VERSION"]),
    "androidPlatformParity": 'android.hardware.type.pc' in manifest and 'minSdk = 31' in gradle,
    "currentKhronosBaseline": 'OpenGL ES 3.2 · GLSL ES 3.20 (spec revision 8) · EGL 1.5' in main and 'Khronos OpenGL registry SHA-256' in main and 'Khronos EGL registry SHA-256' in main,
    "analysisExpressiveOptIn": "@OptIn(ExperimentalMaterial3ExpressiveApi::class)\n@Composable\nprivate fun AnalysisPage" in main,
    "zxing": "com.google.zxing:core:3.5.4" in gradle and "QRCodeWriter" in qr,
    "databaseHost": "https://openglesscope-database-api.openglesscope.workers.dev" in main,
    "databaseBodyLimit": "2 * 1024 * 1024" in main,
    "databaseNoRedirect": ".followRedirects(false)" in main and ".followSslRedirects(false)" in main,
    "schema2": 'SUBMISSION_SCHEMA_VERSION = 2' in main and 'put("schemaVersion", SUBMISSION_SCHEMA_VERSION)' in main,
    "technicalSchema5": 'TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'put("technicalReport", JSONObject()' in main and '.put("schemaVersion", TECHNICAL_REPORT_SCHEMA_VERSION)' in main and '.put("glRuntime", JSONObject()' in main and '.put("internalFormats", internalFormats)' in main,
    "probeProcess": 'android:process=":opengles_probe"' in manifest and 'android:exported="false"' in manifest,
    "probeBound": "MAX_RESULT_BYTES = 8L * 1024L * 1024L" in service and "timeoutMs = 20_000L" in main,
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
    "runtimeFormatQueries": all(x in native for x in ["GL_NUM_COMPRESSED_TEXTURE_FORMATS", "GL_COMPRESSED_TEXTURE_FORMATS", "GL_NUM_SHADER_BINARY_FORMATS", "GL_SHADER_BINARY_FORMATS", "GL_NUM_PROGRAM_BINARY_FORMATS", "GL_PROGRAM_BINARY_FORMATS", "glGetInternalformativ", "GL_NUM_SAMPLE_COUNTS", "GL_SAMPLES"]),
    "reportDatasets": all(x in main for x in ["OPENGL ES LIMITS", "OPENGL ES EXTENSIONS", "EGL DISPLAY EXTENSIONS", "EGL CLIENT EXTENSIONS", "COMPRESSED TEXTURE FORMATS", "INTERNAL FORMAT SAMPLE SUPPORT", "SHADER BINARY FORMATS", "PROGRAM BINARY FORMATS", "SHADER PRECISION", "QUERY DIAGNOSTICS", "EGL CONFIGS", "EGL runtime"]),
    "analysisTabs": all(x in main for x in ['"Compare"','"Search"','"Diagnostics"','"Database"','"Requirements"','"Minimums"','"Graph"','"Presentation"','"Raw JSON"','"History"','"Quality"','"Watched"','"Share"','"Tests"']),
    "analysisSnapshot8MiB": "private const val ANALYSIS_MAX_SNAPSHOT_BYTES = 8 * 1024 * 1024" in main,
    "analysisBounded": all(x in main for x in ["ANALYSIS_MAX_ENTRIES = 32768", "ANALYSIS_MAX_KEY_LENGTH = 1024", "ANALYSIS_MAX_VALUE_LENGTH = 16384", "ANALYSIS_MAX_WATCHED = 256"]),
    "analysisFullEvidence": all(x in main for x in ["limit/", "extension/gl/", "extension/egl-display/", "extension/egl-client/", "format/compressed/", "format/shader-binary/", "format/program-binary/", "precision/", "egl-runtime/", "eglconfig/", "query/", "display/"]) and all(x in main for x in ["recordableAndroid=", "framebufferTargetAndroid=", "colorComponentTypeExt=", "unavailableAttributes="]),
    "analysisCompleteness": "regression candidate" in main and "enumeration" in main.lower(),
    "specMinimumCount": minimum_block.count("GlMinimum(") == 104,
    "specMinimumDirection": all(x in minimum_block for x in ['GlMinimum("GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum")', 'GlMinimum("GL_MIN_PROGRAM_TEXEL_OFFSET", -8.0, "≤ -8", "maximum")']),
    "dependencyGraph": all(x in main for x in ["GL_QUERY_DEPENDENCIES", "EGL_QUERY_DEPENDENCIES", "QUERY_DEPENDENCIES", "EGL_ANDROID_recordable", "EGL_EXT_pixel_format_float", "GL_KHR_shader_subgroup", "GL_EXT_sparse_texture", "GL_OES_viewport_array"]) and "OpenGLESDependencyGraph" in main and "Canvas" in graph,
    "quality": "Collection integrity score" in main and "performance benchmark" in main and "Scoring method" in main,
    "watched": 'putStringSet("watched"' in main and "Matched" in main and "Missing" in main,
    "canonicalPermalink": "#reports/" in main and "/Overview" in main and "last_database_report_id" in main,
    "localQr": "OpenGLESQrCode" in main,
    "htmlCsp": "Content-Security-Policy" in main and "default-src 'none'" in main,
    "securityPatch": 'put("securityPatch", Build.VERSION.SECURITY_PATCH)' in main and 'Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" }' in main and 'matches(Regex("\\\\d{4}-\\\\d{2}-\\\\d{2}"))' in main,
    "singleRulesRoot": rules.count("# OpenGLESScope Engineering Rules") == 1,
    "cleanArchivePolicy": "Release source ZIP must not contain IDE/VCS/cache/build/credential/keystore/pyc/__pycache__ artifacts." in rules,
    "audit072": (root / "rules/0.7.2_COMPILE_CORRECTNESS_AND_SHARED_QUALITY_PARITY_AUDIT.md").is_file(),
    "noReadme": not any(x.is_file() and x.name.lower() == "readme.md" for x in root.rglob("*")),
    "separateLimitDiagnostics": "Query diagnostics are counted separately" in main or "Implementation limits and query diagnostics are counted separately" in main,
    "formatSearch": "Search formats…" in main and "Enumeration query" in main,
    "precisionSearch": "Search shader precision…" in main,
    "eglStructured": all(x in main for x in ["EGL identity", "Current EGL binding and context", "Collector pbuffer", "EGL runtime query failures"]),
    "extensionRegistry": "extensionRegistryUrl" in main and "Khronos specification" in main and "Dedicated query handler" in main,
    "fullAndroidMetadata": all(x in main for x in ["Build.BRAND", "Build.DEVICE", "Build.BOARD", "Build.HARDWARE", "Build.VERSION.CODENAME", "Build.ID", "Build.VERSION.INCREMENTAL", "Build.FINGERPRINT"]),
    "submissionApplicationAbi": all(x in main for x in ['put("applicationAbi", detectInstalledAbi(context))', 'put("supportedDeviceAbis", JSONArray(Build.SUPPORTED_ABIS.toList()))']),
    "rules212": "## Release 2.1.2 current toolchain/spec/query/detail parity audit" in rules,
    "rules213": "## Release 2.1.3 NDK r30 native compile-correctness hotfix" in rules,
    "rules214": "## Release 2.1.4 Kotlin/Compose compile-correctness hotfix" in rules,
    "rules220": "## Release 2.2.0 VulkanScope 3.0.12 visual/interaction parity contract" in rules,
    "rules221": "## Release 2.2.1 Kotlin/Compose parity compile-correctness hotfix" in rules,
    "rules222": "## Release 2.2.2 VulkanScope video-detail parity audit" in rules,
    "uiParity220": (root / "tools/verify_2_2_0_ui_parity.py").is_file() and (root / "tools/verify_2_2_0_regression_contract.py").is_file() and (root / "tools/test_2_2_0_negative_mutations.py").is_file(),
    "kotlinCompileHotfix214": all(x in main for x in ["import androidx.compose.animation.animateContentSize","import androidx.compose.animation.core.FastOutSlowInEasing","internal enum class EvidenceState","val graphRegistryReferences = remember(context, graphSelectedRoot)"]) and "import androidx.compose.foundation.layout.matchParentSize" not in main,
    "audit073": (root / "rules/0.7.3_HDR_PROVENANCE_AND_SPEC_CORRECTNESS_AUDIT.md").is_file(),
    "hdrStatusModel": 'val hdrCapabilityStatus: String = "unknown"' in main,
    "hdrModeApi34": 'Build.VERSION.SDK_INT >= 34 -> if (types.isNotEmpty()) "available" else "unavailable"' in main,
    "hdrNullUnknown": 'Build.VERSION.SDK_INT >= 24 && hdr == null -> "unknown"' in main,
    "hdrTextUnknown": 'display.hdrCapabilityStatus == "unknown" -> "Unknown / not exposed"' in main,
    "hdrStatusAnalysis": 'put("display/hdrCapabilityStatus", display.hdrCapabilityStatus)' in main,
    "hdrStatusSubmission": main.count('put("hdrCapabilityStatus", d.hdrCapabilityStatus)') == 2,
    "hdrStatusText": 'HDR capability status: ${d.hdrCapabilityStatus}' in main,
    "hdrRawUnknown": 'else -> "Android HDR type $v"' in main,
}
for path in (root / "app/src").rglob("*"):
    if path.suffix in {".kt", ".cpp", ".c", ".h", ".hpp"}:
        text = path.read_text(encoding="utf-8", errors="ignore")
        if re.search(r"(^|\s)//(?!/)", text, re.MULTILINE) or "/*" in text:
            checks[f"noSourceComments:{path.relative_to(root)}"] = False
for forbidden in [".gradle", "build", ".idea"]:
    checks[f"noTransient:{forbidden}"] = not any(p.name == forbidden for p in root.rglob("*"))
# __pycache__ is enforced by verify_package_hygiene after all parallel gates finish;
# checking it here races sibling Python verifiers and can produce a false release failure.
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
    checks["reference134Extras"] = len(extras) == 134
    checks["referenceNativeTokens"] = all((lambda cap: cap in native or (cap.startswith('glGetFragmentShadingRatesEXT(samples=') and 'glGetFragmentShadingRatesEXT' in native))(re.sub(r'\[\d+\]$', '', row.get('capability',''))) for row in rows)

forbidden_product = ("caps" + "viewer").lower()
checks["noForbiddenProductName"] = not any(forbidden_product in x.read_text(encoding="utf-8", errors="ignore").lower() for x in root.rglob("*") if x.is_file()) and not any(forbidden_product in str(x.relative_to(root)).lower() for x in root.rglob("*"))
checks["noPackagedStoreMetadata"] = not any(x.is_dir() and x.name.lower() == "fastlane" for x in root.rglob("*"))
checks["noRootReleaseMd"] = not (root / "release.md").exists()
checks["currentRules"] = "VulkanScope 3.0.12 remains the byte-locked API-neutral UI/interaction/quality methodology reference" in rules
checks["networkStateParity"] = "ACCESS_NETWORK_STATE" in manifest and "registerDefaultNetworkCallback" in main and "NetworkStatusBanner" in main and "OfflineFeatureAvailabilityBanner" in main
checks["updateTransferParity"] = all(x in main for x in ["UpdateTransferState", "UpdateTransferPhase", "pauseUpdateDownload", "resumeUpdateDownload", "requestCancelUpdateDownload", "UpdateTransferDialog", "UpdateCancelConfirmationDialog"])
checks["updaterSecurityRetained160"] = all(x in main for x in ["MAX_UPDATE_REDIRECTS = 3", "isAllowedOfficialUpdateRedirect", "sha256FileHex(temp)", "packageSigningCertificatesMatch", "cleanupStaleUpdateArtifacts"])
checks["searchClear160"] = 'contentDescription = "Clear search"' in main and "trailingIcon" in main
checks["semanticIcons160"] = all((root / "app/src/main/res/drawable" / x).is_file() for x in ["ic_analysis.xml","ic_compare.xml","ic_book.xml","ic_self_test.xml","ic_check_updates.xml","ic_update_available.xml"])
failed = [k for k, v in checks.items() if not v]
if failed:
    print("OpenGLESScope 3.0.7 audit: FAIL")
    for key in failed:
        print(key)
    sys.exit(1)
print("OpenGLESScope 3.0.7 audit: PASS")
print(f"minimums={minimum_block.count('GlMinimum(')} referenceFloor=145 extras=134 analysisMaxBytes={8 * 1024 * 1024} schema=2 technicalReport=5")
