package tech.estacionkus.camerastream.data.overlay

import android.graphics.*
import android.media.ImageReader
import android.view.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

enum class OverlayType {
    IMAGE, GIF, VIDEO_NATIVE, LOWER_THIRD, LOGO,
    COUNTDOWN, SCORE, BROWSER_SOURCE, POLL, ALERT
}

data class Overlay(
    val id: String,
    val type: OverlayType,
    val source: String, // URI or text
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0.3f,
    val height: Float = 0.1f,
    val alpha: Float = 1f,
    val animateIn: Boolean = true,
    val autoHideMs: Long? = null,
    // Lower third specific
    val title: String? = null,
    val subtitle: String? = null,
    // Score overlay specific
    val scoreHome: Int? = null,
    val scoreAway: Int? = null,
    val teamHome: String? = null,
    val teamAway: String? = null,
    // Countdown specific
    val countdownSeconds: Int? = null
)

/**
 * Renderiza overlays directamente sobre Canvas con Android 2D API.
 * Sin ExoPlayer. Videos se decodifican via MediaCodec + SurfaceTexture.
 */
@Singleton
class NativeOverlayRenderer @Inject constructor() {
    private val _overlays = MutableStateFlow<List<Overlay>>(emptyList())
    val overlays: StateFlow<List<Overlay>> = _overlays.asStateFlow()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
    }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val activeTimers = mutableMapOf<String, Job>()

    fun addOverlay(overlay: Overlay) {
        _overlays.value = _overlays.value + overlay
        overlay.autoHideMs?.let { ms ->
            activeTimers[overlay.id] = scope.launch {
                delay(ms)
                removeOverlay(overlay.id)
            }
        }
    }

    fun removeOverlay(id: String) {
        activeTimers[id]?.cancel()
        activeTimers.remove(id)
        _overlays.value = _overlays.value.filter { it.id != id }
    }

    fun clearAll() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
        _overlays.value = emptyList()
    }

    fun updateScore(overlayId: String, home: Int, away: Int) {
        _overlays.value = _overlays.value.map { o ->
            if (o.id == overlayId) o.copy(scoreHome = home, scoreAway = away) else o
        }
    }

    fun updateLowerThird(overlayId: String, title: String, subtitle: String) {
        _overlays.value = _overlays.value.map { o ->
            if (o.id == overlayId) o.copy(title = title, subtitle = subtitle) else o
        }
    }

    /**
     * Render all active overlays onto a Canvas.
     * Called each frame from the Compose overlay layer or MediaCodec surface.
     */
    fun renderFrame(canvas: Canvas, screenW: Int, screenH: Int) {
        _overlays.value.forEach { overlay ->
            val left   = overlay.x * screenW
            val top    = overlay.y * screenH
            val right  = left + overlay.width * screenW
            val bottom = top + overlay.height * screenH
            paint.alpha = (overlay.alpha * 255).toInt()

            when (overlay.type) {
                OverlayType.LOWER_THIRD -> renderLowerThird(canvas, overlay, left, top, right, bottom, screenW, screenH)
                OverlayType.SCORE       -> renderScore(canvas, overlay, screenW, screenH)
                OverlayType.COUNTDOWN   -> renderCountdown(canvas, overlay, screenW, screenH)
                OverlayType.LOGO        -> { /* Bitmap drawn by Compose layer */ }
                OverlayType.IMAGE       -> { /* Bitmap drawn by Compose layer */ }
                else                    -> { }
            }
        }
    }

    private fun renderLowerThird(canvas: Canvas, o: Overlay, l: Float, t: Float, r: Float, b: Float, sw: Int, sh: Int) {
        // Background bar
        paint.color = Color.argb(200, 20, 20, 20)
        canvas.drawRoundRect(l, b - sh * 0.12f, r, b, 12f, 12f, paint)
        // Red accent line
        paint.color = Color.rgb(220, 30, 30)
        canvas.drawRect(l, b - sh * 0.12f, l + 6f, b, paint)
        // Title
        textPaint.textSize = sh * 0.04f
        textPaint.color = Color.WHITE
        o.title?.let { canvas.drawText(it, l + 20f, b - sh * 0.07f, textPaint) }
        // Subtitle
        textPaint.textSize = sh * 0.025f
        textPaint.color = Color.LTGRAY
        o.subtitle?.let { canvas.drawText(it, l + 20f, b - sh * 0.03f, textPaint) }
    }

    private fun renderScore(canvas: Canvas, o: Overlay, sw: Int, sh: Int) {
        val cx = sw / 2f
        val cy = sh * 0.08f
        val w  = sw * 0.35f
        val h  = sh * 0.07f
        paint.color = Color.argb(220, 0, 0, 0)
        canvas.drawRoundRect(cx - w/2, cy - h/2, cx + w/2, cy + h/2, 16f, 16f, paint)
        textPaint.textSize = sh * 0.035f
        textPaint.color = Color.WHITE
        val scoreText = "${o.teamHome ?: "HOME"} ${o.scoreHome ?: 0} - ${o.scoreAway ?: 0} ${o.teamAway ?: "AWAY"}"
        val tw = textPaint.measureText(scoreText)
        canvas.drawText(scoreText, cx - tw/2, cy + textPaint.textSize/3, textPaint)
    }

    private fun renderCountdown(canvas: Canvas, o: Overlay, sw: Int, sh: Int) {
        val secs = o.countdownSeconds ?: return
        textPaint.textSize = sh * 0.12f
        textPaint.color = if (secs <= 10) Color.RED else Color.WHITE
        val text = "%02d:%02d".format(secs / 60, secs % 60)
        val tw = textPaint.measureText(text)
        canvas.drawText(text, (sw - tw) / 2f, sh * 0.5f, textPaint)
    }
}
