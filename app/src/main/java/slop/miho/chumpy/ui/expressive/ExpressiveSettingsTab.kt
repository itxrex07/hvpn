package slop.miho.chumpy.ui.expressive

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.BuildConfig
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.IconVariant
import slop.miho.chumpy.data.UiSettings
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.ui.UpdateUiState
import slop.miho.chumpy.ui.wallpapers.WallpaperActivity

/**
 * Unified Settings tab shared identically across Style A (Kiran) and Style B (Sarhad).
 * Adapts only surface/accent colors to match the selected appearance style while keeping
 * layout, wording, and options identical across styles.
 */
@Composable
fun ExpressiveSettingsTab(
    isStyleB: Boolean,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    subscriptions: List<SubscriptionEntity>,
    coreVersion: String?,
    coreRunning: Boolean,
    updateState: UpdateUiState,
    onUpdateSettings: ((UiSettings) -> UiSettings) -> Unit,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onOpenLauncherIconModal: () -> Unit,
    onOpenDnsModal: () -> Unit,
    onOpenFragmentModal: () -> Unit,
    onOpenPerAppProxy: () -> Unit,
    onOpenStyleModal: () -> Unit,
    onOpenHappSpoofModal: () -> Unit,
    onOpenDelayTestModal: () -> Unit,
    onOpenLogsModal: () -> Unit,
    onOpenCustomRouting: () -> Unit,
    onOpenGeodataModal: () -> Unit,
    onOpenBackupModal: () -> Unit,
    onOpenProxyDnsModal: () -> Unit,
    onCheckForUpdate: () -> Unit,
    onUpdateAction: () -> Unit,
) {
    val context = LocalContext.current
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    val cardBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val textPrimary = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val textMuted = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val accentColor = if (isStyleB) SarhadColors.PrimaryLight else KiranColors.Mint
    val switchActiveColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val switchInactiveColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg
    val switchThumbColor = if (isStyleB) Color.White else KiranColors.OnMint
    val cardRadius = if (isStyleB) 28.dp else 32.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Settings",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp,
                    color = textPrimary,
                )
                Text(
                    text = "Essentials up front · pro tools neatly tucked away",
                    fontFamily = OutfitFont,
                    fontSize = 13.sp,
                    color = textMuted,
                )
            }
        }

        // Card 1: Everyday VPN Basics
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardRadius),
                color = cardBg,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "VPN Essentials",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SettingsToggleRow(
                        title = "Keep Connected (Auto Reconnect)",
                        subtitle = "Restore VPN automatically if your network drops",
                        checked = connectionSettings.autoReconnect,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        activeColor = switchActiveColor,
                        inactiveColor = switchInactiveColor,
                        thumbColor = switchThumbColor,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(autoReconnect = enabled) }
                        },
                    )
                    SettingsToggleRow(
                        title = "Block Leaks (Kill Switch)",
                        subtitle = "Stop internet traffic if VPN turns off unexpectedly",
                        checked = connectionSettings.killSwitch,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        activeColor = switchActiveColor,
                        inactiveColor = switchInactiveColor,
                        thumbColor = switchThumbColor,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(killSwitch = enabled) }
                        },
                    )
                    SettingsToggleRow(
                        title = "Smart Auto-Switch",
                        subtitle = "Switch to a working server automatically if this one stalls",
                        checked = connectionSettings.smartSwitchEnabled,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        activeColor = switchActiveColor,
                        inactiveColor = switchInactiveColor,
                        thumbColor = switchThumbColor,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(smartSwitchEnabled = enabled) }
                        },
                    )
                    SettingsToggleRow(
                        title = "Block Ads & Trackers",
                        subtitle = "Automatically block known ad domains",
                        checked = connectionSettings.blockAds,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        activeColor = switchActiveColor,
                        inactiveColor = switchInactiveColor,
                        thumbColor = switchThumbColor,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(blockAds = enabled) }
                        },
                    )
                    SettingsActionRow(
                        title = "Apps Skipping VPN",
                        subtitle = if (connectionSettings.perAppEnabled) {
                            "${connectionSettings.bypassedApps.size} app(s) excluded (e.g. banking)"
                        } else {
                            "All apps go through VPN"
                        },
                        valueText = "Choose ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onOpenPerAppProxy,
                    )
                    SettingsActionRow(
                        title = "Private DNS",
                        subtitle = connectionSettings.dnsProvider,
                        valueText = "Change ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onOpenDnsModal,
                    )
                }
            }
        }

        // Card 2: Smart Country Routing
        item {
            RegionRoutingSelectorCard(
                isStyleB = isStyleB,
                selectedMode = connectionSettings.routingMode,
                flagStyle = settings.flagStyle,
                onSelectMode = { mode ->
                    onUpdateConnection { it.copy(routingMode = mode) }
                },
            )
        }

        // Card 3: Look & Privacy
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardRadius),
                color = cardBg,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Look & Privacy",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SettingsActionRow(
                        title = "Theme, Colors & Layout Style",
                        subtitle = when (settings.designStyle) {
                            DesignStyle.KIRAN_A -> "Style A · Kiran (Mint)"
                            DesignStyle.SARHAD_B -> "Style B · Sarhad (Indigo)"
                        },
                        valueText = "Customize ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onOpenStyleModal,
                    )
                    SettingsActionRow(
                        title = "App Icon",
                        subtitle = when (settings.iconVariant) {
                            IconVariant.CHUMPY -> "Hollow Mint"
                            IconVariant.INVERTED -> "Inverted Mint"
                            IconVariant.DARK -> "Night Dark"
                            IconVariant.THEMED -> "Themed Monochrome"
                        },
                        valueText = "Change ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onOpenLauncherIconModal,
                    )
                    SettingsActionRow(
                        title = "Wallpaper",
                        subtitle = "Set a custom background photo",
                        valueText = "Open ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = {
                            context.startActivity(Intent(context, WallpaperActivity::class.java))
                        },
                    )
                    SettingsToggleRow(
                        title = "Hide Server Addresses",
                        subtitle = "Blur IPs & domains when taking screenshots",
                        checked = settings.maskAddresses,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        activeColor = switchActiveColor,
                        inactiveColor = switchInactiveColor,
                        thumbColor = switchThumbColor,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(maskAddresses = enabled) }
                        },
                    )
                }
            }
        }

        // Card 4: Backup & Updates
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardRadius),
                color = cardBg,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Backup & App Info",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SettingsActionRow(
                        title = "Backup & Restore",
                        subtitle = "Save or restore your servers & settings in one file",
                        valueText = "Open ›",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onOpenBackupModal,
                    )
                    SettingsActionRow(
                        title = "Hollow VPN v${BuildConfig.VERSION_NAME}",
                        subtitle = coreVersion ?: "Xray core ready",
                        valueText = if (updateState.checking) "Checking…" else "Check updates",
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentColor,
                        onClick = onCheckForUpdate,
                    )
                    if (updateState.available != null) {
                        SettingsActionRow(
                            title = "Update ${updateState.available.tag} ready",
                            subtitle = when {
                                updateState.downloadedFile != null -> "Tap to install update"
                                updateState.downloading -> "Downloading ${updateState.downloadPercent}%"
                                else -> "Tap to download latest version"
                            },
                            valueText = if (updateState.downloadedFile != null) "Install" else "Download",
                            textPrimary = textPrimary,
                            textMuted = textMuted,
                            accentColor = accentColor,
                            onClick = onUpdateAction,
                        )
                    }
                }
            }
        }

        // Card 5: Collapsible Pro / Advanced Drawer (Collapsed by default)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardRadius),
                color = cardBg,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(vertical = 4.dp)
                            .testTag("expressive_settings_advanced_toggle"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Advanced (For Pro Users)",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textPrimary,
                            )
                            Text(
                                text = "Custom rules, Anti-DPI, Hotspot share, Geodata & logs",
                                fontFamily = OutfitFont,
                                fontSize = 12.sp,
                                color = textMuted,
                            )
                        }
                        Text(
                            text = if (showAdvanced) "Hide ▴" else "Show ▾",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentColor,
                        )
                    }

                    AnimatedVisibility(visible = showAdvanced) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            SettingsToggleRow(
                                title = "Enable Per-App Split Tunnel",
                                subtitle = "Allow excluding selected apps from VPN",
                                checked = connectionSettings.perAppEnabled,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                activeColor = switchActiveColor,
                                inactiveColor = switchInactiveColor,
                                thumbColor = switchThumbColor,
                                onCheckedChange = { enabled ->
                                    onUpdateConnection { it.copy(perAppEnabled = enabled) }
                                },
                            )
                            SettingsToggleRow(
                                title = "Keep Local Wi-Fi Devices Direct",
                                subtitle = "Printers, smart TVs, and routers stay reachable",
                                checked = connectionSettings.bypassLan,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                activeColor = switchActiveColor,
                                inactiveColor = switchInactiveColor,
                                thumbColor = switchThumbColor,
                                onCheckedChange = { enabled ->
                                    onUpdateConnection { it.copy(bypassLan = enabled) }
                                },
                            )
                            SettingsToggleRow(
                                title = "Skip Local Country Websites",
                                subtitle = "Open domestic sites faster without VPN",
                                checked = connectionSettings.bypassDomestic,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                activeColor = switchActiveColor,
                                inactiveColor = switchInactiveColor,
                                thumbColor = switchThumbColor,
                                onCheckedChange = { enabled ->
                                    onUpdateConnection { it.copy(bypassDomestic = enabled) }
                                },
                            )
                            val customRuleCount = remember(connectionSettings.customRoutingRulesJson) {
                                CustomRoutingRule.listFromJson(connectionSettings.customRoutingRulesJson).count { it.enabled }
                            }
                            SettingsActionRow(
                                title = "Custom Website / IP Rules",
                                subtitle = if (customRuleCount == 0) "No custom rules set" else "$customRuleCount active rule(s)",
                                valueText = "Edit ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenCustomRouting,
                            )
                            SettingsActionRow(
                                title = "Bypass Censorship (Anti-DPI)",
                                subtitle = if (connectionSettings.fragmentEnabled) "Active (${connectionSettings.fragmentLength})" else "Off — split packets if sites block TLS",
                                valueText = "Edit ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenFragmentModal,
                            )
                            SettingsActionRow(
                                title = "Network, Hotspot Share & Proxy Ports",
                                subtitle = if (connectionSettings.proxyOnlyMode) "Proxy-Only Mode (No TUN)" else "SOCKS ${connectionSettings.socksPort} · HTTP ${connectionSettings.httpPort} · MTU ${connectionSettings.tunMtu}",
                                valueText = "Edit ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenProxyDnsModal,
                            )
                            SettingsActionRow(
                                title = "Routing Databases (GeoIP / GeoSite)",
                                subtitle = "Update or reset country & ad databases",
                                valueText = "Manage ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenGeodataModal,
                            )
                            SettingsToggleRow(
                                title = "Auto-Connect on Open Wi-Fi",
                                subtitle = "Turn VPN on automatically on untrusted Wi-Fi",
                                checked = connectionSettings.autoConnectUntrustedWifi,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                activeColor = switchActiveColor,
                                inactiveColor = switchInactiveColor,
                                thumbColor = switchThumbColor,
                                onCheckedChange = { enabled ->
                                    onUpdateConnection { it.copy(autoConnectUntrustedWifi = enabled) }
                                },
                            )
                            SettingsToggleRow(
                                title = "Start on Device Boot",
                                subtitle = "Start VPN automatically when phone restarts",
                                checked = connectionSettings.startOnBoot,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                activeColor = switchActiveColor,
                                inactiveColor = switchInactiveColor,
                                thumbColor = switchThumbColor,
                                onCheckedChange = { enabled ->
                                    onUpdateConnection { it.copy(startOnBoot = enabled) }
                                },
                            )
                            SettingsActionRow(
                                title = "Speed Test URL & Auto-Refresh",
                                subtitle = "${subscriptions.count { it.autoUpdate }}/${subscriptions.size} auto-refresh · ${connectionSettings.delayTestUrl}",
                                valueText = "Configure ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenDelayTestModal,
                            )
                            SettingsActionRow(
                                title = "Panel Device Fingerprint (Happ Spoof)",
                                subtitle = if (settings.happSpoof) "Active (${settings.happDeviceModel})" else "Disabled",
                                valueText = "Configure ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenHappSpoofModal,
                            )
                            SettingsActionRow(
                                title = "Developer Core Logs",
                                subtitle = if (coreRunning) "Core running · Inspect live diagnostics" else "View last session logs",
                                valueText = "Logs ›",
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                accentColor = accentColor,
                                onClick = onOpenLogsModal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    textPrimary: Color,
    textMuted: Color,
    activeColor: Color,
    inactiveColor: Color,
    thumbColor: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = textMuted,
            )
        }
        Spacer(Modifier.width(12.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = activeColor,
            activeThumbColor = thumbColor,
            inactiveTrackColor = inactiveColor,
            inactiveThumbColor = textMuted,
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    valueText: String,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = valueText,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = accentColor,
        )
    }
}
