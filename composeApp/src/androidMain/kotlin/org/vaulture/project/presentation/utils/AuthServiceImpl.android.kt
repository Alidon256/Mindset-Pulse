package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.auth.GoogleAuthProvider
import org.vaulture.project.presentation.utils.AuthServiceImpl

/**
 * Android's implementation of performGoogleSignIn.
 * It throws an error because the sign-in flow must be initiated from an Activity (the UI).
 * This acts as a safeguard to prevent accidental calls from a non-UI context.
 * The correct flow is for the UI to call a method in MainActivity, which then launches the intent.
 */
internal actual suspend fun AuthServiceImpl.performGoogleSignIn() {
    throw NotImplementedError("Google Sign-In on Android must be triggered from MainActivity.")
}

/**
 * Android's implementation for completing the sign-in with the ID token.
 * This is called from MainActivity after the Google Sign-In intent returns a successful result.
 */
internal actual suspend fun AuthServiceImpl.signInWithGoogleIdToken(idToken: String) {
    val credential = GoogleAuthProvider.credential(idToken, null)
    auth.signInWithCredential(credential)
}