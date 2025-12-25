package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.User
import kotlinx.coroutines.flow.Flow

/** Authentication repository interface */
interface AuthRepository {
    /** Get the current user if logged in */
    val currentUser: Flow<User?>

    /** Check if user is logged in */
    val isLoggedIn: Flow<Boolean>

    /** Sign up with email and password */
    suspend fun signUp(email: String, password: String, displayName: String? = null): Result<User>

    /** Sign in with email and password */
    suspend fun signIn(email: String, password: String): Result<User>

    /** Sign in with Google (includes optional nonce for Credential Manager) */
    suspend fun signInWithGoogle(idToken: String, nonce: String? = null): Result<User>

    /** Sign in with Apple */
    suspend fun signInWithApple(idToken: String, nonce: String): Result<User>

    /** Sign out */
    suspend fun signOut(): Result<Unit>

    /** Send password reset email */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /** Update user profile */
    suspend fun updateProfile(
            displayName: String? = null,
            avatarUrl: String? = null,
            country: String? = null,
            currency: String? = null
    ): Result<User>

    /** Delete user account */
    suspend fun deleteAccount(): Result<Unit>

    /** Refresh the authentication token */
    suspend fun refreshToken(): Result<Unit>

    /** Check if biometric authentication is available */
    fun isBiometricAvailable(): Boolean

    /** Enable biometric authentication */
    suspend fun enableBiometric(): Result<Unit>

    /** Authenticate with biometric */
    suspend fun authenticateWithBiometric(): Result<Boolean>
}
