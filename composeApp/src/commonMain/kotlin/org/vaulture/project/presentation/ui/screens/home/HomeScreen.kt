package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.vaulture.project.data.remote.AuthService
import org.vaulture.project.domain.model.RhythmTrack
import org.vaulture.project.domain.model.WellnessType
import org.vaulture.project.domain.engine.CheckInResult
import org.vaulture.project.presentation.navigation.Routes
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.AIPrivacyCard
import org.vaulture.project.presentation.ui.components.CardCarousel
import org.vaulture.project.presentation.ui.components.EmptyStateHero
import org.vaulture.project.presentation.ui.components.InsightHeroCard
import org.vaulture.project.presentation.ui.components.ProfileAvatar
import org.vaulture.project.presentation.ui.components.PulseBadgeCard
import org.vaulture.project.presentation.ui.components.PulseFab
import org.vaulture.project.presentation.ui.components.RhythmItem
import org.vaulture.project.presentation.ui.components.SectionHeader
import org.vaulture.project.presentation.ui.components.ShimmerRhythmItem
import org.vaulture.project.presentation.ui.components.StreakBanner
import org.vaulture.project.presentation.ui.components.StressTrendChart
import org.vaulture.project.presentation.ui.components.WellnessActionItem
import org.vaulture.project.presentation.ui.components.WellnessStatsRow
import org.vaulture.project.presentation.utils.rememberKmpAudioPlayer
import org.vaulture.project.presentation.viewmodels.CheckInViewModel
import org.vaulture.project.presentation.viewmodels.RhythmViewModel
import org.vaulture.project.presentation.viewmodels.WellnessViewModel
import kotlin.time.Clock

val ColorStable = Color(0xFF4CAF50)       // Green
val ColorMild = Color(0xFFFFC107)         // Amber
val ColorHigh = Color(0xFFFF5722)         // Deep Orange
val ColorBurnout = Color(0xFFD32F2F)      // Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authService: AuthService,
    onNavigateToCheckIn: () -> Unit,
    navController: NavController,
    wellnessViewModel: WellnessViewModel,
    onSignOut: () -> Unit
) {
    val user by authService.currentUser.collectAsState(null)
    val checkInViewModel = remember { CheckInViewModel() }
    val kmpAudioPlayer = rememberKmpAudioPlayer()
    val rhythmViewModel = remember(kmpAudioPlayer) {
        RhythmViewModel(audioPlayer = kmpAudioPlayer)
    }
    //val wellnessViewModel = remember { WellnessViewModel() }
    var showCheckInModal by remember { mutableStateOf(false) }
    val checkInState by checkInViewModel.uiState.collectAsState()
    val latestPersistentResult by checkInViewModel.latestResult.collectAsState()

    val displayResult = latestPersistentResult ?: checkInState.result
    val hasResult = displayResult != null
    LaunchedEffect(latestPersistentResult) {
        if (latestPersistentResult != null && checkInState.result == null) {
            checkInViewModel.syncResult(latestPersistentResult!!)
        }
    }

    LaunchedEffect(latestPersistentResult, checkInState.result) {
        println("DEBUG: DB Result = ${latestPersistentResult?.aiInsight?.take(20)}... (Score: ${latestPersistentResult?.score})")
        println("DEBUG: Local Result = ${checkInState.result?.aiInsight?.take(20)}... (Score: ${checkInState.result?.score})")
        println("DEBUG: Consolidated displayResult = ${displayResult?.state?.name ?: "NULL"}")
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showCheckInModal && !hasResult,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                PulseFab(onClick = {
                    showCheckInModal = true
                })
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            val isWideScreen = maxWidth > 800.dp

            AnimatedContent(
                targetState = showCheckInModal,
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { it } + fadeIn() togetherWith fadeOut() + slideOutVertically { -it / 2 }
                    } else {
                        slideInVertically { -it } + fadeIn() togetherWith fadeOut() + slideOutVertically { it / 2 }
                    }
                },
                label = "ScreenTransition"
            ) { isCheckingIn ->
                if (isCheckingIn) {
                    CheckInScreen(
                        viewModel = checkInViewModel,
                        onClose = { showCheckInModal = false }
                    )
                } else {
                    if (isWideScreen) {
                        DashboardWebLayout(
                            latestResult = displayResult,
                            onStartCheckIn = { showCheckInModal = true },
                            navController = navController,
                            viewModel = rhythmViewModel,
                            wellnessViewModel = wellnessViewModel,
                            onSignOut = onSignOut,
                            authService = authService
                        )
                    } else {
                        DashboardMobileLayout(
                            latestResult = displayResult,
                            onStartCheckIn = { showCheckInModal = true },
                            navController = navController,
                            viewModel = rhythmViewModel,
                            wellnessViewModel = wellnessViewModel,
                            authService = authService
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun PopularSection(
    tracks: List<RhythmTrack>,
    isLoading: Boolean,
    viewModel: RhythmViewModel,
    navController: NavController
) {
    val firstRowTracks = tracks.take((tracks.size + 1) / 2)
    val secondRowTracks = tracks.drop((tracks.size + 1) / 2)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular on Mindset Pulse",
                style = PoppinsTypography().headlineMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "See All",
                style = PoppinsTypography().bodySmall.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable { navController.navigate(Routes.RHYTHM_HOME) }
            )
        }
        if (isLoading || tracks.isEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(4, key = { "shimmer_popular_$it" }) {
                    ShimmerRhythmItem()
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(firstRowTracks, key = { it.id }) { track ->
                    RhythmItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track)
                            navController.navigate(Routes.RHYTHM_PLAYER(track.id))
                        }
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                thickness = 1.dp
            )
        }
        if (isLoading || tracks.isEmpty() && tracks.size <= ((tracks.size + 1) / 2) ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(4, key = { "shimmer_popular_row2_$it" }) {
                    ShimmerRhythmItem()
                }
            }
        } else if (secondRowTracks.isNotEmpty()){
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(secondRowTracks, key = { it.id }) { track ->
                    RhythmItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track)
                            navController.navigate(Routes.RHYTHM_PLAYER(track.id))
                        }
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
            thickness = 1.dp
        )
    }
}

