package tech.estacionkus.camerastream.ui.screens.editor

import android.app.Application
import android.content.ContentValues
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class EditorUiState(
    val project: EditorProject = EditorProject(),
    val selectedClipIndex: Int = -1,
    val selectedTextLayerId: String? = null,
    val currentTool: EditorTool = EditorTool.TRIM,
    val playbackPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val zoomLevel: Float = 1f,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportQuality: ExportQuality = ExportQuality.HIGH,
    val exportResolution: ExportResolution = ExportResolution.RES_1080P,
    val exportedUri: Uri? = null,
    val errorMessage: String? = null,
    val thumbnails: Map<String, List<android.graphics.Bitmap>> = emptyMap()
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {

    private val TAG = "EditorViewModel"

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Undo/Redo stacks
    private val undoStack = mutableListOf<EditorProject>()
    private val redoStack = mutableListOf<EditorProject>()
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    private var transformer: Transformer? = null

    // ---- Clip management ----

    fun addClip(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(app, uri)
                val durationMs = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L
                retriever.release()

                val clip = VideoClip(
                    uri = uri,
                    startMs = 0L,
                    endMs = durationMs,
                    originalDurationMs = durationMs
                )

                withContext(Dispatchers.Main) {
                    saveUndoState()
                    val project = _uiState.value.project
                    _uiState.value = _uiState.value.copy(
                        project = project.copy(clips = project.clips + clip),
                        selectedClipIndex = project.clips.size
                    )
                }

                extractThumbnails(clip)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add clip: ${e.message}")
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to add video: ${e.message}")
                }
            }
        }
    }

    fun removeClip(index: Int) {
        saveUndoState()
        val project = _uiState.value.project
        if (index in project.clips.indices) {
            val newClips = project.clips.toMutableList().apply { removeAt(index) }
            val newTransitions = project.transitions.filterKeys { it != index && it != index - 1 }
            _uiState.value = _uiState.value.copy(
                project = project.copy(clips = newClips, transitions = newTransitions),
                selectedClipIndex = -1
            )
        }
    }

    fun reorderClip(fromIndex: Int, toIndex: Int) {
        saveUndoState()
        val project = _uiState.value.project
        val clips = project.clips.toMutableList()
        if (fromIndex in clips.indices && toIndex in clips.indices) {
            val clip = clips.removeAt(fromIndex)
            clips.add(toIndex, clip)
            _uiState.value = _uiState.value.copy(
                project = project.copy(clips = clips)
            )
        }
    }

    fun selectClip(index: Int) {
        _uiState.value = _uiState.value.copy(selectedClipIndex = index)
    }

    // ---- Trimming ----

    fun trimClip(clipIndex: Int, newStartMs: Long, newEndMs: Long) {
        saveUndoState()
        val project = _uiState.value.project
        if (clipIndex in project.clips.indices) {
            val clip = project.clips[clipIndex]
            val updatedClip = clip.copy(
                startMs = newStartMs.coerceIn(0L, clip.originalDurationMs),
                endMs = newEndMs.coerceIn(newStartMs, clip.originalDurationMs)
            )
            val clips = project.clips.toMutableList().apply { set(clipIndex, updatedClip) }
            _uiState.value = _uiState.value.copy(project = project.copy(clips = clips))
        }
    }

    // ---- Speed ----

    fun setClipSpeed(clipIndex: Int, speed: Float) {
        saveUndoState()
        val project = _uiState.value.project
        if (clipIndex in project.clips.indices) {
            val clips = project.clips.toMutableList()
            clips[clipIndex] = clips[clipIndex].copy(speed = speed.coerceIn(0.25f, 4f))
            _uiState.value = _uiState.value.copy(project = project.copy(clips = clips))
        }
    }

    // ---- Volume ----

    fun setClipVolume(clipIndex: Int, volume: Float) {
        saveUndoState()
        val project = _uiState.value.project
        if (clipIndex in project.clips.indices) {
            val clips = project.clips.toMutableList()
            clips[clipIndex] = clips[clipIndex].copy(volume = volume.coerceIn(0f, 2f))
            _uiState.value = _uiState.value.copy(project = project.copy(clips = clips))
        }
    }

    // ---- Filters ----

    fun setClipFilter(clipIndex: Int, filterName: String?, intensity: Float = 1f) {
        saveUndoState()
        val project = _uiState.value.project
        if (clipIndex in project.clips.indices) {
            val clips = project.clips.toMutableList()
            clips[clipIndex] = clips[clipIndex].copy(filterName = filterName, filterIntensity = intensity)
            _uiState.value = _uiState.value.copy(project = project.copy(clips = clips))
        }
    }

    // ---- Text layers ----

    fun addTextLayer(text: String) {
        saveUndoState()
        val project = _uiState.value.project
        val layer = TextLayer(
            text = text,
            startMs = _uiState.value.playbackPositionMs,
            endMs = _uiState.value.playbackPositionMs + 5000L
        )
        _uiState.value = _uiState.value.copy(
            project = project.copy(textLayers = project.textLayers + layer),
            selectedTextLayerId = layer.id
        )
    }

    fun updateTextLayer(id: String, transform: TextLayer.() -> TextLayer) {
        saveUndoState()
        val project = _uiState.value.project
        val layers = project.textLayers.map {
            if (it.id == id) it.transform() else it
        }
        _uiState.value = _uiState.value.copy(project = project.copy(textLayers = layers))
    }

    fun removeTextLayer(id: String) {
        saveUndoState()
        val project = _uiState.value.project
        _uiState.value = _uiState.value.copy(
            project = project.copy(textLayers = project.textLayers.filter { it.id != id }),
            selectedTextLayerId = null
        )
    }

    // ---- Transitions ----

    fun setTransition(clipIndex: Int, transition: Transition) {
        saveUndoState()
        val project = _uiState.value.project
        val transitions = project.transitions.toMutableMap()
        if (transition.type == TransitionType.NONE) {
            transitions.remove(clipIndex)
        } else {
            transitions[clipIndex] = transition
        }
        _uiState.value = _uiState.value.copy(
            project = project.copy(transitions = transitions)
        )
    }

    // ---- Audio layers ----

    fun addAudioLayer(uri: Uri, name: String, isVoiceover: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(app, uri)
                val durationMs = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L
                retriever.release()

                val layer = AudioLayer(
                    uri = uri,
                    name = name,
                    startMs = 0L,
                    endMs = durationMs,
                    isVoiceover = isVoiceover
                )

                withContext(Dispatchers.Main) {
                    saveUndoState()
                    val project = _uiState.value.project
                    _uiState.value = _uiState.value.copy(
                        project = project.copy(audioLayers = project.audioLayers + layer)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add audio: ${e.message}")
            }
        }
    }

    fun updateAudioLayer(id: String, transform: AudioLayer.() -> AudioLayer) {
        saveUndoState()
        val project = _uiState.value.project
        val layers = project.audioLayers.map {
            if (it.id == id) it.transform() else it
        }
        _uiState.value = _uiState.value.copy(project = project.copy(audioLayers = layers))
    }

    fun removeAudioLayer(id: String) {
        saveUndoState()
        val project = _uiState.value.project
        _uiState.value = _uiState.value.copy(
            project = project.copy(audioLayers = project.audioLayers.filter { it.id != id })
        )
    }

    // ---- Templates ----

    fun applyTemplate(template: EditorTemplate) {
        saveUndoState()
        val project = _uiState.value.project
        _uiState.value = _uiState.value.copy(
            project = project.copy(
                aspectRatio = template.aspectRatio,
                textLayers = template.textLayers,
                transitions = project.clips.indices.drop(1).associateWith {
                    Transition(template.transitionType, 500L)
                }
            )
        )
    }

    // ---- Aspect ratio ----

    fun setAspectRatio(ratio: AspectRatio) {
        saveUndoState()
        _uiState.value = _uiState.value.copy(
            project = _uiState.value.project.copy(aspectRatio = ratio)
        )
    }

    // ---- Tool selection ----

    fun selectTool(tool: EditorTool) {
        _uiState.value = _uiState.value.copy(currentTool = tool)
    }

    // ---- Playback ----

    fun setPlaybackPosition(posMs: Long) {
        _uiState.value = _uiState.value.copy(playbackPositionMs = posMs)
    }

    fun togglePlayback() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    // ---- Zoom ----

    fun setZoomLevel(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomLevel = zoom.coerceIn(0.5f, 5f))
    }

    // ---- Export settings ----

    fun setExportQuality(quality: ExportQuality) {
        _uiState.value = _uiState.value.copy(exportQuality = quality)
    }

    fun setExportResolution(resolution: ExportResolution) {
        _uiState.value = _uiState.value.copy(exportResolution = resolution)
    }

    // ---- Undo / Redo ----

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_uiState.value.project)
        val prev = undoStack.removeLast()
        _uiState.value = _uiState.value.copy(project = prev)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(_uiState.value.project)
        val next = redoStack.removeLast()
        _uiState.value = _uiState.value.copy(project = next)
    }

    private fun saveUndoState() {
        undoStack.add(_uiState.value.project)
        if (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
    }

    // ---- Export with Media3 Transformer ----

    fun startExport() {
        val project = _uiState.value.project
        if (project.clips.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Add at least one video clip")
            return
        }

        _uiState.value = _uiState.value.copy(isExporting = true, exportProgress = 0f, exportedUri = null)

        viewModelScope.launch {
            try {
                val resolution = _uiState.value.exportResolution
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "edited_$timestamp.mp4"

                val outputFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val cacheFile = File(app.cacheDir, fileName)
                    cacheFile
                } else {
                    val dir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        "CameraStream/Edited"
                    ).also { it.mkdirs() }
                    File(dir, fileName)
                }

                // Build edited media items from clips
                val editedItems = project.clips.map { clip ->
                    val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.startMs)
                        .setEndPositionMs(clip.endMs)
                        .build()

                    val mediaItem = MediaItem.Builder()
                        .setUri(clip.uri)
                        .setClippingConfiguration(clippingConfig)
                        .build()

                    EditedMediaItem.Builder(mediaItem).build()
                }

                val sequence = EditedMediaItemSequence(editedItems)
                val composition = Composition.Builder(sequence).build()

                withContext(Dispatchers.Main) {
                    transformer = Transformer.Builder(app)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                viewModelScope.launch {
                                    val uri = saveToGallery(outputFile, fileName)
                                    _uiState.value = _uiState.value.copy(
                                        isExporting = false,
                                        exportProgress = 1f,
                                        exportedUri = uri
                                    )
                                    Log.d(TAG, "Export completed: $uri")
                                }
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException
                            ) {
                                _uiState.value = _uiState.value.copy(
                                    isExporting = false,
                                    errorMessage = "Export failed: ${exportException.message}"
                                )
                                Log.e(TAG, "Export error", exportException)
                            }
                        })
                        .build()

                    transformer?.start(composition, outputFile.absolutePath)

                    // Track progress
                    launch {
                        while (isActive && _uiState.value.isExporting) {
                            val progress = transformer?.getProgress(
                                androidx.media3.transformer.ProgressHolder()
                            )
                            // Progress tracking
                            delay(250)
                            if (_uiState.value.isExporting) {
                                _uiState.value = _uiState.value.copy(
                                    exportProgress = (_uiState.value.exportProgress + 0.01f).coerceAtMost(0.95f)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export setup failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${e.message}"
                )
            }
        }
    }

    fun cancelExport() {
        transformer?.cancel()
        _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 0f)
    }

    private suspend fun saveToGallery(file: File, fileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraStream/Edited")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                }
                val uri = app.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
                if (uri != null) {
                    app.contentResolver.openOutputStream(uri)?.use { os ->
                        file.inputStream().use { it.copyTo(os) }
                    }
                    file.delete()
                }
                uri
            } else {
                Uri.fromFile(file)
            }
        }
    }

    // ---- Thumbnail extraction ----

    private fun extractThumbnails(clip: VideoClip) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(app, clip.uri)
                val duration = clip.originalDurationMs
                val thumbCount = (duration / 2000).toInt().coerceIn(3, 20)
                val thumbnails = mutableListOf<android.graphics.Bitmap>()

                for (i in 0 until thumbCount) {
                    val timeUs = (duration * i / thumbCount) * 1000
                    val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    if (bitmap != null) {
                        // Scale down for timeline display
                        val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 80, 45, true)
                        thumbnails.add(scaled)
                        if (bitmap != scaled) bitmap.recycle()
                    }
                }
                retriever.release()

                withContext(Dispatchers.Main) {
                    val current = _uiState.value.thumbnails.toMutableMap()
                    current[clip.id] = thumbnails
                    _uiState.value = _uiState.value.copy(thumbnails = current)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Thumbnail extraction failed: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        transformer?.cancel()
    }
}
