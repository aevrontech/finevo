package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Transaction type - income or expense
 */
@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Transaction/Expense domain model
 */
@Serializable
data class Transaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val categoryId: String,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val description: String? = null,
    val note: String? = null,
    val date: LocalDate,
    val isRecurring: Boolean = false,
    val recurringId: String? = null,
    val receiptUrl: String? = null,
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Category for transactions
 */
@Serializable
data class Category(
    val id: String,
    val userId: String? = null, // null = system default category
    val name: String,
    val icon: String, // Emoji or icon name
    val color: String, // Hex color
    val type: TransactionType,
    val isDefault: Boolean = false,
    val order: Int = 0
)

/**
 * Recurring transaction configuration
 */
@Serializable
data class RecurringTransaction(
    val id: String,
    val userId: String,
    val transactionTemplate: Transaction,
    val frequency: RecurringFrequency,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val lastGeneratedDate: LocalDate? = null,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Recurring frequency options
 */
@Serializable
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

/**
 * Budget for a category
 */
@Serializable
data class Budget(
    val id: String,
    val userId: String,
    val categoryId: String,
    val categoryName: String? = null,
    val amount: Double,
    val spent: Double = 0.0,
    val period: BudgetPeriod,
    val startDate: LocalDate,
    val alertThreshold: Int = 80, // Percentage (0-100)
    val rollover: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val remaining: Double get() = amount - spent
    val percentUsed: Double get() = if (amount > 0) (spent / amount) * 100 else 0.0
    val isOverBudget: Boolean get() = spent > amount
    val isNearLimit: Boolean get() = percentUsed >= alertThreshold
}

/**
 * Budget period options
 */
@Serializable
enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Summary of transactions for a period
 */
data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netAmount: Double,
    val transactionCount: Int,
    val topCategories: List<CategorySummary>
)

/**
 * Category spending summary
 */
data class CategorySummary(
    val category: Category,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)
