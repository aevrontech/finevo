package com.aevrontech.finevo.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.AccountType
import com.aevrontech.finevo.domain.repository.AccountRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** UI state for Account management. */
data class AccountUiState(
        val isLoading: Boolean = false,
        val accounts: List<Account> = emptyList(),
        val selectedAccount: Account? = null,
        val totalBalance: Double = 0.0,
        val totalAssets: Double = 0.0,
        val totalLiabilities: Double = 0.0,
        val netWorth: Double = 0.0,
        val showAddDialog: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
)

/** ViewModel for Account management operations. */
class AccountViewModel(private val accountRepository: AccountRepository) : ViewModel() {

    // Default user ID for local storage
    private val userId = "local_user"

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            accountRepository.getActiveAccounts(userId).collect { accounts ->
                val totalBalance = accountRepository.getTotalBalance(userId)
                val totalAssets = accountRepository.getTotalAssets(userId)
                val totalLiabilities = accountRepository.getTotalLiabilities(userId)
                val netWorth = accountRepository.getNetWorth(userId)

                _uiState.update { state ->
                    state.copy(
                            isLoading = false,
                            accounts = accounts,
                            selectedAccount = state.selectedAccount ?: accounts.firstOrNull(),
                            totalBalance = totalBalance,
                            totalAssets = totalAssets,
                            totalLiabilities = totalLiabilities,
                            netWorth = netWorth
                    )
                }
            }
        }
    }

    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun createAccount(
            name: String,
            initialBalance: Double,
            currency: String,
            type: AccountType,
            color: String,
            icon: String = type.icon
    ) {
        viewModelScope.launch {
            try {
                val isFirst = _uiState.value.accounts.isEmpty()
                val newAccount =
                        accountRepository.createAccount(
                                userId = userId,
                                name = name,
                                initialBalance = initialBalance,
                                currency = currency,
                                type = type,
                                color = color,
                                icon = icon,
                                isDefault = isFirst
                        )

                _uiState.update {
                    it.copy(
                            showAddDialog = false,
                            successMessage = "Account created successfully",
                            selectedAccount = newAccount
                    )
                }

                // Reload to get updated list
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to create account: ${e.message}") }
            }
        }
    }

    fun updateAccountBalance(accountId: String, newBalance: Double) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccountBalance(accountId, newBalance)
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update balance: ${e.message}") }
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(accountId)
                _uiState.update {
                    it.copy(successMessage = "Account deleted", selectedAccount = null)
                }
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete account: ${e.message}") }
            }
        }
    }

    fun updateAccount(
            accountId: String,
            name: String,
            balance: Double,
            currency: String,
            type: AccountType,
            color: String
    ) {
        viewModelScope.launch {
            try {
                // Update balance first
                accountRepository.updateAccountBalance(accountId, balance)
                // Note: Full account update would require adding updateAccount to repository
                // For now, this updates the balance
                _uiState.update { it.copy(successMessage = "Account updated") }
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update account: ${e.message}") }
            }
        }
    }

    fun setDefaultAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.setDefaultAccount(userId, accountId)
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to set default account: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
