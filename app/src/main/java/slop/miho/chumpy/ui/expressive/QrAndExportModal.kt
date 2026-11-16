package slop.miho.chumpy.ui.expressive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.sub.LinkSerializer

/**
 * Per-server Share URI + QR code + full Xray JSON inspection & export sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrAndExportModal(
    isStyleB: Boolean,
    profile: ProfileEntity,
    shareUri: String,
    fullConfigJson: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetBg = if (isStyleB) SarhadColors.Card else KiranColors.Card
    val fieldBg = if (isStyleB) SarhadColors.Bg else KiranColors.Bg
    val textColor = if (isStyleB) SarhadColors.TextPrimary else KiranColors.TextPrimary
    val mutedColor = if (isStyleB) SarhadColors.TextMuted else KiranColors.TextMuted
    val primaryColor = if (isStyleB) SarhadColors.Primary else KiranColors.Mint
    val onPrimaryColor = if (isStyleB) Color.White else KiranColors.OnMint

    var mode by remember { mutableIntStateOf(if (shareUri.isNotBlank()) 0 else 1) } // 0=URI/QR, 1=Xray JSON
    var copied by remember { mutableStateOf(false) }

    val qrBitmap = remember(shareUri) {
        if (shareUri.isNotBlank()) LinkSerializer.generateQrBitmap(shareUri, 640) else null
    }
    val displayedText = if (mode == 0 && shareUri.isNotBlank()) shareUri else fullConfigJson

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = profile.remarks.ifBlank { profile.server },
                fontFamily = OutfitFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(fieldBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EditorChip(
                    label = "Share URI & QR",
                    active = mode == 0,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    inactiveBg = Color.Transparent,
                    textColor = textColor,
                ) {
                    mode = 0
                    copied = false
                }
                EditorChip(
                    label = "Xray JSON",
                    active = mode == 1,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    inactiveBg = Color.Transparent,
                    textColor = textColor,
                ) {
                    mode = 1
                    copied = false
                }
            }

            if (mode == 0 && qrBitmap != null) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Server QR Code",
                        modifier = Modifier.size(188.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mode == 0) 110.dp else 280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(fieldBg)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
            ) {
                SelectionContainer {
                    Text(
                        text = displayedText.ifBlank { "No share URI available for this entry; switch to Xray JSON." },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = textColor,
                    )
                }
            }

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
                        .background(fieldBg)
                        .clickable {
                            if (displayedText.isNotBlank()) {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("Hollow VPN Config", displayedText))
                                copied = true
                            }
                        }
                        .testTag("qr_export_copy_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (copied) "Copied ✓" else "Copy",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(primaryColor)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Close",
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
