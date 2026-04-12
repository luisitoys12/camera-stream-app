package tech.estacionkus.camerastream.ui.screens.media

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VideoPreview(uri: Uri, modifier: Modifier = Modifier) {
    // Placeholder — ExoPlayer/media3 not included in dependencies
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = "Video preview",
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}
