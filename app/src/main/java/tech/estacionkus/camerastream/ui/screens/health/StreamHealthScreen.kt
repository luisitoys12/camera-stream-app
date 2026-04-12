package tech.estacionkus.camerastream.ui.screens.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.streaming.HealthGrade
import tech.estacionkus.camerastream.streaming.HealthSnapshot
import tech.estacionkus.camerastream.streaming.StreamHealthMonitor
import tech.estacionkus.camerastream.streaming.StreamSummary
import tech.estacionkus.camerastream.ui.theme.*
import javax.inject.Inject

// -- Theme colors for health grades -----------------------------------------

private fun HealthGrade.uiColor(): Color = when (this) {
    HealthGrade.EXCELLENT -> Color(0xFF4CAF50)
    HealthGrade.GOOD      -> Color(0xFF2196F3)
    HealthGrade.FAIR      -> Color(0xFFFF9800)
    HealthGrade.POOR      -> CameraRed
}

// -- ViewModel --------------------------------------------------------------

data class HealthUiState(
    val currentHealth: HealthSnapshot  = HealthSnapshot(),
    val healthGrade: HealthGrade       = HealthGrade.GOOD,
    val bitrateHistory: List<Int>      = emptyList(),
    val fpsHistory: List<Int>          = emptyList(),
    val dropHistory: List<Int>         = emptyList(),
    val suggestions: List<String>      = emptyList(),
    val summary: StreamSummary?        = null,
    val canUseStreamHealth: Boolean    = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthMonitor: StreamHealthMonitor,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            healthMonitor.currentHealth.collect { snapshot ->
                _uiState.update { it.copy(currentHealth = snapshot) }
            }
        }
        viewModelScope.launch {
            healthMonitor.healthGrade.collect { grade ->
                _uiState.update { it.copy(healthGrade = grade) }
            }
        }
        viewModelScope.launch {
            healthMonitor.bitrateHistory.collect { history ->
                _uiState.update { it.copy(bitrateHistory = history) }
            }
        }
        viewModelScope.launch {
            healthMonitor.fpsHistory.collect { history ->
                _uiState.update { it.copy(fpsHistory = history) }
            }
        }
        viewModelScope.launch {
            healthMonitor.dropHistory.collect { history ->
                _uiState.update { it.copy(dropHistory = history) }
            }
        }
        viewModelScope.launch {
            healthMonitor.suggestions.collect { list ->
                _uiState.update { it.copy(suggestions = list) }
            }
        }
        viewModelScope.launch {
            healthMonitor.summary.collect { s ->
                _uiState.update { it.copy(summary = s) }
            }
        }
        _uiState.update { it.copy(canUseStreamHealth = featureGate.canStreamHealth()) }
    }
}

// -- Screen -----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamHealthScreen(
    onBack: () -> Unit,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface800,
        topBar = {
            TopAppBar(
                title = { Text("Stream Health", color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface700)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feature gate banner
            if (!state.canUseStreamHealth) {
                item { ProBanner("Upgrade to Pro to unlock Stream Health monitoring") }
            }

            // Health grade card
            item { HealthGradeCard(state.healthGrade) }

            // Real-time stats
            item { StatsCards(state.currentHealth) }

            // Bitrate graph
            item {
                LineChart(
                    title = "Bitrate (kbps)",
                    data = state.bitrateHistory,
                    lineColor = Color(0xFF42A5F5),
                    fillAlpha = 0.15f
                )
            }

            // FPS graph
            item {
                LineChart(
                    title = "FPS",
                    data = state.fpsHistory,
                    lineColor = Color(0xFF66BB6A),
                    fillAlpha = 0.15f
                )
            }

            // Frame drops indicator
            item { FrameDropsCard(state.currentHealth.droppedFrames, state.dropHistory) }

            // Network latency
            item { LatencyCard(rttMs = state.currentHealth.rttMs) }

            // Suggestions
            if (state.suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                items(state.suggestions) { suggestion ->
                    SuggestionRow(suggestion)
                }
            }

            // Post-stream summary
            val summary = state.summary
            if (summary != null) {
                item { PostStreamSummaryCard(summary) }
            }
        }
    }
}

// -- Health grade card ------------------------------------------------------

