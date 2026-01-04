/*package auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
// Assume your custom upload function is in 'utils'
// You will need to create this expect/actual function
// import utils.upload


// --- EXPECT/ACTUAL DEFINITIONS (These remain the same) ---
internal expect suspend fun AuthServiceImpl.performGoogleSignIn()
internal expect suspend fun AuthServiceImpl.signInWithGoogleIdToken(idToken: String)


// --- IMPLEMENTATION ---
class AuthServiceImpl(
    internal val auth: FirebaseAuth
) : AuthService {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    override val isAuthenticated: Flow<Boolean> = auth.authStateChanged
        .map { user -> user != null && !user.isAnonymous }

    override val currentUser: Flow<User?> = auth.authStateChanged
        .map { firebaseUser -> firebaseUser?.toUser() }


        override suspend fun createUser(
            email: String,
            password: String,
            username: String,
            profilePicture: ByteArray?
        ) {
            println("AuthService: Starting user creation for $email")

            // 1. Create the user in Firebase Auth.
            val authResult = auth.createUserWithEmailAndPassword(email, password)
            val firebaseUser = authResult.user
                ?: throw Exception("User creation failed: Firebase user object is null after creation.")
            println("AuthService: Firebase Auth user created successfully with UID: ${firebaseUser.uid}")

            var photoDownloadUrl: String? = null

            // 2. Upload profile picture if it exists.
            if (profilePicture != null) {
                println("AuthService: Profile picture provided, attempting upload...")
                val storageRef = storage.reference("profile_pictures/${firebaseUser.uid}.jpg")

                // --- DEFINITIVE FIX: Use runCatching to safely execute the suspend function ---
                runCatching {
                    // This is your custom expect/actual suspend function.
                    storageRef.upload(profilePicture)

                    // This code will only run if the upload above SUCCEEDS.
                    println("AuthService: Profile picture uploaded successfully.")
                    photoDownloadUrl = storageRef.getDownloadUrl()
                    println("AuthService: Got photo download URL: $photoDownloadUrl")

                }.onFailure { exception ->
                    // This block will reliably execute if ANY exception occurs during upload.
                    println("AuthService: CRITICAL FAILURE during upload: ${exception.message}")
                    // Optional: Decide if you want to stop the user creation process here.
                    // For now, we will log it and continue without a profile picture.
                }
            }

            // 3. Create the user document in Firestore.
            // This part of the code will now be reached even if the upload fails.
            println("AuthService: Preparing to create user document in Firestore.")
            val userDocument = firestore.collection("users").document(firebaseUser.uid)
            val userData = mutableMapOf<String, Any>(
                "uid" to firebaseUser.uid,
                "username" to username,
                "email" to email,
                "createdAt" to dev.gitlive.firebase.firestore.FieldValue.serverTimestamp
            )
            photoDownloadUrl?.let {
                userData["photoUrl"] = it
            }

            userDocument.set(userData)
            println("AuthService: User document created successfully in Firestore for UID: ${firebaseUser.uid}")

            // 4. Update the central Firebase Auth profile.
            println("AuthService: Initiating update of Firebase Auth profile.")
            firebaseUser.updateProfile(
                displayName = username,
                photoUrl = photoDownloadUrl)
            println("AuthService: User creation process complete for $email.")
        }




    override suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun signInWithGoogle() {
        performGoogleSignIn()
    }

    override suspend fun signOut() {
        auth.currentUser?.let { user ->
            if (user.isAnonymous) {
                user.delete()
            }
        }
        auth.signOut()
    }
}

private fun FirebaseUser.toUser() = User(
    uid = this.uid,
    displayName = this.displayName,
    email = this.email,
    isAnonymous = this.isAnonymous,
    photoUrl = this.photoURL
)*/
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

        // Final step: Update the central Firebase Auth profile (displayName, photoUrl)
        auth.currentUser?.updateProfile(displayName = username, photoUrl = photoDownloadUrl)
        println("[COMMON] AuthService: [2/2] Auth profile updated. Profile creation complete.")
    }

    /*
    override suspend fun onSignInSuccess(): Boolean {
        val firebaseUser = auth.currentUser ?: return false
        val userDocRef = firestore.collection("users").document(firebaseUser.uid)

        println("[COMMON] AuthService: Checking for existing profile for user: ${firebaseUser.uid}")
        val userDoc = userDocRef.get()

        if (userDoc.exists) {
            println("[COMMON] AuthService: Profile already exists for ${firebaseUser.email}. Skipping creation.")
            return false // Profile already exists
        }

        println("[COMMON] AuthService: No profile found for ${firebaseUser.email}. Creating new Firestore document from Auth data.")
        val username = firebaseUser.displayName ?: "User-${firebaseUser.uid.take(6)}"
        val email = firebaseUser.email ?: "no-email@example.com"
        val photoUrl = firebaseUser.photoURL // Google provides this URL directly

        val userData = mapOf(
            "uid" to firebaseUser.uid,
            "username" to username,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to dev.gitlive.firebase.firestore.FieldValue.serverTimestamp
        )

        userDocRef.set(userData)
        println("[COMMON] AuthService: Firestore document for new user created successfully.")
        return true // New profile was created
    }
*/
    // ... inside AuthServiceImpl class ...

  /*  override suspend fun onSignInSuccess(): Boolean {
        val firebaseUser = auth.currentUser ?: return false
        val userDocRef = firestore.collection("users").document(firebaseUser.uid)

        println("[COMMON] AuthService: Checking/Syncing profile for user: ${firebaseUser.uid}")

        // We use a get() to see if the user already has a Mindset Pulse profile
        val userDoc = userDocRef.get()

        if (!userDoc.exists) {
            println("[COMMON] AuthService: New Google/External user detected. Creating Firestore profile...")

            // Google provides these directly in the Auth object
            val username = firebaseUser.displayName ?: "User-${firebaseUser.uid.take(6)}"
            val email = firebaseUser.email ?: ""
            val photoUrl = firebaseUser.photoURL

            val userData = mapOf(
                "uid" to firebaseUser.uid,
                "username" to username,
                "email" to email,
                "photoUrl" to photoUrl,
                "createdAt" to FieldValue.serverTimestamp,
                "totalMindfulMinutes" to 0,
                "currentStreak" to 0
            )

            userDocRef.set(userData)
            return true
        }

        println("[COMMON] AuthService: Profile already exists. Sync complete.")
        return false
    }*/


    private suspend fun uploadProfileImage(uid: String, bytes: ByteArray?): String? {
        if (bytes == null) {
            println("[COMMON] AuthService: No profile image provided. Skipping upload.")
            return null
        }
        println("[COMMON] AuthService: Profile image provided. Starting upload for UID: $uid...")
        return try {
            val storageRef = storage.reference("profile_pictures/$uid.jpg")
            storageRef.upload(bytes) // Your working expect/actual function
            val downloadUrl = storageRef.getDownloadUrl()
            println("[COMMON] AuthService: Image upload success. URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            println("[COMMON] AuthService: CRITICAL FAILURE during image upload: ${e.message}")
            null // Return null if upload fails, allowing profile creation to continue
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
                // Profile exists, but maybe update the photoUrl if it changed in Google
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

    /*override suspend fun signInWithGoogle() {
        println("[COMMON] AuthService: Delegating Google Sign-In to platform-specific implementation...")
        performGoogleSignIn()
    }*/
    override suspend fun signInWithGoogle() {
        println("[COMMON] AuthService: Triggering Google Sign-In...")
        performGoogleSignIn() // Platform-specific call
      //  onSignInSuccess()
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

