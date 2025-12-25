package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/** ExpenseRepository implementation using SQLDelight for local storage. */
class ExpenseRepositoryImpl(private val localDataSource: LocalDataSource) : ExpenseRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return localDataSource.getTransactions()
    }

    override fun getTransactions(
            startDate: LocalDate,
            endDate: LocalDate
    ): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByDateRange(startDate, endDate)
    }

    override fun getTransactionsForDate(date: LocalDate): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByDateRange(date, date)
    }

    override fun getTransactionsByAccount(
            accountId: String,
            startDate: LocalDate,
            endDate: LocalDate
    ): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByAccountAndDateRange(accountId, startDate, endDate)
    }

    override suspend fun getTransaction(id: String): Result<Transaction> {
        return try {
            val transaction = localDataSource.getTransactions().first().find { it.id == id }

            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.error(AppException.NotFound("Transaction"))
            }
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            localDataSource.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add transaction"))
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            // Use insert with same ID (INSERT OR REPLACE)
            localDataSource.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to update transaction"))
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            localDataSource.deleteTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to delete transaction"))
        }
    }

    override fun getTransactionSummary(
            startDate: LocalDate,
            endDate: LocalDate
    ): Flow<TransactionSummary> {
        return getTransactions(startDate, endDate).map { transactions ->
            val income =
                    transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense =
                    transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            TransactionSummary(
                    totalIncome = income,
                    totalExpense = expense,
                    netAmount = income - expense,
                    transactionCount = transactions.size,
                    topCategories = emptyList() // TODO: Calculate
            )
        }
    }

    override fun getCategories(): Flow<List<Category>> {
        return localDataSource.getCategories()
    }

    override fun getCategories(type: TransactionType): Flow<List<Category>> {
        return localDataSource.getCategoriesByType(type)
    }

    override suspend fun addCategory(category: Category): Result<Category> {
        return try {
            localDataSource.insertCategory(category)
            Result.success(category)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add category"))
        }
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        return try {
            localDataSource.insertCategory(category) // Upsert
            Result.success(category)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to update category"))
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return Result.success(Unit) // TODO: Implement delete
    }

    override fun getBudgets(): Flow<List<Budget>> {
        return localDataSource.getBudgets()
    }

    override fun getActiveBudgets(): Flow<List<Budget>> {
        return localDataSource.getBudgets().map { list -> list.filter { it.isActive } }
    }

    override suspend fun getBudget(id: String): Result<Budget> {
        return try {
            val budget = localDataSource.getBudgets().first().find { it.id == id }

            if (budget != null) Result.success(budget)
            else Result.error(AppException.NotFound("Budget"))
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun addBudget(budget: Budget): Result<Budget> {
        return try {
            localDataSource.insertBudget(budget)
            Result.success(budget)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add budget"))
        }
    }

    override suspend fun updateBudget(budget: Budget): Result<Budget> {
        return try {
            localDataSource.insertBudget(budget) // Upsert
            Result.success(budget)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to update budget"))
        }
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return flowOf(emptyList()) // TODO: Implement
    }

    override suspend fun addRecurringTransaction(
            recurring: RecurringTransaction
    ): Result<RecurringTransaction> {
        return Result.success(recurring)
    }

    override suspend fun updateRecurringTransaction(
            recurring: RecurringTransaction
    ): Result<RecurringTransaction> {
        return Result.success(recurring)
    }

    override suspend fun deleteRecurringTransaction(id: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun processRecurringTransactions(): Result<List<Transaction>> {
        return Result.success(emptyList())
    }

    override suspend fun sync(): Result<Unit> = Result.success(Unit)

    override fun getPendingChangesCount(): Flow<Int> = flowOf(0)

    /** Initialize default categories if none exist. */
    suspend fun initializeDefaultCategories() {
        val categories = localDataSource.getCategories().first()
        if (categories.isEmpty()) {
            localDataSource.insertDefaultCategories()
        }
    }
}
