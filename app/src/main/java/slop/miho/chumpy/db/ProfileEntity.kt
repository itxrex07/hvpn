package slop.miho.chumpy.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest

/** Proxy protocol kinds a profile can carry; the name is what the Xray config uses. */
enum class ProfileType {
    VMESS,
    VLESS,
    TROJAN,
    SHADOWSOCKS,
    SOCKS,
    HTTP,
    WIREGUARD,
    HYSTERIA2,
    CUSTOM_JSON,

    /** Not a server: a routing balancer over the subscription's other profiles. */
    BALANCER,
}

/**
 * One server, imported from a subscription link or a manual paste. The columns follow the
 * link formats' own vocabulary (transport in `network`/`host`/`path`, security in
 * `security`/`sni`/…), flat and denormalized on purpose: a profile is always read whole,
 * and updates replace the whole set of rows for a subscription.
 */
@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("subscriptionId")],
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    /** Owning subscription, or null for a manually pasted profile. */
    val subscriptionId: String?,
    val type: ProfileType,
    val remarks: String = "",
    /** Position within the subscription, kept stable across updates. */
    val order: Long = 0,
    /**
     * The share link the profile was born from, verbatim — the row menu's copy hands
     * it back word for word, the way it came in. Null for everything that is not a
     * link: raw configs, Clash files, balancers.
     */
    val sourceLink: String? = null,

    // Endpoint and authentication.
    val server: String = "",
    val serverPort: Int = 0,
    /** VMess UUID, VLESS id, Trojan/SS/Hy2 password, SOCKS/HTTP pass. */
    val password: String = "",
    /** SS cipher; VMess security; VLESS encryption. */
    val method: String = "",
    /** SOCKS / HTTP user. */
    val username: String = "",

    // Transport.
    val network: String = "tcp",
    val headerType: String = "",
    val host: String = "",
    val path: String = "",
    /** KCP seed. */
    val seed: String = "",
    /** gRPC service name. */
    val serviceName: String = "",
    /** gRPC authority. */
    val authority: String = "",
    /** gRPC multi / XHTTP mode. */
    val mode: String = "",
    /** XHTTP extra JSON, passed through verbatim. */
    val xhttpExtra: String? = null,

    // TLS / REALITY.
    /** "", "tls" or "reality". */
    val security: String = "",
    val sni: String = "",
    /** Comma-separated ALPN list, as links carry it. */
    val alpn: String = "",
    val fingerprint: String = "",
    val flow: String = "",
    val allowInsecure: Boolean = false,
    /** REALITY public key (WireGuard peer key on WIREGUARD profiles). */
    val publicKey: String = "",
    val shortId: String = "",
    val spiderX: String = "",

    // Hysteria2.
    val obfsPassword: String = "",
    val portHopping: String = "",
    val portHoppingInterval: String = "",

    // WireGuard.
    val wgSecretKey: String = "",
    val wgPresharedKey: String = "",
    /** Comma-separated interface addresses. */
    val wgLocalAddress: String = "",
    val wgMtu: Int = 0,
    /** "1,2,3"-style client reserved bytes. */
    val wgReserved: String = "",

    // Balancer (BALANCER profiles only).
    /** The Xray strategy: random, roundrobin, leastping or leastload. */
    val balancerStrategy: String = "",
    /** The members' linkKeys, newline-joined; resolved against the subscription's rows. */
    val balancerMembers: String = "",

    // Routing (profiles imported from raw configs).
    val routingRules: String = "",
    val geodataSources: String = "",

    val hidden: Boolean = false,

    // Measurements.
    /** The last real ping through the core, in millis; null when never measured. */
    val testDelayMillis: Long? = null,
    val lastTestAt: Long? = null,
    /** Measured single-stream throughput in Mbps; null when not measured. */
    val speedMbps: Double? = null,
    val lastUsedAt: Long? = null,

    val pinned: Boolean = false,
    val concealed: Boolean = false,

    /** Optional parent profile ID to chain through (Xray sockopt.dialerProxy). */
    val dialerProxyId: String = "",
    /** Full or outbound raw Xray JSON for CUSTOM_JSON profiles. */
    val rawConfigJson: String = "",

    val linkKey: String = "",
)

/** The canonical digest used for update diffing; see [linkKey]. */
fun ProfileEntity.computeLinkKey(): String {
    val canonical = when (type) {
        ProfileType.BALANCER -> listOf(type.name, remarks, balancerStrategy)
        ProfileType.CUSTOM_JSON -> listOf(type.name, remarks, rawConfigJson)
        else -> listOf(
            type.name,
            server,
            serverPort.toString(),
            password,
            method,
            username,
            network,
            headerType,
            host,
            path,
            seed,
            serviceName,
            authority,
            mode,
            xhttpExtra.orEmpty(),
            security,
            sni,
            alpn,
            fingerprint,
            flow,
            allowInsecure.toString(),
            publicKey,
            shortId,
            spiderX,
            obfsPassword,
            portHopping,
            portHoppingInterval,
            wgSecretKey,
            wgPresharedKey,
            wgLocalAddress,
            wgMtu.toString(),
            wgReserved,
            dialerProxyId,
        )
    }.joinToString("\u0001")
    val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

/** The balancer's member linkKeys, in column order. */
val ProfileEntity.balancerMemberKeys: List<String>
    get() = balancerMembers.split('\n').filter { it.isNotBlank() }
