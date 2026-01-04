package org.vaulture.project.presentation.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.vaulture.project.presentation.viewmodels.LoginUiState
import org.vaulture.project.presentation.viewmodels.LoginViewModel
import org.vaulture.project.presentation.utils.ImagePicker


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleSignInRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSignUpMode by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    ImagePicker(
        show = showImagePicker,
        onImageSelected = { imageData ->
            showImagePicker = false
            if (imageData != null) {
                viewModel.onProfilePictureChange(imageData)
            }
        }
    )

    // --- Responsive Layout ---
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        if (maxWidth > 700.dp) {
            // --- Wide Screen Layout (Web/Tablet) ---
            Row(modifier = Modifier.fillMaxSize()) {
                // Decorative side panel
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "https://images.pexels.com/photos/931007/pexels-photo-931007.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                        contentDescription = "Decorative background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(0.3f)))
                    Column(
                        Modifier.padding(48.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Vaulture",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Travel Deeper and Build Connection.",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                // Login/Sign-up form
                Box(
                    modifier = Modifier.weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.AuthForm(
                        uiState = uiState,
                        isSignUpMode = isSignUpMode,
                        onToggleMode = { isSignUpMode = !isSignUpMode },
                        onUsernameChange = viewModel::onUsernameChange,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onSignInClick = viewModel::onSignInClick,
                        onCreateAccountClick = viewModel::onCreateAccountClick,
                        onGoogleSignInClick = onGoogleSignInRequest,
                        onProfilePictureClick = { showImagePicker = true }
                    )
                }
            }
        } else {
            // --- Narrow Screen Layout (Mobile) ---
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.AuthForm(
                uiState = uiState,
                isSignUpMode = isSignUpMode,
                onToggleMode = { isSignUpMode = !isSignUpMode },
                onUsernameChange = viewModel::onUsernameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSignInClick = viewModel::onSignInClick,
                onCreateAccountClick = viewModel::onCreateAccountClick,
                onGoogleSignInClick = onGoogleSignInRequest,
                onProfilePictureClick = { showImagePicker = true }
            )
        }
    }
}

@Composable
private fun AuthForm(
    uiState: LoginUiState,
    isSignUpMode: Boolean,
    onToggleMode: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onProfilePictureClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    )
                )
            )
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(32.dp))

        // --- Dynamic Title with animation ---
        Crossfade(
            targetState = isSignUpMode,
            label = "TitleCrossfade",
            animationSpec = tween(500)
        ) { isSignUp ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isSignUp) "Join us to explore the world" else "Sign in to continue your journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // --- Profile Picture and Fields with animation ---
        AnimatedVisibility(visible = isSignUpMode) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onProfilePictureClick() }
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profilePicture != null) {
                        AsyncImage(
                            model = uiState.profilePicture,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add profile picture",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Username Icon") },
                    isError = uiState.error?.contains("username", ignoreCase = true) == true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // --- Common Fields with Icons ---
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon") },
            isError = uiState.error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
            isError = uiState.error != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (isSignUpMode) onCreateAccountClick() else onSignInClick()
            }),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(24.dp))

        // --- Error Message Display ---
        AnimatedVisibility(visible = uiState.error != null && uiState.error.isNotBlank()) {
            Text(
                text = uiState.error ?: "An unknown error occurred",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // --- Action Buttons & Loading State ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
            } else {
                val buttonText = if (isSignUpMode) "CREATE MY ACCOUNT" else "SIGN IN"
                Button(
                    onClick = if (isSignUpMode) onCreateAccountClick else onSignInClick,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // --- Social Login & Toggle Mode ---
        Text("or continue with")
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            // Icon(painterResource(Res.drawable.google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
            // Spacer(Modifier.width(8.dp))
            Text("SIGN IN WITH GOOGLE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.height(24.dp))

        val toggleText = if (isSignUpMode) "Already have an account? Sign In" else "New here? Create an Account"
        TextButton(onClick = onToggleMode) {
            Text(toggleText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(32.dp))
    }
}
