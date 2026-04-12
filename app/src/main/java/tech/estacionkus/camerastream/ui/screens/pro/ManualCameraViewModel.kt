package tech.estacionkus.camerastream.ui.screens.pro

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import tech.estacionkus.camerastream.domain.model.CameraSettings
import tech.estacionkus.camerastream.domain.model.ColorFilter
import tech.estacionkus.camerastream.domain.model.FocusMode
import tech.estacionkus.camerastream.domain.model.GridOverlay
import tech.estacionkus.camerastream.domain.model.WhiteBalance
import javax.inject.Inject

data class ManualCameraUiState(
    val settings: CameraSettings = CameraSettings(),
    val isFront: Boolean = false
)

@HiltViewModel
class ManualCameraViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ManualCameraUiState())
    val uiState: StateFlow<ManualCameraUiState> = _uiState.asStateFlow()

    private fun update(fn: CameraSettings.() -> CameraSettings) {
        _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.fn())
    }

    fun setExposure(v: Int) = update { copy(exposureCompensation = v) }
    fun setZoom(v: Float) = update { copy(zoomRatio = v) }
    fun setIso(v: Int) = update { copy(iso = v) }
    fun setWhiteBalance(wb: WhiteBalance) = update { copy(whiteBalance = wb) }
    fun setFocusMode(mode: FocusMode) = update { copy(focusMode = mode) }
    fun setManualFocus(distance: Float) = update { copy(manualFocusDistance = distance) }
    fun toggleStabilization() = update { copy(stabilizationEnabled = !stabilizationEnabled) }
    fun toggleNightMode() = update { copy(nightModeEnabled = !nightModeEnabled) }
    fun toggleTorch() = update { copy(torchEnabled = !torchEnabled) }
    fun toggleBeautyFilter() = update { copy(beautyFilterEnabled = !beautyFilterEnabled) }
    fun setBeautySmoothing(v: Float) = update { copy(beautySmoothing = v) }
    fun setBeautyBrightness(v: Float) = update { copy(beautyBrightness = v) }
    fun setColorFilter(filter: ColorFilter) = update { copy(colorFilter = filter) }
    fun setGridOverlay(grid: GridOverlay) = update { copy(gridOverlay = grid) }
    fun flipCamera() { _uiState.value = _uiState.value.copy(isFront = !_uiState.value.isFront) }
}
