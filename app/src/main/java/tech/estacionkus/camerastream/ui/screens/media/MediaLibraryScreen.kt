package tech.estacionkus.camerastream.ui.screens.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import tech.estacionkus.camerastream.data.media.MediaAsset
import tech.estacionkus.camerastream.data.media.MediaAssetType
import tech.estacionkus.camerastream.data.media.OverlayCategory
import tech.estacionkus.camerastream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaLibraryScreen(
    viewModel: MediaLibraryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val previewAsset by viewModel.previewAsset.collectAsState()
    val context = LocalContext.current

    // Picker for images
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val name = uri.lastPathSegment ?: "image"
            viewModel.importAsset(uri, name, MediaAssetType.IMAGE, selectedCategory)
        }
    }

    // Picker for videos
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val name = uri.lastPathSegment ?: "video"
            viewModel.importAsset(uri, name, MediaAssetType.VIDEO, selectedCategory)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca de medios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Agregar imagen")
                    }
                    IconButton(onClick = { videoPicker.launch("video/*") }) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = "Agregar video")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface800)
            )
        },
        containerColor = Surface900
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Category filter tabs
            val categories = OverlayCategory.values().toList()
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Surface800,
                edgePadding = 16.dp
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setCategory(cat) },
                        text = {
                            Text(
                                cat.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            if (assets.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.PermMedia, contentDescription = null, modifier = Modifier.size(56.dp), tint = OnSurfaceMuted)
                        Text("Sin medios en esta categoría", color = OnSurfaceMuted)
                        OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                            Text("Importar desde galería")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assets, key = { it.id }) { asset ->
                        MediaAssetCard(
                            asset = asset,
                            onPreview = { viewModel.setPreviewAsset(asset) },
                            onDelete = { viewModel.removeAsset(asset.id) }
                        )
                    }
                }
            }
        }
    }

    // Preview dialog
    previewAsset?.let { asset ->
        MediaPreviewDialog(
            asset = asset,
            onDismiss = { viewModel.setPreviewAsset(null) }
        )
    }
}

@Composable
private fun MediaAssetCard(
    asset: MediaAsset,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface700)
            .clickable(onClick = onPreview)
    ) {
        AsyncImage(
            model = asset.uri,
            contentDescription = asset.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Type badge
        Surface(
            color = if (asset.type == MediaAssetType.VIDEO) CameraRed.copy(alpha = 0.85f)
            else Surface800.copy(alpha = 0.75f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (asset.type == MediaAssetType.VIDEO) Icons.Default.PlayArrow else Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
        }

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        // Name at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush = null, color = Color.Black.copy(alpha = 0.55f))
        ) {
            Text(
                asset.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun MediaPreviewDialog(asset: MediaAsset, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface800,
        title = { Text(asset.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface700)
                ) {
                    if (asset.type == MediaAssetType.IMAGE) {
                        AsyncImage(
                            model = asset.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Video preview via ExoPlayer
                        VideoPreview(uri = asset.uri)
                    }
                }
                Text(
                    "Categoría: ${asset.category.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted
                )
                asset.durationMs?.let {
                    Text(
                        "Duración: ${it / 1000}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
