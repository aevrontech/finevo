package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.data.remote.AuthService
import com.aevrontech.finevo.data.remote.AuthState
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** AuthRepository implementation using Supabase Auth. */
class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {

    override val currentUser: Flow<User?> =
            authService.observeAuthState().map { state ->
                when (state) {
                    is AuthState.Authenticated -> state.user
                    else -> null
                }
            }

    override val isLoggedIn: Flow<Boolean> =
            authService.observeAuthState().map { state -> state is AuthState.Authenticated }

    override suspend fun signUp(
            email: String,
            password: String,
            displayName: String?
    ): Result<User> {
        return authService.signUpWithEmail(email, password)
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return authService.signInWithEmail(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String, nonce: String?): Result<User> {
        return authService.signInWithGoogle(idToken, nonce)
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return authService.signInWithApple(idToken, nonce)
    }

    override suspend fun signOut(): Result<Unit> {
        return authService.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun updateProfile(
            displayName: String?,
            avatarUrl: String?,
            country: String?,
            currency: String?
    ): Result<User> {
        // TODO: Implement profile update in AuthService
        return Result.error(AppException.Unknown("Profile update not yet implemented"))
    }

    override suspend fun deleteAccount(): Result<Unit> {
        // TODO: Implement account deletion
        return Result.error(AppException.Unknown("Account deletion not yet implemented"))
    }

    override suspend fun refreshToken(): Result<Unit> {
        // Supabase handles token refresh automatically
        return Result.success(Unit)
    }

    override fun isBiometricAvailable(): Boolean {
        // TODO: Check platform-specific biometric availability
        return false
    }

    override suspend fun enableBiometric(): Result<Unit> {
        // TODO: Implement biometric enrollment
        return Result.success(Unit)
    }

    override suspend fun authenticateWithBiometric(): Result<Boolean> {
        // TODO: Implement biometric authentication
        return Result.success(false)
    }
}
