package com.aevrontech.finevo.core.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Android implementation - returns the current Activity. */
@Composable
actual fun getActivityContext(): Any? {
    val context = LocalContext.current
    return context as? Activity
        ?: (context as? android.content.ContextWrapper)?.let {
            var ctx = it.baseContext
            while (ctx is android.content.ContextWrapper) {
                if (ctx is Activity) return ctx
                ctx = ctx.baseContext
            }
            null
        }
}