@Composable
private fun HealthGradeCard(grade: HealthGrade) {
    val gradeColor = grade.uiColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = gradeColor.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Stream Health",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceMuted
                )
                Text(
                    text = grade.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
                Text(
                    text = when (grade) {
                        HealthGrade.EXCELLENT -> "Your stream looks great!"
                        HealthGrade.GOOD      -> "Stream is stable"
                        HealthGrade.FAIR      -> "Some issues detected"
                        HealthGrade.POOR      -> "Check network immediately"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }

            // Color circle indicator
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(gradeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (grade) {
                        HealthGrade.EXCELLENT -> Icons.Default.CheckCircle
                        HealthGrade.GOOD      -> Icons.Default.ThumbUp
                        HealthGrade.FAIR      -> Icons.Default.Warning
                        HealthGrade.POOR      -> Icons.Default.Error
                    },
                    contentDescription = grade.label,
                    tint = gradeColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// -- Stats cards ------------------------------------------------------------

@Composable
private fun StatsCards(health: HealthSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Bitrate",
                value = "${health.bitrateKbps}",
                unit = "kbps",
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF42A5F5),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "FPS",
                value = "${health.fps}",
                unit = "fps",
                icon = Icons.Default.Videocam,
                iconColor = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Frame Drops",
                value = "${health.droppedFrames}",
                unit = "frames",
                icon = Icons.Default.Warning,
                iconColor = if (health.droppedFrames > 0) Color(0xFFFF9800) else Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Latency",
                value = "${health.rttMs}",
                unit = "ms",
                icon = Icons.Default.NetworkCheck,
                iconColor = when {
                    health.rttMs > 500 -> CameraRed
                    health.rttMs > 200 -> Color(0xFFFF9800)
                    else               -> Color(0xFF66BB6A)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = OnSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = iconColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// -- Line chart with Canvas -------------------------------------------------

@Composable
private fun LineChart(
    title: String,
    data: List<Int>,
    lineColor: Color,
    fillAlpha: Float = 0.1f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = if (data.isNotEmpty()) "max: ${data.max()}" else "No data",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
            Spacer(Modifier.height(8.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for data...",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val maxVal = data.max().coerceAtLeast(1).toFloat()
                    val pts = data.size
                    if (pts < 2) return@Canvas

                    val stepX = size.width / (pts - 1).toFloat()

                    // Build line path
                    val linePath = Path().apply {
                        data.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = size.height - (v / maxVal) * size.height
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    // Fill path
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(fillPath, color = lineColor.copy(alpha = fillAlpha))

                    // Draw line
                    drawPath(
                        linePath,
                        color = lineColor,
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )

                    // Draw current value dot
                    val lastX = (pts - 1) * stepX
                    val lastY = size.height - (data.last() / maxVal) * size.height
                    drawCircle(color = lineColor, radius = 4f, center = Offset(lastX, lastY))
                    drawCircle(
                        color = lineColor.copy(alpha = 0.3f),
                        radius = 8f,
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }
    }
}

// -- Frame drops indicator --------------------------------------------------

@Composable
private fun FrameDropsCard(droppedFrames: Int, history: List<Int>) {
    val severity = when {
        droppedFrames == 0 -> "None"
        droppedFrames < 5  -> "Low"
        droppedFrames < 15 -> "Medium"
        else               -> "High"
    }
    val severityColor = when (severity) {
        "None"   -> Color(0xFF4CAF50)
        "Low"    -> Color(0xFFFF9800)
        "Medium" -> CameraRed.copy(alpha = 0.8f)
        else     -> CameraRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Frame Drops", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$droppedFrames",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(" frames", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                }
            }
            Surface(
                color = severityColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = severity,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = severityColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// -- Network latency card ---------------------------------------------------

@Composable
private fun LatencyCard(rttMs: Int) {
    val color = when {
        rttMs == 0   -> OnSurfaceMuted
        rttMs < 100  -> Color(0xFF4CAF50)
        rttMs < 300  -> Color(0xFFFF9800)
        else         -> CameraRed
    }
    val label = when {
        rttMs == 0  -> "No data"
        rttMs < 100 -> "Excellent"
        rttMs < 300 -> "Fair"
        else        -> "High"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Column {
                    Text("Network Latency (RTT)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${rttMs}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("ms", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                    }
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -- Suggestion row ---------------------------------------------------------

@Composable
private fun SuggestionRow(suggestion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface
            )
        }
    }
}

// -- Post-stream summary ----------------------------------------------------

@Composable
private fun PostStreamSummaryCard(summary: StreamSummary) {
    val gradeColor = summary.overallGrade.uiColor()
    val durationMin = summary.totalDurationMs / 60_000

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stream Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Surface(
                    color = gradeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = summary.overallGrade.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Surface600)
            Spacer(Modifier.height(8.dp))

            SummaryRow("Duration",       "${durationMin} min")
            SummaryRow("Avg Bitrate",    "${summary.avgBitrateKbps} kbps")
            SummaryRow("Max Bitrate",    "${summary.maxBitrateKbps} kbps")
            SummaryRow("Min Bitrate",    "${summary.minBitrateKbps} kbps")
            SummaryRow("Avg FPS",        "${summary.avgFps} fps")
            SummaryRow("Dropped Frames", "${summary.totalDroppedFrames}")
            SummaryRow("Avg Latency",    "${summary.avgRttMs} ms")
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = OnSurface)
    }
}

// -- Pro banner -------------------------------------------------------------

@Composable
private fun ProBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A237E).copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(18.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = OnSurface)
        }
    }
}
