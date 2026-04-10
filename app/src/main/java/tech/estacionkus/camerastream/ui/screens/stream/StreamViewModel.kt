package tech.estacionkus.camerastream.ui.screens.stream

import android.app.Application
import androidx.camera.core.CameraControl
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.media.MediaAsset
import tech.estacionkus.camerastream.data.media.MediaRepository
import tech.estacionkus.camerastream.data.overlay.ActiveOverlay
import tech.estacionkus.camerastream.data.overlay.OverlayRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.streaming.StreamManager
import tech.estacionkus.camerastream.streaming.StreamStats
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    application: Application,
    private val settingsRepo: SettingsRepository,
    private val mediaRepository: MediaRepository,
    private val overlayRepository: OverlayRepository
) : AndroidViewModel(application) {

    private val streamManager = StreamManager(application)

    val stats: StateFlow<StreamStats> = streamManager.stats
    val activeOverlays: StateFlow<List<ActiveOverlay>> = overlayRepository.activeOverlays
    val mediaAssets: StateFlow<List<MediaAsset>> = mediaRepository.assets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _overlayPanelVisible = MutableStateFlow(false)
    val overlayPanelVisible: StateFlow<Boolean> = _overlayPanelVisible.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var cameraControl: CameraControl? = null
    private var isFrontCamera = false

    init {
        viewModelScope.launch {
            overlayRepository.loadPersisted()
            mediaRepository.loadFromGallery()
        }
    }

    fun onCameraReady(control: CameraControl) { cameraControl = control }

    fun startStream() {
        viewModelScope.launch {
            settingsRepo.settings.first().let { streamManager.startStream(it) }
        }
    }

    fun stopStream() = streamManager.stopStream()

    fun flipCamera() {
        isFrontCamera = !isFrontCamera
        // CameraX re-bind handled by LaunchedEffect key change — pass signal via StateFlow if needed
    }

    fun toggleMute() { _isMuted.value = !_isMuted.value }

    fun toggleOverlayPanel() { _overlayPanelVisible.value = !_overlayPanelVisible.value }

    fun addOverlay(overlay: ActiveOverlay) {
        overlayRepository.addOverlay(overlay)
    }

    fun removeOverlay(id: String) {
        overlayRepository.removeOverlay(id)
    }
}
