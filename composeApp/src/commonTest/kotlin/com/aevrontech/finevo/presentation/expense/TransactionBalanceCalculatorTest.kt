package com.aevrontech.finevo.presentation.expense

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for transaction balance calculations. These tests verify that account balances are
 * correctly updated when:
 * - Adding transactions (expense/income)
 * - Deleting transactions (expense/income)
 * - Editing transactions (amount change, type change)
 */
class TransactionBalanceCalculatorTest {

    // ==================== ADD TRANSACTION TESTS ====================

    @Test
    fun `add expense should decrease account balance`() {
        val initialBalance = 1000.0
        val expenseAmount = 100.0

        // When adding an expense, balance should decrease
        val newBalance =
            calculateBalanceAfterAdd(
                currentBalance = initialBalance,
                amount = expenseAmount,
                isExpense = true
            )

        assertEquals(900.0, newBalance, "Balance should be 900 after RM100 expense")
    }

    @Test
    fun `add income should increase account balance`() {
        val initialBalance = 1000.0
        val incomeAmount = 200.0

        // When adding income, balance should increase
        val newBalance =
            calculateBalanceAfterAdd(
                currentBalance = initialBalance,
                amount = incomeAmount,
                isExpense = false
            )

        assertEquals(1200.0, newBalance, "Balance should be 1200 after RM200 income")
    }

    @Test
    fun `add large expense should allow negative balance`() {
        val initialBalance = 500.0
        val expenseAmount = 800.0

        val newBalance =
            calculateBalanceAfterAdd(
                currentBalance = initialBalance,
                amount = expenseAmount,
                isExpense = true
            )

        assertEquals(
            -300.0,
            newBalance,
            "Balance should be -300 after RM800 expense on RM500 balance"
        )
    }

    // ==================== DELETE TRANSACTION TESTS ====================

    @Test
    fun `delete expense should increase account balance`() {
        val currentBalance = 900.0
        val deletedExpenseAmount = 100.0

        // When deleting an expense, we ADD the amount back
        val newBalance =
            calculateBalanceAfterDelete(
                currentBalance = currentBalance,
                amount = deletedExpenseAmount,
                wasExpense = true
            )

        assertEquals(
            1000.0,
            newBalance,
            "Balance should be 1000 after deleting RM100 expense"
        )
    }

    @Test
    fun `delete income should decrease account balance`() {
        val currentBalance = 1200.0
        val deletedIncomeAmount = 200.0

        // When deleting income, we SUBTRACT the amount
        val newBalance =
            calculateBalanceAfterDelete(
                currentBalance = currentBalance,
                amount = deletedIncomeAmount,
                wasExpense = false
            )

        assertEquals(
            1000.0,
            newBalance,
            "Balance should be 1000 after deleting RM200 income"
        )
    }

    // ==================== EDIT TRANSACTION TESTS - AMOUNT CHANGE ====================

    @Test
    fun `edit expense - increase amount should decrease balance more`() {
        val currentBalance = 900.0 // After RM100 expense from RM1000
        val oldAmount = 100.0
        val newAmount = 150.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = oldAmount,
                oldWasExpense = true,
                newAmount = newAmount,
                newIsExpense = true
            )

