package com.aevrontech.finevo.presentation.security

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.repository.AuthRepository
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    init {
        checkBiometric()
    }

    private fun checkBiometric() {
        val available = authRepository.isBiometricAvailable()
        val enabled = settingsRepository.isBiometricEnabled()
        _state.update { it.copy(isBiometricAvailable = available && enabled) }

        if (available && enabled) {
            authenticateBiometric()
        }
    }

    fun onPinDigit(digit: Char) {
        if (_state.value.pin.length < 4) {
            val newPin = _state.value.pin + digit
            _state.update { it.copy(pin = newPin, error = null) }

            if (newPin.length == 4) {
                verifyPin(newPin)
            }
        }
    }

    fun onBackspace() {
        if (_state.value.pin.isNotEmpty()) {
            _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
        }
    }

    private fun verifyPin(pin: String) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepository.verifyPin(pin)) {
                is Result.Success -> {
                    if (result.data) {
                        _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                    } else {
                        // Shake effect trigger?
                        _state.update {
                            it.copy(isLoading = false, pin = "", error = "Incorrect PIN")
                        }
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                else -> {}
            }
        }
    }

    fun authenticateBiometric() {
        screenModelScope.launch {
            when (val result = authRepository.authenticateWithBiometric()) {
                is Result.Success -> {
                    if (result.data) {
                        _state.update { it.copy(isAuthenticated = true) }
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(error = "Biometric authentication failed") }
                }
                else -> {}
            }
        }
    }
}

data class SecurityState(
    val pin: String = "",
    val isBiometricAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)
