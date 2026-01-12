package com.aevrontech.finevo.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): NotificationPermissionRequester {
    return remember {
        object : NotificationPermissionRequester {
            override fun requestPermission() {
                // TODO: Implement actual iOS permission request
                onPermissionResult(true)
            }

            override fun isPermissionGranted(): Boolean {
                // TODO: Implement actual iOS permission check
                return true
            }
        }
    }
}
