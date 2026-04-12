package tech.estacionkus.camerastream.streaming

import android.util.Log
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.models.SrtSocket
import java.net.InetSocketAddress

internal class RealSrtSocketWrapper : SrtSocketWrapper {
    private val TAG = "SrtCallerManager"
    private var socket: SrtSocket? = null

    override fun startup(): Unit { Srt.startUp() }

    override fun configure(config: SrtCallerManager.SrtConfig) {
        socket = SrtSocket().apply {
            setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
            setSockFlag(SockOpt.RCVSYN, false)
            if (config.latencyMs > 0) setSockFlag(SockOpt.LATENCY, config.latencyMs)
            if (config.streamId.isNotBlank()) setSockFlag(SockOpt.STREAMID, config.streamId)
            if (config.passphrase.isNotBlank()) {
                setSockFlag(SockOpt.PBKEYLEN, config.pbkeylen.coerceAtLeast(16))
                setSockFlag(SockOpt.PASSPHRASE, config.passphrase)
            }
            if (config.maxBandwidthKbps > 0)
                setSockFlag(SockOpt.MAXBW, config.maxBandwidthKbps * 1000L)
        }
    }

    override fun connect(address: InetSocketAddress) {
        socket!!.connect(address)
        Log.i(TAG, "SRT connected to ${address.hostName}:${address.port}")
    }

    override fun recv(buf: ByteArray): Int = socket?.recv(buf) ?: -1

    override fun readStats(): SrtSocketWrapper.Stats? {
        return try {
            socket?.bistats(clear = true, instantaneous = false)?.let {
                SrtSocketWrapper.Stats(rttMs = it.msRTT.toInt(), lostPkts = it.pktRcvLoss)
            }
        } catch (_: Exception) { null }
    }

    override fun close() {
        try { socket?.close() } catch (_: Exception) {}
        socket = null
    }

    override fun cleanup(): Unit {
        try { Srt.cleanUp() } catch (_: Exception) {}
    }

    override fun log(message: String): Unit { Log.e(TAG, message) }
}
