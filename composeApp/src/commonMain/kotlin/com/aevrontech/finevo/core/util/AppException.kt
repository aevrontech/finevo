package com.aevrontech.finevo.core.util

/**
 * Base class for all application exceptions.
 * These are localized-friendly exceptions that can be displayed to users.
 */
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    // ============================================
    // NETWORK ERRORS
    // ============================================

    /**
     * No internet connection available
     */
    data object NetworkUnavailable : AppException("No internet connection")

    /**
     * Request timed out
     */
    data object Timeout : AppException("Request timed out. Please try again.")

    /**
     * Server returned an error
     */
    data class ServerError(
        val code: Int,
        override val message: String = "Server error. Please try again later."
    ) : AppException(message)

    // ============================================
    // AUTHENTICATION ERRORS
    // ============================================

    /**
     * User is not authenticated
     */
    data object Unauthorized : AppException("Please log in to continue")

    /**
     * Session has expired
     */
    data object SessionExpired : AppException("Your session has expired. Please log in again.")

    /**
     * Invalid credentials provided
     */
    data object InvalidCredentials : AppException("Invalid email or password")

    /**
     * Email already registered
     */
    data object EmailAlreadyExists : AppException("This email is already registered")

    /**
     * Weak password
     */
    data object WeakPassword : AppException("Password is too weak. Use at least 8 characters with numbers and symbols.")

    /**
     * Email not verified
     */
    data object EmailNotVerified : AppException("Please verify your email to continue")

    // ============================================
    // VALIDATION ERRORS
    // ============================================

    /**
     * Generic validation error
     */
    data class ValidationError(
        val field: String,
        override val message: String
    ) : AppException(message)

    /**
     * Empty required field
     */
    data class RequiredField(val field: String) : AppException("$field is required")

    /**
     * Invalid format
     */
    data class InvalidFormat(
        val field: String,
        val expectedFormat: String
    ) : AppException("Invalid $field format. Expected: $expectedFormat")

    // ============================================
    // DATA ERRORS
    // ============================================

    /**
     * Resource not found
     */
    data class NotFound(val resource: String) : AppException("$resource not found")

    /**
     * Conflict with existing data
     */
    data class Conflict(override val message: String) : AppException(message)

    /**
     * Database error
     */
    data class DatabaseError(
        override val message: String = "Database error. Please try again.",
        override val cause: Throwable? = null
    ) : AppException(message, cause)

    // ============================================
    // SYNC ERRORS
    // ============================================

    /**
     * Sync failed
     */
    data class SyncFailed(
        override val message: String = "Sync failed. Your data is saved locally.",
        override val cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Conflict during sync
     */
    data class SyncConflict(
        override val message: String = "Sync conflict detected. Please resolve manually."
    ) : AppException(message)

    // ============================================
    // FEATURE ERRORS
    // ============================================

    /**
     * Feature is premium-only
     */
    data class PremiumRequired(
        val feature: String
    ) : AppException("$feature is a premium feature. Upgrade to unlock.")

    /**
     * Feature limit reached
     */
    data class LimitReached(
        val feature: String,
        val limit: Int
    ) : AppException("You've reached the limit of $limit for $feature. Upgrade to premium for unlimited access.")

    // ============================================
    // GENERAL ERRORS
    // ============================================

    /**
     * Unknown error (catch-all)
     */
    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Operation cancelled by user
     */
    data object Cancelled : AppException("Operation cancelled")
}
