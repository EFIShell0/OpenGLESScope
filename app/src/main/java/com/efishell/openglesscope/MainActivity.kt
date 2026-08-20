package com.efishell.openglesscope

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

private const val DATABASE_API = "https://openglesscope-database-api.openglesscope.workers.dev"
private const val DATABASE_WEB = "https://efishell0.github.io/OpenGLESScope_database/"
private const val RELEASE_API = "https://api.github.com/repos/EFIShell0/OpenGLESScope/releases/latest"
private const val REPOSITORY_WEB = "https://github.com/EFIShell0/OpenGLESScope"
private val Blue = ComposeColor(0xFF2979FF)
private val BlueSoft = ComposeColor(0xFF72A7FF)
private val SurfaceDark = ComposeColor(0xFF101114)
private val SurfaceRaised = ComposeColor(0xFF171A20)
private val Muted = ComposeColor(0xFFA4A7AF)

private data class LimitEntry(val name: String, val value: String)
private data class PrecisionEntry(val shader: String, val type: String, val rangeMin: Int, val rangeMax: Int, val precision: Int)
private data class EglConfigEntry(val id: Int, val rgba: String, val depth: Int, val stencil: Int, val samples: Int, val surfaceType: String, val renderableType: String)
private data class EglInfo(val vendor: String, val version: String, val initializedVersion: String, val clientApis: String, val extensions: List<String>)
private data class GlReport(
    val available: Boolean,
    val reason: String,
    val renderer: String,
    val vendor: String,
    val glVersion: String,
    val glslVersion: String,
    val egl: EglInfo,
    val extensions: List<String>,
    val limits: List<LimitEntry>,
    val compressedFormats: List<String>,
    val precision: List<PrecisionEntry>,
    val eglConfigs: List<EglConfigEntry>
)
private data class DisplayInfo(val name: String, val refreshRate: Float, val hdrTypes: List<String>, val desiredMaxLuminance: Float?, val desiredMaxAverageLuminance: Float?, val desiredMinLuminance: Float?, val wideColor: Boolean)
private enum class Page(val title: String) {
    Overview("Overview"), OpenGLES("OpenGL ES"), Display("Display & HDR"), EGL("EGL"), Features("Features"), Limits("Limits"), Formats("Formats"), Extensions("Extensions"), Precision("Precision"), Configs("EGL Configs"), Settings("Settings"), Info("Info")
}

class MainActivity : ComponentActivity() {
    external fun nativeCollect(): String
    private var pendingExportText: String? = null
    private var pendingExportHtml: String? = null
    private val txtLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val text = pendingExportText
        if (uri != null && text != null) contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        pendingExportText = null
    }
    private val htmlLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
        val text = pendingExportHtml
        if (uri != null && text != null) contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        pendingExportHtml = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("openglesscope")
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        setContent { OpenGLESScopeApp(this) }
    }

    fun exportTxt(report: GlReport, display: DisplayInfo) {
        pendingExportText = reportText(this, report, display)
        txtLauncher.launch("OpenGLESScope-${Build.MODEL}-0.1.0.txt")
    }

    fun exportHtml(report: GlReport, display: DisplayInfo) {
        pendingExportHtml = reportHtml(this, report, display)
        htmlLauncher.launch("OpenGLESScope-${Build.MODEL}-0.1.0.html")
    }
}

