package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.estacionkus.camerastream.data.auth.Supabase
import tech.estacionkus.camerastream.ui.screens.auth.AuthScreen
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryScreen
import io.github.jan.supabase.auth.auth

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Stream : Screen("stream")
    object Settings : Screen("settings")
    object MediaLibrary : Screen("media_library")
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
