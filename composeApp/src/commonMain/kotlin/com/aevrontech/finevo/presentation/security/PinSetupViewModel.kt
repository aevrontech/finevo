package com.aevrontech.finevo.presentation.security

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinSetupViewModel(private val settingsRepository: SettingsRepository) : ScreenModel {

    private val _state = MutableStateFlow(PinSetupState())
    val state: StateFlow<PinSetupState> = _state.asStateFlow()

    fun onPinDigit(digit: Char) {
        if (_state.value.currentPin.length < 4) {
            val newPin = _state.value.currentPin + digit
            _state.update { it.copy(currentPin = newPin, error = null) }

            if (newPin.length == 4) {
                handlePinComplete(newPin)
            }
        }
    }

    fun onBackspace() {
        if (_state.value.currentPin.isNotEmpty()) {
            _state.update { it.copy(currentPin = it.currentPin.dropLast(1), error = null) }
        }
    }

    private fun handlePinComplete(pin: String) {
        if (_state.value.step == PinSetupStep.ENTER_NEW) {
            // Move to confirm step
            _state.update { it.copy(step = PinSetupStep.CONFIRM, firstPin = pin, currentPin = "") }
        } else if (_state.value.step == PinSetupStep.CONFIRM) {
            // Verify match
            if (pin == _state.value.firstPin) {
                savePin(pin)
            } else {
                _state.update {
                    it.copy(
                        currentPin = "",
                        error = "PINs do not match. Try again.",
                        step = PinSetupStep.ENTER_NEW, // Reset to start
                        firstPin = ""
                    )
                }
            }
        }
    }

    private fun savePin(pin: String) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepository.setPinEnabled(true, pin)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, isComplete = true) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                else -> {}
            }
        }
    }
}

data class PinSetupState(
    val step: PinSetupStep = PinSetupStep.ENTER_NEW,
    val currentPin: String = "",
    val firstPin: String = "",
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

enum class PinSetupStep {
    ENTER_NEW,
    CONFIRM
}
