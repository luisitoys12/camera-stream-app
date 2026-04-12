package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.estacionkus.camerastream.ui.screens.auth.AuthScreen
import tech.estacionkus.camerastream.ui.screens.chat.UnifiedChatScreen
import tech.estacionkus.camerastream.ui.screens.guest.GuestModeScreen
import tech.estacionkus.camerastream.ui.screens.health.StreamHealthScreen
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.onboarding.OnboardingScreen
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraScreen
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerScreen
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeScreen
import tech.estacionkus.camerastream.ui.screens.scenes.SceneManagerScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioScreen

@Composable
fun AppNavigation(showOnboarding: Boolean = false) {
    val nav = rememberNavController()
    val startDest = if (showOnboarding) "onboarding" else "home"

    NavHost(navController = nav, startDestination = startDest) {
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                nav.navigate("home") { popUpTo("onboarding") { inclusive = true } }
            })
        }
        composable("auth") {
            AuthScreen(onAuthenticated = {
                nav.navigate("home") { popUpTo("auth") { inclusive = true } }
            })
        }
        composable("home") {
            HomeScreen(
                onStartStream = { nav.navigate("stream") },
                onOpenSettings = { nav.navigate("settings") },
                onOpenMedia = { nav.navigate("srt") },
                onUpgrade = { nav.navigate("upgrade") },
                onOpenStudio = { nav.navigate("studio") },
                onOpenScenes = { nav.navigate("scenes") },
                onOpenChat = { nav.navigate("chat") },
                onOpenSports = { nav.navigate("sports") },
                onOpenHealth = { nav.navigate("health") },
                onOpenGuest = { nav.navigate("guest") },
                onOpenManualCam = { nav.navigate("manual_cam") }
            )
        }
        composable("stream") { StreamScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable("upgrade") { UpgradeScreen(onBack = { nav.popBackStack() }) }
        composable("srt") { SrtServerScreen(onBack = { nav.popBackStack() }) }
        composable("manual_cam") { ManualCameraScreen(onBack = { nav.popBackStack() }) }
        composable("studio") { MyStudioScreen(onBack = { nav.popBackStack() }) }
        composable("scenes") { SceneManagerScreen(onBack = { nav.popBackStack() }) }
        composable("chat") { UnifiedChatScreen(onBack = { nav.popBackStack() }) }
        composable("sports") { SportsModeScreen(onBack = { nav.popBackStack() }) }
        composable("health") { StreamHealthScreen(onBack = { nav.popBackStack() }) }
        composable("guest") { GuestModeScreen(onBack = { nav.popBackStack() }) }
    }
}
