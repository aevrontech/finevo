package com.aevrontech.finevo.presentation.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.model.Label
import com.aevrontech.finevo.domain.repository.LabelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/** ViewModel for Label management */
class LabelViewModel(private val labelRepository: LabelRepository) : ViewModel() {

    // Default user ID for local storage (single user mode)
    private val defaultUserId = "local_user"

    private val _uiState = MutableStateFlow(LabelUiState())
    val uiState: StateFlow<LabelUiState> = _uiState.asStateFlow()

    init {
        loadLabels()
    }

    private fun loadLabels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            labelRepository.getAllLabels(defaultUserId).collect { labels ->
                _uiState.update { it.copy(labels = labels, isLoading = false) }
            }
        }
    }

    fun addLabel(name: String, color: String, autoAssign: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val now = Clock.System.now()
                val label =
                    Label(
                        id = generateId(),
                        userId = defaultUserId,
                        name = name,
                        color = color,
                        autoAssign = autoAssign,
                        createdAt = now,
                        updatedAt = now
                    )
                labelRepository.insertLabel(label)
                _uiState.update { it.copy(isLoading = false, successMessage = "Label added") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateLabel(label: Label) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                labelRepository.updateLabel(label.copy(updatedAt = Clock.System.now()))
                _uiState.update { it.copy(isLoading = false, successMessage = "Label updated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun checkUsageAndDelete(label: Label) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val count = labelRepository.getLabelUsageCount(label.id)
                if (count > 0) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            labelUsageWarning =
                                "Label '${label.name}' is used by $count transactions."
                        )
                    }
                } else {
                    deleteLabel(label.id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun dismissWarning() {
        _uiState.update { it.copy(labelUsageWarning = null) }
    }

    fun updateLabelOrders(newLabels: List<Label>) {
        viewModelScope.launch {
            // Update DB
            // We iterate and update only those that changed order
            newLabels.forEachIndexed { index, label ->
                if (label.sortOrder != index) {
                    try {
                        labelRepository.updateLabel(
                            label.copy(sortOrder = index, updatedAt = Clock.System.now())
                        )
                    } catch (e: Exception) {
                        // Log error but continue
                    }
                }
            }
            // Trigger reload to ensure UI syncs with DB eventual state if needed
            // But usually UI is driven by Flow, which will update when DB emits.
            // Problem: If we update 10 items, we might get 10 emissions.
            // Ideally assume local state is correct for now.
        }
    }

    private fun deleteLabel(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                labelRepository.deleteLabel(id)
                _uiState.update { it.copy(isLoading = false, successMessage = "Label deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    suspend fun getLabelsForTransaction(transactionId: String): List<Label> {
        return labelRepository.getLabelsForTransaction(transactionId)
    }

    fun setLabelsForTransaction(transactionId: String, labelIds: List<String>) {
        viewModelScope.launch {
            try {
                labelRepository.setLabelsForTransaction(transactionId, labelIds)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun generateId(): String {
        return "label_${Clock.System.now().toEpochMilliseconds()}"
    }
}

/** UI State for Label management */
data class LabelUiState(
    val labels: List<Label> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val labelUsageWarning: String? = null
)
