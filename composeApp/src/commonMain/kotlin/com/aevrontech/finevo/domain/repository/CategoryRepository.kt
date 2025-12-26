package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/** Repository interface for Category management operations. */
interface CategoryRepository {

    /** Get all categories (both system defaults and user-created). */
    fun getAllCategories(): Flow<List<Category>>

    /** Get categories by type (EXPENSE or INCOME). */
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    /** Get a single category by ID. */
    suspend fun getCategoryById(id: String): Category?

    /** Insert or update a category. */
    suspend fun insertCategory(category: Category)

    /** Update an existing category. */
    suspend fun updateCategory(category: Category)

    /** Delete a category by ID. Note: System default categories cannot be deleted. */
    suspend fun deleteCategory(id: String)

    /** Reorder categories within a type. */
    suspend fun reorderCategories(categories: List<Category>)

    /** Check if a category is deletable (user-created, not system default). */
    suspend fun isDeletable(id: String): Boolean
}
