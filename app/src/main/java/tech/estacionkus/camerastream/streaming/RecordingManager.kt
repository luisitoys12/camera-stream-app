package tech.estacionkus.camerastream.streaming

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
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

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private var currentFile: File? = null

    fun startRecording(): File? {
        return try {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "CameraStream"
            ).also { it.mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "stream_$timestamp.mp4")
            currentFile = file
            _isRecording.value = true
            Log.d(TAG, "Recording started: ${file.absolutePath}")

            // Register with MediaStore on Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraStream")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                }
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            _isRecording.value = false
            null
        }
    }

    fun stopRecording() {
        try {
            _isRecording.value = false
            _recordingDuration.value = 0L
            Log.d(TAG, "Recording stopped: ${currentFile?.absolutePath}")
            currentFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
        }
    }

    fun getRecordingsDir(): File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        "CameraStream"
    )
}
