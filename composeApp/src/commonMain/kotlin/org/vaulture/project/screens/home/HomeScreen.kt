package org.vaulture.project.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.vaulture.project.services.AuthService
import org.vaulture.project.components.CardCarousel
import org.vaulture.project.components.EmptyStateHero
import org.vaulture.project.components.InsightHeroCard
import org.vaulture.project.components.ProfileAvatar
import org.vaulture.project.components.PulseFab
import org.vaulture.project.components.RhythmItem
import org.vaulture.project.components.ScaleButton
import org.vaulture.project.components.SectionHeader
import org.vaulture.project.components.ShimmerRhythmItem
import org.vaulture.project.components.StreakBanner
import org.vaulture.project.components.StressTrendChart
import org.vaulture.project.components.WellnessActionCard
import org.vaulture.project.data.models.ResourceItemData
import org.vaulture.project.data.models.RhythmTrack
import org.vaulture.project.data.models.WellnessType
import org.vaulture.project.domain.CheckInResult
import org.vaulture.project.Routes
import org.vaulture.project.theme.PoppinsTypography
import org.vaulture.project.utils.rememberKmpAudioPlayer
import org.vaulture.project.viewmodels.CheckInViewModel
import org.vaulture.project.viewmodels.RhythmViewModel
import org.vaulture.project.viewmodels.WellnessViewModel
import kotlin.time.Clock

