package slop.miho.chumpy.data

import android.app.ActivityManager
import android.content.Context
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import slop.miho.chumpy.sub.HappSpoof

/**
 * The light/dark choice. AMOLED is the dark theme gone darker — a pure black background and
 * deeper containers; it hides behind a long tap on the dark option.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
}

/**
 * The active UI/UX design style controllable from Appearance settings:
 * - [KIRAN_A]: Style A — Kiran (Emerald / Mint #86E3B0 / Peach #FFDCC4, morphing blob, 4-tab pill nav)
 * - [SARHAD_B]: Style B — Sarhad (Deep Indigo #0B0B10 / Violet #6C60F0 / Coral #FF8FB6, 14-point scalloped seal, 3-tab nav)
 */
enum class DesignStyle {
    KIRAN_A,
    SARHAD_B,
}

/**
 * How the server list draws an exit's country: Twemoji flags from the bundled font, the
 * system's own emoji flags, or the plain globe icon the list used before flags existed.
 */
enum class FlagStyle { TWEMOJI, SYSTEM, GLOBE }

/**
 * The launcher icon variant. CHUMPY is the flat sky-colored art on white; INVERTED swaps
 * the two; DARK is the white art on a near-black #191919; THEMED is the neutral art the
 * system's themed icons (Android 13+) dress in the monet palette — and that the app itself
 * colors from its own accent everywhere it draws (the QS tile, the picker's preview).
 */
enum class IconVariant { CHUMPY, INVERTED, THEMED, DARK }

/**
 * The interface accent. ADAPTIVE follows the wallpaper (Material You, Android 12+); the rest
 * are the seven rainbow hues. [hue] is the HSL hue the color scheme is built from; ADAPTIVE
 * ignores it and is hidden on devices without Material You. The WALLPAPER_* entries are
 * seeds pulled live from the picked wallpaper instead — their [hue] is a placeholder,
 * resolved at theming time by [WallpaperPalette].
 */
enum class Accent(val hue: Float) {
    ADAPTIVE(0f),
    RED(4f),
    ORANGE(30f),
    YELLOW(52f),
    GREEN(145f),

    /** The light blue of #6FACED — the cyan side of blue rather than BLUE's indigo side. */
    SKY(211f),
    BLUE(217f),
    INDIGO(248f),
    VIOLET(285f),

    /** Black-and-white accent; [hue] is unused — the scheme is built from pure neutrals. */
    MONO(0f),

    /**
     * The hue slowly cycling around the whole color wheel; [hue] is unused — the theme
     * animates it.
     */
    RAINBOW(0f),

    /**
     * The user's own picked color; [hue] is unused — the scheme is re-hued from the color
     * stored in the settings at theming time.
     */
    CUSTOM(0f),
}

/** UI preferences. Nothing sensitive here, so plain SharedPreferences storage is fine. */
data class UiSettings(
    val designStyle: DesignStyle = DesignStyle.KIRAN_A,
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: Accent = Accent.ADAPTIVE,
    val slowAnimations: Boolean = false,
    val simpleTransitions: Boolean = false,
    val hapticFeedback: Boolean = true,
    /** Whether the home screen's exit IP chip reads "Hidden"; flipped by tapping the chip. */
    val hideIp: Boolean = false,
    /**
     * The addresses' own curtain: IPs, domains and ports never show — the dots stand
     * where they stood, in every list and on the server card.
     */
    val maskAddresses: Boolean = false,
    val notificationsPermissionAsked: Boolean = false,
    val happSpoof: Boolean = true,
    val happAppVersion: String = "",
    val happOs: String = "",
    val happOsVersion: String = "",
    val happDeviceModel: String = "",
    val happLocale: String = "",
    val happUserId: String = "",
    val happHwid: String = "",
    val fontId: String = FontRegistry.INTER_ID,
    val flagStyle: FlagStyle = FlagStyle.TWEMOJI,
    val iconVariant: IconVariant = IconVariant.CHUMPY,
    val customAccent: Int = 0xFF6FACED.toInt(),
)

object SettingsStore {
    private const val PREFS = "ui_settings"

    /**
     * The current settings as a shared state: seeded from disk once per process and updated
     * by every write. The fonts activity writes to it too, so a font picked there re-themes
     * the main screen the moment the user comes back.
     */
    private val settingsState = MutableStateFlow(UiSettings())

    @Synchronized
    fun settings(context: Context): StateFlow<UiSettings> {
        if (!seeded) {
            settingsState.value = read(context)
            publishTile(context, settingsState.value)
            seeded = true
        }
        return settingsState.asStateFlow()
    }

    @Synchronized
    fun update(context: Context, transform: (UiSettings) -> UiSettings) {
        val old = settings(context).value
        val updated = transform(old)
        write(context, updated)
        settingsState.value = updated
        if (old.iconVariant != updated.iconVariant || old.accent != updated.accent ||
            old.themeMode != updated.themeMode || old.customAccent != updated.customAccent ||
            old.maskAddresses != updated.maskAddresses) {
            publishTile(context, updated)
        }
        // The launcher icon flips the moment the variant is picked — the aliases swap
        // under the launcher without a reinstall.
        if (updated.iconVariant != old.iconVariant) LauncherIcon.apply(context, updated.iconVariant)
    }

