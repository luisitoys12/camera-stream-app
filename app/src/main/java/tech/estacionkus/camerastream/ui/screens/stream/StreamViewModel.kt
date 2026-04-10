package tech.estacionkus.camerastream.ui.screens.stream

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.streaming.StreamManager
import tech.estacionkus.camerastream.streaming.StreamStats
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    application: Application,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    private val streamManager = StreamManager(application)
    val stats: StateFlow<StreamStats> = streamManager.stats

    private val _overlayPanelVisible = MutableStateFlow(false)
    val overlayPanelVisible: StateFlow<Boolean> = _overlayPanelVisible.asStateFlow()

    fun startStream() {
        viewModelScope.launch {
            settingsRepo.settings.first().let { streamManager.startStream(it) }
        }
    }

    fun stopStream() = streamManager.stopStream()
    fun flipCamera() { /* CameraX switch lens */ }
    fun toggleMute() { /* Toggle AudioRecord mute */ }
    fun toggleOverlayPanel() { _overlayPanelVisible.value = !_overlayPanelVisible.value }
}
