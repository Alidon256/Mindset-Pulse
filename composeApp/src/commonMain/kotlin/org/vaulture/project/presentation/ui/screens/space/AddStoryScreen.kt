package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.vaulture.project.domain.model.MediaFile
import org.vaulture.project.domain.model.Story
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.utils.ImagePicker
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import kotlin.text.forEach
import kotlin.text.lowercase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    viewModel: SpaceViewModel,
    onStoryAdded: () -> Unit,
    onCancel: () -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    var selectedMediaFile by remember { mutableStateOf<MediaFile?>(null) }
    var isFeed by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedVisibility by remember { mutableStateOf(Story.Visibility.PUBLIC) }


    ImagePicker(
        show = showImagePicker,
        onImageSelected = { imageData ->
            showImagePicker = false
            if (imageData != null) {
                selectedMediaFile = MediaFile(
                    content = imageData,
                    type = Story.ContentType.PHOTO,
                    aspectRatio = 1f
                )
            }
        }
    )

    val handleAddStoryClick: () -> Unit = {
        isLoading = true
        errorMessage = null
        progressMessage = "Preparing your reflection..."

        val finalContentType = selectedMediaFile?.type ?: Story.ContentType.TEXT

        if (finalContentType == Story.ContentType.TEXT && textContent.isBlank()) {
            errorMessage = "Share a thought or a photo to proceed."
            isLoading = false
        } else {
            viewModel.addStory(
                mediaContent = selectedMediaFile?.content,
                thumbnailContent = selectedMediaFile?.thumbnail,
                textContent = textContent,
                contentType = finalContentType,
                isFeed = isFeed,
                aspectRatio = selectedMediaFile?.aspectRatio ?: 1f,
                visibility = selectedVisibility,
                onProgress = { progressMessage = it },
                onSuccess = {
                    isLoading = false
                    onStoryAdded()
                },
                onError = {
                    isLoading = false
                    errorMessage = it
                }
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 920.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Share Reflection",
                            style = PoppinsTypography().titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isWide) 0.dp else 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp),
                            placeholder = {
                                Text(
                                "What's on your mind today?",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        AnimatedVisibility(visible = selectedMediaFile != null) {
                            selectedMediaFile?.let { file ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.77f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = file.content,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { selectedMediaFile = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.AddAPhoto,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                onClick = { showImagePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                border = if (selectedMediaFile == null)
                                    null
                                else
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Add Image",
                                        style = PoppinsTypography().labelLarge
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Who can see this reflection?",
                                style = PoppinsTypography().labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Story.Visibility.entries.forEach { visibility ->
                                    FilterChip(
                                        selected = selectedVisibility == visibility,
                                        onClick = { selectedVisibility = visibility },
                                        label = {
                                            Text(
                                                text = visibility.name.lowercase()
                                                    .replace("_", " ")
                                                    .replaceFirstChar { it.uppercase() }
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (visibility) {
                                                    Story.Visibility.PUBLIC -> Icons.Default.Public
                                                    Story.Visibility.CONNECTS_ONLY -> Icons.Default.People
                                                    Story.Visibility.PRIVATE -> Icons.Default.Lock
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selectedVisibility == visibility,
                                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            borderWidth = 1.dp
                                        )
                                    )
                                }
                            }

                            AnimatedContent(
                                targetState = selectedVisibility,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "VisibilityDescription"
                            ) { targetVis ->
                                val description = when (targetVis) {
                                    Story.Visibility.PUBLIC -> "Shared with the entire Mindset Pulse community."
                                    Story.Visibility.CONNECTS_ONLY -> "Only visible to you and people you've 'Connected' with."
                                    Story.Visibility.PRIVATE -> "Visible only to you. A private space for deep reflection."
                                }
                                Text(
                                    text = description,
                                    style = PoppinsTypography().bodySmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                                )
                            }
                        }


                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = PoppinsTypography().bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = handleAddStoryClick,
                        enabled = !isLoading && (textContent.isNotBlank() || selectedMediaFile != null),
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 400.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    progressMessage ?: "Posting...",
                                    style = PoppinsTypography().labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                "Breathe into Community",
                                fontWeight = FontWeight.Bold,
                                style = PoppinsTypography().labelMedium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