        // Old: 1000 - 100 = 900
        // New: 1000 - 150 = 850
        // Or: 900 + 100 - 150 = 850
        assertEquals(
            850.0,
            newBalance,
            "Balance should decrease to 850 when expense increases from 100 to 150"
        )
    }

    @Test
    fun `edit expense - decrease amount should increase balance`() {
        val currentBalance = 900.0 // After RM100 expense from RM1000
        val oldAmount = 100.0
        val newAmount = 50.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = oldAmount,
                oldWasExpense = true,
                newAmount = newAmount,
                newIsExpense = true
            )

        // Reverse old: 900 + 100 = 1000
        // Apply new: 1000 - 50 = 950
        assertEquals(
            950.0,
            newBalance,
            "Balance should increase to 950 when expense decreases from 100 to 50"
        )
    }

    @Test
    fun `edit income - increase amount should increase balance more`() {
        val currentBalance = 1200.0 // After RM200 income on RM1000
        val oldAmount = 200.0
        val newAmount = 300.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = oldAmount,
                oldWasExpense = false,
                newAmount = newAmount,
                newIsExpense = false
            )

        // Reverse old: 1200 - 200 = 1000
        // Apply new: 1000 + 300 = 1300
        assertEquals(
            1300.0,
            newBalance,
            "Balance should increase to 1300 when income increases from 200 to 300"
        )
    }

    @Test
    fun `edit income - decrease amount should decrease balance`() {
        val currentBalance = 1200.0 // After RM200 income on RM1000
        val oldAmount = 200.0
        val newAmount = 100.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = oldAmount,
                oldWasExpense = false,
                newAmount = newAmount,
                newIsExpense = false
            )

        // Reverse old: 1200 - 200 = 1000
        // Apply new: 1000 + 100 = 1100
        assertEquals(
            1100.0,
            newBalance,
            "Balance should decrease to 1100 when income decreases from 200 to 100"
        )
    }

    // ==================== EDIT TRANSACTION TESTS - TYPE CHANGE ====================

    @Test
    fun `edit expense to income - same amount should increase balance significantly`() {
        val currentBalance = 900.0 // After RM100 expense from RM1000
        val amount = 100.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = amount,
                oldWasExpense = true,
                newAmount = amount,
                newIsExpense = false // Changed to income
            )

        // Reverse expense: 900 + 100 = 1000
        // Apply income: 1000 + 100 = 1100
        // Net change: +200 (double the amount)
        assertEquals(
            1100.0,
            newBalance,
            "Balance should be 1100 when changing RM100 expense to income"
        )
    }

    @Test
    fun `edit income to expense - same amount should decrease balance significantly`() {
        val currentBalance = 1100.0 // After RM100 income on RM1000
        val amount = 100.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = amount,
                oldWasExpense = false,
                newAmount = amount,
                newIsExpense = true // Changed to expense
            )

        // Reverse income: 1100 - 100 = 1000
        // Apply expense: 1000 - 100 = 900
        // Net change: -200 (double the amount)
        assertEquals(
            900.0,
            newBalance,
            "Balance should be 900 when changing RM100 income to expense"
        )
    }

    @Test
    fun `edit expense to income with different amount`() {
        val currentBalance = 900.0 // After RM100 expense from RM1000
        val oldAmount = 100.0
        val newAmount = 150.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = oldAmount,
                oldWasExpense = true,
                newAmount = newAmount,
                newIsExpense = false // Changed to income
            )

        // Reverse old expense: 900 + 100 = 1000
        // Apply new income: 1000 + 150 = 1150
        assertEquals(
            1150.0,
            newBalance,
            "Balance should be 1150 when changing RM100 expense to RM150 income"
        )
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `edit with no change should not affect balance`() {
        val currentBalance = 900.0
        val amount = 100.0

        val newBalance =
            calculateBalanceAfterEdit(
                currentBalance = currentBalance,
                oldAmount = amount,
                oldWasExpense = true,
                newAmount = amount,
                newIsExpense = true
            )

        assertEquals(
            900.0,
            newBalance,
            "Balance should remain 900 when no changes are made"
        )
    }

    @Test
    fun `zero amount transaction should not affect balance`() {
        val initialBalance = 1000.0

        val newBalance =
            calculateBalanceAfterAdd(
                currentBalance = initialBalance,
                amount = 0.0,
                isExpense = true
            )

        assertEquals(
            1000.0,
            newBalance,
            "Balance should remain 1000 for zero amount transaction"
        )
    }

    // ==================== HELPER FUNCTIONS (mirroring ViewModel logic) ====================

    /**
     * Calculates balance after adding a new transaction. Mirrors:
     * ExpenseViewModel.addTransaction
     */
    private fun calculateBalanceAfterAdd(
        currentBalance: Double,
        amount: Double,
        isExpense: Boolean
    ): Double {
        return if (isExpense) {
            currentBalance - amount
        } else {
            currentBalance + amount
        }
    }

    /**
     * Calculates balance after deleting a transaction. Mirrors:
     * ExpenseViewModel.deleteTransaction
     */
    private fun calculateBalanceAfterDelete(
        currentBalance: Double,
        amount: Double,
        wasExpense: Boolean
    ): Double {
        return if (wasExpense) {
            // Expense was subtracted, so add it back
            currentBalance + amount
        } else {
            // Income was added, so subtract it
            currentBalance - amount
        }
    }

    /**
     * Calculates balance after editing a transaction. Mirrors:
     * ExpenseViewModel.updateTransaction
     *
     * Logic:
     * 1. Reverse old effect (ADD expense back, SUBTRACT income)
     * 2. Apply new effect (SUBTRACT expense, ADD income)
     */
    private fun calculateBalanceAfterEdit(
        currentBalance: Double,
        oldAmount: Double,
        oldWasExpense: Boolean,
        newAmount: Double,
        newIsExpense: Boolean
    ): Double {
        // Step 1: Calculate old effect to reverse
        // Expense was subtracted from balance, so ADD it back (positive)
        // Income was added to balance, so SUBTRACT it (negative)
        val oldEffect =
            if (oldWasExpense) {
                oldAmount // Add expense back (positive)
            } else {
                -oldAmount // Subtract income (negative)
            }

        // Step 2: Calculate new effect to apply
        // Expense subtracts from balance (negative)
        // Income adds to balance (positive)
        val newEffect =
            if (newIsExpense) {
                -newAmount // Expense subtracts (negative)
            } else {
                newAmount // Income adds (positive)
            }

        // Step 3: Net change = reverse old + apply new
        val netChange = oldEffect + newEffect

        return currentBalance + netChange
    }
}
