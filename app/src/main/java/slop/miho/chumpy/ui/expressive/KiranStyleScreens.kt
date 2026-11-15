package slop.miho.chumpy.ui.expressive

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import slop.miho.chumpy.BuildConfig
import slop.miho.chumpy.R
import slop.miho.chumpy.core.TunnelState
import slop.miho.chumpy.core.TunnelStatus
import slop.miho.chumpy.core.stripFlag
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.FlagStyle
import slop.miho.chumpy.data.IconVariant
import slop.miho.chumpy.data.UiSettings
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.ui.HomeUiState
import slop.miho.chumpy.ui.MainViewModel
import slop.miho.chumpy.ui.ServersUiState
import slop.miho.chumpy.ui.StatsUiState
import slop.miho.chumpy.ui.UpdateUiState
import slop.miho.chumpy.ui.components.CountryFlag
import slop.miho.chumpy.ui.components.splitLeadingFlag
import slop.miho.chumpy.ui.wallpapers.WallpaperActivity

// ─────────────────────────────────────────────────────────────────────────────
// STYLE A — KIRAN (Material 3 Expressive · Emerald / Mint / Peach) — HOLLOW VPN
// ─────────────────────────────────────────────────────────────────────────────

enum class KiranTab(val label: String, val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    SERVERS("Servers", R.drawable.ic_servers),
    ROUTING("Routing", R.drawable.ic_swap_horizontal),
    SETTINGS("Settings", R.drawable.ic_settings),
}

// ─────────────────────────────────────────────────────────────────────────────
// WELCOME / ONBOARDING SCREEN (STYLE A — KIRAN)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun KiranWelcomeScreen(
    onGetStarted: () -> Unit,
    onSubmitConfig: (String, String?, (String?) -> Unit) -> Unit,
    onScanQr: () -> Unit,
    onImportFile: () -> Unit,
    onSwitchStyle: (DesignStyle) -> Unit,
) {
    var showImportSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KiranColors.Bg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar with Style switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(KiranColors.Mint),
                    )
                    Text(
                        text = "HOLLOW VPN",
                        fontFamily = OutfitFont,
                        color = KiranColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(KiranColors.Card)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    KiranStyleChipSmall("A · Kiran", active = true) {}
                    KiranStyleChipSmall("B · Sarhad", active = false) {
                        onSwitchStyle(DesignStyle.SARHAD_B)
                    }
                }
            }

            Spacer(Modifier.weight(0.7f))

            // Morphing Organic Blob Hero Emblem
            Box(
                modifier = Modifier.size(210.dp),
                contentAlignment = Alignment.Center,
            ) {
                ExpandingPulseRings(
                    modifier = Modifier.fillMaxSize(),
                    color = KiranColors.Mint,
                    baseDiameter = 172.dp,
                )
                MorphingBlobBox(
                    modifier = Modifier.size(168.dp),
                    color = KiranColors.CardActive,
                    haloColor = KiranColors.BlobHalo,
                    haloSpread = 14.dp,
                    breathe = true,
                ) {
                    KiranRayIcon(
                        modifier = Modifier.size(96.dp),
                        rayColor = KiranColors.Mint,
                        centerCutoutColor = KiranColors.CardActive,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Private, fluid,\nuntethered.",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                color = KiranColors.TextPrimary,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Hollow VPN routes your traffic through Xray-core — VLESS, VMess, Trojan, Shadowsocks, Hysteria2 & WireGuard — with zero bundled or fake servers.",
                fontFamily = OutfitFont,
                color = KiranColors.TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(KiranColors.Mint)
                        .clickable { onGetStarted() }
                        .testTag("kiran_get_started_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Enter Hollow VPN",
                        fontFamily = OutfitFont,
                        color = KiranColors.OnMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiranWelcomeActionBtn(
                        iconRes = R.drawable.ic_content_paste,
                        label = "Paste Link / Sub",
                        modifier = Modifier.weight(1f),
                        onClick = { showImportSheet = true },
                    )
                    KiranWelcomeActionBtn(
                        iconRes = R.drawable.ic_qr_code_scanner,
                        label = "Scan QR",
                        modifier = Modifier.weight(1f),
                        onClick = onScanQr,
                    )
                    KiranWelcomeActionBtn(
                        iconRes = R.drawable.ic_home_storage,
                        label = "Config File",
                        modifier = Modifier.weight(1f),
                        onClick = onImportFile,
                    )
                }
            }
        }

        if (showImportSheet) {
            ImportProfileModal(
                isStyleB = false,
                onDismiss = { showImportSheet = false },
                onSubmitText = { text, name, onResult ->
                    onSubmitConfig(text, name) { error ->
                        onResult(error)
                        if (error == null) {
                            onGetStarted()
                        }
                    }
                },
                onScanQr = onScanQr,
                onImportFile = onImportFile,
            )
        }
    }
}

