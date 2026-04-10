package tech.estacionkus.camerastream.streaming

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.ossrs.yasea.SrsFlvMuxer
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

// SRT server using srtdroid-core JNI wrapper
@Singleton
class SrtServerManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "SrtServerManager"

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _clientCount = MutableStateFlow(0)
    val clientCount: StateFlow<Int> = _clientCount.asStateFlow()

    private val _incomingBitrateKbps = MutableStateFlow(0)
    val incomingBitrateKbps: StateFlow<Int> = _incomingBitrateKbps.asStateFlow()

    var onDataReceived: ((ByteArray, Int) -> Unit)? = null

    // Port for local SRT listener
    val port = 9999

    private var serverJob: Job? = null

    fun start() {
        if (_isRunning.value) return
        serverJob = scope.launch {
            try {
                // Initialize SRT socket via srtdroid
                val srtLib = io.github.thibaultbee.srtdroid.core.Srt
                srtLib.startUp()
                val serverSocket = io.github.thibaultbee.srtdroid.core.models.SrtSocket()
                serverSocket.setSockFlag(
                    io.github.thibaultbee.srtdroid.core.enums.SockOpt.RCVSYN,
                    true
                )
                serverSocket.bind(
                    java.net.InetSocketAddress("0.0.0.0", port)
                )
                serverSocket.listen(5)
                _isRunning.value = true
                Log.d(TAG, "SRT server listening on port $port")

                while (isActive) {
                    val (clientSocket, _) = serverSocket.accept()
                    _clientCount.value++
                    launch { handleClient(clientSocket) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SRT server error: ${e.message}")
                _isRunning.value = false
            }
        }
    }

    private suspend fun handleClient(
        socket: io.github.thibaultbee.srtdroid.core.models.SrtSocket
    ) {
        val buf = ByteArray(1316) // MPEG-TS packet size * 7
        var bytesTotal = 0L
        var lastStatMs = System.currentTimeMillis()
        try {
            while (true) {
                val n = socket.recv(buf)
                if (n <= 0) break
                bytesTotal += n
                onDataReceived?.invoke(buf, n)
                // Update incoming bitrate every second
                val now = System.currentTimeMillis()
                if (now - lastStatMs >= 1000) {
                    _incomingBitrateKbps.value = ((bytesTotal * 8) / 1000).toInt()
                    bytesTotal = 0
                    lastStatMs = now
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Client disconnected: ${e.message}")
        } finally {
            socket.close()
            _clientCount.value = (_clientCount.value - 1).coerceAtLeast(0)
        }
    }

    fun stop() {
        serverJob?.cancel()
        _isRunning.value = false
        _clientCount.value = 0
        _incomingBitrateKbps.value = 0
        io.github.thibaultbee.srtdroid.core.Srt.cleanUp()
    }
}
