package slop.miho.chumpy.ui.expressive

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import slop.miho.chumpy.ui.components.CountryFlag
import slop.miho.chumpy.ui.components.splitLeadingFlag
import slop.miho.chumpy.ui.wallpapers.WallpaperActivity

// ─────────────────────────────────────────────────────────────────────────────
// STYLE A — KIRAN (Material 3 Expressive · Emerald / Mint / Peach) — HOLLOW VPN
// ─────────────────────────────────────────────────────────────────────────────

enum class KiranTab(val label: String, val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    SERVERS("Servers", R.drawable.ic_servers),
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
    val coreVersion by viewModel.coreVersion.collectAsStateWithLifecycle()
    val coreRunning by viewModel.coreRunning.collectAsStateWithLifecycle()
    val coreLogs by viewModel.coreLogs.collectAsStateWithLifecycle()
    val updateState by viewModel.update.collectAsStateWithLifecycle()
    val smartSwitchNotice by viewModel.smartSwitchNotice.collectAsStateWithLifecycle()

    LaunchedEffect(qrExportProfile, connectionSettings) {
        qrExportJson = qrExportProfile?.let { viewModel.buildFullJsonForProfile(it) }.orEmpty()
    }

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

            smartSwitchNotice?.let { notice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(KiranColors.CardActive)
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
                        color = KiranColors.Mint,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Dismiss",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = KiranColors.TextMuted,
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
                            onOpenRouting = { activeTab = KiranTab.SETTINGS },
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

                        KiranTab.SETTINGS -> ExpressiveSettingsTab(
                            isStyleB = false,
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

        if (showServerEditor) {
            ServerEditorModal(
                isStyleB = false,
                editingProfile = editingProfile,
                availableChainParents = servers.profiles,
                onSave = { profile, cb -> viewModel.upsertManualProfile(profile, cb) },
                onDismiss = { showServerEditor = false },
            )
        }

        if (showPolicyEditor) {
            PolicyGroupEditorModal(
                isStyleB = false,
                editingProfile = editingPolicyProfile,
                candidateServers = servers.profiles,
                onSave = { profile, cb -> viewModel.upsertManualProfile(profile, cb) },
                onDismiss = { showPolicyEditor = false },
            )
        }

        qrExportProfile?.let { qrProfile ->
            QrAndExportModal(
                isStyleB = false,
                profile = qrProfile,
                shareUri = LinkSerializer.toShareUri(qrProfile),
                fullConfigJson = qrExportJson,
                onDismiss = { qrExportProfile = null },
            )
        }

        editingSubscription?.let { sub ->
            SubscriptionEditModal(
                isStyleB = false,
                subscription = sub,
                onSave = { id, name, url, filterRegex, ua, mins, autoUpd, refresh ->
                    viewModel.updateSubscriptionEditable(id, name, url, filterRegex, ua, mins, autoUpd, refresh)
                },
                onDismiss = { editingSubscription = null },
            )
        }

        if (showCustomRoutingModal) {
            CustomRoutingModal(
                isStyleB = false,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showCustomRoutingModal = false },
            )
        }

        if (showGeodataModal) {
            GeodataManagerModal(
                isStyleB = false,
                tunnelUp = isConnected,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showGeodataModal = false },
            )
        }

        if (showProxyDnsModal) {
            ProxyAndDnsAdvancedModal(
                isStyleB = false,
                connectionSettings = connectionSettings,
                onUpdateConnection = { viewModel.updateConnection(it) },
                onDismiss = { showProxyDnsModal = false },
            )
        }

        if (showBackupModal) {
            BackupRestoreModal(
                isStyleB = false,
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
// BOTTOM NAVIGATION DOCK (STYLE A — 3-TAB PILL BAR)
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
