package tech.estacionkus.camerastream.streaming

import java.net.InetSocketAddress

/**
 * Abstracción sobre el socket SRT real (srtdroid JNI).
 * Permite unit tests JVM puros sin cargar la librería nativa.
 *
 * En producción usa [real()]. En tests usa [noop()].
 */
interface SrtSocketWrapper {

    data class Stats(val rttMs: Int, val lostPkts: Int)

    fun startup()
    fun configure(config: SrtCallerManager.SrtConfig)
    fun connect(address: InetSocketAddress)
    fun recv(buf: ByteArray): Int
    fun readStats(): Stats?
    fun close()
    fun cleanup()
    fun log(message: String)

    companion object {
        /** Implementación real con srtdroid — sólo se carga en dispositivo/emulador. */
        fun real(): SrtSocketWrapper = RealSrtSocketWrapper()

        /** Implementación vacía para unit tests JVM (no carga JNI). */
        fun noop(): SrtSocketWrapper = object : SrtSocketWrapper {
            override fun startup() {}
            override fun configure(config: SrtCallerManager.SrtConfig) {}
            override fun connect(address: InetSocketAddress) {}
            override fun recv(buf: ByteArray): Int = -1
            override fun readStats(): Stats? = null
            override fun close() {}
            override fun cleanup() {}
            override fun log(message: String) {}
        }
    }
}
