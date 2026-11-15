package slop.miho.chumpy.ui.expressive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.core.TunnelState
import slop.miho.chumpy.core.TunnelStatus
import slop.miho.chumpy.core.stripFlag
import slop.miho.chumpy.data.Accent
import slop.miho.chumpy.data.ConnectionSettings
import slop.miho.chumpy.data.DesignStyle
import slop.miho.chumpy.data.FlagStyle
import slop.miho.chumpy.data.FontRegistry
import slop.miho.chumpy.data.IconVariant
import slop.miho.chumpy.data.ServersSort
import slop.miho.chumpy.data.ThemeMode
import slop.miho.chumpy.data.UiSettings
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.db.STANDALONE_SUBSCRIPTION_ID
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.sub.HappSpoof
import slop.miho.chumpy.ui.ServersUiState
import slop.miho.chumpy.ui.StatsUiState
import slop.miho.chumpy.ui.components.CountryFlag
import slop.miho.chumpy.ui.components.PhoneImportDialog
import slop.miho.chumpy.ui.components.splitLeadingFlag
import slop.miho.chumpy.ui.wallpapers.WallpaperActivity
import slop.miho.chumpy.xray.RoutingMode
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Exact color tokens from Style A (Kiran — Material 3 Expressive Emerald / Mint / Peach).
 */
object KiranColors {
    val Bg = Color(0xFF0E1512)
    val TextPrimary = Color(0xFFE0E8E2)
    val TextMuted = Color(0xFFA5B5AB)
    val Card = Color(0xFF19211D)
    val CardActive = Color(0xFF1F3A2B)
    val Mint = Color(0xFF86E3B0)
    val OnMint = Color(0xFF00391F)
    val MintSoft = Color(0xFFCDEBD9)
    val MintSub = Color(0xFFBFE3CD)
    val TagBg = Color(0xFF2F4B3C)
    val Peach = Color(0xFFFFDCC4)
    val OnPeach = Color(0xFF4B2600)
    val Orange = Color(0xFFFFB784)
    val NavBg = Color(0xFF232C27)
    val BlobHalo = Color(0xFF1F2924)
    val SwitchOffBg = Color(0xFF3A453F)
    val Divider = Color(0xFF232C27)
}

/**
 * Exact color tokens from Style B (Sarhad — Deep Indigo / Violet / Coral Scalloped Seal).
 */
object SarhadColors {
    val Bg = Color(0xFF0B0B10)
    val TextPrimary = Color(0xFFEDEBF7)
    val TextMuted = Color(0xFFA29DC4)
    val PillBg = Color(0xFF1B1B24)
    val Card = Color(0xFF17171F)
    val HeroDisconnected = Color(0xFF14141B)
    val HeroConnecting = Color(0xFF1E1B3A)
    val HeroConnected = Color(0xFF4A3FD6)
    val DetailTile = Color(0xFF3A31B8)
    val DetailText = Color(0xFFD9D5FF)
    val Primary = Color(0xFF6C60F0)
    val PrimaryLight = Color(0xFFB9B4FF)
    val SelectedServer = Color(0xFF2A2660)
    val Pink = Color(0xFFFF8FB6)
    val Rose = Color(0xFFFFB0C8)
    val Orange = Color(0xFFFFB784)
    val Green = Color(0xFF4ADE80)
    val Divider = Color(0xFF24243A)
    val SwitchOff = Color(0xFF2E2E3E)
    val PopupBg = Color(0xFF1E1E2A)
    val PopupBorder = Color(0xFF2E2E42)
}

val OutfitFont: FontFamily
    get() = FontRegistry.outfit

/**
 * Draws the Hollow / Kiran 8-ray sun-ring emblem (`viewBox="0 0 108 108"`).
 */
@Composable
fun KiranRayIcon(
    modifier: Modifier = Modifier,
    rayColor: Color = KiranColors.Mint,
    centerCutoutColor: Color = KiranColors.Bg,
) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 108f
        val scaleY = size.height / 108f
        val stroke = 7f * ((scaleX + scaleY) / 2f)

        val rays = listOf(
            Offset(85f, 54f) to Offset(97f, 54f),
            Offset(75.9f, 75.9f) to Offset(84.4f, 84.4f),
            Offset(54f, 85f) to Offset(54f, 97f),
            Offset(32.1f, 75.9f) to Offset(23.6f, 84.4f),
            Offset(23f, 54f) to Offset(11f, 54f),
            Offset(32.1f, 32.1f) to Offset(23.6f, 23.6f),
            Offset(54f, 23f) to Offset(54f, 11f),
            Offset(75.9f, 32.1f) to Offset(84.4f, 23.6f),
        )
        rays.forEach { (start, end) ->
            drawLine(
                color = rayColor,
                start = Offset(start.x * scaleX, start.y * scaleY),
                end = Offset(end.x * scaleX, end.y * scaleY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }

        val center = Offset(54f * scaleX, 54f * scaleY)
        val outerRadius = 22f * ((scaleX + scaleY) / 2f)
        val innerRadius = 9f * ((scaleX + scaleY) / 2f)

        if (centerCutoutColor == Color.Transparent) {
            val outer = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center.x - outerRadius,
                        center.y - outerRadius,
                        center.x + outerRadius,
                        center.y + outerRadius,
                    )
                )
            }
            val inner = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center.x - innerRadius,
                        center.y - innerRadius,
                        center.x + innerRadius,
                        center.y + innerRadius,
                    )
                )
            }
            val ring = Path.combine(PathOperation.Difference, outer, inner)
            drawPath(ring, color = rayColor)
        } else {
            drawCircle(color = rayColor, radius = outerRadius, center = center)
            drawCircle(color = centerCutoutColor, radius = innerRadius, center = center)
        }
    }
}

/**
 * Organic morphing blob container matching Style A.
 */
@Composable
fun MorphingBlobBox(
    modifier: Modifier = Modifier,
    color: Color,
    haloColor: Color? = null,
    haloSpread: Dp = 14.dp,
    breathe: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "kiran_blob")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "blob_phase",
    )
    val breatheScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (breathe) 1.035f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blob_breathe",
    )

    val blobShape = remember(phase) { OrganicBlobShape(phase) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .scale(breatheScale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (haloColor != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spreadPx = haloSpread.toPx()
                val haloPath = buildBlobPath(
                    width = size.width + spreadPx * 2f,
                    height = size.height + spreadPx * 2f,
                    phase = phase,
                    offsetX = -spreadPx,
                    offsetY = -spreadPx,
                )
                drawPath(haloPath, color = haloColor)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(blobShape)
                .background(color),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

private class OrganicBlobShape(private val phase: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(buildBlobPath(size.width, size.height, phase, 0f, 0f))
    }
}

private fun buildBlobPath(
    width: Float,
    height: Float,
    phase: Float,
    offsetX: Float,
    offsetY: Float,
): Path {
    val path = Path()
    val cx = offsetX + width / 2f
    val cy = offsetY + height / 2f
    val rx = width / 2f
    val ry = height / 2f
    val steps = 72
    for (i in 0..steps) {
        val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
        val wobble = 1f +
            0.045f * sin(2f * theta + phase) +
            0.032f * cos(3f * theta - phase * 1.3f) +
            0.018f * sin(4f * theta + phase * 0.7f)
        val x = cx + rx * wobble * cos(theta)
        val y = cy + ry * wobble * sin(theta)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
fun SpinningDashedRing(
    modifier: Modifier = Modifier,
    color: Color = KiranColors.Mint,
    strokeWidth: Dp = 5.dp,
) {
    val transition = rememberInfiniteTransition(label = "spin_dashed")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val radius = (size.minDimension - strokePx) / 2f
        rotate(angle) {
            drawCircle(
                color = color,
                radius = radius,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 20f), 0f),
                ),
            )
        }
    }
}

