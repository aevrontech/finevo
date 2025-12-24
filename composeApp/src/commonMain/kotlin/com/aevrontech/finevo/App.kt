package com.aevrontech.finevo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.aevrontech.finevo.ui.theme.FinEvoTheme
import com.aevrontech.finevo.presentation.splash.SplashScreen
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        FinEvoTheme(
            darkTheme = true // Always use dark theme for futuristic look
        ) {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Navigator(
                    screen = SplashScreen(),
                    onBackPressed = { currentScreen ->
                        true // Handle back press
                    }
                ) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}
