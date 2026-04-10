package tech.estacionkus.camerastream.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val id: String,
    val name: String,
    val overlays: List<OverlayItem> = emptyList(),
    val cameraSource: CameraSource = CameraSource.BACK
)

@Serializable
data class OverlayItem(
    val id: String,
    val type: OverlayType,
    val uri: String = "",        // for images/GIFs
    val text: String = "",       // for lower thirds / text
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 1f,
    val height: Float = 0.15f,
    val alpha: Float = 1f,
    val zOrder: Int = 0
)

enum class OverlayType {
    IMAGE,          // static image
    GIF,            // animated GIF (Coil handles it natively)
    LOWER_THIRD,    // text bar bottom
    WATERMARK,      // persistent logo
    COUNTDOWN,      // countdown timer
    SCOREBOARD,     // sport score
    BROWSER         // WebView overlay (Pro)
}

enum class CameraSource { FRONT, BACK, WIDE, TELE }
