package com.aevrontech.finevo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.aevrontech.finevo.ui.theme.FinEvoTheme
import com.aevrontech.finevo.ui.theme.ThemeManager
import org.koin.compose.KoinContext

@Composable
fun App(initialScreen: Screen) {
    // Get current theme state (Activity recreates when this changes)
    val isDarkMode = ThemeManager.isDarkMode.value

    KoinContext {
        FinEvoTheme(darkTheme = isDarkMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Navigator(
                    screen = initialScreen,
                    onBackPressed = { currentScreen ->
                        true // Handle back press
                    }
                ) { navigator -> SlideTransition(navigator) }
            }
        }
    }
}
