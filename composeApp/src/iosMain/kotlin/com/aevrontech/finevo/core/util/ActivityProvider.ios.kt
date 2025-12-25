package com.aevrontech.finevo.core.util

import androidx.compose.runtime.Composable

/**
 * iOS implementation - returns null as we don't have Activity. For iOS, social login
 * implementations handle their own context.
 */
@Composable
actual fun getActivityContext(): Any? {
    // iOS doesn't use Activity, return null
    // Social login on iOS will use its own mechanisms
    return null
}
