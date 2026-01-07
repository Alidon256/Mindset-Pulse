package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.auth.GoogleAuthProvider
import org.vaulture.project.presentation.utils.AuthServiceImpl

internal actual suspend fun AuthServiceImpl.performGoogleSignIn() {
    throw NotImplementedError("Google Sign-In on Android must be triggered from MainActivity.")
}

internal actual suspend fun AuthServiceImpl.signInWithGoogleIdToken(idToken: String) {
    val credential = GoogleAuthProvider.credential(idToken, null)
    auth.signInWithCredential(credential)
}