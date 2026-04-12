package tech.estacionkus.camerastream.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────────────────────────────────────

data class OnboardingPage(
    val icon: ImageVector,
    val secondaryIcon: ImageVector? = null,
    val accentColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val bullets: List<String> = emptyList()
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.Videocam,
        secondaryIcon = Icons.Default.LiveTv,
        accentColor = Color(0xFFE53935),
        title = "Professional\nLive Streaming",
        subtitle = "Multi-Platform Broadcasting",
        description = "Stream simultaneously to Twitch, YouTube, Kick, TikTok, Facebook and more — all from your phone.",
        bullets = listOf("Up to 6 platforms at once", "Real-time bitrate control", "RTMP & SRT support")
    ),
    OnboardingPage(
        icon = Icons.Default.Dashboard,
        secondaryIcon = Icons.Default.Layers,
        accentColor = Color(0xFF2196F3),
        title = "My Studio\nOverlays & Scenes",
        subtitle = "OBS-Style Scene Management",
        description = "Add text, images, scoreboards, alerts, chat widgets, lower thirds, and more. Switch scenes live.",
        bullets = listOf("10+ overlay types", "Drag to position overlays", "Save & load presets")
    ),
    OnboardingPage(
        icon = Icons.Default.Groups,
        secondaryIcon = Icons.Default.Chat,
        accentColor = Color(0xFF4CAF50),
        title = "Multi-Platform\nChat & Guests",
        subtitle = "Unified Engagement Hub",
        description = "Read and respond to chat from all platforms in one place. Invite video guests via WebRTC.",
        bullets = listOf("Unified chat feed", "WebRTC guest invites", "Chat-to-overlay reactions")
    ),
    OnboardingPage(
        icon = Icons.Default.SportsSoccer,
        secondaryIcon = Icons.Default.EmojiEvents,
        accentColor = Color(0xFFFF9800),
        title = "Sports &\nEvents Mode",
        subtitle = "Live Scoreboard & Game Clock",
        description = "Built-in scoreboard overlay with team colors, game clock, period controls, and event log.",
        bullets = listOf("Soccer, Basketball, Boxing +", "Animated score updates", "Game event logging")
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Animated background gradient that follows page color
        val bgColor = onboardingPages[pagerState.currentPage].accentColor
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bgColor.copy(alpha = 0.12f),
                            Color(0xFF0D0D0D)
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            AnimatedVisibility(
                visible = pagerState.currentPage < onboardingPages.lastIndex,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "Skip",
                            color = Color(0xFF888888),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(
                    page = onboardingPages[pageIndex],
                    isActive = pagerState.currentPage == pageIndex
                )
            }

            // Bottom controls
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = onboardingPages.size,
                accentColor = onboardingPages[pagerState.currentPage].accentColor,
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onFinish = onFinish
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Page Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean
) {
    // Breathing animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "iconBreath")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    // Orbit rotation for secondary icon
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon cluster
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (isActive) iconScale else 1f)
                    .background(page.accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    tint = page.accentColor,
                    modifier = Modifier.size(60.dp)
                )
            }

            // Secondary icon orbiting
            if (page.secondaryIcon != null && isActive) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopEnd)
                            .background(page.accentColor.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            page.secondaryIcon,
                            contentDescription = null,
                            tint = page.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subtitle badge
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(tween(400, 200)) + slideInVertically { 20 },
            exit = fadeOut()
        ) {
            Surface(
                color = page.accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            ) {
                Text(
                    page.subtitle,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    color = page.accentColor,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(tween(400, 300)) + slideInVertically { 30 },
            exit = fadeOut()
        ) {
            Text(
                page.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(tween(400, 400)) + slideInVertically { 30 },
            exit = fadeOut()
        ) {
            Text(
                page.description,
                fontSize = 15.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        if (page.bullets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Bullet list
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(400, 500)) + slideInVertically { 30 },
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    page.bullets.forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(page.accentColor, CircleShape)
                            )
                            Text(
                                bullet,
                                fontSize = 14.sp,
                                color = Color(0xFFBBBBBB)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    accentColor: Color,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animated dots indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = index == currentPage
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "dotWidth$index"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor else Color(0xFF333333)
                        )
                )
            }
        }

        // Action button
        AnimatedContent(
            targetState = currentPage == pageCount - 1,
            transitionSpec = {
                fadeIn(tween(200)) + slideInVertically { 40 } togetherWith fadeOut(tween(150))
            },
            label = "bottomButton"
        ) { isLastPage ->
            if (isLastPage) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Get Started",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
