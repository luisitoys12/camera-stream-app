package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    FeatureRow("Camera Filters", "3", "20+", "30+"),
    FeatureRow("Overlays", "-", "check", "check"),
    FeatureRow("Scenes", "-", "check", "check"),
    FeatureRow("Manual camera", "-", "check", "check"),
    FeatureRow("Multi-chat", "-", "check", "check"),
    FeatureRow("SRT server", "-", "check", "check"),
    FeatureRow("Disconnect protection", "-", "check", "check"),
    FeatureRow("Guest mode", "-", "-", "check"),
    FeatureRow("Beauty filter", "-", "check", "check"),
    FeatureRow("Sports mode", "-", "-", "check"),
    FeatureRow("Radio Broadcast", "-", "-", "check"),
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
    val successMessage by viewModel.successMessage.collectAsState()
    val licenseKey by viewModel.licenseKey.collectAsState()

    var licenseKeyInput by remember { mutableStateOf("") }
    var couponInput by remember { mutableStateOf("") }
    var showCouponField by remember { mutableStateOf(false) }

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
                Text(
                    text = "Upgrade to unlock all features",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Current plan status
                if (currentPlan != PlanTier.FREE) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (currentPlan) {
                                PlanTier.PRO -> ColorPro.copy(alpha = 0.1f)
                                PlanTier.AGENCY -> ColorAgency.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Plan ${currentPlan.displayName} activo",
                                    fontWeight = FontWeight.Bold
                                )
                                licenseKey?.let {
                                    if (it.isNotEmpty()) {
                                        Text(
                                            "Key: ${it.take(8)}...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { viewModel.deactivatePlan() }) {
                                Text("Desactivar", color = Color(0xFFE53935))
                            }
                        }
                    }
                }

                // Plan cards row
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
                        onSubscribe = { viewModel.openStripeCheckout(PlanTier.PRO) }
                    )
                    PlanCard(
                        tier = PlanTier.AGENCY,
                        color = ColorAgency,
                        isHighlighted = false,
                        isCurrent = currentPlan == PlanTier.AGENCY,
                        onSubscribe = { viewModel.openStripeCheckout(PlanTier.AGENCY) }
                    )
                }

                // License Key Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Activar Licencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Ingresa tu License Key despues del pago",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = licenseKeyInput,
                            onValueChange = { licenseKeyInput = it },
                            label = { Text("License Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            placeholder = { Text("XXXX-XXXX-XXXX-XXXX") }
                        )
                        Button(
                            onClick = { viewModel.activateLicenseKey(licenseKeyInput) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = licenseKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Activar Licencia")
                        }
                        TextButton(
                            onClick = { viewModel.openStripeCheckout(PlanTier.PRO) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No tienes key? Compra tu plan")
                        }
                    }
                }

                // Coupon Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tienes un cupon?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showCouponField = !showCouponField }) {
                                Icon(
                                    if (showCouponField) Icons.Default.ExpandLess
                                    else Icons.Default.ExpandMore,
                                    null
                                )
                            }
                        }
                        if (showCouponField) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it },
                                label = { Text("Codigo de cupon") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("ej: creador100") }
                            )
                            Button(
                                onClick = { viewModel.activateCoupon(couponInput) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = couponInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Icon(Icons.Default.CardGiftcard, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Aplicar Cupon")
                            }
                        }
                    }
                }

                // Feature comparison
                Text(
                    text = "Feature Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                FeatureComparisonTable()

                Spacer(modifier = Modifier.height(16.dp))
            }

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

        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                },
                title = { Text("Error") },
                text = { Text(message) }
            )
        }

        successMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearSuccess() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearSuccess() }) { Text("OK") }
                },
                icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50)) },
                title = { Text("Exito") },
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
    val borderStroke = if (isHighlighted) BorderStroke(2.dp, color) else null

    Card(
        modifier = Modifier.width(cardWidth),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) color.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surface
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
            if (isHighlighted) {
                Surface(color = color, shape = MaterialTheme.shapes.small) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("Most Popular", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(text = tier.displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(text = tier.priceDisplay, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            val highlights = when (tier) {
                PlanTier.FREE -> listOf("1 destination", "720p 30fps", "3 filters")
                PlanTier.PRO -> listOf("5 destinations", "1080p 60fps", "20+ filters", "SRT server")
                PlanTier.AGENCY -> listOf("Unlimited dest.", "4K 60fps", "30+ filters", "Radio mode")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = color.copy(alpha = 0.3f))

            highlights.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(14.dp))
                    Text(text = feature, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                    Text("Comprar", fontSize = 13.sp)
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feature", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.4f))
                Text("Free", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorFree, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                Text("Pro", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorPro, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                Text("Agency", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorAgency, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
            }
            HorizontalDivider()
            featureRows.forEachIndexed { index, feature ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                FeatureComparisonRow(feature)
            }
        }
    }
}

@Composable
private fun FeatureComparisonRow(feature: FeatureRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(feature.name, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
        FeatureCell(value = feature.free, color = ColorFree, modifier = Modifier.weight(0.7f))
        FeatureCell(value = feature.pro, color = ColorPro, modifier = Modifier.weight(0.7f))
        FeatureCell(value = feature.agency, color = ColorAgency, modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun FeatureCell(value: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (value) {
            "check" -> Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(22.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, "Included", tint = color, modifier = Modifier.size(14.dp))
                }
            }
            "-" -> Icon(Icons.Default.Close, "Not included", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            else -> Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color, textAlign = TextAlign.Center)
        }
    }
}
