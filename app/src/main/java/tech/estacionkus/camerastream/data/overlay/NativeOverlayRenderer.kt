package tech.estacionkus.camerastream.data.overlay

import android.graphics.*
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
    val countdownSeconds: Int? = null,
    // Ticker specific
    val tickerText: String? = null,
    val tickerSpeed: Float = 2f,
    // Chat overlay specific
    val chatMessages: List<String> = emptyList(),
    // Alert specific
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    // Watermark specific
    val watermarkBitmap: Bitmap? = null
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

    // Ticker scroll positions keyed by overlay ID
    private val tickerOffsets = mutableMapOf<String, Float>()

    // Cached bitmaps for image/logo overlays
    private val bitmapCache = mutableMapOf<String, Bitmap>()

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
        tickerOffsets.remove(id)
        bitmapCache.remove(id)
        _overlays.value = _overlays.value.filter { it.id != id }
    }

    fun clearAll() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
        tickerOffsets.clear()
        bitmapCache.clear()
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

    fun updateTickerText(overlayId: String, text: String) {
        _overlays.value = _overlays.value.map { o ->
            if (o.id == overlayId) o.copy(tickerText = text) else o
        }
    }

    fun updateChatMessages(overlayId: String, messages: List<String>) {
        _overlays.value = _overlays.value.map { o ->
            if (o.id == overlayId) o.copy(chatMessages = messages) else o
        }
    }

    fun cacheBitmap(overlayId: String, bitmap: Bitmap) {
        bitmapCache[overlayId] = bitmap
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
                OverlayType.LOGO        -> renderImage(canvas, overlay, left, top, right, bottom)
                OverlayType.IMAGE       -> renderImage(canvas, overlay, left, top, right, bottom)
                OverlayType.GIF         -> renderImage(canvas, overlay, left, top, right, bottom)
                OverlayType.ALERT       -> renderAlert(canvas, overlay, left, top, right, bottom, screenW, screenH)
                OverlayType.POLL        -> renderPoll(canvas, overlay, left, top, right, bottom, screenW, screenH)
                OverlayType.BROWSER_SOURCE -> renderBrowserSource(canvas, overlay, left, top, right, bottom, screenW, screenH)
                OverlayType.VIDEO_NATIVE -> renderVideoPlaceholder(canvas, overlay, left, top, right, bottom)
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
        textPaint.typeface = Typeface.DEFAULT_BOLD
        o.title?.let { canvas.drawText(it, l + 20f, b - sh * 0.07f, textPaint) }
        // Subtitle
        textPaint.textSize = sh * 0.025f
        textPaint.color = Color.LTGRAY
        textPaint.typeface = Typeface.DEFAULT
        o.subtitle?.let { canvas.drawText(it, l + 20f, b - sh * 0.03f, textPaint) }
    }

    private fun renderScore(canvas: Canvas, o: Overlay, sw: Int, sh: Int) {
        val cx = sw / 2f
        val cy = sh * 0.08f
        val w  = sw * 0.35f
        val h  = sh * 0.07f
        paint.color = Color.argb(220, 0, 0, 0)
        canvas.drawRoundRect(cx - w/2, cy - h/2, cx + w/2, cy + h/2, 16f, 16f, paint)

        // Team home background
        paint.color = Color.argb(180, 30, 136, 229)
        canvas.drawRoundRect(cx - w/2, cy - h/2, cx - 10f, cy + h/2, 16f, 0f, paint)
        canvas.drawRect(cx - w/2 + 16f, cy - h/2, cx - 10f, cy + h/2, paint)

        // Team away background
        paint.color = Color.argb(180, 229, 57, 53)
        canvas.drawRoundRect(cx + 10f, cy - h/2, cx + w/2, cy + h/2, 0f, 16f, paint)
        canvas.drawRect(cx + 10f, cy - h/2, cx + w/2 - 16f, cy + h/2, paint)

        // Score divider
        paint.color = Color.argb(255, 40, 40, 40)
        canvas.drawRect(cx - 10f, cy - h/2, cx + 10f, cy + h/2, paint)

        textPaint.textSize = sh * 0.035f
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.DEFAULT_BOLD

        // Home team name
        val homeName = o.teamHome ?: "HOME"
        textPaint.textSize = sh * 0.02f
        canvas.drawText(homeName, cx - w/2 + 12f, cy - 4f, textPaint)

        // Away team name
        val awayName = o.teamAway ?: "AWAY"
        val awayNameWidth = textPaint.measureText(awayName)
        canvas.drawText(awayName, cx + w/2 - awayNameWidth - 12f, cy - 4f, textPaint)

        // Scores
        textPaint.textSize = sh * 0.04f
        val homeScore = "${o.scoreHome ?: 0}"
        val homeScoreW = textPaint.measureText(homeScore)
        canvas.drawText(homeScore, cx - homeScoreW - 14f, cy + textPaint.textSize * 0.35f, textPaint)

        val awayScore = "${o.scoreAway ?: 0}"
        canvas.drawText(awayScore, cx + 14f, cy + textPaint.textSize * 0.35f, textPaint)

        // Dash in center
        textPaint.textSize = sh * 0.03f
        val dash = "-"
        val dashW = textPaint.measureText(dash)
        canvas.drawText(dash, cx - dashW / 2f, cy + textPaint.textSize * 0.3f, textPaint)
    }

    private fun renderCountdown(canvas: Canvas, o: Overlay, sw: Int, sh: Int) {
        val secs = o.countdownSeconds ?: return
        textPaint.textSize = sh * 0.12f
        textPaint.color = if (secs <= 10) Color.RED else Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val text = "%02d:%02d".format(secs / 60, secs % 60)
        val tw = textPaint.measureText(text)

        // Background circle
        paint.color = Color.argb(180, 0, 0, 0)
        val cx = sw / 2f
        val cy = sh * 0.5f
        canvas.drawCircle(cx, cy, tw * 0.6f, paint)

        // Ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = if (secs <= 10) Color.RED else Color.WHITE
        canvas.drawCircle(cx, cy, tw * 0.6f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText(text, (sw - tw) / 2f, sh * 0.5f + textPaint.textSize * 0.35f, textPaint)
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    private fun renderImage(canvas: Canvas, overlay: Overlay, l: Float, t: Float, r: Float, b: Float) {
        val bitmap = overlay.watermarkBitmap ?: bitmapCache[overlay.id]
        if (bitmap != null && !bitmap.isRecycled) {
            paint.alpha = (overlay.alpha * 255).toInt()
            val destRect = RectF(l, t, r, b)
            canvas.drawBitmap(bitmap, null, destRect, paint)
        } else {
            // Fallback: draw a semi-transparent placeholder box with image icon indicator
            paint.color = Color.argb((overlay.alpha * 80).toInt(), 100, 100, 100)
            canvas.drawRoundRect(l, t, r, b, 8f, 8f, paint)
            textPaint.textSize = (b - t) * 0.3f
            textPaint.color = Color.argb((overlay.alpha * 150).toInt(), 255, 255, 255)
            val label = if (overlay.source.isNotBlank()) overlay.source.substringAfterLast('/') else overlay.type.name
            val tw = textPaint.measureText(label)
            canvas.drawText(label, (l + r - tw) / 2f, (t + b) / 2f + textPaint.textSize * 0.3f, textPaint)
        }
    }

    private fun renderAlert(canvas: Canvas, overlay: Overlay, l: Float, t: Float, r: Float, b: Float, sw: Int, sh: Int) {
        val alertH = sh * 0.1f
        val alertW = sw * 0.6f
        val alertL = (sw - alertW) / 2f
        val alertT = sh * 0.15f

        // Glow effect
        paint.color = Color.argb(40, 255, 215, 0)
        canvas.drawRoundRect(alertL - 4f, alertT - 4f, alertL + alertW + 4f, alertT + alertH + 4f, 20f, 20f, paint)

        // Background
        paint.color = Color.argb((overlay.alpha * 230).toInt(), 25, 25, 35)
        canvas.drawRoundRect(alertL, alertT, alertL + alertW, alertT + alertH, 16f, 16f, paint)

        // Gold accent bar at top
        paint.color = Color.argb((overlay.alpha * 255).toInt(), 255, 215, 0)
        canvas.drawRoundRect(alertL, alertT, alertL + alertW, alertT + 4f, 16f, 16f, paint)

        // Alert title
        textPaint.textSize = sh * 0.025f
        textPaint.color = Color.argb((overlay.alpha * 255).toInt(), 255, 215, 0)
        textPaint.typeface = Typeface.DEFAULT_BOLD
        val title = overlay.alertTitle ?: "NEW FOLLOWER"
        canvas.drawText(title, alertL + 16f, alertT + alertH * 0.4f, textPaint)

        // Alert message
        textPaint.textSize = sh * 0.022f
        textPaint.color = Color.argb((overlay.alpha * 255).toInt(), 220, 220, 220)
        textPaint.typeface = Typeface.DEFAULT
        val message = overlay.alertMessage ?: overlay.source
        canvas.drawText(message, alertL + 16f, alertT + alertH * 0.72f, textPaint)
    }

    private fun renderPoll(canvas: Canvas, overlay: Overlay, l: Float, t: Float, r: Float, b: Float, sw: Int, sh: Int) {
        // Poll overlay - renders a simple poll display
        val boxW = r - l
        val boxH = b - t

        // Background
        paint.color = Color.argb((overlay.alpha * 210).toInt(), 20, 20, 30)
        canvas.drawRoundRect(l, t, r, b, 12f, 12f, paint)

        // Title bar
        paint.color = Color.argb((overlay.alpha * 255).toInt(), 100, 50, 200)
        canvas.drawRoundRect(l, t, r, t + boxH * 0.25f, 12f, 0f, paint)
        canvas.drawRect(l, t + boxH * 0.15f, r, t + boxH * 0.25f, paint)

        textPaint.textSize = boxH * 0.15f
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.DEFAULT_BOLD
        val title = overlay.source.ifBlank { "POLL" }
        canvas.drawText(title, l + 10f, t + boxH * 0.19f, textPaint)

        // Poll options placeholder bars
        textPaint.textSize = boxH * 0.1f
        textPaint.typeface = Typeface.DEFAULT
        val barY = t + boxH * 0.35f
        for (i in 0..1) {
            val yOff = barY + i * boxH * 0.28f
            paint.color = Color.argb((overlay.alpha * 100).toInt(), 100, 50, 200)
            canvas.drawRoundRect(l + 10f, yOff, r - 10f, yOff + boxH * 0.2f, 6f, 6f, paint)
            paint.color = Color.argb((overlay.alpha * 200).toInt(), 100, 50, 200)
            val fillW = (r - l - 20f) * if (i == 0) 0.6f else 0.4f
            canvas.drawRoundRect(l + 10f, yOff, l + 10f + fillW, yOff + boxH * 0.2f, 6f, 6f, paint)
            textPaint.color = Color.WHITE
            canvas.drawText("Option ${i + 1}", l + 16f, yOff + boxH * 0.14f, textPaint)
        }
    }

    private fun renderBrowserSource(canvas: Canvas, overlay: Overlay, l: Float, t: Float, r: Float, b: Float, sw: Int, sh: Int) {
        // Browser source - render a stylized browser frame
        paint.color = Color.argb((overlay.alpha * 220).toInt(), 30, 30, 40)
        canvas.drawRoundRect(l, t, r, b, 10f, 10f, paint)

        // Title bar
        paint.color = Color.argb((overlay.alpha * 255).toInt(), 45, 45, 55)
        val titleH = (b - t) * 0.15f
        canvas.drawRoundRect(l, t, r, t + titleH, 10f, 0f, paint)
        canvas.drawRect(l, t + titleH * 0.5f, r, t + titleH, paint)

        // URL text
        textPaint.textSize = titleH * 0.6f
        textPaint.color = Color.argb((overlay.alpha * 180).toInt(), 180, 180, 180)
        textPaint.typeface = Typeface.MONOSPACE
        val url = overlay.source.ifBlank { "about:blank" }
        canvas.drawText(url.take(40), l + 8f, t + titleH * 0.75f, textPaint)

        // Content area hint
        textPaint.textSize = (b - t) * 0.08f
        textPaint.color = Color.argb((overlay.alpha * 120).toInt(), 150, 150, 150)
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Web content", (l + r) / 2f - 30f, (t + titleH + b) / 2f, textPaint)
    }

    private fun renderVideoPlaceholder(canvas: Canvas, overlay: Overlay, l: Float, t: Float, r: Float, b: Float) {
        // Native video overlay — when not composited via SurfaceTexture, show a frame
        paint.color = Color.argb((overlay.alpha * 200).toInt(), 10, 10, 10)
        canvas.drawRoundRect(l, t, r, b, 8f, 8f, paint)

        // Play button triangle
        val cx = (l + r) / 2f
        val cy = (t + b) / 2f
        val triSize = (b - t) * 0.2f
        paint.color = Color.argb((overlay.alpha * 180).toInt(), 255, 255, 255)
        val path = android.graphics.Path()
        path.moveTo(cx - triSize * 0.4f, cy - triSize * 0.5f)
        path.lineTo(cx + triSize * 0.6f, cy)
        path.lineTo(cx - triSize * 0.4f, cy + triSize * 0.5f)
        path.close()
        canvas.drawPath(path, paint)
    }
}
