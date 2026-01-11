package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.ScaleButton
import org.vaulture.project.presentation.viewmodels.CheckInViewModel

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val animationKey = remember(uiState.isLoadingQuestions, uiState.isAnalyzing, uiState.result, uiState.step) {
        when {
            uiState.isLoadingQuestions -> "LOADING"
            uiState.isAnalyzing -> "ANALYZING"
            uiState.result != null -> "RESULT"
            else -> "STEP_${uiState.step}"
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = if (maxWidth > 920.dp) 920.dp else maxWidth
        Box(
            modifier = Modifier
                .width(containerWidth)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (!uiState.isLoadingQuestions && uiState.result == null) {
                        Text(
                            "Step ${uiState.step + 1} / ${uiState.questions.size + 1}",
                            style = PoppinsTypography().labelLarge,
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
                    label = "CheckInContent",
                    modifier = Modifier.fillMaxWidth()
                ) { targetKey ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when {
                            targetKey == "LOADING" -> {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Curating questions with AI...",
                                    fontWeight = FontWeight.Medium,
                                    style = PoppinsTypography().headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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
                                    style = PoppinsTypography().headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Detecting sentiment & calculating risk",
                                    style = PoppinsTypography().bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            targetKey == "RESULT" && uiState.result != null -> {

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
                                Text(
                                    "Pulse Recorded",
                                    style = PoppinsTypography().headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(32.dp))
                                Button(
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        "See Insights",
                                        style = PoppinsTypography().labelLarge
                                    )
                                }
                            }
                            targetKey.startsWith("STEP_") && uiState.step < uiState.questions.size -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        Modifier.padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            uiState.questions[uiState.step],
                                            style = PoppinsTypography().headlineSmall,
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
                                                ScaleButton(
                                                    score
                                                ) {
                                                    viewModel.selectAnswer(score)
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Positive",
                                                style = PoppinsTypography().bodySmall,
                                                color = ColorStable
                                            )
                                            Text(
                                                "Negative",
                                                style = PoppinsTypography().bodySmall,
                                                color = ColorBurnout
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    "In your own words...",
                                    style = PoppinsTypography().headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "What's on your mind?",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(24.dp))

                                OutlinedTextField(
                                    value = uiState.textResponse,
                                    onValueChange = { viewModel.onTextChange(it) },
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    placeholder = {
                                        Text(
                                            "I feel overwhelmed because...",
                                            style = PoppinsTypography().bodyMedium
                                            )
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
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
                                    Text(
                                        "Analyze Pulse",
                                        style = PoppinsTypography().labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
