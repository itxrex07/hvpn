package slop.miho.chumpy.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import slop.miho.chumpy.xray.RoutingMode
import slop.miho.chumpy.xray.XrayConfigBuilder
import java.io.File
import java.util.UUID

enum class RoutingRuleMatchType {
    DOMAIN,
    IP,
    PORT,
    PROTOCOL,
}

enum class RoutingRuleAction {
    PROXY,
    DIRECT,
    BLOCK,
}

data class CustomRoutingRule(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val matchType: RoutingRuleMatchType = RoutingRuleMatchType.DOMAIN,
    val pattern: String = "",
    val action: RoutingRuleAction = RoutingRuleAction.DIRECT,
    val enabled: Boolean = true,
) {
    companion object {
        fun listFromJson(raw: String): List<CustomRoutingRule> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val pattern = obj.optString("pattern").trim()
                        if (pattern.isEmpty()) continue
                        add(
                            CustomRoutingRule(
                                id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                                label = obj.optString("label"),
                                matchType = RoutingRuleMatchType.entries.firstOrNull {
                                    it.name == obj.optString("matchType")
                                } ?: RoutingRuleMatchType.DOMAIN,
                                pattern = pattern,
                                action = RoutingRuleAction.entries.firstOrNull {
                                    it.name == obj.optString("action")
                                } ?: RoutingRuleAction.DIRECT,
                                enabled = obj.optBoolean("enabled", true),
                            ),
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }

        fun listToJson(rules: List<CustomRoutingRule>): String {
            if (rules.isEmpty()) return ""
            val arr = JSONArray()
            rules.forEach { r ->
                arr.put(
                    JSONObject()
                        .put("id", r.id)
                        .put("label", r.label)
                        .put("matchType", r.matchType.name)
                        .put("pattern", r.pattern)
                        .put("action", r.action.name)
                        .put("enabled", r.enabled),
                )
            }
            return arr.toString()
        }
    }
}

/**
 * Connection preferences — shared between UI and :core processes via atomic JSON snapshot.
 */
data class ConnectionSettings(
    val selectedSubscriptionId: String? = null,
    val selectedProfileId: String? = null,
    val routingMode: RoutingMode = RoutingMode.GLOBAL,
    val serversSort: ServersSort = ServersSort.DEFAULT,
    val delayTestUrl: String = XrayConfigBuilder.DEFAULT_TEST_URL,
    val bypassedApps: Set<String> = emptySet(),
    val perAppEnabled: Boolean = true,
    val autoReconnect: Boolean = false,
    val bypassLan: Boolean = true,
    val killSwitch: Boolean = true,
    val autoConnectUntrustedWifi: Boolean = false,
    val startOnBoot: Boolean = false,
    val bypassDomestic: Boolean = true,
    val blockAds: Boolean = false,
    val dnsProvider: String = "DoH · 1.1.1.1",
    val fragmentEnabled: Boolean = false,
    val fragmentLength: String = "100-200",
    val fragmentInterval: String = "10-20",
    val fragmentPackets: String = "tlshello",
    val fragmentMaxSplit: String = "",
    val muxEnabled: Boolean = false,
    val muxConcurrency: Int = 8,
    val muxXudpConcurrency: Int = 8,
    val muxXudpQuic: String = "reject",

    // Network, TUN & Proxy Sharing
    val ipv6Enabled: Boolean = false,
    val tunMtu: Int = 1500,
    val proxyOnlyMode: Boolean = false,
    val lanSharingEnabled: Boolean = false,
    val socksPort: Int = XrayConfigBuilder.LOCAL_SOCKS_PORT,
    val httpPort: Int = XrayConfigBuilder.LOCAL_HTTP_PORT,
    val socksUsername: String = "",
    val socksPassword: String = "",

    // Advanced DNS & Routing
    val fakednsEnabled: Boolean = false,
    val localDnsEnabled: Boolean = false,
    val domainStrategy: String = "AsIs",
    val remoteDns: String = "1.1.1.1",
    val domesticDns: String = "223.5.5.5",
    val customRoutingRulesJson: String = "",

    // Smart Connection & Resilience
    val smartSwitchEnabled: Boolean = false,
    val cdnMirrorFallback: Boolean = true,

    // WebDAV Sync (user-configured only)
    val webdavUrl: String = "",
    val webdavUser: String = "",
    val webdavPass: String = "",
)