// --- Semantic Colors for Mental States ---
// These compliment your main AppTheme colors
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
    wellnessViewModel: WellnessViewModel
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
        println("🔍 DEBUG: DB Result = ${latestPersistentResult?.aiInsight?.take(20)}... (Score: ${latestPersistentResult?.score})")
        println("🔍 DEBUG: Local Result = ${checkInState.result?.aiInsight?.take(20)}... (Score: ${checkInState.result?.score})")
        println("🔍 DEBUG: Consolidated displayResult = ${displayResult?.state?.name ?: "NULL"}")
    }


    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning 🌄,"
        in 12..17 -> "Good Afternoon 😎,"
        else -> "Good Evening 🌙,"
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
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

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Spacer(Modifier.width(8.dp))
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
                    Spacer(Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showCheckInModal && !hasResult, // Hide if result exists
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                PulseFab(onClick = { showCheckInModal = true })
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

            // Content Switcher: Dashboard <-> CheckIn
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
                            wellnessViewModel = wellnessViewModel
                        )
                    } else {
                        DashboardMobileLayout(
                            latestResult = displayResult,
                            onStartCheckIn = { showCheckInModal = true },
                            navController = navController,
                            viewModel = rhythmViewModel,
                            wellnessViewModel = wellnessViewModel
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
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp
                ), // Dynamic padding
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
                    .padding(top = 8.dp),
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
                contentPadding = PaddingValues(horizontal = 8.dp), // Dynamic padding
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
                    .padding(horizontal = 12.dp, vertical = 12.dp) // Dynamic padding
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
fun ResourceItem(data: ResourceItemData, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    ModernAnimatedCard(
        title = data.title,
        sub = data.sub,
        icon = data.icon,
        bg = data.bg,
        tint = data.tint,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun ModernAnimatedCard(
    title: String,
    sub: String,
    icon: ImageVector,
    bg: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infinite = rememberInfiniteTransition(label = "cardPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Card(
        modifier = modifier
            .scale(pulse)
            .clickable { onClick() }
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(bg, bg.copy(alpha = 0.75f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DashboardMobileLayout(
    latestResult: CheckInResult?,
    onStartCheckIn: () -> Unit,
    navController: NavController,
    viewModel: RhythmViewModel,
    wellnessViewModel: WellnessViewModel // Pass this from HomeScreen
) {
    val wellnessState by wellnessViewModel.uiState.collectAsState()
    val rhythmState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Resilience Streak Banner
        item {
            StreakBanner(wellnessState.stats.currentStreak)
        }

        item {
            SectionHeader(title = "Mindful Actions", icon = Icons.Default.SelfImprovement)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inside DashboardMobileLayout or DashboardWebLayout
                WellnessActionCard("Breathing", "5 Mins", Icons.Default.Air, bg = Color(0xFF03A9F4)) {
                    // 1. Select the activity first
                    wellnessViewModel.selectActivity(WellnessType.BREATHING)
                    // 2. Start the timer with the duration
                    wellnessViewModel.startTimer(5)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }

                WellnessActionCard("Yoga", "10 Mins", Icons.Default.SelfImprovement, bg = Color(0xFF4CAF50)) {
                    wellnessViewModel.selectActivity(WellnessType.YOGA)
                    wellnessViewModel.startTimer(10)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }

                WellnessActionCard("Meditation", "15 Mins", Icons.Default.Spa, bg = Color(0xFF9C27B0)) {
                    wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                    wellnessViewModel.startTimer( 15)
                    navController.navigate(Routes.WELLNESS_TIMER)
                }
            }
        }
        item {
            CardCarousel(
                modifier = Modifier
                    .fillMaxWidth(),
                navController = navController
            )
        }

        // 3. AI Insights
        item { HeroSection(latestResult, onStartCheckIn) }

        // 4. Rhythm Section
        item {
            PopularSection(
                tracks = rhythmState.tracks,
                isLoading = rhythmState.isTracksLoading,
                viewModel = viewModel,
                navController = navController
            )
        }

        // 5. Weekly Trend
        item {
            SectionHeader(title = "Weekly Stress Trend", icon = Icons.Default.Timeline)
            Spacer(Modifier.height(12.dp))
            StressTrendChart()
        }
    }
}
@Composable
fun DashboardWebLayout(
    latestResult: CheckInResult?,
    onStartCheckIn: () -> Unit,
    navController: NavController,
    viewModel: RhythmViewModel,
    wellnessViewModel: WellnessViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val wellnessState by wellnessViewModel.uiState.collectAsState()


    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column (Main Data)
        LazyColumn(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                StreakBanner(wellnessState.stats.currentStreak)
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
                HeroSection(latestResult, onStartCheckIn)
            }
            item {
                CardCarousel(
                    modifier = Modifier
                        .fillMaxWidth(),
                    navController = navController
                )
            }
            item {
                SectionHeader(title = "Weekly Stress Trend", icon = Icons.Default.Timeline)
            }
            item {
                StressTrendChart(height = 300.dp)
            }

        }

        LazyColumn(
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item{
                Text("Your Toolkit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item{
                ResourceRow(isVertical = true)
            }
            item {
                SectionHeader(title = "Mindful Actions", icon = Icons.Default.SelfImprovement)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    WellnessActionCard("Breathing", "5 Mins", Icons.Default.Air, bg = Color(0xFF03A9F4)) {
                        // 1. Select the activity first
                        wellnessViewModel.selectActivity(WellnessType.BREATHING)
                        // 2. Start the timer with the duration
                        wellnessViewModel.startTimer(5)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionCard("Yoga", "10 Mins", Icons.Default.SelfImprovement, bg = Color(0xFF4CAF50)) {
                        wellnessViewModel.selectActivity(WellnessType.YOGA)
                        wellnessViewModel.startTimer(10)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionCard("Meditation", "15 Mins", Icons.Default.Spa, bg = Color(0xFF9C27B0)) {
                        wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                        wellnessViewModel.startTimer( 15)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }
                }
            }

        }
    }
}

@Composable
fun HeroSection(latestResult: CheckInResult?, onStartCheckIn: () -> Unit) {
    if (latestResult != null) {
        InsightHeroCard(latestResult)
    } else {
        EmptyStateHero(onStartCheckIn)
    }
}




@Composable
fun ResourceRow(isVertical: Boolean = false) {
    val resources = listOf(
        ResourceItemData("Box Breathing", "Anxiety Relief", Icons.Default.Air, Color(0xFFE3F2FD), Color(0xFF1565C0)),
        ResourceItemData("Sleep Hygiene", "Rest Better", Icons.Default.Bedtime, Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
        ResourceItemData("Talk to Someone", "Find Support", Icons.Default.Groups, Color(0xFFE8F5E9), Color(0xFF2E7D32))
    )

    if (isVertical) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            resources.forEach { res -> ResourceItem(res) }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resources) { res -> ResourceItem(res, Modifier.width(280.dp)) }
        }
    }
}


@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animation Key to prevent text field reset
    val animationKey = remember(uiState.isLoadingQuestions, uiState.isAnalyzing, uiState.result, uiState.step) {
        when {
            uiState.isLoadingQuestions -> "LOADING"
            uiState.isAnalyzing -> "ANALYZING"
            uiState.result != null -> "RESULT"
            else -> "STEP_${uiState.step}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nav Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
            }
            if (!uiState.isLoadingQuestions && uiState.result == null) {
                Text(
                    "Step ${uiState.step + 1} / ${uiState.questions.size + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = animationKey,
            transitionSpec = {
                fadeIn(tween(400)) + scaleIn(initialScale = 0.9f) togetherWith fadeOut(tween(400))
            },
            label = "CheckInContent"
        ) { targetKey ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    targetKey == "LOADING" -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Curating questions with AI...", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    targetKey == "ANALYZING" -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 6.dp
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Gemini is analyzing...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Detecting sentiment & calculating risk", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    targetKey == "RESULT" && uiState.result != null -> {
                        // Success View

                        val resultColor = when (uiState.result!!.state.name) {
                            "STABLE" -> ColorStable
                            "MILD_STRESS" -> ColorMild
                            "HIGH_STRESS" -> ColorHigh
                            "BURNOUT_RISK" -> ColorBurnout
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = resultColor,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Pulse Recorded", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("See Insights")
                        }
                    }
                    targetKey.startsWith("STEP_") && uiState.step < uiState.questions.size -> {
                        // Question Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    uiState.questions[uiState.step],
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(40.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    (1..5).forEach { score ->
                                        ScaleButton(score) { viewModel.selectAnswer(score) }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Positive", style = MaterialTheme.typography.bodySmall, color = ColorStable)
                                    Text("Negative", style = MaterialTheme.typography.bodySmall, color = ColorBurnout)
                                }
                            }
                        }
                    }
                    else -> {
                        // Text Input
                        Text(
                            "In your own words...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("What's on your mind?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = uiState.textResponse,
                            onValueChange = { viewModel.onTextChange(it) },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            placeholder = { Text("I feel overwhelmed because...") },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.submitCheckIn() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = uiState.textResponse.length > 3,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Analyze Pulse")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

