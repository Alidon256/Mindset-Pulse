/*package auth

// If your BaseViewModel is from MOKO, this import is correct.
// If not, adjust as needed or remove if you don't use a base class.
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val profilePicture: ByteArray? = null,
    val isLoading: Boolean = false,
    val error: String? = null // For transient UI errors like "Invalid password"
)

class LoginViewModel(val authService: AuthService) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // --- FIX 1: Add a dedicated StateFlow for critical, non-recoverable errors ---
    // This will be used to signal issues like an expired refresh token (400 Bad Request).
    private val _criticalError = MutableStateFlow<String?>(null)
    val criticalError: StateFlow<String?> = _criticalError.asStateFlow()

    // Expose isAuthenticated from the service, starting eagerly to get the initial state.
    // FIX: The initial value should be fetched from the auth service synchronously if possible.
    val isAuthenticated: StateFlow<Boolean> = authService.isAuthenticated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false // Assuming false until the first emission.
        )


    // --- State Update Functions (for UI to call) ---
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }
    fun onUsernameChange(username: String) = _uiState.update { it.copy(username = username, error = null) }
    fun onProfilePictureChange(bytes: ByteArray?) = _uiState.update { it.copy(profilePicture = bytes, error = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearCriticalError() = _criticalError.update { null }


    // --- Action Functions (for UI to trigger) ---

    fun onSignInClick() {
        if (_uiState.value.isLoading) return
        val (email, password) = _uiState.value
        if (!isEmailValid(email) || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a valid email and password.") }
            return
        }

        performAuthAction("Email Sign-In") {
            authService.signInWithEmail(email, password)
            authService.onSignInSuccess() // Check/create profile on sign-in
        }
    }

    fun onCreateAccountClick() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        if (state.username.isBlank() || !isEmailValid(state.email) || state.password.length < 6) {
            _uiState.update { it.copy(error = "Please provide a username, valid email, and a password of at least 6 characters.") }
            return
        }

        performAuthAction("Account Creation") {
            // Step 1: Fast auth user creation
            val uid = authService.createAuthUser(state.email, state.password)
            // Step 2: Slower profile and image creation
            authService.createUserProfile(
                uid = uid,
                email = state.email,
                username = state.username,
                profilePicture = state.profilePicture
            )
        }
    }

    fun onGoogleSignInClick() {
        if (_uiState.value.isLoading) return
        performAuthAction("Google Sign-In") {
            authService.signInWithGoogle()
            authService.onSignInSuccess() // Check/create profile after Google sign-in
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    private fun performAuthAction(actionName: String, action: suspend () -> Unit) {
        println("ViewModel: Starting action '$actionName'.")
        _uiState.update { it.copy(isLoading = true, error = null) }
        _criticalError.update { null } // Clear critical errors on a new action

        viewModelScope.launch {
            try {
                action()
                println("ViewModel: Action '$actionName' completed successfully.")
                // On success, the isAuthenticated collector will handle UI navigation.
                // We just need to stop the loading indicator.
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred."
                println("ViewModel: Action '$actionName' FAILED. Error: $errorMessage")

                // --- FIX 2: Differentiate between recoverable and critical errors ---
                if (errorMessage.contains("400") && errorMessage.contains("Bad Request") || errorMessage.contains("INVALID_REFRESH_TOKEN")) {
                    // This is a critical session error. Post it to the critical error flow.
                    _criticalError.update { "Your session has expired. Please sign in again." }
                } else {
                    // This is a standard error for the user (e.g., wrong password, email exists).
                    _uiState.update { it.copy(error = errorMessage) }
                }
                _uiState.update { it.copy(isLoading = false) } // Always stop loading on failure.
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        // A slightly more robust check for email format.
        return email.isNotBlank() && "@" in email && email.substringAfterLast("@").contains(".")
    }
}


*/
package org.vaulture.project.presentation.viewmodels

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.vaulture.project.data.remote.AuthService

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val profilePicture: ByteArray? = null,
    val isLoading: Boolean = false,
    val error: String? = null // For transient UI errors like "Invalid password"
)

class LoginViewModel(val authService: AuthService) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Dedicated StateFlow for critical, non-recoverable errors (e.g. Session Expired)
    private val _criticalError = MutableStateFlow<String?>(null)
    val criticalError: StateFlow<String?> = _criticalError.asStateFlow()

    // Expose isAuthenticated. We assume false initially.
    val isAuthenticated: StateFlow<Boolean> = authService.isAuthenticated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // --- State Update Functions ---
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }
    fun onUsernameChange(username: String) = _uiState.update { it.copy(username = username, error = null) }
    fun onProfilePictureChange(bytes: ByteArray?) = _uiState.update { it.copy(profilePicture = bytes, error = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearCriticalError() = _criticalError.update { null }

    // --- Action Functions ---

    fun onSignInClick() {
        if (_uiState.value.isLoading) return
        val (email, password) = _uiState.value
        if (!isEmailValid(email) || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a valid email and password.") }
            return
        }

        performAuthAction("Email Sign-In") {
            authService.signInWithEmail(email, password)
            // Ensure we trigger any necessary post-login logic
            authService.onSignInSuccess()
        }
    }

    fun onCreateAccountClick() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value

        // Basic Validation
        if (state.username.isBlank() || !isEmailValid(state.email) || state.password.length < 6) {
            _uiState.update { it.copy(error = "Please provide a username, valid email, and a password of at least 6 characters.") }
            return
        }

        performAuthAction("Account Creation") {
            // Step 1: Create Auth User (Firebase Auth)
            val uid = authService.createAuthUser(state.email, state.password)

            // Step 2: Create User Document in Firestore (CRITICAL for Chat functionality)
            // This function inside AuthService must run: db.collection("users").document(uid).set(userData)
            authService.createUserProfile(
                uid = uid,
                email = state.email,
                username = state.username,
                profilePicture = state.profilePicture
            )

            authService.onSignInSuccess()
        }
    }

    fun onGoogleSignInClick() {
        if (_uiState.value.isLoading) return
        performAuthAction("Google Sign-In") {
            authService.signInWithGoogle()
            authService.onSignInSuccess()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authService.signOut()
            } catch (e: Exception) {
                println("Error signing out: ${e.message}")
            }
        }
    }

    private fun performAuthAction(actionName: String, action: suspend () -> Unit) {
        println("ViewModel: Starting action '$actionName'.")
        _uiState.update { it.copy(isLoading = true, error = null) }
        _criticalError.update { null }

        viewModelScope.launch {
            try {
                action()
                println("ViewModel: Action '$actionName' completed successfully.")
                // Stop loading. Navigation is handled by observing 'isAuthenticated'
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred."
                println("ViewModel: Action '$actionName' FAILED. Error: $errorMessage")
                e.printStackTrace()

                // Differentiate critical errors
                if (errorMessage.contains("400") && (errorMessage.contains("Bad Request") || errorMessage.contains("INVALID_REFRESH_TOKEN"))) {
                    _criticalError.update { "Your session has expired. Please sign in again." }
                } else {
                    _uiState.update { it.copy(error = errorMessage) }
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && "@" in email && email.substringAfterLast("@").contains(".")
    }
}
