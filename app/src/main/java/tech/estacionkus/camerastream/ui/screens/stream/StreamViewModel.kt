package tech.estacionkus.camerastream.ui.screens.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import tech.estacionkus.camerastream.streaming.ChatManager
import tech.estacionkus.camerastream.streaming.RecordingManager
import tech.estacionkus.camerastream.streaming.RtmpStreamManager
import tech.estacionkus.camerastream.streaming.StreamState
import javax.inject.Inject

data class StreamUiState(
    val streamState: StreamState = StreamState.IDLE,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = false,
    val isRecording: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val bitrateKbps: Int = 2500,
    val overlayUri: String = "",
    val overlayEnabled: Boolean = false,
    val chatVisible: Boolean = false,
    val features: PlanFeatures = PlanFeatures.FREE,
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val streamManager: RtmpStreamManager,
    private val recordingManager: RecordingManager,
    private val chatManager: ChatManager,
    private val settingsRepository: SettingsRepository,
    private val licenseRepository: LicenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamUiState())
    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()

    val chatMessages = chatManager.messages
    val chatConnected = chatManager.isConnected
    val streamStats = streamManager.stats

    init {
        viewModelScope.launch {
            // Load license
            val license = licenseRepository.verifyLicense()
            val features = PlanFeatures.fromPlanId(license.plan?.id)

            // Load saved settings
            combine(
                settingsRepository.rtmpUrl,
                settingsRepository.streamKey,
                settingsRepository.bitrate,
                settingsRepository.overlayUri,
                settingsRepository.overlayEnabled
            ) { url, key, bitrate, overlayUri, overlayEnabled ->
                _uiState.value = _uiState.value.copy(
                    rtmpUrl = url,
                    streamKey = key,
                    bitrateKbps = bitrate,
                    overlayUri = overlayUri,
                    overlayEnabled = overlayEnabled,
                    features = features
                )
            }.collect()
        }

        // Sync stream state
        viewModelScope.launch {
            streamManager.state.collect { state ->
                _uiState.value = _uiState.value.copy(streamState = state)
            }
        }

        // Sync elapsed timer
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.streamState == StreamState.LIVE) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }

        // Sync recording
        viewModelScope.launch {
            recordingManager.isRecording.collect {
                _uiState.value = _uiState.value.copy(isRecording = it)
            }
        }
    }

    fun startStream() {
        val state = _uiState.value
        if (state.rtmpUrl.isBlank() || state.streamKey.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Configura RTMP URL y Stream Key primero")
            return
        }
        _uiState.value = state.copy(elapsedSeconds = 0L, errorMessage = null)
        streamManager.startStream(state.rtmpUrl, state.streamKey)
    }

    fun stopStream() {
        streamManager.stopStream()
        _uiState.value = _uiState.value.copy(elapsedSeconds = 0L)
        if (_uiState.value.isRecording) recordingManager.stopRecording()
    }

    fun flipCamera() {
        streamManager.flipCamera()
        _uiState.value = _uiState.value.copy(isFrontCamera = !_uiState.value.isFrontCamera)
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        streamManager.setMute(muted)
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    fun setBitrate(kbps: Int) {
        streamManager.setBitrate(kbps)
        _uiState.value = _uiState.value.copy(bitrateKbps = kbps)
        viewModelScope.launch { settingsRepository.setBitrate(kbps) }
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) recordingManager.stopRecording()
        else recordingManager.startRecording()
    }

    fun toggleOverlay() {
        val enabled = !_uiState.value.overlayEnabled
        _uiState.value = _uiState.value.copy(overlayEnabled = enabled)
        viewModelScope.launch { settingsRepository.setOverlayEnabled(enabled) }
    }

    fun setOverlayUri(uri: String) {
        _uiState.value = _uiState.value.copy(overlayUri = uri)
        viewModelScope.launch { settingsRepository.setOverlayUri(uri) }
    }

    fun toggleChat() {
        _uiState.value = _uiState.value.copy(chatVisible = !_uiState.value.chatVisible)
    }

    fun connectChat(platform: String, channel: String) {
        viewModelScope.launch {
            settingsRepository.setChatPlatform(platform)
            settingsRepository.setChatChannel(channel)
            chatManager.connectTwitch(channel)
        }
    }

    fun dismissError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    override fun onCleared() {
        super.onCleared()
        chatManager.disconnect()
    }
}
