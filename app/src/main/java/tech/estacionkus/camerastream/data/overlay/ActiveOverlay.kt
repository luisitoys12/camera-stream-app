package tech.estacionkus.camerastream.data.overlay

import android.net.Uri
import tech.estacionkus.camerastream.data.media.OverlayCategory

enum class OverlayPosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

data class ActiveOverlay(
    val id: String,
    val uri: Uri,
    val name: String,
    val category: OverlayCategory,
    val isVideo: Boolean,
    val position: OverlayPosition = OverlayPosition.BOTTOM_LEFT,
    val scalePercent: Float = 30f,   // % del ancho de pantalla
    val alpha: Float = 1f,
    val isLooping: Boolean = true,
    val autoHideAfterMs: Long? = null // null = permanente
)
