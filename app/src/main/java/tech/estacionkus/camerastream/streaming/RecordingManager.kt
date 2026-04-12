package tech.estacionkus.camerastream.streaming

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "RecordingManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _recordingFileSize = MutableStateFlow(0L)
    val recordingFileSize: StateFlow<Long> = _recordingFileSize.asStateFlow()

    private val _outputUri = MutableStateFlow<Uri?>(null)
    val outputUri: StateFlow<Uri?> = _outputUri.asStateFlow()

    private var activeRecording: Recording? = null
    private var durationJob: Job? = null
    private var recorder: Recorder? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    /**
     * Get a configured VideoCapture use case for CameraX binding.
     * Call this before binding to lifecycle so recording can share the camera.
     */
    fun getVideoCapture(quality: Quality = Quality.HD): VideoCapture<Recorder> {
        val qualitySelector = QualitySelector.from(
            quality,
            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
        )
        recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder!!)
        return videoCapture!!
    }

    /**
     * Start recording using CameraX VideoCapture<Recorder>.
     * The VideoCapture use case must be bound to the camera lifecycle first.
     */
    fun startRecording(): Uri? {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return null
        }

        val currentRecorder = recorder
        if (currentRecorder == null) {
            Log.e(TAG, "Recorder not initialized. Call getVideoCapture() first.")
            return startFallbackRecording()
        }

        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "stream_$timestamp.mp4"

            val pendingRecording = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraStream")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                }
                val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                    context.contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                    .setContentValues(contentValues)
                    .build()
                currentRecorder.prepareRecording(context, mediaStoreOutput)
            } else {
                // Use file output for older Android
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "CameraStream"
                ).also { it.mkdirs() }
                val file = File(dir, fileName)
                val fileOutput = FileOutputOptions.Builder(file).build()
                currentRecorder.prepareRecording(context, fileOutput)
            }

            activeRecording = pendingRecording
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            Log.d(TAG, "Recording started")
                            _isRecording.value = true
                            startDurationTimer()
                        }
                        is VideoRecordEvent.Finalize -> {
                            _isRecording.value = false
                            stopDurationTimer()
                            if (event.hasError()) {
                                Log.e(TAG, "Recording error: ${event.cause?.message}")
                            } else {
                                val uri = event.outputResults.outputUri
                                _outputUri.value = uri
                                Log.d(TAG, "Recording saved: $uri")
                            }
                        }
                        is VideoRecordEvent.Status -> {
                            val stats = event.recordingStats
                            _recordingFileSize.value = stats.numBytesRecorded
                        }
                    }
                }

            _isRecording.value = true
            Log.d(TAG, "Recording request sent")
            null // URI available after finalization
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            _isRecording.value = false
            null
        }
    }

    /**
     * Fallback recording path when CameraX VideoCapture is not bound.
     * Creates a file entry for external systems to write to.
     */
    private fun startFallbackRecording(): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "stream_$timestamp.mp4"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraStream")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
                _outputUri.value = uri
                _isRecording.value = true
                startDurationTimer()
                Log.d(TAG, "Fallback recording started: $uri")
                uri
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "CameraStream"
                ).also { it.mkdirs() }
                val file = File(dir, fileName)
                _outputUri.value = Uri.fromFile(file)
                _isRecording.value = true
                startDurationTimer()
                Log.d(TAG, "Fallback recording started: ${file.absolutePath}")
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback recording failed: ${e.message}")
            _isRecording.value = false
            null
        }
    }

    fun stopRecording() {
        try {
            activeRecording?.stop()
            activeRecording = null
            stopDurationTimer()
            _isRecording.value = false
            _recordingDuration.value = 0L
            _recordingFileSize.value = 0L
            Log.d(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            _isRecording.value = false
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        _recordingDuration.value = 0L
        durationJob = scope.launch {
            while (isActive && _isRecording.value) {
                delay(1000)
                _recordingDuration.value++
            }
        }
    }

    private fun stopDurationTimer() {
        durationJob?.cancel()
        durationJob = null
    }

    fun getRecordingsDir(): File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        "CameraStream"
    )
}
