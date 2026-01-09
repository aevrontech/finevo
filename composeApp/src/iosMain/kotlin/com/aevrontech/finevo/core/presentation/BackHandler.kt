package com.aevrontech.finevo.core.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS as it uses swipe back or navigation controller
}
