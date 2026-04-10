package com.cushMedia.camerastream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cushMedia.camerastream.ui.navigation.AppNavigation
import com.cushMedia.camerastream.ui.theme.CameraStreamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraStreamTheme {
                AppNavigation()
            }
        }
    }
}