private fun parseReport(raw: String): GlReport {
    return try {
        val o = JSONObject(raw)
        val available = o.optString("status") == "available"
        val eglObj = o.optJSONObject("egl") ?: JSONObject()
        val limits = o.optJSONArray("limits").toObjects().map { x -> LimitEntry(x.optString("name", "Unknown"), x.optString("value", "Unknown")) }
        val precision = o.optJSONArray("precision").toObjects().map { x -> PrecisionEntry(x.optString("shader"), x.optString("type"), x.optInt("rangeMin"), x.optInt("rangeMax"), x.optInt("precision")) }
        val configs = o.optJSONArray("eglConfigs").toObjects().map { x -> EglConfigEntry(x.optInt("id"), x.optString("rgba"), x.optInt("depth"), x.optInt("stencil"), x.optInt("samples"), x.optString("surfaceType"), x.optString("renderableType")) }
        GlReport(
            available,
            o.optString("reason"),
            o.optString("renderer", "Unknown"),
            o.optString("vendor", "Unknown"),
            o.optString("glVersion", "Unknown"),
            o.optString("glslVersion", "Unknown"),
            EglInfo(eglObj.optString("vendor", "Unknown"), eglObj.optString("version", "Unknown"), eglObj.optString("initializedVersion", "Unknown"), eglObj.optString("clientApis", "Unknown"), eglObj.optJSONArray("extensions").toStrings()),
            o.optJSONArray("extensions").toStrings(),
            limits,
            o.optJSONArray("compressedFormats").toStrings(),
            precision,
            configs
        )
    } catch (e: Exception) {
        GlReport(false, e.message ?: "OpenGL ES report parsing failed", "Unknown", "Unknown", "Unknown", "Unknown", EglInfo("Unknown", "Unknown", "Unknown", "Unknown", emptyList()), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

private fun JSONArray?.toStrings(): List<String> = if (this == null) emptyList() else List(length()) { optString(it) }
private fun JSONArray?.toObjects(): List<JSONObject> = if (this == null) emptyList() else List(length()) { optJSONObject(it) ?: JSONObject() }

private fun displayInfo(activity: Activity): DisplayInfo {
    val d = if (Build.VERSION.SDK_INT >= 30) activity.display else @Suppress("DEPRECATION") activity.windowManager.defaultDisplay
    if (d == null) return DisplayInfo("Unknown", 0f, emptyList(), null, null, null, false)
    val hdr = if (Build.VERSION.SDK_INT >= 24) d.hdrCapabilities else null
    val types = hdr?.supportedHdrTypes?.map { hdrName(it) } ?: emptyList()
    val wide = if (Build.VERSION.SDK_INT >= 26) d.isWideColorGamut else false
    return DisplayInfo(d.name, d.refreshRate, types, hdr?.desiredMaxLuminance?.takeIf { it > 0f }, hdr?.desiredMaxAverageLuminance?.takeIf { it > 0f }, hdr?.desiredMinLuminance?.takeIf { it >= 0f }, wide)
}

private fun hdrName(v: Int): String = when (v) {
    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
    Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
    Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
    else -> "Android HDR type $v"
}

@Composable
private fun OpenGLESScopeApp(activity: MainActivity) {
    var report by remember { mutableStateOf<GlReport?>(null) }
    var collecting by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf(Page.Overview) }
    var updateState by remember { mutableStateOf<String?>(null) }
    val display = remember { displayInfo(activity) }
    LaunchedEffect(Unit) {
        report = withContext(Dispatchers.Default) { parseReport(activity.nativeCollect()) }
        collecting = false
        updateState = checkUpdate(activity)
    }
    MaterialTheme(darkColorScheme(background = ComposeColor.Black, surface = SurfaceDark, surfaceVariant = SurfaceRaised, primary = Blue, secondary = BlueSoft, tertiary = BlueSoft, onBackground = ComposeColor(0xFFF5F7FA), onSurface = ComposeColor(0xFFF5F7FA))) {
        Scaffold(
            containerColor = ComposeColor.Black,
            bottomBar = {
                NavigationBar(containerColor = ComposeColor(0xFF090A0D)) {
                    listOf(Page.Overview, Page.OpenGLES, Page.Extensions, Page.Limits, Page.Info).forEach { p ->
                        NavigationBarItem(selected = page == p, onClick = { page = p }, icon = { Text(navGlyph(p), fontWeight = FontWeight.Bold) }, label = { Text(p.title, maxLines = 1) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ComposeColor.White, selectedTextColor = ComposeColor.White, indicatorColor = Blue, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    }
                }
            }
        ) { pad ->
            Column(Modifier.fillMaxSize().padding(pad)) {
                Header(page)
                updateState?.let { UpdateBanner(it) }
                val r = report
                if (collecting || r == null) CenterState("Collecting OpenGL ES capabilities…")
                else if (!r.available) CenterState("OpenGL ES unavailable\n${r.reason}")
                else PageContent(activity, page, r, display, onNavigate = { page = it })
            }
        }
    }
}

private fun navGlyph(p: Page): String = when (p) { Page.Overview -> "⌂"; Page.OpenGLES -> "GL"; Page.Extensions -> "＋"; Page.Limits -> "↕"; Page.Info -> "i"; else -> "•" }

@Composable
private fun Header(page: Page) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Image(painterResource(R.drawable.openglesscope_logo_horizontal), "OpenGLESScope", Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))
        Text(page.title, color = Muted, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun UpdateBanner(text: String) {
    Surface(color = ComposeColor(0xFF102A52), modifier = Modifier.fillMaxWidth()) { Text(text, modifier = Modifier.padding(12.dp), color = ComposeColor(0xFFDCEAFF)) }
}

@Composable
private fun CenterState(text: String) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, color = Muted) } }

@Composable
private fun PageContent(activity: MainActivity, page: Page, report: GlReport, display: DisplayInfo, onNavigate: (Page) -> Unit) {
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
        Page.Settings -> SettingsPage(activity, report, display)
        Page.Info -> InfoPage(activity, report, display)
    }
}

