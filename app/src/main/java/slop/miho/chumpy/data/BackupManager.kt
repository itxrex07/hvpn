package slop.miho.chumpy.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.xray.XrayConfigBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupPayload(
    val subscriptions: List<SubscriptionEntity>,
    val profiles: List<ProfileEntity>,
    val connectionSettings: ConnectionSettings?,
    val uiSettingsJson: JSONObject?,
)

object BackupManager {
    private const val BACKUP_JSON_ENTRY = "hollow_backup.json"
    private const val FORMAT_VERSION = 1

    suspend fun exportToUri(
        context: Context,
        uri: Uri,
        subscriptions: List<SubscriptionEntity>,
        profiles: List<ProfileEntity>,
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = buildZipBytes(context, subscriptions, profiles)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: throw IOException("Unable to open output destination")
            profiles.size
        }
    }

    suspend fun importFromUri(context: Context, uri: Uri): Result<BackupPayload> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Unable to open backup file")
            parseZipBytes(bytes)
        }
    }

    suspend fun uploadWebDav(
        context: Context,
        url: String,
        username: String,
        password: String,
        subscriptions: List<SubscriptionEntity>,
        profiles: List<ProfileEntity>,
        tunnelUp: Boolean,
        socksPort: Int = XrayConfigBuilder.LOCAL_SOCKS_PORT,
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = normalizeWebDavTarget(url)
            val bytes = buildZipBytes(context, subscriptions, profiles)
            val client = httpClient(tunnelUp, socksPort)
            val req = Request.Builder()
                .url(targetUrl)
                .apply {
                    if (username.isNotBlank() || password.isNotBlank()) {
                        header("Authorization", Credentials.basic(username, password))
                    }
                }
                .put(bytes.toRequestBody("application/zip".toMediaType()))
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("WebDAV upload HTTP ${resp.code}")
            }
            profiles.size
        }
    }

    suspend fun downloadWebDav(
        url: String,
        username: String,
        password: String,
        tunnelUp: Boolean,
        socksPort: Int = XrayConfigBuilder.LOCAL_SOCKS_PORT,
    ): Result<BackupPayload> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = normalizeWebDavTarget(url)
            val client = httpClient(tunnelUp, socksPort)
            val req = Request.Builder()
                .url(targetUrl)
                .apply {
                    if (username.isNotBlank() || password.isNotBlank()) {
                        header("Authorization", Credentials.basic(username, password))
                    }
                }
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("WebDAV download HTTP ${resp.code}")
                val bytes = resp.body?.bytes() ?: throw IOException("Empty WebDAV response")
                parseZipBytes(bytes)
            }
        }
    }

    fun applySettingsFromBackup(context: Context, payload: BackupPayload) {
        payload.connectionSettings?.let { restored ->
            ConnectionStore.update(context) { restored }
        }
        payload.uiSettingsJson?.let { ui ->
            SettingsStore.update(context) { cur ->
                cur.copy(
                    designStyle = DesignStyle.entries.firstOrNull { it.name == ui.optString("designStyle") } ?: cur.designStyle,
                    themeMode = ThemeMode.entries.firstOrNull { it.name == ui.optString("themeMode") } ?: cur.themeMode,
                    accent = Accent.entries.firstOrNull { it.name == ui.optString("accent") } ?: cur.accent,
                    hapticFeedback = ui.optBoolean("hapticFeedback", cur.hapticFeedback),
                    maskAddresses = ui.optBoolean("maskAddresses", cur.maskAddresses),
                    happSpoof = ui.optBoolean("happSpoof", cur.happSpoof),
                    fontId = ui.optString("fontId").ifBlank { cur.fontId },
                    flagStyle = FlagStyle.entries.firstOrNull { it.name == ui.optString("flagStyle") } ?: cur.flagStyle,
                )
            }
        }
    }

    private fun buildZipBytes(
        context: Context,
        subscriptions: List<SubscriptionEntity>,
        profiles: List<ProfileEntity>,
    ): ByteArray {
        val ui = SettingsStore.settings(context).value
        val root = JSONObject()
            .put("version", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("connectionSettings", ConnectionStore.toJson(ConnectionStore.current(context)))
            .put(
                "uiSettings",
                JSONObject()
                    .put("designStyle", ui.designStyle.name)
                    .put("themeMode", ui.themeMode.name)
                    .put("accent", ui.accent.name)
                    .put("hapticFeedback", ui.hapticFeedback)
                    .put("maskAddresses", ui.maskAddresses)
                    .put("happSpoof", ui.happSpoof)
                    .put("fontId", ui.fontId)
                    .put("flagStyle", ui.flagStyle.name),
            )
            .put("subscriptions", JSONArray().apply { subscriptions.forEach { put(subToJson(it)) } })
            .put("profiles", JSONArray().apply { profiles.forEach { put(profileToJson(it)) } })

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry(BACKUP_JSON_ENTRY))
            zos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    private fun parseZipBytes(bytes: ByteArray): BackupPayload {
        var jsonText: String? = null
        // Also support raw uncompressed JSON if pasted/uploaded directly
        val trimmed = bytes.toString(Charsets.UTF_8).trimStart()
        if (trimmed.startsWith("{")) {
            jsonText = trimmed
        } else {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".json", ignoreCase = true)) {
                        jsonText = zis.readBytes().toString(Charsets.UTF_8)
                        break
                    }
                    entry = zis.nextEntry
                }
            }
        }
        val body = jsonText ?: throw IOException("Backup archive contains no JSON payload")
        val root = JSONObject(body)
        val subsArray = root.optJSONArray("subscriptions") ?: JSONArray()
        val subs = (0 until subsArray.length()).mapNotNull { i ->
            subsArray.optJSONObject(i)?.let(::subFromJson)
        }
        val profilesArray = root.optJSONArray("profiles") ?: JSONArray()
        val profiles = (0 until profilesArray.length()).mapNotNull { i ->
            profilesArray.optJSONObject(i)?.let(::profileFromJson)
        }
        val conn = root.optJSONObject("connectionSettings")?.let { ConnectionStore.fromJson(it) }
        val ui = root.optJSONObject("uiSettings")
        return BackupPayload(subs, profiles, conn, ui)
    }

    private fun httpClient(tunnelUp: Boolean, socksPort: Int): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .apply {
                if (tunnelUp) {
                    proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socksPort)))
                }
            }
            .build()

    private fun normalizeWebDavTarget(raw: String): String {
        val trimmed = raw.trim()
        require(trimmed.startsWith("https://", true) || trimmed.startsWith("http://", true)) {
            "WebDAV URL must start with http:// or https://"
        }
        return if (trimmed.endsWith("/")) "${trimmed}hollow-vpn-backup.zip" else trimmed
    }

    private fun subToJson(s: SubscriptionEntity): JSONObject = JSONObject()
        .put("id", s.id)
        .put("name", s.name)
        .put("url", s.url)
        .put("createdAt", s.createdAt)
        .put("lastUpdateAttempt", s.lastUpdateAttempt)
        .put("lastUpdateSuccess", s.lastUpdateSuccess)
        .put("updateIntervalHours", s.updateIntervalHours ?: JSONObject.NULL)
        .put("autoUpdate", s.autoUpdate)
        .put("uploadBytes", s.uploadBytes)
        .put("downloadBytes", s.downloadBytes)
        .put("totalBytes", s.totalBytes)
        .put("expireEpochSeconds", s.expireEpochSeconds)
        .put("pinned", s.pinned)
        .put("filterRegex", s.filterRegex)
        .put("customUserAgent", s.customUserAgent)
        .put("updateIntervalMinutes", s.updateIntervalMinutes ?: JSONObject.NULL)

    private fun subFromJson(j: JSONObject): SubscriptionEntity = SubscriptionEntity(
        id = j.optString("id"),
        name = j.optString("name"),
        url = j.optString("url"),
        createdAt = j.optLong("createdAt", System.currentTimeMillis()),
        lastUpdateAttempt = j.optLong("lastUpdateAttempt", 0L),
        lastUpdateSuccess = j.optLong("lastUpdateSuccess", 0L),
        updateIntervalHours = if (j.isNull("updateIntervalHours")) null else j.optInt("updateIntervalHours"),
        autoUpdate = j.optBoolean("autoUpdate", true),
        uploadBytes = j.optLong("uploadBytes", 0L),
        downloadBytes = j.optLong("downloadBytes", 0L),
        totalBytes = j.optLong("totalBytes", 0L),
        expireEpochSeconds = j.optLong("expireEpochSeconds", 0L),
        pinned = j.optBoolean("pinned", false),
        filterRegex = j.optString("filterRegex"),
        customUserAgent = j.optString("customUserAgent"),
        updateIntervalMinutes = if (j.isNull("updateIntervalMinutes")) null else j.optInt("updateIntervalMinutes"),
    )

    private fun profileToJson(p: ProfileEntity): JSONObject = JSONObject()
        .put("id", p.id)
        .put("subscriptionId", p.subscriptionId ?: JSONObject.NULL)
        .put("type", p.type.name)
        .put("remarks", p.remarks)
        .put("order", p.order)
        .put("sourceLink", p.sourceLink ?: JSONObject.NULL)
        .put("server", p.server)
        .put("serverPort", p.serverPort)
        .put("password", p.password)
        .put("method", p.method)
        .put("username", p.username)
        .put("network", p.network)
        .put("headerType", p.headerType)
        .put("host", p.host)
        .put("path", p.path)
        .put("seed", p.seed)
        .put("serviceName", p.serviceName)
        .put("authority", p.authority)
        .put("mode", p.mode)
        .put("xhttpExtra", p.xhttpExtra ?: JSONObject.NULL)
        .put("security", p.security)
        .put("sni", p.sni)
        .put("alpn", p.alpn)
        .put("fingerprint", p.fingerprint)
        .put("flow", p.flow)
        .put("allowInsecure", p.allowInsecure)
        .put("publicKey", p.publicKey)
        .put("shortId", p.shortId)
        .put("spiderX", p.spiderX)
        .put("obfsPassword", p.obfsPassword)
        .put("portHopping", p.portHopping)
        .put("portHoppingInterval", p.portHoppingInterval)
        .put("wgSecretKey", p.wgSecretKey)
        .put("wgPresharedKey", p.wgPresharedKey)
        .put("wgLocalAddress", p.wgLocalAddress)
        .put("wgMtu", p.wgMtu)
        .put("wgReserved", p.wgReserved)
        .put("balancerStrategy", p.balancerStrategy)
        .put("balancerMembers", p.balancerMembers)
        .put("routingRules", p.routingRules)
        .put("geodataSources", p.geodataSources)
        .put("hidden", p.hidden)
        .put("pinned", p.pinned)
        .put("concealed", p.concealed)
        .put("dialerProxyId", p.dialerProxyId)
        .put("rawConfigJson", p.rawConfigJson)
        .put("linkKey", p.linkKey)

    private fun profileFromJson(j: JSONObject): ProfileEntity {
        val type = ProfileType.entries.firstOrNull { it.name == j.optString("type") } ?: ProfileType.VLESS
        return ProfileEntity(
            id = j.optString("id"),
            subscriptionId = if (j.isNull("subscriptionId")) null else j.optString("subscriptionId").takeIf { it.isNotEmpty() },
            type = type,
            remarks = j.optString("remarks"),
            order = j.optLong("order", 0L),
            sourceLink = if (j.isNull("sourceLink")) null else j.optString("sourceLink").takeIf { it.isNotEmpty() },
            server = j.optString("server"),
            serverPort = j.optInt("serverPort", 0),
            password = j.optString("password"),
            method = j.optString("method"),
            username = j.optString("username"),
            network = j.optString("network", "tcp").ifBlank { "tcp" },
            headerType = j.optString("headerType"),
            host = j.optString("host"),
            path = j.optString("path"),
            seed = j.optString("seed"),
            serviceName = j.optString("serviceName"),
            authority = j.optString("authority"),
            mode = j.optString("mode"),
            xhttpExtra = if (j.isNull("xhttpExtra")) null else j.optString("xhttpExtra"),
            security = j.optString("security"),
            sni = j.optString("sni"),
            alpn = j.optString("alpn"),
            fingerprint = j.optString("fingerprint"),
            flow = j.optString("flow"),
            allowInsecure = j.optBoolean("allowInsecure", false),
            publicKey = j.optString("publicKey"),
            shortId = j.optString("shortId"),
            spiderX = j.optString("spiderX"),
            obfsPassword = j.optString("obfsPassword"),
            portHopping = j.optString("portHopping"),
            portHoppingInterval = j.optString("portHoppingInterval"),
            wgSecretKey = j.optString("wgSecretKey"),
            wgPresharedKey = j.optString("wgPresharedKey"),
            wgLocalAddress = j.optString("wgLocalAddress"),
            wgMtu = j.optInt("wgMtu", 0),
            wgReserved = j.optString("wgReserved"),
            balancerStrategy = j.optString("balancerStrategy"),
            balancerMembers = j.optString("balancerMembers"),
            routingRules = j.optString("routingRules"),
            geodataSources = j.optString("geodataSources"),
            hidden = j.optBoolean("hidden", false),
            pinned = j.optBoolean("pinned", false),
            concealed = j.optBoolean("concealed", false),
            dialerProxyId = j.optString("dialerProxyId"),
            rawConfigJson = j.optString("rawConfigJson"),
            linkKey = j.optString("linkKey"),
        )
    }
}
