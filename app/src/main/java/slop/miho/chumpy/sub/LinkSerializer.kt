package slop.miho.chumpy.sub

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.sub.LinkUtils.encodeBase64
import slop.miho.chumpy.sub.LinkUtils.encodeURIComponent
import slop.miho.chumpy.sub.LinkUtils.ipv6Brackets

/**
 * Serializes profiles back to standard share URIs (`vless://`, `vmess://`, `trojan://`,
 * `ss://`, `hy2://`, `wireguard://`, `socks://`, `http://`) and generates QR code bitmaps.
 */
object LinkSerializer {
    fun toShareUri(profile: ProfileEntity): String {
        val rawSource = profile.sourceLink?.trim()
        if (!rawSource.isNullOrEmpty() && profile.type != ProfileType.CUSTOM_JSON && profile.type != ProfileType.BALANCER) {
            // Re-serialize dynamically if user edited fields or if missing sourceLink, else preserve exact URI when untouched.
        }
        return when (profile.type) {
            ProfileType.VLESS -> serializeVless(profile)
            ProfileType.VMESS -> serializeVmess(profile)
            ProfileType.TROJAN -> serializeTrojan(profile)
            ProfileType.SHADOWSOCKS -> serializeShadowsocks(profile)
            ProfileType.HYSTERIA2 -> serializeHysteria2(profile)
            ProfileType.WIREGUARD -> serializeWireguard(profile)
            ProfileType.SOCKS -> serializeSocks(profile)
            ProfileType.HTTP -> serializeHttp(profile)
            ProfileType.CUSTOM_JSON -> profile.rawConfigJson
            ProfileType.BALANCER -> ""
        }
    }

    fun generateQrBitmap(content: String, sizePx: Int = 640): Bitmap? {
        if (content.isBlank() || content.length > 2950) return null
        return runCatching {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        }.getOrNull()
    }

    private fun serializeVless(p: ProfileEntity): String {
        val query = commonQuery(p).apply {
            put("encryption", p.method.ifBlank { "none" })
        }
        return buildUri("vless", p.password, p.server, p.serverPort, query, p.remarks)
    }

    private fun serializeTrojan(p: ProfileEntity): String {
        val query = commonQuery(p)
        return buildUri("trojan", p.password, p.server, p.serverPort, query, p.remarks)
    }

    private fun serializeVmess(p: ProfileEntity): String {
        val obj = buildJsonObject {
            put("v", "2")
            put("ps", p.remarks)
            put("add", p.server)
            put("port", p.serverPort.toString())
            put("id", p.password)
            put("aid", "0")
            put("scy", p.method.ifBlank { "auto" })
            put("net", p.network.ifBlank { "tcp" })
            put("type", when (p.network) {
                "grpc" -> p.mode.ifBlank { "gun" }
                else -> p.headerType.ifBlank { "none" }
            })
            put("host", if (p.network == "grpc") p.authority else p.host)
            put("path", when (p.network) {
                "kcp", "mkcp" -> p.seed
                "grpc" -> p.serviceName
                else -> p.path
            })
            put("tls", if (p.security == "tls") "tls" else "")
            if (p.sni.isNotEmpty()) put("sni", p.sni)
            if (p.alpn.isNotEmpty()) put("alpn", p.alpn)
            if (p.fingerprint.isNotEmpty()) put("fp", p.fingerprint)
            if (p.allowInsecure) put("insecure", "1")
        }
        return "vmess://${encodeBase64(obj.toString())}"
    }

    private fun serializeShadowsocks(p: ProfileEntity): String {
        val userPart = encodeBase64("${p.method}:${p.password}", removePadding = true)
        val remark = if (p.remarks.isNotBlank()) "#${encodeURIComponent(p.remarks)}" else ""
        return "ss://$userPart@${ipv6Brackets(p.server)}:${p.serverPort}$remark"
    }

    private fun serializeHysteria2(p: ProfileEntity): String {
        val query = LinkedHashMap<String, String>()
        if (p.sni.isNotEmpty()) query["sni"] = p.sni
        if (p.allowInsecure) query["insecure"] = "1"
        if (p.alpn.isNotEmpty()) query["alpn"] = p.alpn
        if (p.obfsPassword.isNotEmpty()) {
            query["obfs"] = "salamander"
            query["obfs-password"] = p.obfsPassword
        }
        if (p.portHopping.isNotEmpty()) query["mport"] = p.portHopping
        if (p.portHoppingInterval.isNotEmpty()) query["mportinterval"] = p.portHoppingInterval
        return buildUri("hy2", p.password, p.server, p.serverPort, query, p.remarks)
    }

