package slop.miho.chumpy.ui.expressive

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.db.SubscriptionEntity

/**
 * Local ZIP Backup/Restore (Android SAF) + optional WebDAV sync modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreModal(
    isStyleB: Boolean,
    connectionSettings: ConnectionSettings,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onExportZip: (Uri, (Result<Int>) -> Unit) -> Unit,
    onImportZip: (Uri, (Result<Int>) -> Unit) -> Unit,
    onUploadWebDav: ((Result<Int>) -> Unit) -> Unit,
    onDownloadWebDav: ((Result<Int>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val dangerColor = if (isStyleB) SarhadColors.Pink else KiranColors.Orange

    var statusText by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var showWebDav by remember { mutableStateOf(connectionSettings.webdavUrl.isNotBlank()) }

    var webdavUrl by remember(connectionSettings.webdavUrl) { mutableStateOf(connectionSettings.webdavUrl) }
    var webdavUser by remember(connectionSettings.webdavUser) { mutableStateOf(connectionSettings.webdavUser) }
    var webdavPass by remember(connectionSettings.webdavPass) { mutableStateOf(connectionSettings.webdavPass) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        statusText = "Exporting backup…"
        isError = false
        onExportZip(uri) { res ->
            res.onSuccess { count ->
                statusText = "Exported $count profile(s) & settings to ZIP"
                isError = false
            }.onFailure { err ->
                statusText = err.message ?: "Backup export failed"
                isError = true
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        statusText = "Restoring backup…"
        isError = false
        onImportZip(uri) { res ->
            res.onSuccess { count ->
                statusText = "Restored $count profile(s) & settings"
                isError = false
            }.onFailure { err ->
                statusText = err.message ?: "Backup restore failed"
                isError = true
            }
        }
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
            Text("Backup & Restore", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = textColor)
            Spacer(Modifier.height(4.dp))
            Text(
                "Save all your servers, subscriptions, and settings into one ZIP file, or restore them anytime.",
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(primaryColor)
                        .clickable { exportLauncher.launch("hollow-vpn-backup.zip") }
                        .testTag("export_zip_backup_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Save Backup", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = onPrimaryColor)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(fieldBg)
                        .clickable { importLauncher.launch(arrayOf("application/zip", "application/json", "*/*")) }
                        .testTag("import_zip_backup_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Restore File", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fieldBg)
                    .clickable { showWebDav = !showWebDav }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Advanced: Cloud Sync (WebDAV)", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                Text(if (showWebDav) "Hide ▴" else "Show ▾", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
            }

            AnimatedVisibility(visible = showWebDav) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    EditorTextField("WebDAV URL", webdavUrl, "https://dav.example.com/backup/", fieldBg, textColor, mutedColor) { webdavUrl = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { EditorTextField("Username", webdavUser, "user", fieldBg, textColor, mutedColor) { webdavUser = it } }
                        Box(Modifier.weight(1f)) { EditorTextField("Password", webdavPass, "password", fieldBg, textColor, mutedColor) { webdavPass = it } }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(fieldBg)
                                .clickable {
                                    onUpdateConnection {
                                        it.copy(
                                            webdavUrl = webdavUrl.trim(),
                                            webdavUser = webdavUser.trim(),
                                            webdavPass = webdavPass,
                                        )
                                    }
                                    statusText = "Uploading backup to WebDAV…"
                                    isError = false
                                    onUploadWebDav { res ->
                                        res.onSuccess { count ->
                                            statusText = "Uploaded $count profile(s) via WebDAV"
                                            isError = false
                                        }.onFailure { err ->
                                            statusText = err.message ?: "WebDAV upload failed"
                                            isError = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Upload Now", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(fieldBg)
                                .clickable {
                                    onUpdateConnection {
                                        it.copy(
                                            webdavUrl = webdavUrl.trim(),
                                            webdavUser = webdavUser.trim(),
                                            webdavPass = webdavPass,
                                        )
                                    }
                                    statusText = "Downloading backup from WebDAV…"
                                    isError = false
                                    onDownloadWebDav { res ->
                                        res.onSuccess { count ->
                                            statusText = "Restored $count profile(s) from WebDAV"
                                            isError = false
                                        }.onFailure { err ->
                                            statusText = err.message ?: "WebDAV restore failed"
                                            isError = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Download & Restore", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                        }
                    }
                }
            }

            statusText?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Text(msg, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isError) dangerColor else primaryColor)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Editable Subscription Settings modal: Name, URL, Regex Filter, Custom User-Agent, Update Interval Override.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditModal(
    isStyleB: Boolean,
    subscription: SubscriptionEntity,
    onSave: (
        id: String,
        name: String,
        url: String,
        filterRegex: String,
        customUserAgent: String,
        updateIntervalMinutes: Int?,
        autoUpdate: Boolean,
        refreshAfter: Boolean,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val switchOff = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

    var name by remember(subscription) { mutableStateOf(subscription.name) }
    var url by remember(subscription) { mutableStateOf(subscription.url) }
    var filterRegex by remember(subscription) { mutableStateOf(subscription.filterRegex) }
    var customUa by remember(subscription) { mutableStateOf(subscription.customUserAgent) }
    var intervalMinutes by remember(subscription) { mutableStateOf(subscription.updateIntervalMinutes) }
    var autoUpdate by remember(subscription) { mutableStateOf(subscription.autoUpdate) }
    var showAdvancedSub by remember(subscription) {
        mutableStateOf(
            subscription.filterRegex.isNotBlank() ||
                subscription.customUserAgent.isNotBlank() ||
                subscription.updateIntervalMinutes != null,
        )
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
            Text("Edit Subscription", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = textColor)
            Spacer(Modifier.height(12.dp))

            EditorTextField("Name", name, "Subscription name", fieldBg, textColor, mutedColor) { name = it }
            EditorTextField("Subscription Link", url, "https://...", fieldBg, textColor, mutedColor) { url = it }

            Spacer(Modifier.height(6.dp))
            SwitchTile("Auto-Update Servers", "Keep server list refreshed automatically", autoUpdate, fieldBg, textColor, mutedColor, primaryColor, onPrimaryColor, switchOff) {
                autoUpdate = it
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fieldBg)
                    .clickable { showAdvancedSub = !showAdvancedSub }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Advanced: Filter Regex, User-Agent & Interval", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = textColor, modifier = Modifier.weight(1f))
                Text(if (showAdvancedSub) "Hide ▴" else "Show ▾", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
            }

            AnimatedVisibility(visible = showAdvancedSub) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    EditorTextField("Filter Servers (!pattern excludes)", filterRegex, "e.g. Germany|Netherlands or !Traffic|Expire", fieldBg, textColor, mutedColor) { filterRegex = it }
                    EditorTextField("Custom User-Agent (optional)", customUa, "leave empty for default", fieldBg, textColor, mutedColor) { customUa = it }

                    Spacer(Modifier.height(8.dp))
                    Text("Refresh Interval Override", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = mutedColor)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            null to "Panel Default",
                            15 to "15m",
                            60 to "1h",
                            360 to "6h",
                            720 to "12h",
                            1440 to "24h",
                        ).forEach { (mins, label) ->
                            EditorChip(label, intervalMinutes == mins, primaryColor, onPrimaryColor, fieldBg, textColor) {
                                intervalMinutes = mins
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(fieldBg)
                        .clickable {
                            onSave(subscription.id, name, url, filterRegex, customUa, intervalMinutes, autoUpdate, false)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Save", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                }
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(primaryColor)
                        .clickable {
                            onSave(subscription.id, name, url, filterRegex, customUa, intervalMinutes, autoUpdate, true)
                            onDismiss()
                        }
                        .testTag("save_and_refresh_subscription_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Save & Refresh", fontFamily = OutfitFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = onPrimaryColor)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
