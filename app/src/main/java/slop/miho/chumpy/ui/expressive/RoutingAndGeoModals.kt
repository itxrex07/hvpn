package slop.miho.chumpy.ui.expressive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.GeoAssetInfo
import slop.miho.chumpy.data.GeoAssetPreset
import slop.miho.chumpy.data.GeodataManager
import slop.miho.chumpy.data.RoutingRuleAction
import slop.miho.chumpy.data.RoutingRuleMatchType

/**
 * Custom Xray Routing Rules editor (Domain, IP, Port, Protocol -> Proxy / Direct / Block).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRoutingModal(
    isStyleB: Boolean,
    connectionSettings: ConnectionSettings,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
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

    var rules by remember(connectionSettings.customRoutingRulesJson) {
        mutableStateOf(CustomRoutingRule.listFromJson(connectionSettings.customRoutingRulesJson))
    }
    var matchType by remember { mutableStateOf(RoutingRuleMatchType.DOMAIN) }
    var action by remember { mutableStateOf(RoutingRuleAction.DIRECT) }
    var pattern by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    fun persist(next: List<CustomRoutingRule>) {
        rules = next
        onUpdateConnection { it.copy(customRoutingRulesJson = CustomRoutingRule.listToJson(next)) }
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
            Text("Custom Website & IP Rules", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = textColor)
            Spacer(Modifier.height(4.dp))
            Text(
                "Force specific websites, IPs, or ports to use VPN, skip VPN, or block completely.",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EditorChip("+ Skip Google Play", false, primaryColor, onPrimaryColor, fieldBg, textColor) {
                    matchType = RoutingRuleMatchType.DOMAIN
                    action = RoutingRuleAction.DIRECT
                    pattern = "domain:play.googleapis.com,domain:gvt1.com"
                    label = "Google Play Direct"
                }
                EditorChip("+ Block Ads", false, primaryColor, onPrimaryColor, fieldBg, textColor) {
                    matchType = RoutingRuleMatchType.DOMAIN
                    action = RoutingRuleAction.BLOCK
                    pattern = "geosite:category-ads-all"
                    label = "Block Ads"
                }
                EditorChip("+ Skip Local IP", false, primaryColor, onPrimaryColor, fieldBg, textColor) {
                    matchType = RoutingRuleMatchType.IP
                    action = RoutingRuleAction.DIRECT
                    pattern = "192.168.0.0/16,10.0.0.0/8"
                    label = "Local Wi-Fi Direct"
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    RoutingRuleMatchType.DOMAIN to "Website",
                    RoutingRuleMatchType.IP to "IP Address",
                    RoutingRuleMatchType.PORT to "Port",
                    RoutingRuleMatchType.PROTOCOL to "Protocol",
                ).forEach { (mt, title) ->
                    EditorChip(title, matchType == mt, primaryColor, onPrimaryColor, fieldBg, textColor) { matchType = mt }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    RoutingRuleAction.PROXY to "Use VPN",
                    RoutingRuleAction.DIRECT to "Skip VPN",
                    RoutingRuleAction.BLOCK to "Block",
                ).forEach { (act, title) ->
                    EditorChip(title, action == act, primaryColor, onPrimaryColor, fieldBg, textColor) { action = act }
                }
            }
            Spacer(Modifier.height(8.dp))
            EditorTextField(
                label = when (matchType) {
                    RoutingRuleMatchType.DOMAIN -> "Website or Domain"
                    RoutingRuleMatchType.IP -> "IP or Subnet"
                    RoutingRuleMatchType.PORT -> "Port (e.g. 443)"
                    RoutingRuleMatchType.PROTOCOL -> "Protocol (e.g. bittorrent)"
                },
                value = pattern,
                hint = when (matchType) {
                    RoutingRuleMatchType.DOMAIN -> "example.com or geosite:youtube"
                    RoutingRuleMatchType.IP -> "192.168.1.0/24"
                    RoutingRuleMatchType.PORT -> "443 or 5000-6000"
                    RoutingRuleMatchType.PROTOCOL -> "bittorrent"
                },
                fieldBg = fieldBg,
                textColor = textColor,
                mutedColor = mutedColor,
            ) { pattern = it }
            EditorTextField("Rule Name (optional)", label, "e.g. Banking Skip", fieldBg, textColor, mutedColor) { label = it }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(primaryColor)
                    .clickable {
                        val trimmed = pattern.trim()
                        if (trimmed.isNotEmpty()) {
                            persist(
                                rules + CustomRoutingRule(
                                    label = label.trim(),
                                    matchType = matchType,
                                    pattern = trimmed,
                                    action = action,
                                    enabled = true,
                                ),
                            )
                            pattern = ""
                            label = ""
                        }
                    }
                    .testTag("add_custom_routing_rule_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text("+ Save Rule", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = onPrimaryColor)
            }

            Spacer(Modifier.height(14.dp))
            if (rules.isEmpty()) {
                Text("No custom rules added yet.", fontFamily = OutfitFont, fontSize = 13.sp, color = mutedColor)
            } else {
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(fieldBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = rule.label.ifBlank { "${rule.matchType.name} → ${rule.action.name}" },
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor,
                            )
                            Text(
                                text = "${rule.matchType.name}: ${rule.pattern}  (${rule.action.name})",
                                fontFamily = OutfitFont,
                                fontSize = 11.5.sp,
                                color = mutedColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "🗑",
                            fontSize = 15.sp,
                            color = dangerColor,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable { persist(rules.filterNot { it.id == rule.id }) },
                        )
                        ExpressiveSwitch(
                            checked = rule.enabled,
                            onCheckedChange = { en ->
                                persist(rules.map { if (it.id == rule.id) it.copy(enabled = en) else it })
                            },
                            activeTrackColor = primaryColor,
                            activeThumbColor = onPrimaryColor,
                            inactiveTrackColor = switchOff,
                            inactiveThumbColor = mutedColor,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

/**
 * In-App Geodata Asset Manager (inspect geoip.dat / geosite.dat, install presets or custom URL, reset to bundled).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeodataManagerModal(
    isStyleB: Boolean,
    tunnelUp: Boolean,
    connectionSettings: ConnectionSettings,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val switchOff = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

    var assets by remember { mutableStateOf<List<GeoAssetInfo>>(emptyList()) }
    var busyMessage by remember { mutableStateOf<String?>(null) }
    var customGeoipUrl by remember { mutableStateOf("") }
    var customGeositeUrl by remember { mutableStateOf("") }
    var showCustomGeo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        assets = GeodataManager.inspectAssets(context)
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
            Text("Routing Databases", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = textColor)
            Spacer(Modifier.height(4.dp))
            Text(
                "Used locally to recognize country websites, local IPs, and ad domains.",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(12.dp))

            assets.forEach { info ->
                val friendlyName = if (info.fileName == "geoip.dat") "IP Country Database" else "Website & Ad Database"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(fieldBg)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(friendlyName, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                        Text(
                            text = "${formatCompactBytes(info.sizeBytes)} · ${info.categoryCount} lists · ${if (info.isCustom) "Custom" else "Built-in"}",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = mutedColor,
                        )
                    }
                    if (info.isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(primaryColor.copy(alpha = 0.16f))
                                .clickable {
                                    scope.launch {
                                        busyMessage = "Resetting ${info.fileName}…"
                                        GeodataManager.resetAssetToBundled(context, info.fileName)
                                        assets = GeodataManager.inspectAssets(context)
                                        busyMessage = "Restored built-in $friendlyName"
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text("Reset", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("1-Tap Database Updates", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = mutedColor)
            Spacer(Modifier.height(6.dp))
            GeoAssetPreset.entries.forEach { preset ->
                val friendlyPreset = when (preset) {
                    GeoAssetPreset.XTLS_OFFICIAL -> "Official Standard (Recommended)"
                    GeoAssetPreset.LOYALSOLDIER_FULL -> "Extended Community List"
                    GeoAssetPreset.IRAN_RUNET -> "Iran & Regional Optimization"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(fieldBg)
                        .clickable {
                            scope.launch {
                                busyMessage = "Downloading $friendlyPreset…"
                                val r1 = GeodataManager.downloadAsset(
                                    context, "geoip.dat", preset.geoipUrl,
                                    tunnelUp, connectionSettings.socksPort, connectionSettings.cdnMirrorFallback,
                                )
                                val r2 = GeodataManager.downloadAsset(
                                    context, "geosite.dat", preset.geositeUrl,
                                    tunnelUp, connectionSettings.socksPort, connectionSettings.cdnMirrorFallback,
                                )
                                assets = GeodataManager.inspectAssets(context)
                                busyMessage = if (r1.isSuccess && r2.isSuccess) {
                                    "Updated to $friendlyPreset"
                                } else {
                                    "Download failed: ${(r1.exceptionOrNull() ?: r2.exceptionOrNull())?.message}"
                                }
                            }
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(friendlyPreset, fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                    Text("Update ›", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fieldBg)
                    .clickable { showCustomGeo = !showCustomGeo }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Advanced: Custom URL & Mirror Fallback", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                Text(if (showCustomGeo) "Hide ▴" else "Show ▾", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
            }

            AnimatedVisibility(visible = showCustomGeo) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    SwitchTile(
                        title = "Auto CDN Mirror Fallback",
                        subtitle = "Try fast mirrors automatically if GitHub is blocked",
                        checked = connectionSettings.cdnMirrorFallback,
                        fieldBg = fieldBg,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        primaryColor = primaryColor,
                        onPrimaryColor = onPrimaryColor,
                        switchOff = switchOff,
                    ) { en ->
                        onUpdateConnection { it.copy(cdnMirrorFallback = en) }
                    }
                    Spacer(Modifier.height(6.dp))
                    EditorTextField("Custom geoip.dat URL", customGeoipUrl, "https://.../geoip.dat", fieldBg, textColor, mutedColor) { customGeoipUrl = it }
                    EditorTextField("Custom geosite.dat URL", customGeositeUrl, "https://.../geosite.dat", fieldBg, textColor, mutedColor) { customGeositeUrl = it }
                    if (customGeoipUrl.isNotBlank() || customGeositeUrl.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(primaryColor)
                                .clickable {
                                    scope.launch {
                                        busyMessage = "Downloading custom database…"
                                        val results = buildList {
                                            if (customGeoipUrl.isNotBlank()) {
                                                add(GeodataManager.downloadAsset(context, "geoip.dat", customGeoipUrl, tunnelUp, connectionSettings.socksPort, connectionSettings.cdnMirrorFallback))
                                            }
                                            if (customGeositeUrl.isNotBlank()) {
                                                add(GeodataManager.downloadAsset(context, "geosite.dat", customGeositeUrl, tunnelUp, connectionSettings.socksPort, connectionSettings.cdnMirrorFallback))
                                            }
                                        }
                                        assets = GeodataManager.inspectAssets(context)
                                        busyMessage = if (results.all { it.isSuccess }) "Custom database installed" else results.firstNotNullOfOrNull { it.exceptionOrNull()?.message }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Download Custom URL(s)", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = onPrimaryColor)
                        }
                    }
                }
            }

            busyMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Advanced Network, Local Proxy Ports/Auth, Proxy-Only Mode, IPv6, TUN MTU, FakeDNS & Split DNS modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyAndDnsAdvancedModal(
    isStyleB: Boolean,
    connectionSettings: ConnectionSettings,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val switchOff = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

    var proxyOnly by remember(connectionSettings) { mutableStateOf(connectionSettings.proxyOnlyMode) }
    var lanShare by remember(connectionSettings) { mutableStateOf(connectionSettings.lanSharingEnabled) }
    var ipv6 by remember(connectionSettings) { mutableStateOf(connectionSettings.ipv6Enabled) }
    var tunMtuText by remember(connectionSettings) { mutableStateOf(connectionSettings.tunMtu.toString()) }
    var socksPortText by remember(connectionSettings) { mutableStateOf(connectionSettings.socksPort.toString()) }
    var httpPortText by remember(connectionSettings) { mutableStateOf(connectionSettings.httpPort.toString()) }
    var socksUser by remember(connectionSettings) { mutableStateOf(connectionSettings.socksUsername) }
    var socksPass by remember(connectionSettings) { mutableStateOf(connectionSettings.socksPassword) }
    var fakeDns by remember(connectionSettings) { mutableStateOf(connectionSettings.fakednsEnabled) }
    var localDns by remember(connectionSettings) { mutableStateOf(connectionSettings.localDnsEnabled) }
    var domainStrategy by remember(connectionSettings) { mutableStateOf(connectionSettings.domainStrategy) }
    var remoteDns by remember(connectionSettings) { mutableStateOf(connectionSettings.remoteDns) }
    var domesticDns by remember(connectionSettings) { mutableStateOf(connectionSettings.domesticDns) }
    var showExpertTuning by remember { mutableStateOf(false) }

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
            Text("Network & Hotspot Sharing", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = textColor)
            Spacer(Modifier.height(4.dp))
            Text(
                "Share your VPN with other devices over Wi-Fi or adjust connection basics.",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(12.dp))

            SwitchTile("Share VPN on Wi-Fi / Hotspot", "Let laptops & other phones connect via SOCKS (${connectionSettings.socksPort}) or HTTP (${connectionSettings.httpPort})", lanShare, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                lanShare = it
            }
            SwitchTile("IPv6 Support", "Turn on only if your network supports IPv6 servers", ipv6, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                ipv6 = it
            }
            SwitchTile("Faster Page Loading (FakeDNS)", "Speed up website lookups inside the VPN", fakeDns, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                fakeDns = it
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fieldBg)
                    .clickable { showExpertTuning = !showExpertTuning }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Expert Tuning: Proxy-Only, Ports, MTU & Custom DNS", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = textColor, modifier = Modifier.weight(1f))
                Text(if (showExpertTuning) "Hide ▴" else "Show ▾", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
            }

            AnimatedVisibility(visible = showExpertTuning) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    SwitchTile("Proxy-Only Mode (No Android VPN)", "Expose local SOCKS5/HTTP ports without creating a VPN tunnel", proxyOnly, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                        proxyOnly = it
                    }
                    SwitchTile("Xray Internal Local DNS", "Route DNS lookups inside Xray DNS engine", localDns, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                        localDns = it
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { EditorTextField("SOCKS Port", socksPortText, "10808", fieldBg, textColor, mutedColor, KeyboardType.Number) { socksPortText = it } }
                        Box(Modifier.weight(1f)) { EditorTextField("HTTP Port", httpPortText, "10809", fieldBg, textColor, mutedColor, KeyboardType.Number) { httpPortText = it } }
                        Box(Modifier.weight(1f)) { EditorTextField("TUN MTU", tunMtuText, "1500", fieldBg, textColor, mutedColor, KeyboardType.Number) { tunMtuText = it } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { EditorTextField("Proxy User (opt)", socksUser, "optional", fieldBg, textColor, mutedColor) { socksUser = it } }
                        Box(Modifier.weight(1f)) { EditorTextField("Proxy Pass (opt)", socksPass, "optional", fieldBg, textColor, mutedColor) { socksPass = it } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { EditorTextField("Remote DNS", remoteDns, "1.1.1.1", fieldBg, textColor, mutedColor) { remoteDns = it } }
                        Box(Modifier.weight(1f)) { EditorTextField("Domestic DNS", domesticDns, "223.5.5.5", fieldBg, textColor, mutedColor) { domesticDns = it } }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text("Domain Strategy", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = mutedColor)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AsIs", "IPIfNonMatch", "IPOnDemand").forEach { ds ->
                            EditorChip(ds, domainStrategy == ds, primaryColor, onPrimaryColor, fieldBg, textColor) { domainStrategy = ds }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(primaryColor)
                    .clickable {
                        onUpdateConnection {
                            it.copy(
                                proxyOnlyMode = proxyOnly,
                                lanSharingEnabled = lanShare,
                                ipv6Enabled = ipv6,
                                tunMtu = (tunMtuText.toIntOrNull() ?: 1500).coerceIn(1280, 9000),
                                socksPort = (socksPortText.toIntOrNull() ?: 10808).coerceIn(1024, 65535),
                                httpPort = (httpPortText.toIntOrNull() ?: 10809).coerceIn(1024, 65535),
                                socksUsername = socksUser.trim(),
                                socksPassword = socksPass.trim(),
                                fakednsEnabled = fakeDns,
                                localDnsEnabled = localDns,
                                domainStrategy = domainStrategy,
                                remoteDns = remoteDns.trim().ifBlank { "1.1.1.1" },
                                domesticDns = domesticDns.trim().ifBlank { "223.5.5.5" },
                            )
                        }
                        onDismiss()
                    }
                    .testTag("save_proxy_dns_advanced_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text("Save & Apply", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = onPrimaryColor)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun SwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    fieldBg: Color,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    onPrimaryColor: Color,
    switchOff: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fieldBg)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
            Text(subtitle, fontFamily = OutfitFont, fontSize = 11.5.sp, color = mutedColor)
        }
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = primaryColor,
            activeThumbColor = onPrimaryColor,
            inactiveTrackColor = switchOff,
            inactiveThumbColor = mutedColor,
        )
    }
}
