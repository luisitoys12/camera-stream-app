package com.cushMedia.camerastream.streaming

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * StreamManager coordina los outputs de streaming:
 * - SRT via libsrt (JNI nativo)
 * - RTMP via sockets para redes sociales
 *
 * TODO: Integrar libsrt nativa y librería RTMP (ej. NodeMediaClient o librtmp)
 */
class StreamManager {

    private val TAG = "StreamManager"
    private val activeOutputs = mutableListOf<StreamOutput>()

    suspend fun startStreaming() = withContext(Dispatchers.IO) {
        // TODO: Cargar configuración guardada y activar outputs
        Log.i(TAG, "startStreaming: iniciando ${activeOutputs.size} outputs")
        activeOutputs.forEach { output ->
            runCatching { output.connect() }
                .onSuccess { Log.i(TAG, "Conectado: ${output.name}") }
                .onFailure { Log.e(TAG, "Error conectando ${output.name}: ${it.message}") }
        }
    }

    suspend fun stopStreaming() = withContext(Dispatchers.IO) {
        Log.i(TAG, "stopStreaming: cerrando outputs")
        activeOutputs.forEach { output ->
            runCatching { output.disconnect() }
                .onFailure { Log.w(TAG, "Error cerrando ${output.name}: ${it.message}") }
        }
    }

    fun addOutput(output: StreamOutput) {
        activeOutputs.add(output)
        Log.i(TAG, "Output añadido: ${output.name}")
    }

    fun release() {
        activeOutputs.clear()
        Log.i(TAG, "StreamManager liberado")
    }
}

interface StreamOutput {
    val name: String
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendFrame(data: ByteArray)
}
