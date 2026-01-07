package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import org.vaulture.project.domain.model.AnalyticsSummary
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.viewmodels.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Wellness Analytics", style = PoppinsTypography().headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val coroutineScope = rememberCoroutineScope()
                    IconButton(
                        onClick = { coroutineScope.launch { viewModel.refreshGeminiInsights() } },
                        enabled = !uiState.isLoadingGemini
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh AI Insights")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.summary == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.summary == null || (uiState.summary?.totalCheckIns == 0 && uiState.error == null) -> {
                    NoDataState()
                }
                uiState.summary != null -> {
                    AnalyticsContent(
                        summary = uiState.summary!!,
                        geminiInsights = uiState.geminiInsights,
                        isLoadingGemini = uiState.isLoadingGemini
                    )
                }
            }
        }
    }
}

@Composable
fun NoDataState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = "No data",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Not enough data yet.",
            style = PoppinsTypography().titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Start making check-ins to see your analytics here!",
            style = PoppinsTypography().bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AnalyticsContent(
    summary: AnalyticsSummary,
    geminiInsights: String?,
    isLoadingGemini: Boolean
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val contentFraction = if (isTablet) 0.85f else 1f

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(Modifier.fillMaxWidth(contentFraction)) {
                        AnalyticsHeader(
                            totalCheckIns = summary.totalCheckIns,
                            averageMoodIntensity = summary.averageMoodIntensity
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(Modifier.fillMaxWidth(contentFraction)) {
                        GeminiInsightsSection(
                            insights = geminiInsights,
                            isLoading = isLoadingGemini
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (isTablet) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    if (summary.overallMoodDistribution.isNotEmpty()) {
                                        AnalyticsSectionTitle(
                                            "Overall Mood Intensity",
                                            Icons.Filled.Mood
                                        )
                                        MoodIntensityBarChartCard(
                                            summary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        EmptyDataPlaceholder(
                                            "No mood data recorded yet."
                                        )
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    if (summary.mostFrequentEmotions.isNotEmpty()) {
                                        AnalyticsSectionTitle(
                                            "Most Frequent Emotions",
                                            Icons.Filled.SentimentSatisfied
                                        )
                                        EmotionPieChartCard(
                                            summary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        EmptyDataPlaceholder(
                                            "No specific emotions tracked yet."
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    if (summary.cbtExerciseUsage.isNotEmpty()) {
                                        AnalyticsSectionTitle(
                                            "CBT Exercise Usage",
                                            Icons.Filled.SelfImprovement
                                        )
                                        CbtUsageBarChartCard(
                                            summary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        EmptyDataPlaceholder(
                                            "No CBT exercises logged."
                                        )
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    ConditionalInsightsCard(
                                        summary,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            if (summary.overallMoodDistribution.isNotEmpty()) {
                                AnalyticsSectionTitle(
                                    "Overall Mood Intensity",
                                    Icons.Filled.Mood
                                )
                                MoodIntensityBarChartCard(
                                    summary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                EmptyDataPlaceholder(
                                    "No mood data recorded yet."
                                )
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            if (summary.mostFrequentEmotions.isNotEmpty()) {
                                AnalyticsSectionTitle(
                                    "Most Frequent Emotions",
                                    Icons.Filled.SentimentSatisfied
                                )
                                EmotionPieChartCard(
                                    summary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                EmptyDataPlaceholder(
                                    "No specific emotions tracked yet."
                                )
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            if (summary.cbtExerciseUsage.isNotEmpty()) {
                                AnalyticsSectionTitle(
                                    "CBT Exercise Usage",
                                    Icons.Filled.SelfImprovement
                                )
                                CbtUsageBarChartCard(
                                    summary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                EmptyDataPlaceholder(
                                    "No CBT exercises logged."
                                )
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            ConditionalInsightsCard(
                                summary
                            )
                        }
                    }
                }
            }

            if (summary.selfCareActivityFrequency.isNotEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(Modifier.fillMaxWidth(contentFraction)) {
                            AnalyticsSectionTitle(
                                "Top Self-Care Activities",
                                Icons.Filled.Spa
                            )
                            summary.selfCareActivityFrequency.toList()
                                .sortedByDescending { it.second }
                                .take(5)
                                .forEach { (activity, count) ->
                                    SimpleFrequencyItem(
                                        item = activity,
                                        count = count
                                    )
                                }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }
}

@Composable
fun GeminiInsightsSection(insights: String?, isLoading: Boolean) {
    val clipboardManager = LocalClipboardManager.current
    var isVisible by remember { mutableStateOf(true) }
    var showCopied by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI Insights",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Personalized AI Insights ✨",
                    style = PoppinsTypography().titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Generating your insights with AI... 🧠",
                            style = PoppinsTypography().bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !isLoading && !insights.isNullOrBlank() && isVisible,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                RichAIInsightsCard(
                    insights
                )
            }

            AnimatedVisibility(
                visible = !isLoading && insights.isNullOrBlank(),
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Text(
                    text = "Hmm, I couldn't generate AI insights this time. 😕\nPlease try refreshing or check back later.",
                    style = PoppinsTypography().bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .padding(vertical = 20.dp, horizontal = 8.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!insights.isNullOrBlank()) {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(insights ?: ""))
                        showCopied = true
                    }) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy to Clipboard",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showCopied) {
                LaunchedEffect(showCopied) {
                    delay(1500)
                    showCopied = false
                }
                Text(
                    text = "Copied!",
                    style = PoppinsTypography().labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun RichAIInsightsCard(insights: String?) {
    val codeBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    Markdown(
        content = insights ?: "No insights available yet. Check back soon!",
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.small)
            .border(BorderStroke(1.dp, codeBorderColor), MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}


@Composable
fun AnalyticsHeader(totalCheckIns: Int, averageMoodIntensity: Float) {
    val cardShape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                ),
                shape = cardShape
            ),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    ),
                    shape = cardShape
                )
                .clip(cardShape)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val textColorOnGradient = MaterialTheme.colorScheme.onSurface
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalCheckIns.toString(),
                    style = PoppinsTypography().headlineMedium.copy(color = textColorOnGradient)
                )
                Text(
                    text = "Total Check-ins",
                    style = PoppinsTypography().bodySmall.copy(color = textColorOnGradient)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val formattedAvg = (averageMoodIntensity * 10).roundToInt() / 10.0
                Text(
                    text = "$formattedAvg/10",
                    style = PoppinsTypography().headlineMedium.copy(color = textColorOnGradient)
                )
                Text(
                    text = "Avg. Mood Intensity",
                    style = PoppinsTypography().bodySmall.copy(color = textColorOnGradient)
                )
            }
        }
    }
}

@Composable
fun AnalyticsSectionTitle(title: String, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = "$title icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = PoppinsTypography().titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun MoodIntensityBarChartCard(summary: AnalyticsSummary, modifier: Modifier = Modifier) {
    val items = summary.overallMoodDistribution
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val maxValue = (items.maxOfOrNull { it.averageIntensity } ?: 10f).coerceAtLeast(1f)
            val barColors =
                chartPalette()
            val animProgress by animateFloatAsState(1f, tween(800), label = "MoodBarAnim")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, data ->
                    val heightFraction = (data.averageIntensity / maxValue) * animProgress
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 6.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((160.dp * heightFraction).coerceAtLeast(2.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(barColors[index % barColors.size])
                            )
                        }
                        Text(
                            text = data.mood,
                            style = PoppinsTypography().labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionPieChartCard(summary: AnalyticsSummary, modifier: Modifier = Modifier) {
    val items = summary.mostFrequentEmotions
    val total = items.sumOf { it.count }.coerceAtLeast(1)
    val palette =
        chartPalette()
    val sweepAnim by animateFloatAsState(1f, tween(900), label = "PieSweep")
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    var startAngle = -90f
                    items.forEachIndexed { index, e ->
                        val sweep = 360f * (e.count.toFloat() / total.toFloat()) * sweepAnim
                        drawArc(
                            color = palette[index % palette.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size.minDimension, size.minDimension),
                            topLeft = Offset(
                                (size.width - size.minDimension) / 2f,
                                (size.height - size.minDimension) / 2f
                            )
                        )
                        startAngle += sweep
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEachIndexed { index, e ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(palette[index % palette.size])
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${e.emotion} • ${((e.count * 100f) / total).roundToInt()}%",
                                style = PoppinsTypography().bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CbtUsageBarChartCard(summary: AnalyticsSummary, modifier: Modifier = Modifier) {
    val items = summary.cbtExerciseUsage
    val maxCount = (items.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    val barColors =
        chartPalette()
    val animProgress by animateFloatAsState(1f, tween(800), label = "CbtBarAnim")
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, data ->
                    val heightFraction = (data.count.toFloat() / maxCount.toFloat()) * animProgress
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 6.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((160.dp * heightFraction).coerceAtLeast(2.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(barColors[index % barColors.size])
                            )
                        }
                        Text(
                            text = data.exerciseType.displayName,
                            style = PoppinsTypography().labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionalInsightsCard(summary: AnalyticsSummary, modifier: Modifier = Modifier) {
    val lowMood = summary.averageMoodIntensity < 5f
    val topEmotion = summary.mostFrequentEmotions.maxByOrNull { it.count }
    val lowCbt = summary.cbtExerciseUsage.sumOf { it.count } < 3
    if (!lowMood && topEmotion == null && !lowCbt) return
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Insights",
                style = PoppinsTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(8.dp))
            if (lowMood) {
                Text(
                    text = "Your average mood intensity is trending low. Consider short grounding exercises or a brief walk today.",
                    style = PoppinsTypography().bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
            topEmotion?.let { e ->
                Text(
                    text = "You're often feeling ${e.emotion.lowercase()}. Try journaling or a targeted CBT exercise to explore triggers.",
                    style = PoppinsTypography().bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
            if (lowCbt) {
                Text(
                    text = "CBT usage is low. Practicing even once a day can help build helpful habits.",
                    style = PoppinsTypography().bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun chartPalette(): List<Color> = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
)

@Composable
fun EmptyDataPlaceholder(message: String) {
    Text(
        text = message,
        style = PoppinsTypography().bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SimpleFrequencyItem(item: String, count: Int) {
    ListItem(
        headlineContent = { Text(item, style = PoppinsTypography().bodyLarge) },
        trailingContent = {
            Text(
                text = "$count times",
                style = PoppinsTypography().labelMedium,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}