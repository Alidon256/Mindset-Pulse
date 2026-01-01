package org.vaulture.project.screens.space

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
import org.vaulture.project.data.models.Story
import org.vaulture.project.utils.ImagePicker
import org.vaulture.project.viewmodels.SpaceViewModel

// A platform-agnostic representation of picked media
data class MediaFile(
    val content: ByteArray,
    val thumbnail: ByteArray? = null,
    val type: Story.ContentType,
    val aspectRatio: Float
)

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

    // The actual ImagePicker composable, which is shown when `showImagePicker` is true
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

    // Note: The current ImagePicker implementation only supports single image picking.
    // The video picker functionality will require extending the expect/actual ImagePicker.
    // For now, we will disable the video button to prevent confusion.

    // CORRECTED: Labeled the lambda as 'lambda@' to resolve the return scope.
    val handleAddStoryClick: () -> Unit = lambda@{
        isLoading = true
        errorMessage = null
        progressMessage = "Starting..."

        val finalContentType = selectedMediaFile?.type ?: Story.ContentType.TEXT

        // --- Improved Validation Logic ---
        if (finalContentType == Story.ContentType.TEXT && textContent.isBlank()) {
            errorMessage = "Please enter some text for your post."
            isLoading = false
            return@lambda // CORRECTED: Use the correct label to return from the lambda.
        }
        if (finalContentType != Story.ContentType.TEXT && selectedMediaFile == null) {
            errorMessage = "Please select a photo to post."
            isLoading = false
            return@lambda // CORRECTED: Use the correct label to return from the lambda.
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
