package slop.miho.chumpy.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import slop.miho.chumpy.sub.RouteRace
import slop.miho.chumpy.xray.XrayConfigBuilder
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

data class GeoAssetInfo(
    val fileName: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val isCustom: Boolean,
    val categoryCount: Int,
)

enum class GeoAssetPreset(val label: String, val geoipUrl: String, val geositeUrl: String) {
    XTLS_OFFICIAL(
        label = "XTLS / Xray-rules-dat (Official)",
        geoipUrl = "https://github.com/XTLS/Xray-rules-dat/releases/latest/download/geoip.dat",
        geositeUrl = "https://github.com/XTLS/Xray-rules-dat/releases/latest/download/geosite.dat",
    ),
    LOYALSOLDIER_FULL(
        label = "Loyalsoldier / v2ray-rules-dat (Full)",
        geoipUrl = "https://github.com/Loyalsoldier/v2ray-rules-dat/releases/latest/download/geoip.dat",
        geositeUrl = "https://github.com/Loyalsoldier/v2ray-rules-dat/releases/latest/download/geosite.dat",
    ),
    IRAN_RUNET(
        label = "chocolate4u / Iran-v2ray-rules",
        geoipUrl = "https://github.com/chocolate4u/Iran-v2ray-rules/releases/latest/download/geoip.dat",
        geositeUrl = "https://github.com/chocolate4u/Iran-v2ray-rules/releases/latest/download/geosite.dat",
    ),
}

object GeodataManager {
    private const val ASSET_DIR = "xray_assets"
    private const val CUSTOM_MARKER_PREFIX = ".custom_"
    val MANAGED_FILES = listOf("geoip.dat", "geosite.dat")

    fun customMarkerFile(dir: File, fileName: String): File = File(dir, "$CUSTOM_MARKER_PREFIX$fileName")

    suspend fun inspectAssets(context: Context): List<GeoAssetInfo> = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, ASSET_DIR).apply { mkdirs() }
        MANAGED_FILES.map { name ->
            val file = File(dir, name)
            val marker = customMarkerFile(dir, name)
            val isCustom = marker.isFile && file.isFile && file.length() > 0L
            val size = if (file.isFile && file.length() > 0L) {
                file.length()
            } else {
                runCatching { context.assets.openFd(name).length }.getOrElse {
                    runCatching { context.assets.open(name).use { it.readBytes().size.toLong() } }.getOrDefault(0L)
                }
            }
            val cats = runCatching {
                if (file.isFile && file.length() > 0L) {
                    file.inputStream().use { GeodataFiles.categories(it).size }
                } else {
                    context.assets.open(name).use { GeodataFiles.categories(it).size }
                }
            }.getOrDefault(0)
            GeoAssetInfo(
                fileName = name,
                sizeBytes = size,
                lastModifiedMillis = if (file.isFile) file.lastModified() else 0L,
                isCustom = isCustom,
                categoryCount = cats,
            )
        }
    }

    suspend fun downloadAsset(
        context: Context,
        fileName: String,
        url: String,
        tunnelUp: Boolean,
        socksPort: Int = XrayConfigBuilder.LOCAL_SOCKS_PORT,
        useMirrors: Boolean = true,
    ): Result<GeoAssetInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = url.trim()
            require(trimmed.startsWith("https://", ignoreCase = true) || trimmed.startsWith("http://", ignoreCase = true)) {
                "Invalid geodata URL"
            }
            val dir = File(context.filesDir, ASSET_DIR).apply { mkdirs() }
            val candidates = if (useMirrors) RouteRace.mirrorUrlsFor(trimmed) else listOf(trimmed)
            val baseClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)
                .apply {
                    if (tunnelUp) {
                        proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socksPort)))
                    }
                }
                .build()

            val downloadedTemp = File.createTempFile("geo-dl-", ".part", dir)
            try {
                val winner = RouteRace.first(candidates) { routeUrl ->
                    val part = File.createTempFile("geo-race-", ".part", dir)
                    try {
                        val req = Request.Builder()
                            .url(routeUrl)
                            .header("User-Agent", "HollowVPN/1.0")
                            .build()
                        baseClient.newCall(req).execute().use { resp ->
                            if (!resp.isSuccessful) return@first null
                            val rawStream = resp.body?.byteStream()?.buffered() ?: return@first null
                            rawStream.mark(2)
                            val isGzip = rawStream.read() == 0x1f && rawStream.read() == 0x8b
                            rawStream.reset()
                            val src = if (isGzip) GZIPInputStream(rawStream) else rawStream
                            src.use { input ->
                                part.outputStream().buffered().use { out ->
                                    val buf = ByteArray(64 * 1024)
                                    var total = 0L
                                    while (true) {
                                        val count = input.read(buf)
                                        if (count < 0) break
                                        total += count
                                        if (total > GeodataFiles.MAX_BYTES) throw IOException("Geodata exceeds 64 MiB")
                                        out.write(buf, 0, count)
                                    }
                                    if (total == 0L) throw IOException("Empty geodata")
                                }
                            }
                        }
                        // Validate protobuf categories before accepting!
                        val count = part.inputStream().use { GeodataFiles.categories(it).size }
                        if (count <= 0) return@first null
                        part
                    } catch (_: Exception) {
                        part.delete()
                        null
                    }
                } ?: throw IOException("All download routes failed for $fileName")

                if (!winner.renameTo(downloadedTemp)) {
                    winner.copyTo(downloadedTemp, overwrite = true)
                    winner.delete()
                }
                val finalTarget = File(dir, fileName)
                if (!downloadedTemp.renameTo(finalTarget)) {
                    downloadedTemp.copyTo(finalTarget, overwrite = true)
                    downloadedTemp.delete()
                }
                customMarkerFile(dir, fileName).writeText(trimmed)
                inspectAssets(context).first { it.fileName == fileName }
            } finally {
                downloadedTemp.delete()
            }
        }
    }

    suspend fun resetAssetToBundled(context: Context, fileName: String): Result<GeoAssetInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, ASSET_DIR).apply { mkdirs() }
            customMarkerFile(dir, fileName).delete()
            val target = File(dir, fileName)
            val tmp = File(dir, "$fileName.tmp")
            context.assets.open(fileName).use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            }
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
                tmp.delete()
            }
            inspectAssets(context).first { it.fileName == fileName }
        }
    }
}
