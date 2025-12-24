package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Debt type
 */
@Serializable
enum class DebtType {
    CREDIT_CARD,
    CAR_LOAN,
    HOUSING_LOAN,
    PERSONAL_LOAN,
    STUDENT_LOAN,
    MEDICAL_DEBT,
    OTHER
}

/**
 * Debt domain model
 */
@Serializable
data class Debt(
    val id: String,
    val userId: String,
    val name: String,
    val type: DebtType,
    val originalAmount: Double,
    val currentBalance: Double,
    val interestRate: Double, // Annual percentage rate
    val minimumPayment: Double,
    val dueDay: Int, // Day of month (1-31)
    val lenderName: String? = null,
    val accountNumber: String? = null,
    val notes: String? = null,
    val startDate: LocalDate,
    val targetPayoffDate: LocalDate? = null,
    val isActive: Boolean = true,
    val isPaidOff: Boolean = false,
    val paidOffDate: LocalDate? = null,
    val color: String? = null,
    val priority: Int = 0, // For custom ordering
    val isSynced: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val totalPaid: Double get() = originalAmount - currentBalance
    val percentPaid: Double get() = if (originalAmount > 0) (totalPaid / originalAmount) * 100 else 0.0
    val remainingPercent: Double get() = 100 - percentPaid
}

/**
 * Debt payment record
 */
@Serializable
data class DebtPayment(
    val id: String,
    val debtId: String,
    val amount: Double,
    val principalAmount: Double,
    val interestAmount: Double,
    val date: LocalDate,
    val note: String? = null,
    val isExtraPayment: Boolean = false,
    val createdAt: Instant
)

/**
 * Payoff strategy
 */
@Serializable
enum class PayoffStrategy {
    AVALANCHE,  // Highest interest first
    SNOWBALL,   // Lowest balance first
    CUSTOM      // User-defined order
}

/**
 * Debt payoff plan
 */
data class PayoffPlan(
    val strategy: PayoffStrategy,
    val debts: List<DebtPayoffInfo>,
    val monthlyPayment: Double,
    val totalInterestSaved: Double,
    val payoffDate: LocalDate,
    val totalMonths: Int
)

/**
 * Individual debt payoff info within a plan
 */
data class DebtPayoffInfo(
    val debt: Debt,
    val payoffOrder: Int,
    val payoffDate: LocalDate,
    val totalInterest: Double,
    val monthlyPayments: List<MonthlyPayment>
)

/**
 * Monthly payment breakdown
 */
data class MonthlyPayment(
    val month: LocalDate,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
)

/**
 * What-if scenario for extra payments
 */
data class WhatIfScenario(
    val extraMonthlyPayment: Double,
    val originalPayoffDate: LocalDate,
    val newPayoffDate: LocalDate,
    val monthsSaved: Int,
    val interestSaved: Double
)

/**
 * Bill/Subscription tracking
 */
@Serializable
data class Bill(
    val id: String,
    val userId: String,
    val name: String,
    val amount: Double,
    val dueDay: Int,
    val frequency: RecurringFrequency,
    val categoryId: String? = null,
    val isAutoPayEnabled: Boolean = false,
    val reminderDaysBefore: Int = 3,
    val isActive: Boolean = true,
    val lastPaidDate: LocalDate? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)
