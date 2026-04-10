package tech.estacionkus.camerastream.streaming

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Presets optimized for different broadcast scenarios.
 * Based on CBR + low-latency SRT best practices for live sports/TV.
 */
@Singleton
class SportsModeManager @Inject constructor() {

    enum class BroadcastPreset(
        val label: String,
        val widthPx: Int,
        val heightPx: Int,
        val fps: Int,
        val bitrateKbps: Int,
        val audioBitrateKbps: Int,
        val keyframeIntervalSec: Int, // 1-2s recommended for live
        val srtLatencyMs: Int
    ) {
        /** Deporte en vivo - celular - LTE/5G */
        SPORTS_720P(
            "Deportes 720p (LTE)",
            1280, 720, 60, 4000, 128, 1, 200
        ),
        /** Deportes premium - WiFi o 5G */
        SPORTS_1080P(
            "Deportes 1080p (5G/WiFi)",
            1920, 1080, 60, 8000, 192, 1, 120
        ),
        /** Programa de TV / estudio */
        TV_SHOW(
            "Programa de TV 1080p",
            1920, 1080, 30, 6000, 192, 2, 120
        ),
        /** Noticias - ancho de banda bajo */
        NEWS_LOW_BW(
            "Noticias / bajo ancho de banda",
            1280, 720, 30, 2000, 96, 2, 300
        ),
        /** Conferencia / entrevistas */
        INTERVIEW(
            "Entrevista / conferencia",
            1280, 720, 30, 2500, 128, 2, 200
        ),
        /** Concierto - alta calidad audio */
        CONCERT(
            "Concierto / música",
            1920, 1080, 30, 5000, 320, 2, 150
        );

        val resolution get() = "${widthPx}x${heightPx}"
        val description get() = "$resolution @ ${fps}fps | ${bitrateKbps}kbps | SRT ${srtLatencyMs}ms"
    }

    var activePreset: BroadcastPreset = BroadcastPreset.SPORTS_720P
        private set

    fun apply(preset: BroadcastPreset) {
        activePreset = preset
    }

    /** Adaptive bitrate suggestion based on measured network conditions */
    fun suggestBitrate(availableBandwidthKbps: Int): Int {
        return when {
            availableBandwidthKbps > 12000 -> BroadcastPreset.SPORTS_1080P.bitrateKbps
            availableBandwidthKbps > 6000  -> BroadcastPreset.SPORTS_720P.bitrateKbps
            availableBandwidthKbps > 3000  -> BroadcastPreset.INTERVIEW.bitrateKbps
            else                           -> BroadcastPreset.NEWS_LOW_BW.bitrateKbps
        }
    }
}
