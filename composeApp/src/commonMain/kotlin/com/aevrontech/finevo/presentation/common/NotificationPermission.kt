package com.aevrontech.finevo.presentation.common

import androidx.compose.runtime.Composable

interface NotificationPermissionRequester {
    fun requestPermission()
    fun isPermissionGranted(): Boolean
}

@Composable
expect fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): NotificationPermissionRequester