@Composable
private fun RecommendedSection(
    tracks: List<RhythmTrack>,
    isLoading: Boolean,
    viewModel: RhythmViewModel,
    navController: NavController
) {
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

    val (greetingTitle, targetTags) = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Start Your Day ☀️" to listOf("energy", "upbeat", "motivation", "focus")
            in 12..17 -> "Afternoon Focus 🧠" to listOf("focus", "productivity", "concentration", "chill")
            else -> "Wind Down for Sleep 🌙" to listOf("sleep", "relaxation", "calm", "peaceful")
        }
    }

    val recommendedTracks = remember(tracks, targetTags) {
        if (tracks.isEmpty()) return@remember emptyList()

        val filtered = tracks.filter { track ->
            track.tags.any { tag -> targetTags.contains(tag.lowercase()) }
        }

        if (filtered.isNotEmpty()) filtered.take(10) else tracks.shuffled().take(10)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = greetingTitle,
                style = PoppinsTypography().headlineMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "See All",
                style = PoppinsTypography().bodySmall.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable { navController.navigate(Routes.RHYTHM_HOME) }
            )
        }

        if (isLoading || tracks.isEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(4, key = { "shimmer_recommended_$it" }) {
                    ShimmerRhythmItem()
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendedTracks, key = { it.id }) { track ->
                    RhythmItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track)
                            navController.navigate(Routes.RHYTHM_PLAYER(track.id))
                        }
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                thickness = 1.dp
            )
        }
    }
}


