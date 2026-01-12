package com.aevrontech.finevo.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel for Category Management. */
class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
                val incomeCategories = categories.filter { it.type == TransactionType.INCOME }

                _uiState.update {
                    it.copy(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSelectedTab(type: TransactionType) {
        _uiState.update { it.copy(selectedTab = type) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddEditDialog = true, editingCategory = null) }
    }

    fun showEditDialog(category: Category) {
        _uiState.update { it.copy(showAddEditDialog = true, editingCategory = category) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, editingCategory = null) }
    }

    fun addCategory(name: String, icon: String, color: String, type: TransactionType) {
        viewModelScope.launch {
            val newCategory =
                Category(
                    id = "cat_${getCurrentTimeMillis()}",
                    userId = "local_user", // User-created categories have userId
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    isDefault = false,
                    order =
                        if (type == TransactionType.EXPENSE)
                            _uiState.value.expenseCategories.size
                        else _uiState.value.incomeCategories.size
                )
            categoryRepository.insertCategory(newCategory)
            hideDialog()
        }
    }

    fun updateCategory(category: Category, name: String, icon: String, color: String) {
        viewModelScope.launch {
            val updatedCategory = category.copy(name = name, icon = icon, color = color)
            categoryRepository.updateCategory(updatedCategory)
            hideDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (categoryRepository.isDeletable(category.id)) {
                categoryRepository.deleteCategory(category.id)
                _uiState.update { it.copy(showDeleteConfirmation = false, categoryToDelete = null) }
            }
        }
    }

    fun showDeleteConfirmation(category: Category) {
        viewModelScope.launch {
            val isDeletable = categoryRepository.isDeletable(category.id)
            _uiState.update {
                it.copy(
                    showDeleteConfirmation = true,
                    categoryToDelete = category,
                    deleteError =
                        if (!isDeletable) "System default categories cannot be deleted"
                        else null
                )
            }
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update {
            it.copy(showDeleteConfirmation = false, categoryToDelete = null, deleteError = null)
        }
    }
}

/** UI State for Category Management screen. */
data class CategoryUiState(
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val selectedTab: TransactionType = TransactionType.EXPENSE,
    val isLoading: Boolean = true,
    val showAddEditDialog: Boolean = false,
    val editingCategory: Category? = null,
    val showDeleteConfirmation: Boolean = false,
    val categoryToDelete: Category? = null,
    val deleteError: String? = null
)
