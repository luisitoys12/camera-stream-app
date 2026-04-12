package tech.estacionkus.camerastream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    secondary = CameraRed,
    tertiary = PurpleAccent,
    background = Surface900,
    surface = Surface800,
    surfaceVariant = Surface600,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceMuted,
    error = Color(0xFFCF6679)
)

@Composable
fun CameraStreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