    private val tileUpdates = kotlinx.coroutines.channels.Channel<Pair<Context, UiSettings>>(
        kotlinx.coroutines.channels.Channel.CONFLATED,
    )
    private val tileWriter = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob(),
    ).launch {
        for (update in tileUpdates) {
            kotlinx.coroutines.delay(50)
            var latest = update
            while (true) latest = tileUpdates.tryReceive().getOrNull() ?: break
            runCatching { writeTileSnapshot(latest.first, latest.second) }
                .onFailure { android.util.Log.w("Chumpy/Settings", "Could not publish tile settings", it) }
        }
    }

    private fun publishTile(context: Context, settings: UiSettings) {
        tileUpdates.trySend(context.applicationContext to settings)
    }

    private var seeded = false

    const val TILE_SETTINGS_FILE = "ui-tile-settings.json"

    fun tileSettings(context: Context): UiSettings = runCatching {
        val json = JSONObject(AtomicSnapshot.read(File(context.filesDir, TILE_SETTINGS_FILE))
            ?: error("No published tile settings"))
        UiSettings(
            iconVariant = enumEntry(json.optString(KEY_ICON_VARIANT), IconVariant.CHUMPY),
            accent = enumEntry(json.optString(KEY_ACCENT), Accent.ADAPTIVE),
            customAccent = json.optInt(KEY_CUSTOM_ACCENT, 0xFF6FACED.toInt()),
            maskAddresses = json.optBoolean(KEY_MASK_ADDRESSES, false),
            themeMode = enumEntry(json.optString(KEY_THEME), ThemeMode.SYSTEM),
        )
    }.getOrDefault(UiSettings())

    private fun writeTileSnapshot(context: Context, settings: UiSettings) {
        val json = JSONObject().put(KEY_ICON_VARIANT, settings.iconVariant.name)
            .put(KEY_ACCENT, settings.accent.name).put(KEY_THEME, settings.themeMode.name)
            .put(KEY_CUSTOM_ACCENT, settings.customAccent).put(KEY_MASK_ADDRESSES, settings.maskAddresses)
        AtomicSnapshot.write(File(context.filesDir, TILE_SETTINGS_FILE), json.toString())
    }

    private fun read(context: Context): UiSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // The fingerprint's identity half (user id, HWID) is generated once and kept:
        // panels bind accounts to it, so a blank stored identity is seeded and written
        // back right here — the panel sees the same "device" from the very first fetch.
        val identityBlank =
            prefs.getString(KEY_HAPP_USER_ID, null).isNullOrBlank() ||
                prefs.getString(KEY_HAPP_HWID, null).isNullOrBlank()
        val settings = UiSettings(
            designStyle = enumEntry(prefs.getString(KEY_DESIGN_STYLE, null), DesignStyle.KIRAN_A),
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            themeMode = enumEntry(prefs.getString(KEY_THEME, null), ThemeMode.SYSTEM),
            accent = enumEntry(prefs.getString(KEY_ACCENT, null), Accent.ADAPTIVE),
            // The speed setting used to be a three-way enum; a stored SLOW carries over.
            slowAnimations = if (prefs.contains(KEY_SLOW)) {
                prefs.getBoolean(KEY_SLOW, false)
            } else {
                prefs.getString(KEY_SPEED, null) == "SLOW"
            },
            simpleTransitions = prefs.getBoolean(KEY_SIMPLE_TRANSITIONS, isWeakDevice(context)),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            hideIp = prefs.getBoolean(KEY_HIDE_IP, false),
            maskAddresses = prefs.getBoolean(KEY_MASK_ADDRESSES, false),
            notificationsPermissionAsked = prefs.getBoolean(KEY_NOTIFICATIONS_ASKED, false),
            happSpoof = prefs.getBoolean(KEY_HAPP_SPOOF, true),
            happAppVersion = prefs.getString(KEY_HAPP_APP_VERSION, null).orEmpty()
                .ifBlank { HappSpoof.DEFAULT_APP_VERSION },
            happOs = prefs.getString(KEY_HAPP_OS, null).orEmpty().ifBlank { "Android" },
            happOsVersion = prefs.getString(KEY_HAPP_OS_VERSION, null).orEmpty()
                .ifBlank { HappSpoof.defaultOsVersion() },
            happDeviceModel = prefs.getString(KEY_HAPP_DEVICE_MODEL, null).orEmpty()
                .ifBlank { HappSpoof.defaultDeviceModel() },
            happLocale = prefs.getString(KEY_HAPP_LOCALE, null).orEmpty()
                .ifBlank { HappSpoof.defaultLocale() },
            happUserId = prefs.getString(KEY_HAPP_USER_ID, null).orEmpty()
                .ifBlank { HappSpoof.randomUserId() },
            happHwid = prefs.getString(KEY_HAPP_HWID, null).orEmpty()
                .ifBlank { HappSpoof.randomHwid() },
            fontId = prefs.getString(KEY_FONT, null) ?: FontRegistry.INTER_ID,
            flagStyle = enumEntry(prefs.getString(KEY_FLAG_STYLE, null), FlagStyle.TWEMOJI),
            iconVariant = enumEntry(prefs.getString(KEY_ICON_VARIANT, null), IconVariant.CHUMPY),
            customAccent = prefs.getInt(KEY_CUSTOM_ACCENT, 0xFF6FACED.toInt()),
        )
        if (identityBlank) write(context, settings)
        return settings
    }

    private fun write(context: Context, settings: UiSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DESIGN_STYLE, settings.designStyle.name)
            .putBoolean(KEY_ONBOARDING_COMPLETED, settings.onboardingCompleted)
            .putString(KEY_THEME, settings.themeMode.name)
            .putString(KEY_ACCENT, settings.accent.name)
            .putBoolean(KEY_SLOW, settings.slowAnimations)
            .remove(KEY_SPEED)
            .remove(KEY_EXPRESSIVE_TRANSITIONS)
            .putBoolean(KEY_SIMPLE_TRANSITIONS, settings.simpleTransitions)
            .putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            .putBoolean(KEY_HIDE_IP, settings.hideIp)
            .putBoolean(KEY_MASK_ADDRESSES, settings.maskAddresses)
            .putBoolean(KEY_NOTIFICATIONS_ASKED, settings.notificationsPermissionAsked)
            .putBoolean(KEY_HAPP_SPOOF, settings.happSpoof)
            .putString(KEY_HAPP_APP_VERSION, settings.happAppVersion)
            .putString(KEY_HAPP_OS, settings.happOs)
            .putString(KEY_HAPP_OS_VERSION, settings.happOsVersion)
            .putString(KEY_HAPP_DEVICE_MODEL, settings.happDeviceModel)
            .putString(KEY_HAPP_LOCALE, settings.happLocale)
            .putString(KEY_HAPP_USER_ID, settings.happUserId)
            .putString(KEY_HAPP_HWID, settings.happHwid)
            .putString(KEY_FONT, settings.fontId)
            .putString(KEY_FLAG_STYLE, settings.flagStyle.name)
            .putString(KEY_ICON_VARIANT, settings.iconVariant.name)
            .putInt(KEY_CUSTOM_ACCENT, settings.customAccent)
            .apply()
    }

    /** Unknown stored values (renamed enums) fall back to the default instead of crashing. */
    private inline fun <reified T : Enum<T>> enumEntry(name: String?, default: T): T =
        name?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default

    /**
     * Whether the device is weak enough that the strip's continuous scroll would
     * stutter: the cutoff rides on RAM — the phones that never got past 4 GB
     * (an 821-class one) can't carry it, while anything an 855 shipped in starts
     * at 6.
     */
    private fun isWeakDevice(context: Context): Boolean {
        val info = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java).getMemoryInfo(info)
        return info.totalMem < 6L * 1024 * 1024 * 1024
    }

    private const val KEY_DESIGN_STYLE = "design_style"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_ACCENT = "accent"
    private const val KEY_SLOW = "slow_animations"

    /** Legacy key of the old three-way animation speed enum, read once for the migration. */
    private const val KEY_SPEED = "animation_speed"
    /** Legacy key of the expressive-transitions toggle the simple one replaced; dropped on the next write. */
    private const val KEY_EXPRESSIVE_TRANSITIONS = "expressive_transitions"
    private const val KEY_HAPTIC = "haptic_feedback"
    private const val KEY_SIMPLE_TRANSITIONS = "simple_transitions"
    private const val KEY_HIDE_IP = "hide_ip"
    private const val KEY_MASK_ADDRESSES = "mask_addresses"
    private const val KEY_NOTIFICATIONS_ASKED = "notifications_permission_asked"

    // The Happ fingerprint keys, all one global block.
    private const val KEY_HAPP_SPOOF = "happ_spoof"
    private const val KEY_HAPP_APP_VERSION = "happ_app_version"
    private const val KEY_HAPP_OS = "happ_os"
    private const val KEY_HAPP_OS_VERSION = "happ_os_version"
    private const val KEY_HAPP_DEVICE_MODEL = "happ_device_model"
    private const val KEY_HAPP_LOCALE = "happ_locale"
    private const val KEY_HAPP_USER_ID = "happ_user_id"
    private const val KEY_HAPP_HWID = "happ_hwid"
    private const val KEY_FONT = "font"
    private const val KEY_FLAG_STYLE = "flag_style"
    private const val KEY_ICON_VARIANT = "icon_variant"
    private const val KEY_CUSTOM_ACCENT = "custom_accent"
}
