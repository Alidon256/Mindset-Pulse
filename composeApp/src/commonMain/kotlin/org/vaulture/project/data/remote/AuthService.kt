package org.vaulture.project.data.remote

import kotlinx.coroutines.flow.Flow
import org.vaulture.project.domain.model.User

interface AuthService {
    val isAuthenticated: Flow<Boolean>
    val currentUser: Flow<User?>
    suspend fun createAuthUser(email: String, password: String): String
    suspend fun createUserProfile(
        uid: String,
        email: String,
        username: String,
        profilePicture: ByteArray?
    )
    suspend fun onSignInSuccess(): Boolean
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signInWithGoogle()
    suspend fun signOut()
}