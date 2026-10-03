package com.efishell.openglesscope

import android.content.Context
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

internal data class RegistryCatalogEntry(
    val name: String,
    val kind: String,
    val api: String,
    val value: String,
    val alias: String,
    val group: String,
    val signature: String,
    val definition: String,
    val owners: List<String>,
    val searchText: String
)

internal object RegistryCatalog {
    private const val MAX_CATALOG_BYTES = 2 * 1024 * 1024
    private const val MAX_CATALOG_ENTRIES = 6_000
    @Volatile private var cached: List<RegistryCatalogEntry>? = null

    private fun readBoundedCatalog(context: Context): String {
        val output = ByteArrayOutputStream(1024 * 1024)
        context.assets.open("registry_catalog.json.gz").use { raw ->
            GZIPInputStream(raw).use { input ->
                val buffer = ByteArray(16 * 1024)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    total += read
                    require(total <= MAX_CATALOG_BYTES) { "Registry catalog exceeds the bounded ${MAX_CATALOG_BYTES / (1024 * 1024)} MiB limit" }
                    output.write(buffer, 0, read)
                }
            }
        }
        return output.toString(Charsets.UTF_8.name())
    }

    fun load(context: Context): List<RegistryCatalogEntry> {
        cached?.let { return it }
        return synchronized(this) {
            cached?.let { return@synchronized it }
            val root = JSONObject(readBoundedCatalog(context))
            require(root.optString("schema") == "OpenGLESScopeRegistryCatalog1") { "Unsupported registry catalog schema" }
            val array = root.getJSONArray("entries")
            require(array.length() <= MAX_CATALOG_ENTRIES) { "Registry catalog entry count exceeds the bounded release limit" }
            val out = ArrayList<RegistryCatalogEntry>(array.length())
            val seen = HashSet<String>(array.length())
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                val name = o.optString("name").trim()
                val kind = o.optString("kind").trim()
                val api = o.optString("api").trim()
                if (name.isEmpty() || kind.isEmpty() || api.isEmpty()) continue
                if (name.length > 512 || kind.length > 64 || api.length > 64) continue
                if (!seen.add("$api|$kind|$name")) continue
                val ownersArray = o.optJSONArray("owners")
                val owners = ArrayList<String>(minOf(ownersArray?.length() ?: 0, 128))
                if (ownersArray != null) {
                    for (j in 0 until minOf(ownersArray.length(), 128)) {
                        owners += ownersArray.optString(j).take(512)
                    }
                }
                val value = o.optString("value").take(4096)
                val alias = o.optString("alias").take(1024)
                val group = o.optString("group").take(2048)
                val signature = o.optString("signature").take(8192)
                val definition = o.optString("definition").take(8192)
                val searchText = buildString {
                    append(name).append('\n').append(kind).append('\n').append(api).append('\n')
                    append(value).append('\n').append(alias).append('\n').append(group).append('\n')
                    append(signature).append('\n').append(definition).append('\n')
                    owners.forEach { append(it).append('\n') }
                }.lowercase(java.util.Locale.ROOT)
                out += RegistryCatalogEntry(
                    name = name,
                    kind = kind,
                    api = api,
                    value = value,
                    alias = alias,
                    group = group,
                    signature = signature,
                    definition = definition,
                    owners = owners,
                    searchText = searchText
                )
            }
            require(out.isNotEmpty()) { "Registry catalog contains no valid entries" }
            out.toList().also { cached = it }
        }
    }
}
