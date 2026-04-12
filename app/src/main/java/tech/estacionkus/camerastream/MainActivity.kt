package tech.estacionkus.camerastream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import tech.estacionkus.camerastream.ui.navigation.AppNavigation
import tech.estacionkus.camerastream.ui.theme.CameraStreamTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requiredPermissions = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CameraStreamTheme {
                var permissionsGranted by remember { mutableStateOf(checkPermissions()) }
                var showRationale by remember { mutableStateOf(false) }
                var permissionsDenied by remember { mutableStateOf(false) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val allGranted = results.values.all { it }
                    val cameraGranted = results[Manifest.permission.CAMERA] == true
                    val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true

                    if (cameraGranted && audioGranted) {
                        permissionsGranted = true
                    } else {
                        permissionsDenied = true
                    }
                }

                if (permissionsGranted) {
                    val prefs = getSharedPreferences("camerastream", MODE_PRIVATE)
                    val showOnboarding = !prefs.getBoolean("onboarding_done", false)
                    if (showOnboarding) prefs.edit().putBoolean("onboarding_done", true).apply()
                    AppNavigation(showOnboarding = showOnboarding)
                } else if (permissionsDenied) {
                    PermissionDeniedScreen(
                        onRetry = {
                            permissionsDenied = false
                            launcher.launch(requiredPermissions)
                        }
                    )
                } else if (showRationale) {
                    PermissionRationaleScreen(
                        onAccept = {
                            showRationale = false
                            launcher.launch(requiredPermissions)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        val shouldShowRationale = requiredPermissions.any {
                            shouldShowRequestPermissionRationale(it)
                        }
                        if (shouldShowRationale) {
                            showRationale = true
                        } else {
                            launcher.launch(requiredPermissions)
                        }
                    }
                    // Show loading while waiting for permission dialog
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Composable
    private fun rememberLauncherForActivityResult(
        contract: ActivityResultContracts.RequestMultiplePermissions,
        onResult: (Map<String, Boolean>) -> Unit
    ) = androidx.activity.compose.rememberLauncherForActivityResult(contract, onResult)
}

@Composable
fun PermissionRationaleScreen(onAccept: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(72.dp), tint = Color(0xFFE53935))
            Text(
                "Camera & Microphone Required",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "CameraStream needs camera and microphone access to capture video and audio for live streaming and recording.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFBBBBBB),
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFFE53935), modifier = Modifier.size(24.dp))
                Text("Camera - for live video preview and streaming", color = Color(0xFFDDDDDD))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Mic, null, tint = Color(0xFFE53935), modifier = Modifier.size(24.dp))
                Text("Microphone - for live audio in your stream", color = Color(0xFFDDDDDD))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Grant Permissions", color = Color.White)
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Warning, null, modifier = Modifier.size(64.dp), tint = Color(0xFFFF9800))
            Text(
                "Permissions Required",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Camera and microphone permissions are required to use CameraStream. Please grant these permissions to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFBBBBBB),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Try Again", color = Color.White)
            }
            Text(
                "If permissions keep being denied, open Settings > Apps > CameraStream > Permissions",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }
    }
}