@Composable
fun ExpandingPulseRings(
    modifier: Modifier = Modifier,
    color: Color = KiranColors.Mint,
    baseDiameter: Dp = 190.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse_rings")
    val progress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "p1",
    )
    val progress2 = (progress1 + 0.5f) % 1f

    Canvas(modifier = modifier) {
        val baseRadius = baseDiameter.toPx() / 2f
        val strokePx = 2.dp.toPx()
        listOf(progress1, progress2).forEach { p ->
            val scale = 0.92f + 0.43f * p
            val alpha = (0.55f * (1f - p)).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = baseRadius * scale,
                style = Stroke(width = strokePx),
            )
        }
    }
}

/**
 * Draws the exact 14-point scalloped starburst seal from Style B (`Sarhad`).
 */
@Composable
fun SarhadSeal(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    spinDurationMs: Int? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "sarhad_seal")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (spinDurationMs != null) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = spinDurationMs ?: 8000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "seal_rotation",
    )
    val actualRotation = if (spinDurationMs != null) rotation else 0f
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier
            }
        ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 120f
            val scaleY = size.height / 120f
            val rawPoints = listOf(
                114.0f to 60.0f, 104.8f to 70.2f, 108.7f to 83.4f, 96.0f to 88.7f,
                93.7f to 102.2f, 80.0f to 101.4f, 72.0f to 112.6f, 60.0f to 106.0f,
                48.0f to 112.6f, 40.0f to 101.4f, 26.3f to 102.2f, 24.0f to 88.7f,
                11.3f to 83.4f, 15.2f to 70.2f, 6.0f to 60.0f, 15.2f to 49.8f,
                11.3f to 36.6f, 24.0f to 31.3f, 26.3f to 17.8f, 40.0f to 18.6f,
                48.0f to 7.4f, 60.0f to 14.0f, 72.0f to 7.4f, 80.0f to 18.6f,
                93.7f to 17.8f, 96.0f to 31.3f, 108.7f to 36.6f, 104.8f to 49.8f,
            )
            val path = Path().apply {
                rawPoints.forEachIndexed { idx, (px, py) ->
                    val x = px * scaleX
                    val y = py * scaleY
                    if (idx == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            val brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            )
            val cornerRadiusPx = 6f * ((scaleX + scaleY) / 2f)
            rotate(actualRotation) {
                drawPath(path = path, brush = brush)
                drawPath(
                    path = path,
                    brush = brush,
                    style = Stroke(
                        width = cornerRadiusPx,
                        join = StrokeJoin.Round,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }
        content()
    }
}

@Composable
fun PowerVectorIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 2.6.dp,
) {
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        val stroke = strokeWidth.toPx()
        drawLine(
            color = color,
            start = Offset(12f * sx, 3f * sy),
            end = Offset(12f * sx, 12f * sy),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawArc(
            color = color,
            startAngle = -50f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(4f * sx, 5f * sy),
            size = Size(16f * sx, 16f * sy),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeTrackColor: Color,
    activeThumbColor: Color,
    inactiveTrackColor: Color,
    inactiveThumbColor: Color,
    modifier: Modifier = Modifier,
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeTrackColor else inactiveTrackColor,
        animationSpec = tween(200),
        label = "switch_track",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) activeThumbColor else inactiveThumbColor,
        animationSpec = tween(200),
        label = "switch_thumb",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 26.dp else 4.dp,
        animationSpec = tween(200),
        label = "switch_offset",
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
}

fun formatProfileProtocolLine(profile: ProfileEntity, uppercase: Boolean = false): String {
    val proto = when (profile.type) {
        ProfileType.VLESS -> "VLESS"
        ProfileType.VMESS -> "VMess"
        ProfileType.TROJAN -> "Trojan"
        ProfileType.SHADOWSOCKS -> "Shadowsocks"
        ProfileType.SOCKS -> "SOCKS"
        ProfileType.WIREGUARD -> "WireGuard"
        ProfileType.HYSTERIA2 -> "Hysteria2"
        ProfileType.BALANCER -> "Balancer"
    }
    val transport = when (profile.network.lowercase()) {
        "ws" -> "WS"
        "grpc" -> "gRPC"
        "httpupgrade" -> "HTTPUpgrade"
        "xhttp", "splithttp" -> "XHTTP"
        "kcp" -> "mKCP"
        "quic" -> "QUIC"
        else -> "TCP"
    }
    val sec = when (profile.security.lowercase()) {
        "reality" -> "Reality"
        "tls" -> "TLS"
        else -> ""
    }
    return if (uppercase) {
        listOfNotNull(
            proto.uppercase(),
            transport.uppercase(),
            sec.takeIf { it.isNotEmpty() }?.uppercase(),
        ).joinToString(" · ")
    } else {
        when {
            transport == "WS" && sec == "TLS" -> "$proto · WS+TLS"
            sec.isNotEmpty() -> "$proto · $sec · $transport"
            else -> "$proto · $transport"
        }
    }
}

fun formatSpeedMbps(bytesPerSec: Long): Pair<String, String> {
    val mbps = (bytesPerSec * 8.0) / 1_000_000.0
    return if (mbps >= 0.1) {
        String.format(Locale.US, "%.1f", mbps) to "Mbps"
    } else {
        val kbps = (bytesPerSec * 8.0) / 1_000.0
        String.format(Locale.US, "%.1f", kbps) to "Kbps"
    }
}

fun formatCompactBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}

fun formatElapsedHms(elapsedMillis: Long): String {
    val totalSeconds = (elapsedMillis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Default Style B Servers Page used across BOTH Style A (`Kiran`) and Style B (`Sarhad`),
 * with a bottom-right Floating Action Button (FAB) to add servers/subscriptions and 100%
 * real data (no dummy servers or fake ping values).
 */
@Composable
fun ExpressiveServersTab(
    isStyleB: Boolean,
    servers: ServersUiState,
    stats: StatsUiState,
    settings: UiSettings,
    subscriptions: List<SubscriptionEntity>,
    selectedSubscription: SubscriptionEntity?,
    manualProfileCount: Int,
    selectedProfileId: String?,
    onSelectProfile: (ProfileEntity) -> Unit,
    onSelectSubscription: (String) -> Unit,
    onUpdateSubscription: () -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onToggleSubscriptionPinned: (String, Boolean) -> Unit,
    onToggleSubscriptionAutoUpdate: (String, Boolean) -> Unit,
    onToggleProfilePinned: (String, Boolean) -> Unit,
    onToggleProfileConcealed: (String, Boolean) -> Unit,
    onPingAll: () -> Unit,
    onOpenImport: () -> Unit,
    onUpdateAll: () -> Unit,
    onSetSort: (ServersSort) -> Unit,
    onDeleteDuplicates: () -> Int,
    onDeleteInvalid: () -> Int,
    onDeleteSelected: (Set<String>) -> Unit,
    onExportAll: () -> String,
    onDeleteAll: () -> Unit,
) {
    val context = LocalContext.current
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showConcealed by rememberSaveable { mutableStateOf(false) }
    var multiSelectMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var expandedCardMenuId by remember { mutableStateOf<String?>(null) }

    val textPrimary = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val textMuted = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val cardBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val selectedCardBg = if (isStyleB) SarhadColors.SelectedServer else KiranColors.CardActive
    val pillBg = if (isStyleB) SarhadColors.PillBg else KiranColors.NavBg
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val accentLight = if (isStyleB) SarhadColors.PrimaryLight else KiranColors.MintSoft
    val dangerColor = if (isStyleB) SarhadColors.Pink else KiranColors.Orange
    val popupBg = if (isStyleB) SarhadColors.PopupBg else KiranColors.Card
    val popupBorder = if (isStyleB) SarhadColors.PopupBorder else KiranColors.TagBg

    val sourceList = if (showConcealed) servers.profiles + servers.concealed else servers.profiles
    val filteredProfiles = remember(sourceList, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            sourceList
        } else {
            sourceList.filter { p ->
                p.remarks.contains(q, ignoreCase = true) ||
                    p.server.contains(q, ignoreCase = true) ||
                    p.type.name.contains(q, ignoreCase = true) ||
                    p.sni.contains(q, ignoreCase = true) ||
                    p.network.contains(q, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Top bar: Title + real server count on left, Search + Ping All + More Options on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Servers",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp,
                        color = textPrimary,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val sortLabel = when (servers.sort) {
                            ServersSort.DEFAULT -> "by config"
                            ServersSort.NAME -> "by name"
                            ServersSort.PING -> "by ping"
                            ServersSort.LAST_USED -> "by last used"
                        }
                        Text(
                            text = "${servers.profiles.size} ${if (servers.profiles.size == 1) "server" else "servers"} · $sortLabel",
                            fontFamily = OutfitFont,
                            fontSize = 13.sp,
                            color = textMuted,
                        )
                        if (servers.concealed.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (showConcealed) primaryColor else pillBg)
                                    .clickable { showConcealed = !showConcealed }
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = if (showConcealed) "Showing ${servers.concealed.size} hidden" else "+${servers.concealed.size} hidden",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (showConcealed) onPrimaryColor else accentLight,
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Search button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (searchOpen) primaryColor else pillBg)
                            .clickable {
                                searchOpen = !searchOpen
                                if (!searchOpen) searchQuery = ""
                            }
                            .testTag("servers_search_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        SearchVectorIcon(
                            modifier = Modifier.size(18.dp),
                            color = if (searchOpen) onPrimaryColor else textPrimary,
                        )
                    }

                    // 2. Speedometer / Real Ping all button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (servers.isPinging) primaryColor else pillBg)
                            .clickable {
                                onPingAll()
                                statusBanner = if (servers.profiles.isEmpty()) {
                                    "No servers to test yet"
                                } else {
                                    "Testing real latency on ${servers.profiles.size} servers…"
                                }
                            }
                            .testTag("sarhad_ping_all_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        SpeedometerVectorIcon(
                            modifier = Modifier.size(18.dp),
                            color = if (servers.isPinging) onPrimaryColor else textPrimary,
                        )
                    }

                    // 3. More options (3 vertical dots) button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (showMenu) primaryColor else pillBg)
                            .clickable { showMenu = !showMenu }
                            .testTag("sarhad_more_options_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        ThreeDotsVerticalIcon(
                            modifier = Modifier.size(18.dp),
                            color = if (showMenu) onPrimaryColor else textPrimary,
                        )
                    }
                }
            }

            // Expandable Search Bar
            AnimatedVisibility(visible = searchOpen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search server name, host, VLESS, Reality, SNI…",
                                fontFamily = OutfitFont,
                                fontSize = 14.sp,
                                color = textMuted.copy(alpha = 0.7f),
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = OutfitFont,
                                fontSize = 14.sp,
                                color = textPrimary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("servers_search_input"),
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentLight,
                            modifier = Modifier.clickable { searchQuery = "" },
                        )
                    }
                }
            }

            // Subscription / Group Selector Strip
            if (subscriptions.isNotEmpty() || manualProfileCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (manualProfileCount > 0 || subscriptions.isEmpty()) {
                        val isStandaloneSelected = selectedSubscription?.id == STANDALONE_SUBSCRIPTION_ID
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isStandaloneSelected) primaryColor else pillBg)
                                .clickable { onSelectSubscription(STANDALONE_SUBSCRIPTION_ID) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "Loose configs ($manualProfileCount)",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isStandaloneSelected) onPrimaryColor else textPrimary,
                            )
                        }
                    }

                    subscriptions.forEach { sub ->
                        val isSubSelected = selectedSubscription?.id == sub.id
                        val subTitle = sub.name.ifBlank {
                            if (settings.maskAddresses) "Subscription" else sub.url.removePrefix("https://").removePrefix("http://").take(22)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSubSelected) primaryColor else pillBg)
                                .clickable { onSelectSubscription(sub.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (sub.pinned) {
                                Text(
                                    text = "📌",
                                    fontSize = 11.sp,
                                )
                            }
                            Text(
                                text = subTitle,
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSubSelected) onPrimaryColor else textPrimary,
                                maxLines = 1,
                            )
                            if (isSubSelected) {
                                Text(
                                    text = if (servers.isUpdating) "…" else "↻",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = onPrimaryColor,
                                    modifier = Modifier.clickable { onUpdateSubscription() },
                                )
                                Text(
                                    text = "×",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = onPrimaryColor.copy(alpha = 0.8f),
                                    modifier = Modifier.clickable { onDeleteSubscription(sub.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Multi-select toolbar
            if (multiSelectMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(selectedCardBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${selectedIds.size} selected",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (selectedIds.size == filteredProfiles.size) "None" else "All",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentLight,
                            modifier = Modifier.clickable {
                                selectedIds = if (selectedIds.size == filteredProfiles.size) {
                                    emptySet()
                                } else {
                                    filteredProfiles.map { it.id }.toSet()
                                }
                            },
                        )
                        Text(
                            text = "Delete",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = dangerColor,
                            modifier = Modifier.clickable {
                                onDeleteSelected(selectedIds)
                                selectedIds = emptySet()
                                multiSelectMode = false
                            },
                        )
                        Text(
                            text = "Done",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accentLight,
                            modifier = Modifier.clickable {
                                selectedIds = emptySet()
                                multiSelectMode = false
                            },
                        )
                    }
                }
            }

            // Error or status banner
            val activeBanner = servers.error ?: statusBanner
            activeBanner?.let { banner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(pillBg)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = banner,
                        fontFamily = OutfitFont,
                        fontSize = 12.sp,
                        color = if (servers.error != null) dangerColor else accentLight,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "×",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textMuted,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { statusBanner = null },
                    )
                }
            }

            // Server List
            Box(modifier = Modifier.weight(1f)) {
                if (filteredProfiles.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        color = cardBg,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching servers" else "No servers configured",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textPrimary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "Try clearing your search filter."
                                } else {
                                    "Tap the + floating button at the bottom right to paste a VLESS / VMess / Trojan / WireGuard link, scan a QR code, or import a subscription URL."
                                },
                                fontFamily = OutfitFont,
                                fontSize = 13.sp,
                                color = textMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp),
                    ) {
                        items(
                            items = filteredProfiles,
                            key = { it.id },
                        ) { profile ->
                            val selected = profile.id == selectedProfileId
                            val checkedInMulti = profile.id in selectedIds
                            val ping = profile.testDelayMillis
                            val (flagEmoji, cleanTitle) = remember(profile.remarks, profile.server) {
                                val (flag, rest) = splitLeadingFlag(profile.remarks)
                                flag to rest.ifBlank { profile.server }
                            }
                            val cardMenuExpanded = expandedCardMenuId == profile.id

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (selected) {
                                            Modifier.border(1.5.dp, primaryColor.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        if (multiSelectMode) {
                                            selectedIds = if (checkedInMulti) {
                                                selectedIds - profile.id
                                            } else {
                                                selectedIds + profile.id
                                            }
                                        } else {
                                            onSelectProfile(profile)
                                        }
                                    }
                                    .testTag("sarhad_server_card_${profile.id}"),
                                shape = RoundedCornerShape(26.dp),
                                color = if (selected || checkedInMulti) selectedCardBg else cardBg,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                if (flagEmoji != null && settings.flagStyle != FlagStyle.GLOBE) {
                                                    Text(
                                                        text = flagEmoji,
                                                        fontSize = 16.sp,
                                                    )
                                                }
                                                if (profile.pinned) {
                                                    Text(
                                                        text = "📌",
                                                        fontSize = 13.sp,
                                                    )
                                                }
                                                if (profile.concealed) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(pillBg)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                                    ) {
                                                        Text(
                                                            text = "Hidden",
                                                            fontFamily = OutfitFont,
                                                            fontSize = 10.sp,
                                                            color = textMuted,
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = cleanTitle,
                                                    fontFamily = OutfitFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = formatProfileProtocolLine(profile, uppercase = true),
                                                fontFamily = OutfitFont,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                letterSpacing = 0.5.sp,
                                                color = textMuted,
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            val endpointText = if (settings.maskAddresses) {
                                                "••••••••••:${profile.serverPort}"
                                            } else if (profile.serverPort > 0) {
                                                "${profile.server}:${profile.serverPort}"
                                            } else {
                                                profile.server
                                            }
                                            Text(
                                                text = endpointText,
                                                fontFamily = OutfitFont,
                                                fontSize = 12.sp,
                                                color = accentLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (selected && (stats.rxTotal > 0L || stats.txTotal > 0L)) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "↓ ${formatCompactBytes(stats.rxTotal)}  ·  ↑ ${formatCompactBytes(stats.txTotal)}",
                                                    fontFamily = OutfitFont,
                                                    fontSize = 12.sp,
                                                    color = textMuted,
                                                )
                                            }
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            val pingText = when {
                                                ping != null && ping > 0L -> "$ping ms"
                                                profile.lastTestAt != null && (ping == null || ping <= 0L) -> "Timeout"
                                                servers.isPinging -> "…"
                                                else -> "Untested"
                                            }
                                            val pingColor = when {
                                                ping != null && ping in 1..120 -> if (isStyleB) SarhadColors.Green else KiranColors.Mint
                                                ping != null && ping > 120 -> SarhadColors.Orange
                                                profile.lastTestAt != null -> dangerColor
                                                else -> textMuted
                                            }
                                            Text(
                                                text = pingText,
                                                fontFamily = OutfitFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = pingColor,
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                // Per-card quick menu toggle
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(pillBg)
                                                        .clickable {
                                                            expandedCardMenuId = if (cardMenuExpanded) null else profile.id
                                                        },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = "⋯",
                                                        fontFamily = OutfitFont,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = textPrimary,
                                                    )
                                                }

                                                if (selected || checkedInMulti) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(primaryColor),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Text(
                                                            text = "✓",
                                                            fontFamily = OutfitFont,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = onPrimaryColor,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Per-server expandable action strip (Pin, Hide, Copy Link, Delete)
                                    AnimatedVisibility(visible = cardMenuExpanded) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            ServerCardActionChip(
                                                label = if (profile.pinned) "Unpin" else "Pin",
                                                bg = pillBg,
                                                fg = textPrimary,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                onToggleProfilePinned(profile.id, !profile.pinned)
                                                expandedCardMenuId = null
                                            }
                                            ServerCardActionChip(
                                                label = if (profile.concealed) "Unhide" else "Hide",
                                                bg = pillBg,
                                                fg = textPrimary,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                onToggleProfileConcealed(profile.id, !profile.concealed)
                                                expandedCardMenuId = null
                                            }
                                            if (!profile.sourceLink.isNullOrBlank()) {
                                                ServerCardActionChip(
                                                    label = "Copy link",
                                                    bg = pillBg,
                                                    fg = accentLight,
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                    cm?.setPrimaryClip(ClipData.newPlainText("Hollow VPN Server", profile.sourceLink))
                                                    statusBanner = "Copied share link to clipboard"
                                                    expandedCardMenuId = null
                                                }
                                            }
                                            ServerCardActionChip(
                                                label = "Delete",
                                                bg = pillBg,
                                                fg = dangerColor,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                onDeleteSelected(setOf(profile.id))
                                                expandedCardMenuId = null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom-Right Floating Action Button (FAB) to add servers/subscriptions
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 14.dp, end = 4.dp)
                .size(60.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(primaryColor)
                .clickable(onClick = onOpenImport)
                .testTag("add_server_fab"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = onPrimaryColor,
            )
        }

        // More options popup menu (Style B Popup Menu)
        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showMenu = false },
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp)
                    .width(290.dp)
                    .shadow(24.dp, RoundedCornerShape(26.dp))
                    .border(1.dp, popupBorder, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = popupBg,
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    PopupMenuRow("↻", "Update all subscriptions", textPrimary) {
                        showMenu = false
                        onUpdateAll()
                        statusBanner = "Updating subscriptions & testing latency…"
                    }
                    PopupMenuRow("☑", if (multiSelectMode) "Exit multi-select" else "Select several", textPrimary) {
                        showMenu = false
                        multiSelectMode = !multiSelectMode
                    }
                    PopupMenuRow("⇅", "Sort by lowest ping", textPrimary) {
                        showMenu = false
                        onSetSort(ServersSort.PING)
                        statusBanner = "Sorted servers by lowest ping"
                    }
                    PopupMenuRow("🔤", "Sort by server name", textPrimary) {
                        showMenu = false
                        onSetSort(ServersSort.NAME)
                        statusBanner = "Sorted servers alphabetically"
                    }
                    PopupMenuRow("🕒", "Sort by last used", textPrimary) {
                        showMenu = false
                        onSetSort(ServersSort.LAST_USED)
                        statusBanner = "Sorted servers by most recently used"
                    }
                    PopupMenuRow("📋", "Sort by config order", textPrimary) {
                        showMenu = false
                        onSetSort(ServersSort.DEFAULT)
                        statusBanner = "Sorted servers by subscription order"
                    }
                    PopupMenuRow("⧉", "Delete duplicates", textPrimary) {
                        showMenu = false
                        val removed = onDeleteDuplicates()
                        statusBanner = if (removed > 0) "Removed $removed duplicate servers" else "No duplicate servers found"
                    }
                    PopupMenuRow("⊘", "Delete invalid / timed out", textPrimary) {
                        showMenu = false
                        val removed = onDeleteInvalid()
                        statusBanner = if (removed > 0) "Removed $removed unreachable servers" else "All tested servers are reachable"
                    }
                    PopupMenuRow("⤴", "Export all to clipboard", textPrimary) {
                        showMenu = false
                        val exported = onExportAll()
                        if (exported.isBlank()) {
                            statusBanner = "No servers to export"
                        } else {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("Hollow VPN Servers", exported))
                            statusBanner = "Exported ${servers.profiles.size} servers to clipboard"
                        }
                    }
                    PopupMenuRow("🗑", "Delete all servers", dangerColor) {
                        showMenu = false
                        onDeleteAll()
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerCardActionChip(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = fg,
        )
    }
}

