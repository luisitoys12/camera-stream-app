package tech.estacionkus.camerastream.data.camera

import android.content.Context
import android.hardware.camera2.*
import android.util.Range
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class CameraState(
    val isFrontCamera: Boolean = false,
    val zoom: Float = 1f,
    val iso: Int? = null,
    val exposureCompensation: Int = 0,
    val whiteBalance: Int = CaptureRequest.CONTROL_AWB_MODE_AUTO,
    val isManualFocus: Boolean = false,
    val focusDistance: Float = 0f,
    val isStabilizationEnabled: Boolean = true,
    val activeLens: LensType = LensType.MAIN
)

enum class LensType { ULTRA_WIDE, MAIN, TELEPHOTO }

@Singleton
class CameraController @Inject constructor(private val context: Context) {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    fun flipCamera() {
        _state.value = _state.value.copy(isFrontCamera = !_state.value.isFrontCamera)
        // Re-bind CameraX with new selector
    }

    fun setZoom(zoomRatio: Float) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
        _state.value = _state.value.copy(zoom = zoomRatio)
    }

    fun setManualISO(iso: Int) {
        applyCamera2Option { builder ->
            builder.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, iso)
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
        }
        _state.value = _state.value.copy(iso = iso)
    }

    fun setExposureCompensation(ev: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(ev)
        _state.value = _state.value.copy(exposureCompensation = ev)
    }

    fun setWhiteBalance(mode: Int) {
        applyCamera2Option { builder ->
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, mode)
        }
        _state.value = _state.value.copy(whiteBalance = mode)
    }

    fun setManualFocus(distance: Float) {
        applyCamera2Option { builder ->
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            builder.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, distance)
        }
        _state.value = _state.value.copy(isManualFocus = true, focusDistance = distance)
    }

    fun resetAutoFocus() {
        applyCamera2Option { builder ->
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
        }
        _state.value = _state.value.copy(isManualFocus = false)
    }

    fun setStabilization(enabled: Boolean) {
        applyCamera2Option { builder ->
            builder.setCaptureRequestOption(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                if (enabled) CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                else CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            )
        }
        _state.value = _state.value.copy(isStabilizationEnabled = enabled)
    }

    fun switchLens(lens: LensType) {
        _state.value = _state.value.copy(activeLens = lens)
        // Re-bind CameraX with physical camera ID for multi-lens
    }

    private fun applyCamera2Option(block: (CaptureRequestOptions.Builder) -> Unit) {
        camera?.let { cam ->
            val c2control = Camera2CameraControl.from(cam.cameraControl)
            val builder = CaptureRequestOptions.Builder()
            block(builder)
            c2control.captureRequestOptions = builder.build()
        }
    }
}
