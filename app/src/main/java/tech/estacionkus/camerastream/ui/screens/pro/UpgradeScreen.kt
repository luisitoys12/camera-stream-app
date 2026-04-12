package tech.estacionkus.camerastream.ui.screens.pro

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.billing.PlanTier

private val ColorFree = Color(0xFF1565C0)
private val ColorPro = Color(0xFFE53935)
private val ColorAgency = Color(0xFF6A1B9A)

private data class FeatureRow(
    val name: String,
    val free: String,
    val pro: String,
    val agency: String
)

private val featureRows = listOf(
    FeatureRow("Max destinations", "1", "5", "Unlimited"),
    FeatureRow("Resolution", "720p", "1080p", "4K"),
    FeatureRow("FPS", "30", "60", "60"),
    FeatureRow("Overlays", "-", "check", "check"),
    FeatureRow("Scenes", "-", "check", "check"),
    FeatureRow("Manual camera", "-", "check", "check"),
    FeatureRow("Multi-chat", "-", "check", "check"),
    FeatureRow("SRT server", "-", "check", "check"),
    FeatureRow("Disconnect protection", "-", "check", "check"),
    FeatureRow("Guest mode", "-", "-", "check"),
    FeatureRow("Beauty filter", "-", "check", "check"),
    FeatureRow("Sports mode", "-", "-", "check"),
    FeatureRow("Watermark removal", "-", "check", "check"),
    FeatureRow("Stream health dashboard", "-", "check", "check")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    onBack: () -> Unit,
    viewModel: UpgradeViewModel = hiltViewModel()
) {
    val currentPlan by viewModel.currentPlan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val activity = LocalContext.current as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Upgrade to unlock all features",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "7-day free trial on all paid plans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Plan cards row (horizontally scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlanCard(
                        tier = PlanTier.FREE,
                        color = ColorFree,
                        isHighlighted = false,
                        isCurrent = currentPlan == PlanTier.FREE,
                        onSubscribe = {}
                    )
                    PlanCard(
                        tier = PlanTier.PRO,
                        color = ColorPro,
                        isHighlighted = true,
                        isCurrent = currentPlan == PlanTier.PRO,
                        onSubscribe = {
                            activity?.let { viewModel.launchPurchase(it, PlanTier.PRO) }
                        }
                    )
                    PlanCard(
                        tier = PlanTier.AGENCY,
                        color = ColorAgency,
                        isHighlighted = false,
                        isCurrent = currentPlan == PlanTier.AGENCY,
                        onSubscribe = {
                            activity?.let { viewModel.launchPurchase(it, PlanTier.AGENCY) }
                        }
                    )
                }

                // Feature comparison table
                Text(
                    text = "Feature Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                FeatureComparisonTable()

                // Restore purchases
                TextButton(
                    onClick = { viewModel.restorePurchases() },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Restore Purchases")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Error snackbar
        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                },
                title = { Text("Error") },
                text = { Text(message) }
            )
        }
    }
}

@Composable
private fun PlanCard(
    tier: PlanTier,
    color: Color,
    isHighlighted: Boolean,
    isCurrent: Boolean,
    onSubscribe: () -> Unit
) {
    val cardWidth = 180.dp
    val borderStroke = if (isHighlighted) {
        BorderStroke(2.dp, color)
    } else {
        null
    }

    Card(
        modifier = Modifier.width(cardWidth),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                color.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Most Popular" badge
            if (isHighlighted) {
                Surface(
                    color = color,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "Most Popular",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Plan name
            Text(
                text = tier.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )

            // Price
            Text(
                text = tier.priceDisplay,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Trial note for paid plans
            if (tier != PlanTier.FREE) {
                Text(
                    text = "7-day free trial",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick feature highlights
            val highlights = when (tier) {
                PlanTier.FREE -> listOf("1 destination", "720p 30fps", "Basic streaming")
                PlanTier.PRO -> listOf("5 destinations", "1080p 60fps", "Overlays & scenes", "SRT server")
                PlanTier.AGENCY -> listOf("Unlimited destinations", "4K 60fps", "Guest mode", "Sports mode")
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = color.copy(alpha = 0.3f)
            )

            highlights.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = feature,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subscribe / Current plan button
            if (isCurrent) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                ) {
                    Text("Current Plan", fontSize = 13.sp)
                }
            } else if (tier == PlanTier.FREE) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                ) {
                    Text("Free", fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text("Subscribe", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun FeatureComparisonTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Feature",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1.4f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Free",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ColorFree,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "Pro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ColorPro,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "Agency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ColorAgency,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
            }

            Divider()

            // Feature rows
            featureRows.forEachIndexed { index, feature ->
                if (index > 0) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                FeatureComparisonRow(feature)
            }
        }
    }
}

@Composable
private fun FeatureComparisonRow(feature: FeatureRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature.name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.4f)
        )
        FeatureCell(value = feature.free, color = ColorFree, modifier = Modifier.weight(0.7f))
        FeatureCell(value = feature.pro, color = ColorPro, modifier = Modifier.weight(0.7f))
        FeatureCell(value = feature.agency, color = ColorAgency, modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun FeatureCell(value: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (value) {
            "check" -> {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Included",
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            "-" -> {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Not included",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
            else -> {
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
