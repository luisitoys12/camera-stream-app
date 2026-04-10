package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stream : Screen("stream")
    object Settings : Screen("settings")
    object MediaLibrary : Screen("media_library")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartStream = { navController.navigate(Screen.Stream.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenMedia = { navController.navigate(Screen.MediaLibrary.route) }
            )
        }
        composable(Screen.Stream.route) {
            StreamScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.MediaLibrary.route) {
            MediaLibraryScreen(onBack = { navController.popBackStack() })
        }
    }
}
