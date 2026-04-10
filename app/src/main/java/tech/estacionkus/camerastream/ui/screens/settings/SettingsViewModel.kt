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
            combine(repo.platform, repo.rtmpUrl, repo.streamKey, repo.bitrate) { p, url, key, b ->
                _uiState.value = _uiState.value.copy(platform = p, rtmpUrl = url, streamKey = key, bitrateKbps = b)
            }.collect()
        }
    }

    fun setPlatform(p: Platform) {
        _uiState.value = _uiState.value.copy(platform = p.displayName, rtmpUrl = p.rtmpBase)
    }
    fun setRtmpUrl(v: String) { _uiState.value = _uiState.value.copy(rtmpUrl = v) }
    fun setStreamKey(v: String) { _uiState.value = _uiState.value.copy(streamKey = v) }
    fun setBitrate(v: Int) { _uiState.value = _uiState.value.copy(bitrateKbps = v) }

    fun save() = viewModelScope.launch {
        repo.setRtmpUrl(_uiState.value.rtmpUrl)
        repo.setStreamKey(_uiState.value.streamKey)
        repo.setPlatform(_uiState.value.platform)
        repo.setBitrate(_uiState.value.bitrateKbps)
        _uiState.value = _uiState.value.copy(saved = true)
    }
}
