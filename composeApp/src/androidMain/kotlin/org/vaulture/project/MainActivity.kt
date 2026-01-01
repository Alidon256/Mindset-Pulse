package org.vaulture.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.launch
import org.vaulture.project.utils.AuthServiceImpl
import org.vaulture.project.utils.signInWithGoogleIdToken

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

     private val authService by lazy { AuthServiceImpl(Firebase.auth) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        Firebase.initialize(
            applicationContext,
            options = FirebaseOptions(
                applicationId = "1:410223288840:android:34799bcad6f21a8bf9a7bd",
                apiKey = "AIzaSyDFvaq81oLRVEf1wDyv588GgDskMunJlQM",
                projectId = "tija-a7b75",
                storageBucket = "tija-a7b75.firebasestorage.app",
            )
        )

        Firebase.firestore.setSettings(persistenceEnabled = true)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        lifecycleScope.launch {
                            try {
                                authService.signInWithGoogleIdToken(idToken)
                            } catch (e: Exception) {
                                showToast("Firebase sign-in failed: ${e.message}")
                            }
                        }
                    } else {
                        showToast("Google sign-in failed: ID token was null.")
                    }
                } catch (e: ApiException) {
                    showToast("Google sign-in failed with status code: ${e.statusCode}")
                }
            } else {
                // showToast("Google sign-in cancelled.")
            }
        }

        setContent {
            AppNavigation(onGoogleSignInRequest = { launchGoogleSignIn() })
        }
    }

    /**
     * Creates and launches the Google Sign-In intent.
     */
    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