@Composable
fun DashboardMobileLayout(
    latestResult: CheckInResult?,
    onStartCheckIn: () -> Unit,
    navController: NavController,
    viewModel: RhythmViewModel,
    wellnessViewModel: WellnessViewModel,
    authService: AuthService
) {
    val wellnessState by wellnessViewModel.uiState.collectAsState()
    val rhythmState by viewModel.uiState.collectAsState()
    val user by authService.currentUser.collectAsState(null)
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning 🌄,"
        in 12..17 -> "Good Afternoon 😎,"
        else -> "Good Evening 🌙,"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting,
                    style = PoppinsTypography().bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "${" "}${user?.displayName?.substringBefore(' ') ?: "..."}",
                    style = PoppinsTypography().bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { /* Profile */ },
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatar(
                        user?.photoUrl
                            ?: "https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg",
                        "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )

                }
            }

        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ){
                StreakBanner(
                    wellnessState.stats.currentStreak
                )
            }

        }

        item {
            RecommendedSection(
                tracks = rhythmState.tracks,
                isLoading = rhythmState.isTracksLoading,
                viewModel = viewModel,
                navController = navController
            )
        }

        item {
            CardCarousel(
                modifier = Modifier
                    .fillMaxWidth(),
                navController = navController
            )
        }

        item {
            HeroSection(
                latestResult,
                onStartCheckIn
            )
        }

        item {
            PopularSection(
                tracks = rhythmState.tracks,
                isLoading = rhythmState.isTracksLoading,
                viewModel = viewModel,
                navController = navController
            )
        }

        item {
            SectionHeader(
                title = "Mindful Actions",
                icon = Icons.Default.SelfImprovement
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WellnessActionItem(
                    "Breathing",
                    "5 Mins",
                    Icons.Default.Air,
                    bg = Color(0xFF03A9F4)
                ) {
                    wellnessViewModel.selectActivity(WellnessType.BREATHING)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }

                WellnessActionItem(
                    "Yoga",
                    "10 Mins",
                    Icons.Default.SelfImprovement,
                    bg = Color(0xFF4CAF50)
                ) {
                    wellnessViewModel.selectActivity(WellnessType.YOGA)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }

                WellnessActionItem(
                    "Meditation",
                    "15 Mins",
                    Icons.Default.Spa,
                    bg = Color(0xFF9C27B0)
                ) {
                    wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }
            }
        }
    }
}
@Composable
fun DashboardWebLayout(
    latestResult: CheckInResult?,
    onStartCheckIn: () -> Unit,
    navController: NavController,
    viewModel: RhythmViewModel,
    wellnessViewModel: WellnessViewModel,
    onSignOut: () -> Unit,
    authService: AuthService
) {
    val state by viewModel.uiState.collectAsState()
    val wellnessState by wellnessViewModel.uiState.collectAsState()
    var selectedRailItem by remember { mutableStateOf("Home") }
    val user by authService.currentUser.collectAsState(null)
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning 🌄,"
        in 12..17 -> "Good Afternoon 😎,"
        else -> "Good Evening 🌙,"
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp)
                .padding(vertical =8.dp, horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greeting,
                        style = PoppinsTypography().bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "${" "}${user?.displayName?.substringBefore(' ') ?: "..."}",
                        style = PoppinsTypography().bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StreakBanner(
                    wellnessState.stats.currentStreak
                )
            }
            item {
                SectionHeader(
                    title = "Mindful Actions",
                    icon = Icons.Default.SelfImprovement
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    WellnessActionItem(
                        "Breathing",
                        "5 Mins",
                        Icons.Default.Air,
                        bg = Color(0xFF03A9F4)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.BREATHING)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Yoga",
                        "10 Mins",
                        Icons.Default.SelfImprovement,
                        bg = Color(0xFF4CAF50)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.YOGA)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Meditation",
                        "15 Mins",
                        Icons.Default.Spa,
                        bg = Color(0xFF9C27B0)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }
                }
            }
            item {
                Card(
                    onClick = { selectedRailItem = "Home" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Home") MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (selectedRailItem == "Home") Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Home",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item{
                Card(
                    onClick = { navController.navigate(Routes.SETTINGS) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Settings")
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("SignOut")
                }
            }
        }
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                RecommendedSection(
                    tracks = state.tracks,
                    isLoading = state.isTracksLoading,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            item {
                CardCarousel(
                    modifier = Modifier
                        .fillMaxWidth(),
                    navController = navController
                )
            }
            item {
                PopularSection(
                    tracks = state.tracks,
                    isLoading = state.isTracksLoading,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            item {
                HeroSection(
                    latestResult,
                    onStartCheckIn
                )
            }

        }
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Your Wellness Vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            PulseBadgeCard(totalPoints = wellnessState.stats.resiliencePoints)

            WellnessStatsRow(stats = wellnessState.stats)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShieldMoon, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "This is a safe space. Please be kind, supportive, and respectful to everyone in the Mindset Pulse community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            AIPrivacyCard()

            Spacer(Modifier.padding(vertical = 60.dp))
        }
    }
}

@Composable
fun HeroSection(latestResult: CheckInResult?, onStartCheckIn: () -> Unit) {
    if (latestResult != null) {
        InsightHeroCard(
            latestResult
        )
    } else {
        EmptyStateHero(
            onStartCheckIn
        )
    }
}

