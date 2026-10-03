package com.efishell.openglesscope

import android.util.JsonReader
import android.util.JsonToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.StringReader

internal object OpenGLESProbeContract {
    private const val MAX_JSON_DEPTH = 64
    private const val MAX_JSON_CONTAINER_ITEMS = 131072
    private const val MAX_LIMITS = 8192
    private const val MAX_ENUMERATION_ITEMS = 16384
    private const val MAX_EGL_CAPABILITIES = 256
    private const val MAX_INTERNAL_FORMATS = 256
    private const val MAX_PRECISION = 64
    private const val MAX_DIAGNOSTICS = 16384
    private const val MAX_EGL_CONFIGS = 4096
    private const val MAX_EGL_FAILURES = 64
    private val terminalStates = setOf("Available", "Unavailable", "Not applicable", "Unknown")

    private val requiredBaseFields = setOf(
        "renderer", "vendor", "glVersion", "glMajor", "glMinor", "glslVersion", "egl", "glRuntime", "eglRuntime", "eglCapabilities",
        "extensions", "limits", "compressedFormats", "internalFormats", "shaderBinaryFormats", "programBinaryFormats", "precision",
        "eglConfigs", "diagnostics"
    )

    private val requiredEglFields = setOf(
        "vendor", "version", "initializedVersion", "clientApis", "extensions", "clientExtensions"
    )

    private val requiredGlRuntimeFields = setOf(
        "contextFlags", "resetNotificationStrategy", "resetNotificationStrategyQuery", "robustAccess", "robustAccessQuery", "unavailableAttributes"
    )

    private val requiredEglRuntimeFields = setOf(
        "boundApi", "configId", "clientType", "clientVersion", "renderBuffer",
        "currentContext", "currentDisplay", "currentDrawSurface", "currentReadSurface",
        "surfaceGlColorspace", "surfaceGlColorspaceQuery", "surfaceVgAlphaFormat", "surfaceVgAlphaFormatQuery", "surfaceVgColorspace", "surfaceVgColorspaceQuery", "surfaceConfigId",
        "surfaceWidth", "surfaceHeight", "surfaceHorizontalResolution", "surfaceLargestPbuffer", "surfacePixelAspectRatio", "surfaceVerticalResolution",
        "surfaceRenderBuffer", "surfaceSwapBehavior", "surfaceTextureFormat", "surfaceTextureTarget", "surfaceMipmapTexture", "surfaceMipmapLevel",
        "surfaceMultisampleResolve", "unavailableAttributes"
    )

    private val requiredEglConfigFields = setOf(
        "id", "red", "green", "blue", "alpha", "depth", "stencil", "sampleBuffers", "samples",
        "surfaceType", "renderableType", "conformant", "configCaveat", "colorBufferType", "level",
        "nativeRenderable", "nativeVisualId", "minSwapInterval", "maxSwapInterval", "bufferSize",
        "luminanceSize", "alphaMaskSize", "bindToTextureRgb", "bindToTextureRgba", "maxPbufferWidth",
        "maxPbufferHeight", "maxPbufferPixels", "nativeVisualType", "transparentType", "transparentRed",
        "transparentGreen", "transparentBlue", "recordableAndroid", "framebufferTargetAndroid",
        "colorComponentTypeExt", "unavailableAttributes"
    )

    fun isTerminalJson(candidate: String, selfTest: Boolean): Boolean = runCatching {
        if (!hasStrictJsonGrammar(candidate)) return@runCatching false
        val root = JSONObject(candidate)
        if (selfTest) isSelfTestTerminal(root) else isBaseTerminal(root)
    }.getOrDefault(false)

    fun isCompleteAvailableReport(root: JSONObject): Boolean {
        if (root.optString("status") != "available") return false
        if (!requiredBaseFields.all(root::has)) return false
        if (!isString(root, "renderer") || !isString(root, "vendor") || !isString(root, "glVersion") ||
            !isNumber(root, "glMajor") || !isNumber(root, "glMinor") || !isString(root, "glslVersion")) return false
        if (!isEglCapabilityArray(root.opt("eglCapabilities")) || !isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS) || !isLimitArray(root.opt("limits")) ||
            !isBoundedStringArray(root.opt("compressedFormats"), MAX_ENUMERATION_ITEMS) || !isInternalFormatArray(root.opt("internalFormats")) ||
            !isBoundedStringArray(root.opt("shaderBinaryFormats"), MAX_ENUMERATION_ITEMS) || !isBoundedStringArray(root.opt("programBinaryFormats"), MAX_ENUMERATION_ITEMS) || !isPrecisionArray(root.opt("precision")) ||
            !isEglConfigArray(root.opt("eglConfigs")) || !isDiagnosticArray(root.opt("diagnostics"))) return false