@Composable
private fun OverviewPage(report: GlReport, display: DisplayInfo, nav: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Hero(report) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Metric("OpenGL ES", shortGlVersion(report.glVersion), Modifier.weight(1f))
                Metric("Extensions", report.extensions.size.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Metric("EGL", report.egl.initializedVersion, Modifier.weight(1f))
                Metric("HDR types", display.hdrTypes.size.toString(), Modifier.weight(1f))
            }
        }
        item { Text("Inspection areas", fontWeight = FontWeight.SemiBold) }
        items(listOf(Page.OpenGLES, Page.Display, Page.EGL, Page.Features, Page.Limits, Page.Formats, Page.Extensions, Page.Precision, Page.Configs, Page.Settings)) { p -> QuickCard(p, nav) }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun Hero(r: GlReport) {
    Card(colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF111C30)), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.renderer, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(r.vendor, color = BlueSoft)
            Text(r.glVersion, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(r.glslVersion, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceRaised), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(15.dp)) { Text(label, color = Muted, style = MaterialTheme.typography.labelMedium); Text(value, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis) } }
}

@Composable
private fun QuickCard(page: Page, nav: (Page) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { nav(page) }, colors = CardDefaults.cardColors(containerColor = SurfaceRaised), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(page.title, fontWeight = FontWeight.Medium); Spacer(Modifier.weight(1f)); Text("›", color = BlueSoft) } }
}

@Composable
private fun OpenGLESPage(r: GlReport) = SimpleList("OpenGL ES runtime", listOf("Renderer" to r.renderer, "Vendor" to r.vendor, "GL_VERSION" to r.glVersion, "GL_SHADING_LANGUAGE_VERSION" to r.glslVersion, "Runtime extensions" to r.extensions.size.toString()))

@Composable
private fun DisplayPage(d: DisplayInfo) = SimpleList("Android Display", buildList {
    add("Display" to d.name); add("Refresh rate" to "${"%.2f".format(d.refreshRate)} Hz"); add("Wide color gamut" to if (d.wideColor) "Supported by Android display API" else "Not reported"); add("HDR types" to if (d.hdrTypes.isEmpty()) "Unavailable" else d.hdrTypes.joinToString()); add("Desired max luminance" to d.desiredMaxLuminance?.let { "$it cd/m²" }.orEmpty().ifBlank { "Unavailable" }); add("Desired max average luminance" to d.desiredMaxAverageLuminance?.let { "$it cd/m²" }.orEmpty().ifBlank { "Unavailable" }); add("Desired min luminance" to d.desiredMinLuminance?.let { "$it cd/m²" }.orEmpty().ifBlank { "Unavailable" })
})