object ConnectionStore {
    private const val PREFS = "connection_settings"

    private val state = MutableStateFlow(ConnectionSettings())
    private var seeded = false

    @Synchronized
    fun settings(context: Context): StateFlow<ConnectionSettings> {
        if (!seeded) {
            state.value = current(context)
            writeSnapshot(context, state.value)
            seeded = true
        }
        return state.asStateFlow()
    }

    @Synchronized
    fun update(context: Context, transform: (ConnectionSettings) -> ConnectionSettings) {
        val updated = transform(settings(context).value)
        write(context, updated)
        state.value = updated
    }

    fun current(context: Context): ConnectionSettings = runCatching {
        val json = JSONObject(AtomicSnapshot.read(snapshot(context)) ?: error("No published connection settings"))
        fromJson(json)
    }.getOrElse { read(context) }

    fun fromJson(json: JSONObject): ConnectionSettings = ConnectionSettings(
        selectedSubscriptionId = json.optString(KEY_SUB).takeIf { it.isNotEmpty() },
        selectedProfileId = json.optString(KEY_PROFILE).takeIf { it.isNotEmpty() },
        routingMode = RoutingMode.entries.firstOrNull { it.name == json.optString(KEY_ROUTING) }
            ?: RoutingMode.GLOBAL,
        serversSort = json.optString(KEY_SERVERS_SORT)
            .let { stored -> ServersSort.entries.firstOrNull { it.name == stored } }
            ?: ServersSort.DEFAULT,
        delayTestUrl = json.optString(KEY_TEST_URL).ifBlank { XrayConfigBuilder.DEFAULT_TEST_URL },
        bypassedApps = json.optJSONArray(KEY_BYPASS_APPS)?.let { apps ->
            (0 until apps.length()).map { apps.getString(it) }.toSet()
        } ?: emptySet(),
        perAppEnabled = json.optBoolean(KEY_PER_APP, true),
        autoReconnect = json.optBoolean(KEY_AUTO_RECONNECT, false),
        bypassLan = json.optBoolean(KEY_BYPASS_LAN, true),
        killSwitch = json.optBoolean(KEY_KILL_SWITCH, true),
        autoConnectUntrustedWifi = json.optBoolean(KEY_AUTO_CONNECT_WIFI, false),
        startOnBoot = json.optBoolean(KEY_START_ON_BOOT, false),
        bypassDomestic = json.optBoolean(KEY_BYPASS_DOMESTIC, true),
        blockAds = json.optBoolean(KEY_BLOCK_ADS, false),
        dnsProvider = json.optString(KEY_DNS_PROVIDER).ifBlank { "DoH · 1.1.1.1" },
        fragmentEnabled = json.optBoolean(KEY_FRAGMENT_ENABLED, false),
        fragmentLength = json.optString(KEY_FRAGMENT_LENGTH).ifBlank { "100-200" },
        fragmentInterval = json.optString(KEY_FRAGMENT_INTERVAL).ifBlank { "10-20" },
        fragmentPackets = json.optString(KEY_FRAGMENT_PACKETS).ifBlank { "tlshello" },
        fragmentMaxSplit = json.optString(KEY_FRAGMENT_MAX_SPLIT),
        muxEnabled = json.optBoolean(KEY_MUX_ENABLED, false),
        muxConcurrency = json.optInt(KEY_MUX_CONCURRENCY, 8).coerceIn(1, 128),
        muxXudpConcurrency = json.optInt(KEY_MUX_XUDP_CONC, 8).coerceIn(1, 128),
        muxXudpQuic = json.optString(KEY_MUX_XUDP_QUIC).ifBlank { "reject" },
        ipv6Enabled = json.optBoolean(KEY_IPV6_ENABLED, false),
        tunMtu = json.optInt(KEY_TUN_MTU, 1500).coerceIn(1280, 9000),
        proxyOnlyMode = json.optBoolean(KEY_PROXY_ONLY, false),
        lanSharingEnabled = json.optBoolean(KEY_LAN_SHARING, false),
        socksPort = json.optInt(KEY_SOCKS_PORT, XrayConfigBuilder.LOCAL_SOCKS_PORT).coerceIn(1024, 65535),
        httpPort = json.optInt(KEY_HTTP_PORT, XrayConfigBuilder.LOCAL_HTTP_PORT).coerceIn(1024, 65535),
        socksUsername = json.optString(KEY_SOCKS_USER),
        socksPassword = json.optString(KEY_SOCKS_PASS),
        fakednsEnabled = json.optBoolean(KEY_FAKEDNS_ENABLED, false),
        localDnsEnabled = json.optBoolean(KEY_LOCAL_DNS_ENABLED, false),
        domainStrategy = json.optString(KEY_DOMAIN_STRATEGY).ifBlank { "AsIs" },
        remoteDns = json.optString(KEY_REMOTE_DNS).ifBlank { "1.1.1.1" },
        domesticDns = json.optString(KEY_DOMESTIC_DNS).ifBlank { "223.5.5.5" },
        customRoutingRulesJson = json.optString(KEY_CUSTOM_RULES),
        smartSwitchEnabled = json.optBoolean(KEY_SMART_SWITCH, false),
        cdnMirrorFallback = json.optBoolean(KEY_CDN_MIRROR, true),
        webdavUrl = json.optString(KEY_WEBDAV_URL),
        webdavUser = json.optString(KEY_WEBDAV_USER),
        webdavPass = json.optString(KEY_WEBDAV_PASS),
    )

