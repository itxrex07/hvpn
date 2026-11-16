package slop.miho.chumpy.xray

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.sub.LinkUtils

/**
 * Renders a [ProfileEntity] as one Xray outbound, in the current core's flat settings
 * dialect ({@code address}/{@code port}/... directly in settings). Modeled on v2rayNG's
 * CoreOutboundBuilder.
 */
object OutboundBuilder {
    private const val TLS = "tls"
    private const val REALITY = "reality"
    private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun host(profile: ProfileEntity): String = LinkUtils.ipv6Brackets(profile.server)

    fun build(
        profile: ProfileEntity,
        tag: String = "proxy",
        muxEnabled: Boolean = false,
        muxConcurrency: Int = 8,
        dialerProxy: String? = null,
        muxXudpConcurrency: Int = 8,
        muxXudpQuic: String = "reject",
    ): JsonObject {
        if (profile.type == ProfileType.CUSTOM_JSON) {
            return buildCustomJsonOutbound(
                profile = profile,
                tag = tag,
                muxEnabled = muxEnabled,
                muxConcurrency = muxConcurrency,
                dialerProxy = dialerProxy,
                muxXudpConcurrency = muxXudpConcurrency,
                muxXudpQuic = muxXudpQuic,
            )
        }
        return buildJsonObject {
            put("tag", tag)
            put("protocol", protocolName(profile.type))
            put("settings", settings(profile))
            if (profile.type != ProfileType.WIREGUARD) {
                val baseStream = streamSettings(profile)
                if (dialerProxy != null) {
                    val stream = baseStream ?: buildJsonObject { put("network", "tcp") }
                    val existingSockopt = stream["sockopt"] as? JsonObject
                    val updatedSockopt = JsonObject(
                        (existingSockopt?.toMap() ?: emptyMap()) + ("dialerProxy" to JsonPrimitive(dialerProxy)),
                    )
                    put("streamSettings", stream.with("sockopt" to updatedSockopt))
                } else {
                    baseStream?.let { put("streamSettings", it) }
                }
            }
            val useMux = muxEnabled &&
                profile.type != ProfileType.WIREGUARD &&
                profile.type != ProfileType.HYSTERIA2 &&
                !profile.flow.contains("vision", ignoreCase = true)
            put(
                "mux",
                buildJsonObject {
                    put("enabled", useMux)
                    if (useMux) {
                        put("concurrency", muxConcurrency.coerceIn(1, 128))
                        put("xudpConcurrency", muxXudpConcurrency.coerceIn(1, 128))
                        put("xudpProxyUDP443", muxXudpQuic.ifBlank { "reject" })
                    }
                },
            )
        }
    }

    private fun buildCustomJsonOutbound(
        profile: ProfileEntity,
        tag: String,
        muxEnabled: Boolean,
        muxConcurrency: Int,
        dialerProxy: String?,
        muxXudpConcurrency: Int,
        muxXudpQuic: String,
    ): JsonObject {
        val parsed = runCatching { lenientJson.parseToJsonElement(profile.rawConfigJson).jsonObject }.getOrNull()
        val rawOutbound = when {
            parsed == null -> null
            parsed["outbounds"] is JsonArray -> (parsed["outbounds"] as JsonArray)
                .filterIsInstance<JsonObject>()
                .firstOrNull {
                    val proto = (it["protocol"] as? JsonPrimitive)?.content
                    proto != null && proto !in setOf("freedom", "blackhole", "dns")
                }
            parsed["protocol"] != null -> parsed
            else -> null
        } ?: return buildJsonObject {
            put("tag", tag)
            put("protocol", "freedom")
            put("settings", buildJsonObject {})
        }

        val map = rawOutbound.toMutableMap()
        map["tag"] = JsonPrimitive(tag)
        if (dialerProxy != null) {
            val stream = (map["streamSettings"] as? JsonObject) ?: buildJsonObject { put("network", "tcp") }
            val existingSockopt = stream["sockopt"] as? JsonObject
            val updatedSockopt = JsonObject(
                (existingSockopt?.toMap() ?: emptyMap()) + ("dialerProxy" to JsonPrimitive(dialerProxy)),
            )
            map["streamSettings"] = stream.with("sockopt" to updatedSockopt)
        }
        if (muxEnabled && "mux" !in map) {
            map["mux"] = buildJsonObject {
                put("enabled", true)
                put("concurrency", muxConcurrency.coerceIn(1, 128))
                put("xudpConcurrency", muxXudpConcurrency.coerceIn(1, 128))
                put("xudpProxyUDP443", muxXudpQuic.ifBlank { "reject" })
            }
        }
        return JsonObject(map)
    }

