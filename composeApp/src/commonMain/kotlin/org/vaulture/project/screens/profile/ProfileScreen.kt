package org.vaulture.project.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.services.AuthService
import org.vaulture.project.components.AIPrivacyCard
import org.vaulture.project.components.ProfileAvatar
import org.vaulture.project.components.PulseBadgeCard
import org.vaulture.project.components.SignOutButton
import org.vaulture.project.viewmodels.WellnessViewModel
import org.vaulture.project.data.models.WellnessStats
import vaulture.composeapp.generated.resources.*

@Composable
fun ProfileScreen(
    authService: AuthService,
    wellnessViewModel: WellnessViewModel, // Injected via AppNavigation
    onSignOut: () -> Unit,
) {
    val statsState by wellnessViewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth > 920.dp

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = { fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring()) },
            label = "ResponsiveProfileLayout"
        ) { expanded ->
            if (expanded) {
                ProfileScreenExpanded(
                    authService = authService,
                    stats = statsState.stats,
                    onSignOut = onSignOut
                )
            } else {
                ProfileScreenCompact(
                    authService = authService,
                    stats = statsState.stats,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

// =================================================================================
// Compact Layout (Mobile)
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenCompact(
    authService: AuthService,
    stats: WellnessStats,
    onSignOut: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    val user by authService.currentUser.collectAsState(null)
    val lazyListState = rememberLazyListState()
    val bannerHeight = 200.dp
    val bannerHeightPx = with(LocalDensity.current) { bannerHeight.toPx() }
    val avatarInitialSize = 110.dp
    val avatarFinalSize = 40.dp

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding()),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(bannerHeight + avatarInitialSize / 2)) {
                    AsyncImage(
                        model = "https://images.pexels.com/photos/1231265/pexels-photo-1231265.jpeg",
                        contentDescription = "Wellness banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(bannerHeight).graphicsLayer {
                            val scrollOffset = if (lazyListState.firstVisibleItemIndex == 0) lazyListState.firstVisibleItemScrollOffset.toFloat() else bannerHeightPx
                            alpha = 1f - (scrollOffset / bannerHeightPx).coerceIn(0f, 1f)
                        }
                    )
                    Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent))))

                    val scrollOffset = if (lazyListState.firstVisibleItemIndex == 0) lazyListState.firstVisibleItemScrollOffset.toFloat() else bannerHeightPx
                    val collapsedPercentage = (scrollOffset / (bannerHeightPx - (avatarInitialSize.value / 2))).coerceIn(0f, 1f)
                    val avatarSize = lerp(avatarInitialSize, avatarFinalSize, collapsedPercentage)
                    val avatarY = (bannerHeight - (avatarInitialSize / 2))

                    Box(Modifier.padding(top = avatarY).size(avatarSize).align(Alignment.TopCenter)) {
                        ProfileAvatar(user?.photoUrl, "Profile")
                    }
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                    Text(user?.displayName ?: "Guest User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                RealWellnessStatsRow(stats)
                Spacer(Modifier.height(32.dp))
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PulseBadgeCard(streak = stats.currentStreak)
                    AIPrivacyCard()
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                SignOutButton(onSignOut)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// =================================================================================
// Expanded Layout (Web/Tablet) - Perfected Three-Pane Dashboard
// =================================================================================

@Composable
private fun ProfileScreenExpanded(
    authService: AuthService,
    stats: WellnessStats,
    onSignOut: () -> Unit
) {
    val user by authService.currentUser.collectAsState(null)
    var selectedRailItem by remember { mutableStateOf("Account") }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // --- PANEL 1: NAVIGATION RAIL ---
        NavigationRail(
            header = {
                Icon(
                    painterResource(Res.drawable.mindset_pulse_logo),
                    null,
                    modifier = Modifier.padding(vertical = 24.dp).size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationRailItem(
                selected = selectedRailItem == "Account",
                onClick = { selectedRailItem = "Account" },
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text("Account") }
            )
            NavigationRailItem(
                selected = selectedRailItem == "Insights",
                onClick = { selectedRailItem = "Insights" },
                icon = { Icon(Icons.Default.AutoAwesome, null) },
                label = { Text("Insights") }
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSignOut, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
            }
        }

        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // --- PANEL 2: MAIN CONTENT (Identity & Progress) ---
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 48.dp),
            contentPadding = PaddingValues(vertical = 64.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(user?.photoUrl, "User", Modifier.size(120.dp))
                    Spacer(Modifier.width(32.dp))
                    Column {
                        Text(user?.displayName ?: "User", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text(user?.email ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(48.dp))
            }

            item {
                Text("Wellness Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                RealWellnessStatsRow(stats)
                Spacer(Modifier.height(48.dp))
            }

            item {
                Text("Your Resilience Journey", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                PulseBadgeCard(streak = stats.currentStreak)
            }
        }

        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // --- PANEL 3: THE VAULT (Responsible AI & Settings) ---
        Column(
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("The Mindset Vault", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            AIPrivacyCard()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Investment", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("${stats.totalMinutes} Mindful Minutes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Saved across all your devices.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { /* Navigate to Security Rules Detail */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Shield, null)
                Spacer(Modifier.width(8.dp))
                Text("Data Security Settings")
            }
        }
    }
}

// --- HELPER COMPONENTS WITH REAL DATA ---

@Composable
fun RealWellnessStatsRow(stats: WellnessStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(label = "Daily Goal", value = "${stats.sessionsToday}/5", modifier = Modifier.weight(1f), icon = Icons.Default.BatteryChargingFull)
        StatBox(label = "Streak", value = "${stats.currentStreak}d", modifier = Modifier.weight(1f), icon = Icons.Default.LocalFireDepartment)
        StatBox(label = "Total Points", value = "${stats.resiliencePoints}", modifier = Modifier.weight(1f), icon = Icons.Default.EmojiEvents)
    }
}

@Composable
private fun StatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
