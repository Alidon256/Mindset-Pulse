/*package org.vaulture.project.screens.space

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
// import components.toImageBitmap // REMOVED: No longer needed
import coil3.compose.AsyncImage // IMPORT ADDED: For displaying images from data
import org.vaulture.project.domain.model.MediaFile
import org.vaulture.project.domain.model.Story
import org.vaulture.project.utils.ImagePicker
import org.vaulture.project.viewmodels.SpaceViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    viewModel: SpaceViewModel,
    onStoryAdded: () -> Unit,
    onCancel: () -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    var selectedMediaFile by remember { mutableStateOf<MediaFile?>(null) }
    var isFeed by remember { mutableStateOf(true) } // Default to posting to main feed

    var isLoading by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State to control the visibility of the image picker
    var showImagePicker by remember { mutableStateOf(false) }

    ImagePicker(
        show = showImagePicker,
        onImageSelected = { imageData ->
            showImagePicker = false // Hide picker after selection
            if (imageData != null) {
                selectedMediaFile = MediaFile(
                    content = imageData,
                    type = Story.ContentType.PHOTO,
                    aspectRatio = 4f / 5f // A common portrait aspect ratio, adjust as needed
                )
            }
        }
    )

    val handleAddStoryClick: () -> Unit = lambda@{
        isLoading = true
        errorMessage = null
        progressMessage = "Starting..."

        val finalContentType = selectedMediaFile?.type ?: Story.ContentType.TEXT

        // --- Improved Validation Logic ---
        if (finalContentType == Story.ContentType.TEXT && textContent.isBlank()) {
            errorMessage = "Please enter some text for your post."
            isLoading = false
            return@lambda
        }
        if (finalContentType != Story.ContentType.TEXT && selectedMediaFile == null) {
            errorMessage = "Please select a photo to post."
            isLoading = false
            return@lambda
        }

        viewModel.addStory(
            mediaContent = selectedMediaFile?.content,
            thumbnailContent = selectedMediaFile?.thumbnail,
            textContent = textContent,
            contentType = finalContentType,
            isFeed = isFeed,
            aspectRatio = selectedMediaFile?.aspectRatio ?: 1f,
            onProgress = { message -> progressMessage = message },
            onSuccess = {
                isLoading = false
                progressMessage = null
                onStoryAdded() // Navigate back or show success
            },
            onError = { errorMsg ->
                isLoading = false
                progressMessage = null
                errorMessage = errorMsg
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = handleAddStoryClick,
                        enabled = !isLoading
                    ) {
                        if (!isLoading) {
                            Text("Post")
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Content Input
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("What's on your mind?") },
                placeholder = { Text("Share your thoughts, add a photo, or post a video.") },
                shape = RoundedCornerShape(16.dp)
            )

            // Media Preview (if selected)
            selectedMediaFile?.let { file ->
                // CORRECTED: Use AsyncImage to display the image from the ByteArray.
                // This removes the need for the problematic 'toImageBitmap()' function.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(file.aspectRatio)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = file.content,
                        contentDescription = "Media preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Media Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = { showImagePicker = true }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
                    Spacer(Modifier.width(8.dp))
                    Text("Photo")
                }
                Button(
                    onClick = { /* TODO: Extend ImagePicker for video */ },
                    enabled = false // Disabled until video picking is implemented in ImagePicker
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Add Video")
                    Spacer(Modifier.width(8.dp))
                    Text("Video")
                }
            }

            // Toggle for "Add to Feed"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Switch(
                    checked = isFeed,
                    onCheckedChange = { isFeed = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("Post to main feed (Memories)")
            }

            // Loading / Error State
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(progressMessage ?: "Loading...")
                }
            }
            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
*/
package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
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
import org.vaulture.project.presentation.utils.ImagePicker
import org.vaulture.project.presentation.viewmodels.SpaceViewModel

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
        val isWide = maxWidth > 800.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Share Reflection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                        // Text Input
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp),
                            placeholder = { Text("What's on your mind today?") },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Media Preview
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
                                        Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        // Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                onClick = { showImagePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                border = if (selectedMediaFile == null) null else BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Add Image", style = MaterialTheme.typography.labelLarge)
                                }
                            }

                            Surface(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Videocam, null, modifier = Modifier.alpha(0.5f))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Add Video", style = MaterialTheme.typography.labelLarge, modifier = Modifier.alpha(0.5f))
                                }
                            }
                        }

                        // Visibility Settings
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Post to Safe Space Feed", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text("Visible to everyone in Mindset Pulse", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = isFeed, onCheckedChange = { isFeed = it })
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }

                // Centered Action Button Overlay
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
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(progressMessage ?: "Posting...", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Breathe into Community", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