    fun protocolName(type: ProfileType): String = when (type) {
        ProfileType.VMESS -> "vmess"
        ProfileType.VLESS -> "vless"
        ProfileType.TROJAN -> "trojan"
        ProfileType.SHADOWSOCKS -> "shadowsocks"
        ProfileType.SOCKS -> "socks"
        ProfileType.HTTP -> "http"
        ProfileType.WIREGUARD -> "wireguard"
        ProfileType.HYSTERIA2 -> "hysteria"
        ProfileType.CUSTOM_JSON -> "freedom"
        ProfileType.BALANCER -> error("a balancer is not an outbound")
    }

    private fun settings(profile: ProfileEntity): JsonObject = buildJsonObject {
        when (profile.type) {
            ProfileType.BALANCER -> error("a balancer is not an outbound")
            ProfileType.CUSTOM_JSON -> Unit

            ProfileType.VMESS -> {
                put("address", host(profile))
                put("port", profile.serverPort)
                put("id", profile.password)
                put("security", profile.method.ifEmpty { "auto" })
                put("level", 8)
            }

            ProfileType.VLESS -> {
                put("address", host(profile))
                put("port", profile.serverPort)
                put("id", profile.password)
                put("encryption", profile.method.ifEmpty { "none" })
                if (profile.flow.isNotEmpty()) put("flow", profile.flow)
                put("level", 8)
            }

            ProfileType.TROJAN -> {
                put("address", host(profile))
                put("port", profile.serverPort)
                put("password", profile.password)
                if (profile.flow.isNotEmpty()) put("flow", profile.flow)
                put("level", 8)
            }

            ProfileType.SHADOWSOCKS -> {
                put("address", host(profile))
                put("port", profile.serverPort)
                put("method", profile.method)
                put("password", profile.password)
                put("level", 8)
            }

            ProfileType.SOCKS, ProfileType.HTTP -> {
                put("address", host(profile))
                put("port", profile.serverPort)
                if (profile.username.isNotEmpty()) put("user", profile.username)
                if (profile.password.isNotEmpty()) put("pass", profile.password)
                put("level", 8)
            }

            ProfileType.HYSTERIA2 -> {
                put("version", 2)
                put("address", host(profile))
                put("port", profile.serverPort)
            }

            ProfileType.WIREGUARD -> wireguardSettings(profile).forEach { (key, value) ->
                put(key, value)
            }
        }
    }