    private fun serializeWireguard(p: ProfileEntity): String {
        val query = LinkedHashMap<String, String>()
        if (p.publicKey.isNotEmpty()) query["publickey"] = p.publicKey
        if (p.wgPresharedKey.isNotEmpty()) query["presharedkey"] = p.wgPresharedKey
        if (p.wgLocalAddress.isNotEmpty()) query["address"] = p.wgLocalAddress
        if (p.wgMtu > 0) query["mtu"] = p.wgMtu.toString()
        if (p.wgReserved.isNotEmpty()) query["reserved"] = p.wgReserved
        return buildUri("wireguard", p.wgSecretKey, p.server, p.serverPort, query, p.remarks)
    }

    private fun serializeSocks(p: ProfileEntity): String {
        val userInfo = when {
            p.username.isNotEmpty() || p.password.isNotEmpty() ->
                "${encodeURIComponent(p.username)}:${encodeURIComponent(p.password)}@"
            else -> ""
        }
        val remark = if (p.remarks.isNotBlank()) "#${encodeURIComponent(p.remarks)}" else ""
        return "socks://$userInfo${ipv6Brackets(p.server)}:${p.serverPort}$remark"
    }

    private fun serializeHttp(p: ProfileEntity): String {
        val userInfo = when {
            p.username.isNotEmpty() || p.password.isNotEmpty() ->
                "${encodeURIComponent(p.username)}:${encodeURIComponent(p.password)}@"
            else -> ""
        }
        val remark = if (p.remarks.isNotBlank()) "#${encodeURIComponent(p.remarks)}" else ""
        return "http://$userInfo${ipv6Brackets(p.server)}:${p.serverPort}$remark"
    }

    private fun commonQuery(p: ProfileEntity): LinkedHashMap<String, String> {
        val q = LinkedHashMap<String, String>()
        q["type"] = p.network.ifBlank { "tcp" }
        if (p.headerType.isNotEmpty()) q["headerType"] = p.headerType
        if (p.host.isNotEmpty()) q["host"] = p.host
        if (p.path.isNotEmpty()) q["path"] = p.path
        if (p.seed.isNotEmpty()) q["seed"] = p.seed
        if (p.mode.isNotEmpty()) q["mode"] = p.mode
        if (p.serviceName.isNotEmpty()) q["serviceName"] = p.serviceName
        if (p.authority.isNotEmpty()) q["authority"] = p.authority
        if (!p.xhttpExtra.isNullOrBlank()) q["extra"] = p.xhttpExtra
        if (p.security.isNotEmpty()) q["security"] = p.security
        if (p.sni.isNotEmpty()) q["sni"] = p.sni
        if (p.fingerprint.isNotEmpty()) q["fp"] = p.fingerprint
        if (p.alpn.isNotEmpty()) q["alpn"] = p.alpn
        if (p.flow.isNotEmpty()) q["flow"] = p.flow
        if (p.allowInsecure) q["allowInsecure"] = "1"
        if (p.publicKey.isNotEmpty()) q["pbk"] = p.publicKey
        if (p.shortId.isNotEmpty()) q["sid"] = p.shortId
        if (p.spiderX.isNotEmpty()) q["spx"] = p.spiderX
        return q
    }

    private fun buildUri(
        scheme: String,
        userInfo: String,
        server: String,
        port: Int,
        query: Map<String, String>,
        remarks: String,
    ): String {
        val auth = if (userInfo.isNotEmpty()) "${encodeURIComponent(userInfo)}@" else ""
        val qs = if (query.isEmpty()) "" else "?" + query.entries.joinToString("&") { (k, v) ->
            "${encodeURIComponent(k)}=${encodeURIComponent(v)}"
        }
        val frag = if (remarks.isNotBlank()) "#${encodeURIComponent(remarks)}" else ""
        return "$scheme://$auth${ipv6Brackets(server)}:$port$qs$frag"
    }
}
