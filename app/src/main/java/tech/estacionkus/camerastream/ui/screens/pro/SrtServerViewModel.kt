package tech.estacionkus.camerastream.ui.screens.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.streaming.CloudflaredManager
import tech.estacionkus.camerastream.streaming.SrtServerManager
import tech.estacionkus.camerastream.streaming.TunnelStatus
import javax.inject.Inject

data class SrtServerUiState(
    val serverRunning: Boolean = false,
    val tunnelRunning: Boolean = false,
    val tunnelUrl: String? = null,
    val srtUrl: String? = null,
    val localSrtUrl: String? = null,
    val tunnelStatus: TunnelStatus = TunnelStatus.IDLE,
    val tunnelStatusMessage: String = "",
    val downloadProgress: Float = 0f,
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
            combine(
                srtServer.isRunning, srtServer.clientCount, srtServer.incomingBitrateKbps,
                cloudflared.isRunning, cloudflared.tunnelUrl
            ) { running, clients, bitrate, tRunning, tUrl ->
                _ui.value = _ui.value.copy(
                    serverRunning = running, clientCount = clients,
                    incomingBitrateKbps = bitrate, tunnelRunning = tRunning, tunnelUrl = tUrl,
                    localSrtUrl = if (running) "srt://${getLocalIp()}:${srtServer.port}" else null
                )
            }.collect()
        }
        viewModelScope.launch {
            cloudflared.status.collect { _ui.value = _ui.value.copy(tunnelStatus = it) }
        }
        viewModelScope.launch {
            cloudflared.statusMessage.collect { _ui.value = _ui.value.copy(tunnelStatusMessage = it) }
        }
        viewModelScope.launch {
            cloudflared.srtUrl.collect { _ui.value = _ui.value.copy(srtUrl = it) }
        }
        viewModelScope.launch {
            cloudflared.downloadProgress.collect { _ui.value = _ui.value.copy(downloadProgress = it) }
        }
    }

    fun startServer() {
        srtServer.start()
    }

    fun stopServer() {
        srtServer.stop()
        cloudflared.stopTunnel()
    }

    fun startTunnel() = cloudflared.startTunnel(srtServer.port)

    fun startAutoSetup() = cloudflared.startWithSrt(srtServer)

    fun stopTunnel() = cloudflared.stopTunnel()

    fun connectToRelay(url: String) {
        // Placeholder for SRT caller connection to relay
    }

    private fun getLocalIp(): String {
        return try {
            java.net.NetworkInterface.getNetworkInterfaces()?.toList()
                ?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull { !it.isLoopbackAddress && it is java.net.Inet4Address }
                ?.hostAddress ?: "192.168.x.x"
        } catch (_: Exception) {
            "192.168.x.x"
        }
    }
}
