package tech.estacionkus.camerastream.data.streaming

import android.content.Context
import io.github.thibaultbee.srtdroid.core.Srt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class SrtServerState(
    val isRunning: Boolean = false,
    val port: Int = 9999,
    val connectedClients: Int = 0,
    val localUrl: String = "",
    val tunnelUrl: String? = null,
    val bytesReceived: Long = 0L
)

@Singleton
class SrtServerManager @Inject constructor(
    private val context: Context,
    private val cloudflaredManager: CloudflaredManager
) {
    private val _state = MutableStateFlow(SrtServerState())
    val state: StateFlow<SrtServerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null
    private val SRT_PORT = 9999

    fun startServer(withTunnel: Boolean = true) {
        serverJob = scope.launch {
            try {
                Srt.startUp()
                _state.value = SrtServerState(
                    isRunning = true,
                    port = SRT_PORT,
                    localUrl = "srt://127.0.0.1:$SRT_PORT"
                )
                if (withTunnel) {
                    val tunnelUrl = cloudflaredManager.startTunnel(SRT_PORT)
                    _state.value = _state.value.copy(tunnelUrl = tunnelUrl)
                }
                acceptConnections()
            } catch (e: Exception) {
                _state.value = SrtServerState(isRunning = false)
            }
        }
    }

    fun stopServer() {
        serverJob?.cancel()
        cloudflaredManager.stopTunnel()
        Srt.cleanUp()
        _state.value = SrtServerState()
    }

    private suspend fun acceptConnections() {
        // Accept SRT clients in loop — each connects as caller, server relays to stream engine
        while (true) {
            delay(500) // polling placeholder — real impl uses srtdroid ServerSocket
            // srtdroid ServerSocket.accept() blocks until client connects
            // On connection: pipe incoming H.264/AAC to NativeVideoEngine for re-streaming
        }
    }
}
