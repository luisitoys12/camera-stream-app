package tech.estacionkus.camerastream

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit4.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.estacionkus.camerastream.ui.screens.home.HomeScreen
import tech.estacionkus.camerastream.ui.theme.CameraStreamTheme

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createComposeRule()

    @Before fun setup() { hiltRule.inject() }

    @Test fun homeScreen_showsStartStreamButton() {
        composeRule.setContent {
            CameraStreamTheme {
                HomeScreen(onStartStream = {}, onOpenSettings = {}, onOpenMedia = {}, onUpgrade = {})
            }
        }
        composeRule.onNodeWithText("Iniciar Stream").assertIsDisplayed()
    }

    @Test fun homeScreen_showsSrtButton() {
        composeRule.setContent {
            CameraStreamTheme {
                HomeScreen(onStartStream = {}, onOpenSettings = {}, onOpenMedia = {}, onUpgrade = {})
            }
        }
        composeRule.onNodeWithText("Servidor SRT (Pro)").assertIsDisplayed()
    }

    @Test fun homeScreen_startStream_triggersCallback() {
        var called = false
        composeRule.setContent {
            CameraStreamTheme {
                HomeScreen(onStartStream = { called = true }, onOpenSettings = {}, onOpenMedia = {}, onUpgrade = {})
            }
        }
        composeRule.onNodeWithText("Iniciar Stream").performClick()
        assert(called)
    }
}
