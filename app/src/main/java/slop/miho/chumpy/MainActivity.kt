package slop.miho.chumpy

import android.Manifest
import android.content.Intent
import android.os.Bundle
import slop.miho.chumpy.ui.components.ControllerActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import slop.miho.chumpy.ui.MainViewModel
import slop.miho.chumpy.ui.components.SystemBarsIconsDark
import slop.miho.chumpy.ui.main.MainShell
import slop.miho.chumpy.ui.theme.ChumpyTheme

class MainActivity : ControllerActivity() {
    companion object {
        /** The update notification's tap: hand the downloaded APK to the installer. */
        const val EXTRA_INSTALL_UPDATE = "install_update"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            // The notifications permission rides the first config's landing — asked once,
            // ever. The ask belongs to the activity, not the shell: a fresh install adds
            // its first config from the welcome screen, where the shell is not yet alive.
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { }
            val notificationsAsk by viewModel.notificationsAsk.collectAsStateWithLifecycle()
            LaunchedEffect(notificationsAsk) {
                if (notificationsAsk) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    viewModel.consumeNotificationsAsk()
                }
            }
            ChumpyTheme(settings = settings) {

                Surface(color = MaterialTheme.colorScheme.surface) {
                    val motion = MaterialTheme.motionScheme
                    // setupDone stays null while the database answers for the first time:
                    // the app holds on the theme until it knows which screen to show.
                    val setupDone by viewModel.setupDone.collectAsStateWithLifecycle()
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val qrScanner = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                    ) { result ->
                        val text = result.data?.getStringExtra(slop.miho.chumpy.ui.scan.ScannerActivity.KEY_TEXT)
                            ?: return@rememberLauncherForActivityResult
                        viewModel.addSubscription(text, null) {}
                    }
                    val filePicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument(),
                    ) { uri ->
                        if (uri == null) return@rememberLauncherForActivityResult
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            val text = runCatching {
                                context.contentResolver.openInputStream(uri)?.use {
                                    slop.miho.chumpy.data.ImportedFile.readText(it)
                                }
                            }.getOrNull()
                            if (!text.isNullOrBlank()) {
                                viewModel.addManualProfiles(text) {}
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = setupDone,
                        transitionSpec = {
                            if (initialState == null) {
                                ContentTransform(
                                    targetContentEnter = fadeIn(snap()),
                                    initialContentExit = fadeOut(snap()),
                                    sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> snap() }),
                                )
                            } else {
                                (fadeIn(motion.slowEffectsSpec()) +
                                    scaleIn(motion.slowSpatialSpec(), initialScale = 0.92f))
                                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                            }
                        },
                        label = "onboarding",
                    ) { onboarded ->
                        if (onboarded == true) {
                            MainShell(viewModel)
                        } else if (onboarded == false) {
                            when (settings.designStyle) {
                                slop.miho.chumpy.data.DesignStyle.KIRAN_A -> {
                                    SystemBarsIconsDark(dark = true)
                                    slop.miho.chumpy.ui.expressive.KiranWelcomeScreen(
                                        onGetStarted = { viewModel.completeOnboarding() },
                                        onSubmitConfig = { text, name, onDone ->
                                            viewModel.addSubscription(text, name, onDone)
                                        },
                                        onScanQr = {
                                            qrScanner.launch(Intent(context, slop.miho.chumpy.ui.scan.ScannerActivity::class.java))
                                        },
                                        onImportFile = {
                                            filePicker.launch(arrayOf("*/*"))
                                        },
                                        onSwitchStyle = { newStyle ->
                                            viewModel.updateSettings { it.copy(designStyle = newStyle) }
                                        },
                                    )
                                }

                                slop.miho.chumpy.data.DesignStyle.SARHAD_B -> {
                                    SystemBarsIconsDark(dark = true)
                                    slop.miho.chumpy.ui.expressive.SarhadWelcomeScreen(
                                        onGetStarted = { viewModel.completeOnboarding() },
                                        onSubmitConfig = { text, name, onDone ->
                                            viewModel.addSubscription(text, name, onDone)
                                        },
                                        onScanQr = {
                                            qrScanner.launch(Intent(context, slop.miho.chumpy.ui.scan.ScannerActivity::class.java))
                                        },
                                        onImportFile = {
                                            filePicker.launch(arrayOf("*/*"))
                                        },
                                        onSwitchStyle = { newStyle ->
                                            viewModel.updateSettings { it.copy(designStyle = newStyle) }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // The update notification's tap on a cold start: the request rides the launching
        // intent, and the view model reads the downloaded file from its store.
        handleInstallRequest(intent)
        handleIncomingDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInstallRequest(intent)
        handleIncomingDeepLink(intent)
    }

    /** The update notification's tap: re-held by the live instance when one exists. */
    private fun handleInstallRequest(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_INSTALL_UPDATE, false) != true) return
        ViewModelProvider(this)[MainViewModel::class.java].installUpdate()
    }

    private fun handleIncomingDeepLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uriString = intent.dataString ?: return
        ViewModelProvider(this)[MainViewModel::class.java].handleIncomingUri(uriString)
    }

    override fun onStart() {
        super.onStart()
        // The same instance the compose tree holds: the stats loops sample once a second
        // while this is true.
        ViewModelProvider(this)[MainViewModel::class.java].setAppInForeground(true)
    }

    override fun onStop() {
        super.onStop()
        // The activity leaving the screen is the app going background: the loops take
        // their slow stride and the history keeps trickling in behind the scenes.
        ViewModelProvider(this)[MainViewModel::class.java].setAppInForeground(false)
    }
}
