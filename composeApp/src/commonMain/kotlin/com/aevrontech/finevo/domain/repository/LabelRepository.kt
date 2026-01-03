package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.domain.model.Label
import kotlinx.coroutines.flow.Flow

/** Repository interface for Label management operations */
interface LabelRepository {

    /** Get all labels for a user */
    fun getAllLabels(userId: String): Flow<List<Label>>

    /** Get a single label by ID */
    suspend fun getLabelById(id: String): Label?

    /** Get labels that are set to auto-assign */
    fun getAutoAssignLabels(userId: String): Flow<List<Label>>

    /** Get labels for a specific transaction */
    suspend fun getLabelsForTransaction(transactionId: String): List<Label>

    /** Insert or update a label */
    suspend fun insertLabel(label: Label)

    /** Update an existing label */
    suspend fun updateLabel(label: Label)

    /** Delete a label by ID */
    suspend fun deleteLabel(id: String)

    /** Add a label to a transaction */
    suspend fun addLabelToTransaction(transactionId: String, labelId: String)

    /** Remove a label from a transaction */
    suspend fun removeLabelFromTransaction(transactionId: String, labelId: String)

    /** Remove all labels from a transaction */
    suspend fun removeAllLabelsFromTransaction(transactionId: String)

    /** Set labels for a transaction (replaces existing) */
    suspend fun setLabelsForTransaction(transactionId: String, labelIds: List<String>)

    /** Get number of transactions using this label */
    suspend fun getLabelUsageCount(labelId: String): Long
}
