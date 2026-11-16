package slop.miho.chumpy.xray

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.RoutingRuleAction
import slop.miho.chumpy.data.RoutingRuleMatchType
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType

enum class RoutingMode(val region: String?) {
    GLOBAL(null),
    BYPASS_RU("ru"),
    BYPASS_CN("cn"),
    BYPASS_IR("ir"),
    BYPASS_AF("af"),
    BYPASS_ID("id"),
    BYPASS_TR("tr"),
    BYPASS_BR("br"),
}

object XrayConfigBuilder {
    const val PROXY_TAG = "proxy"
    const val CHAIN_HOP_TAG = "chain-hop-1"
    const val DIRECT_TAG = "direct"
    const val BLOCK_TAG = "block"
    const val SOCKS_TAG = "socks"
    const val HTTP_TAG = "http-in"
    const val TUN_TAG = "tun"

    const val LOCAL_SOCKS_PORT = 10808
    const val LOCAL_HTTP_PORT = 10809
    const val DEFAULT_TEST_URL = "https://www.gstatic.com/generate_204"

    private const val TUN_NAME = "chumpy0"
    private const val USER_LEVEL = 8
    private const val BALANCER_TAG = "balancer"
    private const val OBSERVATORY_PROBE_INTERVAL = "1m"
    private val OBSERVING_STRATEGIES = setOf("leastping", "leastload")
    private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

