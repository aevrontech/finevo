package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant

/** Account types available in the app. Each type has an associated icon and display name. */
enum class AccountType(val icon: String, val displayName: String) {
    CASH("💵", "Cash"),
    CURRENT("🏦", "Current Account"),
    GENERAL("📁", "General"),
    CREDIT_CARD("💳", "Credit Card"),
    SAVINGS("💰", "Savings Account"),
    BONUS("🎁", "Bonus"),
    INSURANCE("🛡️", "Insurance"),
    INVESTMENT("📈", "Investment"),
    LOAN("💸", "Loan"),
    MORTGAGE("🏠", "Mortgage");

    companion object {
        fun fromString(value: String): AccountType {
            return values().find { it.name == value } ?: GENERAL
        }
    }
}

/** Account data class representing a financial account. */
data class Account(
        val id: String,
        val userId: String,
        val name: String,
        val balance: Double,
        val currency: String,
        val type: AccountType,
        val color: String,
        val icon: String,
        val isDefault: Boolean = false,
        val isActive: Boolean = true,
        val isExcludedFromTotal: Boolean = false,
        val sortOrder: Int = 0,
        val createdAt: Instant,
        val updatedAt: Instant
) {
    /** Get formatted balance with currency symbol. */
    fun formattedBalance(): String {
        val symbol = getCurrencySymbol(currency)
        return "$symbol ${String.format("%.2f", balance)}"
    }

    /** Check if this is a liability account (negative contribution to net worth). */
    val isLiability: Boolean
        get() = type in listOf(AccountType.CREDIT_CARD, AccountType.LOAN, AccountType.MORTGAGE)

    companion object {
        fun getCurrencySymbol(currency: String): String {
            return when (currency.uppercase()) {
                "MYR" -> "RM"
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                "CNY" -> "¥"
                "SGD" -> "S$"
                "THB" -> "฿"
                "IDR" -> "Rp"
                "PHP" -> "₱"
                "VND" -> "₫"
                "INR" -> "₹"
                "KRW" -> "₩"
                "AUD" -> "A$"
                else -> currency
            }
        }

        /** Default colors for account creation. */
        val defaultColors =
                listOf(
                        "#00D9FF", // Cyan
                        "#7C4DFF", // Purple
                        "#00E5A0", // Green
                        "#FF6B6B", // Red
                        "#FFD93D", // Yellow
                        "#FF8C00", // Orange
                        "#4ECDC4", // Teal
                        "#A855F7" // Violet
                )
    }
}
