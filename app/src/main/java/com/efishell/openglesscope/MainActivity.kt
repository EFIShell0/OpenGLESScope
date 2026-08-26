package com.efishell.openglesscope

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.Intent
import android.hardware.display.DisplayManager
import android.content.pm.PackageManager
import android.provider.Settings
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import okhttp3.Call
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

private const val DATABASE_API = "https://openglesscope-database-api.openglesscope.workers.dev"
private const val DATABASE_WEB = "https://efishell0.github.io/OpenGLESScope_database/"
private const val RELEASES_API = "https://api.github.com/repos/EFIShell0/OpenGLESScope/releases?per_page=20"
private const val REPOSITORY_WEB = "https://github.com/EFIShell0/OpenGLESScope"
private const val DEVELOPER_WEB = "https://github.com/EFIShell0"
private val Brand = ComposeColor(0xFFBA2A8D)
private val BrandSoft = ComposeColor(0xFFF06BC7)
private val SurfaceDark = ComposeColor(0xFF120B10)
private val SurfaceRaised = ComposeColor(0xFF1D111A)
private val Muted = ComposeColor(0xFFA4A7AF)
private val TextPrimary = ComposeColor(0xFFF7F2F3)
private val TextSecondary = ComposeColor(0xFFB6ACAE)
private val TextMuted = ComposeColor(0xFF968D8F)
private val SurfaceLow = ComposeColor(0xFF120B10)
private val SurfaceTonal = ComposeColor(0xFF21161E)
private val BrandContainer = ComposeColor(0xFF3A1831)
private val Outline = ComposeColor(0xFF51434D)
private val OutlineVariant = ComposeColor(0xFF30272E)
private val OpenGLESExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

internal data class LimitEntry(val name: String, val value: String)
internal data class QueryDiagnostic(val name: String, val status: String, val detail: String)
internal data class PrecisionEntry(val shader: String, val type: String, val rangeMin: Int, val rangeMax: Int, val precision: Int)
internal data class EglUnavailableAttribute(val name: String, val error: String)
internal data class EglRuntimeInfo(
    val boundApi: String, val configId: Int?, val clientType: String?, val clientVersion: Int?, val renderBuffer: String?,
    val currentContext: Boolean, val currentDisplay: Boolean, val currentDrawSurface: Boolean, val currentReadSurface: Boolean,
    val surfaceWidth: Int?, val surfaceHeight: Int?, val surfaceRenderBuffer: String?, val surfaceSwapBehavior: String?,
    val surfaceTextureFormat: String?, val surfaceTextureTarget: String?, val surfaceMipmapTexture: Int?, val surfaceMipmapLevel: Int?,
    val surfaceMultisampleResolve: String?, val unavailableAttributes: List<EglUnavailableAttribute>
)
internal data class EglConfigEntry(
    val id: Int,
    val red: Int?, val green: Int?, val blue: Int?, val alpha: Int?,
    val depth: Int?, val stencil: Int?, val sampleBuffers: Int?, val samples: Int?,
    val surfaceType: String?, val renderableType: String?, val conformant: String?,
    val configCaveat: String?, val colorBufferType: String?, val level: Int?,
    val nativeRenderable: Int?, val nativeVisualId: Int?, val minSwapInterval: Int?, val maxSwapInterval: Int?,
    val bufferSize: Int?, val luminanceSize: Int?, val alphaMaskSize: Int?,
    val bindToTextureRgb: Int?, val bindToTextureRgba: Int?,
    val maxPbufferWidth: Int?, val maxPbufferHeight: Int?, val maxPbufferPixels: Int?,
    val nativeVisualType: Int?, val transparentType: String?,
    val transparentRed: Int?, val transparentGreen: Int?, val transparentBlue: Int?,
    val recordableAndroid: Int?, val framebufferTargetAndroid: Int?, val colorComponentTypeExt: String?,
    val unavailableAttributes: List<EglUnavailableAttribute>
)
internal data class EglInfo(val vendor: String, val version: String, val initializedVersion: String, val clientApis: String, val extensions: List<String>, val clientExtensions: List<String>)
internal data class GlReport(
    val available: Boolean,
    val reason: String,
    val renderer: String,
    val vendor: String,
    val glVersion: String,
    val glMajor: Int,
    val glMinor: Int,
    val glslVersion: String,
    val egl: EglInfo,
    val eglRuntime: EglRuntimeInfo,
    val extensions: List<String>,
    val limits: List<LimitEntry>,
    val compressedFormats: List<String>,
    val shaderBinaryFormats: List<String>,
    val programBinaryFormats: List<String>,
    val precision: List<PrecisionEntry>,
    val eglConfigs: List<EglConfigEntry>,
    val diagnostics: List<QueryDiagnostic>
)
internal data class DisplayInfo(val name: String, val modeId: Int?, val width: Int?, val height: Int?, val refreshRate: Float?, val supportedModes: List<String>, val hdrTypes: List<String>, val desiredMaxLuminance: Float?, val desiredMaxAverageLuminance: Float?, val desiredMinLuminance: Float?, val wideColor: Boolean?)
private enum class EvidenceState { Supported, Unsupported, Unknown }
private enum class Page(val title: String) {
    Overview("Overview"), OpenGLES("OpenGL ES"), Display("Display & HDR"), EGL("EGL"), Features("Features"), Limits("Limits"), Formats("Formats"), Extensions("Extensions"), Precision("Precision"), Configs("EGL Configs"), Analysis("Analysis"), Settings("Settings"), Info("Info")
}

internal data class AppUpdate(
    val version: String,
    val assetName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val installedAbi: String,
    val downloadAbi: String,
    val installedVersion: String,
    val installedVersionCode: Long
)
internal sealed interface UpdateStatus {
    data object Hidden : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data object DirectUpdatesDisabledIntro : UpdateStatus
    data class Available(val update: AppUpdate) : UpdateStatus
    data class Downloading(val update: AppUpdate) : UpdateStatus
    data class Failed(val message: String) : UpdateStatus
}

class MainActivity : ComponentActivity() {
    internal var updateStatus by mutableStateOf<UpdateStatus>(UpdateStatus.Hidden)
    internal var updateConfirmation by mutableStateOf<AppUpdate?>(null)
    private var updateCheckJob: Job? = null
    private var updateDownloadJob: Job? = null
    @Volatile private var activeUpdateCheckCall: Call? = null
    @Volatile private var activeUpdateDownloadCall: Call? = null
    private var pendingUpdateApk: File? = null
    private lateinit var prefs: android.content.SharedPreferences
    internal var directUpdatesEnabled by mutableStateOf(true)
    internal var directUpdatesConsentVisible by mutableStateOf(false)


    override fun onResume() {
        super.onResume()
        val pending = pendingUpdateApk
        if (pending != null && pending.exists() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls())) {
            pendingUpdateApk = null
            launchPackageInstaller(pending)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = android.graphics.Color.rgb(17, 17, 17)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        directUpdatesEnabled = prefs.getBoolean("direct_updates_enabled", true)
        setContent { OpenGLESScopeApp(this) }
        if (directUpdatesEnabled) {
            checkForApplicationUpdate(false)
        } else {
            showDirectUpdatesDisabledIntroIfFirstInstall()
        }
    }


    private fun showDirectUpdatesDisabledIntroIfFirstInstall() {
        if (prefs.getBoolean("direct_updates_intro_seen", false)) return
        val packageInfo = runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull() ?: return
        prefs.edit().putBoolean("direct_updates_intro_seen", true).apply()
        if (packageInfo.firstInstallTime != packageInfo.lastUpdateTime) return
        updateStatus = UpdateStatus.DirectUpdatesDisabledIntro
        lifecycleScope.launch {
            kotlinx.coroutines.delay(7_000L)
            if (updateStatus is UpdateStatus.DirectUpdatesDisabledIntro) updateStatus = UpdateStatus.Hidden
        }
    }

    internal fun requestDirectUpdatesChanged(enabled: Boolean) {
        if (enabled) {
            directUpdatesConsentVisible = true
        } else {
            directUpdatesEnabled = false
            directUpdatesConsentVisible = false
            prefs.edit().putBoolean("direct_updates_enabled", false).apply()
            updateCheckJob?.cancel()
            updateDownloadJob?.cancel()
            activeUpdateCheckCall?.cancel()
            activeUpdateDownloadCall?.cancel()
            updateCheckJob = null
            updateDownloadJob = null
            pendingUpdateApk?.let { runCatching { it.delete() } }
            pendingUpdateApk = null
            updateStatus = UpdateStatus.Hidden
            updateConfirmation = null
        }
    }

    internal fun confirmDirectUpdatesConsent() {
        directUpdatesEnabled = true
        directUpdatesConsentVisible = false
        if (updateStatus is UpdateStatus.DirectUpdatesDisabledIntro) updateStatus = UpdateStatus.Hidden
        prefs.edit().putBoolean("direct_updates_enabled", true).apply()
    }

    private fun stopOpenGlesProbeProcess() {
        runCatching { startService(Intent(this, OpenGLESProbeService::class.java).setAction(OpenGLESProbeService.ACTION_ABORT)) }
        runCatching { stopService(Intent(this, OpenGLESProbeService::class.java)) }
        runCatching {
            val manager = getSystemService(android.app.ActivityManager::class.java) ?: return@runCatching
            val expectedName = "${packageName}:opengles_probe"
            manager.runningAppProcesses.orEmpty()
                .filter { it.uid == android.os.Process.myUid() && it.processName == expectedName }
                .forEach { android.os.Process.killProcess(it.pid) }
        }
    }

    internal suspend fun collectOpenGlesReport(): String = withContext(Dispatchers.IO) {
        val probeDir = File(cacheDir, "probe").canonicalFile.apply { mkdirs() }
        val resultFile = File(probeDir, "opengles-${System.nanoTime()}.json").canonicalFile
        if (resultFile.parentFile != probeDir) return@withContext JSONObject().put("status", "unavailable").put("reason", "Probe result path validation failed").toString()
        runCatching { resultFile.delete() }
        val intent = Intent(this@MainActivity, OpenGLESProbeService::class.java).putExtra(OpenGLESProbeService.EXTRA_RESULT_PATH, resultFile.absolutePath)
        val started = runCatching { startService(intent) }.isSuccess
        if (!started) return@withContext JSONObject().put("status", "unavailable").put("reason", "OpenGL ES probe service could not be started").toString()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
        try {
            while (System.nanoTime() < deadline) {
                if (resultFile.isFile && resultFile.length() > 0L) {
                    val length = resultFile.length()
                    if (length > 8L * 1024L * 1024L) {
                        stopOpenGlesProbeProcess()
                        return@withContext JSONObject().put("status", "unavailable").put("reason", "OpenGL ES probe result exceeded the 8 MiB safety limit").toString()
                    }
                    val text = resultFile.readText(Charsets.UTF_8)
                    if (text.isNotBlank()) return@withContext text
                }
                delay(50L)
            }
            stopOpenGlesProbeProcess()
            JSONObject().put("status", "unavailable").put("reason", "OpenGL ES probe did not complete within 20 seconds").toString()
        } finally {
            runCatching { stopService(intent) }
            runCatching { resultFile.delete() }
        }
    }

    internal fun checkForApplicationUpdate(showProgress: Boolean) {
        if (!directUpdatesEnabled) {
            if (showProgress) updateStatus = UpdateStatus.Failed("Direct GitHub updates are disabled. Use Obtainium for external update management or enable them in Settings.")
            return
        }
        if (updateCheckJob?.isActive == true || updateStatus is UpdateStatus.Downloading) return
        updateCheckJob = lifecycleScope.launch {
            if (showProgress) updateStatus = UpdateStatus.Checking
            val result = withContext(Dispatchers.IO) { fetchLatestCompatibleUpdateResult() }
            if (!directUpdatesEnabled) {
                updateStatus = UpdateStatus.Hidden
                updateConfirmation = null
                return@launch
            }
            updateStatus = when (result) {
                is UpdateCheckResult.Available -> UpdateStatus.Available(result.update)
                UpdateCheckResult.UpToDate -> if (showProgress) UpdateStatus.UpToDate else UpdateStatus.Hidden
                is UpdateCheckResult.Failed -> if (showProgress) UpdateStatus.Failed(result.message) else UpdateStatus.Hidden
            }
            if (showProgress && result is UpdateCheckResult.Available) updateConfirmation = result.update
            if (updateStatus !is UpdateStatus.Hidden && updateStatus !is UpdateStatus.Downloading) {
                val displayDurationMillis = if (updateStatus is UpdateStatus.UpToDate) 8_000L else 10_000L
                delay(displayDurationMillis)
                if (updateStatus !is UpdateStatus.Downloading) updateStatus = UpdateStatus.Hidden
            }
        }
    }

    private sealed interface UpdateCheckResult {
        data class Available(val update: AppUpdate) : UpdateCheckResult
        data object UpToDate : UpdateCheckResult
        data class Failed(val message: String) : UpdateCheckResult
    }

    private fun fetchLatestCompatibleUpdateResult(): UpdateCheckResult = try {
        val update = fetchLatestCompatibleUpdate()
        if (update != null) UpdateCheckResult.Available(update) else UpdateCheckResult.UpToDate
    } catch (error: Exception) {
        UpdateCheckResult.Failed(error.message ?: "Update check failed.")
    }

    private fun fetchLatestCompatibleUpdate(): AppUpdate? {
        val request = Request.Builder()
            .url(RELEASES_API)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "OpenGLESScope/${installedVersionName()}")
            .get()
            .build()
        val call = HTTP_CLIENT.newCall(request)
        activeUpdateCheckCall = call
        return try {
            call.execute().use { response ->
                if (!response.isSuccessful) error("Update check failed (HTTP ${response.code}).")
            val releases = JSONArray(readResponseTextLimited(response.body, 2 * 1024 * 1024))
            val current = installedVersionName()
            val candidates = (0 until releases.length())
                .mapNotNull { releases.optJSONObject(it) }
                .filter { !it.optBoolean("draft", false) }
                .mapNotNull { release ->
                    val version = release.optString("tag_name").trim().removePrefix("v")
                    if (version.isBlank() || !version.matches(Regex("\\d+(?:\\.\\d+){1,3}(?:[-+][0-9A-Za-z.-]+)?"))) null else release to version
                }
                .sortedWith { a, b -> -compareVersions(a.second, b.second) }
            val candidate = candidates.firstOrNull { isNewerVersion(it.second, current) } ?: return null
            val json = candidate.first
            val latest = candidate.second
            val assets = json.optJSONArray("assets") ?: error("A newer OpenGLESScope release exists, but its asset list is unavailable.")
            val apkAssets = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }.filter { it.optString("name").endsWith(".apk", true) }
            val abi = detectInstalledAbi(this)
            val abiTokens = when (abi) {
                "arm64-v8a" -> listOf("arm64-v8a", "arm64_v8a", "arm64")
                "armeabi-v7a" -> listOf("armeabi-v7a", "armeabi_v7a", "armv7")
                "x86_64" -> listOf("x86_64", "x86-64")
                else -> listOf(abi.lowercase())
            }
            val exact = apkAssets.firstOrNull { asset -> abiTokens.any { asset.optString("name").lowercase().contains(it) } }
            val universal = apkAssets.firstOrNull { it.optString("name").lowercase().contains("universal") }
            val selected = exact ?: universal ?: error("A newer OpenGLESScope release exists, but it has no APK compatible with the installed ABI ($abi).")
            val url = selected.optString("browser_download_url")
            val parsedUrl = url.toHttpUrlOrNull()
            if (parsedUrl == null || parsedUrl.scheme != "https" || parsedUrl.host != "github.com" || parsedUrl.username.isNotEmpty() || parsedUrl.password.isNotEmpty() || parsedUrl.query != null || parsedUrl.fragment != null || !parsedUrl.encodedPath.startsWith("/EFIShell0/OpenGLESScope/releases/download/")) error("The release APK URL is not an official OpenGLESScope GitHub release asset.")
                AppUpdate(latest, selected.optString("name"), url, json.optString("body").trim().ifBlank { "No release notes were provided for this GitHub release." }, abi, if (exact != null) abi else "universal", current, installedVersionCode())
            }
        } finally {
            if (activeUpdateCheckCall === call) activeUpdateCheckCall = null
        }
    }

    private fun installedVersionName(): String = runCatching { packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0" }.getOrDefault("0.0.0")
    private fun installedVersionCode(): Long = runCatching {
        val info = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else {
            @Suppress("DEPRECATION")
            val legacy = info.versionCode.toLong()
            legacy
        }
    }.getOrDefault(0L)
    private fun isNewerVersion(candidate: String, current: String): Boolean = compareVersions(candidate, current) > 0

    internal fun downloadAndInstallUpdate(update: AppUpdate) {
        if (!directUpdatesEnabled || updateStatus is UpdateStatus.Downloading) return
        updateStatus = UpdateStatus.Downloading(update)
        updateDownloadJob = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { downloadUpdateApk(update) }
            result.onSuccess { apk ->
                if (!directUpdatesEnabled) {
                    runCatching { apk.delete() }
                    updateStatus = UpdateStatus.Hidden
                } else {
                    updateStatus = UpdateStatus.Hidden
                    requestPackageInstall(apk)
                }
            }.onFailure { error ->
                if (directUpdatesEnabled) {
                    updateStatus = UpdateStatus.Failed(error.message ?: "Update download failed.")
                    kotlinx.coroutines.delay(10_000)
                    if (updateStatus is UpdateStatus.Failed) updateStatus = UpdateStatus.Hidden
                } else {
                    updateStatus = UpdateStatus.Hidden
                }
            }
            updateDownloadJob = null
        }
    }

    private fun downloadUpdateApk(update: AppUpdate): Result<File> = runCatching {
        val safeAssetName = update.assetName.substringAfterLast('/').substringAfterLast('\\').takeIf { it.endsWith(".apk", true) && it.length in 5..160 } ?: error("The release asset has an invalid APK filename.")
        val updateDir = File(cacheDir, "updates").apply { mkdirs() }
        val target = File(updateDir, safeAssetName)
        if (target.parentFile?.canonicalFile != updateDir.canonicalFile) error("The release asset path is invalid.")
        val temp = File(updateDir, "$safeAssetName.part")
        try {
            val request = Request.Builder().url(update.downloadUrl).header("User-Agent", "OpenGLESScope/${installedVersionName()}").get().build()
            val call = UPDATE_DOWNLOAD_CLIENT.newCall(request)
            activeUpdateDownloadCall = call
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) error("Update download failed (HTTP ${response.code}).")
                val body = response.body
                if (body.contentLength() > 256L * 1024L * 1024L) error("Update package exceeds the safety limit.")
                body.byteStream().use { input -> FileOutputStream(temp).use { output ->
                    val buffer = ByteArray(64 * 1024); var total = 0L
                    while (true) { val count = input.read(buffer); if (count < 0) break; total += count; if (total > 256L * 1024L * 1024L) error("Update package exceeds the safety limit."); output.write(buffer, 0, count) }
                    output.fd.sync()
                } }
                }
            } finally {
                if (activeUpdateDownloadCall === call) activeUpdateDownloadCall = null
            }
            if (!temp.renameTo(target)) { temp.copyTo(target, overwrite = true); temp.delete() }
            val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else {
                @Suppress("DEPRECATION")
                val legacy = PackageManager.GET_SIGNATURES
                legacy
            }
            try {
                val archive = packageManager.getPackageArchiveInfo(target.absolutePath, flags) ?: error("Downloaded file is not a valid Android package.")
                if (archive.packageName != packageName) error("Downloaded package identity does not match OpenGLESScope.")
                val installed = packageManager.getPackageInfo(packageName, flags)
                if (!packageSigningCertificatesMatch(installed, archive)) error("Downloaded package signing certificate does not match the installed OpenGLESScope build.")
                val archiveCode = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else {
                    @Suppress("DEPRECATION")
                    val legacy = archive.versionCode.toLong()
                    legacy
                }
                val installedCode = if (Build.VERSION.SDK_INT >= 28) installed.longVersionCode else {
                    @Suppress("DEPRECATION")
                    val legacy = installed.versionCode.toLong()
                    legacy
                }
                if (archiveCode <= installedCode) error("Downloaded package versionCode is not newer than the installed OpenGLESScope build.")
                val archiveVersion = archive.versionName ?: error("Downloaded package has no version metadata.")
                if (!isNewerVersion(archiveVersion, installedVersionName())) error("Downloaded package versionName is not newer than the installed OpenGLESScope version.")
                target
            } catch (error: Throwable) {
                runCatching { target.delete() }
                throw error
            }
        } finally { if (temp.exists()) temp.delete() }
    }

    private fun packageSigningCertificatesMatch(installed: android.content.pm.PackageInfo, archive: android.content.pm.PackageInfo): Boolean {
        fun encoded(signatures: Array<android.content.pm.Signature>): Set<String> = signatures.map { Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP) }.toSet()
        if (Build.VERSION.SDK_INT >= 28) {
            val installedInfo = installed.signingInfo ?: return false
            val archiveInfo = archive.signingInfo ?: return false
            val installedCurrent = encoded(installedInfo.apkContentsSigners)
            if (installedCurrent.isEmpty()) return false
            if (installedInfo.hasMultipleSigners() || archiveInfo.hasMultipleSigners()) {
                val archiveCurrent = encoded(archiveInfo.apkContentsSigners)
                return archiveCurrent.isNotEmpty() && installedCurrent == archiveCurrent
            }
            val archiveHistory = encoded(archiveInfo.signingCertificateHistory)
            return archiveHistory.isNotEmpty() && archiveHistory.containsAll(installedCurrent)
        }
        @Suppress("DEPRECATION")
        val installedLegacy = encoded(installed.signatures ?: emptyArray())
        @Suppress("DEPRECATION")
        val archiveLegacy = encoded(archive.signatures ?: emptyArray())
        return installedLegacy.isNotEmpty() && installedLegacy == archiveLegacy
    }

    private fun requestPackageInstall(apk: File) {
        if (Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) { pendingUpdateApk = apk; startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))); return }
        launchPackageInstaller(apk)
    }

    private fun launchPackageInstaller(apk: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", apk)
        startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/vnd.android.package-archive"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    override fun onDestroy() {
        activeUpdateCheckCall?.cancel()
        activeUpdateDownloadCall?.cancel()
        updateCheckJob?.cancel()
        updateDownloadJob?.cancel()
        super.onDestroy()
    }


    internal suspend fun runOpenGlesSelfTests(expected: GlReport): String = withContext(Dispatchers.IO) {
        val probeDir = File(cacheDir, "probe").canonicalFile.apply { mkdirs() }
        val resultFile = File(probeDir, "opengles-selftest-${System.nanoTime()}.json").canonicalFile
        if (resultFile.parentFile != probeDir) return@withContext JSONObject().put("status", "unavailable").put("reason", "Self-test result path validation failed").toString()
        runCatching { resultFile.delete() }
        val intent = Intent(this@MainActivity, OpenGLESProbeService::class.java)
            .putExtra(OpenGLESProbeService.EXTRA_RESULT_PATH, resultFile.absolutePath)
            .putExtra(OpenGLESProbeService.EXTRA_SELF_TEST, true)
        if (!runCatching { startService(intent) }.isSuccess) return@withContext JSONObject().put("status", "unavailable").put("reason", "OpenGL ES self-test service could not be started").toString()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
        try {
            while (System.nanoTime() < deadline) {
                if (resultFile.isFile && resultFile.length() > 0L) {
                    if (resultFile.length() > 1024L * 1024L) return@withContext JSONObject().put("status", "unavailable").put("reason", "Self-test result exceeded 1 MiB").toString()
                    val raw = resultFile.readText(Charsets.UTF_8)
                    val parsed = runCatching { JSONObject(raw) }.getOrNull() ?: return@withContext JSONObject().put("status", "unavailable").put("reason", "Self-test result JSON was invalid").toString()
                    if (parsed.optString("status").startsWith("completed")) {
                        val sameVendor = parsed.optString("vendor") == expected.vendor
                        val sameRenderer = parsed.optString("renderer") == expected.renderer
                        val sameVersion = parsed.optString("runtimeVersion") == expected.glVersion
                        if (!sameVendor || !sameRenderer || !sameVersion) {
                            return@withContext JSONObject()
                                .put("status", "unavailable")
                                .put("reason", "Isolated OpenGL ES runtime identity did not match the selected report; test attribution was refused")
                                .put("vendor", parsed.optString("vendor", "Unknown"))
                                .put("renderer", parsed.optString("renderer", "Unknown"))
                                .put("runtimeVersion", parsed.optString("runtimeVersion", "Unknown"))
                                .put("tests", JSONArray())
                                .toString()
                        }
                    }
                    return@withContext raw
                }
                delay(50L)
            }
            stopOpenGlesProbeProcess()
            JSONObject().put("status", "unavailable").put("reason", "OpenGL ES self-test timed out").toString()
        } finally {
            runCatching { resultFile.delete() }
        }
    }
}