@Composable
private fun EglPage(r: GlReport) = SimpleList("EGL runtime", listOf("EGL_VENDOR" to r.egl.vendor, "EGL_VERSION" to r.egl.version, "Initialized EGL version" to r.egl.initializedVersion, "EGL_CLIENT_APIS" to r.egl.clientApis, "EGL extensions" to r.egl.extensions.size.toString()))

@Composable
private fun FeaturesPage(r: GlReport) {
    val v = parseVersion(r.glVersion)
    val rows = listOf("OpenGL ES 2.0 core" to (v >= 200), "OpenGL ES 3.0 core" to (v >= 300), "OpenGL ES 3.1 core" to (v >= 310), "OpenGL ES 3.2 core" to (v >= 320), "Compute shaders" to (v >= 310), "Tessellation shaders" to (v >= 320), "Geometry shaders" to (v >= 320), "ASTC LDR extension" to r.extensions.any { it == "GL_KHR_texture_compression_astc_ldr" }, "Debug output" to r.extensions.any { it == "GL_KHR_debug" || it == "GL_EXT_debug_marker" })
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { SectionTitle("Core and extension evidence") }; items(rows) { (n, s) -> StatusRow(n, s) }; item { Text("Core states are derived only from the runtime GL_VERSION. Extension states are exact runtime extension-name membership.", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 10.dp)) } }
}

@Composable
private fun LimitsPage(r: GlReport) = SearchRows("OpenGL ES limits", r.limits.map { it.name to it.value })

@Composable
private fun FormatsPage(r: GlReport) = SearchRows("Compressed texture formats", r.compressedFormats.map { it to "Reported by GL_COMPRESSED_TEXTURE_FORMATS" })

@Composable
private fun ExtensionsPage(r: GlReport) {
    var egl by remember { mutableStateOf(false) }
    val list = if (egl) r.egl.extensions else r.extensions
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("OpenGL ES", !egl) { egl = false }
            Pill("EGL", egl) { egl = true }
        }
        SearchRows(if (egl) "EGL extensions" else "OpenGL ES extensions", list.map { it to "Supported" })
    }
}

@Composable
private fun PrecisionPage(r: GlReport) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle("Shader numeric precision") }
        items(r.precision) { p -> KeyValueCard("${p.shader} · ${p.type}", "range ${p.rangeMin}…${p.rangeMax} · precision ${p.precision}") }
    }
}

@Composable
private fun ConfigsPage(r: GlReport) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle("EGL configurations (${r.eglConfigs.size})") }
        items(r.eglConfigs) { c -> KeyValueCard("EGL_CONFIG_ID ${c.id}", "RGBA ${c.rgba} · depth ${c.depth} · stencil ${c.stencil} · samples ${c.samples}\nsurface ${c.surfaceType} · renderable ${c.renderableType}") }
    }
}

@Composable
private fun SettingsPage(activity: MainActivity, report: GlReport, display: DisplayInfo) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle("Export") }
        item { ActionButton("Export complete TXT report") { activity.exportTxt(report, display) } }
        item { ActionButton("Export complete HTML report") { activity.exportHtml(report, display) } }
        item { SectionTitle("Collection policy") }
        item { KeyValueCard("Driver", "System OpenGL ES/EGL implementation") }
        item { Text("Capability collection uses the active Android system EGL/OpenGL ES implementation only; third-party graphics driver bundle loading is not part of this application.", color = Muted) }
    }
}

