package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.utils.ImagePicker
import org.vaulture.project.presentation.viewmodels.SpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceScreen(
    viewModel: SpaceViewModel,
    onNavigateBack: () -> Unit,
    onSpaceCreated: (spaceId: String) -> Unit
) {
    val isCreating by viewModel.isCreatingSpace.collectAsState()
    var coverImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isFormValid = viewModel.spaceName.value.isNotBlank() &&
            viewModel.spaceDescription.value.isNotBlank()

    ImagePicker(
        show = showImagePicker,
        onImageSelected = { imageData ->
            showImagePicker = false
            coverImageBytes = imageData
        }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 800.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Create Space",
                            style = PoppinsTypography().titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
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
                        .padding(horizontal = if (isWideScreen) 0.dp else 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { showImagePicker = true }
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (coverImageBytes != null) {
                                AsyncImage(
                                    model = coverImageBytes,
                                    contentDescription = "Cover",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Add a Calming Cover",
                                        style = PoppinsTypography().labelLarge
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = viewModel.spaceName.value,
                            onValueChange = { viewModel.spaceName.value = it },
                            label = { Text(
                                "Space Name",
                                style = PoppinsTypography().labelLarge
                                )
                            },
                            placeholder = {
                                Text("e.g., Morning Reflections",
                                    style = PoppinsTypography().labelLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            isError = viewModel.spaceName.value.isBlank() && isCreating
                        )

                        OutlinedTextField(
                            value = viewModel.spaceDescription.value,
                            onValueChange = { viewModel.spaceDescription.value = it },
                            label = {
                                Text(
                                "Describe the Atmosphere",
                                    style = PoppinsTypography().labelLarge
                                )
                            },
                            placeholder = {
                                Text(
                                    "What is the intent of this space?",
                                    style = PoppinsTypography().labelLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            isError = viewModel.spaceDescription.value.isBlank() && isCreating
                        )

                        Spacer(Modifier.height(120.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.8f), MaterialTheme.colorScheme.background),
                                startY = 0f
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.createSpace(
                                coverImageBytes = coverImageBytes,
                                onSuccess = onSpaceCreated,
                                onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                        },
                        enabled = isFormValid && !isCreating,
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 400.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Breathe Life into Space",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
