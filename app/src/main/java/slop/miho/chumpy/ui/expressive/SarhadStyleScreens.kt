package slop.miho.chumpy.ui.expressive

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import slop.miho.chumpy.ui.wallpapers.WallpaperActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// STYLE B — SARHAD (Deep Indigo / Violet / Coral 14-Point Seal) — HOLLOW VPN
// ─────────────────────────────────────────────────────────────────────────────

enum class SarhadTab(val label: String, val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    SERVERS("Servers", R.drawable.ic_servers),
    STATS("Stats", R.drawable.ic_stats),
    SETTINGS("Settings", R.drawable.ic_settings),
}

// ─────────────────────────────────────────────────────────────────────────────
// WELCOME / ONBOARDING SCREEN (STYLE B — SARHAD)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SarhadWelcomeScreen(
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
            .background(SarhadColors.Bg)
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
                            .background(SarhadColors.PrimaryLight),
                    )
                    Text(
                        text = "HOLLOW VPN",
                        fontFamily = OutfitFont,
                        color = SarhadColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SarhadColors.Card)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SarhadStyleChipSmall("A · Kiran", active = false) {
                        onSwitchStyle(DesignStyle.KIRAN_A)
                    }
                    SarhadStyleChipSmall("B · Sarhad", active = true) {}
                }
            }

            Spacer(Modifier.weight(0.75f))

            // 14-point Scalloped Seal Hero Emblem
            SarhadSeal(
                modifier = Modifier.size(198.dp),
                gradientColors = listOf(SarhadColors.Primary, SarhadColors.Pink),
                spinDurationMs = 16000,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    KiranRayIcon(
                        modifier = Modifier.size(72.dp),
                        rayColor = Color.White,
                        centerCutoutColor = Color.Transparent,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "HOLLOW",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Expressive speed.\nUnbroken stealth.",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                color = SarhadColors.TextPrimary,
                fontSize = 33.sp,
                lineHeight = 38.sp,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Hollow VPN pairs deep indigo Material 3 Expressive craft with Xray-core — real latency probes, 7-day traffic metrics, and anti-DPI fragmentation.",
                fontFamily = OutfitFont,
                color = SarhadColors.TextMuted,
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
                        .background(
                            Brush.horizontalGradient(
                                listOf(SarhadColors.Primary, SarhadColors.Pink),
                            ),
                        )
                        .clickable { onGetStarted() }
                        .testTag("sarhad_get_started_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Enter Hollow VPN",
                        fontFamily = OutfitFont,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SarhadWelcomeActionBtn(
                        iconRes = R.drawable.ic_content_paste,
                        label = "Paste Link / Sub",
                        modifier = Modifier.weight(1f),
                        onClick = { showImportSheet = true },
                    )
                    SarhadWelcomeActionBtn(
                        iconRes = R.drawable.ic_qr_code_scanner,
                        label = "Scan QR",
                        modifier = Modifier.weight(1f),
                        onClick = onScanQr,
                    )
                    SarhadWelcomeActionBtn(
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
                isStyleB = true,
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
private fun SarhadStyleChipSmall(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) SarhadColors.Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            color = if (active) Color.White else SarhadColors.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SarhadWelcomeActionBtn(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SarhadColors.Card)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = SarhadColors.PrimaryLight,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            fontFamily = OutfitFont,
            color = SarhadColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SHELL (STYLE B — SARHAD)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SarhadMainShell(
    viewModel: MainViewModel,
    tunnelStatus: TunnelStatus,
    home: HomeUiState,
    servers: ServersUiState,
    stats: StatsUiState,
    settings: UiSettings,
    connectionSettings: ConnectionSettings,
    selectedProfile: ProfileEntity?,
    coreVersion: String?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenPerAppProxy: () -> Unit,
    onScanQr: () -> Unit,
    onImportFile: () -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(SarhadTab.HOME) }

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
    val coreRunning by viewModel.coreRunning.collectAsStateWithLifecycle()
    val coreLogs by viewModel.coreLogs.collectAsStateWithLifecycle()
    val updateState by viewModel.update.collectAsStateWithLifecycle()

    BackHandler(enabled = activeTab != SarhadTab.HOME) {
        activeTab = SarhadTab.HOME
    }

    val isConnected = tunnelStatus.state == TunnelState.UP
    val isBusy = tunnelStatus.state == TunnelState.CONNECTING ||
        tunnelStatus.state == TunnelState.DISCONNECTING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SarhadColors.Bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues()),
        ) {
            if (activeTab == SarhadTab.HOME) {
                SarhadTopHeader(
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
                    label = "sarhadTabTransition",
                ) { tab ->
                    when (tab) {
                        SarhadTab.HOME -> SarhadHomeTab(
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
                            onOpenServers = { activeTab = SarhadTab.SERVERS },
                            onRefreshPing = { viewModel.refreshPing(showPending = true) },
                            onPingSelected = { viewModel.pingSelected() },
                            onOpenLogs = { showLogsModal = true },
                            onOpenDns = { showDnsModal = true },
                            onUpdateChipTap = { viewModel.onUpdateChipTap() },
                        )

                        SarhadTab.SERVERS -> ExpressiveServersTab(
                            isStyleB = true,
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

                        SarhadTab.STATS -> SarhadStatsTab(
                            isConnected = isConnected,
                            home = home,
                            stats = stats,
                            onOpenLogs = { showLogsModal = true },
                        )

                        SarhadTab.SETTINGS -> SarhadSettingsTab(
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

            SarhadBottomDock(
                activeTab = activeTab,
                onSelectTab = { activeTab = it },
                modifier = Modifier
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }

        if (showImportModal) {
            ImportProfileModal(
                isStyleB = true,
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
                isStyleB = true,
                currentDns = connectionSettings.dnsProvider,
                onSelectDns = { provider ->
                    viewModel.updateConnection { it.copy(dnsProvider = provider) }
                },
                onDismiss = { showDnsModal = false },
            )
        }

        if (showFragmentModal) {
            FragmentMuxModal(
                isStyleB = true,
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
                isStyleB = true,
                settings = settings,
                onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                onDismiss = { showHappSpoofModal = false },
            )
        }

        if (showDelayTestModal) {
            DelayTestAndRefreshModal(
                isStyleB = true,
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
                isStyleB = true,
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
// TOP HEADER (STYLE B)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadTopHeader(
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(SarhadColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                KiranRayIcon(
                    modifier = Modifier.size(22.dp),
                    rayColor = Color.White,
                    centerCutoutColor = SarhadColors.Primary,
                )
            }
            Text(
                text = "Hollow VPN",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                color = SarhadColors.TextPrimary,
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
                    .background(SarhadColors.PillBg)
                    .clickable(onClick = onOpenAppearance)
                    .testTag("sarhad_style_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_appearance),
                    contentDescription = "Appearance & Launcher",
                    tint = SarhadColors.PrimaryLight,
                    modifier = Modifier.size(18.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SarhadColors.PillBg)
                    .clickable(onClick = onOpenLogs)
                    .testTag("sarhad_logs_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = "Core Logs",
                    tint = SarhadColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1: HOME (STYLE B — 14-POINT SCALLOPED SEAL)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadHomeTab(
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
    onPingSelected: () -> Unit,
    onOpenLogs: () -> Unit,
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

    val heroCardBg by animateColorAsState(
        targetValue = when {
            isConnected -> SarhadColors.HeroConnected
            isBusy -> SarhadColors.HeroConnecting
            else -> SarhadColors.HeroDisconnected
        },
        animationSpec = tween(400),
        label = "sarhadHeroBg",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Hero Scalloped Seal Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
                color = heroCardBg,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val sealGradient = when {
                        isConnected -> listOf(SarhadColors.Green, SarhadColors.PrimaryLight)
                        isBusy -> listOf(SarhadColors.Pink, SarhadColors.Primary)
                        else -> listOf(SarhadColors.Primary, Color(0xFF4C43B8))
                    }
                    SarhadSeal(
                        modifier = Modifier
                            .size(164.dp)
                            .testTag("sarhad_connect_button"),
                        gradientColors = sealGradient,
                        spinDurationMs = when {
                            isBusy -> 3600
                            isConnected -> 14000
                            else -> null
                        },
                        onClick = onToggleConnect,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PowerVectorIcon(
                                modifier = Modifier.size(44.dp),
                                color = if (isConnected) Color(0xFF0A1410) else Color.White,
                                strokeWidth = 3.dp,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when {
                                    tunnelStatus.state == TunnelState.CONNECTING -> "DIALING"
                                    tunnelStatus.state == TunnelState.DISCONNECTING -> "STOPPING"
                                    isConnected -> "ACTIVE"
                                    selectedProfile == null -> "ADD SERVER"
                                    else -> "CONNECT"
                                },
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = if (isConnected) Color(0xFF0A1410) else Color.White,
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = if (isConnected) formatElapsedHms(elapsedMillis) else "Tap seal to start tunnel",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (isConnected) Color.White else SarhadColors.TextPrimary,
                    )
                    if (home.recentLossPercent != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Loss warning: ${home.recentLossPercent.toInt()}% probes failed",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SarhadColors.Pink,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Hero Detail Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SarhadHeroActionTile(
                            label = if (isConnected) "Live ping" else "Test delay",
                            value = (home.pingMillis ?: selectedProfile?.testDelayMillis)
                                ?.takeIf { it > 0 }
                                ?.let { "$it ms" } ?: "Tap to ping",
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = { if (isConnected) onRefreshPing() else onPingSelected() },
                        )
                        SarhadHeroActionTile(
                            label = "DNS",
                            value = connectionSettings.dnsProvider,
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenDns,
                        )
                        SarhadHeroActionTile(
                            label = "Diagnostics",
                            value = "Core Logs ›",
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenLogs,
                        )
                    }
                }
            }
        }

        // Error card
        tunnelStatus.error?.takeIf { it.isNotBlank() }?.let { err ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = SarhadColors.Pink.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = err,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SarhadColors.Pink,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // App update banner
        if (updateState.available != null && !updateState.hidden) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUpdateChipTap),
                    shape = RoundedCornerShape(22.dp),
                    color = SarhadColors.SelectedServer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                updateState.downloadedFile != null -> "Update ${updateState.available.tag} ready — Install"
                                updateState.downloading -> "Downloading ${updateState.available.tag} (${updateState.downloadPercent}%)"
                                else -> "Update available: ${updateState.available.tag}"
                            },
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SarhadColors.PrimaryLight,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (updateState.downloading) "Pause" else "Get ›",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White,
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
                    .testTag("sarhad_active_server_card"),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
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
                                .background(SarhadColors.PillBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (flag != null && settings.flagStyle != FlagStyle.GLOBE) {
                                Text(text = flag, fontSize = 22.sp)
                            } else {
                                KiranRayIcon(
                                    modifier = Modifier.size(26.dp),
                                    rayColor = SarhadColors.PrimaryLight,
                                    centerCutoutColor = SarhadColors.PillBg,
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = home.subscriptionName ?: "Selected Endpoint",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (home.subscriptionExpired) SarhadColors.Orange else SarhadColors.PrimaryLight,
                            )
                            Text(
                                text = home.profileLabel?.takeIf { it.isNotBlank() }
                                    ?: selectedProfile?.let { stripFlag(it.remarks).ifBlank { if (settings.maskAddresses) "Hidden endpoint" else it.server } }
                                    ?: "No server selected",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = SarhadColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (selectedProfile != null) {
                                    formatProfileProtocolLine(selectedProfile, uppercase = true)
                                } else {
                                    "Tap to open Servers and import or select"
                                },
                                fontFamily = OutfitFont,
                                fontSize = 12.sp,
                                color = SarhadColors.TextMuted,
                            )
                        }
                    }

                    Text(
                        text = "Change ›",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SarhadColors.PrimaryLight,
                    )
                }
            }
        }

        // Live Speed & Session Totals Row
        item {
            val (downVal, downUnit) = formatSpeedMbps(stats.liveDownBps)
            val (upVal, upUnit) = formatSpeedMbps(stats.liveUpBps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SarhadMetricTile(
                    label = "Download",
                    value = "$downVal $downUnit",
                    sub = "Session ↓ ${formatCompactBytes(stats.rxTotal)}",
                    accent = SarhadColors.Green,
                    modifier = Modifier.weight(1f),
                )
                SarhadMetricTile(
                    label = "Upload",
                    value = "$upVal $upUnit",
                    sub = "Session ↑ ${formatCompactBytes(stats.txTotal)}",
                    accent = SarhadColors.Pink,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SarhadHeroActionTile(
    label: String,
    value: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isConnected) SarhadColors.DetailTile else SarhadColors.PillBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontSize = 11.sp,
            color = if (isConnected) SarhadColors.DetailText else SarhadColors.TextMuted,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SarhadMetricTile(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = SarhadColors.Card,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = SarhadColors.TextMuted,
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
                color = SarhadColors.TextMuted,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3: LIVE TELEMETRY & 7-DAY STATS (STYLE B — 100% REAL DATA)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadStatsTab(
    isConnected: Boolean,
    home: HomeUiState,
    stats: StatsUiState,
    onOpenLogs: () -> Unit,
) {
    val dayFmt = remember { DateTimeFormatter.ofPattern("EEE", Locale.US) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Telemetry",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        letterSpacing = (-1).sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Text(
                        text = when {
                            isConnected -> "Live 15s Xray counters & probes"
                            stats.connectedMillis != null -> "Last session frozen (${formatElapsedHms(stats.connectedMillis)})"
                            else -> "Connect tunnel to record live traffic"
                        },
                        fontFamily = OutfitFont,
                        fontSize = 12.sp,
                        color = SarhadColors.TextMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SarhadColors.PillBg)
                        .clickable(onClick = onOpenLogs)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Core Logs",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SarhadColors.PrimaryLight,
                    )
                }
            }
        }

        // Totals Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SarhadMetricTile(
                    label = "Session RX",
                    value = formatCompactBytes(stats.rxTotal),
                    sub = formatSpeedMbps(stats.liveDownBps).let { "${it.first} ${it.second}" },
                    accent = SarhadColors.Green,
                    modifier = Modifier.weight(1f),
                )
                SarhadMetricTile(
                    label = "Session TX",
                    value = formatCompactBytes(stats.txTotal),
                    sub = formatSpeedMbps(stats.liveUpBps).let { "${it.first} ${it.second}" },
                    accent = SarhadColors.Pink,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Live Throughput Sparkline (real samples from StatsUiState.samples)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Throughput Window (15m)",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SarhadColors.TextPrimary,
                        )
                        Text(
                            text = if (home.pingMillis != null) "${home.pingMillis} ms RTT" else "↓ Green · ↑ Pink",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = SarhadColors.TextMuted,
                        )
                    }
                    Spacer(Modifier.height(14.dp))

                    if (stats.samples.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SarhadColors.Bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No traffic samples recorded yet",
                                fontFamily = OutfitFont,
                                fontSize = 13.sp,
                                color = SarhadColors.TextMuted,
                            )
                        }
                    } else {
                        val samples = stats.samples
                        val peak = samples.maxOf { maxOf(it.downBps, it.upBps) }.coerceAtLeast(1L).toFloat()
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SarhadColors.Bg)
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                        ) {
                            val n = samples.size
                            val stepX = if (n <= 1) size.width else size.width / (n - 1).toFloat()
                            val downPath = Path()
                            val upPath = Path()
                            samples.forEachIndexed { idx, s ->
                                val x = if (n == 1) size.width / 2f else idx * stepX
                                val yDown = size.height - (s.downBps / peak) * size.height
                                val yUp = size.height - (s.upBps / peak) * size.height
                                if (idx == 0) {
                                    downPath.moveTo(x, yDown)
                                    upPath.moveTo(x, yUp)
                                } else {
                                    downPath.lineTo(x, yDown)
                                    upPath.lineTo(x, yUp)
                                }
                            }
                            if (n == 1) {
                                val s = samples.first()
                                drawCircle(
                                    color = SarhadColors.Green,
                                    radius = 5.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height - (s.downBps / peak) * size.height),
                                )
                                drawCircle(
                                    color = SarhadColors.Pink,
                                    radius = 4.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height - (s.upBps / peak) * size.height),
                                )
                            } else {
                                drawPath(
                                    path = downPath,
                                    color = SarhadColors.Green,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                                )
                                drawPath(
                                    path = upPath,
                                    color = SarhadColors.Pink,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7-Day Daily Traffic Bar Chart (real persisted data from DailyTrafficStore)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Last 7 Days Usage",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Spacer(Modifier.height(14.dp))

                    val today = LocalDate.now().toEpochDay()
                    val byDay = stats.week.associateBy { it.epochDay }
                    val sevenDays = (-6L..0L).map { offset ->
                        val day = today + offset
                        val entry = byDay[day]
                        Triple(
                            LocalDate.ofEpochDay(day).format(dayFmt),
                            (entry?.rx ?: 0L) + (entry?.tx ?: 0L),
                            day == today,
                        )
                    }
                    val maxTotal = sevenDays.maxOf { it.second }.coerceAtLeast(1L).toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        sevenDays.forEach { (label, total, isToday) ->
                            val fraction = (total / maxTotal).coerceIn(0.05f, 1f)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                            ) {
                                Text(
                                    text = if (total > 0L) formatCompactBytes(total).substringBefore(' ') else "0",
                                    fontFamily = OutfitFont,
                                    fontSize = 10.sp,
                                    color = SarhadColors.TextMuted,
                                    maxLines = 1,
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((76.dp * if (total > 0L) fraction else 0.08f))
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (total == 0L) SarhadColors.PillBg
                                            else if (isToday) SarhadColors.Primary
                                            else SarhadColors.SelectedServer,
                                        ),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontFamily = OutfitFont,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (isToday) SarhadColors.PrimaryLight else SarhadColors.TextMuted,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Probe Loss Summary
        item {
            val totalSent = stats.buckets.sumOf { it.sent }
            val totalLost = stats.buckets.sumOf { it.lost }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = SarhadColors.Card,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Connectivity Probes",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SarhadColors.TextPrimary,
                        )
                        Text(
                            text = if (totalSent == 0) "No 15s probes dispatched yet" else "$totalSent sent · $totalLost lost",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = SarhadColors.TextMuted,
                        )
                    }
                    Text(
                        text = if (totalSent == 0) "—" else "${((totalSent - totalLost) * 100 / totalSent)}% OK",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (stats.recentLossPercent != null) SarhadColors.Pink else SarhadColors.Green,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 4: SETTINGS (STYLE B — FULL FEATURE PARITY WITH STYLE A & CLASSIC)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadSettingsTab(
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
                color = SarhadColors.TextPrimary,
            )
        }

        // Appearance & Launcher Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Appearance & Launcher",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SarhadActionRow(
                        title = "UI Style, Theme, Fonts & Flags",
                        subtitle = "Style A · Kiran / Style B · Sarhad · ${settings.themeMode.name}",
                        valueText = "Customize ›",
                        onClick = onOpenStyleModal,
                    )
                    SarhadActionRow(
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
                    SarhadActionRow(
                        title = "Wallpaper & Backdrop",
                        subtitle = "Custom image backdrop, blur & dim",
                        valueText = "Open ›",
                        onClick = {
                            context.startActivity(Intent(context, WallpaperActivity::class.java))
                        },
                    )
                    SarhadToggleRow(
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

        // Regional Routing Card (Moved from Classic)
        item {
            RegionRoutingSelectorCard(
                isStyleB = true,
                selectedMode = connectionSettings.routingMode,
                flagStyle = settings.flagStyle,
                onSelectMode = { mode ->
                    onUpdateConnection { it.copy(routingMode = mode) }
                },
            )
        }

        // Connection & Protection Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Connection & Protection",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SarhadToggleRow(
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
                    SarhadActionRow(
                        title = "Manage Bypassed Apps",
                        subtitle = "Select apps to exclude from VPN tunnel",
                        valueText = "${connectionSettings.bypassedApps.size} ›",
                        onClick = onOpenPerAppProxy,
                    )
                    SarhadToggleRow(
                        title = "Bypass LAN & Private Subnets",
                        subtitle = "Keep local devices direct",
                        checked = connectionSettings.bypassLan,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(bypassLan = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Bypass Domestic Sites",
                        subtitle = "Direct routing for local country traffic",
                        checked = connectionSettings.bypassDomestic,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(bypassDomestic = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Block Ads & Trackers",
                        subtitle = "Drop geosite:category-ads-all in Xray core",
                        checked = connectionSettings.blockAds,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(blockAds = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Auto Reconnect",
                        subtitle = "Automatically restore tunnel after drops",
                        checked = connectionSettings.autoReconnect,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(autoReconnect = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Kill Switch",
                        subtitle = "Prevent leaks if VPN drops unexpectedly",
                        checked = connectionSettings.killSwitch,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(killSwitch = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Untrusted Wi-Fi Auto-Connect",
                        subtitle = "Automatically enable VPN on open Wi-Fi",
                        checked = connectionSettings.autoConnectUntrustedWifi,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(autoConnectUntrustedWifi = enabled) }
                        },
                    )
                    SarhadToggleRow(
                        title = "Start on Boot",
                        subtitle = "Start Hollow VPN after device restart",
                        checked = connectionSettings.startOnBoot,
                        onCheckedChange = { enabled ->
                            onUpdateConnection { it.copy(startOnBoot = enabled) }
                        },
                    )
                }
            }
        }

        // Core, DNS, Spoof & Diagnostics
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Core, DNS & Diagnostics",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))

                    SarhadActionRow(
                        title = "DNS Resolver",
                        subtitle = connectionSettings.dnsProvider,
                        valueText = "Edit ›",
                        onClick = onOpenDnsModal,
                    )
                    SarhadActionRow(
                        title = "TLS Fragment & Mux",
                        subtitle = buildString {
                            append(if (connectionSettings.fragmentEnabled) "Fragment ON (${connectionSettings.fragmentLength})" else "Fragment OFF")
                            append(" · ")
                            append(if (connectionSettings.muxEnabled) "Mux ON (${connectionSettings.muxConcurrency})" else "Mux OFF")
                        },
                        valueText = "Edit ›",
                        onClick = onOpenFragmentModal,
                    )
                    SarhadActionRow(
                        title = "Delay Test URL & Auto-Update",
                        subtitle = "${subscriptions.count { it.autoUpdate }}/${subscriptions.size} auto-refresh · ${connectionSettings.delayTestUrl}",
                        valueText = "Configure ›",
                        onClick = onOpenDelayTestModal,
                    )
                    SarhadActionRow(
                        title = "Spoof as Happ",
                        subtitle = if (settings.happSpoof) "Active (${settings.happDeviceModel})" else "Disabled",
                        valueText = "Configure ›",
                        onClick = onOpenHappSpoofModal,
                    )
                    SarhadActionRow(
                        title = "Xray Core Logs",
                        subtitle = if (coreRunning) "Core running · Tap to inspect live logs" else "View diagnostics & session logs",
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
                shape = RoundedCornerShape(28.dp),
                color = SarhadColors.Card,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "About Hollow VPN",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SarhadColors.TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                    SarhadActionRow(
                        title = "App Version",
                        subtitle = "v${BuildConfig.VERSION_NAME} · ${coreVersion ?: "Xray-core ready"}",
                        valueText = if (updateState.checking) "Checking…" else "Check updates",
                        onClick = onCheckForUpdate,
                    )
                    if (updateState.available != null) {
                        SarhadActionRow(
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
private fun SarhadToggleRow(
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
                color = SarhadColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = SarhadColors.TextMuted,
            )
        }
        Spacer(Modifier.width(12.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = SarhadColors.Primary,
            activeThumbColor = Color.White,
            inactiveTrackColor = SarhadColors.SwitchOff,
            inactiveThumbColor = SarhadColors.TextMuted,
        )
    }
}

@Composable
private fun SarhadActionRow(
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
                color = SarhadColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = SarhadColors.TextMuted,
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
            color = SarhadColors.PrimaryLight,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM NAVIGATION DOCK (STYLE B)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadBottomDock(
    activeTab: SarhadTab,
    onSelectTab: (SarhadTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(SarhadColors.PillBg)
            .border(1.dp, SarhadColors.PopupBorder, RoundedCornerShape(30.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SarhadTab.entries.forEach { tab ->
            val selected = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) SarhadColors.Primary else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .testTag("sarhad_tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label,
                        tint = if (selected) Color.White else SarhadColors.TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    if (selected) {
                        Text(
                            text = tab.label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
