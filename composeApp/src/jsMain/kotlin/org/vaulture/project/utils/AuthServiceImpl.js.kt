package org.vaulture.project.utils

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.js // Required for the 'FirebaseAuth.js' property
import kotlinx.coroutines.await
import kotlin.js.Promise

// --- Native JavaScript Interop Definition ---

// Defines a Kotlin interface that matches the native Firebase JS Auth object's structure.
private external interface FirebaseJsAuth {
    fun signInWithPopup(provider: dynamic): Promise<dynamic>
}

// This extension property provides a safe, typed way to access the native JS 'signInWithPopup' function.
private val FirebaseAuth.jsAuth: FirebaseJsAuth
    get() = this.js.unsafeCast<FirebaseJsAuth>()

// --- Dynamic JS Interop for Provider ---

// This dynamic block allows us to write raw JavaScript to get the native provider.
// We need to do this because the gitlive library does not provide a direct Kotlin equivalent.
@JsModule("firebase/auth")
@JsNonModule
private external val GoogleAuthProviderModule: dynamic

// --- Actual Implementations ---

/**
 * Web's implementation of performGoogleSignIn.
 * It calls the native Firebase JS signInWithPopup method via our interop helper.
 */
internal actual suspend fun AuthServiceImpl.performGoogleSignIn() {
    // 1. Create a new instance of the native provider directly.
    // This resolves the "unused variable" warning and is the correct approach.
    val jsProvider = js("new GoogleAuthProviderModule.GoogleAuthProvider()")

    // 2. Call the native 'signInWithPopup' function and await the result.
    auth.jsAuth.signInWithPopup(jsProvider).await()
}

/**
 * This function is not needed for the web flow, so we provide an empty 'actual'
 * implementation to satisfy the 'expect' contract in commonMain.
 */
internal actual suspend fun AuthServiceImpl.signInWithGoogleIdToken(idToken: String) {
    // Intentionally empty. The popup flow on the web handles everything.
}
