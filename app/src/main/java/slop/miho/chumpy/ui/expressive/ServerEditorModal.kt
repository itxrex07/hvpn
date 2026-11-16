package slop.miho.chumpy.ui.expressive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.db.balancerMemberKeys
import java.util.UUID

/**
 * Full manual server creator and editor for VLESS, VMess, Trojan, Shadowsocks,
 * Hysteria2, WireGuard, SOCKS, HTTP, Custom Xray JSON, and Proxy Chaining.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerEditorModal(
    isStyleB: Boolean,
    editingProfile: ProfileEntity?,
    availableChainParents: List<ProfileEntity>,
    onSave: (ProfileEntity, (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val dangerColor = if (isStyleB) SarhadColors.Pink else KiranColors.Orange
    val switchOff = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

    val supportedTypes = remember {
        listOf(
            ProfileType.VLESS,
            ProfileType.VMESS,
            ProfileType.TROJAN,
            ProfileType.SHADOWSOCKS,
            ProfileType.HYSTERIA2,
            ProfileType.WIREGUARD,
            ProfileType.SOCKS,
            ProfileType.HTTP,
            ProfileType.CUSTOM_JSON,
        )
    }

    var type by remember(editingProfile) {
        mutableStateOf(
            editingProfile?.type?.takeIf { it in supportedTypes } ?: ProfileType.VLESS,
        )
    }
    var remarks by remember(editingProfile) { mutableStateOf(editingProfile?.remarks.orEmpty()) }
    var server by remember(editingProfile) { mutableStateOf(editingProfile?.server.orEmpty()) }
    var portText by remember(editingProfile) {
        mutableStateOf((editingProfile?.serverPort?.takeIf { it > 0 } ?: 443).toString())
    }
    var password by remember(editingProfile) { mutableStateOf(editingProfile?.password.orEmpty()) }
    var username by remember(editingProfile) { mutableStateOf(editingProfile?.username.orEmpty()) }
    var method by remember(editingProfile) {
        mutableStateOf(
            editingProfile?.method.orEmpty().ifBlank {
                when (editingProfile?.type) {
                    ProfileType.VMESS -> "auto"
                    ProfileType.SHADOWSOCKS -> "2022-blake3-aes-128-gcm"
                    else -> "none"
                }
            },
        )
    }
    var network by remember(editingProfile) {
        mutableStateOf(editingProfile?.network?.ifBlank { "tcp" } ?: "tcp")
    }
    var host by remember(editingProfile) { mutableStateOf(editingProfile?.host.orEmpty()) }
    var path by remember(editingProfile) { mutableStateOf(editingProfile?.path.orEmpty()) }
    var serviceName by remember(editingProfile) { mutableStateOf(editingProfile?.serviceName.orEmpty()) }
    var mode by remember(editingProfile) { mutableStateOf(editingProfile?.mode.orEmpty()) }
    var security by remember(editingProfile) { mutableStateOf(editingProfile?.security.orEmpty()) }
    var sni by remember(editingProfile) { mutableStateOf(editingProfile?.sni.orEmpty()) }
    var alpn by remember(editingProfile) { mutableStateOf(editingProfile?.alpn.orEmpty()) }
    var fingerprint by remember(editingProfile) { mutableStateOf(editingProfile?.fingerprint.orEmpty()) }
    var flow by remember(editingProfile) { mutableStateOf(editingProfile?.flow.orEmpty()) }
    var allowInsecure by remember(editingProfile) { mutableStateOf(editingProfile?.allowInsecure ?: false) }
    var publicKey by remember(editingProfile) { mutableStateOf(editingProfile?.publicKey.orEmpty()) }
    var shortId by remember(editingProfile) { mutableStateOf(editingProfile?.shortId.orEmpty()) }
    var spiderX by remember(editingProfile) { mutableStateOf(editingProfile?.spiderX.orEmpty()) }
    var obfsPassword by remember(editingProfile) { mutableStateOf(editingProfile?.obfsPassword.orEmpty()) }
    var portHopping by remember(editingProfile) { mutableStateOf(editingProfile?.portHopping.orEmpty()) }
    var wgSecretKey by remember(editingProfile) { mutableStateOf(editingProfile?.wgSecretKey.orEmpty()) }
    var wgPresharedKey by remember(editingProfile) { mutableStateOf(editingProfile?.wgPresharedKey.orEmpty()) }
    var wgLocalAddress by remember(editingProfile) {
        mutableStateOf(editingProfile?.wgLocalAddress.orEmpty().ifBlank { "10.0.0.2/32" })
    }
    var wgMtuText by remember(editingProfile) {
        mutableStateOf((editingProfile?.wgMtu?.takeIf { it > 0 } ?: 1420).toString())
    }
    var wgReserved by remember(editingProfile) { mutableStateOf(editingProfile?.wgReserved.orEmpty()) }
    var dialerProxyId by remember(editingProfile) { mutableStateOf(editingProfile?.dialerProxyId.orEmpty()) }
    var rawConfigJson by remember(editingProfile) { mutableStateOf(editingProfile?.rawConfigJson.orEmpty()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Text(
                text = if (editingProfile == null) "New Server" else "Edit Server",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Configure endpoint, transport, TLS/Reality, WireGuard, proxy chaining, or raw Xray JSON.",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(14.dp))

            // Protocol chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                supportedTypes.forEach { pt ->
                    val active = type == pt
                    EditorChip(
                        label = when (pt) {
                            ProfileType.CUSTOM_JSON -> "Raw JSON"
                            ProfileType.SHADOWSOCKS -> "SS"
                            ProfileType.HYSTERIA2 -> "Hy2"
                            ProfileType.WIREGUARD -> "WG"
                            else -> pt.name
                        },
                        active = active,
                        primaryColor = primaryColor,
                        onPrimaryColor = onPrimaryColor,
                        inactiveBg = fieldBg,
                        textColor = textColor,
                    ) {
                        type = pt
                        if (pt == ProfileType.HYSTERIA2) {
                            network = "hysteria"
                            security = "tls"
                        } else if (network == "hysteria") {
                            network = "tcp"
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            EditorTextField("Remarks / Name", remarks, "e.g. Frankfurt Edge", fieldBg, textColor, mutedColor) { remarks = it }

            if (type == ProfileType.CUSTOM_JSON) {
                EditorMultilineField(
                    label = "Raw Xray JSON Config",
                    value = rawConfigJson,
                    hint = "{\"outbounds\":[...]} or full Xray JSON",
                    fieldBg = fieldBg,
                    textColor = textColor,
                    mutedColor = mutedColor,
                ) { rawConfigJson = it }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(2f)) {
                        EditorTextField("Server Host / IP", server, "example.com", fieldBg, textColor, mutedColor) { server = it }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        EditorTextField("Port", portText, "443", fieldBg, textColor, mutedColor, keyboardType = KeyboardType.Number) { portText = it }
                    }
                }

                when (type) {
                    ProfileType.VLESS -> {
                        EditorTextField("UUID", password, "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", fieldBg, textColor, mutedColor) { password = it }
                        EditorTextField("Flow (optional)", flow, "xtls-rprx-vision", fieldBg, textColor, mutedColor) { flow = it }
                    }
                    ProfileType.VMESS -> {
                        EditorTextField("UUID", password, "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", fieldBg, textColor, mutedColor) { password = it }
                        EditorTextField("Security Cipher", method, "auto / aes-128-gcm / chacha20-poly1305", fieldBg, textColor, mutedColor) { method = it }
                    }
                    ProfileType.TROJAN -> {
                        EditorTextField("Password", password, "trojan password", fieldBg, textColor, mutedColor) { password = it }
                    }
                    ProfileType.SHADOWSOCKS -> {
                        EditorTextField("Cipher Method", method, "2022-blake3-aes-128-gcm / aes-256-gcm", fieldBg, textColor, mutedColor) { method = it }
                        EditorTextField("Password", password, "ss password", fieldBg, textColor, mutedColor) { password = it }
                    }
                    ProfileType.HYSTERIA2 -> {
                        EditorTextField("Auth Password", password, "hy2 password", fieldBg, textColor, mutedColor) { password = it }
                        EditorTextField("Obfs Salamander Password (optional)", obfsPassword, "leave empty if none", fieldBg, textColor, mutedColor) { obfsPassword = it }
                        EditorTextField("Port Hopping Range (optional)", portHopping, "e.g. 20000-50000", fieldBg, textColor, mutedColor) { portHopping = it }
                    }
                    ProfileType.WIREGUARD -> {
                        EditorTextField("Interface Private Key", wgSecretKey, "base64 private key", fieldBg, textColor, mutedColor) { wgSecretKey = it }
                        EditorTextField("Peer Public Key", publicKey, "base64 public key", fieldBg, textColor, mutedColor) { publicKey = it }
                        EditorTextField("Preshared Key (optional)", wgPresharedKey, "optional base64", fieldBg, textColor, mutedColor) { wgPresharedKey = it }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(2f)) {
                                EditorTextField("Local Address(es)", wgLocalAddress, "10.0.0.2/32", fieldBg, textColor, mutedColor) { wgLocalAddress = it }
                            }
                            Box(Modifier.weight(1f)) {
                                EditorTextField("MTU", wgMtuText, "1420", fieldBg, textColor, mutedColor, KeyboardType.Number) { wgMtuText = it }
                            }
                        }
                        EditorTextField("Reserved Bytes (optional)", wgReserved, "e.g. 1,2,3", fieldBg, textColor, mutedColor) { wgReserved = it }
                    }
                    ProfileType.SOCKS, ProfileType.HTTP -> {
                        EditorTextField("Username (optional)", username, "optional", fieldBg, textColor, mutedColor) { username = it }
                        EditorTextField("Password (optional)", password, "optional", fieldBg, textColor, mutedColor) { password = it }
                    }
                    else -> {}
                }

                if (type in listOf(ProfileType.VLESS, ProfileType.VMESS, ProfileType.TROJAN, ProfileType.SHADOWSOCKS)) {
                    Spacer(Modifier.height(6.dp))
                    Text("Transport", fontFamily = OutfitFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedColor)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("tcp", "ws", "grpc", "httpupgrade", "xhttp", "kcp").forEach { net ->
                            EditorChip(net.uppercase(), network == net, primaryColor, onPrimaryColor, fieldBg, textColor) {
                                network = net
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    if (network in listOf("ws", "httpupgrade", "xhttp")) {
                        EditorTextField("Host Header", host, "cdn.example.com", fieldBg, textColor, mutedColor) { host = it }
                        EditorTextField("Path", path, "/ws", fieldBg, textColor, mutedColor) { path = it }
                    } else if (network == "grpc") {
                        EditorTextField("gRPC Service Name", serviceName, "grpc-service", fieldBg, textColor, mutedColor) { serviceName = it }
                        EditorTextField("gRPC Mode (gun / multi)", mode, "gun", fieldBg, textColor, mutedColor) { mode = it }
                    }
                }

                if (type in listOf(ProfileType.VLESS, ProfileType.VMESS, ProfileType.TROJAN, ProfileType.HYSTERIA2, ProfileType.HTTP)) {
                    Spacer(Modifier.height(6.dp))
                    Text("Security / TLS", fontFamily = OutfitFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedColor)
                    Spacer(Modifier.height(6.dp))
                    val secOptions = if (type == ProfileType.HYSTERIA2) listOf("tls") else listOf("", "tls", "reality")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        secOptions.forEach { sec ->
                            val label = if (sec.isEmpty()) "None" else sec.uppercase()
                            EditorChip(label, security == sec, primaryColor, onPrimaryColor, fieldBg, textColor) {
                                security = sec
                            }
                        }
                    }
                    if (security == "tls" || security == "reality") {
                        Spacer(Modifier.height(6.dp))
                        EditorTextField("SNI / Server Name", sni, "example.com", fieldBg, textColor, mutedColor) { sni = it }
                        EditorTextField("uTLS Fingerprint", fingerprint, "chrome / firefox / safari", fieldBg, textColor, mutedColor) { fingerprint = it }
                        if (security == "tls") {
                            EditorTextField("ALPN (comma-separated)", alpn, "h2,http/1.1", fieldBg, textColor, mutedColor) { alpn = it }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(fieldBg)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Allow Insecure Certificate", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                                ExpressiveSwitch(
                                    checked = allowInsecure,
                                    onCheckedChange = { allowInsecure = it },
                                    activeTrackColor = primaryColor,
                                    activeThumbColor = onPrimaryColor,
                                    inactiveTrackColor = switchOff,
                                    inactiveThumbColor = mutedColor,
                                )
                            }
                        } else {
                            EditorTextField("Reality Public Key (pbk)", publicKey, "public key", fieldBg, textColor, mutedColor) { publicKey = it }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.weight(1f)) {
                                    EditorTextField("Short ID (sid)", shortId, "abcd1234", fieldBg, textColor, mutedColor) { shortId = it }
                                }
                                Box(Modifier.weight(1f)) {
                                    EditorTextField("SpiderX (spx)", spiderX, "/", fieldBg, textColor, mutedColor) { spiderX = it }
                                }
                            }
                        }
                    }
                }

                // Proxy Chaining (dialerProxy)
                val eligibleParents = remember(availableChainParents, editingProfile?.id) {
                    availableChainParents.filter {
                        it.id != editingProfile?.id && it.type != ProfileType.BALANCER && it.type != ProfileType.CUSTOM_JSON
                    }
                }
                if (eligibleParents.isNotEmpty() && type != ProfileType.WIREGUARD) {
                    Spacer(Modifier.height(8.dp))
                    Text("Proxy Chain (Dialer Hop)", fontFamily = OutfitFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedColor)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EditorChip("Direct (None)", dialerProxyId.isBlank(), primaryColor, onPrimaryColor, fieldBg, textColor) {
                            dialerProxyId = ""
                        }
                        eligibleParents.forEach { hop ->
                            val label = hop.remarks.ifBlank { "${hop.server}:${hop.serverPort}" }
                            EditorChip(label, dialerProxyId == hop.id, primaryColor, onPrimaryColor, fieldBg, textColor) {
                                dialerProxyId = hop.id
                            }
                        }
                    }
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontFamily = OutfitFont, fontSize = 12.sp, color = dangerColor)
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(primaryColor)
                    .clickable {
                        val trimmedHost = server.trim()
                        val portNum = portText.trim().toIntOrNull() ?: 0
                        if (type == ProfileType.CUSTOM_JSON) {
                            if (rawConfigJson.isBlank()) {
                                errorMsg = "Enter valid Xray JSON config"
                                return@clickable
                            }
                        } else {
                            if (trimmedHost.isBlank()) {
                                errorMsg = "Server host cannot be empty"
                                return@clickable
                            }
                            if (portNum !in 1..65535) {
                                errorMsg = "Port must be between 1 and 65535"
                                return@clickable
                            }
                        }
                        val base = editingProfile ?: ProfileEntity(id = UUID.randomUUID().toString(), subscriptionId = null, type = type)
                        val updated = base.copy(
                            type = type,
                            remarks = remarks.trim().ifBlank { if (type == ProfileType.CUSTOM_JSON) "Custom Xray" else trimmedHost },
                            server = if (type == ProfileType.CUSTOM_JSON) "custom" else trimmedHost,
                            serverPort = if (type == ProfileType.CUSTOM_JSON) 0 else portNum,
                            password = password.trim(),
                            username = username.trim(),
                            method = method.trim(),
                            network = if (type == ProfileType.HYSTERIA2) "hysteria" else network,
                            host = host.trim(),
                            path = path.trim(),
                            serviceName = serviceName.trim(),
                            mode = mode.trim(),
                            security = security.trim(),
                            sni = sni.trim(),
                            alpn = alpn.trim(),
                            fingerprint = fingerprint.trim(),
                            flow = flow.trim(),
                            allowInsecure = allowInsecure,
                            publicKey = publicKey.trim(),
                            shortId = shortId.trim(),
                            spiderX = spiderX.trim(),
                            obfsPassword = obfsPassword.trim(),
                            portHopping = portHopping.trim(),
                            wgSecretKey = wgSecretKey.trim(),
                            wgPresharedKey = wgPresharedKey.trim(),
                            wgLocalAddress = wgLocalAddress.trim(),
                            wgMtu = wgMtuText.trim().toIntOrNull() ?: 0,
                            wgReserved = wgReserved.trim(),
                            dialerProxyId = dialerProxyId,
                            rawConfigJson = rawConfigJson.trim(),
                            sourceLink = null,
                        )
                        onSave(updated) { err ->
                            if (err != null) errorMsg = err else onDismiss()
                        }
                    }
                    .testTag("save_server_editor_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Save Server",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = onPrimaryColor,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Policy Group (Xray Balancer) Creator & Editor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyGroupEditorModal(
    isStyleB: Boolean,
    editingProfile: ProfileEntity?,
    candidateServers: List<ProfileEntity>,
    onSave: (ProfileEntity, (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val dangerColor = if (isStyleB) SarhadColors.Pink else KiranColors.Orange

    var remarks by remember(editingProfile) {
        mutableStateOf(editingProfile?.remarks.orEmpty().ifBlank { "Auto Balance Group" })
    }
    var strategy by remember(editingProfile) {
        mutableStateOf(editingProfile?.balancerStrategy?.ifBlank { "leastping" } ?: "leastping")
    }
    var selectedKeys by remember(editingProfile) {
        mutableStateOf(editingProfile?.balancerMemberKeys?.toSet() ?: emptySet())
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val selectable = remember(candidateServers) {
        candidateServers.filter { it.type != ProfileType.BALANCER && it.linkKey.isNotBlank() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Text(
                text = if (editingProfile == null) "New Policy Group" else "Edit Policy Group",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Group multiple servers under an Xray balancer (leastPing, leastLoad, random, roundRobin).",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(14.dp))

            EditorTextField("Group Name", remarks, "e.g. Fast Auto Group", fieldBg, textColor, mutedColor) { remarks = it }
            Spacer(Modifier.height(6.dp))
            Text("Balancing Strategy", fontFamily = OutfitFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedColor)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "leastping" to "Lowest Ping",
                    "leastload" to "Least Load",
                    "roundrobin" to "Round Robin",
                    "random" to "Random",
                ).forEach { (key, label) ->
                    EditorChip(label, strategy == key, primaryColor, onPrimaryColor, fieldBg, textColor) {
                        strategy = key
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Member Servers (${selectedKeys.size} selected)", fontFamily = OutfitFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mutedColor)
            Spacer(Modifier.height(6.dp))
            if (selectable.isEmpty()) {
                Text("Add at least one regular server first.", fontFamily = OutfitFont, fontSize = 13.sp, color = mutedColor)
            } else {
                selectable.forEach { member ->
                    val checked = member.linkKey in selectedKeys
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (checked) primaryColor.copy(alpha = 0.16f) else fieldBg)
                            .border(
                                1.dp,
                                if (checked) primaryColor else Color.Transparent,
                                RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                selectedKeys = if (checked) selectedKeys - member.linkKey else selectedKeys + member.linkKey
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = member.remarks.ifBlank { member.server },
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${member.type.name} · ${member.server}:${member.serverPort}",
                                fontFamily = OutfitFont,
                                fontSize = 11.sp,
                                color = mutedColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = if (checked) "✓" else "○",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (checked) primaryColor else mutedColor,
                        )
                    }
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontFamily = OutfitFont, fontSize = 12.sp, color = dangerColor)
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(primaryColor)
                    .clickable {
                        if (selectedKeys.isEmpty()) {
                            errorMsg = "Select at least one server"
                            return@clickable
                        }
                        val base = editingProfile ?: ProfileEntity(
                            id = UUID.randomUUID().toString(),
                            subscriptionId = null,
                            type = ProfileType.BALANCER,
                        )
                        val updated = base.copy(
                            type = ProfileType.BALANCER,
                            remarks = remarks.trim().ifBlank { "Policy Group" },
                            server = "${selectedKeys.size} nodes",
                            serverPort = 0,
                            balancerStrategy = strategy,
                            balancerMembers = selectedKeys.joinToString("\n"),
                        )
                        onSave(updated) { err ->
                            if (err != null) errorMsg = err else onDismiss()
                        }
                    }
                    .testTag("save_policy_group_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text("Save Policy Group", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = onPrimaryColor)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun EditorChip(
    label: String,
    active: Boolean,
    primaryColor: Color,
    onPrimaryColor: Color,
    inactiveBg: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) primaryColor else inactiveBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (active) onPrimaryColor else textColor,
            maxLines = 1,
        )
    }
}

@Composable
internal fun EditorTextField(
    label: String,
    value: String,
    hint: String,
    fieldBg: Color,
    textColor: Color,
    mutedColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(fieldBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, fontFamily = OutfitFont, fontSize = 11.sp, color = mutedColor)
        Spacer(Modifier.height(3.dp))
        Box {
            if (value.isEmpty()) {
                Text(hint, fontFamily = OutfitFont, fontSize = 13.sp, color = mutedColor.copy(alpha = 0.55f))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EditorMultilineField(
    label: String,
    value: String,
    hint: String,
    fieldBg: Color,
    textColor: Color,
    mutedColor: Color,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(fieldBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, fontFamily = OutfitFont, fontSize = 11.sp, color = mutedColor)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).verticalScroll(rememberScrollState())) {
            if (value.isEmpty()) {
                Text(hint, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = mutedColor.copy(alpha = 0.55f))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = textColor),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
