package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Expense/Transaction repository interface
 */
interface ExpenseRepository {
    // ============================================
    // TRANSACTIONS
    // ============================================

    /**
     * Get all transactions for the current user
     */
    fun getTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions for a specific date range
     */
    fun getTransactions(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    /**
     * Get transactions for a specific date
     */
    fun getTransactionsForDate(date: LocalDate): Flow<List<Transaction>>

    /**
     * Get a single transaction by ID
     */
    suspend fun getTransaction(id: String): Result<Transaction>

    /**
     * Add a new transaction
     */
    suspend fun addTransaction(transaction: Transaction): Result<Transaction>

    /**
     * Update an existing transaction
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>

    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(id: String): Result<Unit>

    /**
     * Get transaction summary for a period
     */
    fun getTransactionSummary(startDate: LocalDate, endDate: LocalDate): Flow<TransactionSummary>

    // ============================================
    // CATEGORIES
    // ============================================

    /**
     * Get all categories
     */
    fun getCategories(): Flow<List<Category>>

    /**
     * Get categories by type (income/expense)
     */
    fun getCategories(type: TransactionType): Flow<List<Category>>

    /**
     * Add a custom category
     */
    suspend fun addCategory(category: Category): Result<Category>

    /**
     * Update a category
     */
    suspend fun updateCategory(category: Category): Result<Category>

    /**
     * Delete a custom category
     */
    suspend fun deleteCategory(id: String): Result<Unit>

    // ============================================
    // BUDGETS
    // ============================================

    /**
     * Get all budgets
     */
    fun getBudgets(): Flow<List<Budget>>

    /**
     * Get active budgets
     */
    fun getActiveBudgets(): Flow<List<Budget>>

    /**
     * Get a budget by ID
     */
    suspend fun getBudget(id: String): Result<Budget>

    /**
     * Add a new budget
     */
    suspend fun addBudget(budget: Budget): Result<Budget>

    /**
     * Update a budget
     */
    suspend fun updateBudget(budget: Budget): Result<Budget>

    /**
     * Delete a budget
     */
    suspend fun deleteBudget(id: String): Result<Unit>

    // ============================================
    // RECURRING TRANSACTIONS
    // ============================================

    /**
     * Get all recurring transactions
     */
    fun getRecurringTransactions(): Flow<List<RecurringTransaction>>

    /**
     * Add a recurring transaction
     */
    suspend fun addRecurringTransaction(recurring: RecurringTransaction): Result<RecurringTransaction>

    /**
     * Update a recurring transaction
     */
    suspend fun updateRecurringTransaction(recurring: RecurringTransaction): Result<RecurringTransaction>

    /**
     * Delete a recurring transaction
     */
    suspend fun deleteRecurringTransaction(id: String): Result<Unit>

    /**
     * Generate transactions from recurring templates
     */
    suspend fun processRecurringTransactions(): Result<List<Transaction>>

    // ============================================
    // SYNC
    // ============================================

    /**
     * Sync all transaction data with server
     */
    suspend fun sync(): Result<Unit>

    /**
     * Get pending changes count
     */
    fun getPendingChangesCount(): Flow<Int>
}
