package slop.miho.chumpy.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import slop.miho.chumpy.xray.RoutingMode
import slop.miho.chumpy.xray.XrayConfigBuilder
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Connection preferences — the selected subscription and profile, the routing mode and
 * the delay test URL. Kept apart from [SettingsStore] (the interface's own settings) the
 * same way the connection screens are apart from the interface ones.
 */
data class ConnectionSettings(
    /** The subscription whose servers the servers screen shows; null until one exists. */
    val selectedSubscriptionId: String? = null,
    /** The profile a connect uses; restored across restarts. */
    val selectedProfileId: String? = null,
    val routingMode: RoutingMode = RoutingMode.GLOBAL,
    /** The servers list's own order — the header's chip picks it, and it sticks. */
    val serversSort: ServersSort = ServersSort.DEFAULT,
    /** The URL real delay measurements fetch through the tested outbound. */
    val delayTestUrl: String = XrayConfigBuilder.DEFAULT_TEST_URL,
    /** Packages the tunnel leaves alone — the per-app bypass list, v2rayNG's arrangement. */
    val bypassedApps: Set<String> = emptySet(),
    /** The per-app proxy's own master switch: off, and the whole list stops applying. */
    val perAppEnabled: Boolean = true,
    /**
     * Whether the system's own demand for the VPN — the always-on restart, the sticky
     * resurrection after the core process died — brings the tunnel back by itself. Off
     * (the default): a fall stands, the alert notification's Reconnect being the way
     * back by the user's own hand.
     */
    val autoReconnect: Boolean = false,
    /**
     * Whether the local network stays out of the tunnel — the LAN-friendly default.
     * On, the tunnel claims the public internet only and LAN traffic rides the
     * physical network as if the VPN were off; off, the one 0.0.0.0/0 route swallows
     * everything again. The core reads it fresh off disk when a tunnel starts.
     */
    val bypassLan: Boolean = true,
    /** Block system traffic when VPN drops unexpectedly. */
    val killSwitch: Boolean = true,
    /** Automatically connect on untrusted Wi-Fi networks. */
    val autoConnectUntrustedWifi: Boolean = false,
    /** Start VPN tunnel on device boot. */
    val startOnBoot: Boolean = false,
    /** Bypass domestic sites (geosite:pk -> direct). */
    val bypassDomestic: Boolean = true,
    /** Block ads & trackers (geosite:category-ads-all -> block). */
    val blockAds: Boolean = false,
    /** Selected DNS provider label (e.g. "DoH · 1.1.1.1"). */
    val dnsProvider: String = "DoH · 1.1.1.1",
    /** TLS Fragmentation & Mux settings. */
    val fragmentEnabled: Boolean = false,
    val fragmentLength: String = "100-200",
    val fragmentInterval: String = "10-20",
    val muxEnabled: Boolean = false,
    val muxConcurrency: Int = 8,
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

    /**
     * The settings straight off disk. The VPN service reads the bypass list from the
     * :core process, whose in-memory copy would go stale the moment the UI process
     * writes a new one.
     */
    fun current(context: Context): ConnectionSettings = runCatching {
        val json = JSONObject(AtomicSnapshot.read(snapshot(context)) ?: error("No published connection settings"))
        ConnectionSettings(
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
            muxEnabled = json.optBoolean(KEY_MUX_ENABLED, false),
            muxConcurrency = json.optInt(KEY_MUX_CONCURRENCY, 8),
        )
    }.getOrElse { read(context) }

    /**
     * The auto-reconnect flag read straight off the prefs *file*, not this process's
     * SharedPreferences cache — which never sees the UI process's writes. The flag rules
     * the mid-flight decisions (a handover re-dials or falls by it), so it must be the
     * value the user last set, not the one this process loaded at birth. Absent from the
     * file, it is the default: off.
     */
    fun autoReconnectFresh(context: Context): Boolean {
        return current(context).autoReconnect
    }

    private fun snapshot(context: Context) = File(context.filesDir, "connection-settings.json")

    private fun writeSnapshot(context: Context, settings: ConnectionSettings) {
        val json = JSONObject()
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
            .put(KEY_MUX_ENABLED, settings.muxEnabled)
            .put(KEY_MUX_CONCURRENCY, settings.muxConcurrency)
        AtomicSnapshot.write(snapshot(context), json.toString())
    }

    private fun read(context: Context): ConnectionSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ConnectionSettings(
            selectedSubscriptionId = prefs.getString(KEY_SUB, null),
            selectedProfileId = prefs.getString(KEY_PROFILE, null),
            routingMode = prefs.getString(KEY_ROUTING, null)
                // "RU_BYPASS" is the old spelling of the Russia bypass from before the
                // region list grew; carried over so an update keeps the pick.
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
            muxEnabled = prefs.getBoolean(KEY_MUX_ENABLED, false),
            muxConcurrency = prefs.getInt(KEY_MUX_CONCURRENCY, 8),
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
            .putBoolean(KEY_MUX_ENABLED, settings.muxEnabled)
            .putInt(KEY_MUX_CONCURRENCY, settings.muxConcurrency)
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
    private const val KEY_MUX_ENABLED = "mux_enabled"
    private const val KEY_MUX_CONCURRENCY = "mux_concurrency"
}
