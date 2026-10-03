package com.efishell.openglesscope

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.Intent
import android.hardware.display.DisplayManager
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.graphics.Color
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Network
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.Inet6Address
import java.net.InetAddress
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private fun strictOpenGlesProbeTerminalCandidate(candidate: String, selfTest: Boolean): Boolean =
    OpenGLESProbeContract.isTerminalJson(candidate, selfTest)

private const val DATABASE_API = "https://openglesscope-database-api.openglesscope.workers.dev"
private const val DATABASE_WEB = "https://efishell0.github.io/OpenGLESScope_database/"
private const val RELEASES_API = "https://api.github.com/repos/EFIShell0/OpenGLESScope/releases?per_page=20"
private const val GITHUB_API_VERSION = "2026-03-10"
private const val SUBMISSION_SCHEMA_VERSION = 2
private const val TECHNICAL_REPORT_SCHEMA_VERSION = 5
private const val MAX_UPDATE_REDIRECTS = 3
private const val COLLECTION_PAGE_SIZE = 25
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
private val GlassTint = ComposeColor(0xA81D111A)
private val GlassBorder = BrandSoft.copy(alpha = 0.24f)
private val PrimaryNavigationMaxWidth = 310.dp
private val PrimaryNavigationHeight = 54.dp
private val PrimaryNavigationIndicatorHeight = 42.dp
private val PrimaryNavigationIndicatorHorizontalInset = 8.dp
private val PrimaryNavigationBottomGap = 4.dp
private val PrimaryNavigationContentGap = 10.dp
private val PageChromeSeparation = 12.dp
private val OverlayCoordinationLead = 96.dp
private const val OverlayCoordinationMotionMillis = 120
private val LocalPinnedPagerContentInset = staticCompositionLocalOf { 0.dp }
private typealias StickyPagerStateReporter = (String, Boolean, Int) -> Unit
private val LocalStickyPagerStateReporter = staticCompositionLocalOf<StickyPagerStateReporter> { { _, _, _ -> } }
private data class CollectionPagerRegistration(
    val key: String,
    val itemIndex: Int,
    val heightPx: Int,
    val layoutItemCount: Int,
    val totalItems: Int,
    val currentPage: Int,
    val pageSize: Int,
    val onPageChange: (Int) -> Unit,
    val onMeasuredHeight: (Int) -> Unit
)
private data class CollectionPagerVisualState(
    val registration: CollectionPagerRegistration,
    val progress: Float,
    val overlayOffsetPx: Float,
    val laneExtentPx: Int,
    val glassHeightPx: Int
)
private typealias CollectionPagerRegistrationReporter = (String, CollectionPagerRegistration?) -> Unit
private val LocalCollectionPagerRegistrationReporter = staticCompositionLocalOf<CollectionPagerRegistrationReporter> { { _, _ -> } }
private typealias CollectionPagerMeasuredHeightLookup = (String) -> Int
private typealias CollectionPagerMeasuredHeightReporter = (String, Int) -> Unit
private val LocalCollectionPagerMeasuredHeightLookup = staticCompositionLocalOf<CollectionPagerMeasuredHeightLookup> { { _ -> 0 } }
private val LocalCollectionPagerMeasuredHeightReporter = staticCompositionLocalOf<CollectionPagerMeasuredHeightReporter> { { _, _ -> } }
private val LocalPrimaryLazyListState = staticCompositionLocalOf<LazyListState?> { null }
private val LocalAppHeaderContentInset = staticCompositionLocalOf { 0.dp }
private val LocalBottomNavigationContentInset = staticCompositionLocalOf { PrimaryNavigationBottomGap + PrimaryNavigationHeight + PrimaryNavigationContentGap }
private val LocalTransientOverlayContentInset = staticCompositionLocalOf { 0.dp }
private data class ChromeBackdropSource(val layer: GraphicsLayer, val rootOffset: Offset)
private typealias ChromeBackdropReporter = (GraphicsLayer, Offset?) -> Unit
private val LocalChromeBackdropReporter = staticCompositionLocalOf<ChromeBackdropReporter> { { _, _ -> } }
private val OPENGL_ES_TRADEMARK_DISPLAY_REGEX = Regex("""\bOpenGL(?:®)?\s+ES(?!™|[A-Za-z0-9_])""")
private val EGL_TRADEMARK_DISPLAY_REGEX = Regex("""\bEGL(?!™|[A-Za-z0-9_])""")
private val SPIRV_TRADEMARK_DISPLAY_REGEX = Regex("""\bSPIR-V(?!™|[A-Za-z0-9_])""")
private fun trademarkApiDisplayText(text: String): String = SPIRV_TRADEMARK_DISPLAY_REGEX.replace(EGL_TRADEMARK_DISPLAY_REGEX.replace(OPENGL_ES_TRADEMARK_DISPLAY_REGEX.replace(text, "OpenGL® ES™"), "EGL™"), "SPIR-V™")
private const val CHROMEOS_ARC_FEATURE = "org.chromium.arc"
private data class EvidenceActionEnvironment(val openEncyclopedia: (String) -> Unit, val addWatch: (String) -> Unit)
private val LocalEvidenceActionEnvironment = staticCompositionLocalOf<EvidenceActionEnvironment?> { null }
private val LocalDetailKeyValuePresentation = staticCompositionLocalOf { false }
private data class EvidenceProvenance(
    val evidenceClass: String,
    val source: String,
    val queryPath: String,
    val queryGroup: String,
    val registryRelation: String,
    val interpretation: String
)

private fun evidenceTokenForReference(key: String, value: String): String {
    val combined = "$key $value"
    Regex("GL_[A-Z0-9_]+|EGL_[A-Z0-9_]+", RegexOption.IGNORE_CASE).find(combined)?.value?.let { return it.uppercase(java.util.Locale.ROOT) }
    Regex("gl[A-Z][A-Za-z0-9_]+|egl[A-Z][A-Za-z0-9_]+", RegexOption.IGNORE_CASE).find(combined)?.value?.let { return it }
    return key.substringAfterLast('/').substringAfterLast('·').trim().take(256)
}

private fun evidenceProvenance(key: String, value: String): EvidenceProvenance {
    val normalized = key.lowercase(java.util.Locale.ROOT)
    return when {
        normalized.startsWith("limit/") -> EvidenceProvenance("Runtime implementation-limit evidence", key.substringAfter("limit/"), "glGet* / exact validated query gate", "OpenGL® ES implementation limits", "Canonical token name comes from the release-locked gl.xml registry", "The value is retained from the active implementation. No conformance or performance judgment is inferred.")
        normalized.startsWith("extension/gl/") -> EvidenceProvenance("Runtime OpenGL® ES extension enumeration", key.substringAfter("extension/gl/"), "glGetStringi(GL_EXTENSIONS) / validated enumeration", "OpenGL® ES extension enumeration", "Extension ownership/reference metadata comes from locked gl.xml", "Presence means the active runtime enumerated the extension. Absence is meaningful only when enumeration completed authoritatively.")
        normalized.startsWith("extension/egl-display/") -> EvidenceProvenance("Runtime EGL™ display-extension enumeration", key.substringAfter("extension/egl-display/"), "eglQueryString(display, EGL_EXTENSIONS)", "EGL™ display extension enumeration", "Extension metadata comes from locked egl.xml", "Presence means the active EGL™ display enumerated the extension; registry presence alone is never support evidence.")
        normalized.startsWith("extension/egl-client/") -> EvidenceProvenance("Runtime EGL™ client-extension enumeration", key.substringAfter("extension/egl-client/"), "eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS)", "EGL™ client extension enumeration", "Extension metadata comes from locked egl.xml", "Presence means the client extension string enumerated the extension; missing/failed enumeration remains distinct from Unsupported.")
        normalized.startsWith("format/") -> EvidenceProvenance("Runtime format evidence", key.substringAfter("format/"), "OpenGL® ES format enumeration / internal-format query path", "Format capability collection", "Format/token names come from locked gl.xml", "The exact collected format evidence is shown without converting registry metadata into runtime support.")
        normalized.startsWith("precision/") -> EvidenceProvenance("Runtime shader-precision evidence", key.substringAfter("precision/"), "glGetShaderPrecisionFormat", "Shader precision query", "Shader/type names are canonical OpenGL® ES registry names", "The range and precision come from the active implementation and are not a benchmark score.")
        normalized.startsWith("egl-runtime/") || normalized.startsWith("eglconfig/") -> EvidenceProvenance("Runtime EGL™ evidence", key.substringAfterLast('/'), "eglQueryContext / eglQuerySurface / eglGetConfigAttrib", "EGL™ runtime/config query path", "Canonical EGL™ tokens come from locked egl.xml", "Unavailable query evidence remains explicit and is never fabricated from registry ownership.")
        normalized.startsWith("query/") -> EvidenceProvenance("Collector diagnostic evidence", key.substringAfter("query/"), "OpenGLESScope validated query diagnostic", "Query diagnostics", "Registry legality is validated separately", "This describes query availability/completeness and is not itself a hardware support inference.")
        normalized.startsWith("display/") -> EvidenceProvenance("Android display evidence", key.substringAfter("display/"), "Android Display / HdrCapabilities APIs", "Android display path", "Separate from OpenGL® ES/EGL™ registry metadata", "Android display evidence is kept separate from OpenGL® ES/EGL™ capability claims.")
        else -> EvidenceProvenance("Collected or derived evidence", key, "OpenGLESScope report/analysis model", "General evidence path", "Registry/reference metadata remains separate from runtime evidence", "The displayed value is preserved without converting missing or unknown information into Unsupported.")
    }
}

private fun isChromeOsRuntime(context: Context): Boolean = runCatching { context.packageManager.hasSystemFeature(CHROMEOS_ARC_FEATURE) }.getOrDefault(false)
private fun isAndroidPcFormFactor(context: Context): Boolean = runCatching { context.packageManager.hasSystemFeature(PackageManager.FEATURE_PC) }.getOrDefault(false)
private fun hasFreeformWindowManagement(context: Context): Boolean = runCatching { context.packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT) }.getOrDefault(false)
private typealias SharedStorageAccessRequest = ((() -> Unit), (() -> Unit)) -> Unit
private val LocalSharedStorageAccessRequest = staticCompositionLocalOf<SharedStorageAccessRequest> { { granted, _ -> granted() } }
private val OpenGLESExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
    largeIncreased = RoundedCornerShape(32.dp),
    extraLargeIncreased = RoundedCornerShape(36.dp),
    extraExtraLarge = RoundedCornerShape(40.dp)
)

private val OpenGLESBaseTypography = Typography()
private val OpenGLESTypography = Typography(
    displayLarge = OpenGLESBaseTypography.displayLarge.copy(textDirection = TextDirection.ContentOrLtr),
    displayMedium = OpenGLESBaseTypography.displayMedium.copy(textDirection = TextDirection.ContentOrLtr),
    displaySmall = OpenGLESBaseTypography.displaySmall.copy(textDirection = TextDirection.ContentOrLtr),
    headlineLarge = OpenGLESBaseTypography.headlineLarge.copy(textDirection = TextDirection.ContentOrLtr),
    headlineMedium = OpenGLESBaseTypography.headlineMedium.copy(textDirection = TextDirection.ContentOrLtr),
    headlineSmall = OpenGLESBaseTypography.headlineSmall.copy(textDirection = TextDirection.ContentOrLtr),
    titleLarge = OpenGLESBaseTypography.titleLarge.copy(textDirection = TextDirection.ContentOrLtr),
    titleMedium = OpenGLESBaseTypography.titleMedium.copy(textDirection = TextDirection.ContentOrLtr),
    titleSmall = OpenGLESBaseTypography.titleSmall.copy(textDirection = TextDirection.ContentOrLtr),
    bodyLarge = OpenGLESBaseTypography.bodyLarge.copy(textDirection = TextDirection.ContentOrLtr),
    bodyMedium = OpenGLESBaseTypography.bodyMedium.copy(textDirection = TextDirection.ContentOrLtr),
    bodySmall = OpenGLESBaseTypography.bodySmall.copy(textDirection = TextDirection.ContentOrLtr),
    labelLarge = OpenGLESBaseTypography.labelLarge.copy(textDirection = TextDirection.ContentOrLtr),
    labelMedium = OpenGLESBaseTypography.labelMedium.copy(textDirection = TextDirection.ContentOrLtr),
    labelSmall = OpenGLESBaseTypography.labelSmall.copy(textDirection = TextDirection.ContentOrLtr)
)

internal data class LimitEntry(val name: String, val value: String)
internal data class QueryDiagnostic(val name: String, val status: String, val detail: String)
internal data class FeatureEvidenceRow(val name: String, val state: EvidenceState, val source: String, val detail: String)
internal data class EglCapabilityEntry(val name: String, val status: String, val value: String, val detail: String)
internal data class PrecisionEntry(val shader: String, val type: String, val rangeMin: Int, val rangeMax: Int, val precision: Int)
internal data class EglUnavailableAttribute(val name: String, val error: String)
internal data class GlRuntimeInfo(
    val contextFlags: String?, val resetNotificationStrategy: String?, val resetNotificationStrategyQuery: String?,
    val robustAccess: Boolean?, val robustAccessQuery: String?, val unavailableAttributes: List<EglUnavailableAttribute>
)
internal data class EglRuntimeInfo(
    val boundApi: String, val configId: Int?, val clientType: String?, val clientVersion: Int?, val renderBuffer: String?,
    val currentContext: Boolean, val currentDisplay: Boolean, val currentDrawSurface: Boolean, val currentReadSurface: Boolean,
    val surfaceGlColorspace: String?, val surfaceGlColorspaceQuery: String?, val surfaceVgAlphaFormat: String?, val surfaceVgAlphaFormatQuery: String?,
    val surfaceVgColorspace: String?, val surfaceVgColorspaceQuery: String?, val surfaceConfigId: Int?,
    val surfaceWidth: Int?, val surfaceHeight: Int?, val surfaceHorizontalResolution: Int?, val surfaceLargestPbuffer: Boolean?,
    val surfacePixelAspectRatio: Int?, val surfaceVerticalResolution: Int?, val surfaceRenderBuffer: String?, val surfaceSwapBehavior: String?,
    val surfaceTextureFormat: String?, val surfaceTextureTarget: String?, val surfaceMipmapTexture: Boolean?, val surfaceMipmapLevel: Int?,
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
internal data class NvSampleProperty(val samples: Int, val multisamples: Int, val supersampleScaleX: Int, val supersampleScaleY: Int, val conformant: Boolean)
internal data class InternalFormatEntry(
    val target: String,
    val internalFormat: String,
    val status: String,
    val detail: String,
    val sampleCounts: List<Int>,
    val nvSampleProperties: List<NvSampleProperty>
)
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
    val glRuntime: GlRuntimeInfo,
    val eglRuntime: EglRuntimeInfo,
    val eglCapabilities: List<EglCapabilityEntry>,
    val extensions: List<String>,
    val limits: List<LimitEntry>,
    val compressedFormats: List<String>,
    val internalFormats: List<InternalFormatEntry>,
    val shaderBinaryFormats: List<String>,
    val programBinaryFormats: List<String>,
    val precision: List<PrecisionEntry>,
    val eglConfigs: List<EglConfigEntry>,
    val diagnostics: List<QueryDiagnostic>
)
internal data class DisplayInfo(val name: String, val modeId: Int?, val width: Int?, val height: Int?, val refreshRate: Float?, val supportedModes: List<String>, val hdrTypes: List<String>, val desiredMaxLuminance: Float?, val desiredMaxAverageLuminance: Float?, val desiredMinLuminance: Float?, val wideColor: Boolean?, val hdrCapabilityStatus: String = "unknown")
internal enum class EvidenceState { Supported, Unsupported, Unknown }
private enum class CollectionStatus { IDLE, COLLECTING, COMPLETED, FAILED }
private enum class Page(val title: String) {
    Overview("Overview"), OpenGLES("OpenGL® ES™"), Display("Display & HDR"), EGL("EGL™"), Features("Features"), Limits("Limits"), Formats("Formats"), Extensions("Extensions"), Precision("Precision"), Configs("EGL™ Configs"), Encyclopedia("Encyclopedia"), Analysis("Analysis workspace"), Settings("Settings")
}

private enum class OpenGlesSection(val label: String, val description: String, val icon: Int) {
    RUNTIME("OpenGL® ES™ runtime", "Driver identity, core runtime strings, context state and implementation evidence.", R.drawable.ic_opengles_gl_es),
    EGL("EGL™", "EGL identity, display/client extensions, current bindings, surface attributes and EGL configs.", R.drawable.ic_egl_official)
}

private enum class SettingsSection(val label: String, val description: String, val icon: Int) {
    INFO("Info", "Developer, application, libraries, build, Android/device and Khronos registry information.", R.drawable.ic_info),
    REPORTS_DATABASE("Reports & Database", "Complete TXT/HTML export, Database submission, compatibility state and public report access.", R.drawable.ic_database_submit),
    UPDATE_PREFERENCES("Update Preferences", "Built-in GitHub update preference, opening animation and Obtainium guidance.", R.drawable.ic_settings)
}

private enum class InfoContentMode { ALL, INFO, REPORTS }

internal data class AppUpdate(
    val version: String,
    val assetName: String,
    val downloadUrl: String,
    val expectedSha256: String?,
    val assetSizeBytes: Long?,
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

internal enum class NetworkBannerState { HIDDEN, CONNECTED, DISCONNECTED }
private val LocalValidatedNetwork = staticCompositionLocalOf { false }

internal enum class UpdateTransferPhase {
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    COMPLETED,
    CANCELED,
    FAILED
}

internal data class UpdateTransferState(
    val update: AppUpdate,
    val phase: UpdateTransferPhase,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long? = null,
    val bytesPerSecond: Long = 0L,
    val connectionStatus: String = "Connecting",
    val log: List<String> = emptyList(),
    val apk: File? = null,
    val errorMessage: String? = null
)

private enum class SharedStorageBrowserMode { IMPORT, EXPORT }
private enum class FileManagerViewMode(val label: String, val icon: Int) {
    LIST("List", R.drawable.ic_view_list),
    COMPACT_LIST("Compact list", R.drawable.ic_view_compact),
    DETAILED_LIST("Detailed list", R.drawable.ic_view_details),
    GRID("Grid", R.drawable.ic_view_grid),
    DENSE_GRID("Dense grid", R.drawable.ic_view_grid_dense),
    LARGE_TILES("Large tiles", R.drawable.ic_view_tiles_large)
}
private enum class FileManagerSortMode(val label: String, val icon: Int) {
    NAME_ASC("Name A–Z", R.drawable.ic_sort_name_asc),
    NAME_DESC("Name Z–A", R.drawable.ic_sort_name_desc),
    MODIFIED_NEWEST("Modified newest", R.drawable.ic_sort_modified_newest),
    MODIFIED_OLDEST("Modified oldest", R.drawable.ic_sort_modified_oldest),
    CREATED_NEWEST("Created newest", R.drawable.ic_sort_created_newest),
    CREATED_OLDEST("Created oldest", R.drawable.ic_sort_created_oldest)
}
private enum class AnalysisStorageAction { IMPORT_SNAPSHOT, EXPORT_SNAPSHOT, IMPORT_MINIMUM_PROFILE, EXPORT_MINIMUM_PROFILE, EXPORT_TECHNICAL_REPORT }

private data class SharedStorageBrowserRequest(
    val title: String,
    val description: String,
    val mode: SharedStorageBrowserMode,
    val allowedExtensions: Set<String>,
    val suggestedFileName: String = "",
    val maxImportBytes: Long? = null
)

private data class SharedStorageFileEntry(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedAtMillis: Long,
    val createdAtMillis: Long
)

private data class SharedStorageDirectoryListing(
    val folders: List<String>,
    val files: List<SharedStorageFileEntry>,
    val entryLimitReached: Boolean
)

private fun sharedStorageRoot(): File = Environment.getExternalStorageDirectory().canonicalFile

private fun isCanonicalSharedStoragePath(root: File, candidate: File): Boolean {
    val canonicalRoot = root.canonicalFile
    val canonicalCandidate = candidate.canonicalFile
    val prefix = canonicalRoot.path.trimEnd(File.separatorChar) + File.separator
    return canonicalCandidate == canonicalRoot || canonicalCandidate.path.startsWith(prefix)
}

private fun fileManagerCreationMillis(file: File): Long = runCatching {
    java.nio.file.Files.readAttributes(file.toPath(), java.nio.file.attribute.BasicFileAttributes::class.java).creationTime().toMillis()
}.getOrElse { file.lastModified().coerceAtLeast(0L) }.coerceAtLeast(0L)

private fun fileManagerSortFiles(files: List<SharedStorageFileEntry>, mode: FileManagerSortMode): List<SharedStorageFileEntry> = when (mode) {
    FileManagerSortMode.NAME_ASC -> files.sortedBy { it.name.lowercase(java.util.Locale.ROOT) }
    FileManagerSortMode.NAME_DESC -> files.sortedWith(compareByDescending<SharedStorageFileEntry> { it.name.lowercase(java.util.Locale.ROOT) })
    FileManagerSortMode.MODIFIED_NEWEST -> files.sortedByDescending { it.modifiedAtMillis }
    FileManagerSortMode.MODIFIED_OLDEST -> files.sortedBy { it.modifiedAtMillis }
    FileManagerSortMode.CREATED_NEWEST -> files.sortedByDescending { it.createdAtMillis }
    FileManagerSortMode.CREATED_OLDEST -> files.sortedBy { it.createdAtMillis }
}

private fun fileManagerSortFolders(paths: List<String>, mode: FileManagerSortMode): List<String> = when (mode) {
    FileManagerSortMode.NAME_ASC -> paths.sortedBy { File(it).name.lowercase(java.util.Locale.ROOT) }
    FileManagerSortMode.NAME_DESC -> paths.sortedByDescending { File(it).name.lowercase(java.util.Locale.ROOT) }
    FileManagerSortMode.MODIFIED_NEWEST -> paths.sortedByDescending { File(it).lastModified() }
    FileManagerSortMode.MODIFIED_OLDEST -> paths.sortedBy { File(it).lastModified() }
    FileManagerSortMode.CREATED_NEWEST -> paths.sortedByDescending { fileManagerCreationMillis(File(it)) }
    FileManagerSortMode.CREATED_OLDEST -> paths.sortedBy { fileManagerCreationMillis(File(it)) }
}

private suspend fun scanSharedStorageDirectory(
    root: File,
    directory: File,
    allowedExtensions: Set<String>,
    includeFiles: Boolean
): SharedStorageDirectoryListing {
    val coroutineContext = currentCoroutineContext()
    coroutineContext.ensureActive()
    val canonicalRoot = root.canonicalFile
    val canonicalDirectory = directory.canonicalFile
    if (!isCanonicalSharedStoragePath(canonicalRoot, canonicalDirectory)) error("Directory is outside shared storage")
    if (directory.absoluteFile.path != canonicalDirectory.path && directory.absoluteFile != canonicalRoot) error("Symbolic-link directories are not supported")
    if (!canonicalDirectory.isDirectory || !canonicalDirectory.canRead()) error("Directory is unavailable")
    val children = ArrayList<File>(256)
    var entryLimitReached = false
    java.nio.file.Files.newDirectoryStream(canonicalDirectory.toPath()).use { stream ->
        val iterator = stream.iterator()
        while (iterator.hasNext()) {
            coroutineContext.ensureActive()
            if (children.size >= 4096) {
                entryLimitReached = true
                break
            }
            children += iterator.next().toFile()
        }
    }
    children.sortBy { it.name.lowercase(java.util.Locale.ROOT) }
    val folders = ArrayList<String>()
    val files = ArrayList<SharedStorageFileEntry>()
    for (child in children) {
        coroutineContext.ensureActive()
        val canonical = runCatching { child.canonicalFile }.getOrNull() ?: continue
        if (!isCanonicalSharedStoragePath(canonicalRoot, canonical)) continue
        if (child.absoluteFile.path != canonical.path) continue
        if (canonical.isDirectory && canonical.canRead()) {
            folders += canonical.path
            continue
        }
        if (!includeFiles || !canonical.isFile || !canonical.canRead()) continue
        val extension = canonical.extension.lowercase(java.util.Locale.ROOT)
        if (extension in allowedExtensions) {
            files += SharedStorageFileEntry(canonical.path, canonical.name.take(512), canonical.length().coerceAtLeast(0L), canonical.lastModified().coerceAtLeast(0L), fileManagerCreationMillis(canonical))
        }
    }
    return SharedStorageDirectoryListing(folders, files, entryLimitReached)
}

private fun validatedSharedStorageImportFile(file: File, allowedExtensions: Set<String>, maxBytes: Long): File {
    val root = sharedStorageRoot()
    val canonical = file.canonicalFile
    if (!isCanonicalSharedStoragePath(root, canonical) || file.absoluteFile.path != canonical.path) error("Selected file is outside approved shared storage")
    if (!canonical.isFile || !canonical.canRead()) error("Selected file is unavailable")
    if (canonical.extension.lowercase(java.util.Locale.ROOT) !in allowedExtensions) error("Selected file type is not allowed")
    val length = canonical.length()
    if (length <= 0L || length > maxBytes) error("Selected file exceeds the allowed size")
    return canonical
}

private fun validatedSharedStorageDestination(directory: File, filename: String, allowedExtensions: Set<String>): File {
    val root = sharedStorageRoot()
    val canonicalDirectory = directory.canonicalFile
    if (!isCanonicalSharedStoragePath(root, canonicalDirectory) || !canonicalDirectory.isDirectory || !canonicalDirectory.canWrite()) error("Destination folder is unavailable")
    val clean = filename.trim()
    if (clean.isBlank() || clean.length > 180 || clean == "." || clean == ".." || clean.contains('/') || clean.contains('\\') || clean.any { it.code < 0x20 }) error("Invalid file name")
    if (File(clean).name != clean) error("Invalid file name")
    if (clean.substringAfterLast('.', "").lowercase(java.util.Locale.ROOT) !in allowedExtensions) error("File extension is not allowed")
    val target = File(canonicalDirectory, clean).canonicalFile
    if (!isCanonicalSharedStoragePath(root, target) || target.parentFile?.canonicalFile != canonicalDirectory) error("Unsafe destination path")
    if (target.exists() && (!target.isFile || target.absoluteFile.path != target.canonicalFile.path)) error("Destination is not a regular file")
    return target
}

private fun atomicWriteSharedStorageFile(destination: File, writer: (FileOutputStream) -> Unit) {
    val directory = destination.parentFile?.canonicalFile ?: error("Destination folder is unavailable")
    val temp = File(directory, ".openglesscope-${java.util.UUID.randomUUID()}.tmp").canonicalFile
    if (temp.parentFile != directory) error("Unsafe temporary destination")
    try {
        FileOutputStream(temp, false).use { output ->
            writer(output)
            output.flush()
            output.fd.sync()
        }
        try {
            java.nio.file.Files.move(temp.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            java.nio.file.Files.move(temp.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
        if (!destination.isFile || destination.length() <= 0L) error("Saved file could not be verified")
    } catch (error: Throwable) {
        runCatching { temp.delete() }
        throw error
    }
}

private fun writeSharedStorageBytes(destination: File, bytes: ByteArray, maxBytes: Int) {
    if (bytes.isEmpty() || bytes.size > maxBytes) error("Export exceeds the allowed size")
    atomicWriteSharedStorageFile(destination) { output -> output.write(bytes) }
}

private fun copySharedStorageFile(destination: File, source: File, maxBytes: Long = 64L * 1024L * 1024L) {
    val canonicalSource = source.canonicalFile
    if (!canonicalSource.isFile || !canonicalSource.canRead()) error("Export snapshot is unavailable")
    val size = canonicalSource.length()
    if (size <= 0L || size > maxBytes) error("Export snapshot exceeds the allowed size")
    atomicWriteSharedStorageFile(destination) { output ->
        FileInputStream(canonicalSource).use { input -> input.copyTo(output, 64 * 1024) }
    }
}

class MainActivity : ComponentActivity() {
    internal var updateStatus by mutableStateOf<UpdateStatus>(UpdateStatus.Hidden)
    internal var updateCheckInFlight by mutableStateOf(false)
    internal var updateConfirmation by mutableStateOf<AppUpdate?>(null)
    private var updateCheckJob: Job? = null
    private var updateStatusHideJob: Job? = null
    private var updateDownloadJob: Job? = null
    @Volatile private var activeUpdateCheckCall: Call? = null
    @Volatile private var activeUpdateDownloadCall: Call? = null
    private var pendingUpdateApk: File? = null
    private lateinit var prefs: android.content.SharedPreferences
    internal var directUpdatesEnabled by mutableStateOf(true)
    internal var openingAnimationEnabled by mutableStateOf(true)
    private val startupActivityStartNanos = System.nanoTime()
    private var startupTimingColdLaunch = true
    internal var startupGateDelayMs by mutableStateOf<Long?>(null)
    private var startupPostAnimationWorkStarted = false
    internal var directUpdatesConsentVisible by mutableStateOf(false)
    internal var networkBannerState by mutableStateOf(NetworkBannerState.HIDDEN)
    internal var validatedNetworkAvailable by mutableStateOf(false)
    internal var networkStateKnown by mutableStateOf(false)
    internal var updateTransferState by mutableStateOf<UpdateTransferState?>(null)
    internal var updateCancelConfirmationVisible by mutableStateOf(false)
    private var networkBannerGeneration = 0L
    private var lastValidatedNetwork: Boolean? = null
    private var trackedDefaultNetwork: Network? = null
    private var networkCallbackRegistered = false
    @Volatile private var updateDownloadPaused = false
    @Volatile private var updateDownloadCancelRequested = false
    private val probeMutex = Mutex()
    private var awaitingAllFilesAccessReturn = false
    private var pendingSharedStorageGranted: (() -> Unit)? = null
    private var pendingSharedStorageDenied: (() -> Unit)? = null
    private var storagePermissionFeedbackGeneration = 0L

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            lifecycleScope.launch {
                trackedDefaultNetwork = network
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    delay(80L)
                    if (trackedDefaultNetwork == network) {
                        val capabilities = runCatching { getSystemService(ConnectivityManager::class.java).getNetworkCapabilities(network) }.getOrNull()
                        applyValidatedNetworkState(capabilities?.let(::hasValidatedInternetCapabilities) == true)
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            lifecycleScope.launch {
                if (trackedDefaultNetwork == network) {
                    trackedDefaultNetwork = null
                    applyValidatedNetworkState(false)
                }
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            lifecycleScope.launch {
                trackedDefaultNetwork = network
                applyValidatedNetworkState(hasValidatedInternetCapabilities(networkCapabilities))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerNetworkStateCallback()
    }

    override fun onStop() {
        if (networkCallbackRegistered) {
            runCatching { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(networkCallback) }
            networkCallbackRegistered = false
        }
        super.onStop()
    }

    private fun registerNetworkStateCallback() {
        if (networkCallbackRegistered) return
        val manager = getSystemService(ConnectivityManager::class.java)
        runCatching {
            manager.registerDefaultNetworkCallback(networkCallback)
            networkCallbackRegistered = true
        }
        trackedDefaultNetwork = runCatching { manager.activeNetwork }.getOrNull()
        applyValidatedNetworkState(validatedDefaultNetwork())
    }

    private fun validatedDefaultNetwork(): Boolean = hasValidatedInternet(this)

    private fun applyValidatedNetworkState(validated: Boolean) {
        val previous = lastValidatedNetwork
        validatedNetworkAvailable = validated
        networkStateKnown = true
        if (previous == null) {
            lastValidatedNetwork = validated
            networkBannerState = NetworkBannerState.HIDDEN
            return
        }
        if (previous == validated) return
        lastValidatedNetwork = validated
        networkBannerGeneration += 1L
        val generation = networkBannerGeneration
        networkBannerState = if (validated) NetworkBannerState.CONNECTED else NetworkBannerState.DISCONNECTED
        lifecycleScope.launch {
            var remainingVisibleMillis = 4_500L
            while (networkBannerGeneration == generation && remainingVisibleMillis > 0L) {
                val step = minOf(remainingVisibleMillis, 100L)
                delay(step)
                remainingVisibleMillis -= step
            }
            if (networkBannerGeneration == generation) networkBannerState = NetworkBannerState.HIDDEN
        }
    }

    override fun onResume() {
        super.onResume()
        val pending = pendingUpdateApk
        if (pending != null && pending.exists() && directUpdatesEnabled && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls())) {
            pendingUpdateApk = null
            launchPackageInstaller(pending)
        } else if (pending != null && !directUpdatesEnabled) {
            runCatching { pending.delete() }
            pendingUpdateApk = null
        }
        if (awaitingAllFilesAccessReturn) {
            awaitingAllFilesAccessReturn = false
            val granted = pendingSharedStorageGranted
            val denied = pendingSharedStorageDenied
            pendingSharedStorageGranted = null
            pendingSharedStorageDenied = null
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()) granted?.invoke() else {
                showStoragePermissionDeniedFeedback()
                denied?.invoke()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        startupTimingColdLaunch = savedInstanceState == null
        enableEdgeToEdge()
        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(360L)
                .withEndAction { provider.remove() }
                .start()
        }
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        directUpdatesEnabled = prefs.getBoolean("direct_updates_enabled", true)
        openingAnimationEnabled = prefs.getBoolean("opening_animation_enabled", true)
        cleanupStaleUpdateArtifacts()
        setContent {
            var startupGateOpen by rememberSaveable { mutableStateOf(!openingAnimationEnabled || savedInstanceState != null) }
            LaunchedEffect(startupGateOpen) {
                if (startupGateOpen) completeStartupPostAnimationWork()
            }
            LaunchedEffect(openingAnimationEnabled, startupGateOpen) {
                if (openingAnimationEnabled && !startupGateOpen) {
                    delay(3_200L)
                    startupGateOpen = true
                }
            }
            CompositionLocalProvider(LocalSharedStorageAccessRequest provides { granted, denied -> requestSharedStorageAccess(granted, denied) }) {
                if (startupGateOpen || !openingAnimationEnabled) {
                    OpenGLESScopeApp(this)
                } else {
                    OpenGLESScopeOpeningAnimation(startAnimation = true) { startupGateOpen = true }
                }
            }
        }
    }

    private fun completeStartupPostAnimationWork() {
        if (startupPostAnimationWorkStarted) return
        if (startupTimingColdLaunch && startupGateDelayMs == null) {
            startupGateDelayMs = ((System.nanoTime() - startupActivityStartNanos) / 1_000_000L).coerceAtLeast(0L)
        }
        startupPostAnimationWorkStarted = true
        registerNetworkStateCallback()
        if (directUpdatesEnabled) checkForApplicationUpdate(false) else showDirectUpdatesDisabledIntroIfFirstInstall()
    }

    internal fun persistOpeningAnimationPreference(enabled: Boolean) {
        openingAnimationEnabled = enabled
        prefs.edit().putBoolean("opening_animation_enabled", enabled).apply()
    }


    private fun requestSharedStorageAccess(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()) {
            onGranted()
            return
        }
        if (awaitingAllFilesAccessReturn) return
        pendingSharedStorageGranted = onGranted
        pendingSharedStorageDenied = onDenied
        awaitingAllFilesAccessReturn = true
        val specific = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
        val launched = runCatching { startActivity(specific); true }.getOrElse {
            runCatching { startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)); true }.getOrDefault(false)
        }
        if (!launched) {
            awaitingAllFilesAccessReturn = false
            pendingSharedStorageGranted = null
            pendingSharedStorageDenied = null
            showStoragePermissionDeniedFeedback()
            onDenied()
        }
    }

    private fun showStoragePermissionDeniedFeedback() {
        storagePermissionFeedbackGeneration += 1L
        val generation = storagePermissionFeedbackGeneration
        android.widget.Toast.makeText(this, "Permission denied", android.widget.Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(3_000L)
            if (storagePermissionFeedbackGeneration == generation) Unit
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
            updateStatusHideJob?.cancel()
            updateStatusHideJob = null
            updateCheckInFlight = false
            updateDownloadCancelRequested = true
            updateDownloadPaused = false
            updateDownloadJob?.cancel()
            activeUpdateCheckCall?.cancel()
            activeUpdateDownloadCall?.cancel()
            updateCheckJob = null
            updateDownloadJob = null
            pendingUpdateApk?.let { runCatching { it.delete() } }
            pendingUpdateApk = null
            updateStatus = UpdateStatus.Hidden
            updateConfirmation = null
            updateTransferState?.apk?.let { runCatching { it.delete() } }
            updateTransferState = null
            updateCancelConfirmationVisible = false
        }
    }

    internal fun confirmDirectUpdatesConsent() {
        directUpdatesEnabled = true
        directUpdatesConsentVisible = false
        if (updateStatus is UpdateStatus.DirectUpdatesDisabledIntro) updateStatus = UpdateStatus.Hidden
        prefs.edit().putBoolean("direct_updates_enabled", true).apply()
    }

    private fun runningOpenGlesProbePids(): List<Int> = runCatching {
        val manager = getSystemService(android.app.ActivityManager::class.java) ?: return@runCatching emptyList()
        val expectedName = "${packageName}:opengles_probe"
        manager.runningAppProcesses.orEmpty()
            .filter { it.uid == android.os.Process.myUid() && it.processName == expectedName }
            .map { it.pid }
            .distinct()
    }.getOrElse { error ->
        Log.w("OpenGLESScope", "Unable to inspect the dedicated OpenGL ES probe process", error)
        emptyList()
    }

    private fun stopOpenGlesProbeProcess() {
        runCatching { stopService(Intent(this, OpenGLESProbeService::class.java)) }
        runningOpenGlesProbePids().forEach { pid ->
            runCatching { android.os.Process.killProcess(pid) }.onFailure { error ->
                Log.w("OpenGLESScope", "Unable to kill stale OpenGL ES probe pid=$pid", error)
            }
        }
    }

    private suspend fun ensureOpenGlesProbeProcessQuiescent(timeoutMs: Long = 1_500L): Boolean {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000L
        val stopRequested = runCatching {
            withContext(Dispatchers.Main.immediate) { stopService(Intent(this@MainActivity, OpenGLESProbeService::class.java)) }
        }.getOrDefault(false)
        if (stopRequested) delay(150L)
        while (true) {
            val pids = runningOpenGlesProbePids()
            if (pids.isEmpty()) return true
            pids.forEach { pid -> runCatching { android.os.Process.killProcess(pid) } }
            if (System.nanoTime() >= deadline) {
                Log.e("OpenGLESScope", "Previous dedicated OpenGL ES probe process remained alive after bounded teardown: pids=$pids")
                return false
            }
            delay(25L)
        }
    }

    private suspend fun runOpenGlesServiceProbe(selfTest: Boolean, timeoutMs: Long, maxResultBytes: Long): String = probeMutex.withLock {
        fun unavailable(reason: String): String = JSONObject().put("status", "unavailable").put("reason", reason).toString()
        if (!ensureOpenGlesProbeProcessQuiescent()) {
            return@withLock unavailable("The previous dedicated OpenGL ES probe process could not be terminated within the bounded teardown window.")
        }
        val probeDir = File(cacheDir, "probe").canonicalFile.apply { mkdirs() }
        val prefix = if (selfTest) "opengles-selftest-" else "opengles-"
        val resultFile = File(probeDir, "$prefix${java.util.UUID.randomUUID()}.json").canonicalFile
        val terminalFile = File(resultFile.absolutePath + ".done").canonicalFile
        if (resultFile.parentFile != probeDir || terminalFile.parentFile != probeDir || terminalFile.path != resultFile.path + ".done") {
            return@withLock unavailable("Probe result path validation failed")
        }
        resultFile.delete()
        terminalFile.delete()
        val intent = Intent(this@MainActivity, OpenGLESProbeService::class.java)
            .putExtra(OpenGLESProbeService.EXTRA_RESULT_PATH, resultFile.absolutePath)
            .putExtra(OpenGLESProbeService.EXTRA_TERMINAL_PATH, terminalFile.absolutePath)
            .putExtra(OpenGLESProbeService.EXTRA_TIMEOUT_MS, timeoutMs)
            .putExtra(OpenGLESProbeService.EXTRA_SELF_TEST, selfTest)
        val started = runCatching { withContext(Dispatchers.Main.immediate) { startService(intent) } }.isSuccess
        if (!started) {
            resultFile.delete()
            terminalFile.delete()
            return@withLock unavailable(if (selfTest) "OpenGL ES self-test service could not be started" else "OpenGL ES probe service could not be started")
        }
        withContext(Dispatchers.IO) {
            var value: String? = null
            fun readCandidate(): String? {
                if (!resultFile.isFile || resultFile.length() !in 1L..maxResultBytes) return null
                return runCatching { resultFile.readText(Charsets.UTF_8) }.getOrNull()
            }
            suspend fun stableTerminalCandidate(graceMs: Long = 300L): String? {
                val deadline = System.nanoTime() + graceMs * 1_000_000L
                do {
                    val candidate = readCandidate()
                    if (candidate != null && strictOpenGlesProbeTerminalCandidate(candidate, selfTest)) return candidate
                    delay(20L)
                } while (System.nanoTime() < deadline)
                return readCandidate()?.takeIf { strictOpenGlesProbeTerminalCandidate(it, selfTest) }
            }
            try {
                withTimeout(timeoutMs) {
                    while (value == null) {
                        if (terminalFile.isFile) {
                            value = stableTerminalCandidate() ?: unavailable("The dedicated OpenGL ES probe signaled completion but no valid terminal JSON became readable within the bounded handoff window.")
                            continue
                        }
                        if (resultFile.isFile && resultFile.length() > maxResultBytes) {
                            value = unavailable("OpenGL ES probe result exceeded the safety size limit")
                            stopOpenGlesProbeProcess()
                            continue
                        }
                        delay(40L)
                    }
                }
            } catch (_: TimeoutCancellationException) {
                val publishedBeforeTimeout = readCandidate()?.takeIf { strictOpenGlesProbeTerminalCandidate(it, selfTest) }
                stopOpenGlesProbeProcess()
                delay(150L)
                val settled = publishedBeforeTimeout ?: readCandidate()?.takeIf { strictOpenGlesProbeTerminalCandidate(it, selfTest) }
                value = settled ?: unavailable(if (selfTest) "OpenGL ES self-test timed out" else "OpenGL ES probe did not complete within ${timeoutMs / 1000} seconds")
                if (settled != null) Log.w("OpenGLESScope", "Recovered an atomic OpenGL ES probe publication at the timeout boundary.")
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    if (!ensureOpenGlesProbeProcessQuiescent()) {
                        Log.e("OpenGLESScope", "Cancelled OpenGL ES probe did not reach a confirmed quiescent state before cleanup.")
                    }
                }
                throw cancelled
            } finally {
                resultFile.delete()
                terminalFile.delete()
            }
            value ?: unavailable("The dedicated OpenGL ES probe returned no result")
        }
    }

    internal suspend fun collectOpenGlesReport(): String = runOpenGlesServiceProbe(
        selfTest = false,
        timeoutMs = 20_000L,
        maxResultBytes = 8L * 1024L * 1024L
    )
internal fun checkForApplicationUpdate(showProgress: Boolean) {
        if (!validatedDefaultNetwork()) {
            if (showProgress) updateStatus = UpdateStatus.Failed("No validated internet connection. Internet-dependent update checks are unavailable until connectivity returns.")
            return
        }
        if (!directUpdatesEnabled) {
            if (showProgress) updateStatus = UpdateStatus.Failed("Direct GitHub updates are disabled. Use Obtainium for external update management or enable them in Settings.")
            return
        }
        if (updateCheckInFlight || updateDownloadJob != null || updateTransferState != null) return
        updateStatusHideJob?.cancel()
        updateStatusHideJob = null
        updateCheckInFlight = true
        updateCheckJob = lifecycleScope.launch {
            if (showProgress) updateStatus = UpdateStatus.Checking
            val result = try {
                withContext(Dispatchers.IO) { fetchLatestCompatibleUpdateResult() }
            } finally {
                updateCheckInFlight = false
            }
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
                val displayedStatus = updateStatus
                val displayDurationMillis = if (displayedStatus is UpdateStatus.UpToDate) 8_000L else 10_000L
                updateStatusHideJob = lifecycleScope.launch {
                    delay(displayDurationMillis)
                    if (!updateCheckInFlight && updateStatus == displayedStatus && updateStatus !is UpdateStatus.Downloading) updateStatus = UpdateStatus.Hidden
                }
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
            .header("X-GitHub-Api-Version", GITHUB_API_VERSION)
            .header("User-Agent", "OpenGLESScope/${installedVersionName()}")
            .get()
            .build()
        val call = UPDATE_METADATA_HTTP_CLIENT.newCall(request)
        activeUpdateCheckCall = call
        return try {
            call.execute().use { response ->
                if (!response.isSuccessful) error("Update check failed (HTTP ${response.code}).")
            val releases = JSONArray(readResponseTextLimited(response.body, 2 * 1024 * 1024))
            val current = installedVersionName()
            val candidates = (0 until releases.length())
                .mapNotNull { releases.optJSONObject(it) }
                .filter { !it.optBoolean("draft", false) && !it.optBoolean("prerelease", false) }
                .mapNotNull { release ->
                    val version = release.optString("tag_name").trim().removePrefix("v")
                    if (version.isBlank() || !version.matches(Regex("\\d+(?:\\.\\d+){1,3}(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?"))) null else release to version
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
            val digest = selected.optString("digest").trim().takeIf { it.isNotBlank() }
            val expectedSha256 = digest?.let { raw ->
                Regex("^sha256:([0-9a-fA-F]{64})$").matchEntire(raw)?.groupValues?.get(1)?.lowercase(java.util.Locale.ROOT)
                    ?: error("The official release asset exposes an unsupported or malformed digest.")
            }
            val assetSizeBytes = selected.optLong("size", -1L).takeIf { it in 1..(256L * 1024L * 1024L) }
            AppUpdate(latest, selected.optString("name"), url, expectedSha256, assetSizeBytes, json.optString("body").trim().ifBlank { "No release notes were provided for this GitHub release." }, abi, if (exact != null) abi else "universal", current, installedVersionCode())
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
        if (!directUpdatesEnabled || updateDownloadJob?.isActive == true) return
        if (!validatedDefaultNetwork()) {
            updateStatus = UpdateStatus.Failed("No validated internet connection. The update download remains unavailable while offline.")
            return
        }
        updateStatusHideJob?.cancel()
        updateStatusHideJob = null
        updateStatus = UpdateStatus.Hidden
        updateConfirmation = null
        updateDownloadPaused = false
        updateDownloadCancelRequested = false
        updateCancelConfirmationVisible = false
        updateTransferState = UpdateTransferState(
            update = update,
            phase = UpdateTransferPhase.CONNECTING,
            connectionStatus = "Connecting to the official GitHub release asset",
            log = listOf("Preparing secure OpenGLESScope update download…")
        )
        updateDownloadJob = lifecycleScope.launch {
            try {
                val apk = downloadUpdateApk(update)
                if (!directUpdatesEnabled) {
                    runCatching { apk.delete() }
                    updateTransferState = null
                } else {
                    val prior = updateTransferState
                    updateTransferState = (prior ?: UpdateTransferState(update, UpdateTransferPhase.COMPLETED)).copy(
                        phase = UpdateTransferPhase.COMPLETED,
                        bytesPerSecond = 0L,
                        connectionStatus = "Download verified and ready to install",
                        log = appendBoundedUpdateLog(prior?.log.orEmpty(), "Package identity, signing certificate, version and release metadata verified."),
                        apk = apk,
                        errorMessage = null
                    )
                }
            } catch (cancelled: CancellationException) {
                if (updateDownloadCancelRequested) {
                    val prior = updateTransferState
                    updateTransferState = prior?.copy(
                        phase = UpdateTransferPhase.CANCELED,
                        bytesPerSecond = 0L,
                        connectionStatus = "Download canceled",
                        log = appendBoundedUpdateLog(prior.log, "Update download canceled by the user."),
                        errorMessage = null
                    )
                } else if (directUpdatesEnabled) {
                    throw cancelled
                } else {
                    updateTransferState = null
                }
            } catch (error: Throwable) {
                val prior = updateTransferState
                if (updateDownloadCancelRequested) {
                    updateTransferState = prior?.copy(
                        phase = UpdateTransferPhase.CANCELED,
                        bytesPerSecond = 0L,
                        connectionStatus = "Download canceled",
                        log = appendBoundedUpdateLog(prior.log, "Update download canceled by the user."),
                        errorMessage = null
                    )
                } else if (directUpdatesEnabled) {
                    updateTransferState = (prior ?: UpdateTransferState(update, UpdateTransferPhase.FAILED)).copy(
                        phase = UpdateTransferPhase.FAILED,
                        bytesPerSecond = 0L,
                        connectionStatus = "Download failed",
                        log = appendBoundedUpdateLog(prior?.log.orEmpty(), "Failure: ${error.message ?: "unknown update error"}"),
                        errorMessage = error.message ?: "Update download failed."
                    )
                } else {
                    updateTransferState = null
                }
            } finally {
                updateDownloadPaused = false
                updateDownloadCancelRequested = false
                activeUpdateDownloadCall = null
                updateDownloadJob = null
            }
        }
    }

    internal fun pauseUpdateDownload() {
        val state = updateTransferState ?: return
        if (state.phase != UpdateTransferPhase.DOWNLOADING && state.phase != UpdateTransferPhase.CONNECTING) return
        updateDownloadPaused = true
        updateTransferState = state.copy(
            phase = UpdateTransferPhase.PAUSED,
            bytesPerSecond = 0L,
            connectionStatus = "Paused",
            log = appendBoundedUpdateLog(state.log, "Download paused.")
        )
    }

    internal fun resumeUpdateDownload() {
        val state = updateTransferState ?: return
        if (state.phase != UpdateTransferPhase.PAUSED || updateDownloadJob?.isActive != true) return
        if (!validatedDefaultNetwork()) {
            updateTransferState = state.copy(
                connectionStatus = "Paused — waiting for validated internet",
                log = appendBoundedUpdateLog(state.log, "Resume blocked because validated internet is unavailable.")
            )
            return
        }
        updateDownloadPaused = false
        updateTransferState = state.copy(
            phase = UpdateTransferPhase.DOWNLOADING,
            connectionStatus = "Resuming download",
            log = appendBoundedUpdateLog(state.log, "Download resumed.")
        )
    }
internal fun requestCancelUpdateDownload() {
        val state = updateTransferState ?: return
        if (state.phase !in setOf(UpdateTransferPhase.CONNECTING, UpdateTransferPhase.DOWNLOADING, UpdateTransferPhase.PAUSED)) return
        if (state.phase != UpdateTransferPhase.PAUSED) pauseUpdateDownload()
        updateCancelConfirmationVisible = true
    }

    internal fun dismissCancelUpdateDownload() {
        updateCancelConfirmationVisible = false
        val state = updateTransferState ?: return
        if (state.phase == UpdateTransferPhase.PAUSED && updateDownloadJob?.isActive == true) resumeUpdateDownload()
    }

    internal fun confirmCancelUpdateDownload() {
        updateCancelConfirmationVisible = false
        updateDownloadCancelRequested = true
        updateDownloadPaused = false
        activeUpdateDownloadCall?.cancel()
        val state = updateTransferState
        if (state != null) {
            updateTransferState = state.copy(
                connectionStatus = "Canceling…",
                log = appendBoundedUpdateLog(state.log, "Cancel requested; closing the active transfer.")
            )
        }
    }
internal fun installDownloadedUpdate() {
        val state = updateTransferState ?: return
        if (state.phase != UpdateTransferPhase.COMPLETED) return
        val apk = state.apk?.takeIf { it.isFile } ?: return
        updateTransferState = state.copy(log = appendBoundedUpdateLog(state.log, "Opening Android package installer…"))
        requestPackageInstall(apk)
    }
internal fun closeUpdateTransfer() {
        val state = updateTransferState ?: return
        if (state.phase !in setOf(UpdateTransferPhase.COMPLETED, UpdateTransferPhase.CANCELED, UpdateTransferPhase.FAILED)) return
        state.apk?.let { runCatching { it.delete() } }
        updateTransferState = null
        updateCancelConfirmationVisible = false
    }

    private fun appendBoundedUpdateLog(existing: List<String>, message: String): List<String> =
        (existing + message).takeLast(120)

    private suspend fun publishUpdateTransfer(transform: (UpdateTransferState) -> UpdateTransferState) {
        withContext(Dispatchers.Main.immediate) {
            updateTransferState = updateTransferState?.let(transform)
        }
    }

    private suspend fun awaitUpdateTransferPermission() {
        while (updateDownloadPaused) {
            currentCoroutineContext().ensureActive()
            if (updateDownloadCancelRequested || !directUpdatesEnabled) throw CancellationException("Update download canceled.")
            delay(100L)
        }
        currentCoroutineContext().ensureActive()
        if (updateDownloadCancelRequested || !directUpdatesEnabled) throw CancellationException("Update download canceled.")
    }

    private suspend fun downloadUpdateApk(update: AppUpdate): File = withContext(Dispatchers.IO) {
        val safeAssetName = update.assetName.substringAfterLast('/').substringAfterLast('\\')
            .takeIf { it.endsWith(".apk", true) && it.length in 5..160 }
            ?: error("The release asset has an invalid APK filename.")
        val updateDir = File(cacheDir, "updates").apply { mkdirs() }
        val target = File(updateDir, safeAssetName)
        if (target.parentFile?.canonicalFile != updateDir.canonicalFile) error("The release asset path is invalid.")
        val temp = File(updateDir, "$safeAssetName.part")
        runCatching { temp.delete() }
        runCatching { target.delete() }
        try {
            downloadOfficialReleaseAsset(update, temp)
            publishUpdateTransfer { state ->
                state.copy(
                    phase = UpdateTransferPhase.VERIFYING,
                    bytesPerSecond = 0L,
                    connectionStatus = "Verifying downloaded package",
                    log = appendBoundedUpdateLog(state.log, "Download complete; starting cryptographic and APK verification.")
                )
            }
            update.expectedSha256?.let { expected ->
                val actual = sha256FileHex(temp)
                if (!actual.equals(expected, ignoreCase = true)) error("Downloaded package SHA-256 does not match the official GitHub release asset digest.")
                publishUpdateTransfer { state -> state.copy(log = appendBoundedUpdateLog(state.log, "Official GitHub SHA-256 digest matched.")) }
            }
            awaitUpdateTransferPermission()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            try {
                val archive = packageManager.getPackageArchiveInfo(target.absolutePath, flags) ?: error("Downloaded file is not a valid Android package.")
                if (archive.packageName != packageName) error("Downloaded package identity does not match OpenGLESScope.")
                val installed = packageManager.getPackageInfo(packageName, flags)
                if (!packageSigningCertificatesMatch(installed, archive)) error("Downloaded package signing certificate does not match the installed OpenGLESScope build.")
                val archiveCode = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else {
                    @Suppress("DEPRECATION")
                    archive.versionCode.toLong()
                }
                val installedCode = if (Build.VERSION.SDK_INT >= 28) installed.longVersionCode else {
                    @Suppress("DEPRECATION")
                    installed.versionCode.toLong()
                }
                if (archiveCode <= installedCode) error("Downloaded package versionCode is not newer than the installed OpenGLESScope build.")
                val archiveVersion = archive.versionName ?: error("Downloaded package has no version metadata.")
                if (!isNewerVersion(archiveVersion, installedVersionName())) error("Downloaded package versionName is not newer than the installed OpenGLESScope version.")
                if (archiveVersion != update.version) error("Downloaded package versionName does not exactly match the selected official GitHub release (${update.version}).")
                target
            } catch (error: Throwable) {
                runCatching { target.delete() }
                throw error
            }
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private suspend fun downloadOfficialReleaseAsset(update: AppUpdate, temp: File) {
        var currentUrl = update.downloadUrl.toHttpUrlOrNull() ?: error("The release APK URL is invalid.")
        var redirectCount = 0
        while (true) {
            awaitUpdateTransferPermission()
            if (!validatedDefaultNetwork()) error("Validated internet access was lost during the update transfer.")
            publishUpdateTransfer { state ->
                state.copy(
                    phase = UpdateTransferPhase.CONNECTING,
                    connectionStatus = if (redirectCount == 0) "Connecting to GitHub" else "Following approved GitHub release redirect",
                    log = if (redirectCount == 0) state.log else appendBoundedUpdateLog(state.log, "Following approved release-asset redirect ${redirectCount}/$MAX_UPDATE_REDIRECTS.")
                )
            }
            val request = Request.Builder().url(currentUrl).header("User-Agent", "OpenGLESScope/${installedVersionName()}").get().build()
            val call = UPDATE_DOWNLOAD_HTTP_CLIENT.newCall(request)
            activeUpdateDownloadCall = call
            val response = try { call.execute() } catch (error: Throwable) {
                if (activeUpdateDownloadCall === call) activeUpdateDownloadCall = null
                throw error
            }
            if (response.code in setOf(301, 302, 303, 307, 308)) {
                val location = response.header("Location")
                response.close()
                if (activeUpdateDownloadCall === call) activeUpdateDownloadCall = null
                val next = location?.let { currentUrl.resolve(it) } ?: error("Update redirect is missing a valid Location header.")
                redirectCount += 1
                if (redirectCount > MAX_UPDATE_REDIRECTS) error("Update download exceeded the redirect safety limit.")
                if (!isAllowedOfficialUpdateRedirect(next)) error("Update redirect target is not an approved GitHub release-asset host.")
                currentUrl = next
                continue
            }
            try {
                response.use { finalResponse ->
                    if (!finalResponse.isSuccessful) error("Update download failed (HTTP ${finalResponse.code}).")
                    val body = finalResponse.body
                    val responseLength = body.contentLength().takeIf { it >= 0L }
                    val trustedLength = update.assetSizeBytes
                    if (trustedLength != null && responseLength != null && trustedLength != responseLength) error("Update package size does not match the official GitHub release metadata.")
                    val reportedLength = trustedLength ?: responseLength
                    if (reportedLength != null && reportedLength > 256L * 1024L * 1024L) error("Update package exceeds the safety limit.")
                    publishUpdateTransfer { state ->
                        state.copy(
                            phase = UpdateTransferPhase.DOWNLOADING,
                            totalBytes = reportedLength,
                            connectionStatus = "Downloading from an approved GitHub release-asset host",
                            log = appendBoundedUpdateLog(state.log, "Secure transfer started${reportedLength?.let { " (${formatUpdateBytes(it)})" }.orEmpty()}.")
                        )
                    }
                    body.byteStream().use { input ->
                        FileOutputStream(temp).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var total = 0L
                            var speedWindowBytes = 0L
                            var speedWindowStarted = System.nanoTime()
                            var lastLogAt = speedWindowStarted
                            while (true) {
                                awaitUpdateTransferPermission()
                                if (!validatedDefaultNetwork()) error("Validated internet access was lost during the update transfer.")
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                speedWindowBytes += count
                                if (total > 256L * 1024L * 1024L) error("Update package exceeds the safety limit.")
                                output.write(buffer, 0, count)
                                val now = System.nanoTime()
                                val elapsed = now - speedWindowStarted
                                if (elapsed >= 400_000_000L) {
                                    val speed = ((speedWindowBytes.toDouble() * 1_000_000_000.0) / elapsed.toDouble()).toLong().coerceAtLeast(0L)
                                    val addLog = now - lastLogAt >= 2_000_000_000L
                                    publishUpdateTransfer { state ->
                                        state.copy(
                                            phase = if (updateDownloadPaused) UpdateTransferPhase.PAUSED else UpdateTransferPhase.DOWNLOADING,
                                            bytesDownloaded = total,
                                            totalBytes = reportedLength,
                                            bytesPerSecond = if (updateDownloadPaused) 0L else speed,
                                            connectionStatus = if (updateDownloadPaused) "Paused" else "Downloading",
                                            log = if (addLog) appendBoundedUpdateLog(state.log, "Received ${formatUpdateBytes(total)}${reportedLength?.let { " / ${formatUpdateBytes(it)}" }.orEmpty()} at ${formatUpdateSpeed(speed)}.") else state.log
                                        )
                                    }
                                    if (addLog) lastLogAt = now
                                    speedWindowBytes = 0L
                                    speedWindowStarted = now
                                }
                            }
                            output.fd.sync()
                            update.assetSizeBytes?.let { expectedSize ->
                                if (total != expectedSize) error("Downloaded package size does not match the official GitHub release metadata.")
                            }
                            publishUpdateTransfer { state -> state.copy(bytesDownloaded = total, totalBytes = reportedLength ?: total, bytesPerSecond = 0L) }
                        }
                    }
                }
            } finally {
                if (activeUpdateDownloadCall === call) activeUpdateDownloadCall = null
            }
            return
        }
    }

    private fun formatUpdateBytes(bytes: Long): String = when {
        bytes >= 1024L * 1024L * 1024L -> String.format(java.util.Locale.US, "%.2f GiB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f MiB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format(java.util.Locale.US, "%.1f KiB", bytes / 1024.0)
        else -> "$bytes B"
    }

    private fun formatUpdateSpeed(bytesPerSecond: Long): String = if (bytesPerSecond <= 0L) "—" else "${formatUpdateBytes(bytesPerSecond)}/s"

    private fun isAllowedOfficialUpdateRedirect(url: okhttp3.HttpUrl): Boolean {
        if (url.scheme != "https" || url.username.isNotEmpty() || url.password.isNotEmpty() || url.fragment != null) return false
        return when (url.host) {
            "github.com" -> url.query == null && url.encodedPath.startsWith("/EFIShell0/OpenGLESScope/releases/download/")
            "release-assets.githubusercontent.com", "objects.githubusercontent.com" -> true
            else -> false
        }
    }

    private fun sha256FileHex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun cleanupStaleUpdateArtifacts() {
        val updateDir = File(cacheDir, "updates")
        if (!updateDir.isDirectory) return
        updateDir.listFiles().orEmpty().forEach { file ->
            if (file.isFile && (file.name.endsWith(".apk", true) || file.name.endsWith(".part", true))) runCatching { file.delete() }
        }
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
        updateDownloadCancelRequested = true
        updateDownloadPaused = false
        activeUpdateDownloadCall?.cancel()
        updateCheckJob?.cancel()
        updateStatusHideJob?.cancel()
        updateDownloadJob?.cancel()
        updateCheckInFlight = false
        super.onDestroy()
    }


    internal suspend fun runOpenGlesSelfTests(expected: GlReport): String {
        val raw = runOpenGlesServiceProbe(selfTest = true, timeoutMs = 20_000L, maxResultBytes = 1024L * 1024L)
        val parsed = runCatching { JSONObject(raw) }.getOrNull()
            ?: return JSONObject().put("status", "unavailable").put("reason", "Self-test result JSON was invalid").toString()
        if (parsed.optString("status").startsWith("completed")) {
            val sameVendor = parsed.optString("vendor") == expected.vendor
            val sameRenderer = parsed.optString("renderer") == expected.renderer
            val sameVersion = parsed.optString("runtimeVersion") == expected.glVersion
            if (!sameVendor || !sameRenderer || !sameVersion) {
                return JSONObject()
                    .put("status", "unavailable")
                    .put("reason", "Isolated OpenGL ES runtime identity did not match the selected report; test attribution was refused")
                    .put("vendor", parsed.optString("vendor", "Unknown"))
                    .put("renderer", parsed.optString("renderer", "Unknown"))
                    .put("runtimeVersion", parsed.optString("runtimeVersion", "Unknown"))
                    .put("tests", JSONArray())
                    .toString()
            }
        }
        return raw
    }

}

private fun parseReport(raw: String): GlReport {
    return try {
        val o = JSONObject(raw)
        val eglObj = o.optJSONObject("egl") ?: JSONObject()
        val glRuntimeObj = o.optJSONObject("glRuntime") ?: JSONObject()
        val eglRuntimeObj = o.optJSONObject("eglRuntime") ?: JSONObject()
        val completeSnapshot = OpenGLESProbeContract.isCompleteAvailableReport(o)
        val available = completeSnapshot
        val limits = o.optJSONArray("limits").toObjects().map { x -> LimitEntry(x.optString("name", "Unknown"), x.optString("value", "Unknown")) }
        val precision = o.optJSONArray("precision").toObjects().map { x -> PrecisionEntry(x.optString("shader"), x.optString("type"), x.optInt("rangeMin"), x.optInt("rangeMax"), x.optInt("precision")) }
        val diagnostics = o.optJSONArray("diagnostics").toObjects().map { x -> QueryDiagnostic(x.optString("name", "Unknown"), x.optString("status", "Unknown"), x.optString("detail", "")) }
        val eglCapabilities = o.optJSONArray("eglCapabilities").toObjects().map { x -> EglCapabilityEntry(x.optString("name", "Unknown"), x.optString("status", "Unknown"), x.optString("value", ""), x.optString("detail", "")) }
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
        val internalFormats = o.optJSONArray("internalFormats").toObjects().map { x ->
            InternalFormatEntry(
                x.optString("target", "Unknown"),
                x.optString("internalFormat", "Unknown"),
                x.optString("status", "Unknown"),
                x.optString("detail", ""),
                buildList { x.optJSONArray("sampleCounts")?.let { a -> for (i in 0 until a.length()) add(a.optInt(i)) } },
                x.optJSONArray("nvSampleProperties").toObjects().map { n ->
                    NvSampleProperty(n.optInt("samples"), n.optInt("multisamples"), n.optInt("supersampleScaleX"), n.optInt("supersampleScaleY"), n.optBoolean("conformant"))
                }
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
            GlRuntimeInfo(
                glRuntimeObj.optNullableString("contextFlags"), glRuntimeObj.optNullableString("resetNotificationStrategy"), glRuntimeObj.optNullableString("resetNotificationStrategyQuery"), glRuntimeObj.optNullableBoolean("robustAccess"), glRuntimeObj.optNullableString("robustAccessQuery"),
                glRuntimeObj.optJSONArray("unavailableAttributes").toObjects().map { a -> EglUnavailableAttribute(a.optString("name", "Unknown"), a.optString("error", "Unknown GL error")) }
            ),
            EglRuntimeInfo(
                eglRuntimeObj.optString("boundApi", "Unknown"), eglRuntimeObj.optNullableInt("configId"), eglRuntimeObj.optNullableString("clientType"), eglRuntimeObj.optNullableInt("clientVersion"), eglRuntimeObj.optNullableString("renderBuffer"),
                eglRuntimeObj.optBoolean("currentContext", false), eglRuntimeObj.optBoolean("currentDisplay", false), eglRuntimeObj.optBoolean("currentDrawSurface", false), eglRuntimeObj.optBoolean("currentReadSurface", false),
                eglRuntimeObj.optNullableString("surfaceGlColorspace"), eglRuntimeObj.optNullableString("surfaceGlColorspaceQuery"),
                eglRuntimeObj.optNullableString("surfaceVgAlphaFormat"), eglRuntimeObj.optNullableString("surfaceVgAlphaFormatQuery"),
                eglRuntimeObj.optNullableString("surfaceVgColorspace"), eglRuntimeObj.optNullableString("surfaceVgColorspaceQuery"), eglRuntimeObj.optNullableInt("surfaceConfigId"),
                eglRuntimeObj.optNullableInt("surfaceWidth"), eglRuntimeObj.optNullableInt("surfaceHeight"), eglRuntimeObj.optNullableInt("surfaceHorizontalResolution"), eglRuntimeObj.optNullableBoolean("surfaceLargestPbuffer"),
                eglRuntimeObj.optNullableInt("surfacePixelAspectRatio"), eglRuntimeObj.optNullableInt("surfaceVerticalResolution"), eglRuntimeObj.optNullableString("surfaceRenderBuffer"), eglRuntimeObj.optNullableString("surfaceSwapBehavior"),
                eglRuntimeObj.optNullableString("surfaceTextureFormat"), eglRuntimeObj.optNullableString("surfaceTextureTarget"), eglRuntimeObj.optNullableBoolean("surfaceMipmapTexture"), eglRuntimeObj.optNullableInt("surfaceMipmapLevel"), eglRuntimeObj.optNullableString("surfaceMultisampleResolve"),
                eglRuntimeObj.optJSONArray("unavailableAttributes").toObjects().map { a -> EglUnavailableAttribute(a.optString("name", "Unknown"), a.optString("error", "Unknown EGL error")) }
            ),
            eglCapabilities,
            o.optJSONArray("extensions").toStrings(),
            limits,
            o.optJSONArray("compressedFormats").toStrings(),
            internalFormats,
            o.optJSONArray("shaderBinaryFormats").toStrings(),
            o.optJSONArray("programBinaryFormats").toStrings(),
            precision,
            configs,
            diagnostics
        )
    } catch (e: Exception) {
        GlReport(false, e.message ?: "OpenGL ES report parsing failed", "Unknown", "Unknown", "Unknown", 0, 0, "Unknown", EglInfo("Unknown", "Unknown", "Unknown", "Unknown", emptyList(), emptyList()), GlRuntimeInfo(null, null, null, null, null, emptyList()), EglRuntimeInfo(
            boundApi = "Unknown", configId = null, clientType = null, clientVersion = null, renderBuffer = null,
            currentContext = false, currentDisplay = false, currentDrawSurface = false, currentReadSurface = false,
            surfaceGlColorspace = null, surfaceGlColorspaceQuery = null, surfaceVgAlphaFormat = null, surfaceVgAlphaFormatQuery = null,
            surfaceVgColorspace = null, surfaceVgColorspaceQuery = null, surfaceConfigId = null, surfaceWidth = null, surfaceHeight = null,
            surfaceHorizontalResolution = null, surfaceLargestPbuffer = null, surfacePixelAspectRatio = null, surfaceVerticalResolution = null,
            surfaceRenderBuffer = null, surfaceSwapBehavior = null, surfaceTextureFormat = null, surfaceTextureTarget = null,
            surfaceMipmapTexture = null, surfaceMipmapLevel = null, surfaceMultisampleResolve = null, unavailableAttributes = emptyList()
        ), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

private fun JSONArray?.toStrings(): List<String> = if (this == null) emptyList() else List(length()) { optString(it) }
private fun JSONArray?.toObjects(): List<JSONObject> = if (this == null) emptyList() else List(length()) { optJSONObject(it) ?: JSONObject() }
private fun JSONObject.optNullableInt(name: String): Int? = if (isNull(name) || !has(name)) null else optInt(name)
private fun JSONObject.optNullableString(name: String): String? = if (isNull(name) || !has(name)) null else optString(name).takeIf { it.isNotBlank() }
private fun JSONObject.optNullableBoolean(name: String): Boolean? = if (isNull(name) || !has(name)) null else optBoolean(name)
private fun formatTimestampOrUnavailable(value: Long?): String = value?.let {
    java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date(it))
} ?: "Not available"

private fun formatBytes(value: Long): String {
    if (value <= 0) return "0 B"
    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
    var number = value.toDouble()
    var index = 0
    while (number >= 1024.0 && index < units.lastIndex) {
        number /= 1024.0
        index++
    }
    return String.format(java.util.Locale.US, "%.2f %s", number, units[index])
}

private fun safeFilePart(s: String): String = s.replace(Regex("[^A-Za-z0-9._-]+"), "_").take(80).ifBlank { "device" }
private fun eglBooleanEvidence(value: Boolean?): String = when (value) {
    true -> "EGL_TRUE"
    false -> "EGL_FALSE"
    null -> "Unavailable"
}

private fun eglScaledSurfaceEvidence(value: Int?, aspect: Boolean = false): String = when {
    value == null -> "Unavailable"
    value == -1 -> "EGL_UNKNOWN (-1)"
    aspect -> "$value raw / EGL_DISPLAY_SCALING = ${String.format(java.util.Locale.US, "%.4f", value / 10000.0)}"
    else -> "$value raw / EGL_DISPLAY_SCALING = ${String.format(java.util.Locale.US, "%.4f", value / 10000.0)} px/m"
}

private fun runtimeQueryEvidence(r: GlReport, value: String?, vararg names: String): String {
    if (value != null) return value
    val diagnostics = names.mapNotNull { name -> r.diagnostics.firstOrNull { it.name == name } }
    val diagnostic = diagnostics.firstOrNull { !it.status.equals("Not applicable", true) } ?: diagnostics.firstOrNull()
    if (diagnostic == null) return "Unknown / not queried"
    return if (diagnostic.detail.isBlank()) diagnostic.status else "${diagnostic.status} · ${diagnostic.detail}"
}

private fun eglConfigAttributeEvidence(c: EglConfigEntry, key: String, value: String?): String {
    if (value != null) return value
    val failure = c.unavailableAttributes.firstOrNull { it.name == key } ?: return "Unknown / not queried"
    return if (failure.error.startsWith("Not applicable:", ignoreCase = true)) {
        "Not applicable · ${failure.error.substringAfter(':').trim()}"
    } else {
        "Unavailable · ${failure.error}"
    }
}

private fun eglConfigIntEvidence(c: EglConfigEntry, key: String, value: Int?): String =
    eglConfigAttributeEvidence(c, key, value?.toString())

private fun eglConfigBooleanEvidence(c: EglConfigEntry, key: String, value: Int?): String =
    eglConfigAttributeEvidence(c, key, value?.let { eglBooleanLabel(it) })

private fun eglConfigExtensionEvidence(
    r: GlReport,
    c: EglConfigEntry,
    value: String?,
    extensionName: String,
    attributeName: String
): String {
    if (value != null) return value
    if (extensionName !in r.egl.extensions) return "Not applicable · $extensionName is not advertised"
    val failure = c.unavailableAttributes.firstOrNull { it.name == attributeName }
    return if (failure != null) "Unavailable · ${failure.error}" else "Unknown / not queried"
}

private fun eglRuntimeSizeEvidence(r: GlReport): String {
    val width = runtimeQueryEvidence(r, r.eglRuntime.surfaceWidth?.toString(), "EGL_WIDTH")
    val height = runtimeQueryEvidence(r, r.eglRuntime.surfaceHeight?.toString(), "EGL_HEIGHT")
    return if (r.eglRuntime.surfaceWidth != null && r.eglRuntime.surfaceHeight != null) "${r.eglRuntime.surfaceWidth} × ${r.eglRuntime.surfaceHeight}" else "width=$width · height=$height"
}

private fun coreVersionProvenance(r: GlReport): String {
    val directMajor = r.diagnostics.any { it.name == "GL_MAJOR_VERSION" && it.status == "Available" }
    val directMinor = r.diagnostics.any { it.name == "GL_MINOR_VERSION" && it.status == "Available" }
    return if (directMajor && directMinor) "Direct GL_MAJOR_VERSION / GL_MINOR_VERSION query" else "Parsed from GL_VERSION runtime string"
}

private fun displayInfo(activity: Activity): DisplayInfo {
    val d = if (Build.VERSION.SDK_INT >= 30) activity.display else @Suppress("DEPRECATION") activity.windowManager.defaultDisplay
    if (d == null) return DisplayInfo("Unavailable", null, null, null, null, emptyList(), emptyList(), null, null, null, null, "unknown")
    val hdr = if (Build.VERSION.SDK_INT >= 24) d.hdrCapabilities else null
    val rawTypes = when {
        Build.VERSION.SDK_INT >= 34 -> d.mode.supportedHdrTypes.toList()
        Build.VERSION.SDK_INT >= 24 && hdr != null -> @Suppress("DEPRECATION") hdr.supportedHdrTypes.toList()
        else -> emptyList()
    }
    val types = rawTypes.filter { it != Display.HdrCapabilities.HDR_TYPE_INVALID }.distinct().map { hdrName(it) }
    val hdrCapabilityStatus = when {
        Build.VERSION.SDK_INT >= 34 -> if (types.isNotEmpty()) "available" else "unavailable"
        Build.VERSION.SDK_INT >= 24 && hdr == null -> "unknown"
        Build.VERSION.SDK_INT >= 24 -> if (types.isNotEmpty()) "available" else "unavailable"
        else -> "unknown"
    }
    val wide = if (Build.VERSION.SDK_INT >= 26) d.isWideColorGamut else null
    val invalid = if (Build.VERSION.SDK_INT >= 24) Display.HdrCapabilities.INVALID_LUMINANCE else -1f
    fun validLuminance(v: Float?): Float? = v?.takeIf { it != invalid && it >= 0f && it.isFinite() }
    val mode = d.mode
    val supportedModes = d.supportedModes.map { candidate -> "${candidate.physicalWidth}×${candidate.physicalHeight} @ ${String.format(java.util.Locale.US, "%.2f", candidate.refreshRate)} Hz" }.distinct()
    return DisplayInfo(d.name.ifBlank { "Unavailable" }, mode.modeId, mode.physicalWidth, mode.physicalHeight, mode.refreshRate, supportedModes, types, validLuminance(hdr?.desiredMaxLuminance), validLuminance(hdr?.desiredMaxAverageLuminance), validLuminance(hdr?.desiredMinLuminance), wide, hdrCapabilityStatus)
}

private fun hdrTypesText(display: DisplayInfo): String = when {
    display.hdrTypes.isNotEmpty() -> display.hdrTypes.joinToString(", ")
    display.hdrCapabilityStatus == "unknown" -> "Unknown / not exposed"
    else -> "Unavailable"
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
private fun OpenGLESScopeOpeningAnimation(startAnimation: Boolean, onFinished: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    val contentAlpha by animateFloatAsState(
        targetValue = when { phase >= 4 -> 0f; phase >= 1 -> 1f; else -> 0f },
        animationSpec = tween(durationMillis = if (phase >= 4) 260 else 360),
        label = "openingContentAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = when { phase >= 4 -> 1.025f; phase >= 2 -> 1f; phase >= 1 -> 1.012f; else -> 0.94f },
        animationSpec = tween(durationMillis = if (phase >= 4) 280 else 520),
        label = "openingLogoScale"
    )
    val haloAlpha by animateFloatAsState(
        targetValue = when { phase >= 4 -> 0f; phase >= 3 -> 0.26f; phase >= 2 -> 0.52f; phase >= 1 -> 0.34f; else -> 0f },
        animationSpec = tween(durationMillis = 460),
        label = "openingHaloAlpha"
    )
    val lineScale by animateFloatAsState(
        targetValue = when { phase >= 2 -> 1f; phase >= 1 -> 0.34f; else -> 0f },
        animationSpec = tween(durationMillis = 520),
        label = "openingLineScale"
    )
    val lineAlpha by animateFloatAsState(
        targetValue = if (phase >= 4) 0f else if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = if (phase >= 4) 220 else 300),
        label = "openingLineAlpha"
    )
    LaunchedEffect(startAnimation) {
        if (!startAnimation) return@LaunchedEffect
        phase = 1
        delay(520L)
        phase = 2
        delay(500L)
        phase = 3
        delay(300L)
        phase = 4
        delay(280L)
        onFinished()
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ComposeColor.Black, ComposeColor(0xFF09060A), SurfaceDark, ComposeColor.Black)))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        val landscape = maxWidth > maxHeight
        val logoMaxWidth = if (landscape) 560.dp else 360.dp
        val logoWidthFraction = if (landscape) 0.52f else 0.78f
        val haloSize = if (landscape) 620.dp else 470.dp
        Box(
            Modifier
                .size(haloSize)
                .graphicsLayer { alpha = haloAlpha; scaleX = 0.92f + haloAlpha * 0.16f; scaleY = 0.92f + haloAlpha * 0.16f }
                .background(Brush.radialGradient(listOf(Brand.copy(alpha = 0.32f), BrandContainer.copy(alpha = 0.20f), ComposeColor.Transparent)))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (landscape) 18.dp else 16.dp),
            modifier = Modifier.graphicsLayer { alpha = contentAlpha; scaleX = logoScale; scaleY = logoScale }
        ) {
            Image(
                painter = painterResource(R.drawable.openglesscope_logo_horizontal),
                contentDescription = "OpenGLESScope",
                contentScale = ContentScale.Fit,
                modifier = Modifier.widthIn(max = logoMaxWidth).fillMaxWidth(logoWidthFraction).heightIn(max = if (landscape) 112.dp else 96.dp)
            )
            Box(
                Modifier
                    .width(if (landscape) 200.dp else 162.dp)
                    .height(2.dp)
                    .graphicsLayer { alpha = lineAlpha; scaleX = lineScale }
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(Brand.copy(alpha = 0.72f), BrandSoft, Brand.copy(alpha = 0.72f))))
            )
        }
    }
}

@Composable
private fun OpenGLESScopeApp(activity: MainActivity) {
    var report by remember { mutableStateOf<GlReport?>(null) }
    var collectionStatus by remember { mutableStateOf(CollectionStatus.COLLECTING) }
    var collectionElapsedMs by remember { mutableStateOf<Long?>(null) }
    var page by rememberSaveable { mutableStateOf(Page.Overview) }
    var settingsSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    var openGlesSection by rememberSaveable { mutableStateOf<OpenGlesSection?>(null) }
    var encyclopediaSeed by rememberSaveable { mutableStateOf("") }
    var display by remember { mutableStateOf(displayInfo(activity)) }
    var pageChromeBackdrop by remember { mutableStateOf<ChromeBackdropSource?>(null) }
    val chromeSourceLayer = rememberGraphicsLayer()
    val chromeBlurredLayer = rememberGraphicsLayer()
    val chromeBackdropReporter: ChromeBackdropReporter = remember {
        { layer, offset ->
            if (offset == null) {
                if (pageChromeBackdrop?.layer === layer) pageChromeBackdrop = null
            } else {
                pageChromeBackdrop = ChromeBackdropSource(layer, offset)
            }
            Unit
        }
    }
    val appDensity = LocalDensity.current
    val chromeBlurRadiusPx = with(appDensity) { 24.dp.toPx() }
    val chromeRenderEffect = remember(chromeBlurRadiusPx) {
        AndroidRenderEffect.createBlurEffect(chromeBlurRadiusPx, chromeBlurRadiusPx, Shader.TileMode.CLAMP).asComposeRenderEffect()
    }
    SideEffect { chromeBlurredLayer.renderEffect = chromeRenderEffect }
    var transientOverlayHeightPx by remember { mutableIntStateOf(0) }
    var bottomNavigationHeightPx by remember { mutableIntStateOf(0) }
    val bottomNavigationContentInset = with(appDensity) { bottomNavigationHeightPx.toDp() } + PrimaryNavigationContentGap
    val pinnedPagerHeights = remember { mutableStateMapOf<String, Int>() }
    val transientOverlayContentInset = with(appDensity) { transientOverlayHeightPx.toDp() }.let { if (it > 0.dp) it + 8.dp else 0.dp }
    val pinnedPagerHeightPx = pinnedPagerHeights.values.maxOrNull() ?: 0
    val pinnedPagerContentInset = with(appDensity) { pinnedPagerHeightPx.toDp() }.let { if (it > 0.dp) it + 4.dp else 0.dp }
        val coordinatedPinnedPagerInset by animateDpAsState(
            targetValue = pinnedPagerContentInset,
            animationSpec = tween(OverlayCoordinationMotionMillis, easing = FastOutSlowInEasing),
            label = "coordinatedPinnedPagerInset"
        )
        val stickyPagerStateReporter = remember(pinnedPagerHeights) {
            { key: String, pinned: Boolean, heightPx: Int ->
                if (pinned && heightPx > 0) pinnedPagerHeights[key] = heightPx else pinnedPagerHeights.remove(key)
                Unit
            }
        }
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
        collectionStatus = CollectionStatus.COLLECTING
        collectionElapsedMs = null
        val collectionStartedNanos = System.nanoTime()
        try {
            val raw = activity.collectOpenGlesReport()
            val parsed = withContext(Dispatchers.Default) { parseReport(raw) }
            report = parsed
            collectionStatus = if (parsed.available) CollectionStatus.COMPLETED else CollectionStatus.FAILED
        } catch (cancelled: CancellationException) {
            collectionStatus = CollectionStatus.IDLE
            throw cancelled
        } catch (error: Throwable) {
            report = parseReport(JSONObject()
                .put("status", "unavailable")
                .put("reason", error.message?.take(240)?.ifBlank { "The isolated graphics probe failed." }
                    ?: "The isolated graphics probe failed.")
                .toString())
            collectionStatus = CollectionStatus.FAILED
        } finally {
            collectionElapsedMs = ((System.nanoTime() - collectionStartedNanos) / 1_000_000L).coerceAtLeast(0L)
        }
        if (collectionStatus == CollectionStatus.COMPLETED) {
            delay(2200L)
            if (collectionStatus == CollectionStatus.COMPLETED) collectionStatus = CollectionStatus.IDLE
        }
    }
    val collecting = collectionStatus == CollectionStatus.COLLECTING
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
        typography = OpenGLESTypography,
        shapes = OpenGLESExpressiveShapes,
        motionScheme = MotionScheme.expressive()
    ) {
        CompositionLocalProvider(LocalValidatedNetwork provides activity.validatedNetworkAvailable) {
            BackHandler(enabled = page != Page.Overview || settingsSection != null || openGlesSection != null) {
                when {
                    page == Page.Settings && settingsSection != null -> settingsSection = null
                    page == Page.OpenGLES && openGlesSection != null -> openGlesSection = null
                    else -> page = Page.Overview
                }
            }
            CompositionLocalProvider(LocalChromeBackdropReporter provides chromeBackdropReporter,
                LocalTransientOverlayContentInset provides transientOverlayContentInset,
                LocalPinnedPagerContentInset provides pinnedPagerContentInset,
                LocalStickyPagerStateReporter provides stickyPagerStateReporter,
                LocalBottomNavigationContentInset provides bottomNavigationContentInset) {
            Scaffold(
                containerColor = ComposeColor.Black,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    val headerBackdrop = pageChromeBackdrop ?: ChromeBackdropSource(chromeBlurredLayer, Offset.Zero)
                    AppHeader(
                        page = page,
                        onBack = { when { page == Page.Settings && settingsSection != null -> settingsSection = null; page == Page.OpenGLES && openGlesSection != null -> openGlesSection = null; else -> page = Page.Overview } },
                        onSettings = { settingsSection = null; openGlesSection = null; page = Page.Settings },
                        backdropLayer = headerBackdrop.layer,
                        backdropRootOffset = headerBackdrop.rootOffset
                    )
                }
            ) { padding ->
                val headerContentInset = padding.calculateTopPadding()
                CompositionLocalProvider(LocalAppHeaderContentInset provides headerContentInset) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.fillMaxSize().drawWithContent {
                                chromeSourceLayer.record { this@drawWithContent.drawContent() }
                                chromeBlurredLayer.record { drawLayer(chromeSourceLayer) }
                                drawLayer(chromeSourceLayer)
                            }
                        ) {
                        val current = report
                        if (collecting) LoadingView()
                        else if (current == null) EmptyState("No OpenGL® ES™ report")
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
                                val evidenceContext = LocalContext.current
                                CompositionLocalProvider(
                                    LocalEvidenceActionEnvironment provides EvidenceActionEnvironment(
                                        openEncyclopedia = { term -> encyclopediaSeed = term.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = Page.Encyclopedia },
                                        addWatch = { token ->
                                            val clean = token.trim().take(256)
                                            if (clean.isNotBlank()) {
                                                val prefs = evidenceContext.getSharedPreferences("analysis_tools", Context.MODE_PRIVATE)
                                                val watched = prefs.getStringSet("watched", emptySet())?.toMutableSet() ?: mutableSetOf()
                                                if (watched.size < ANALYSIS_MAX_WATCHED || clean in watched) { watched += clean; prefs.edit().putStringSet("watched", watched).apply() }
                                            }
                                        }
                                    )
                                ) {
                                    PageContent(
                                        activity = activity,
                                        page = targetPage,
                                        report = current,
                                        display = display,
                                        collectionReady = collectionStatus != CollectionStatus.FAILED && !collecting && current.available,
                                        collectionElapsedMs = collectionElapsedMs,
                                        onNavigate = { target -> openGlesSection = null; page = target },
                                        settingsSection = settingsSection,
                                        onSettingsSectionChanged = { settingsSection = it },
                                        openGlesSection = openGlesSection,
                                        onOpenGlesSectionChanged = { openGlesSection = it },
                                        encyclopediaSeed = encyclopediaSeed,
                                        onEncyclopediaSeedConsumed = { encyclopediaSeed = "" }
                                    )
                                }
                            }
                        }
                        }
                        val navigationBackdrop = pageChromeBackdrop ?: ChromeBackdropSource(chromeBlurredLayer, Offset.Zero)
                        SystemNavigationBackdrop(navigationBackdrop.layer, navigationBackdrop.rootOffset, Modifier.matchParentSize().zIndex(6f))
                        CompactBottomNavigationBar(
                            selectedPage = selectedNavigationPage(page),
                            onPageSelected = { destination -> settingsSection = null; openGlesSection = null; page = destination },
                            backdropLayer = navigationBackdrop.layer,
                            backdropRootOffset = navigationBackdrop.rootOffset,
                            modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { bottomNavigationHeightPx = it.height }.zIndex(10f)
                        )
                        TransientStatusOverlayHost(
                            collectionStatus = collectionStatus,
                            collecting = collecting,
                            networkStateKnown = activity.networkStateKnown,
                            networkAvailable = activity.validatedNetworkAvailable,
                            networkBannerState = activity.networkBannerState,
                            updateStatus = activity.updateStatus,
                            onInstallUpdate = { update -> activity.updateConfirmation = update },
                            modifier = Modifier.align(Alignment.TopCenter)
                                .offset(y = headerContentInset + coordinatedPinnedPagerInset)
                                .zIndex(11f)
                                .onSizeChanged { transientOverlayHeightPx = it.height }
                        )
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
                    networkAvailable = activity.validatedNetworkAvailable,
                    onDismiss = { activity.updateConfirmation = null },
                    onConfirm = {
                        activity.updateConfirmation = null
                        activity.downloadAndInstallUpdate(update)
                    }
                )
            }
            activity.updateTransferState?.let { state ->
                UpdateTransferDialog(
                    state = state,
                    networkAvailable = activity.validatedNetworkAvailable,
                    onPause = { activity.pauseUpdateDownload() },
                    onResume = { activity.resumeUpdateDownload() },
                    onRequestCancel = { activity.requestCancelUpdateDownload() },
                    onInstall = { activity.installDownloadedUpdate() },
                    onClose = { activity.closeUpdateTransfer() }
                )
            }
            if (activity.updateCancelConfirmationVisible) {
                UpdateCancelConfirmationDialog(
                    onResume = { activity.dismissCancelUpdateDownload() },
                    onConfirmCancel = { activity.confirmCancelUpdateDownload() }
                )
            }
            }
        }
    }
}

@Composable
private fun preferExpandedTextLayout(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.fontScale >= 1.3f || configuration.screenWidthDp < 360
}

@Composable
private fun AnimatedNavigationIcon(
    page: Page,
    icon: Int,
    trigger: Int,
    size: Dp,
    tint: ComposeColor? = null
) {
    val motion = remember(page) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            motion.snapTo(0f)
            motion.animateTo(1f, animationSpec = tween(durationMillis = 320))
            motion.snapTo(0f)
        }
    }
    val wave = kotlin.math.sin(motion.value * kotlin.math.PI).toFloat()
    val iconModifier = Modifier.size(size).graphicsLayer {
        when (page) {
            Page.Overview -> {
                scaleX = 1f + 0.16f * wave
                scaleY = 1f + 0.16f * wave
            }
            Page.OpenGLES -> {
                rotationZ = -11f * wave
                scaleX = 1f + 0.08f * wave
                scaleY = 1f + 0.08f * wave
            }
            Page.EGL -> {
                translationY = -5f * wave
                rotationZ = 4f * wave
            }
            Page.Display -> {
                scaleX = 1f + 0.12f * wave
                scaleY = 1f - 0.10f * wave
                alpha = 1f - 0.12f * wave
            }
            Page.Extensions -> {
                rotationZ = 10f * wave
                scaleX = 1f + 0.09f * wave
                scaleY = 1f + 0.09f * wave
            }
            else -> {
                scaleX = 1f + 0.08f * wave
                scaleY = 1f + 0.08f * wave
            }
        }
    }
    if (tint == null) Icon(painterResource(icon), contentDescription = null, modifier = iconModifier)
    else Icon(painterResource(icon), contentDescription = null, modifier = iconModifier, tint = tint)
}

@Composable
private fun CollectionPager(totalItems: Int, currentPage: Int, onPageChange: (Int) -> Unit, pageSize: Int = COLLECTION_PAGE_SIZE) {
    val pageCount = maxOf(1, (totalItems + pageSize - 1) / pageSize)
    if (pageCount <= 1) return
    val focusManager = LocalFocusManager.current
    var pageField by remember { mutableStateOf(TextFieldValue((currentPage + 1).toString())) }
    var pageFieldFocused by remember { mutableStateOf(false) }
    var suppressFocusCommit by remember { mutableStateOf(false) }
    fun commitPageSelection() {
        val requested = pageField.text.toIntOrNull()
        val resolvedPage = when {
            requested == null -> currentPage + 1
            requested < 1 -> 1
            requested > pageCount -> pageCount
            else -> requested
        }
        pageField = TextFieldValue(resolvedPage.toString())
        if (resolvedPage - 1 != currentPage) onPageChange(resolvedPage - 1)
    }
    fun requestPageChange(targetPage: Int) {
        val bounded = targetPage.coerceIn(0, pageCount - 1)
        suppressFocusCommit = true
        pageField = TextFieldValue((bounded + 1).toString())
        focusManager.clearFocus(force = true)
        pageFieldFocused = false
        suppressFocusCommit = false
        if (bounded != currentPage) onPageChange(bounded)
    }
    LaunchedEffect(currentPage, pageCount, pageFieldFocused) {
        if (!pageFieldFocused) pageField = TextFieldValue((currentPage + 1).coerceIn(1, pageCount).toString())
    }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = SurfaceRaised.copy(alpha = 0.96f),
        contentColor = TextPrimary,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.82f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { if (currentPage > 0) requestPageChange(currentPage - 1) },
                    enabled = currentPage > 0,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = BrandContainer, contentColor = TextPrimary, disabledContainerColor = SurfaceTonal, disabledContentColor = TextMuted)
                ) { Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Previous page") }
                Box(Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                    OutlinedTextField(
                        value = pageField,
                        onValueChange = { value ->
                            val candidate = value.text
                            when {
                                candidate.isEmpty() -> pageField = value
                                candidate.all { it.isDigit() } && !(candidate.length > 1 && candidate.startsWith('0')) && (candidate.toIntOrNull() ?: 0) in 1..pageCount -> pageField = value
                            }
                        },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                            val wasFocused = pageFieldFocused
                            pageFieldFocused = focus.isFocused
                            if (focus.isFocused) pageField = pageField.copy(selection = TextRange(0, pageField.text.length))
                            else if (wasFocused && !suppressFocusCommit) {
                                if (pageField.text.isBlank()) pageField = TextFieldValue((currentPage + 1).toString())
                                commitPageSelection()
                            }
                        },
                        singleLine = true,
                        label = { Text("Page") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { commitPageSelection(); focusManager.clearFocus(force = true) }),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandSoft, unfocusedBorderColor = Outline, focusedTextColor = TextPrimary, unfocusedTextColor = ComposeColor.Transparent, cursorColor = BrandSoft)
                    )
                    if (!pageFieldFocused) {
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                            AnimatedContent(targetState = (currentPage + 1).coerceIn(1, pageCount), transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(130)) }, label = "pageNumberTransition") { animatedNumber ->
                                Text(animatedNumber.toString(), color = TextPrimary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.offset(y = 4.dp))
                            }
                        }
                    }
                }
                Text("/ $pageCount", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                IconButton(
                    onClick = { if (currentPage + 1 < pageCount) requestPageChange(currentPage + 1) },
                    enabled = currentPage + 1 < pageCount,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = BrandContainer, contentColor = TextPrimary, disabledContainerColor = SurfaceTonal, disabledContentColor = TextMuted)
                ) { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Next page") }
            }
        }
    }
}

@Composable
private fun OpenGLESScopeLazyPage(
    verticalSpacing: Dp,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val pointerWheelScalePx = rememberPointerWheelScalePx()
    val focusManager = LocalFocusManager.current
    val tvNavigationScope = rememberCoroutineScope()
    val pageFocusRequester = remember { FocusRequester() }
    LaunchedEffect(listState) { runCatching { pageFocusRequester.requestFocus() } }
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val transientOverlayContentInset = LocalTransientOverlayContentInset.current
    val pinnedPagerContentInset = LocalPinnedPagerContentInset.current
    val coordinatedTransientOverlayInset by animateDpAsState(
        targetValue = transientOverlayContentInset,
        animationSpec = tween(OverlayCoordinationMotionMillis, easing = FastOutSlowInEasing),
        label = "coordinatedTopOverlayInset"
    )
    val headerContentInset = LocalAppHeaderContentInset.current
    val bottomNavigationContentInset = LocalBottomNavigationContentInset.current
    val horizontalNavigationStartInset = navigationPadding.calculateLeftPadding(layoutDirection)
    val horizontalNavigationEndInset = navigationPadding.calculateRightPadding(layoutDirection)
    val density = LocalDensity.current
    val headerBoundaryPx = with(density) { headerContentInset.roundToPx() }
    val pageTopContentInset = headerContentInset + PageChromeSeparation
    val glassPaddingPx = with(density) { 12.dp.roundToPx() }
    val overlayCoordinationLeadPx = with(density) { OverlayCoordinationLead.roundToPx() }
    val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }
    val pagerRegistrations = remember(listState) { mutableStateMapOf<String, CollectionPagerRegistration>() }
    val pagerMeasuredHeights = remember(listState) { mutableStateMapOf<String, Int>() }
    val registrationReporter = remember(pagerRegistrations) {
        { key: String, registration: CollectionPagerRegistration? ->
            if (registration == null) pagerRegistrations.remove(key) else pagerRegistrations[key] = registration
            Unit
        }
    }
    val measuredHeightLookup = remember(pagerMeasuredHeights) {
        { key: String -> pagerMeasuredHeights[key] ?: 0 }
    }
    val measuredHeightReporter = remember(pagerMeasuredHeights) {
        { key: String, heightPx: Int ->
            if (heightPx > 0 && pagerMeasuredHeights[key] != heightPx) pagerMeasuredHeights[key] = heightPx
            Unit
        }
    }
    val activePagerVisual by remember(listState, pagerRegistrations, headerBoundaryPx, glassPaddingPx, overlayCoordinationLeadPx, verticalSpacingPx) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            pagerRegistrations.values
                .asSequence()
                .filter { it.itemIndex >= 0 && it.heightPx > 0 && it.layoutItemCount == layoutInfo.totalItemsCount }
                .mapNotNull { registration ->
                    val direct = visibleItems.firstOrNull { it.key == registration.key }
                    val previous = visibleItems.firstOrNull { it.index == registration.itemIndex - 1 }
                    val next = visibleItems.firstOrNull { it.index == registration.itemIndex + 1 }
                    val naturalOffset = when {
                        direct != null -> (direct.offset - layoutInfo.viewportStartOffset).toFloat()
                        previous != null -> (previous.offset + previous.size + verticalSpacingPx - layoutInfo.viewportStartOffset).toFloat()
                        next != null -> (next.offset - registration.heightPx - verticalSpacingPx - layoutInfo.viewportStartOffset).toFloat()
                        else -> null
                    }
                    val passedAnchor = naturalOffset == null && (
                        firstIndex > registration.itemIndex ||
                            (firstIndex == registration.itemIndex && firstOffset > 0)
                        )
                    if (naturalOffset == null && !passedAnchor) {
                        null
                    } else {
                        val resolvedNaturalOffset = naturalOffset ?: headerBoundaryPx.toFloat()
                        val transitionDistance = (registration.heightPx + glassPaddingPx).coerceAtLeast(1).toFloat()
                        val progress = if (passedAnchor && naturalOffset == null) {
                            1f
                        } else {
                            ((headerBoundaryPx + transitionDistance - resolvedNaturalOffset) / transitionDistance).coerceIn(0f, 1f)
                        }
                        val overlayOffset = resolvedNaturalOffset.coerceAtLeast(headerBoundaryPx.toFloat())
                        val pagerBottom = overlayOffset + registration.heightPx
                        val coordinationProgress = if (passedAnchor && naturalOffset == null) {
                            1f
                        } else {
                            ((headerBoundaryPx + transitionDistance + overlayCoordinationLeadPx - resolvedNaturalOffset) / overlayCoordinationLeadPx.coerceAtLeast(1)).coerceIn(0f, 1f)
                        }
                        val liveLaneExtent = (pagerBottom + glassPaddingPx - headerBoundaryPx).coerceAtLeast(0f)
                        val joinStartLaneExtent = ((registration.heightPx + glassPaddingPx) * 2).toFloat()
                        val laneExtent = when {
                            progress > 0f -> liveLaneExtent.toInt()
                            coordinationProgress > 0f -> (joinStartLaneExtent * coordinationProgress).toInt()
                            else -> 0
                        }
                        val glassHeightPx = if (progress > 0f) {
                            (headerBoundaryPx + transitionDistance * progress).toInt().coerceAtLeast(headerBoundaryPx)
                        } else {
                            headerBoundaryPx
                        }
                        CollectionPagerVisualState(registration, progress, overlayOffset, laneExtent, glassHeightPx)
                    }
                }
                .maxByOrNull { it.registration.itemIndex }
        }
    }
    val reportStickyPagerState = LocalStickyPagerStateReporter.current
    val activeReporterKey = activePagerVisual?.registration?.let { "${it.key}@${System.identityHashCode(listState)}" }
    val activeLaneExtent = activePagerVisual?.laneExtentPx ?: 0
    SideEffect {
        val reporterKey = activeReporterKey
        if (reporterKey != null) reportStickyPagerState(reporterKey, activeLaneExtent > 0, activeLaneExtent)
    }
    DisposableEffect(activeReporterKey) {
        val reporterKey = activeReporterKey
        onDispose {
            if (reporterKey != null) reportStickyPagerState(reporterKey, false, 0)
        }
    }
    val sourceLayer = rememberGraphicsLayer()
    val blurredLayer = rememberGraphicsLayer()
    val reportChromeBackdrop = LocalChromeBackdropReporter.current
    var pageRootOffset by remember { mutableStateOf(Offset.Zero) }
    val blurRadiusPx = with(density) { 24.dp.toPx() }
    val glassRenderEffect = remember(blurRadiusPx) {
        AndroidRenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP).asComposeRenderEffect()
    }
    LaunchedEffect(blurredLayer, glassRenderEffect) {
        blurredLayer.renderEffect = glassRenderEffect
    }
    LaunchedEffect(blurredLayer, pageRootOffset) {
        if (pageRootOffset.y <= 0.5f) reportChromeBackdrop(blurredLayer, pageRootOffset) else reportChromeBackdrop(blurredLayer, null)
    }
    DisposableEffect(blurredLayer) {
        onDispose { reportChromeBackdrop(blurredLayer, null) }
    }
    val pagerVisual = activePagerVisual
    val glassHeight = if (pagerVisual != null && pagerVisual.progress > 0f) with(density) { pagerVisual.glassHeightPx.toDp() } else 0.dp
    val scrollHintTopInset by animateDpAsState(
        targetValue = headerContentInset + pinnedPagerContentInset + transientOverlayContentInset + 10.dp,
        animationSpec = tween(OverlayCoordinationMotionMillis, easing = FastOutSlowInEasing),
        label = "scrollHintTopInset"
    )
    CompositionLocalProvider(
        LocalPrimaryLazyListState provides listState,
        LocalCollectionPagerRegistrationReporter provides registrationReporter,
        LocalCollectionPagerMeasuredHeightLookup provides measuredHeightLookup,
        LocalCollectionPagerMeasuredHeightReporter provides measuredHeightReporter
    ) {
        Box(
            modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val offset = coordinates.positionInRoot()
                    if (offset != pageRootOffset) pageRootOffset = offset
                }
        ) {
            LazyColumn(
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp + horizontalNavigationStartInset,
                    top = pageTopContentInset + coordinatedTransientOverlayInset,
                    end = 18.dp + horizontalNavigationEndInset,
                    bottom = bottomNavigationContentInset
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        sourceLayer.record { this@drawWithContent.drawContent() }
                        blurredLayer.record { drawLayer(sourceLayer) }
                        drawLayer(sourceLayer)
                    }
                    .desktopVerticalPointerScroll(listState, pointerWheelScalePx)
                    .focusRequester(pageFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        val direction = when (event.key.nativeKeyCode) {
                            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> FocusDirection.Down
                            AndroidKeyEvent.KEYCODE_DPAD_UP -> FocusDirection.Up
                            else -> null
                        }
                        if (direction != null) {
                            if (focusManager.moveFocus(direction)) return@onPreviewKeyEvent true
                            val info = listState.layoutInfo
                            if (info.totalItemsCount <= 0) return@onPreviewKeyEvent false
                            val target = if (direction == FocusDirection.Down) {
                                ((info.visibleItemsInfo.lastOrNull()?.index ?: listState.firstVisibleItemIndex) + 1).coerceAtMost(info.totalItemsCount - 1)
                            } else {
                                ((info.visibleItemsInfo.firstOrNull()?.index ?: listState.firstVisibleItemIndex) - 1).coerceAtLeast(0)
                            }
                            if (target == listState.firstVisibleItemIndex && !listState.canScrollBackward && direction == FocusDirection.Up) return@onPreviewKeyEvent false
                            if (target == info.totalItemsCount - 1 && !listState.canScrollForward && direction == FocusDirection.Down) return@onPreviewKeyEvent false
                            tvNavigationScope.launch {
                                listState.animateScrollToItem(target)
                                delay(24L)
                                focusManager.moveFocus(direction)
                            }
                            return@onPreviewKeyEvent true
                        }
                        when (event.key.nativeKeyCode) {
                            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> {
                                val info = listState.layoutInfo
                                if (info.totalItemsCount <= 0 || !listState.canScrollForward) false else {
                                    val target = (info.visibleItemsInfo.lastOrNull()?.index ?: listState.firstVisibleItemIndex).coerceAtMost(info.totalItemsCount - 1)
                                    tvNavigationScope.launch { listState.animateScrollToItem(target) }
                                    true
                                }
                            }
                            AndroidKeyEvent.KEYCODE_PAGE_UP -> {
                                if (!listState.canScrollBackward) false else {
                                    val target = (listState.firstVisibleItemIndex - (listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1) - 1)).coerceAtLeast(0)
                                    tvNavigationScope.launch { listState.animateScrollToItem(target) }
                                    true
                                }
                            }
                            else -> false
                        }
                    }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                userScrollEnabled = true,
                content = content
            )
            if (glassHeight > 0.dp) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(glassHeight)
                        .clipToBounds()
                        .drawWithContent {
                            val glassTop = headerBoundaryPx.toFloat().coerceIn(0f, size.height)
                            if (glassTop < size.height) {
                                clipRect(top = glassTop) {
                                    drawLayer(blurredLayer)
                                    drawRect(
                                        color = GlassTint,
                                        topLeft = Offset(0f, glassTop),
                                        size = Size(size.width, size.height - glassTop)
                                    )
                                }
                            }
                            drawContent()
                        }
                        .zIndex(7f)
                )
            }
            if (pagerVisual != null) {
                val registration = pagerVisual.registration
                Surface(
                    color = ComposeColor.Transparent,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(start = 18.dp + horizontalNavigationStartInset, end = 18.dp + horizontalNavigationEndInset)
                        .offset { IntOffset(0, pagerVisual.overlayOffsetPx.toInt()) }
                        .zIndex(9f)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .onSizeChanged { size -> if (size.height > 0 && size.height != registration.heightPx) registration.onMeasuredHeight(size.height) }
                    ) {
                        CollectionPager(
                            totalItems = registration.totalItems,
                            currentPage = registration.currentPage,
                            onPageChange = { targetPage ->
                                val current = pagerRegistrations[registration.key] ?: registration
                                pagerRegistrations[registration.key] = current.copy(currentPage = targetPage)
                                current.onPageChange(targetPage)
                            },
                            pageSize = registration.pageSize
                        )
                    }
                }
            }
            ExpressiveScrollHints(
                listState,
                Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .padding(
                        start = 12.dp + horizontalNavigationStartInset,
                        top = scrollHintTopInset,
                        end = 12.dp + horizontalNavigationEndInset,
                        bottom = bottomNavigationContentInset + 4.dp
                    )
            )
        }
    }
}

private fun LazyListScope.stickyCollectionPager(
    totalItems: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    pageSize: Int = COLLECTION_PAGE_SIZE,
    pagerKey: String = "collection-pager"
) {
    if (totalItems <= pageSize) {
        item(key = "$pagerKey-cleanup") {
            val reportRegistration = LocalCollectionPagerRegistrationReporter.current
            LaunchedEffect(pagerKey) { reportRegistration(pagerKey, null) }
        }
        return
    }
    item(key = pagerKey) {
        val listState = LocalPrimaryLazyListState.current
        val density = LocalDensity.current
        val reportRegistration = LocalCollectionPagerRegistrationReporter.current
        val measuredHeightLookup = LocalCollectionPagerMeasuredHeightLookup.current
        val reportMeasuredHeight = LocalCollectionPagerMeasuredHeightReporter.current
        val latestOnPageChange = rememberUpdatedState(onPageChange)
        val initialPagerHeightPx = with(density) { 72.dp.roundToPx() }
        val storedPagerHeightPx = measuredHeightLookup(pagerKey)
        val pagerHeightPx = if (storedPagerHeightPx > 0) storedPagerHeightPx else initialPagerHeightPx
        var pagerIndex by remember(pagerKey) { mutableIntStateOf(-1) }
        val updateMeasuredHeight = remember(pagerKey, reportMeasuredHeight) {
            { measuredHeight: Int ->
                if (measuredHeight > 0) reportMeasuredHeight(pagerKey, measuredHeight)
                Unit
            }
        }
        val pagerInfo by remember(listState, pagerKey) {
            derivedStateOf { listState?.layoutInfo?.visibleItemsInfo?.firstOrNull { it.key == pagerKey } }
        }
        val layoutItemCount by remember(listState) {
            derivedStateOf { listState?.layoutInfo?.totalItemsCount ?: 0 }
        }
        LaunchedEffect(pagerInfo?.index) {
            pagerInfo?.let { pagerIndex = it.index }
        }
        LaunchedEffect(pagerIndex, pagerHeightPx, layoutItemCount, totalItems, currentPage, pageSize) {
            if (pagerIndex >= 0 && pagerHeightPx > 0 && layoutItemCount > 0) {
                reportRegistration(
                    pagerKey,
                    CollectionPagerRegistration(
                        key = pagerKey,
                        itemIndex = pagerIndex,
                        heightPx = pagerHeightPx,
                        layoutItemCount = layoutItemCount,
                        totalItems = totalItems,
                        currentPage = currentPage,
                        pageSize = pageSize,
                        onPageChange = { targetPage -> latestOnPageChange.value(targetPage) },
                        onMeasuredHeight = updateMeasuredHeight
                    )
                )
            }
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(with(density) { pagerHeightPx.toDp() })
        )
    }
}

@Composable
private fun ScrollBoundaryIndicators(listState: LazyListState, modifier: Modifier = Modifier) {
    val showUp by remember(listState) { derivedStateOf { listState.canScrollBackward } }
    val showDown by remember(listState) { derivedStateOf { listState.canScrollForward } }
    val visibilityKey by remember(listState) {
        derivedStateOf { Triple(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, listState.layoutInfo.totalItemsCount) }
    }
    val visible = rememberScrollIndicatorVisibility(visibilityKey, showUp || showDown, listState.isScrollInProgress)
    ScrollBoundaryIndicatorColumn(showUp, showDown, visible, modifier)
}

@Composable
private fun ScrollBoundaryIndicators(gridState: LazyGridState, modifier: Modifier = Modifier) {
    val showUp by remember(gridState) { derivedStateOf { gridState.canScrollBackward } }
    val showDown by remember(gridState) { derivedStateOf { gridState.canScrollForward } }
    val visibilityKey by remember(gridState) {
        derivedStateOf { Triple(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset, gridState.layoutInfo.totalItemsCount) }
    }
    val visible = rememberScrollIndicatorVisibility(visibilityKey, showUp || showDown, gridState.isScrollInProgress)
    ScrollBoundaryIndicatorColumn(showUp, showDown, visible, modifier)
}

@Composable
private fun ScrollBoundaryIndicators(scrollState: ScrollState, modifier: Modifier = Modifier) {
    val showUp by remember(scrollState) { derivedStateOf { scrollState.value > 0 } }
    val showDown by remember(scrollState) { derivedStateOf { scrollState.value < scrollState.maxValue } }
    val visible = rememberScrollIndicatorVisibility(scrollState.value to scrollState.maxValue, showUp || showDown, scrollState.isScrollInProgress)
    ScrollBoundaryIndicatorColumn(showUp, showDown, visible, modifier)
}

@Composable
private fun ExpressiveScrollHints(listState: LazyListState, modifier: Modifier = Modifier) = ScrollBoundaryIndicators(listState, modifier)

@Composable
private fun ExpressiveScrollHints(gridState: LazyGridState, modifier: Modifier = Modifier) = ScrollBoundaryIndicators(gridState, modifier)

@Composable
private fun ExpressiveScrollHints(scrollState: ScrollState, modifier: Modifier = Modifier) = ScrollBoundaryIndicators(scrollState, modifier)

@Composable
private fun rememberScrollIndicatorVisibility(triggerKey: Any, hasScrollableDirection: Boolean, isScrollInProgress: Boolean): Boolean {
    var visible by remember { mutableStateOf(hasScrollableDirection) }
    LaunchedEffect(triggerKey, hasScrollableDirection, isScrollInProgress) {
        if (!hasScrollableDirection) {
            visible = false
        } else {
            visible = true
            delay(if (isScrollInProgress) 1050 else 900)
            visible = false
        }
    }
    return visible
}

@Composable
private fun ScrollBoundaryIndicatorColumn(showUp: Boolean, showDown: Boolean, visible: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.zIndex(10f)) {
        AnimatedVisibility(
            visible = visible && showUp,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ScrollBoundaryIndicatorBubble(up = true)
        }
        AnimatedVisibility(
            visible = visible && showDown,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ScrollBoundaryIndicatorBubble(up = false)
        }
    }
}

@Composable
private fun ScrollBoundaryIndicatorBubble(up: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = BrandContainer.copy(alpha = 0.84f),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp
    ) {
        Icon(
            painter = painterResource(if (up) R.drawable.ic_scroll_up else R.drawable.ic_scroll_down),
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.padding(8.dp).size(24.dp)
        )
    }
}
@Composable
private fun rememberPointerWheelScalePx(): Float {
    val context = LocalContext.current
    return remember(context) { android.view.ViewConfiguration.get(context).scaledVerticalScrollFactor.coerceAtLeast(1f) }
}

@Composable
private fun Modifier.desktopVerticalPointerScroll(state: ScrollableState): Modifier =
    desktopVerticalPointerScroll(state, rememberPointerWheelScalePx())

private fun Modifier.desktopVerticalPointerScroll(state: ScrollableState, wheelScalePx: Float): Modifier =
    this
        .pointerInput(state, wheelScalePx) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    if (event.type != PointerEventType.Scroll) continue
                    val change = event.changes.firstOrNull { !it.isConsumed } ?: continue
                    val axis = if (change.scrollDelta.y != 0f) change.scrollDelta.y else change.scrollDelta.x
                    if (axis == 0f) continue
                    val consumed = state.dispatchRawDelta(axis * wheelScalePx)
                    if (consumed != 0f) change.consume()
                }
            }
        }
        .pointerInput(state) {
            val slop = viewConfiguration.touchSlop
            awaitPointerEventScope {
                var activeId: androidx.compose.ui.input.pointer.PointerId? = null
                var lastY = 0f
                var accumulatedY = 0f
                var dragging = false
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val mouse = event.changes.firstOrNull { change ->
                        change.type == PointerType.Mouse && (activeId == null || change.id == activeId)
                    }
                    if (mouse == null) {
                        if (!event.buttons.isPrimaryPressed) {
                            activeId = null
                            dragging = false
                            accumulatedY = 0f
                        }
                        continue
                    }
                    if (!event.buttons.isPrimaryPressed || !mouse.pressed) {
                        activeId = null
                        dragging = false
                        accumulatedY = 0f
                        continue
                    }
                    if (activeId == null) {
                        activeId = mouse.id
                        lastY = mouse.position.y
                        accumulatedY = 0f
                        dragging = false
                        continue
                    }
                    val deltaY = mouse.position.y - lastY
                    lastY = mouse.position.y
                    if (deltaY == 0f) continue
                    if (!dragging) {
                        accumulatedY += deltaY
                        if (kotlin.math.abs(accumulatedY) <= slop) continue
                        dragging = true
                        val slopDirection = if (accumulatedY > 0f) slop else -slop
                        val overSlop = accumulatedY - slopDirection
                        if (overSlop != 0f) state.dispatchRawDelta(-overSlop)
                        mouse.consume()
                    } else {
                        state.dispatchRawDelta(-deltaY)
                        mouse.consume()
                    }
                }
            }
        }

@Composable
private fun Modifier.tvRemoteLazyListNavigation(state: LazyListState): Modifier {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    return onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        val direction = when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> FocusDirection.Down
            AndroidKeyEvent.KEYCODE_DPAD_UP -> FocusDirection.Up
            else -> null
        }
        if (direction != null) {
            if (focusManager.moveFocus(direction)) return@onPreviewKeyEvent true
            val info = state.layoutInfo
            if (info.totalItemsCount <= 0) return@onPreviewKeyEvent false
            val target = if (direction == FocusDirection.Down) {
                ((info.visibleItemsInfo.lastOrNull()?.index ?: state.firstVisibleItemIndex) + 1).coerceAtMost(info.totalItemsCount - 1)
            } else {
                ((info.visibleItemsInfo.firstOrNull()?.index ?: state.firstVisibleItemIndex) - 1).coerceAtLeast(0)
            }
            val canAdvance = if (direction == FocusDirection.Down) state.canScrollForward else state.canScrollBackward
            if (!canAdvance) return@onPreviewKeyEvent false
            scope.launch {
                state.animateScrollToItem(target)
                delay(24L)
                focusManager.moveFocus(direction)
            }
            true
        } else when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> if (!state.canScrollForward) false else {
                val target = (state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: state.firstVisibleItemIndex).coerceAtMost((state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
                scope.launch { state.animateScrollToItem(target) }
                true
            }
            AndroidKeyEvent.KEYCODE_PAGE_UP -> if (!state.canScrollBackward) false else {
                val target = (state.firstVisibleItemIndex - (state.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1) - 1)).coerceAtLeast(0)
                scope.launch { state.animateScrollToItem(target) }
                true
            }
            else -> false
        }
    }
}

@Composable
private fun Modifier.tvRemoteLazyGridNavigation(state: LazyGridState): Modifier {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    return onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        val direction = when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> FocusDirection.Down
            AndroidKeyEvent.KEYCODE_DPAD_UP -> FocusDirection.Up
            else -> null
        }
        if (direction != null) {
            if (focusManager.moveFocus(direction)) return@onPreviewKeyEvent true
            val info = state.layoutInfo
            if (info.totalItemsCount <= 0) return@onPreviewKeyEvent false
            val target = if (direction == FocusDirection.Down) {
                ((info.visibleItemsInfo.maxOfOrNull { it.index } ?: state.firstVisibleItemIndex) + 1).coerceAtMost(info.totalItemsCount - 1)
            } else {
                ((info.visibleItemsInfo.minOfOrNull { it.index } ?: state.firstVisibleItemIndex) - 1).coerceAtLeast(0)
            }
            val canAdvance = if (direction == FocusDirection.Down) state.canScrollForward else state.canScrollBackward
            if (!canAdvance) return@onPreviewKeyEvent false
            scope.launch {
                state.animateScrollToItem(target)
                delay(24L)
                focusManager.moveFocus(direction)
            }
            true
        } else when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> if (!state.canScrollForward) false else {
                val target = (state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: state.firstVisibleItemIndex).coerceAtMost((state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
                scope.launch { state.animateScrollToItem(target) }
                true
            }
            AndroidKeyEvent.KEYCODE_PAGE_UP -> if (!state.canScrollBackward) false else {
                val target = (state.firstVisibleItemIndex - state.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)).coerceAtLeast(0)
                scope.launch { state.animateScrollToItem(target) }
                true
            }
            else -> false
        }
    }
}

@Composable
private fun Modifier.dpadScrollableNavigation(state: androidx.compose.foundation.ScrollState): Modifier {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val lineStepPx = with(density) { 176.dp.roundToPx() }
    val pageStepPx = with(density) { (configuration.screenHeightDp.dp * 0.68f).roundToPx() }
    return onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        val direction = when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> FocusDirection.Down
            AndroidKeyEvent.KEYCODE_DPAD_UP -> FocusDirection.Up
            else -> null
        }
        if (direction != null) {
            if (focusManager.moveFocus(direction)) return@onPreviewKeyEvent true
            val delta = if (direction == FocusDirection.Down) lineStepPx else -lineStepPx
            val target = (state.value + delta).coerceIn(0, state.maxValue)
            if (target == state.value) return@onPreviewKeyEvent false
            scope.launch { state.animateScrollTo(target) }
            true
        } else when (event.key.nativeKeyCode) {
            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> {
                val target = (state.value + pageStepPx).coerceAtMost(state.maxValue)
                if (target == state.value) false else { scope.launch { state.animateScrollTo(target) }; true }
            }
            AndroidKeyEvent.KEYCODE_PAGE_UP -> {
                val target = (state.value - pageStepPx).coerceAtLeast(0)
                if (target == state.value) false else { scope.launch { state.animateScrollTo(target) }; true }
            }
            else -> false
        }
    }
}

private fun isOfficialApiArtwork(icon: Int): Boolean = icon == R.drawable.ic_opengles_gl_es || icon == R.drawable.openglesscope_scope_wordmark

@Composable
private fun EglBrandArtwork(modifier: Modifier = Modifier, contentDescription: String? = null, tint: ComposeColor = BrandSoft) {
    Image(
        painter = painterResource(R.drawable.ic_egl_official),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
        modifier = modifier
    )
}

private enum class EglArtworkUse { IDENTITY, CONTEXT, PBUFFER }

@Composable
private fun EglSectionArtwork(use: EglArtworkUse) {
    Box(Modifier.padding(7.dp).size(27.dp), contentAlignment = Alignment.Center) {
        EglBrandArtwork(
            modifier = if (use == EglArtworkUse.IDENTITY) Modifier.fillMaxSize() else Modifier.align(Alignment.TopStart).size(19.dp),
            tint = BrandSoft
        )
        val companion = when (use) {
            EglArtworkUse.IDENTITY -> null
            EglArtworkUse.CONTEXT -> R.drawable.ic_cpu
            EglArtworkUse.PBUFFER -> R.drawable.ic_surface
        }
        if (companion != null) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceDark,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(painterResource(companion), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.dp).size(11.dp))
            }
        }
    }
}

@Composable
private fun SemanticArtwork(icon: Int, contentDescription: String?, modifier: Modifier, tint: ComposeColor = BrandSoft) {
    when {
        icon == R.drawable.ic_egl_official -> EglBrandArtwork(modifier, contentDescription, tint)
        isOfficialApiArtwork(icon) -> Image(painter = painterResource(icon), contentDescription = contentDescription, contentScale = ContentScale.Fit, modifier = modifier)
        else -> Icon(painter = painterResource(icon), contentDescription = contentDescription, tint = tint, modifier = modifier)
    }
}

@Composable
private fun ExpressiveDestinationCard(title: String, subtitle: String, icon: Int, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Surface(
        color = ComposeColor(0xFF181516),
        shape = shape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
                SemanticArtwork(icon, null, Modifier.padding(10.dp).size(21.dp), BrandSoft)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(trademarkApiDisplayText(subtitle), style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = ComposeColor(0xFF291721), contentColor = BrandSoft)
            ) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Open ${trademarkApiDisplayText(title)}", tint = BrandSoft, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun OverviewDestinationCard(title: String, subtitle: String, destination: Page, navigate: (Page) -> Unit) {
    ExpressiveDestinationCard(title, subtitle, pageIcon(destination)) { navigate(destination) }
}

@Composable
private fun SettingsSectionCards(onSelected: (SettingsSection) -> Unit) {
    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardsVisible = true }
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsSection.entries.forEachIndexed { index, section ->
            AnimatedVisibility(
                visible = cardsVisible,
                enter = fadeIn(tween(durationMillis = 260, delayMillis = index * 45)) + slideInHorizontally(tween(durationMillis = 260, delayMillis = index * 45)) { it / 10 },
                exit = fadeOut(tween(durationMillis = 120))
            ) {
                ExpressiveDestinationCard(section.label, section.description, section.icon) { onSelected(section) }
            }
        }
    }
}

private data class NavigationItem(val page: Page, val label: String, val icon: Int)

private fun selectedNavigationPage(page: Page): Page = when (page) {
    Page.EGL -> Page.OpenGLES
    Page.Features, Page.Limits, Page.Formats, Page.Precision, Page.Configs, Page.Encyclopedia, Page.Analysis, Page.Settings -> Page.Overview
    else -> page
}

private fun navigationItems(): List<NavigationItem> = listOf(
    NavigationItem(Page.Overview, "Overview", R.drawable.ic_home),
    NavigationItem(Page.OpenGLES, "OpenGL® ES™", R.drawable.ic_opengles_gl_es),
    NavigationItem(Page.Display, "Display", R.drawable.ic_display),
    NavigationItem(Page.Extensions, "Extensions", R.drawable.ic_extensions)
)

private fun pageTransitionIndex(page: Page): Int = when (page) {
    Page.Overview -> 0
    Page.OpenGLES -> 1
    Page.Display -> 2
    Page.EGL -> 1
    Page.Extensions -> 3
    Page.Features -> 5
    Page.Limits -> 6
    Page.Formats -> 7
    Page.Precision -> 8
    Page.Configs -> 9
    Page.Encyclopedia -> 10
    Page.Analysis -> 11
    Page.Settings -> 12
}

private fun pageIcon(page: Page): Int = when (page) {
    Page.Overview -> R.drawable.ic_home
    Page.OpenGLES -> R.drawable.ic_opengles_gl_es
    Page.Display -> R.drawable.ic_tablet
    Page.EGL -> R.drawable.ic_egl_official
    Page.Features -> R.drawable.ic_features
    Page.Limits -> R.drawable.ic_properties
    Page.Formats -> R.drawable.ic_formats
    Page.Extensions -> R.drawable.ic_extensions
    Page.Precision -> R.drawable.ic_precision
    Page.Configs -> R.drawable.ic_configs
    Page.Encyclopedia -> R.drawable.ic_book
    Page.Analysis -> R.drawable.ic_analysis
    Page.Settings -> R.drawable.ic_settings
}

@Composable
private fun CompactBottomNavigationBar(
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
    backdropLayer: GraphicsLayer,
    backdropRootOffset: Offset,
    modifier: Modifier = Modifier
) {
    val items = navigationItems()
    val navigationBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val bottomSystemInset = navigationBarInsets.calculateBottomPadding()
    val startSystemInset = navigationBarInsets.calculateLeftPadding(layoutDirection)
    val endSystemInset = navigationBarInsets.calculateRightPadding(layoutDirection)
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val accessibilityScale = configuration.fontScale >= 1.3f
    val compactMaxWidth = if (landscape) 340.dp else PrimaryNavigationMaxWidth
    val compactHeight = if (accessibilityScale) (if (landscape) 62.dp else 72.dp) else if (landscape) 46.dp else PrimaryNavigationHeight
    val compactIndicatorHeight = if (accessibilityScale) compactHeight - 11.dp else if (landscape) 35.dp else PrimaryNavigationIndicatorHeight
    val compactIndicatorInset = if (landscape) 6.dp else PrimaryNavigationIndicatorHorizontalInset
    val compactIconSize = if (landscape) 18.dp else 20.dp
    val artworkSlot = if (landscape) 24.dp else 28.dp
    val glesArtworkWidth = if (landscape) 23.dp else 28.dp
    val artworkHeight = if (landscape) 13.dp else 16.dp
    val compactLabelFontSize = if (accessibilityScale) 10.sp else if (landscape) 8.sp else 9.sp
    val compactOpenGlesLabelFontSize = if (landscape) 7.5.sp else 8.5.sp
    val compactLabelLineHeight = if (accessibilityScale) 12.sp else if (landscape) 9.sp else 10.sp
    val compactItemTopPadding = if (landscape) 2.dp else 4.dp
    val compactItemBottomPadding = if (landscape) 2.dp else 3.dp
    val compactLabelSpacing = if (landscape) 1.dp else 2.dp
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp + startSystemInset, end = 16.dp + endSystemInset, bottom = PrimaryNavigationBottomGap + bottomSystemInset),
        contentAlignment = Alignment.BottomCenter
    ) {
        val targetWidth = maxWidth.coerceAtMost(compactMaxWidth)
        val navigationShape = RoundedCornerShape(999.dp)
        var navigationRootOffset by remember { mutableStateOf(Offset.Zero) }
        Surface(
            color = ComposeColor.Transparent,
            contentColor = TextPrimary,
            shape = navigationShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            shadowElevation = 10.dp,
            modifier = Modifier.width(targetWidth).height(compactHeight)
        ) {
            Box(Modifier.fillMaxSize().clip(navigationShape).onGloballyPositioned { navigationRootOffset = it.positionInRoot() }.frostedChromeBackdrop(backdropLayer, backdropRootOffset, navigationRootOffset)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                items.forEach { item ->
                    val selected = item.page == selectedPage
                    val interactionSource = remember(item.page) { MutableInteractionSource() }
                    val indicatorAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                        label = "navigationIndicatorAlpha"
                    )
                    val indicatorScale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.86f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "navigationIndicatorScale"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.94f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                        label = "navigationIconScale"
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (selected) BrandSoft else ComposeColor(0xFFE3DEE0),
                        animationSpec = tween(durationMillis = 120),
                        label = "navigationIconTint"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (selected) BrandSoft else ComposeColor(0xFFE9E3E6),
                        animationSpec = tween(durationMillis = 120),
                        label = "navigationTextTint"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(selected = selected, interactionSource = interactionSource, indication = null, role = Role.Tab, onClick = { onPageSelected(item.page) })
                            .then(tvBrowseModifier(RoundedCornerShape(999.dp))),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = compactIndicatorInset)
                                .height(compactIndicatorHeight)
                                .graphicsLayer {
                                    alpha = indicatorAlpha
                                    scaleX = indicatorScale
                                    scaleY = 0.96f + indicatorAlpha * 0.04f
                                }
                                .clip(RoundedCornerShape(999.dp))
                                .background(BrandContainer.copy(alpha = 0.82f))
                        )
                        Column(
                            modifier = Modifier.padding(top = compactItemTopPadding, bottom = compactItemBottomPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(artworkSlot).graphicsLayer { scaleX = iconScale; scaleY = iconScale },
                                contentAlignment = Alignment.Center
                            ) {
                                when (item.page) {
                                    Page.OpenGLES -> Image(
                                        painter = painterResource(R.drawable.ic_opengles_gl_es),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.width(glesArtworkWidth).height(artworkHeight)
                                    )
                                    else -> SemanticArtwork(
                                        icon = item.icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(compactIconSize)
                                    )
                                }
                            }
                            Spacer(Modifier.height(compactLabelSpacing))
                            Text(
                                item.label,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = if (accessibilityScale && !landscape) 2 else 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontSize = if (item.page == Page.OpenGLES && !accessibilityScale) compactOpenGlesLabelFontSize else compactLabelFontSize,
                                lineHeight = compactLabelLineHeight,
                                color = textColor,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 50.dp
) {
    val background = if (enabled) BrandContainer else SurfaceLow
    val content = if (enabled) BrandSoft else TextMuted
    Surface(
        shape = CircleShape,
        color = background,
        contentColor = content,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) BrandSoft.copy(alpha = 0.34f) else OutlineVariant),
        tonalElevation = 2.dp,
        shadowElevation = 5.dp,
        modifier = modifier.size(size).clip(CircleShape).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = contentDescription, tint = content, modifier = Modifier.size(if (size < 46.dp) 20.dp else 23.dp))
        }
    }
}

@Composable
private fun AnimatedHeaderActionButton(
    visible: Boolean,
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fromEnd: Boolean = false,
    buttonSize: Dp = 50.dp,
    slotSize: Dp = 50.dp
) {
    Box(modifier.size(slotSize), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(190)) + slideInHorizontally(tween(240)) { width -> if (fromEnd) width / 2 else -width / 2 } + scaleIn(tween(190), initialScale = 0.82f),
            exit = fadeOut(tween(150)) + slideOutHorizontally(tween(210)) { width -> if (fromEnd) width / 2 else -width / 2 } + scaleOut(tween(150), targetScale = 0.82f)
        ) {
            HeaderActionButton(icon, contentDescription, onClick, enabled = enabled, size = buttonSize)
        }
    }
}

@Composable
private fun HeaderTitleBadge(title: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
        Box(Modifier.width(28.dp).height(2.dp).clip(RoundedCornerShape(99.dp)).background(BrandSoft.copy(alpha = 0.88f)))
    }
}

private fun Modifier.frostedChromeBackdrop(backdropLayer: GraphicsLayer, backdropRootOffset: Offset, targetRootOffset: Offset): Modifier = drawWithContent {
    clipRect {
        withTransform({ translate(backdropRootOffset.x - targetRootOffset.x, backdropRootOffset.y - targetRootOffset.y) }) {
            drawLayer(backdropLayer)
        }
        drawRect(GlassTint)
    }
    drawContent()
}

@Composable
private fun SystemNavigationBackdrop(backdropLayer: GraphicsLayer, backdropRootOffset: Offset, modifier: Modifier = Modifier) {
    val navigationInsets = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val bottomInset = navigationInsets.calculateBottomPadding()
    val leftInset = navigationInsets.calculateLeftPadding(layoutDirection)
    val rightInset = navigationInsets.calculateRightPadding(layoutDirection)
    Box(modifier) {
        if (bottomInset > 0.dp) {
            var targetRootOffset by remember { mutableStateOf(Offset.Zero) }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(bottomInset).onGloballyPositioned { targetRootOffset = it.positionInRoot() }.frostedChromeBackdrop(backdropLayer, backdropRootOffset, targetRootOffset))
        } else {
            if (leftInset > 0.dp) {
                var targetRootOffset by remember { mutableStateOf(Offset.Zero) }
                Box(Modifier.align(if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr) Alignment.CenterStart else Alignment.CenterEnd).fillMaxHeight().width(leftInset).onGloballyPositioned { targetRootOffset = it.positionInRoot() }.frostedChromeBackdrop(backdropLayer, backdropRootOffset, targetRootOffset))
            }
            if (rightInset > 0.dp) {
                var targetRootOffset by remember { mutableStateOf(Offset.Zero) }
                Box(Modifier.align(if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr) Alignment.CenterEnd else Alignment.CenterStart).fillMaxHeight().width(rightInset).onGloballyPositioned { targetRootOffset = it.positionInRoot() }.frostedChromeBackdrop(backdropLayer, backdropRootOffset, targetRootOffset))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader(page: Page, onBack: () -> Unit, onSettings: () -> Unit, backdropLayer: GraphicsLayer, backdropRootOffset: Offset) {
    val showBack = page != Page.Overview
    val showSettings = page != Page.Settings
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val startInset = navigationPadding.calculateLeftPadding(layoutDirection)
    val endInset = navigationPadding.calculateRightPadding(layoutDirection)
    var headerRootOffset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = Modifier.fillMaxWidth().onGloballyPositioned { headerRootOffset = it.positionInRoot() }.frostedChromeBackdrop(backdropLayer, backdropRootOffset, headerRootOffset)
    ) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 14.dp + startInset, end = 14.dp + endInset, top = 8.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedHeaderActionButton(visible = showBack, icon = R.drawable.ic_back, contentDescription = "Back", onClick = onBack, buttonSize = 36.dp, slotSize = 50.dp)
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painter = painterResource(R.drawable.openglesscope_logo_horizontal_aligned),
                    contentDescription = "OpenGLESScope",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(if (LocalConfiguration.current.screenWidthDp < 350 || LocalConfiguration.current.fontScale >= 1.55f) 104.dp else 126.dp).height(30.dp)
                )
                Box(Modifier.width(1.dp).height(25.dp).background(ComposeColor.White.copy(alpha = 0.13f)))
                HeaderTitleBadge(page.title, Modifier.weight(1f))
            }
            AnimatedHeaderActionButton(visible = showSettings, icon = R.drawable.ic_settings, contentDescription = "Settings", onClick = onSettings, fromEnd = true, buttonSize = 50.dp, slotSize = 50.dp)
        }
    }
}

@Composable
private fun PageContent(
    activity: MainActivity,
    page: Page,
    report: GlReport,
    display: DisplayInfo,
    collectionReady: Boolean,
    collectionElapsedMs: Long?,
    onNavigate: (Page) -> Unit,
    settingsSection: SettingsSection?,
    onSettingsSectionChanged: (SettingsSection?) -> Unit,
    openGlesSection: OpenGlesSection?,
    onOpenGlesSectionChanged: (OpenGlesSection?) -> Unit,
    encyclopediaSeed: String,
    onEncyclopediaSeedConsumed: () -> Unit
) {
    val requiresGraphicsReport = when (page) {
        Page.OpenGLES, Page.EGL, Page.Features, Page.Limits, Page.Formats, Page.Extensions, Page.Precision, Page.Configs, Page.Analysis -> true
        else -> false
    }
    if (requiresGraphicsReport && !report.available) {
        UnavailableCapabilityPage(page.title, report.reason)
        return
    }
    when (page) {
        Page.Overview -> OverviewPage(report, display, onNavigate)
        Page.OpenGLES -> OpenGlesDestinationPage(report, openGlesSection, onOpenGlesSectionChanged)
        Page.Display -> DisplayPage(display)
        Page.EGL -> EglPage(report)
        Page.Features -> FeaturesPage(report)
        Page.Limits -> LimitsPage(report)
        Page.Formats -> FormatsPage(report)
        Page.Extensions -> ExtensionsPage(report)
        Page.Precision -> PrecisionPage(report)
        Page.Configs -> ConfigsPage(report)
        Page.Encyclopedia -> RegistryEncyclopediaPage(activity, report, encyclopediaSeed, onEncyclopediaSeedConsumed)
        Page.Analysis -> AnalysisPage(activity, report, display, collectionElapsedMs, activity.startupGateDelayMs)
        Page.Settings -> SettingsPage(activity, report, display, collectionReady, settingsSection, onSettingsSectionChanged)
    }
}

@Composable
private fun UnavailableCapabilityPage(area: String, reason: String) {
    OpenGLESScopeLazyPage(verticalSpacing = 14.dp) {
        item {
            CapabilitySectionCard("$area unavailable") {
                CapabilityStatusBadge("Unavailable", false)
                Text(reason.ifBlank { "The OpenGL® ES™/EGL™ probe did not return a complete report." }, color = ComposeColor(0xFFFF7676), style = MaterialTheme.typography.bodyMedium)
                Text("This page requires a completed runtime report. Missing or failed collection evidence is not Unsupported.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("Collection status") {
                CapabilityKeyValue("OpenGL® ES™ / EGL™", "Unavailable")
                Text("Overview, Android Display, Encyclopedia and Settings remain available. Complete-report submission and exports stay disabled.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun OverviewPage(report: GlReport, display: DisplayInfo, navigate: (Page) -> Unit) {
    val expandedTextLayout = preferExpandedTextLayout()
    OpenGLESScopeLazyPage(verticalSpacing = 14.dp) {
        item { HeroCard(report) }
        if (!report.available) {
            item {
                CapabilitySectionCard("Capability collection unavailable") {
                    CapabilityStatusBadge("Unavailable", false)
                    Text(report.reason.ifBlank { "The OpenGL® ES™/EGL™ probe did not return a complete report." }, color = ComposeColor(0xFFFF7676), style = MaterialTheme.typography.bodyMedium)
                    Text("The system graphics implementation and available Android information are shown separately from the failed inspection result.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { ExploreCard(navigate) }
        item {
            if (expandedTextLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("OpenGL® ES™", if (report.available) shortGlVersion(report.glVersion) else "Unknown", Modifier.fillMaxWidth())
                    MetricCard("EGL™", report.egl.initializedVersion, Modifier.fillMaxWidth())
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("OpenGL® ES™", if (report.available) shortGlVersion(report.glVersion) else "Unknown", Modifier.weight(1f))
                    MetricCard("EGL™", report.egl.initializedVersion, Modifier.weight(1f))
                }
            }
        }
        item {
            if (expandedTextLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("HDR", when (display.hdrCapabilityStatus) { "available" -> "${display.hdrTypes.size} types"; "unknown" -> "Unknown"; else -> "Unavailable" }, Modifier.fillMaxWidth())
                    MetricCard("Wide gamut", when (display.wideColor) { true -> "Supported"; false -> "Unsupported"; null -> "Unavailable" }, Modifier.fillMaxWidth())
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("HDR", when (display.hdrCapabilityStatus) { "available" -> "${display.hdrTypes.size} types"; "unknown" -> "Unknown"; else -> "Unavailable" }, Modifier.weight(1f))
                    MetricCard("Wide gamut", when (display.wideColor) { true -> "Supported"; false -> "Unsupported"; null -> "Unavailable" }, Modifier.weight(1f))
                }
            }
        }
        item {
            CapabilitySectionCard("Quick access") {
                val quickAccessItems = listOf(
                    "OpenGL® ES™" to Page.OpenGLES,
                    "EGL™" to Page.EGL,
                    "Display" to Page.Display,
                    "Extensions" to Page.Extensions,
                    "Limits" to Page.Limits,
                    "Formats" to Page.Formats,
                    "Features" to Page.Features,
                    "Precision" to Page.Precision
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val columns = when {
                        expandedTextLayout || maxWidth < 300.dp -> 1
                        maxWidth < 540.dp -> 2
                        maxWidth < 780.dp -> 3
                        else -> 4
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickAccessItems.chunked(columns).forEach { rowItems ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowItems.forEach { (title, destination) -> QuickAccessCard(title, destination, navigate, Modifier.weight(1f)) }
                                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
        item { OverviewDestinationCard("Encyclopedia", "Offline Khronos OpenGL® ES™ and EGL™ registry reference", Page.Encyclopedia, navigate) }
        item { OverviewDestinationCard("Analysis workspace", "Compare, search, diagnostics, requirements, Database, graph, history, integrity, watched evidence and tests", Page.Analysis, navigate) }
        if (report.available) item {
            CapabilitySectionCard("Runtime snapshot") {
                CapabilityKeyValue("Renderer", presentedRendererName(report.vendor, report.renderer))
                CapabilityKeyValue("Vendor", presentedGlVendor(report.vendor, report.renderer))
                CapabilityKeyValue("GL_VERSION", report.glVersion)
                CapabilityKeyValue("GLSL ES", report.glslVersion)
                CapabilityKeyValue("OpenGL® ES™ extensions", report.extensions.size.toString())
                CapabilityKeyValue("EGL™ extensions", (report.egl.extensions.size + report.egl.clientExtensions.size).toString())
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
    if (!report.available) {
        Surface(color = ComposeColor(0xFF181516), shape = MaterialTheme.shapes.extraLargeIncreased, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                VendorLogo("Unknown", "Unknown", Modifier.size(66.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("OpenGL® ES™ unavailable", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("The native OpenGL® ES™ / EGL™ inspection did not complete.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("System OpenGL® ES™ / EGL™ driver", color = TextPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }
    var showImplementationDetail by remember { mutableStateOf(false) }
    if (showImplementationDetail) {
        ExpressiveDetailDialog("Graphics implementation", onDismiss = { showImplementationDetail = false }) {
            DetailEvidenceRow("GL_RENDERER", report.renderer.ifBlank { "Unavailable" })
            DetailEvidenceRow("GL_VENDOR", report.vendor.ifBlank { "Unavailable" })
            DetailEvidenceRow("GL_VERSION", report.glVersion.ifBlank { "Unavailable" })
            DetailEvidenceRow("Core OpenGL® ES™ version", "${report.glMajor}.${report.glMinor}")
            DetailEvidenceRow("Core version provenance", coreVersionProvenance(report))
            DetailEvidenceRow("GL_SHADING_LANGUAGE_VERSION", report.glslVersion.ifBlank { "Unavailable" })
            DetailEvidenceRow("Implementation path", "System OpenGL® ES™ / EGL™")
            DetailEvidenceRow("GL_CONTEXT_FLAGS", runtimeQueryEvidence(report, report.glRuntime.contextFlags, "GL_CONTEXT_FLAGS"))
            DetailEvidenceRow("Reset notification strategy", runtimeQueryEvidence(report, report.glRuntime.resetNotificationStrategy, "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT"))
            DetailEvidenceRow("Reset strategy query", report.glRuntime.resetNotificationStrategyQuery ?: "Not applicable")
            DetailEvidenceRow("Robust access", runtimeQueryEvidence(report, report.glRuntime.robustAccess?.toString(), "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT"))
            DetailEvidenceRow("Robust access query", report.glRuntime.robustAccessQuery ?: "Not applicable")
            DetailEvidenceRow("Unavailable GL runtime attributes", report.glRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" })
            HorizontalDivider(color = OutlineVariant)
            DetailEvidenceRow("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })
            DetailEvidenceRow("EGL_VERSION", report.egl.version.ifBlank { "Unavailable" })
            DetailEvidenceRow("Initialized EGL version", report.egl.initializedVersion.ifBlank { "Unavailable" })
            DetailEvidenceRow("EGL_CLIENT_APIS", report.egl.clientApis.ifBlank { "Unavailable" })
            DetailEvidenceRow("Bound EGL API", report.eglRuntime.boundApi.ifBlank { "Unavailable" })
            DetailEvidenceRow("Current EGL context", report.eglRuntime.currentContext.toString())
            DetailEvidenceRow("Current EGL display", report.eglRuntime.currentDisplay.toString())
            DetailEvidenceRow("EGL config ID", report.eglRuntime.configId?.toString() ?: "Unavailable")
            DetailEvidenceRow("EGL context client", listOfNotNull(report.eglRuntime.clientType, report.eglRuntime.clientVersion?.toString()).joinToString(" ").ifBlank { "Unavailable" })
            DetailEvidenceRow("EGL surface resolution", if (report.eglRuntime.surfaceWidth != null && report.eglRuntime.surfaceHeight != null) "${report.eglRuntime.surfaceWidth} × ${report.eglRuntime.surfaceHeight}" else "Unavailable")
            DetailEvidenceRow("Unavailable EGL attributes", report.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" })
            HorizontalDivider(color = OutlineVariant)
            DetailEvidenceRow("GL extensions", report.extensions.size.toString())
            DetailEvidenceRow("EGL display extensions", report.egl.extensions.size.toString())
            DetailEvidenceRow("EGL client extensions", report.egl.clientExtensions.size.toString())
            DetailEvidenceRow("EGL configurations", report.eglConfigs.size.toString())
            DetailEvidenceRow("GL limits", report.limits.size.toString())
            DetailEvidenceRow("Compressed texture formats", report.compressedFormats.size.toString())
            DetailEvidenceRow("Internal formats queried", report.internalFormats.size.toString())
            DetailEvidenceRow("Query diagnostics", "${report.diagnostics.count { it.status == "Available" }} available / ${report.diagnostics.count { it.status == "Unavailable" }} unavailable / ${report.diagnostics.count { it.status == "Unknown" }} unknown / ${report.diagnostics.count { it.status == "Not applicable" }} N/A")
            Text("These are current GL/EGL runtime and query results. A marketing GPU name, listed extension or registry token does not establish feature support. Driver build/version is unavailable through a standardized OpenGL® ES™ query.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
    Surface(color = ComposeColor(0xFF181516), shape = MaterialTheme.shapes.extraLargeIncreased, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VendorLogo(report.vendor, report.renderer, Modifier.size(82.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(presentedRendererName(report.vendor, report.renderer).ifBlank { "OpenGL® ES™ renderer unavailable" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(presentedGlVendor(report.vendor, report.renderer).ifBlank { "Unknown vendor" }, color = ComposeColor(0xFFBDBDBD), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    reportedTranslationLayer(report.renderer)?.let { Text("$it translation layer · reported GPU model, if identified, is presentation-only · raw GL_RENDERER in Details", color = TextSecondary, style = MaterialTheme.typography.labelSmall) }
                    Text("System Driver", color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
            CapabilityKeyValue("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })
            Text("GL_VENDOR / GL_RENDERER and EGL_VENDOR are separate runtime implementation strings. Core OpenGL® ES™/EGL™ does not expose a numeric GPU vendor ID; no vendor/device ID or physical GPU model is inferred from names or artwork.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ChevronAffordance("Details", "Open graphics implementation evidence", onClick = { showImplementationDetail = true })
            }
        }
    }
}

private val gpuVendorArtworkRules = listOf(
    Regex("""^qualcomm\b""") to R.drawable.gpu_vendor_qualcomm,
    Regex("""^arm\b""") to R.drawable.gpu_vendor_arm,
    Regex("""^(?:imagination|powervr)\b""") to R.drawable.gpu_vendor_imagination,
    Regex("""^nvidia\b""") to R.drawable.gpu_vendor_nvidia,
    Regex("""^intel\b""") to R.drawable.gpu_vendor_intel,
    Regex("""^(?:amd|ati technologies)\b""") to R.drawable.gpu_vendor_amd,
    Regex("""^broadcom\b""") to R.drawable.gpu_vendor_broadcom,
    Regex("""^samsung\b""") to R.drawable.gpu_vendor_samsung,
    Regex("""^huawei\b""") to R.drawable.gpu_vendor_huawei,
    Regex("""^vivante\b""") to R.drawable.gpu_vendor_vivante
)

private data class ReportedAngleGpu(val model: String, val maker: String, val icon: Int)
private val reportedSoftwareLayer = Regex(
    """\b(?:SwiftShader|llvmpipe|software rasterizer|WARP|Microsoft Basic Render Driver)\b""",
    RegexOption.IGNORE_CASE
)
private val reportedGpuFamilies = listOf(
    Triple(Regex("""\bAdreno\s*(?:\(TM\)|[®™])?\s*\d{2,4}[A-Za-z0-9+._-]*\b""", RegexOption.IGNORE_CASE), "Qualcomm", R.drawable.gpu_vendor_qualcomm),
    Triple(Regex("""\b(?:Mali[- ]?[GT]\d{2,4}|Immortalis[- ]?G\d{2,4})(?:[- ]?MP\d+)?\b""", RegexOption.IGNORE_CASE), "Arm", R.drawable.gpu_vendor_arm),
    Triple(Regex("""\b(?:PowerVR|Power VR)\s+(?:(?:Rogue|SGX|GE|GT|GX|GM|Series)\s*[- ]?){0,2}[A-Za-z]*\d+[A-Za-z0-9._-]*\b""", RegexOption.IGNORE_CASE), "Imagination Technologies", R.drawable.gpu_vendor_imagination),
    Triple(Regex("""\bXclipse\s*\d{2,4}\b""", RegexOption.IGNORE_CASE), "Samsung", R.drawable.gpu_vendor_samsung),
    Triple(Regex("""\b(?:NVIDIA\s+)?(?:GeForce|Quadro|Tesla|Tegra)\s+(?:(?:RTX|GTX|GT|MX)\s*)?[A-Za-z]?\d{1,4}(?:\s+(?:Ti|SUPER|Mobile))?(?:\s+Laptop\s+GPU)?\b""", RegexOption.IGNORE_CASE), "NVIDIA", R.drawable.gpu_vendor_nvidia),
    Triple(Regex("""\b(?:AMD\s+)?Radeon\s*(?:\(TM\)|[®™])?\s*(?:(?:RX|PRO|HD|Vega|R\d)\s*)?[A-Za-z]?\d{2,4}(?:\s*(?:XT|M|XTX))?\b""", RegexOption.IGNORE_CASE), "AMD", R.drawable.gpu_vendor_amd),
    Triple(Regex("""\b(?:Intel(?:\(R\)|®)?\s+)?(?:Arc\s+[AB]?\d{2,4}|(?:UHD|HD|Iris(?:\s+Xe)?)\s+Graphics(?:\s+\d{3,4})?)\b""", RegexOption.IGNORE_CASE), "Intel", R.drawable.gpu_vendor_intel),
    Triple(Regex("""\bVideoCore\s+(?:IV|VI|VII|V3D|\d+)\b""", RegexOption.IGNORE_CASE), "Broadcom", R.drawable.gpu_vendor_broadcom),
    Triple(Regex("""\bVivante\s+GC\d{3,5}\b""", RegexOption.IGNORE_CASE), "Vivante", R.drawable.gpu_vendor_vivante),
    Triple(Regex("""\bMaleoon\s*\d{2,4}\b""", RegexOption.IGNORE_CASE), "Huawei", R.drawable.gpu_vendor_huawei),
    Triple(Regex("""\bVeriSilicon\s+(?:VIP|GC)\d{3,5}\b""", RegexOption.IGNORE_CASE), "VeriSilicon", R.drawable.gpu_vendor_unknown),
    Triple(Regex("""\bApple\s+[MA]\d{1,3}(?:\s+(?:Pro|Max|Ultra))?\b""", RegexOption.IGNORE_CASE), "Apple", R.drawable.gpu_vendor_unknown)
)
private val anglePrefix = Regex("""^ANGLE\s*\(""", RegexOption.IGNORE_CASE)
private val emulatorTranslatorPrefix = Regex("""^Android Emulator OpenGL ES Translator\s*\(""", RegexOption.IGNORE_CASE)
private fun reportedTranslationLayer(renderer: String): String? {
    val raw = renderer.trim()
    return when {
        anglePrefix.containsMatchIn(raw) -> "ANGLE"
        emulatorTranslatorPrefix.containsMatchIn(raw) -> "Android Emulator OpenGL ES Translator"
        else -> null
    }
}
private fun reportedGpuModel(renderer: String, translationOnly: Boolean = true): ReportedAngleGpu? {
    if (translationOnly && reportedTranslationLayer(renderer) == null) return null
    if (reportedSoftwareLayer.containsMatchIn(renderer)) return null
    val candidates = reportedGpuFamilies.flatMap { (pattern, maker, icon) ->
        pattern.findAll(renderer).map { ReportedAngleGpu(it.value.trim(), maker, icon) }.toList()
    }
    return candidates.singleOrNull()
}
private fun presentedRendererName(vendor: String, renderer: String): String =
    reportedGpuModel(renderer)?.model ?: renderer

private fun presentedGlVendor(vendor: String, renderer: String): String {
    val declared = vendor.trim().replace(Regex("""^Google Inc\.(?=\s|$)""", RegexOption.IGNORE_CASE), "Google LLC")
    val gpu = reportedGpuModel(renderer)
    return if (gpu != null && !declared.contains(gpu.maker, ignoreCase = true)) "$declared (${gpu.maker})" else declared
}

private fun vendorArtworkResource(vendor: String, renderer: String): Int {
    if (reportedSoftwareLayer.containsMatchIn("$vendor $renderer")) return R.drawable.gpu_vendor_unknown
    if (reportedTranslationLayer(renderer) != null) {
        return reportedGpuModel(renderer)?.icon ?: R.drawable.gpu_vendor_unknown
    }
    val declared = vendor.trim().lowercase(java.util.Locale.ROOT)
    val implementationLayers = listOf("angle", "swiftshader", "mesa", "freedreno", "panfrost", "zink", "llvmpipe", "virgl", "software rasterizer")
    val path = renderer.trim().lowercase(java.util.Locale.ROOT)
    if (implementationLayers.any { it in declared || it in path } || declared.isBlank() || declared == "unknown" || declared == "unavailable") return R.drawable.gpu_vendor_unknown
    return gpuVendorArtworkRules.firstOrNull { (pattern, _) -> pattern.containsMatchIn(declared) }?.second
        ?: R.drawable.gpu_vendor_unknown
}

@Composable
private fun DatabaseReportVendorBadge(vendor: String, renderer: String) {
    Surface(
        shape = RoundedCornerShape(18.dp), color = BrandContainer, contentColor = BrandSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.28f)),
        modifier = Modifier.size(50.dp)
    ) {
        Image(
            painter = painterResource(vendorArtworkResource(vendor, renderer)),
            contentDescription = "Reported graphics vendor artwork",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(7.dp)
        )
    }
}

@Composable
private fun VendorLogo(vendor: String, renderer: String, modifier: Modifier = Modifier) {
    val icon = vendorArtworkResource(vendor, renderer)
    val artworkDescription = when (icon) {
        R.drawable.gpu_vendor_qualcomm -> "Qualcomm GL implementation vendor artwork"
        R.drawable.gpu_vendor_arm -> "Arm GL implementation vendor artwork"
        R.drawable.gpu_vendor_imagination -> "Imagination GL implementation vendor artwork"
        R.drawable.gpu_vendor_nvidia -> "NVIDIA GL implementation vendor artwork"
        R.drawable.gpu_vendor_intel -> "Intel GL implementation vendor artwork"
        R.drawable.gpu_vendor_amd -> "AMD GL implementation vendor artwork"
        R.drawable.gpu_vendor_broadcom -> "Broadcom GL implementation vendor artwork"
        R.drawable.gpu_vendor_samsung -> "Samsung GL implementation vendor artwork"
        R.drawable.gpu_vendor_huawei -> "Huawei GL implementation vendor artwork"
        R.drawable.gpu_vendor_vivante -> "Vivante GL implementation vendor artwork"
        else -> "Unverified graphics implementation artwork"
    }
    Card(colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF111111)), shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Image(
            painter = painterResource(icon),
            contentDescription = artworkDescription,
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun QuickAccessCard(title: String, destination: Page, navigate: (Page) -> Unit, modifier: Modifier) {
    val shape = MaterialTheme.shapes.medium
    Card(
        onClick = { navigate(destination) },
        colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF1A1718)),
        shape = shape,
        modifier = modifier.heightIn(min = 72.dp).then(tvBrowseModifier(shape))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            SemanticArtwork(pageIcon(destination), null, Modifier.size(if (destination == Page.OpenGLES || destination == Page.EGL) 22.dp else 19.dp), BrandSoft)
            Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ExploreCard(onNavigate: (Page) -> Unit) {
    val expandedTextLayout = preferExpandedTextLayout()
    val pages = listOf(Page.Features, Page.Limits, Page.Formats, Page.Precision, Page.Configs)
    CapabilitySectionCard("Explore") {
        Text("Detailed OpenGL® ES™ and EGL™ inspection areas", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = when {
                expandedTextLayout || maxWidth < 300.dp -> 1
                maxWidth < 620.dp -> 2
                else -> 3
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.chunked(columns).forEach { rowPages ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        rowPages.forEach { page -> ExploreDestinationTile(page, onNavigate, Modifier.weight(1f)) }
                        repeat(columns - rowPages.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreDestinationTile(page: Page, onNavigate: (Page) -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Card(
        onClick = { onNavigate(page) },
        colors = CardDefaults.cardColors(containerColor = SurfaceTonal),
        shape = shape,
        modifier = modifier.heightIn(min = 52.dp).then(tvBrowseModifier(shape))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = BrandContainer) {
                SemanticArtwork(pageIcon(page), null, Modifier.padding(7.dp).size(18.dp), BrandSoft)
            }
            Text(page.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OpenGlesSectionCards(onSelected: (OpenGlesSection) -> Unit) {
    var cardsVisible by remember { mutableStateOf(false) }
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val topOverlayInset = LocalTransientOverlayContentInset.current
    val coordinatedTopOverlayInset by animateDpAsState(targetValue = topOverlayInset, animationSpec = tween(220, easing = FastOutSlowInEasing), label = "openGlesTopOverlayInset")
    val headerContentInset = LocalAppHeaderContentInset.current
    val pageTopInset = headerContentInset + PageChromeSeparation
    val bottomNavigationInset = LocalBottomNavigationContentInset.current
    val startInset = navigationPadding.calculateLeftPadding(layoutDirection)
    val endInset = navigationPadding.calculateRightPadding(layoutDirection)
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { cardsVisible = true }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).focusable().focusGroup().padding(
                start = 18.dp + startInset,
                top = pageTopInset + coordinatedTopOverlayInset,
                end = 18.dp + endInset,
                bottom = bottomNavigationInset
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OpenGlesSection.entries.forEachIndexed { index, section ->
                AnimatedVisibility(
                    visible = cardsVisible,
                    enter = fadeIn(tween(durationMillis = 260, delayMillis = index * 45)) + slideInHorizontally(tween(durationMillis = 260, delayMillis = index * 45)) { it / 10 },
                    exit = fadeOut(tween(durationMillis = 120))
                ) { ExpressiveDestinationCard(section.label, section.description, section.icon) { onSelected(section) } }
            }
        }
        ExpressiveScrollHints(
            scrollState,
            Modifier.fillMaxSize().padding(start = 12.dp + startInset, top = headerContentInset + 8.dp + coordinatedTopOverlayInset, end = 12.dp + endInset, bottom = bottomNavigationInset + 4.dp)
        )
    }
}

@Composable
private fun OpenGlesDestinationPage(report: GlReport, selectedSection: OpenGlesSection?, onSectionSelected: (OpenGlesSection?) -> Unit) {
    AnimatedContent(targetState = selectedSection, transitionSpec = {
        if (targetState != null) slideInHorizontally(tween(240)) { it / 7 } + fadeIn(tween(220)) togetherWith slideOutHorizontally(tween(180)) { -it / 9 } + fadeOut(tween(160))
        else slideInHorizontally(tween(240)) { -it / 7 } + fadeIn(tween(220)) togetherWith slideOutHorizontally(tween(180)) { it / 9 } + fadeOut(tween(160))
    }, label = "openGlesSectionTransition") { section ->
        when (section) {
            null -> OpenGlesSectionCards { onSectionSelected(it) }
            OpenGlesSection.RUNTIME -> OpenGLESPage(report)
            OpenGlesSection.EGL -> EglPage(report)
        }
    }
}

@Composable
private fun OpenGLESPage(r: GlReport) = CapabilityListPage("OpenGL® ES™ runtime", listOf(
    "Driver mode" to "System OpenGL® ES™/EGL™",
    "Driver version" to "Unavailable (OpenGL® ES™ does not expose a standardized driver-version query)",
    "Renderer" to r.renderer,
    "Vendor" to r.vendor,
    "GL_VERSION" to r.glVersion,
    "Core version" to "${r.glMajor}.${r.glMinor}",
    "Core version provenance" to coreVersionProvenance(r),
    "GL_SHADING_LANGUAGE_VERSION" to r.glslVersion,
    "GL_CONTEXT_FLAGS" to runtimeQueryEvidence(r, r.glRuntime.contextFlags, "GL_CONTEXT_FLAGS"),
    "Reset notification strategy" to runtimeQueryEvidence(r, r.glRuntime.resetNotificationStrategy, "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT"),
    "Reset strategy query" to (r.glRuntime.resetNotificationStrategyQuery ?: "Not applicable"),
    "Robust access" to runtimeQueryEvidence(r, r.glRuntime.robustAccess?.toString(), "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT"),
    "Robust access query" to (r.glRuntime.robustAccessQuery ?: "Not applicable"),
    "Unavailable GL runtime attributes" to r.glRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }
))

@Composable
private fun DisplayPage(d: DisplayInfo) {
    OpenGLESScopeLazyPage(verticalSpacing = 14.dp) {
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
                    CapabilityKeyValue("HDR types", when (d.hdrCapabilityStatus) {
                        "available" -> "None reported"
                        "unknown" -> "Unknown / not exposed"
                        else -> "Unavailable"
                    })
                } else {
                    Text("The logos below represent HDR types reported by Android for the current display mode. HLG and HLG+ use text because no official logos are included in the locked assets.",
                        color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    HdrCapabilitiesCarousel(d.hdrTypes)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ComposeColor(0xFF303030))
                CapabilityKeyValue("HDR capability status", d.hdrCapabilityStatus)
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
                Text("Android display and HDR evidence is reported separately from OpenGL® ES™ and EGL™ capability data.", color = ComposeColor(0xFF9E9E9E), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HdrCapabilitiesCarousel(types: List<String>) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val canMoveLeft = scrollState.value > 0
    val canMoveRight = scrollState.value < scrollState.maxValue
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            types.forEach { HdrTypeCard(it) }
        }
        AnimatedVisibility(visible = canMoveLeft, modifier = Modifier.align(Alignment.CenterStart), enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value - 360).coerceAtLeast(0)) } },
                colors = IconButtonDefaults.iconButtonColors(containerColor = BrandContainer, contentColor = TextPrimary), modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Scroll HDR capabilities left", modifier = Modifier.size(23.dp))
            }
        }
        AnimatedVisibility(visible = canMoveRight, modifier = Modifier.align(Alignment.CenterEnd), enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value + 360).coerceAtMost(scrollState.maxValue)) } },
                colors = IconButtonDefaults.iconButtonColors(containerColor = BrandContainer, contentColor = TextPrimary), modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Scroll HDR capabilities right", modifier = Modifier.size(23.dp))
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
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
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
                CapabilityKeyValue("Current config ID", runtimeQueryEvidence(r, r.eglRuntime.configId?.toString(), "EGL_CONFIG_ID/context"))
                CapabilityKeyValue("Context client type", runtimeQueryEvidence(r, r.eglRuntime.clientType, "EGL_CONTEXT_CLIENT_TYPE"))
                CapabilityKeyValue("Context client version", runtimeQueryEvidence(r, r.eglRuntime.clientVersion?.toString(), "EGL_CONTEXT_CLIENT_VERSION"))
                CapabilityKeyValue("Context render buffer", runtimeQueryEvidence(r, r.eglRuntime.renderBuffer, "EGL_RENDER_BUFFER/context"))
                CapabilityKeyValue("Current context", if (r.eglRuntime.currentContext) "Available" else "Unavailable")
                CapabilityKeyValue("Current display", if (r.eglRuntime.currentDisplay) "Available" else "Unavailable")
                CapabilityKeyValue("Current draw surface", if (r.eglRuntime.currentDrawSurface) "Available" else "Unavailable")
                CapabilityKeyValue("Current read surface", if (r.eglRuntime.currentReadSurface) "Available" else "Unavailable")
            }
        }
        item {
            CapabilitySectionCard("Collector pbuffer") {
                CapabilityKeyValue("Surface config ID", runtimeQueryEvidence(r, r.eglRuntime.surfaceConfigId?.toString(), "EGL_CONFIG_ID/surface"))
                CapabilityKeyValue("Size", eglRuntimeSizeEvidence(r))
                CapabilityKeyValue("GL colorspace", runtimeQueryEvidence(r, r.eglRuntime.surfaceGlColorspace, "EGL_GL_COLORSPACE"))
                CapabilityKeyValue("GL colorspace query", r.eglRuntime.surfaceGlColorspaceQuery ?: "Not applicable")
                CapabilityKeyValue("VG alpha format", runtimeQueryEvidence(r, r.eglRuntime.surfaceVgAlphaFormat, "EGL_VG_ALPHA_FORMAT"))
                CapabilityKeyValue("VG alpha query", r.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable")
                CapabilityKeyValue("VG colorspace", runtimeQueryEvidence(r, r.eglRuntime.surfaceVgColorspace, "EGL_VG_COLORSPACE"))
                CapabilityKeyValue("VG colorspace query", r.eglRuntime.surfaceVgColorspaceQuery ?: "Not applicable")
                CapabilityKeyValue("Horizontal resolution", runtimeQueryEvidence(r, r.eglRuntime.surfaceHorizontalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_HORIZONTAL_RESOLUTION"))
                CapabilityKeyValue("Vertical resolution", runtimeQueryEvidence(r, r.eglRuntime.surfaceVerticalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_VERTICAL_RESOLUTION"))
                CapabilityKeyValue("Pixel aspect ratio", runtimeQueryEvidence(r, r.eglRuntime.surfacePixelAspectRatio?.let { eglScaledSurfaceEvidence(it, aspect = true) }, "EGL_PIXEL_ASPECT_RATIO"))
                CapabilityKeyValue("Largest pbuffer", runtimeQueryEvidence(r, r.eglRuntime.surfaceLargestPbuffer?.let { eglBooleanEvidence(it) }, "EGL_LARGEST_PBUFFER"))
                CapabilityKeyValue("Render buffer", runtimeQueryEvidence(r, r.eglRuntime.surfaceRenderBuffer, "EGL_RENDER_BUFFER/surface"))
                CapabilityKeyValue("Swap behavior", runtimeQueryEvidence(r, r.eglRuntime.surfaceSwapBehavior, "EGL_SWAP_BEHAVIOR"))
                CapabilityKeyValue("Texture format", runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureFormat, "EGL_TEXTURE_FORMAT"))
                CapabilityKeyValue("Texture target", runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureTarget, "EGL_TEXTURE_TARGET"))
                CapabilityKeyValue("Mipmap texture", runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapTexture?.let { eglBooleanEvidence(it) }, "EGL_MIPMAP_TEXTURE"))
                CapabilityKeyValue("Mipmap level", runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapLevel?.toString(), "EGL_MIPMAP_LEVEL"))
                CapabilityKeyValue("Multisample resolve", runtimeQueryEvidence(r, r.eglRuntime.surfaceMultisampleResolve, "EGL_MULTISAMPLE_RESOLVE"))
            }
        }
        item {
            CapabilitySectionCard("EGL capability queries") {
                CapabilityKeyValue("Capability records", r.eglCapabilities.size.toString())
                Text("Extension strings decide whether an extension-defined query is applicable; each value below is runtime query evidence, not an inference from registry presence.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(r.eglCapabilities, key = { it.name }) { cap ->
            CapabilityItemCard {
                CapabilityKeyValue(cap.name, cap.status)
                if (cap.value.isNotBlank()) CapabilityKeyValue("Value", cap.value)
                if (cap.detail.isNotBlank()) CapabilityKeyValue("Evidence", cap.detail)
            }
        }
        item {
            CapabilitySectionCard("EGL runtime query failures") {
                CapabilityKeyValue("Unavailable attributes", r.eglRuntime.unavailableAttributes.size.toString())
                if (r.eglRuntime.unavailableAttributes.isEmpty()) {
                    Text("No explicit EGL™ runtime attribute failure was recorded.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                } else {
                    r.eglRuntime.unavailableAttributes.forEach { CapabilityKeyValue(it.name, it.error) }
                }
            }
        }
    }
}

@Composable
private fun FeaturesPage(r: GlReport) {
    var query by rememberSaveable { mutableStateOf("") }
    var stateFilter by rememberSaveable { mutableStateOf("All") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val version = r.glMajor * 100 + r.glMinor * 10
    val coreState: (Int) -> EvidenceState = { required ->
        if (version <= 0) EvidenceState.Unknown else if (version >= required) EvidenceState.Supported else EvidenceState.Unsupported
    }
    val glEnumerationAvailable = r.diagnostics.firstOrNull { it.name == "GL_EXTENSIONS" }?.status.equals("Available", true)
    val eglDisplayEnumerationAvailable = r.diagnostics.firstOrNull { it.name == "EGL_EXTENSIONS" }?.status.equals("Available", true)
    val eglClientEnumerationAvailable = r.diagnostics.firstOrNull { it.name == "EGL_NO_DISPLAY/EGL_EXTENSIONS" }?.status.equals("Available", true)
    val allEglExtensions = remember(r.egl.extensions, r.egl.clientExtensions) { (r.egl.extensions + r.egl.clientExtensions).toSet() }
    val rows = remember(r, version) {
        buildList {
            listOf(
                Triple("OpenGL ES 2.0 core", 200, "GL_VERSION"),
                Triple("OpenGL ES 3.0 core", 300, "GL_VERSION"),
                Triple("OpenGL ES 3.1 core", 310, "GL_VERSION"),
                Triple("OpenGL ES 3.2 core", 320, "GL_VERSION")
            ).forEach { (name, required, source) ->
                add(FeatureEvidenceRow(name, coreState(required), source, "Runtime core ${r.glMajor}.${r.glMinor}; required ${required / 100}.${(required % 100) / 10}"))
            }
            QUERY_DEPENDENCIES.keys.sorted().forEach { extension ->
                val state = when {
                    extension.startsWith("GL_") && extension in r.extensions -> EvidenceState.Supported
                    extension.startsWith("GL_") && glEnumerationAvailable -> EvidenceState.Unsupported
                    extension.startsWith("GL_") -> EvidenceState.Unknown
                    extension.startsWith("EGL_") && extension in allEglExtensions -> EvidenceState.Supported
                    extension.startsWith("EGL_") && eglDisplayEnumerationAvailable && eglClientEnumerationAvailable -> EvidenceState.Unsupported
                    extension.startsWith("EGL_") -> EvidenceState.Unknown
                    else -> EvidenceState.Unknown
                }
                val queryRows = QUERY_DEPENDENCIES[extension].orEmpty()
                val querySummary = queryRows.mapNotNull { name -> r.diagnostics.firstOrNull { it.name == name }?.let { "$name=${it.status}" } }.joinToString(" · ")
                val source = if (extension.startsWith("GL_")) "GL_EXTENSIONS + query gates" else "EGL display/client extension enumeration + query gates"
                val detail = buildString {
                    append(if (state == EvidenceState.Supported) "Exact runtime token enumerated" else if (state == EvidenceState.Unsupported) "Exact token not enumerated by complete relevant extension evidence" else "Relevant extension enumeration is incomplete/unavailable")
                    if (querySummary.isNotBlank()) append(" · ").append(querySummary)
                }
                add(FeatureEvidenceRow(extension, state, source, detail))
            }
        }
    }
    val filtered = remember(rows, query, stateFilter) {
        rows.filter { row ->
            (stateFilter == "All" || row.state.name.equals(stateFilter, true)) &&
                (query.isBlank() || row.name.contains(query, true) || row.source.contains(query, true) || row.detail.contains(query, true))
        }
    }
    val pageCount = maxOf(1, (filtered.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(query, stateFilter) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visible = remember(filtered, page) { filtered.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item { CapabilitySectionCard("Feature evidence") {
            Text("This screen is exhaustive for OpenGLESScope's query-gated feature catalog, not for every concept in the OpenGL® ES™ specification. Core milestones come only from GL_VERSION; extension-backed rows require exact runtime tokens and preserve Unknown when enumeration evidence is incomplete.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            ExpressiveSearchField(value = query, onValueChange = { query = it.take(256) }, placeholderText = "Search feature evidence…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            val states = listOf("All", "Supported", "Unsupported", "Unknown")
            ExpressiveFilterBar(states, states.indexOf(stateFilter).coerceAtLeast(0)) { stateFilter = states[it] }
        } }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Evidence rows" to filtered.size.toString(),
                "Supported" to filtered.count { it.state == EvidenceState.Supported }.toString(),
                "Unsupported" to filtered.count { it.state == EvidenceState.Unsupported }.toString(),
                "Unknown" to filtered.count { it.state == EvidenceState.Unknown }.toString(),
                "Catalog rows" to rows.size.toString(),
                "Showing" to if (filtered.isEmpty()) "0" else "${first + 1}-${first + visible.size}"
            ))
        }
        stickyCollectionPager(filtered.size, page, { page = it })
        items(visible, key = { it.name }) { row -> CapabilityItemCard {
            CapabilityKeyValue(row.name, row.state.name)
            CapabilityKeyValue("Evidence source", row.source)
            CapabilityKeyValue("Evidence detail", row.detail)
        } }
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
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val rows = if (mode == 0) r.limits.map { it.name to it.value } else r.diagnostics.map { it.name to if (it.detail.isBlank()) it.status else "${it.status} · ${it.detail}" }
    val diagnosticByName = remember(r.diagnostics) { r.diagnostics.associateBy { it.name } }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    val pageCount = maxOf(1, (filtered.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(mode, query) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visible = remember(filtered, page) { filtered.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard(if (mode == 0) "OpenGL® ES™ limits" else "Query diagnostics") {
                ExpressiveFilterBar(listOf("Limits", "Diagnostics"), mode) { mode = it }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = if (mode == 0) "Search limits…" else "Search diagnostics…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                if (mode == 0) Text("Implementation limits and query diagnostics are counted separately so diagnostic evidence never inflates the implementation-limit total.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            val metrics = if (mode == 0) listOf(
                "Limits" to filtered.size.toString(),
                "Available queries" to filtered.count { (name, _) -> diagnosticByName[name]?.status.equals("Available", true) }.toString()
            ) else listOf(
                "Evidence rows" to filtered.size.toString(),
                "Available" to filtered.count { (name, _) -> r.diagnostics.any { it.name == name && it.status.equals("Available", true) } }.toString(),
                "Unavailable" to filtered.count { (name, _) -> r.diagnostics.any { it.name == name && it.status.equals("Unavailable", true) } }.toString(),
                "Not applicable" to filtered.count { (name, _) -> r.diagnostics.any { it.name == name && it.status.equals("Not applicable", true) } }.toString(),
                "Unknown" to filtered.count { (name, _) -> r.diagnostics.any { it.name == name && it.status.equals("Unknown", true) } }.toString()
            )
            ExpressiveMetricGrid(metrics + listOf("Showing" to if (filtered.isEmpty()) "0" else "${first + 1}-${first + visible.size}"))
        }
        stickyCollectionPager(filtered.size, page, { page = it })
        items(visible, key = { "${mode}|${it.first}" }) { (name, value) -> CapabilityItemCard {
            CapabilityKeyValue(name, value)
            if (mode == 0) diagnosticByName[name]?.let { diagnostic ->
                CapabilityKeyValue("Query evidence", diagnostic.status)
                if (diagnostic.detail.isNotBlank()) CapabilityKeyValue("Diagnostic", diagnostic.detail)
            }
        } }
    }
}

@Composable
private fun FormatsPage(r: GlReport) {
    var mode by rememberSaveable { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val category = when (mode) { 0 -> "Compressed texture format"; 1 -> "Shader binary format"; 2 -> "Program binary format"; else -> "Internal-format sample support" }
    val queryName = when (mode) { 0 -> "GL_COMPRESSED_TEXTURE_FORMATS"; 1 -> "GL_SHADER_BINARY_FORMATS"; 2 -> "GL_PROGRAM_BINARY_FORMATS"; else -> "glGetInternalformativ(GL_NUM_SAMPLE_COUNTS / GL_SAMPLES)" }
    val rows = when (mode) {
        0 -> r.compressedFormats.mapIndexed { i, v -> "Compressed format ${i + 1}" to v }
        1 -> r.shaderBinaryFormats.mapIndexed { i, v -> "Shader binary ${i + 1}" to v }
        2 -> r.programBinaryFormats.mapIndexed { i, v -> "Program binary ${i + 1}" to v }
        else -> r.internalFormats.map { f ->
            val samples = f.sampleCounts.joinToString(", ").ifBlank { "none reported" }
            val nv = f.nvSampleProperties.joinToString("; ") { n -> "${n.samples}x→MSAA ${n.multisamples}, scale ${n.supersampleScaleX}×${n.supersampleScaleY}, conformant=${n.conformant}" }
            "${f.target} · ${f.internalFormat}" to buildString {
                append("${f.status} · samples: $samples")
                if (nv.isNotBlank()) append(" · NV: $nv")
                if (f.detail.isNotBlank()) append(" · ${f.detail}")
            }
        }
    }
    val diagnostic = if (mode < 3) r.diagnostics.firstOrNull { it.name == queryName } else null
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    val pageCount = maxOf(1, (filtered.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(mode, query) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visible = remember(filtered, page) { filtered.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard("Formats") {
                ExpressiveFilterBar(listOf("Texture", "Shader binary", "Program binary", "Internal samples"), mode) { mode = it }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search formats…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                CapabilityKeyValue("Enumeration query", queryName)
                if (mode < 3) {
                    CapabilityKeyValue("Query evidence", diagnostic?.status ?: "Unknown")
                    if (!diagnostic?.detail.isNullOrBlank()) CapabilityKeyValue("Query diagnostic", diagnostic!!.detail)
                } else {
                    CapabilityKeyValue("Evidence", "Per target + sized renderable internal format")
                    Text("OpenGL® ES™ 3.0+ sample-count support is queried from runtime evidence. NV per-sample multisample, supersample-scale and conformance evidence is added only when GL_NV_internalformat_sample_query is advertised and callable.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
                Text("Select an entry for canonical/raw evidence details.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Matching formats" to filtered.size.toString(),
                "Showing" to if (filtered.isEmpty()) "0" else "${first + 1}-${first + visible.size}",
                "Reported in group" to rows.size.toString()
            ))
        }
        stickyCollectionPager(filtered.size, page, { page = it })
        items(visible, key = { "${mode}|${it.first}" }) { row ->
            Card(onClick = { selected = row }, colors = CardDefaults.cardColors(containerColor = SurfaceRaised), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) { CapabilityKeyValue(row.first, row.second) }
            }
        }
    }
    selected?.let { row ->
        ExpressiveDetailDialog(row.first, onDismiss = { selected = null }) {
            DetailEvidenceRow("Runtime value", row.second)
            DetailEvidenceRow("Query", queryName)
            if (mode < 3) {
                DetailEvidenceRow("Evidence", diagnostic?.status ?: "Unknown")
                if (!diagnostic?.detail.isNullOrBlank()) DetailEvidenceRow("Diagnostic", diagnostic!!.detail)
            }
            Text("Registered symbolic names are displayed when authoritative; unknown or unavailable runtime values are not silently converted to Supported.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExtensionsPage(r: GlReport) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var mode by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val rows = when (mode) { 0 -> r.extensions; 1 -> r.egl.extensions; else -> r.egl.clientExtensions }
    val scopeName = when (mode) { 0 -> "OpenGL® ES™ runtime"; 1 -> "EGL™ display runtime"; else -> "EGL™ client runtime" }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.contains(query, true) } }
    val pageCount = maxOf(1, (filtered.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(mode, query) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visible = remember(filtered, page) { filtered.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard("Extensions") {
                ExpressiveFilterBar(listOf("OpenGL® ES™", "EGL™ display", "EGL™ client"), mode) { mode = it }
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
                Text("Long press a token for the same evidence actions as other capability rows, or choose Details for registry and query evidence.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Matching extensions" to filtered.size.toString(),
                "Scope total" to rows.size.toString(),
                "Showing" to if (filtered.isEmpty()) "0" else "${first + 1}-${first + visible.size}"
            ))
        }
        stickyCollectionPager(filtered.size, page, { page = it })
        items(visible, key = { it }) { ext ->
            CapabilityItemCard {
                CapabilityKeyValue(ext, "ENUMERATED · $scopeName")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ChevronAffordance("Details", "Inspect runtime and registry evidence for $ext") { selected = ext }
                }
            }
        }
    }
    selected?.let { ext ->
        val diagnostic = r.diagnostics.firstOrNull { it.name == ext }
        val evidenceValue = "ENUMERATED · $scopeName"
        ExpressiveDetailDialog(ext, onDismiss = { selected = null }) {
            CapabilityKeyValue("Runtime evidence", "Exact extension token enumerated")
            CapabilityKeyValue("Scope", scopeName)
            CapabilityKeyValue("Namespace", extensionNamespace(ext))
            CapabilityKeyValue("Runtime status", evidenceValue)
            CapabilityKeyValue("Registry baseline", if (mode == 0) "Khronos OpenGL® ES™ registry · ES 3.2" else "Khronos EGL™ registry · EGL™ 1.5")
            val queryGates = QUERY_DEPENDENCIES[ext].orEmpty()
            CapabilityKeyValue("Dedicated query handler", if (queryGates.isEmpty()) "No dedicated implementation-dependent query gate" else queryGates.joinToString(" · "))
            queryGates.forEach { gate ->
                val gateEvidence = r.diagnostics.firstOrNull { it.name == gate }
                CapabilityKeyValue(gate, if (gateEvidence == null) "Unknown · query evidence unavailable" else if (gateEvidence.detail.isBlank()) gateEvidence.status else "${gateEvidence.status} · ${gateEvidence.detail}")
            }
            diagnostic?.let { CapabilityKeyValue("Related query", if (it.detail.isBlank()) it.status else "${it.status} · ${it.detail}") }
            extensionRegistryUrl(ext)?.let { url ->
                ExpressiveContainedIconTextButton("Open Khronos specification", R.drawable.ic_open_external,
                    enabled = LocalValidatedNetwork.current) { uriHandler.openUri(url) }
            }
            Text("Runtime enumeration, registry metadata and dedicated feature/limit query evidence remain separate. The Khronos link remains authoritative for the complete interface definition.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PrecisionPage(r: GlReport) {
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val diagnosticByName = remember(r.diagnostics) { r.diagnostics.associateBy { it.name } }
    val rows = remember(r.precision, query) {
        if (query.isBlank()) r.precision else r.precision.filter { listOf(it.shader, it.type, it.rangeMin, it.rangeMax, it.precision).joinToString(" ").contains(query, true) }
    }
    val pageCount = maxOf(1, (rows.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(query) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visibleRows = remember(rows, page) { rows.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard("Shader precision") {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search shader precision…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Matching precision rows" to rows.size.toString(),
                "Shader stages" to rows.map { it.shader }.distinct().size.toString(),
                "Precision types" to rows.map { it.type }.distinct().size.toString(),
                "Showing" to if (rows.isEmpty()) "0" else "${first + 1}-${first + visibleRows.size}"
            ))
        }
        stickyCollectionPager(rows.size, page, { page = it })
        items(visibleRows, key = { "${it.shader}|${it.type}" }) { p ->
            CapabilityItemCard {
                CapabilityKeyValue("Shader", p.shader)
                CapabilityKeyValue("Type", p.type)
                CapabilityKeyValue("Range", "${p.rangeMin} … ${p.rangeMax}")
                CapabilityKeyValue("Precision", p.precision.toString())
                val diagnostic = diagnosticByName["${p.shader}/${p.type}"]
                CapabilityKeyValue("Query evidence", diagnostic?.status ?: "Unknown")
                if (!diagnostic?.detail.isNullOrBlank()) CapabilityKeyValue("Diagnostic", diagnostic!!.detail)
            }
        }
    }
}

private fun eglBooleanLabel(value: Int?): String = when (value) { 0 -> "False"; 1 -> "True"; else -> "Unavailable" }

@Composable
private fun ConfigsPage(r: GlReport) {
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
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
    val pageCount = maxOf(1, (rows.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(query) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visibleRows = remember(rows, page) { rows.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard("EGL Configs") {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search EGL configs…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Matching configs" to rows.size.toString(),
                "Reported configs" to r.eglConfigs.size.toString(),
                "Showing" to if (rows.isEmpty()) "0" else "${first + 1}-${first + visibleRows.size}"
            ))
        }
        stickyCollectionPager(rows.size, page, { page = it })
        items(visibleRows, key = { it.id }) { c ->
            CapabilitySectionCard("EGL Config ${c.id}") {
                CapabilityKeyValue("RGBA", if (listOf(c.red, c.green, c.blue, c.alpha).all { it != null }) listOf(c.red, c.green, c.blue, c.alpha).joinToString(" / ") else "R=${eglConfigIntEvidence(c, "red", c.red)} · G=${eglConfigIntEvidence(c, "green", c.green)} · B=${eglConfigIntEvidence(c, "blue", c.blue)} · A=${eglConfigIntEvidence(c, "alpha", c.alpha)}")
                CapabilityKeyValue("Depth / stencil", if (c.depth != null && c.stencil != null) "${c.depth} / ${c.stencil}" else "depth=${eglConfigIntEvidence(c, "depth", c.depth)} · stencil=${eglConfigIntEvidence(c, "stencil", c.stencil)}")
                CapabilityKeyValue("Samples", if (c.sampleBuffers != null && c.samples != null) "${c.sampleBuffers} buffers, ${c.samples} samples" else "buffers=${eglConfigIntEvidence(c, "sampleBuffers", c.sampleBuffers)} · samples=${eglConfigIntEvidence(c, "samples", c.samples)}")
                CapabilityKeyValue("Surface type", eglConfigAttributeEvidence(c, "surfaceType", c.surfaceType))
                CapabilityKeyValue("Renderable type", eglConfigAttributeEvidence(c, "renderableType", c.renderableType))
                CapabilityKeyValue("Conformant", eglConfigAttributeEvidence(c, "conformant", c.conformant))
                CapabilityKeyValue("Config caveat", eglConfigAttributeEvidence(c, "configCaveat", c.configCaveat))
                CapabilityKeyValue("Color buffer type", eglConfigAttributeEvidence(c, "colorBufferType", c.colorBufferType))
                CapabilityKeyValue("Buffer size", eglConfigIntEvidence(c, "bufferSize", c.bufferSize))
                CapabilityKeyValue("Luminance size", eglConfigIntEvidence(c, "luminanceSize", c.luminanceSize))
                CapabilityKeyValue("Alpha mask size", eglConfigIntEvidence(c, "alphaMaskSize", c.alphaMaskSize))
                CapabilityKeyValue("Bind to texture RGB", eglConfigBooleanEvidence(c, "bindToTextureRgb", c.bindToTextureRgb))
                CapabilityKeyValue("Bind to texture RGBA", eglConfigBooleanEvidence(c, "bindToTextureRgba", c.bindToTextureRgba))
                CapabilityKeyValue("Max pbuffer width", eglConfigIntEvidence(c, "maxPbufferWidth", c.maxPbufferWidth))
                CapabilityKeyValue("Max pbuffer height", eglConfigIntEvidence(c, "maxPbufferHeight", c.maxPbufferHeight))
                CapabilityKeyValue("Max pbuffer pixels", eglConfigIntEvidence(c, "maxPbufferPixels", c.maxPbufferPixels))
                CapabilityKeyValue("Native renderable", eglConfigBooleanEvidence(c, "nativeRenderable", c.nativeRenderable))
                CapabilityKeyValue("Native visual ID", eglConfigIntEvidence(c, "nativeVisualId", c.nativeVisualId))
                CapabilityKeyValue("Native visual type", eglConfigIntEvidence(c, "nativeVisualType", c.nativeVisualType))
                CapabilityKeyValue("Transparency type", eglConfigAttributeEvidence(c, "transparentType", c.transparentType))
                CapabilityKeyValue("Transparent RGB", if (c.transparentRed != null && c.transparentGreen != null && c.transparentBlue != null) "${c.transparentRed} / ${c.transparentGreen} / ${c.transparentBlue}" else "R=${eglConfigIntEvidence(c, "transparentRed", c.transparentRed)} · G=${eglConfigIntEvidence(c, "transparentGreen", c.transparentGreen)} · B=${eglConfigIntEvidence(c, "transparentBlue", c.transparentBlue)}")
                CapabilityKeyValue("EGL_ANDROID_recordable", eglConfigExtensionEvidence(r, c, c.recordableAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_recordable", "EGL_RECORDABLE_ANDROID"))
                CapabilityKeyValue("EGL_ANDROID_framebuffer_target", eglConfigExtensionEvidence(r, c, c.framebufferTargetAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_framebuffer_target", "EGL_FRAMEBUFFER_TARGET_ANDROID"))
                CapabilityKeyValue("EGL_EXT_pixel_format_float", eglConfigExtensionEvidence(r, c, c.colorComponentTypeExt, "EGL_EXT_pixel_format_float", "EGL_COLOR_COMPONENT_TYPE_EXT"))
                if (c.unavailableAttributes.isNotEmpty()) CapabilityKeyValue("Unavailable attributes", c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" })
                CapabilityKeyValue("Level", eglConfigIntEvidence(c, "level", c.level))
                CapabilityKeyValue("Swap interval", if (c.minSwapInterval != null && c.maxSwapInterval != null) "${c.minSwapInterval} … ${c.maxSwapInterval}" else "min=${eglConfigIntEvidence(c, "minSwapInterval", c.minSwapInterval)} · max=${eglConfigIntEvidence(c, "maxSwapInterval", c.maxSwapInterval)}")
            }
        }
    }
}

@Composable
private fun SettingsPage(
    activity: MainActivity,
    report: GlReport,
    display: DisplayInfo,
    collectionReady: Boolean,
    selectedSection: SettingsSection?,
    onSectionSelected: (SettingsSection?) -> Unit
) {
    AnimatedContent(
        targetState = selectedSection,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            when {
                initialState == null && targetState != null ->
                    slideInHorizontally(tween(durationMillis = 280)) { it / 8 } + fadeIn(tween(durationMillis = 220)) togetherWith
                        slideOutHorizontally(tween(durationMillis = 180)) { -it / 12 } + fadeOut(tween(durationMillis = 150))
                initialState != null && targetState == null ->
                    slideInHorizontally(tween(durationMillis = 240)) { -it / 10 } + fadeIn(tween(durationMillis = 200)) togetherWith
                        slideOutHorizontally(tween(durationMillis = 200)) { it / 10 } + fadeOut(tween(durationMillis = 150))
                else -> fadeIn(tween(durationMillis = 200)) togetherWith fadeOut(tween(durationMillis = 150))
            }
        },
        label = "settingsSectionTransition"
    ) { targetSection ->
        when (targetSection) {
            null -> OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
                item { SettingsSectionCards { onSectionSelected(it) } }
            }
            SettingsSection.INFO -> InfoPage(activity, report, display, collectionReady, InfoContentMode.INFO)
            SettingsSection.REPORTS_DATABASE -> InfoPage(activity, report, display, collectionReady, InfoContentMode.REPORTS)
            SettingsSection.UPDATE_PREFERENCES -> UpdatePreferencesPage(activity)
        }
    }
}

@Composable
private fun UpdatePreferencesPage(activity: MainActivity) {
    val networkAvailable = LocalValidatedNetwork.current
    OpenGLESScopeLazyPage(verticalSpacing = 14.dp) {
        item {
            CapabilitySectionCard("Update preferences") {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Direct GitHub updates", fontWeight = FontWeight.SemiBold)
                        Text(if (activity.directUpdatesEnabled) "Enabled · official OpenGLESScope GitHub Releases channel" else "Disabled · recommended when Obtainium manages updates", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    ExpressiveSwitch(checked = activity.directUpdatesEnabled, onCheckedChange = { activity.requestDirectUpdatesChanged(it) })
                }
                Text("Direct GitHub updates are enabled by default. Disabling them stops startup discovery and built-in APK download; Obtainium can track the official universal APK instead.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("Opening animation") {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Show opening animation", fontWeight = FontWeight.SemiBold)
                        Text(if (activity.openingAnimationEnabled) "Enabled · OpenGL® ES™ collection starts only after the animation finishes" else "Disabled · collection starts as soon as the main UI opens", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    ExpressiveSwitch(checked = activity.openingAnimationEnabled, onCheckedChange = activity::persistOpeningAnimationPreference)
                }
                Text("A watchdog releases startup automatically if the animation cannot complete, so devices without usable EGL™/OpenGL® ES™ information cannot become stuck on launch.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}




private const val ANALYSIS_MAX_SNAPSHOT_BYTES = 8 * 1024 * 1024
private const val ANALYSIS_MAX_ENTRIES = 32768
private const val ANALYSIS_MAX_KEY_LENGTH = 1024
private const val ANALYSIS_MAX_VALUE_LENGTH = 16384
private const val ANALYSIS_MAX_WATCHED = 256
private const val ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT = 250
private const val ANALYSIS_DATABASE_LIST_LIMIT = 50
private const val ANALYSIS_DATABASE_LIST_MAX = 200

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
    "GL_EXT_disjoint_timer_query" to listOf("GL_TIME_ELAPSED_EXT", "GL_TIMESTAMP_EXT"),
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
    "EGL_KHR_create_context" to listOf("EGL_CONTEXT_CLIENT_TYPE", "EGL_CONTEXT_CLIENT_VERSION", "EGL_CONTEXT_MINOR_VERSION", "EGL_CONTEXT_FLAGS_KHR", "EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY"),
    "EGL_KHR_create_context_no_error" to listOf("EGL_CONTEXT_OPENGL_NO_ERROR_KHR"),
    "EGL_ANDROID_recordable" to listOf("EGL_RECORDABLE_ANDROID"),
    "EGL_ANDROID_framebuffer_target" to listOf("EGL_FRAMEBUFFER_TARGET_ANDROID"),
    "EGL_EXT_pixel_format_float" to listOf("EGL_COLOR_COMPONENT_TYPE_EXT"),
    "EGL_EXT_protected_content" to listOf("EGL_PROTECTED_CONTENT_EXT"),
    "EGL_EXT_buffer_age" to listOf("EGL_BUFFER_AGE_KHR"),
    "EGL_KHR_partial_update" to listOf("EGL_BUFFER_AGE_KHR"),
    "EGL_EXT_device_query" to listOf("EGL_DEVICE_EXT", "EGL_RENDERER_EXT", "EGL_DRIVER_NAME_EXT", "EGL_DEVICE_TYPE_EXT"),
    "EGL_EXT_device_base" to listOf("EGL_DEVICE_EXT", "EGL_EXT_device_enumeration"),
    "EGL_EXT_device_enumeration" to listOf("EGL_EXT_device_enumeration"),
    "EGL_EXT_surface_compression" to listOf("EGL_EXT_surface_compression rates")
)

private val QUERY_DEPENDENCIES = linkedMapOf<String, List<String>>().apply {
    putAll(GL_QUERY_DEPENDENCIES)
    putAll(EGL_QUERY_DEPENDENCIES)
}

private fun eglConfigAnalysisValue(r: GlReport, c: EglConfigEntry): String = listOf(
    "red=${eglConfigIntEvidence(c, "red", c.red)}", "green=${eglConfigIntEvidence(c, "green", c.green)}", "blue=${eglConfigIntEvidence(c, "blue", c.blue)}", "alpha=${eglConfigIntEvidence(c, "alpha", c.alpha)}",
    "depth=${eglConfigIntEvidence(c, "depth", c.depth)}", "stencil=${eglConfigIntEvidence(c, "stencil", c.stencil)}",
    "sampleBuffers=${eglConfigIntEvidence(c, "sampleBuffers", c.sampleBuffers)}", "samples=${eglConfigIntEvidence(c, "samples", c.samples)}",
    "surfaceType=${eglConfigAttributeEvidence(c, "surfaceType", c.surfaceType)}", "renderableType=${eglConfigAttributeEvidence(c, "renderableType", c.renderableType)}",
    "conformant=${eglConfigAttributeEvidence(c, "conformant", c.conformant)}", "configCaveat=${eglConfigAttributeEvidence(c, "configCaveat", c.configCaveat)}",
    "colorBufferType=${eglConfigAttributeEvidence(c, "colorBufferType", c.colorBufferType)}", "level=${eglConfigIntEvidence(c, "level", c.level)}",
    "nativeRenderable=${eglConfigBooleanEvidence(c, "nativeRenderable", c.nativeRenderable)}", "nativeVisualId=${eglConfigIntEvidence(c, "nativeVisualId", c.nativeVisualId)}",
    "minSwapInterval=${eglConfigIntEvidence(c, "minSwapInterval", c.minSwapInterval)}", "maxSwapInterval=${eglConfigIntEvidence(c, "maxSwapInterval", c.maxSwapInterval)}",
    "bufferSize=${eglConfigIntEvidence(c, "bufferSize", c.bufferSize)}", "luminanceSize=${eglConfigIntEvidence(c, "luminanceSize", c.luminanceSize)}", "alphaMaskSize=${eglConfigIntEvidence(c, "alphaMaskSize", c.alphaMaskSize)}",
    "bindToTextureRgb=${eglConfigBooleanEvidence(c, "bindToTextureRgb", c.bindToTextureRgb)}", "bindToTextureRgba=${eglConfigBooleanEvidence(c, "bindToTextureRgba", c.bindToTextureRgba)}",
    "maxPbufferWidth=${eglConfigIntEvidence(c, "maxPbufferWidth", c.maxPbufferWidth)}", "maxPbufferHeight=${eglConfigIntEvidence(c, "maxPbufferHeight", c.maxPbufferHeight)}", "maxPbufferPixels=${eglConfigIntEvidence(c, "maxPbufferPixels", c.maxPbufferPixels)}",
    "nativeVisualType=${eglConfigIntEvidence(c, "nativeVisualType", c.nativeVisualType)}", "transparentType=${eglConfigAttributeEvidence(c, "transparentType", c.transparentType)}",
    "transparentRed=${eglConfigIntEvidence(c, "transparentRed", c.transparentRed)}", "transparentGreen=${eglConfigIntEvidence(c, "transparentGreen", c.transparentGreen)}", "transparentBlue=${eglConfigIntEvidence(c, "transparentBlue", c.transparentBlue)}",
    "recordableAndroid=${eglConfigExtensionEvidence(r, c, c.recordableAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_recordable", "EGL_RECORDABLE_ANDROID")}",
    "framebufferTargetAndroid=${eglConfigExtensionEvidence(r, c, c.framebufferTargetAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_framebuffer_target", "EGL_FRAMEBUFFER_TARGET_ANDROID")}",
    "colorComponentTypeExt=${eglConfigExtensionEvidence(r, c, c.colorComponentTypeExt, "EGL_EXT_pixel_format_float", "EGL_COLOR_COMPONENT_TYPE_EXT")}",
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
    put("gl-runtime/contextFlags", runtimeQueryEvidence(report, report.glRuntime.contextFlags, "GL_CONTEXT_FLAGS"))
    put("gl-runtime/resetNotificationStrategy", runtimeQueryEvidence(report, report.glRuntime.resetNotificationStrategy, "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT"))
    put("gl-runtime/resetNotificationStrategyQuery", report.glRuntime.resetNotificationStrategyQuery ?: "Not applicable")
    put("gl-runtime/robustAccess", runtimeQueryEvidence(report, report.glRuntime.robustAccess?.toString(), "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT"))
    put("gl-runtime/robustAccessQuery", report.glRuntime.robustAccessQuery ?: "Not applicable")
    report.glRuntime.unavailableAttributes.forEach { put("gl-runtime/unavailable/${it.name}", it.error) }
    put("egl-runtime/boundApi", report.eglRuntime.boundApi)
    put("egl-runtime/configId", runtimeQueryEvidence(report, report.eglRuntime.configId?.toString(), "EGL_CONFIG_ID/context"))
    put("egl-runtime/clientType", runtimeQueryEvidence(report, report.eglRuntime.clientType, "EGL_CONTEXT_CLIENT_TYPE"))
    put("egl-runtime/clientVersion", runtimeQueryEvidence(report, report.eglRuntime.clientVersion?.toString(), "EGL_CONTEXT_CLIENT_VERSION"))
    put("egl-runtime/renderBuffer", runtimeQueryEvidence(report, report.eglRuntime.renderBuffer, "EGL_RENDER_BUFFER/context"))
    put("egl-runtime/currentContext", report.eglRuntime.currentContext.toString())
    put("egl-runtime/currentDisplay", report.eglRuntime.currentDisplay.toString())
    put("egl-runtime/currentDrawSurface", report.eglRuntime.currentDrawSurface.toString())
    put("egl-runtime/currentReadSurface", report.eglRuntime.currentReadSurface.toString())
    put("egl-runtime/surfaceConfigId", runtimeQueryEvidence(report, report.eglRuntime.surfaceConfigId?.toString(), "EGL_CONFIG_ID/surface"))
    put("egl-runtime/surfaceSize", eglRuntimeSizeEvidence(report).replace(" × ", "x"))
    put("egl-runtime/surfaceGlColorspace", runtimeQueryEvidence(report, report.eglRuntime.surfaceGlColorspace, "EGL_GL_COLORSPACE"))
    put("egl-runtime/surfaceGlColorspaceQuery", report.eglRuntime.surfaceGlColorspaceQuery ?: "Not applicable")
    put("egl-runtime/surfaceVgAlphaFormat", runtimeQueryEvidence(report, report.eglRuntime.surfaceVgAlphaFormat, "EGL_VG_ALPHA_FORMAT"))
    put("egl-runtime/surfaceVgAlphaFormatQuery", report.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable")
    put("egl-runtime/surfaceVgColorspace", runtimeQueryEvidence(report, report.eglRuntime.surfaceVgColorspace, "EGL_VG_COLORSPACE"))
    put("egl-runtime/surfaceVgColorspaceQuery", report.eglRuntime.surfaceVgColorspaceQuery ?: "Not applicable")
    put("egl-runtime/surfaceHorizontalResolution", runtimeQueryEvidence(report, report.eglRuntime.surfaceHorizontalResolution?.toString(), "EGL_HORIZONTAL_RESOLUTION"))
    put("egl-runtime/surfaceVerticalResolution", runtimeQueryEvidence(report, report.eglRuntime.surfaceVerticalResolution?.toString(), "EGL_VERTICAL_RESOLUTION"))
    put("egl-runtime/surfacePixelAspectRatio", runtimeQueryEvidence(report, report.eglRuntime.surfacePixelAspectRatio?.toString(), "EGL_PIXEL_ASPECT_RATIO"))
    put("egl-runtime/surfaceLargestPbuffer", runtimeQueryEvidence(report, report.eglRuntime.surfaceLargestPbuffer?.toString(), "EGL_LARGEST_PBUFFER"))
    put("egl-runtime/surfaceRenderBuffer", runtimeQueryEvidence(report, report.eglRuntime.surfaceRenderBuffer, "EGL_RENDER_BUFFER/surface"))
    put("egl-runtime/surfaceSwapBehavior", runtimeQueryEvidence(report, report.eglRuntime.surfaceSwapBehavior, "EGL_SWAP_BEHAVIOR"))
    put("egl-runtime/surfaceTextureFormat", runtimeQueryEvidence(report, report.eglRuntime.surfaceTextureFormat, "EGL_TEXTURE_FORMAT"))
    put("egl-runtime/surfaceTextureTarget", runtimeQueryEvidence(report, report.eglRuntime.surfaceTextureTarget, "EGL_TEXTURE_TARGET"))
    put("egl-runtime/surfaceMipmapTexture", runtimeQueryEvidence(report, report.eglRuntime.surfaceMipmapTexture?.toString(), "EGL_MIPMAP_TEXTURE"))
    put("egl-runtime/surfaceMipmapLevel", runtimeQueryEvidence(report, report.eglRuntime.surfaceMipmapLevel?.toString(), "EGL_MIPMAP_LEVEL"))
    put("egl-runtime/surfaceMultisampleResolve", runtimeQueryEvidence(report, report.eglRuntime.surfaceMultisampleResolve, "EGL_MULTISAMPLE_RESOLVE"))
    report.eglRuntime.unavailableAttributes.forEach { put("egl-runtime/unavailable/${it.name}", it.error) }
    report.eglCapabilities.forEach { put("egl-capability/${it.name}", "${it.status}${it.value.takeIf { v -> v.isNotBlank() }?.let { v -> " · $v" } ?: ""}${it.detail.takeIf { d -> d.isNotBlank() }?.let { d -> " · $d" } ?: ""}") }
    report.limits.forEach { put("limit/${it.name}", it.value) }
    report.extensions.forEach { put("extension/gl/$it", "present") }
    report.egl.extensions.forEach { put("extension/egl-display/$it", "present") }
    report.egl.clientExtensions.forEach { put("extension/egl-client/$it", "present") }
    report.compressedFormats.forEach { put("format/compressed/$it", "present") }
    report.internalFormats.forEach { f ->
        val samples = f.sampleCounts.joinToString(",").ifBlank { "none" }
        val nv = f.nvSampleProperties.joinToString(";") { n -> "${n.samples}:${n.multisamples}:${n.supersampleScaleX}x${n.supersampleScaleY}:conformant=${n.conformant}" }.ifBlank { "none" }
        put("format/internal/${f.target}/${f.internalFormat}", "${f.status}; samples=$samples; nv=$nv${f.detail.takeIf { it.isNotBlank() }?.let { "; detail=$it" } ?: ""}")
    }
    report.shaderBinaryFormats.forEach { put("format/shader-binary/$it", "present") }
    report.programBinaryFormats.forEach { put("format/program-binary/$it", "present") }
    report.precision.forEach { put("precision/${it.shader}/${it.type}", "range=${it.rangeMin}..${it.rangeMax}, precision=${it.precision}") }
    report.eglConfigs.forEach { put("eglconfig/${it.id}", eglConfigAnalysisValue(report, it)) }
    report.diagnostics.forEach { put("query/${it.name}", "${it.status}${it.detail.takeIf { d -> d.isNotBlank() }?.let { d -> " · $d" } ?: ""}") }
    put("display/name", display.name)
    put("display/modeId", display.modeId?.toString() ?: "Unavailable")
    put("display/resolution", if (display.width != null && display.height != null) "${display.width}x${display.height}" else "Unavailable")
    put("display/refreshRate", display.refreshRate?.toString() ?: "Unavailable")
    put("display/wideColor", display.wideColor?.toString() ?: "Unavailable")
    put("display/hdrCapabilityStatus", display.hdrCapabilityStatus)
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
        listOf("name", "resolution", "refreshRate", "wideColor", "hdrCapabilityStatus").forEach { key -> put("display/$key", display.optString(key, "Unavailable")) }
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

private data class GlDiagnosticCheck(val label: String, val deduction: Int, val triggered: Boolean, val evidence: String)
private data class GlDiagnosticScore(val score: Int?, val level: String, val factors: List<String>, val checks: List<GlDiagnosticCheck>)

private fun glDiagnosticEvidenceChecks(report: GlReport): List<GlDiagnosticCheck> {
    if (!report.available) return listOf(GlDiagnosticCheck("Completed capability report", 100, true, report.reason.ifBlank { "No completed OpenGL ES report is available" }))
    val unavailable = report.diagnostics.filter { it.status.equals("Unavailable", true) }
    val unknown = report.diagnostics.filter { it.status.equals("Unknown", true) }
    val checks = mutableListOf<GlDiagnosticCheck>()
    checks += GlDiagnosticCheck(
        "Explicit query failures",
        minOf(35, 5 + unavailable.size.coerceAtMost(6) * 5),
        unavailable.isNotEmpty(),
        if (unavailable.isEmpty()) "No query diagnostic is Unavailable" else "${unavailable.size} query diagnostic(s) are Unavailable"
    )
    checks += GlDiagnosticCheck(
        "Unknown query results",
        minOf(15, unknown.size.coerceAtMost(5) * 3),
        unknown.isNotEmpty(),
        if (unknown.isEmpty()) "No query diagnostic remains Unknown" else "${unknown.size} query diagnostic(s) remain Unknown"
    )
    listOf("GL_VENDOR", "GL_RENDERER", "GL_VERSION", "GL_SHADING_LANGUAGE_VERSION").forEach { name ->
        val d = report.diagnostics.firstOrNull { it.name == name }
        checks += GlDiagnosticCheck(
            "$name runtime identity query",
            10,
            d == null || !d.status.equals("Available", true),
            d?.let { "${it.status}${if (it.detail.isBlank()) "" else " · ${it.detail}"}" } ?: "Diagnostic missing"
        )
    }
    val glEnumeration = report.diagnostics.firstOrNull { it.name == "GL_EXTENSIONS" }
    checks += GlDiagnosticCheck(
        "OpenGL ES extension enumeration",
        10,
        glEnumeration == null || !glEnumeration.status.equals("Available", true),
        glEnumeration?.let { "${it.status}${if (it.detail.isBlank()) "" else " · ${it.detail}"}" } ?: "Diagnostic missing"
    )
    val eglEnumeration = report.diagnostics.firstOrNull { it.name == "EGL_EXTENSIONS" }
    checks += GlDiagnosticCheck(
        "EGL display extension enumeration",
        5,
        eglEnumeration == null || !eglEnumeration.status.equals("Available", true),
        eglEnumeration?.let { "${it.status}${if (it.detail.isBlank()) "" else " · ${it.detail}"}" } ?: "Diagnostic missing"
    )
    val bindingsComplete = report.eglRuntime.currentContext && report.eglRuntime.currentDisplay && report.eglRuntime.currentDrawSurface && report.eglRuntime.currentReadSurface
    checks += GlDiagnosticCheck(
        "Current EGL context/display/surface bindings",
        10,
        !bindingsComplete,
        "context=${report.eglRuntime.currentContext}, display=${report.eglRuntime.currentDisplay}, draw=${report.eglRuntime.currentDrawSurface}, read=${report.eglRuntime.currentReadSurface}"
    )
    return checks
}

private fun glDiagnosticEvidenceScore(report: GlReport): GlDiagnosticScore {
    if (!report.available) return GlDiagnosticScore(null, "Unavailable", listOf(report.reason.ifBlank { "No completed OpenGL ES report is available" }), glDiagnosticEvidenceChecks(report))
    val checks = glDiagnosticEvidenceChecks(report)
    val score = (100 - checks.filter { it.triggered }.sumOf { it.deduction }).coerceAtLeast(0)
    val factors = checks.filter { it.triggered }.map { "-${it.deduction} · ${it.label} · ${it.evidence}" }.toMutableList()
    if (report.eglRuntime.unavailableAttributes.isNotEmpty()) factors += "${report.eglRuntime.unavailableAttributes.size} explicit EGL runtime attribute failure(s) retained with exact EGL errors; these remain evidence but do not add a second score penalty"
    val level = when {
        score >= 95 -> "No explicit collection anomalies"
        score >= 80 -> "Minor explicit anomalies"
        score >= 60 -> "Multiple explicit anomalies"
        else -> "Severe explicit collection anomalies"
    }
    if (factors.isEmpty()) factors += "No explicit collection/query anomaly was recorded by OpenGLESScope"
    return GlDiagnosticScore(score, level, factors, checks)
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

private data class AnalysisDatabaseRow(
    val id: String,
    val submittedAt: String,
    val gpuName: String,
    val vendor: String,
    val openGlesVersion: String,
    val eglVersion: String,
    val manufacturer: String,
    val model: String,
    val applicationVersion: String
)

private data class DatabaseSubmittedDisplay(val date: String, val time: String, val zone: String)

private fun databaseSubmittedDisplay(context: Context, raw: String): DatabaseSubmittedDisplay? = runCatching {
    val instant = java.time.Instant.parse(raw.trim())
    val date = java.util.Date.from(instant)
    val timeZone = java.util.TimeZone.getDefault()
    val dateFormat = android.text.format.DateFormat.getMediumDateFormat(context).apply { this.timeZone = timeZone }
    val timeFormat = android.text.format.DateFormat.getTimeFormat(context).apply { this.timeZone = timeZone }
    DatabaseSubmittedDisplay(
        date = dateFormat.format(date),
        time = timeFormat.format(date),
        zone = timeZone.getDisplayName(timeZone.inDaylightTime(date), java.util.TimeZone.SHORT)
    )
}.getOrNull()

@Composable
private fun DatabaseSubmittedAt(raw: String) {
    val context = LocalContext.current
    val display = remember(raw, context) { databaseSubmittedDisplay(context, raw) }
    if (display == null) {
        CapabilityKeyValue("Submitted", raw.ifBlank { "Unknown" })
    } else {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Submitted", color = TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveInfoPill("Date", display.date, Modifier.weight(1f))
                ExpressiveInfoPill("Time", display.time, Modifier.weight(1f))
            }
            Text("Device local time · ${display.zone}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private data class AnalysisReportCursor(val submittedAt: String, val id: String)
private data class AnalysisReportPage(val rows: List<AnalysisDatabaseRow>, val nextCursor: AnalysisReportCursor?)
private val REPORT_LIST_CURSOR_TIMESTAMP = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(?:\\.[0-9]+)?Z")
private val REPORT_LIST_CURSOR_ID = Regex("[a-f0-9]{64}")
private suspend fun fetchDatabaseReportPage(context: Context, cursor: AnalysisReportCursor? = null): AnalysisReportPage = withContext(Dispatchers.IO) {
    if (!hasValidatedInternet(context)) error("Validated internet access is unavailable")
    val base = DATABASE_API.toHttpUrlOrNull() ?: error("Official Database endpoint is invalid")
    if (cursor != null && (!REPORT_LIST_CURSOR_TIMESTAMP.matches(cursor.submittedAt) || !REPORT_LIST_CURSOR_ID.matches(cursor.id))) error("Invalid Database list cursor")
    val urlBuilder = base.newBuilder().addPathSegments("v1/reports").addQueryParameter("limit", ANALYSIS_DATABASE_LIST_LIMIT.toString())
    if (cursor != null) urlBuilder.addQueryParameter("beforeSubmittedAt", cursor.submittedAt).addQueryParameter("beforeId", cursor.id)
    val url = urlBuilder.build()
    DATABASE_HTTP_CLIENT.newCall(Request.Builder().url(url).get().header("Accept", "application/json").build()).execute().use { response ->
        val body = readResponseTextLimited(response.body, 2 * 1024 * 1024)
        if (!response.isSuccessful) error(runCatching { JSONObject(body).optString("error") }.getOrDefault("").ifBlank { "HTTP ${response.code}" })
        val root = JSONObject(body)
        val array = root.optJSONArray("reports") ?: error("Database report list is missing")
        if (array.length() > ANALYSIS_DATABASE_LIST_MAX) error("Database returned too many report rows")
        val rows = (0 until minOf(array.length(), ANALYSIS_DATABASE_LIST_LIMIT)).mapNotNull { index ->
            val row = array.optJSONObject(index) ?: return@mapNotNull null
            val id = row.optString("id")
            if (!id.matches(Regex("[a-f0-9]{64}"))) return@mapNotNull null
            AnalysisDatabaseRow(
                id = id,
                submittedAt = row.optString("submitted_at").take(64),
                gpuName = row.optString("gpu_name").take(512),
                vendor = row.optString("vendor").take(512),
                openGlesVersion = row.optString("opengles_version").take(512),
                eglVersion = row.optString("egl_version").take(512),
                manufacturer = row.optString("manufacturer").take(256),
                model = row.optString("model").take(256),
                applicationVersion = row.optString("application_version").take(32)
            )
        }
        val nextJson = root.optJSONObject("nextCursor")
        val next = nextJson?.let {
            AnalysisReportCursor(it.optString("submittedAt"), it.optString("id"))
        }?.takeIf { REPORT_LIST_CURSOR_TIMESTAMP.matches(it.submittedAt) && REPORT_LIST_CURSOR_ID.matches(it.id) && rows.isNotEmpty() }
        if (nextJson != null && next == null) error("Database returned a malformed list cursor")
        AnalysisReportPage(rows, next)
    }
}

private fun analysisHistoryLabel(record: AnalysisHistoryRecord): String = runCatching {
    java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM).format(java.util.Date(record.savedAt))
}.getOrDefault(record.savedAt.toString())

private const val ANALYSIS_HISTORY_MAX_RECORDS = 8
private const val ANALYSIS_HISTORY_MAX_COMPRESSED_BYTES = 4 * 1024 * 1024
private data class AnalysisHistoryRecord(val file: File, val savedAt: Long, val renderer: String, val bytes: Long)

private fun analysisHistoryDirectory(context: Context): File = File(context.filesDir, "analysis_history")

private fun saveAnalysisHistory(context: Context, snapshot: JSONObject, renderer: String): AnalysisHistoryRecord {
    val raw = snapshot.toString().toByteArray(Charsets.UTF_8)
    if (raw.size > ANALYSIS_MAX_SNAPSHOT_BYTES) error("History snapshot exceeds the 8 MiB uncompressed safety bound")
    val dir = analysisHistoryDirectory(context)
    if (!dir.exists() && !dir.mkdirs()) error("Unable to create private history directory")
    val now = System.currentTimeMillis()
    val target = File(dir, "session-$now.json.gz")
    val temp = File(dir, ".session-$now.tmp")
    GZIPOutputStream(FileOutputStream(temp)).use { it.write(raw) }
    if (temp.length() > ANALYSIS_HISTORY_MAX_COMPRESSED_BYTES) { temp.delete(); error("History snapshot exceeds the 4 MiB compressed safety bound") }
    if (!temp.renameTo(target)) { temp.delete(); error("Unable to publish history snapshot atomically") }
    dir.listFiles { f -> f.isFile && f.name.startsWith("session-") && f.name.endsWith(".json.gz") }
        ?.sortedByDescending { it.lastModified() }?.drop(ANALYSIS_HISTORY_MAX_RECORDS)?.forEach { it.delete() }
    return AnalysisHistoryRecord(target, now, renderer, target.length())
}

private fun listAnalysisHistory(context: Context): List<AnalysisHistoryRecord> {
    val dir = analysisHistoryDirectory(context)
    dir.listFiles { f -> f.isFile && f.name.startsWith(".session-") && f.name.endsWith(".tmp") }?.forEach { it.delete() }
    return dir.listFiles { f -> f.isFile && f.name.startsWith("session-") && f.name.endsWith(".json.gz") }
        ?.sortedByDescending { it.lastModified() }
        ?.take(ANALYSIS_HISTORY_MAX_RECORDS)
        ?.map { file ->
            val savedAt = file.name.removePrefix("session-").removeSuffix(".json.gz").toLongOrNull() ?: file.lastModified()
            AnalysisHistoryRecord(file, savedAt, "Session snapshot", file.length())
        }.orEmpty()
}

private fun stableSnapshotFingerprint(snapshot: JSONObject): String = MessageDigest.getInstance("SHA-256")
    .digest(snapshot.toString().toByteArray(Charsets.UTF_8))
    .joinToString("") { "%02x".format(it) }

private fun loadAnalysisHistory(file: File): JSONObject {
    if (!file.isFile || file.length() > ANALYSIS_HISTORY_MAX_COMPRESSED_BYTES) error("Invalid or oversized history record")
    val bytes = GZIPInputStream(file.inputStream()).use { input -> readBoundedAnalysisBytes(input, ANALYSIS_MAX_SNAPSHOT_BYTES) }
    return validateGlAnalysisSnapshot(JSONObject(bytes.toString(Charsets.UTF_8)))
}

private fun databasePayloadToAnalysisSnapshot(payload: JSONObject): JSONObject {
    val entries = linkedMapOf<String, String>()
    val application = payload.optJSONObject("application") ?: JSONObject()
    val gpu = payload.optJSONObject("gpu") ?: JSONObject()
    val gl = payload.optJSONObject("opengles") ?: JSONObject()
    val egl = payload.optJSONObject("egl") ?: JSONObject()
    val device = payload.optJSONObject("device") ?: JSONObject()
    val tech = payload.optJSONObject("technicalReport") ?: error("Database report has no technicalReport object")
    val display = tech.optJSONObject("display") ?: payload.optJSONObject("display") ?: JSONObject()
    val technicalQueryDiagnostics = tech.optJSONArray("queryDiagnostics").toObjects().associateBy { it.optString("name") }
    val technicalEglExtensions = jsonStrings(tech.optJSONArray("eglExtensions")).toSet()
    fun technicalRuntimeEvidence(source: JSONObject, key: String, vararg names: String): String {
        if (source.has(key) && !source.isNull(key)) return source.opt(key)?.toString() ?: "Unknown"
        val diagnostics = names.mapNotNull { technicalQueryDiagnostics[it] }
        val diagnostic = diagnostics.firstOrNull { !it.optString("status").equals("Not applicable", true) } ?: diagnostics.firstOrNull()
        if (diagnostic == null) return "Unknown / not queried"
        val status = diagnostic.optString("status", "Unknown")
        val detail = diagnostic.optString("detail")
        return if (detail.isBlank()) status else "$status · $detail"
    }
    entries["identity/applicationVersion"] = application.optString("version", "Unknown")
    entries["identity/renderer"] = gpu.optString("name", "Unknown")
    entries["identity/vendor"] = gpu.optString("vendor", "Unknown")
    entries["identity/glVersion"] = gl.optString("version", "Unknown")
    entries["identity/glCoreVersion"] = "${gl.optInt("major")}.${gl.optInt("minor")}"
    entries["identity/glslVersion"] = gl.optString("glslVersion", "Unknown")
    entries["identity/eglVendor"] = egl.optString("vendor", "Unknown")
    entries["identity/eglVersion"] = egl.optString("version", "Unknown")
    entries["identity/eglInitializedVersion"] = egl.optString("initializedVersion", "Unknown")
    entries["identity/eglClientApis"] = egl.optString("clientApis", "Unknown")
    entries["identity/androidSecurityPatch"] = device.optString("securityPatch", "Unavailable")
    entries["identity/androidSdk"] = device.opt("sdk")?.toString() ?: "Unavailable"
    entries["identity/androidRelease"] = device.optString("androidRelease", "Unavailable")
    entries["identity/deviceManufacturer"] = device.optString("manufacturer", "Unavailable")
    entries["identity/deviceModel"] = device.optString("model", "Unavailable")
    tech.optJSONObject("glRuntime")?.let { g ->
        entries["gl-runtime/contextFlags"] = technicalRuntimeEvidence(g, "contextFlags", "GL_CONTEXT_FLAGS")
        entries["gl-runtime/resetNotificationStrategy"] = technicalRuntimeEvidence(g, "resetNotificationStrategy", "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT")
        entries["gl-runtime/resetNotificationStrategyQuery"] = if (g.isNull("resetNotificationStrategyQuery")) "Not applicable" else g.optString("resetNotificationStrategyQuery", "Not applicable")
        entries["gl-runtime/robustAccess"] = technicalRuntimeEvidence(g, "robustAccess", "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT")
        entries["gl-runtime/robustAccessQuery"] = if (g.isNull("robustAccessQuery")) "Not applicable" else g.optString("robustAccessQuery", "Not applicable")
        g.optJSONArray("unavailableAttributes").toObjects().forEach { a -> entries["gl-runtime/unavailable/${a.optString("name")}"] = a.optString("error") }
    }
    tech.optJSONObject("eglRuntime")?.let { e ->
        fun value(key: String, vararg names: String): String = technicalRuntimeEvidence(e, key, *names)
        entries["egl-runtime/boundApi"] = if (e.isNull("boundApi")) "Unknown" else e.optString("boundApi", "Unknown")
        entries["egl-runtime/configId"] = value("configId", "EGL_CONFIG_ID/context")
        entries["egl-runtime/clientType"] = value("clientType", "EGL_CONTEXT_CLIENT_TYPE")
        entries["egl-runtime/clientVersion"] = value("clientVersion", "EGL_CONTEXT_CLIENT_VERSION")
        entries["egl-runtime/renderBuffer"] = value("renderBuffer", "EGL_RENDER_BUFFER/context")
        entries["egl-runtime/currentContext"] = if (e.isNull("currentContext")) "Unknown" else e.optBoolean("currentContext").toString()
        entries["egl-runtime/currentDisplay"] = if (e.isNull("currentDisplay")) "Unknown" else e.optBoolean("currentDisplay").toString()
        entries["egl-runtime/currentDrawSurface"] = if (e.isNull("currentDrawSurface")) "Unknown" else e.optBoolean("currentDrawSurface").toString()
        entries["egl-runtime/currentReadSurface"] = if (e.isNull("currentReadSurface")) "Unknown" else e.optBoolean("currentReadSurface").toString()
        entries["egl-runtime/surfaceConfigId"] = value("surfaceConfigId", "EGL_CONFIG_ID/surface")
        val width = value("surfaceWidth", "EGL_WIDTH")
        val height = value("surfaceHeight", "EGL_HEIGHT")
        entries["egl-runtime/surfaceSize"] = if (!e.isNull("surfaceWidth") && !e.isNull("surfaceHeight")) "${e.optInt("surfaceWidth")}x${e.optInt("surfaceHeight")}" else "width=$width · height=$height"
        entries["egl-runtime/surfaceGlColorspace"] = value("surfaceGlColorspace", "EGL_GL_COLORSPACE")
        entries["egl-runtime/surfaceGlColorspaceQuery"] = e.optNullableString("surfaceGlColorspaceQuery") ?: "Not applicable"
        entries["egl-runtime/surfaceVgAlphaFormat"] = value("surfaceVgAlphaFormat", "EGL_VG_ALPHA_FORMAT")
        entries["egl-runtime/surfaceVgAlphaFormatQuery"] = e.optNullableString("surfaceVgAlphaFormatQuery") ?: "Not applicable"
        entries["egl-runtime/surfaceVgColorspace"] = value("surfaceVgColorspace", "EGL_VG_COLORSPACE")
        entries["egl-runtime/surfaceVgColorspaceQuery"] = e.optNullableString("surfaceVgColorspaceQuery") ?: "Not applicable"
        entries["egl-runtime/surfaceHorizontalResolution"] = value("surfaceHorizontalResolution", "EGL_HORIZONTAL_RESOLUTION")
        entries["egl-runtime/surfaceVerticalResolution"] = value("surfaceVerticalResolution", "EGL_VERTICAL_RESOLUTION")
        entries["egl-runtime/surfacePixelAspectRatio"] = value("surfacePixelAspectRatio", "EGL_PIXEL_ASPECT_RATIO")
        entries["egl-runtime/surfaceLargestPbuffer"] = value("surfaceLargestPbuffer", "EGL_LARGEST_PBUFFER")
        entries["egl-runtime/surfaceRenderBuffer"] = value("surfaceRenderBuffer", "EGL_RENDER_BUFFER/surface")
        entries["egl-runtime/surfaceSwapBehavior"] = value("surfaceSwapBehavior", "EGL_SWAP_BEHAVIOR")
        entries["egl-runtime/surfaceTextureFormat"] = value("surfaceTextureFormat", "EGL_TEXTURE_FORMAT")
        entries["egl-runtime/surfaceTextureTarget"] = value("surfaceTextureTarget", "EGL_TEXTURE_TARGET")
        entries["egl-runtime/surfaceMipmapTexture"] = value("surfaceMipmapTexture", "EGL_MIPMAP_TEXTURE")
        entries["egl-runtime/surfaceMipmapLevel"] = value("surfaceMipmapLevel", "EGL_MIPMAP_LEVEL")
        entries["egl-runtime/surfaceMultisampleResolve"] = value("surfaceMultisampleResolve", "EGL_MULTISAMPLE_RESOLVE")
        e.optJSONArray("unavailableAttributes").toObjects().forEach { a -> entries["egl-runtime/unavailable/${a.optString("name")}"] = a.optString("error") }
    }
    tech.optJSONArray("eglCapabilities").toObjects().forEach { c -> entries["egl-capability/${c.optString("name")}"] = "${c.optString("status")}${c.optString("value").takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}${c.optString("detail").takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}" }
    tech.optJSONArray("limits").toObjects().forEach { x -> entries["limit/${x.optString("name")}"] = x.optString("value") }
    jsonStrings(tech.optJSONArray("extensions")).forEach { entries["extension/gl/$it"] = "present" }
    jsonStrings(tech.optJSONArray("eglExtensions")).forEach { entries["extension/egl-display/$it"] = "present" }
    jsonStrings(tech.optJSONArray("eglClientExtensions")).forEach { entries["extension/egl-client/$it"] = "present" }
    jsonStrings(tech.optJSONArray("compressedFormats")).forEach { entries["format/compressed/$it"] = "present" }
    tech.optJSONArray("internalFormats").toObjects().forEach { f ->
        val samples = jsonStrings(f.optJSONArray("sampleCounts")?.let { a -> JSONArray().apply { for (i in 0 until a.length()) put(a.opt(i).toString()) } }).joinToString(",").ifBlank { "none" }
        val nv = f.optJSONArray("nvSampleProperties").toObjects().joinToString(";") { n ->
            "${n.optInt("samples")}:${n.optInt("multisamples")}:${n.optInt("supersampleScaleX")}x${n.optInt("supersampleScaleY")}:conformant=${n.optBoolean("conformant")}"
        }.ifBlank { "none" }
        entries["format/internal/${f.optString("target")}/${f.optString("internalFormat")}"] = "${f.optString("status")}; samples=$samples; nv=$nv${f.optString("detail").takeIf { it.isNotBlank() }?.let { "; detail=$it" } ?: ""}"
    }
    jsonStrings(tech.optJSONArray("shaderBinaryFormats")).forEach { entries["format/shader-binary/$it"] = "present" }
    jsonStrings(tech.optJSONArray("programBinaryFormats")).forEach { entries["format/program-binary/$it"] = "present" }
    tech.optJSONArray("precision").toObjects().forEach { x -> entries["precision/${x.optString("shader")}/${x.optString("type")}"] = "range=${x.optInt("rangeMin")}..${x.optInt("rangeMax")}, precision=${x.optInt("precision")}" }
    tech.optJSONArray("eglConfigs").toObjects().forEach { c ->
        val unavailableObjects = c.optJSONArray("unavailableAttributes").toObjects()
        val unavailableByName = unavailableObjects.associateBy { it.optString("name") }
        fun jv(name: String): String {
            if (c.has(name) && !c.isNull(name)) return c.opt(name)?.toString() ?: "Unknown"
            val failure = unavailableByName[name] ?: return "Unknown / not queried"
            val error = failure.optString("error", "Unknown EGL error")
            return if (error.startsWith("Not applicable:", ignoreCase = true)) "Not applicable · ${error.substringAfter(':').trim()}" else "Unavailable · $error"
        }
        fun extv(name: String, extensionName: String, attributeName: String): String {
            if (c.has(name) && !c.isNull(name)) return c.opt(name)?.toString() ?: "Unknown"
            if (extensionName !in technicalEglExtensions) return "Not applicable · $extensionName is not advertised"
            val failure = unavailableByName[attributeName] ?: return "Unknown / not queried"
            return "Unavailable · ${failure.optString("error", "Unknown EGL error")}"
        }
        val unavailable = unavailableObjects.joinToString("|") { "${it.optString("name")}:${it.optString("error")}" }
        entries["eglconfig/${c.optInt("id")}"] = listOf(
            "red=${jv("red")}", "green=${jv("green")}", "blue=${jv("blue")}", "alpha=${jv("alpha")}", "depth=${jv("depth")}", "stencil=${jv("stencil")}",
            "sampleBuffers=${jv("sampleBuffers")}", "samples=${jv("samples")}", "surfaceType=${jv("surfaceType")}", "renderableType=${jv("renderableType")}",
            "conformant=${jv("conformant")}", "configCaveat=${jv("configCaveat")}", "colorBufferType=${jv("colorBufferType")}", "level=${jv("level")}",
            "nativeRenderable=${jv("nativeRenderable")}", "nativeVisualId=${jv("nativeVisualId")}", "minSwapInterval=${jv("minSwapInterval")}", "maxSwapInterval=${jv("maxSwapInterval")}",
            "bufferSize=${jv("bufferSize")}", "luminanceSize=${jv("luminanceSize")}", "alphaMaskSize=${jv("alphaMaskSize")}", "bindToTextureRgb=${jv("bindToTextureRgb")}",
            "bindToTextureRgba=${jv("bindToTextureRgba")}", "maxPbufferWidth=${jv("maxPbufferWidth")}", "maxPbufferHeight=${jv("maxPbufferHeight")}",
            "maxPbufferPixels=${jv("maxPbufferPixels")}", "nativeVisualType=${jv("nativeVisualType")}", "transparentType=${jv("transparentType")}",
            "transparentRed=${jv("transparentRed")}", "transparentGreen=${jv("transparentGreen")}", "transparentBlue=${jv("transparentBlue")}",
            "recordableAndroid=${extv("recordableAndroid", "EGL_ANDROID_recordable", "EGL_RECORDABLE_ANDROID")}",
            "framebufferTargetAndroid=${extv("framebufferTargetAndroid", "EGL_ANDROID_framebuffer_target", "EGL_FRAMEBUFFER_TARGET_ANDROID")}",
            "colorComponentTypeExt=${extv("colorComponentTypeExt", "EGL_EXT_pixel_format_float", "EGL_COLOR_COMPONENT_TYPE_EXT")}",
            "unavailableAttributes=$unavailable"
        ).joinToString(", ")
    }
    tech.optJSONArray("queryDiagnostics").toObjects().forEach { x -> entries["query/${x.optString("name")}"] = "${x.optString("status")}${x.optString("detail").takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}" }
    entries["display/name"] = display.optString("name", "Unavailable")
    entries["display/modeId"] = if (display.isNull("modeId")) "Unavailable" else display.opt("modeId")?.toString() ?: "Unavailable"
    entries["display/resolution"] = if (!display.isNull("width") && !display.isNull("height")) "${display.optInt("width")}x${display.optInt("height")}" else "Unavailable"
    entries["display/refreshRate"] = if (display.isNull("refreshRate")) "Unavailable" else display.opt("refreshRate")?.toString() ?: "Unavailable"
    entries["display/wideColor"] = if (display.isNull("wideColor")) "Unavailable" else display.opt("wideColor")?.toString() ?: "Unavailable"
    entries["display/hdrCapabilityStatus"] = display.optString("hdrCapabilityStatus", "Unavailable")
    jsonStrings(display.optJSONArray("supportedModes")).forEachIndexed { index, v -> entries["display/mode/$index"] = v }
    jsonStrings(display.optJSONArray("hdrTypes")).forEach { entries["display/hdr/$it"] = "present" }
    listOf("desiredMaxLuminance","desiredMaxAverageLuminance","desiredMinLuminance").forEach { k -> entries["display/$k"] = if (display.isNull(k)) "Unavailable" else display.opt(k)?.toString() ?: "Unavailable" }
    if (entries.size > ANALYSIS_MAX_ENTRIES) error("Database report contains too many comparison entries")
    return validateGlAnalysisSnapshot(JSONObject().put("schema", "OpenGLESScopeAnalysisSnapshot1").put("applicationVersion", application.optString("version", "Unknown")).put("entries", JSONObject(entries as Map<*, *>)))
}

private data class CustomMinimumResult(val rule: String, val evidence: String, val state: String)
private const val CUSTOM_MINIMUM_MAX_PROFILES = 16
private const val CUSTOM_MINIMUM_MAX_RULES = 64

private fun loadCustomMinimumProfiles(raw: String?): Map<String, String> = runCatching {
    val root = JSONObject(raw ?: return@runCatching emptyMap())
    if (root.optString("schema") != "OpenGLESScopeMinimumProfiles1") return@runCatching emptyMap()
    val profiles = root.optJSONObject("profiles") ?: return@runCatching emptyMap()
    profiles.keys().asSequence().take(CUSTOM_MINIMUM_MAX_PROFILES).associateWith { profiles.optString(it).take(16 * 1024) }
}.getOrDefault(emptyMap())

private fun encodeCustomMinimumProfiles(profiles: Map<String, String>): String = JSONObject()
    .put("schema", "OpenGLESScopeMinimumProfiles1")
    .put("profiles", JSONObject(profiles))
    .toString()

private fun evaluateCustomMinimumRules(report: GlReport, rulesText: String): List<CustomMinimumResult> {
    val limits = report.limits.associate { it.name to it.value }
    val glExt = report.extensions.toSet()
    val eglExt = (report.egl.extensions + report.egl.clientExtensions).toSet()
    val eglCaps = report.eglCapabilities.associateBy { it.name }
    val glEnumerationComplete = report.diagnostics.firstOrNull { it.name == "GL_EXTENSIONS" }?.status.equals("Available", true)
    val eglDisplayEnumerationComplete = report.diagnostics.firstOrNull { it.name == "EGL_EXTENSIONS" }?.status.equals("Available", true)
    val eglClientEnumerationComplete = report.diagnostics.firstOrNull { it.name == "EGL_NO_DISPLAY/EGL_EXTENSIONS" }?.status.equals("Available", true)
    fun compare(actual: Int, op: String, required: Int): Boolean = when (op) { ">=" -> actual >= required; "<=" -> actual <= required; else -> actual == required }
    return rulesText.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.take(CUSTOM_MINIMUM_MAX_RULES).map { rule ->
        when {
            rule.startsWith("extension:", true) -> {
                val name = rule.substringAfter(':').trim()
                when { name in glExt -> CustomMinimumResult(rule, "Runtime enumerated", "PASS"); glEnumerationComplete -> CustomMinimumResult(rule, "Not enumerated in complete GL extension evidence", "FAIL"); else -> CustomMinimumResult(rule, "GL extension enumeration incomplete/unavailable", "UNKNOWN") }
            }
            rule.startsWith("egl-extension:", true) -> {
                val name = rule.substringAfter(':').trim()
                when { name in eglExt -> CustomMinimumResult(rule, "Runtime enumerated", "PASS"); eglDisplayEnumerationComplete && eglClientEnumerationComplete -> CustomMinimumResult(rule, "Not enumerated in complete EGL display/client evidence", "FAIL"); else -> CustomMinimumResult(rule, "EGL extension enumeration incomplete/unavailable", "UNKNOWN") }
            }
            rule.startsWith("egl-capability:", true) -> {
                val body = rule.substringAfter(':').trim(); val parts = body.split('=', limit = 2); val name = parts[0].trim(); val expected = parts.getOrNull(1)?.trim()?.ifBlank { "Available" } ?: "Available"; val cap = eglCaps[name]
                if (cap == null) CustomMinimumResult(rule, "Capability not queried/reported", "UNKNOWN") else CustomMinimumResult(rule, "${cap.status}${if (cap.value.isBlank()) "" else " · ${cap.value}"}", if (cap.status.equals(expected, true)) "PASS" else if (cap.status.equals("Unknown", true) || cap.status.equals("Unavailable", true)) "UNKNOWN" else "FAIL")
            }
            rule.startsWith("api", true) -> {
                val m = Regex("(?i)^api\\s*(>=|<=|=)\\s*(\\d+)\\.(\\d+)$").matchEntire(rule)
                if (m == null) CustomMinimumResult(rule, "Invalid API rule", "UNKNOWN") else { val required = m.groupValues[2].toInt() * 100 + m.groupValues[3].toInt(); val actual = report.glMajor * 100 + report.glMinor; CustomMinimumResult(rule, "${report.glMajor}.${report.glMinor}", if (compare(actual, m.groupValues[1], required)) "PASS" else "FAIL") }
            }
            rule.startsWith("egl", true) -> {
                val m = Regex("(?i)^egl\\s*(>=|<=|=)\\s*(\\d+)\\.(\\d+)$").matchEntire(rule)
                val actualMatch = Regex("(\\d+)\\.(\\d+)").find(report.egl.initializedVersion)
                if (m == null || actualMatch == null) CustomMinimumResult(rule, report.egl.initializedVersion.ifBlank { "Unavailable" }, "UNKNOWN") else { val required = m.groupValues[2].toInt() * 100 + m.groupValues[3].toInt(); val actual = actualMatch.groupValues[1].toInt() * 100 + actualMatch.groupValues[2].toInt(); CustomMinimumResult(rule, report.egl.initializedVersion, if (compare(actual, m.groupValues[1], required)) "PASS" else "FAIL") }
            }
            else -> {
                val m = Regex("^([A-Z0-9_]+)\\s*(>=|<=|=)\\s*(-?\\d+(?:\\.\\d+)?)$").matchEntire(rule)
                if (m == null) CustomMinimumResult(rule, "Invalid rule syntax", "UNKNOWN") else { val name=m.groupValues[1]; val op=m.groupValues[2]; val required=m.groupValues[3].toDoubleOrNull(); val raw=limits[name]; val evidence=raw ?: "Unavailable"; val actual=numericAnalysisValue(raw); if (required == null || actual == null) CustomMinimumResult(rule, evidence, "UNKNOWN") else { val pass=when(op){">="->actual>=required;"<="->actual<=required;else->actual==required}; CustomMinimumResult(rule, evidence, if(pass)"PASS" else "FAIL") } }
            }
        }
    }
}

private suspend fun fetchDatabaseAnalysisSnapshot(context: Context, id: String): JSONObject = withContext(Dispatchers.IO) {
    if (!id.matches(Regex("[a-f0-9]{64}"))) error("Report ID must be exactly 64 lowercase hexadecimal characters")
    if (!hasValidatedInternet(context)) error("Validated internet access is unavailable")
    val base = DATABASE_API.toHttpUrlOrNull() ?: error("Official Database endpoint is invalid")
    val url = base.newBuilder().addPathSegments("v1/reports/$id").build()
    DATABASE_HTTP_CLIENT.newCall(Request.Builder().url(url).get().header("Accept", "application/json").build()).execute().use { response ->
        val body = readResponseTextLimited(response.body, 2 * 1024 * 1024)
        if (!response.isSuccessful) error(runCatching { JSONObject(body).optString("error") }.getOrDefault("").ifBlank { "HTTP ${response.code}" })
        databasePayloadToAnalysisSnapshot(JSONObject(body))
    }
}

private fun flattenJsonEvidence(value: Any?, prefix: String = "", out: LinkedHashMap<String, String> = linkedMapOf()): LinkedHashMap<String, String> {
    if (out.size >= ANALYSIS_MAX_ENTRIES) return out
    when (value) {
        is JSONObject -> value.keys().asSequence().toList().sorted().forEach { key -> flattenJsonEvidence(value.opt(key), if (prefix.isBlank()) key else "$prefix/$key", out) }
        is JSONArray -> for (i in 0 until value.length()) flattenJsonEvidence(value.opt(i), "$prefix/$i", out)
        JSONObject.NULL, null -> out[prefix] = "null"
        else -> out[prefix] = value.toString().take(ANALYSIS_MAX_VALUE_LENGTH)
    }
    return out
}

private fun MainActivity.shareText(text: String) {
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share"))
}

private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 250
private const val ENCYCLOPEDIA_MAX_QUERY_LENGTH = 160

private fun encyclopediaEntryMatches(entry: RegistryCatalogEntry, terms: List<String>): Boolean =
    terms.all { term -> term in entry.searchText }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RegistryEncyclopediaPage(activity: MainActivity, report: GlReport, initialQuery: String = "", onSeedConsumed: () -> Unit = {}) {
    val context = activity as Context
    var catalog by remember { mutableStateOf<List<RegistryCatalogEntry>>(emptyList()) }
    var catalogLoading by remember { mutableStateOf(true) }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(initialQuery) { if (initialQuery.isNotBlank()) { query = initialQuery.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = 0; onSeedConsumed() } }
    var apiFilter by rememberSaveable { mutableStateOf("All") }
    var category by rememberSaveable { mutableStateOf("All") }
    var runtimeFilter by rememberSaveable { mutableStateOf("All") }
    LaunchedEffect(Unit) {
        catalogLoading = true
        try {
            catalog = withContext(Dispatchers.IO) { RegistryCatalog.load(context) }
            catalogError = null
            catalogLoading = false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            catalog = emptyList()
            catalogError = error.message?.take(240) ?: error.javaClass.simpleName
            catalogLoading = false
        }
    }
    val runtimeGl = remember(report.extensions) { report.extensions.toSet() }
    val runtimeEgl = remember(report.egl.extensions, report.egl.clientExtensions) { (report.egl.extensions + report.egl.clientExtensions).toSet() }
    val categoryKinds = remember(category) {
        when (category) {
            "Extensions" -> setOf("extension")
            "Core versions" -> setOf("core-version")
            "Commands" -> setOf("command")
            "Tokens" -> setOf("token")
            "Types" -> setOf("type")
            else -> emptySet()
        }
    }
    val trimmedQuery = query.trim().take(ENCYCLOPEDIA_MAX_QUERY_LENGTH)
    val largeFamilyNeedsQuery = category in setOf("Commands", "Tokens", "Types") && trimmedQuery.length < 2
    val terms = remember(trimmedQuery) { trimmedQuery.lowercase(java.util.Locale.ROOT).split(Regex("\\s+")).filter { it.isNotBlank() } }
    val boundedMatches = remember(catalog, terms, apiFilter, categoryKinds, runtimeFilter, runtimeGl, runtimeEgl, largeFamilyNeedsQuery) {
        if (largeFamilyNeedsQuery) emptyList() else catalog.asSequence().filter { entry ->
            val runtimeState = when {
                entry.kind != "extension" -> "Reference"
                entry.api == "OpenGL ES" && entry.name in runtimeGl -> "Runtime"
                entry.api == "EGL" && entry.name in runtimeEgl -> "Runtime"
                else -> "Not enumerated"
            }
            (apiFilter == "All" || entry.api == apiFilter) &&
                (categoryKinds.isEmpty() || entry.kind in categoryKinds) &&
                (runtimeFilter == "All" || runtimeState == runtimeFilter) &&
                encyclopediaEntryMatches(entry, terms)
        }.take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1).toList()
    }
    val truncated = boundedMatches.size > ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT
    val matchedEntries = if (truncated) boundedMatches.take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT) else boundedMatches
    val pageCount = maxOf(1, (matchedEntries.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(trimmedQuery, apiFilter, category, runtimeFilter) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val safePage = page.coerceIn(0, pageCount - 1)
    val visibleEntries = remember(matchedEntries, safePage) { matchedEntries.drop(safePage * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    val categories = listOf("All", "Extensions", "Core versions", "Commands", "Tokens", "Types")
    val apis = listOf("All", "OpenGL ES", "EGL")
    val runtimeStates = listOf("All", "Runtime", "Not enumerated", "Reference")

    OpenGLESScopeLazyPage(verticalSpacing = 12.dp) {
        item {
            CapabilitySectionCard("Encyclopedia") {
                Text("A local, offline OpenGL® ES™ / EGL™ reference generated from the release-locked Khronos gl.xml and egl.xml registries. Browsing this page never performs a network request.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                ExpressiveMetricGrid(listOf(
                    "REGISTRY" to if (catalogLoading) "Loading…" else catalog.size.toString(),
                    "GL EXTENSIONS" to runtimeGl.size.toString(),
                    "EGL EXTENSIONS" to runtimeEgl.size.toString(),
                    "STATUS" to when { catalogLoading -> "Loading"; catalogError != null -> "Unavailable"; else -> "Ready" }
                ))
                Surface(shape = MaterialTheme.shapes.large, color = SurfaceLow, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.28f)), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(painterResource(R.drawable.ic_shield), contentDescription = null, tint = BrandSoft, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Evidence boundary", color = TextPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("Registry presence, ownership, aliases and command/token definitions are reference metadata. They never substitute for runtime support or query evidence from the active OpenGL® ES™ / EGL™ implementation.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        item {
            CapabilitySectionCard("Reference search") {
                ExpressiveSearchField(
                    value = query,
                    onValueChange = { query = it.take(ENCYCLOPEDIA_MAX_QUERY_LENGTH); page = 0 },
                    placeholderText = "Search GL_MAX_TEXTURE_SIZE, glBindTexture, EGL_KHR_…",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
                )
                Text("Category", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                ExpressiveFilterBar(categories, categories.indexOf(category).coerceAtLeast(0)) { category = categories[it]; page = 0 }
                Text("API", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                ExpressiveFilterBar(apis, apis.indexOf(apiFilter).coerceAtLeast(0)) { apiFilter = apis[it]; page = 0 }
                Text("Runtime state", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                ExpressiveFilterBar(runtimeStates, runtimeStates.indexOf(runtimeFilter).coerceAtLeast(0)) { runtimeFilter = runtimeStates[it]; page = 0 }
                ExpressiveMetricGrid(listOf(
                    "CATEGORY" to category,
                    "MATCHES" to matchedEntries.size.toString(),
                    "PAGE" to "${safePage + 1} / $pageCount"
                ))
                Text(
                    when {
                        catalogError != null -> "The local registry catalog could not be loaded. The Encyclopedia stays open instead of terminating the app."
                        catalogLoading -> "Loading the bounded local registry index…"
                        largeFamilyNeedsQuery -> "Type at least 2 characters before searching this large symbol family. This prevents the 5,000+ entry registry from becoming an unbounded Compose workload."
                        truncated -> "Showing a paged view of the first $ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT matches. Refine the query for a narrower result."
                        category == "All" && trimmedQuery.isBlank() -> "${matchedEntries.size} bounded reference entries match this view. Search or choose a category for a targeted view."
                        else -> "${matchedEntries.size} matching reference entr${if (matchedEntries.size == 1) "y" else "ies"}."
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        item {
            CapabilitySectionCard("How to read encyclopedia entries") {
                CapabilityKeyValue("Title", "Exact Khronos registry symbol, extension or core-version name")
                CapabilityKeyValue("API", "OpenGL® ES™ or EGL™ registry provenance")
                CapabilityKeyValue("Kind", "Command, token, type, extension or core-version reference family")
                CapabilityKeyValue("Runtime", "Shown only for extension entries and based on exact runtime enumeration")
                CapabilityKeyValue("Definition", "Registry declaration text or generated signature metadata; never synthesized runtime support")
                Text("Unknown future enum values remain numeric evidence. An extension listed in the registry but not enumerated by this runtime is reference metadata, not proof that the device lacks every capability related to that extension family.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (!catalogLoading && catalogError == null) {
            stickyCollectionPager(matchedEntries.size, safePage, { page = it.coerceIn(0, pageCount - 1) })
        }
        catalogError?.let { error ->
            item {
                CapabilityItemCard {
                    Text("Encyclopedia unavailable", color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(error, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (catalogLoading) {
            item {
                CapabilityItemCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LoadingIndicator(color = BrandSoft, modifier = Modifier.size(30.dp))
                        Text("Loading Khronos registry reference…", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else if (catalogError == null && visibleEntries.isEmpty()) {
            item { EmptyState(if (largeFamilyNeedsQuery) "Search input is intentionally bounded before the large symbol index is touched." else "No matching local OpenGL® ES™ / EGL™ reference entry.") }
        } else if (catalogError == null) {
            items(visibleEntries, key = { "${it.api}:${it.kind}:${it.name}" }) { entry ->
                val runtimeState = when {
                    entry.kind != "extension" -> "Registry reference"
                    entry.api == "OpenGL ES" && entry.name in runtimeGl -> "Runtime enumerated"
                    entry.api == "EGL" && entry.name in runtimeEgl -> "Runtime enumerated"
                    else -> "Not enumerated by current runtime"
                }
                CapabilityItemCard {
                    Text(entry.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    Text("${entry.api} · ${entry.kind}", color = BrandSoft, style = MaterialTheme.typography.labelSmall)
                    if (entry.kind == "extension") CapabilityKeyValue("Runtime", runtimeState)
                    if (entry.value.isNotBlank()) CapabilityKeyValue("Value", entry.value)
                    if (entry.alias.isNotBlank()) CapabilityKeyValue("Alias", entry.alias)
                    if (entry.group.isNotBlank()) CapabilityKeyValue("Group", entry.group)
                    if (entry.signature.isNotBlank()) CapabilityKeyValue("Signature", entry.signature)
                    if (entry.definition.isNotBlank()) CapabilityKeyValue("Definition", entry.definition)
                    if (entry.owners.isNotEmpty()) CapabilityKeyValue("Registry owners", entry.owners.joinToString(", "))
                }
            }
        }
    }
}

private fun formatAnalysisElapsedTime(elapsedMs: Long): String =
    String.format(java.util.Locale.ROOT, "%.3f s (%d ms)", elapsedMs.coerceAtLeast(0L) / 1000.0, elapsedMs.coerceAtLeast(0L))

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnalysisPage(activity: MainActivity, report: GlReport, display: DisplayInfo, collectionElapsedMs: Long?, startupGateDelayMs: Long?) {
    val context = activity as Context
    val networkAvailable = LocalValidatedNetwork.current
    val scope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(0) }
    val tabLabels = listOf("Compare", "Search", "Diagnostics", "Requirements", "Minimums", "Graph", "Presentation", "Raw JSON", "Database", "History", "Watched", "Quality", "Share", "Tests")
    var globalQuery by rememberSaveable { mutableStateOf("") }
    var globalKindFilter by rememberSaveable { mutableStateOf("All") }
    var diagnosticQuery by rememberSaveable { mutableStateOf("") }
    var diagnosticStateFilter by rememberSaveable { mutableStateOf("All") }
    var baseline by remember { mutableStateOf<JSONObject?>(null) }
    var importStatus by remember { mutableStateOf("No baseline imported") }
    var includeUnchanged by rememberSaveable { mutableStateOf(false) }
    var diffQuery by rememberSaveable { mutableStateOf("") }
    var diffStateFilter by rememberSaveable { mutableStateOf("All") }
    var diffKindFilter by rememberSaveable { mutableStateOf("All") }
    var databaseLookupMode by rememberSaveable { mutableStateOf(0) }
    var databaseListQuery by rememberSaveable { mutableStateOf("") }
    var databaseListPage by rememberSaveable { mutableIntStateOf(0) }
    var databaseId by rememberSaveable { mutableStateOf("") }
    var databaseBaseline by remember { mutableStateOf<JSONObject?>(null) }
    var databaseStatus by remember { mutableStateOf("Enter a 64-hex report ID or browse recent reports") }
    var databaseRunning by remember { mutableStateOf(false) }
    var databaseListStatus by remember { mutableStateOf("Recent reports have not been loaded") }
    var databaseListRunning by remember { mutableStateOf(false) }
    var databaseRows by remember { mutableStateOf<List<AnalysisDatabaseRow>>(emptyList()) }
    var databaseNextCursor by remember { mutableStateOf<AnalysisReportCursor?>(null) }
    var minimumQuery by rememberSaveable { mutableStateOf("") }
    var minimumStateFilter by rememberSaveable { mutableStateOf("All") }
    var graphQuery by rememberSaveable { mutableStateOf("") }
    var graphRoot by rememberSaveable { mutableStateOf("GL_EXT_texture_filter_anisotropic") }
    var graphDepth by rememberSaveable { mutableStateOf(2) }
    var rawQuery by rememberSaveable { mutableStateOf("") }
    var historyRevision by remember { mutableStateOf(0) }
    var history by remember { mutableStateOf<List<AnalysisHistoryRecord>>(emptyList()) }
    var pendingHistoryDelete by remember { mutableStateOf<AnalysisHistoryRecord?>(null) }
    var pendingHistoryDeleteAll by remember { mutableStateOf(false) }
    var pendingWatchDelete by remember { mutableStateOf<String?>(null) }
    var pendingWatchDeleteAll by remember { mutableStateOf(false) }
    var pendingMinimumProfileSave by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingMinimumProfileLoad by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingMinimumProfileDelete by remember { mutableStateOf<String?>(null) }
    var watchInput by rememberSaveable { mutableStateOf("") }
    var watchQuery by rememberSaveable { mutableStateOf("") }
    var watchStateFilter by rememberSaveable { mutableStateOf("All") }
    val prefs = remember { context.getSharedPreferences("analysis_tools", Context.MODE_PRIVATE) }
    var watched by remember { mutableStateOf((prefs.getStringSet("watched", emptySet())?.toSet() ?: emptySet()).take(ANALYSIS_MAX_WATCHED).toSet()) }
    var minimumProfiles by remember { mutableStateOf(loadCustomMinimumProfiles(prefs.getString("minimum_profiles_v1", null))) }
    var minimumProfileName by rememberSaveable { mutableStateOf("") }
    var minimumProfileRules by rememberSaveable { mutableStateOf("# OpenGLESScopeMinimumProfile1\napi >= 3.2\negl >= 1.5\nextension:GL_KHR_debug\negl-extension:EGL_KHR_create_context") }
    var minimumProfileStatus by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<JSONObject?>(null) }
    var testRunning by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf("") }
    val currentSnapshot = remember(report, display) { glAnalysisSnapshot(report, display) }
    val currentFlat = remember(currentSnapshot) { flattenGlSnapshot(currentSnapshot) }
    var rawTechnicalEntries by remember(report, display) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var rawTechnicalStatus by remember(report, display) { mutableStateOf("Not loaded") }
    LaunchedEffect(report, display, mode) {
        if (tabLabels.getOrNull(mode) != "Raw JSON") return@LaunchedEffect
        rawTechnicalStatus = "Preparing bounded technical evidence…"
        try {
            val decoded = withContext(Dispatchers.Default) {
                flattenJsonEvidence(submissionJson(context, report, display).getJSONObject("technicalReport"))
            }
            rawTechnicalEntries = decoded
            rawTechnicalStatus = "Loaded ${decoded.size} evidence leaves"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            rawTechnicalEntries = emptyMap()
            rawTechnicalStatus = "Raw evidence unavailable: ${error.message?.take(160) ?: "serialization failure"}"
        }
    }

    LaunchedEffect(currentSnapshot) {
        if (!report.available) return@LaunchedEffect
        val fingerprint = withContext(Dispatchers.Default) { stableSnapshotFingerprint(currentSnapshot) }
        if (prefs.getString("history_last_fingerprint", "") != fingerprint) {
            withContext(Dispatchers.IO) { runCatching { saveAnalysisHistory(context, currentSnapshot, report.renderer) } }
                .onSuccess { prefs.edit().putString("history_last_fingerprint", fingerprint).apply(); historyRevision++ }
        }
    }
    LaunchedEffect(historyRevision) {
        history = withContext(Dispatchers.IO) { listAnalysisHistory(context) }
    }

    var analysisStorageAction by remember { mutableStateOf<AnalysisStorageAction?>(null) }
    analysisStorageAction?.let { action ->
        val request = when (action) {
            AnalysisStorageAction.IMPORT_SNAPSHOT -> SharedStorageBrowserRequest(
                title = "Import analysis snapshot",
                description = "Choose a bounded OpenGLESScope analysis JSON snapshot from shared storage.",
                mode = SharedStorageBrowserMode.IMPORT,
                allowedExtensions = setOf("json"),
                maxImportBytes = ANALYSIS_MAX_SNAPSHOT_BYTES.toLong()
            )
            AnalysisStorageAction.EXPORT_SNAPSHOT -> SharedStorageBrowserRequest(
                title = "Export analysis snapshot",
                description = "Choose a shared-storage folder and file name for the validated analysis snapshot.",
                mode = SharedStorageBrowserMode.EXPORT,
                allowedExtensions = setOf("json"),
                suggestedFileName = "OpenGLESScope-${safeFilePart(report.renderer.ifBlank { "Unknown-GPU" })}-analysis.json"
            )
            AnalysisStorageAction.IMPORT_MINIMUM_PROFILE -> SharedStorageBrowserRequest(
                title = "Import minimum profile",
                description = "Choose an OpenGLESScope minimum-profile JSON file. Schema and rule bounds are validated before use.",
                mode = SharedStorageBrowserMode.IMPORT,
                allowedExtensions = setOf("json"),
                maxImportBytes = 256L * 1024L
            )
            AnalysisStorageAction.EXPORT_MINIMUM_PROFILE -> SharedStorageBrowserRequest(
                title = "Export minimum profile",
                description = "Choose a shared-storage folder for the bounded OpenGLESScope minimum-profile JSON.",
                mode = SharedStorageBrowserMode.EXPORT,
                allowedExtensions = setOf("json"),
                suggestedFileName = "OpenGLESScope-${safeFilePart(minimumProfileName.ifBlank { "profile" })}-minimum.json"
            )
            AnalysisStorageAction.EXPORT_TECHNICAL_REPORT -> SharedStorageBrowserRequest(
                title = "Export technicalReport JSON",
                description = "Choose a shared-storage folder for the exact bounded technicalReport JSON used by Database submission.",
                mode = SharedStorageBrowserMode.EXPORT,
                allowedExtensions = setOf("json"),
                suggestedFileName = "OpenGLESScope-${safeFilePart(report.renderer.ifBlank { "Unknown-GPU" })}-technicalReport.json"
            )
        }
        SharedStorageBrowserDialog(
            request = request,
            onDismiss = { analysisStorageAction = null },
            onImport = { file ->
                when (action) {
                    AnalysisStorageAction.IMPORT_SNAPSHOT -> try {
                        val snapshot = withContext(Dispatchers.IO) {
                            val selected = validatedSharedStorageImportFile(file, setOf("json"), ANALYSIS_MAX_SNAPSHOT_BYTES.toLong())
                            val bytes = FileInputStream(selected).use { input -> readBoundedAnalysisBytes(input, ANALYSIS_MAX_SNAPSHOT_BYTES) }
                            validateGlAnalysisSnapshot(JSONObject(bytes.toString(Charsets.UTF_8)))
                        }
                        baseline = snapshot
                        importStatus = "Baseline imported · ${flattenGlSnapshot(snapshot)["identity/renderer"] ?: "Unknown GPU"}"
                        Result.success("Imported ${file.name}")
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Throwable) {
                        importStatus = error.message ?: "Import failed"
                        Result.failure(error)
                    }
                    AnalysisStorageAction.IMPORT_MINIMUM_PROFILE -> try {
                        val imported = withContext(Dispatchers.IO) {
                            val selected = validatedSharedStorageImportFile(file, setOf("json"), 256L * 1024L)
                            val bytes = FileInputStream(selected).use { input -> readBoundedAnalysisBytes(input, 256 * 1024) }
                            val root = JSONObject(bytes.toString(Charsets.UTF_8))
                            if (root.optString("schema") != "OpenGLESScopeMinimumProfile1") error("Unsupported minimum profile schema")
                            val name = root.optString("name").trim()
                            val array = root.optJSONArray("rules") ?: error("Minimum profile rules are missing")
                            if (name.isBlank() || name.length > 128 || array.length() !in 1..CUSTOM_MINIMUM_MAX_RULES) error("Minimum profile bounds are invalid")
                            val rules = (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }
                            if (rules.size != array.length() || rules.any { it.length > 512 }) error("Minimum profile contains an invalid rule")
                            name to rules.joinToString("\n")
                        }
                        minimumProfileName = imported.first
                        minimumProfileRules = imported.second
                        minimumProfileStatus = "Minimum profile imported · ${imported.first}"
                        Result.success("Minimum profile imported")
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Throwable) {
                        minimumProfileStatus = error.message ?: "Minimum profile import failed"
                        Result.failure(error)
                    }
                    else -> Result.failure(IllegalStateException("This action does not import files"))
                }
            },
            onExport = { destination ->
                when (action) {
                    AnalysisStorageAction.EXPORT_SNAPSHOT -> try {
                        val bytes = withContext(Dispatchers.Default) {
                            currentSnapshot.toString(2).toByteArray(Charsets.UTF_8).also { payload ->
                                if (payload.size > ANALYSIS_MAX_SNAPSHOT_BYTES) error("Snapshot exceeds ${ANALYSIS_MAX_SNAPSHOT_BYTES / (1024 * 1024)} MiB; no evidence was truncated")
                            }
                        }
                        val saved = withContext(Dispatchers.IO) {
                            val target = validatedSharedStorageDestination(destination.parentFile ?: error("Destination folder is unavailable"), destination.name, setOf("json"))
                            writeSharedStorageBytes(target, bytes, ANALYSIS_MAX_SNAPSHOT_BYTES)
                            target
                        }
                        exportStatus = "Snapshot exported · ${saved.absolutePath}"
                        Result.success("Saved ${saved.name}")
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Throwable) {
                        exportStatus = error.message ?: "Snapshot export failed"
                        Result.failure(error)
                    }
                    AnalysisStorageAction.EXPORT_MINIMUM_PROFILE -> try {
                        val rules = minimumProfileRules.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.take(CUSTOM_MINIMUM_MAX_RULES + 1).toList()
                        val name = minimumProfileName.trim()
                        if (rules.isEmpty() || rules.size > CUSTOM_MINIMUM_MAX_RULES || rules.any { it.length > 512 }) error("Minimum profile must contain 1..$CUSTOM_MINIMUM_MAX_RULES bounded rules")
                        if (name.isBlank() || name.length > 128) error("Minimum profile name is required and must be at most 128 characters")
                        val bytes = JSONObject().put("schema", "OpenGLESScopeMinimumProfile1").put("name", name).put("rules", JSONArray(rules)).toString(2).toByteArray(Charsets.UTF_8)
                        val saved = withContext(Dispatchers.IO) {
                            val target = validatedSharedStorageDestination(destination.parentFile ?: error("Destination folder is unavailable"), destination.name, setOf("json"))
                            writeSharedStorageBytes(target, bytes, 256 * 1024)
                            target
                        }
                        minimumProfileStatus = "Minimum profile exported · ${saved.absolutePath}"
                        Result.success("Saved ${saved.name}")
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Throwable) {
                        minimumProfileStatus = error.message ?: "Minimum profile export failed"
                        Result.failure(error)
                    }
                    AnalysisStorageAction.EXPORT_TECHNICAL_REPORT -> try {
                        val bytes = withContext(Dispatchers.Default) {
                            submissionJson(context, report, display).getJSONObject("technicalReport").toString(2).toByteArray(Charsets.UTF_8).also { payload ->
                                if (payload.size > ANALYSIS_MAX_SNAPSHOT_BYTES) error("Structured technical report exceeds the 8 MiB local export bound")
                            }
                        }
                        val saved = withContext(Dispatchers.IO) {
                            val target = validatedSharedStorageDestination(destination.parentFile ?: error("Destination folder is unavailable"), destination.name, setOf("json"))
                            writeSharedStorageBytes(target, bytes, ANALYSIS_MAX_SNAPSHOT_BYTES)
                            target
                        }
                        exportStatus = "Structured technicalReport exported · ${saved.absolutePath}"
                        Result.success("Saved ${saved.name}")
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Throwable) {
                        exportStatus = error.message ?: "Structured technicalReport export failed"
                        Result.failure(error)
                    }
                    else -> Result.failure(IllegalStateException("This action does not export files"))
                }
            }
        )
    }

    val graphRuntimeExtensions = remember(report.extensions, report.egl.extensions, report.egl.clientExtensions) {
        (report.extensions + report.egl.extensions + report.egl.clientExtensions).toSet()
    }
    val graphDiagnosticByName = remember(report.diagnostics) { report.diagnostics.associateBy { it.name } }
    val graphRows = remember(graphQuery) {
        QUERY_DEPENDENCIES.entries.filter { (extension, queries) ->
            graphQuery.isBlank() || extension.contains(graphQuery, true) || queries.any { it.contains(graphQuery, true) }
        }
    }
    val graphSelectedRoot = remember(graphRoot, graphRows) {
        graphRoot.trim().takeIf { QUERY_DEPENDENCIES.containsKey(it) } ?: graphRows.firstOrNull()?.key
    }
    val graphSelectedQueries = remember(graphSelectedRoot) {
        graphSelectedRoot?.let { QUERY_DEPENDENCIES[it].orEmpty() }.orEmpty()
    }
    val graphRegistryReferences = remember(context, graphSelectedRoot) {
        if (graphSelectedRoot == null) emptyList()
        else runCatching {
            RegistryCatalog.load(context)
                .filter { it.name == graphSelectedRoot || graphSelectedRoot in it.owners }
                .take(64)
        }.getOrDefault(emptyList())
    }

    val databaseComparisonRows = remember(databaseBaseline, currentSnapshot, includeUnchanged) {
        databaseBaseline?.let { baselineSnapshot ->
            glSnapshotDiff(baselineSnapshot, currentSnapshot)
                .filter { row -> row.state != "Unchanged" || includeUnchanged }
        }.orEmpty()
    }

    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard("Analysis workspace") {
                Text("Comparison, global evidence search, query diagnostics, requirements, Database lookup, custom minimums, registry/query graphs, raw evidence, session history, watched evidence, integrity scoring and active tests preserve OpenGLESScope's evidence-state boundary. Registry metadata is never promoted into runtime support.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                ExpressiveFilterBar(tabLabels, mode) { mode = it }
            }
        }
        when (tabLabels.getOrNull(mode) ?: "Compare") {
            "Compare" -> {
                item { CapabilitySectionCard("Offline report compare") {
                    CapabilityKeyValue("Baseline", importStatus)
                    if (exportStatus.isNotBlank()) CapabilityKeyValue("Export", exportStatus)
                    Text("Analysis JSON exchange uses OpenGLESScope's in-app shared-storage browser; storage access is requested only after an explicit import/export action.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    SharedStoragePermissionActionButton("Import analysis snapshot", "Open the in-app shared-storage browser · JSON · 8 MiB bound", R.drawable.ic_action_import) {
                        analysisStorageAction = AnalysisStorageAction.IMPORT_SNAPSHOT
                    }
                    SharedStoragePermissionActionButton("Export analysis snapshot", "Choose a shared-storage folder and JSON file name", R.drawable.ic_export) {
                        analysisStorageAction = AnalysisStorageAction.EXPORT_SNAPSHOT
                    }
                    if (baseline != null) {
                        ExpressiveSearchField(value = diffQuery, onValueChange = { diffQuery = it }, placeholderText = "Search diff evidence…", modifier = Modifier.fillMaxWidth())
                        val diffStates = listOf("All", "Added", "Removed", "Changed", "Regression")
                        ExpressiveFilterBar(diffStates, diffStates.indexOf(diffStateFilter).coerceAtLeast(0)) { diffStateFilter = diffStates[it] }
                        val diffKinds = listOf("All", "identity", "egl-runtime", "egl-capability", "limit", "extension", "format", "precision", "eglconfig", "query", "display")
                        ExpressiveFilterBar(diffKinds, diffKinds.indexOf(diffKindFilter).coerceAtLeast(0)) { diffKindFilter = diffKinds[it] }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { ExpressiveSwitch(includeUnchanged) { includeUnchanged = it }; Text("Show unchanged", style = MaterialTheme.typography.bodySmall) }
                    }
                } }
                val allRows = baseline?.let { glSnapshotDiff(it, currentSnapshot) }.orEmpty()
                val rows = allRows.filter { row ->
                    (includeUnchanged || row.state != "Unchanged") && matchesGlAnalysisQuery(row, diffQuery) &&
                        (diffKindFilter == "All" || row.kind == diffKindFilter) && when (diffStateFilter) {
                        "Added" -> row.state == "Added"; "Removed" -> row.state.startsWith("Removed"); "Changed" -> row.state.startsWith("Changed"); "Regression" -> row.state.contains("regression candidate"); else -> true
                    }
                }
                if (baseline != null) {
                    item { CapabilitySectionCard("Diff summary") {
                        CapabilityKeyValue("Baseline fields", flattenGlSnapshot(baseline!!).size.toString()); CapabilityKeyValue("Current fields", currentFlat.size.toString())
                        CapabilityKeyValue("Added", allRows.count { it.state == "Added" }.toString()); CapabilityKeyValue("Removed", allRows.count { it.state.startsWith("Removed") }.toString()); CapabilityKeyValue("Changed", allRows.count { it.state.startsWith("Changed") }.toString()); CapabilityKeyValue("Regression candidates", allRows.count { it.state.contains("regression candidate") }.toString())
                        Text("A regression candidate requires direct comparable evidence; it is not a conformance or driver-bug verdict.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    } }
                    items(rows, key = { it.key }) { row -> CapabilityItemCard { CapabilityKeyValue(row.key, row.state); row.baseline?.let { CapabilityKeyValue("Baseline", it) }; row.current?.let { CapabilityKeyValue("Current", it) } } }
                }
            }
            "Search" -> {
                val kinds = listOf("All") + currentFlat.keys.map(::analysisKind).distinct().sorted()
                val matching = currentFlat.entries.asSequence().filter { (key, value) ->
                    (globalKindFilter == "All" || analysisKind(key).equals(globalKindFilter, true)) &&
                        (globalQuery.isBlank() || analysisQueryTokens(globalQuery).all { token -> key.contains(token, true) || value.contains(token, true) })
                }.toList()
                val filtered = matching.take(ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT)
                item { CapabilitySectionCard("Global OpenGL® ES™/EGL™ report search") {
                    Text("Searches the current structured analysis evidence locally. Results are exact report evidence; registry-only metadata is not mixed into this view.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    ExpressiveSearchField(value = globalQuery, onValueChange = { globalQuery = it.take(ANALYSIS_MAX_KEY_LENGTH) }, modifier = Modifier.fillMaxWidth(), placeholderText = "Search key or value…")
                    ExpressiveFilterBar(kinds, kinds.indexOf(globalKindFilter).coerceAtLeast(0)) { globalKindFilter = kinds[it] }
                    ExpressiveMetricGrid(listOf(
                        "Evidence fields" to currentFlat.size.toString(),
                        "Matching entries" to matching.size.toString(),
                        "Rendered" to filtered.size.toString(),
                        "Visible result bound" to ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT.toString()
                    ))
                    if (matching.size > filtered.size) Text("${matching.size - filtered.size} additional matching evidence field(s) are intentionally not composed; refine the local search to inspect them without unbounded UI work.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                items(filtered, key = { "search:${it.key}" }) { (key, value) -> CapabilityItemCard { CapabilityKeyValue(key, value); CapabilityKeyValue("Kind", analysisKind(key)) } }
            }
            "Diagnostics" -> {
                val states = listOf("All", "Available", "Unavailable", "Not applicable", "Unknown")
                val filtered = report.diagnostics.filter { d ->
                    (diagnosticStateFilter == "All" || d.status.equals(diagnosticStateFilter, true)) &&
                        (diagnosticQuery.isBlank() || d.name.contains(diagnosticQuery, true) || d.detail.contains(diagnosticQuery, true))
                }
                item { CapabilitySectionCard("Diagnostic collection") {
                    Text("Every shown row is collector/query evidence. Unavailable and Unknown remain explicit states and are never converted into Unsupported capability claims.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Renderer", presentedRendererName(report.vendor, report.renderer).ifBlank { "Unavailable" })
                    CapabilityKeyValue("Vendor", presentedGlVendor(report.vendor, report.renderer).ifBlank { "Unavailable" })
                    CapabilityKeyValue("OpenGL® ES™", report.glVersion.ifBlank { "Unavailable" })
                    CapabilityKeyValue("GLSL ES", report.glslVersion.ifBlank { "Unavailable" })
                    CapabilityKeyValue("EGL™ initialized", report.egl.initializedVersion.ifBlank { "Unavailable" })
                    CapabilityKeyValue("Diagnostics", report.diagnostics.size.toString())
                    CapabilityKeyValue("Available", report.diagnostics.count { it.status.equals("Available", true) }.toString())
                    CapabilityKeyValue("Unavailable", report.diagnostics.count { it.status.equals("Unavailable", true) }.toString())
                    CapabilityKeyValue("Not applicable", report.diagnostics.count { it.status.equals("Not applicable", true) }.toString())
                    CapabilityKeyValue("Unknown", report.diagnostics.count { it.status.equals("Unknown", true) }.toString())
                    ExpressiveSearchField(value = diagnosticQuery, onValueChange = { diagnosticQuery = it.take(256) }, modifier = Modifier.fillMaxWidth(), placeholderText = "Search query diagnostics…")
                    ExpressiveFilterBar(states, states.indexOf(diagnosticStateFilter).coerceAtLeast(0)) { diagnosticStateFilter = states[it] }
                } }
                item { CapabilitySectionCard("OpenGL® ES™ runtime diagnostics") {
                    CapabilityKeyValue("Context flags", report.glRuntime.contextFlags ?: "Unavailable")
                    CapabilityKeyValue("Reset notification strategy", report.glRuntime.resetNotificationStrategy ?: "Unavailable")
                    CapabilityKeyValue("Reset-strategy query", report.glRuntime.resetNotificationStrategyQuery ?: "Not applicable")
                    CapabilityKeyValue("Robust access", report.glRuntime.robustAccess?.toString() ?: "Unavailable")
                    CapabilityKeyValue("Robust-access query", report.glRuntime.robustAccessQuery ?: "Not applicable")
                    CapabilityKeyValue("Unavailable GL runtime attributes", report.glRuntime.unavailableAttributes.size.toString())
                    report.glRuntime.unavailableAttributes.take(32).forEach { CapabilityKeyValue(it.name, it.error) }
                    if (report.glRuntime.unavailableAttributes.size > 32) CapabilityKeyValue("Displayed unavailable attributes", "32 / ${report.glRuntime.unavailableAttributes.size}")
                } }
                item { CapabilitySectionCard("Probe and scheduler timing") {
                    Text("Measured durations are app-observed intervals. Opening gate delay is recorded separately from GL/EGL probing, and is unavailable on activity recreation rather than inferred.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    startupGateDelayMs?.let { gate ->
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = BrandContainer, contentColor = TextPrimary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.44f))) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Opening / startup gate", color = BrandSoft, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(formatAnalysisElapsedTime(gate), color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text("Cold-launch activity creation to completion of the opening sequence", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    CapabilityKeyValue("Opening / startup gate", startupGateDelayMs?.let(::formatAnalysisElapsedTime) ?: "Unavailable · not a cold launch")
                    CapabilityKeyValue("App-observed collection", collectionElapsedMs?.let(::formatAnalysisElapsedTime) ?: "Unavailable")
                    CapabilityKeyValue("Probe timeout budget", "20,000 ms")
                    CapabilityKeyValue("Probe result bound", "8 MiB")
                    CapabilityKeyValue("Execution", "Dedicated :opengles_probe process")
                    CapabilityKeyValue("Publication", "Atomic result + terminal marker")
                    Text("The displayed duration is wall-clock time observed by the app for the isolated collection request, including service/process handoff and report parsing. Per-query timings are not fabricated: the current OpenGL® ES™ collector does not record them, avoiding measurement overhead in the canonical capability path.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                item { CapabilitySectionCard("EGL binding diagnostics") {
                    CapabilityKeyValue("Current context", report.eglRuntime.currentContext.toString())
                    CapabilityKeyValue("Current display", report.eglRuntime.currentDisplay.toString())
                    CapabilityKeyValue("Current draw surface", report.eglRuntime.currentDrawSurface.toString())
                    CapabilityKeyValue("Current read surface", report.eglRuntime.currentReadSurface.toString())
                    CapabilityKeyValue("Unavailable EGL runtime attributes", report.eglRuntime.unavailableAttributes.size.toString())
                    report.eglRuntime.unavailableAttributes.take(32).forEach { CapabilityKeyValue(it.name, it.error) }
                } }
                items(filtered, key = { "diag:${it.name}" }) { diagnostic -> CapabilityItemCard {
                    CapabilityKeyValue(diagnostic.name, diagnostic.status)
                    CapabilityKeyValue("Detail", diagnostic.detail.ifBlank { "No additional detail reported" })
                } }
            }
            "Database" -> {
                item { CapabilitySectionCard("OpenGLESScope Database lookup") {
                    Text("Choose a bounded public Database list or enter an exact report ID. Both paths are explicit user-initiated reads from the fixed official HTTPS endpoint and the selected technicalReport is compared locally.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    ExpressiveFilterBar(listOf("Database list", "Report ID"), databaseLookupMode.coerceIn(0, 1)) { databaseLookupMode = it }
                    if (databaseLookupMode == 0) {
                        ExpressiveSearchField(
                            value = databaseListQuery,
                            onValueChange = { databaseListQuery = it.take(120); databaseListPage = 0 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = networkAvailable,
                            placeholderText = "Filter loaded GPU, device, API or report ID…"
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExpressiveContainedIconTextButton(
                            if (databaseRows.isEmpty()) "Load reports" else "Refresh",
                            R.drawable.ic_database_fetch,
                            modifier = Modifier.weight(1f),
                            enabled = networkAvailable && !databaseListRunning
                        ) {
                            databaseListRunning = true
                            databaseNextCursor = null
                            databaseListStatus = "Fetching recent reports…"
                            scope.launch {
                                try {
                                    val result = fetchDatabaseReportPage(context)
                                    databaseRows = result.rows
                                    databaseNextCursor = result.nextCursor
                                    databaseListPage = 0
                                    databaseListStatus = "Loaded ${result.rows.size} recent report(s)"
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (error: Throwable) { databaseRows = emptyList(); databaseNextCursor = null; databaseListStatus = error.message ?: "Recent report lookup failed" }
                                finally { databaseListRunning = false }
                            }
                        }
                            ExpressiveContainedIconTextButton(
                                "Load more", R.drawable.ic_download,
                                modifier = Modifier.weight(1f),
                                enabled = networkAvailable && !databaseListRunning && databaseNextCursor != null && databaseRows.size < ANALYSIS_DATABASE_LIST_MAX
                            ) {
                                val requestedCursor = databaseNextCursor ?: return@ExpressiveContainedIconTextButton
                                databaseListRunning = true
                                databaseListStatus = "Loading the next public report page…"
                                scope.launch {
                                    try {
                                        val result = fetchDatabaseReportPage(context, requestedCursor)
                                        val previous = databaseRows.map { it.id }.toHashSet()
                                        val additions = result.rows.filter { previous.add(it.id) }
                                        databaseRows = (databaseRows + additions).take(ANALYSIS_DATABASE_LIST_MAX)
                                        databaseNextCursor = result.nextCursor.takeIf { databaseRows.size < ANALYSIS_DATABASE_LIST_MAX && additions.isNotEmpty() }
                                        databaseListStatus = "Loaded ${databaseRows.size} public report(s)"
                                    } catch (cancelled: CancellationException) { throw cancelled }
                                    catch (error: Throwable) { databaseListStatus = error.message ?: "Next Database page failed; loaded results remain available" }
                                    finally { databaseListRunning = false }
                                }
                            }
                        }
                        CapabilityKeyValue("List status", databaseListStatus)
                        Text("Each request is limited to $ANALYSIS_DATABASE_LIST_LIMIT public summaries; no more than $ANALYSIS_DATABASE_LIST_MAX are retained in memory. Use the page control to inspect loaded entries.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    } else {
                        ExpressiveSearchField(
                            value = databaseId,
                            onValueChange = { typed ->
                                val nextId = typed.lowercase(java.util.Locale.ROOT).filter { ch -> ch in '0'..'9' || ch in 'a'..'f' }.take(64)
                                if (nextId != databaseId) {
                                    databaseId = nextId
                                    databaseBaseline = null
                                    databaseStatus = "Fetch the selected report ID to compare it"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = networkAvailable,
                            placeholderText = "64-character report id"
                        )
                        ExpressiveActionButton(
                            "Fetch by report ID",
                            if (networkAvailable) "Load the exact public technicalReport for local comparison" else "Unavailable without a validated internet connection",
                            R.drawable.ic_database_fetch,
                            enabled = networkAvailable && !databaseRunning && databaseId.length == 64
                        ) {
                            databaseRunning = true
                            databaseBaseline = null
                            val requestedId = databaseId
                            databaseStatus = "Fetching exact report…"
                            scope.launch {
                                try {
                                    val snapshot = fetchDatabaseAnalysisSnapshot(context, requestedId)
                                    if (requestedId == databaseId) {
                                        databaseBaseline = snapshot
                                        databaseStatus = "Fetched · ${flattenGlSnapshot(snapshot)["identity/renderer"] ?: "Unknown GPU"}"
                                    }
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (error: Throwable) { if (requestedId == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }
                                finally { databaseRunning = false }
                            }
                        }
                        CapabilityKeyValue("Status", databaseStatus)
                    }
                    if (!networkAvailable) Text("Database lookup is locked until Android reports a validated internet connection.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
                    if (databaseListRunning || databaseRunning) LoadingIndicator(color = BrandSoft, modifier = Modifier.size(32.dp))
                    if (databaseBaseline != null) ExpressiveToggleRow("Show unchanged", "Include identical structured JSON leaves.", includeUnchanged) { includeUnchanged = it }
                    Text("List responses contain summary metadata only. Full comparison begins only after you explicitly select a row or fetch an exact ID; no report is uploaded or mutated by this screen.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                if (databaseLookupMode == 0) {
                    val query = databaseListQuery.trim()
                    val visibleRows = databaseRows.filter { row ->
                        query.isBlank() || listOf(row.id, row.gpuName, row.vendor, row.openGlesVersion, row.eglVersion, row.manufacturer, row.model, row.applicationVersion).any { it.contains(query, true) }
                    }
                    val pageCount = maxOf(1, (visibleRows.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
                    val resolvedPage = databaseListPage.coerceIn(0, pageCount - 1)
                    stickyCollectionPager(visibleRows.size, resolvedPage, { databaseListPage = it }, pagerKey = "analysis-database-pager")
                    items(visibleRows.drop(resolvedPage * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE), key = { "database-list:${it.id}" }) { row ->
                        CapabilityItemCard {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DatabaseReportVendorBadge(row.vendor, row.gpuName)
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(row.gpuName.ifBlank { "Unknown GPU" }, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text(listOf(row.manufacturer, row.model).filter { it.isNotBlank() }.joinToString(" ").ifBlank { row.vendor.ifBlank { "Unknown vendor" } }, color = TextSecondary, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ExpressiveInfoPill("OpenGL® ES™", row.openGlesVersion.ifBlank { "Unavailable" })
                                    ExpressiveInfoPill("EGL™", row.eglVersion.ifBlank { "Unavailable" })
                                    ExpressiveInfoPill("App", row.applicationVersion.ifBlank { "Unknown" })
                                }
                                DatabaseSubmittedAt(row.submittedAt)
                                CapabilityKeyValue("Report ID", row.id)
                                ExpressiveContainedIconTextButton("Compare this report", R.drawable.ic_compare, enabled = networkAvailable && !databaseRunning) {
                                    databaseLookupMode = 1
                                    databaseId = row.id
                                    databaseRunning = true
                                    databaseBaseline = null
                                    databaseStatus = "Fetching selected report…"
                                    scope.launch {
                                        try {
                                            val snapshot = fetchDatabaseAnalysisSnapshot(context, row.id)
                                            if (row.id == databaseId) {
                                                databaseBaseline = snapshot
                                                databaseStatus = "Fetched · ${flattenGlSnapshot(snapshot)["identity/renderer"] ?: "Unknown GPU"}"
                                            }
                                        } catch (cancelled: CancellationException) { throw cancelled }
                                        catch (error: Throwable) { if (row.id == databaseId) { databaseBaseline = null; databaseStatus = error.message ?: "Fetch failed" } }
                                        finally { databaseRunning = false }
                                    }
                                }
                            }
                        }
                    }
                }
                if (databaseBaseline != null) {
                    item { CapabilitySectionCard("Database comparison summary") {
                        ExpressiveMetric("Differences", databaseComparisonRows.count { it.state != "Unchanged" }.toString())
                        ExpressiveMetric("Added locally", databaseComparisonRows.count { it.state == "Added" }.toString())
                        ExpressiveMetric("Missing locally", databaseComparisonRows.count { it.state.startsWith("Removed") }.toString())
                        ExpressiveMetric("Changed", databaseComparisonRows.count { it.state.startsWith("Changed") }.toString())
                        Text("A difference is a report-evidence difference only; it is not a GPU ranking, performance result, conformance verdict or market-share statement.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    } }
                    items(databaseComparisonRows.take(ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT), key = { "db:${it.key}" }) { row -> CapabilityItemCard { CapabilityKeyValue(row.key, row.state); row.baseline?.let { CapabilityKeyValue("Database", it) }; row.current?.let { CapabilityKeyValue("Current", it) } } }
                }
            }
            "Requirements" -> {
                val evaluated = OPENGL_ES_32_MINIMUMS.map { requirement ->
                    val raw = report.limits.firstOrNull { it.name == requirement.name }?.value; val actual = numericAnalysisValue(raw)
                    val state = when { actual == null -> "UNKNOWN"; requirement.direction == "minimum" && actual >= requirement.threshold -> "PASS"; requirement.direction == "maximum" && actual <= requirement.threshold -> "PASS"; else -> "FAIL" }
                    Triple(requirement, raw, state)
                }
                val filtered = evaluated.filter { (req, raw, state) -> (minimumQuery.isBlank() || req.name.contains(minimumQuery, true) || raw?.contains(minimumQuery, true) == true) && (minimumStateFilter == "All" || minimumStateFilter == state) }
                item { CapabilitySectionCard("OpenGL® ES™ 3.2 scalar implementation requirements") {
                    Text("Only checked-in Khronos implementation-dependent scalar minima/maxima that map to collected numeric evidence are evaluated here. Non-scalar language/API rules, behavioral requirements and conformance tests are deliberately outside this resolver; missing evidence remains UNKNOWN rather than a fabricated failure.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Runtime core", "${report.glMajor}.${report.glMinor}")
                    CapabilityKeyValue("Evaluated scalar requirements", OPENGL_ES_32_MINIMUMS.size.toString())
                    CapabilityKeyValue("Scope", "Collected implementation-dependent numeric limits only")
                    CapabilityKeyValue("PASS", evaluated.count { it.third == "PASS" }.toString())
                    CapabilityKeyValue("FAIL", evaluated.count { it.third == "FAIL" }.toString())
                    CapabilityKeyValue("UNKNOWN", evaluated.count { it.third == "UNKNOWN" }.toString())
                    ExpressiveSearchField(value = minimumQuery, onValueChange = { minimumQuery = it }, modifier = Modifier.fillMaxWidth(), placeholderText = "Search requirements…")
                    val minimumStates = listOf("All", "PASS", "FAIL", "UNKNOWN")
                    ExpressiveFilterBar(minimumStates, minimumStates.indexOf(minimumStateFilter).coerceAtLeast(0)) { minimumStateFilter = minimumStates[it] }
                } }
                items(filtered, key = { it.first.name }) { (req, raw, state) -> CapabilityItemCard {
                    CapabilityKeyValue(req.name, state)
                    CapabilityKeyValue("Requirement", req.display)
                    CapabilityKeyValue("Direction", req.direction)
                    CapabilityKeyValue("Threshold", req.threshold.toString())
                    CapabilityKeyValue("Runtime evidence", raw ?: "Unavailable")
                    CapabilityKeyValue("Semantics", if (state == "UNKNOWN") "Collected numeric evidence could not safely resolve this rule" else "Direct numeric comparison only")
                } }
            }
            "Minimums" -> {
                val evaluated = evaluateCustomMinimumRules(report, minimumProfileRules)
                item { CapabilitySectionCard("Custom minimum profiles") {
                    Text("Local bounded profiles support API/EGL™ minima, exact runtime extension requirements, EGL™ capability-state rules and scalar limit comparisons. Missing enumeration/query evidence becomes UNKNOWN rather than fabricated failure.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Saved profiles", "${minimumProfiles.size} / $CUSTOM_MINIMUM_MAX_PROFILES")
                    CapabilityKeyValue("Parsed rules", minimumProfileRules.lineSequence().map { it.trim() }.count { it.isNotBlank() && !it.startsWith("#") }.coerceAtMost(CUSTOM_MINIMUM_MAX_RULES).toString())
                    CapabilityKeyValue("Rule bound", CUSTOM_MINIMUM_MAX_RULES.toString())
                    Text("Supported rule families: api, egl, extension:, egl-extension:, egl-capability: and scalar GL_* comparisons. Unknown or unavailable source evidence propagates as UNKNOWN.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(value = minimumProfileName, onValueChange = { minimumProfileName = it.take(128) }, label = { Text("Profile name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minimumProfileRules, onValueChange = { minimumProfileRules = it.take(16 * 1024) }, label = { Text("Rules") }, modifier = Modifier.fillMaxWidth(), minLines = 5, maxLines = 12)
                    ExpressiveActionButton("Save local profile", "Bounded local profile; no capability mutation", R.drawable.ic_save) {
                        val name = minimumProfileName.trim()
                        val rules = minimumProfileRules.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.take(CUSTOM_MINIMUM_MAX_RULES + 1).toList()
                        if (name.isNotEmpty() && name.length <= 128 && rules.isNotEmpty() && rules.size <= CUSTOM_MINIMUM_MAX_RULES && rules.all { it.length <= 512 } && (minimumProfiles.size < CUSTOM_MINIMUM_MAX_PROFILES || name in minimumProfiles)) {
                            pendingMinimumProfileSave = name to rules.joinToString("\n")
                        } else minimumProfileStatus = "Minimum profile violates local bounds"
                    }
                    SharedStoragePermissionActionButton("Import profile JSON", "Open the in-app shared-storage browser · JSON · 256 KiB bound", R.drawable.ic_action_import) { analysisStorageAction = AnalysisStorageAction.IMPORT_MINIMUM_PROFILE }
                    SharedStoragePermissionActionButton("Export profile JSON", "Choose a shared-storage folder and JSON file name", R.drawable.ic_export) { analysisStorageAction = AnalysisStorageAction.EXPORT_MINIMUM_PROFILE }
                    if (minimumProfileStatus.isNotBlank()) CapabilityKeyValue("Status", minimumProfileStatus)
                    if (minimumProfiles.isNotEmpty()) {
                        val profileEntries = minimumProfiles.toSortedMap().entries.toList()
                        ExpressiveSingleFilterSelector(
                            labels = profileEntries.map { it.key },
                            selectedIndex = profileEntries.indexOfFirst { it.key == minimumProfileName }.takeIf { it >= 0 },
                            enabled = true,
                            indicatorTint = TextPrimary
                        ) { index ->
                            profileEntries.getOrNull(index)?.let { selected ->
                                minimumProfileName = selected.key
                                minimumProfileRules = selected.value
                            }
                        }
                    }
                    CapabilityKeyValue("PASS", evaluated.count { it.state == "PASS" }.toString()); CapabilityKeyValue("FAIL", evaluated.count { it.state == "FAIL" }.toString()); CapabilityKeyValue("UNKNOWN", evaluated.count { it.state == "UNKNOWN" }.toString())
                } }
                if (minimumProfiles.isNotEmpty()) {
                    item { CapabilitySectionCard("Saved minimum profiles") {
                        Text("Load changes only the local builder fields. Delete removes only the selected locally saved profile and requires confirmation; neither action changes collected OpenGL® ES™/EGL™ evidence.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    } }
                    items(minimumProfiles.toSortedMap().entries.toList(), key = { "minimum:${it.key}" }) { entry ->
                        CapabilityItemCard {
                            CapabilityKeyValue(entry.key, "${entry.value.lineSequence().count { it.isNotBlank() && !it.trim().startsWith("#") }} rule(s)")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExpressiveContainedIconTextButton("Load", R.drawable.ic_action_import, modifier = Modifier.weight(1f)) { pendingMinimumProfileLoad = entry.key to entry.value }
                                ExpressiveContainedIconTextButton("Delete", R.drawable.ic_delete, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold) { pendingMinimumProfileDelete = entry.key }
                            }
                        }
                    }
                }
                item { CapabilitySectionCard("Current custom evaluation") {
                    CapabilityKeyValue("PASS", evaluated.count { it.state == "PASS" }.toString())
                    CapabilityKeyValue("FAIL", evaluated.count { it.state == "FAIL" }.toString())
                    CapabilityKeyValue("UNKNOWN", evaluated.count { it.state == "UNKNOWN" }.toString())
                    Text("Each rule is evaluated independently against the current evidence. UNKNOWN is preserved whenever the requested source cannot be safely resolved.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                items(evaluated, key = { it.rule }) { result -> CapabilityItemCard { CapabilityKeyValue(result.rule, result.state); CapabilityKeyValue("Evidence", result.evidence) } }
            }
            "Graph" -> {
                val visualNodes = buildList {
                    if (graphSelectedRoot != null) {
                        add(OpenGLESGraphNode(graphSelectedRoot, 0, null, if (graphSelectedRoot in graphRuntimeExtensions) "Runtime enumerated" else "Not enumerated"))
                        if (graphDepth >= 2) graphSelectedQueries.forEach { q ->
                            add(OpenGLESGraphNode(q, 1, graphSelectedRoot, graphDiagnosticByName[q]?.status ?: "Not reported"))
                        }
                    }
                }
                item { CapabilitySectionCard("Dependency graph explorer") {
                    Text("Runtime query-gate relationships and checked-in registry ownership/reference rows are shown as separate evidence classes. Registry ownership is reference metadata; it is never interpreted as runtime support or as an extension dependency unless the checked-in query-gate catalog explicitly says so.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    ExpressiveSearchField(value = graphRoot, onValueChange = { graphRoot = it.take(256) }, modifier = Modifier.fillMaxWidth(), placeholderText = "Exact GL_/EGL_ extension…")
                    ExpressiveSearchField(value = graphQuery, onValueChange = { graphQuery = it.take(256) }, modifier = Modifier.fillMaxWidth(), placeholderText = "Filter query-gate catalog…")
                    Text("Query-gate depth", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    ExpressiveSingleFilterSelector(labels = listOf("Root", "Queries", "Queries + registry"), selectedIndex = (graphDepth - 1).coerceIn(0, 2), enabled = true, indicatorTint = TextPrimary) { index -> graphDepth = index + 1 }
                    CapabilityKeyValue("Query-gate roots", graphRows.size.toString())
                    CapabilityKeyValue("Selected root", graphSelectedRoot ?: "No matching query-gate root")
                    CapabilityKeyValue("Selected query rows", graphSelectedQueries.size.toString())
                    CapabilityKeyValue("Registry reference rows", graphRegistryReferences.size.toString())
                } }
                if (visualNodes.isNotEmpty()) item { CapabilitySectionCard("Interactive query-gate map") { OpenGLESDependencyGraph(visualNodes, Modifier.fillMaxWidth()); Text("Node state reflects only current runtime enumeration/query diagnostics.", color = TextMuted, style = MaterialTheme.typography.labelSmall) } }
                if (graphDepth >= 3 && graphRegistryReferences.isNotEmpty()) {
                    item { CapabilitySectionCard("Registry references") { Text("These rows come from the locked gl.xml/egl.xml-derived catalog and do not prove runtime support.", color = TextMuted, style = MaterialTheme.typography.labelSmall) } }
                    items(graphRegistryReferences, key = { "registry:${it.api}:${it.kind}:${it.name}" }) { entry -> CapabilityItemCard {
                        CapabilityKeyValue(entry.name, "${entry.api} · ${entry.kind}")
                        if (entry.owners.isNotEmpty()) CapabilityKeyValue("Owners", entry.owners.joinToString(", "))
                        if (entry.alias.isNotBlank()) CapabilityKeyValue("Alias", entry.alias)
                        if (entry.value.isNotBlank()) CapabilityKeyValue("Value", entry.value)
                        if (entry.signature.isNotBlank()) CapabilityKeyValue("Signature", entry.signature)
                    } }
                }
                items(graphRows.take(ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT), key = { it.key }) { (extension, queries) -> CapabilityItemCard {
                    CapabilityKeyValue(extension, if (extension in graphRuntimeExtensions) "Runtime enumerated" else "Not enumerated")
                    CapabilityKeyValue("Query gates", queries.size.toString())
                    if (graphDepth >= 2) queries.forEach { q -> CapabilityKeyValue("↳ $q", graphDiagnosticByName[q]?.status ?: "Not reported") }
                } }
            }
            "Presentation" -> {
                val glColorExt = report.egl.extensions.filter { it.contains("colorspace", true) || it.contains("protected", true) || it.contains("surface_compression", true) || it.contains("buffer_age", true) || it.contains("partial_update", true) }
                item { CapabilitySectionCard("Presentation evidence") {
                    Text("EGL™ surface/display extensions and Android display evidence are shown side-by-side. Matching colorspace/HDR names are compatibility evidence only; they are not an end-to-end guarantee that an application window can present a specific gamut, transfer function, luminance or compression mode.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Display", display.name.ifBlank { "Unavailable" })
                    CapabilityKeyValue("Mode", listOfNotNull(display.width, display.height).takeIf { it.size == 2 }?.joinToString("×") ?: "Unavailable")
                    CapabilityKeyValue("Refresh rate", display.refreshRate?.let { "${it} Hz" } ?: "Unavailable")
                    CapabilityKeyValue("Android wide color", display.wideColor?.toString() ?: "Unknown")
                    CapabilityKeyValue("Android HDR", display.hdrCapabilityStatus)
                    CapabilityKeyValue("HDR types", display.hdrTypes.joinToString().ifBlank { "None/unknown" })
                    CapabilityKeyValue("Desired max luminance", display.desiredMaxLuminance?.let { "$it cd/m²" } ?: "Unavailable")
                    CapabilityKeyValue("Desired max average luminance", display.desiredMaxAverageLuminance?.let { "$it cd/m²" } ?: "Unavailable")
                    CapabilityKeyValue("Desired min luminance", display.desiredMinLuminance?.let { "$it cd/m²" } ?: "Unavailable")
                    CapabilityKeyValue("Relevant EGL™ display extensions", glColorExt.size.toString())
                    CapabilityKeyValue("EGL™ vendor", report.egl.vendor.ifBlank { "Unavailable" })
                    CapabilityKeyValue("EGL™ initialized", report.egl.initializedVersion.ifBlank { "Unavailable" })
                    CapabilityKeyValue("Current EGL™ context", report.eglRuntime.currentContext.toString())
                    CapabilityKeyValue("Current draw/read surface", "${report.eglRuntime.currentDrawSurface} / ${report.eglRuntime.currentReadSurface}")
                    CapabilityKeyValue("Surface config ID", report.eglRuntime.surfaceConfigId?.toString() ?: "Unavailable")
                    CapabilityKeyValue("Surface size", if (report.eglRuntime.surfaceWidth != null && report.eglRuntime.surfaceHeight != null) "${report.eglRuntime.surfaceWidth}×${report.eglRuntime.surfaceHeight}" else "Unavailable")
                    CapabilityKeyValue("Surface render buffer", report.eglRuntime.surfaceRenderBuffer ?: "Unavailable")
                    CapabilityKeyValue("Surface swap behavior", report.eglRuntime.surfaceSwapBehavior ?: "Unavailable")
                    CapabilityKeyValue("Surface GL colorspace", report.eglRuntime.surfaceGlColorspace ?: "Unavailable")
                    if (report.eglRuntime.unavailableAttributes.isNotEmpty()) CapabilityKeyValue("Unavailable EGL™ surface/runtime attributes", report.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" })
                } }
                items(glColorExt, key = { "presentation:$it" }) { ext -> CapabilityItemCard { CapabilityKeyValue(ext, "Runtime enumerated"); extensionRegistryUrl(ext)?.let { url -> ExpressiveContainedIconTextButton("Khronos reference", R.drawable.ic_open_external) { open(activity, url) } } } }
                items(report.eglCapabilities.filter { it.name.contains("compression", true) || it.name.contains("protected", true) || it.name.contains("buffer_age", true) }, key = { "presentation-cap:${it.name}" }) { cap -> CapabilityItemCard { CapabilityKeyValue(cap.name, cap.status); if (cap.value.isNotBlank()) CapabilityKeyValue("Value", cap.value); if (cap.detail.isNotBlank()) CapabilityKeyValue("Detail", cap.detail) } }
            }
            "Raw JSON" -> {
                val matched = rawTechnicalEntries.entries.filter { rawQuery.isBlank() || it.key.contains(rawQuery, true) || it.value.contains(rawQuery, true) }
                val visible = matched.take(ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT)
                item { CapabilitySectionCard("Raw structured technicalReport") {
                    Text("Read-only tree leaves from the exact local schema-v5 technicalReport object used by Database submission. Values are searchable and exportable without changing the canonical report or inferring support from absent fields.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Load state", rawTechnicalStatus)
                    ExpressiveMetricGrid(listOf(
                        "Fields" to rawTechnicalEntries.size.toString(),
                        "Matches" to matched.size.toString(),
                        "Rendered" to visible.size.toString(),
                        "Render bound" to ANALYSIS_GLOBAL_SEARCH_VISIBLE_LIMIT.toString()
                    ))
                    ExpressiveSearchField(value = rawQuery, onValueChange = { rawQuery = it.take(ANALYSIS_MAX_KEY_LENGTH) }, modifier = Modifier.fillMaxWidth(), placeholderText = "Search raw evidence…")
                    SharedStoragePermissionActionButton("Export technicalReport JSON", "Choose a shared-storage folder · exact local technicalReport JSON · 8 MiB bound", R.drawable.ic_export) { analysisStorageAction = AnalysisStorageAction.EXPORT_TECHNICAL_REPORT }
                    if (matched.size > visible.size) Text("${matched.size - visible.size} additional matching row(s) are intentionally not rendered; refine the search to inspect them. Export remains complete.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                items(visible, key = { "raw:${it.key}" }) { (key, value) -> CapabilityItemCard { CapabilityKeyValue(key, value) } }
            }
            "History" -> {
                item { CapabilitySectionCard("Local session history") { Text("App-private compressed snapshots are capped at 8 records, 8 MiB uncompressed and 4 MiB compressed each. A completed changed report is checkpointed automatically; records never leave the device unless you explicitly export/share them.", color = TextSecondary, style = MaterialTheme.typography.bodySmall); CapabilityKeyValue("Records", "${history.size} / $ANALYSIS_HISTORY_MAX_RECORDS"); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ExpressiveContainedIconTextButton("Save checkpoint", R.drawable.ic_save) { scope.launch { withContext(Dispatchers.IO) { runCatching { saveAnalysisHistory(context, currentSnapshot, report.renderer) } }; historyRevision++ } }; if (history.isNotEmpty()) ExpressiveContainedIconTextButton("Delete all", R.drawable.ic_clear_all) { pendingHistoryDeleteAll = true } } } }
                items(history, key = { it.file.name }) { record -> CapabilityItemCard { CapabilityKeyValue(record.renderer, analysisHistoryLabel(record)); CapabilityKeyValue("Compressed", "${record.bytes} bytes"); CapabilityKeyValue("Storage", "Private app storage"); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ExpressiveContainedIconTextButton("Use as baseline", R.drawable.ic_baseline, modifier = Modifier.weight(1f)) { scope.launch { val loaded = withContext(Dispatchers.IO) { runCatching { loadAnalysisHistory(record.file) } }; loaded.onSuccess { snapshot -> baseline = snapshot; importStatus = "History baseline · ${flattenGlSnapshot(snapshot)["identity/renderer"] ?: "Unknown GPU"}"; mode = 0 }.onFailure { importStatus = it.message ?: "History load failed" } } }; ExpressiveContainedIconTextButton("Delete", R.drawable.ic_delete, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold) { pendingHistoryDelete = record } } } }
            }
            "Quality" -> {
                val quality = glDiagnosticEvidenceScore(report)
                item { CapabilitySectionCard("Collection integrity score") {
                    CapabilityKeyValue("Current evidence score", quality.score?.let { "$it / 100" } ?: "Unavailable")
                    CapabilityKeyValue("Interpretation", quality.level)
                    CapabilityKeyValue("Triggered deductions", quality.checks.count { it.triggered }.toString())
                    CapabilityKeyValue("Audited checks", quality.checks.size.toString())
                    Text("The score starts at 100 and subtracts only the explicit collection/query anomalies printed below. It does not use GPU model, vendor, extension count, benchmark data or market ranking.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } }
                item { CapabilitySectionCard("Scoring method") {
                    Text("CLEAR subtracts 0 points. TRIGGERED subtracts the printed deduction. The final score is floored at 0. This is a diagnostic completeness heuristic, not conformance or driver-quality evidence.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
                items(quality.checks, key = { "quality:${it.label}" }) { check -> CapabilityItemCard {
                    CapabilityKeyValue(check.label, if (check.triggered) "TRIGGERED · -${check.deduction}" else "CLEAR · -0")
                    CapabilityKeyValue("Maximum deduction", check.deduction.toString())
                    CapabilityKeyValue("Evidence", check.evidence)
                } }
                if (quality.factors.any { !it.startsWith("-") }) {
                    item { CapabilitySectionCard("Additional retained evidence") { quality.factors.filter { !it.startsWith("-") }.forEach { Text(it, color = TextMuted, style = MaterialTheme.typography.labelSmall) } } }
                }
                item { CapabilitySectionCard("What this score does not mean") {
                    Text("It is not OpenGL® ES™ conformance, a performance benchmark, GPU ranking, driver certification or vendor score. It summarizes only the explicit collection/safety evidence checked above so the calculation can be audited line by line.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } }
            }
            "Watched" -> {
                item { CapabilitySectionCard("Watched evidence") { CapabilityKeyValue("Stored", "${watched.size} / $ANALYSIS_MAX_WATCHED"); ExpressiveSearchField(value = watchInput, onValueChange = { watchInput = it }, modifier = Modifier.fillMaxWidth(), placeholderText = "Exact extension, limit, format or evidence token…"); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { TransientActionButton("Add to watch list", "Local bounded watch list", R.drawable.ic_watch_add, enabled = watchInput.trim().isNotEmpty() && watched.size < ANALYSIS_MAX_WATCHED, idleTrailingIcon = R.drawable.ic_add) { val token = watchInput.trim(); if (token.isNotEmpty() && token.length <= 256 && watched.size < ANALYSIS_MAX_WATCHED) { watched = watched + token; prefs.edit().putStringSet("watched", watched).apply(); watchInput = ""; true } else false }; if (watched.isNotEmpty()) ExpressiveContainedIconTextButton("Delete all", R.drawable.ic_clear_all) { pendingWatchDeleteAll = true } }; ExpressiveSearchField(value = watchQuery, onValueChange = { watchQuery = it }, modifier = Modifier.fillMaxWidth(), placeholderText = "Filter watch list…"); val watchStates = listOf("All", "Matched", "Missing"); ExpressiveFilterBar(watchStates, watchStates.indexOf(watchStateFilter).coerceAtLeast(0)) { watchStateFilter = watchStates[it] } } }
                val watchRows = watched.sorted().map { token -> token to currentFlat.filterKeys { it.contains(token, true) } }.filter { (token, matches) -> (watchQuery.isBlank() || token.contains(watchQuery, true)) && when (watchStateFilter) { "Matched" -> matches.isNotEmpty(); "Missing" -> matches.isEmpty(); else -> true } }
                items(watchRows, key = { it.first }) { (token, matches) -> CapabilityItemCard {
                    CapabilityKeyValue(token, if (matches.isEmpty()) "Missing" else "Matched · ${matches.size} evidence item(s)")
                    matches.entries.take(10).forEach { CapabilityKeyValue(it.key, it.value) }
                    if (matches.size > 10) CapabilityKeyValue("Displayed", "10 / ${matches.size} matches · refine/search elsewhere for complete evidence")
                    ExpressiveContainedIconTextButton("Remove", R.drawable.ic_delete) { pendingWatchDelete = token }
                } }
            }
            "Share" -> {
                val lastReportId = prefs.getString("last_database_report_id", "").orEmpty()
                val permalink = databaseReportUrl(lastReportId)
                item { CapabilitySectionCard("Database permalink & QR") {
                    Text("A canonical report link exists only after a successful explicit Database submission returns a validated 64-hex ID. Sharing uses the Android Sharesheet and never uploads in the background.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    CapabilityKeyValue("Report ID", lastReportId.ifBlank { "No successful submission recorded" })
                    CapabilityKeyValue("Permalink", permalink ?: "Unavailable")
                    if (permalink != null) {
                        ExpressiveActionButton("Open report", if (networkAvailable) "Open canonical Database route" else "Validated internet connection required", R.drawable.ic_database_browse, enabled = networkAvailable, trailingIcon = R.drawable.ic_open_external) { open(activity, permalink) }
                        ExpressiveActionButton("Share link", "Android Sharesheet · no background upload", R.drawable.ic_share, trailingIcon = R.drawable.ic_open_external) { activity.shareText(permalink) }
                        ExpressiveContainedIconTextButton("Copy permalink", R.drawable.ic_copy) { val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager; cb?.setPrimaryClip(android.content.ClipData.newPlainText("OpenGLESScope report", permalink)) }
                    }
                } }
                if (permalink != null) item { CapabilitySectionCard("QR payload") {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OpenGLESQrCode(permalink, Modifier.size(220.dp)) }
                    Text("The QR payload is exactly the permalink shown above. No device identifier, hidden token or additional payload is embedded.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                } }
            }
            "Tests" -> {
                item { CapabilitySectionCard("OpenGL® ES™ active self-tests") {
                    Text("These tests run only after you press Run. They execute in the isolated OpenGL® ES™ probe process and are attributed only when the isolated GL identity matches the current report. Test evidence never rewrites canonical capability support states.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("PASS means the specific operation completed successfully. FAIL means the attempted operation returned a failure or failed its round-trip check. UNAVAILABLE means the test could not be safely attempted because a required entry point, prerequisite or identity match was unavailable.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    ExpressiveActionButton("Run OpenGL® ES™ self-tests", "Identity, shader compile/link, program-binary round-trip when applicable and KHR_debug insertion", R.drawable.ic_self_test, enabled = !testRunning) {
                        testRunning = true
                        scope.launch {
                            testResult = withContext(Dispatchers.IO) { runCatching { JSONObject(activity.runOpenGlesSelfTests(report)) }.getOrElse { JSONObject().put("status", "unavailable").put("reason", it.message ?: "Self-test failed") } }
                            testRunning = false
                        }
                    }
                    if (testRunning) LoadingIndicator()
                } }
                testResult?.let { result ->
                    val tests = result.optJSONArray("tests")
                    val testRows = if (tests == null) emptyList() else (0 until tests.length()).mapNotNull { tests.optJSONObject(it) }
                    item { CapabilitySectionCard("Test result summary") {
                        CapabilityKeyValue("Overall status", result.optString("status", "unknown"))
                        result.optString("reason").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Reason", it) }
                        result.optString("renderer").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Isolated renderer", it) }
                        CapabilityKeyValue("Reported checks", testRows.size.toString())
                        CapabilityKeyValue("PASS", testRows.count { it.optString("status").equals("pass", true) || it.optString("status").equals("passed", true) }.toString())
                        CapabilityKeyValue("FAIL", testRows.count { it.optString("status").equals("fail", true) || it.optString("status").equals("failed", true) }.toString())
                        CapabilityKeyValue("UNAVAILABLE", testRows.count { it.optString("status").equals("unavailable", true) }.toString())
                    } }
                    items(testRows, key = { "test:${it.optString("name")}" }) { test -> CapabilityItemCard {
                        CapabilityKeyValue(test.optString("name", "Test"), test.optString("status", "unknown"))
                        test.optString("detail").takeIf { it.isNotBlank() }?.let { CapabilityKeyValue("Detail", it) }
                    } }
                    item { CapabilitySectionCard("Result semantics") {
                        Text("Self-test results are operational evidence only. They do not convert OpenGL® ES™/EGL™ feature support to Unsupported, do not replace Khronos conformance testing and do not contribute to the Collection integrity score.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    } }
                }
            }
        }
    }
    pendingHistoryDelete?.let { record ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingHistoryDelete = null }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Delete analysis history snapshot?") },
            text = { Text("This deletes the selected bounded local analysis snapshot from OpenGLESScope private storage. The action cannot be undone.") },
            confirmButton = { ExpressiveContainedIconTextButton("Yes", R.drawable.ic_delete) { pendingHistoryDelete = null; scope.launch { if (withContext(Dispatchers.IO) { record.file.delete() }) historyRevision++ } } },
            dismissButton = { ExpressiveCancelButton { pendingHistoryDelete = null } }
        )
    }
    if (pendingHistoryDeleteAll) {
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingHistoryDeleteAll = false }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Delete all analysis history?") },
            text = { Text("Delete all ${history.size} retained local analysis snapshots? This does not change current OpenGL® ES™/EGL™ capability evidence.") },
            confirmButton = { ExpressiveContainedIconTextButton("Delete all", R.drawable.ic_clear_all, fontWeight = FontWeight.Bold) { pendingHistoryDeleteAll = false; scope.launch { withContext(Dispatchers.IO) { history.forEach { runCatching { it.file.delete() } } }; historyRevision++ } } },
            dismissButton = { ExpressiveCloseButton { pendingHistoryDeleteAll = false } }
        )
    }
    pendingWatchDelete?.let { token ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingWatchDelete = null }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Remove watched evidence?") },
            text = { Text("Remove $token from the local watched-evidence list?") },
            confirmButton = { ExpressiveContainedIconTextButton("Delete", R.drawable.ic_delete, fontWeight = FontWeight.Bold) { pendingWatchDelete = null; watched = watched - token; prefs.edit().putStringSet("watched", watched).apply() } },
            dismissButton = { ExpressiveCloseButton { pendingWatchDelete = null } }
        )
    }
    if (pendingWatchDeleteAll) {
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingWatchDeleteAll = false }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Delete all watched evidence?") },
            text = { Text("Delete all ${watched.size} entries from the local watched-evidence list? This does not change OpenGL® ES™/EGL™ capability evidence.") },
            confirmButton = { ExpressiveContainedIconTextButton("Delete all", R.drawable.ic_clear_all, fontWeight = FontWeight.Bold) { pendingWatchDeleteAll = false; watched = emptySet(); prefs.edit().putStringSet("watched", watched).apply() } },
            dismissButton = { ExpressiveCloseButton { pendingWatchDeleteAll = false } }
        )
    }
    pendingMinimumProfileSave?.let { pending ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingMinimumProfileSave = null }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Save minimum profile?") },
            text = { Text("Save ${pending.first} to OpenGLESScope private local minimum profiles? An existing entry with the same name is replaced only after you confirm.") },
            confirmButton = { ExpressiveContainedIconTextButton("Save", R.drawable.ic_save, fontWeight = FontWeight.Bold) {
                minimumProfiles = minimumProfiles + (pending.first to pending.second)
                prefs.edit().putString("minimum_profiles_v1", encodeCustomMinimumProfiles(minimumProfiles)).apply()
                minimumProfileStatus = "Saved ${pending.first}"
                pendingMinimumProfileSave = null
            } },
            dismissButton = { ExpressiveCancelButton { pendingMinimumProfileSave = null } }
        )
    }
    pendingMinimumProfileLoad?.let { pending ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingMinimumProfileLoad = null }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Load minimum profile?") },
            text = { Text("Load ${pending.first} into the custom minimum editor? Unsaved editor changes will be replaced only after you confirm.") },
            confirmButton = { ExpressiveContainedIconTextButton("Load", R.drawable.ic_action_import, fontWeight = FontWeight.Bold) {
                minimumProfileName = pending.first
                minimumProfileRules = pending.second
                minimumProfileStatus = "Loaded ${pending.first}"
                pendingMinimumProfileLoad = null
            } },
            dismissButton = { ExpressiveCancelButton { pendingMinimumProfileLoad = null } }
        )
    }
    pendingMinimumProfileDelete?.let { profileName ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { pendingMinimumProfileDelete = null }, shape = MaterialTheme.shapes.extraLarge, containerColor = SurfaceRaised, titleContentColor = TextPrimary, textContentColor = TextPrimary, tonalElevation = 0.dp,
            title = { QuestionDialogTitle("Delete saved minimum profile?") },
            text = { Text("Delete the local minimum profile ‘$profileName’? This removes only the saved builder profile and does not change collected OpenGL® ES™/EGL™ evidence.") },
            confirmButton = { ExpressiveContainedIconTextButton("Delete", R.drawable.ic_delete, fontWeight = FontWeight.Bold) {
                pendingMinimumProfileDelete = null
                minimumProfiles = minimumProfiles - profileName
                prefs.edit().putString("minimum_profiles_v1", encodeCustomMinimumProfiles(minimumProfiles)).apply()
                if (minimumProfileName == profileName) minimumProfileStatus = "Deleted $profileName"
            } },
            dismissButton = { ExpressiveCloseButton { pendingMinimumProfileDelete = null } }
        )
    }
}


@Composable
private fun DirectUpdatesConsentDialog(appName: String, releaseSource: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = SurfaceRaised,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        tonalElevation = 0.dp,
        title = { QuestionDialogTitle("Enable direct GitHub updates?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("$appName will check for updates and download APKs directly from $releaseSource.", color = TextPrimary)
                Text("If you use Obtainium, leave this disabled so Obtainium remains the single update manager. Enabling direct updates makes the app independently check the same official GitHub Releases source and may duplicate update notifications.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { ExpressivePrimaryButton("Enable direct updates", onClick = onConfirm) },
        dismissButton = { ExpressiveCancelButton(onClick = onDismiss) }
    )
}

private data class LibraryVersionInfo(val name: String, val version: String, val detail: String, val licenseName: String, val licenseAsset: String)

private val OPENGLESSCOPE_LIBRARY_VERSIONS = listOf(
    LibraryVersionInfo("AndroidX Core KTX", BuildConfig.CORE_KTX_VERSION, "Android application support; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("AndroidX Core SplashScreen", BuildConfig.SPLASHSCREEN_VERSION, "Android 12+ compatible platform splash integration; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("AndroidX Activity Compose", BuildConfig.ACTIVITY_COMPOSE_VERSION, "Compose activity integration; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Compose UI", BuildConfig.COMPOSE_VERSION, "Compose UI runtime; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Compose Foundation", BuildConfig.COMPOSE_VERSION, "Compose foundation components; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Compose Animation", BuildConfig.COMPOSE_VERSION, "Compose animation primitives; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Material 3", BuildConfig.MATERIAL3_VERSION, "Material 3 Expressive components; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Lifecycle Runtime Compose", BuildConfig.LIFECYCLE_VERSION, "Lifecycle-aware Compose state; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("Lifecycle Runtime KTX", BuildConfig.LIFECYCLE_VERSION, "Lifecycle coroutine integration used by application-scoped work; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("OkHttp", BuildConfig.OKHTTP_VERSION, "Explicit bounded HTTPS network requests; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md"),
    LibraryVersionInfo("ZXing Core", BuildConfig.ZXING_VERSION, "Local QR code generation; version generated from the pinned Gradle dependency.", "Apache License 2.0", "licenses/apache_2_0.md")
)

@Composable
private fun LibraryLicenseDialog(library: LibraryVersionInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val horizontalMargin = if (configuration.screenWidthDp < 360) 10.dp else 18.dp
    val verticalMargin = if (configuration.screenHeightDp < 520) 8.dp else 16.dp
    val dialogMaxHeight = maxOf(320.dp, configuration.screenHeightDp.dp - verticalMargin * 2)
    var markdown by remember(library.licenseAsset) { mutableStateOf<String?>(null) }
    LaunchedEffect(library.licenseAsset) {
        markdown = withContext(Dispatchers.IO) {
            runCatching { context.assets.open(library.licenseAsset).bufferedReader().use { it.readText() } }
                .getOrElse { "# License unavailable\n\nThe packaged license document could not be read." }
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().padding(horizontal = horizontalMargin, vertical = verticalMargin), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 620.dp).heightIn(max = dialogMaxHeight),
                shape = MaterialTheme.shapes.extraLarge,
                color = SurfaceRaised,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.34f)),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(Modifier.fillMaxWidth().heightIn(max = dialogMaxHeight)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
                            Icon(painterResource(R.drawable.ic_action_text), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(10.dp).size(21.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${library.name} license", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(library.licenseName, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = ComposeColor(0xFF0D0D0D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                    ) {
                        val text = markdown
                        if (text == null) {
                            Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                                Text("Loading license…", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            ReleaseNotesContent(text, Modifier.fillMaxSize())
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), horizontalArrangement = Arrangement.End) {
                        ExpressiveContainedIconTextButton("Close", R.drawable.ic_close, onClick = onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseSubmissionFailureDialog(log: String, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = SurfaceRaised,
        tonalElevation = 0.dp,
        title = { DatabaseFailureDialogTitle() },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("The Database submission did not complete successfully. The complete bounded submission log is shown below.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Surface(shape = MaterialTheme.shapes.medium, color = ComposeColor(0xFF0D0D0D), border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)) {
                    Text(
                        log,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).padding(14.dp),
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = { ExpressiveContainedIconTextButton("Copy all", R.drawable.ic_copy) { copyEvidenceText(context, "OpenGLESScope Database submission log", log) } },
        dismissButton = { ExpressiveContainedIconTextButton("Close", R.drawable.ic_close, onClick = onDismiss) }
    )
}

@Composable
private fun DatabaseFailureDialogTitle() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
            Box(Modifier.padding(8.dp).size(24.dp)) {
                Icon(painterResource(R.drawable.ic_database_submit), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(19.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = SurfaceRaised, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = null, tint = ComposeColor(0xFFFF6B6B), modifier = Modifier.padding(1.dp).size(10.dp))
                }
            }
        }
        Text("Database submission failed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

private fun copyEvidenceText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(trademarkApiDisplayText(label), text))
}

@Composable
private fun InfoPage(activity: MainActivity, report: GlReport, display: DisplayInfo, collectionReady: Boolean, contentMode: InfoContentMode = InfoContentMode.ALL) {
    val context = activity as Context
    val networkAvailable = LocalValidatedNetwork.current
    val scope = rememberCoroutineScope()
    val abi = detectInstalledAbi(context)
    var submitState by remember { mutableStateOf("Ready") }
    var submissionInFlight by remember { mutableStateOf(false) }
    var submissionSuccessId by remember { mutableStateOf<String?>(null) }
    var submissionFailureLog by remember { mutableStateOf<String?>(null) }
    var selectedLibraryLicense by remember { mutableStateOf<LibraryVersionInfo?>(null) }
    val completeReportReady = collectionReady && report.available
    val exportStem = remember(report) { "OpenGLESScope-${safeFilePart(report.renderer.ifBlank { "Unknown-GPU" })}-report" }
    var pendingExportFilename by rememberSaveable { mutableStateOf("") }
    var pendingExportPath by rememberSaveable { mutableStateOf("") }
    var pendingExportMime by rememberSaveable { mutableStateOf("") }
    var exportPreparing by remember { mutableStateOf(false) }
    fun pendingSnapshot(): ExportSnapshot? {
        if (pendingExportFilename.isBlank() || pendingExportPath.isBlank() || pendingExportMime !in setOf("text/plain", "text/html")) return null
        return ExportSnapshot(pendingExportFilename, pendingExportPath, pendingExportMime)
    }
    fun resetPendingSnapshot() {
        pendingExportFilename = ""
        pendingExportPath = ""
        pendingExportMime = ""
    }
    fun discardPendingSnapshot() {
        val snapshot = pendingSnapshot()
        resetPendingSnapshot()
        if (snapshot != null) scope.launch { withContext(Dispatchers.IO) { deleteExportSnapshot(context, snapshot) } }
    }
    fun prepareReportStorageExport(mime: String) {
        if (!completeReportReady || exportPreparing || pendingExportPath.isNotBlank() || mime !in setOf("text/plain", "text/html")) return
        exportPreparing = true
        val isHtml = mime == "text/html"
        val filename = "$exportStem.${if (isHtml) "html" else "txt"}"
        scope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    createExportSnapshot(context, filename, mime) {
                        if (isHtml) reportHtml(context, report, display) else reportText(context, report, display)
                    }
                }
                pendingExportFilename = snapshot.filename
                pendingExportPath = snapshot.path
                pendingExportMime = snapshot.mime
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Log.e("OpenGLESScope", "Report snapshot creation failed", error)
                android.widget.Toast.makeText(context, "${if (isHtml) "HTML" else "TXT"} report could not be prepared", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                exportPreparing = false
            }
        }
    }
    val showInfo = contentMode != InfoContentMode.REPORTS
    val showReporting = contentMode != InfoContentMode.INFO
    submissionFailureLog?.let { log -> DatabaseSubmissionFailureDialog(log = log, onDismiss = { submissionFailureLog = null }) }
    selectedLibraryLicense?.let { library -> LibraryLicenseDialog(library = library, onDismiss = { selectedLibraryLicense = null }) }
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { cleanupStaleExportSnapshots(context) } }
    pendingSnapshot()?.let { snapshot ->
        val isHtml = snapshot.mime == "text/html"
        SharedStorageBrowserDialog(
            request = SharedStorageBrowserRequest(
                title = if (isHtml) "Export HTML report" else "Export TXT report",
                description = "Choose a shared-storage folder for the complete ${if (isHtml) "offline HTML" else "plain-text"} OpenGLESScope report.",
                mode = SharedStorageBrowserMode.EXPORT,
                allowedExtensions = setOf(if (isHtml) "html" else "txt"),
                suggestedFileName = snapshot.filename
            ),
            onDismiss = { discardPendingSnapshot() },
            onExport = { destination ->
                try {
                    val saved = withContext(Dispatchers.IO) {
                        val target = validatedSharedStorageDestination(
                            destination.parentFile ?: error("Destination folder is unavailable"),
                            destination.name,
                            setOf(if (isHtml) "html" else "txt")
                        )
                        val source = validatedExportSnapshot(context, snapshot)
                        copySharedStorageFile(target, source)
                        target
                    }
                    android.widget.Toast.makeText(context, "${if (isHtml) "HTML" else "TXT"} report saved to ${saved.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                    Result.success("Saved ${saved.name}")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    Result.failure(error)
                }
            }
        )
    }
    OpenGLESScopeLazyPage(verticalSpacing = 14.dp) {
        if (showInfo) {
        item {
            CapabilitySectionCard("Developer") {
                ExpressiveIdentityBlock(
                    title = "Semih Boran",
                    subtitle = "EFI Shell · OpenGLESScope developer",
                    icon = R.drawable.ic_person
                )
                ExpressiveExternalLinkRow("Open GitHub profile", if (networkAvailable) "EFIShell0 · Projects and public profile" else "Unavailable without a validated internet connection", R.drawable.ic_action_github, enabled = networkAvailable) { open(activity, DEVELOPER_WEB) }
            }
        }
        item {
            CapabilitySectionCard("Application") {
                ExpressiveVersionBlock("OpenGLESScope", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString(), activity.packageName, abi)
                ExpressiveActionButton("Check for updates", when { !activity.directUpdatesEnabled -> "Direct GitHub updates are disabled in Settings"; !networkAvailable -> "Unavailable without a validated internet connection"; !activity.updateCheckInFlight -> "Official EFIShell0/OpenGLESScope GitHub release channel"; else -> "Checking official release channel…" }, R.drawable.ic_download, enabled = activity.directUpdatesEnabled && networkAvailable && !activity.updateCheckInFlight, trailingIcon = R.drawable.ic_receive, onClick = { activity.checkForApplicationUpdate(true) })
                ExpressiveExternalLinkRow("Open GitHub repository", if (networkAvailable) "Source, releases and project history" else "Unavailable without a validated internet connection", R.drawable.ic_action_github, enabled = networkAvailable) { open(activity, REPOSITORY_WEB) }
                Text(if (activity.directUpdatesEnabled) "Direct update checks use the official OpenGLESScope GitHub release channel. APK download still requires explicit review and confirmation." else "Direct GitHub update checks are currently disabled. Obtainium can manage updates from the official GitHub Releases source without OpenGLESScope running its own update discovery.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("Libraries") {
                Text("Direct application and native library identities are reported from the release's pinned build configuration. Build tools are listed separately and are not presented as runtime libraries.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                OPENGLESSCOPE_LIBRARY_VERSIONS.forEach { library ->
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CapabilityKeyValue(library.name, library.version)
                        Text(library.detail, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        Text(library.licenseName, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            ChevronAffordance("License", "Open ${library.name} license") { selectedLibraryLicense = library }
                        }
                    }
                }
            }
        }
        item {
            CapabilitySectionCard("Build toolchain") {
                CapabilityKeyValue("Android Gradle Plugin", BuildConfig.AGP_VERSION)
                CapabilityKeyValue("Kotlin Compose plugin", BuildConfig.KOTLIN_VERSION)
                CapabilityKeyValue("Gradle wrapper", BuildConfig.GRADLE_VERSION)
                CapabilityKeyValue("Android NDK", BuildConfig.NDK_VERSION)
                CapabilityKeyValue("compileSdk", "${BuildConfig.COMPILE_SDK_LEVEL}.${BuildConfig.COMPILE_SDK_MINOR_LEVEL}")
                CapabilityKeyValue("minSdk", BuildConfig.MIN_SDK_LEVEL.toString())
                CapabilityKeyValue("targetSdk", BuildConfig.TARGET_SDK_LEVEL.toString())
                CapabilityKeyValue("C++ language level", "C++20")
                CapabilityKeyValue("CMake minimum", "3.22.1")
                Text("These values are generated from the pinned release build configuration instead of being maintained as a separate UI copy.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
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
            CapabilitySectionCard("OpenGL® ES™ / EGL™ query engine") {
                CapabilityKeyValue("Normative API baseline", "OpenGL ES 3.2 · GLSL ES 3.20 (spec revision 8) · EGL 1.5")
                CapabilityKeyValue("Khronos OpenGL registry SHA-256", "b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3")
                CapabilityKeyValue("Khronos EGL registry SHA-256", "3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8")
                CapabilityKeyValue("Registry audit date", "2026-09-30")
                CapabilityKeyValue("Runtime OpenGL® ES™", report.glVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("Runtime GLSL ES", report.glslVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("Runtime EGL™", report.egl.version.ifBlank { "Unavailable" })
                CapabilityKeyValue("Initialized EGL™", report.egl.initializedVersion.ifBlank { "Unavailable" })
                CapabilityKeyValue("GL extension tokens", report.extensions.size.toString())
                CapabilityKeyValue("EGL™ display extension tokens", report.egl.extensions.size.toString())
                CapabilityKeyValue("EGL™ client extension tokens", report.egl.clientExtensions.size.toString())
                CapabilityKeyValue("Implementation limits", report.limits.size.toString())
                CapabilityKeyValue("Query diagnostics", report.diagnostics.size.toString())
                CapabilityKeyValue("EGL™ configs", report.eglConfigs.size.toString())
                CapabilityKeyValue("Report schema", "$SUBMISSION_SCHEMA_VERSION · technical report $TECHNICAL_REPORT_SCHEMA_VERSION")
                Text("Capability metadata is collected directly from the active system OpenGL® ES™/EGL™ implementation. Runtime extension tokens are preserved as reported. A value is not inferred when no validated core-version or exact-extension query path exists.", color = ComposeColor(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("About") {
                Text("OpenGLESScope is not an official Khronos Group project.", color = ComposeColor(0xFFFFC857), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("OpenGLESScope is an OpenGL® ES™ and EGL™ capability and device inspection utility for Android. It reports information exposed by the active OpenGL® ES™/EGL™ implementation while keeping Android display/HDR evidence separate from OpenGL® ES™/EGL™ capability claims.", color = ComposeColor(0xFFB0B0B0))
            }
        }
        }
        if (showReporting) {
        item {
            CapabilitySectionCard("Export complete report") {
                Text("TXT/HTML export uses OpenGLESScope's in-app shared-storage browser. Storage access is requested only when you explicitly start an export. Report serialization runs off the UI thread through a private temporary snapshot and the destination write is atomic.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SharedStoragePermissionActionButton(
                        "Export TXT",
                        when { exportPreparing -> "Preparing private report snapshot…"; completeReportReady -> "Plain-text complete report · choose shared-storage destination"; else -> "Waiting for complete OpenGL ES/EGL collection" },
                        R.drawable.ic_action_text,
                        Modifier.fillMaxWidth(),
                        completeReportReady && !exportPreparing && pendingExportPath.isBlank(),
                        false
                    ) { prepareReportStorageExport("text/plain") }
                    SharedStoragePermissionActionButton(
                        "Export HTML",
                        when { exportPreparing -> "Preparing private report snapshot…"; completeReportReady -> "Styled offline complete report · choose shared-storage destination"; else -> "Waiting for complete OpenGL ES/EGL collection" },
                        R.drawable.ic_action_html,
                        Modifier.fillMaxWidth(),
                        completeReportReady && !exportPreparing && pendingExportPath.isBlank(),
                        false
                    ) { prepareReportStorageExport("text/html") }
                }
                if (!completeReportReady) Text("TXT and HTML export remain disabled until the complete OpenGL® ES™/EGL™ collection pass has finished, matching the Database completeness gate.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            CapabilitySectionCard("OpenGLESScope Database") {
                Text("Submit the complete technical OpenGLESScope report to the public database. Capability fields cannot be selectively omitted, and sensitive device identifiers or private paths are not included.", color = ComposeColor(0xFFB6ACAE), style = MaterialTheme.typography.bodySmall)
                TransientActionButton(
                    if (submissionInFlight) "Submitting…" else "Submit complete report",
                    when {
                        submissionInFlight -> "Uploading the complete technical dataset"
                        !completeReportReady && !networkAvailable -> "Waiting for complete OpenGL ES/EGL collection · internet unavailable"
                        !completeReportReady -> "Waiting for complete OpenGL ES/EGL collection"
                        !networkAvailable -> "Unavailable without a validated internet connection"
                        else -> "Structured JSON + canonical TXT report"
                    },
                    R.drawable.ic_database_submit,
                    enabled = !submissionInFlight && completeReportReady && networkAvailable,
                    idleTrailingIcon = R.drawable.ic_upload
                ) {
                    if (submissionInFlight || !completeReportReady || !networkAvailable) false else {
                        submissionInFlight = true
                        submissionSuccessId = null
                        submissionFailureLog = null
                        submitState = "Submitting complete technical report…"
                        try {
                            val result = submitReport(context, report, display)
                            submitState = result.summary
                            if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log
                            result.success
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Throwable) {
                            submitState = "Submission failed: ${error.message ?: error.javaClass.simpleName}"
                            submissionFailureLog = databaseSubmissionExceptionLog("unexpected", error)
                            false
                        } finally {
                            submissionInFlight = false
                        }
                    }
                }
                Text(
                    "Compatibility notice: when a newer OpenGLESScope release raises the Database submission floor, reports from older app versions are rejected by the server. A rejection is returned as a submission error and is shown here instead of being treated as a successful upload.",
                    color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall
                )
                Text(submitState, color = ComposeColor(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall)
                submissionSuccessId?.let { reportId ->
                    Text("Report ID", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceLow,
                        contentColor = TextPrimary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(reportId, modifier = Modifier.weight(1f), color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            TransientIconButton(idleIcon = R.drawable.ic_copy, contentDescription = "Copy report ID") {
                                runCatching { copyEvidenceText(context, "OpenGLESScope report ID", reportId) }.isSuccess
                            }
                        }
                    }
                    ExpressiveContainedIconTextButton("Open report", R.drawable.ic_open_external, modifier = Modifier.fillMaxWidth(), enabled = networkAvailable) {
                        databaseReportUrl(reportId)?.let { open(activity, it) }
                    }
                }
                if (!completeReportReady && !networkAvailable) {
                    Text("Database submission is locked for two independent reasons: OpenGL® ES™/EGL™ collection is incomplete and Android does not report a validated internet connection.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
                    Text("When internet returns during collection, the network lock clears immediately; submission still waits for complete report evidence.", color = ComposeColor(0xFF9CCBFF), style = MaterialTheme.typography.bodySmall)
                } else {
                    if (!completeReportReady) Text("Wait for the complete OpenGL® ES™/EGL™ collection pass to finish before submitting.", color = ComposeColor(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
                    if (!networkAvailable) Text("Database upload and public report browsing are disabled until Android reports a validated internet connection.", color = ComposeColor(0xFF9CCBFF), style = MaterialTheme.typography.bodySmall)
                }
                ExpressiveExternalLinkRow("Open OpenGLESScope Database", if (networkAvailable) "Browse public OpenGLESScope hardware reports" else "Unavailable without a validated internet connection", R.drawable.ic_database_browse, enabled = networkAvailable) { open(activity, DATABASE_WEB) }
                Text("Submission is explicit and user-initiated. The fixed official HTTPS endpoint is used automatically; no report is uploaded automatically or in the background.", color = ComposeColor(0xFF777777), style = MaterialTheme.typography.bodySmall)
            }
        }
        }
    }
}

@Composable
private fun CapabilityListPage(title: String, rows: List<Pair<String, String>>) {
    var page by rememberSaveable(title) { mutableIntStateOf(0) }
    val pageCount = maxOf(1, (rows.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(rows.size, pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visibleRows = remember(rows, page) { rows.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item { CapabilitySectionCard(title) { Text("Runtime-reported entries only.", color = TextMuted, style = MaterialTheme.typography.bodySmall) } }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf("Entries" to rows.size.toString(), "Showing" to if (rows.isEmpty()) "0" else "${first + 1}-${first + visibleRows.size}"))
        }
        stickyCollectionPager(rows.size, page, { page = it })
        items(visibleRows, key = { it.first }) { (a, b) -> CapabilityItemCard { CapabilityKeyValue(a, b) } }
    }
}

@Composable
private fun SearchRows(title: String, rows: List<Pair<String, String>>) {
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable(title) { mutableIntStateOf(0) }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    val pageCount = maxOf(1, (filtered.size + COLLECTION_PAGE_SIZE - 1) / COLLECTION_PAGE_SIZE)
    LaunchedEffect(query, filtered.size) { page = 0 }
    LaunchedEffect(pageCount) { page = page.coerceIn(0, pageCount - 1) }
    val visibleRows = remember(filtered, page) { filtered.drop(page * COLLECTION_PAGE_SIZE).take(COLLECTION_PAGE_SIZE) }
    OpenGLESScopeLazyPage(verticalSpacing = 10.dp) {
        item {
            CapabilitySectionCard(title) {
                ExpressiveSearchField(value = query, onValueChange = { query = it }, placeholderText = "Search…", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None))
            }
        }
        item {
            val first = page * COLLECTION_PAGE_SIZE
            ExpressiveMetricGrid(listOf(
                "Matching entries" to filtered.size.toString(),
                "Total entries" to rows.size.toString(),
                "Showing" to if (filtered.isEmpty()) "0" else "${first + 1}-${first + visibleRows.size}"
            ))
        }
        stickyCollectionPager(filtered.size, page, { page = it })
        items(visibleRows, key = { it.first }) { (a, b) -> CapabilityItemCard { CapabilityKeyValue(a, b) } }
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
private fun TransientStatusOverlayHost(
    collectionStatus: CollectionStatus,
    collecting: Boolean,
    networkStateKnown: Boolean,
    networkAvailable: Boolean,
    networkBannerState: NetworkBannerState,
    updateStatus: UpdateStatus,
    onInstallUpdate: (AppUpdate) -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayVisible = collectionStatus != CollectionStatus.IDLE ||
        updateStatus !is UpdateStatus.Hidden ||
        networkBannerState != NetworkBannerState.HIDDEN ||
        (networkStateKnown && !networkAvailable)
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (overlayVisible) 8.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CollectionStatusBanner(collectionStatus)
        ConnectivityStatusHost(collectionStatus, networkStateKnown, networkAvailable, networkBannerState)
        UpdateStatusBanner(updateStatus, onInstallUpdate)
    }
}

@Composable
private fun ConnectivityStatusHost(collectionStatus: CollectionStatus, networkStateKnown: Boolean, networkAvailable: Boolean, transitionState: NetworkBannerState) {
    var showPersistentOffline by remember { mutableStateOf(false) }
    LaunchedEffect(networkStateKnown, networkAvailable, transitionState) {
        showPersistentOffline = false
        if (networkStateKnown && !networkAvailable && transitionState == NetworkBannerState.HIDDEN) {
            delay(380L)
            showPersistentOffline = true
        }
    }
    NetworkStatusBanner(transitionState)
    AnimatedVisibility(
        visible = showPersistentOffline,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(220)) + shrinkVertically(animationSpec = tween(220))
    ) {
        OfflineFeatureAvailabilityBanner(collectionStatus == CollectionStatus.COLLECTING)
    }
}

@Composable
private fun FloatingStatusSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().widthIn(max = 520.dp),
        color = ComposeColor(0xD61A1A1F),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor(0x423A3438)),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) { content() }
}

@Composable
private fun OfflineFeatureAvailabilityBanner(collectionInProgress: Boolean) {
    FloatingStatusSurface(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(50), color = ComposeColor(0xFF16344F), modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(R.drawable.ic_info), contentDescription = null, tint = ComposeColor(0xFF5CA9FF), modifier = Modifier.size(17.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Internet features are unavailable", color = ComposeColor(0xFF9CCBFF), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    if (collectionInProgress) "OpenGL® ES™/EGL™ collection continues offline. Internet-dependent actions remain locked by network state, while report-dependent actions also remain locked until collection completes."
                    else "OpenGL® ES™/EGL™ inspection stays available offline. Database submission/fetching, web links and update checks remain disabled until Android reports a validated internet connection.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun NetworkStatusBanner(state: NetworkBannerState) {
    var renderedState by remember { mutableStateOf(NetworkBannerState.CONNECTED) }
    LaunchedEffect(state) { if (state != NetworkBannerState.HIDDEN) renderedState = state }
    AnimatedVisibility(
        visible = state != NetworkBannerState.HIDDEN,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(360)) + shrinkVertically(animationSpec = tween(360))
    ) {
        val connected = renderedState == NetworkBannerState.CONNECTED
        val stateColor = if (connected) ComposeColor(0xFF55D98A) else ComposeColor(0xFFFF7676)
        val stateContainer = if (connected) ComposeColor(0xFF163D24) else ComposeColor(0xFF431C20)
        FloatingStatusSurface(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(50), color = stateContainer, modifier = Modifier.size(30.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (connected) R.drawable.ic_network_connected else R.drawable.ic_network_disconnected),
                            contentDescription = null,
                            tint = stateColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(if (connected) "Connected to network" else "No internet connection", color = stateColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(if (connected) "Android reports a validated default network." else "Android does not currently report a validated default network.", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableIcon() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BrandContainer,
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_update_available),
                contentDescription = null,
                tint = BrandSoft,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UpdateSourceIcon() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BrandContainer,
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                tint = BrandSoft,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UpdateStatusBadge(label: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF163D24)) {
        Text(trademarkApiDisplayText(label), color = ComposeColor(0xFF55D98A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
    }
}

@Composable
private fun UpdateStatusBanner(status: UpdateStatus, onInstall: (AppUpdate) -> Unit) {
    AnimatedVisibility(
        visible = status !is UpdateStatus.Hidden,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) + expandVertically(animationSpec = androidx.compose.animation.core.tween(220)),
        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(360)) + shrinkVertically(animationSpec = androidx.compose.animation.core.tween(360))
    ) {
        FloatingStatusSurface(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (status) {
                    UpdateStatus.Checking -> { ExpressiveLinearProgressIndicator(Modifier.width(72.dp)); Text("Checking for updates…", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    UpdateStatus.UpToDate -> { UpdateStatusBadge("UP TO DATE"); Text("OpenGLESScope is up to date.", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    UpdateStatus.DirectUpdatesDisabledIntro -> { UpdateSourceIcon(); Text("Direct GitHub updates are currently disabled. Obtainium can manage updates externally, or direct updates can be enabled in Settings.", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    is UpdateStatus.Available -> { UpdateAvailableIcon(); Text("OpenGLESScope ${status.update.version} available", color = BrandSoft, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); ChevronAffordance("Review", "Review update") { onInstall(status.update) } }
                    is UpdateStatus.Downloading -> { ExpressiveLinearProgressIndicator(Modifier.width(72.dp)); Text("Downloading update…", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f)) }
                    is UpdateStatus.Failed -> Text(status.message, color = ComposeColor(0xFFFF8A8A), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    UpdateStatus.Hidden -> Unit
                }
            }
        }
    }
}

@Composable
private fun CollectionStatusBanner(status: CollectionStatus) {
    AnimatedVisibility(
        visible = status != CollectionStatus.IDLE,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(260)) + expandVertically(animationSpec = androidx.compose.animation.core.tween(260)),
        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(420)) + shrinkVertically(animationSpec = androidx.compose.animation.core.tween(420))
    ) {
        val collecting = status == CollectionStatus.COLLECTING
        val failed = status == CollectionStatus.FAILED
        FloatingStatusSurface(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (collecting) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = BrandContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_action_update),
                                    contentDescription = null,
                                    tint = BrandSoft,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            "OpenGLESScope is collecting OpenGL ES and EGL information in the background.",
                            color = ComposeColor(0xFF9E9E9E),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val stateColor = if (failed) ComposeColor(0xFFFF7676) else ComposeColor(0xFF55D98A)
                        val stateContainer = if (failed) ComposeColor(0xFF431C20) else ComposeColor(0xFF163D24)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = stateContainer,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(if (failed) R.drawable.ic_close else R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = stateColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            if (failed) "Failed" else "Completed",
                            color = stateColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (failed) "No complete OpenGL ES/EGL information was collected." else "OpenGL ES/EGL information updated.",
                            color = ComposeColor(0xFF9E9E9E),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (collecting) ExpressiveLinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingView() {
    OpenGLESScopeLazyPage(verticalSpacing = 12.dp, modifier = Modifier.background(SurfaceDark).semantics { liveRegion = LiveRegionMode.Polite }) {
        item {
            CapabilitySectionCard("OpenGL® ES™ / EGL™ inspection") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(shape = MaterialTheme.shapes.large, color = BrandContainer) {
                        Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                            LoadingIndicator(color = BrandSoft, modifier = Modifier.size(34.dp))
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Inspecting OpenGL® ES™ / EGL™…", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Collecting the complete graphics evidence set for this session.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Surface(shape = MaterialTheme.shapes.medium, color = SurfaceTonal) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Collection in progress", color = TextPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text("Capability pages become authoritative only after the base report reaches a validated terminal state. Missing evidence is never converted into Unsupported while collection is incomplete.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                ExpressiveLinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Supported, Unsupported, Unavailable, Not applicable and Unknown remain separate evidence states.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(color = SurfaceLow, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = SurfaceTonal) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(9.dp).size(20.dp))
            }
            Text(trademarkApiDisplayText(message), color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

private fun capabilitySectionIcon(title: String): Int = when {
    title.equals("Display", true) -> R.drawable.ic_tablet
    title.equals("Developer", true) -> R.drawable.ic_code
    title.equals("Application", true) -> R.drawable.openglesscope_scope_wordmark
    title.equals("Libraries", true) -> R.drawable.ic_library
    title.equals("Build toolchain", true) -> R.drawable.ic_build
    title.equals("Device ABI", true) -> R.drawable.ic_cpu
    title.equals("Android", true) || title.equals("Android runtime", true) || title.equals("Operating system", true) -> R.drawable.ic_android_brand
    title.equals("Updates", true) || title.equals("Update preferences", true) -> R.drawable.ic_download
    title.equals("Encyclopedia", true) -> R.drawable.ic_book
    title.equals("How to read encyclopedia entries", true) -> R.drawable.ic_question
    title.equals("Reference search", true) -> R.drawable.ic_search
    title.equals("Evidence boundary", true) -> R.drawable.ic_shield
    title.equals("Opening animation", true) -> R.drawable.ic_opening_animation_toggle
    title.equals("Analysis workspace", true) -> R.drawable.ic_analysis
    title.equals("Collection integrity score", true) -> R.drawable.ic_shield
    title.equals("Scoring method", true) -> R.drawable.ic_evidence
    title.equals("What this score does not mean", true) -> R.drawable.ic_question
    title.equals("Dependency graph explorer", true) -> R.drawable.ic_graph
    title.equals("Interactive query-gate map", true) -> R.drawable.ic_compass
    title.equals("Global OpenGL® ES™/EGL™ report search", true) -> R.drawable.ic_search
    title.equals("Result semantics", true) -> R.drawable.ic_question
    title.equals("Current custom evaluation", true) -> R.drawable.ic_check
    title.equals("Saved minimum profiles", true) -> R.drawable.ic_save
    title.equals("Custom minimum profiles", true) -> R.drawable.ic_profile
    title.equals("Database comparison summary", true) -> R.drawable.ic_compare
    title.equals("Registry references", true) -> R.drawable.ic_registry
    title.equals("Additional retained evidence", true) -> R.drawable.ic_evidence
    title.equals("QR payload", true) -> R.drawable.ic_qr
    title.equals("Probe and scheduler timing", true) -> R.drawable.ic_evidence
    title.equals("Session History", true) || title.equals("Local session history", true) -> R.drawable.ic_history
    title.equals("Watched evidence", true) -> R.drawable.ic_watch_add
    title.equals("Offline report compare", true) || title.equals("Diff summary", true) || title.equals("Database diff summary", true) -> R.drawable.ic_compare
    title.equals("Runtime/query dependency graph", true) || title.equals("Visual query-gate graph", true) || title.equals("Capability dependency graph", true) || title.equals("Visual registry-reference graph", true) -> R.drawable.ic_graph
    title.equals("Optional active tests", true) || title.equals("OpenGL® ES™ active self-tests", true) -> R.drawable.ic_test
    title.equals("Self-test result", true) || title.equals("Test result summary", true) -> R.drawable.ic_self_test
    title.equals("OpenGL® ES™ / EGL™ query engine", true) || title.equals("Raw technicalReport tree", true) || title.equals("Raw structured technicalReport", true) -> R.drawable.ic_registry
    title.equals("Export complete report", true) -> R.drawable.ic_surface
    title.equals("Database permalink / Share", true) -> R.drawable.ic_qr
    title.equals("OpenGLESScope Database Compare", true) || title.equals("OpenGLESScope Database", true) -> R.drawable.ic_action_database
    title.contains("Database", true) -> R.drawable.ic_action_database
    title.contains("profile", true) -> R.drawable.ic_profile
    title.contains("diagnostic", true) || title.equals("Query overview", true) || title.equals("Collection diagnostics", true) || title.equals("EGL capability queries", true) -> R.drawable.ic_evidence
    title.equals("Extension explorer", true) -> R.drawable.ic_extensions
    title.equals("Explore", true) -> R.drawable.ic_compass
    title.contains("quick access", true) -> R.drawable.ic_quick_access_grid
    title.contains("snapshot", true) || title.contains("inspection", true) -> R.drawable.ic_opengles_gl_es
    title.equals("EGL Configs", true) || title.startsWith("EGL Config ", true) -> R.drawable.ic_configs
    title.equals("EGL identity", true) || title.equals("Current EGL binding and context", true) || title.equals("Collector pbuffer", true) -> R.drawable.ic_egl_official
    title.contains("display", true) || title.contains("HDR", true) -> R.drawable.ic_display
    title.contains("surface", true) || title.contains("present", true) -> R.drawable.ic_surface
    title.contains("feature", true) || title.contains("requirement", true) || title.contains("evaluation", true) || title.contains("minimum", true) -> R.drawable.ic_features
    title.contains("format", true) || title.contains("color space", true) -> R.drawable.ic_formats
    title.contains("extension", true) -> R.drawable.ic_extensions
    title.equals("Shader precision", true) -> R.drawable.ic_precision
    title.contains("property", true) || title.contains("limit", true) || title.contains("runtime", true) -> R.drawable.ic_properties
    else -> R.drawable.ic_info
}

@Composable
private fun tvBrowseModifier(shape: Shape, enabled: Boolean = true): Modifier {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focused) { if (focused) requester.bringIntoView() }
    return Modifier
        .bringIntoViewRequester(requester)
        .onFocusChanged { state -> focused = state.isFocused }
        .focusable(enabled = enabled)
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
private fun FileManagerNavigateArrow(enabled: Boolean, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val rtl = LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
    Surface(
        shape = shape,
        color = if (enabled) BrandContainer else SurfaceLow,
        contentColor = if (enabled) BrandSoft else TextMuted,
        modifier = Modifier.size(44.dp).clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onClick).then(tvBrowseModifier(shape))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(painterResource(if (rtl) R.drawable.ic_chevron_left else R.drawable.ic_chevron_right), contentDescription = description, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun SharedStoragePermissionActionButton(
    title: String,
    subtitle: String,
    icon: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    compact: Boolean = false,
    onGranted: () -> Unit
) {
    val requestAccess = LocalSharedStorageAccessRequest.current
    val scope = rememberCoroutineScope()
    var denied by remember { mutableStateOf(false) }
    var feedbackGeneration by remember { mutableIntStateOf(0) }
    AnimatedContent(targetState = denied, label = "sharedStoragePermissionFeedback") { permissionDenied ->
        ExpressiveActionButton(
            title = if (permissionDenied) "Permission denied" else title,
            subtitle = if (permissionDenied) "Shared-storage access was not granted" else subtitle,
            icon = if (permissionDenied) R.drawable.ic_close else icon,
            modifier = modifier,
            enabled = enabled,
            compact = compact,
            trailingIcon = if (permissionDenied) R.drawable.ic_close else R.drawable.ic_chevron_right,
            trailingTint = if (permissionDenied) ComposeColor(0xFFFF6B6B) else null
        ) {
            if (!enabled) return@ExpressiveActionButton
            requestAccess(
                {
                    denied = false
                    onGranted()
                },
                {
                    feedbackGeneration += 1
                    val generation = feedbackGeneration
                    denied = true
                    scope.launch {
                        delay(3_000L)
                        if (feedbackGeneration == generation) denied = false
                    }
                }
            )
        }
    }
}

private data class FileManagerBreadcrumb(val label: String, val path: String)

private fun fileManagerBreadcrumbs(rootPath: String, directoryPath: String): List<FileManagerBreadcrumb> {
    val root = File(rootPath)
    val current = File(directoryPath)
    val rootText = root.path.trimEnd(File.separatorChar)
    val currentText = current.path.trimEnd(File.separatorChar)
    if (rootText.isBlank() || currentText.isBlank()) return emptyList()
    val relative = if (currentText == rootText) "" else currentText.removePrefix(rootText).trim(File.separatorChar)
    val crumbs = mutableListOf(FileManagerBreadcrumb("Storage", rootText))
    if (relative.isNotBlank()) {
        var path = rootText
        relative.split(File.separatorChar).filter { it.isNotBlank() }.forEach { segment ->
            path += File.separator + segment
            crumbs += FileManagerBreadcrumb(segment, path)
        }
    }
    return crumbs
}

@Composable
private fun FileManagerBreadcrumbBar(
    rootPath: String,
    directoryPath: String,
    enabled: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val crumbs = remember(rootPath, directoryPath) { fileManagerBreadcrumbs(rootPath, directoryPath) }
    val scrollState = rememberScrollState()
    val isRtl = LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        crumbs.forEachIndexed { index, crumb ->
            if (index > 0) Text(if (isRtl) "‹" else "›", color = TextMuted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            val active = index == crumbs.lastIndex
            val shape = RoundedCornerShape(12.dp)
            val crumbContainer by animateColorAsState(if (active) BrandContainer else SurfaceLow, tween(210), label = "breadcrumbContainer")
            val crumbContent by animateColorAsState(if (active) BrandSoft else TextSecondary, tween(210), label = "breadcrumbContent")
            val crumbBorder by animateColorAsState(if (active) BrandSoft.copy(alpha = 0.46f) else OutlineVariant, tween(210), label = "breadcrumbBorder")
            Surface(
                shape = shape,
                color = crumbContainer,
                contentColor = crumbContent,
                border = androidx.compose.foundation.BorderStroke(1.dp, crumbBorder),
                modifier = Modifier
                    .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                    .clip(shape)
                    .clickable(enabled = enabled && !active, role = Role.Button) { onNavigate(crumb.path) }
                    .then(tvBrowseModifier(shape, enabled && !active))
            ) {
                Text(
                    crumb.label,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    color = crumbContent,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedStorageBrowserDialog(
    request: SharedStorageBrowserRequest,
    onDismiss: () -> Unit,
    onImport: suspend (File) -> Result<String> = { Result.failure(IllegalStateException("Import is unavailable")) },
    onExport: suspend (File) -> Result<String> = { Result.failure(IllegalStateException("Export is unavailable")) }
) {
    val root = remember { runCatching { sharedStorageRoot() }.getOrNull() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val suppressDesktopSecondaryInput = remember(context) { isChromeOsRuntime(context) || isAndroidPcFormFactor(context) }
    val fileManagerPrefs = remember(context) { context.getSharedPreferences("file_manager", Context.MODE_PRIVATE) }
    var directoryPath by remember(request.title) { mutableStateOf(root?.path.orEmpty()) }
    var listing by remember(request.title) { mutableStateOf(SharedStorageDirectoryListing(emptyList(), emptyList(), false)) }
    var loading by remember(request.title) { mutableStateOf(root != null) }
    var busy by remember(request.title) { mutableStateOf(false) }
    var status by remember(request.title) { mutableStateOf<String?>(if (root == null) "Shared storage is unavailable" else null) }
    var search by remember(request.title, directoryPath) { mutableStateOf("") }
    var viewMode by remember {
        mutableStateOf(runCatching {
            FileManagerViewMode.valueOf(fileManagerPrefs.getString("shared_view_mode", FileManagerViewMode.LIST.name) ?: FileManagerViewMode.LIST.name)
        }.getOrDefault(FileManagerViewMode.LIST))
    }
    var sortMode by remember {
        mutableStateOf(runCatching {
            FileManagerSortMode.valueOf(fileManagerPrefs.getString("shared_sort_mode", FileManagerSortMode.NAME_ASC.name) ?: FileManagerSortMode.NAME_ASC.name)
        }.getOrDefault(FileManagerSortMode.NAME_ASC))
    }
    val exportExtension = remember(request.mode, request.allowedExtensions) {
        if (request.mode == SharedStorageBrowserMode.EXPORT) request.allowedExtensions.singleOrNull()?.lowercase(java.util.Locale.ROOT) else null
    }
    val suggestedBase = remember(request.title, exportExtension) {
        val suggested = request.suggestedFileName.take(180)
        val suffix = exportExtension?.let { ".$it" }.orEmpty()
        if (suffix.isNotEmpty() && suggested.endsWith(suffix, ignoreCase = true)) suggested.dropLast(suffix.length) else suggested
    }
    var filenameBase by remember(request.title) { mutableStateOf(suggestedBase.take(160)) }
    var pendingOverwrite by remember(request.title) { mutableStateOf<File?>(null) }
    val directory = remember(directoryPath) { directoryPath.takeIf { it.isNotBlank() }?.let(::File) }
    val atRoot = root != null && directoryPath == root.path
    val filteredFolders = remember(listing.folders, search, sortMode) {
        fileManagerSortFolders(if (search.isBlank()) listing.folders else listing.folders.filter { File(it).name.contains(search, true) }, sortMode)
    }
    val filteredFiles = remember(listing.files, search, sortMode) {
        fileManagerSortFiles(if (search.isBlank()) listing.files else listing.files.filter { it.name.contains(search, true) }, sortMode)
    }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val navigationStartInset = navigationPadding.calculateLeftPadding(layoutDirection)
    val navigationEndInset = navigationPadding.calculateRightPadding(layoutDirection)
    val navigationBottomInset = navigationPadding.calculateBottomPadding()
    var screenVisible by remember(request.title) { mutableStateOf(false) }
    var closing by remember(request.title) { mutableStateOf(false) }
    var searchExpanded by rememberSaveable(request.title) { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { screenVisible = true }
    LaunchedEffect(searchExpanded, busy) {
        if (searchExpanded && !busy) runCatching { searchFocusRequester.requestFocus() }
    }
    fun requestClose() {
        if (busy || closing) return
        closing = true
        focusManager.clearFocus(force = true)
        screenVisible = false
        scope.launch {
            delay(220L)
            onDismiss()
        }
    }

    LaunchedEffect(directoryPath, request.mode, request.allowedExtensions) {
        val scanRoot = root ?: return@LaunchedEffect
        val target = directory ?: return@LaunchedEffect
        loading = true
        status = null
        listing = SharedStorageDirectoryListing(emptyList(), emptyList(), false)
        try {
            val scanned = withContext(Dispatchers.IO) { scanSharedStorageDirectory(scanRoot, target, request.allowedExtensions, request.mode == SharedStorageBrowserMode.IMPORT) }
            listing = scanned
            if (scanned.entryLimitReached) status = "This folder reached the bounded 4096-entry scan limit."
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            status = error.message ?: "Unable to read this folder"
        } finally {
            loading = false
        }
    }

    fun navigateUp() {
        val scanRoot = root ?: return
        val current = directory ?: return
        if (current.path == scanRoot.path || loading || busy) return
        val parent = runCatching { current.parentFile?.canonicalFile }.getOrNull() ?: return
        if (isCanonicalSharedStoragePath(scanRoot, parent)) directoryPath = parent.path
    }

    fun runExport(destination: File) {
        if (busy) return
        busy = true
        status = "Saving…"
        scope.launch {
            val result = try { onExport(destination) }
            catch (cancelled: CancellationException) { busy = false; throw cancelled }
            catch (error: Throwable) { Result.failure(error) }
            busy = false
            result.onSuccess { status = it; requestClose() }.onFailure { status = "Export failed: ${it.message ?: it.javaClass.simpleName}" }
        }
    }

    fun runImport(entry: SharedStorageFileEntry) {
        if (busy) return
        busy = true
        status = "Validating ${entry.name}…"
        scope.launch {
            val result = try { onImport(File(entry.path)) }
            catch (cancelled: CancellationException) { busy = false; throw cancelled }
            catch (error: Throwable) { Result.failure(error) }
            busy = false
            result.onSuccess { status = it; requestClose() }.onFailure { status = "Import failed: ${it.message ?: it.javaClass.simpleName}" }
        }
    }


    val headerContent: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnimatedHeaderActionButton(
                visible = !atRoot,
                icon = R.drawable.ic_back,
                contentDescription = "Parent folder",
                onClick = ::navigateUp,
                enabled = !loading && !busy,
                buttonSize = 42.dp,
                slotSize = 50.dp
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(request.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (root != null) {
                    FileManagerBreadcrumbBar(
                        rootPath = root.path,
                        directoryPath = directoryPath,
                        enabled = !loading && !busy,
                        onNavigate = { directoryPath = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Shared storage unavailable", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            HeaderActionButton(icon = R.drawable.ic_close, contentDescription = "Close", onClick = ::requestClose, enabled = !busy)
        }
    }

    val descriptionContent: @Composable () -> Unit = {
        Text(request.description, modifier = Modifier.fillMaxWidth(), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }

    val searchContent: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(240, easing = FastOutSlowInEasing)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedContent(
                targetState = searchExpanded,
                transitionSpec = {
                    (fadeIn(tween(180)) + expandHorizontally(tween(240, easing = FastOutSlowInEasing), expandFrom = Alignment.End)) togetherWith
                        (fadeOut(tween(130)) + shrinkHorizontally(tween(210, easing = FastOutSlowInEasing), shrinkTowards = Alignment.End))
                },
                label = "sharedFileManagerSearchResize",
                modifier = Modifier.weight(1f).animateContentSize(animationSpec = tween(240, easing = FastOutSlowInEasing))
            ) { expanded ->
                if (expanded) {
                    ExpressiveSearchField(
                        value = search,
                        onValueChange = { search = it.take(120) },
                        modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                        enabled = !busy,
                        placeholderText = if (request.mode == SharedStorageBrowserMode.IMPORT) "Folders and supported files" else "Folders"
                    )
                } else {
                    val visibleFileCount = if (request.mode == SharedStorageBrowserMode.IMPORT) filteredFiles.size else 0
                    Text(
                        if (request.mode == SharedStorageBrowserMode.IMPORT) "${filteredFolders.size} folders · $visibleFileCount files" else "${filteredFolders.size} folders",
                        modifier = Modifier.fillMaxWidth(),
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            FileManagerOptionsChooser(
                viewMode = viewMode,
                sortMode = sortMode,
                onViewMode = { mode ->
                    viewMode = mode
                    fileManagerPrefs.edit().putString("shared_view_mode", mode.name).apply()
                },
                onSortMode = { mode ->
                    sortMode = mode
                    fileManagerPrefs.edit().putString("shared_sort_mode", mode.name).apply()
                },
                enabled = !busy,
                modifier = Modifier.width(50.dp)
            )
            AnimatedContent(
                targetState = searchExpanded,
                transitionSpec = { fadeIn(tween(160)) + scaleIn(tween(190), initialScale = 0.82f) togetherWith fadeOut(tween(120)) + scaleOut(tween(150), targetScale = 0.82f) },
                label = "sharedFileManagerSearchAction"
            ) { expanded ->
                HeaderActionButton(
                    icon = if (expanded) R.drawable.ic_close else R.drawable.ic_search,
                    contentDescription = if (expanded) "Close search" else "Search files and folders",
                    onClick = {
                        if (expanded) {
                            search = ""
                            focusManager.clearFocus(force = true)
                            searchExpanded = false
                        } else searchExpanded = true
                    },
                    enabled = !busy,
                    size = if (expanded) 46.dp else 48.dp
                )
            }
        }
    }

    val exportNameContent: @Composable () -> Unit = {
        if (request.mode == SharedStorageBrowserMode.EXPORT) {
            Surface(shape = MaterialTheme.shapes.large, color = SurfaceTonal, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File name", color = TextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = filenameBase,
                            onValueChange = { value -> filenameBase = value.take(160).filterNot { it == '/' || it == '\\' || it.code < 0x20 } },
                            modifier = Modifier.weight(1f),
                            enabled = !busy,
                            singleLine = true,
                            label = { Text("Name") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandSoft, unfocusedBorderColor = Outline, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = BrandSoft)
                        )
                        Surface(shape = RoundedCornerShape(16.dp), color = BrandContainer, contentColor = BrandSoft, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.35f))) {
                            Text(exportExtension?.let { ".$it" } ?: "type", modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp), color = BrandSoft, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Text("The file type is fixed for this export; only the name can be changed.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    val statusContent: @Composable () -> Unit = {
        AnimatedContent(
            targetState = status,
            transitionSpec = { (fadeIn(tween(180)) + expandVertically(tween(220))) togetherWith (fadeOut(tween(140)) + shrinkVertically(tween(180))) },
            label = "sharedStorageImportExportState",
            modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
        ) { message ->
            if (message != null) {
                val warning = listOf("failed", "invalid", "unavailable", "limit", "denied", "unable", "error").any { message.contains(it, true) }
                Surface(modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }, shape = MaterialTheme.shapes.medium, color = if (warning) ComposeColor(0xFF2A2115) else SurfaceTonal, contentColor = if (warning) ComposeColor(0xFFFFC857) else TextSecondary, border = androidx.compose.foundation.BorderStroke(1.dp, if (warning) ComposeColor(0xFF55401E) else OutlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(painterResource(if (warning) R.drawable.ic_close else R.drawable.ic_info), contentDescription = null, tint = if (warning) ComposeColor(0xFFFFC857) else BrandSoft, modifier = Modifier.size(18.dp))
                        Text(message, modifier = Modifier.weight(1f), color = if (warning) ComposeColor(0xFFFFC857) else TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    val footerContent: @Composable () -> Unit = {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = SurfaceLow, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)) {
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (request.mode == SharedStorageBrowserMode.IMPORT) "Only ${request.allowedExtensions.joinToString { ".$it" }} files are shown. Selection is revalidated before import." else "Choose a folder and name. Saving uses an atomic temporary write and asks before replacing an existing file.",
                    modifier = Modifier.weight(1f), color = TextMuted, style = MaterialTheme.typography.labelSmall
                )
                if (busy) LoadingIndicator(color = BrandSoft, modifier = Modifier.size(30.dp))
                else if (request.mode == SharedStorageBrowserMode.EXPORT) {
                    ExpressiveContainedIconTextButton("Save", R.drawable.ic_save, enabled = root != null && directory != null && filenameBase.trim().isNotBlank() && exportExtension != null) {
                        val extension = exportExtension ?: return@ExpressiveContainedIconTextButton
                        val fixedName = "${filenameBase.trim()}.$extension"
                        val target = try { validatedSharedStorageDestination(directory ?: return@ExpressiveContainedIconTextButton, fixedName, setOf(extension)) }
                        catch (error: Throwable) { status = "Export failed: ${error.message ?: "Invalid destination"}"; return@ExpressiveContainedIconTextButton }
                        if (target.exists()) pendingOverwrite = target else runExport(target)
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!busy) requestClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)

    ) {
        BackHandler(enabled = !busy) {
            if (imeVisible) focusManager.clearFocus(force = true)
            else if (!atRoot) navigateUp()
            else requestClose()
        }
        Box(Modifier.fillMaxSize().background(ComposeColor.Black).zIndex(50f)) {
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(180)) + slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 10 },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(210, easing = FastOutSlowInEasing)) { it / 12 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = navigationStartInset, end = navigationEndInset, bottom = navigationBottomInset)
                    .consumeDesktopSecondaryMouseInput(suppressDesktopSecondaryInput),
                shape = RoundedCornerShape(0.dp),
                color = ComposeColor.Black,
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    val landscapeLayout = maxWidth > maxHeight && maxWidth >= 700.dp
                    if (landscapeLayout) {
                        val controlsScrollState = rememberScrollState()
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(
                                Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight()
                                    .desktopVerticalPointerScroll(controlsScrollState)
                                    .dpadScrollableNavigation(controlsScrollState)
                                    .verticalScroll(controlsScrollState)
                                    .focusable()
                                    .focusGroup(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                headerContent()
                                descriptionContent()
                                searchContent()
                                exportNameContent()
                                statusContent()
                                footerContent()
                            }
                            Box(Modifier.weight(0.6f).fillMaxHeight()) {
                                SharedStorageBrowserListing(
                                    loading = loading,
                                    viewMode = viewMode,
                                    filteredFolders = filteredFolders,
                                    filteredFiles = filteredFiles,
                                    mode = request.mode,
                                    busy = busy,
                                    navigationKey = directoryPath,
                                    onNavigate = { directoryPath = it },
                                    onImport = ::runImport,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            headerContent()
                            descriptionContent()
                            searchContent()
                            exportNameContent()
                            statusContent()
                            SharedStorageBrowserListing(
                                loading = loading,
                                viewMode = viewMode,
                                filteredFolders = filteredFolders,
                                filteredFiles = filteredFiles,
                                mode = request.mode,
                                busy = busy,
                                navigationKey = directoryPath,
                                onNavigate = { directoryPath = it },
                                onImport = ::runImport,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            footerContent()
                        }
                    }
                }
            }
        }
    }

    }

    pendingOverwrite?.let { target ->
        AlertDialog(
            modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
            onDismissRequest = { if (!busy) pendingOverwrite = null },
            containerColor = SurfaceRaised,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { QuestionDialogTitle("Overwrite existing file?") },
            text = { Text("${target.name} already exists in this folder. Replace it with the new OpenGLESScope export?") },
            confirmButton = { ExpressiveContainedIconTextButton("Replace", R.drawable.ic_save, enabled = !busy) { pendingOverwrite = null; runExport(target) } },
            dismissButton = { ExpressiveCancelButton(enabled = !busy) { pendingOverwrite = null } }
        )
    }
}

@Composable
private fun SoftScrollIntersectionShadows(
    showTop: Boolean,
    showBottom: Boolean,
    modifier: Modifier = Modifier,
    edgeColor: ComposeColor = SurfaceDark
) {
    Box(modifier) {
        AnimatedVisibility(
            visible = showTop,
            enter = fadeIn(tween(170)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                edgeColor.copy(alpha = 0.96f),
                                edgeColor.copy(alpha = 0.72f),
                                edgeColor.copy(alpha = 0.30f),
                                ComposeColor.Transparent
                            )
                        )
                    )
            )
        }
        AnimatedVisibility(
            visible = showBottom,
            enter = fadeIn(tween(170)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ComposeColor.Transparent,
                                edgeColor.copy(alpha = 0.28f),
                                edgeColor.copy(alpha = 0.70f),
                                edgeColor.copy(alpha = 0.96f)
                            )
                        )
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedStorageBrowserListing(
    loading: Boolean,
    viewMode: FileManagerViewMode,
    filteredFolders: List<String>,
    filteredFiles: List<SharedStorageFileEntry>,
    mode: SharedStorageBrowserMode,
    busy: Boolean,
    navigationKey: String,
    onNavigate: (String) -> Unit,
    onImport: (SharedStorageFileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = navigationKey,
        transitionSpec = {
            (fadeIn(tween(190)) + slideInHorizontally(tween(230, easing = FastOutSlowInEasing)) { it / 8 }) togetherWith
                (fadeOut(tween(150)) + slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { -it / 8 })
        },
        label = "sharedStorageDirectoryTransition",
        modifier = modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LoadingIndicator(color = BrandSoft)
                Text("Reading shared storage…", color = TextSecondary)
            }
        } else if (viewMode in setOf(FileManagerViewMode.GRID, FileManagerViewMode.DENSE_GRID, FileManagerViewMode.LARGE_TILES)) {
            val gridState = rememberLazyGridState()
            val denseGrid = viewMode == FileManagerViewMode.DENSE_GRID
            val largeGrid = viewMode == FileManagerViewMode.LARGE_TILES
            val showTopFade by remember(gridState) { derivedStateOf { gridState.canScrollBackward } }
            val showBottomFade by remember(gridState) { derivedStateOf { gridState.canScrollForward } }
            val minimumCellWidth = when (viewMode) {
                FileManagerViewMode.DENSE_GRID -> 92.dp
                FileManagerViewMode.LARGE_TILES -> 228.dp
                else -> 156.dp
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minimumCellWidth),
                state = gridState,
                modifier = Modifier.fillMaxSize().desktopVerticalPointerScroll(gridState).tvRemoteLazyGridNavigation(gridState).focusGroup(),
                horizontalArrangement = Arrangement.spacedBy(if (denseGrid) 6.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (denseGrid) 6.dp else if (largeGrid) 10.dp else 8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                gridItems(filteredFolders, key = { "shared-folder:$it" }) { path ->
                    SharedStorageFolderGridCard(path = path, enabled = !busy, dense = denseGrid, large = largeGrid) { onNavigate(path) }
                }
                if (mode == SharedStorageBrowserMode.IMPORT) {
                    gridItems(filteredFiles, key = { "shared-file:${it.path}" }) { entry ->
                        SharedStorageFileGridCard(entry = entry, enabled = !busy, dense = denseGrid, large = largeGrid) { onImport(entry) }
                    }
                }
                if (filteredFolders.isEmpty() && (mode == SharedStorageBrowserMode.EXPORT || filteredFiles.isEmpty())) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { EmptyState("No matching folders or files in this location") }
                }
            }
            SoftScrollIntersectionShadows(showTopFade, showBottomFade, Modifier.fillMaxSize(), edgeColor = SurfaceDark)
            ExpressiveScrollHints(gridState, Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp))
        } else {
            val listState = rememberLazyListState()
            val compact = viewMode == FileManagerViewMode.COMPACT_LIST
            val details = viewMode == FileManagerViewMode.DETAILED_LIST
            val showTopFade by remember(listState) { derivedStateOf { listState.canScrollBackward } }
            val showBottomFade by remember(listState) { derivedStateOf { listState.canScrollForward } }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().desktopVerticalPointerScroll(listState).tvRemoteLazyListNavigation(listState).focusGroup(),
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filteredFolders, key = { "shared-folder:$it" }) { path ->
                    SharedStorageFolderRow(path = path, compact = compact, detailed = details, enabled = !busy) { onNavigate(path) }
                }
                if (mode == SharedStorageBrowserMode.IMPORT) {
                    items(filteredFiles, key = { "shared-file:${it.path}" }) { entry ->
                        SharedStorageFileRow(entry = entry, compact = compact, detailed = details, enabled = !busy) { onImport(entry) }
                    }
                }
                if (filteredFolders.isEmpty() && (mode == SharedStorageBrowserMode.EXPORT || filteredFiles.isEmpty())) item { EmptyState("No matching folders or files in this location") }
            }
            SoftScrollIntersectionShadows(showTopFade, showBottomFade, Modifier.fillMaxSize(), edgeColor = SurfaceDark)
            ExpressiveScrollHints(listState, Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp))
        }
        }
    }
}

@Composable
private fun SharedStorageFolderRow(path: String, enabled: Boolean, compact: Boolean = false, detailed: Boolean = false, onOpen: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onOpen).then(tvBrowseModifier(shape, enabled)),
        shape = shape,
        color = SurfaceTonal,
        contentColor = TextPrimary,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)),
        tonalElevation = if (compact) 0.dp else 1.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = ComposeColor(0xFF2A2418), contentColor = ComposeColor(0xFFFFC857)) {
                Icon(painterResource(R.drawable.ic_folder), contentDescription = null, modifier = Modifier.padding(if (compact) 7.dp else 8.dp).size(if (compact) 22.dp else 24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(File(path).name.ifBlank { path }, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis)
                if (!compact) Text(if (detailed) path else "Folder", color = TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = if (detailed) 2 else 1, overflow = TextOverflow.Ellipsis)
            }
            FileManagerNavigateArrow(enabled = enabled, description = "Open folder", onClick = onOpen)
        }
    }
}

private fun sharedStorageFileIcon(entry: SharedStorageFileEntry): Int = when (entry.name.substringAfterLast('.', "").lowercase(java.util.Locale.ROOT)) {
    "html", "htm" -> R.drawable.ic_action_html
    "txt" -> R.drawable.ic_action_text
    else -> R.drawable.ic_export
}

@Composable
private fun SharedStorageFileRow(entry: SharedStorageFileEntry, enabled: Boolean, compact: Boolean = false, detailed: Boolean = false, onSelect: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onSelect).then(tvBrowseModifier(shape, enabled)),
        shape = shape,
        color = SurfaceTonal,
        contentColor = TextPrimary,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)),
        tonalElevation = if (compact) 0.dp else 1.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = BrandContainer, contentColor = BrandSoft) {
                Icon(painterResource(sharedStorageFileIcon(entry)), contentDescription = null, modifier = Modifier.padding(if (compact) 7.dp else 8.dp).size(if (compact) 22.dp else 24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis)
                if (!compact) Text("${formatBytes(entry.sizeBytes)} · ${formatTimestampOrUnavailable(entry.modifiedAtMillis.takeIf { it > 0L })}", color = TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (detailed) Text("Created ${formatTimestampOrUnavailable(entry.createdAtMillis.takeIf { it > 0L })} · ${entry.path}", color = TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            FileManagerNavigateArrow(enabled = enabled, description = "Select ${entry.name}", onClick = onSelect)
        }
    }
}

@Composable
private fun SharedStorageFolderGridCard(path: String, enabled: Boolean, dense: Boolean, large: Boolean, onOpen: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    if (large) {
        Surface(shape = shape, color = SurfaceTonal, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)), modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onOpen).then(tvBrowseModifier(shape, enabled))) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = ComposeColor(0xFF2A2418), contentColor = ComposeColor(0xFFFFC857)) {
                    Icon(painterResource(R.drawable.ic_folder), contentDescription = null, modifier = Modifier.padding(10.dp).size(38.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(File(path).name.ifBlank { path }, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("Folder", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
                FileManagerNavigateArrow(enabled = enabled, description = "Open folder", onClick = onOpen)
            }
        }
    } else {
        Surface(shape = shape, color = SurfaceTonal, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)), modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onOpen).then(tvBrowseModifier(shape, enabled))) {
            Column(Modifier.fillMaxWidth().padding(if (dense) 8.dp else 11.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (dense) 5.dp else 8.dp)) {
                Surface(shape = RoundedCornerShape(if (dense) 12.dp else 16.dp), color = ComposeColor(0xFF2A2418), contentColor = ComposeColor(0xFFFFC857)) {
                    Icon(painterResource(R.drawable.ic_folder), contentDescription = null, modifier = Modifier.padding(if (dense) 7.dp else 9.dp).size(if (dense) 25.dp else 32.dp))
                }
                Text(File(path).name.ifBlank { path }, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = if (dense) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                FileManagerNavigateArrow(enabled = enabled, description = "Open folder", onClick = onOpen)
            }
        }
    }
}

@Composable
private fun SharedStorageFileGridCard(entry: SharedStorageFileEntry, enabled: Boolean, dense: Boolean, large: Boolean, onSelect: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    if (large) {
        Surface(shape = shape, color = SurfaceTonal, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)), modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onSelect).then(tvBrowseModifier(shape, enabled))) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer, contentColor = BrandSoft) {
                    Icon(painterResource(sharedStorageFileIcon(entry)), contentDescription = null, modifier = Modifier.padding(10.dp).size(36.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(entry.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${formatBytes(entry.sizeBytes)} · ${formatTimestampOrUnavailable(entry.modifiedAtMillis.takeIf { it > 0L })}", color = TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                FileManagerNavigateArrow(enabled = enabled, description = "Select ${entry.name}", onClick = onSelect)
            }
        }
    } else {
        Surface(shape = shape, color = SurfaceTonal, contentColor = TextPrimary, border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)), modifier = Modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).fillMaxWidth().clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onSelect).then(tvBrowseModifier(shape, enabled))) {
            Column(Modifier.fillMaxWidth().padding(if (dense) 8.dp else 11.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (dense) 5.dp else 8.dp)) {
                Surface(shape = RoundedCornerShape(if (dense) 12.dp else 16.dp), color = BrandContainer, contentColor = BrandSoft) {
                    Icon(painterResource(sharedStorageFileIcon(entry)), contentDescription = null, modifier = Modifier.padding(if (dense) 7.dp else 9.dp).size(if (dense) 25.dp else 32.dp))
                }
                Text(entry.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = if (dense) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                if (!dense) Text(formatBytes(entry.sizeBytes), color = TextMuted, style = MaterialTheme.typography.labelSmall)
                FileManagerNavigateArrow(enabled = enabled, description = "Select ${entry.name}", onClick = onSelect)
            }
        }
    }
}


private fun vendorIdFromDisplay(value: String?): Long? {
    val text = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return if (text.startsWith("0x", ignoreCase = true)) text.substring(2).toLongOrNull(16) else text.toLongOrNull()
}


@Composable
private fun FileManagerOptionsChooser(
    viewMode: FileManagerViewMode,
    sortMode: FileManagerSortMode,
    onViewMode: (FileManagerViewMode) -> Unit,
    onSortMode: (FileManagerSortMode) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.CenterEnd) {
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            HeaderActionButton(
                icon = viewMode.icon,
                contentDescription = "View and sort files and folders",
                onClick = { if (enabled) expanded = true },
                modifier = Modifier.size(48.dp),
                enabled = enabled,
                size = 48.dp
            )
            Surface(
                shape = CircleShape,
                color = BrandContainer,
                contentColor = BrandSoft,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.38f)),
                modifier = Modifier.align(Alignment.BottomEnd).size(20.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(painterResource(sortMode.icon), contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
    if (expanded) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val dialogView = LocalView.current
            SideEffect {
                (dialogView.parent as? DialogWindowProvider)?.window?.apply {
                    navigationBarColor = android.graphics.Color.BLACK
                    statusBarColor = android.graphics.Color.TRANSPARENT
                    isNavigationBarContrastEnforced = false
                }
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(ComposeColor.Black.copy(alpha = 0.52f)).statusBarsPadding().navigationBarsPadding().padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val scrollState = rememberScrollState()
                val menuMaxHeight = (maxHeight - 24.dp).coerceAtLeast(220.dp)
                val chooserColumns = if (maxWidth < 310.dp || LocalConfiguration.current.fontScale >= 1.65f) 2 else 3
                Surface(
                    color = ComposeColor(0xFF181516),
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    shadowElevation = 14.dp,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.30f)),
                    modifier = Modifier.widthIn(min = 292.dp, max = 360.dp).heightIn(max = menuMaxHeight).focusGroup()
                ) {
                    Column(
                        Modifier.fillMaxWidth().desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            HeaderActionButton(
                                icon = R.drawable.ic_close,
                                contentDescription = "Close view and sort menu",
                                onClick = { expanded = false },
                                enabled = true,
                                size = 36.dp
                            )
                            Text(viewMode.label, color = BrandSoft, style = MaterialTheme.typography.labelMedium)
                        }
                        HorizontalDivider(color = OutlineVariant)
                        FileManagerViewMode.entries.chunked(chooserColumns).forEach { rowModes ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowModes.forEach { mode ->
                                    val selected = mode == viewMode
                                    val tileShape = RoundedCornerShape(16.dp)
                                    val tileColor by animateColorAsState(if (selected) BrandContainer else ComposeColor.Transparent, tween(180), label = "fileManagerViewTileColor")
                                    Column(
                                        modifier = Modifier.weight(1f).clip(tileShape)
                                            .background(tileColor)
                                            .clickable(enabled = enabled, role = Role.Button) { onViewMode(mode) }
                                            .then(tvBrowseModifier(tileShape, enabled))
                                            .padding(horizontal = 6.dp, vertical = 9.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(painterResource(mode.icon), contentDescription = null, tint = if (selected) BrandSoft else TextPrimary, modifier = Modifier.size(27.dp))
                                        Text(mode.label, color = if (selected) TextPrimary else TextSecondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                                        Box(Modifier.width(34.dp).height(3.dp).clip(RoundedCornerShape(99.dp)).background(if (selected) BrandSoft else ComposeColor.Transparent))
                                    }
                                }
                                repeat(chooserColumns - rowModes.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                        HorizontalDivider(color = OutlineVariant)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sort", color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(sortMode.label, color = BrandSoft, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        FileManagerSortMode.entries.forEach { mode ->
                            val selected = mode == sortMode
                            val rowShape = RoundedCornerShape(14.dp)
                            val sortColor by animateColorAsState(if (selected) BrandContainer.copy(alpha = 0.72f) else ComposeColor.Transparent, tween(180), label = "fileManagerSortRowColor")
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(rowShape)
                                    .background(sortColor)
                                    .clickable(enabled = enabled, role = Role.Button) { onSortMode(mode) }
                                    .then(tvBrowseModifier(rowShape, enabled))
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(painterResource(mode.icon), contentDescription = null, tint = if (selected) BrandSoft else TextSecondary, modifier = Modifier.size(19.dp))
                                Text(mode.label, color = if (selected) BrandSoft else TextPrimary, modifier = Modifier.weight(1f), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                if (selected) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = BrandSoft, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
    enabled: Boolean = true,
    placeholderText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var focused by remember { mutableStateOf(false) }
    val containerColor = if (focused) SurfaceTonal else SurfaceLow
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val fontPx = with(density) { MaterialTheme.typography.bodyLarge.fontSize.toPx() }
        val availableTextPx = with(density) { (maxWidth - 112.dp).coerceAtLeast(24.dp).toPx() }
        val measuredTextPx = remember(value, fontPx) {
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textSize = fontPx }.measureText(value)
        }
        val textOverflows = value.isNotBlank() && measuredTextPx > availableTextPx
        val showLeadingFade = focused && textOverflows
        val showTrailingFade = textOverflows
        val rtl = LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                leadingIcon = { Icon(painter = painterResource(R.drawable.ic_search), contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = value.isNotEmpty(),
                        enter = fadeIn(tween(150)) + slideInHorizontally(tween(150)) { it / 2 },
                        exit = fadeOut(tween(120)) + slideOutHorizontally(tween(120)) { it / 2 }
                    ) {
                        IconButton(onClick = { onValueChange("") }, enabled = enabled, modifier = Modifier.size(40.dp)) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                label = if (labelText == null) null else { { Text(labelText) } },
                placeholder = if (placeholderText == null) null else { { Text(placeholderText) } },
                keyboardOptions = keyboardOptions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledTextColor = TextMuted,
                    focusedContainerColor = SurfaceTonal,
                    unfocusedContainerColor = SurfaceLow,
                    disabledContainerColor = SurfaceLow,
                    cursorColor = BrandSoft,
                    focusedBorderColor = BrandSoft,
                    unfocusedBorderColor = Outline,
                    disabledBorderColor = OutlineVariant,
                    focusedLeadingIconColor = BrandSoft,
                    unfocusedLeadingIconColor = TextMuted,
                    disabledLeadingIconColor = TextMuted,
                    focusedTrailingIconColor = BrandSoft,
                    unfocusedTrailingIconColor = TextSecondary,
                    disabledTrailingIconColor = TextMuted,
                    focusedLabelColor = BrandSoft,
                    unfocusedLabelColor = TextSecondary,
                    disabledLabelColor = TextMuted,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextMuted,
                    disabledPlaceholderColor = TextMuted
                )
            )
            AnimatedVisibility(
                visible = showLeadingFade,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120)),
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 44.dp)
            ) {
                Box(Modifier.width(24.dp).height(34.dp).background(Brush.horizontalGradient(if (rtl) listOf(ComposeColor.Transparent, containerColor.copy(alpha = 0.54f), containerColor.copy(alpha = 0.92f)) else listOf(containerColor.copy(alpha = 0.92f), containerColor.copy(alpha = 0.54f), ComposeColor.Transparent))))
            }
            AnimatedVisibility(
                visible = showTrailingFade,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120)),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 43.dp)
            ) {
                Box(Modifier.width(28.dp).height(34.dp).background(Brush.horizontalGradient(if (rtl) listOf(containerColor.copy(alpha = 0.88f), containerColor.copy(alpha = 0.48f), ComposeColor.Transparent) else listOf(ComposeColor.Transparent, containerColor.copy(alpha = 0.48f), containerColor.copy(alpha = 0.88f)))))
            }
        }
    }
}
@Composable
private fun rememberFilterScrollBoundaryConnection(): NestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            return if (available.y != 0f) Offset(0f, available.y) else Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return if (available.y != 0f) Velocity(0f, available.y) else Velocity.Zero
        }
    }
}

@Composable
private fun ExpressiveSingleFilterSelector(
    labels: List<String>,
    selectedIndex: Int?,
    enabled: Boolean,
    indicatorTint: ComposeColor,
    onSelected: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var dropdownMounted by remember { mutableStateOf(false) }
    var dropdownVisible by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var pageField by remember { mutableStateOf(TextFieldValue("1")) }
    var pageFieldFocused by remember { mutableStateOf(false) }
    var suppressFilterPageCommit by remember { mutableStateOf(false) }
    val pageSize = COLLECTION_PAGE_SIZE
    val showSearch = labels.size >= 5
    val indexed = remember(labels, query) {
        labels.mapIndexed { index, label -> index to label }
            .filter { (_, label) -> query.isBlank() || label.contains(query, ignoreCase = true) }
    }
    val pageCount = maxOf(1, (indexed.size + pageSize - 1) / pageSize)
    val selectedLabel = selectedIndex?.takeIf { it in labels.indices }?.let(labels::get) ?: "Select filter"
    val selectorAlpha by animateFloatAsState(if (enabled) 1f else 0.52f, animationSpec = tween(180), label = "filterSelectorAlpha")
    val selectorScale by animateFloatAsState(if (enabled) 1f else 0.985f, animationSpec = tween(180), label = "filterSelectorScale")
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, animationSpec = tween(180), label = "filterSelectorArrow")
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }
    LaunchedEffect(showSearch) {
        if (!showSearch && query.isNotEmpty()) query = ""
    }
    LaunchedEffect(expanded, enabled) {
        if (expanded && enabled) {
            query = ""
            val openingPage = selectedIndex?.takeIf { it in labels.indices }?.div(pageSize) ?: 0
            page = openingPage.coerceIn(0, pageCount - 1)
            pageField = TextFieldValue((page + 1).toString())
            dropdownMounted = true
            dropdownVisible = false
            delay(20)
            dropdownVisible = true
        } else {
            dropdownVisible = false
            delay(190)
            dropdownMounted = false
        }
    }
    fun commitFilterPage() {
        val requested = pageField.text.toIntOrNull()?.coerceIn(1, pageCount) ?: (page + 1).coerceIn(1, pageCount)
        page = requested - 1
        pageField = TextFieldValue(requested.toString())
    }
    fun requestFilterPageChange(targetPage: Int) {
        val bounded = targetPage.coerceIn(0, pageCount - 1)
        suppressFilterPageCommit = true
        pageField = TextFieldValue((bounded + 1).toString())
        focusManager.clearFocus(force = true)
        pageFieldFocused = false
        suppressFilterPageCommit = false
        page = bounded
    }
    LaunchedEffect(query, pageCount) {
        page = page.coerceIn(0, pageCount - 1)
        if (!pageFieldFocused) pageField = TextFieldValue((page + 1).toString())
    }
    LaunchedEffect(page, pageCount, pageFieldFocused) {
        page = page.coerceIn(0, pageCount - 1)
        if (!pageFieldFocused) pageField = TextFieldValue((page + 1).toString())
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val dropdownWidth = if (landscape) maxWidth else maxWidth.coerceAtMost(560.dp)
        val screenHeight = configuration.screenHeightDp.dp
        val imeHeight = with(density) { WindowInsets.ime.getBottom(density).toDp() }
        val usableHeight = (screenHeight - imeHeight).coerceAtLeast(220.dp)
        val dropdownMaxHeight = if (landscape) {
            ((usableHeight.value * 0.62f).dp).coerceIn(220.dp, 440.dp)
        } else {
            (usableHeight - 96.dp).coerceIn(240.dp, 620.dp)
        }
        val visibleRows = indexed.drop(page * pageSize).take(pageSize).size.coerceIn(1, if (landscape) 4 else 7)
        val dropdownDesiredHeight = (
            28.dp +
                (if (showSearch) 96.dp else 0.dp) +
                (58.dp * visibleRows) +
                (if (pageCount > 1 && indexed.isNotEmpty()) 78.dp else 0.dp)
            ).coerceAtMost(dropdownMaxHeight)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val selectorShape = MaterialTheme.shapes.medium
            Surface(
                shape = selectorShape,
                color = SurfaceTonal,
                contentColor = TextPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .alpha(selectorAlpha)
                    .graphicsLayer(scaleX = selectorScale, scaleY = selectorScale)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Filter", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        Text(
                            selectedLabel,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val arrowShape = RoundedCornerShape(15.dp)
                    Surface(
                        shape = arrowShape,
                        color = if (expanded) Brand else BrandContainer,
                        contentColor = indicatorTint,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(arrowShape)
                            .clickable(enabled = enabled, role = Role.Button) { expanded = !expanded }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_expand_more),
                                contentDescription = if (expanded) "Close filter menu" else "Open filter menu",
                                tint = indicatorTint,
                                modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = arrowRotation)
                            )
                        }
                    }
                }
            }

            BackHandler(enabled = dropdownMounted && expanded) {
                if (imeVisible) focusManager.clearFocus(force = true)
            }

            if (dropdownMounted && enabled) {
                AnimatedVisibility(
                    visible = dropdownVisible,
                    enter = fadeIn(tween(220)) + scaleIn(tween(240), initialScale = 0.97f, transformOrigin = TransformOrigin(0.5f, 0f)) + expandVertically(tween(220), expandFrom = Alignment.Top),
                    exit = fadeOut(tween(150)) + scaleOut(tween(170), targetScale = 0.985f, transformOrigin = TransformOrigin(0.5f, 0f)) + shrinkVertically(tween(170), shrinkTowards = Alignment.Top)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = SurfaceRaised,
                        contentColor = TextPrimary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                        shadowElevation = 8.dp,
                        modifier = Modifier.width(dropdownWidth).height(dropdownDesiredHeight)
                    ) {
                        Column(Modifier.fillMaxHeight().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (showSearch) {
                                ExpressiveSearchField(
                                    value = query,
                                    onValueChange = { value ->
                                        query = value.take(120)
                                        page = 0
                                        pageField = TextFieldValue("1")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    labelText = "Search filters",
                                    placeholderText = "Type a filter name…"
                                )
                            }
                            if (showSearch) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (indexed.isEmpty()) "No matching filters" else "${indexed.size} result${if (indexed.size == 1) "" else "s"} · up to $pageSize per page",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (indexed.isNotEmpty() && pageCount > 1) {
                                        Text("Page ${page + 1} / $pageCount", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Box(Modifier.weight(1f).fillMaxWidth().heightIn(min = 56.dp)) {
                                AnimatedContent(
                                    targetState = page to query,
                                    transitionSpec = {
                                        val direction = if (targetState.first >= initialState.first) 1 else -1
                                        (fadeIn(tween(180)) + slideInHorizontally(tween(190)) { direction * (it / 10) }) togetherWith
                                            (fadeOut(tween(120)) + slideOutHorizontally(tween(140)) { -direction * (it / 10) })
                                    },
                                    label = "filterPageTransition"
                                ) { (targetPage, targetQuery) ->
                                    val targetIndexed = labels.mapIndexed { index, label -> index to label }
                                        .filter { (_, label) -> targetQuery.isBlank() || label.contains(targetQuery, ignoreCase = true) }
                                    val targetPageItems = targetIndexed.drop(targetPage * pageSize).take(pageSize)
                                    val selectedOffset = if (targetQuery.isBlank()) targetPageItems.indexOfFirst { it.first == selectedIndex } else -1
                                    val targetListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedOffset.coerceAtLeast(0))
                                    val boundaryScrollConnection = rememberFilterScrollBoundaryConnection()
                                    Box(Modifier.fillMaxSize().nestedScroll(boundaryScrollConnection)) {
                                        LazyColumn(
                                            state = targetListState,
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(5.dp),
                                            contentPadding = PaddingValues(vertical = 2.dp)
                                        ) {
                                            items(targetPageItems, key = { it.first }) { (index, label) ->
                                                val selected = index == selectedIndex
                                                val rowShape = RoundedCornerShape(18.dp)
                                                Surface(
                                                    shape = rowShape,
                                                    color = if (selected) BrandContainer else ComposeColor.Transparent,
                                                    contentColor = if (selected) TextPrimary else TextSecondary,
                                                    modifier = Modifier.fillMaxWidth().clip(rowShape).clickable(role = Role.RadioButton) { onSelected(index) }.then(tvBrowseModifier(rowShape))
                                                ) {
                                                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        Surface(shape = RoundedCornerShape(999.dp), color = if (selected) BrandSoft else OutlineVariant, modifier = Modifier.size(10.dp)) {}
                                                        Text(
                                                            label,
                                                            modifier = Modifier.weight(1f),
                                                            textAlign = TextAlign.Start,
                                                            softWrap = true,
                                                            color = if (selected) TextPrimary else TextSecondary,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                            }
                                            if (targetPageItems.isEmpty()) {
                                                item {
                                                    Text(
                                                        "No filter matches this search.",
                                                        color = TextMuted,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 16.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        ExpressiveScrollHints(targetListState, Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp))
                                    }
                                }
                            }
                            if (indexed.isNotEmpty() && pageCount > 1) {
                                HorizontalDivider(color = OutlineVariant)
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { if (page > 0) requestFilterPageChange(page - 1) },
                                            enabled = page > 0,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = BrandContainer,
                                                contentColor = TextPrimary,
                                                disabledContainerColor = SurfaceLow,
                                                disabledContentColor = TextMuted
                                            )
                                        ) {
                                            Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Previous filter page")
                                        }
                                        Box(Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                                            OutlinedTextField(
                                                value = pageField,
                                                onValueChange = { value ->
                                                    val candidate = value.text
                                                    when {
                                                        candidate.isEmpty() -> pageField = value
                                                        candidate.all { it.isDigit() } && !(candidate.length > 1 && candidate.startsWith('0')) && (candidate.toIntOrNull() ?: 0) in 1..pageCount -> pageField = value
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                                                    val wasFocused = pageFieldFocused
                                                    pageFieldFocused = focus.isFocused
                                                    if (focus.isFocused) {
                                                        pageField = pageField.copy(selection = TextRange(0, pageField.text.length))
                                                    } else if (wasFocused && !suppressFilterPageCommit) {
                                                        commitFilterPage()
                                                    }
                                                },
                                                singleLine = true,
                                                label = { Text("Page") },
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    commitFilterPage()
                                                    focusManager.clearFocus(force = true)
                                                }),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = BrandSoft,
                                                    unfocusedBorderColor = Outline,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = ComposeColor.Transparent,
                                                    cursorColor = BrandSoft
                                                )
                                            )
                                            if (!pageFieldFocused) {
                                                Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                                    AnimatedContent(
                                                        targetState = (page + 1).coerceIn(1, pageCount),
                                                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(130)) },
                                                        label = "filterPageNumberTransition"
                                                    ) { animatedNumber ->
                                                        Text(animatedNumber.toString(), color = TextPrimary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.offset(y = 4.dp))
                                                    }
                                                }
                                            }
                                        }
                                        Text("/ $pageCount", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        IconButton(
                                            onClick = { if (page + 1 < pageCount) requestFilterPageChange(page + 1) },
                                            enabled = page + 1 < pageCount,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = BrandContainer,
                                                contentColor = TextPrimary,
                                                disabledContainerColor = SurfaceLow,
                                                disabledContentColor = TextMuted
                                            )
                                        ) {
                                            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Next filter page")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun ExpressiveFilterBar(labels: List<String>, selectedIndex: Int, arrowTint: ComposeColor = TextPrimary, onSelected: (Int) -> Unit) {
    if (labels.firstOrNull()?.equals("All", true) == true && labels.size > 1) {
        val allEnabled = selectedIndex == 0
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExpressiveSwitch(checked = allEnabled, onCheckedChange = { enabled -> onSelected(if (enabled) 0 else 1) })
                Column(Modifier.weight(1f)) {
                    Text("All", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(if (allEnabled) "All entries are shown; specific filters are locked." else "Specific filter selection is enabled.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            ExpressiveSingleFilterSelector(
                labels = labels.drop(1),
                selectedIndex = if (selectedIndex <= 0) null else selectedIndex - 1,
                enabled = !allEnabled,
                indicatorTint = arrowTint,
                onSelected = { onSelected(it + 1) }
            )
        }
    } else {
        ExpressiveSingleFilterSelector(labels, selectedIndex, true, arrowTint, onSelected)
    }
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
        label = { Text(trademarkApiDisplayText(label), fontWeight = FontWeight.Medium) },
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
private fun ExpressiveToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExpressiveSwitch(checked = checked, onCheckedChange = onCheckedChange)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(trademarkApiDisplayText(subtitle), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ExpressiveMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Surface(
        shape = shape,
        color = BrandContainer,
        contentColor = TextPrimary,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.34f)),
        modifier = modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).then(tvBrowseModifier(shape))
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(trademarkApiDisplayText(label), color = BrandSoft, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(trademarkApiDisplayText(value), color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExpressiveMetricGrid(metrics: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    if (metrics.isEmpty()) return
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth < 300.dp || (fontScale >= 1.55f && maxWidth < 420.dp) -> 1
            maxWidth < 760.dp -> 2
            else -> 3
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowMetrics.forEach { (label, value) -> ExpressiveMetric(label, value, Modifier.weight(1f)) }
                    repeat(columns - rowMetrics.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
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
private fun ExpressivePrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(20.dp),
            pressedShape = RoundedCornerShape(26.dp)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brand,
            contentColor = TextPrimary
        )
    ) {
        Text(trademarkApiDisplayText(label), fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveTextButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        colors = ButtonDefaults.textButtonColors(contentColor = BrandSoft)
    ) {
        Text(trademarkApiDisplayText(label), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ExpressiveCancelButton(enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, colors = ButtonDefaults.textButtonColors(contentColor = BrandSoft, disabledContentColor = TextMuted)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(17.dp))
            }
            Text("Cancel", fontWeight = FontWeight.Normal, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun UpdateDialogKeyValue(key: String, value: String) {
    val expandedTextLayout = preferExpandedTextLayout()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked = expandedTextLayout || maxWidth < 360.dp || key.length > 24 || value.length > 32 || value.contains("\\n")
        val modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(RoundedCornerShape(12.dp))).semantics(mergeDescendants = true) { }
        if (stacked) {
            Column(modifier.padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(key, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text(value.ifBlank { "Unavailable" }, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Row(modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Text(key, color = TextMuted, modifier = Modifier.weight(0.82f), style = MaterialTheme.typography.labelSmall)
                Text(value.ifBlank { "Unavailable" }, modifier = Modifier.weight(1.18f), color = TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ActionButtonIconArtwork(title: String, icon: Int, tint: ComposeColor) {
    val badge = when {
        title.equals("Export TXT", true) -> "TXT"
        title.equals("Export HTML", true) -> "HTML"
        else -> null
    }
    if (badge == null) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        return
    }
    Box(Modifier.size(24.dp)) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.align(Alignment.TopStart).size(19.dp))
        Surface(shape = RoundedCornerShape(3.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd).border(0.7.dp, tint, RoundedCornerShape(3.dp))) {
            Text(badge, color = tint, fontSize = if (badge == "HTML") 3.7.sp else 4.4.sp, lineHeight = if (badge == "HTML") 4.0.sp else 4.7.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = if (badge == "HTML") 0.8.dp else 1.0.dp, vertical = 0.7.dp))
        }
    }
}

@Composable
private fun ChevronAffordance(label: String, contentDescription: String, onClick: () -> Unit) {
    Surface(
        color = BrandContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.semantics { role = Role.Button }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(start = 12.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
        ) {
            Text(trademarkApiDisplayText(label), color = BrandSoft, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = contentDescription, tint = BrandSoft, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ExpressiveExternalLinkRow(title: String, subtitle: String, icon: Int, enabled: Boolean = true, onOpen: () -> Unit) {
    Surface(color = if (enabled) ComposeColor(0xFF1A1718) else ComposeColor(0xFF111111), shape = MaterialTheme.shapes.largeIncreased, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(18.dp), color = if (enabled) BrandContainer else ComposeColor(0xFF181818)) {
                Icon(painterResource(icon), contentDescription = null, tint = if (enabled) BrandSoft else TextMuted, modifier = Modifier.padding(11.dp).size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(trademarkApiDisplayText(title), color = if (enabled) TextPrimary else TextMuted, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(trademarkApiDisplayText(subtitle), color = if (enabled) TextSecondary else TextMuted, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onOpen, enabled = enabled, colors = IconButtonDefaults.iconButtonColors(containerColor = if (enabled) BrandContainer else SurfaceLow, contentColor = BrandSoft, disabledContentColor = TextMuted), modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_open_external), contentDescription = "Open external link", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TransientActionButton(
    title: String,
    subtitle: String,
    icon: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    idleTrailingIcon: Int = icon,
    readableDisabledTrailing: Boolean = false,
    action: suspend () -> Boolean
) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(0) }
    val busy = state != 0
    val trailing = when (state) {
        2 -> R.drawable.ic_check
        3 -> R.drawable.ic_close
        else -> idleTrailingIcon
    }
    val trailingTint = when (state) {
        2 -> ComposeColor(0xFF73C991)
        3 -> ComposeColor(0xFFFF6B6B)
        else -> BrandSoft
    }
    val semanticTitle = when (state) {
        2 -> "$title · completed"
        3 -> "$title · failed"
        else -> title
    }
    val semanticSubtitle = when (state) {
        2 -> "Completed successfully"
        3 -> "The action failed"
        else -> subtitle
    }
    ExpressiveActionButton(
        semanticTitle,
        semanticSubtitle,
        icon,
        modifier,
        enabled && !busy,
        trailingIcon = trailing,
        trailingTint = trailingTint,
        readableDisabledTrailing = readableDisabledTrailing
    ) {
        if (!enabled || busy) return@ExpressiveActionButton
        state = 1
        scope.launch {
            val success = try {
                action()
            } catch (error: CancellationException) {
                state = 0
                throw error
            } catch (_: Throwable) {
                false
            }
            state = if (success) 2 else 3
            delay(3000)
            state = 0
        }
    }
}

@Composable
private fun TransientIconButton(
    idleIcon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    action: suspend () -> Boolean
) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableIntStateOf(0) }
    val busy = state != 0
    val icon = when (state) { 2 -> R.drawable.ic_check; 3 -> R.drawable.ic_close; else -> idleIcon }
    val tint = when (state) { 2 -> ComposeColor(0xFF73C991); 3 -> ComposeColor(0xFFFF6B6B); else -> BrandSoft }
    val container = when (state) { 2 -> ComposeColor(0xFF173524); 3 -> ComposeColor(0xFF3B1B1E); else -> BrandContainer }
    IconButton(
        onClick = {
            if (!enabled || busy) return@IconButton
            state = 1
            scope.launch {
                val success = try { action() }
                catch (cancelled: CancellationException) { state = 0; throw cancelled }
                catch (_: Throwable) { false }
                state = if (success) 2 else 3
                delay(3000L)
                state = 0
            }
        },
        enabled = enabled && !busy,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = container, contentColor = tint,
            disabledContainerColor = container, disabledContentColor = tint
        ),
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = { (fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.72f)) togetherWith (fadeOut(tween(120)) + scaleOut(tween(150), targetScale = 0.82f)) },
            label = "transientIcon"
        ) { targetIcon ->
            Icon(painterResource(targetIcon), contentDescription = if (state == 2) "$contentDescription completed" else contentDescription, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun ExpressiveLinearProgressIndicator(modifier: Modifier = Modifier) {
    LinearWavyProgressIndicator(
        modifier = modifier.height(12.dp),
        color = BrandSoft,
        trackColor = ComposeColor(0xFF2A2022)
    )
}

@Composable
private fun SectionHeaderIcon(title: String, sectionIcon: Int) {
    when {
        title.equals("Opening animation", true) -> {
            Box(Modifier.padding(8.dp).size(27.dp), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(R.drawable.ic_opening_animation_toggle), contentDescription = null, tint = BrandSoft, modifier = Modifier.size(22.dp))
            }
        }
        title.equals("Encyclopedia", true) -> SectionVectorBadgeIcon(R.drawable.ic_book, "REF")
        title.equals("Reference search", true) -> SectionVectorBadgeIcon(R.drawable.ic_book, overlayIcon = R.drawable.ic_search)
        title.equals("How to read encyclopedia entries", true) -> SectionVectorBadgeIcon(R.drawable.ic_book, overlayIcon = R.drawable.ic_question)
        title.contains("requirement", true) -> SectionVectorBadgeIcon(R.drawable.ic_registry, overlayIcon = R.drawable.ic_check)
        title.contains("minimum", true) && title.contains("saved", true) -> SectionVectorBadgeIcon(R.drawable.ic_profile, overlayIcon = R.drawable.ic_save)
        title.contains("minimum", true) -> SectionVectorBadgeIcon(R.drawable.ic_profile, overlayIcon = R.drawable.ic_add)
        title.contains("dependency graph", true) || title.contains("query-gate graph", true) -> SectionVectorBadgeIcon(R.drawable.ic_graph, overlayIcon = R.drawable.ic_search)
        title.contains("integrity score", true) -> SectionVectorBadgeIcon(R.drawable.ic_shield, overlayIcon = R.drawable.ic_check)
        title.equals("Scoring method", true) -> SectionVectorBadgeIcon(R.drawable.ic_shield, overlayIcon = R.drawable.ic_evidence)
        title.contains("self-test", true) || title.contains("active tests", true) -> SectionVectorBadgeIcon(R.drawable.ic_test, "RUN")
        title.equals("Test result summary", true) -> SectionVectorBadgeIcon(R.drawable.ic_self_test, "SUM")
        title.equals("Android runtime", true) -> AndroidRuntimeSectionIcon()
        sectionIcon == R.drawable.openglesscope_scope_wordmark -> {
            Image(painter = painterResource(sectionIcon), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(horizontal = 5.dp, vertical = 11.dp).width(32.dp).height(18.dp))
        }
        sectionIcon == R.drawable.ic_android_brand -> {
            Image(painter = painterResource(sectionIcon), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(horizontal = 7.dp, vertical = 12.dp).width(27.dp).height(16.dp))
        }
        title.equals("EGL identity", true) -> EglSectionArtwork(EglArtworkUse.IDENTITY)
        title.equals("Current EGL binding and context", true) -> EglSectionArtwork(EglArtworkUse.CONTEXT)
        title.equals("Collector pbuffer", true) -> EglSectionArtwork(EglArtworkUse.PBUFFER)
        sectionIcon == R.drawable.ic_egl_official -> EglBrandArtwork(Modifier.padding(7.dp).size(27.dp))
        sectionIcon == R.drawable.ic_opengles_gl_es -> {
            Image(painter = painterResource(sectionIcon), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).size(25.dp))
        }
        title.equals("OpenGL® ES™ / EGL™ query engine", true) -> SectionVectorBadgeIcon(R.drawable.ic_registry, "REG")
        title.equals("About", true) -> AboutSectionIcon()
        title.equals("Extensions", true) || title.equals("Extension explorer", true) -> SectionVectorBadgeIcon(R.drawable.ic_extensions, overlayIcon = R.drawable.ic_search)
        title.equals("Formats", true) || title.equals("Format explorer", true) -> SectionVectorBadgeIcon(R.drawable.ic_formats, overlayIcon = R.drawable.ic_search)
        title.equals("Export complete report", true) -> {
            Box(Modifier.padding(7.dp).size(27.dp)) {
                Icon(painter = painterResource(R.drawable.ic_surface), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(21.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Icon(painter = painterResource(R.drawable.ic_export), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.3.dp).size(9.dp))
                }
            }
        }
        title.equals("Raw structured technical Report", true) || title.equals("Raw technicalReport tree", true) -> {
            Box(Modifier.padding(7.dp).size(27.dp)) {
                Icon(painter = painterResource(R.drawable.ic_registry), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(21.dp))
                Surface(shape = RoundedCornerShape(3.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd).border(0.7.dp, BrandSoft, RoundedCornerShape(3.dp))) {
                    Text("JSON", color = BrandSoft, fontSize = 4.2.sp, lineHeight = 4.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 1.1.dp, vertical = 0.8.dp))
                }
            }
        }
        title.equals("Compare with OpenGLESScope Database", true) || title.equals("OpenGLESScope Database Compare", true) -> {
            Box(Modifier.padding(7.dp).size(27.dp)) {
                Icon(painter = painterResource(R.drawable.ic_action_database), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(21.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Icon(painter = painterResource(R.drawable.ic_compare), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.2.dp).size(9.dp))
                }
            }
        }
        title.equals("HDR capabilities", true) -> DisplaySectionBadgeIcon("HDR")
        title.equals("Supported display modes", true) -> DisplaySectionBadgeIcon("MODE")
        title.equals("Presentation evidence", true) -> {
            Box(Modifier.padding(7.dp).size(27.dp)) {
                Icon(painter = painterResource(R.drawable.ic_display), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(21.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Icon(painter = painterResource(R.drawable.ic_surface), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.5.dp).size(9.dp))
                }
            }
        }
        else -> {
            Icon(painter = painterResource(sectionIcon), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(10.dp).size(21.dp))
        }
    }
}

@Composable
private fun SectionVectorBadgeIcon(primary: Int, badgeText: String? = null, overlayIcon: Int? = null) {
    Box(Modifier.padding(7.dp).size(27.dp)) {
        Icon(painterResource(primary), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(21.dp))
        Surface(shape = RoundedCornerShape(3.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd).border(0.7.dp, BrandSoft, RoundedCornerShape(3.dp))) {
            if (overlayIcon != null) {
                Icon(painterResource(overlayIcon), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.2.dp).size(9.dp))
            } else {
                Text(badgeText.orEmpty(), color = BrandSoft, fontSize = if (badgeText == "JSON") 4.0.sp else 4.2.sp, lineHeight = 4.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 1.dp, vertical = 0.8.dp))
            }
        }
    }
}

@Composable
private fun AndroidRuntimeSectionIcon() {
    Box(Modifier.padding(7.dp).size(27.dp)) {
        Image(painterResource(R.drawable.ic_android_brand), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.align(Alignment.TopStart).width(22.dp).height(15.dp))
        Surface(shape = RoundedCornerShape(3.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd).border(0.7.dp, BrandSoft, RoundedCornerShape(3.dp))) {
            Text("RUN", color = BrandSoft, fontSize = 4.0.sp, lineHeight = 4.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 1.dp, vertical = 0.8.dp))
        }
    }
}

@Composable
private fun AboutSectionIcon() {
    Box(Modifier.padding(6.dp).size(30.dp)) {
        Image(painterResource(R.drawable.openglesscope_scope_wordmark), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.align(Alignment.CenterStart).width(27.dp).height(15.dp))
        Surface(shape = RoundedCornerShape(4.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd)) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(1.2.dp).size(9.dp))
        }
    }
}

@Composable
private fun DisplaySectionBadgeIcon(label: String) {
    Box(Modifier.padding(7.dp).size(27.dp)) {
        Icon(painter = painterResource(R.drawable.ic_display), contentDescription = null, tint = BrandSoft, modifier = Modifier.align(Alignment.TopStart).size(22.dp))
        Surface(shape = RoundedCornerShape(3.dp), color = SurfaceDark, modifier = Modifier.align(Alignment.BottomEnd).border(0.7.dp, BrandSoft, RoundedCornerShape(3.dp))) {
            Text(trademarkApiDisplayText(label), color = BrandSoft, fontSize = if (label == "MODE") 4.2.sp else 5.2.sp, lineHeight = if (label == "MODE") 4.5.sp else 5.4.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 1.2.dp, vertical = 0.8.dp))
        }
    }
}

@Composable
private fun CapabilitySectionCard(title: String, content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.extraLarge
    Surface(
        color = ComposeColor(0xFF181516),
        shape = shape,
        modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val sectionIcon = capabilitySectionIcon(title)
                Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
                    SectionHeaderIcon(title, sectionIcon)
                }
                Text(
                    trademarkApiDisplayText(title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ComposeColor(0xFFF7F2F3),
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
            }
            HorizontalDivider(color = ComposeColor(0xFF2A2527))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
        }
    }
}

@Composable
private fun CapabilityItemCard(containerColor: ComposeColor = ComposeColor(0xFF181516), content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Surface(color = containerColor, shape = shape, modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { content() }
    }
}

private fun shareEvidenceText(context: Context, text: String) {
    runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Share OpenGLESScope evidence")) }
}

private fun Modifier.consumeDesktopSecondaryMouseInput(enabled: Boolean): Modifier {
    if (!enabled) return this
    return pointerInput(enabled) {
        awaitPointerEventScope {
            var secondarySequence = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.none { it.type == PointerType.Mouse }) continue
                if (secondarySequence || event.buttons.isSecondaryPressed) {
                    event.changes.forEach { change -> if (!change.isConsumed) change.consume() }
                    secondarySequence = event.buttons.isSecondaryPressed
                }
            }
        }
    }
}

@Composable
private fun ExpressiveDetailDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val horizontalMargin = if (configuration.screenWidthDp < 360) 10.dp else 18.dp
    val verticalMargin = if (configuration.screenHeightDp < 520 || landscape) 8.dp else 16.dp
    val availableHeight = maxOf(280.dp, configuration.screenHeightDp.dp - verticalMargin * 2)
    val dialogHeight = if (landscape) availableHeight else minOf(720.dp, availableHeight)
    val dialogWidth = if (landscape) 720.dp else 640.dp
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().padding(horizontal = horizontalMargin, vertical = verticalMargin), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = dialogWidth).height(dialogHeight),
                shape = MaterialTheme.shapes.extraLarge,
                color = SurfaceRaised,
                contentColor = TextPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.46f)),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
                            Icon(painterResource(capabilitySectionIcon(title)), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(10.dp).size(21.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Detailed OpenGL® ES™ / EGL™ evidence", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Column(
                            Modifier.fillMaxWidth().desktopVerticalPointerScroll(scrollState).dpadScrollableNavigation(scrollState).verticalScroll(scrollState).focusable().focusGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) { CompositionLocalProvider(LocalDetailKeyValuePresentation provides true) { content() } }
                        ExpressiveScrollHints(scrollState, Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp))
                    }
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 66.dp).padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpressiveContainedIconTextButton("Close", R.drawable.ic_close, onClick = onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveEvidenceRow(key: String, value: String) {
    val expandedTextLayout = preferExpandedTextLayout()
    val detailPresentation = LocalDetailKeyValuePresentation.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked = if (detailPresentation) {
            expandedTextLayout || maxWidth < 420.dp || key.length > 22 || value.length > 30 || value.contains('\n')
        } else {
            expandedTextLayout || maxWidth < 360.dp || key.length > 26 || value.length > 34 || value.contains('\n')
        }
        val shape: Shape = if (detailPresentation) MaterialTheme.shapes.medium else RoundedCornerShape(16.dp)
        Surface(
            color = ComposeColor(0xFF211E1F),
            shape = shape,
            border = if (detailPresentation) androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant) else null,
            modifier = Modifier.fillMaxWidth().then(tvBrowseModifier(shape)).semantics(mergeDescendants = true) { }
        ) {
            if (stacked) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = if (detailPresentation) 13.dp else 14.dp, vertical = if (detailPresentation) 10.dp else 11.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(trademarkApiDisplayText(key), color = TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = if (detailPresentation) FontWeight.SemiBold else FontWeight.Normal)
                    Text(trademarkApiDisplayText(value.ifBlank { "Unavailable" }), color = ComposeColor(0xFFE7DFE1), style = if (detailPresentation) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall)
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = if (detailPresentation) 13.dp else 14.dp, vertical = if (detailPresentation) 10.dp else 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trademarkApiDisplayText(key), color = TextMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.88f))
                    Text(trademarkApiDisplayText(value.ifBlank { "Unavailable" }), color = ComposeColor(0xFFE7DFE1), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.12f))
                }
            }
        }
    }
}

@Composable
private fun DetailEvidenceRow(key: String, value: String) = ExpressiveEvidenceRow(key, value)

@Composable
private fun EvidenceCopyDialog(key: String, value: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val environment = LocalEvidenceActionEnvironment.current
    val provenance = remember(key, value) { evidenceProvenance(key, value) }
    val referenceToken = remember(key, value) { evidenceTokenForReference(key, value) }
    ExpressiveDetailDialog("Evidence provenance", onDismiss) {
        DetailEvidenceRow("Evidence key", key)
        DetailEvidenceRow("Value", value.ifBlank { "Unavailable" })
        DetailEvidenceRow("Evidence class", provenance.evidenceClass)
        DetailEvidenceRow("Source", provenance.source)
        DetailEvidenceRow("Query/API path", provenance.queryPath)
        DetailEvidenceRow("Query group", provenance.queryGroup)
        DetailEvidenceRow("Registry relationship", provenance.registryRelation)
        Text(trademarkApiDisplayText(provenance.interpretation), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        TransientActionButton("Copy name + value", "Copy the exact displayed evidence pair", R.drawable.ic_copy) {
            runCatching { copyEvidenceText(context, "OpenGLESScope evidence", "$key = $value") }.isSuccess
        }
        ExpressiveActionButton("Share evidence", "Android Sharesheet · explicit user action", R.drawable.ic_share, trailingIcon = R.drawable.ic_open_external) {
            shareEvidenceText(context, "$key = $value")
        }
        TransientActionButton("Add to watched evidence", "Local bounded watch list", R.drawable.ic_watch_add,
            enabled = referenceToken.isNotBlank() && environment != null, idleTrailingIcon = R.drawable.ic_add
        ) { runCatching { environment?.addWatch?.invoke(referenceToken) }.isSuccess }
        ExpressiveActionButton("Open in Encyclopedia", "Open the closest OpenGL® ES™/EGL™ registry token", R.drawable.ic_book,
            enabled = referenceToken.isNotBlank() && environment != null
        ) { environment?.openEncyclopedia?.invoke(referenceToken); onDismiss() }
    }
}

@Composable
private fun CapabilityKeyValueStatic(key: String, value: String) = ExpressiveEvidenceRow(key, value)

@Composable
private fun CapabilityKeyValue(key: String, value: String) {
    val context = LocalContext.current
    val environment = LocalEvidenceActionEnvironment.current
    val referenceToken = remember(key, value) { evidenceTokenForReference(key, value) }
    var showActions by remember(key, value) { mutableStateOf(false) }
    var showQuickMenu by remember(key, value) { mutableStateOf(false) }
    var pressed by remember(key, value) { mutableStateOf(false) }
    var tvLongPressConsumed by remember(key, value) { mutableStateOf(false) }
    var tvLongPressJob by remember(key, value) { mutableStateOf<Job?>(null) }
    val tvLongPressScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTelevision = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
    val suppressDesktopQuickMenu = remember(context) { isChromeOsRuntime(context) || isAndroidPcFormFactor(context) || hasFreeformWindowManagement(context) }
    val pressScale by animateFloatAsState(if (pressed) 0.985f else 1f, tween(110), label = "evidenceHoldScale")
    val pressHighlightAlpha by animateFloatAsState(if (pressed) 0.58f else 0f, tween(110), label = "evidenceHoldHighlight")
    val pressBorderAlpha by animateFloatAsState(if (pressed) 0.46f else 0f, tween(140, easing = FastOutSlowInEasing), label = "evidenceHoldBorder")
    Box {
        Box(
            Modifier.fillMaxWidth()
                .graphicsLayer(scaleX = pressScale, scaleY = pressScale)
                .background(BrandContainer.copy(alpha = pressHighlightAlpha), RoundedCornerShape(14.dp))
                .border(1.dp, BrandSoft.copy(alpha = pressBorderAlpha), RoundedCornerShape(14.dp))
                .consumeDesktopSecondaryMouseInput(suppressDesktopQuickMenu)
                .onPreviewKeyEvent { event ->
                    if (!isTelevision) return@onPreviewKeyEvent false
                    val native = event.key.nativeKeyCode
                    val activation = event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter || native == AndroidKeyEvent.KEYCODE_BUTTON_SELECT || native == AndroidKeyEvent.KEYCODE_BUTTON_A
                    if (!activation) return@onPreviewKeyEvent false
                    when (event.type) {
                        KeyEventType.KeyDown -> {
                            if (tvLongPressConsumed) true else {
                                val active = tvLongPressJob
                                if (active == null || !active.isActive) {
                                    tvLongPressJob = tvLongPressScope.launch { delay(550L); showActions = true; tvLongPressConsumed = true }
                                }
                                false
                            }
                        }
                        KeyEventType.KeyUp -> {
                            val consumed = tvLongPressConsumed
                            tvLongPressJob?.cancel(); tvLongPressJob = null; tvLongPressConsumed = false
                            consumed
                        }
                        else -> false
                    }
                }
                .pointerInput(key, value) {
                    detectTapGestures(
                        onPress = { pressed = true; try { tryAwaitRelease() } finally { pressed = false } },
                        onLongPress = { showActions = true }
                    )
                }
                .pointerInput(key, value, suppressDesktopQuickMenu) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                if (!suppressDesktopQuickMenu) showQuickMenu = true
                                event.changes.forEach { change -> if (!change.isConsumed) change.consume() }
                            }
                        }
                    }
                }
                .semantics { customActions = listOf(CustomAccessibilityAction("Evidence actions") { showActions = true; true }) }
        ) { CapabilityKeyValueStatic(key, value) }
        if (!suppressDesktopQuickMenu) {
            DropdownMenu(expanded = showQuickMenu, onDismissRequest = { showQuickMenu = false }) {
                DropdownMenuItem(text = { Text("Copy name + value") }, onClick = { showQuickMenu = false; copyEvidenceText(context, "OpenGLESScope evidence", "$key = $value") })
                DropdownMenuItem(text = { Text("Share evidence") }, onClick = { showQuickMenu = false; shareEvidenceText(context, "$key = $value") })
                DropdownMenuItem(text = { Text("Add to watched evidence") }, enabled = referenceToken.isNotBlank() && environment?.addWatch != null, onClick = { showQuickMenu = false; environment?.addWatch?.invoke(referenceToken) })
                DropdownMenuItem(text = { Text("Open in Encyclopedia") }, enabled = referenceToken.isNotBlank() && environment?.openEncyclopedia != null, onClick = { showQuickMenu = false; environment?.openEncyclopedia?.invoke(referenceToken) })
                DropdownMenuItem(text = { Text("More details") }, onClick = { showQuickMenu = false; showActions = true })
            }
        }
    }
    if (showActions) EvidenceCopyDialog(key, value, onDismiss = { showActions = false })
}

@Composable
private fun CapabilityStatusBadge(label: String, positive: Boolean? = null) {
    val normalized = label.trim().uppercase().replace('_', ' ')
    val available = positive == null && (normalized == "AVAILABLE" || normalized.startsWith("AVAILABLE "))
    val unknown = positive == null && (normalized == "UNKNOWN" || normalized.startsWith("UNKNOWN ") || normalized == "UNRESOLVED")
    val unavailable = positive == null && (normalized == "UNAVAILABLE" || normalized.startsWith("UNAVAILABLE "))
    val incomplete = positive == null && normalized == "INCOMPLETE"
    val notApplicable = positive == null && (normalized == "NOT APPLICABLE" || normalized.startsWith("NOT APPLICABLE "))
    val background = when {
        positive == true -> ComposeColor(0xFF173421)
        positive == false -> ComposeColor(0xFF3A1D20)
        available -> ComposeColor(0xFF172B3A)
        unavailable -> ComposeColor(0xFF332A16)
        incomplete -> ComposeColor(0xFF30243A)
        notApplicable -> ComposeColor(0xFF272727)
        unknown -> ComposeColor(0xFF252525)
        else -> SurfaceTonal
    }
    val foreground = when {
        positive == true -> ComposeColor(0xFF73C991)
        positive == false -> ComposeColor(0xFFFF8A8A)
        available -> ComposeColor(0xFF7CC4FF)
        unavailable -> ComposeColor(0xFFFFC857)
        incomplete -> ComposeColor(0xFFD3A4FF)
        notApplicable -> ComposeColor(0xFFC4C4C4)
        unknown -> ComposeColor(0xFFA8A8A8)
        else -> TextSecondary
    }
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(trademarkApiDisplayText(label), color = foreground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
    }
}

@Composable
private fun ExpressiveActionButton(
    title: String,
    subtitle: String,
    icon: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    compact: Boolean = false,
    trailingIcon: Int = R.drawable.ic_chevron_right,
    trailingTint: ComposeColor? = null,
    readableDisabledTrailing: Boolean = false,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val shape = if (compact) MaterialTheme.shapes.large else MaterialTheme.shapes.largeIncreased
    val container = if (!enabled) ComposeColor(0xFF111111) else if (focused) ComposeColor(0xFF2B1726) else ComposeColor(0xFF1A1718)
    val iconContainer = if (enabled) BrandContainer else ComposeColor(0xFF181818)
    val accent = if (enabled) BrandSoft else ComposeColor(0xFF606064)
    val titleColor = if (enabled) ComposeColor(0xFFF7F2F3) else ComposeColor(0xFF6C696A)
    val detailColor = if (enabled) ComposeColor(0xFFB6ACAE) else ComposeColor(0xFF5A5758)
    val trailingContainerColor = when {
        enabled -> ComposeColor(0xFF291721)
        readableDisabledTrailing -> BrandContainer.copy(alpha = 0.80f)
        else -> ComposeColor(0xFF171717)
    }
    val trailingContentColor = when {
        !enabled && readableDisabledTrailing -> BrandSoft.copy(alpha = 0.90f)
        !enabled -> accent
        else -> trailingTint ?: accent
    }
    Surface(
        color = container,
        shape = shape,
        modifier = modifier.border(if (focused && enabled) 2.dp else 0.dp, if (focused && enabled) BrandSoft else ComposeColor.Transparent, shape)
    ) {
        if (compact) {
            Column(
                Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(17.dp), color = iconContainer) {
                        Box(Modifier.padding(9.dp)) { ActionButtonIconArtwork(title, icon, accent) }
                    }
                    IconButton(
                        onClick = onClick,
                        enabled = enabled,
                        modifier = Modifier.size(48.dp).onFocusChanged { focused = it.isFocused },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = trailingContainerColor, contentColor = trailingContentColor, disabledContainerColor = trailingContainerColor, disabledContentColor = trailingContentColor)
                    ) {
                        AnimatedContent(targetState = trailingIcon, label = "actionTrailingIconCompact") { currentIcon ->
                            Icon(painter = painterResource(currentIcon), contentDescription = title, tint = trailingContentColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(trademarkApiDisplayText(title), color = titleColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(trademarkApiDisplayText(subtitle), color = detailColor, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = RoundedCornerShape(18.dp), color = iconContainer) {
                    Box(Modifier.padding(10.dp)) { ActionButtonIconArtwork(title, icon, accent) }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(trademarkApiDisplayText(title), color = titleColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(trademarkApiDisplayText(subtitle), color = detailColor, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.size(48.dp).onFocusChanged { focused = it.isFocused },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = trailingContainerColor, contentColor = trailingContentColor, disabledContainerColor = trailingContainerColor, disabledContentColor = trailingContentColor)
                ) {
                    AnimatedContent(targetState = trailingIcon, label = "actionTrailingIcon") { currentIcon ->
                        Icon(painter = painterResource(currentIcon), contentDescription = title, tint = trailingContentColor, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveIdentityBlock(title: String, subtitle: String, icon: Int) {
    Surface(color = ComposeColor(0xFF181516), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = BrandContainer) { Icon(painterResource(icon), null, tint = BrandSoft, modifier = Modifier.padding(12.dp).size(24.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(trademarkApiDisplayText(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ComposeColor(0xFFF7F2F3))
                Text(trademarkApiDisplayText(subtitle), style = MaterialTheme.typography.bodySmall, color = ComposeColor(0xFFB6ACAE))
            }
        }
    }
}

@Composable
private fun ExpressiveVersionBlock(application: String, version: String, versionCode: String, packageName: String, abi: String) {
    val expandedTextLayout = preferExpandedTextLayout()
    Surface(color = SurfaceRaised, shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Surface(shape = RoundedCornerShape(19.dp), color = BrandContainer) {
                    Image(
                        painter = painterResource(R.drawable.openglesscope_logo_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(46.dp)
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(application, style = if (application.length > 11) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Version $version", color = BrandSoft, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = ComposeColor(0xFF272224)) {
                    Text("#$versionCode", color = ComposeColor(0xFFC7BEC0), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
                }
            }
            if (expandedTextLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveInfoPill("Installed ABI", abi, Modifier.fillMaxWidth())
                    ExpressiveInfoPill("Package", packageName, Modifier.fillMaxWidth())
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveInfoPill("Installed ABI", abi, Modifier.weight(1f))
                    ExpressiveInfoPill("Package", packageName, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExpressiveInfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = ComposeColor(0xFF211E1F), shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(trademarkApiDisplayText(label), color = ComposeColor(0xFF968D8F), style = MaterialTheme.typography.labelSmall)
            Text(trademarkApiDisplayText(value), color = ComposeColor(0xFFE7DFE1), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier) {
    val shape = MaterialTheme.shapes.large
    Surface(
        color = BrandContainer,
        contentColor = TextPrimary,
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.34f)),
        modifier = modifier.animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)).then(tvBrowseModifier(shape))
    ) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(trademarkApiDisplayText(title), color = BrandSoft, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(trademarkApiDisplayText(value), color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SemanticDialogTitle(text: String, icon: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
            Icon(painterResource(icon), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(9.dp).size(20.dp))
        }
        Text(trademarkApiDisplayText(text), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuestionDialogTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = BrandContainer) {
            Icon(painterResource(R.drawable.ic_question), contentDescription = null, tint = BrandSoft, modifier = Modifier.padding(9.dp).size(20.dp))
        }
        Text(trademarkApiDisplayText(text), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressivePrimaryIconTextButton(label: String, icon: Int, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(20.dp),
            pressedShape = RoundedCornerShape(26.dp)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brand,
            contentColor = TextPrimary,
            disabledContainerColor = SurfaceLow,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(trademarkApiDisplayText(label), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ExpressiveContainedTextButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = BrandContainer,
            contentColor = BrandSoft,
            disabledContainerColor = SurfaceLow,
            disabledContentColor = TextMuted
        )
    ) {
        Text(trademarkApiDisplayText(label), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ExpressiveContainedIconTextButton(label: String, icon: Int, modifier: Modifier = Modifier, enabled: Boolean = true, fontWeight: FontWeight = FontWeight.SemiBold, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shapes = ButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(24.dp)
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = BrandContainer,
            contentColor = BrandSoft,
            disabledContainerColor = SurfaceLow,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.heightIn(min = 24.dp)) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(trademarkApiDisplayText(label), fontWeight = fontWeight, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ExpressiveCloseButton(enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, colors = ButtonDefaults.textButtonColors(contentColor = BrandSoft, disabledContentColor = TextMuted)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(17.dp))
            }
            Text("Close", fontWeight = FontWeight.Normal, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun UpdateConfirmationDialog(update: AppUpdate, networkAvailable: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val expandedTextLayout = preferExpandedTextLayout()
    val releaseNotesMaxHeight = if (expandedTextLayout) 220.dp else 360.dp
    AlertDialog(
        modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = SurfaceRaised,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        tonalElevation = 0.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                SemanticDialogTitle("Download OpenGLESScope ${update.version}?", R.drawable.ic_update_available)
                Text("Review the target build and release notes before any APK download starts.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.medium, color = SurfaceTonal, contentColor = TextPrimary) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        UpdateDialogKeyValue("Installed version", "${update.installedVersion} (versionCode ${update.installedVersionCode})")
                        UpdateDialogKeyValue("Available release", update.version)
                        UpdateDialogKeyValue("Installed ABI", update.installedAbi)
                        UpdateDialogKeyValue("Download ABI", update.downloadAbi)
                        UpdateDialogKeyValue("APK asset", update.assetName)
                        UpdateDialogKeyValue("Downloaded versionCode", "Verified from the APK before installation")
                    }
                }
                Text("Release notes", color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Surface(shape = MaterialTheme.shapes.medium, color = SurfaceLow, contentColor = TextPrimary) {
                    ReleaseNotesContent(update.releaseNotes, Modifier.fillMaxWidth().heightIn(max = releaseNotesMaxHeight))
                }
                Text("The APK is validated for official release provenance, package identity, signing certificate, versionCode and versionName before Android's installer is opened.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                if (update.expectedSha256 != null) Text("The published GitHub SHA-256 digest is also verified before package inspection.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                if (!networkAvailable) Text("Download is disabled until Android reports a validated internet connection.", color = ComposeColor(0xFF9CCBFF), style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { ExpressivePrimaryIconTextButton("Download update", R.drawable.ic_download_update, enabled = networkAvailable, onClick = onConfirm) },
        dismissButton = { ExpressiveCancelButton(onClick = onDismiss) }
    )
}

private fun formatUpdateBytesDisplay(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val units = listOf("KiB", "MiB", "GiB")
    var value = bytes.toDouble()
    var unitIndex = -1
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    return if (value >= 100.0) "%.0f %s".format(java.util.Locale.US, value, units[unitIndex]) else "%.1f %s".format(java.util.Locale.US, value, units[unitIndex])
}

private fun formatUpdateSpeedDisplay(bytesPerSecond: Long): String =
    if (bytesPerSecond <= 0L) "—" else "${formatUpdateBytesDisplay(bytesPerSecond)}/s"

@Composable
private fun ExpressiveDeterminateDownloadProgress(progress: Float, modifier: Modifier = Modifier) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "updateDownloadProgress"
    )
    val shape = RoundedCornerShape(999.dp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.fillMaxWidth().height(14.dp).clip(shape).background(ComposeColor(0xFF2A2022))) {
            if (animatedProgress > 0f) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(animatedProgress).clip(shape).background(
                        Brush.horizontalGradient(
                            listOf(ComposeColor(0xFF79305D), BrandSoft, ComposeColor(0xFFF59AD8))
                        )
                    )
                )
            }
        }
        Text(
            "${"%.1f".format(java.util.Locale.US, target * 100f)}%",
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun UpdateTransferDialog(
    state: UpdateTransferState,
    networkAvailable: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestCancel: () -> Unit,
    onInstall: () -> Unit,
    onClose: () -> Unit
) {
    val terminal = state.phase in setOf(UpdateTransferPhase.COMPLETED, UpdateTransferPhase.CANCELED, UpdateTransferPhase.FAILED)
    val title = when (state.phase) {
        UpdateTransferPhase.CONNECTING -> "Connecting to update"
        UpdateTransferPhase.DOWNLOADING -> "Downloading OpenGLESScope ${state.update.version}"
        UpdateTransferPhase.PAUSED -> "Update download paused"
        UpdateTransferPhase.VERIFYING -> "Verifying update"
        UpdateTransferPhase.COMPLETED -> "Update downloaded"
        UpdateTransferPhase.CANCELED -> "Update canceled"
        UpdateTransferPhase.FAILED -> "Update download failed"
    }
    val titleIcon = when (state.phase) {
        UpdateTransferPhase.COMPLETED -> R.drawable.ic_check
        UpdateTransferPhase.CANCELED -> R.drawable.ic_close
        UpdateTransferPhase.FAILED -> R.drawable.ic_network_disconnected
        else -> R.drawable.ic_download_update
    }
    val connection = when {
        state.phase == UpdateTransferPhase.CANCELED -> "Canceled"
        state.phase == UpdateTransferPhase.FAILED -> "Error"
        state.phase == UpdateTransferPhase.COMPLETED -> "Completed"
        state.phase == UpdateTransferPhase.PAUSED -> "Paused"
        !networkAvailable && state.phase in setOf(UpdateTransferPhase.CONNECTING, UpdateTransferPhase.DOWNLOADING) -> "Connection unavailable"
        else -> state.connectionStatus
    }
    val progressTotal = state.totalBytes?.takeIf { it > 0L } ?: state.update.assetSizeBytes?.takeIf { it > 0L }
    val progressFraction = when {
        state.phase == UpdateTransferPhase.COMPLETED || state.phase == UpdateTransferPhase.VERIFYING -> 1f
        progressTotal != null -> (state.bytesDownloaded.toDouble() / progressTotal.toDouble()).coerceIn(0.0, 1.0).toFloat()
        else -> 0f
    }
    val progressText = progressTotal?.let { total ->
        "${formatUpdateBytesDisplay(state.bytesDownloaded)} / ${formatUpdateBytesDisplay(total)} (${"%.1f".format(java.util.Locale.US, progressFraction * 100f)}%)"
    } ?: formatUpdateBytesDisplay(state.bytesDownloaded)
    val logState = rememberLazyListState()
    LaunchedEffect(state.log.size) {
        if (state.log.isNotEmpty()) logState.animateScrollToItem(state.log.lastIndex)
    }
    AlertDialog(
        modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
        onDismissRequest = { if (terminal) onClose() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = SurfaceRaised,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        tonalElevation = 0.dp,
        title = { SemanticDialogTitle(title, titleIcon) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.phase !in setOf(UpdateTransferPhase.CANCELED, UpdateTransferPhase.FAILED)) {
                    ExpressiveDeterminateDownloadProgress(progressFraction, Modifier.fillMaxWidth())
                }
                Surface(shape = MaterialTheme.shapes.medium, color = SurfaceTonal, contentColor = TextPrimary) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        UpdateDialogKeyValue("Connection", connection)
                        UpdateDialogKeyValue("Download speed", formatUpdateSpeedDisplay(state.bytesPerSecond))
                        UpdateDialogKeyValue("Downloaded", progressText)
                        UpdateDialogKeyValue("APK asset", state.update.assetName)
                    }
                }
                state.errorMessage?.let { Text(it, color = ComposeColor(0xFFFF8A8A), style = MaterialTheme.typography.bodySmall) }
                Text("Live log", color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Surface(shape = MaterialTheme.shapes.medium, color = ComposeColor.Black, contentColor = TextPrimary) {
                    Box(Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 220.dp).padding(12.dp)) {
                        LazyColumn(
                            state = logState,
                            modifier = Modifier.fillMaxWidth().desktopVerticalPointerScroll(logState).tvRemoteLazyListNavigation(logState).focusable().focusGroup(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            userScrollEnabled = true
                        ) {
                            itemsIndexed(state.log, key = { index, _ -> "update-log:$index" }) { _, line ->
                                Text(line, color = ComposeColor(0xFFB9D6B9), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                            }
                        }
                        ExpressiveScrollHints(logState, Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                if (state.phase == UpdateTransferPhase.COMPLETED) {
                    Text("The verified APK remains available here until you choose Install or Close.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            when (state.phase) {
                UpdateTransferPhase.CONNECTING, UpdateTransferPhase.DOWNLOADING, UpdateTransferPhase.PAUSED -> ExpressiveContainedIconTextButton("Cancel", R.drawable.ic_close, onClick = onRequestCancel)
                UpdateTransferPhase.COMPLETED -> ExpressivePrimaryIconTextButton("Install", R.drawable.ic_download_update, onClick = onInstall)
                else -> Unit
            }
        },
        dismissButton = {
            when (state.phase) {
                UpdateTransferPhase.CONNECTING, UpdateTransferPhase.DOWNLOADING -> ExpressiveTextButton("Pause", onClick = onPause)
                UpdateTransferPhase.PAUSED -> ExpressiveTextButton("Resume", onClick = onResume)
                UpdateTransferPhase.COMPLETED, UpdateTransferPhase.CANCELED, UpdateTransferPhase.FAILED -> ExpressiveCloseButton(onClick = onClose)
                UpdateTransferPhase.VERIFYING -> Unit
            }
        }
    )
}

@Composable
private fun UpdateCancelConfirmationDialog(onResume: () -> Unit, onConfirmCancel: () -> Unit) {
    AlertDialog(
        modifier = Modifier.border(1.dp, BrandSoft.copy(alpha = 0.46f), MaterialTheme.shapes.extraLarge),
        onDismissRequest = onResume,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = SurfaceRaised,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        tonalElevation = 0.dp,
        title = { QuestionDialogTitle("Cancel update download?") },
        text = { Text("The download has been paused. Canceling removes the partial APK. Resume continues the current download.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { ExpressiveContainedIconTextButton("Cancel download", R.drawable.ic_close, onClick = onConfirmCancel) },
        dismissButton = { ExpressiveTextButton("Resume", onClick = onResume) }
    )
}

@Composable
private fun ReleaseNotesContent(markdown: String, modifier: Modifier = Modifier) {
    val lines = remember(markdown) { markdown.lines() }
    val listState = rememberLazyListState()
    Box(modifier.padding(14.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().desktopVerticalPointerScroll(listState).tvRemoteLazyListNavigation(listState).focusGroup(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            userScrollEnabled = true
        ) {
            itemsIndexed(lines, key = { index, _ -> "release-note:$index" }) { _, raw ->
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
        ExpressiveScrollHints(listState, Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp))
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
private fun hasValidatedInternetCapabilities(capabilities: NetworkCapabilities): Boolean =
    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

private fun hasValidatedInternet(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return hasValidatedInternetCapabilities(capabilities)
}

private fun open(c: Context, url: String) {
    if (!hasValidatedInternet(c)) {
        android.widget.Toast.makeText(c, "Validated internet connection required.", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private val IPV6_FIRST_DNS = Dns { hostname ->
    Dns.SYSTEM.lookup(hostname).sortedWith(compareBy<InetAddress> { if (it is Inet6Address) 0 else 1 })
}

private val NO_REDIRECT_HTTP_CLIENT: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .dns(IPV6_FIRST_DNS)
        .fastFallback(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
}

private val DATABASE_HTTP_CLIENT: OkHttpClient by lazy { NO_REDIRECT_HTTP_CLIENT }

private val UPDATE_METADATA_HTTP_CLIENT: OkHttpClient by lazy { NO_REDIRECT_HTTP_CLIENT }

private val UPDATE_DOWNLOAD_HTTP_CLIENT: OkHttpClient by lazy {
    NO_REDIRECT_HTTP_CLIENT.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.MINUTES)
        .followRedirects(false)
        .followSslRedirects(false)
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

private data class DatabaseSubmissionResult(val success: Boolean, val reportId: String?, val summary: String, val log: String)

private fun boundedDatabaseSubmissionLog(value: String): String {
    val limit = 96 * 1024
    return if (value.length <= limit) value else value.take(limit) + "\n[log truncated at $limit characters]"
}

private fun databaseSubmissionExceptionLog(phase: String, error: Throwable): String = boundedDatabaseSubmissionLog(buildString {
    appendLine("OpenGLESScope Database submission")
    appendLine("result=failure")
    appendLine("phase=$phase")
    appendLine("exception=${error.javaClass.name}")
    appendLine("message=${error.message ?: "Unavailable"}")
    append(error.stackTraceToString())
})

private fun databaseSubmissionFailure(summary: String, phase: String, detail: String, httpCode: Int? = null, responseBody: String? = null): DatabaseSubmissionResult {
    val log = boundedDatabaseSubmissionLog(buildString {
        appendLine("OpenGLESScope Database submission")
        appendLine("result=failure")
        appendLine("phase=$phase")
        if (httpCode != null) appendLine("httpStatus=$httpCode")
        appendLine("detail=$detail")
        if (responseBody != null) {
            appendLine("responseBody:")
            append(responseBody.ifBlank { "<empty>" })
        }
    })
    return DatabaseSubmissionResult(false, null, summary, log)
}

private suspend fun submitReport(context: Context, report: GlReport, display: DisplayInfo): DatabaseSubmissionResult = withContext(Dispatchers.IO) {
    if (!hasValidatedInternet(context)) return@withContext databaseSubmissionFailure("Submission blocked: no validated internet connection is available.", "network-validation", "Android does not report a validated internet connection.")
    if (!report.available) return@withContext databaseSubmissionFailure("Submission unavailable until a complete OpenGL ES report exists.", "report-validation", "report.available=false")
    val base = DATABASE_API.toHttpUrlOrNull() ?: return@withContext databaseSubmissionFailure("The official OpenGLESScope Database endpoint is invalid.", "endpoint-validation", "Endpoint parsing failed.")
    if (base.scheme != "https" || base.host != "openglesscope-database-api.openglesscope.workers.dev" || base.username.isNotEmpty() || base.password.isNotEmpty() || base.query != null || base.fragment != null || base.encodedPath != "/") {
        return@withContext databaseSubmissionFailure("The official OpenGLESScope Database endpoint is invalid.", "endpoint-validation", "The fixed endpoint failed origin/path constraints.")
    }
    val payload = try {
        submissionJson(context, report, display).toString()
    } catch (error: Throwable) {
        return@withContext DatabaseSubmissionResult(false, null, "Submission failed: the complete report could not be serialized locally.", databaseSubmissionExceptionLog("serialization", error))
    }
    val payloadBytes = payload.toByteArray(Charsets.UTF_8)
    if (payloadBytes.size > 2 * 1024 * 1024) return@withContext databaseSubmissionFailure("Report exceeds the 2 MiB transport limit; no data was truncated.", "payload-validation", "payloadBytes=${payloadBytes.size}")
    val submissionUrl = base.newBuilder().addPathSegments("v1/reports").build()
    val req = Request.Builder().url(submissionUrl).header("Accept", "application/json")
        .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()
    try {
        DATABASE_HTTP_CLIENT.newCall(req).execute().use { res ->
            val body = readResponseTextLimited(res.body, 64 * 1024)
            if (!res.isSuccessful) {
                val message = runCatching { JSONObject(body).optString("error") }.getOrDefault("").ifBlank { "HTTP ${res.code}" }
                return@use databaseSubmissionFailure("Submission failed (HTTP ${res.code}): $message", "http-response", message, res.code, body)
            }
            val id = runCatching { JSONObject(body).optString("id") }.getOrDefault("")
            if (!id.matches(Regex("[a-f0-9]{64}"))) return@use databaseSubmissionFailure("Submission failed: Database returned a malformed report ID.", "response-validation", "Expected a 64-character lowercase hexadecimal report ID.", res.code, body)
            context.getSharedPreferences("analysis_tools", Context.MODE_PRIVATE).edit().putString("last_database_report_id", id).apply()
            DatabaseSubmissionResult(true, id, "Report submitted successfully · ${id.take(12)}", boundedDatabaseSubmissionLog(buildString {
                appendLine("OpenGLESScope Database submission")
                appendLine("result=success")
                appendLine("httpStatus=${res.code}")
                appendLine("reportId=$id")
            }))
        }
    } catch (error: Throwable) {
        if (error is CancellationException) throw error
        DatabaseSubmissionResult(false, null, "Submission failed: ${error.message?.take(240) ?: "network error"}", databaseSubmissionExceptionLog("network-request", error))
    }
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
    val internalFormats = JSONArray(r.internalFormats.map { f ->
        JSONObject()
            .put("target", f.target)
            .put("internalFormat", f.internalFormat)
            .put("status", f.status)
            .put("detail", f.detail)
            .put("sampleCounts", JSONArray(f.sampleCounts))
            .put("nvSampleProperties", JSONArray(f.nvSampleProperties.map { n ->
                JSONObject()
                    .put("samples", n.samples)
                    .put("multisamples", n.multisamples)
                    .put("supersampleScaleX", n.supersampleScaleX)
                    .put("supersampleScaleY", n.supersampleScaleY)
                    .put("conformant", n.conformant)
            }))
    })
    return JSONObject().apply {
        put("schemaVersion", SUBMISSION_SCHEMA_VERSION)
        put("application", JSONObject().put("name", "OpenGLESScope").put("packageName", "com.efishell.openglesscope").put("version", BuildConfig.VERSION_NAME).put("versionCode", BuildConfig.VERSION_CODE).put("applicationAbi", detectInstalledAbi(context)).put("supportedDeviceAbis", JSONArray(Build.SUPPORTED_ABIS.toList())))
        put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("brand", Build.BRAND).put("model", Build.MODEL).put("product", Build.PRODUCT).put("device", Build.DEVICE).put("board", Build.BOARD).put("hardware", Build.HARDWARE).put("androidRelease", Build.VERSION.RELEASE).put("codename", Build.VERSION.CODENAME).put("sdk", Build.VERSION.SDK_INT).put("buildId", Build.ID).put("incremental", Build.VERSION.INCREMENTAL).put("fingerprint", Build.FINGERPRINT).apply { if (Build.VERSION.SECURITY_PATCH.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) put("securityPatch", Build.VERSION.SECURITY_PATCH) })
        put("gpu", JSONObject().put("name", r.renderer).put("vendor", r.vendor))
        put("driver", JSONObject().put("mode", "System OpenGL ES/EGL").put("version", "Unavailable (OpenGL ES does not expose a standardized driver-version query)"))
        put("opengles", JSONObject().put("version", r.glVersion).put("major", r.glMajor).put("minor", r.glMinor).put("glslVersion", r.glslVersion).put("extensions", JSONArray(r.extensions)).put("extensionCount", r.extensions.size))
        put("egl", JSONObject().put("vendor", r.egl.vendor).put("version", r.egl.version).put("initializedVersion", r.egl.initializedVersion).put("clientApis", r.egl.clientApis).put("extensions", JSONArray(r.egl.extensions)).put("clientExtensions", JSONArray(r.egl.clientExtensions)).put("extensionCount", r.egl.extensions.size).put("clientExtensionCount", r.egl.clientExtensions.size))
        put("display", JSONObject().put("name", d.name).putNullable("modeId", d.modeId).putNullable("width", d.width).putNullable("height", d.height).putNullable("refreshRate", d.refreshRate).put("supportedModes", JSONArray(d.supportedModes)).putNullable("wideColor", d.wideColor).put("hdrTypes", JSONArray(d.hdrTypes)).put("hdrCapabilityStatus", d.hdrCapabilityStatus).putNullable("desiredMaxLuminance", d.desiredMaxLuminance).putNullable("desiredMaxAverageLuminance", d.desiredMaxAverageLuminance).putNullable("desiredMinLuminance", d.desiredMinLuminance))
        put("collection", JSONObject().put("status", if (r.available) "available" else "unavailable").put("complete", r.available).put("source", "active Android system EGL/OpenGL ES implementation"))
        put("technicalReport", JSONObject()
            .put("schemaVersion", TECHNICAL_REPORT_SCHEMA_VERSION)
            .put("glRuntime", JSONObject()
                .putNullable("contextFlags", r.glRuntime.contextFlags).putNullable("resetNotificationStrategy", r.glRuntime.resetNotificationStrategy).putNullable("resetNotificationStrategyQuery", r.glRuntime.resetNotificationStrategyQuery).putNullable("robustAccess", r.glRuntime.robustAccess).putNullable("robustAccessQuery", r.glRuntime.robustAccessQuery)
                .put("unavailableAttributes", JSONArray(r.glRuntime.unavailableAttributes.map { JSONObject().put("name", it.name).put("error", it.error) })))
            .put("eglRuntime", JSONObject()
                .put("boundApi", r.eglRuntime.boundApi).putNullable("configId", r.eglRuntime.configId).putNullable("clientType", r.eglRuntime.clientType).putNullable("clientVersion", r.eglRuntime.clientVersion).putNullable("renderBuffer", r.eglRuntime.renderBuffer)
                .put("currentContext", r.eglRuntime.currentContext).put("currentDisplay", r.eglRuntime.currentDisplay).put("currentDrawSurface", r.eglRuntime.currentDrawSurface).put("currentReadSurface", r.eglRuntime.currentReadSurface)
                .putNullable("surfaceGlColorspace", r.eglRuntime.surfaceGlColorspace).putNullable("surfaceGlColorspaceQuery", r.eglRuntime.surfaceGlColorspaceQuery).putNullable("surfaceVgAlphaFormat", r.eglRuntime.surfaceVgAlphaFormat).putNullable("surfaceVgAlphaFormatQuery", r.eglRuntime.surfaceVgAlphaFormatQuery).putNullable("surfaceVgColorspace", r.eglRuntime.surfaceVgColorspace).putNullable("surfaceVgColorspaceQuery", r.eglRuntime.surfaceVgColorspaceQuery).putNullable("surfaceConfigId", r.eglRuntime.surfaceConfigId)
                .putNullable("surfaceWidth", r.eglRuntime.surfaceWidth).putNullable("surfaceHeight", r.eglRuntime.surfaceHeight).putNullable("surfaceHorizontalResolution", r.eglRuntime.surfaceHorizontalResolution).putNullable("surfaceLargestPbuffer", r.eglRuntime.surfaceLargestPbuffer).putNullable("surfacePixelAspectRatio", r.eglRuntime.surfacePixelAspectRatio).putNullable("surfaceVerticalResolution", r.eglRuntime.surfaceVerticalResolution).putNullable("surfaceRenderBuffer", r.eglRuntime.surfaceRenderBuffer).putNullable("surfaceSwapBehavior", r.eglRuntime.surfaceSwapBehavior)
                .putNullable("surfaceTextureFormat", r.eglRuntime.surfaceTextureFormat).putNullable("surfaceTextureTarget", r.eglRuntime.surfaceTextureTarget).putNullable("surfaceMipmapTexture", r.eglRuntime.surfaceMipmapTexture).putNullable("surfaceMipmapLevel", r.eglRuntime.surfaceMipmapLevel).putNullable("surfaceMultisampleResolve", r.eglRuntime.surfaceMultisampleResolve)
                .put("unavailableAttributes", JSONArray(r.eglRuntime.unavailableAttributes.map { JSONObject().put("name", it.name).put("error", it.error) })))
            .put("eglCapabilities", JSONArray(r.eglCapabilities.map { JSONObject().put("name", it.name).put("status", it.status).put("value", it.value).put("detail", it.detail) }))
            .put("limits", JSONArray(r.limits.map { JSONObject().put("name", it.name).put("value", it.value) }))
            .put("extensions", JSONArray(r.extensions))
            .put("eglExtensions", JSONArray(r.egl.extensions))
            .put("eglClientExtensions", JSONArray(r.egl.clientExtensions))
            .put("compressedFormats", JSONArray(r.compressedFormats))
            .put("internalFormats", internalFormats)
            .put("shaderBinaryFormats", JSONArray(r.shaderBinaryFormats))
            .put("programBinaryFormats", JSONArray(r.programBinaryFormats))
            .put("precision", JSONArray(r.precision.map { JSONObject().put("shader", it.shader).put("type", it.type).put("rangeMin", it.rangeMin).put("rangeMax", it.rangeMax).put("precision", it.precision) }))
            .put("queryDiagnostics", JSONArray(r.diagnostics.map { JSONObject().put("name", it.name).put("status", it.status).put("detail", it.detail) }))
            .put("eglConfigs", configs)
            .put("display", JSONObject().put("name", d.name).putNullable("modeId", d.modeId).putNullable("width", d.width).putNullable("height", d.height).putNullable("refreshRate", d.refreshRate).put("supportedModes", JSONArray(d.supportedModes)).putNullable("wideColor", d.wideColor).put("hdrTypes", JSONArray(d.hdrTypes)).put("hdrCapabilityStatus", d.hdrCapabilityStatus).putNullable("desiredMaxLuminance", d.desiredMaxLuminance).putNullable("desiredMaxAverageLuminance", d.desiredMaxAverageLuminance).putNullable("desiredMinLuminance", d.desiredMinLuminance)))
        put("reportText", text)
    }
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject = put(name, value ?: JSONObject.NULL)

private data class ExportSnapshot(val filename: String, val path: String, val mime: String)

private fun exportSnapshotRoot(context: Context): File {
    val root = File(context.cacheDir, "report_exports")
    if (!root.exists() && !root.mkdirs()) throw IllegalStateException("Unable to create the report export cache")
    return root.canonicalFile
}

private fun validatedExportSnapshot(context: Context, snapshot: ExportSnapshot): File {
    if (snapshot.filename.isBlank() || snapshot.path.isBlank() || snapshot.mime !in setOf("text/plain", "text/html")) error("Invalid report export snapshot")
    val root = exportSnapshotRoot(context)
    val file = File(snapshot.path).canonicalFile
    val prefix = root.path.trimEnd(File.separatorChar) + File.separator
    if (!file.path.startsWith(prefix) || !file.isFile || !file.canRead() || file.length() <= 0L) error("Report export snapshot is unavailable")
    return file
}

private suspend fun createExportSnapshot(context: Context, filename: String, mime: String, contentFactory: () -> String): ExportSnapshot {
    require(mime == "text/plain" || mime == "text/html")
    val exportContext = currentCoroutineContext()
    val root = exportSnapshotRoot(context)
    val file = File.createTempFile("openglesscope_report_", ".tmp", root).canonicalFile
    val prefix = root.path.trimEnd(File.separatorChar) + File.separator
    if (!file.path.startsWith(prefix)) {
        file.delete()
        throw SecurityException("Unsafe report export cache path")
    }
    try {
        FileOutputStream(file, false).use { output ->
            val writer = output.writer(Charsets.UTF_8).buffered()
            val content = contentFactory()
            exportContext.ensureActive()
            writer.write(content)
            writer.flush()
            output.fd.sync()
        }
        exportContext.ensureActive()
        if (!file.isFile || !file.canRead() || file.length() <= 0L || file.length() > 64L * 1024L * 1024L) error("The report snapshot could not be persisted within the 64 MiB export bound")
        return ExportSnapshot(filename, file.absolutePath, mime)
    } catch (error: Throwable) {
        runCatching { file.delete() }
        throw error
    }
}

private fun deleteExportSnapshot(context: Context, snapshot: ExportSnapshot) {
    val root = runCatching { exportSnapshotRoot(context) }.getOrNull() ?: return
    val file = runCatching { File(snapshot.path).canonicalFile }.getOrNull() ?: return
    val prefix = root.path.trimEnd(File.separatorChar) + File.separator
    if (file.path.startsWith(prefix) && file.isFile) runCatching { file.delete() }
}

private fun cleanupStaleExportSnapshots(context: Context) {
    val root = runCatching { exportSnapshotRoot(context) }.getOrNull() ?: return
    val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
    root.listFiles().orEmpty().asSequence().filter { it.isFile && it.name.startsWith("openglesscope_report_") }.take(512).forEach { file ->
        val canonical = runCatching { file.canonicalFile }.getOrNull() ?: return@forEach
        if (canonical.lastModified() < cutoff) runCatching { canonical.delete() }
    }
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
    appendLine("Submission schema: $SUBMISSION_SCHEMA_VERSION")
    appendLine("Technical report schema: $TECHNICAL_REPORT_SCHEMA_VERSION")
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
    appendLine("HDR capability status: ${d.hdrCapabilityStatus}")
    appendLine("HDR types: ${hdrTypesText(d)}")
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
    appendLine("GL_CONTEXT_FLAGS: ${runtimeQueryEvidence(r, r.glRuntime.contextFlags, "GL_CONTEXT_FLAGS")}")
    appendLine("Reset notification strategy: ${runtimeQueryEvidence(r, r.glRuntime.resetNotificationStrategy, "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT")}")
    appendLine("Reset strategy query: ${r.glRuntime.resetNotificationStrategyQuery ?: "Not applicable"}")
    appendLine("Robust access: ${runtimeQueryEvidence(r, r.glRuntime.robustAccess?.toString(), "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT")}")
    appendLine("Robust access query: ${r.glRuntime.robustAccessQuery ?: "Not applicable"}")
    if (r.glRuntime.unavailableAttributes.isNotEmpty()) appendLine("Unavailable GL runtime attributes: ${r.glRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }}")
    appendLine()
    appendLine("EGL")
    appendLine("EGL_VENDOR: ${r.egl.vendor}")
    appendLine("EGL_VERSION: ${r.egl.version}")
    appendLine("Initialized EGL version: ${r.egl.initializedVersion}")
    appendLine("EGL_CLIENT_APIS: ${r.egl.clientApis}")
    appendLine("Bound client API: ${r.eglRuntime.boundApi}")
    appendLine("Current config ID: ${runtimeQueryEvidence(r, r.eglRuntime.configId?.toString(), "EGL_CONFIG_ID/context")}")
    appendLine("Context client type: ${runtimeQueryEvidence(r, r.eglRuntime.clientType, "EGL_CONTEXT_CLIENT_TYPE")}")
    appendLine("Context client version: ${runtimeQueryEvidence(r, r.eglRuntime.clientVersion?.toString(), "EGL_CONTEXT_CLIENT_VERSION")}")
    appendLine("Context render buffer: ${runtimeQueryEvidence(r, r.eglRuntime.renderBuffer, "EGL_RENDER_BUFFER/context")}")
    appendLine("Current EGL bindings: context=${r.eglRuntime.currentContext}, display=${r.eglRuntime.currentDisplay}, draw=${r.eglRuntime.currentDrawSurface}, read=${r.eglRuntime.currentReadSurface}")
    appendLine("Pbuffer: config=${runtimeQueryEvidence(r, r.eglRuntime.surfaceConfigId?.toString(), "EGL_CONFIG_ID/surface")} · size=${eglRuntimeSizeEvidence(r)} · GL colorspace=${runtimeQueryEvidence(r, r.eglRuntime.surfaceGlColorspace, "EGL_GL_COLORSPACE")} [query=${r.eglRuntime.surfaceGlColorspaceQuery ?: "Not applicable"}] · VG alpha=${runtimeQueryEvidence(r, r.eglRuntime.surfaceVgAlphaFormat, "EGL_VG_ALPHA_FORMAT")} [query=${r.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable"}] · VG colorspace=${runtimeQueryEvidence(r, r.eglRuntime.surfaceVgColorspace, "EGL_VG_COLORSPACE")} [query=${r.eglRuntime.surfaceVgColorspaceQuery ?: "Not applicable"}] · hRes=${runtimeQueryEvidence(r, r.eglRuntime.surfaceHorizontalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_HORIZONTAL_RESOLUTION")} · vRes=${runtimeQueryEvidence(r, r.eglRuntime.surfaceVerticalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_VERTICAL_RESOLUTION")} · aspect=${runtimeQueryEvidence(r, r.eglRuntime.surfacePixelAspectRatio?.let { eglScaledSurfaceEvidence(it, aspect = true) }, "EGL_PIXEL_ASPECT_RATIO")} · largest=${runtimeQueryEvidence(r, r.eglRuntime.surfaceLargestPbuffer?.let { eglBooleanEvidence(it) }, "EGL_LARGEST_PBUFFER")} · render=${runtimeQueryEvidence(r, r.eglRuntime.surfaceRenderBuffer, "EGL_RENDER_BUFFER/surface")} · swap=${runtimeQueryEvidence(r, r.eglRuntime.surfaceSwapBehavior, "EGL_SWAP_BEHAVIOR")} · texture=${runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureFormat, "EGL_TEXTURE_FORMAT")}/${runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureTarget, "EGL_TEXTURE_TARGET")} · mipmapTexture=${runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapTexture?.let { eglBooleanEvidence(it) }, "EGL_MIPMAP_TEXTURE")} · mipmapLevel=${runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapLevel?.toString(), "EGL_MIPMAP_LEVEL")} · multisampleResolve=${runtimeQueryEvidence(r, r.eglRuntime.surfaceMultisampleResolve, "EGL_MULTISAMPLE_RESOLVE")}")
    appendLine("Pbuffer config ID: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceConfigId?.toString(), "EGL_CONFIG_ID/surface")}")
    appendLine("Pbuffer GL colorspace: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceGlColorspace, "EGL_GL_COLORSPACE")}")
    appendLine("Pbuffer VG alpha format: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceVgAlphaFormat, "EGL_VG_ALPHA_FORMAT")}")
    appendLine("Pbuffer VG colorspace: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceVgColorspace, "EGL_VG_COLORSPACE")}")
    appendLine("Pbuffer horizontal resolution: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceHorizontalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_HORIZONTAL_RESOLUTION")}")
    appendLine("Pbuffer largest pbuffer: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceLargestPbuffer?.let { eglBooleanEvidence(it) }, "EGL_LARGEST_PBUFFER")}")
    appendLine("Pbuffer pixel aspect ratio: ${runtimeQueryEvidence(r, r.eglRuntime.surfacePixelAspectRatio?.let { eglScaledSurfaceEvidence(it, aspect = true) }, "EGL_PIXEL_ASPECT_RATIO")}")
    appendLine("Pbuffer vertical resolution: ${runtimeQueryEvidence(r, r.eglRuntime.surfaceVerticalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_VERTICAL_RESOLUTION")}")
    if (r.eglRuntime.unavailableAttributes.isNotEmpty()) appendLine("Unavailable EGL runtime attributes: ${r.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }}")
    appendLine()
    appendLine("DISPLAY & HDR")
    appendLine("Display: ${d.name}")
    appendLine("Current mode: ${d.modeId?.toString() ?: "Unavailable"} | ${if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width}x${d.height}" else "Unavailable"} | ${d.refreshRate?.let { "$it Hz" } ?: "Unavailable"}")
    appendLine("Supported display modes (${d.supportedModes.size}): ${if (d.supportedModes.isEmpty()) "Unavailable" else d.supportedModes.joinToString(" | ")}")
    appendLine("Refresh rate: ${d.refreshRate?.let { "$it Hz" } ?: "Unavailable"}")
    appendLine("Wide color gamut: ${when (d.wideColor) { true -> "Reported by Android display API"; false -> "Not supported by Android display API"; null -> "Unavailable on this Android API/display context" }}")
    appendLine("HDR capability status: ${d.hdrCapabilityStatus}")
    appendLine("HDR types: ${hdrTypesText(d)}")
    appendLine("Desired max luminance: ${d.desiredMaxLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine("Desired max average luminance: ${d.desiredMaxAverageLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine("Desired min luminance: ${d.desiredMinLuminance?.let { "$it cd/m²" } ?: "Unavailable"}")
    appendLine()
    appendLine("EGL CAPABILITY QUERIES (${r.eglCapabilities.size})")
    r.eglCapabilities.forEach { appendLine("${it.name}: ${it.status}${if (it.value.isBlank()) "" else " | ${it.value}"}${if (it.detail.isBlank()) "" else " | ${it.detail}"}") }
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
    appendLine("INTERNAL FORMAT SAMPLE SUPPORT (${r.internalFormats.size})")
    r.internalFormats.forEach { f ->
        val samples = f.sampleCounts.joinToString(", ").ifBlank { "None reported" }
        appendLine("${f.target} | ${f.internalFormat} | ${f.status} | samples=$samples${if (f.detail.isBlank()) "" else " | ${f.detail}"}")
        f.nvSampleProperties.forEach { n -> appendLine("  NV samples=${n.samples} | multisamples=${n.multisamples} | supersample=${n.supersampleScaleX}x${n.supersampleScaleY} | conformant=${n.conformant}") }
    }
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
        appendLine("${c.id} | ${eglConfigAnalysisValue(r, c)}")
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
        "Submission schema" to e(SUBMISSION_SCHEMA_VERSION),
        "Technical report schema" to e(TECHNICAL_REPORT_SCHEMA_VERSION),
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
    val glRows = rows(listOf("Driver mode" to "System OpenGL® ES™/EGL™", "Driver version" to "Unavailable (OpenGL® ES™ does not expose a standardized driver-version query)", "GL_RENDERER" to r.renderer, "GL_VENDOR" to r.vendor, "GL_VERSION" to r.glVersion, "Core version" to "${r.glMajor}.${r.glMinor}", "Core version provenance" to coreVersionProvenance(r), "GL_SHADING_LANGUAGE_VERSION" to r.glslVersion, "GL_CONTEXT_FLAGS" to runtimeQueryEvidence(r, r.glRuntime.contextFlags, "GL_CONTEXT_FLAGS"), "Reset notification strategy" to runtimeQueryEvidence(r, r.glRuntime.resetNotificationStrategy, "GL_RESET_NOTIFICATION_STRATEGY", "GL_RESET_NOTIFICATION_STRATEGY_KHR", "GL_RESET_NOTIFICATION_STRATEGY_EXT"), "Reset strategy query" to (r.glRuntime.resetNotificationStrategyQuery ?: "Not applicable"), "Robust access" to runtimeQueryEvidence(r, r.glRuntime.robustAccess?.toString(), "GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "GL_CONTEXT_ROBUST_ACCESS_KHR", "GL_CONTEXT_ROBUST_ACCESS_EXT"), "Robust access query" to (r.glRuntime.robustAccessQuery ?: "Not applicable"), "Unavailable GL runtime attributes" to r.glRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }))
    val eglRows = rows(listOf(
        "EGL_VENDOR" to r.egl.vendor, "EGL_VERSION" to r.egl.version, "Initialized EGL version" to r.egl.initializedVersion, "EGL_CLIENT_APIS" to r.egl.clientApis,
        "Bound client API" to r.eglRuntime.boundApi, "Current config ID" to runtimeQueryEvidence(r, r.eglRuntime.configId?.toString(), "EGL_CONFIG_ID/context"), "Context client type" to runtimeQueryEvidence(r, r.eglRuntime.clientType, "EGL_CONTEXT_CLIENT_TYPE"),
        "Context client version" to runtimeQueryEvidence(r, r.eglRuntime.clientVersion?.toString(), "EGL_CONTEXT_CLIENT_VERSION"), "Context render buffer" to runtimeQueryEvidence(r, r.eglRuntime.renderBuffer, "EGL_RENDER_BUFFER/context"),
        "Current EGL bindings" to "context=${r.eglRuntime.currentContext}, display=${r.eglRuntime.currentDisplay}, draw=${r.eglRuntime.currentDrawSurface}, read=${r.eglRuntime.currentReadSurface}",
        "Pbuffer config ID" to runtimeQueryEvidence(r, r.eglRuntime.surfaceConfigId?.toString(), "EGL_CONFIG_ID/surface"),
        "Pbuffer size" to eglRuntimeSizeEvidence(r),
        "Pbuffer GL colorspace" to runtimeQueryEvidence(r, r.eglRuntime.surfaceGlColorspace, "EGL_GL_COLORSPACE"), "Pbuffer GL colorspace query" to (r.eglRuntime.surfaceGlColorspaceQuery ?: "Not applicable"), "Pbuffer VG alpha format" to runtimeQueryEvidence(r, r.eglRuntime.surfaceVgAlphaFormat, "EGL_VG_ALPHA_FORMAT"), "Pbuffer VG alpha query" to (r.eglRuntime.surfaceVgAlphaFormatQuery ?: "Not applicable"), "Pbuffer VG colorspace" to runtimeQueryEvidence(r, r.eglRuntime.surfaceVgColorspace, "EGL_VG_COLORSPACE"), "Pbuffer VG colorspace query" to (r.eglRuntime.surfaceVgColorspaceQuery ?: "Not applicable"),
        "Pbuffer horizontal resolution" to runtimeQueryEvidence(r, r.eglRuntime.surfaceHorizontalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_HORIZONTAL_RESOLUTION"), "Pbuffer vertical resolution" to runtimeQueryEvidence(r, r.eglRuntime.surfaceVerticalResolution?.let { eglScaledSurfaceEvidence(it) }, "EGL_VERTICAL_RESOLUTION"), "Pbuffer pixel aspect ratio" to runtimeQueryEvidence(r, r.eglRuntime.surfacePixelAspectRatio?.let { eglScaledSurfaceEvidence(it, aspect = true) }, "EGL_PIXEL_ASPECT_RATIO"), "Pbuffer largest pbuffer" to runtimeQueryEvidence(r, r.eglRuntime.surfaceLargestPbuffer?.let { eglBooleanEvidence(it) }, "EGL_LARGEST_PBUFFER"),
        "Pbuffer render buffer" to runtimeQueryEvidence(r, r.eglRuntime.surfaceRenderBuffer, "EGL_RENDER_BUFFER/surface"), "Pbuffer swap behavior" to runtimeQueryEvidence(r, r.eglRuntime.surfaceSwapBehavior, "EGL_SWAP_BEHAVIOR"),
        "Pbuffer texture" to "${runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureFormat, "EGL_TEXTURE_FORMAT")} / ${runtimeQueryEvidence(r, r.eglRuntime.surfaceTextureTarget, "EGL_TEXTURE_TARGET")}",
        "Pbuffer mipmap texture" to runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapTexture?.let { eglBooleanEvidence(it) }, "EGL_MIPMAP_TEXTURE"),
        "Pbuffer mipmap level" to runtimeQueryEvidence(r, r.eglRuntime.surfaceMipmapLevel?.toString(), "EGL_MIPMAP_LEVEL"),
        "Pbuffer multisample resolve" to runtimeQueryEvidence(r, r.eglRuntime.surfaceMultisampleResolve, "EGL_MULTISAMPLE_RESOLVE"),
        "Unavailable EGL runtime attributes" to r.eglRuntime.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" }
    ))
    val displayRows = rows(listOf(
        "Display" to d.name, "Current mode ID" to (d.modeId?.toString() ?: "Unavailable"), "Current mode resolution" to if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width} × ${d.height}" else "Unavailable",
        "Refresh rate" to (d.refreshRate?.let { "$it Hz" } ?: "Unavailable"), "Supported display modes" to if (d.supportedModes.isEmpty()) "Unavailable" else d.supportedModes.joinToString(" | "),
        "Wide color gamut" to when (d.wideColor) { true -> "Reported by Android display API"; false -> "Not supported by Android display API"; null -> "Unavailable on this Android API/display context" },
        "HDR capability status" to d.hdrCapabilityStatus,
        "HDR types" to hdrTypesText(d),
        "Desired max luminance" to d.desiredMaxLuminance?.let { "$it cd/m²" }, "Desired max average luminance" to d.desiredMaxAverageLuminance?.let { "$it cd/m²" }, "Desired min luminance" to d.desiredMinLuminance?.let { "$it cd/m²" }
    ))
    val eglCapabilityRows = r.eglCapabilities.joinToString("") { "<tr><td class=\"code\">${e(it.name)}</td><td>${badge(it.status)}</td><td>${e(it.value.ifBlank { "—" })}</td><td>${e(it.detail.ifBlank { "—" })}</td></tr>" }
    val limitRows = r.limits.joinToString("") { "<tr><td class=\"code\">${e(it.name)}</td><td>${e(it.value)}</td></tr>" }
    val internalFormatRows = r.internalFormats.joinToString("") { f ->
        val nv = f.nvSampleProperties.joinToString(" · ") { n -> "${n.samples}: MSAA ${n.multisamples}, scale ${n.supersampleScaleX}×${n.supersampleScaleY}, conformant=${n.conformant}" }.ifBlank { "—" }
        "<tr><td class=\"code\">${e(f.target)}</td><td class=\"code\">${e(f.internalFormat)}</td><td>${badge(f.status)}</td><td>${e(f.sampleCounts.joinToString(", ").ifBlank { "None reported" })}</td><td>${e(nv)}</td><td>${e(f.detail.ifBlank { "—" })}</td></tr>"
    }
    val precisionRows = r.precision.joinToString("") { "<tr><td class=\"code\">${e(it.shader)}</td><td class=\"code\">${e(it.type)}</td><td>${e("${it.rangeMin}..${it.rangeMax}")}</td><td>${e(it.precision)}</td></tr>" }
    val diagnosticRows = r.diagnostics.joinToString("") { "<tr><td class=\"code\">${e(it.name)}</td><td>${badge(it.status)}</td><td>${e(it.detail.ifBlank { "—" })}</td></tr>" }
    val configRows = r.eglConfigs.joinToString("") { c ->
        val rgba = listOf("red" to c.red, "green" to c.green, "blue" to c.blue, "alpha" to c.alpha).joinToString("/") { (k, v) -> eglConfigIntEvidence(c, k, v) }
        val pbuffer = listOf("maxPbufferWidth" to c.maxPbufferWidth, "maxPbufferHeight" to c.maxPbufferHeight, "maxPbufferPixels" to c.maxPbufferPixels).joinToString(" / ") { (k, v) -> eglConfigIntEvidence(c, k, v) }
        val transparentRgb = listOf("transparentRed" to c.transparentRed, "transparentGreen" to c.transparentGreen, "transparentBlue" to c.transparentBlue).joinToString("/") { (k, v) -> eglConfigIntEvidence(c, k, v) }
        val swap = "${eglConfigIntEvidence(c, "minSwapInterval", c.minSwapInterval)}..${eglConfigIntEvidence(c, "maxSwapInterval", c.maxSwapInterval)}"
        "<tr><td>${e(c.id)}</td><td>${e(rgba)}</td><td>${e(eglConfigIntEvidence(c, "bufferSize", c.bufferSize))}</td><td>${e(eglConfigIntEvidence(c, "luminanceSize", c.luminanceSize))}</td><td>${e(eglConfigIntEvidence(c, "alphaMaskSize", c.alphaMaskSize))}</td><td>${e(eglConfigIntEvidence(c, "depth", c.depth))}</td><td>${e(eglConfigIntEvidence(c, "stencil", c.stencil))}</td><td>${e(eglConfigIntEvidence(c, "sampleBuffers", c.sampleBuffers))}</td><td>${e(eglConfigIntEvidence(c, "samples", c.samples))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "surfaceType", c.surfaceType))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "renderableType", c.renderableType))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "conformant", c.conformant))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "configCaveat", c.configCaveat))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "colorBufferType", c.colorBufferType))}</td><td>${e(eglConfigBooleanEvidence(c, "bindToTextureRgb", c.bindToTextureRgb))}</td><td>${e(eglConfigBooleanEvidence(c, "bindToTextureRgba", c.bindToTextureRgba))}</td><td>${e(pbuffer)}</td><td>${e(eglConfigIntEvidence(c, "level", c.level))}</td><td>${e(eglConfigBooleanEvidence(c, "nativeRenderable", c.nativeRenderable))}</td><td>${e(eglConfigIntEvidence(c, "nativeVisualId", c.nativeVisualId))}</td><td>${e(eglConfigIntEvidence(c, "nativeVisualType", c.nativeVisualType))}</td><td class=\"code\">${e(eglConfigAttributeEvidence(c, "transparentType", c.transparentType))}</td><td>${e(transparentRgb)}</td><td>${e(swap)}</td><td>${e(eglConfigExtensionEvidence(r, c, c.recordableAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_recordable", "EGL_RECORDABLE_ANDROID"))}</td><td>${e(eglConfigExtensionEvidence(r, c, c.framebufferTargetAndroid?.let { eglBooleanLabel(it) }, "EGL_ANDROID_framebuffer_target", "EGL_FRAMEBUFFER_TARGET_ANDROID"))}</td><td class=\"code\">${e(eglConfigExtensionEvidence(r, c, c.colorComponentTypeExt, "EGL_EXT_pixel_format_float", "EGL_COLOR_COMPONENT_TYPE_EXT"))}</td><td>${e(c.unavailableAttributes.joinToString(" · ") { "${it.name}: ${it.error}" }.ifBlank { "None" })}</td></tr>"
    }
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
        metric("GPU", r.renderer.ifBlank { "Unavailable" }); metric("OpenGL® ES™", r.glVersion.ifBlank { "Unavailable" }); metric("EGL™", r.egl.initializedVersion.ifBlank { r.egl.version }); metric("Display", if ((d.width ?: 0) > 0 && (d.height ?: 0) > 0) "${d.width} × ${d.height} @ ${d.refreshRate?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "Unavailable"} Hz" else d.name); metric("HDR", hdrTypesText(d)); metric("Queries", "$availableQueries available / $unavailableQueries unavailable / $naQueries N/A / $unknownQueries unknown")
        append("</div></div>")
        fun section(title: String, header: String, body: String) { append("<div class=\"section\"><h2>${e(title)}</h2><table><thead><tr>$header</tr></thead><tbody>$body</tbody></table></div>") }
        fun htmlRows(values: List<Pair<String, String>>): String = values.joinToString("") { "<tr><td>${e(it.first)}</td><td>${it.second}</td></tr>" }
        section("Application", "<th>Property</th><th>Value</th>", htmlRows(applicationRows))
        section("Android / device", "<th>Property</th><th>Value</th>", deviceRows)
        section("OpenGL ES runtime", "<th>Property</th><th>Value</th>", glRows)
        section("EGL runtime", "<th>Property</th><th>Value</th>", eglRows)
        section("EGL capability queries (${r.eglCapabilities.size})", "<th>Capability</th><th>Status</th><th>Value</th><th>Detail</th>", eglCapabilityRows)
        section("Android display &amp; HDR", "<th>Property</th><th>Value</th>", displayRows)
        section("OpenGL ES limits (${r.limits.size})", "<th>Limit</th><th>Value</th>", limitRows)
        section("OpenGL ES extensions (${r.extensions.size})", "<th>Extension</th>", listRows(r.extensions))
        section("EGL display extensions (${r.egl.extensions.size})", "<th>Extension</th>", listRows(r.egl.extensions))
        section("EGL client extensions (${r.egl.clientExtensions.size})", "<th>Extension</th>", listRows(r.egl.clientExtensions))
        section("Compressed texture formats (${r.compressedFormats.size})", "<th>Format</th>", listRows(r.compressedFormats))
        section("Internal-format sample support (${r.internalFormats.size})", "<th>Target</th><th>Internal format</th><th>Status</th><th>Sample counts</th><th>NV sample properties</th><th>Detail</th>", internalFormatRows)
        section("Shader binary formats (${r.shaderBinaryFormats.size})", "<th>Format</th>", listRows(r.shaderBinaryFormats))
        section("Program binary formats (${r.programBinaryFormats.size})", "<th>Format</th>", listRows(r.programBinaryFormats))
        section("Shader precision (${r.precision.size})", "<th>Shader</th><th>Type</th><th>Range</th><th>Precision</th>", precisionRows)
        section("Query diagnostics (${r.diagnostics.size})", "<th>Query</th><th>Status</th><th>Detail</th>", diagnosticRows)
        section("EGL configurations (${r.eglConfigs.size})", "<th>ID</th><th>RGBA</th><th>Buffer</th><th>Luminance</th><th>Alpha mask</th><th>Depth</th><th>Stencil</th><th>Sample buffers</th><th>Samples</th><th>Surface</th><th>Renderable</th><th>Conformant</th><th>Caveat</th><th>Color buffer</th><th>Bind RGB</th><th>Bind RGBA</th><th>Max pbuffer W×H / pixels</th><th>Level</th><th>Native renderable</th><th>Visual ID</th><th>Visual type</th><th>Transparency</th><th>Transparent RGB</th><th>Swap interval</th><th>Recordable</th><th>Framebuffer target</th><th>Color component type</th><th>Unavailable attributes</th>", configRows)
        append("</div></body></html>")
    }
}

