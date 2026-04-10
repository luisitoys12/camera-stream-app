package tech.estacionkus.camerastream.ui.screens.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.media.*
import javax.inject.Inject

@HiltViewModel
class MediaLibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(OverlayCategory.OTHER)
    val selectedCategory: StateFlow<OverlayCategory> = _selectedCategory.asStateFlow()

    private val _previewAsset = MutableStateFlow<MediaAsset?>(null)
    val previewAsset: StateFlow<MediaAsset?> = _previewAsset.asStateFlow()

    val assets: StateFlow<List<MediaAsset>> = combine(
        mediaRepository.assets,
        _selectedCategory
    ) { all, cat -> all.filter { it.category == cat } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { viewModelScope.launch { mediaRepository.loadFromGallery() } }

    fun setCategory(cat: OverlayCategory) { _selectedCategory.value = cat }
    fun setPreviewAsset(asset: MediaAsset?) { _previewAsset.value = asset }

    fun importAsset(uri: Uri, name: String, type: MediaAssetType, category: OverlayCategory) {
        viewModelScope.launch {
            mediaRepository.importAsset(uri, name, type, category)
        }
    }

    fun removeAsset(id: String) { mediaRepository.removeAsset(id) }
}
