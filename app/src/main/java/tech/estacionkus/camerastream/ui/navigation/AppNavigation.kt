package tech.estacionkus.camerastream.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tech.estacionkus.camerastream.ui.screens.auth.AuthScreen
import tech.estacionkus.camerastream.ui.screens.chat.UnifiedChatScreen
import tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersScreen
import tech.estacionkus.camerastream.ui.screens.guest.GuestModeScreen
import tech.estacionkus.camerastream.ui.screens.health.StreamHealthScreen
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.screens.onboarding.OnboardingScreen
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraScreen
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerScreen
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeScreen
import tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastScreen
import tech.estacionkus.camerastream.ui.screens.scenes.SceneManagerScreen
import tech.estacionkus.camerastream.ui.screens.settings.SettingsScreen
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeScreen
import tech.estacionkus.camerastream.ui.screens.stream.StreamScreen
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("stream", "Stream", Icons.Default.Videocam),
    BottomNavItem("studio", "Studio", Icons.Default.Dashboard),
    BottomNavItem("chat", "Chat", Icons.Default.Chat),
    BottomNavItem("settings", "Settings", Icons.Default.Settings)
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AppNavigation(showOnboarding: Boolean = false) {
    val nav = rememberNavController()
    val startDest = if (showOnboarding) "onboarding" else "home"
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar(
                    containerColor = Color(0xFF1A1A2E),
                    contentColor = Color.White
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (currentRoute == item.route) Color(0xFF64B5F6) else Color.White.copy(0.5f)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (currentRoute == item.route) Color(0xFF64B5F6) else Color.White.copy(0.5f)
                                )
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    nav.navigate(item.route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF64B5F6).copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
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
                    onOpenManualCam = { nav.navigate("manual_cam") },
                    onOpenFilters = { nav.navigate("filters") },
                    onOpenRadio = { nav.navigate("radio") }
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
            composable("filters") {
                CameraFiltersScreen(
                    onBack = { nav.popBackStack() },
                    onUpgrade = { nav.navigate("upgrade") }
                )
            }
            composable("radio") {
                RadioBroadcastScreen(
                    onBack = { nav.popBackStack() },
                    onUpgrade = { nav.navigate("upgrade") }
                )
            }
        }
    }
}
