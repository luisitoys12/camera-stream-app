package tech.estacionkus.camerastream.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.billing.PlanTier
import tech.estacionkus.camerastream.domain.model.CameraFilter
import tech.estacionkus.camerastream.domain.model.CameraFilters
import tech.estacionkus.camerastream.domain.model.FilterCategory
import tech.estacionkus.camerastream.domain.model.FilterTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraFiltersScreen(
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: CameraFiltersViewModel = hiltViewModel()
) {
    val currentFilter by viewModel.currentFilter.collectAsState()
    val currentPlan by viewModel.currentPlan.collectAsState()
    var selectedCategory by remember { mutableStateOf(FilterCategory.COLOR) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Filters") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current filter display
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated camera preview with filter applied
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF2C3E50),
                                        Color(0xFF1A1A2E)
                                    )
                                )
                            )
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            tint = Color.White.copy(0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Filter: ${currentFilter?.name ?: "None"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Preview area",
                            color = Color.White.copy(0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = FilterCategory.entries.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                FilterCategory.entries.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                category.displayName,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Filter thumbnails strip
            Text(
                "${CameraFilters.getByCategory(selectedCategory).size} filters",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CameraFilters.getByCategory(selectedCategory).forEach { filter ->
                    FilterThumbnail(
                        filter = filter,
                        isSelected = currentFilter?.id == filter.id,
                        isLocked = !viewModel.canUseFilter(filter),
                        onClick = {
                            if (viewModel.canUseFilter(filter)) {
                                viewModel.selectFilter(filter)
                            } else {
                                onUpgrade()
                            }
                        }
                    )
                }
            }

            // Filter info
            Spacer(Modifier.weight(1f))

            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${CameraFilters.allFilters.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val unlocked = CameraFilters.allFilters.count { viewModel.canUseFilter(it) }
                        Text("$unlocked", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF4CAF50))
                        Text("Unlocked", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val locked = CameraFilters.allFilters.count { !viewModel.canUseFilter(it) }
                        Text("$locked", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFF9800))
                        Text("Locked", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterThumbnail(
    filter: CameraFilter,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val colorMap = mapOf(
        FilterCategory.BEAUTY to Color(0xFFE91E63),
        FilterCategory.COLOR to Color(0xFF2196F3),
        FilterCategory.MOOD to Color(0xFF9C27B0),
        FilterCategory.FUN to Color(0xFFFF9800),
        FilterCategory.PRO to Color(0xFF607D8B)
    )
    val accentColor = colorMap[filter.category] ?: Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f))
                .then(
                    if (isSelected) Modifier.border(3.dp, Color(0xFF2196F3), CircleShape)
                    else Modifier
                )
        ) {
            // Filter color hint
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accentColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.2f))
                        )
                    )
            )
            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            filter.name,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface
        )
        if (isLocked) {
            Text(
                if (filter.tier == FilterTier.AGENCY) "AGENCY" else "PRO",
                fontSize = 8.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
