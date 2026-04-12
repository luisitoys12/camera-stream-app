package tech.estacionkus.camerastream.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.domain.model.Platform
import javax.inject.Inject

data class SettingsUiState(
    val platform: String = "YouTube",
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val bitrateKbps: Int = 2500,
    val fps: Int = 30,
    val resolution: String = "720p",
    val audioBitrateKbps: Int = 128,
    val saved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.platform,
                repo.rtmpUrl,
                repo.streamKey,
                repo.bitrate,
                repo.fps,
                repo.resolution
            ) { values ->
                _uiState.update { it.copy(
                    platform = values[0] as String,
                    rtmpUrl = values[1] as String,
                    streamKey = values[2] as String,
                    bitrateKbps = values[3] as Int,
                    fps = values[4] as Int,
                    resolution = values[5] as String
                ) }
            }.collect()
        }

        viewModelScope.launch {
            repo.audioBitrate.collect { abr ->
                _uiState.update { it.copy(audioBitrateKbps = abr) }
            }
        }
    }

    fun setPlatform(p: Platform) {
        _uiState.update { it.copy(platform = p.displayName, rtmpUrl = p.rtmpBase) }
    }
    fun setRtmpUrl(v: String) { _uiState.update { it.copy(rtmpUrl = v, saved = false) } }
    fun setStreamKey(v: String) { _uiState.update { it.copy(streamKey = v, saved = false) } }
    fun setBitrate(v: Int) { _uiState.update { it.copy(bitrateKbps = v, saved = false) } }
    fun setFps(v: Int) { _uiState.update { it.copy(fps = v, saved = false) } }
    fun setResolution(v: String) { _uiState.update { it.copy(resolution = v, saved = false) } }
    fun setAudioBitrate(v: Int) { _uiState.update { it.copy(audioBitrateKbps = v, saved = false) } }

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        repo.setRtmpUrl(state.rtmpUrl)
        repo.setStreamKey(state.streamKey)
        repo.setPlatform(state.platform)
        repo.setBitrate(state.bitrateKbps)
        repo.setFps(state.fps)
        repo.setResolution(state.resolution)
        repo.setAudioBitrate(state.audioBitrateKbps)
        _uiState.update { it.copy(saved = true) }
    }
}
