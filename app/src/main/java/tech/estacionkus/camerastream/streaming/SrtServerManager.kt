package tech.estacionkus.camerastream.streaming

import android.util.Log
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.models.SrtSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SrtServerManager @Inject constructor() {
    private val TAG = "SrtServerManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val port = 9999

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    private val _clientCount = MutableStateFlow(0)
    val clientCount: StateFlow<Int> = _clientCount.asStateFlow()
    private val _incomingBitrateKbps = MutableStateFlow(0)
    val incomingBitrateKbps: StateFlow<Int> = _incomingBitrateKbps.asStateFlow()

    var onDataReceived: ((ByteArray, Int) -> Unit)? = null
    private var serverJob: Job? = null

    fun start() {
        if (_isRunning.value) return
        serverJob = scope.launch {
            try {
                Srt.startUp()
                val server = SrtSocket()
                server.setSockFlag(SockOpt.RCVSYN, true)
                server.bind(InetSocketAddress("0.0.0.0", port))
                server.listen(5)
                _isRunning.value = true
                Log.d(TAG, "SRT server listening :$port")
                while (isActive) {
                    val result = server.accept()
                    val client = result.first
                    _clientCount.value++
                    launch { handleClient(client) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SRT error: ${e.message}")
                _isRunning.value = false
            }
        }
    }

    private suspend fun handleClient(socket: SrtSocket) {
        val buf = ByteArray(1316)
        var bytes = 0L
        var lastMs = System.currentTimeMillis()
        try {
            while (true) {
                val n = socket.recv(buf)
                if (n <= 0) break
                bytes += n
                onDataReceived?.invoke(buf, n)
                val now = System.currentTimeMillis()
                if (now - lastMs >= 1000) {
                    _incomingBitrateKbps.value = (bytes * 8 / 1000).toInt()
                    bytes = 0; lastMs = now
                }
            }
        } catch (e: Exception) { Log.w(TAG, "Client: ${e.message}") }
        finally {
            socket.close()
            _clientCount.value = (_clientCount.value - 1).coerceAtLeast(0)
        }
    }

    fun stop() {
        serverJob?.cancel()
        _isRunning.value = false
        _clientCount.value = 0
        _incomingBitrateKbps.value = 0
        try { Srt.cleanUp() } catch (_: Exception) {}
    }
}
