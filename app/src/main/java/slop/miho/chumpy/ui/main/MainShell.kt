package slop.miho.chumpy.ui.main

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.ImportedFile
import slop.miho.chumpy.ui.MainViewModel
import slop.miho.chumpy.ui.components.SystemBarsIconsDark
import slop.miho.chumpy.ui.expressive.KiranMainShell
import slop.miho.chumpy.ui.expressive.SarhadMainShell
import slop.miho.chumpy.ui.perapp.PerAppProxyActivity
import slop.miho.chumpy.ui.scan.ScannerActivity

@Composable
fun MainShell(viewModel: MainViewModel) {
    val tunnelStatus by viewModel.tunnelStatus.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val home by viewModel.home.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val connectionSettings by viewModel.connection.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val coreVersion by viewModel.coreVersion.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val vpnPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.connect()
    }

    val connectWithConsent: () -> Unit = {
        if (connectionSettings.proxyOnlyMode) {
            viewModel.connect()
        } else {
            val consent = VpnService.prepare(context)
            if (consent != null) vpnPermission.launch(consent) else viewModel.connect()
        }
    }

    val scope = rememberCoroutineScope()
    val qrScanner = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val text = result.data?.getStringExtra(ScannerActivity.KEY_TEXT)
            ?: return@rememberLauncherForActivityResult
        viewModel.addSubscription(text, null) {}
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { ImportedFile.readText(it) }
            }.getOrNull()
            if (!text.isNullOrBlank()) {
                viewModel.addManualProfiles(text) {}
            }
        }
    }

    SystemBarsIconsDark(dark = true)
    when (settings.designStyle) {
        DesignStyle.KIRAN_A -> KiranMainShell(
            viewModel = viewModel,
            tunnelStatus = tunnelStatus,
            home = home,
            servers = servers,
            stats = stats,
            settings = settings,
            connectionSettings = connectionSettings,
            selectedProfile = selectedProfile,
            onConnect = connectWithConsent,
            onDisconnect = viewModel::disconnect,
            onOpenPerAppProxy = {
                context.startActivity(Intent(context, PerAppProxyActivity::class.java))
            },
            onScanQr = {
                qrScanner.launch(Intent(context, ScannerActivity::class.java))
            },
            onImportFile = { filePicker.launch(arrayOf("*/*")) },
        )

        DesignStyle.SARHAD_B -> SarhadMainShell(
            viewModel = viewModel,
            tunnelStatus = tunnelStatus,
            home = home,
            servers = servers,
            stats = stats,
            settings = settings,
            connectionSettings = connectionSettings,
            selectedProfile = selectedProfile,
            coreVersion = coreVersion,
            onConnect = connectWithConsent,
            onDisconnect = viewModel::disconnect,
            onOpenPerAppProxy = {
                context.startActivity(Intent(context, PerAppProxyActivity::class.java))
            },
            onScanQr = {
                qrScanner.launch(Intent(context, ScannerActivity::class.java))
            },
            onImportFile = { filePicker.launch(arrayOf("*/*")) },
        )
    }
}
