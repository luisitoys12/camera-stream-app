package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.estacionkus.camerastream.ui.screens.auth.AuthScreen
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraScreen
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerScreen
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("auth") { AuthScreen(onAuthenticated = { nav.navigate("home") { popUpTo("auth") { inclusive = true } } }) }
        composable("home") {
            HomeScreen(
                onStartStream = { nav.navigate("stream") },
                onOpenSettings = { nav.navigate("settings") },
                onOpenMedia = { nav.navigate("srt") },
                onUpgrade = { nav.navigate("upgrade") }
            )
        }
        composable("stream") { StreamScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable("upgrade") { UpgradeScreen(onBack = { nav.popBackStack() }) }
        composable("srt") { SrtServerScreen(onBack = { nav.popBackStack() }) }
        composable("manual_cam") { ManualCameraScreen(onBack = { nav.popBackStack() }) }
    }
}
