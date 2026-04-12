package tech.estacionkus.camerastream.streaming

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import tech.estacionkus.camerastream.domain.model.AnimationType
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType

@Composable
fun OverlayRenderer(overlays: List<OverlayItem>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        overlays.filter { it.isVisible }.sortedBy { it.zOrder }.forEach { overlay ->
            key(overlay.id) {
                val enterTransition = when (overlay.animationType) {
                    AnimationType.FADE_IN -> fadeIn()
                    AnimationType.SLIDE_LEFT -> slideInHorizontally { -it }
                    AnimationType.SLIDE_RIGHT -> slideInHorizontally { it }
                    AnimationType.SLIDE_UP -> slideInVertically { it }
                    AnimationType.BOUNCE -> fadeIn() + scaleIn(initialScale = 0.5f)
                    AnimationType.SCALE_IN -> scaleIn(initialScale = 0.1f)
                    else -> fadeIn()
                }

                AnimatedVisibility(
                    visible = true,
                    enter = enterTransition,
                    exit = fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth(overlay.width)
                        .fillMaxHeight(overlay.height)
                        .offset(x = (overlay.x * 100).dp, y = (overlay.y * 100).dp)
                        .align(
                            when (overlay.type) {
                                OverlayType.LOWER_THIRD -> Alignment.BottomStart
                                OverlayType.WATERMARK -> Alignment.TopEnd
                                OverlayType.SCOREBOARD -> Alignment.TopStart
                                OverlayType.CHAT_WIDGET -> Alignment.BottomEnd
                                OverlayType.TIMER -> Alignment.TopCenter
                                OverlayType.QR_CODE -> Alignment.BottomStart
                                else -> Alignment.Center
                            }
                        )
                ) {
                    when (overlay.type) {
                        OverlayType.IMAGE, OverlayType.GIF, OverlayType.WATERMARK -> {
                            AsyncImage(
                                model = overlay.uri,
                                contentDescription = null,
                                alpha = overlay.alpha,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        OverlayType.TEXT, OverlayType.SOCIAL_HANDLE -> {
                            TextOverlay(overlay)
                        }
                        OverlayType.LOWER_THIRD -> {
                            LowerThirdOverlay(text = overlay.text, alpha = overlay.alpha)
                        }
                        OverlayType.TICKER -> {
                            TickerOverlay(overlay)
                        }
                        OverlayType.COUNTDOWN -> {
                            CountdownOverlay()
                        }
                        OverlayType.TIMER -> {
                            TimerOverlay()
                        }
                        OverlayType.SCOREBOARD -> {
                            ScoreboardOverlay(text = overlay.text)
                        }
                        OverlayType.QR_CODE -> {
                            QrCodeOverlay(data = overlay.text)
                        }
                        OverlayType.ALERT -> {
                            AlertOverlay(text = overlay.text)
                        }
                        OverlayType.CHAT_WIDGET, OverlayType.BROWSER -> {
                            // Handled externally
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextOverlay(overlay: OverlayItem) {
    Box(
        modifier = Modifier
            .background(Color(overlay.backgroundColor), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            overlay.text,
            color = Color(overlay.textColor),
            fontSize = overlay.fontSize.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TickerOverlay(overlay: OverlayItem) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(overlay.text) {
        while (true) {
            kotlinx.coroutines.delay(50)
            offsetX -= (overlay.scrollSpeed.coerceAtLeast(2f))
            if (offsetX < -2000f) offsetX = 1000f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(overlay.backgroundColor).copy(alpha = overlay.alpha), RoundedCornerShape(2.dp))
            .padding(vertical = 4.dp)
    ) {
        Text(
            overlay.text,
            color = Color(overlay.textColor),
            fontSize = overlay.fontSize.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            modifier = Modifier.offset(x = offsetX.dp)
        )
    }
}

@Composable
fun LowerThirdOverlay(text: String, alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = alpha * 0.75f), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CountdownOverlay() {
    var seconds by remember { mutableIntStateOf(10) }
    LaunchedEffect(Unit) {
        while (seconds > 0) {
            kotlinx.coroutines.delay(1000)
            seconds--
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .background(Color.Black.copy(0.7f), RoundedCornerShape(40.dp))
    ) {
        Text("$seconds", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TimerOverlay() {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsedMs += 1000
        }
    }
    val minutes = elapsedMs / 60000
    val seconds = (elapsedMs % 60000) / 1000
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(Color(0xCC000000), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            "%02d:%02d".format(minutes, seconds),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScoreboardOverlay(text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xCC000000), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QrCodeOverlay(data: String) {
    val bitmap = remember(data) { generateQrBitmap(data, 200) }
    if (bitmap != null) {
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            drawImage(bitmap.asImageBitmap())
        }
    }
}

@Composable
fun AlertOverlay(text: String) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        visible = false
    }
    AnimatedVisibility(visible = visible, enter = slideInVertically { -it } + fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE6E53935), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun generateQrBitmap(data: String, size: Int): Bitmap? {
    if (data.isBlank()) return null
    return try {
        val writer = QRCodeWriter()
        val matrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (_: Exception) { null }
}
