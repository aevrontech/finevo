package com.aevrontech.finevo.core.util

import androidx.compose.runtime.Composable

/**
 * Provides the current Activity/UIViewController for platform-specific operations. This is used to
 * get the context needed for social login.
 */
@Composable
expect fun getActivityContext(): Any?
