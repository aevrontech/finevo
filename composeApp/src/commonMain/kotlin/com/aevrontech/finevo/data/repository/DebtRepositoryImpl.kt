package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Bill
import com.aevrontech.finevo.domain.model.Debt
import com.aevrontech.finevo.domain.model.DebtPayment
import com.aevrontech.finevo.domain.model.DebtPayoffInfo
import com.aevrontech.finevo.domain.model.MonthlyPayment
import com.aevrontech.finevo.domain.model.PayoffPlan
import com.aevrontech.finevo.domain.model.PayoffStrategy
import com.aevrontech.finevo.domain.model.WhatIfScenario
import com.aevrontech.finevo.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** DebtRepository implementation using SQLDelight for local storage. */
class DebtRepositoryImpl(private val localDataSource: LocalDataSource) : DebtRepository {

    override fun getDebts(): Flow<List<Debt>> {
        return localDataSource.getDebts()
    }

    override fun getActiveDebts(): Flow<List<Debt>> {
        return localDataSource.getActiveDebts()
    }

    override suspend fun getDebt(id: String): Result<Debt> {
        return try {
            val debt = localDataSource.getDebts().first().find { it.id == id }

            if (debt != null) Result.success(debt) else Result.error(AppException.NotFound("Debt"))
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun addDebt(debt: Debt): Result<Debt> {
        return try {
            localDataSource.insertDebt(debt)
            Result.success(debt)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add debt"))
        }
    }

    override suspend fun updateDebt(debt: Debt): Result<Debt> {
        return try {
            localDataSource.insertDebt(debt) // Uses INSERT OR REPLACE
            Result.success(debt)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to update debt"))
        }
    }

    override suspend fun deleteDebt(id: String): Result<Unit> {
        return try {
            localDataSource.deleteDebt(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to delete debt"))
        }
    }

    override suspend fun markAsPaidOff(id: String): Result<Debt> {
        return try {
            val debt = localDataSource.getDebts().first().find { it.id == id }
                ?: return Result.error(AppException.NotFound("Debt"))

            val paidOff = debt.copy(
                isPaidOff = true,
                currentBalance = 0.0,
                updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
            )
            localDataSource.insertDebt(paidOff)
            Result.success(paidOff)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to mark debt as paid"))
        }
    }

    override fun getPayments(debtId: String): Flow<List<DebtPayment>> {
        return localDataSource.getPaymentsForDebt(debtId)
    }

    override fun getAllPayments(): Flow<List<DebtPayment>> {
        return flowOf(emptyList()) // TODO: Implement getAllPayments in LocalDataSource
    }

    override suspend fun addPayment(payment: DebtPayment): Result<DebtPayment> {
        return try {
            localDataSource.insertDebtPayment(payment)

            // Update debt balance
            val debt = localDataSource.getDebts().first().find { it.id == payment.debtId }
            if (debt != null) {
                val newBalance = (debt.currentBalance - payment.principalAmount).coerceAtLeast(0.0)
                val updated = debt.copy(
                    currentBalance = newBalance,
                    isPaidOff = newBalance == 0.0,
                    updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                )
                localDataSource.insertDebt(updated)
            }

            Result.success(payment)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add payment"))
        }
    }

    override suspend fun deletePayment(id: String): Result<Unit> {
        return Result.success(Unit) // TODO: Implement
    }

    override suspend fun calculatePayoffPlan(
        strategy: PayoffStrategy, extraMonthlyPayment: Double): Result<PayoffPlan> {
        return try {
            val debts = localDataSource.getActiveDebts().first()

            if (debts.isEmpty()) {
                return Result.error(
                    AppException.ValidationError("validation", "No active debts to calculate")
                )
            }

            // Sort by strategy
            val sortedDebts = when (strategy) {
                PayoffStrategy.AVALANCHE -> debts.sortedByDescending { it.interestRate }
                PayoffStrategy.SNOWBALL -> debts.sortedBy { it.currentBalance }
                PayoffStrategy.CUSTOM -> debts.sortedBy { it.priority }
            }

            // Simple calculation
            val totalDebt = debts.sumOf { it.currentBalance }
            val monthlyPayment = debts.sumOf { it.minimumPayment } + extraMonthlyPayment
            val estimatedMonths = if (monthlyPayment > 0) ((totalDebt / monthlyPayment) + 1).toInt() else 12

            val today = getCurrentLocalDate()
            val payoffDate = today.plus(DatePeriod(months = estimatedMonths))

            Result.success(
                PayoffPlan(
                    strategy = strategy,
                    debts = sortedDebts.mapIndexed { index, debt ->
                        DebtPayoffInfo(
                            debt = debt,
                            payoffOrder = index + 1,
                            payoffDate = payoffDate,
                            totalInterest = debt.currentBalance * (debt.interestRate / 100) * (estimatedMonths / 12.0),
                            monthlyPayments = emptyList()
                        )
                    },
                    monthlyPayment = monthlyPayment,
                    totalInterestSaved = 0.0,
                    payoffDate = payoffDate,
                    totalMonths = estimatedMonths
                )
            )
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to calculate payoff plan"))
        }
    }

    override suspend fun calculateWhatIf(
        debtId: String, extraPayment: Double): Result<WhatIfScenario> {
        return Result.error(AppException.Unknown("Not implemented"))
    }

    override suspend fun getAmortizationSchedule(debtId: String): Result<List<MonthlyPayment>> {
        return Result.success(emptyList())
    }

    override fun getBills(): Flow<List<Bill>> {
        return flowOf(emptyList()) // TODO: Implement
    }

    override suspend fun addBill(bill: Bill): Result<Bill> {
        return Result.success(bill)
    }

    override suspend fun updateBill(bill: Bill): Result<Bill> {
        return Result.success(bill)
    }

    override suspend fun deleteBill(id: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun markBillPaid(id: String, date: LocalDate): Result<Bill> {
        return Result.error(AppException.NotFound("Bill"))
    }

    override fun getUpcomingBills(days: Int): Flow<List<Bill>> {
        return flowOf(emptyList())
    }

    override fun getTotalDebt(): Flow<Double> {
        return localDataSource.getDebts().map { debts ->
            debts.filter { it.isActive && !it.isPaidOff }.sumOf { it.currentBalance }
        }
    }

    override suspend fun getEstimatedDebtFreeDate(): Result<LocalDate?> {
        return Result.success(null)
    }

    override suspend fun sync(): Result<Unit> = Result.success(Unit)
}
