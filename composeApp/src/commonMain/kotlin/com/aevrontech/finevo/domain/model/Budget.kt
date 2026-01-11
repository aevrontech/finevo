package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Budget period type */
@Serializable
enum class BudgetPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    ONCE("Once")
}

/** Budget domain model */
@Serializable
data class Budget(
    val id: String,
    val userId: String,
    val name: String? = null, // Budget display name
    val categoryId: String, // Primary category (for backwards compatibility)
    val categoryIds: List<String> = emptyList(), // Multi-category support
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val accountIds: List<String> = emptyList(), // Empty = all accounts
    val amount: Double,
    val spent: Double = 0.0,
    val period: BudgetPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate? = null, // End date for ONCE period budgets
    val alertThreshold: Int = 80, // Alert when X% spent
    val rollover: Boolean = false, // Unused budget rolls over
    val notifyOverspent: Boolean = true, // Notify when budget exceeded
    val notifyRisk: Boolean = true, // Notify when trending to overspend
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
    val transactionCount: Int = 0 // Number of transactions in the current period
) {
    /** Display name - use name if set, otherwise category name */
    val displayName: String
        get() = name ?: categoryName ?: "Budget"

    /** Percentage of budget used (0-100+) */
    val percentUsed: Double
        get() = if (amount > 0) (spent / amount) * 100 else 0.0

    /** Remaining budget amount */
    val remaining: Double
        get() = (amount - spent).coerceAtLeast(0.0)

    /** Amount over budget (0 if under) */
    val overAmount: Double
        get() = (spent - amount).coerceAtLeast(0.0)

    /** Is spending over the budget limit */
    val isOverBudget: Boolean
        get() = spent > amount

    /** Is spending near the alert threshold */
    val isNearThreshold: Boolean
        get() = percentUsed >= alertThreshold && !isOverBudget

    /** Status for UI display */
    val status: BudgetStatus
        get() =
            when {
                isOverBudget -> BudgetStatus.OVER
                isNearThreshold -> BudgetStatus.WARNING
                else -> BudgetStatus.ON_TRACK
            }

    /** Check if tracking all accounts */
    val isAllAccounts: Boolean
        get() = accountIds.isEmpty()

    /** Check if tracking all categories (using multi-category) */
    val isMultiCategory: Boolean
        get() = categoryIds.isNotEmpty()
}

/** Budget status for UI styling */
enum class BudgetStatus {
    ON_TRACK, // Green - under threshold
    WARNING, // Orange - near threshold
    OVER // Red - over budget
}
