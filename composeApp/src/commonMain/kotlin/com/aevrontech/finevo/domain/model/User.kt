package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * User domain model
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val currency: String = "USD",
    val tier: UserTier = UserTier.FREE,
    val isMalaysian: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * User subscription tier
 */
@Serializable
enum class UserTier {
    FREE,
    PREMIUM,
    FAMILY,
    FAMILY_MEMBER
}

/**
 * User preferences/settings
 */
@Serializable
data class UserPreferences(
    val userId: String,
    val currency: String = "USD",
    val locale: String = "en",
    val useDecimals: Boolean = true,
    val darkMode: Boolean = true,
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val budgetAlerts: Boolean = true,
    val paymentReminders: Boolean = true,
    val habitReminders: Boolean = true,
    val weeklyReport: Boolean = true,
    val firstDayOfWeek: Int = 1 // 1 = Monday, 7 = Sunday
)
