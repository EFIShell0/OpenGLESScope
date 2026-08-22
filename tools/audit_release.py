from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle.kts').read_text()
main = (root / 'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
service = (root / 'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt').read_text()
native = (root / 'app/src/main/cpp/openglesscope.cpp').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
rules = (root / 'rules/PROJECT_RULES.md').read_text()
checks = {
    'versionName': 'val releaseVersionName = "0.2.7"' in gradle,
    'versionCode': 'val releaseVersionCode = 207' in gradle,
    'compileTarget37': 'compileSdk = 37' in gradle and 'targetSdk = 37' in gradle,
    'minSdk24': 'minSdk = 24' in gradle,
    'abis': all(x in gradle for x in ['arm64-v8a', 'armeabi-v7a', 'x86_64']) and 'include("x86")' not in gradle,
    'databaseHost': 'https://openglesscope-database-api.openglesscope.workers.dev' in main,
    'databaseBodyLimit': '2 * 1024 * 1024' in main,
    'schema2': 'put("schemaVersion", 2)' in main,
    'technicalSchema1': '.put("schemaVersion", 1)' in main,
    'hdrUnitUi': main.count('cd/m²') >= 6,
    'hdrInvalidAllApis': 'rawTypes.filter { it != Display.HdrCapabilities.HDR_TYPE_INVALID }' in main,
    'api34HdrMode': 'Build.VERSION.SDK_INT >= 34 -> d.mode.supportedHdrTypes.toList()' in main,
    'wideColorApi26': 'if (Build.VERSION.SDK_INT >= 26) d.isWideColorGamut else null' in main,
    'displayListenerDispose': 'unregisterDisplayListener(listener)' in main,
    'probeProcess': 'android:process=":opengles_probe"' in manifest and 'android:exported="false"' in manifest,
    'probeBound': '8 * 1024 * 1024' in service and '20 seconds' in main,
    'probeExecutorShutdown': 'worker.shutdownNow()' in service,
    'eglCleanup': all(x in native for x in ['eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)', 'eglDestroySurface(d, s)', 'eglDestroyContext(d, c)', 'eglTerminate(d)']),
    'glIdentity': all(x in native for x in ['GL_VENDOR', 'GL_RENDERER', 'GL_VERSION', 'GL_SHADING_LANGUAGE_VERSION']),
    'khrDebugGate': 'glCode < 320 && hasExt(glExt, "GL_KHR_debug")' in native,
    'timerGate': 'hasExt(glExt, "GL_EXT_disjoint_timer_query")' in native and 'eglGetProcAddress("glGetQueryivEXT")' in native,
    'reportDatasets': all(x in main for x in ['OPENGL ES LIMITS', 'OPENGL ES EXTENSIONS', 'EGL DISPLAY EXTENSIONS', 'EGL CLIENT EXTENSIONS', 'COMPRESSED TEXTURE FORMATS', 'SHADER BINARY FORMATS', 'PROGRAM BINARY FORMATS', 'SHADER PRECISION', 'QUERY DIAGNOSTICS', 'EGL CONFIGS']),
    'htmlCsp': 'Content-Security-Policy' in main and "default-src 'none'" in main,
    'updateOfficialRepo': '/EFIShell0/OpenGLESScope/releases/download/' in main,
    'rules022': 'Release 0.2.2 Android security-patch end-to-end reporting' in rules,
    'securityPatchJson': '.put("securityPatch", Build.VERSION.SECURITY_PATCH)' in main,
    'securityPatchTxtTop': 'Android security patch: ${Build.VERSION.SECURITY_PATCH}' in main,
    'securityPatchTxtDevice': 'Security patch: ${Build.VERSION.SECURITY_PATCH}' in main,
    'securityPatchUi': 'CapabilityKeyValue("Security patch", Build.VERSION.SECURITY_PATCH)' in main,
    'securityPatchHtml': '"Security patch" to Build.VERSION.SECURITY_PATCH' in main,
}
source_files = [root / 'app/src/main/java/com/efishell/openglesscope/MainActivity.kt', root / 'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt', root / 'app/src/main/cpp/openglesscope.cpp']
for path in source_files:
    text = path.read_text()
    if re.search(r'(^|\s)//(?!/)', text, re.MULTILINE) or re.search(r'/\*', text):
        checks[f'noSourceComments:{path.name}'] = False
for forbidden in ['.gradle', 'build', '.idea', '__pycache__']:
    checks[f'noTransient:{forbidden}'] = not any(p.name == forbidden for p in root.rglob('*'))
failed = [k for k, v in checks.items() if not v]
if failed:
    print('OpenGLESScope 0.2.7 audit: FAIL')
    for key in failed:
        print(key)
    sys.exit(1)

main_source = (root / "app/src/main/java/com/efishell/openglesscope/MainActivity.kt").read_text(encoding="utf-8")
for required in [
    'prefs.getBoolean("direct_updates_enabled", true)',
    'if (directUpdatesEnabled) {',
    'checkForApplicationUpdate(false)',
    'showDirectUpdatesDisabledIntroIfFirstInstall()',
    'kotlinx.coroutines.delay(7_000L)',
    'Direct GitHub updates are currently disabled. Obtainium can manage updates externally, or direct updates can be enabled in Settings.',
    'may duplicate update notifications.',
    'github.com/EFIShell0/OpenGLESScope/releases',
    'Switch(checked = activity.directUpdatesEnabled'
]:
    if required not in main_source:
        raise SystemExit(f"Missing Obtainium update-management evidence: {required}")
if 'Add to Obtainium' in main_source or 'obtainium://app/' in main_source or 'openInObtainium' in main_source:
    raise SystemExit('Runtime source must not expose the removed Add to Obtainium action')
if 'IzzyOnDroid' in main_source:
    raise SystemExit('Current OpenGLESScope runtime source must not retain IzzyOnDroid-specific messaging')
obtainium_config = root / 'obtainium-config.json'
if not obtainium_config.is_file():
    raise SystemExit('Missing obtainium-config.json')
import json
obtainium_data = json.loads(obtainium_config.read_text(encoding='utf-8'))
obtainium_settings = json.loads(obtainium_data['apps'][0]['additionalSettings'])
if obtainium_settings.get('apkFilterRegEx') != r'(?i).*universal.*\.apk$' or obtainium_settings.get('autoApkFilterByArch') is not False:
    raise SystemExit('obtainium-config.json must preserve universal APK selection')
if not (root / "LICENSE").is_file():
    raise SystemExit("Missing root LICENSE")

print('OpenGLESScope 0.2.7 audit: PASS')
