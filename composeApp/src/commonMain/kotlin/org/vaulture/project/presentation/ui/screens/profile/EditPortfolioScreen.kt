package org.vaulture.project.presentation.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.utils.ImagePicker
import org.vaulture.project.presentation.viewmodels.SpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPortfolioScreen(
    viewModel: SpaceViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.targetUserProfile.collectAsState()
    val isUpdating by viewModel.isUpdatingProfile.collectAsState()

    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            displayName = user?.displayName ?: ""
        }
    }
    ImagePicker(
        show = showImagePicker,
        onImageSelected = {
            selectedImageBytes = it
            showImagePicker = false
        }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 920.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Edit Identity",
                            style = PoppinsTypography().titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null
                            )
                        }
                    },
                    actions = {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.
                                size(24.dp)
                                    .padding(end = 16.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.updatePortfolio(
                                        displayName, selectedImageBytes,
                                        onSuccess = onBack,
                                        onError = { /* Handle Error */ }
                                    )
                                },
                                enabled = displayName.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    "Save",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isWide) 0.dp else 24.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AsyncImage(
                            model = selectedImageBytes ?: user?.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(36.dp)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                }
            }
        }
    }
}