    private fun wireguardSettings(profile: ProfileEntity): JsonObject = buildJsonObject {
        put("secretKey", profile.wgSecretKey)
        put(
            "address",
            JsonArray(
                profile.wgLocalAddress.split(',').map { it.trim() }
                    .filter { it.isNotEmpty() }.map(::JsonPrimitive),
            ),
        )
        put(
            "peers",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("publicKey", profile.publicKey)
                        if (profile.wgPresharedKey.isNotEmpty()) put("preSharedKey", profile.wgPresharedKey)
                        put("endpoint", "${host(profile)}:${profile.serverPort}")
                        put("keepAlive", 0)
                    },
                )
            },
        )
        put("mtu", if (profile.wgMtu > 0) profile.wgMtu else 1420)
        reservedOf(profile)?.let { put("reserved", it) }
        put("remoteDNS", JsonArray(listOf(JsonPrimitive("1.1.1.1"), JsonPrimitive("8.8.8.8"))))
    }

    private fun reservedOf(profile: ProfileEntity): JsonArray? {
        val bytes = profile.wgReserved.split(',').mapNotNull { it.trim().toIntOrNull() }
        return if (bytes.size == 3) JsonArray(bytes.map(::JsonPrimitive)) else null
    }

    private fun streamSettings(profile: ProfileEntity): JsonObject? {
        val network = if (profile.type == ProfileType.HYSTERIA2) "hysteria" else profile.network.ifEmpty { "tcp" }
        var json = buildJsonObject { put("network", network) }

        when (profile.security) {
            TLS -> json = json.with("security" to JsonPrimitive(TLS)).with("tlsSettings" to tlsSettings(profile))
            REALITY -> json = json.with("security" to JsonPrimitive(REALITY)).with("realitySettings" to realitySettings(profile))
        }

        transportSettings(profile)?.let { (name, settings) -> json = json.with(name to settings) }

        if (profile.type == ProfileType.HYSTERIA2 || profile.network == "hysteria") {
            json = json.with(
                "hysteriaSettings" to buildJsonObject {
                    put("version", 2)
                    put("auth", profile.password)
                },
            )
            finalMask(profile)?.let { json = json.with("finalmask" to it) }
        }

        return if (json.size > 1 || network !in setOf("tcp", "raw")) json else null
    }

    private fun tlsSettings(profile: ProfileEntity): JsonObject = buildJsonObject {
        if (profile.sni.isNotEmpty()) put("serverName", profile.sni)
        if (profile.allowInsecure) put("allowInsecure", true)
        if (profile.fingerprint.isNotEmpty()) put("fingerprint", profile.fingerprint)
        val alpn = profile.alpn.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val effective = if (alpn.isNotEmpty()) alpn else if (profile.type == ProfileType.HYSTERIA2 || profile.network == "hysteria") listOf("h3") else emptyList()
        if (effective.isNotEmpty()) put("alpn", JsonArray(effective.map(::JsonPrimitive)))
    }

    private fun realitySettings(profile: ProfileEntity): JsonObject = buildJsonObject {
        if (profile.sni.isNotEmpty()) put("serverName", profile.sni)
        if (profile.fingerprint.isNotEmpty()) put("fingerprint", profile.fingerprint)
        put("publicKey", profile.publicKey)
        if (profile.shortId.isNotEmpty()) put("shortId", profile.shortId)
        if (profile.spiderX.isNotEmpty()) put("spiderX", profile.spiderX)
        put("show", false)
    }

    private fun transportSettings(profile: ProfileEntity): Pair<String, JsonObject>? = when (profile.network) {
        "tcp", "raw" -> {
            if (profile.headerType == "http") {
                "tcpSettings" to buildJsonObject {
                    put(
                        "header",
                        buildJsonObject {
                            put("type", "http")
                            put(
                                "request",
                                buildJsonObject {
                                    val hostHeader = profile.host.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                                    if (hostHeader.isNotEmpty()) {
                                        put("headers", buildJsonObject { put("Host", JsonArray(hostHeader.map(::JsonPrimitive))) })
                                    }
                                    put("path", JsonArray(listOf(JsonPrimitive(profile.path.ifEmpty { "/" }))))
                                },
                            )
                        },
                    )
                }
            } else {
                null
            }
        }

        "kcp", "mkcp" -> "kcpSettings" to buildJsonObject {
            put("header", buildJsonObject { put("type", profile.headerType.ifEmpty { "none" }) })
            if (profile.seed.isNotEmpty()) put("seed", profile.seed)
        }

        "ws", "websocket" -> "wsSettings" to buildJsonObject {
            put("path", profile.path)
            if (profile.host.isNotEmpty()) put("host", profile.host)
        }

        "httpupgrade" -> "httpupgradeSettings" to buildJsonObject {
            put("path", profile.path)
            if (profile.host.isNotEmpty()) put("host", profile.host)
        }

        "grpc" -> "grpcSettings" to buildJsonObject {
            put("serviceName", profile.serviceName)
            if (profile.authority.isNotEmpty()) put("authority", profile.authority)
            if (profile.mode == "multi") put("multiMode", true)
        }

        "xhttp", "splithttp" -> "xhttpSettings" to buildJsonObject {
            if (profile.host.isNotEmpty()) put("host", profile.host)
            put("path", profile.path)
            put("mode", profile.mode.ifEmpty { "auto" })
            rawJson(profile.xhttpExtra)?.let { put("extra", it) }
        }

        else -> null
    }

    private fun finalMask(profile: ProfileEntity): JsonObject? {
        val udp = buildJsonArray {
            if (profile.obfsPassword.isNotEmpty()) {
                add(
                    buildJsonObject {
                        put("type", "salamander")
                        put("settings", buildJsonObject { put("password", profile.obfsPassword) })
                    },
                )
            }
            if (profile.portHopping.isNotEmpty()) {
                add(
                    buildJsonObject {
                        put("type", "udphop")
                        put(
                            "settings",
                            buildJsonObject {
                                put("mode", "intervalremote")
                                put("interval", profile.portHoppingInterval.ifEmpty { "30" })
                                put("remotePorts", profile.portHopping)
                            },
                        )
                    },
                )
            }
        }
        if (udp.isEmpty()) return null
        return buildJsonObject { put("udp", udp) }
    }

    private fun rawJson(raw: String?): JsonElement? {
        if (raw.isNullOrBlank()) return null
        return try {
            Json.parseToJsonElement(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun JsonObject.with(vararg pairs: Pair<String, JsonElement>): JsonObject =
        JsonObject(toMap() + pairs)
}
