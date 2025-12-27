package com.aevrontech.finevo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aevrontech.finevo.presentation.auth.SocialLoginHandler
import com.aevrontech.finevo.ui.theme.ThemeManager
import com.aevrontech.finevo.ui.theme.initThemePreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize theme preferences with context
        initThemePreferences(this)

        // Set up theme change callback to recreate activity
        ThemeManager.onThemeChanged = { recreate() }

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent { App() }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear callback to avoid memory leak
        if (isFinishing) {
            ThemeManager.onThemeChanged = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Google Sign-In result
        SocialLoginHandler.handleActivityResult(requestCode, data)
    }
}
