package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import tech.estacionkus.camerastream.data.media.MediaAssetType
import tech.estacionkus.camerastream.data.overlay.ActiveOverlay
import tech.estacionkus.camerastream.data.overlay.OverlayPosition
import tech.estacionkus.camerastream.ui.screens.media.VideoPreview
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/**
 * Renderiza todos los overlays activos encima del preview de cámara.
 * Cada overlay se posiciona usando [OverlayPosition] y respeta
 * su scalePercent, alpha y autoHideAfterMs.
 */
@Composable
fun OverlayRenderer(
    overlays: List<ActiveOverlay>,
    onAutoHide: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        overlays.forEach { overlay ->
            key(overlay.id) {
                OverlayItem(
                    overlay = overlay,
                    onAutoHide = { onAutoHide(overlay.id) }
                )
            }
        }
    }
}

@Composable
private fun OverlayItem(
    overlay: ActiveOverlay,
    onAutoHide: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    // Auto-hide timer
    LaunchedEffect(overlay.id) {
        visible = true
        overlay.autoHideAfterMs?.let { ms ->
            delay(ms)
            visible = false
            delay(400) // fade out
            onAutoHide()
        }
    }

    val alignment = overlay.position.toAlignment()
    val padding = overlay.position.toPadding()

    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(alignment),
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 0.85f)
    ) {
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(overlay.scalePercent / 100f)
                .alpha(overlay.alpha)
        ) {
            if (overlay.isVideo) {
                VideoPreview(
                    uri = overlay.uri,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AsyncImage(
                    model = overlay.uri,
                    contentDescription = overlay.name,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun OverlayPosition.toAlignment(): Alignment = when (this) {
    OverlayPosition.TOP_LEFT     -> Alignment.TopStart
    OverlayPosition.TOP_CENTER   -> Alignment.TopCenter
    OverlayPosition.TOP_RIGHT    -> Alignment.TopEnd
    OverlayPosition.CENTER       -> Alignment.Center
    OverlayPosition.BOTTOM_LEFT  -> Alignment.BottomStart
    OverlayPosition.BOTTOM_CENTER-> Alignment.BottomCenter
    OverlayPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
}

private fun OverlayPosition.toPadding(): PaddingValues = when (this) {
    OverlayPosition.TOP_LEFT, OverlayPosition.TOP_CENTER, OverlayPosition.TOP_RIGHT ->
        PaddingValues(top = 56.dp, start = 12.dp, end = 12.dp)
    OverlayPosition.BOTTOM_LEFT, OverlayPosition.BOTTOM_CENTER, OverlayPosition.BOTTOM_RIGHT ->
        PaddingValues(bottom = 100.dp, start = 12.dp, end = 12.dp)
    OverlayPosition.CENTER ->
        PaddingValues(0.dp)
}
