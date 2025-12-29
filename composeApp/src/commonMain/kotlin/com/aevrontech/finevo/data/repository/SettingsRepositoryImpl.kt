package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.UserPreferences
import com.aevrontech.finevo.domain.repository.ExportFormat
import com.aevrontech.finevo.domain.repository.SettingsRepository
import com.aevrontech.finevo.domain.repository.StorageInfo
import com.aevrontech.finevo.domain.repository.UpdateInfo
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Stub implementation of SettingsRepository for Phase 1. */
class SettingsRepositoryImpl : SettingsRepository {

    private val _preferences = MutableStateFlow(UserPreferences(userId = ""))
    private val settings: Settings = Settings()

    override fun getPreferences(): Flow<UserPreferences> = _preferences

    override fun hasCompletedOnboarding(): Boolean {
        return settings.getBoolean("onboarding_completed", false)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settings.putBoolean("onboarding_completed", completed)
    }

    // ============================================
    // SESSION STATE (Offline-First)
    // ============================================

    override fun isLoggedIn(): Boolean {
        return settings.getBoolean("is_logged_in", false)
    }

    override fun setLoggedIn(loggedIn: Boolean) {
        settings.putBoolean("is_logged_in", loggedIn)
    }

    override fun getCurrentUserId(): String? {
        return settings.getStringOrNull("current_user_id")
    }

    override fun setCurrentUserId(userId: String?) {
        if (userId == null) {
            settings.remove("current_user_id")
        } else {
            settings.putString("current_user_id", userId)
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> {
        _preferences.value = preferences
        return Result.success(preferences)
    }

    override suspend fun setCurrency(currencyCode: String): Result<Unit> {
        _preferences.value = _preferences.value.copy(currency = currencyCode)
        return Result.success(Unit)
    }

    override suspend fun setLocale(locale: String): Result<Unit> {
        _preferences.value = _preferences.value.copy(locale = locale)
        return Result.success(Unit)
    }

    override suspend fun setDarkMode(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(darkMode = enabled)
        return Result.success(Unit)
    }

    override suspend fun setUseDecimals(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(useDecimals = enabled)
        return Result.success(Unit)
    }

    override suspend fun setPinEnabled(enabled: Boolean, pin: String?): Result<Unit> {
        _preferences.value = _preferences.value.copy(pinEnabled = enabled)
        return Result.success(Unit)
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun changePin(oldPin: String, newPin: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(biometricEnabled = enabled)
        return Result.success(Unit)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(notificationsEnabled = enabled)
        return Result.success(Unit)
    }

    override suspend fun setBudgetAlerts(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(budgetAlerts = enabled)
        return Result.success(Unit)
    }

    override suspend fun setPaymentReminders(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(paymentReminders = enabled)
        return Result.success(Unit)
    }

    override suspend fun setHabitReminders(enabled: Boolean): Result<Unit> {
        _preferences.value = _preferences.value.copy(habitReminders = enabled)
        return Result.success(Unit)
    }

    override suspend fun exportData(format: ExportFormat): Result<String> {
        return Result.success("export_${System.currentTimeMillis()}.${format.name.lowercase()}")
    }

    override suspend fun clearLocalData(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getStorageUsage(): Result<StorageInfo> {
        return Result.success(
            StorageInfo(
                usedBytes = 1024 * 1024,
                totalBytes = 500 * 1024 * 1024,
                transactionCount = 0,
                debtCount = 0,
                habitCount = 0
            )
        )
    }

    override fun getAppVersion(): String = "1.0.0"

    override suspend fun checkForUpdate(): Result<UpdateInfo?> {
        return Result.success(null)
    }

    override suspend fun getMinimumRequiredVersion(): Result<String> {
        return Result.success("1.0.0")
    }
}
