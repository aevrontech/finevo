package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Debt repository interface
 */
interface DebtRepository {
    // ============================================
    // DEBTS
    // ============================================

    /**
     * Get all debts for the current user
     */
    fun getDebts(): Flow<List<Debt>>

    /**
     * Get active debts (not paid off)
     */
    fun getActiveDebts(): Flow<List<Debt>>

    /**
     * Get a single debt by ID
     */
    suspend fun getDebt(id: String): Result<Debt>

    /**
     * Add a new debt
     */
    suspend fun addDebt(debt: Debt): Result<Debt>

    /**
     * Update an existing debt
     */
    suspend fun updateDebt(debt: Debt): Result<Debt>

    /**
     * Delete a debt
     */
    suspend fun deleteDebt(id: String): Result<Unit>

    /**
     * Mark debt as paid off
     */
    suspend fun markAsPaidOff(id: String): Result<Debt>

    // ============================================
    // PAYMENTS
    // ============================================

    /**
     * Get payments for a debt
     */
    fun getPayments(debtId: String): Flow<List<DebtPayment>>

    /**
     * Get all payments
     */
    fun getAllPayments(): Flow<List<DebtPayment>>

    /**
     * Add a payment to a debt
     */
    suspend fun addPayment(payment: DebtPayment): Result<DebtPayment>

    /**
     * Delete a payment
     */
    suspend fun deletePayment(id: String): Result<Unit>

    // ============================================
    // PAYOFF CALCULATIONS
    // ============================================

    /**
     * Calculate payoff plan with a specific strategy
     */
    suspend fun calculatePayoffPlan(
        strategy: PayoffStrategy,
        extraMonthlyPayment: Double = 0.0
    ): Result<PayoffPlan>

    /**
     * Calculate what-if scenario
     */
    suspend fun calculateWhatIf(
        debtId: String,
        extraPayment: Double
    ): Result<WhatIfScenario>

    /**
     * Get amortization schedule for a debt
     */
    suspend fun getAmortizationSchedule(debtId: String): Result<List<MonthlyPayment>>

    // ============================================
    // BILLS
    // ============================================

    /**
     * Get all bills
     */
    fun getBills(): Flow<List<Bill>>

    /**
     * Add a bill
     */
    suspend fun addBill(bill: Bill): Result<Bill>

    /**
     * Update a bill
     */
    suspend fun updateBill(bill: Bill): Result<Bill>

    /**
     * Delete a bill
     */
    suspend fun deleteBill(id: String): Result<Unit>

    /**
     * Mark bill as paid
     */
    suspend fun markBillPaid(id: String, date: LocalDate): Result<Bill>

    /**
     * Get upcoming bills for the next N days
     */
    fun getUpcomingBills(days: Int): Flow<List<Bill>>

    // ============================================
    // SUMMARY
    // ============================================

    /**
     * Get total debt amount
     */
    fun getTotalDebt(): Flow<Double>

    /**
     * Get estimated debt-free date
     */
    suspend fun getEstimatedDebtFreeDate(): Result<LocalDate?>

    // ============================================
    // SYNC
    // ============================================

    /**
     * Sync all debt data with server
     */
    suspend fun sync(): Result<Unit>
}
