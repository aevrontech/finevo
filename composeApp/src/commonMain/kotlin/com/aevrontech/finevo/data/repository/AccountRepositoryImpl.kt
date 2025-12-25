package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.AccountType
import com.aevrontech.finevo.domain.repository.AccountRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Implementation of AccountRepository using SQLDelight. */
class AccountRepositoryImpl(private val localDataSource: LocalDataSource) : AccountRepository {

    override fun getAccounts(userId: String): Flow<List<Account>> {
        return localDataSource.getAllAccounts(userId).map { accounts ->
            accounts.map { it.toDomainModel() }
        }
    }

    override fun getActiveAccounts(userId: String): Flow<List<Account>> {
        return localDataSource.getActiveAccounts(userId).map { accounts ->
            accounts.map { it.toDomainModel() }
        }
    }

    override suspend fun getAccountById(accountId: String): Account? {
        return localDataSource.getAccountById(accountId)?.toDomainModel()
    }

    override suspend fun getDefaultAccount(userId: String): Account? {
        return localDataSource.getDefaultAccount(userId)?.toDomainModel()
    }

    override suspend fun createAccount(
            userId: String,
            name: String,
            initialBalance: Double,
            currency: String,
            type: AccountType,
            color: String,
            icon: String,
            isDefault: Boolean
    ): Account {
        val now = Clock.System.now()
        val account =
                Account(
                        id = generateId(),
                        userId = userId,
                        name = name,
                        balance = initialBalance,
                        currency = currency,
                        type = type,
                        color = color,
                        icon = icon,
                        isDefault = isDefault,
                        isActive = true,
                        isExcludedFromTotal = false,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now
                )

        localDataSource.insertAccount(account.toEntity())
        return account
    }

    override suspend fun updateAccount(account: Account) {
        val updated = account.copy(updatedAt = Clock.System.now())
        localDataSource.insertAccount(updated.toEntity())
    }

    override suspend fun updateAccountBalance(accountId: String, newBalance: Double) {
        localDataSource.updateAccountBalance(
                accountId,
                newBalance,
                Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun deleteAccount(accountId: String) {
        localDataSource.deleteAccount(accountId)
    }

    override suspend fun setDefaultAccount(userId: String, accountId: String) {
        // First, unset any existing default
        val accounts = localDataSource.getActiveAccountsSync(userId)
        accounts.forEach { account ->
            if (account.is_default == 1L && account.id != accountId) {
                localDataSource.insertAccount(
                        account.copy(
                                is_default = 0,
                                updated_at = Clock.System.now().toEpochMilliseconds()
                        )
                )
            }
        }

        // Set the new default
        val targetAccount = localDataSource.getAccountById(accountId)
        if (targetAccount != null) {
            localDataSource.insertAccount(
                    targetAccount.copy(
                            is_default = 1,
                            updated_at = Clock.System.now().toEpochMilliseconds()
                    )
            )
        }
    }

    override suspend fun getTotalBalance(userId: String): Double {
        val accounts = localDataSource.getActiveAccountsSync(userId)
        return accounts.filter { it.is_excluded_from_total == 0L }.sumOf { account ->
            val type = AccountType.fromString(account.type)
            if (type in listOf(AccountType.CREDIT_CARD, AccountType.LOAN, AccountType.MORTGAGE)) {
                -account.balance
            } else {
                account.balance
            }
        }
    }

    override suspend fun getTotalAssets(userId: String): Double {
        val accounts = localDataSource.getActiveAccountsSync(userId)
        return accounts
                .filter { it.is_excluded_from_total == 0L }
                .filter {
                    AccountType.fromString(it.type) !in
                            listOf(AccountType.CREDIT_CARD, AccountType.LOAN, AccountType.MORTGAGE)
                }
                .sumOf { it.balance }
    }

    override suspend fun getTotalLiabilities(userId: String): Double {
        val accounts = localDataSource.getActiveAccountsSync(userId)
        return accounts
                .filter { it.is_excluded_from_total == 0L }
                .filter {
                    AccountType.fromString(it.type) in
                            listOf(AccountType.CREDIT_CARD, AccountType.LOAN, AccountType.MORTGAGE)
                }
                .sumOf { it.balance }
    }

    override suspend fun getNetWorth(userId: String): Double {
        return getTotalAssets(userId) - getTotalLiabilities(userId)
    }

    // Extension functions for mapping
    private fun com.aevrontech.finevo.data.local.Accounts.toDomainModel(): Account {
        return Account(
                id = id,
                userId = user_id,
                name = name,
                balance = balance,
                currency = currency,
                type = AccountType.fromString(type),
                color = color,
                icon = icon,
                isDefault = is_default == 1L,
                isActive = is_active == 1L,
                isExcludedFromTotal = is_excluded_from_total == 1L,
                sortOrder = sort_order.toInt(),
                createdAt = Instant.fromEpochMilliseconds(created_at),
                updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )
    }

    private fun Account.toEntity(): com.aevrontech.finevo.data.local.Accounts {
        return com.aevrontech.finevo.data.local.Accounts(
                id = id,
                user_id = userId,
                name = name,
                balance = balance,
                currency = currency,
                type = type.name,
                color = color,
                icon = icon,
                is_default = if (isDefault) 1L else 0L,
                is_active = if (isActive) 1L else 0L,
                is_excluded_from_total = if (isExcludedFromTotal) 1L else 0L,
                sort_order = sortOrder.toLong(),
                created_at = createdAt.toEpochMilliseconds(),
                updated_at = updatedAt.toEpochMilliseconds()
        )
    }

    private fun generateId(): String {
        return "acc_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000, 9999)}"
    }
}
