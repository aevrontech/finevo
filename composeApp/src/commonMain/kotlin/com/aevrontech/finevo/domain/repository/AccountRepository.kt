package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

/** Repository for account management operations. */
interface AccountRepository {

    /** Get all accounts for a user. */
    fun getAccounts(userId: String): Flow<List<Account>>

    /** Get only active accounts for a user. */
    fun getActiveAccounts(userId: String): Flow<List<Account>>

    /** Get account by ID. */
    suspend fun getAccountById(accountId: String): Account?

    /** Get the default account for a user. */
    suspend fun getDefaultAccount(userId: String): Account?

    /** Create a new account. */
    suspend fun createAccount(
            userId: String,
            name: String,
            initialBalance: Double,
            currency: String,
            type: AccountType,
            color: String,
            icon: String = type.icon,
            isDefault: Boolean = false
    ): Account

    /** Update an existing account. */
    suspend fun updateAccount(account: Account)

    /** Update account balance. */
    suspend fun updateAccountBalance(accountId: String, newBalance: Double)

    /** Delete an account. */
    suspend fun deleteAccount(accountId: String)

    /** Set an account as default (unsets previous default). */
    suspend fun setDefaultAccount(userId: String, accountId: String)

    /** Get total balance across all active accounts. */
    suspend fun getTotalBalance(userId: String): Double

    /** Get total balance for assets only (excluding liabilities). */
    suspend fun getTotalAssets(userId: String): Double

    /** Get total liabilities. */
    suspend fun getTotalLiabilities(userId: String): Double

    /** Get net worth (assets - liabilities). */
    suspend fun getNetWorth(userId: String): Double
}