    fun buildFull(
        profile: ProfileEntity,
        mode: RoutingMode,
        members: List<ProfileEntity> = emptyList(),
        testUrl: String = DEFAULT_TEST_URL,
        bypassDomestic: Boolean = false,
        blockAds: Boolean = false,
        dnsProvider: String? = null,
        fragmentEnabled: Boolean = false,
        fragmentLength: String = "100-200",
        fragmentInterval: String = "10-20",
        muxEnabled: Boolean = false,
        muxConcurrency: Int = 8,
        chainParent: ProfileEntity? = null,
        ipv6Enabled: Boolean = false,
        tunMtu: Int = 1500,
        proxyOnlyMode: Boolean = false,
        lanSharingEnabled: Boolean = false,
        socksPort: Int = LOCAL_SOCKS_PORT,
        httpPort: Int = LOCAL_HTTP_PORT,
        socksUsername: String = "",
        socksPassword: String = "",
        fakednsEnabled: Boolean = false,
        localDnsEnabled: Boolean = false,
        domainStrategy: String = "AsIs",
        remoteDns: String = "1.1.1.1",
        domesticDns: String = "223.5.5.5",
        fragmentPackets: String = "tlshello",
        fragmentMaxSplit: String = "",
        muxXudpConcurrency: Int = 8,
        muxXudpQuic: String = "reject",
        customRules: List<CustomRoutingRule> = emptyList(),
    ): String {
        if (profile.type == ProfileType.CUSTOM_JSON) {
            buildFullCustomOverride(
                profile = profile,
                tunMtu = tunMtu,
                proxyOnlyMode = proxyOnlyMode,
                lanSharingEnabled = lanSharingEnabled,
                socksPort = socksPort,
                httpPort = httpPort,
                socksUsername = socksUsername,
                socksPassword = socksPassword,
                fakednsEnabled = fakednsEnabled,
            )?.let { return it }
        }

        require(profile.type != ProfileType.BALANCER || members.isNotEmpty()) { "Balancer has no usable servers" }
        val balancerMembers = if (profile.type == ProfileType.BALANCER) members else emptyList()
        val strategy = profile.balancerStrategy.ifEmpty { "random" }
        val listenAddr = if (lanSharingEnabled) "0.0.0.0" else "127.0.0.1"
        val effectiveChainParent = chainParent?.takeIf {
            it.id != profile.id && it.type != ProfileType.BALANCER
        }

        return buildJsonObject {
            put("stats", buildJsonObject {})
            put("log", buildJsonObject { put("loglevel", "warning") })
            put(
                "policy",
                buildJsonObject {
                    put(
                        "levels",
                        buildJsonObject {
                            put(
                                "8",
                                buildJsonObject {
                                    put("handshake", 4)
                                    put("connIdle", 300)
                                    put("uplinkOnly", 1)
                                    put("downlinkOnly", 1)
                                },
                            )
                        },
                    )
                    put(
                        "system",
                        buildJsonObject {
                            put("statsOutboundUplink", true)
                            put("statsOutboundDownlink", true)
                        },
                    )
                },
            )
            put(
                "inbounds",
                buildInboundsArray(
                    listenAddr = listenAddr,
                    socksPort = socksPort,
                    httpPort = httpPort,
                    socksUsername = socksUsername,
                    socksPassword = socksPassword,
                    tunMtu = tunMtu,
                    proxyOnlyMode = proxyOnlyMode,
                    fakednsEnabled = fakednsEnabled,
                    includeHttp = lanSharingEnabled || httpPort != LOCAL_HTTP_PORT || proxyOnlyMode,
                ),
            )
            put(
                "outbounds",
                buildJsonArray {
                    val fragTag = if (fragmentEnabled) "fragment" else null
                    val proxyDialer = when {
                        effectiveChainParent != null -> CHAIN_HOP_TAG
                        else -> fragTag
                    }
                    if (balancerMembers.isEmpty()) {
                        add(
                            OutboundBuilder.build(
                                profile = profile,
                                tag = PROXY_TAG,
                                muxEnabled = muxEnabled,
                                muxConcurrency = muxConcurrency,
                                dialerProxy = proxyDialer,
                                muxXudpConcurrency = muxXudpConcurrency,
                                muxXudpQuic = muxXudpQuic,
                            ),
                        )
                    } else {
                        balancerMembers.forEachIndexed { index, member ->
                            add(
                                OutboundBuilder.build(
                                    profile = member,
                                    tag = memberTag(index),
                                    muxEnabled = muxEnabled,
                                    muxConcurrency = muxConcurrency,
                                    dialerProxy = proxyDialer,
                                    muxXudpConcurrency = muxXudpConcurrency,
                                    muxXudpQuic = muxXudpQuic,
                                ),
                            )
                        }
                    }
                    if (effectiveChainParent != null) {
                        add(
                            OutboundBuilder.build(
                                profile = effectiveChainParent,
                                tag = CHAIN_HOP_TAG,
                                muxEnabled = false,
                                dialerProxy = fragTag,
                            ),
                        )
                    }
                    if (fragmentEnabled) {
                        add(
                            buildJsonObject {
                                put("tag", "fragment")
                                put("protocol", "freedom")
                                put(
                                    "settings",
                                    buildJsonObject {
                                        put(
                                            "fragment",
                                            buildJsonObject {
                                                put("packets", fragmentPackets.ifBlank { "tlshello" })
                                                put("length", fragmentLength.ifBlank { "100-200" })
                                                put("interval", fragmentInterval.ifBlank { "10-20" })
                                                if (fragmentMaxSplit.isNotBlank()) {
                                                    put("maxSplit", fragmentMaxSplit.trim())
                                                }
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    }
                    add(
                        buildJsonObject {
                            put("tag", DIRECT_TAG)
                            put("protocol", "freedom")
                            put("settings", buildJsonObject { put("domainStrategy", if (ipv6Enabled) "UseIP" else "UseIPv4") })
                        },
                    )
                    add(
                        buildJsonObject {
                            put("tag", BLOCK_TAG)
                            put("protocol", "blackhole")
                            put("settings", buildJsonObject { put("response", buildJsonObject { put("type", "http") }) })
                        },
                    )
                },
            )
            put(
                "routing",
                routing(
                    mode = mode,
                    balanced = balancerMembers.isNotEmpty(),
                    strategy = strategy,
                    profileRules = if (mode == RoutingMode.GLOBAL) profile.routingRules else "",
                    geodataSources = profile.geodataSources,
                    bypassDomestic = bypassDomestic,
                    blockAds = blockAds,
                    domainStrategy = domainStrategy,
                    customRules = customRules,
                ),
            )
            if (strategy in OBSERVING_STRATEGIES && balancerMembers.isNotEmpty()) {
                put(
                    "observatory",
                    buildJsonObject {
                        put("subjectSelector", buildJsonArray { add(JsonPrimitive(PROXY_TAG)) })
                        put("probeURL", testUrl)
                        put("probeInterval", OBSERVATORY_PROBE_INTERVAL)
                        put("enableConcurrency", true)
                    },
                )
            }
            if (fakednsEnabled) {
                put(
                    "fakedns",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put("ipPool", "198.18.0.0/15")
                                put("poolSize", 65535)
                            },
                        )
                    },
                )
            }
            put(
                "dns",
                buildJsonObject {
                    put("hosts", buildJsonObject {})
                    put(
                        "servers",
                        buildJsonArray {
                            if (fakednsEnabled) add(JsonPrimitive("fakedns"))
                            add(JsonPrimitive(if (localDnsEnabled) "localhost" else "localhost"))
                            val primaryDns = resolvePrimaryDns(dnsProvider, remoteDns)
                            add(JsonPrimitive(primaryDns))
                            val fallbackDns = if (domesticDns.isNotBlank() && domesticDns != primaryDns) {
                                domesticDns.trim()
                            } else if (primaryDns == "8.8.8.8") {
                                "1.1.1.1"
                            } else {
                                "8.8.8.8"
                            }
                            add(JsonPrimitive(fallbackDns))
                        },
                    )
                },
            )
        }.toString()
    }

    /**
     * When the profile is a full Xray JSON document (`CUSTOM_JSON` with an `"outbounds"` array),
     * merge our required local inbounds, stats, and policy cleanly so live monitoring works.
     */
    private fun buildFullCustomOverride(
        profile: ProfileEntity,
        tunMtu: Int,
        proxyOnlyMode: Boolean,
        lanSharingEnabled: Boolean,
        socksPort: Int,
        httpPort: Int,
        socksUsername: String,
        socksPassword: String,
        fakednsEnabled: Boolean,
    ): String? {
        val parsed = runCatching { lenientJson.parseToJsonElement(profile.rawConfigJson).jsonObject }.getOrNull()
            ?: return null
        if (parsed["outbounds"] !is JsonArray) return null
        val map = parsed.toMutableMap()
        if ("stats" !in map) map["stats"] = buildJsonObject {}
        val listenAddr = if (lanSharingEnabled) "0.0.0.0" else "127.0.0.1"
        map["inbounds"] = buildInboundsArray(
            listenAddr = listenAddr,
            socksPort = socksPort,
            httpPort = httpPort,
            socksUsername = socksUsername,
            socksPassword = socksPassword,
            tunMtu = tunMtu,
            proxyOnlyMode = proxyOnlyMode,
            fakednsEnabled = fakednsEnabled,
            includeHttp = true,
        )
        return JsonObject(map).toString()
    }

    private fun buildInboundsArray(
        listenAddr: String,
        socksPort: Int,
        httpPort: Int,
        socksUsername: String,
        socksPassword: String,
        tunMtu: Int,
        proxyOnlyMode: Boolean,
        fakednsEnabled: Boolean,
        includeHttp: Boolean,
    ): JsonArray = buildJsonArray {
        add(
            buildJsonObject {
                put("tag", SOCKS_TAG)
                put("port", socksPort.coerceIn(1024, 65535))
                put("listen", listenAddr)
                put("protocol", "socks")
                put(
                    "settings",
                    buildJsonObject {
                        if (socksUsername.isNotEmpty() || socksPassword.isNotEmpty()) {
                            put("auth", "password")
                            put(
                                "accounts",
                                buildJsonArray {
                                    add(
                                        buildJsonObject {
                                            put("user", socksUsername)
                                            put("pass", socksPassword)
                                        },
                                    )
                                },
                            )
                        } else {
                            put("auth", "noauth")
                        }
                        put("udp", true)
                        put("userLevel", USER_LEVEL)
                    },
                )
                sniffing(fakednsEnabled)
            },
        )
        if (includeHttp) {
            add(
                buildJsonObject {
                    put("tag", HTTP_TAG)
                    put("port", httpPort.coerceIn(1024, 65535))
                    put("listen", listenAddr)
                    put("protocol", "http")
                    put(
                        "settings",
                        buildJsonObject {
                            if (socksUsername.isNotEmpty() || socksPassword.isNotEmpty()) {
                                put(
                                    "accounts",
                                    buildJsonArray {
                                        add(
                                            buildJsonObject {
                                                put("user", socksUsername)
                                                put("pass", socksPassword)
                                            },
                                        )
                                    },
                                )
                            }
                            put("userLevel", USER_LEVEL)
                        },
                    )
                    sniffing(fakednsEnabled)
                },
            )
        }
        if (!proxyOnlyMode) {
            add(
                buildJsonObject {
                    put("tag", TUN_TAG)
                    put("protocol", "tun")
                    put(
                        "settings",
                        buildJsonObject {
                            put("name", TUN_NAME)
                            put("mtu", tunMtu.coerceIn(1280, 9000))
                            put("userLevel", USER_LEVEL)
                        },
                    )
                    sniffing(fakednsEnabled)
                },
            )
        }
    }

    /**
     * Bare document for delay measurement (no inbounds).
     */
    fun buildTest(profile: ProfileEntity): String = buildJsonObject {
        put("log", buildJsonObject { put("loglevel", "warning") })
        put("inbounds", buildJsonArray {})
        put("outbounds", buildJsonArray { add(OutboundBuilder.build(profile, PROXY_TAG)) })
        put(
            "routing",
            buildJsonObject {
                put("domainStrategy", "AsIs")
                put("rules", buildJsonArray {})
            },
        )
    }.toString()

    /**
     * Standalone loopback SOCKS document for single-stream download throughput probes.
     */
    fun buildThroughputTest(profile: ProfileEntity, socksPort: Int): String = buildJsonObject {
        put("log", buildJsonObject { put("loglevel", "warning") })
        put(
            "inbounds",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("tag", SOCKS_TAG)
                        put("port", socksPort)
                        put("listen", "127.0.0.1")
                        put("protocol", "socks")
                        put(
                            "settings",
                            buildJsonObject {
                                put("auth", "noauth")
                                put("udp", true)
                                put("userLevel", USER_LEVEL)
                            },
                        )
                    },
                )
            },
        )
        put(
            "outbounds",
            buildJsonArray {
                add(OutboundBuilder.build(profile, PROXY_TAG))
                add(
                    buildJsonObject {
                        put("tag", DIRECT_TAG)
                        put("protocol", "freedom")
                    },
                )
            },
        )
        put(
            "routing",
            buildJsonObject {
                put("domainStrategy", "AsIs")
                put("rules", buildJsonArray {})
            },
        )
    }.toString()

