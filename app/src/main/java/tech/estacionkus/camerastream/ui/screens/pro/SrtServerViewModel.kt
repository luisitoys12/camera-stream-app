package tech.estacionkus.camerastream.ui.screens.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.streaming.CloudflaredManager
import tech.estacionkus.camerastream.streaming.SrtServerManager
import javax.inject.Inject

data class SrtServerUiState(
    val serverRunning: Boolean = false,
    val tunnelRunning: Boolean = false,
    val tunnelUrl: String? = null,
    val port: Int = 9999,
    val clientCount: Int = 0,
    val incomingBitrateKbps: Int = 0
)

@HiltViewModel
class SrtServerViewModel @Inject constructor(
    private val srtServer: SrtServerManager,
    private val cloudflared: CloudflaredManager
) : ViewModel() {
    private val _ui = MutableStateFlow(SrtServerUiState(port = srtServer.port))
    val uiState: StateFlow<SrtServerUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            combine(srtServer.isRunning, srtServer.clientCount, srtServer.incomingBitrateKbps, cloudflared.isRunning, cloudflared.tunnelUrl)
            { running, clients, bitrate, tRunning, tUrl ->
                _ui.value = _ui.value.copy(serverRunning = running, clientCount = clients, incomingBitrateKbps = bitrate, tunnelRunning = tRunning, tunnelUrl = tUrl)
            }.collect()
        }
    }

    fun startServer() = srtServer.start()
    fun stopServer() { srtServer.stop(); cloudflared.stopTunnel() }
    fun startTunnel() = cloudflared.startTunnel(srtServer.port)
    fun stopTunnel() = cloudflared.stopTunnel()
}
