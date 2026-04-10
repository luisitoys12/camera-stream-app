package com.cushMedia.camerastream.streaming

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RTMPOutput — wrapper para conexión RTMP.
 * Compatible con: YouTube Live, Twitch, Facebook, TikTok, Kick.
 *
 * Opciones de librería RTMP:
 * - NodeMediaClient (com.github.NodeMedia:NodeMediaClient-android)
 * - librtmp-android
 * - FFmpeg con protocolo rtmp://
 */
class RTMPOutput(
    private val rtmpUrl: String,
    private val streamKey: String
) : StreamOutput {

    override val name = "RTMP[$rtmpUrl]"
    private val TAG = "RTMPOutput"
    private val fullUrl get() = "$rtmpUrl/$streamKey"

    override suspend fun connect() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Conectando RTMP: $fullUrl")
        // TODO: Inicializar cliente RTMP y conectar
        // Ejemplo con NodeMediaClient:
        // nodePublisher.start(fullUrl)
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Desconectando RTMP: $rtmpUrl")
        // TODO: nodePublisher.stop()
    }

    override suspend fun sendFrame(data: ByteArray) = withContext(Dispatchers.IO) {
        // TODO: nodePublisher.sendVideoData(data)
    }
}