    fun toJson(settings: ConnectionSettings): JSONObject = JSONObject()
        .put(KEY_SUB, settings.selectedSubscriptionId.orEmpty())
        .put(KEY_PROFILE, settings.selectedProfileId.orEmpty())
        .put(KEY_ROUTING, settings.routingMode.name)
        .put(KEY_SERVERS_SORT, settings.serversSort.name)
        .put(KEY_TEST_URL, settings.delayTestUrl)
        .put(KEY_BYPASS_APPS, JSONArray(settings.bypassedApps.sorted()))
        .put(KEY_PER_APP, settings.perAppEnabled)
        .put(KEY_AUTO_RECONNECT, settings.autoReconnect)
        .put(KEY_BYPASS_LAN, settings.bypassLan)
        .put(KEY_KILL_SWITCH, settings.killSwitch)
        .put(KEY_AUTO_CONNECT_WIFI, settings.autoConnectUntrustedWifi)
        .put(KEY_START_ON_BOOT, settings.startOnBoot)
        .put(KEY_BYPASS_DOMESTIC, settings.bypassDomestic)
        .put(KEY_BLOCK_ADS, settings.blockAds)
        .put(KEY_DNS_PROVIDER, settings.dnsProvider)
        .put(KEY_FRAGMENT_ENABLED, settings.fragmentEnabled)
        .put(KEY_FRAGMENT_LENGTH, settings.fragmentLength)
        .put(KEY_FRAGMENT_INTERVAL, settings.fragmentInterval)
        .put(KEY_FRAGMENT_PACKETS, settings.fragmentPackets)
        .put(KEY_FRAGMENT_MAX_SPLIT, settings.fragmentMaxSplit)
        .put(KEY_MUX_ENABLED, settings.muxEnabled)
        .put(KEY_MUX_CONCURRENCY, settings.muxConcurrency)
        .put(KEY_MUX_XUDP_CONC, settings.muxXudpConcurrency)
        .put(KEY_MUX_XUDP_QUIC, settings.muxXudpQuic)
        .put(KEY_IPV6_ENABLED, settings.ipv6Enabled)
        .put(KEY_TUN_MTU, settings.tunMtu)
        .put(KEY_PROXY_ONLY, settings.proxyOnlyMode)
        .put(KEY_LAN_SHARING, settings.lanSharingEnabled)
        .put(KEY_SOCKS_PORT, settings.socksPort)
        .put(KEY_HTTP_PORT, settings.httpPort)
        .put(KEY_SOCKS_USER, settings.socksUsername)
        .put(KEY_SOCKS_PASS, settings.socksPassword)
        .put(KEY_FAKEDNS_ENABLED, settings.fakednsEnabled)
        .put(KEY_LOCAL_DNS_ENABLED, settings.localDnsEnabled)
        .put(KEY_DOMAIN_STRATEGY, settings.domainStrategy)
        .put(KEY_REMOTE_DNS, settings.remoteDns)
        .put(KEY_DOMESTIC_DNS, settings.domesticDns)
        .put(KEY_CUSTOM_RULES, settings.customRoutingRulesJson)
        .put(KEY_SMART_SWITCH, settings.smartSwitchEnabled)
        .put(KEY_CDN_MIRROR, settings.cdnMirrorFallback)
        .put(KEY_WEBDAV_URL, settings.webdavUrl)
        .put(KEY_WEBDAV_USER, settings.webdavUser)
        .put(KEY_WEBDAV_PASS, settings.webdavPass)

