package tech.estacionkus.camerastream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CameraRed = Color(0xFFE53935)
val Surface900 = Color(0xFF0A0A0A)
val Surface700 = Color(0xFF1E1E1E)
val Surface600 = Color(0xFF2C2C2C)
val OnSurface = Color(0xFFEEEEEE)
val OnSurfaceMuted = Color(0xFF888888)

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
