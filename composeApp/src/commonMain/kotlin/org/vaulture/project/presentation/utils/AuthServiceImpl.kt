package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.vaulture.project.data.remote.AuthService
import org.vaulture.project.domain.model.User

// EXPECT declarations for platform-specific Google Sign-In implementations
internal expect suspend fun AuthServiceImpl.performGoogleSignIn()
internal expect suspend fun AuthServiceImpl.signInWithGoogleIdToken(idToken: String)

class AuthServiceImpl(
    internal val auth: FirebaseAuth
) : AuthService {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    init {
        GlobalScope.launch {
            auth.authStateChanged.collect { user ->
                if (user != null) {
                    println("[COMMON] Global Auth Observer: User detected. Syncing...")
                    onSignInSuccess()
                }
            }
        }
    }
    override val isAuthenticated: Flow<Boolean> = auth.authStateChanged.map { it != null }
    override val currentUser: Flow<User?> = auth.authStateChanged.map { it?.toUser() }

    override suspend fun createAuthUser(email: String, password: String): String {
        println("[COMMON] AuthService: [1/2] Creating user in Firebase Auth for email: $email...")
        val authResult = auth.createUserWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("Auth creation failed, UID is null.")
        println("[COMMON] AuthService: [1/2] Auth user created successfully. UID: $uid")
        return uid
    }

    override suspend fun createUserProfile(
        uid: String,
        email: String,
        username: String,
        profilePicture: ByteArray?
    ) {
        println("[COMMON] AuthService: [2/2] Starting user profile creation for UID: $uid")
        val photoDownloadUrl = uploadProfileImage(uid, profilePicture)

        println("[COMMON] AuthService: [2/2] Preparing to create Firestore document.")
        val userDocument = firestore.collection("users").document(uid)
        val userData = mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "photoUrl" to photoDownloadUrl,
            "createdAt" to FieldValue.serverTimestamp
        )
        userDocument.set(userData)
        println("[COMMON] AuthService: [2/2] Firestore document created successfully.")
        auth.currentUser?.updateProfile(displayName = username, photoUrl = photoDownloadUrl)
        println("[COMMON] AuthService: [2/2] Auth profile updated. Profile creation complete.")
    }


    private suspend fun uploadProfileImage(uid: String, bytes: ByteArray?): String? {
        if (bytes == null) {
            println("[COMMON] AuthService: No profile image provided. Skipping upload.")
            return null
        }
        println("[COMMON] AuthService: Profile image provided. Starting upload for UID: $uid...")
        return try {
            val storageRef = storage.reference("profile_pictures/$uid.jpg")
            storageRef.upload(bytes)
            val downloadUrl = storageRef.getDownloadUrl()
            println("[COMMON] AuthService: Image upload success. URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            println("[COMMON] AuthService: CRITICAL FAILURE during image upload: ${e.message}")
            null
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        println("[COMMON] AuthService: Attempting to sign in with email: $email")
        auth.signInWithEmailAndPassword(email, password)
        println("[COMMON] AuthService: Email sign-in successful.")
    }

    override suspend fun onSignInSuccess(): Boolean {
        val firebaseUser = auth.currentUser ?: return false
        val userDocRef = firestore.collection("users").document(firebaseUser.uid)

        return try {
            val userDoc = userDocRef.get()

            if (!userDoc.exists) {
                println("[COMMON] AuthService: New user [${firebaseUser.uid}]. Creating Firestore profile...")

                val userData = mapOf(
                    "uid" to firebaseUser.uid,
                    "username" to (firebaseUser.displayName ?: "User-${firebaseUser.uid.take(4)}"),
                    "email" to (firebaseUser.email ?: ""),
                    "photoUrl" to (firebaseUser.photoURL ?: ""),
                    "createdAt" to FieldValue.serverTimestamp,
                    "totalMindfulMinutes" to 0,
                    "currentStreak" to 0,
                    "resiliencePoints" to 0
                )

                userDocRef.set(userData)
                println("[COMMON] AuthService: Firestore Profile Created Successfully.")
                true
            } else {
                if (firebaseUser.photoURL != null) {
                    userDocRef.update("photoUrl" to firebaseUser.photoURL)
                }
                println("[COMMON] AuthService: Profile synced for existing user.")
                false
            }
        } catch (e: Exception) {
            println("[COMMON] AuthService: SYNC ERROR: ${e.message}")
            false
        }
    }

    override suspend fun signInWithGoogle() {
        println("[COMMON] AuthService: Triggering Google Sign-In...")
        performGoogleSignIn() // Platform-specific call

    }

    override suspend fun signOut() {
        println("[COMMON] AuthService: Signing out user.")
        auth.signOut()
    }
}

private fun FirebaseUser.toUser() = User(
    uid = this.uid,
    displayName = this.displayName,
    email = this.email,
    isAnonymous = this.isAnonymous,
    photoUrl = this.photoURL
)

