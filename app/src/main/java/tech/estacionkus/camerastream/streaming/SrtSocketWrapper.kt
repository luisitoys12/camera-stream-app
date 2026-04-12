package tech.estacionkus.camerastream.streaming

import java.net.InetSocketAddress

/**
 * Abstraction over the real SRT socket (srtdroid JNI).
 * Allows pure JVM unit tests without loading the native library.
 *
 * Production uses [real()]. Tests use [noop()].
 */
interface SrtSocketWrapper {

    data class Stats(val rttMs: Int, val lostPkts: Int, val bandwidth: Long = 0)

    fun startup()
    fun configure(config: SrtCallerManager.SrtConfig)
    fun connect(address: InetSocketAddress)
    fun send(data: ByteArray, offset: Int = 0, len: Int = data.size): Int
    fun recv(buf: ByteArray): Int
    fun readStats(): Stats?
    fun close()
    fun cleanup()
    fun log(message: String)

    companion object {
        /** Real implementation with srtdroid — only loads on device/emulator. */
        fun real(): SrtSocketWrapper = RealSrtSocketWrapper()

        /** Empty implementation for JVM unit tests (no JNI loading). */
        fun noop(): SrtSocketWrapper = object : SrtSocketWrapper {
            override fun startup() {}
            override fun configure(config: SrtCallerManager.SrtConfig) {}
            override fun connect(address: InetSocketAddress) {}
            override fun send(data: ByteArray, offset: Int, len: Int): Int = len
            override fun recv(buf: ByteArray): Int = -1
            override fun readStats(): Stats? = null
            override fun close() {}
            override fun cleanup() {}
            override fun log(message: String) {}
        }
    }
}