    fun autoReconnectFresh(context: Context): Boolean = current(context).autoReconnect

    private fun snapshot(context: Context) = File(context.filesDir, "connection-settings.json")

    private fun writeSnapshot(context: Context, settings: ConnectionSettings) {
        AtomicSnapshot.write(snapshot(context), toJson(settings).toString())
    }

    private fun read(context: Context): ConnectionSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ConnectionSettings(
            selectedSubscriptionId = prefs.getString(KEY_SUB, null),
            selectedProfileId = prefs.getString(KEY_PROFILE, null),
            routingMode = prefs.getString(KEY_ROUTING, null)
                ?.let { stored ->
                    RoutingMode.entries.firstOrNull { it.name == stored }
                        ?: if (stored == "RU_BYPASS") RoutingMode.BYPASS_RU else null
                }
                ?: RoutingMode.GLOBAL,
            serversSort = prefs.getString(KEY_SERVERS_SORT, null)
                ?.let { stored -> ServersSort.entries.firstOrNull { it.name == stored } }
                ?: ServersSort.DEFAULT,
            delayTestUrl = prefs.getString(KEY_TEST_URL, null)?.takeIf { it.isNotBlank() }
                ?: XrayConfigBuilder.DEFAULT_TEST_URL,
            bypassedApps = prefs.getStringSet(KEY_BYPASS_APPS, null)?.toSet() ?: emptySet(),
            perAppEnabled = prefs.getBoolean(KEY_PER_APP, true),
            autoReconnect = prefs.getBoolean(KEY_AUTO_RECONNECT, false),
            bypassLan = prefs.getBoolean(KEY_BYPASS_LAN, true),
            killSwitch = prefs.getBoolean(KEY_KILL_SWITCH, true),
            autoConnectUntrustedWifi = prefs.getBoolean(KEY_AUTO_CONNECT_WIFI, false),
            startOnBoot = prefs.getBoolean(KEY_START_ON_BOOT, false),
            bypassDomestic = prefs.getBoolean(KEY_BYPASS_DOMESTIC, true),
            blockAds = prefs.getBoolean(KEY_BLOCK_ADS, false),
            dnsProvider = prefs.getString(KEY_DNS_PROVIDER, null)?.takeIf { it.isNotBlank() }
                ?: "DoH · 1.1.1.1",
            fragmentEnabled = prefs.getBoolean(KEY_FRAGMENT_ENABLED, false),
            fragmentLength = prefs.getString(KEY_FRAGMENT_LENGTH, null)?.takeIf { it.isNotBlank() }
                ?: "100-200",
            fragmentInterval = prefs.getString(KEY_FRAGMENT_INTERVAL, null)?.takeIf { it.isNotBlank() }
                ?: "10-20",
            fragmentPackets = prefs.getString(KEY_FRAGMENT_PACKETS, null)?.takeIf { it.isNotBlank() }
                ?: "tlshello",
            fragmentMaxSplit = prefs.getString(KEY_FRAGMENT_MAX_SPLIT, null).orEmpty(),
            muxEnabled = prefs.getBoolean(KEY_MUX_ENABLED, false),
            muxConcurrency = prefs.getInt(KEY_MUX_CONCURRENCY, 8).coerceIn(1, 128),
            muxXudpConcurrency = prefs.getInt(KEY_MUX_XUDP_CONC, 8).coerceIn(1, 128),
            muxXudpQuic = prefs.getString(KEY_MUX_XUDP_QUIC, null)?.takeIf { it.isNotBlank() } ?: "reject",
            ipv6Enabled = prefs.getBoolean(KEY_IPV6_ENABLED, false),
            tunMtu = prefs.getInt(KEY_TUN_MTU, 1500).coerceIn(1280, 9000),
            proxyOnlyMode = prefs.getBoolean(KEY_PROXY_ONLY, false),
            lanSharingEnabled = prefs.getBoolean(KEY_LAN_SHARING, false),
            socksPort = prefs.getInt(KEY_SOCKS_PORT, XrayConfigBuilder.LOCAL_SOCKS_PORT).coerceIn(1024, 65535),
            httpPort = prefs.getInt(KEY_HTTP_PORT, XrayConfigBuilder.LOCAL_HTTP_PORT).coerceIn(1024, 65535),
            socksUsername = prefs.getString(KEY_SOCKS_USER, null).orEmpty(),
            socksPassword = prefs.getString(KEY_SOCKS_PASS, null).orEmpty(),
            fakednsEnabled = prefs.getBoolean(KEY_FAKEDNS_ENABLED, false),
            localDnsEnabled = prefs.getBoolean(KEY_LOCAL_DNS_ENABLED, false),
            domainStrategy = prefs.getString(KEY_DOMAIN_STRATEGY, null)?.takeIf { it.isNotBlank() } ?: "AsIs",
            remoteDns = prefs.getString(KEY_REMOTE_DNS, null)?.takeIf { it.isNotBlank() } ?: "1.1.1.1",
            domesticDns = prefs.getString(KEY_DOMESTIC_DNS, null)?.takeIf { it.isNotBlank() } ?: "223.5.5.5",
            customRoutingRulesJson = prefs.getString(KEY_CUSTOM_RULES, null).orEmpty(),
            smartSwitchEnabled = prefs.getBoolean(KEY_SMART_SWITCH, false),
            cdnMirrorFallback = prefs.getBoolean(KEY_CDN_MIRROR, true),
            webdavUrl = prefs.getString(KEY_WEBDAV_URL, null).orEmpty(),
            webdavUser = prefs.getString(KEY_WEBDAV_USER, null).orEmpty(),
            webdavPass = prefs.getString(KEY_WEBDAV_PASS, null).orEmpty(),
        )
    }

    private fun write(context: Context, settings: ConnectionSettings) {
        writeSnapshot(context, settings)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .also { editor ->
                val sub = settings.selectedSubscriptionId
                if (sub != null) editor.putString(KEY_SUB, sub) else editor.remove(KEY_SUB)
                val profile = settings.selectedProfileId
                if (profile != null) editor.putString(KEY_PROFILE, profile) else editor.remove(KEY_PROFILE)
                if (settings.bypassedApps.isEmpty()) {
                    editor.remove(KEY_BYPASS_APPS)
                } else {
                    editor.putStringSet(KEY_BYPASS_APPS, settings.bypassedApps)
                }
            }
            .putString(KEY_ROUTING, settings.routingMode.name)
            .putString(KEY_SERVERS_SORT, settings.serversSort.name)
            .putString(KEY_TEST_URL, settings.delayTestUrl)
            .putBoolean(KEY_PER_APP, settings.perAppEnabled)
            .putBoolean(KEY_AUTO_RECONNECT, settings.autoReconnect)
            .putBoolean(KEY_BYPASS_LAN, settings.bypassLan)
            .putBoolean(KEY_KILL_SWITCH, settings.killSwitch)
            .putBoolean(KEY_AUTO_CONNECT_WIFI, settings.autoConnectUntrustedWifi)
            .putBoolean(KEY_START_ON_BOOT, settings.startOnBoot)
            .putBoolean(KEY_BYPASS_DOMESTIC, settings.bypassDomestic)
            .putBoolean(KEY_BLOCK_ADS, settings.blockAds)
            .putString(KEY_DNS_PROVIDER, settings.dnsProvider)
            .putBoolean(KEY_FRAGMENT_ENABLED, settings.fragmentEnabled)
            .putString(KEY_FRAGMENT_LENGTH, settings.fragmentLength)
            .putString(KEY_FRAGMENT_INTERVAL, settings.fragmentInterval)
            .putString(KEY_FRAGMENT_PACKETS, settings.fragmentPackets)
            .putString(KEY_FRAGMENT_MAX_SPLIT, settings.fragmentMaxSplit)
            .putBoolean(KEY_MUX_ENABLED, settings.muxEnabled)
            .putInt(KEY_MUX_CONCURRENCY, settings.muxConcurrency)
            .putInt(KEY_MUX_XUDP_CONC, settings.muxXudpConcurrency)
            .putString(KEY_MUX_XUDP_QUIC, settings.muxXudpQuic)
            .putBoolean(KEY_IPV6_ENABLED, settings.ipv6Enabled)
            .putInt(KEY_TUN_MTU, settings.tunMtu)
            .putBoolean(KEY_PROXY_ONLY, settings.proxyOnlyMode)
            .putBoolean(KEY_LAN_SHARING, settings.lanSharingEnabled)
            .putInt(KEY_SOCKS_PORT, settings.socksPort)
            .putInt(KEY_HTTP_PORT, settings.httpPort)
            .putString(KEY_SOCKS_USER, settings.socksUsername)
            .putString(KEY_SOCKS_PASS, settings.socksPassword)
            .putBoolean(KEY_FAKEDNS_ENABLED, settings.fakednsEnabled)
            .putBoolean(KEY_LOCAL_DNS_ENABLED, settings.localDnsEnabled)
            .putString(KEY_DOMAIN_STRATEGY, settings.domainStrategy)
            .putString(KEY_REMOTE_DNS, settings.remoteDns)
            .putString(KEY_DOMESTIC_DNS, settings.domesticDns)
            .putString(KEY_CUSTOM_RULES, settings.customRoutingRulesJson)
            .putBoolean(KEY_SMART_SWITCH, settings.smartSwitchEnabled)
            .putBoolean(KEY_CDN_MIRROR, settings.cdnMirrorFallback)
            .putString(KEY_WEBDAV_URL, settings.webdavUrl)
            .putString(KEY_WEBDAV_USER, settings.webdavUser)
            .putString(KEY_WEBDAV_PASS, settings.webdavPass)
            .apply()
    }

    private const val KEY_SUB = "selected_subscription"
    private const val KEY_PROFILE = "selected_profile"
    private const val KEY_ROUTING = "routing_mode"
    private const val KEY_SERVERS_SORT = "servers_sort"
    private const val KEY_TEST_URL = "delay_test_url"
    private const val KEY_BYPASS_APPS = "bypassed_apps"
    private const val KEY_PER_APP = "per_app_enabled"
    private const val KEY_AUTO_RECONNECT = "auto_reconnect"
    private const val KEY_BYPASS_LAN = "bypass_lan"
    private const val KEY_KILL_SWITCH = "kill_switch"
    private const val KEY_AUTO_CONNECT_WIFI = "auto_connect_wifi"
    private const val KEY_START_ON_BOOT = "start_on_boot"
    private const val KEY_BYPASS_DOMESTIC = "bypass_domestic"
    private const val KEY_BLOCK_ADS = "block_ads"
    private const val KEY_DNS_PROVIDER = "dns_provider"
    private const val KEY_FRAGMENT_ENABLED = "fragment_enabled"
    private const val KEY_FRAGMENT_LENGTH = "fragment_length"
    private const val KEY_FRAGMENT_INTERVAL = "fragment_interval"
    private const val KEY_FRAGMENT_PACKETS = "fragment_packets"
    private const val KEY_FRAGMENT_MAX_SPLIT = "fragment_max_split"
    private const val KEY_MUX_ENABLED = "mux_enabled"
    private const val KEY_MUX_CONCURRENCY = "mux_concurrency"
    private const val KEY_MUX_XUDP_CONC = "mux_xudp_conc"
    private const val KEY_MUX_XUDP_QUIC = "mux_xudp_quic"
    private const val KEY_IPV6_ENABLED = "ipv6_enabled"
    private const val KEY_TUN_MTU = "tun_mtu"
    private const val KEY_PROXY_ONLY = "proxy_only_mode"
    private const val KEY_LAN_SHARING = "lan_sharing_enabled"
    private const val KEY_SOCKS_PORT = "socks_port"
    private const val KEY_HTTP_PORT = "http_port"
    private const val KEY_SOCKS_USER = "socks_username"
    private const val KEY_SOCKS_PASS = "socks_password"
    private const val KEY_FAKEDNS_ENABLED = "fakedns_enabled"
    private const val KEY_LOCAL_DNS_ENABLED = "local_dns_enabled"
    private const val KEY_DOMAIN_STRATEGY = "domain_strategy"
    private const val KEY_REMOTE_DNS = "remote_dns"
    private const val KEY_DOMESTIC_DNS = "domestic_dns"
    private const val KEY_CUSTOM_RULES = "custom_routing_rules"
    private const val KEY_SMART_SWITCH = "smart_switch_enabled"
    private const val KEY_CDN_MIRROR = "cdn_mirror_fallback"
    private const val KEY_WEBDAV_URL = "webdav_url"
    private const val KEY_WEBDAV_USER = "webdav_user"
    private const val KEY_WEBDAV_PASS = "webdav_pass"
}
