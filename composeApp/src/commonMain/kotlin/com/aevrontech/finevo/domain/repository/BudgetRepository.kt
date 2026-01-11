package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Repository interface for Budget operations */
interface BudgetRepository {
    /** Get all budgets for a user */
    fun getAllBudgets(userId: String): Flow<List<Budget>>

    /** Get active budgets for a user */
    fun getActiveBudgets(userId: String): Flow<List<Budget>>

    /** Get a budget by ID */
    suspend fun getBudgetById(id: String): Budget?

    /** Get a budget stream by ID */
    fun getBudgetFlow(id: String): Flow<Budget?>

    /** Get budget for a specific category */
    suspend fun getBudgetByCategory(userId: String, categoryId: String): Budget?

    /** Add a new budget */
    suspend fun addBudget(budget: Budget)

    /** Update an existing budget */
    suspend fun updateBudget(budget: Budget)

    /** Update the spent amount for a budget */
    suspend fun updateBudgetSpent(budgetId: String, spent: Double)

    /** Delete a budget */
    suspend fun deleteBudget(id: String)

    /** Recalculate spent amounts for all budgets based on transactions */
    suspend fun recalculateAllBudgets(userId: String)

    /** Calculate the date range for a budget period */
    fun getBudgetPeriodDates(
        period: BudgetPeriod,
        startDate: LocalDate,
        endDate: LocalDate?, // For ONCE period
        today: LocalDate,
        periodOffset: Int = 0 // 0 = current, -1 = previous, etc.
    ): Pair<LocalDate, LocalDate>
}