@Composable
private fun PopupMenuRow(
    icon: String,
    label: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontFamily = OutfitFont,
            fontSize = 15.sp,
            color = textColor,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = textColor,
        )
    }
}

@Composable
fun SearchVectorIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        val stroke = 2.1f * ((sx + sy) / 2f)
        drawCircle(
            color = color,
            radius = 7f * sx,
            center = Offset(10.5f * sx, 10.5f * sy),
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(15.8f * sx, 15.8f * sy),
            end = Offset(20.5f * sx, 20.5f * sy),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun SpeedometerVectorIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        val stroke = 2f * ((sx + sy) / 2f)
        drawArc(
            color = color,
            startAngle = 160f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(3f * sx, 5f * sy),
            size = Size(18f * sx, 18f * sy),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawLine(color, Offset(12f * sx, 14f * sy), Offset(16f * sx, 9f * sy), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun ThreeDotsVerticalIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val r = size.width * 0.09f
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.22f))
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.50f))
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.78f))
    }
}

/**
 * Modal sheet for importing a VLESS/VMess/Trojan/WireGuard link, Subscription URL, QR code,
 * config file, or LAN phone import. Zero dummy data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportProfileModal(
    isStyleB: Boolean,
    onDismiss: () -> Unit,
    onSubmitText: (text: String, name: String?, onResult: (String?) -> Unit) -> Unit,
    onScanQr: () -> Unit,
    onImportFile: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }
    var subName by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var showPhoneImport by remember { mutableStateOf(false) }

    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg

    if (showPhoneImport) {
        PhoneImportDialog(
            onDismiss = { showPhoneImport = false },
            onImport = { received, onDone ->
                onSubmitText(received, null) { err ->
                    onDone(err)
                    if (err == null) {
                        showPhoneImport = false
                        onDismiss()
                    }
                }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Add server or subscription",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Paste a vless://, vmess://, trojan://, ss://, wg://, hy2:// link, Clash YAML, or an https:// subscription URL.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .padding(16.dp),
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "vless://... or https://...",
                        fontFamily = OutfitFont,
                        fontSize = 14.sp,
                        color = mutedColor.copy(alpha = 0.65f),
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        statusMessage = null
                    },
                    textStyle = TextStyle(
                        fontFamily = OutfitFont,
                        fontSize = 14.sp,
                        color = textColor,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("import_link_input"),
                )
            }

            if (inputText.trim().startsWith("http", ignoreCase = true)) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(fieldBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    if (subName.isEmpty()) {
                        Text(
                            text = "Subscription name (optional)",
                            fontFamily = OutfitFont,
                            fontSize = 14.sp,
                            color = mutedColor.copy(alpha = 0.65f),
                        )
                    }
                    BasicTextField(
                        value = subName,
                        onValueChange = { subName = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = OutfitFont,
                            fontSize = 14.sp,
                            color = textColor,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            statusMessage?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = msg,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isError) Color(0xFFFF8FB6) else primaryColor,
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickImportChip(
                    label = "Paste clipboard",
                    bg = fieldBg,
                    fg = textColor,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = cm?.primaryClip?.takeIf { it.itemCount > 0 }
                            ?.getItemAt(0)?.coerceToText(context)?.toString()?.trim().orEmpty()
                        if (clip.isNotEmpty()) {
                            inputText = clip
                            statusMessage = "Pasted from clipboard"
                            isError = false
                        } else {
                            statusMessage = "Clipboard is empty"
                            isError = true
                        }
                    },
                )
                QuickImportChip(
                    label = "Scan QR",
                    bg = fieldBg,
                    fg = textColor,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismiss()
                        onScanQr()
                    },
                )
                QuickImportChip(
                    label = "Config file",
                    bg = fieldBg,
                    fg = textColor,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismiss()
                        onImportFile()
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            QuickImportChip(
                label = "Receive link over local Wi-Fi (Phone / TV import)",
                bg = if (isStyleB) SarhadColors.PillBg else KiranColors.TagBg,
                fg = if (isStyleB) SarhadColors.PrimaryLight else KiranColors.MintSoft,
                modifier = Modifier.fillMaxWidth(),
                onClick = { showPhoneImport = true },
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(primaryColor)
                    .clickable {
                        val trimmed = inputText.trim()
                        if (trimmed.isEmpty()) {
                            statusMessage = "Please paste a link or subscription URL first"
                            isError = true
                            return@clickable
                        }
                        onSubmitText(trimmed, subName.takeIf { it.isNotBlank() }) { err ->
                            if (err == null) {
                                onDismiss()
                            } else {
                                statusMessage = err
                                isError = true
                            }
                        }
                    }
                    .testTag("confirm_import_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Import config",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = onPrimaryColor,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickImportChip(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = fg,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * Complete Appearance & Launcher Menu containing ALL style settings:
 * - UI Design Style (`Style A · Kiran` vs `Style B · Sarhad`)
 * - All 4 Launcher Icon variants (`Hollow Mint`, `Inverted`, `Dark`, `Themed`) + 108dp Showcase
 * - Theme Mode (`Auto`, `Light`, `Dark`, `AMOLED`)
 * - Accent Color Picker
 * - Font / Typography Selector
 * - Country Flag Style (`Twemoji`, `System Emoji`, `Globe`)
 * - Wallpaper & Backdrop (`WallpaperActivity`, Grayscale offline, Navbar blur & intensity)
 * - Privacy & Motion (`Mask addresses`, `Hide exit IP`, `Haptic feedback`, `Slow animations`, `Simple transitions`)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceStyleModal(
    settings: UiSettings,
    onUpdateSettings: ((UiSettings) -> UiSettings) -> Unit,
    onOpenLauncherShowcase: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isStyleB = settings.designStyle == DesignStyle.SARHAD_B
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val switchOffColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

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
            Text(
                text = "Appearance & Launcher",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Customize UI style, launcher icon, theme, typography, flags, wallpapers, and privacy.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(16.dp))

            // 1. UI Design Style (Style A vs Style B)
            ModalSectionTitle("UI Design Style", mutedColor)
            StyleOptionCard(
                title = "Style A · Kiran (M3 Expressive)",
                subtitle = "Emerald & Mint #86E3B0 · Morphing organic blob · 4-tab pill bar",
                badgeColor = KiranColors.Mint,
                badgeTextColor = KiranColors.OnMint,
                selected = settings.designStyle == DesignStyle.KIRAN_A,
                isStyleB = isStyleB,
                onClick = {
                    onUpdateSettings { it.copy(designStyle = DesignStyle.KIRAN_A) }
                },
            )
            Spacer(Modifier.height(8.dp))
            StyleOptionCard(
                title = "Style B · Sarhad (Scalloped Seal)",
                subtitle = "Deep Indigo & Violet #6C60F0 · 14-point rotating seal · Live logs & details",
                badgeColor = SarhadColors.Primary,
                badgeTextColor = Color.White,
                selected = settings.designStyle == DesignStyle.SARHAD_B,
                isStyleB = isStyleB,
                onClick = {
                    onUpdateSettings { it.copy(designStyle = DesignStyle.SARHAD_B) }
                },
            )

            Spacer(Modifier.height(18.dp))

            // 2. Launcher Icon Variants (All 4 aliases + live preview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModalSectionTitle("Launcher Icon", mutedColor)
                if (onOpenLauncherShowcase != null) {
                    Text(
                        text = "108dp Showcase ›",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = primaryColor,
                        modifier = Modifier.clickable {
                            onDismiss()
                            onOpenLauncherShowcase()
                        },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple(IconVariant.CHUMPY, "Hollow Mint", KiranColors.Bg to KiranColors.Mint),
                    Triple(IconVariant.INVERTED, "Inverted", KiranColors.Mint to KiranColors.OnMint),
                    Triple(IconVariant.DARK, "Night Dark", Color(0xFF070A09) to Color.White),
                    Triple(IconVariant.THEMED, "Themed", SarhadColors.SelectedServer to SarhadColors.PrimaryLight),
                ).forEach { (variant, label, colors) ->
                    val selected = settings.iconVariant == variant
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (selected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .background(fieldBg)
                            .clickable { onUpdateSettings { it.copy(iconVariant = variant) } }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.first)
                                .border(1.dp, mutedColor.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            KiranRayIcon(
                                modifier = Modifier.size(38.dp),
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
                            color = if (selected) primaryColor else textColor,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3. Theme Mode
            ModalSectionTitle("Theme Mode", mutedColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    ThemeMode.SYSTEM to "Auto",
                    ThemeMode.DARK to "Dark",
                    ThemeMode.AMOLED to "AMOLED",
                    ThemeMode.LIGHT to "Light",
                ).forEach { (mode, label) ->
                    val active = settings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(if (active) primaryColor else fieldBg)
                            .clickable { onUpdateSettings { it.copy(themeMode = mode) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (active) onPrimaryColor else textColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4. Accent Color Swatches
            ModalSectionTitle("Accent Palette", mutedColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val accents = listOf(
                    Triple(Accent.ADAPTIVE, "Auto", Color(0xFF86E3B0)),
                    Triple(Accent.GREEN, "Mint", Color(0xFF4ADE80)),
                    Triple(Accent.INDIGO, "Indigo", Color(0xFF6C60F0)),
                    Triple(Accent.SKY, "Sky", Color(0xFF6FACED)),
                    Triple(Accent.VIOLET, "Violet", Color(0xFFA855F7)),
                    Triple(Accent.RED, "Coral", Color(0xFFFF8FB6)),
                    Triple(Accent.ORANGE, "Peach", Color(0xFFFFB784)),
                    Triple(Accent.YELLOW, "Gold", Color(0xFFFACC15)),
                    Triple(Accent.MONO, "Mono", Color(0xFFE2E8F0)),
                    Triple(Accent.RAINBOW, "Rainbow", Color(0xFF38BDF8)),
                )
                accents.forEach { (accent, label, swatchColor) ->
                    val selected = settings.accent == accent
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            onUpdateSettings { it.copy(accent = accent) }
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .then(
                                    if (selected) Modifier.border(3.dp, textColor, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Text(
                                    text = "✓",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0B0B10),
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontFamily = OutfitFont,
                            fontSize = 11.sp,
                            color = if (selected) primaryColor else mutedColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 5. Typography / Font Picker
            ModalSectionTitle("Typography", mutedColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    FontRegistry.OUTFIT_ID to "Outfit (Expressive)",
                    FontRegistry.INTER_ID to "Inter",
                    FontRegistry.SYSTEM_ID to "System",
                ).forEach { (fontId, label) ->
                    val active = settings.fontId == fontId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(if (active) primaryColor else fieldBg)
                            .clickable { onUpdateSettings { it.copy(fontId = fontId) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (active) onPrimaryColor else textColor,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 6. Country Flag Style
            ModalSectionTitle("Country Flag Style", mutedColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    FlagStyle.TWEMOJI to "Twemoji",
                    FlagStyle.SYSTEM to "System Emoji",
                    FlagStyle.GLOBE to "Globe Only",
                ).forEach { (flagStyle, label) ->
                    val active = settings.flagStyle == flagStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(if (active) primaryColor else fieldBg)
                            .clickable { onUpdateSettings { it.copy(flagStyle = flagStyle) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (active) onPrimaryColor else textColor,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 7. Wallpapers & Backdrop
            ModalSectionTitle("Wallpaper & Backdrop", mutedColor)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .clickable {
                        context.startActivity(Intent(context, WallpaperActivity::class.java))
                    }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Configure custom wallpaper & dim backdrop",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor,
                    )
                    Text(
                        text = "Open ›",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryColor,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 8. Privacy, Haptics & Motion Toggles
            ModalSectionTitle("Privacy, Haptics & Motion", mutedColor)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(fieldBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                ModalToggleRow(
                    title = "Mask addresses",
                    subtitle = "Hide server IPs, domains & subscription URLs in lists",
                    checked = settings.maskAddresses,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(maskAddresses = enabled) }
                    },
                )
                ModalToggleRow(
                    title = "Hide exit IP on Home",
                    subtitle = "Mask verified exit IP in details view",
                    checked = settings.hideIp,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(hideIp = enabled) }
                    },
                )
                ModalToggleRow(
                    title = "Haptic feedback",
                    subtitle = "Tactile vibration pokes on interactive controls",
                    checked = settings.hapticFeedback,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(hapticFeedback = enabled) }
                    },
                )
                ModalToggleRow(
                    title = "Navigation bar blur",
                    subtitle = "Frosted glass backdrop under navigation bar",
                    checked = settings.navigationBarBlur,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(navigationBarBlur = enabled) }
                    },
                )
                if (settings.navigationBarBlur) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Blur intensity",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = mutedColor,
                        )
                        Slider(
                            value = settings.navigationBarBlurIntensity,
                            onValueChange = { v ->
                                onUpdateSettings { it.copy(navigationBarBlurIntensity = v) }
                            },
                            valueRange = 0.35f..0.85f,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${(settings.navigationBarBlurIntensity * 100).roundToInt()}%",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textColor,
                        )
                    }
                }
                ModalToggleRow(
                    title = "Slow animations",
                    subtitle = "Softer springs and relaxed motion speed",
                    checked = settings.slowAnimations,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(slowAnimations = enabled) }
                    },
                )
                ModalToggleRow(
                    title = "Simple transitions",
                    subtitle = "Lightweight page transitions for battery saving",
                    checked = settings.simpleTransitions,
                    textColor = textColor,
                    mutedColor = mutedColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    switchOffColor = switchOffColor,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(simpleTransitions = enabled) }
                    },
                )
            }

            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun ModalSectionTitle(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = color,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ModalToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    onPrimaryColor: Color,
    switchOffColor: Color,
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
                color = textColor,
            )
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
        }
        Spacer(Modifier.width(12.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeTrackColor = primaryColor,
            activeThumbColor = onPrimaryColor,
            inactiveTrackColor = switchOffColor,
            inactiveThumbColor = mutedColor,
        )
    }
}

@Composable
private fun StyleOptionCard(
    title: String,
    subtitle: String,
    badgeColor: Color,
    badgeTextColor: Color,
    selected: Boolean,
    isStyleB: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        selected && isStyleB -> SarhadColors.SelectedServer
        selected -> KiranColors.CardActive
        isStyleB -> SarhadColors.Bg
        else -> KiranColors.Bg
    }
    val borderMod = if (selected) {
        Modifier.border(2.dp, badgeColor, RoundedCornerShape(24.dp))
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .then(borderMod)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (selected) "✓" else "•",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = badgeTextColor,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted,
            )
        }
    }
}

/**
 * Spoof as Happ Modal (moved from Classic Connection Settings into Style A & Style B).
 * Controls subscription fetch fingerprint headers, User ID / HWID regeneration, and all 7 fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappSpoofModal(
    isStyleB: Boolean,
    settings: UiSettings,
    onUpdateSettings: ((UiSettings) -> UiSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val dangerColor = if (isStyleB) SarhadColors.Pink else KiranColors.Orange

    var confirmRegen by remember { mutableStateOf(false) }

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
            Text(
                text = "Spoof as Happ",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Send Happ client headers and hardware identity when fetching subscriptions.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Happ Spoof",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor,
                    )
                    Text(
                        text = if (settings.happSpoof) "Active on all subscription fetches" else "Disabled",
                        fontFamily = OutfitFont,
                        fontSize = 12.sp,
                        color = mutedColor,
                    )
                }
                ExpressiveSwitch(
                    checked = settings.happSpoof,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(happSpoof = enabled) }
                    },
                    activeTrackColor = primaryColor,
                    activeThumbColor = onPrimaryColor,
                    inactiveTrackColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg,
                    inactiveThumbColor = mutedColor,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Regenerate Identity button with confirmation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(fieldBg)
                    .clickable {
                        if (confirmRegen) {
                            onUpdateSettings {
                                it.copy(
                                    happUserId = HappSpoof.randomUserId(),
                                    happHwid = HappSpoof.randomHwid(),
                                )
                            }
                            confirmRegen = false
                        } else {
                            confirmRegen = true
                        }
                    }
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (confirmRegen) "Tap again to confirm new identity" else "Regenerate User ID & HWID",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (confirmRegen) dangerColor else textColor,
                        )
                        Text(
                            text = "Generates a fresh device fingerprint for subscription panels",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = mutedColor,
                        )
                    }
                    Text(
                        text = "↻",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (confirmRegen) dangerColor else primaryColor,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HappEditableField("App version", settings.happAppVersion, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happAppVersion = v) }
            }
            HappEditableField("Language / Locale", settings.happLocale, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happLocale = v) }
            }
            HappEditableField("OS", settings.happOs, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happOs = v) }
            }
            HappEditableField("OS version", settings.happOsVersion, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happOsVersion = v) }
            }
            HappEditableField("Device model", settings.happDeviceModel, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happDeviceModel = v) }
            }
            HappEditableField("User ID", settings.happUserId, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happUserId = v) }
            }
            HappEditableField("HWID", settings.happHwid, fieldBg, textColor, mutedColor) { v ->
                onUpdateSettings { it.copy(happHwid = v) }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(primaryColor)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Done",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = onPrimaryColor,
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HappEditableField(
    label: String,
    value: String,
    fieldBg: Color,
    textColor: Color,
    mutedColor: Color,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fieldBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontSize = 11.sp,
            color = mutedColor,
        )
        Spacer(Modifier.height(2.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textColor,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Delay Test URL & Background Subscription Refresh Modal (moved from Classic Connection Settings).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelayTestAndRefreshModal(
    isStyleB: Boolean,
    connectionSettings: ConnectionSettings,
    subscriptions: List<SubscriptionEntity>,
    maskAddresses: Boolean,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onToggleAutoUpdate: (String, Boolean) -> Unit,
    onUpdateAllSubscriptions: () -> Unit,
    onPingAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val switchOffColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg

    var urlText by remember(connectionSettings.delayTestUrl) {
        mutableStateOf(connectionSettings.delayTestUrl)
    }

    val presets = listOf(
        "Google 204" to "https://www.gstatic.com/generate_204",
        "Cloudflare 204" to "https://cp.cloudflare.com/generate_204",
        "Firefox Portal" to "http://detectportal.firefox.com/success.txt",
        "Apple Captive" to "http://captive.apple.com/hotspot-detect.html",
    )

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
            Text(
                text = "Delay Test URL & Subscriptions",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Configure the HTTP endpoint fetched through every server to measure real latency, and manage background subscription updates.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(14.dp))

            ModalSectionTitle("Delay Test URL", mutedColor)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(fieldBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                BasicTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        onUpdateConnection { conn -> conn.copy(delayTestUrl = it.trim()) }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = textColor,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { (label, presetUrl) ->
                    val active = urlText.trim() == presetUrl
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (active) primaryColor else fieldBg)
                            .clickable {
                                urlText = presetUrl
                                onUpdateConnection { it.copy(delayTestUrl = presetUrl) }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = label,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (active) onPrimaryColor else textColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            ModalSectionTitle("Background Subscription Auto-Update", mutedColor)
            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(fieldBg)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "No remote subscriptions added yet. Add a subscription URL in Servers to configure background refresh.",
                        fontFamily = OutfitFont,
                        fontSize = 13.sp,
                        color = mutedColor,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(fieldBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    subscriptions.forEach { sub ->
                        val title = sub.name.ifBlank {
                            if (maskAddresses) "Subscription" else sub.url
                        }
                        ModalToggleRow(
                            title = title,
                            subtitle = if (sub.autoUpdate) "Auto-updates in background" else "Manual update only",
                            checked = sub.autoUpdate,
                            textColor = textColor,
                            mutedColor = mutedColor,
                            primaryColor = primaryColor,
                            onPrimaryColor = onPrimaryColor,
                            switchOffColor = switchOffColor,
                            onCheckedChange = { enabled ->
                                onToggleAutoUpdate(sub.id, enabled)
                            },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(fieldBg)
                        .clickable {
                            onUpdateAllSubscriptions()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "↻  Update all subscriptions now",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryColor,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(fieldBg)
                        .clickable {
                            onPingAll()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Test all pings",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(primaryColor)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Save & close",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = onPrimaryColor,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

/**
 * Region Routing Selector Strip (moved from Classic Connection Settings into Style A & Style B).
 * Supports Global routing or bypassing specific countries (RU, CN, IR, AF, ID, TR, BR).
 */
