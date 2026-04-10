package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.auth.auth
import tech.estacionkus.camerastream.data.auth.Supabase
import tech.estacionkus.camerastream.ui.screens.auth.AuthScreen
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraScreen
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerScreen
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Stream : Screen("stream")
    object Settings : Screen("settings")
    object Upgrade : Screen("upgrade")
    object SrtServer : Screen("srt_server")
    object ManualCamera : Screen("manual_camera")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isLoggedIn = Supabase.client.auth.currentUserOrNull() != null
    val start = if (isLoggedIn) Screen.Home.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = start) {
        composable(Screen.Auth.route) {
            AuthScreen(onAuthenticated = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onStartStream = { navController.navigate(Screen.Stream.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenMedia = { navController.navigate(Screen.SrtServer.route) },
                onUpgrade = { navController.navigate(Screen.Upgrade.route) }
            )
        }
        composable(Screen.Stream.route) {
            StreamScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Upgrade.route) {
            UpgradeScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SrtServer.route) {
            SrtServerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ManualCamera.route) {
            ManualCameraScreen(onBack = { navController.popBackStack() })
        }
    }
}
