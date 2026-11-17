package slop.miho.chumpy.ui.expressive

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.BuildConfig
import slop.miho.chumpy.R
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.FlagStyle
import slop.miho.chumpy.data.IconVariant
import slop.miho.chumpy.data.UiSettings
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.ui.UpdateUiState
import slop.miho.chumpy.xray.RoutingMode

private enum class SettingsRoute {
    ROOT,
    APPEARANCE,
    ROUTING,
    CONNECTION,
    BACKUP,
    ADVANCED,
}

private data class SettingsPalette(
    val cardBg: Color,
    val innerPillBg: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val iconTintBg: Color,
    val switchActive: Color,
    val switchInactive: Color,
    val switchThumb: Color,
    val cardRadius: Dp,
)

/**
 * Unified Hub-and-Spoke Settings experience shared identically by Style A (Kiran) and Style B (Sarhad).
 * Root screen shows only 3 everyday toggles + 5 clean category hubs; tapping any hub drills
 * into a focused sub-page with predictive/system Back support.
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
    onOpenDnsModal: () -> Unit,
    onOpenFragmentModal: () -> Unit,
    onOpenPerAppProxy: () -> Unit,
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
    var currentRoute by rememberSaveable { mutableStateOf(SettingsRoute.ROOT) }

    BackHandler(enabled = currentRoute != SettingsRoute.ROOT) {
        currentRoute = SettingsRoute.ROOT
    }

    val palette = if (isStyleB) {
        SettingsPalette(
            cardBg = SarhadColors.Card,
            innerPillBg = SarhadColors.PillBg,
            textPrimary = SarhadColors.TextPrimary,
            textMuted = SarhadColors.TextMuted,
            accent = SarhadColors.PrimaryLight,
            iconTintBg = SarhadColors.SelectedServer,
            switchActive = SarhadColors.Primary,
            switchInactive = SarhadColors.SwitchOff,
            switchThumb = Color.White,
            cardRadius = 28.dp,
        )
    } else {
        SettingsPalette(
            cardBg = KiranColors.Card,
            innerPillBg = KiranColors.NavBg,
            textPrimary = KiranColors.TextPrimary,
            textMuted = KiranColors.TextMuted,
            accent = KiranColors.Mint,
            iconTintBg = KiranColors.TagBg,
            switchActive = KiranColors.Mint,
            switchInactive = KiranColors.SwitchOffBg,
            switchThumb = KiranColors.OnMint,
            cardRadius = 32.dp,
        )
    }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (targetState != SettingsRoute.ROOT && initialState == SettingsRoute.ROOT) {
                (slideInHorizontally(tween(220)) { it / 4 } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(180)) { -it / 6 } + fadeOut(tween(150)))
            } else if (targetState == SettingsRoute.ROOT && initialState != SettingsRoute.ROOT) {
                (slideInHorizontally(tween(220)) { -it / 4 } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(180)) { it / 6 } + fadeOut(tween(150)))
            } else {
                fadeIn(tween(180)) togetherWith fadeOut(tween(140))
            }
        },
        label = "settingsHubRoute",
    ) { route ->
        when (route) {
            SettingsRoute.ROOT -> SettingsRootPage(
                palette = palette,
                settings = settings,
                connectionSettings = connectionSettings,
                updateState = updateState,
                onUpdateConnection = onUpdateConnection,
                onNavigate = { currentRoute = it },
            )

            SettingsRoute.APPEARANCE -> SettingsAppearancePage(
                isStyleB = isStyleB,
                palette = palette,
                settings = settings,
                onBack = { currentRoute = SettingsRoute.ROOT },
                onUpdateSettings = onUpdateSettings,
            )

            SettingsRoute.ROUTING -> SettingsRoutingPage(
                isStyleB = isStyleB,
                palette = palette,
                settings = settings,
                connectionSettings = connectionSettings,
                onBack = { currentRoute = SettingsRoute.ROOT },
                onUpdateConnection = onUpdateConnection,
                onOpenPerAppProxy = onOpenPerAppProxy,
                onOpenCustomRouting = onOpenCustomRouting,
            )

            SettingsRoute.CONNECTION -> SettingsConnectionPage(
                palette = palette,
                connectionSettings = connectionSettings,
                onBack = { currentRoute = SettingsRoute.ROOT },
                onUpdateConnection = onUpdateConnection,
                onOpenDnsModal = onOpenDnsModal,
                onOpenFragmentModal = onOpenFragmentModal,
                onOpenProxyDnsModal = onOpenProxyDnsModal,
            )

            SettingsRoute.BACKUP -> SettingsBackupPage(
                palette = palette,
                coreVersion = coreVersion,
                updateState = updateState,
                onBack = { currentRoute = SettingsRoute.ROOT },
                onOpenBackupModal = onOpenBackupModal,
                onCheckForUpdate = onCheckForUpdate,
                onUpdateAction = onUpdateAction,
            )

            SettingsRoute.ADVANCED -> SettingsAdvancedPage(
                palette = palette,
                settings = settings,
                connectionSettings = connectionSettings,
                subscriptions = subscriptions,
                coreRunning = coreRunning,
                onBack = { currentRoute = SettingsRoute.ROOT },
                onOpenGeodataModal = onOpenGeodataModal,
                onOpenDelayTestModal = onOpenDelayTestModal,
                onOpenHappSpoofModal = onOpenHappSpoofModal,
                onOpenLogsModal = onOpenLogsModal,
            )
        }
    }
}

@Composable
private fun SettingsRootPage(
    palette: SettingsPalette,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    updateState: UpdateUiState,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onNavigate: (SettingsRoute) -> Unit,
) {
    val styleLabel = when (settings.designStyle) {
        DesignStyle.KIRAN_A -> "Style A · Kiran"
        DesignStyle.SARHAD_B -> "Style B · Sarhad"
    }
    val iconLabel = when (settings.iconVariant) {
        IconVariant.CHUMPY -> "Hollow Mint"
        IconVariant.INVERTED -> "Inverted"
        IconVariant.DARK -> "Night Dark"
        IconVariant.THEMED -> "Themed"
    }
    val routingLabel = when (connectionSettings.routingMode) {
        RoutingMode.GLOBAL -> "Global"
        else -> "Bypass ${connectionSettings.routingMode.region?.uppercase().orEmpty()}"
    }
    val splitLabel = if (connectionSettings.perAppEnabled && connectionSettings.bypassedApps.isNotEmpty()) {
        "${connectionSettings.bypassedApps.size} app(s) excluded"
    } else if (connectionSettings.perAppEnabled) {
        "Split tunnel on"
    } else {
        "All apps via VPN"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Settings",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp,
                    color = palette.textPrimary,
                )
                Text(
                    text = "Everyday protection and app preferences",
                    fontFamily = OutfitFont,
                    fontSize = 13.sp,
                    color = palette.textMuted,
                )
            }
        }

        // Card 1: 3 everyday quick protection toggles
        item {
            SettingsGroupCard(palette = palette, title = "Quick Protection") {
                SettingsToggleRow(
                    title = "Auto-Reconnect",
                    subtitle = "Stay connected if your network drops",
                    checked = connectionSettings.autoReconnect,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(autoReconnect = enabled) }
                    },
                )
                SettingsToggleRow(
                    title = "Block Ads & Trackers",
                    subtitle = "Filter known ad and tracking domains",
                    checked = connectionSettings.blockAds,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(blockAds = enabled) }
                    },
                )
                SettingsToggleRow(
                    title = "Kill Switch",
                    subtitle = "Stop internet access if VPN drops unexpectedly",
                    checked = connectionSettings.killSwitch,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(killSwitch = enabled) }
                    },
                )
            }
        }

        // Card 2: 5 clean category hubs
        item {
            SettingsGroupCard(palette = palette, title = null) {
                SettingsHubRow(
                    iconRes = R.drawable.ic_appearance_fill,
                    title = "Appearance",
                    subtitle = "$styleLabel · $iconLabel icon",
                    palette = palette,
                    testTag = "settings_hub_appearance",
                    onClick = { onNavigate(SettingsRoute.APPEARANCE) },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_swap_horizontal,
                    title = "Routing & Split Tunnel",
                    subtitle = "$routingLabel · $splitLabel",
                    palette = palette,
                    testTag = "settings_hub_routing",
                    onClick = { onNavigate(SettingsRoute.ROUTING) },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_plug,
                    title = "Connection & Network",
                    subtitle = "DNS ${connectionSettings.dnsProvider} · ${if (connectionSettings.smartSwitchEnabled) "Smart Switch on" else "Manual switch"}",
                    palette = palette,
                    testTag = "settings_hub_connection",
                    onClick = { onNavigate(SettingsRoute.CONNECTION) },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_home_storage,
                    title = "Backup & Updates",
                    subtitle = if (updateState.available != null) {
                        "Update ${updateState.available.tag} ready"
                    } else {
                        "v${BuildConfig.VERSION_NAME} · Backup & cloud sync"
                    },
                    palette = palette,
                    testTag = "settings_hub_backup",
                    onClick = { onNavigate(SettingsRoute.BACKUP) },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_tune,
                    title = "Advanced & Diagnostics",
                    subtitle = "GeoIP databases, speed probes, spoof & core logs",
                    palette = palette,
                    testTag = "settings_hub_advanced",
                    onClick = { onNavigate(SettingsRoute.ADVANCED) },
                )
            }
        }
    }
}

@Composable
private fun SettingsAppearancePage(
    isStyleB: Boolean,
    palette: SettingsPalette,
    settings: UiSettings,
    onBack: () -> Unit,
    onUpdateSettings: ((UiSettings) -> UiSettings) -> Unit,
) {
    val activeChipBg = palette.switchActive
    val activeChipFg = palette.switchThumb

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSubHeader(
                title = "Appearance",
                subtitle = "Design style, app icon, flags & privacy",
                palette = palette,
                onBack = onBack,
            )
        }

        item {
            SettingsGroupCard(palette = palette, title = "Design Style") {
                val styles = listOf(
                    Triple(
                        DesignStyle.KIRAN_A,
                        "Style A · Kiran",
                        "Emerald & Mint · Organic morphing blob",
                    ) to (KiranColors.Mint to KiranColors.OnMint),
                    Triple(
                        DesignStyle.SARHAD_B,
                        "Style B · Sarhad",
                        "Deep Indigo & Violet · Rotating seal",
                    ) to (SarhadColors.Primary to Color.White),
                )
                styles.forEachIndexed { idx, (info, badgeColors) ->
                    val (style, title, subtitle) = info
                    val selected = settings.designStyle == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) palette.iconTintBg else palette.innerPillBg)
                            .then(
                                if (selected) Modifier.border(1.5.dp, palette.accent, RoundedCornerShape(18.dp))
                                else Modifier
                            )
                            .clickable { onUpdateSettings { it.copy(designStyle = style) } }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(badgeColors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (style == DesignStyle.KIRAN_A) "A" else "B",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = badgeColors.second,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = palette.textPrimary,
                            )
                            Text(
                                text = subtitle,
                                fontFamily = OutfitFont,
                                fontSize = 12.sp,
                                color = palette.textMuted,
                            )
                        }
                        if (selected) {
                            Text(
                                text = "Active",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = palette.accent,
                            )
                        }
                    }
                    if (idx == 0) Spacer(Modifier.height(8.dp))
                }
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "App Icon") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        Triple(IconVariant.CHUMPY, "Mint", KiranColors.Bg to KiranColors.Mint),
                        Triple(IconVariant.INVERTED, "Inverted", KiranColors.Mint to KiranColors.OnMint),
                        Triple(IconVariant.DARK, "Dark", Color(0xFF070A09) to Color.White),
                        Triple(IconVariant.THEMED, "Themed", SarhadColors.SelectedServer to SarhadColors.PrimaryLight),
                    ).forEach { (variant, label, colors) ->
                        val selected = settings.iconVariant == variant
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.innerPillBg)
                                .then(
                                    if (selected) Modifier.border(2.dp, palette.accent, RoundedCornerShape(18.dp))
                                    else Modifier
                                )
                                .clickable { onUpdateSettings { it.copy(iconVariant = variant) } }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.first)
                                    .border(1.dp, palette.textMuted.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                KiranRayIcon(
                                    modifier = Modifier.size(34.dp),
                                    rayColor = colors.second,
                                    centerCutoutColor = colors.first,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = label,
                                fontFamily = OutfitFont,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (selected) palette.accent else palette.textPrimary,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "Flags & Privacy") {
                Text(
                    text = "Country Flags",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        FlagStyle.TWEMOJI to "Twemoji",
                        FlagStyle.SYSTEM to "Emoji",
                        FlagStyle.GLOBE to "Globe Only",
                    ).forEach { (flagStyle, label) ->
                        val active = settings.flagStyle == flagStyle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (active) activeChipBg else palette.innerPillBg)
                                .clickable { onUpdateSettings { it.copy(flagStyle = flagStyle) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (active) activeChipFg else palette.textPrimary,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SettingsToggleRow(
                    title = "Hide Server Addresses",
                    subtitle = "Blur IPs & domains when taking screenshots",
                    checked = settings.maskAddresses,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(maskAddresses = enabled) }
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsRoutingPage(
    isStyleB: Boolean,
    palette: SettingsPalette,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    onBack: () -> Unit,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onOpenPerAppProxy: () -> Unit,
    onOpenCustomRouting: () -> Unit,
) {
    val customRuleCount = remember(connectionSettings.customRoutingRulesJson) {
        CustomRoutingRule.listFromJson(connectionSettings.customRoutingRulesJson).count { it.enabled }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSubHeader(
                title = "Routing & Split Tunnel",
                subtitle = "Choose which apps and websites use the VPN",
                palette = palette,
                onBack = onBack,
            )
        }

        item {
            SettingsGroupCard(palette = palette, title = "App Split Tunnel") {
                SettingsToggleRow(
                    title = "Enable Per-App Split Tunnel",
                    subtitle = "Let chosen apps use your normal connection",
                    checked = connectionSettings.perAppEnabled,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(perAppEnabled = enabled) }
                    },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_apps,
                    title = "Apps Skipping VPN",
                    subtitle = if (connectionSettings.bypassedApps.isEmpty()) {
                        "No apps excluded yet (e.g. banking)"
                    } else {
                        "${connectionSettings.bypassedApps.size} app(s) excluded"
                    },
                    palette = palette,
                    onClick = onOpenPerAppProxy,
                )
            }
        }

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

        item {
            SettingsGroupCard(palette = palette, title = "Local & Custom Rules") {
                SettingsToggleRow(
                    title = "Keep Local Wi-Fi Devices Direct",
                    subtitle = "Printers, smart TVs, and routers stay reachable",
                    checked = connectionSettings.bypassLan,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(bypassLan = enabled) }
                    },
                )
                SettingsToggleRow(
                    title = "Skip Local Country Websites",
                    subtitle = "Open domestic sites faster without VPN",
                    checked = connectionSettings.bypassDomestic,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(bypassDomestic = enabled) }
                    },
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_public,
                    title = "Custom Website & IP Rules",
                    subtitle = if (customRuleCount == 0) "No custom rules added" else "$customRuleCount active rule(s)",
                    palette = palette,
                    onClick = onOpenCustomRouting,
                )
            }
        }
    }
}

@Composable
private fun SettingsConnectionPage(
    palette: SettingsPalette,
    connectionSettings: ConnectionSettings,
    onBack: () -> Unit,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onOpenDnsModal: () -> Unit,
    onOpenFragmentModal: () -> Unit,
    onOpenProxyDnsModal: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSubHeader(
                title = "Connection & Network",
                subtitle = "DNS, failover, anti-DPI & hotspot sharing",
                palette = palette,
                onBack = onBack,
            )
        }

        item {
            SettingsGroupCard(palette = palette, title = "DNS & Failover") {
                SettingsHubRow(
                    iconRes = R.drawable.ic_public,
                    title = "Private DNS",
                    subtitle = connectionSettings.dnsProvider,
                    palette = palette,
                    onClick = onOpenDnsModal,
                )
                SettingsToggleRow(
                    title = "Smart Auto-Switch",
                    subtitle = "Switch automatically to a working server if this one stalls",
                    checked = connectionSettings.smartSwitchEnabled,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(smartSwitchEnabled = enabled) }
                    },
                )
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "Automation") {
                SettingsToggleRow(
                    title = "Auto-Connect on Open Wi-Fi",
                    subtitle = "Turn VPN on automatically on untrusted Wi-Fi",
                    checked = connectionSettings.autoConnectUntrustedWifi,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(autoConnectUntrustedWifi = enabled) }
                    },
                )
                SettingsToggleRow(
                    title = "Start on Device Boot",
                    subtitle = "Start VPN automatically when phone restarts",
                    checked = connectionSettings.startOnBoot,
                    palette = palette,
                    onCheckedChange = { enabled ->
                        onUpdateConnection { it.copy(startOnBoot = enabled) }
                    },
                )
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "Censorship & Sharing") {
                SettingsHubRow(
                    iconRes = R.drawable.ic_tune,
                    title = "Bypass Censorship (Anti-DPI)",
                    subtitle = if (connectionSettings.fragmentEnabled) {
                        "Active (${connectionSettings.fragmentLength})"
                    } else {
                        "Off — split packets if sites block TLS"
                    },
                    palette = palette,
                    onClick = onOpenFragmentModal,
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_plug,
                    title = "Network, Hotspot Share & Proxy Ports",
                    subtitle = when {
                        connectionSettings.proxyOnlyMode -> "Proxy-Only Mode (No TUN)"
                        connectionSettings.lanSharingEnabled -> "Wi-Fi / Hotspot sharing active"
                        else -> "Share VPN over Wi-Fi / Hotspot"
                    },
                    palette = palette,
                    onClick = onOpenProxyDnsModal,
                )
            }
        }
    }
}

@Composable
private fun SettingsBackupPage(
    palette: SettingsPalette,
    coreVersion: String?,
    updateState: UpdateUiState,
    onBack: () -> Unit,
    onOpenBackupModal: () -> Unit,
    onCheckForUpdate: () -> Unit,
    onUpdateAction: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSubHeader(
                title = "Backup & Updates",
                subtitle = "Keep your servers safe and stay up to date",
                palette = palette,
                onBack = onBack,
            )
        }

        item {
            SettingsGroupCard(palette = palette, title = "Backup & Cloud Sync") {
                SettingsHubRow(
                    iconRes = R.drawable.ic_home_storage,
                    title = "Backup & Restore",
                    subtitle = "Save or restore servers & settings via file or WebDAV",
                    palette = palette,
                    onClick = onOpenBackupModal,
                )
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "App Info") {
                SettingsActionRow(
                    title = "Hollow VPN v${BuildConfig.VERSION_NAME}",
                    subtitle = coreVersion ?: "Xray core ready",
                    valueText = if (updateState.checking) "Checking…" else "Check updates",
                    palette = palette,
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
                        palette = palette,
                        onClick = onUpdateAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsAdvancedPage(
    palette: SettingsPalette,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    subscriptions: List<SubscriptionEntity>,
    coreRunning: Boolean,
    onBack: () -> Unit,
    onOpenGeodataModal: () -> Unit,
    onOpenDelayTestModal: () -> Unit,
    onOpenHappSpoofModal: () -> Unit,
    onOpenLogsModal: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSubHeader(
                title = "Advanced & Diagnostics",
                subtitle = "Databases, speed probes, spoofing & logs",
                palette = palette,
                onBack = onBack,
            )
        }

        item {
            SettingsGroupCard(palette = palette, title = "Pro Tools") {
                SettingsHubRow(
                    iconRes = R.drawable.ic_database,
                    title = "Routing Databases (GeoIP / GeoSite)",
                    subtitle = if (connectionSettings.cdnMirrorFallback) {
                        "CDN mirror fallback active"
                    } else {
                        "Update or reset country & ad databases"
                    },
                    palette = palette,
                    onClick = onOpenGeodataModal,
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_speed_normal,
                    title = "Speed Test URL & Auto-Refresh",
                    subtitle = "${subscriptions.count { it.autoUpdate }}/${subscriptions.size} auto-refresh · ${connectionSettings.delayTestUrl}",
                    palette = palette,
                    onClick = onOpenDelayTestModal,
                )
                SettingsHubRow(
                    iconRes = R.drawable.ic_device,
                    title = "Panel Device Fingerprint (Happ Spoof)",
                    subtitle = if (settings.happSpoof) "Active (${settings.happDeviceModel})" else "Disabled",
                    palette = palette,
                    onClick = onOpenHappSpoofModal,
                )
            }
        }

        item {
            SettingsGroupCard(palette = palette, title = "Diagnostics") {
                SettingsHubRow(
                    iconRes = R.drawable.ic_info_fill,
                    title = "Developer Core Logs",
                    subtitle = if (coreRunning) "Core running · Inspect live diagnostics" else "View last session logs",
                    palette = palette,
                    onClick = onOpenLogsModal,
                )
            }
        }
    }
}

@Composable
private fun SettingsSubHeader(
    title: String,
    subtitle: String,
    palette: SettingsPalette,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.cardBg)
                .clickable(onClick = onBack)
                .testTag("settings_subpage_back"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Back to Settings",
                tint = palette.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp,
                letterSpacing = (-0.5).sp,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = palette.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingsGroupCard(
    palette: SettingsPalette,
    title: String?,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(palette.cardRadius),
        color = palette.cardBg,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.3.sp,
                    color = palette.accent,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(4.dp))
            }
            content()
        }
    }
}

@Composable
private fun SettingsHubRow(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    palette: SettingsPalette,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .let { if (testTag != null) it.testTag(testTag) else it }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(palette.iconTintBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = palette.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = palette.textMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: SettingsPalette,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = palette.textMuted,
            )
        }
        Spacer(Modifier.width(12.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = palette.switchActive,
            activeThumbColor = palette.switchThumb,
            inactiveTrackColor = palette.switchInactive,
            inactiveThumbColor = palette.textMuted,
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    valueText: String,
    palette: SettingsPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = palette.textMuted,
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
            color = palette.accent,
        )
    }
}
