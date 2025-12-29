package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.data.remote.AuthService
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.repository.AuthRepository
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Offline-First AuthRepository Implementation.
 *
 * - Source of truth: LocalDataSource (SQLDelight)
 * - Remote calls: Only for login, signup, logout
 * - Session state: SettingsRepository (SharedPrefs)
 * - Token refresh: Handled automatically by Supabase SDK
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val localDataSource: LocalDataSource,
    private val settingsRepository: SettingsRepository
) : AuthRepository {

    // Internal state to track current user ID for Flow switching
    private val _currentUserId = MutableStateFlow(settingsRepository.getCurrentUserId())

    /** Flow that emits the cached user from local DB */
    override val currentUser: Flow<User?> =
        _currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                localDataSource.getUser(userId)
            } else {
                flowOf(null)
            }
        }

    override val isLoggedIn: Flow<Boolean> = _currentUserId.map { it != null }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): Result<User> {
        return when (val result = authService.signUpWithEmail(email, password)) {
            is Result.Success -> {
                handleLoginSuccess(result.data)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return when (val result = authService.signInWithEmail(email, password)) {
            is Result.Success -> {
                handleLoginSuccess(result.data)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signInWithGoogle(idToken: String, nonce: String?): Result<User> {
        return when (val result = authService.signInWithGoogle(idToken, nonce)) {
            is Result.Success -> {
                handleLoginSuccess(result.data)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return when (val result = authService.signInWithApple(idToken, nonce)) {
            is Result.Success -> {
                handleLoginSuccess(result.data)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signOut(): Result<Unit> {
        // Clear local session state first (fast)
        settingsRepository.setLoggedIn(false)
        settingsRepository.setCurrentUserId(null)
        _currentUserId.value = null

        // Then call remote logout (can be slow/fail, but local state is already cleared)
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
        // Clear local data first
        _currentUserId.value?.let { localDataSource.deleteUser(it) }
        signOut()
        return Result.error(AppException.Unknown("Account deletion not yet implemented"))
    }

    override suspend fun refreshToken(): Result<Unit> {
        // Supabase SDK handles token refresh automatically
        return Result.success(Unit)
    }

    override fun isBiometricAvailable(): Boolean {
        return false
    }

    override suspend fun enableBiometric(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun authenticateWithBiometric(): Result<Boolean> {
        return Result.success(false)
    }

    /**
     * Handle successful login/signup:
     * 1. Cache user in local DB
     * 2. Set session flags
     * 3. Update internal state
     */
    private suspend fun handleLoginSuccess(user: User) {
        // 1. Cache user in local database
        localDataSource.insertUser(user)

        // 2. Set session flags for fast startup
        settingsRepository.setCurrentUserId(user.id)
        settingsRepository.setLoggedIn(true)

        // 3. Update internal Flow state
        _currentUserId.value = user.id
    }
}
