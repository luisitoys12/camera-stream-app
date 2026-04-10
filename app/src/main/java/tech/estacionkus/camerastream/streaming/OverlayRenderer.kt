package tech.estacionkus.camerastream.streaming

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OverlayRenderer(overlays: List<OverlayItem>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        overlays.sortedBy { it.zOrder }.forEach { overlay ->
            key(overlay.id) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth(overlay.width)
                        .fillMaxHeight(overlay.height)
                        .align(
                            when (overlay.type) {
                                OverlayType.LOWER_THIRD -> Alignment.BottomStart
                                OverlayType.WATERMARK -> Alignment.TopEnd
                                OverlayType.SCOREBOARD -> Alignment.TopStart
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
                        OverlayType.LOWER_THIRD -> {
                            LowerThirdOverlay(text = overlay.text, alpha = overlay.alpha)
                        }
                        OverlayType.COUNTDOWN -> {
                            CountdownOverlay()
                        }
                        OverlayType.SCOREBOARD -> {
                            ScoreboardOverlay(text = overlay.text)
                        }
                        OverlayType.BROWSER -> {
                            // WebView overlay — handled in Pro WebViewOverlay composable
                        }
                    }
                }
            }
        }
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
    var seconds by remember { mutableStateOf(10) }
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
fun ScoreboardOverlay(text: String) {
    // Format: "TeamA 2 - 1 TeamB"
    Row(
        modifier = Modifier
            .background(Color(0xCC000000), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