        val egl = root.optJSONObject("egl") ?: return false
        if (!requiredEglFields.all(egl::has)) return false
        if (!isString(egl, "vendor") || !isString(egl, "version") || !isString(egl, "initializedVersion") ||
            !isString(egl, "clientApis") || !isUniqueStringArray(egl.opt("extensions"), MAX_ENUMERATION_ITEMS) ||
            !isUniqueStringArray(egl.opt("clientExtensions"), MAX_ENUMERATION_ITEMS)) return false

        val glRuntime = root.optJSONObject("glRuntime") ?: return false
        if (!requiredGlRuntimeFields.all(glRuntime::has)) return false
        if (!isNullableString(glRuntime, "contextFlags") || !isNullableString(glRuntime, "resetNotificationStrategy") ||
            !isNullableString(glRuntime, "resetNotificationStrategyQuery") || !isNullableBoolean(glRuntime, "robustAccess") ||
            !isNullableString(glRuntime, "robustAccessQuery") || !isUnavailableAttributeArray(glRuntime.opt("unavailableAttributes"))) return false

        val runtime = root.optJSONObject("eglRuntime") ?: return false
        if (!requiredEglRuntimeFields.all(runtime::has)) return false
        if (!isString(runtime, "boundApi") || !isNullableNumber(runtime, "configId") ||
            !isNullableString(runtime, "clientType") || !isNullableNumber(runtime, "clientVersion") ||
            !isNullableString(runtime, "renderBuffer") || !isBoolean(runtime, "currentContext") ||
            !isBoolean(runtime, "currentDisplay") || !isBoolean(runtime, "currentDrawSurface") ||
            !isBoolean(runtime, "currentReadSurface") || !isNullableString(runtime, "surfaceGlColorspace") || !isNullableString(runtime, "surfaceGlColorspaceQuery") ||
            !isNullableString(runtime, "surfaceVgAlphaFormat") || !isNullableString(runtime, "surfaceVgAlphaFormatQuery") || !isNullableString(runtime, "surfaceVgColorspace") || !isNullableString(runtime, "surfaceVgColorspaceQuery") ||
            !isNullableNumber(runtime, "surfaceConfigId") || !isNullableNumber(runtime, "surfaceWidth") ||
            !isNullableNumber(runtime, "surfaceHeight") || !isNullableNumber(runtime, "surfaceHorizontalResolution") ||
            !isNullableBoolean(runtime, "surfaceLargestPbuffer") || !isNullableNumber(runtime, "surfacePixelAspectRatio") ||
            !isNullableNumber(runtime, "surfaceVerticalResolution") || !isNullableString(runtime, "surfaceRenderBuffer") ||
            !isNullableString(runtime, "surfaceSwapBehavior") || !isNullableString(runtime, "surfaceTextureFormat") ||
            !isNullableString(runtime, "surfaceTextureTarget") || !isNullableBoolean(runtime, "surfaceMipmapTexture") ||
            !isNullableNumber(runtime, "surfaceMipmapLevel") || !isNullableString(runtime, "surfaceMultisampleResolve") ||
            !isUnavailableAttributeArray(runtime.opt("unavailableAttributes"))) return false
        return true
    }

    fun terminalValidationIssue(candidate: String, selfTest: Boolean): String? {
        if (!hasStrictJsonGrammar(candidate)) return "strict JSON grammar or duplicate object key"
        val root = runCatching { JSONObject(candidate) }.getOrNull() ?: return "invalid JSON object"
        if (isTerminalJson(candidate, selfTest)) return null
        if (selfTest) return "self-test terminal fields or status"
        if (root.optString("status") != "available") return "invalid terminal status or unavailable reason"
        val missing = requiredBaseFields.filterNot(root::has)
        if (missing.isNotEmpty()) return "missing report fields: ${missing.joinToString(", ")}"
        val arrays = listOf(
            "extensions" to isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS),
            "limits" to isLimitArray(root.opt("limits")),
            "compressedFormats" to isBoundedStringArray(root.opt("compressedFormats"), MAX_ENUMERATION_ITEMS),
            "internalFormats" to isInternalFormatArray(root.opt("internalFormats")),
            "shaderBinaryFormats" to isBoundedStringArray(root.opt("shaderBinaryFormats"), MAX_ENUMERATION_ITEMS),
            "programBinaryFormats" to isBoundedStringArray(root.opt("programBinaryFormats"), MAX_ENUMERATION_ITEMS),
            "precision" to isPrecisionArray(root.opt("precision")),
            "eglConfigs" to isEglConfigArray(root.opt("eglConfigs")),
            "diagnostics" to isDiagnosticArray(root.opt("diagnostics")),
            "eglCapabilities" to isEglCapabilityArray(root.opt("eglCapabilities"))
        )
        val failedArray = arrays.firstOrNull { !it.second }
        if (failedArray != null) return "invalid ${failedArray.first} evidence: type, bound or duplicate identity"
        val egl = root.optJSONObject("egl")
        if (egl == null || !requiredEglFields.all(egl::has)) return "missing EGL identity fields"
        if (!isUniqueStringArray(egl.opt("extensions"), MAX_ENUMERATION_ITEMS) ||
            !isUniqueStringArray(egl.opt("clientExtensions"), MAX_ENUMERATION_ITEMS)) return "invalid EGL extension evidence"
        val glRuntime = root.optJSONObject("glRuntime")
        if (glRuntime == null || !requiredGlRuntimeFields.all(glRuntime::has)) return "missing GL runtime fields"
        val eglRuntime = root.optJSONObject("eglRuntime")
        if (eglRuntime == null || !requiredEglRuntimeFields.all(eglRuntime::has)) return "missing EGL runtime fields"
        return "identity or GL/EGL runtime value type mismatch"
    }

    private fun isBaseTerminal(root: JSONObject): Boolean = when (root.optString("status")) {
        "available" -> isCompleteAvailableReport(root)
        "unavailable" -> isString(root, "reason")
        else -> false
    }

    private fun isSelfTestTerminal(root: JSONObject): Boolean = when (root.optString("status")) {
        "completed", "completed_with_failures" ->
            isString(root, "vendor") && isString(root, "renderer") && isString(root, "runtimeVersion") &&
                isSelfTestArray(root.opt("tests"))
        "unavailable" -> isString(root, "reason")
        else -> false
    }

    private fun hasStrictJsonGrammar(candidate: String): Boolean = runCatching {
        JsonReader(StringReader(candidate)).use { reader ->
            reader.isLenient = false
            if (!consumeStrictValue(reader, 0)) return@use false
            reader.peek() == JsonToken.END_DOCUMENT
        }
    }.getOrDefault(false)

    private fun consumeStrictValue(reader: JsonReader, depth: Int): Boolean {
        if (depth > MAX_JSON_DEPTH) return false
        return when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                val names = HashSet<String>()
                var count = 0
                while (reader.hasNext()) {
                    if (++count > MAX_JSON_CONTAINER_ITEMS) return false
                    val name = reader.nextName()
                    if (!names.add(name) || !consumeStrictValue(reader, depth + 1)) return false
                }
                reader.endObject()
                true
            }
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                var count = 0
                while (reader.hasNext()) {
                    if (++count > MAX_JSON_CONTAINER_ITEMS || !consumeStrictValue(reader, depth + 1)) return false
                }
                reader.endArray()
                true
            }
            JsonToken.STRING -> { reader.nextString(); true }
            JsonToken.NUMBER -> { reader.nextString(); true }
            JsonToken.BOOLEAN -> { reader.nextBoolean(); true }
            JsonToken.NULL -> { reader.nextNull(); true }
            else -> false
        }
    }

    private fun isString(root: JSONObject, key: String): Boolean = root.has(key) && root.opt(key) is String
    private fun isNumber(root: JSONObject, key: String): Boolean = root.has(key) && root.opt(key) is Number
    private fun isBoolean(root: JSONObject, key: String): Boolean = root.has(key) && root.opt(key) is Boolean
    private fun isNullableString(root: JSONObject, key: String): Boolean = root.has(key) && (root.isNull(key) || root.opt(key) is String)
    private fun isNullableNumber(root: JSONObject, key: String): Boolean = root.has(key) && (root.isNull(key) || root.opt(key) is Number)
    private fun isNullableBoolean(root: JSONObject, key: String): Boolean = root.has(key) && (root.isNull(key) || root.opt(key) is Boolean)

    private fun isBoundedStringArray(value: Any?, maxItems: Int): Boolean {
        val array = value as? JSONArray ?: return false
        if (array.length() > maxItems) return false
        for (index in 0 until array.length()) {
            if (array.opt(index) !is String) return false
        }
        return true
    }

    private fun isUniqueStringArray(value: Any?, maxItems: Int): Boolean {
        val array = value as? JSONArray ?: return false
        if (array.length() > maxItems) return false
        val seen = HashSet<String>(array.length())
        for (index in 0 until array.length()) {
            val item = array.opt(index) as? String ?: return false
            if (!seen.add(item)) return false
        }
        return true
    }

    private fun isLimitArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_LIMITS, { it.optString("name") }) { item ->
        isString(item, "name") && isString(item, "value")
    }

    private fun isEglCapabilityArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_EGL_CAPABILITIES, { it.optString("name") }) { item ->
        isString(item, "name") && isString(item, "status") && item.optString("status") in terminalStates &&
            isString(item, "value") && isString(item, "detail")
    }

    private fun isInternalFormatArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_INTERNAL_FORMATS, { "${it.optString("target")}/${it.optString("internalFormat")}" }) { item ->
        if (!isString(item, "target") || !isString(item, "internalFormat") || !isString(item, "status") || item.optString("status") !in terminalStates || !isString(item, "detail")) return@uniqueObjectArray false
        val sampleCounts = item.opt("sampleCounts") as? JSONArray ?: return@uniqueObjectArray false
        if (sampleCounts.length() > 64) return@uniqueObjectArray false
        val sampleSeen = HashSet<Int>()
        for (index in 0 until sampleCounts.length()) {
            val sample = sampleCounts.opt(index)
            if (sample !is Number || sample.toInt() <= 0 || !sampleSeen.add(sample.toInt())) return@uniqueObjectArray false
        }
        val nvOk = uniqueObjectArray(item.opt("nvSampleProperties"), 64, { it.optInt("samples").toString() }) { nv ->
            isNumber(nv, "samples") && nv.optInt("samples") > 0 && sampleSeen.contains(nv.optInt("samples")) &&
                isNumber(nv, "multisamples") && nv.optInt("multisamples") >= 0 && isNumber(nv, "supersampleScaleX") && nv.optInt("supersampleScaleX") >= 0 &&
                isNumber(nv, "supersampleScaleY") && nv.optInt("supersampleScaleY") >= 0 && isBoolean(nv, "conformant")
        }
        nvOk && (item.optString("status") == "Available" || (sampleCounts.length() == 0 && (item.optJSONArray("nvSampleProperties")?.length() ?: -1) == 0))
    }

    private fun isPrecisionArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_PRECISION, { "${it.optString("shader")}/${it.optString("type")}" }) { item ->
        isString(item, "shader") && isString(item, "type") && isNumber(item, "rangeMin") &&
            isNumber(item, "rangeMax") && isNumber(item, "precision")
    }

    private fun isDiagnosticArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_DIAGNOSTICS, { it.optString("name") }) { item ->
        isString(item, "name") && isString(item, "status") && item.optString("status") in terminalStates && isString(item, "detail")
    }

    private fun isUnavailableAttributeArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_EGL_FAILURES, { it.optString("name") }) { item ->
        isString(item, "name") && isString(item, "error")
    }

    private fun isSelfTestArray(value: Any?): Boolean = objectArray(value) { item ->
        isString(item, "name") && isString(item, "status") && isString(item, "detail")
    }

    private fun isEglConfigArray(value: Any?): Boolean = uniqueObjectArray(value, MAX_EGL_CONFIGS, { it.opt("id").toString() }) { item ->
        requiredEglConfigFields.all(item::has) && isNumber(item, "id") &&
            listOf("red", "green", "blue", "alpha", "depth", "stencil", "sampleBuffers", "samples", "level",
                "nativeRenderable", "nativeVisualId", "minSwapInterval", "maxSwapInterval", "bufferSize", "luminanceSize",
                "alphaMaskSize", "bindToTextureRgb", "bindToTextureRgba", "maxPbufferWidth", "maxPbufferHeight",
                "maxPbufferPixels", "nativeVisualType", "transparentRed", "transparentGreen", "transparentBlue",
                "recordableAndroid", "framebufferTargetAndroid").all { isNullableNumber(item, it) } &&
            listOf("surfaceType", "renderableType", "conformant", "configCaveat", "colorBufferType", "transparentType",
                "colorComponentTypeExt").all { isNullableString(item, it) } &&
            isUnavailableAttributeArray(item.opt("unavailableAttributes"))
    }

    private inline fun uniqueObjectArray(value: Any?, maxItems: Int, key: (JSONObject) -> String, predicate: (JSONObject) -> Boolean): Boolean {
        val array = value as? JSONArray ?: return false
        if (array.length() > maxItems) return false
        val seen = HashSet<String>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: return false
            if (!predicate(item) || !seen.add(key(item))) return false
        }
        return true
    }

    private inline fun objectArray(value: Any?, predicate: (JSONObject) -> Boolean): Boolean {
        val array = value as? JSONArray ?: return false
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: return false
            if (!predicate(item)) return false
        }
        return true
    }
}