@Composable
private fun InfoPage(activity: MainActivity, report: GlReport, display: DisplayInfo) {
    var submitState by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle("OpenGLESScope") }
        item { KeyValueCard("Version", "0.1.0 (100)") }
        item { KeyValueCard("Package", "com.efishell.openglesscope") }
        item { ActionButton("Check for updates") { scope.launch { submitState = checkUpdate(activity) } } }
        item { ActionButton("Open GitHub repository") { open(activity, REPOSITORY_WEB) } }
        item { SectionTitle("OpenGLESScope Database") }
        item { Text("Submission is explicit and user-initiated. The complete technical report is sent as one dataset. IMEI, Android ID, serial, MAC addresses, account data and authentication tokens are not collected.", color = Muted) }
        item { ActionButton("Submit complete report") { scope.launch { submitState = submitReport(activity, report, display) } } }
        item { ActionButton("Open OpenGLESScope Database") { open(activity, DATABASE_WEB) } }
        submitState?.let { item { KeyValueCard("Status", it) } }
        item { SectionTitle("Network") }
        item { KeyValueCard("Database endpoint", DATABASE_API) }
        item { KeyValueCard("Address-family policy", "IPv6 preferred on dual-stack; IPv4 fallback retained") }
    }
}

@Composable
private fun SectionTitle(s: String) { Text(s, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) }

@Composable
private fun SimpleList(title: String, rows: List<Pair<String, String>>) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { SectionTitle(title) }; items(rows) { (a,b) -> KeyValueCard(a,b) } } }

@Composable
private fun SearchRows(title: String, rows: List<Pair<String, String>>) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(rows, query) { if (query.isBlank()) rows else rows.filter { it.first.contains(query, true) || it.second.contains(query, true) } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle("$title (${rows.size})") }
        item { OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)) }
        items(filtered) { (a,b) -> KeyValueCard(a,b) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun KeyValueCard(name: String, value: String) { Card(colors = CardDefaults.cardColors(containerColor = SurfaceRaised), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(14.dp)) { Text(name, fontWeight = FontWeight.Medium); Text(value.ifBlank { "Unavailable" }, color = Muted, style = MaterialTheme.typography.bodySmall, fontFamily = if (name.startsWith("GL_") || name.startsWith("EGL_")) FontFamily.Monospace else FontFamily.Default) } } }

@Composable
private fun StatusRow(name: String, supported: Boolean) { Card(colors = CardDefaults.cardColors(containerColor = SurfaceRaised), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(name, Modifier.weight(1f)); Surface(color = if (supported) ComposeColor(0xFF123E2A) else ComposeColor(0xFF462027), shape = RoundedCornerShape(99.dp)) { Text(if (supported) "SUPPORTED" else "UNSUPPORTED", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = if (supported) ComposeColor(0xFF7EE0A9) else ComposeColor(0xFFFF9CA7), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } } } }

@Composable
private fun Pill(text: String, selected: Boolean, click: () -> Unit) { Surface(color = if (selected) Blue else SurfaceRaised, shape = RoundedCornerShape(99.dp), modifier = Modifier.clickable { click() }) { Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (selected) ComposeColor.White else Muted) } }

@Composable
private fun ActionButton(text: String, action: () -> Unit) { Button(onClick = action, colors = ButtonDefaults.buttonColors(containerColor = Blue), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text(text) } }