private fun parseReport(raw: String): GlReport {
    return try {
        val o = JSONObject(raw)
        val eglObj = o.optJSONObject("egl") ?: JSONObject()
        val eglRuntimeObj = o.optJSONObject("eglRuntime") ?: JSONObject()
        val requiredTopLevel = listOf("renderer", "vendor", "glVersion", "glMajor", "glMinor", "glslVersion", "egl", "eglRuntime", "extensions", "limits", "compressedFormats", "shaderBinaryFormats", "programBinaryFormats", "precision", "eglConfigs", "diagnostics")
        val requiredEgl = listOf("vendor", "version", "initializedVersion", "clientApis", "extensions", "clientExtensions")
        val requiredEglRuntime = listOf("boundApi", "configId", "clientType", "clientVersion", "renderBuffer", "currentContext", "currentDisplay", "currentDrawSurface", "currentReadSurface", "surfaceWidth", "surfaceHeight", "surfaceRenderBuffer", "surfaceSwapBehavior", "surfaceTextureFormat", "surfaceTextureTarget", "surfaceMipmapTexture", "surfaceMipmapLevel", "surfaceMultisampleResolve", "unavailableAttributes")
        val completeSnapshot = requiredTopLevel.all { o.has(it) } && o.optJSONObject("egl") != null && o.optJSONObject("eglRuntime") != null && requiredEgl.all { eglObj.has(it) } && requiredEglRuntime.all { eglRuntimeObj.has(it) }
        val available = o.optString("status") == "available" && completeSnapshot
        val limits = o.optJSONArray("limits").toObjects().map { x -> LimitEntry(x.optString("name", "Unknown"), x.optString("value", "Unknown")) }
        val precision = o.optJSONArray("precision").toObjects().map { x -> PrecisionEntry(x.optString("shader"), x.optString("type"), x.optInt("rangeMin"), x.optInt("rangeMax"), x.optInt("precision")) }
        val diagnostics = o.optJSONArray("diagnostics").toObjects().map { x -> QueryDiagnostic(x.optString("name", "Unknown"), x.optString("status", "Unknown"), x.optString("detail", "")) }
        val configs = o.optJSONArray("eglConfigs").toObjects().map { x ->
            EglConfigEntry(
                x.optInt("id"), x.optNullableInt("red"), x.optNullableInt("green"), x.optNullableInt("blue"), x.optNullableInt("alpha"),
                x.optNullableInt("depth"), x.optNullableInt("stencil"), x.optNullableInt("sampleBuffers"), x.optNullableInt("samples"),
                x.optNullableString("surfaceType"), x.optNullableString("renderableType"), x.optNullableString("conformant"),
                x.optNullableString("configCaveat"), x.optNullableString("colorBufferType"), x.optNullableInt("level"),
                x.optNullableInt("nativeRenderable"), x.optNullableInt("nativeVisualId"), x.optNullableInt("minSwapInterval"), x.optNullableInt("maxSwapInterval"),
                x.optNullableInt("bufferSize"), x.optNullableInt("luminanceSize"), x.optNullableInt("alphaMaskSize"),
                x.optNullableInt("bindToTextureRgb"), x.optNullableInt("bindToTextureRgba"),
                x.optNullableInt("maxPbufferWidth"), x.optNullableInt("maxPbufferHeight"), x.optNullableInt("maxPbufferPixels"),
                x.optNullableInt("nativeVisualType"), x.optNullableString("transparentType"),
                x.optNullableInt("transparentRed"), x.optNullableInt("transparentGreen"), x.optNullableInt("transparentBlue"),
                x.optNullableInt("recordableAndroid"), x.optNullableInt("framebufferTargetAndroid"), x.optNullableString("colorComponentTypeExt"),
                x.optJSONArray("unavailableAttributes").toObjects().map { a -> EglUnavailableAttribute(a.optString("name", "Unknown"), a.optString("error", "Unknown EGL error")) }
            )
        }
        GlReport(
            available,
            if (!completeSnapshot && o.optString("status") == "available") "Capability snapshot was incomplete" else o.optString("reason"),
            o.optString("renderer", "Unknown"),
            o.optString("vendor", "Unknown"),
            o.optString("glVersion", "Unknown"),
            o.optInt("glMajor"),
            o.optInt("glMinor"),
            o.optString("glslVersion", "Unknown"),
            EglInfo(eglObj.optString("vendor", "Unknown"), eglObj.optString("version", "Unknown"), eglObj.optString("initializedVersion", "Unknown"), eglObj.optString("clientApis", "Unknown"), eglObj.optJSONArray("extensions").toStrings(), eglObj.optJSONArray("clientExtensions").toStrings()),
            EglRuntimeInfo(
                eglRuntimeObj.optString("boundApi", "Unknown"), eglRuntimeObj.optNullableInt("configId"), eglRuntimeObj.optNullableString("clientType"), eglRuntimeObj.optNullableInt("clientVersion"), eglRuntimeObj.optNullableString("renderBuffer"),
                eglRuntimeObj.optBoolean("currentContext", false), eglRuntimeObj.optBoolean("currentDisplay", false), eglRuntimeObj.optBoolean("currentDrawSurface", false), eglRuntimeObj.optBoolean("currentReadSurface", false),
                eglRuntimeObj.optNullableInt("surfaceWidth"), eglRuntimeObj.optNullableInt("surfaceHeight"), eglRuntimeObj.optNullableString("surfaceRenderBuffer"), eglRuntimeObj.optNullableString("surfaceSwapBehavior"),
                eglRuntimeObj.optNullableString("surfaceTextureFormat"), eglRuntimeObj.optNullableString("surfaceTextureTarget"), eglRuntimeObj.optNullableInt("surfaceMipmapTexture"), eglRuntimeObj.optNullableInt("surfaceMipmapLevel"), eglRuntimeObj.optNullableString("surfaceMultisampleResolve"),
                eglRuntimeObj.optJSONArray("unavailableAttributes").toObjects().map { a -> EglUnavailableAttribute(a.optString("name", "Unknown"), a.optString("error", "Unknown EGL error")) }
            ),
            o.optJSONArray("extensions").toStrings(),
            limits,
            o.optJSONArray("compressedFormats").toStrings(),
            o.optJSONArray("shaderBinaryFormats").toStrings(),
            o.optJSONArray("programBinaryFormats").toStrings(),
            precision,
            configs,
            diagnostics
        )
    } catch (e: Exception) {
        GlReport(false, e.message ?: "OpenGL ES report parsing failed", "Unknown", "Unknown", "Unknown", 0, 0, "Unknown", EglInfo("Unknown", "Unknown", "Unknown", "Unknown", emptyList(), emptyList()), EglRuntimeInfo("Unknown", null, null, null, null, false, false, false, false, null, null, null, null, null, null, null, null, null, emptyList()), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

private fun JSONArray?.toStrings(): List<String> = if (this == null) emptyList() else List(length()) { optString(it) }
private fun JSONArray?.toObjects(): List<JSONObject> = if (this == null) emptyList() else List(length()) { optJSONObject(it) ?: JSONObject() }
private fun JSONObject.optNullableInt(name: String): Int? = if (isNull(name) || !has(name)) null else optInt(name)
private fun JSONObject.optNullableString(name: String): String? = if (isNull(name) || !has(name)) null else optString(name).takeIf { it.isNotBlank() }
private fun safeFilePart(s: String): String = s.replace(Regex("[^A-Za-z0-9._-]+"), "_").take(80).ifBlank { "device" }
private fun coreVersionProvenance(r: GlReport): String {
    val directMajor = r.diagnostics.any { it.name == "GL_MAJOR_VERSION" && it.status == "Available" }
    val directMinor = r.diagnostics.any { it.name == "GL_MINOR_VERSION" && it.status == "Available" }
    return if (directMajor && directMinor) "Direct GL_MAJOR_VERSION / GL_MINOR_VERSION query" else "Parsed from GL_VERSION runtime string"
}

private fun displayInfo(activity: Activity): DisplayInfo {
    val d = if (Build.VERSION.SDK_INT >= 30) activity.display else @Suppress("DEPRECATION") activity.windowManager.defaultDisplay
    if (d == null) return DisplayInfo("Unavailable", null, null, null, null, emptyList(), emptyList(), null, null, null, null)
    val hdr = if (Build.VERSION.SDK_INT >= 24) d.hdrCapabilities else null
    val rawTypes = when {
        Build.VERSION.SDK_INT >= 34 -> d.mode.supportedHdrTypes.toList()
        Build.VERSION.SDK_INT >= 24 -> @Suppress("DEPRECATION") hdr?.supportedHdrTypes?.toList().orEmpty()
        else -> emptyList()
    }
    val types = rawTypes.filter { it != Display.HdrCapabilities.HDR_TYPE_INVALID }.distinct().map { hdrName(it) }
    val wide = if (Build.VERSION.SDK_INT >= 26) d.isWideColorGamut else null
    val invalid = if (Build.VERSION.SDK_INT >= 24) Display.HdrCapabilities.INVALID_LUMINANCE else -1f
    fun validLuminance(v: Float?): Float? = v?.takeIf { it != invalid && it >= 0f && it.isFinite() }
    val mode = d.mode
    val supportedModes = d.supportedModes.map { candidate -> "${candidate.physicalWidth}×${candidate.physicalHeight} @ ${String.format(java.util.Locale.US, "%.2f", candidate.refreshRate)} Hz" }.distinct()
    return DisplayInfo(d.name.ifBlank { "Unavailable" }, mode.modeId, mode.physicalWidth, mode.physicalHeight, mode.refreshRate, supportedModes, types, validLuminance(hdr?.desiredMaxLuminance), validLuminance(hdr?.desiredMaxAverageLuminance), validLuminance(hdr?.desiredMinLuminance), wide)
}

private fun hdrName(v: Int): String = when {
    v == Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
    v == Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
    v == Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
    Build.VERSION.SDK_INT >= 29 && v == Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
    Build.VERSION.SDK_INT >= 37 && v == Display.HdrCapabilities.HDR_TYPE_HLG_PLUS -> "HLG+"
    else -> "Android HDR type $v"
}

@Composable
private fun OpenGLESScopeApp(activity: MainActivity) {
    var report by remember { mutableStateOf<GlReport?>(null) }
    var collecting by remember { mutableStateOf(true) }
    var collectionCompleted by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(Page.Overview) }
    var display by remember { mutableStateOf(displayInfo(activity)) }
    DisposableEffect(activity) {
        val displayManager = activity.getSystemService(DisplayManager::class.java)
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) { if (activity.display?.displayId == displayId) display = displayInfo(activity) }
            override fun onDisplayRemoved(displayId: Int) { display = displayInfo(activity) }
            override fun onDisplayChanged(displayId: Int) { if (activity.display?.displayId == displayId) display = displayInfo(activity) }
        }
        displayManager?.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        onDispose { displayManager?.unregisterDisplayListener(listener) }
    }
    LaunchedEffect(Unit) {
        collecting = true
        collectionCompleted = false
        try {
            val raw = activity.collectOpenGlesReport()
            report = withContext(Dispatchers.Default) { parseReport(raw) }
        } finally {
            collecting = false
            collectionCompleted = true
        }
        delay(2200)
        collectionCompleted = false
    }
    MaterialExpressiveTheme(
        colorScheme = darkColorScheme(
            background = ComposeColor.Black,
            surface = ComposeColor(0xFF101010),
            surfaceVariant = SurfaceTonal,
            primary = Brand,
            onPrimary = TextPrimary,
            primaryContainer = BrandContainer,
            onPrimaryContainer = TextPrimary,
            secondary = BrandSoft,
            onSecondary = TextPrimary,
            secondaryContainer = ComposeColor(0xFF32202D),
            onSecondaryContainer = TextPrimary,
            tertiary = BrandSoft,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            outline = Outline,
            outlineVariant = OutlineVariant
        ),
        shapes = OpenGLESExpressiveShapes,
        motionScheme = MotionScheme.expressive()
    ) {
        BackHandler(enabled = page != Page.Overview) { page = Page.Overview }
        val configuration = LocalConfiguration.current
        val isTelevision = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val useRail = isLandscape || isTelevision
        Scaffold(
            containerColor = ComposeColor.Black,
            topBar = {
                Column {
                    AppHeader(page, onBack = { page = Page.Overview }, onSettings = { page = Page.Settings }, onInfo = { page = Page.Info })
                    CollectionStatusBanner(collecting, collectionCompleted)
                    UpdateStatusBanner(activity.updateStatus) { update -> activity.updateConfirmation = update }
                }
            },
            bottomBar = {
                if (!useRail) {
                    ShortNavigationBar(containerColor = ComposeColor(0xFF0A0A0A)) {
                        navigationItems().forEach { item ->
                            ShortNavigationBarItem(
                                selected = selectedNavigationPage(page) == item.page,
                                onClick = { page = item.page },
                                icon = {
                                    Icon(
                                        painter = painterResource(when (item.page) { Page.OpenGLES -> R.drawable.ic_opengles_gl_es; Page.EGL -> R.drawable.ic_egl_official; else -> item.icon }),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(if (item.page == Page.OpenGLES || item.page == Page.EGL) 27.dp else 24.dp)
                                    )
                                },
                                label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = ShortNavigationBarItemDefaults.colors(
                                    selectedIconColor = BrandSoft,
                                    selectedTextColorTopIconPosition = TextPrimary,
                                    selectedTextColorStartIconPosition = TextPrimary,
                                    selectedIndicatorColor = BrandContainer,
                                    unselectedIconColor = ComposeColor(0xFFB8B8B8),
                                    unselectedTextColor = ComposeColor(0xFFB8B8B8)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (useRail) {
                    CompactNavigationRail(
                        selectedPage = selectedNavigationPage(page),
                        onPageSelected = { page = it },
                        requestInitialFocus = isTelevision
                    )
                }
                Box(Modifier.weight(1f)) {
                    val current = report
                    if (collecting) LoadingView()
                    else if (current == null) EmptyState("No OpenGL ES report")
                    else if (!current.available) EmptyState("OpenGL ES unavailable\n${current.reason}")
                    else {
                        AnimatedContent(
                            targetState = page,
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart,
                            transitionSpec = {
                                val forward = pageTransitionIndex(targetState) > pageTransitionIndex(initialState)
                                if (forward) {
                                    slideInHorizontally(animationSpec = spring()) { it / 5 } + fadeIn(animationSpec = spring()) togetherWith
                                        slideOutHorizontally(animationSpec = spring()) { -it / 5 } + fadeOut(animationSpec = spring())
                                } else {
                                    slideInHorizontally(animationSpec = spring()) { -it / 5 } + fadeIn(animationSpec = spring()) togetherWith
                                        slideOutHorizontally(animationSpec = spring()) { it / 5 } + fadeOut(animationSpec = spring())
                                }
                            },
                            label = "pageTransition"
                        ) { targetPage ->
                            PageContent(activity, targetPage, current, display, collectionReady = !collecting && current.available, onNavigate = { page = it })
                        }
                    }
                }
            }
        }
        if (activity.directUpdatesConsentVisible) {
            DirectUpdatesConsentDialog(
                appName = "OpenGLESScope",
                releaseSource = "github.com/EFIShell0/OpenGLESScope/releases",
                onDismiss = { activity.directUpdatesConsentVisible = false },
                onConfirm = { activity.confirmDirectUpdatesConsent() }
            )
        }
        activity.updateConfirmation?.let { update ->
            UpdateConfirmationDialog(
                update = update,
                onDismiss = { activity.updateConfirmation = null },
                onConfirm = {
                    activity.updateConfirmation = null
                    activity.downloadAndInstallUpdate(update)
                }
            )
        }
    }
}

private data class NavigationItem(val page: Page, val label: String, val icon: Int)

private fun selectedNavigationPage(page: Page): Page = when (page) {
    Page.Features, Page.Limits, Page.Formats, Page.Precision, Page.Configs, Page.Analysis, Page.Settings, Page.Info -> Page.Overview
    else -> page
}

private fun navigationItems(): List<NavigationItem> = listOf(
    NavigationItem(Page.Overview, "Overview", R.drawable.ic_home),
    NavigationItem(Page.OpenGLES, "OpenGL ES", R.drawable.ic_features),
    NavigationItem(Page.EGL, "EGL", R.drawable.ic_egl_official),
    NavigationItem(Page.Display, "Display", R.drawable.ic_display),
    NavigationItem(Page.Extensions, "Extensions", R.drawable.ic_extensions)
)

private fun pageTransitionIndex(page: Page): Int = when (page) {
    Page.Overview -> 0
    Page.OpenGLES -> 1
    Page.EGL -> 2
    Page.Display -> 3
    Page.Extensions -> 4
    Page.Features -> 5
    Page.Limits -> 6
    Page.Formats -> 7
    Page.Precision -> 8
    Page.Configs -> 9
    Page.Analysis -> 10
    Page.Settings -> 11
    Page.Info -> 12
}

private fun pageIcon(page: Page): Int = when (page) {
    Page.Overview -> R.drawable.ic_home
    Page.OpenGLES -> R.drawable.ic_opengles_gl_es
    Page.Display -> R.drawable.ic_display
    Page.EGL -> R.drawable.ic_egl_official
    Page.Features -> R.drawable.ic_features
    Page.Limits -> R.drawable.ic_properties
    Page.Formats -> R.drawable.ic_formats
    Page.Extensions -> R.drawable.ic_extensions
    Page.Precision -> R.drawable.ic_features
    Page.Configs -> R.drawable.ic_surface
    Page.Analysis -> R.drawable.ic_properties
    Page.Settings -> R.drawable.ic_settings
    Page.Info -> R.drawable.ic_info
}

@Composable
private fun CompactNavigationRail(selectedPage: Page, onPageSelected: (Page) -> Unit, requestInitialFocus: Boolean) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(requestInitialFocus) { if (requestInitialFocus) firstFocusRequester.requestFocus() }
    Surface(modifier = Modifier.width(80.dp), color = ComposeColor(0xFF101010)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .focusGroup()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            navigationItems().forEachIndexed { index, item ->
                val selected = selectedPage == item.page
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val scope = rememberCoroutineScope()
                var focused by remember { mutableStateOf(false) }
                val shape = RoundedCornerShape(18.dp)
                Card(
                    onClick = { onPageSelected(item.page) },
                    colors = CardDefaults.cardColors(containerColor = if (selected) BrandContainer else if (focused) ComposeColor(0xFF2B1726) else ComposeColor.Transparent),
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .then(if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { state -> focused = state.isFocused; if (state.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } }
                        .border(if (focused) 2.dp else 0.dp, if (focused) BrandSoft else ComposeColor.Transparent, shape)
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(when (item.page) { Page.OpenGLES -> R.drawable.ic_opengles_gl_es; Page.EGL -> R.drawable.ic_egl_official; else -> item.icon }),
                            contentDescription = item.label,
                            modifier = Modifier.size(if (item.page == Page.OpenGLES || item.page == Page.EGL) 23.dp else 21.dp),
                            tint = if (selected) BrandSoft else ComposeColor(0xFFB8B8B8)
                        )
                        Text(
                            item.label,
                            color = if (selected) TextPrimary else ComposeColor(0xFFB8B8B8),
                            fontSize = 9.sp,
                            lineHeight = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader(page: Page, onBack: () -> Unit, onSettings: () -> Unit, onInfo: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            if (page != Page.Overview) {
                ExpressiveIconButton(R.drawable.ic_back, "Back", onBack)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(R.drawable.openglesscope_logo_horizontal),
                    contentDescription = "OpenGLESScope",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(148.dp).height(28.dp)
                )
                Text(page.title, style = MaterialTheme.typography.labelMedium, color = ComposeColor(0xFF9E9E9E), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        actions = {
            if (page != Page.Settings && page != Page.Info) {
                ExpressiveIconButton(R.drawable.ic_info, "Info", onInfo)
                ExpressiveIconButton(R.drawable.ic_settings, "Settings", onSettings)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ComposeColor.Black)
    )
}

@Composable
private fun PageContent(activity: MainActivity, page: Page, report: GlReport, display: DisplayInfo, collectionReady: Boolean, onNavigate: (Page) -> Unit) {
    val requiresGraphicsReport = when (page) {
        Page.OpenGLES, Page.EGL, Page.Features, Page.Limits, Page.Formats, Page.Extensions, Page.Precision, Page.Configs, Page.Analysis -> true
        else -> false
    }
    if (requiresGraphicsReport && !report.available) {
        UnavailableCapabilityPage(report.reason)
        return
    }
    when (page) {
        Page.Overview -> OverviewPage(report, display, onNavigate)
        Page.OpenGLES -> OpenGLESPage(report)
        Page.Display -> DisplayPage(display)
        Page.EGL -> EglPage(report)
        Page.Features -> FeaturesPage(report)
        Page.Limits -> LimitsPage(report)
        Page.Formats -> FormatsPage(report)
        Page.Extensions -> ExtensionsPage(report)
        Page.Precision -> PrecisionPage(report)
        Page.Configs -> ConfigsPage(report)
        Page.Analysis -> AnalysisPage(activity, report, display)
        Page.Settings -> SettingsPage(activity)
        Page.Info -> InfoPage(activity, report, display, collectionReady)
    }
}

@Composable
private fun UnavailableCapabilityPage(reason: String) {
    Box(Modifier.fillMaxSize().padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
        CapabilitySectionCard("Capability collection unavailable") {
            CapabilityStatusBadge("Unavailable", false)
            Text(reason.ifBlank { "The OpenGL ES/EGL probe did not return a complete report." }, color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OverviewPage(report: GlReport, display: DisplayInfo, navigate: (Page) -> Unit) {
    LazyColumn(
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HeroCard(report) }
        if (!report.available) {
            item {
                CapabilitySectionCard("Capability collection unavailable") {
                    CapabilityStatusBadge("Unavailable", false)
                    Text(report.reason.ifBlank { "The OpenGL ES/EGL probe did not return a complete report." }, color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { ExploreCard(navigate) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("OpenGL ES", shortGlVersion(report.glVersion), Modifier.weight(1f))
                MetricCard("EGL", report.egl.initializedVersion, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("HDR", if (display.hdrTypes.isEmpty()) "Unavailable" else "${display.hdrTypes.size} types", Modifier.weight(1f))
                MetricCard("Wide gamut", when (display.wideColor) { true -> "Supported"; false -> "Not supported"; null -> "Unavailable" }, Modifier.weight(1f))
            }
        }
        item {
            CapabilitySectionCard("Quick access") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickAccessCard("OpenGL ES", Page.OpenGLES, navigate, Modifier.weight(1f))
                        QuickAccessCard("EGL", Page.EGL, navigate, Modifier.weight(1f))
                        QuickAccessCard("Display", Page.Display, navigate, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickAccessCard("Extensions", Page.Extensions, navigate, Modifier.weight(1f))
                        QuickAccessCard("Limits", Page.Limits, navigate, Modifier.weight(1f))
                        QuickAccessCard("Formats", Page.Formats, navigate, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickAccessCard("Features", Page.Features, navigate, Modifier.weight(1f))
                        QuickAccessCard("Precision", Page.Precision, navigate, Modifier.weight(1f))
                        QuickAccessCard("EGL Configs", Page.Configs, navigate, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickAccessCard("Analysis", Page.Analysis, navigate, Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            CapabilitySectionCard("Runtime snapshot") {
                CapabilityKeyValue("Renderer", report.renderer)
                CapabilityKeyValue("Vendor", report.vendor)
                CapabilityKeyValue("GL_VERSION", report.glVersion)
                CapabilityKeyValue("GLSL ES", report.glslVersion)
                CapabilityKeyValue("OpenGL ES extensions", report.extensions.size.toString())
                CapabilityKeyValue("EGL extensions", (report.egl.extensions.size + report.egl.clientExtensions.size).toString())
                CapabilityKeyValue(
                    "Implementation queries",
                    listOf(
                        "Available" to report.diagnostics.count { it.status == "Available" },
                        "Unavailable" to report.diagnostics.count { it.status == "Unavailable" },
                        "Not applicable" to report.diagnostics.count { it.status == "Not applicable" },
                        "Unknown" to report.diagnostics.count { it.status == "Unknown" }
                    ).filter { it.second > 0 }.joinToString(" / ") { "${it.second} ${it.first.lowercase()}" }
                )
            }
        }
        item {
            CapabilitySectionCard("Operating system") {
                CapabilityKeyValue("Architecture", System.getProperty("os.arch")?.ifBlank { "Unavailable" } ?: "Unavailable")
                CapabilityKeyValue("Android", Build.VERSION.RELEASE.ifBlank { "Unavailable" })
                CapabilityKeyValue("Codename", Build.VERSION.CODENAME.ifBlank { "Unavailable" })
                CapabilityKeyValue("SDK", Build.VERSION.SDK_INT.toString())
                CapabilityKeyValue("Build ID", Build.ID.ifBlank { "Unavailable" })
                CapabilityKeyValue("Incremental", Build.VERSION.INCREMENTAL.ifBlank { "Unavailable" })
                CapabilityKeyValue("Security patch", Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" })
                CapabilityKeyValue("Manufacturer", Build.MANUFACTURER.ifBlank { "Unavailable" })
                CapabilityKeyValue("Model", Build.MODEL.ifBlank { "Unavailable" })
                CapabilityKeyValue("Hardware", Build.HARDWARE.ifBlank { "Unavailable" })
            }
        }
    }
}

@Composable
private fun HeroCard(report: GlReport) {
    val shape = RoundedCornerShape(32.dp)
    Surface(color = ComposeColor(0xFF181516), shape = shape, modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VendorLogo(report.vendor, report.renderer, Modifier.size(82.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(report.renderer, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(report.vendor, color = ComposeColor(0xFFBDBDBD), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(shortGlVersion(report.glVersion), color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun VendorLogo(vendor: String, renderer: String, modifier: Modifier = Modifier) {
    val text = "$vendor $renderer".lowercase()
    val implementationLayer = listOf("angle", "swiftshader", "mesa", "turnip", "freedreno", "panfrost", "zink", "llvmpipe", "virgl", "software rasterizer").any { it in text }
    val icon = when {
        implementationLayer -> R.drawable.gpu_vendor_unknown
        "qualcomm" in text || "adreno" in text -> R.drawable.gpu_vendor_qualcomm
        "arm" in text || "mali" in text -> R.drawable.gpu_vendor_arm
        "imagination" in text || "powervr" in text -> R.drawable.gpu_vendor_imagination
        "nvidia" in text || "tegra" in text -> R.drawable.gpu_vendor_nvidia
        "intel" in text -> R.drawable.gpu_vendor_intel
        "amd" in text || "radeon" in text -> R.drawable.gpu_vendor_amd
        "broadcom" in text || "videocore" in text -> R.drawable.gpu_vendor_broadcom
        "samsung" in text || "xclipse" in text -> R.drawable.gpu_vendor_samsung
        "huawei" in text -> R.drawable.gpu_vendor_huawei
        "vivante" in text -> R.drawable.gpu_vendor_vivante
        "verisilicon" in text || "vsi" in text -> R.drawable.gpu_vendor_vsi
        else -> R.drawable.gpu_vendor_unknown
    }
    Card(colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF111111)), shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Image(
            painter = painterResource(icon),
            contentDescription = when {
                implementationLayer -> "Graphics implementation layer"
                "digital media professionals" in text || "dmp" in text -> "Digital Media Professionals"
                "think silicon" in text || "thinksilicon" in text -> "Think Silicon"
                "vmware" in text -> "VMware"
                "microsoft" in text -> "Microsoft"
                "apple" in text -> "Apple"
                "qualcomm" in text || "adreno" in text -> "Qualcomm"
                "arm" in text || "mali" in text -> "Arm"
                "imagination" in text || "powervr" in text -> "Imagination Technologies"
                "nvidia" in text || "tegra" in text -> "NVIDIA"
                "intel" in text -> "Intel"
                "amd" in text || "radeon" in text -> "AMD"
                "broadcom" in text || "videocore" in text -> "Broadcom"
                "samsung" in text || "xclipse" in text -> "Samsung"
                "huawei" in text -> "Huawei"
                "vivante" in text -> "Vivante"
                "verisilicon" in text || "vsi" in text -> "VeriSilicon"
                else -> "Unknown vendor"
            },
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun QuickAccessCard(title: String, destination: Page, navigate: (Page) -> Unit, modifier: Modifier) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(22.dp)
    Card(
        onClick = { navigate(destination) },
        colors = CardDefaults.cardColors(containerColor = if (focused) ComposeColor(0xFF2B1726) else ComposeColor(0xFF1A1718)),
        shape = shape,
        modifier = modifier.height(72.dp).onFocusChanged { focused = it.isFocused }.border(if (focused) 2.dp else 0.dp, if (focused) BrandSoft else ComposeColor.Transparent, shape)
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(pageIcon(destination)),
                contentDescription = null,
                modifier = Modifier.size(if (destination == Page.OpenGLES || destination == Page.EGL) 23.dp else 19.dp),
                tint = BrandSoft
            )
            Text(title, fontSize = 11.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ExploreCard(onNavigate: (Page) -> Unit) {
    CapabilitySectionCard("Explore") {
        Text("Detailed OpenGL ES and EGL inspection areas", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Page.Features, Page.Limits, Page.Formats, Page.Precision, Page.Configs).forEach { page ->
                ExpressiveAssistChip(
                    label = page.title,
                    leadingIcon = pageIcon(page),
                    onClick = { onNavigate(page) }
                )
            }
        }
    }
}

@Composable
private fun OpenGLESPage(r: GlReport) = CapabilityListPage("OpenGL ES runtime", listOf(
    "Driver mode" to "System OpenGL ES/EGL",
    "Driver version" to "Unavailable (OpenGL ES does not expose a standardized driver-version query)",
    "Renderer" to r.renderer,
    "Vendor" to r.vendor,
    "GL_VERSION" to r.glVersion,
    "Core version" to "${r.glMajor}.${r.glMinor}",
    "Core version provenance" to coreVersionProvenance(r),
    "GL_SHADING_LANGUAGE_VERSION" to r.glslVersion
))

@Composable
private fun DisplayPage(d: DisplayInfo) {
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CapabilitySectionCard("Display") {
                CapabilityKeyValue("Display", d.name)
                CapabilityKeyValue("Current mode ID", d.modeId?.toString() ?: "Unavailable")
                CapabilityKeyValue("Current mode resolution", if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width} × ${d.height}" else "Unavailable")
                CapabilityKeyValue("Refresh rate", d.refreshRate?.let { String.format(java.util.Locale.US, "%.2f Hz", it) } ?: "Unavailable")
                CapabilityKeyValue("Wide color gamut", when (d.wideColor) { true -> "SUPPORTED"; false -> "NOT SUPPORTED"; null -> "UNAVAILABLE" })
            }
        }
        item {
            CapabilitySectionCard("HDR capabilities") {
                if (d.hdrTypes.isEmpty()) {
                    Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF4A3211)) {
                        Text("UNAVAILABLE", color = ComposeColor(0xFFFFC857), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                    }
                } else {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        d.hdrTypes.forEach { HdrTypeCard(it) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ComposeColor(0xFF303030))
                CapabilityKeyValue("Desired minimum luminance", d.desiredMinLuminance?.let { "$it cd/m²" } ?: "Unavailable")
                CapabilityKeyValue("Desired maximum luminance", d.desiredMaxLuminance?.let { "$it cd/m²" } ?: "Unavailable")
                CapabilityKeyValue("Desired maximum average luminance", d.desiredMaxAverageLuminance?.let { "$it cd/m²" } ?: "Unavailable")
            }
        }
        item {
            CapabilitySectionCard("Supported display modes") {
                if (d.supportedModes.isEmpty()) Text("Unavailable", color = ComposeColor(0xFF9E9E9E))
                else d.supportedModes.forEachIndexed { index, mode -> CapabilityKeyValue("Mode ${index + 1}", mode) }
            }
        }
        item {
            CapabilitySectionCard("Display evidence") {
                CapabilityKeyValue("Android wide color gamut", when (d.wideColor) { true -> "Supported"; false -> "Not supported"; null -> "Unavailable" })
                Text("Android display and HDR evidence is reported separately from OpenGL ES and EGL capability data.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HdrTypeCard(type: String) {
    val normalized = type.trim().lowercase(java.util.Locale.ROOT)
    val logo = when (normalized) {
        "dolby vision" -> R.drawable.hdr_dolby_vision
        "dolby vision 2" -> R.drawable.hdr_dolby_vision_2
        "hdr10" -> R.drawable.hdr_hdr10
        "hdr10+" -> R.drawable.hdr_hdr10_plus
        "hdr10+ advanced" -> R.drawable.hdr_hdr10_plus_advanced
        "hdr vivid" -> R.drawable.hdr_vivid
        else -> null
    }
    val whiteCard = normalized == "hdr10"
    val shape = RoundedCornerShape(18.dp)
    Surface(shape = shape, color = if (whiteCard) ComposeColor.White else ComposeColor(0xFF111111), border = androidx.compose.foundation.BorderStroke(1.dp, if (whiteCard) ComposeColor(0xFFE0E0E0) else ComposeColor(0xFF2B2B2B)), modifier = Modifier.then(tvBrowseModifier(shape))) {
        if (logo != null) {
            Image(painter = painterResource(logo), contentDescription = type, contentScale = ContentScale.Fit, modifier = Modifier.width(154.dp).height(62.dp).padding(horizontal = 13.dp, vertical = 10.dp))
        } else {
            Text(type, color = if (whiteCard) ComposeColor.Black else ComposeColor.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp))
        }
    }
}

@Composable
private fun EglPage(r: GlReport) {
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("EGL identity") {
                CapabilityKeyValue("EGL_VENDOR", r.egl.vendor)
                CapabilityKeyValue("EGL_VERSION", r.egl.version)
                CapabilityKeyValue("Initialized EGL version", r.egl.initializedVersion)
                CapabilityKeyValue("EGL_CLIENT_APIS", r.egl.clientApis)
                CapabilityKeyValue("Display extensions", r.egl.extensions.size.toString())
                CapabilityKeyValue("Client extensions", r.egl.clientExtensions.size.toString())
            }
        }
        item {
            CapabilitySectionCard("Current EGL binding and context") {
                CapabilityKeyValue("Bound client API", r.eglRuntime.boundApi)
                CapabilityKeyValue("Current config ID", r.eglRuntime.configId?.toString() ?: "Unavailable")
                CapabilityKeyValue("Context client type", r.eglRuntime.clientType ?: "Unavailable")
                CapabilityKeyValue("Context client version", r.eglRuntime.clientVersion?.toString() ?: "Unavailable")
                CapabilityKeyValue("Context render buffer", r.eglRuntime.renderBuffer ?: "Unavailable")
                CapabilityKeyValue("Current context", if (r.eglRuntime.currentContext) "Available" else "Unavailable")
                CapabilityKeyValue("Current display", if (r.eglRuntime.currentDisplay) "Available" else "Unavailable")
                CapabilityKeyValue("Current draw surface", if (r.eglRuntime.currentDrawSurface) "Available" else "Unavailable")
                CapabilityKeyValue("Current read surface", if (r.eglRuntime.currentReadSurface) "Available" else "Unavailable")
            }
        }
        item {
            CapabilitySectionCard("Collector pbuffer") {
                CapabilityKeyValue("Size", if (r.eglRuntime.surfaceWidth != null && r.eglRuntime.surfaceHeight != null) "${r.eglRuntime.surfaceWidth} × ${r.eglRuntime.surfaceHeight}" else "Unavailable")
                CapabilityKeyValue("Render buffer", r.eglRuntime.surfaceRenderBuffer ?: "Unavailable")
                CapabilityKeyValue("Swap behavior", r.eglRuntime.surfaceSwapBehavior ?: "Unavailable")
                CapabilityKeyValue("Texture format", r.eglRuntime.surfaceTextureFormat ?: "Unavailable")
                CapabilityKeyValue("Texture target", r.eglRuntime.surfaceTextureTarget ?: "Unavailable")
                CapabilityKeyValue("Mipmap texture", r.eglRuntime.surfaceMipmapTexture?.toString() ?: "Unavailable")
                CapabilityKeyValue("Mipmap level", r.eglRuntime.surfaceMipmapLevel?.toString() ?: "Unavailable")
                CapabilityKeyValue("Multisample resolve", r.eglRuntime.surfaceMultisampleResolve ?: "Not applicable / unavailable")
            }
        }
        item {
            CapabilitySectionCard("EGL runtime query failures") {
                CapabilityKeyValue("Unavailable attributes", r.eglRuntime.unavailableAttributes.size.toString())
                if (r.eglRuntime.unavailableAttributes.isEmpty()) {
                    Text("No explicit EGL runtime attribute failure was recorded.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                } else {
                    r.eglRuntime.unavailableAttributes.forEach { CapabilityKeyValue(it.name, it.error) }
                }
            }
        }
    }
}

@Composable
private fun FeaturesPage(r: GlReport) {
    val version = r.glMajor * 100 + r.glMinor * 10
    val coreState: (Int) -> EvidenceState = { required ->
        if (version <= 0) EvidenceState.Unknown else if (version >= required) EvidenceState.Supported else EvidenceState.Unsupported
    }
    val rows = listOf(
        "OpenGL ES 2.0 core" to coreState(200),
        "OpenGL ES 3.0 core" to coreState(300),
        "OpenGL ES 3.1 core" to coreState(310),
        "OpenGL ES 3.2 core" to coreState(320),
        "EGL_KHR_create_context" to extensionState("EGL_KHR_create_context", r.egl.extensions, r.diagnostics, listOf("EGL_EXTENSIONS")),
        "EGL_KHR_surfaceless_context" to extensionState("EGL_KHR_surfaceless_context", r.egl.extensions, r.diagnostics, listOf("EGL_EXTENSIONS")),
        "GL_KHR_debug" to extensionState("GL_KHR_debug", r.extensions, r.diagnostics, listOf("GL_EXTENSIONS")),
        "GL_EXT_texture_filter_anisotropic" to extensionState("GL_EXT_texture_filter_anisotropic", r.extensions, r.diagnostics, listOf("GL_EXTENSIONS"))
    )
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CapabilitySectionCard("Feature evidence") { Text("Core feature state is derived only from GL_VERSION. Extension-backed state requires an exact runtime extension name.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall) } }
        items(rows) { (name, state) -> StatusRow(name, state) }
    }
}

private fun extensionState(name: String, extensions: List<String>, diagnostics: List<QueryDiagnostic>, evidenceQueries: List<String>): EvidenceState {
    if (extensions.contains(name)) return EvidenceState.Supported
    val evidenceAvailable = evidenceQueries.any { query -> diagnostics.any { it.name == query && it.status == "Available" } }
    return if (evidenceAvailable) EvidenceState.Unsupported else EvidenceState.Unknown
}

@Composable
private fun LimitsPage(r: GlReport) {
    var mode by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    val rows = if (mode == 0) r.limits.map { it.name to it.value } else r.diagnostics.map { it.name to if (it.detail.isBlank()) it.status else "${it.status} · ${it.detail}" }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard(if (mode == 0) "OpenGL ES limits" else "Query diagnostics") {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveFilterChip(selected = mode == 0, label = "Limits", onClick = { mode = 0 })
                    ExpressiveFilterChip(selected = mode == 1, label = "Diagnostics", onClick = { mode = 1 })
                }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = if (mode == 0) "Search limits…" else "Search diagnostics…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Matches", "${filtered.size} / ${rows.size}")
                if (mode == 0) Text("Implementation limits and query diagnostics are counted separately so diagnostic evidence never inflates the implementation-limit total.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(filtered, key = { "${mode}|${it.first}" }) { (name, value) -> CapabilityItemCard { CapabilityKeyValue(name, value) } }
    }
}

@Composable
private fun FormatsPage(r: GlReport) {
    var mode by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
    var query by remember { mutableStateOf("") }
    val category = when (mode) { 0 -> "Compressed texture format"; 1 -> "Shader binary format"; else -> "Program binary format" }
    val queryName = when (mode) { 0 -> "GL_COMPRESSED_TEXTURE_FORMATS"; 1 -> "GL_SHADER_BINARY_FORMATS"; else -> "GL_PROGRAM_BINARY_FORMATS" }
    val rows = when (mode) {
        0 -> r.compressedFormats.mapIndexed { i, v -> "Compressed format ${i + 1}" to v }
        1 -> r.shaderBinaryFormats.mapIndexed { i, v -> "Shader binary ${i + 1}" to v }
        else -> r.programBinaryFormats.mapIndexed { i, v -> "Program binary ${i + 1}" to v }
    }
    val diagnostic = r.diagnostics.firstOrNull { it.name == queryName }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("Formats") {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveFilterChip(selected = mode == 0, label = "Texture", onClick = { mode = 0 })
                    ExpressiveFilterChip(selected = mode == 1, label = "Shader binary", onClick = { mode = 1 })
                    ExpressiveFilterChip(selected = mode == 2, label = "Program binary", onClick = { mode = 2 })
                }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search formats…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Matches", "${filtered.size} / ${rows.size}")
                CapabilityKeyValue("Enumeration query", queryName)
                CapabilityKeyValue("Query evidence", diagnostic?.status ?: "Unknown")
                if (!diagnostic?.detail.isNullOrBlank()) CapabilityKeyValue("Query diagnostic", diagnostic!!.detail)
                Text("Select an entry for canonical/raw evidence details.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(filtered, key = { it.first }) { row ->
            Card(onClick = { selected = row }, colors = CardDefaults.cardColors(containerColor = SurfaceRaised), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) { CapabilityKeyValue(row.first, row.second) }
            }
        }
    }
    selected?.let { row ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(category) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CapabilityKeyValue("Runtime value", row.second)
                CapabilityKeyValue("Query", queryName)
                CapabilityKeyValue("Evidence", diagnostic?.status ?: "Unknown")
                if (!diagnostic?.detail.isNullOrBlank()) CapabilityKeyValue("Diagnostic", diagnostic!!.detail)
                Text("Known registered values are shown as canonical symbolic name plus raw hexadecimal value. Unknown future values remain raw evidence.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { ExpressiveTextButton("Close") { selected = null } }
        )
    }
}

@Composable
private fun ExtensionsPage(r: GlReport) {
    val uriHandler = LocalUriHandler.current
    var mode by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    val rows = when (mode) {
        0 -> r.extensions
        1 -> r.egl.extensions
        else -> r.egl.clientExtensions
    }
    val scopeName = when (mode) { 0 -> "OpenGL ES runtime"; 1 -> "EGL display runtime"; else -> "EGL client runtime" }
    var query by remember { mutableStateOf("") }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.contains(query, true) } }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("Extensions") {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveFilterChip(selected = mode == 0, label = "OpenGL ES", onClick = { mode = 0 })
                    ExpressiveFilterChip(selected = mode == 1, label = "EGL display", onClick = { mode = 1 })
                    ExpressiveFilterChip(selected = mode == 2, label = "EGL client", onClick = { mode = 2 })
                }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Matches", "${filtered.size} / ${rows.size}")
                Text("Select an exact runtime token for offline registry/provenance details.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(filtered, key = { it }) { ext ->
            Card(onClick = { selected = ext }, colors = CardDefaults.cardColors(containerColor = SurfaceRaised), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Text(ext, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(14.dp))
            }
        }
    }
    selected?.let { ext ->
        val diagnostic = r.diagnostics.firstOrNull { it.name.contains(ext, true) }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(ext, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CapabilityKeyValue("Scope", scopeName)
                CapabilityKeyValue("Namespace", extensionNamespace(ext))
                CapabilityKeyValue("Runtime evidence", "Exact token enumerated")
                CapabilityKeyValue("Registry baseline", if (mode == 0) "Khronos OpenGL ES registry · ES 3.2 baseline" else "Khronos EGL registry · EGL 1.5 baseline")
                val queryGates = QUERY_DEPENDENCIES[ext].orEmpty()
                CapabilityKeyValue("Implemented query gates", if (queryGates.isEmpty()) "No dedicated implementation-dependent query gate" else queryGates.joinToString(" · "))
                queryGates.forEach { gate ->
                    val gateEvidence = r.diagnostics.firstOrNull { it.name == gate }
                    if (gateEvidence != null) CapabilityKeyValue(gate, if (gateEvidence.detail.isBlank()) gateEvidence.status else "${gateEvidence.status} · ${gateEvidence.detail}")
                }
                diagnostic?.let { CapabilityKeyValue("Related query", if (it.detail.isBlank()) it.status else "${it.status} · ${it.detail}") }
                Text("Promotion, dependency or feature state is not inferred from this token alone; exact runtime queries remain authoritative.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } },
            dismissButton = {
                extensionRegistryUrl(ext)?.let { url -> ExpressiveTextButton("Khronos spec") { uriHandler.openUri(url) } }
            },
            confirmButton = { ExpressiveTextButton("Close") { selected = null } }
        )
    }
}

@Composable
private fun PrecisionPage(r: GlReport) {
    var query by remember { mutableStateOf("") }
    val rows = remember(r.precision, query) {
        if (query.isBlank()) r.precision else r.precision.filter { listOf(it.shader, it.type, it.rangeMin, it.rangeMax, it.precision).joinToString(" ").contains(query, true) }
    }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("Shader precision") {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search shader precision…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Matches", "${rows.size} / ${r.precision.size}")
            }
        }
        items(rows, key = { "${it.shader}|${it.type}" }) { p ->
            CapabilityItemCard {
                CapabilityKeyValue("Shader", p.shader)
                CapabilityKeyValue("Type", p.type)
                CapabilityKeyValue("Range", "${p.rangeMin} … ${p.rangeMax}")
                CapabilityKeyValue("Precision", p.precision.toString())
            }
        }
    }
}

private fun eglBooleanLabel(value: Int?): String = when (value) { 0 -> "False"; 1 -> "True"; else -> "Unavailable" }

@Composable
private fun ConfigsPage(r: GlReport) {
    var query by remember { mutableStateOf("") }
    val rows = remember(r.eglConfigs, query) {
        r.eglConfigs.filter { c ->
            query.isBlank() || listOf(
                c.id, c.red, c.green, c.blue, c.alpha, c.depth, c.stencil, c.sampleBuffers, c.samples,
                c.surfaceType, c.renderableType, c.conformant, c.configCaveat, c.colorBufferType, c.level,
                c.nativeRenderable, c.nativeVisualId, c.minSwapInterval, c.maxSwapInterval, c.bufferSize,
                c.luminanceSize, c.alphaMaskSize, c.bindToTextureRgb, c.bindToTextureRgba, c.maxPbufferWidth,
                c.maxPbufferHeight, c.maxPbufferPixels, c.nativeVisualType, c.transparentType, c.transparentRed,
                c.transparentGreen, c.transparentBlue, c.recordableAndroid, c.framebufferTargetAndroid, c.colorComponentTypeExt,
                c.unavailableAttributes.joinToString(" ") { "${it.name} ${it.error}" }
            ).joinToString(" ") { it?.toString().orEmpty() }.contains(query, true)
        }
    }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("EGL Configs") {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search EGL configs…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                CapabilityKeyValue("Matches", "${rows.size} / ${r.eglConfigs.size}")
            }
        }
        items(rows, key = { it.id }) { c ->
            CapabilitySectionCard("EGL Config ${c.id}") {
                CapabilityKeyValue("RGBA", listOf(c.red, c.green, c.blue, c.alpha).joinToString(" / ") { it?.toString() ?: "?" })
                CapabilityKeyValue("Depth / stencil", "${c.depth ?: "?"} / ${c.stencil ?: "?"}")
                CapabilityKeyValue("Samples", "${c.sampleBuffers ?: "?"} buffers, ${c.samples ?: "?"} samples")
                CapabilityKeyValue("Surface type", c.surfaceType.orEmpty())
                CapabilityKeyValue("Renderable type", c.renderableType.orEmpty())
                CapabilityKeyValue("Conformant", c.conformant.orEmpty())
                CapabilityKeyValue("Config caveat", c.configCaveat.orEmpty())
                CapabilityKeyValue("Color buffer type", c.colorBufferType.orEmpty())
                CapabilityKeyValue("Buffer size", c.bufferSize?.toString() ?: "Unavailable")
                CapabilityKeyValue("Luminance size", c.luminanceSize?.toString() ?: "Unavailable")
                CapabilityKeyValue("Alpha mask size", c.alphaMaskSize?.toString() ?: "Unavailable")
                CapabilityKeyValue("Bind to texture RGB", eglBooleanLabel(c.bindToTextureRgb))
                CapabilityKeyValue("Bind to texture RGBA", eglBooleanLabel(c.bindToTextureRgba))
                CapabilityKeyValue("Max pbuffer width", c.maxPbufferWidth?.toString() ?: "Unavailable")
                CapabilityKeyValue("Max pbuffer height", c.maxPbufferHeight?.toString() ?: "Unavailable")
                CapabilityKeyValue("Max pbuffer pixels", c.maxPbufferPixels?.toString() ?: "Unavailable")
                CapabilityKeyValue("Native renderable", eglBooleanLabel(c.nativeRenderable))
                CapabilityKeyValue("Native visual ID", c.nativeVisualId?.toString() ?: "Unavailable")
                CapabilityKeyValue("Native visual type", c.nativeVisualType?.toString() ?: "Unavailable")
                CapabilityKeyValue("Transparency type", c.transparentType ?: "Unavailable")
                CapabilityKeyValue("Transparent RGB", "${c.transparentRed ?: "?"} / ${c.transparentGreen ?: "?"} / ${c.transparentBlue ?: "?"}")
                CapabilityKeyValue("EGL_ANDROID_recordable", c.recordableAndroid?.let { eglBooleanLabel(it) } ?: "Not applicable / unavailable")
                CapabilityKeyValue("EGL_ANDROID_framebuffer_target", c.framebufferTargetAndroid?.let { eglBooleanLabel(it) } ?: "Not applicable / unavailable")
                CapabilityKeyValue("EGL_EXT_pixel_format_float", c.colorComponentTypeExt ?: "Not applicable / unavailable")
                if (c.unavailableAttributes.isNotEmpty()) CapabilityKeyValue("Unavailable attributes", c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" })
                CapabilityKeyValue("Level", c.level?.toString() ?: "Unavailable")
                CapabilityKeyValue("Swap interval", "${c.minSwapInterval ?: "?"} … ${c.maxSwapInterval ?: "?"}")
            }
        }
    }
}

@Composable
private fun SettingsPage(activity: MainActivity) {
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CapabilitySectionCard("Updates") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Direct GitHub updates", fontWeight = FontWeight.SemiBold)
                        Text(if (activity.directUpdatesEnabled) "Enabled · update checks use the official OpenGLESScope GitHub Releases channel" else "Disabled · recommended when Obtainium manages updates", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
                    }
                    ExpressiveSwitch(checked = activity.directUpdatesEnabled, onCheckedChange = { activity.requestDirectUpdatesChanged(it) })
                }
                Text("Direct GitHub updates are enabled by default so new installations receive update checks. When disabled, OpenGLESScope performs no startup update check and will not download update APKs. Obtainium can track the universal APK from the official GitHub Releases channel without enabling the built-in updater.", color = ComposeColor(0xFF777777), style = MaterialTheme.typography.bodySmall)
                val context = androidx.compose.ui.platform.LocalContext.current
            }
        }
    }
}




private const val ANALYSIS_MAX_SNAPSHOT_BYTES = 8 * 1024 * 1024
private const val ANALYSIS_MAX_ENTRIES = 32768
private const val ANALYSIS_MAX_KEY_LENGTH = 1024
private const val ANALYSIS_MAX_VALUE_LENGTH = 16384
private const val ANALYSIS_MAX_WATCHED = 256

private fun readBoundedAnalysisBytes(input: java.io.InputStream, maxBytes: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream(minOf(maxBytes, 64 * 1024))
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        if (read == 0) continue
        total += read
        if (total > maxBytes) error("Snapshot exceeds ${maxBytes / (1024 * 1024)} MiB")
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

private data class AnalysisDiffRow(val key: String, val baseline: String?, val current: String?, val state: String, val kind: String)
private data class GlMinimum(val name: String, val threshold: Double, val display: String, val direction: String)

private val OPENGL_ES_32_MINIMUMS = listOf(
    GlMinimum("GL_SUBPIXEL_BITS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_ELEMENT_INDEX", 16777215.0, "≥ 16,777,215", "minimum"),
    GlMinimum("GL_MAX_3D_TEXTURE_SIZE", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_TEXTURE_SIZE", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_ARRAY_TEXTURE_LAYERS", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_TEXTURE_LOD_BIAS", 2.0, "≥ 2.0", "minimum"),
    GlMinimum("GL_MAX_CUBE_MAP_TEXTURE_SIZE", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_RENDERBUFFER_SIZE", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_DRAW_BUFFERS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_FRAMEBUFFER_WIDTH", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_FRAMEBUFFER_HEIGHT", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_FRAMEBUFFER_LAYERS", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_FRAMEBUFFER_SAMPLES", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_COLOR_ATTACHMENTS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_FRAGMENT_INTERPOLATION_OFFSET_BITS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_SAMPLES", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_SAMPLE_MASK_WORDS", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_COLOR_TEXTURE_SAMPLES", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_DEPTH_TEXTURE_SAMPLES", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_INTEGER_SAMPLES", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET", 2047.0, "≥ 2047", "minimum"),
    GlMinimum("GL_MAX_VERTEX_ATTRIB_BINDINGS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_VERTEX_ATTRIB_STRIDE", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_TEXTURE_BUFFER_SIZE", 65536.0, "≥ 65,536", "minimum"),
    GlMinimum("GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum"),
    GlMinimum("GL_MAX_VERTEX_ATTRIBS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_VERTEX_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_VERTEX_UNIFORM_VECTORS", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_VERTEX_UNIFORM_BLOCKS", 12.0, "≥ 12", "minimum"),
    GlMinimum("GL_MAX_VERTEX_OUTPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_TESS_GEN_LEVEL", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_PATCH_VERTICES", 32.0, "≥ 32", "minimum"),
    GlMinimum("GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_TESS_PATCH_COMPONENTS", 120.0, "≥ 120", "minimum"),
    GlMinimum("GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS", 2048.0, "≥ 2048", "minimum"),
    GlMinimum("GL_MAX_TESS_CONTROL_INPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS", 12.0, "≥ 12", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_UNIFORM_BLOCKS", 12.0, "≥ 12", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_INPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_OUTPUT_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_OUTPUT_VERTICES", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_GEOMETRY_SHADER_INVOCATIONS", 32.0, "≥ 32", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_UNIFORM_VECTORS", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_UNIFORM_BLOCKS", 12.0, "≥ 12", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_INPUT_COMPONENTS", 60.0, "≥ 60", "minimum"),
    GlMinimum("GL_MAX_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_ATOMIC_COUNTERS", 8.0, "≥ 8", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MIN_PROGRAM_TEXEL_OFFSET", -8.0, "≤ -8", "maximum"),
    GlMinimum("GL_MAX_PROGRAM_TEXEL_OFFSET", 7.0, "≥ 7", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_COUNT[0]", 65535.0, "≥ 65,535", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_COUNT[1]", 65535.0, "≥ 65,535", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_COUNT[2]", 65535.0, "≥ 65,535", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_SIZE[0]", 128.0, "≥ 128", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_SIZE[1]", 128.0, "≥ 128", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_SIZE[2]", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS", 128.0, "≥ 128", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_UNIFORM_BLOCKS", 12.0, "≥ 12", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS", 16.0, "≥ 16", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_SHARED_MEMORY_SIZE", 16384.0, "≥ 16,384", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_UNIFORM_COMPONENTS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_ATOMIC_COUNTERS", 8.0, "≥ 8", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_UNIFORM_BUFFER_BINDINGS", 72.0, "≥ 72", "minimum"),
    GlMinimum("GL_MAX_UNIFORM_BLOCK_SIZE", 16384.0, "≥ 16,384", "minimum"),
    GlMinimum("GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum"),
    GlMinimum("GL_MAX_COMBINED_UNIFORM_BLOCKS", 60.0, "≥ 60", "minimum"),
    GlMinimum("GL_MAX_VARYING_COMPONENTS", 60.0, "≥ 60", "minimum"),
    GlMinimum("GL_MAX_VARYING_VECTORS", 15.0, "≥ 15", "minimum"),
    GlMinimum("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS", 96.0, "≥ 96", "minimum"),
    GlMinimum("GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_UNIFORM_LOCATIONS", 1024.0, "≥ 1024", "minimum"),
    GlMinimum("GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE", 32.0, "≥ 32", "minimum"),
    GlMinimum("GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_COMBINED_ATOMIC_COUNTERS", 8.0, "≥ 8", "minimum"),
    GlMinimum("GL_MAX_IMAGE_UNITS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_FRAGMENT_IMAGE_UNIFORMS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_COMPUTE_IMAGE_UNIFORMS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_COMBINED_IMAGE_UNIFORMS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_SHADER_STORAGE_BLOCK_SIZE", 134217728.0, "≥ 134,217,728", "minimum"),
    GlMinimum("GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", 256.0, "≤ 256", "maximum"),
    GlMinimum("GL_MAX_DEBUG_MESSAGE_LENGTH", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_DEBUG_LOGGED_MESSAGES", 1.0, "≥ 1", "minimum"),
    GlMinimum("GL_MAX_DEBUG_GROUP_STACK_DEPTH", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_LABEL_LENGTH", 256.0, "≥ 256", "minimum"),
    GlMinimum("GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS", 64.0, "≥ 64", "minimum"),
    GlMinimum("GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS", 4.0, "≥ 4", "minimum"),
    GlMinimum("GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS", 4.0, "≥ 4", "minimum")
)

private val GL_QUERY_DEPENDENCIES = linkedMapOf(
    "GL_EXT_texture_filter_anisotropic" to listOf("GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT"),
    "GL_KHR_debug" to listOf("GL_MAX_DEBUG_MESSAGE_LENGTH", "GL_MAX_DEBUG_LOGGED_MESSAGES", "GL_MAX_DEBUG_GROUP_STACK_DEPTH", "GL_MAX_LABEL_LENGTH"),
    "GL_EXT_disjoint_timer_query" to listOf("GL_TIME_ELAPSED_EXT_QUERY_COUNTER_BITS", "GL_TIMESTAMP_EXT_QUERY_COUNTER_BITS"),
    "GL_EXT_blend_func_extended" to listOf("GL_MAX_DUAL_SOURCE_DRAW_BUFFERS_EXT"),
    "GL_OVR_multiview" to listOf("GL_MAX_VIEWS_OVR"),
    "GL_OVR_multiview2" to listOf("GL_MAX_VIEWS_OVR"),
    "GL_EXT_multiview_draw_buffers" to listOf("GL_MAX_MULTIVIEW_BUFFERS_EXT"),
    "GL_EXT_texture_buffer" to listOf("GL_MAX_TEXTURE_BUFFER_SIZE_EXT", "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT"),
    "GL_EXT_clip_cull_distance" to listOf("GL_MAX_CLIP_DISTANCES_EXT", "GL_MAX_CULL_DISTANCES_EXT", "GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES_EXT"),
    "GL_EXT_draw_buffers" to listOf("GL_MAX_DRAW_BUFFERS_EXT", "GL_MAX_COLOR_ATTACHMENTS_EXT"),
    "GL_NV_draw_buffers" to listOf("GL_MAX_DRAW_BUFFERS_NV"),
    "GL_EXT_multisampled_render_to_texture" to listOf("GL_MAX_SAMPLES_EXT"),
    "GL_NV_framebuffer_multisample" to listOf("GL_MAX_SAMPLES_NV"),
    "GL_IMG_multisampled_render_to_texture" to listOf("GL_MAX_SAMPLES_IMG"),
    "GL_KHR_shader_subgroup" to listOf("GL_SUBGROUP_SIZE_KHR", "GL_SUBGROUP_SUPPORTED_STAGES_KHR", "GL_SUBGROUP_SUPPORTED_FEATURES_KHR", "GL_SUBGROUP_QUAD_ALL_STAGES_KHR"),
    "GL_EXT_window_rectangles" to listOf("GL_MAX_WINDOW_RECTANGLES_EXT"),
    "GL_OES_viewport_array" to listOf("GL_MAX_VIEWPORTS_OES", "GL_VIEWPORT_SUBPIXEL_BITS_OES", "GL_VIEWPORT_BOUNDS_RANGE_OES", "GL_VIEWPORT_INDEX_PROVOKING_VERTEX_OES"),
    "GL_EXT_shader_pixel_local_storage" to listOf("GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_FAST_SIZE_EXT", "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_SIZE_EXT"),
    "GL_EXT_shader_pixel_local_storage2" to listOf("GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_FAST_SIZE_EXT", "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_SIZE_EXT"),
    "GL_OES_sample_shading" to listOf("GL_MIN_SAMPLE_SHADING_VALUE_OES"),
    "GL_EXT_sparse_texture" to listOf("GL_MAX_SPARSE_TEXTURE_SIZE_EXT", "GL_MAX_SPARSE_3D_TEXTURE_SIZE_EXT", "GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_EXT", "GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_EXT"),
    "GL_OES_get_program_binary" to listOf("GL_NUM_PROGRAM_BINARY_FORMATS", "GL_PROGRAM_BINARY_FORMATS")
)

private val EGL_QUERY_DEPENDENCIES = linkedMapOf(
    "EGL_KHR_create_context" to listOf("EGL_CONTEXT_CLIENT_TYPE", "EGL_CONTEXT_CLIENT_VERSION", "EGL_RENDER_BUFFER/context"),
    "EGL_ANDROID_recordable" to listOf("EGL_RECORDABLE_ANDROID"),
    "EGL_ANDROID_framebuffer_target" to listOf("EGL_FRAMEBUFFER_TARGET_ANDROID"),
    "EGL_EXT_pixel_format_float" to listOf("EGL_COLOR_COMPONENT_TYPE_EXT")
)

private val QUERY_DEPENDENCIES = linkedMapOf<String, List<String>>().apply {
    putAll(GL_QUERY_DEPENDENCIES)
    putAll(EGL_QUERY_DEPENDENCIES)
}

private fun eglConfigAnalysisValue(c: EglConfigEntry): String = listOf(
    "red=${c.red}", "green=${c.green}", "blue=${c.blue}", "alpha=${c.alpha}", "depth=${c.depth}", "stencil=${c.stencil}",
    "sampleBuffers=${c.sampleBuffers}", "samples=${c.samples}", "surfaceType=${c.surfaceType}", "renderableType=${c.renderableType}",
    "conformant=${c.conformant}", "configCaveat=${c.configCaveat}", "colorBufferType=${c.colorBufferType}", "level=${c.level}",
    "nativeRenderable=${c.nativeRenderable}", "nativeVisualId=${c.nativeVisualId}", "minSwapInterval=${c.minSwapInterval}",
    "maxSwapInterval=${c.maxSwapInterval}", "bufferSize=${c.bufferSize}", "luminanceSize=${c.luminanceSize}", "alphaMaskSize=${c.alphaMaskSize}",
    "bindToTextureRgb=${c.bindToTextureRgb}", "bindToTextureRgba=${c.bindToTextureRgba}", "maxPbufferWidth=${c.maxPbufferWidth}",
    "maxPbufferHeight=${c.maxPbufferHeight}", "maxPbufferPixels=${c.maxPbufferPixels}", "nativeVisualType=${c.nativeVisualType}",
    "transparentType=${c.transparentType}", "transparentRed=${c.transparentRed}", "transparentGreen=${c.transparentGreen}", "transparentBlue=${c.transparentBlue}",
    "recordableAndroid=${c.recordableAndroid}", "framebufferTargetAndroid=${c.framebufferTargetAndroid}", "colorComponentTypeExt=${c.colorComponentTypeExt}",
    "unavailableAttributes=${c.unavailableAttributes.joinToString("|") { "${it.name}:${it.error}" }}"
).joinToString(", ")

private fun glAnalysisEntries(report: GlReport, display: DisplayInfo): LinkedHashMap<String, String> = linkedMapOf<String, String>().apply {
    put("identity/applicationVersion", BuildConfig.VERSION_NAME)
    put("identity/renderer", report.renderer)
    put("identity/vendor", report.vendor)
    put("identity/glVersion", report.glVersion)
    put("identity/glCoreVersion", "${report.glMajor}.${report.glMinor}")
    put("identity/glslVersion", report.glslVersion)
    put("identity/eglVendor", report.egl.vendor)
    put("identity/eglVersion", report.egl.version)
    put("identity/eglInitializedVersion", report.egl.initializedVersion)
    put("identity/eglClientApis", report.egl.clientApis)
    put("identity/androidSecurityPatch", Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" })
    put("identity/androidSdk", Build.VERSION.SDK_INT.toString())
    put("identity/androidRelease", Build.VERSION.RELEASE.ifBlank { "Unavailable" })
    put("identity/deviceManufacturer", Build.MANUFACTURER.ifBlank { "Unavailable" })
    put("identity/deviceModel", Build.MODEL.ifBlank { "Unavailable" })
    put("egl-runtime/boundApi", report.eglRuntime.boundApi)
    put("egl-runtime/configId", report.eglRuntime.configId?.toString() ?: "Unavailable")
    put("egl-runtime/clientType", report.eglRuntime.clientType ?: "Unavailable")
    put("egl-runtime/clientVersion", report.eglRuntime.clientVersion?.toString() ?: "Unavailable")
    put("egl-runtime/renderBuffer", report.eglRuntime.renderBuffer ?: "Unavailable")
    put("egl-runtime/currentContext", report.eglRuntime.currentContext.toString())
    put("egl-runtime/currentDisplay", report.eglRuntime.currentDisplay.toString())
    put("egl-runtime/currentDrawSurface", report.eglRuntime.currentDrawSurface.toString())
    put("egl-runtime/currentReadSurface", report.eglRuntime.currentReadSurface.toString())
    put("egl-runtime/surfaceSize", if (report.eglRuntime.surfaceWidth != null && report.eglRuntime.surfaceHeight != null) "${report.eglRuntime.surfaceWidth}x${report.eglRuntime.surfaceHeight}" else "Unavailable")
    put("egl-runtime/surfaceRenderBuffer", report.eglRuntime.surfaceRenderBuffer ?: "Unavailable")
    put("egl-runtime/surfaceSwapBehavior", report.eglRuntime.surfaceSwapBehavior ?: "Unavailable")
    put("egl-runtime/surfaceTextureFormat", report.eglRuntime.surfaceTextureFormat ?: "Unavailable")
    put("egl-runtime/surfaceTextureTarget", report.eglRuntime.surfaceTextureTarget ?: "Unavailable")
    put("egl-runtime/surfaceMipmapTexture", report.eglRuntime.surfaceMipmapTexture?.toString() ?: "Unavailable")
    put("egl-runtime/surfaceMipmapLevel", report.eglRuntime.surfaceMipmapLevel?.toString() ?: "Unavailable")
    put("egl-runtime/surfaceMultisampleResolve", report.eglRuntime.surfaceMultisampleResolve ?: "Unavailable")
    report.eglRuntime.unavailableAttributes.forEach { put("egl-runtime/unavailable/${it.name}", it.error) }
    report.limits.forEach { put("limit/${it.name}", it.value) }
    report.extensions.forEach { put("extension/gl/$it", "present") }
    report.egl.extensions.forEach { put("extension/egl-display/$it", "present") }
    report.egl.clientExtensions.forEach { put("extension/egl-client/$it", "present") }
    report.compressedFormats.forEach { put("format/compressed/$it", "present") }
    report.shaderBinaryFormats.forEach { put("format/shader-binary/$it", "present") }
    report.programBinaryFormats.forEach { put("format/program-binary/$it", "present") }
    report.precision.forEach { put("precision/${it.shader}/${it.type}", "range=${it.rangeMin}..${it.rangeMax}, precision=${it.precision}") }
    report.eglConfigs.forEach { put("eglconfig/${it.id}", eglConfigAnalysisValue(it)) }
    report.diagnostics.forEach { put("query/${it.name}", "${it.status}${it.detail.takeIf { d -> d.isNotBlank() }?.let { d -> " · $d" } ?: ""}") }
    put("display/name", display.name)
    put("display/modeId", display.modeId?.toString() ?: "Unavailable")
    put("display/resolution", if (display.width != null && display.height != null) "${display.width}x${display.height}" else "Unavailable")
    put("display/refreshRate", display.refreshRate?.toString() ?: "Unavailable")
    put("display/wideColor", display.wideColor?.toString() ?: "Unavailable")
    display.supportedModes.forEachIndexed { index, value -> put("display/mode/$index", value) }
    display.hdrTypes.forEach { put("display/hdr/$it", "present") }
    put("display/desiredMaxLuminance", display.desiredMaxLuminance?.toString() ?: "Unavailable")
    put("display/desiredMaxAverageLuminance", display.desiredMaxAverageLuminance?.toString() ?: "Unavailable")
    put("display/desiredMinLuminance", display.desiredMinLuminance?.toString() ?: "Unavailable")
}

private fun glAnalysisSnapshot(report: GlReport, display: DisplayInfo): JSONObject = JSONObject()
    .put("schema", "OpenGLESScopeAnalysisSnapshot1")
    .put("applicationVersion", BuildConfig.VERSION_NAME)
    .put("entries", JSONObject().apply { glAnalysisEntries(report, display).forEach { (key, value) -> put(key, value) } })

private fun jsonStrings(array: JSONArray?): List<String> = buildList {
    if (array != null) for (i in 0 until array.length()) add(array.optString(i))
}

private fun flattenLegacyGlSnapshot(snapshot: JSONObject): LinkedHashMap<String, String> = linkedMapOf<String, String>().apply {
    listOf("renderer", "vendor", "glVersion", "glslVersion", "eglVendor", "eglVersion", "eglInitializedVersion").forEach { key -> put("identity/$key", snapshot.optString(key, "Unavailable")) }
    val limits = snapshot.optJSONObject("limits") ?: JSONObject()
    limits.keys().forEach { key -> put("limit/$key", limits.optString(key)) }
    jsonStrings(snapshot.optJSONArray("extensions")).forEach { put("extension/gl/$it", "present") }
    jsonStrings(snapshot.optJSONArray("eglExtensions")).forEach { put("extension/egl-display/$it", "present") }
    jsonStrings(snapshot.optJSONArray("eglClientExtensions")).forEach { put("extension/egl-client/$it", "present") }
    jsonStrings(snapshot.optJSONArray("compressedFormats")).forEach { put("format/compressed/$it", "present") }
    jsonStrings(snapshot.optJSONArray("shaderBinaryFormats")).forEach { put("format/shader-binary/$it", "present") }
    jsonStrings(snapshot.optJSONArray("programBinaryFormats")).forEach { put("format/program-binary/$it", "present") }
    val diagnostics = snapshot.optJSONObject("diagnostics") ?: JSONObject()
    diagnostics.keys().forEach { key ->
        val item = diagnostics.optJSONObject(key)
        put("query/$key", if (item == null) diagnostics.optString(key) else "${item.optString("status")}${item.optString("detail").takeIf { d -> d.isNotBlank() }?.let { d -> " · $d" } ?: ""}")
    }
    val display = snapshot.optJSONObject("display")
    if (display != null) {
        listOf("name", "resolution", "refreshRate", "wideColor").forEach { key -> put("display/$key", display.optString(key, "Unavailable")) }
        jsonStrings(display.optJSONArray("hdrTypes")).forEach { put("display/hdr/$it", "present") }
    }
}

private fun validateGlAnalysisSnapshot(snapshot: JSONObject): JSONObject {
    if (snapshot.optString("schema") != "OpenGLESScopeAnalysisSnapshot1") error("Unsupported OpenGLESScope analysis snapshot")
    val applicationVersion = snapshot.optString("applicationVersion", "Unknown")
    if (applicationVersion.length > 64) error("Snapshot application version is invalid")
    val entries = snapshot.optJSONObject("entries") ?: JSONObject().apply { flattenLegacyGlSnapshot(snapshot).forEach { (key, value) -> put(key, value) } }
    val keys = entries.keys().asSequence().toList()
    if (keys.size > ANALYSIS_MAX_ENTRIES) error("Snapshot contains too many evidence entries")
    keys.forEach { key ->
        if (key.isBlank() || key.length > ANALYSIS_MAX_KEY_LENGTH) error("Snapshot contains an invalid evidence key")
        val value = entries.opt(key)
        if (value !is String || value.length > ANALYSIS_MAX_VALUE_LENGTH) error("Snapshot contains an invalid evidence value")
    }
    return JSONObject().put("schema", "OpenGLESScopeAnalysisSnapshot1").put("applicationVersion", applicationVersion).put("entries", entries)
}

private fun flattenGlSnapshot(snapshot: JSONObject): Map<String, String> {
    val validated = validateGlAnalysisSnapshot(snapshot)
    val entries = validated.getJSONObject("entries")
    return linkedMapOf<String, String>().apply { entries.keys().forEach { key -> put(key, entries.getString(key)) } }
}

private fun numericAnalysisValue(value: String?): Double? = value?.trim()?.takeIf { it.matches(Regex("-?\\d+(?:\\.\\d+)?")) }?.toDoubleOrNull()

private fun analysisKind(key: String): String = key.substringBefore('/').ifBlank { "other" }

private fun queryAvailable(now: Map<String, String>, name: String): Boolean = now["query/$name"]?.startsWith("Available", true) == true

private fun removalHasCompleteEvidence(key: String, now: Map<String, String>): Boolean = when {
    key.startsWith("extension/gl/") -> queryAvailable(now, "GL_EXTENSIONS")
    key.startsWith("extension/egl-display/") -> queryAvailable(now, "EGL_EXTENSIONS")
    key.startsWith("extension/egl-client/") -> queryAvailable(now, "EGL_NO_DISPLAY/EGL_EXTENSIONS")
    key.startsWith("format/compressed/") -> queryAvailable(now, "compressedFormats")
    key.startsWith("format/shader-binary/") -> queryAvailable(now, "GL_SHADER_BINARY_FORMATS")
    key.startsWith("format/program-binary/") -> queryAvailable(now, "GL_PROGRAM_BINARY_FORMATS") || now["query/programBinaryFormats"]?.startsWith("Not applicable", true) == true
    else -> false
}

private fun scalarLimitRegression(key: String, before: String?, after: String?): Boolean {
    if (!key.startsWith("limit/")) return false
    val a = numericAnalysisValue(before) ?: return false
    val b = numericAnalysisValue(after) ?: return false
    val name = key.removePrefix("limit/")
    return when {
        name in setOf("GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", "GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT") -> b > a
        name.startsWith("GL_MAX_") -> b < a
        else -> false
    }
}

private fun glSnapshotDiff(baseline: JSONObject, current: JSONObject): List<AnalysisDiffRow> {
    val old = flattenGlSnapshot(baseline)
    val now = flattenGlSnapshot(current)
    return (old.keys + now.keys).toSortedSet().map { key ->
        val before = old[key]
        val after = now[key]
        val state = when {
            before == null -> "Added"
            after == null && removalHasCompleteEvidence(key, now) -> "Removed · regression candidate"
            after == null -> "Removed · evidence incomplete"
            before == after -> "Unchanged"
            scalarLimitRegression(key, before, after) -> "Changed · regression candidate"
            else -> "Changed"
        }
        AnalysisDiffRow(key, before, after, state, analysisKind(key))
    }
}

private fun extensionNamespace(name: String): String {
    val parts = name.split('_')
    return if (parts.size >= 3) parts[1] else "Unknown"
}

private fun extensionRegistryUrl(name: String): String? {
    val parts = name.split('_', limit = 3)
    if (parts.size != 3) return null
    val family = when {
        name.startsWith("GL_") -> "OpenGL"
        name.startsWith("EGL_") -> "EGL"
        else -> return null
    }
    val namespace = parts[1]
    return "https://registry.khronos.org/$family/extensions/$namespace/${namespace}_${parts[2]}.txt"
}

private data class GlDiagnosticScore(val score: Int?, val level: String, val factors: List<String>)

private fun glDiagnosticEvidenceScore(report: GlReport): GlDiagnosticScore {
    if (!report.available) return GlDiagnosticScore(null, "Unavailable", listOf(report.reason.ifBlank { "No completed OpenGL ES report is available" }))
    var score = 100
    val factors = mutableListOf<String>()
    fun deduct(points: Int, label: String) {
        score = (score - points).coerceAtLeast(0)
        factors += "-$points · $label"
    }
    val unavailable = report.diagnostics.filter { it.status.equals("Unavailable", true) }
    val unknown = report.diagnostics.filter { it.status.equals("Unknown", true) }
    if (unavailable.isNotEmpty()) deduct(minOf(35, 5 + unavailable.size.coerceAtMost(6) * 5), "${unavailable.size} explicit query failure(s)")
    if (unknown.isNotEmpty()) deduct(minOf(15, unknown.size.coerceAtMost(5) * 3), "${unknown.size} query result(s) remain Unknown")
    listOf("GL_VENDOR", "GL_RENDERER", "GL_VERSION", "GL_SHADING_LANGUAGE_VERSION").forEach { name ->
        val d = report.diagnostics.firstOrNull { it.name == name }
        if (d == null || !d.status.equals("Available", true)) deduct(10, "$name runtime identity query is not Available")
    }
    val glEnumeration = report.diagnostics.firstOrNull { it.name == "GL_EXTENSIONS" }
    if (glEnumeration == null || !glEnumeration.status.equals("Available", true)) deduct(10, "OpenGL ES extension enumeration is not complete/Available")
    val eglEnumeration = report.diagnostics.firstOrNull { it.name == "EGL_EXTENSIONS" }
    if (eglEnumeration == null || !eglEnumeration.status.equals("Available", true)) deduct(5, "EGL display extension enumeration is not Available")
    if (!(report.eglRuntime.currentContext && report.eglRuntime.currentDisplay && report.eglRuntime.currentDrawSurface && report.eglRuntime.currentReadSurface)) deduct(10, "Current EGL context/display/surface binding evidence is incomplete")
    if (report.eglRuntime.unavailableAttributes.isNotEmpty()) factors += "${report.eglRuntime.unavailableAttributes.size} explicit EGL runtime attribute failure(s) retained with exact EGL errors"
    val level = when {
        score >= 95 -> "No explicit collection anomalies"
        score >= 80 -> "Minor explicit anomalies"
        score >= 60 -> "Multiple explicit anomalies"
        else -> "Severe explicit collection anomalies"
    }
    if (factors.isEmpty()) factors += "No explicit collection/query anomaly was recorded by OpenGLESScope"
    return GlDiagnosticScore(score, level, factors)
}

private fun analysisQueryTokens(query: String): List<String> = Regex("\"([^\"]+)\"|\\S+").findAll(query).map { it.groups[1]?.value ?: it.value }.toList()

private fun matchesGlAnalysisQuery(row: AnalysisDiffRow, query: String): Boolean {
    if (query.isBlank()) return true
    val extension = row.key.split('/').firstOrNull { it.startsWith("GL_") || it.startsWith("EGL_") }
    return analysisQueryTokens(query).all { token ->
        val split = token.split(':', limit = 2)
        if (split.size == 1) row.key.contains(token, true) || row.state.contains(token, true) || row.baseline?.contains(token, true) == true || row.current?.contains(token, true) == true
        else when (split[0].lowercase()) {
            "state" -> row.state.contains(split[1], true)
            "kind" -> row.kind.equals(split[1], true)
            "changed" -> (row.state != "Unchanged") == split[1].equals("true", true)
            "vendor", "namespace" -> extension?.let(::extensionNamespace)?.equals(split[1], true) == true
            "scope" -> row.key.startsWith("extension/${split[1].lowercase()}/", true)
            else -> row.key.contains(token, true) || row.baseline?.contains(token, true) == true || row.current?.contains(token, true) == true
        }
    }
}

private fun databaseReportUrl(id: String): String? = id.takeIf { it.matches(Regex("[a-f0-9]{64}")) }?.let { "${DATABASE_WEB}#reports/$it/Overview" }

private fun MainActivity.shareText(text: String) {
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share"))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnalysisPage(activity: MainActivity, report: GlReport, display: DisplayInfo) {
    val context = activity as Context
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(0) }
    var baseline by remember { mutableStateOf<JSONObject?>(null) }
    var importStatus by remember { mutableStateOf("No baseline imported") }
    var includeUnchanged by remember { mutableStateOf(false) }
    var diffQuery by remember { mutableStateOf("") }
    var diffStateFilter by remember { mutableStateOf("All") }
    var diffKindFilter by remember { mutableStateOf("All") }
    var minimumQuery by remember { mutableStateOf("") }
    var minimumStateFilter by remember { mutableStateOf("All") }
    var graphQuery by remember { mutableStateOf("") }
    var graphRoot by remember { mutableStateOf("GL_EXT_texture_filter_anisotropic") }
    var graphDepth by remember { mutableStateOf(2) }
    var watchInput by remember { mutableStateOf("") }
    var watchQuery by remember { mutableStateOf("") }
    var watchStateFilter by remember { mutableStateOf("All") }
    val prefs = remember { context.getSharedPreferences("analysis_tools", Context.MODE_PRIVATE) }
    var watched by remember { mutableStateOf((prefs.getStringSet("watched", emptySet())?.toSet() ?: emptySet()).take(ANALYSIS_MAX_WATCHED).toSet()) }
    var testResult by remember { mutableStateOf<JSONObject?>(null) }
    var testRunning by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf("") }
    val currentSnapshot = remember(report, display) { glAnalysisSnapshot(report, display) }
    val currentFlat = remember(currentSnapshot) { flattenGlSnapshot(currentSnapshot) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { input -> readBoundedAnalysisBytes(input, ANALYSIS_MAX_SNAPSHOT_BYTES) } ?: error("Unable to read snapshot")
                    validateGlAnalysisSnapshot(JSONObject(bytes.toString(Charsets.UTF_8)))
                }
            }
            result.onSuccess {
                baseline = it
                importStatus = "Baseline imported · ${flattenGlSnapshot(it)["identity/renderer"] ?: "Unknown GPU"}"
            }.onFailure { importStatus = it.message ?: "Import failed" }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = currentSnapshot.toString(2).toByteArray(Charsets.UTF_8)
                    if (bytes.size > ANALYSIS_MAX_SNAPSHOT_BYTES) error("Snapshot exceeds ${ANALYSIS_MAX_SNAPSHOT_BYTES / (1024 * 1024)} MiB; no evidence was truncated")
                    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) } ?: error("Unable to open snapshot destination")
                }
            }
            exportStatus = result.fold({ "Snapshot exported" }, { it.message ?: "Snapshot export failed" })
        }
    }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard("Analysis tools") {
                Text("Imported snapshots, comparisons, watch lists, query graphs and optional active tests stay local. They never rewrite canonical OpenGL ES/EGL capability evidence or Database submissions.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Compare", "Spec minimums", "Graph", "Quality", "Watched", "Share", "Tests").forEachIndexed { index, label -> ExpressiveFilterChip(selected = mode == index, label = label, onClick = { mode = index }) }
                }
            }
        }
        if (mode == 0) {
            item {
                CapabilitySectionCard("Offline report compare") {
                    CapabilityKeyValue("Baseline", importStatus)
                    if (exportStatus.isNotBlank()) CapabilityKeyValue("Export", exportStatus)
                    ExpressiveActionButton("Import analysis snapshot", "Select a previously exported OpenGLESScope JSON snapshot", R.drawable.ic_action_text) { importLauncher.launch(arrayOf("application/json", "text/plain")) }
                    ExpressiveActionButton("Export analysis snapshot", "Complete bounded local evidence snapshot for offline comparison", R.drawable.ic_action_html) { exportLauncher.launch("OpenGLESScope-${safeFilePart(report.renderer)}-analysis.json") }
                    if (baseline != null) {
                        ExpressiveSearchField(value = diffQuery, onValueChange = { diffQuery = it }, placeholderText = "Search diff evidence…", modifier = Modifier.fillMaxWidth())
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "Added", "Removed", "Changed", "Regression").forEach { value -> ExpressiveFilterChip(selected = diffStateFilter == value, label = value, onClick = { diffStateFilter = value }) }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "identity", "extension", "limit", "format", "precision", "egl-runtime", "eglconfig", "display", "query").forEach { value -> ExpressiveFilterChip(selected = diffKindFilter == value, label = value, onClick = { diffKindFilter = value }) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Switch(checked = includeUnchanged, onCheckedChange = { includeUnchanged = it })
                            Text("Show unchanged", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            val allRows = baseline?.let { glSnapshotDiff(it, currentSnapshot) }.orEmpty()
            val rows = allRows.filter { row ->
                (includeUnchanged || row.state != "Unchanged") &&
                    matchesGlAnalysisQuery(row, diffQuery) &&
                    (diffKindFilter == "All" || row.kind == diffKindFilter) &&
                    when (diffStateFilter) {
                        "Added" -> row.state == "Added"
                        "Removed" -> row.state.startsWith("Removed")
                        "Changed" -> row.state.startsWith("Changed")
                        "Regression" -> row.state.contains("regression candidate")
                        else -> true
                    }
            }
            if (baseline != null) {
                item { CapabilitySectionCard("Diff summary") {
                    CapabilityKeyValue("Baseline fields", flattenGlSnapshot(baseline!!).size.toString())
                    CapabilityKeyValue("Current fields", currentFlat.size.toString())
                    CapabilityKeyValue("Added", allRows.count { it.state == "Added" }.toString())
                    CapabilityKeyValue("Removed", allRows.count { it.state.startsWith("Removed") }.toString())
                    CapabilityKeyValue("Changed", allRows.count { it.state.startsWith("Changed") }.toString())
                    CapabilityKeyValue("Regression candidates", allRows.count { it.state.contains("regression candidate") }.toString())
                    CapabilityKeyValue("Visible", rows.size.toString())
                    Text("Regression candidate means the current snapshot contains enough direct enumeration evidence to justify the comparison. It is not a conformance, performance, or driver-bug verdict.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } }
                items(rows, key = { it.key }) { row -> CapabilityItemCard {
                    CapabilityKeyValue(row.key, row.state)
                    if (row.baseline != null) CapabilityKeyValue("Baseline", row.baseline)
                    if (row.current != null) CapabilityKeyValue("Current", row.current)
                } }
            }
        } else if (mode == 1) {
            val evaluated = OPENGL_ES_32_MINIMUMS.map { requirement ->
                val raw = report.limits.firstOrNull { it.name == requirement.name }?.value
                val actual = numericAnalysisValue(raw)
                val state = when {
                    actual == null -> "UNKNOWN"
                    requirement.direction == "minimum" && actual >= requirement.threshold -> "PASS"
                    requirement.direction == "maximum" && actual <= requirement.threshold -> "PASS"
                    else -> "FAIL"
                }
                Triple(requirement, raw, state)
            }
            val filtered = evaluated.filter { (requirement, raw, state) ->
                (minimumQuery.isBlank() || requirement.name.contains(minimumQuery, true) || raw?.contains(minimumQuery, true) == true) && (minimumStateFilter == "All" || minimumStateFilter == state)
            }
            item { CapabilitySectionCard("OpenGL ES 3.2 minimum comparison") {
                Text("Only values verified against the Khronos OpenGL ES 3.2 implementation-dependent-value requirements are evaluated. Missing or non-scalar runtime evidence remains UNKNOWN. This lightweight evaluator is not a conformance test.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                CapabilityKeyValue("Runtime core", "${report.glMajor}.${report.glMinor}")
                CapabilityKeyValue("Evaluated requirements", OPENGL_ES_32_MINIMUMS.size.toString())
                CapabilityKeyValue("PASS", evaluated.count { it.third == "PASS" }.toString())
                CapabilityKeyValue("FAIL", evaluated.count { it.third == "FAIL" }.toString())
                CapabilityKeyValue("UNKNOWN", evaluated.count { it.third == "UNKNOWN" }.toString())
                ExpressiveSearchField(value = minimumQuery, onValueChange = { minimumQuery = it }, placeholderText = "Search requirements…", modifier = Modifier.fillMaxWidth())
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "PASS", "FAIL", "UNKNOWN").forEach { value -> ExpressiveFilterChip(selected = minimumStateFilter == value, label = value, onClick = { minimumStateFilter = value }) }
                }
            } }
            items(filtered, key = { it.first.name }) { (requirement, raw, state) ->
                CapabilityItemCard {
                    CapabilityKeyValue(requirement.name, state)
                    CapabilityKeyValue("Requirement", requirement.display)
                    CapabilityKeyValue("Runtime", raw ?: "Unavailable")
                }
            }
        } else if (mode == 2) {
            val runtimeExtensions = (report.extensions + report.egl.extensions + report.egl.clientExtensions).toSet()
            val diagnosticByName = report.diagnostics.associateBy { it.name }
            val graphRows = QUERY_DEPENDENCIES.entries.filter { (extension, queries) -> graphQuery.isBlank() || extension.contains(graphQuery, true) || queries.any { it.contains(graphQuery, true) } }
            val selectedRoot = graphRoot.trim().takeIf { QUERY_DEPENDENCIES.containsKey(it) } ?: graphRows.firstOrNull()?.key
            val selectedQueries = selectedRoot?.let { QUERY_DEPENDENCIES[it].orEmpty() }.orEmpty()
            val visualNodes = buildList {
                if (selectedRoot != null) {
                    add(OpenGLESGraphNode(selectedRoot, 0, null, if (selectedRoot in runtimeExtensions) "Runtime enumerated" else "Not enumerated"))
                    if (graphDepth >= 2) selectedQueries.forEach { query ->
                        val diag = diagnosticByName[query]
                        add(OpenGLESGraphNode(query, 1, selectedRoot, diag?.status ?: "Not reported"))
                    }
                }
            }
            item { CapabilitySectionCard("Runtime/query dependency graph") {
                Text("The graph is derived from OpenGLESScope's checked-in query gates. Runtime extension presence and query evidence remain separate. No registry dependency or promotion relationship is inferred.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                ExpressiveSearchField(value = graphRoot, onValueChange = { graphRoot = it }, placeholderText = "Exact extension for visual graph…", modifier = Modifier.fillMaxWidth())
                ExpressiveSearchField(value = graphQuery, onValueChange = { graphQuery = it }, placeholderText = "Filter query-gate catalog…", modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Depth", style = MaterialTheme.typography.bodySmall)
                    (1..3).forEach { depth -> ExpressiveFilterChip(selected = graphDepth == depth, label = depth.toString(), onClick = { graphDepth = depth }) }
                }
                CapabilityKeyValue("Visual root", selectedRoot ?: "No exact query-gated extension selected")
            } }
            if (visualNodes.isNotEmpty()) item { CapabilitySectionCard("Visual query-gate graph") { OpenGLESDependencyGraph(visualNodes, Modifier.fillMaxWidth()) } }
            items(graphRows, key = { it.key }) { (extension, queries) ->
                CapabilityItemCard {
                    CapabilityKeyValue(extension, if (extension in runtimeExtensions) "Runtime enumerated" else "Not enumerated")
                    if (graphDepth >= 2) queries.forEach { query ->
                        val diag = diagnosticByName[query]
                        CapabilityKeyValue("↳ $query", if (diag == null) "Not reported" else diag.status)
                        if (graphDepth >= 3 && diag?.detail?.isNotBlank() == true) CapabilityKeyValue("Evidence", diag.detail)
                    }
                }
            }
        } else if (mode == 3) {
            val quality = glDiagnosticEvidenceScore(report)
            item { CapabilitySectionCard("Heuristic diagnostic evidence score") {
                CapabilityKeyValue("Score", quality.score?.let { "$it / 100" } ?: "Unavailable")
                CapabilityKeyValue("Interpretation", quality.level)
                Text("This heuristic is not an OpenGL ES conformance result, benchmark, performance score, driver-quality verdict or hardware-quality claim. It summarizes only explicit collection/query anomalies observed by OpenGLESScope.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } }
            items(quality.factors, key = { it }) { factor -> CapabilityItemCard { Text(factor, modifier = Modifier.padding(14.dp)) } }
        } else if (mode == 4) {
            item { CapabilitySectionCard("Watched capabilities") {
                CapabilityKeyValue("Stored", "${watched.size} / $ANALYSIS_MAX_WATCHED")
                ExpressiveSearchField(value = watchInput, onValueChange = { watchInput = it }, placeholderText = "Exact extension, limit, format or evidence token…", modifier = Modifier.fillMaxWidth())
                ExpressiveTextButton("Add to watch list") {
                    val token = watchInput.trim()
                    if (token.isNotEmpty() && token.length <= 256 && watched.size < ANALYSIS_MAX_WATCHED) {
                        watched = watched + token
                        prefs.edit().putStringSet("watched", watched).apply()
                        watchInput = ""
                    }
                }
                ExpressiveSearchField(value = watchQuery, onValueChange = { watchQuery = it }, placeholderText = "Filter watch list…", modifier = Modifier.fillMaxWidth())
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Matched", "Missing").forEach { value -> ExpressiveFilterChip(selected = watchStateFilter == value, label = value, onClick = { watchStateFilter = value }) }
                }
            } }
            val watchRows = watched.sorted().map { token -> token to currentFlat.filterKeys { it.contains(token, true) } }.filter { (token, matches) ->
                (watchQuery.isBlank() || token.contains(watchQuery, true)) && when (watchStateFilter) { "Matched" -> matches.isNotEmpty(); "Missing" -> matches.isEmpty(); else -> true }
            }
            items(watchRows, key = { it.first }) { (token, matches) ->
                CapabilityItemCard {
                    CapabilityKeyValue(token, if (matches.isEmpty()) "Missing" else "Matched · ${matches.size} evidence item(s)")
                    matches.entries.take(10).forEach { CapabilityKeyValue(it.key, it.value) }
                    ExpressiveTextButton("Remove") { watched = watched - token; prefs.edit().putStringSet("watched", watched).apply() }
                }
            }
        } else if (mode == 5) {
            val lastReportId = prefs.getString("last_database_report_id", "").orEmpty()
            val permalink = databaseReportUrl(lastReportId)
            item { CapabilitySectionCard("Database permalink") {
                Text("Permalinks use the canonical OpenGLESScope Database hash route. A link is saved only after a successful explicit submission response returns a valid report ID.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                CapabilityKeyValue("Report ID", lastReportId.ifBlank { "No successful submission recorded" })
                CapabilityKeyValue("Permalink", permalink ?: "Unavailable")
                if (permalink != null) {
                    ExpressiveActionButton("Open report", "Open the canonical Database report route", R.drawable.ic_action_database) { open(activity, permalink) }
                    ExpressiveActionButton("Share permalink", "Share the canonical report URL using Android's local share sheet", R.drawable.ic_action_text) { activity.shareText(permalink) }
                    ExpressiveTextButton("Copy permalink") {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("OpenGLESScope report", permalink))
                    }
                }
            } }
            if (permalink != null) item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OpenGLESQrCode(permalink, Modifier.size(220.dp)) } }
        } else {
            item { CapabilitySectionCard("Optional active tests") {
                Text("Tests run in the isolated probe process and are attributed to the current report only when the isolated context returns the same vendor, renderer and GL_VERSION identity. A failed test is test evidence only and is never converted into an Unsupported capability.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                ExpressiveActionButton("Run OpenGL ES self-tests", "Identity check, shader compile/link, program-binary round-trip when applicable, and KHR_debug insertion when available", R.drawable.ic_action_update, enabled = !testRunning) {
                    testRunning = true
                    scope.launch {
                        testResult = withContext(Dispatchers.IO) { runCatching { JSONObject(activity.runOpenGlesSelfTests(report)) }.getOrElse { JSONObject().put("status", "unavailable").put("reason", it.message ?: "Self-test failed") } }
                        testRunning = false
                    }
                }
                if (testRunning) LoadingIndicator()
            } }
            val result = testResult
            if (result != null) {
                item { CapabilitySectionCard("Self-test result") {
                    CapabilityKeyValue("Status", result.optString("status", "unknown"))
                    result.optString("reason").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Reason", it) }
                    result.optString("renderer").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Isolated renderer", it) }
                    result.optString("runtimeVersion").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Isolated GL_VERSION", it) }
                } }
                val tests = result.optJSONArray("tests")
                if (tests != null) items((0 until tests.length()).mapNotNull { tests.optJSONObject(it) }) { test -> CapabilityItemCard {
                    CapabilityKeyValue(test.optString("name", "Test"), test.optString("status", "unknown"))
                    test.optString("detail").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Detail", it) }
                } }
            }
        }
    }
}

@Composable
private fun DirectUpdatesConsentDialog(appName: String, releaseSource: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = SurfaceRaised,
        tonalElevation = 0.dp,
        title = { Text("Enable direct GitHub updates?", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("$appName will check for updates and download APKs directly from $releaseSource.")
                Text("If you use Obtainium, leave this disabled so Obtainium remains the single update manager. Enabling direct updates makes the app independently check the same official GitHub Releases source and may duplicate update notifications.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { ExpressivePrimaryButton("Enable direct updates", onConfirm) },
        dismissButton = { ExpressiveTextButton("Cancel", onDismiss) }
    )
}

@Composable
private fun InfoPage(activity: MainActivity, report: GlReport, display: DisplayInfo, collectionReady: Boolean) {
    val context = activity as Context
    val scope = rememberCoroutineScope()
    val abi = detectInstalledAbi(context)
    var submitState by remember { mutableStateOf("Ready") }
    var submissionInFlight by remember { mutableStateOf(false) }
    var exportInFlight by remember { mutableStateOf(false) }
    val pendingLegacyExport = remember { mutableStateOf<ExportPayload?>(null) }
    val pendingSafExport = remember { mutableStateOf<ExportPayload?>(null) }
    val legacyPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val pending = pendingLegacyExport.value
        pendingLegacyExport.value = null
        if (pending == null) return@rememberLauncherForActivityResult
        if (granted) {
            scope.launch {
                val message = withContext(Dispatchers.IO) { writeExportToDownloads(context, pending.filename, pending.content, pending.mime) }
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Storage permission is required to save the report.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    val textLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val pending = pendingSafExport.value
        pendingSafExport.value = null
        if (uri != null && pending != null && pending.mime == "text/plain") scope.launch {
            val message = withContext(Dispatchers.IO) { writeExport(context, uri, pending.content, pending.mime) }
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    val htmlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
        val pending = pendingSafExport.value
        pendingSafExport.value = null
        if (uri != null && pending != null && pending.mime == "text/html") scope.launch {
            val message = withContext(Dispatchers.IO) { writeExport(context, uri, pending.content, pending.mime) }
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    fun exportDocument(filename: String, content: String, mime: String, launcher: ActivityResultLauncher<String>) {
        val snapshot = ExportPayload(filename, content, mime)
        pendingSafExport.value = snapshot
        exportWithSafOrDownloads(context, filename, content, mime, launcher) { payload ->
            pendingSafExport.value = null
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                pendingLegacyExport.value = payload
                legacyPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                scope.launch {
                    val message = withContext(Dispatchers.IO) { writeExportToDownloads(context, payload.filename, payload.content, payload.mime) }
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val exportStem = remember(report) { "OpenGLESScope-${safeFilePart(report.renderer.ifBlank { "Unknown-GPU" })}-report" }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CapabilitySectionCard("Developer") {
                ExpressiveIdentityBlock(
                    title = "Semih Boran",
                    subtitle = "EFI Shell · OpenGLESScope developer",
                    icon = R.drawable.ic_info
                )
                ExpressiveActionButton(
                    title = "Open GitHub profile",
                    subtitle = "EFIShell0 · Projects and public profile",
                    icon = R.drawable.ic_action_github,
                    onClick = { open(activity, DEVELOPER_WEB) }
                )
            }
        }
        item {
            CapabilitySectionCard("Application") {
                ExpressiveVersionBlock("OpenGLESScope", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString(), activity.packageName, abi)
                ExpressiveActionButton("Check for updates", if (activity.directUpdatesEnabled) "Official EFIShell0/OpenGLESScope GitHub release channel" else "Direct GitHub updates are disabled in Settings", R.drawable.ic_action_update, enabled = activity.directUpdatesEnabled) { activity.checkForApplicationUpdate(true) }
                ExpressiveActionButton("Open GitHub repository", "Source, releases and project history", R.drawable.ic_action_github) { open(activity, REPOSITORY_WEB) }
                Text(if (activity.directUpdatesEnabled) "Direct update checks use the official OpenGLESScope GitHub release channel. APK download still requires explicit review and confirmation." else "Direct GitHub update checks are currently disabled. Obtainium can manage updates from the official GitHub Releases source without OpenGLESScope running its own update discovery.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("Device ABI") {
                CapabilityKeyValue("Installed ABI", abi)
                CapabilityKeyValue("Supported ABIs", Build.SUPPORTED_ABIS.joinToString(", "))
                Text("Installed ABI is the native ABI used by this OpenGLESScope installation; supported ABIs are the ABIs reported by Android for the device.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("Android") {
                CapabilityKeyValue("Manufacturer", Build.MANUFACTURER.ifBlank { "Unavailable" })
                CapabilityKeyValue("Brand", Build.BRAND.ifBlank { "Unavailable" })
                CapabilityKeyValue("Model", Build.MODEL.ifBlank { "Unavailable" })
                CapabilityKeyValue("Product", Build.PRODUCT.ifBlank { "Unavailable" })
                CapabilityKeyValue("Device", Build.DEVICE.ifBlank { "Unavailable" })
                CapabilityKeyValue("Board", Build.BOARD.ifBlank { "Unavailable" })
                CapabilityKeyValue("Hardware", Build.HARDWARE.ifBlank { "Unavailable" })
                CapabilityKeyValue("Android", Build.VERSION.RELEASE.ifBlank { "Unavailable" })
                CapabilityKeyValue("Codename", Build.VERSION.CODENAME.ifBlank { "Unavailable" })
                CapabilityKeyValue("SDK", Build.VERSION.SDK_INT.toString())
                CapabilityKeyValue("Build ID", Build.ID.ifBlank { "Unavailable" })
                CapabilityKeyValue("Incremental", Build.VERSION.INCREMENTAL.ifBlank { "Unavailable" })
                CapabilityKeyValue("Security patch", Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" })
                CapabilityKeyValue("Fingerprint", Build.FINGERPRINT.ifBlank { "Unavailable" })
            }
        }
        item {
            CapabilitySectionCard("OpenGL ES / EGL query engine") {
                CapabilityKeyValue("Core baseline", "OpenGL ES 3.2 · EGL 1.5")
                CapabilityKeyValue("Runtime OpenGL ES", report.glVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("Runtime GLSL ES", report.glslVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("Runtime EGL", report.egl.version.ifBlank { "Unavailable" })
                CapabilityKeyValue("Initialized EGL", report.egl.initializedVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("GL extension tokens", report.extensions.size.toString())
                CapabilityKeyValue("EGL display extension tokens", report.egl.extensions.size.toString())
                CapabilityKeyValue("EGL client extension tokens", report.egl.clientExtensions.size.toString())
                CapabilityKeyValue("Implementation limits", report.limits.size.toString())
                CapabilityKeyValue("Query diagnostics", report.diagnostics.size.toString())
                CapabilityKeyValue("EGL configs", report.eglConfigs.size.toString())
                CapabilityKeyValue("Report schema", "2 · technical report 2")
                Text("Capability metadata is collected directly from the active system OpenGL ES/EGL implementation. Runtime extension tokens are preserved as reported. A value is not inferred when no validated core-version or exact-extension query path exists.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("About") {
                Text("OpenGLESScope is an OpenGL ES and EGL capability inspection utility for Android. It reports implementation data exposed by the active system graphics stack and keeps Android display/HDR evidence separate from OpenGL ES/EGL capability claims.", color = ComposeColor(0xFFB0B0B0))
            }
        }
        item {
            CapabilitySectionCard("Export complete report") {
                Text("Export the complete currently collected OpenGL ES, EGL and Android display/HDR report. On phones and tablets OpenGLESScope uses Android's Storage Access Framework. On Android TV, or when a document provider cannot be opened, it falls back to Downloads.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExpressiveActionButton(
                        title = "Export TXT",
                        subtitle = if (collectionReady) "Plain-text complete report" else "Waiting for complete OpenGL ES/EGL collection",
                        icon = R.drawable.ic_action_text,
                        modifier = Modifier.weight(1f),
                        compact = true,
                        enabled = collectionReady && report.available && !exportInFlight,
                        onClick = {
                            if (collectionReady && report.available && !exportInFlight) {
                                exportInFlight = true
                                scope.launch {
                                    val content = withContext(Dispatchers.Default) { reportText(context, report, display) }
                                    exportDocument("${exportStem}.txt", content, "text/plain", textLauncher)
                                    exportInFlight = false
                                }
                            }
                        }
                    )
                    ExpressiveActionButton(
                        title = "Export HTML",
                        subtitle = if (collectionReady) "Styled offline complete report" else "Waiting for complete OpenGL ES/EGL collection",
                        icon = R.drawable.ic_action_html,
                        modifier = Modifier.weight(1f),
                        compact = true,
                        enabled = collectionReady && report.available && !exportInFlight,
                        onClick = {
                            if (collectionReady && report.available && !exportInFlight) {
                                exportInFlight = true
                                scope.launch {
                                    val content = withContext(Dispatchers.Default) { reportHtml(context, report, display) }
                                    exportDocument("${exportStem}.html", content, "text/html", htmlLauncher)
                                    exportInFlight = false
                                }
                            }
                        }
                    )
                }
                if (!collectionReady || !report.available) Text("TXT and HTML export remain disabled until the complete OpenGL ES/EGL collection pass has finished.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("OpenGLESScope Database") {
                Text("Submit the complete technical OpenGLESScope report to the public database. Capability fields cannot be selectively omitted. OpenGLESScope does not include IMEI, Android ID, device serial, MAC addresses, account data, authentication tokens or private file paths.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
                ExpressiveActionButton(
                    title = if (submissionInFlight) "Submitting…" else "Submit complete report",
                    subtitle = if (submissionInFlight) "Uploading the complete technical dataset" else if (collectionReady) "Structured JSON + canonical TXT report" else "Waiting for complete OpenGL ES/EGL collection",
                    icon = R.drawable.ic_action_database,
                    enabled = collectionReady && report.available && !submissionInFlight,
                    onClick = {
                        if (!submissionInFlight && collectionReady && report.available) {
                            submissionInFlight = true
                            submitState = "Submitting complete technical report…"
                            scope.launch {
                                submitState = submitReport(context, report, display)
                                submissionInFlight = false
                            }
                        }
                    }
                )
                CapabilityKeyValue("Status", submitState)
                if (!collectionReady || !report.available) Text("Wait for the complete OpenGL ES/EGL collection pass to finish before submitting.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
                ExpressiveActionButton("Open OpenGLESScope Database", "Browse submitted reports", R.drawable.ic_action_database) { open(activity, DATABASE_WEB) }
                Text("Submission is explicit and user-initiated. The fixed official HTTPS endpoint is used automatically; no report is uploaded automatically or in the background.", color = ComposeColor(0xFF777777), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CapabilityListPage(title: String, rows: List<Pair<String, String>>) {
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CapabilitySectionCard(title) { CapabilityKeyValue("Entries", rows.size.toString()) } }
        items(rows, key = { it.first }) { (a, b) -> CapabilityItemCard { CapabilityKeyValue(a, b) } }
    }
}

@Composable
private fun SearchRows(title: String, rows: List<Pair<String, String>>) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues(), modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            CapabilitySectionCard(title) {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Matches", "${filtered.size} / ${rows.size}")
            }
        }
        items(filtered, key = { it.first }) { (a, b) -> CapabilityItemCard { CapabilityKeyValue(a, b) } }
    }
}

@Composable
private fun StatusRow(name: String, state: EvidenceState) {
    CapabilityItemCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(name, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            CapabilityStatusBadge(state.name.uppercase(), when (state) { EvidenceState.Supported -> true; EvidenceState.Unsupported -> false; EvidenceState.Unknown -> null })
        }
    }
}

@Composable
private fun UpdateStatusBanner(status: UpdateStatus, onInstall: (AppUpdate) -> Unit) {
    AnimatedVisibility(
        visible = status !is UpdateStatus.Hidden,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(360)) + shrinkVertically(animationSpec = tween(360))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            color = SurfaceRaised,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 0.dp
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (status) {
                    UpdateStatus.Checking -> { ExpressiveLinearProgressIndicator(Modifier.width(72.dp)); Text("Checking for updates…", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    UpdateStatus.UpToDate -> { CapabilityStatusBadge("UP TO DATE", true); Text("OpenGLESScope is up to date.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    UpdateStatus.DirectUpdatesDisabledIntro -> { CapabilityStatusBadge("INFO", true); Text("Direct GitHub updates are currently disabled. Obtainium can manage updates externally, or direct updates can be enabled in Settings.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    is UpdateStatus.Available -> { CapabilityStatusBadge("UPDATE", true); Text("OpenGLESScope ${status.update.version} available", modifier = Modifier.weight(1f)); ExpressiveTextButton("Review") { onInstall(status.update) } }
                    is UpdateStatus.Downloading -> { ExpressiveLinearProgressIndicator(Modifier.width(72.dp)); Text("Downloading update…", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    is UpdateStatus.Failed -> Text(status.message, color = ComposeColor(0xFFFF8A8A), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    UpdateStatus.Hidden -> Unit
                }
            }
        }
    }
}

@Composable
private fun CollectionStatusBanner(collecting: Boolean, completed: Boolean) {
    AnimatedVisibility(
        visible = collecting || completed,
        enter = fadeIn(animationSpec = tween(260)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(420)) + shrinkVertically(animationSpec = tween(420))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            color = SurfaceRaised,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 0.dp
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (collecting) {
                        ExpressiveAssistChip(label = "Collecting information…", leadingIcon = R.drawable.ic_action_update, enabled = false, onClick = {})
                        Text("OpenGLESScope is collecting OpenGL ES and EGL information in the background.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    } else {
                        Surface(shape = RoundedCornerShape(50), color = ComposeColor(0xFF163D24), modifier = Modifier.size(30.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = ComposeColor(0xFF55D98A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text("Completed", color = ComposeColor(0xFF55D98A), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text("OpenGL ES information updated.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                }
                if (collecting) ExpressiveLinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LoadingIndicator(color = BrandSoft, modifier = Modifier.size(48.dp))
            Text("Inspecting OpenGL ES…", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState(message: String) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(message, color = ComposeColor(0xFFBDBDBD)) } }

private fun capabilitySectionIcon(title: String): Int = when {
    title.contains("quick access", true) || title.contains("explore", true) -> R.drawable.ic_home
    title.contains("OpenGL", true) || title.contains("snapshot", true) || title.contains("inspection", true) -> R.drawable.ic_opengles_gl_es
    title.contains("display", true) || title.contains("HDR", true) -> R.drawable.ic_display
    title.equals("EGL runtime", true) -> R.drawable.ic_egl_official
    title.contains("EGL Config", true) || title.contains("config", true) -> R.drawable.ic_surface
    title.contains("EGL", true) -> R.drawable.ic_egl_official
    title.contains("feature", true) -> R.drawable.ic_features
    title.contains("format", true) -> R.drawable.ic_formats
    title.contains("extension", true) -> R.drawable.ic_extensions
    title.contains("property", true) || title.contains("limit", true) || title.contains("runtime", true) || title.contains("network", true) -> R.drawable.ic_properties
    else -> R.drawable.ic_info
}

@Composable
private fun tvBrowseModifier(shape: RoundedCornerShape): Modifier {
    val configuration = LocalConfiguration.current
    val isTelevision = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
    if (!isTelevision) return Modifier
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focused) { if (focused) requester.bringIntoView() }
    return Modifier
        .bringIntoViewRequester(requester)
        .onFocusChanged { state -> focused = state.isFocused }
        .focusable()
        .border(if (focused) 2.dp else 0.dp, if (focused) BrandSoft else ComposeColor.Transparent, shape)
}

@Composable
private fun ExpressiveIconButton(icon: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = SurfaceRaised,
            contentColor = TextPrimary
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ExpressiveSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
    placeholderText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        label = if (labelText == null) null else { { Text(labelText) } },
        placeholder = if (placeholderText == null) null else { { Text(placeholderText) } },
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = SurfaceTonal,
            unfocusedContainerColor = SurfaceLow,
            cursorColor = BrandSoft,
            focusedBorderColor = BrandSoft,
            unfocusedBorderColor = Outline,
            focusedLeadingIconColor = BrandSoft,
            unfocusedLeadingIconColor = TextMuted,
            focusedLabelColor = BrandSoft,
            unfocusedLabelColor = TextSecondary,
            focusedPlaceholderColor = TextSecondary,
            unfocusedPlaceholderColor = TextMuted
        )
    )
}

@Composable
private fun ExpressiveFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shapes = FilterChipDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            selectedShape = RoundedCornerShape(22.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium) },
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceLow,
            labelColor = TextSecondary,
            iconColor = TextMuted,
            selectedContainerColor = BrandContainer,
            selectedLabelColor = TextPrimary,
            selectedLeadingIconColor = BrandSoft
        )
    )
}

@Composable
private fun ExpressiveAssistChip(
    label: String,
    leadingIcon: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        leadingIcon = if (leadingIcon == null) null else {
            {
                Icon(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = SurfaceTonal,
            labelColor = TextPrimary,
            leadingIconContentColor = BrandSoft,
            disabledContainerColor = SurfaceLow,
            disabledLabelColor = TextMuted,
            disabledLeadingIconContentColor = TextMuted
        )
    )
}

@Composable
private fun ExpressiveSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = if (checked) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Brand,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = TextPrimary,
            checkedTrackColor = Brand,
            uncheckedThumbColor = TextSecondary,
            uncheckedTrackColor = SurfaceTonal,
            uncheckedBorderColor = Outline
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressivePrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(20.dp),
            pressedShape = RoundedCornerShape(26.dp)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brand,
            contentColor = TextPrimary
        )
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveTextButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        colors = ButtonDefaults.textButtonColors(contentColor = BrandSoft)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ExpressiveLinearProgressIndicator(modifier: Modifier = Modifier) {
    LinearWavyProgressIndicator(
        modifier = modifier.height(12.dp),
        color = BrandSoft,
        trackColor = ComposeColor(0xFF34212F)
    )
}

@Composable
private fun CapabilitySectionCard(title: String, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(30.dp)
    Surface(color = ComposeColor(0xFF181516), shape = shape, modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = ComposeColor(0xFF351B31)) {
                    Icon(painter = painterResource(capabilitySectionIcon(title)), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(10.dp).size(21.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ComposeColor(0xFFF7F2F3), modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = ComposeColor(0xFF2A2527))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
        }
    }
}

@Composable
private fun CapabilityItemCard(containerColor: ComposeColor = ComposeColor(0xFF181516), content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Surface(color = containerColor, shape = shape, modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { content() }
    }
}

@Composable
private fun CapabilityKeyValue(key: String, value: String) {
    val shape = RoundedCornerShape(12.dp)
    Row(Modifier.fillMaxWidth().then(tvBrowseModifier(shape)), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(key, color = ComposeColor(0xFF8F8F8F), modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall)
        Text(value.ifBlank { "Unavailable" }, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End, fontFamily = if (key.startsWith("GL_") || key.startsWith("EGL_")) FontFamily.Monospace else FontFamily.Default, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CapabilityStatusBadge(label: String, positive: Boolean? = null) {
    val background = when (positive) { true -> ComposeColor(0xFF163D24); false -> ComposeColor(0xFF482027); null -> ComposeColor(0xFF3A321A) }
    val foreground = when (positive) { true -> ComposeColor(0xFF55D98A); false -> ComposeColor(0xFFFF8A9A); null -> ComposeColor(0xFFFFC857) }
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(label, color = foreground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
    }
}

@Composable
private fun ExpressiveActionButton(title: String, subtitle: String, icon: Int, modifier: Modifier = Modifier.fillMaxWidth(), enabled: Boolean = true, compact: Boolean = false, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(if (compact) 26.dp else 28.dp)
    val container = if (!enabled) ComposeColor(0xFF111111) else if (focused) ComposeColor(0xFF2B1726) else ComposeColor(0xFF1A1718)
    val iconContainer = if (enabled) ComposeColor(0xFF351B31) else ComposeColor(0xFF181818)
    val accent = if (enabled) BrandSoft else ComposeColor(0xFF606064)
    val titleColor = if (enabled) ComposeColor(0xFFF7F2F3) else ComposeColor(0xFF6C696A)
    val detailColor = if (enabled) ComposeColor(0xFFB6ACAE) else ComposeColor(0xFF5A5758)
    Card(onClick = onClick, enabled = enabled, colors = CardDefaults.cardColors(containerColor = container), shape = shape, modifier = modifier.onFocusChanged { focused = it.isFocused }.border(if (focused && enabled) 2.dp else 0.dp, if (focused && enabled) BrandSoft else ComposeColor.Transparent, shape)) {
        if (compact) {
            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(17.dp), color = iconContainer) { Icon(painterResource(icon), null, tint = accent, modifier = Modifier.padding(10.dp).size(21.dp)) }
                    Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF28191F)) { Text("›", color = accent, fontSize = 21.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, color = titleColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = detailColor, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = iconContainer) { Icon(painterResource(icon), null, tint = accent, modifier = Modifier.padding(11.dp).size(22.dp)) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, color = titleColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = detailColor, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF28191F)) { Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = accent, modifier = Modifier.padding(9.dp).size(19.dp)) }
            }
        }
    }
}

@Composable
private fun ExpressiveIdentityBlock(title: String, subtitle: String, icon: Int) {
    Surface(color = ComposeColor(0xFF181516), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = ComposeColor(0xFF351B31)) { Icon(painterResource(icon), null, tint = BrandSoft, modifier = Modifier.padding(12.dp).size(24.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ComposeColor(0xFFF7F2F3))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ComposeColor(0xFFB6ACAE))
            }
        }
    }
}

@Composable
private fun ExpressiveVersionBlock(application: String, version: String, versionCode: String, packageName: String, abi: String) {
    val shape = RoundedCornerShape(30.dp)
    Surface(color = ComposeColor(0xFF181516), shape = shape, modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Surface(shape = RoundedCornerShape(19.dp), color = ComposeColor(0xFF351B31)) { Icon(painterResource(R.drawable.ic_info), null, tint = BrandSoft, modifier = Modifier.padding(11.dp).size(23.dp)) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(application, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Version $version", color = BrandSoft, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF272224)) { Text("#$versionCode", color = ComposeColor(0xFFC7BEC0), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveInfoPill("Installed ABI", abi, Modifier.weight(1f))
                ExpressiveInfoPill("Package", packageName, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExpressiveInfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = ComposeColor(0xFF211E1F), shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = ComposeColor(0xFF968D8F), style = MaterialTheme.typography.labelSmall)
            Text(value, color = ComposeColor(0xFFE7DFE1), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier) {
    val shape = RoundedCornerShape(26.dp)
    Surface(color = ComposeColor(0xFF181516), shape = shape, modifier = modifier.then(tvBrowseModifier(shape))) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF2A2022)) { Text(title, color = BrandSoft, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)) }
            Text(value, color = ComposeColor(0xFFF7F2F3), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UpdateConfirmationDialog(update: AppUpdate, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = SurfaceRaised,
        tonalElevation = 0.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Download OpenGLESScope ${update.version}?", fontWeight = FontWeight.SemiBold)
                Text("Review the target build and release notes before any APK download starts.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(20.dp), color = ComposeColor(0xFF1A1718)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        CapabilityKeyValue("Installed version", "${update.installedVersion} (versionCode ${update.installedVersionCode})")
                        CapabilityKeyValue("Available release", update.version)
                        CapabilityKeyValue("Installed ABI", update.installedAbi)
                        CapabilityKeyValue("Download ABI", update.downloadAbi)
                        CapabilityKeyValue("APK asset", update.assetName)
                        CapabilityKeyValue("Downloaded versionCode", "Verified from the APK before installation")
                    }
                }
                Text("Release notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(20.dp), color = ComposeColor(0xFF0D0D0D)) { ReleaseNotesContent(update.releaseNotes, Modifier.fillMaxWidth().heightIn(max = 360.dp)) }
                Text("The APK is validated for official release provenance, package identity, signing certificate, versionCode and versionName before Android's installer is opened.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { ExpressivePrimaryButton("Download APK", onConfirm) },
        dismissButton = { ExpressiveTextButton("Cancel", onDismiss) }
    )
}

@Composable
private fun ReleaseNotesContent(markdown: String, modifier: Modifier = Modifier) {
    val lines = remember(markdown) { markdown.lines() }
    LazyColumn(modifier = modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        items(lines) { raw ->
            val line = raw.trimEnd()
            when {
                line.isBlank() -> Spacer(Modifier.height(3.dp))
                line.startsWith("### ") -> Text(line.removePrefix("### "), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = ComposeColor(0xFFF3EDEF))
                line.startsWith("## ") -> Text(line.removePrefix("## "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ComposeColor(0xFFF7F2F3))
                line.startsWith("# ") -> Text(line.removePrefix("# "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ComposeColor.White)
                line.startsWith("- ") || line.startsWith("* ") -> Text("• " + line.drop(2), color = ComposeColor(0xFFD3CBCD), style = MaterialTheme.typography.bodySmall)
                line.startsWith("> ") -> Text(line.drop(2), color = BrandSoft, style = MaterialTheme.typography.bodySmall)
                line.startsWith("```") -> Spacer(Modifier.height(1.dp))
                else -> Text(line, color = ComposeColor(0xFFBEB6B8), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
private fun detectInstalledAbi(context: Context): String {
    val nativeDir = context.applicationInfo.nativeLibraryDir.orEmpty().lowercase()
    return when {
        nativeDir.contains("arm64") -> "arm64-v8a"
        nativeDir.contains("armeabi-v7a") || nativeDir.endsWith("/arm") -> "armeabi-v7a"
        nativeDir.contains("x86_64") -> "x86_64"
        nativeDir.contains("x86") -> "x86"
        else -> Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
    }
}

private fun parseVersion(s: String): Int { val m = Regex("OpenGL ES (\\d+)\\.(\\d+)").find(s) ?: return 0; return (m.groupValues[1].toIntOrNull() ?: 0) * 100 + (m.groupValues[2].toIntOrNull() ?: 0) * 10 }
private fun shortGlVersion(s: String): String = Regex("OpenGL ES \\d+\\.\\d+").find(s)?.value ?: s.take(24)
private fun open(c: Context, url: String) { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

private val IPV6_FIRST_DNS = Dns { hostname ->
    Dns.SYSTEM.lookup(hostname).sortedWith(compareBy<InetAddress> { if (it is Inet6Address) 0 else 1 })
}

private val HTTP_CLIENT: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .dns(IPV6_FIRST_DNS)
        .fastFallback(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}

private val DATABASE_HTTP_CLIENT: OkHttpClient by lazy {
    HTTP_CLIENT.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
}

private val UPDATE_DOWNLOAD_CLIENT: OkHttpClient by lazy {
    HTTP_CLIENT.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.MINUTES)
        .build()
}
private fun readResponseTextLimited(body: ResponseBody, maxBytes: Int): String {
    require(maxBytes > 0)
    val declared = body.contentLength()
    if (declared > maxBytes) error("Response exceeds the safety limit.")
    val output = ByteArrayOutputStream(minOf(maxBytes, 64 * 1024))
    body.byteStream().use { input -> val buffer = ByteArray(8192); var total = 0; while (true) { val count = input.read(buffer); if (count < 0) break; total += count; if (total > maxBytes) error("Response exceeds the safety limit."); output.write(buffer, 0, count) } }
    return output.toString(Charsets.UTF_8.name())
}

private fun compareVersions(a: String, b: String): Int {
    fun parse(value: String): Pair<List<Int>, List<String>?> {
        val withoutBuild = value.substringBefore('+')
        val core = withoutBuild.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val suffix = withoutBuild.substringAfter('-', "").takeIf { it.isNotEmpty() }?.split('.')
        return core to suffix
    }
    val (aa, ap) = parse(a)
    val (bb, bp) = parse(b)
    for (i in 0 until maxOf(aa.size, bb.size)) {
        val x = aa.getOrElse(i) { 0 }
        val y = bb.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    if (ap == null && bp != null) return 1
    if (ap != null && bp == null) return -1
    if (ap == null || bp == null) return 0
    for (i in 0 until maxOf(ap.size, bp.size)) {
        val x = ap.getOrNull(i) ?: return -1
        val y = bp.getOrNull(i) ?: return 1
        val xn = x.toIntOrNull()
        val yn = y.toIntOrNull()
        val c = when {
            xn != null && yn != null -> xn.compareTo(yn)
            xn != null -> -1
            yn != null -> 1
            else -> x.compareTo(y)
        }
        if (c != 0) return c
    }
    return 0
}

private suspend fun submitReport(context: Context, report: GlReport, display: DisplayInfo): String = withContext(Dispatchers.IO) {
    try {
        if (!report.available) return@withContext "Submission unavailable until a complete OpenGL ES report exists"
        val base = DATABASE_API.toHttpUrlOrNull() ?: return@withContext "The official OpenGLESScope Database endpoint is invalid"
        if (base.scheme != "https" || base.host != "openglesscope-database-api.openglesscope.workers.dev" || base.username.isNotEmpty() || base.password.isNotEmpty() || base.query != null || base.fragment != null || base.encodedPath != "/") return@withContext "The official OpenGLESScope Database endpoint is invalid"
        val payload = submissionJson(context, report, display).toString()
        if (payload.toByteArray().size > 2 * 1024 * 1024) return@withContext "Report exceeds the 2 MiB transport limit; no data was truncated"
        val req = Request.Builder().url(base.newBuilder().addPathSegments("v1/reports").build()).header("Accept", "application/json")
            .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        DATABASE_HTTP_CLIENT.newCall(req).execute().use { res ->
            val body = readResponseTextLimited(res.body, 64 * 1024)
            if (res.isSuccessful) {
                val id = runCatching { JSONObject(body).optString("id") }.getOrDefault("")
                if (id.matches(Regex("[a-f0-9]{64}"))) context.getSharedPreferences("analysis_tools", Context.MODE_PRIVATE).edit().putString("last_database_report_id", id).apply()
                if (id.isBlank()) "Report submitted successfully." else "Report submitted successfully · ${id.take(12)}"
            } else {
                val message = runCatching { JSONObject(body).optString("error") }.getOrDefault("").ifBlank { "HTTP ${res.code}" }
                "Submission failed: $message"
            }
        }
    } catch (e: Exception) { "Submission failed · ${e.message ?: "network error"}" }
}

private fun submissionJson(context: Context, r: GlReport, d: DisplayInfo): JSONObject {
    val text = reportText(context, r, d)
    val configs = JSONArray(r.eglConfigs.map { c ->
        JSONObject()
            .put("id", c.id)
            .putNullable("red", c.red).putNullable("green", c.green).putNullable("blue", c.blue).putNullable("alpha", c.alpha)
            .putNullable("depth", c.depth).putNullable("stencil", c.stencil).putNullable("sampleBuffers", c.sampleBuffers).putNullable("samples", c.samples)
            .putNullable("surfaceType", c.surfaceType).putNullable("renderableType", c.renderableType).putNullable("conformant", c.conformant)
            .putNullable("configCaveat", c.configCaveat).putNullable("colorBufferType", c.colorBufferType).putNullable("level", c.level)
            .putNullable("nativeRenderable", c.nativeRenderable).putNullable("nativeVisualId", c.nativeVisualId)
            .putNullable("minSwapInterval", c.minSwapInterval).putNullable("maxSwapInterval", c.maxSwapInterval)
            .putNullable("bufferSize", c.bufferSize).putNullable("luminanceSize", c.luminanceSize).putNullable("alphaMaskSize", c.alphaMaskSize)
            .putNullable("bindToTextureRgb", c.bindToTextureRgb).putNullable("bindToTextureRgba", c.bindToTextureRgba)
            .putNullable("maxPbufferWidth", c.maxPbufferWidth).putNullable("maxPbufferHeight", c.maxPbufferHeight).putNullable("maxPbufferPixels", c.maxPbufferPixels)
            .putNullable("nativeVisualType", c.nativeVisualType).putNullable("transparentType", c.transparentType)
            .putNullable("transparentRed", c.transparentRed).putNullable("transparentGreen", c.transparentGreen).putNullable("transparentBlue", c.transparentBlue)
            .putNullable("recordableAndroid", c.recordableAndroid).putNullable("framebufferTargetAndroid", c.framebufferTargetAndroid).putNullable("colorComponentTypeExt", c.colorComponentTypeExt)
            .put("unavailableAttributes", JSONArray(c.unavailableAttributes.map { JSONObject().put("name", it.name).put("error", it.error) }))
    })
    return JSONObject().apply {
        put("schemaVersion", 2)
        put("application", JSONObject().put("name", "OpenGLESScope").put("packageName", "com.efishell.openglesscope").put("version", BuildConfig.VERSION_NAME).put("versionCode", BuildConfig.VERSION_CODE).put("applicationAbi", detectInstalledAbi(context)).put("supportedDeviceAbis", JSONArray(Build.SUPPORTED_ABIS.toList())))
        put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("product", Build.PRODUCT).put("androidRelease", Build.VERSION.RELEASE).put("sdk", Build.VERSION.SDK_INT).apply { if (Build.VERSION.SECURITY_PATCH.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) put("securityPatch", Build.VERSION.SECURITY_PATCH) })
        put("gpu", JSONObject().put("name", r.renderer).put("vendor", r.vendor))
        put("driver", JSONObject().put("mode", "System OpenGL ES/EGL").put("version", "Unavailable (OpenGL ES does not expose a standardized driver-version query)"))
        put("opengles", JSONObject().put("version", r.glVersion).put("major", r.glMajor).put("minor", r.glMinor).put("glslVersion", r.glslVersion).put("extensions", JSONArray(r.extensions)).put("extensionCount", r.extensions.size))
        put("egl", JSONObject().put("vendor", r.egl.vendor).put("version", r.egl.version).put("initializedVersion", r.egl.initializedVersion).put("clientApis", r.egl.clientApis).put("extensions", JSONArray(r.egl.extensions)).put("clientExtensions", JSONArray(r.egl.clientExtensions)).put("extensionCount", r.egl.extensions.size).put("clientExtensionCount", r.egl.clientExtensions.size))
        put("display", JSONObject().put("name", d.name).putNullable("modeId", d.modeId).putNullable("width", d.width).putNullable("height", d.height).putNullable("refreshRate", d.refreshRate).put("supportedModes", JSONArray(d.supportedModes)).putNullable("wideColor", d.wideColor).put("hdrTypes", JSONArray(d.hdrTypes)).putNullable("desiredMaxLuminance", d.desiredMaxLuminance).putNullable("desiredMaxAverageLuminance", d.desiredMaxAverageLuminance).putNullable("desiredMinLuminance", d.desiredMinLuminance))
        put("collection", JSONObject().put("status", if (r.available) "available" else "unavailable").put("complete", r.available).put("source", "active Android system EGL/OpenGL ES implementation"))
        put("technicalReport", JSONObject()
            .put("schemaVersion", 2)
            .put("eglRuntime", JSONObject()
                .put("boundApi", r.eglRuntime.boundApi).putNullable("configId", r.eglRuntime.configId).putNullable("clientType", r.eglRuntime.clientType).putNullable("clientVersion", r.eglRuntime.clientVersion).putNullable("renderBuffer", r.eglRuntime.renderBuffer)
                .put("currentContext", r.eglRuntime.currentContext).put("currentDisplay", r.eglRuntime.currentDisplay).put("currentDrawSurface", r.eglRuntime.currentDrawSurface).put("currentReadSurface", r.eglRuntime.currentReadSurface)
                .putNullable("surfaceWidth", r.eglRuntime.surfaceWidth).putNullable("surfaceHeight", r.eglRuntime.surfaceHeight).putNullable("surfaceRenderBuffer", r.eglRuntime.surfaceRenderBuffer).putNullable("surfaceSwapBehavior", r.eglRuntime.surfaceSwapBehavior)
                .putNullable("surfaceTextureFormat", r.eglRuntime.surfaceTextureFormat).putNullable("surfaceTextureTarget", r.eglRuntime.surfaceTextureTarget).putNullable("surfaceMipmapTexture", r.eglRuntime.surfaceMipmapTexture).putNullable("surfaceMipmapLevel", r.eglRuntime.surfaceMipmapLevel).putNullable("surfaceMultisampleResolve", r.eglRuntime.surfaceMultisampleResolve)
                .put("unavailableAttributes", JSONArray(r.eglRuntime.unavailableAttributes.map { JSONObject().put("name", it.name).put("error", it.error) })))
            .put("limits", JSONArray(r.limits.map { JSONObject().put("name", it.name).put("value", it.value) }))
            .put("extensions", JSONArray(r.extensions))
            .put("eglExtensions", JSONArray(r.egl.extensions))
            .put("eglClientExtensions", JSONArray(r.egl.clientExtensions))
            .put("compressedFormats", JSONArray(r.compressedFormats))
            .put("shaderBinaryFormats", JSONArray(r.shaderBinaryFormats))
            .put("programBinaryFormats", JSONArray(r.programBinaryFormats))
            .put("precision", JSONArray(r.precision.map { JSONObject().put("shader", it.shader).put("type", it.type).put("rangeMin", it.rangeMin).put("rangeMax", it.rangeMax).put("precision", it.precision) }))
            .put("queryDiagnostics", JSONArray(r.diagnostics.map { JSONObject().put("name", it.name).put("status", it.status).put("detail", it.detail) }))
            .put("eglConfigs", configs)
            .put("display", JSONObject().put("name", d.name).putNullable("modeId", d.modeId).putNullable("width", d.width).putNullable("height", d.height).putNullable("refreshRate", d.refreshRate).put("supportedModes", JSONArray(d.supportedModes)).putNullable("wideColor", d.wideColor).put("hdrTypes", JSONArray(d.hdrTypes)).putNullable("desiredMaxLuminance", d.desiredMaxLuminance).putNullable("desiredMaxAverageLuminance", d.desiredMaxAverageLuminance).putNullable("desiredMinLuminance", d.desiredMinLuminance)))
        put("reportText", text)
    }
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject = put(name, value ?: JSONObject.NULL)

private data class ExportPayload(val filename: String, val content: String, val mime: String)

private fun isTvDevice(context: Context): Boolean {
    val uiModeType = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) || uiModeType == Configuration.UI_MODE_TYPE_TELEVISION
}

private fun exportWithSafOrDownloads(context: Context, filename: String, content: String, mime: String, launcher: ActivityResultLauncher<String>, onDownloadsFallback: (ExportPayload) -> Unit) {
    val payload = ExportPayload(filename, content, mime)
    if (isTvDevice(context)) {
        onDownloadsFallback(payload)
        return
    }
    runCatching { launcher.launch(filename) }
        .onFailure { error ->
            Log.w("OpenGLESScope", "Document picker unavailable; using Downloads", error)
            onDownloadsFallback(payload)
        }
}

private fun writeExportToDownloads(context: Context, filename: String, content: String, mime: String): String {
    val success = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Unable to create the Downloads entry")
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                    ?: throw IllegalStateException("Unable to open the Downloads entry")
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            } catch (error: Throwable) {
                context.contentResolver.delete(uri, null, null)
                throw error
            }
        } else {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists() && !directory.mkdirs()) throw IllegalStateException("Unable to create the Downloads directory")
            val target = uniqueDownloadFile(directory, filename)
            FileOutputStream(target).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        }
    }.isSuccess
    val message = if (success) {
        if (mime == "text/html") "HTML report saved to Downloads" else "TXT report saved to Downloads"
    } else {
        if (mime == "text/html") "HTML report could not be saved" else "TXT report could not be saved"
    }
    return message
}

private fun uniqueDownloadFile(directory: File, filename: String): File {
    val original = File(directory, filename)
    if (!original.exists()) return original
    val dot = filename.lastIndexOf('.')
    val base = if (dot > 0) filename.substring(0, dot) else filename
    val extension = if (dot > 0) filename.substring(dot) else ""
    var index = 2
    while (true) {
        val candidate = File(directory, "${base} (${index})${extension}")
        if (!candidate.exists()) return candidate
        index++
    }
}

private fun writeExport(context: Context, uri: Uri, content: String, mime: String): String {
    val success = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
            ?: throw IllegalStateException("Unable to open the selected destination")
    }.isSuccess
    val message = if (success) {
        if (mime == "text/html") "HTML report saved successfully" else "TXT report saved successfully"
    } else {
        if (mime == "text/html") "HTML report could not be saved" else "TXT report could not be saved"
    }
    return message
}

private fun reportText(context: Context, r: GlReport, d: DisplayInfo): String = buildString {
    val packageInfo = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    val appVersionName = packageInfo?.versionName ?: BuildConfig.VERSION_NAME
    val appVersionCode = if (packageInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION") packageInfo?.versionCode?.toString() ?: BuildConfig.VERSION_CODE.toString()
    }
    val applicationAbi = detectInstalledAbi(context)
    appendLine("OpenGLESScope report")
    appendLine("==================")
    appendLine("Application: OpenGLESScope")
    appendLine("Application version: $appVersionName")
    appendLine("Application version code: $appVersionCode")
    appendLine("Application package: ${context.packageName}")
    appendLine("Application ABI: $applicationAbi")
    appendLine("Developer: Semih Boran")
    appendLine("Nickname: EFI Shell")
    appendLine("GitHub: https://github.com/EFIShell0")
    appendLine("GPU: ${r.renderer.ifBlank { "Unavailable" }}")
    appendLine("Driver mode: System OpenGL ES/EGL")
    appendLine("Driver version: Unavailable (OpenGL ES does not expose a standardized driver-version query)")
    appendLine("OpenGL ES: ${r.glVersion.ifBlank { "Unavailable" }}")
    appendLine("EGL: ${r.egl.initializedVersion.ifBlank { r.egl.version.ifBlank { "Unavailable" } }}")
    appendLine("Display: ${if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width}x${d.height} @ ${d.refreshRate?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "Unavailable"} Hz" else d.name.ifBlank { "Unavailable" }}")
    appendLine("HDR types: ${d.hdrTypes.joinToString(", ").ifBlank { "Unavailable" }}")
    appendLine("Android: ${Build.MANUFACTURER} ${Build.MODEL}, ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
    appendLine("Android security patch: ${Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" }}")
    appendLine("Supported device ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
    appendLine("Collection status: ${if (r.available) "Available" else "Unavailable"}")
    appendLine("Collection source: active Android system EGL/OpenGL ES implementation")
    appendLine()
    appendLine("DEVICE")
    appendLine("Manufacturer: ${Build.MANUFACTURER.ifBlank { "Unavailable" }}")
    appendLine("Brand: ${Build.BRAND.ifBlank { "Unavailable" }}")
    appendLine("Model: ${Build.MODEL.ifBlank { "Unavailable" }}")
    appendLine("Product: ${Build.PRODUCT.ifBlank { "Unavailable" }}")
    appendLine("Device: ${Build.DEVICE.ifBlank { "Unavailable" }}")
    appendLine("Board: ${Build.BOARD.ifBlank { "Unavailable" }}")
    appendLine("Hardware: ${Build.HARDWARE.ifBlank { "Unavailable" }}")
    appendLine("Android: ${Build.VERSION.RELEASE.ifBlank { "Unavailable" }} / API ${Build.VERSION.SDK_INT}")
    appendLine("Codename: ${Build.VERSION.CODENAME.ifBlank { "Unavailable" }}")
    appendLine("Build ID: ${Build.ID.ifBlank { "Unavailable" }}")
    appendLine("Incremental: ${Build.VERSION.INCREMENTAL.ifBlank { "Unavailable" }}")
    appendLine("Security patch: ${Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" }}")
    appendLine("Fingerprint: ${Build.FINGERPRINT.ifBlank { "Unavailable" }}")
    appendLine()
    appendLine("OPENGL ES")
    appendLine("GL_RENDERER: ${r.renderer}")
    appendLine("GL_VENDOR: ${r.vendor}")
    appendLine("GL_VERSION: ${r.glVersion}")
    appendLine("Core version: ${r.glMajor}.${r.glMinor}")
    appendLine("Core version provenance: ${coreVersionProvenance(r)}")
    appendLine("GL_SHADING_LANGUAGE_VERSION: ${r.glslVersion}")
    appendLine()
    appendLine("EGL")
    appendLine("EGL_VENDOR: ${r.egl.vendor}")
    appendLine("EGL_VERSION: ${r.egl.version}")
    appendLine("Initialized EGL version: ${r.egl.initializedVersion}")
    appendLine("EGL_CLIENT_APIS: ${r.egl.clientApis}")
    appendLine("Bound client API: ${r.eglRuntime.boundApi}")
    appendLine("Current config ID: ${r.eglRuntime.configId ?: "Unavailable"}")
    appendLine("Context client type: ${r.eglRuntime.clientType ?: "Unavailable"}")
    appendLine("Context client version: ${r.eglRuntime.clientVersion ?: "Unavailable"}")
    appendLine("Context render buffer: ${r.eglRuntime.renderBuffer ?: "Unavailable"}")
    appendLine("Current EGL bindings: context=${r.eglRuntime.currentContext}, display=${r.eglRuntime.currentDisplay}, draw=${r.eglRuntime.currentDrawSurface}, read=${r.eglRuntime.currentReadSurface}")
    appendLine("Pbuffer: ${r.eglRuntime.surfaceWidth ?: "?"}x${r.eglRuntime.surfaceHeight ?: "?"} · render=${r.eglRuntime.surfaceRenderBuffer ?: "Unavailable"} · swap=${r.eglRuntime.surfaceSwapBehavior ?: "Unavailable"} · texture=${r.eglRuntime.surfaceTextureFormat ?: "Unavailable"}/${r.eglRuntime.surfaceTextureTarget ?: "Unavailable"}")
    if (r.eglRuntime.unavailableAttributes.isNotEmpty()) appendLine("Unavailable EGL runtime attributes: ${r.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }}")
    appendLine()
    appendLine("DISPLAY & HDR")
    appendLine("Display: ${d.name}")
    appendLine("Current mode: ${d.modeId?.toString() ?: "Unavailable"} | ${if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width}x${d.height}" else "Unavailable"} | ${d.refreshRate?.let { "$it Hz" } ?: "Unavailable"}")
    appendLine("Supported display modes (${d.supportedModes.size}): ${if (d.supportedModes.isEmpty()) "Unavailable" else d.supportedModes.joinToString(" | ")}")
    appendLine("Refresh rate: ${d.refreshRate?.let { "$it Hz" } ?: "Unavailable"}")
    appendLine("Wide color gamut: ${when (d.wideColor) { true -> "Reported by Android display API"; false -> "Not supported by Android display API"; null -> "Unavailable on this Android API/display context" }}")
    appendLine("HDR types: ${if (d.hdrTypes.isEmpty()) "Unavailable" else d.hdrTypes.joinToString()}")
    appendLine("Desired max luminance: ${d.desiredMaxLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine("Desired max average luminance: ${d.desiredMaxAverageLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine("Desired min luminance: ${d.desiredMinLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine()
    appendLine("OPENGL ES LIMITS (${r.limits.size})")
    r.limits.forEach { appendLine("${it.name}: ${it.value}") }
    appendLine()
    appendLine("OPENGL ES EXTENSIONS (${r.extensions.size})")
    r.extensions.forEach { appendLine(it) }
    appendLine()
    appendLine("EGL DISPLAY EXTENSIONS (${r.egl.extensions.size})")
    r.egl.extensions.forEach { appendLine(it) }
    appendLine()
    appendLine("EGL CLIENT EXTENSIONS (${r.egl.clientExtensions.size})")
    r.egl.clientExtensions.forEach { appendLine(it) }
    appendLine()
    appendLine("COMPRESSED TEXTURE FORMATS (${r.compressedFormats.size})")
    r.compressedFormats.forEach { appendLine(it) }
    appendLine()
    appendLine("SHADER BINARY FORMATS (${r.shaderBinaryFormats.size})")
    r.shaderBinaryFormats.forEach { appendLine(it) }
    appendLine()
    appendLine("PROGRAM BINARY FORMATS (${r.programBinaryFormats.size})")
    r.programBinaryFormats.forEach { appendLine(it) }
    appendLine()
    appendLine("SHADER PRECISION (${r.precision.size})")
    r.precision.forEach { appendLine("${it.shader} | ${it.type} | range ${it.rangeMin}..${it.rangeMax} | precision ${it.precision}") }
    appendLine()
    appendLine("QUERY DIAGNOSTICS (${r.diagnostics.size})")
    r.diagnostics.forEach { appendLine("${it.name}: ${it.status}${if (it.detail.isBlank()) "" else " · ${it.detail}"}") }
    appendLine()
    appendLine("EGL CONFIGS (${r.eglConfigs.size})")
    r.eglConfigs.forEach { c ->
        appendLine("${c.id} | RGBA ${c.red ?: "?"}/${c.green ?: "?"}/${c.blue ?: "?"}/${c.alpha ?: "?"} | bufferSize ${c.bufferSize ?: "?"} | luminance ${c.luminanceSize ?: "?"} | alphaMask ${c.alphaMaskSize ?: "?"} | depth ${c.depth ?: "?"} | stencil ${c.stencil ?: "?"} | sampleBuffers ${c.sampleBuffers ?: "?"} | samples ${c.samples ?: "?"} | surface ${c.surfaceType ?: "Unavailable"} | renderable ${c.renderableType ?: "Unavailable"} | conformant ${c.conformant ?: "Unavailable"} | caveat ${c.configCaveat ?: "Unavailable"} | colorBuffer ${c.colorBufferType ?: "Unavailable"} | bindRGB ${eglBooleanLabel(c.bindToTextureRgb)} | bindRGBA ${eglBooleanLabel(c.bindToTextureRgba)} | maxPbuffer ${c.maxPbufferWidth ?: "?"}x${c.maxPbufferHeight ?: "?"}/${c.maxPbufferPixels ?: "?"}px | level ${c.level ?: "?"} | nativeRenderable ${eglBooleanLabel(c.nativeRenderable)} | nativeVisualId ${c.nativeVisualId ?: "?"} | nativeVisualType ${c.nativeVisualType ?: "?"} | transparent ${c.transparentType ?: "Unavailable"} ${c.transparentRed ?: "?"}/${c.transparentGreen ?: "?"}/${c.transparentBlue ?: "?"} | swap ${c.minSwapInterval ?: "?"}..${c.maxSwapInterval ?: "?"}")
    }
}

private fun reportHtml(context: Context, r: GlReport, d: DisplayInfo): String {
    fun e(s: Any?): String = (s?.toString() ?: "Unavailable").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
    fun statusClass(value: String): String {
        val v = value.trim().lowercase(java.util.Locale.ROOT)
        return when {
            v == "available" || v == "supported" || v == "true" || v == "yes" -> "yes"
            v == "unavailable" -> "unavailable"
            v == "unsupported" || v == "false" || v == "no" -> "no"
            v.contains("not applicable") -> "neutral"
            v.contains("unknown") -> "unknown"
            else -> "available"
        }
    }
    fun badge(value: String): String = "<span class=\"badge ${statusClass(value)}\">${e(value.uppercase(java.util.Locale.ROOT))}</span>"
    fun rows(values: List<Pair<String, Any?>>): String = values.joinToString("") { "<tr><th>${e(it.first)}</th><td>${e(it.second)}</td></tr>" }
    fun listRows(values: List<String>): String = values.joinToString("") { "<tr><td class=\"code\">${e(it)}</td></tr>" }
    val logoData = runCatching {
        context.resources.openRawResource(R.drawable.openglesscope_logo_horizontal).use { input -> Base64.encodeToString(input.readBytes(), Base64.NO_WRAP) }
    }.getOrNull()
    val packageInfo = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    val appVersionName = packageInfo?.versionName ?: BuildConfig.VERSION_NAME
    val appVersionCode = if (packageInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION") packageInfo?.versionCode?.toString() ?: BuildConfig.VERSION_CODE.toString()
    }
    val applicationAbi = detectInstalledAbi(context)
    val supportedDeviceAbis = Build.SUPPORTED_ABIS.joinToString(", ")
    val applicationRows = listOf(
        "Version" to e(appVersionName),
        "Version code" to e(appVersionCode),
        "Package" to e(context.packageName),
        "Application ABI" to e(applicationAbi),
        "Supported device ABIs" to e(supportedDeviceAbis),
        "Developer" to "Semih Boran",
        "Nickname" to "EFI Shell",
        "GitHub" to "<a class=\"github-link\" href=\"https://github.com/EFIShell0\" rel=\"noopener noreferrer\">github.com/EFIShell0</a>"
    )
    val deviceRows = rows(listOf(
        "Manufacturer" to Build.MANUFACTURER.ifBlank { "Unavailable" }, "Brand" to Build.BRAND.ifBlank { "Unavailable" }, "Model" to Build.MODEL.ifBlank { "Unavailable" },
        "Product" to Build.PRODUCT.ifBlank { "Unavailable" }, "Device" to Build.DEVICE.ifBlank { "Unavailable" }, "Board" to Build.BOARD.ifBlank { "Unavailable" },
        "Hardware" to Build.HARDWARE.ifBlank { "Unavailable" }, "Android" to Build.VERSION.RELEASE.ifBlank { "Unavailable" }, "Codename" to Build.VERSION.CODENAME.ifBlank { "Unavailable" },
        "SDK" to Build.VERSION.SDK_INT, "Build ID" to Build.ID.ifBlank { "Unavailable" }, "Incremental" to Build.VERSION.INCREMENTAL.ifBlank { "Unavailable" },
        "Security patch" to Build.VERSION.SECURITY_PATCH.ifBlank { "Unavailable" }, "Fingerprint" to Build.FINGERPRINT.ifBlank { "Unavailable" }
    ))
    val glRows = rows(listOf("Driver mode" to "System OpenGL ES/EGL", "Driver version" to "Unavailable (OpenGL ES does not expose a standardized driver-version query)", "GL_RENDERER" to r.renderer, "GL_VENDOR" to r.vendor, "GL_VERSION" to r.glVersion, "Core version" to "${r.glMajor}.${r.glMinor}", "Core version provenance" to coreVersionProvenance(r), "GL_SHADING_LANGUAGE_VERSION" to r.glslVersion))
    val eglRows = rows(listOf(
        "EGL_VENDOR" to r.egl.vendor, "EGL_VERSION" to r.egl.version, "Initialized EGL version" to r.egl.initializedVersion, "EGL_CLIENT_APIS" to r.egl.clientApis,
        "Bound client API" to r.eglRuntime.boundApi, "Current config ID" to (r.eglRuntime.configId?.toString() ?: "Unavailable"), "Context client type" to (r.eglRuntime.clientType ?: "Unavailable"),
        "Context client version" to (r.eglRuntime.clientVersion?.toString() ?: "Unavailable"), "Context render buffer" to (r.eglRuntime.renderBuffer ?: "Unavailable"),
        "Current EGL bindings" to "context=${r.eglRuntime.currentContext}, display=${r.eglRuntime.currentDisplay}, draw=${r.eglRuntime.currentDrawSurface}, read=${r.eglRuntime.currentReadSurface}",
        "Pbuffer size" to if (r.eglRuntime.surfaceWidth != null && r.eglRuntime.surfaceHeight != null) "${r.eglRuntime.surfaceWidth} × ${r.eglRuntime.surfaceHeight}" else "Unavailable",
        "Pbuffer render buffer" to (r.eglRuntime.surfaceRenderBuffer ?: "Unavailable"), "Pbuffer swap behavior" to (r.eglRuntime.surfaceSwapBehavior ?: "Unavailable"),
        "Pbuffer texture" to "${r.eglRuntime.surfaceTextureFormat ?: "Unavailable"} / ${r.eglRuntime.surfaceTextureTarget ?: "Unavailable"}",
        "Unavailable EGL runtime attributes" to r.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }
    ))
    val displayRows = rows(listOf(
        "Display" to d.name, "Current mode ID" to (d.modeId?.toString() ?: "Unavailable"), "Current mode resolution" to if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width} × ${d.height}" else "Unavailable",
        "Refresh rate" to (d.refreshRate?.let { "$it Hz" } ?: "Unavailable"), "Supported display modes" to if (d.supportedModes.isEmpty()) "Unavailable" else d.supportedModes.joinToString(" | "),
        "Wide color gamut" to when (d.wideColor) { true -> "Reported by Android display API"; false -> "Not supported by Android display API"; null -> "Unavailable on this Android API/display context" },
        "HDR types" to if (d.hdrTypes.isEmpty()) "Unavailable" else d.hdrTypes.joinToString(),
        "Desired max luminance" to d.desiredMaxLuminance?.let { "$it cd/m²" }, "Desired max average luminance" to d.desiredMaxAverageLuminance?.let { "$it cd/m²" }, "Desired min luminance" to d.desiredMinLuminance?.let { "$it cd/m²" }
    ))
    val limitRows = r.limits.joinToString("") { "<tr><td class=\"code\">${e(it.name)}</td><td>${e(it.value)}</td></tr>" }
    val precisionRows = r.precision.joinToString("") { "<tr><td class=\"code\">${e(it.shader)}</td><td class=\"code\">${e(it.type)}</td><td>${e("${it.rangeMin}..${it.rangeMax}")}</td><td>${e(it.precision)}</td></tr>" }
    val diagnosticRows = r.diagnostics.joinToString("") { "<tr><td class=\"code\">${e(it.name)}</td><td>${badge(it.status)}</td><td>${e(it.detail.ifBlank { "—" })}</td></tr>" }
    val configRows = r.eglConfigs.joinToString("") { c -> "<tr><td>${e(c.id)}</td><td>${e("${c.red ?: "?"}/${c.green ?: "?"}/${c.blue ?: "?"}/${c.alpha ?: "?"}")}</td><td>${e(c.bufferSize)}</td><td>${e(c.luminanceSize)}</td><td>${e(c.alphaMaskSize)}</td><td>${e(c.depth)}</td><td>${e(c.stencil)}</td><td>${e(c.sampleBuffers)}</td><td>${e(c.samples)}</td><td class=\"code\">${e(c.surfaceType)}</td><td class=\"code\">${e(c.renderableType)}</td><td class=\"code\">${e(c.conformant)}</td><td class=\"code\">${e(c.configCaveat)}</td><td class=\"code\">${e(c.colorBufferType)}</td><td>${e(eglBooleanLabel(c.bindToTextureRgb))}</td><td>${e(eglBooleanLabel(c.bindToTextureRgba))}</td><td>${e("${c.maxPbufferWidth ?: "?"}×${c.maxPbufferHeight ?: "?"} / ${c.maxPbufferPixels ?: "?"}")}</td><td>${e(c.level)}</td><td>${e(eglBooleanLabel(c.nativeRenderable))}</td><td>${e(c.nativeVisualId)}</td><td>${e(c.nativeVisualType)}</td><td class=\"code\">${e(c.transparentType)}</td><td>${e("${c.transparentRed ?: "?"}/${c.transparentGreen ?: "?"}/${c.transparentBlue ?: "?"}")}</td><td>${e("${c.minSwapInterval ?: "?"}..${c.maxSwapInterval ?: "?"}")}</td><td>${e(c.recordableAndroid?.let { eglBooleanLabel(it) } ?: "N/A")}</td><td>${e(c.framebufferTargetAndroid?.let { eglBooleanLabel(it) } ?: "N/A")}</td><td class=\"code\">${e(c.colorComponentTypeExt ?: "N/A")}</td><td>${e(c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" })}</td></tr>" }
    val availableQueries = r.diagnostics.count { it.status == "Available" }
    val unavailableQueries = r.diagnostics.count { it.status == "Unavailable" }
    val naQueries = r.diagnostics.count { it.status == "Not applicable" }
    val unknownQueries = r.diagnostics.count { it.status == "Unknown" }
    return buildString {
        append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><meta name=\"color-scheme\" content=\"dark\"><meta name=\"referrer\" content=\"no-referrer\"><meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none'; img-src data:; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'\"><title>OpenGLESScope report</title>")
        append("<style>body{font-family:Inter,system-ui,-apple-system,BlinkMacSystemFont,\"Segoe UI\",sans-serif;background:#0a0a0b;color:#f4f4f5;margin:0;line-height:1.45}.wrap{max-width:1320px;margin:0 auto;padding:28px}.hero{background:linear-gradient(135deg,#21131e,#0f1012);border:1px solid #3b2636;border-radius:26px;padding:30px;box-shadow:0 16px 50px rgba(0,0,0,.28)}h1{margin:0 0 8px;font-size:36px}h2{margin:0 0 14px;font-size:22px}.muted{color:#a7a7ae}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:12px;margin-top:18px}.metric{background:#141113;border:1px solid #342630;border-radius:17px;padding:14px}.section{margin-top:24px;background:#111113;border:1px solid #30242c;border-radius:22px;padding:18px;overflow:auto}.section h2{position:sticky;left:0}table{border-collapse:collapse;width:100%;min-width:660px}td,th{border-bottom:1px solid #2c2329;padding:10px 8px;text-align:left;vertical-align:top}th{color:#cbcad0;font-weight:600}.badge{display:inline-block;border-radius:999px;padding:3px 9px;font-size:11px;font-weight:800;letter-spacing:.03em}.yes{background:#133b28;color:#74e2a6}.available{background:#39142f;color:#f06bc7}.unavailable{background:#3a2b14;color:#ffc66d}.no{background:#49171c;color:#ff8f98}.neutral{background:#403713;color:#ffd76b}.unknown{background:#292a2f;color:#c6c6cc}.code{font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace;overflow-wrap:anywhere}.small{font-size:13px}.subtle{color:#7f8088}.accent{color:#f06bc7}.github-link{color:#e25db8;text-decoration:none;font-weight:600}.github-link:hover{color:#f58bd6;text-decoration:underline}.github-link:visited{color:#e25db8}@media(max-width:700px){.wrap{padding:14px}.hero{padding:20px}.section{padding:14px}h1{font-size:29px}}</style></head><body><div class=\"wrap\">")
        append("<div class=\"hero\">")
        if (logoData != null) append("<div style=\"display:flex;align-items:center;justify-content:flex-start;margin-bottom:14px\"><img src=\"data:image/png;base64,$logoData\" alt=\"OpenGLESScope\" style=\"display:block;width:min(522px,100%);height:auto;max-height:76px;object-fit:contain;object-position:left center\"></div>") else append("<h1>OpenGLESScope</h1>")
        append("<div class=\"muted\">Runtime OpenGL ES and EGL capability report</div><div class=\"grid\">")
        fun metric(label: String, value: String) { append("<div class=\"metric\"><div class=\"muted small\">${e(label)}</div><strong>${e(value)}</strong></div>") }
        metric("GPU", r.renderer.ifBlank { "Unavailable" }); metric("OpenGL ES", r.glVersion.ifBlank { "Unavailable" }); metric("EGL", r.egl.initializedVersion.ifBlank { r.egl.version }); metric("Display", if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width} × ${d.height} @ ${d.refreshRate?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "Unavailable"} Hz" else d.name); metric("HDR", d.hdrTypes.joinToString(", ").ifBlank { "Unavailable" }); metric("Queries", "$availableQueries available / $unavailableQueries unavailable / $naQueries N/A / $unknownQueries unknown")
        append("</div></div>")
        fun section(title: String, header: String, body: String) { append("<div class=\"section\"><h2>${e(title)}</h2><table><thead><tr>$header</tr></thead><tbody>$body</tbody></table></div>") }
        fun htmlRows(values: List<Pair<String, String>>): String = values.joinToString("") { "<tr><td>${e(it.first)}</td><td>${it.second}</td></tr>" }
        section("Application", "<th>Property</th><th>Value</th>", htmlRows(applicationRows))
        section("Android / device", "<th>Property</th><th>Value</th>", deviceRows)
        section("OpenGL ES runtime", "<th>Property</th><th>Value</th>", glRows)
        section("EGL runtime", "<th>Property</th><th>Value</th>", eglRows)
        section("Android display &amp; HDR", "<th>Property</th><th>Value</th>", displayRows)
        section("OpenGL ES limits (${r.limits.size})", "<th>Limit</th><th>Value</th>", limitRows)
        section("OpenGL ES extensions (${r.extensions.size})", "<th>Extension</th>", listRows(r.extensions))
        section("EGL display extensions (${r.egl.extensions.size})", "<th>Extension</th>", listRows(r.egl.extensions))
        section("EGL client extensions (${r.egl.clientExtensions.size})", "<th>Extension</th>", listRows(r.egl.clientExtensions))
        section("Compressed texture formats (${r.compressedFormats.size})", "<th>Format</th>", listRows(r.compressedFormats))
        section("Shader binary formats (${r.shaderBinaryFormats.size})", "<th>Format</th>", listRows(r.shaderBinaryFormats))
        section("Program binary formats (${r.programBinaryFormats.size})", "<th>Format</th>", listRows(r.programBinaryFormats))
        section("Shader precision (${r.precision.size})", "<th>Shader</th><th>Type</th><th>Range</th><th>Precision</th>", precisionRows)
        section("Query diagnostics (${r.diagnostics.size})", "<th>Query</th><th>Status</th><th>Detail</th>", diagnosticRows)
        section("EGL configurations (${r.eglConfigs.size})", "<th>ID</th><th>RGBA</th><th>Buffer</th><th>Luminance</th><th>Alpha mask</th><th>Depth</th><th>Stencil</th><th>Sample buffers</th><th>Samples</th><th>Surface</th><th>Renderable</th><th>Conformant</th><th>Caveat</th><th>Color buffer</th><th>Bind RGB</th><th>Bind RGBA</th><th>Max pbuffer W×H / pixels</th><th>Level</th><th>Native renderable</th><th>Visual ID</th><th>Visual type</th><th>Transparency</th><th>Transparent RGB</th><th>Swap interval</th><th>Recordable</th><th>Framebuffer target</th><th>Color component type</th><th>Unavailable attributes</th>", configRows)
        append("</div></body></html>")
    }
}

