package com.cushMedia.camerastream.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cushMedia.camerastream.streaming.StreamManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StreamViewModel : ViewModel() {

    private val TAG = "StreamViewModel"

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _streamStatus = MutableStateFlow("Listo para transmitir")
    val streamStatus: StateFlow<String> = _streamStatus

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null

    private val streamManager = StreamManager()

    fun startCamera(context: Context, owner: LifecycleOwner, preview: PreviewView) {
        lifecycleOwner = owner
        previewView = preview
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        val owner = lifecycleOwner ?: return
        val preview = previewView ?: return

        val cameraPreview = Preview.Builder().build().also {
            it.setSurfaceProvider(preview.surfaceProvider)
        }

        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(owner, cameraSelector, cameraPreview)
            Log.i(TAG, "Cámara iniciada: $cameraSelector")
        }.onFailure {
            Log.e(TAG, "Error al iniciar cámara: ${it.message}")
            _streamStatus.value = "Error de cámara"
        }
    }

    fun flipCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        bindCamera()
        Log.i(TAG, "Cámara volteada a: $cameraSelector")
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        Log.i(TAG, "Mute: ${_isMuted.value}")
    }

    fun startStream() {
        viewModelScope.launch {
            runCatching {
                _streamStatus.value = "Conectando..."
                streamManager.startStreaming()
                _isStreaming.value = true
                _streamStatus.value = "Transmitiendo"
                Log.i(TAG, "Stream iniciado")
            }.onFailure {
                _streamStatus.value = "Error: ${it.message}"
                Log.e(TAG, "Error al iniciar stream: ${it.message}")
            }
        }
    }

    fun stopStream() {
        viewModelScope.launch {
            runCatching {
                streamManager.stopStreaming()
                _isStreaming.value = false
                _streamStatus.value = "Stream detenido"
                Log.i(TAG, "Stream detenido")
            }.onFailure {
                Log.e(TAG, "Error al detener stream: ${it.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamManager.release()
    }
}
