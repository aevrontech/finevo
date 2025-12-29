package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/** Settings repository interface */
interface SettingsRepository {
    // ============================================
    // USER PREFERENCES
    // ============================================

    /** Get user preferences */
    fun getPreferences(): Flow<UserPreferences>

    /** Check if onboarding has been completed */
    fun hasCompletedOnboarding(): Boolean

    /** Set onboarding completion status */
    suspend fun setOnboardingCompleted(completed: Boolean)

    // ============================================
    // SESSION STATE (Offline-First)
    // ============================================

    /** Check if user is logged in (fast local check for startup) */
    fun isLoggedIn(): Boolean

    /** Set login status */
    fun setLoggedIn(loggedIn: Boolean)

    /** Get current cached user ID */
    fun getCurrentUserId(): String?

    /** Set current user ID for cache lookup */
    fun setCurrentUserId(userId: String?)

    /** Update user preferences */
    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences>

    /** Set currency */
    suspend fun setCurrency(currencyCode: String): Result<Unit>

    /** Set locale */
    suspend fun setLocale(locale: String): Result<Unit>

    /** Toggle dark mode */
    suspend fun setDarkMode(enabled: Boolean): Result<Unit>

    /** Toggle decimal display */
    suspend fun setUseDecimals(enabled: Boolean): Result<Unit>

    // ============================================
    // SECURITY
    // ============================================

    /** Enable/disable PIN */
    suspend fun setPinEnabled(enabled: Boolean, pin: String? = null): Result<Unit>

    /** Verify PIN */
    suspend fun verifyPin(pin: String): Result<Boolean>

    /** Change PIN */
    suspend fun changePin(oldPin: String, newPin: String): Result<Unit>

    /** Enable/disable biometric */
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>

    // ============================================
    // NOTIFICATIONS
    // ============================================

    /** Set notifications enabled */
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>

    /** Set budget alerts enabled */
    suspend fun setBudgetAlerts(enabled: Boolean): Result<Unit>

    /** Set payment reminders enabled */
    suspend fun setPaymentReminders(enabled: Boolean): Result<Unit>

    /** Set habit reminders enabled */
    suspend fun setHabitReminders(enabled: Boolean): Result<Unit>

    // ============================================
    // DATA MANAGEMENT
    // ============================================

    /** Export all data */
    suspend fun exportData(format: ExportFormat): Result<String>

    /** Clear all local data */
    suspend fun clearLocalData(): Result<Unit>

    /** Get storage usage */
    suspend fun getStorageUsage(): Result<StorageInfo>

    // ============================================
    // APP INFO
    // ============================================

    /** Get app version */
    fun getAppVersion(): String

    /** Check for updates */
    suspend fun checkForUpdate(): Result<UpdateInfo?>

    /** Get minimum required version */
    suspend fun getMinimumRequiredVersion(): Result<String>
}

/** Export format options */
enum class ExportFormat {
    CSV,
    EXCEL,
    PDF,
    JSON
}

/** Storage information */
data class StorageInfo(
    val usedBytes: Long,
    val totalBytes: Long,
    val transactionCount: Int,
    val debtCount: Int,
    val habitCount: Int
) {
    val usedMb: Double
        get() = usedBytes / (1024.0 * 1024.0)
    val percentUsed: Double
        get() = if (totalBytes > 0) usedBytes.toDouble() / totalBytes * 100 else 0.0
}

/** Update information */
data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val minimumVersion: String,
    val isForceUpdate: Boolean,
    val updateUrl: String,
    val releaseNotes: String?
)
