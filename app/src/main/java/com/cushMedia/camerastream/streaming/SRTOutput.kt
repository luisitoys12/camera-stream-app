package com.cushMedia.camerastream.streaming

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SRTOutput — wrapper para conexión SRT.
 * Requiere libsrt compilada vía NDK.
 * Ver: app/src/main/cpp/CMakeLists.txt
 *
 * Para compilar libsrt:
 * 1. Clonar https://github.com/Haivision/srt
 * 2. Compilar con Android NDK toolchain
 * 3. Poner .so en app/src/main/jniLibs/{abi}/
 */
class SRTOutput(
    private val host: String,
    private val port: Int,
    private val streamId: String,
    private val latencyMs: Int = 200
) : StreamOutput {

    override val name = "SRT[$host:$port]"
    private val TAG = "SRTOutput"

    override suspend fun connect() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Conectando SRT a $host:$port (streamId=$streamId, latency=${latencyMs}ms)")
        // TODO: Llamar a JNI: SrtNative.connect(host, port, streamId, latencyMs)
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Desconectando SRT")
        // TODO: SrtNative.disconnect()
    }

    override suspend fun sendFrame(data: ByteArray) = withContext(Dispatchers.IO) {
        // TODO: SrtNative.sendPacket(data)
    }

    // Futuro: companion object con native methods
    // companion object {
    //     init { System.loadLibrary("srt-jni") }
    //     external fun nativeConnect(host: String, port: Int, streamId: String, latency: Int): Long
    //     external fun nativeSend(socketHandle: Long, data: ByteArray): Int
    //     external fun nativeDisconnect(socketHandle: Long)
    // }
}
