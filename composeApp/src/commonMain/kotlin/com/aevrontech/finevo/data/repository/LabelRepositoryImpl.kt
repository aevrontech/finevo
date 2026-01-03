package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Label
import com.aevrontech.finevo.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow

/** Implementation of LabelRepository using LocalDataSource */
class LabelRepositoryImpl(private val localDataSource: LocalDataSource) : LabelRepository {

    // Default user ID for local storage (single user mode)
    private val defaultUserId = "local_user"

    override fun getAllLabels(userId: String): Flow<List<Label>> {
        return localDataSource.getLabels(userId)
    }

    override suspend fun getLabelById(id: String): Label? {
        return localDataSource.getLabelById(id)
    }

    override fun getAutoAssignLabels(userId: String): Flow<List<Label>> {
        return localDataSource.getAutoAssignLabels(userId)
    }

    override suspend fun getLabelsForTransaction(transactionId: String): List<Label> {
        return localDataSource.getLabelsForTransaction(transactionId)
    }

    override suspend fun insertLabel(label: Label) {
        localDataSource.insertLabel(label)
    }

    override suspend fun updateLabel(label: Label) {
        localDataSource.updateLabel(label)
    }

    override suspend fun deleteLabel(id: String) {
        localDataSource.deleteLabel(id)
    }

    override suspend fun addLabelToTransaction(transactionId: String, labelId: String) {
        localDataSource.addLabelToTransaction(transactionId, labelId)
    }

    override suspend fun removeLabelFromTransaction(transactionId: String, labelId: String) {
        localDataSource.removeLabelFromTransaction(transactionId, labelId)
    }

    override suspend fun removeAllLabelsFromTransaction(transactionId: String) {
        localDataSource.removeAllLabelsFromTransaction(transactionId)
    }

    override suspend fun setLabelsForTransaction(transactionId: String, labelIds: List<String>) {
        // Remove all existing labels first, then add new ones
        localDataSource.removeAllLabelsFromTransaction(transactionId)
        labelIds.forEach { labelId ->
            localDataSource.addLabelToTransaction(transactionId, labelId)
        }
    }

    override suspend fun getLabelUsageCount(labelId: String): Long {
        return localDataSource.getLabelUsageCount(labelId)
    }
}
