package tech.estacionkus.camerastream.data.media

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _assets = MutableStateFlow<List<MediaAsset>>(emptyList())
    val assets: StateFlow<List<MediaAsset>> = _assets.asStateFlow()

    // Load images+videos from device gallery
    suspend fun loadFromGallery() = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaAsset>()

        // Query images
        val imgProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imgProjection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                items.add(MediaAsset(
                    id = id.toString(),
                    uri = uri,
                    name = cursor.getString(nameCol),
                    type = MediaAssetType.IMAGE,
                    sizeBytes = cursor.getLong(sizeCol)
                ))
            }
        }

        // Query videos
        val vidProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            vidProjection, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                items.add(MediaAsset(
                    id = id.toString(),
                    uri = uri,
                    name = cursor.getString(nameCol),
                    type = MediaAssetType.VIDEO,
                    durationMs = cursor.getLong(durCol),
                    sizeBytes = cursor.getLong(sizeCol)
                ))
            }
        }

        _assets.value = items
    }

    // Import URI from picker (copies to app storage)
    suspend fun importAsset(uri: Uri, name: String, type: MediaAssetType, category: OverlayCategory): MediaAsset = withContext(Dispatchers.IO) {
        val asset = MediaAsset(
            id = UUID.randomUUID().toString(),
            uri = uri,
            name = name,
            type = type,
            category = category
        )
        _assets.value = _assets.value + asset
        asset
    }

    fun removeAsset(id: String) {
        _assets.value = _assets.value.filter { it.id != id }
    }

    fun getByCategory(category: OverlayCategory) = assets.value.filter { it.category == category }
}
