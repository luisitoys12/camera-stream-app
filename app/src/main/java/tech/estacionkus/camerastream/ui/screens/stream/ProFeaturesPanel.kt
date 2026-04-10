package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.estacionkus.camerastream.domain.features.Feature
import tech.estacionkus.camerastream.domain.features.FeatureManager
import tech.estacionkus.camerastream.domain.features.Tier

data class FeatureButton(
    val feature: Feature,
    val icon: ImageVector,
    val label: String,
    val isActive: Boolean = false
)

@Composable
fun ProFeaturesPanel(
    featureManager: FeatureManager,
    activeFeatures: Set<Feature>,
    onToggleFeature: (Feature) -> Unit,
    onUpgradeTap: (Feature) -> Unit
) {
    val featureButtons = listOf(
        FeatureButton(Feature.SRT_SERVER_LOCAL, Icons.Default.Router, "Servidor SRT"),
        FeatureButton(Feature.SRTLA_BONDING, Icons.Default.SignalCellularAlt, "Bonding"),
        FeatureButton(Feature.CHROMA_KEY, Icons.Default.Layers, "Chroma Key"),
        FeatureButton(Feature.SCENE_SWITCHING, Icons.Default.Theaters, "Escenas"),
        FeatureButton(Feature.LOWER_THIRDS_CUSTOM, Icons.Default.TextFields, "Lower Third"),
        FeatureButton(Feature.SCORE_OVERLAY, Icons.Default.SportsSoccer, "Marcador"),
        FeatureButton(Feature.COUNTDOWN_OVERLAY, Icons.Default.Timer, "Countdown"),
        FeatureButton(Feature.WEBCAM_MODE, Icons.Default.Videocam, "Webcam PC"),
        FeatureButton(Feature.CHAT_MULTI, Icons.Default.Forum, "Chat Multi"),
        FeatureButton(Feature.ALERTS_OVERLAY, Icons.Default.Notifications, "Alertas"),
        FeatureButton(Feature.RECORD_1080P, Icons.Default.FiberManualRecord, "Rec 1080p"),
        FeatureButton(Feature.EXPORT_CLOUD, Icons.Default.CloudUpload, "Export Cloud"),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Funciones Pro",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(featureButtons) { btn ->
                val enabled = featureManager.isEnabled(btn.feature)
                val active  = activeFeatures.contains(btn.feature)
                FeatureTile(
                    icon = btn.icon,
                    label = btn.label,
                    isEnabled = enabled,
                    isActive = active,
                    onClick = {
                        if (enabled) onToggleFeature(btn.feature)
                        else onUpgradeTap(btn.feature)
                    }
                )
            }
        }
    }
}

@Composable
private fun FeatureTile(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        !isEnabled -> Color(0xFF1A1A1A)
        isActive   -> Color(0xFF1A237E)
        else       -> Color(0xFF212121)
    }
    val iconTint = when {
        !isEnabled -> Color(0xFF555555)
        isActive   -> Color(0xFF82B1FF)
        else       -> Color(0xFFBBBBBB)
    }

    Column(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(22.dp))
            if (!isEnabled) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(10.dp).align(Alignment.TopEnd)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label, fontSize = 9.sp,
            color = if (isEnabled) Color.White else Color(0xFF666666),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