    private fun resolvePrimaryDns(dnsProvider: String?, remoteDns: String): String {
        if (!dnsProvider.isNullOrBlank()) {
            when {
                dnsProvider.contains("9.9.9.9") -> return "9.9.9.9"
                dnsProvider.contains("8.8.8.8") -> return "8.8.8.8"
                dnsProvider.contains("AdGuard", ignoreCase = true) || dnsProvider.contains("94.140") -> return "94.140.14.14"
                dnsProvider.equals("Custom", ignoreCase = true) && remoteDns.isNotBlank() -> return remoteDns.trim()
                dnsProvider.contains("1.1.1.1") -> return if (remoteDns.isNotBlank() && remoteDns != "1.1.1.1") remoteDns.trim() else "1.1.1.1"
            }
        }
        return remoteDns.trim().ifBlank { "1.1.1.1" }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.sniffing(fakednsEnabled: Boolean = false) {
        put(
            "sniffing",
            buildJsonObject {
                put("enabled", true)
                put(
                    "destOverride",
                    buildJsonArray {
                        add(JsonPrimitive("http"))
                        add(JsonPrimitive("tls"))
                        add(JsonPrimitive("quic"))
                        if (fakednsEnabled) add(JsonPrimitive("fakedns"))
                    },
                )
            },
        )
    }

    private fun routing(
        mode: RoutingMode,
        balanced: Boolean,
        strategy: String,
        profileRules: String,
        geodataSources: String,
        bypassDomestic: Boolean = false,
        blockAds: Boolean = false,
        domainStrategy: String = "AsIs",
        customRules: List<CustomRoutingRule> = emptyList(),
    ) = buildJsonObject {
        put("domainStrategy", domainStrategy.ifBlank { "AsIs" })
        if (balanced) {
            put(
                "balancers",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("tag", BALANCER_TAG)
                            put("selector", buildJsonArray { add(JsonPrimitive(PROXY_TAG)) })
                            put("strategy", buildJsonObject { put("type", strategy) })
                        },
                    )
                },
            )
        }
        put(
            "rules",
            buildJsonArray {
                // User-defined custom rules take highest priority
                customRules.filter { it.enabled && it.pattern.isNotBlank() }.forEach { userRule ->
                    val target = when (userRule.action) {
                        RoutingRuleAction.DIRECT -> DIRECT_TAG
                        RoutingRuleAction.BLOCK -> BLOCK_TAG
                        RoutingRuleAction.PROXY -> PROXY_TAG
                    }
                    val tokens = userRule.pattern.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                    if (tokens.isNotEmpty()) {
                        rule {
                            when (userRule.matchType) {
                                RoutingRuleMatchType.DOMAIN -> put("domain", JsonArray(tokens.map(::JsonPrimitive)))
                                RoutingRuleMatchType.IP -> put("ip", JsonArray(tokens.map(::JsonPrimitive)))
                                RoutingRuleMatchType.PORT -> put("port", tokens.joinToString(","))
                                RoutingRuleMatchType.PROTOCOL -> put("protocol", JsonArray(tokens.map(::JsonPrimitive)))
                            }
                            if (userRule.action == RoutingRuleAction.PROXY && balanced) {
                                put("balancerTag", BALANCER_TAG)
                            } else {
                                put("outboundTag", target)
                            }
                        }
                    }
                }
                mode.region?.let { region ->
                    rule {
                        put("protocol", buildJsonArray { add(JsonPrimitive("bittorrent")) })
                        put("outboundTag", DIRECT_TAG)
                    }
                    rule {
                        put("domain", buildJsonArray {
                            when (region) {
                                "ru" -> "geosite:category-ru"
                                "ir" -> "geosite:category-ir"
                                "cn" -> "geosite:cn"
                                else -> null
                            }?.let { add(JsonPrimitive(it)) }
                            add(JsonPrimitive("domain:$region"))
                        })
                        put("outboundTag", DIRECT_TAG)
                    }
                    rule {
                        put("ip", buildJsonArray { add(JsonPrimitive("geoip:$region")) })
                        put("outboundTag", DIRECT_TAG)
                    }
                }
                rule {
                    put("port", "443")
                    put("network", "udp")
                    put("outboundTag", BLOCK_TAG)
                }
                rule {
                    put("ip", buildJsonArray { add(JsonPrimitive("geoip:private")) })
                    put("outboundTag", DIRECT_TAG)
                }
                rule {
                    put("domain", buildJsonArray { add(JsonPrimitive("geosite:private")) })
                    put("outboundTag", DIRECT_TAG)
                }
                if (bypassDomestic && mode == RoutingMode.GLOBAL) {
                    rule {
                        put("domain", buildJsonArray { add(JsonPrimitive("domain:pk")) })
                        put("outboundTag", DIRECT_TAG)
                    }
                }
                if (blockAds) {
                    rule {
                        put("domain", buildJsonArray { add(JsonPrimitive("geosite:category-ads-all")) })
                        put("outboundTag", BLOCK_TAG)
                    }
                }
                panelRules(profileRules, balanced).forEach { add(slop.miho.chumpy.sub.GeodataSources.rewrite(it, geodataSources)) }
                if (balanced) {
                    rule {
                        put("network", "tcp,udp")
                        put("balancerTag", BALANCER_TAG)
                    }
                }
            },
        )
    }

    private inline fun kotlinx.serialization.json.JsonArrayBuilder.rule(
        block: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit,
    ) {
        add(
            buildJsonObject {
                put("type", "field")
                block()
            },
        )
    }

    private fun panelRules(profileRules: String, balanced: Boolean): List<JsonObject> {
        if (profileRules.isBlank()) return emptyList()
        val rules = try {
            (lenientJson.parseToJsonElement(profileRules) as? JsonArray)
                ?.filterIsInstance<JsonObject>()
        } catch (_: Exception) {
            null
        } ?: return emptyList()
        return rules.map { rule ->
            val target = rule.string("outboundTag") ?: rule.string("balancerTag") ?: ""
            val rewritten = rule.toMutableMap()
            rewritten.remove("outboundTag")
            rewritten.remove("balancerTag")
            when (target.trim().lowercase()) {
                "direct", "freedom", "bypass" -> rewritten["outboundTag"] = JsonPrimitive(DIRECT_TAG)
                "block", "blackhole" -> rewritten["outboundTag"] = JsonPrimitive(BLOCK_TAG)
                else -> if (balanced) {
                    rewritten["balancerTag"] = JsonPrimitive(BALANCER_TAG)
                } else {
                    rewritten["outboundTag"] = JsonPrimitive(PROXY_TAG)
                }
            }
            JsonObject(rewritten)
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun memberTag(index: Int): String = if (index == 0) PROXY_TAG else "$PROXY_TAG-${index + 1}"
}
