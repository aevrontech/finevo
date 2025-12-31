package com.aevrontech.finevo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val pendingAvatarBytes: ByteArray? = null // For local avatar display
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UserProfileUiState
        if (isLoading != other.isLoading) return false
        if (isSaving != other.isSaving) return false
        if (user != other.user) return false
        if (error != other.error) return false
        if (successMessage != other.successMessage) return false
        if (pendingAvatarBytes != null) {
            if (other.pendingAvatarBytes == null) return false
            if (!pendingAvatarBytes.contentEquals(other.pendingAvatarBytes))
                return false
        } else if (other.pendingAvatarBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + (user?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (successMessage?.hashCode() ?: 0)
        result = 31 * result + (pendingAvatarBytes?.contentHashCode() ?: 0)
        return result
    }
}

class UserProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value =
                    UserProfileUiState(
                        isLoading = false,
                        user = user,
                        error =
                            if (user == null) "No user data found"
                            else null
                    )
            }
        }
    }

    /** Update the user's display name */
    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isSaving = true,
                    error = null,
                    successMessage = null
                )

            authRepository
                .updateProfile(displayName = name)
                .onSuccess { updatedUser ->
                    _uiState.value =
                        _uiState.value.copy(
                            isSaving = false,
                            user = updatedUser,
                            successMessage = "Name updated successfully"
                        )
                }
                .onError { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            isSaving = false,
                            error =
                                "Failed to update name: ${exception.message}"
                        )
                }
        }
    }

    /**
     * Set avatar from image bytes (stored in UI state for display) The avatar is stored locally
     * and the file path is saved to the user profile
     */
    fun updateAvatar(imageBytes: ByteArray, localFilePath: String? = null) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isSaving = true,
                    error = null,
                    successMessage = null,
                    pendingAvatarBytes = imageBytes
                )

            // If a local file path is provided, save it to the user profile
            if (localFilePath != null) {
                authRepository
                    .updateProfile(avatarUrl = localFilePath)
                    .onSuccess { updatedUser ->
                        _uiState.value =
                            _uiState.value.copy(
                                isSaving = false,
                                user = updatedUser,
                                successMessage =
                                    "Avatar updated successfully"
                            )
                    }
                    .onError { exception ->
                        _uiState.value =
                            _uiState.value.copy(
                                isSaving = false,
                                error =
                                    "Failed to update avatar: ${exception.message}"
                            )
                    }
            } else {
                // Just store in UI state for display (no persistence)
                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        successMessage = "Avatar selected"
                    )
            }
        }
    }

    /** Clear any success/error messages */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
