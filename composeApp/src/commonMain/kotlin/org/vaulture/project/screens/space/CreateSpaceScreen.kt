package org.vaulture.project.screens.space

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.vaulture.project.utils.ImagePicker
import org.vaulture.project.viewmodels.SpaceViewModel

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

    ImagePicker(
        show = showImagePicker,
        onImageSelected = { imageData ->
            showImagePicker = false
            coverImageBytes = imageData
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create New Space") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.createSpace(
                        coverImageBytes = coverImageBytes,
                        onSuccess = { spaceId ->
                            // This lambda is called when the space is created successfully.
                            // It provides the new spaceId.
                            onSpaceCreated(spaceId)
                        },
                        onError = { errorMessage ->
                            // This lambda is called when an error occurs.
                            // It provides the error message.
                            scope.launch {
                                snackbarHostState.showSnackbar(errorMessage)
                            }
                        }
                    )
                },
                enabled = viewModel.spaceName.value.isNotBlank() && !isCreating,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Space")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showImagePicker = true }
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (coverImageBytes != null) {
                    AsyncImage(
                        model = coverImageBytes,
                        contentDescription = "Cover Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AddAPhoto, "Add Cover Photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text("Add Cover Photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.spaceName.value,
                onValueChange = { viewModel.spaceName.value = it },
                label = { Text("Space Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.spaceDescription.value,
                onValueChange = { viewModel.spaceDescription.value = it },
                label = { Text("Space Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }
    }
}
