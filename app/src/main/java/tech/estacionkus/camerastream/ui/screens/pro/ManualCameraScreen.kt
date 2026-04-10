package tech.estacionkus.camerastream.ui.screens.pro

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.domain.model.WhiteBalance

@Composable
fun ManualCameraScreen(
    onBack: () -> Unit,
    viewModel: ManualCameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val selector = if (uiState.isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        provider.unbindAll()
        camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
    }

    // Apply manual controls when camera is available
    LaunchedEffect(camera, uiState.settings) {
        camera?.let { cam ->
            cam.cameraControl.setExposureCompensationIndex(uiState.settings.exposureCompensation)
            if (uiState.settings.stabilizationEnabled) cam.cameraControl.enableTorch(false)
            cam.cameraControl.setZoomRatio(uiState.settings.zoomRatio)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Preview
        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))

        // Controls
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Exposure
            Text("Exposición: ${uiState.settings.exposureCompensation}")
            Slider(
                value = uiState.settings.exposureCompensation.toFloat(),
                onValueChange = { viewModel.setExposure(it.toInt()) },
                valueRange = -4f..4f, steps = 7,
                modifier = Modifier.fillMaxWidth()
            )

            // Zoom
            Text("Zoom: ${"%,.1f".format(uiState.settings.zoomRatio)}x")
            Slider(
                value = uiState.settings.zoomRatio,
                onValueChange = viewModel::setZoom,
                valueRange = 1f..10f,
                modifier = Modifier.fillMaxWidth()
            )

            // White balance
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WhiteBalance.entries.forEach { wb ->
                    FilterChip(
                        selected = uiState.settings.whiteBalance == wb,
                        onClick = { viewModel.setWhiteBalance(wb) },
                        label = { Text(wb.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Stabilization & Night mode
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.settings.stabilizationEnabled,
                    onClick = viewModel::toggleStabilization,
                    label = { Text("Estabilización") }
                )
                FilterChip(
                    selected = uiState.settings.nightModeEnabled,
                    onClick = viewModel::toggleNightMode,
                    label = { Text("Modo nocturno") }
                )
            }
        }
    }
}
