package tech.estacionkus.camerastream.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CameraRed = Color(0xFFE53935)
val CameraRedDark = Color(0xFFB71C1C)
val Surface900 = Color(0xFF0A0A0A)
val Surface800 = Color(0xFF141414)
val Surface700 = Color(0xFF1E1E1E)
val Surface600 = Color(0xFF2A2A2A)
val OnSurface = Color(0xFFF5F5F5)
val OnSurfaceMuted = Color(0xFF8A8A8A)

private val DarkColorScheme = darkColorScheme(
    primary = CameraRed,
    onPrimary = Color.White,
    primaryContainer = CameraRedDark,
    background = Surface900,
    surface = Surface800,
    surfaceVariant = Surface700,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceMuted,
    outline = Surface600,
)

@Composable
fun CameraStreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = CameraStreamTypography,
        content = content
    )
}