@Composable
private fun KiranStyleChipSmall(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) KiranColors.Mint else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            color = if (active) KiranColors.OnMint else KiranColors.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KiranWelcomeActionBtn(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(KiranColors.Card)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = KiranColors.Mint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            fontFamily = OutfitFont,
            color = KiranColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SHELL (STYLE A — KIRAN)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun KiranMainShell(
    viewModel: MainViewModel,
    tunnelStatus: TunnelStatus,
    home: HomeUiState,
    servers: ServersUiState,
    stats: StatsUiState,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    selectedProfile: ProfileEntity?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenPerAppProxy: () -> Unit,
    onScanQr: () -> Unit,
    onImportFile: () -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(KiranTab.HOME) }

    var showImportModal by remember { mutableStateOf(false) }
    var showLauncherIconModal by remember { mutableStateOf(false) }
    var showDnsModal by remember { mutableStateOf(false) }
    var showFragmentModal by remember { mutableStateOf(false) }
    var showStyleModal by remember { mutableStateOf(false) }
    var showHappSpoofModal by remember { mutableStateOf(false) }
    var showDelayTestModal by remember { mutableStateOf(false) }
    var showLogsModal by remember { mutableStateOf(false) }

    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val selectedSubscription by viewModel.selectedSubscription.collectAsStateWithLifecycle()
    val manualProfileCount by viewModel.manualProfileCount.collectAsStateWithLifecycle()
    val coreVersion by viewModel.coreVersion.collectAsStateWithLifecycle()
    val coreRunning by viewModel.coreRunning.collectAsStateWithLifecycle()
    val coreLogs by viewModel.coreLogs.collectAsStateWithLifecycle()
    val updateState by viewModel.update.collectAsStateWithLifecycle()

    BackHandler(enabled = activeTab != KiranTab.HOME) {
        activeTab = KiranTab.HOME
    }

    val isConnected = tunnelStatus.state == TunnelState.UP
    val isBusy = tunnelStatus.state == TunnelState.CONNECTING ||
        tunnelStatus.state == TunnelState.DISCONNECTING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KiranColors.Bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues()),
        ) {
            if (activeTab == KiranTab.HOME) {
                KiranTopHeader(
                    onOpenAppearance = { showStyleModal = true },
                    onOpenLogs = { showLogsModal = true },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(160)) },
                    label = "kiranTabTransition",
                ) { tab ->
                    when (tab) {
                        KiranTab.HOME -> KiranHomeTab(
                            tunnelStatus = tunnelStatus,
                            home = home,
                            stats = stats,
                            settings = settings,
                            connectionSettings = connectionSettings,
                            selectedProfile = selectedProfile,
                            updateState = updateState,
                            onToggleConnect = {
                                if (isConnected || isBusy) {
                                    onDisconnect()
                                } else if (selectedProfile == null) {
                                    showImportModal = true
                                } else {
                                    onConnect()
                                }
                            },
                            onOpenServers = { activeTab = KiranTab.SERVERS },
                            onRefreshPing = { viewModel.refreshPing(showPending = true) },
                            onOpenRouting = { activeTab = KiranTab.ROUTING },
                            onOpenDns = { showDnsModal = true },
                            onUpdateChipTap = { viewModel.onUpdateChipTap() },
                        )

                        KiranTab.SERVERS -> ExpressiveServersTab(
                            isStyleB = false,
                            servers = servers,
                            stats = stats,
                            settings = settings,
                            subscriptions = subscriptions,
                            selectedSubscription = selectedSubscription,
                            manualProfileCount = manualProfileCount,
                            selectedProfileId = selectedProfile?.id,
                            onSelectProfile = { profile ->
                                viewModel.selectProfile(profile.id)
                            },
                            onSelectSubscription = { viewModel.selectSubscription(it) },
                            onUpdateSubscription = { viewModel.updateSelectedSubscription() },
                            onDeleteSubscription = { viewModel.deleteSubscription(it) },
                            onToggleSubscriptionPinned = { id, pinned ->
                                viewModel.toggleSubscriptionPinned(id, pinned)
                            },
                            onToggleSubscriptionAutoUpdate = { id, enabled ->
                                viewModel.setSubscriptionAutoUpdate(id, enabled)
                            },
                            onToggleProfilePinned = { id, pinned ->
                                viewModel.toggleProfilePinned(id, pinned)
                            },
                            onToggleProfileConcealed = { id, concealed ->
                                viewModel.toggleProfileConcealed(id, concealed)
                            },
                            onPingAll = { viewModel.pingServers() },
                            onOpenImport = { showImportModal = true },
                            onUpdateAll = { viewModel.updateAllSubscriptions() },
                            onSetSort = { viewModel.setServersSort(it) },
                            onDeleteDuplicates = { viewModel.deleteDuplicateProfiles() },
                            onDeleteInvalid = { viewModel.deleteInvalidProfiles() },
                            onDeleteSelected = { viewModel.deleteProfiles(it) },
                            onExportAll = { viewModel.exportAllProfiles() },
                            onDeleteAll = { viewModel.deleteAllProfiles() },
                        )

                        KiranTab.ROUTING -> KiranRoutingTab(
                            connectionSettings = connectionSettings,
                            flagStyle = settings.flagStyle,
                            onUpdateConnection = { transform -> viewModel.updateConnection(transform) },
                            onOpenPerAppProxy = onOpenPerAppProxy,
                            onOpenDnsModal = { showDnsModal = true },
                            onOpenFragmentModal = { showFragmentModal = true },
                        )

                        KiranTab.SETTINGS -> KiranSettingsTab(
                            settings = settings,
                            connectionSettings = connectionSettings,
                            subscriptions = subscriptions,
                            coreVersion = coreVersion,
                            coreRunning = coreRunning,
                            updateState = updateState,
                            onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                            onUpdateConnection = { transform -> viewModel.updateConnection(transform) },
                            onOpenLauncherIconModal = { showLauncherIconModal = true },
                            onOpenDnsModal = { showDnsModal = true },
                            onOpenFragmentModal = { showFragmentModal = true },
                            onOpenPerAppProxy = onOpenPerAppProxy,
                            onOpenStyleModal = { showStyleModal = true },
                            onOpenHappSpoofModal = { showHappSpoofModal = true },
                            onOpenDelayTestModal = { showDelayTestModal = true },
                            onOpenLogsModal = { showLogsModal = true },
                            onCheckForUpdate = { viewModel.recheckUpdate() },
                            onUpdateAction = { viewModel.onUpdateChipTap() },
                        )
                    }
                }
            }

            KiranBottomDock(
                activeTab = activeTab,
                onSelectTab = { activeTab = it },
                modifier = Modifier
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }

        if (showImportModal) {
            ImportProfileModal(
                isStyleB = false,
                onDismiss = { showImportModal = false },
                onSubmitText = { text, name, onResult ->
                    viewModel.addSubscription(text, name, onResult)
                },
                onScanQr = onScanQr,
                onImportFile = onImportFile,
            )
        }

        if (showLauncherIconModal) {
            KiranLauncherIconModal(
                currentVariant = settings.iconVariant,
                onSelectVariant = { variant ->
                    viewModel.updateSettings { it.copy(iconVariant = variant) }
                },
                onDismiss = { showLauncherIconModal = false },
            )
        }

        if (showDnsModal) {
            DnsPickerModal(
                isStyleB = false,
                currentDns = connectionSettings.dnsProvider,
                onSelectDns = { provider ->
                    viewModel.updateConnection { it.copy(dnsProvider = provider) }
                },
                onDismiss = { showDnsModal = false },
            )
        }

        if (showFragmentModal) {
            FragmentMuxModal(
                isStyleB = false,
                connectionSettings = connectionSettings,
                onUpdateConnection = { transform -> viewModel.updateConnection(transform) },
                onDismiss = { showFragmentModal = false },
            )
        }

        if (showStyleModal) {
            AppearanceStyleModal(
                settings = settings,
                onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                onOpenLauncherShowcase = { showLauncherIconModal = true },
                onDismiss = { showStyleModal = false },
            )
        }

        if (showHappSpoofModal) {
            HappSpoofModal(
                isStyleB = false,
                settings = settings,
                onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                onDismiss = { showHappSpoofModal = false },
            )
        }

        if (showDelayTestModal) {
            DelayTestAndRefreshModal(
                isStyleB = false,
                connectionSettings = connectionSettings,
                subscriptions = subscriptions,
                maskAddresses = settings.maskAddresses,
                onUpdateConnection = { transform -> viewModel.updateConnection(transform) },
                onToggleAutoUpdate = { id, enabled -> viewModel.setSubscriptionAutoUpdate(id, enabled) },
                onUpdateAllSubscriptions = { viewModel.updateAllSubscriptions() },
                onPingAll = { viewModel.pingServers() },
                onDismiss = { showDelayTestModal = false },
            )
        }

        if (showLogsModal) {
            RealLogsModal(
                isStyleB = false,
                coreLogs = coreLogs,
                tunnelStatus = tunnelStatus,
                selectedProfile = selectedProfile,
                connectionSettings = connectionSettings,
                coreVersion = coreVersion,
                pingMillis = home.pingMillis,
                onDismiss = { showLogsModal = false },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP HEADER (STYLE A)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun KiranTopHeader(
    onOpenAppearance: () -> Unit,
    onOpenLogs: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(KiranColors.CardActive),
                contentAlignment = Alignment.Center,
            ) {
                KiranRayIcon(
                    modifier = Modifier.size(22.dp),
                    rayColor = KiranColors.Mint,
                    centerCutoutColor = KiranColors.CardActive,
                )
            }
            Text(
                text = "Hollow VPN",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                color = KiranColors.TextPrimary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KiranColors.Card)
                    .clickable(onClick = onOpenAppearance)
                    .testTag("kiran_style_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_appearance),
                    contentDescription = "Appearance & Launcher",
                    tint = KiranColors.Mint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KiranColors.Card)
                    .clickable(onClick = onOpenLogs)
                    .testTag("kiran_logs_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = "Core Logs",
                    tint = KiranColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1: HOME (STYLE A)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun KiranHomeTab(
    tunnelStatus: TunnelStatus,
    home: HomeUiState,
    stats: StatsUiState,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    selectedProfile: ProfileEntity?,
    updateState: UpdateUiState,
    onToggleConnect: () -> Unit,
    onOpenServers: () -> Unit,
    onRefreshPing: () -> Unit,
    onOpenRouting: () -> Unit,
    onOpenDns: () -> Unit,
    onUpdateChipTap: () -> Unit,
) {
    val isConnected = tunnelStatus.state == TunnelState.UP
    val isBusy = tunnelStatus.state == TunnelState.CONNECTING ||
        tunnelStatus.state == TunnelState.DISCONNECTING

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isConnected, home.uptimeStartMillis) {
        while (isConnected && home.uptimeStartMillis != null) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedMillis = if (isConnected && home.uptimeStartMillis != null) {
        (nowMillis - home.uptimeStartMillis).coerceAtLeast(0L)
    } else {
        0L
    }

    val blobColor by animateColorAsState(
        targetValue = when {
            isConnected -> KiranColors.Mint
            isBusy -> KiranColors.Peach
            else -> KiranColors.CardActive
        },
        animationSpec = tween(400),
        label = "kiranHeroColor",
    )
    val blobIconColor = when {
        isConnected -> KiranColors.OnMint
        isBusy -> KiranColors.OnPeach
        else -> KiranColors.Mint
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero Morphing Blob Connect Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isConnected) {
                        ExpandingPulseRings(
                            modifier = Modifier.fillMaxSize(),
                            color = KiranColors.Mint,
                            baseDiameter = 184.dp,
                        )
                    } else if (isBusy) {
                        SpinningDashedRing(
                            modifier = Modifier.size(216.dp),
                            color = KiranColors.Peach,
                            strokeWidth = 4.dp,
                        )
                    }

                    MorphingBlobBox(
                        modifier = Modifier
                            .size(182.dp)
                            .testTag("kiran_connect_button"),
                        color = blobColor,
                        haloColor = if (isConnected) KiranColors.TagBg else KiranColors.BlobHalo,
                        haloSpread = 14.dp,
                        breathe = true,
                        onClick = onToggleConnect,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PowerVectorIcon(
                                modifier = Modifier.size(46.dp),
                                color = blobIconColor,
                                strokeWidth = 3.2.dp,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when {
                                    tunnelStatus.state == TunnelState.CONNECTING -> "CONNECTING"
                                    tunnelStatus.state == TunnelState.DISCONNECTING -> "STOPPING"
                                    isConnected -> "CONNECTED"
                                    selectedProfile == null -> "ADD SERVER"
                                    else -> "CONNECT"
                                },
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = blobIconColor,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isConnected) formatElapsedHms(elapsedMillis) else "Ready to tunnel",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isConnected) KiranColors.Mint else KiranColors.TextMuted,
                )
                if (home.recentLossPercent != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Packet loss warning: ${home.recentLossPercent.toInt()}%",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = KiranColors.Orange,
                    )
                }
            }
        }

        // Real Tunnel Error Card
        tunnelStatus.error?.takeIf { it.isNotBlank() }?.let { err ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = KiranColors.Peach.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = err,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = KiranColors.Peach,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // App Update Chip when a newer release exists
        if (updateState.available != null && !updateState.hidden) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUpdateChipTap),
                    shape = RoundedCornerShape(22.dp),
                    color = KiranColors.TagBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                updateState.downloadedFile != null -> "Update ${updateState.available.tag} ready — Tap to install"
                                updateState.downloading -> "Downloading ${updateState.available.tag} (${updateState.downloadPercent}%)"
                                else -> "Update available: ${updateState.available.tag}"
                            },
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KiranColors.MintSoft,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (updateState.downloading) "Pause" else "Get ›",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = KiranColors.Mint,
                        )
                    }
                }
            }
        }

        // Active Server Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenServers)
                    .testTag("kiran_active_server_card"),
                shape = RoundedCornerShape(30.dp),
                color = KiranColors.Card,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val flag = home.profileFlag
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(KiranColors.CardActive),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (flag != null && settings.flagStyle != FlagStyle.GLOBE) {
                                Text(text = flag, fontSize = 22.sp)
                            } else {
                                KiranRayIcon(
                                    modifier = Modifier.size(26.dp),
                                    rayColor = KiranColors.Mint,
                                    centerCutoutColor = KiranColors.CardActive,
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = home.subscriptionName ?: "Active Server",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (home.subscriptionExpired) KiranColors.Orange else KiranColors.MintSub,
                            )
                            Text(
                                text = home.profileLabel?.takeIf { it.isNotBlank() }
                                    ?: selectedProfile?.let { stripFlag(it.remarks).ifBlank { if (settings.maskAddresses) "Hidden endpoint" else it.server } }
                                    ?: "No server selected",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = KiranColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (selectedProfile != null) {
                                Text(
                                    text = formatProfileProtocolLine(selectedProfile, uppercase = true),
                                    fontFamily = OutfitFont,
                                    fontSize = 12.sp,
                                    color = KiranColors.TextMuted,
                                )
                            } else {
                                Text(
                                    text = "Tap to add or select a server",
                                    fontFamily = OutfitFont,
                                    fontSize = 12.sp,
                                    color = KiranColors.TextMuted,
                                )
                            }
                        }
                    }

                    val activePing = home.pingMillis ?: selectedProfile?.testDelayMillis
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(KiranColors.TagBg)
                            .clickable(onClick = onRefreshPing)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = if (activePing != null && activePing > 0L) "$activePing ms" else "Switch ›",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = KiranColors.Mint,
                        )
                    }
                }
            }
        }

        // Live Speed & Session Traffic Grid (100% real data from StatsUiState)
        item {
            val (downVal, downUnit) = formatSpeedMbps(stats.liveDownBps)
            val (upVal, upUnit) = formatSpeedMbps(stats.liveUpBps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KiranMetricCard(
                    label = "Download",
                    value = "$downVal $downUnit",
                    sub = "Total ↓ ${formatCompactBytes(stats.rxTotal)}",
                    accent = KiranColors.Mint,
                    modifier = Modifier.weight(1f),
                )
                KiranMetricCard(
                    label = "Upload",
                    value = "$upVal $upUnit",
                    sub = "Total ↑ ${formatCompactBytes(stats.txTotal)}",
                    accent = KiranColors.Peach,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Quick Routing & DNS Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenRouting),
                    shape = RoundedCornerShape(24.dp),
                    color = KiranColors.Card,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Routing",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = KiranColors.TextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = connectionSettings.routingMode.name.replace("BYPASS_", "Bypass "),
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KiranColors.TextPrimary,
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenDns),
                    shape = RoundedCornerShape(24.dp),
                    color = KiranColors.Card,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resolver",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = KiranColors.TextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = connectionSettings.dnsProvider,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KiranColors.MintSoft,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KiranMetricCard(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = KiranColors.Card,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = KiranColors.TextMuted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = accent,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = sub,
                fontFamily = OutfitFont,
                fontSize = 11.sp,
                color = KiranColors.TextMuted,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3: ROUTING & SPLIT TUNNEL (STYLE A)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun KiranRoutingTab(
    connectionSettings: ConnectionSettings,
    flagStyle: FlagStyle,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onOpenPerAppProxy: () -> Unit,
    onOpenDnsModal: () -> Unit,
    onOpenFragmentModal: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Routing & Core",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                letterSpacing = (-1).sp,
                color = KiranColors.TextPrimary,
            )
        }

        item {
            RegionRoutingSelectorCard(
                isStyleB = false,
                selectedMode = connectionSettings.routingMode,
                flagStyle = flagStyle,
                onSelectMode = { mode ->
                    onUpdateConnection { it.copy(routingMode = mode) }
                },
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Split Tunneling & Filters",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    KiranToggleRow(
                        title = "Per-App Proxy",
                        subtitle = if (connectionSettings.perAppEnabled) {
                            "${connectionSettings.bypassedApps.size} app(s) bypassed"
                        } else {
                            "Disabled — routing all apps"
                        },
                        checked = connectionSettings.perAppEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(perAppEnabled = enabled) }
                        },
                    )
                    KiranActionRow(
                        title = "Manage Bypassed Apps",
                        subtitle = "Select apps to exclude from VPN tunnel",
                        valueText = "${connectionSettings.bypassedApps.size} ›",
                        onClick = onOpenPerAppProxy,
                    )
                    KiranToggleRow(
                        title = "Bypass LAN & Private Subnets",
                        subtitle = "Keep local devices (192.168.x.x, 10.x.x.x) direct",
                        checked = connectionSettings.bypassLan,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(bypassLan = enabled) }
                        },
                    )
                    KiranToggleRow(
                        title = "Bypass Domestic Sites",
                        subtitle = "Direct routing for local country traffic",
                        checked = connectionSettings.bypassDomestic,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(bypassDomestic = enabled) }
                        },
                    )
                    KiranToggleRow(
                        title = "Block Ads & Trackers",
                        subtitle = "Drop geosite:category-ads-all in Xray core",
                        checked = connectionSettings.blockAds,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(blockAds = enabled) }
                        },
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DNS & Anti-DPI",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    KiranActionRow(
                        title = "DNS Resolver",
                        subtitle = "Encrypted DNS over HTTPS inside tunnel",
                        valueText = "${connectionSettings.dnsProvider} ›",
                        onClick = onOpenDnsModal,
                    )
                    KiranActionRow(
                        title = "TLS Fragment & Mux",
                        subtitle = buildString {
                            append(if (connectionSettings.fragmentEnabled) "Fragment ON (${connectionSettings.fragmentLength})" else "Fragment OFF")
                            append(" · ")
                            append(if (connectionSettings.muxEnabled) "Mux ON (${connectionSettings.muxConcurrency})" else "Mux OFF")
                        },
                        valueText = "Configure ›",
                        onClick = onOpenFragmentModal,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 4: SETTINGS (STYLE A — FULL FEATURE PARITY WITH STYLE B & CLASSIC)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun KiranSettingsTab(
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
    onCheckForUpdate: () -> Unit,
    onUpdateAction: () -> Unit,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Settings",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                letterSpacing = (-1).sp,
                color = KiranColors.TextPrimary,
            )
        }

        // Appearance & Launcher Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Appearance & Launcher",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    KiranActionRow(
                        title = "UI Style, Theme, Fonts & Flags",
                        subtitle = "Style A · Kiran / Style B · Sarhad · ${settings.themeMode.name}",
                        valueText = "Customize ›",
                        onClick = onOpenStyleModal,
                    )
                    KiranActionRow(
                        title = "Launcher Icon",
                        subtitle = when (settings.iconVariant) {
                            IconVariant.CHUMPY -> "Hollow Mint"
                            IconVariant.INVERTED -> "Inverted Mint"
                            IconVariant.DARK -> "Night Dark"
                            IconVariant.THEMED -> "Themed Monochrome"
                        },
                        valueText = "Change ›",
                        onClick = onOpenLauncherIconModal,
                    )
                    KiranActionRow(
                        title = "Wallpaper & Backdrop",
                        subtitle = "Custom image backdrop, blur & dim",
                        valueText = "Open ›",
                        onClick = {
                            context.startActivity(Intent(context, WallpaperActivity::class.java))
                        },
                    )
                    KiranToggleRow(
                        title = "Mask Server Addresses",
                        subtitle = "Hide domains, IPs and subscription URLs",
                        checked = settings.maskAddresses,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(maskAddresses = enabled) }
                        },
                    )
                }
            }
        }

        // Connection & Protection (Migrated from Classic Connection Settings)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Connection & Protection",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    KiranToggleRow(
                        title = "Auto Reconnect",
                        subtitle = "Automatically restore tunnel after network drops or VPN restarts",
                        checked = connectionSettings.autoReconnect,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(autoReconnect = enabled) }
                        },
                    )
                    KiranToggleRow(
                        title = "Kill Switch",
                        subtitle = "Prevent leaks if VPN disconnects unexpectedly",
                        checked = connectionSettings.killSwitch,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(killSwitch = enabled) }
                        },
                    )
                    KiranToggleRow(
                        title = "Untrusted Wi-Fi Auto-Connect",
                        subtitle = "Automatically enable VPN on open or unfamiliar Wi-Fi",
                        checked = connectionSettings.autoConnectUntrustedWifi,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(autoConnectUntrustedWifi = enabled) }
                        },
                    )
                    KiranToggleRow(
                        title = "Start on Boot",
                        subtitle = "Start Hollow VPN automatically after device restart",
                        checked = connectionSettings.startOnBoot,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(startOnBoot = enabled) }
                        },
                    )
                    KiranActionRow(
                        title = "Per-App Proxy",
                        subtitle = "${connectionSettings.bypassedApps.size} app(s) bypassed",
                        valueText = "Open ›",
                        onClick = onOpenPerAppProxy,
                    )
                    KiranActionRow(
                        title = "DNS Resolver",
                        subtitle = connectionSettings.dnsProvider,
                        valueText = "Edit ›",
                        onClick = onOpenDnsModal,
                    )
                    KiranActionRow(
                        title = "TLS Fragment & Mux",
                        subtitle = "Anti-DPI packet splitting & multiplexing",
                        valueText = "Edit ›",
                        onClick = onOpenFragmentModal,
                    )
                }
            }
        }

        // Subscriptions, Spoof & Diagnostics
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Subscriptions & Diagnostics",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    KiranActionRow(
                        title = "Delay Test URL & Auto-Update",
                        subtitle = "${subscriptions.count { it.autoUpdate }}/${subscriptions.size} auto-refresh · ${connectionSettings.delayTestUrl}",
                        valueText = "Configure ›",
                        onClick = onOpenDelayTestModal,
                    )
                    KiranActionRow(
                        title = "Spoof as Happ",
                        subtitle = if (settings.happSpoof) "Active (${settings.happDeviceModel})" else "Disabled",
                        valueText = "Configure ›",
                        onClick = onOpenHappSpoofModal,
                    )
                    KiranActionRow(
                        title = "Xray Core Logs",
                        subtitle = if (coreRunning) "Core running · Tap to inspect live logs" else "View diagnostics & last session logs",
                        valueText = "Logs ›",
                        onClick = onOpenLogsModal,
                    )
                }
            }
        }

        // About & Updates
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = KiranColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "About Hollow VPN",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KiranColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                    KiranActionRow(
                        title = "App Version",
                        subtitle = "v${BuildConfig.VERSION_NAME} · ${coreVersion ?: "Xray-core ready"}",
                        valueText = if (updateState.checking) "Checking…" else "Check updates",
                        onClick = onCheckForUpdate,
                    )
                    if (updateState.available != null) {
                        KiranActionRow(
                            title = "Release ${updateState.available.tag}",
                            subtitle = when {
                                updateState.downloadedFile != null -> "APK downloaded and ready"
                                updateState.downloading -> "Downloading ${updateState.downloadPercent}%"
                                else -> "Tap to download latest APK"
                            },
                            valueText = if (updateState.downloadedFile != null) "Install" else "Download",
                            onClick = onUpdateAction,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KiranToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
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
                color = KiranColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = KiranColors.TextMuted,
            )
        }
        Spacer(Modifier.width(12.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = KiranColors.Mint,
            activeThumbColor = KiranColors.OnMint,
            inactiveTrackColor = KiranColors.SwitchOffBg,
            inactiveThumbColor = KiranColors.TextMuted,
        )
    }
}

@Composable
private fun KiranActionRow(
    title: String,
    subtitle: String,
    valueText: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                color = KiranColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = KiranColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = valueText,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = KiranColors.Mint,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM NAVIGATION DOCK (STYLE A — 4-TAB PILL BAR)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun KiranBottomDock(
    activeTab: KiranTab,
    onSelectTab: (KiranTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(KiranColors.NavBg)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KiranTab.entries.forEach { tab ->
            val selected = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) KiranColors.Mint else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .testTag("kiran_tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label,
                        tint = if (selected) KiranColors.OnMint else KiranColors.TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    if (selected) {
                        Text(
                            text = tab.label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = KiranColors.OnMint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
