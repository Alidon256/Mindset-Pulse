package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info // Added for ExplainerText
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.vaulture.project.domain.model.CbtExerciseType
import org.vaulture.project.domain.model.MoodData
import org.vaulture.project.domain.model.cognitiveDistortionsList
import org.vaulture.project.domain.model.commonEmotions
import org.vaulture.project.presentation.navigation.Routes
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.viewmodels.CBTScreenUiState
import org.vaulture.project.presentation.viewmodels.CBTViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CBTScreen(
    navController: NavController,
    viewModel: CBTViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            snackbarHostState.showSnackbar(
                message = "Check-in submitted successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.resetSubmissionStatus()
            // navController.popBackStack()
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = if (maxWidth > 800.dp) 800.dp else maxWidth
        Box(
            modifier = Modifier
                .width(containerWidth)
                .fillMaxSize()
                .padding(24.dp)
        ){
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Daily CBT",
                            style = PoppinsTypography().headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Routes.ANALYTICS) }
                        ) {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = "Analytics",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.submitCheckIn() },
                    icon = {
                        Icon(
                            Icons.Filled.Done,
                            "Submit Check-in"
                        )
                    },
                    text = {
                        Text(
                            "Submit",
                            style = PoppinsTypography().labelLarge
                        )
                    },
                    expanded = !uiState.isLoading,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    SectionTitle("How are you feeling overall?")
                    OverallMoodSelector(
                        selectedMood = uiState.overallMood,
                        onMoodSelected = viewModel::onOverallMoodChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IntensitySlider(
                        label = "Overall Mood Intensity",
                        intensity = uiState.moodIntensity,
                        onIntensityChange = viewModel::onMoodIntensityChange
                    )

                    SectionTitle("Any specific emotions?")
                    MultiSelectChipGroup(
                        title = "Select Primary Emotions",
                        items = commonEmotions,
                        selectedItems = viewModel.selectedPrimaryEmotions.map { it.emotion },
                        onItemSelected = { emotion ->
                            viewModel.togglePrimaryEmotion(emotion)
                        },
                        itemToString = { it }
                    )
                    viewModel.selectedPrimaryEmotions.forEach { emotionRating ->
                        IntensitySlider(
                            label = "Intensity for ${emotionRating.emotion}",
                            intensity = emotionRating.intensity,
                            onIntensityChange = { newIntensity ->
                                viewModel.updatePrimaryEmotionIntensity(
                                    emotionRating.emotion,
                                    newIntensity
                                )
                            },
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    SectionTitle("Your Reflections")
                    StyledTextField(
                        value = uiState.generalThoughts,
                        onValueChange = viewModel::onGeneralThoughtsChange,
                        label = "General thoughts or notes for today...",
                        minLines = 3
                    )
                    StyledTextField(
                        value = uiState.positiveHighlights,
                        onValueChange = viewModel::onPositiveHighlightsChange,
                        label = "What went well today? (Positive Highlights)",
                        minLines = 2
                    )
                    StyledTextField(
                        value = uiState.challengesFaced,
                        onValueChange = viewModel::onChallengesFacedChange,
                        label = "What was challenging today?",
                        minLines = 2
                    )

                    SectionTitle("Activities Log")
                    ActivityInputSection(
                        title = "Significant Activities",
                        activities = viewModel.selectedSignificantActivities,
                        onAddActivity = viewModel::addSignificantActivity,
                        onRemoveActivity = viewModel::removeSignificantActivity
                    )
                    ActivityInputSection(
                        title = "Self-Care Activities",
                        activities = viewModel.selectedSelfCareActivities,
                        onAddActivity = viewModel::addSelfCareActivity,
                        onRemoveActivity = viewModel::removeSelfCareActivity
                    )

                    SectionTitle("Cognitive Behavioral Therapy (CBT) Exercise")
                    CbtExerciseTypeSelector(
                        selectedType = uiState.cbtExerciseType,
                        onTypeSelected = viewModel::onCbtExerciseTypeChange
                    )

                    AnimatedVisibility(visible = uiState.cbtExerciseType != CbtExerciseType.NONE) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            when (uiState.cbtExerciseType) {
                                CbtExerciseType.THOUGHT_RECORD -> {
                                    ThoughtRecordForm(
                                        viewModel = viewModel,
                                        uiState = uiState
                                    )
                                }

                                CbtExerciseType.GRATITUDE_JOURNALING,
                                CbtExerciseType.BEHAVIORAL_ACTIVATION,
                                CbtExerciseType.PROBLEM_SOLVING,
                                CbtExerciseType.MINDFULNESS_REFLECTION -> {
                                    val prompt = when (uiState.cbtExerciseType) {
                                        CbtExerciseType.GRATITUDE_JOURNALING -> "What are three things you are grateful for today and why?"
                                        CbtExerciseType.BEHAVIORAL_ACTIVATION -> "Describe a positive or valued activity you engaged in or plan to."
                                        CbtExerciseType.PROBLEM_SOLVING -> "Outline a problem and how you approached or might approach solving it."
                                        CbtExerciseType.MINDFULNESS_REFLECTION -> "Reflect on your mindfulness practice or a mindful moment today."
                                        else -> "Your reflections on this exercise:"
                                    }
                                    Text(
                                        prompt,
                                        style = PoppinsTypography().bodyMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    StyledTextField(
                                        value = uiState.cbtReflectionResponse,
                                        onValueChange = viewModel::onCbtReflectionResponseChange,
                                        label = "Your reflections...",
                                        minLines = 3
                                    )
                                }

                                CbtExerciseType.NONE -> { /* Handled by AnimatedVisibility */
                                }
                            }

                            StyledTextField(
                                value = uiState.learnedFromCbt,
                                onValueChange = viewModel::onLearnedFromCbtChange,
                                label = "Key takeaway or what I learned from this exercise",
                                minLines = 2,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
      }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = PoppinsTypography().headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(bottom = 16.dp, top = 20.dp)
    )
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next
    ),
    singleLine: Boolean = false,
    explainerText: String? = null
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = PoppinsTypography().bodySmall) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp), // Slightly more rounded
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() } // Added onDone
            ),
            singleLine = singleLine,
            textStyle = PoppinsTypography().bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        explainerText?.let {
            ExplainerText(
                text = it,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun ExplainerText(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Information",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .size(16.dp)
                .padding(end = 6.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = text,
            style = PoppinsTypography().bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            ),
        )
    }
}