private fun parseVersion(s: String): Int { val m = Regex("OpenGL ES (\\d+)\\.(\\d+)").find(s) ?: return 0; return (m.groupValues[1].toIntOrNull() ?: 0) * 100 + (m.groupValues[2].toIntOrNull() ?: 0) * 10 }
private fun shortGlVersion(s: String): String = Regex("OpenGL ES \\d+\\.\\d+").find(s)?.value ?: s.take(24)
private fun open(c: Context, url: String) { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

private fun client(): OkHttpClient = OkHttpClient.Builder().dns(Dns { host -> InetAddress.getAllByName(host).sortedBy { if (it is Inet6Address) 0 else 1 } }).fastFallback(true).connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()

private suspend fun checkUpdate(context: Context): String = withContext(Dispatchers.IO) {
    try {
        val req = Request.Builder().url(RELEASE_API).header("Accept", "application/vnd.github+json").build()
        client().newCall(req).execute().use { res ->
            if (!res.isSuccessful) return@withContext "Update check unavailable · HTTP ${res.code}"
            val json = JSONObject(res.body.string())
            val tag = json.optString("tag_name").removePrefix("v")
            if (tag.isBlank() || compareVersions(tag, "0.1.0") <= 0) "OpenGLESScope 0.1.0 is up to date" else "OpenGLESScope $tag is available on GitHub"
        }
    } catch (e: Exception) { "Update check unavailable · ${e.message ?: "network error"}" }
}

private fun compareVersions(a: String, b: String): Int { val aa=a.split('.').map{it.toIntOrNull()?:0}; val bb=b.split('.').map{it.toIntOrNull()?:0}; for(i in 0 until maxOf(aa.size,bb.size)){val x=aa.getOrElse(i){0}; val y=bb.getOrElse(i){0}; if(x!=y)return x.compareTo(y)}; return 0 }

private suspend fun submitReport(context: Context, report: GlReport, display: DisplayInfo): String = withContext(Dispatchers.IO) {
    try {
        val payload = submissionJson(context, report, display).toString()
        if (payload.toByteArray().size > 2 * 1024 * 1024) return@withContext "Report exceeds the 2 MiB transport limit; no data was truncated"
        val req = Request.Builder().url("$DATABASE_API/v1/reports").post(payload.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        dualStackSummary(Uri.parse(DATABASE_API).host.orEmpty())
        client().newCall(req).execute().use { res ->
            val body = res.body.string()
            if (res.isSuccessful) "Report submitted successfully · ${JSONObject(body).optString("id").take(12)}" else "Submission failed · HTTP ${res.code} · ${runCatching { JSONObject(body).optString("error") }.getOrDefault("server error")}"
        }
    } catch (e: Exception) { "Submission failed · ${e.message ?: "network error"}" }
}

private fun dualStackSummary(host: String): String { val all = runCatching { InetAddress.getAllByName(host).toList() }.getOrDefault(emptyList()); val v6=all.any{it is Inet6Address}; val v4=all.any{it is Inet4Address}; return when { v6&&v4->"dual-stack"; v6->"IPv6"; v4->"IPv4"; else->"unresolved" } }

private fun submissionJson(context: Context, r: GlReport, d: DisplayInfo): JSONObject {
    val reportText = reportText(context, r, d)
    return JSONObject().apply {
        put("schemaVersion", 2)
        put("application", JSONObject().put("name", "OpenGLESScope").put("packageName", "com.efishell.openglesscope").put("version", "0.1.0").put("versionCode", 100))
        put("device", JSONObject().put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("product", Build.PRODUCT).put("androidRelease", Build.VERSION.RELEASE).put("sdk", Build.VERSION.SDK_INT))
        put("gpu", JSONObject().put("name", r.renderer).put("vendor", r.vendor))
        put("driver", JSONObject().put("mode", "System OpenGL ES/EGL").put("version", r.glVersion))
        put("opengles", JSONObject().put("version", r.glVersion).put("glslVersion", r.glslVersion).put("extensionCount", r.extensions.size))
        put("egl", JSONObject().put("vendor", r.egl.vendor).put("version", r.egl.version).put("initializedVersion", r.egl.initializedVersion).put("extensionCount", r.egl.extensions.size))
        put("display", JSONObject().put("name", d.name).put("refreshRate", d.refreshRate).put("wideColor", d.wideColor).put("hdrTypes", JSONArray(d.hdrTypes)))
        put("collection", JSONObject().put("status", if (r.available) "available" else "unavailable").put("complete", r.available))
        put("technicalReport", JSONObject().put("schemaVersion", 1).put("limits", JSONArray(r.limits.map { JSONObject().put("name", it.name).put("value", it.value) })).put("extensions", JSONArray(r.extensions)).put("eglExtensions", JSONArray(r.egl.extensions)).put("compressedFormats", JSONArray(r.compressedFormats)).put("precision", JSONArray(r.precision.map { JSONObject().put("shader",it.shader).put("type",it.type).put("rangeMin",it.rangeMin).put("rangeMax",it.rangeMax).put("precision",it.precision) })).put("eglConfigs", JSONArray(r.eglConfigs.map { JSONObject().put("id",it.id).put("rgba",it.rgba).put("depth",it.depth).put("stencil",it.stencil).put("samples",it.samples).put("surfaceType",it.surfaceType).put("renderableType",it.renderableType) })))
        put("reportText", reportText)
    }
}

private fun reportText(context: Context, r: GlReport, d: DisplayInfo): String = buildString {
    appendLine("OpenGLESScope 0.1.0")
    appendLine("Package: com.efishell.openglesscope")
    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
    appendLine("Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
    appendLine("Renderer: ${r.renderer}")
    appendLine("Vendor: ${r.vendor}")
    appendLine("OpenGL ES: ${r.glVersion}")
    appendLine("GLSL ES: ${r.glslVersion}")
    appendLine("EGL vendor: ${r.egl.vendor}")
    appendLine("EGL version: ${r.egl.version}")
    appendLine("EGL initialized: ${r.egl.initializedVersion}")
    appendLine("Display: ${d.name} @ ${d.refreshRate} Hz")
    appendLine("Wide color: ${d.wideColor}")
    appendLine("HDR types: ${if(d.hdrTypes.isEmpty()) "Unavailable" else d.hdrTypes.joinToString()}")
    appendLine()
    appendLine("OPENGL ES LIMITS")
    r.limits.forEach { appendLine("${it.name}: ${it.value}") }
    appendLine()
    appendLine("OPENGL ES EXTENSIONS")
    r.extensions.forEach { appendLine(it) }
    appendLine()
    appendLine("EGL EXTENSIONS")
    r.egl.extensions.forEach { appendLine(it) }
    appendLine()
    appendLine("COMPRESSED TEXTURE FORMATS")
    r.compressedFormats.forEach { appendLine(it) }
    appendLine()
    appendLine("SHADER PRECISION")
    r.precision.forEach { appendLine("${it.shader} | ${it.type} | ${it.rangeMin}..${it.rangeMax} | ${it.precision}") }
    appendLine()
    appendLine("EGL CONFIGS")
    r.eglConfigs.forEach { appendLine("${it.id} | RGBA ${it.rgba} | depth ${it.depth} | stencil ${it.stencil} | samples ${it.samples} | surface ${it.surfaceType} | renderable ${it.renderableType}") }
    appendLine()
    appendLine("Database: $DATABASE_WEB")
    appendLine("Repository: $REPOSITORY_WEB")
}

private fun reportHtml(context: Context, r: GlReport, d: DisplayInfo): String {
    fun e(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
    val limits=r.limits.joinToString(""){"<tr><td>${e(it.name)}</td><td>${e(it.value)}</td></tr>"}
    val exts=r.extensions.joinToString(""){"<tr><td>${e(it)}</td></tr>"}
    return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>OpenGLESScope Report</title><style>body{font-family:system-ui;background:#080a0e;color:#f5f7fa;margin:0}.wrap{max-width:1200px;margin:auto;padding:28px}.hero,.section{background:#111722;border:1px solid #263246;border-radius:22px;padding:20px;margin-bottom:16px}h1{color:#72a7ff}table{width:100%;border-collapse:collapse}td{padding:9px;border-bottom:1px solid #273142}.muted{color:#a4a7af}</style></head><body><div class=\"wrap\"><div class=\"hero\"><h1>OpenGLESScope 0.1.0</h1><div>${e(r.renderer)}</div><div class=\"muted\">${e(r.vendor)} · ${e(r.glVersion)} · ${e(r.glslVersion)}</div></div><div class=\"section\"><h2>Display & HDR</h2><p>${e(d.name)} @ ${d.refreshRate} Hz</p><p>${e(if(d.hdrTypes.isEmpty()) "HDR unavailable" else d.hdrTypes.joinToString())}</p></div><div class=\"section\"><h2>Limits</h2><table>$limits</table></div><div class=\"section\"><h2>Extensions</h2><table>$exts</table></div></div></body></html>"
}
