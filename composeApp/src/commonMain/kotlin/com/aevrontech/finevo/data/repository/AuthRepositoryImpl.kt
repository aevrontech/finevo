package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.model.UserTier
import com.aevrontech.finevo.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Stub implementation of AuthRepository for Phase 1.
 * TODO: Implement Supabase auth in Phase 2
 */
class AuthRepositoryImpl : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    
    override val currentUser: Flow<User?> = _currentUser
    
    override val isLoggedIn: Flow<Boolean> = _currentUser.map { it != null }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): Result<User> {
        // Simulate network delay
        delay(1000)
        
        // TODO: Implement Supabase auth
        val user = User(
            id = "user_${System.currentTimeMillis()}",
            email = email,
            displayName = displayName ?: email.substringBefore("@"),
            tier = UserTier.FREE,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        // Simulate network delay
        delay(1000)
        
        // TODO: Implement Supabase auth
        // For demo, accept any email/password
        val user = User(
            id = "user_demo",
            email = email,
            displayName = email.substringBefore("@"),
            tier = UserTier.FREE,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return Result.error(AppException.Unknown("Google Sign-In not implemented yet"))
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return Result.error(AppException.Unknown("Apple Sign-In not implemented yet"))
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    override suspend fun updateProfile(
        displayName: String?,
        avatarUrl: String?,
        country: String?,
        currency: String?
    ): Result<User> {
        val currentUser = _currentUser.value ?: return Result.error(AppException.Unauthorized)
        
        val updatedUser = currentUser.copy(
            displayName = displayName ?: currentUser.displayName,
            avatarUrl = avatarUrl ?: currentUser.avatarUrl,
            country = country ?: currentUser.country,
            currency = currency ?: currentUser.currency,
            updatedAt = Clock.System.now()
        )
        
        _currentUser.value = updatedUser
        return Result.success(updatedUser)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun refreshToken(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun isBiometricAvailable(): Boolean {
        // TODO: Platform-specific implementation
        return false
    }

    override suspend fun enableBiometric(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun authenticateWithBiometric(): Result<Boolean> {
        return Result.success(true)
    }
}
