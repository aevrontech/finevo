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
    val user: User? = null,
    val error: String? = null
)

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
                        error = if (user == null) "No user data found" else null
                    )
            }
        }
    }
}
