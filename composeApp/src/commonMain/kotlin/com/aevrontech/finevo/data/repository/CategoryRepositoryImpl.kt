package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

/** Implementation of CategoryRepository using LocalDataSource. */
class CategoryRepositoryImpl(private val localDataSource: LocalDataSource) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return localDataSource.getCategories()
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return localDataSource.getCategoriesByType(type)
    }

    override suspend fun getCategoryById(id: String): Category? {
        return localDataSource.getCategoryById(id)
    }

    override suspend fun insertCategory(category: Category) {
        localDataSource.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        // Use insertCategory with INSERT OR REPLACE
        localDataSource.insertCategory(category)
    }

    override suspend fun deleteCategory(id: String) {
        // Only delete if not a system default
        if (isDeletable(id)) {
            localDataSource.deleteCategory(id)
        }
    }

    override suspend fun reorderCategories(categories: List<Category>) {
        categories.forEachIndexed { index, category ->
            localDataSource.insertCategory(category.copy(order = index))
        }
    }

    override suspend fun isDeletable(id: String): Boolean {
        val category = localDataSource.getCategoryById(id)
        // System defaults (user_id = null and is_default = true) cannot be deleted
        return category != null && (category.userId != null || !category.isDefault)
    }
}
