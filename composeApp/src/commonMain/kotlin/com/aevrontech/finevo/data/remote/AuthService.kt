package com.aevrontech.finevo.data.remote

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.model.UserTier
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Service layer for Supabase authentication. Handles email/password auth, social login, and session
 * management.
 */
class AuthService {

    private val auth: Auth?
        get() = if (SupabaseConfig.isInitialized) SupabaseConfig.client.auth else null

    /** Observe authentication state changes. */
    fun observeAuthState(): Flow<AuthState> {
        val authClient = auth ?: return kotlinx.coroutines.flow.flowOf(AuthState.NotAuthenticated)

        return authClient.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
                        AuthState.Authenticated(user.toDomainUser())
                    } else {
                        AuthState.NotAuthenticated
                    }
                }
                is SessionStatus.NotAuthenticated -> AuthState.NotAuthenticated
                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.RefreshFailure -> AuthState.NotAuthenticated
            }
        }
    }

    /** Sign in with email and password. */
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        val authClient =
            auth
                ?: return Result.error(
                    AppException.ServerError(
                        503,
                        "Service unavailable. Please check your connection."
                    )
                )

        return try {
            authClient.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = authClient.currentUserOrNull()
            if (user != null) {
                Result.success(user.toDomainUser())
            } else {
                Result.error(AppException.InvalidCredentials)
            }
        } catch (e: Exception) {
            Result.error(parseAuthException(e, "sign in"))
        }
    }

    /** Sign up with email and password. */
    suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        val authClient =
            auth
                ?: return Result.error(
                    AppException.ServerError(
                        503,
                        "Service unavailable. Please check your connection."
                    )
                )

        return try {
            authClient.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val user = authClient.currentUserOrNull()
            if (user != null) {
                Result.success(user.toDomainUser())
            } else {
                // Email confirmation might be required
                Result.error(AppException.EmailNotVerified)
            }
        } catch (e: Exception) {
            Result.error(parseAuthException(e, "sign up"))
        }
    }

    /**
     * Sign in with Google using ID Token and optional nonce. The ID token should be obtained from
     * Google Sign-In SDK on the platform. Nonce is required when using Credential Manager (contains
     * nonce in token).
     */
    suspend fun signInWithGoogle(idToken: String, nonce: String? = null): Result<User> {
        val authClient =
            auth
                ?: return Result.error(
                    AppException.ServerError(
                        503,
                        "Service unavailable. Please check your connection."
                    )
                )

        return try {
            println("DEBUG: Attempting Google sign-in with Supabase...")
            println("DEBUG: ID Token (first 50 chars): ${idToken.take(50)}...")
            println("DEBUG: Nonce provided: ${nonce != null}")
            if (nonce != null) {
                println("DEBUG: Nonce (first 20 chars): ${nonce.take(20)}...")
            }

            authClient.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                // Pass the raw nonce if provided (required for Credential Manager tokens)
                if (nonce != null) {
                    this.nonce = nonce
                }
            }

            val user = authClient.currentUserOrNull()
            if (user != null) {
                println("DEBUG: Google sign-in successful! User: ${user.email}")
                Result.success(user.toDomainUser())
            } else {
                println("DEBUG: Google sign-in returned no user")
                Result.error(AppException.Unknown("Google sign-in failed. Please try again."))
            }
        } catch (e: Exception) {
            println("DEBUG: Google sign-in exception: ${e::class.simpleName}")
            println("DEBUG: Exception message: ${e.message}")
            e.printStackTrace()
            Result.error(parseAuthException(e, "Google sign-in"))
        }
    }

    /**
     * Sign in with Apple using ID Token. The ID token should be obtained from Apple Sign-In SDK on
     * the platform.
     */
    suspend fun signInWithApple(idToken: String, nonce: String? = null): Result<User> {
        val authClient =
            auth
                ?: return Result.error(
                    AppException.ServerError(
                        503,
                        "Service unavailable. Please check your connection."
                    )
                )

        return try {
            authClient.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Apple
                nonce?.let { this.nonce = it }
            }

            val user = authClient.currentUserOrNull()
            if (user != null) {
                Result.success(user.toDomainUser())
            } else {
                Result.error(AppException.Unknown("Apple sign-in failed. Please try again."))
            }
        } catch (e: Exception) {
            Result.error(parseAuthException(e, "Apple sign-in"))
        }
    }

    /** Sign out the current user. */
    suspend fun signOut(): Result<Unit> {
        val authClient = auth ?: return Result.success(Unit)

        return try {
            authClient.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.Unknown(e.message ?: "Sign out failed"))
        }
    }

    /** Get the current user if authenticated. */
    fun getCurrentUser(): User? {
        val authClient = auth ?: return null
        return authClient.currentUserOrNull()?.toDomainUser()
    }

    /** Check if user is currently authenticated. */
    fun isAuthenticated(): Boolean {
        val authClient = auth ?: return false
        return authClient.currentUserOrNull() != null
    }

    /** Send password reset email. */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val authClient =
            auth
                ?: return Result.error(
                    AppException.ServerError(
                        503,
                        "Service unavailable. Please check your connection."
                    )
                )

        return try {
            authClient.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(parseAuthException(e, "password reset"))
        }
    }

    /** Parse authentication exceptions into user-friendly AppException. */
    private fun parseAuthException(e: Exception, action: String): AppException {
        val message = e.message ?: "Unknown error"

        return when {
            // Invalid credentials
            message.contains("Invalid", ignoreCase = true) &&
                message.contains("credentials", ignoreCase = true) ->
                AppException.InvalidCredentials

            // Email not verified
            message.contains("email", ignoreCase = true) &&
                message.contains("confirm", ignoreCase = true) -> AppException.EmailNotVerified

            // Email already exists
            message.contains("already registered", ignoreCase = true) ||
                message.contains("already exists", ignoreCase = true) ->
                AppException.EmailAlreadyExists

            // Weak password
            message.contains("password", ignoreCase = true) &&
                (message.contains("weak", ignoreCase = true) ||
                    message.contains("short", ignoreCase = true) ||
                    message.contains("at least", ignoreCase = true)) ->
                AppException.WeakPassword

            // Email sending errors (Supabase SMTP not configured)
            message.contains("sending", ignoreCase = true) &&
                message.contains("email", ignoreCase = true) ->
                AppException.ServerError(
                    500,
                    "Unable to send email. Please try again later or contact support."
                )

            // OAuth/Social login errors
            message.contains("oauth", ignoreCase = true) ||
                message.contains("provider", ignoreCase = true) ->
                AppException.ServerError(
                    400,
                    "Social login is not configured. Please use email sign-in or contact support."
                )

            // Rate limiting
            message.contains("rate", ignoreCase = true) ||
                message.contains("too many", ignoreCase = true) ->
                AppException.ServerError(
                    429,
                    "Too many attempts. Please wait a moment and try again."
                )

            // Network errors
            message.contains("network", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ->
                AppException.NetworkUnavailable

            // Generic server error
            message.contains("server", ignoreCase = true) ||
                message.contains("500", ignoreCase = true) ->
                AppException.ServerError(500, "Server error. Please try again later.")

            // Fallback: show a user-friendly message
            else ->
                AppException.Unknown(
                    "Unable to $action. Please check your connection and try again."
                )
        }
    }
}

/** Authentication state sealed class. */
sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}

/** Extension to convert Supabase UserInfo to domain User. */
private fun UserInfo.toDomainUser(): User {
    return User(
        id = id,
        email = email ?: "",
        displayName = userMetadata?.get("display_name")?.toString()
            ?: userMetadata?.get("full_name")?.toString()
            ?: userMetadata?.get("name")?.toString(),
        avatarUrl = userMetadata?.get("avatar_url")?.toString()
            ?: userMetadata?.get("picture")?.toString(),
        tier = UserTier.FREE,
        country = userMetadata?.get("country")?.toString() ?: "MY",
        currency = userMetadata?.get("currency")?.toString() ?: "MYR",
        createdAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis()),
        updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
    )
}
