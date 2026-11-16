package slop.miho.chumpy.xray

import android.app.Application
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import slop.miho.chumpy.core.SmartSwitchEngine
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.RoutingRuleAction
import slop.miho.chumpy.data.RoutingRuleMatchType
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.sub.LinkParsers
import slop.miho.chumpy.sub.LinkSerializer

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class V2rayVFeaturesTest {
    private val sampleVless = ProfileEntity(
        id = "v1",
        subscriptionId = null,
        type = ProfileType.VLESS,
        remarks = "Tokyo-VLESS",
        server = "vless.example.com",
        serverPort = 443,
        password = "b831381d-6324-4d53-ad4f-8cda48b30811",
        network = "ws",
        path = "/ws",
        host = "cdn.example.com",
        security = "tls",
        sni = "cdn.example.com",
        fingerprint = "chrome",
    )

    @Test
    fun `link serializer generates parseable URIs across protocols`() {
        val candidates = listOf(
            sampleVless,
            ProfileEntity(
                id = "vm1",
                subscriptionId = null,
                type = ProfileType.VMESS,
                remarks = "HK-VMess",
                server = "vmess.example.com",
                serverPort = 8443,
                password = "b831381d-6324-4d53-ad4f-8cda48b30811",
                method = "auto",
                network = "ws",
                path = "/v",
                security = "tls",
                sni = "vmess.example.com",
            ),
            ProfileEntity(
                id = "tr1",
                subscriptionId = null,
                type = ProfileType.TROJAN,
                remarks = "SG-Trojan",
                server = "trojan.example.com",
                serverPort = 443,
                password = "secret-password",
                security = "tls",
                sni = "trojan.example.com",
            ),
            ProfileEntity(
                id = "ss1",
                subscriptionId = null,
                type = ProfileType.SHADOWSOCKS,
                remarks = "DE-SS",
                server = "ss.example.com",
                serverPort = 8388,
                method = "2022-blake3-aes-128-gcm",
                password = "key123",
            ),
            ProfileEntity(
                id = "hy1",
                subscriptionId = null,
                type = ProfileType.HYSTERIA2,
                remarks = "US-Hy2",
                server = "hy2.example.com",
                serverPort = 443,
                password = "hy2-auth",
                sni = "hy2.example.com",
                portHopping = "20000-30000",
            ),
            ProfileEntity(
                id = "wg1",
                subscriptionId = null,
                type = ProfileType.WIREGUARD,
                remarks = "NL-WG",
                server = "wg.example.com",
                serverPort = 51820,
                wgSecretKey = "privkey",
                publicKey = "pubkey",
                wgLocalAddress = "10.0.0.2/32",
                wgMtu = 1420,
            ),
            ProfileEntity(
                id = "so1",
                subscriptionId = null,
                type = ProfileType.SOCKS,
                remarks = "Local-Socks",
                server = "127.0.0.1",
                serverPort = 1080,
                username = "user",
                password = "pw",
            ),
            ProfileEntity(
                id = "ht1",
                subscriptionId = null,
                type = ProfileType.HTTP,
                remarks = "Corp-HTTP",
                server = "proxy.example.com",
                serverPort = 8080,
                username = "u",
                password = "p",
            ),
        )

        candidates.forEach { entity ->
            val uri = LinkSerializer.toShareUri(entity)
            assertTrue("URI for ${entity.type} should not be blank", uri.isNotBlank())
            val parsed = LinkParsers.parse(uri)
            assertNotNull("LinkParsers should round-trip $uri", parsed)
            assertEquals(entity.type, parsed!!.type)
            assertEquals(entity.server, parsed.server)
            assertEquals(entity.serverPort, parsed.serverPort)
        }
    }

    @Test
    fun `smart switch triggers after 8 dead samples, resets on rx, and enforces cooldown and hourly cap`() {
        val engine = SmartSwitchEngine(stallThreshold = 8, cooldownMillis = 45_000L, maxSwitchesPerHour = 4)
        var t = 1_000_000L
        // 7 stalls followed by healthy rx should not trigger
        repeat(7) {
            assertFalse(engine.recordSample(rxBytesPerSec = 100L, txBytesPerSec = 5_000L, nowMillis = t))
            t += 1_000L
        }
        assertFalse(engine.recordSample(rxBytesPerSec = 4_096L, txBytesPerSec = 5_000L, nowMillis = t))
        t += 1_000L

        // 8 consecutive stalls triggers failover #1
        repeat(7) {
            assertFalse(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))
            t += 1_000L
        }
        assertTrue(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))

        // Inside 45s cooldown -> must NOT trigger
        t += 10_000L
        repeat(8) {
            assertFalse(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))
            t += 1_000L
        }

        // After cooldown -> failover #2, #3, #4 succeed; #5 within the hour is capped
        repeat(3) {
            t += 50_000L
            repeat(7) {
                assertFalse(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))
                t += 1_000L
            }
            assertTrue(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))
        }
        t += 50_000L
        repeat(8) {
            assertFalse(engine.recordSample(rxBytesPerSec = 0L, txBytesPerSec = 3_000L, nowMillis = t))
            t += 1_000L
        }
    }

    @Test
    fun `smart switch candidate selector prefers working lowest latency then highest speed`() {
        val current = sampleVless.copy(id = "cur")
        val dead = sampleVless.copy(id = "dead", testDelayMillis = -1L)
        val slow = sampleVless.copy(id = "slow", testDelayMillis = 200L, speedMbps = 80.0)
        val fast = sampleVless.copy(id = "fast", testDelayMillis = 60L, speedMbps = 20.0)
        val hidden = sampleVless.copy(id = "hid", testDelayMillis = 5L, hidden = true)

        val picked = SmartSwitchEngine.pickNextCandidate(listOf(current, dead, slow, fast, hidden), currentId = "cur")
        assertEquals("fast", picked?.id)

        val onlyDead = SmartSwitchEngine.pickNextCandidate(listOf(current, hidden), currentId = "cur")
        assertNull(onlyDead)
    }

    @Test
    fun `xray config builder wires extended features, custom rules, proxy chain, lan share, and fakedns`() {
        val hop = ProfileEntity(
            id = "hop",
            subscriptionId = null,
            type = ProfileType.SOCKS,
            remarks = "Relay",
            server = "198.51.100.2",
            serverPort = 1080,
        )
        val rules = listOf(
            CustomRoutingRule(
                id = "r1",
                label = "Corp direct",
                matchType = RoutingRuleMatchType.DOMAIN,
                pattern = "domain:corp.internal",
                action = RoutingRuleAction.DIRECT,
            ),
            CustomRoutingRule(
                id = "r2",
                label = "Disable check",
                matchType = RoutingRuleMatchType.IP,
                pattern = "1.2.3.4/32",
                action = RoutingRuleAction.BLOCK,
                enabled = false,
            ),
        )
        val parsedRules = CustomRoutingRule.listFromJson(CustomRoutingRule.listToJson(rules))
        assertEquals(2, parsedRules.size)

        val jsonStr = XrayConfigBuilder.buildFull(
            profile = sampleVless.copy(dialerProxyId = hop.id),
            mode = RoutingMode.GLOBAL,
            chainParent = hop,
            lanSharingEnabled = true,
            proxyOnlyMode = true,
            socksPort = 20808,
            httpPort = 20809,
            fakednsEnabled = true,
            fragmentEnabled = true,
            fragmentPackets = "1-3",
            fragmentMaxSplit = "512",
            muxEnabled = true,
            muxXudpConcurrency = 16,
            muxXudpQuic = "allow",
            customRules = parsedRules,
        )
        val root = Json.parseToJsonElement(jsonStr).jsonObject

        // Inbounds: proxyOnlyMode omits tun; lanSharingEnabled binds 0.0.0.0 on custom ports
        val inbounds = root["inbounds"]!!.jsonArray.map { it.jsonObject }
        assertEquals(2, inbounds.size)
        assertTrue(inbounds.none { it["tag"]?.jsonPrimitive?.content == XrayConfigBuilder.TUN_TAG })
        val socks = inbounds.first { it["tag"]?.jsonPrimitive?.content == XrayConfigBuilder.SOCKS_TAG }
        assertEquals("0.0.0.0", socks["listen"]!!.jsonPrimitive.content)
        assertEquals(20808, socks["port"]!!.jsonPrimitive.content.toInt())

        // Outbounds: chain hop outbound + proxy outbound with dialerProxy + fragment + mux
        val outbounds = root["outbounds"]!!.jsonArray.map { it.jsonObject }
        val proxyOut = outbounds.first { it["tag"]?.jsonPrimitive?.content == XrayConfigBuilder.PROXY_TAG }
        assertEquals(
            XrayConfigBuilder.CHAIN_HOP_TAG,
            proxyOut["streamSettings"]!!.jsonObject["sockopt"]!!.jsonObject["dialerProxy"]!!.jsonPrimitive.content,
        )
        assertTrue(outbounds.any { it["tag"]?.jsonPrimitive?.content == XrayConfigBuilder.CHAIN_HOP_TAG })

        // FakeDNS present
        assertTrue(root.containsKey("fakedns"))

        // Enabled custom rule included, disabled custom rule omitted
        val routingRules = root["routing"]!!.jsonObject["rules"]!!.jsonArray.map { it.jsonObject }
        assertTrue(routingRules.any {
            it["domain"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content == "domain:corp.internal" &&
                it["outboundTag"]?.jsonPrimitive?.content == XrayConfigBuilder.DIRECT_TAG
        })
        assertFalse(routingRules.any {
            it["ip"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content == "1.2.3.4/32"
        })
    }
}