@Composable
fun OverallMoodSelector(
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        MoodData(
            "Awful",
            Icons.Filled.SentimentVeryDissatisfied,
            Color(0xFFD32F2F),
            Color.White
        ), // Reddish
        MoodData(
            "Bad",
            Icons.Filled.SentimentDissatisfied,
            Color(0xFFFBC02D),
            Color.Black
        ),      // Amberish
        MoodData(
            "Okay",
            Icons.Filled.SentimentNeutral,
            Color(0xFF7CB342),
            Color.White
        ),         // Greenish
        MoodData(
            "Good",
            Icons.Filled.SentimentSatisfied,
            Color(0xFF03A9F4),
            Color.White
        ),        // Light Blueish
        MoodData(
            "Great",
            Icons.Filled.SentimentVerySatisfied,
            Color(0xFF9C27B0),
            Color.White
        )   // Purpleish
    )

    val flyingBubbles = remember { mutableStateListOf<FlyingBubbleState>() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { moodData ->
            key(moodData.name) {
                MoodItem(
                    moodData = moodData,
                    isSelected = selectedMood == moodData.name,
                    onSelected = {
                        onMoodSelected(moodData.name)

                        flyingBubbles.add(
                            FlyingBubbleState(
                                id = Clock.System.now()
                                    .toEpochMilliseconds(),
                                color = moodData.selectedColor.copy(alpha = 0.8f),
                                icon = moodData.icon,
                                initialXPercent = (moods.indexOf(moodData) + 0.5f) / moods.size
                            )
                        )
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        flyingBubbles.forEach { bubbleState ->
            key(bubbleState.id) {
                FlyingBubble(
                    state = bubbleState,
                    onAnimationComplete = { flyingBubbles.remove(bubbleState) }
                )
            }
        }
    }
}

@Composable
private fun MoodItem(
    moodData: MoodData,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f),
        label = "MoodItemScale"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "MoodItemBackgroundAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                moodData.selectedColor.copy(alpha = backgroundAlpha * 0.25f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelected
            )
            .padding(vertical = 12.dp, horizontal = 6.dp) // Inner padding
    ) {
        val iconTint = if (isSelected) moodData.contentColor else MaterialTheme.colorScheme.onSurfaceVariant
        val textColor = if (isSelected) moodData.contentColor else MaterialTheme.colorScheme.onSurfaceVariant

        Icon(
            imageVector = moodData.icon,
            contentDescription = moodData.name,
            modifier = Modifier.size(if (isSelected) 44.dp else 40.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            moodData.name,
            style = PoppinsTypography().bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
    }
}


data class FlyingBubbleState(
    val id: Long,
    val color: Color,
    val icon: ImageVector,
    val initialXPercent: Float
)

@Composable
private fun FlyingBubble(
    state: FlyingBubbleState,
    onAnimationComplete: () -> Unit,
    bubbleSizeStart: Dp = 30.dp,
    bubbleSizeEnd: Dp = 50.dp,
    flyDistance: Dp = (-150).dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {

            launch {
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
                onAnimationComplete()
            }
            launch {
                delay(700)
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }

    val currentSize = bubbleSizeStart + (bubbleSizeEnd - bubbleSizeStart) * animatedProgress.value
    val currentYOffset = flyDistance * animatedProgress.value
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
          .offset {
              val parentWidthPx = with(density) { 360.dp.toPx() }
              val initialXPx = parentWidthPx * state.initialXPercent - (currentSize.toPx() / 2)
              IntOffset(initialXPx.toInt(), 0)
          }
    ) {
        Icon(
            imageVector = state.icon,
            contentDescription = null,
            tint = state.color.copy(alpha = alpha.value * 0.7f),
            modifier = Modifier
                .size(currentSize)
                .offset(y = currentYOffset)
                .alpha(alpha.value)
                .clip(CircleShape)
        )
    }
}


@Composable
fun IntensitySlider(
    label: String,
    intensity: Int,
    onIntensityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style =PoppinsTypography().bodyMedium)
            Text(
                intensity.toString(),
                style = PoppinsTypography().bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Slider(
            value = intensity.toFloat(),
            onValueChange = { onIntensityChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> MultiSelectChipGroup(
    title: String,
    items: List<T>,
    selectedItems: List<T>,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String = { it.toString() }
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        if (title.isNotBlank()){
            Text(
                title,
                style =PoppinsTypography().labelLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    label = { Text(itemToString(item), style = PoppinsTypography().bodySmall) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = "Selected", Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        selectedBorderColor = MaterialTheme.colorScheme.secondary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.5.dp,
                        enabled = true,
                        selected = isSelected,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun ActivityInputSection(
    title: String,
    activities: List<String>,
    onAddActivity: (String) -> Unit,
    onRemoveActivity: (String) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = {
                Text(
                    "Add $title (e.g., Read)",
                    style =PoppinsTypography().bodySmall
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (textState.isNotBlank()) {
                        onAddActivity(textState.trim())
                        textState = ""
                    }
                }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Activity",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (textState.isNotBlank()) {
                    onAddActivity(textState.trim())
                    textState = ""
                }
                focusManager.clearFocus()
            }),
            textStyle = PoppinsTypography().bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        if (activities.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    InputChip(
                        selected = false,
                        onClick = { /* Could be used for editing in the future */ },
                        label = {
                            Text(
                                activity,
                                style = PoppinsTypography().bodySmall
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            trailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove $activity",
                                modifier = Modifier
                                    .size(InputChipDefaults.IconSize)
                                    .clickable { onRemoveActivity(activity) }
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = InputChipDefaults.inputChipBorder(
                            borderColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                            borderWidth = 1.dp,
                            enabled = true,
                            selected = false,
                            selectedBorderColor = MaterialTheme.colorScheme.secondary,
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            selectedBorderWidth = 1.5.dp
                        )
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtExerciseTypeSelector(
    selectedType: CbtExerciseType,
    onTypeSelected: (CbtExerciseType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(vertical = 8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        "Select CBT Exercise",
                        style = PoppinsTypography().bodySmall
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                textStyle = PoppinsTypography().bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            ) {
                CbtExerciseType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                type.displayName,
                                style = PoppinsTypography().bodyMedium
                            )
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtRecordForm(viewModel: CBTViewModel, uiState: CBTScreenUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Thought Record",
                style = PoppinsTypography().titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StyledTextField(
                value = uiState.trSituation,
                onValueChange = viewModel::onTrSituationChange,
                label = "1. Situation",
                explainerText = "Describe the event or situation. Where were you? What were you doing? Who was with you?"
            )
            Spacer(modifier = Modifier.height(12.dp)) // Increased space

            StyledTextField(
                value = uiState.trAutomaticNegativeThought,
                onValueChange = viewModel::onTrAutomaticNegativeThoughtChange,
                label = "2. Automatic Negative Thought(s)",
                explainerText = "What thoughts or images went through your mind? Try to capture them exactly as they occurred.",
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            MultiSelectChipGroup(
                title = "3. Cognitive Distortions",
                items = cognitiveDistortionsList,
                selectedItems = viewModel.selectedCognitiveDistortions,
                onItemSelected = viewModel::toggleCognitiveDistortion,
                itemToString = { it }
            )
            ExplainerText(
                text = "Identify any thinking patterns that might be twisting your view (e.g., All-or-Nothing, Overgeneralization). This step is optional.",
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
            )

           StyledTextField(
                value = uiState.trEvidenceForThought,
                onValueChange = viewModel::onTrEvidenceForThoughtChange,
                label = "4. Evidence FOR the thought(s)",
                explainerText = "What facts or experiences support this negative thought? Be objective.",
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = uiState.trEvidenceAgainstThought,
                onValueChange = viewModel::onTrEvidenceAgainstThoughtChange,
                label = "5. Evidence AGAINST the thought(s)",
                explainerText = "What facts or experiences contradict this thought or suggest it's not entirely true?",
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = uiState.trAlternativeThought,
                onValueChange = viewModel::onTrAlternativeThoughtChange,
                label = "6. Alternative/Balanced Thought",
                explainerText = "Considering all the evidence, create a more realistic and balanced thought. How else could you view the situation?",
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
    }
    }
}
