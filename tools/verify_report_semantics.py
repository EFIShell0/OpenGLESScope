#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text()
    required=['renderer','vendor','glVersion','glMajor','glMinor','glslVersion','egl','glRuntime','eglRuntime','eglCapabilities','extensions','limits','compressedFormats','internalFormats','shaderBinaryFormats','programBinaryFormats','precision','eglConfigs','diagnostics']
    for x in required: require(f'"{x}"' in main,f'parse/report required field missing: {x}')
    contract=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text()
    require('val completeSnapshot = OpenGLESProbeContract.isCompleteAvailableReport(o)' in main and 'val available = completeSnapshot' in main,'partial report could be promoted to available')
    for token in ['requiredBaseFields','requiredEglFields','requiredGlRuntimeFields','requiredEglRuntimeFields','requiredEglConfigFields','isUniqueStringArray','isBoundedStringArray','isLimitArray','isEglCapabilityArray','isInternalFormatArray','isPrecisionArray','isDiagnosticArray','uniqueObjectArray']:
        require(token in contract,f'structural complete-report contract missing: {token}')
    for token in [
        '!isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS)', '!isLimitArray(root.opt("limits"))',
        '!isBoundedStringArray(root.opt("compressedFormats"), MAX_ENUMERATION_ITEMS)', '!isInternalFormatArray(root.opt("internalFormats"))', '!isBoundedStringArray(root.opt("shaderBinaryFormats"), MAX_ENUMERATION_ITEMS)',
        '!isBoundedStringArray(root.opt("programBinaryFormats"), MAX_ENUMERATION_ITEMS)', '!isPrecisionArray(root.opt("precision"))',
        '!isEglConfigArray(root.opt("eglConfigs"))', '!isDiagnosticArray(root.opt("diagnostics"))',
        'requiredEglFields.all(egl::has)', 'requiredEglRuntimeFields.all(runtime::has)',
        'requiredEglConfigFields.all(item::has)', '!names.add(name)'
    ]:
        require(token in contract,f'strict structural report validation regressed: {token}')
    for status in ['Available','Unavailable','Not applicable','Unknown']:
        require(status in main or status in native,f'evidence state missing: {status}')
    require('OpenGL ES does not expose a standardized driver-version query' in main,'driver version must remain explicitly unavailable rather than inferred')
    require('runtimeVersion' in main and 'expected.renderer' in main and 'expected.vendor' in main,'self-test runtime attribution guard missing')
    require('Report exceeds the 2 MiB transport limit; no data was truncated' in main,'non-truncating database transport guard missing')
    require('Database returned a malformed report ID' in main and 'Regex("[a-f0-9]{64}")' in main,'database success must require canonical 64-hex report ID')
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and '.put("schemaVersion", TECHNICAL_REPORT_SCHEMA_VERSION)' in main and '.put("glRuntime", JSONObject()' in main and '.putNullable("robustAccessQuery", r.glRuntime.robustAccessQuery)' in main and '.put("internalFormats", internalFormats)' in main and '.put("eglCapabilities", JSONArray(r.eglCapabilities.map' in main,'Database technicalReport v5 must preserve GL runtime provenance, EGL runtime, internal-format and EGL capability evidence')
    for token in ['Build.MANUFACTURER','Build.BRAND','Build.MODEL','Build.PRODUCT','Build.DEVICE','Build.BOARD','Build.HARDWARE','Build.VERSION.CODENAME','Build.ID','Build.VERSION.INCREMENTAL','Build.FINGERPRINT','Build.VERSION.SECURITY_PATCH']:
        require(token in main,f'structured/full device build metadata missing: {token}')
    # No device-unique IDs are collected/submitted.
    for forbidden in ['Settings.Secure.ANDROID_ID','getSerial()','Build.getSerial','getImei','TelephonyManager','WifiInfo.getMacAddress','AccountManager']:
        require(forbidden not in main,f'forbidden device-unique/private identifier path present: {forbidden}')
    for label in ['OPENGL ES LIMITS','OPENGL ES EXTENSIONS','EGL DISPLAY EXTENSIONS','EGL CLIENT EXTENSIONS','EGL CAPABILITY QUERIES','COMPRESSED TEXTURE FORMATS','INTERNAL FORMAT SAMPLE SUPPORT','SHADER BINARY FORMATS','PROGRAM BINARY FORMATS','SHADER PRECISION','QUERY DIAGNOSTICS','EGL CONFIGS']:
        require(label in main,f'TXT/HTML report dataset contract missing: {label}')

if __name__=='__main__': main_guard('verify_report_semantics',verify)
