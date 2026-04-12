package tech.estacionkus.camerastream.ui.screens.stream

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import tech.estacionkus.camerastream.domain.model.StreamTarget
import tech.estacionkus.camerastream.service.StreamForegroundService
import tech.estacionkus.camerastream.streaming.*
import javax.inject.Inject

data class StreamUiState(
    val streamState: StreamState = StreamState.IDLE,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = false,
    val isRecording: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val bitrateKbps: Int = 2500,
    val fps: Int = 30,
    val resolution: String = "720p",
    val overlayUri: String = "",
    val overlayEnabled: Boolean = false,
    val chatVisible: Boolean = false,
    val features: PlanFeatures = PlanFeatures.FREE,
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val platform: String = "YouTube",
    val errorMessage: String? = null,
    // Multi-stream
    val isMultiStream: Boolean = false,
    val multiStreamTargets: List<StreamTarget> = emptyList(),
    val targetStatuses: Map<String, TargetStatus> = emptyMap(),
    // Disconnect protection
    val showBrb: Boolean = false,
    val brbCountdown: Int = 0,
    val networkType: String = "Unknown",
    // Health
    val healthGrade: String = "GOOD",
    val droppedFrames: Int = 0,
    // Scenes
    val activeSceneId: String = "main",
    val isTransitioning: Boolean = false
)

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val app: Application,
    private val streamManager: RtmpStreamManager,
    private val multiStreamManager: MultiStreamManager,
    private val recordingManager: RecordingManager,
    private val chatManager: ChatManager,
    private val settingsRepository: SettingsRepository,
    private val licenseRepository: LicenseRepository,
    private val featureGate: FeatureGate,
    private val disconnectProtection: DisconnectProtectionManager,
    private val healthMonitor: StreamHealthMonitor,
    private val sceneManager: SceneManager
) : ViewModel() {

    private val TAG = "StreamViewModel"

    private val _uiState = MutableStateFlow(StreamUiState())
    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()

    val chatMessages = chatManager.messages
    val chatConnected = chatManager.isConnected
    val streamStats = streamManager.stats

    // Expose scene data
    val scenes = sceneManager.scenes
    val activeSceneId = sceneManager.activeSceneId

    // Expose health data
    val healthGradeFlow = healthMonitor.healthGrade
    val bitrateHistory = healthMonitor.bitrateHistory
    val fpsHistory = healthMonitor.fpsHistory

    // Expose multi-stream statuses
    val multiStreamStatuses = multiStreamManager.statuses

    init {
        // Load license and settings
        viewModelScope.launch {
            try {
                val license = licenseRepository.verifyLicense()
                val features = PlanFeatures.fromPlanId(license.plan?.id)
                _uiState.update { it.copy(features = features) }
                featureGate.upgrade(features)
            } catch (e: Exception) {
                Log.w(TAG, "License check failed: ${e.message}")
            }
        }

        // Load saved settings
        viewModelScope.launch {
            combine(
                settingsRepository.rtmpUrl,
                settingsRepository.streamKey,
                settingsRepository.bitrate,
                settingsRepository.platform,
                settingsRepository.overlayUri,
                settingsRepository.overlayEnabled
            ) { values ->
                _uiState.update { state ->
                    state.copy(
                        rtmpUrl = values[0] as String,
                        streamKey = values[1] as String,
                        bitrateKbps = values[2] as Int,
                        platform = values[3] as String,
                        overlayUri = values[4] as String,
                        overlayEnabled = values[5] as Boolean
                    )
                }
            }.collect()
        }

        // Load multi-stream targets
        viewModelScope.launch {
            settingsRepository.streamTargets.collect { targets ->
                _uiState.update { it.copy(multiStreamTargets = targets) }
            }
        }

        // Sync stream state from manager
        viewModelScope.launch {
            streamManager.state.collect { state ->
                _uiState.update { it.copy(streamState = state) }
                when (state) {
                    StreamState.LIVE -> {
                        startForegroundService()
                        startHealthMonitoring()
                        startDisconnectProtection()
                    }
                    StreamState.IDLE -> {
                        stopForegroundService()
                        stopHealthMonitoring()
                        stopDisconnectProtection()
                    }
                    else -> {}
                }
            }
        }

        // Elapsed timer
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.streamState == StreamState.LIVE) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }

        // Sync recording state
        viewModelScope.launch {
            recordingManager.isRecording.collect { recording ->
                _uiState.update { it.copy(isRecording = recording) }
            }
        }

        // Sync stats
        viewModelScope.launch {
            streamManager.stats.collect { stats ->
                _uiState.update { it.copy(bitrateKbps = if (stats.bitrateKbps > 0) stats.bitrateKbps else it.bitrateKbps) }
                // Report to health monitor
                healthMonitor.reportStats(
                    bitrateKbps = stats.bitrateKbps,
                    fps = stats.fps,
                    droppedFrames = stats.droppedFrames,
                    rttMs = 0
                )
            }
        }

        // Sync multi-stream statuses
        viewModelScope.launch {
            multiStreamManager.statuses.collect { statuses ->
                _uiState.update { it.copy(targetStatuses = statuses) }
            }
        }

        // Sync disconnect protection state
        viewModelScope.launch {
            disconnectProtection.showBrb.collect { show ->
                _uiState.update { it.copy(showBrb = show) }
            }
        }
        viewModelScope.launch {
            disconnectProtection.reconnectCountdown.collect { seconds ->
                _uiState.update { it.copy(brbCountdown = seconds) }
            }
        }
        viewModelScope.launch {
            disconnectProtection.networkType.collect { type ->
                _uiState.update { it.copy(networkType = type.name) }
            }
        }

        // Sync health grade
        viewModelScope.launch {
            healthMonitor.healthGrade.collect { grade ->
                _uiState.update { it.copy(healthGrade = grade.name) }
            }
        }

        // Sync scene state
        viewModelScope.launch {
            sceneManager.activeSceneId.collect { id ->
                _uiState.update { it.copy(activeSceneId = id) }
            }
        }
        viewModelScope.launch {
            sceneManager.isTransitioning.collect { transitioning ->
                _uiState.update { it.copy(isTransitioning = transitioning) }
            }
        }
    }

    fun startStream() {
        val state = _uiState.value

        // Check if we should do multi-stream
        val targets = state.multiStreamTargets.filter { it.isEnabled }
        if (targets.size > 1 && featureGate.canMultiStream()) {
            startMultiStream(targets)
            return
        }

        // Single stream mode
        if (state.rtmpUrl.isBlank() || state.streamKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Configure RTMP URL and Stream Key in Settings first") }
            return
        }

        val resolution = parseResolution(state.resolution)
        if (!streamManager.isConfigured()) {
            streamManager.configure(
                width = resolution.first,
                height = resolution.second,
                fps = state.fps,
                bitrate = state.bitrateKbps
            )
        }

        _uiState.update { it.copy(elapsedSeconds = 0L, errorMessage = null, isMultiStream = false) }
        streamManager.startStream(state.rtmpUrl, state.streamKey)
    }

    private fun startMultiStream(targets: List<StreamTarget>) {
        val state = _uiState.value
        val resolution = parseResolution(state.resolution)
        _uiState.update { it.copy(elapsedSeconds = 0L, errorMessage = null, isMultiStream = true) }
        multiStreamManager.startAll(
            targets = targets,
            bitrateKbps = state.bitrateKbps,
            fps = state.fps,
            width = resolution.first,
            height = resolution.second
        )
        // Also start single stream for primary target
        val primary = targets.first()
        if (!streamManager.isConfigured()) {
            streamManager.configure(
                width = resolution.first,
                height = resolution.second,
                fps = state.fps,
                bitrate = state.bitrateKbps
            )
        }
        streamManager.startStream(primary.rtmpUrl, primary.streamKey)
    }

    fun stopStream() {
        streamManager.stopStream()
        if (_uiState.value.isMultiStream) {
            multiStreamManager.stopAll()
        }
        _uiState.update { it.copy(elapsedSeconds = 0L, isMultiStream = false) }
        if (_uiState.value.isRecording) recordingManager.stopRecording()
    }

    fun flipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
        if (_uiState.value.isMultiStream) {
            multiStreamManager.flipCamera()
        }
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        streamManager.setMute(muted)
        if (_uiState.value.isMultiStream) {
            multiStreamManager.setMute(muted)
        }
        _uiState.update { it.copy(isMuted = muted) }
    }

    fun setBitrate(kbps: Int) {
        streamManager.setBitrate(kbps)
        if (_uiState.value.isMultiStream) {
            multiStreamManager.updateBitrate(kbps)
        }
        _uiState.update { it.copy(bitrateKbps = kbps) }
        viewModelScope.launch { settingsRepository.setBitrate(kbps) }
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) recordingManager.stopRecording()
        else recordingManager.startRecording()
    }

    fun toggleOverlay() {
        val enabled = !_uiState.value.overlayEnabled
        _uiState.update { it.copy(overlayEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setOverlayEnabled(enabled) }
    }

    fun setOverlayUri(uri: String) {
        _uiState.update { it.copy(overlayUri = uri) }
        viewModelScope.launch { settingsRepository.setOverlayUri(uri) }
    }

    fun toggleChat() {
        _uiState.update { it.copy(chatVisible = !it.chatVisible) }
    }

    fun connectChat(platform: String, channel: String) {
        viewModelScope.launch {
            settingsRepository.setChatPlatform(platform)
            settingsRepository.setChatChannel(channel)
            chatManager.connectTwitch(channel)
        }
    }

    fun switchScene(sceneId: String) {
        sceneManager.switchTo(sceneId)
    }

    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }

    private fun startHealthMonitoring() {
        healthMonitor.startMonitoring()
    }

    private fun stopHealthMonitoring() {
        val summary = healthMonitor.stopMonitoring()
        Log.d(TAG, "Stream summary: duration=${summary.totalDurationMs}ms avg bitrate=${summary.avgBitrateKbps}")
    }

    private fun startDisconnectProtection() {
        if (!featureGate.canDisconnectProtection()) return
        disconnectProtection.onReconnect = {
            // Auto-reconnect stream
            viewModelScope.launch {
                val state = _uiState.value
                if (state.streamState != StreamState.LIVE) {
                    startStream()
                }
            }
        }
        disconnectProtection.onGiveUp = {
            viewModelScope.launch {
                stopStream()
                _uiState.update { it.copy(errorMessage = "Network lost. Stream stopped.") }
            }
        }
        disconnectProtection.startMonitoring()
    }

    private fun stopDisconnectProtection() {
        disconnectProtection.stopMonitoring()
    }

    private fun startForegroundService() {
        val intent = Intent(app, StreamForegroundService::class.java).apply {
            action = StreamForegroundService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(app, StreamForegroundService::class.java).apply {
            action = StreamForegroundService.ACTION_STOP
        }
        app.startService(intent)
    }

    private fun parseResolution(res: String): Pair<Int, Int> = when (res) {
        "1080p" -> 1920 to 1080
        "1440p" -> 2560 to 1440
        else -> 1280 to 720
    }

    override fun onCleared() {
        super.onCleared()
        chatManager.disconnect()
        disconnectProtection.stopMonitoring()
    }
}