@Composable
fun RegionRoutingSelectorCard(
    isStyleB: Boolean,
    selectedMode: RoutingMode,
    flagStyle: FlagStyle,
    onSelectMode: (RoutingMode) -> Unit,
) {
    val cardBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val pillBg = if (isStyleB) SarhadColors.PillBg else KiranColors.NavBg
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted

    val modes = listOf(
        Triple(RoutingMode.GLOBAL, "Global", "🌐"),
        Triple(RoutingMode.BYPASS_RU, "Russia", "🇷🇺"),
        Triple(RoutingMode.BYPASS_CN, "China", "🇨🇳"),
        Triple(RoutingMode.BYPASS_IR, "Iran", "🇮🇷"),
        Triple(RoutingMode.BYPASS_AF, "Afghanistan", "🇦🇫"),
        Triple(RoutingMode.BYPASS_ID, "Indonesia", "🇮🇩"),
        Triple(RoutingMode.BYPASS_TR, "Turkey", "🇹🇷"),
        Triple(RoutingMode.BYPASS_BR, "Brazil", "🇧🇷"),
    )

    val subtitle = when (selectedMode) {
        RoutingMode.GLOBAL -> "Route all international & domestic traffic through VPN"
        else -> {
            val country = modes.firstOrNull { it.first == selectedMode }?.second ?: ""
            "Don't route $country domains & IPs (direct connection)"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isStyleB) 28.dp else 36.dp),
        color = cardBg,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Regional Routing Mode",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                modes.forEach { (mode, name, emoji) ->
                    val active = selectedMode == mode
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) primaryColor else pillBg)
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val regionCode = mode.region
                        if (regionCode != null && flagStyle == FlagStyle.TWEMOJI) {
                            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                                CountryFlag(code = regionCode.uppercase(), twemoji = true)
                            }
                        } else if (flagStyle != FlagStyle.GLOBE) {
                            Text(text = emoji, fontSize = 14.sp)
                        }
                        Text(
                            text = name,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (active) onPrimaryColor else textColor,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time Xray Core Logs Modal used across both Style A and Style B.
 * Displays actual core log output from `viewModel.coreLogs` plus live tunnel state, with Copy support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLogsModal(
    isStyleB: Boolean,
    coreLogs: String?,
    tunnelStatus: TunnelStatus,
    selectedProfile: ProfileEntity?,
    connectionSettings: ConnectionSettings,
    coreVersion: String?,
    pingMillis: Long?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    var copiedNotice by remember { mutableStateOf(false) }

    val diagnosticSummary = remember(coreLogs, tunnelStatus, selectedProfile, connectionSettings, coreVersion, pingMillis) {
        buildString {
            appendLine("[Core] ${coreVersion ?: "Xray Core"} · state=${tunnelStatus.state.name}")
            if (selectedProfile != null) {
                appendLine("[Profile] ${stripFlag(selectedProfile.remarks).ifBlank { selectedProfile.server }} (${formatProfileProtocolLine(selectedProfile, uppercase = true)})")
                appendLine("[Endpoint] ${selectedProfile.server}:${selectedProfile.serverPort} · sni=${selectedProfile.sni.ifBlank { "default" }}")
            } else {
                appendLine("[Profile] No server selected")
            }
            if (pingMillis != null && pingMillis > 0) {
                appendLine("[Latency] Last measured RTT: ${pingMillis}ms (testUrl=${connectionSettings.delayTestUrl})")
            }
            appendLine("[Routing] mode=${connectionSettings.routingMode.name}, bypassLan=${connectionSettings.bypassLan}, bypassDomestic=${connectionSettings.bypassDomestic}, blockAds=${connectionSettings.blockAds}, dns=${connectionSettings.dnsProvider}")
            if (tunnelStatus.error != null) {
                appendLine("[Error] ${tunnelStatus.error}")
            }
            if (!coreLogs.isNullOrBlank()) {
                appendLine("----------------------------------------")
                append(coreLogs.trim())
            } else if (tunnelStatus.state != TunnelState.UP) {
                appendLine("----------------------------------------")
                append("Connect the VPN tunnel to stream live Xray core logs.")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Core Logs",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = textColor,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(fieldBg)
                        .clickable {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("Hollow VPN Logs", diagnosticSummary))
                            copiedNotice = true
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = if (copiedNotice) "Copied ✓" else "Copy logs",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = primaryColor,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = diagnosticSummary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp,
                    color = textColor,
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(primaryColor)
                    .clickable(onClick = onDismiss)
                    .testTag("sarhad_close_logs_button"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Close",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = onPrimaryColor,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * DNS Configuration Modal Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsPickerModal(
    isStyleB: Boolean,
    currentDns: String,
    onSelectDns: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint

    val options = listOf(
        Triple("DoH · 1.1.1.1", "Cloudflare DNS over HTTPS", "https://1.1.1.1/dns-query · Fast & private"),
        Triple("DoH · 9.9.9.9", "Quad9 Secure DNS", "https://dns.quad9.net/dns-query · Malware blocking"),
        Triple("DoH · 8.8.8.8", "Google Public DNS", "https://dns.google/dns-query · Global Anycast"),
        Triple("DoH · 94.140.14.14", "AdGuard DNS", "https://dns.adguard-dns.com/dns-query · Ad & tracker filtering"),
    )

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
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Text(
                text = "DNS Resolver",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Encrypted DNS queries routed through the Xray core.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(16.dp))

            options.forEach { (id, title, sub) ->
                val selected = currentDns == id
                val rowBg = when {
                    selected && isStyleB -> SarhadColors.SelectedServer
                    selected -> KiranColors.CardActive
                    isStyleB -> SarhadColors.Bg
                    else -> KiranColors.Bg
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(rowBg)
                        .clickable {
                            onSelectDns(id)
                            onDismiss()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$title ($id)",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textColor,
                        )
                        Text(
                            text = sub,
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = mutedColor,
                        )
                    }
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(primaryColor)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "Active",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = onPrimaryColor,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Fragment & Mux configuration sheet for both Style A and Style B.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentMuxModal(
    isStyleB: Boolean,
    connectionSettings: ConnectionSettings,
    onUpdateConnection: ((ConnectionSettings) -> ConnectionSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg

    var fragEnabled by remember(connectionSettings) { mutableStateOf(connectionSettings.fragmentEnabled) }
    var fragLength by remember(connectionSettings) { mutableStateOf(connectionSettings.fragmentLength) }
    var fragInterval by remember(connectionSettings) { mutableStateOf(connectionSettings.fragmentInterval) }
    var muxEnabled by remember(connectionSettings) { mutableStateOf(connectionSettings.muxEnabled) }
    var muxConc by remember(connectionSettings) { mutableStateOf(connectionSettings.muxConcurrency.toString()) }

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
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Fragment & Mux",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Split TLS ClientHello packets to bypass DPI inspection and multiplex TCP streams.",
                fontFamily = OutfitFont,
                fontSize = 13.sp,
                color = mutedColor,
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TLS Fragment",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor,
                    )
                    Text(
                        text = "Length $fragLength bytes · Interval $fragInterval ms",
                        fontFamily = OutfitFont,
                        fontSize = 12.sp,
                        color = mutedColor,
                    )
                }
                ExpressiveSwitch(
                    checked = fragEnabled,
                    onCheckedChange = { fragEnabled = it },
                    activeTrackColor = primaryColor,
                    activeThumbColor = onPrimaryColor,
                    inactiveTrackColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg,
                    inactiveThumbColor = mutedColor,
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(fieldBg)
                        .padding(14.dp),
                ) {
                    Text("Fragment length (B)", fontFamily = OutfitFont, fontSize = 12.sp, color = mutedColor)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = fragLength,
                        onValueChange = { fragLength = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textColor,
                        ),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(fieldBg)
                        .padding(14.dp),
                ) {
                    Text("Fragment interval (ms)", fontFamily = OutfitFont, fontSize = 12.sp, color = mutedColor)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = fragInterval,
                        onValueChange = { fragInterval = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textColor,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(fieldBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Xray Mux.Cool",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor,
                    )
                    Text(
                        text = "Concurrency: ${muxConc.toIntOrNull() ?: 8} streams",
                        fontFamily = OutfitFont,
                        fontSize = 12.sp,
                        color = mutedColor,
                    )
                }
                ExpressiveSwitch(
                    checked = muxEnabled,
                    onCheckedChange = { muxEnabled = it },
                    activeTrackColor = primaryColor,
                    activeThumbColor = onPrimaryColor,
                    inactiveTrackColor = if (isStyleB) SarhadColors.SwitchOff else KiranColors.SwitchOffBg,
                    inactiveThumbColor = mutedColor,
                )
            }

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(primaryColor)
                    .clickable {
                        onUpdateConnection {
                            it.copy(
                                fragmentEnabled = fragEnabled,
                                fragmentLength = fragLength.ifBlank { "100-200" },
                                fragmentInterval = fragInterval.ifBlank { "10-20" },
                                muxEnabled = muxEnabled,
                                muxConcurrency = muxConc.toIntOrNull()?.coerceIn(1, 64) ?: 8,
                            )
                        }
                        onDismiss()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Save & apply",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = onPrimaryColor,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Screen 8 · Launcher icon showcase & interactive switcher (`Hollow VPN`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiranLauncherIconModal(
    currentVariant: IconVariant,
    onSelectVariant: (IconVariant) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KiranColors.Bg,
        contentColor = KiranColors.TextPrimary,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Launcher icon",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = (-1).sp,
                    color = KiranColors.TextPrimary,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(KiranColors.TagBg)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Adaptive · 108dp",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = KiranColors.MintSoft,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(44.dp),
                color = KiranColors.Card,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(48.dp))
                            .background(KiranColors.Bg),
                        contentAlignment = Alignment.Center,
                    ) {
                        KiranRayIcon(
                            modifier = Modifier.size(160.dp),
                            rayColor = KiranColors.Mint,
                            centerCutoutColor = KiranColors.Bg,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Hollow VPN · 108 × 108 dp (66dp safe zone)",
                        fontFamily = OutfitFont,
                        fontSize = 13.sp,
                        color = KiranColors.TextMuted,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdaptiveShapePreviewCard(
                    label = "Hollow Mint (Circle)",
                    selected = currentVariant == IconVariant.CHUMPY,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectVariant(IconVariant.CHUMPY) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(KiranColors.Bg),
                        contentAlignment = Alignment.Center,
                    ) {
                        KiranRayIcon(
                            modifier = Modifier.size(76.dp),
                            rayColor = KiranColors.Mint,
                            centerCutoutColor = KiranColors.Bg,
                        )
                    }
                }
                AdaptiveShapePreviewCard(
                    label = "Night Dark (Squircle)",
                    selected = currentVariant == IconVariant.DARK,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectVariant(IconVariant.DARK) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF070A09)),
                        contentAlignment = Alignment.Center,
                    ) {
                        KiranRayIcon(
                            modifier = Modifier.size(76.dp),
                            rayColor = Color.White,
                            centerCutoutColor = Color(0xFF070A09),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdaptiveShapePreviewCard(
                    label = "Inverted (Blob)",
                    selected = currentVariant == IconVariant.INVERTED,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectVariant(IconVariant.INVERTED) },
                ) {
                    MorphingBlobBox(
                        modifier = Modifier.size(92.dp),
                        color = KiranColors.Mint,
                        breathe = false,
                    ) {
                        KiranRayIcon(
                            modifier = Modifier.size(76.dp),
                            rayColor = KiranColors.OnMint,
                            centerCutoutColor = KiranColors.Mint,
                        )
                    }
                }
                AdaptiveShapePreviewCard(
                    label = "Themed (Monochrome)",
                    selected = currentVariant == IconVariant.THEMED,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectVariant(IconVariant.THEMED) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(KiranColors.CardActive),
                        contentAlignment = Alignment.Center,
                    ) {
                        KiranRayIcon(
                            modifier = Modifier.size(76.dp),
                            rayColor = KiranColors.Mint,
                            centerCutoutColor = KiranColors.CardActive,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdaptiveShapePreviewCard(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .then(
                if (selected) Modifier.border(2.dp, KiranColors.Mint, RoundedCornerShape(28.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = if (selected) KiranColors.CardActive else KiranColors.Card,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                fontFamily = OutfitFont,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (selected) KiranColors.Mint else KiranColors.TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
