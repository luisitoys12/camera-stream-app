package tech.estacionkus.camerastream.domain.model

data class Scene(
    val id: String,
    val name: String,
    val overlays: List<OverlayItem> = emptyList(),
    val isDefault: Boolean = false,
    val thumbnailColor: Long = 0xFF1A1A2E
)

data class OverlayItem(
    val id: String,
    val type: OverlayType,
    val uri: String = "",
    val text: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 1f,
    val height: Float = 0.15f,
    val alpha: Float = 1f,
    val zOrder: Int = 0,
    val animationType: AnimationType = AnimationType.NONE,
    val isVisible: Boolean = true,
    val rotation: Float = 0f,
    val fontSize: Float = 18f,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0xCC000000,
    val scrollSpeed: Float = 0f
)

enum class OverlayType {
    IMAGE, GIF, LOWER_THIRD, WATERMARK, COUNTDOWN, SCOREBOARD, BROWSER,
    TEXT, TICKER, ALERT, CHAT_WIDGET, TIMER, QR_CODE, SOCIAL_HANDLE
}

enum class AnimationType { NONE, FADE_IN, SLIDE_LEFT, SLIDE_RIGHT, SLIDE_UP, BOUNCE, SCALE_IN }

enum class TransitionType { CUT, FADE, SLIDE_LEFT, SLIDE_RIGHT }
