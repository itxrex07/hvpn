package slop.miho.chumpy.ui.expressive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import slop.miho.chumpy.data.CustomRoutingRule
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.FlagStyle
import slop.miho.chumpy.data.IconVariant
import slop.miho.chumpy.data.UiSettings
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.sub.LinkSerializer
import slop.miho.chumpy.ui.HomeUiState
import slop.miho.chumpy.ui.MainViewModel
import slop.miho.chumpy.ui.ServersUiState
import slop.miho.chumpy.ui.StatsUiState
import slop.miho.chumpy.ui.UpdateUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// STYLE B — SARHAD (Deep Indigo / Violet / Coral 14-Point Seal) — HOLLOW VPN
// ─────────────────────────────────────────────────────────────────────────────

enum class SarhadTab(val label: String, val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    SERVERS("Servers", R.drawable.ic_servers),
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
                text = "Hollow VPN pairs deep indigo Material 3 Expressive craft with Xray-core — real latency probes, smart failover, and anti-DPI fragmentation.",
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
    var showDnsModal by remember { mutableStateOf(false) }
    var showFragmentModal by remember { mutableStateOf(false) }
    var showHappSpoofModal by remember { mutableStateOf(false) }
    var showDelayTestModal by remember { mutableStateOf(false) }
    var showLogsModal by remember { mutableStateOf(false) }
    var showServerEditor by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var showPolicyEditor by remember { mutableStateOf(false) }
    var editingPolicyProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var qrExportProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var qrExportJson by remember { mutableStateOf("") }
    var editingSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var showCustomRoutingModal by remember { mutableStateOf(false) }
    var showGeodataModal by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }
    var showProxyDnsModal by remember { mutableStateOf(false) }

    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val selectedSubscription by viewModel.selectedSubscription.collectAsStateWithLifecycle()
    val manualProfileCount by viewModel.manualProfileCount.collectAsStateWithLifecycle()
    val coreRunning by viewModel.coreRunning.collectAsStateWithLifecycle()
    val coreLogs by viewModel.coreLogs.collectAsStateWithLifecycle()
    val updateState by viewModel.update.collectAsStateWithLifecycle()
    val smartSwitchNotice by viewModel.smartSwitchNotice.collectAsStateWithLifecycle()

    LaunchedEffect(qrExportProfile, connectionSettings) {
        qrExportJson = qrExportProfile?.let { viewModel.buildFullJsonForProfile(it) }.orEmpty()
    }

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
                    onOpenLogs = { showLogsModal = true },
                )
            }

            smartSwitchNotice?.let { notice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SarhadColors.SelectedServer)
                        .clickable { viewModel.dismissSmartSwitchNotice() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = notice,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SarhadColors.PrimaryLight,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Dismiss",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SarhadColors.TextMuted,
                    )
                }
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
                            onNewServer = {
                                editingProfile = null
                                showServerEditor = true
                            },
                            onNewPolicyGroup = {
                                editingPolicyProfile = null
                                showPolicyEditor = true
                            },
                            onEditProfile = { profile ->
                                if (profile.type == ProfileType.BALANCER) {
                                    editingPolicyProfile = profile
                                    showPolicyEditor = true
                                } else {
                                    editingProfile = profile
                                    showServerEditor = true
                                }
                            },
                            onShareOrQr = { profile -> qrExportProfile = profile },
                            onTestSpeed = { profile -> viewModel.testProfileSpeed(profile) },
                            onEditSubscription = { sub -> editingSubscription = sub },
                        )

                        SarhadTab.SETTINGS -> ExpressiveSettingsTab(
                            isStyleB = true,
                            settings = settings,
                            connectionSettings = connectionSettings,
                            subscriptions = subscriptions,
                            coreVersion = coreVersion,
                            coreRunning = coreRunning,
                            updateState = updateState,
                            onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                            onUpdateConnection = { transform -> viewModel.updateConnection(transform) },
                            onOpenDnsModal = { showDnsModal = true },
                            onOpenFragmentModal = { showFragmentModal = true },
                            onOpenPerAppProxy = onOpenPerAppProxy,
                            onOpenHappSpoofModal = { showHappSpoofModal = true },
                            onOpenDelayTestModal = { showDelayTestModal = true },
                            onOpenLogsModal = { showLogsModal = true },
                            onOpenCustomRouting = { showCustomRoutingModal = true },
                            onOpenGeodataModal = { showGeodataModal = true },
                            onOpenBackupModal = { showBackupModal = true },
                            onOpenProxyDnsModal = { showProxyDnsModal = true },
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

        if (showServerEditor) {
            ServerEditorModal(
                isStyleB = true,
                editingProfile = editingProfile,
                availableChainParents = servers.profiles,
                onSave = { profile, cb -> viewModel.upsertManualProfile(profile, cb) },
                onDismiss = { showServerEditor = false },
            )
        }

        if (showPolicyEditor) {
            PolicyGroupEditorModal(
                isStyleB = true,
                editingProfile = editingPolicyProfile,
                candidateServers = servers.profiles,
                onSave = { profile, cb -> viewModel.upsertManualProfile(profile, cb) },
                onDismiss = { showPolicyEditor = false },
            )
        }

        qrExportProfile?.let { qrProfile ->
            QrAndExportModal(
                isStyleB = true,
                profile = qrProfile,
                shareUri = LinkSerializer.toShareUri(qrProfile),
                fullConfigJson = qrExportJson,
                onDismiss = { qrExportProfile = null },
            )
        }

        editingSubscription?.let { sub ->
            SubscriptionEditModal(
                isStyleB = true,
                subscription = sub,
                onSave = { id, name, url, filterRegex, ua, mins, autoUpd, refresh ->
                    viewModel.updateSubscriptionEditable(id, name, url, filterRegex, ua, mins, autoUpd, refresh)
                },
                onDismiss = { editingSubscription = null },
            )
        }

        if (showCustomRoutingModal) {
            CustomRoutingModal(
                isStyleB = true,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showCustomRoutingModal = false },
            )
        }

        if (showGeodataModal) {
            GeodataManagerModal(
                isStyleB = true,
                tunnelUp = isConnected,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showGeodataModal = false },
            )
        }

        if (showProxyDnsModal) {
            ProxyAndDnsAdvancedModal(
                isStyleB = true,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showProxyDnsModal = false },
            )
        }

        if (showBackupModal) {
            BackupRestoreModal(
                isStyleB = true,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onExportZip = { uri, cb -> viewModel.exportBackupZip(uri, cb) },
                onImportZip = { uri, cb -> viewModel.importBackupZip(uri, cb) },
                onUploadWebDav = { cb -> viewModel.uploadWebDav(cb) },
                onDownloadWebDav = { cb -> viewModel.downloadWebDav(cb) },
                onDismiss = { showBackupModal = false },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP HEADER (STYLE B)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadTopHeader(
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

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1: HOME (STYLE B — 14-POINT SCALLOPED SEAL)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SarhadHomeTab(
    tunnelStatus: TunnelStatus,
    home: HomeUiState,
    stats: StatsUiState,
    settings: UiSettings,
    selectedProfile: ProfileEntity?,
    updateState: UpdateUiState,
    onToggleConnect: () -> Unit,
    onOpenServers: () -> Unit,
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

        // Live Speed & Session Totals Row (shown only when connected)
        item {
            AnimatedVisibility(visible = isConnected) {
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
// BOTTOM NAVIGATION DOCK (STYLE B — 3-TAB PILL BAR)
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
