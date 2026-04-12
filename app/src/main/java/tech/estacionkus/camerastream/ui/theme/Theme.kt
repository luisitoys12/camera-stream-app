package tech.estacionkus.camerastream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = CameraRed,
    background = Surface900,
    surface = Surface700,
    onPrimary = Color.White,
    onBackground = OnSurface,
    onSurface = OnSurface
)

@Composable
fun CameraStreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
