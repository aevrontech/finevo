package com.aevrontech.finevo.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.presentation.home.HomeScreen
import com.aevrontech.finevo.presentation.onboarding.OnboardingScreen
import com.aevrontech.finevo.ui.theme.Background
import com.aevrontech.finevo.ui.theme.BackgroundLight
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.Secondary
import com.aevrontech.finevo.ui.theme.Tertiary
import org.koin.compose.viewmodel.koinViewModel

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SplashViewModel = koinViewModel()

        val uiState by viewModel.uiState.collectAsState()

        // Animation values
        val infiniteTransition = rememberInfiniteTransition()

        val logoScale by animateFloatAsState(
            targetValue = if (uiState.isAnimating) 1f else 0.8f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )

        val logoAlpha by animateFloatAsState(
            targetValue = if (uiState.isAnimating) 1f else 0f,
            animationSpec = tween(800)
        )

        val taglineAlpha by animateFloatAsState(
            targetValue = if (uiState.showTagline) 1f else 0f,
            animationSpec = tween(600, delayMillis = 200)
        )

        // Gradient animation

        // Navigation effect
        LaunchedEffect(uiState.navigateTo) {
            uiState.navigateTo?.let { destination ->
                when (destination) {
                    SplashDestination.ONBOARDING -> {
                        navigator.replace(OnboardingScreen())
                    }

                    SplashDestination.LOGIN -> {
                        navigator.replace(LoginScreen())
                    }

                    SplashDestination.HOME -> {
                        navigator.replace(HomeScreen())
                    }
                }
            }
        }

        // Start animation
        LaunchedEffect(Unit) {
            viewModel.startAnimation()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Background,
                            BackgroundLight,
                            Background
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo/Name with gradient
                Text(
                    text = "FinEvo",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Primary,
                                Secondary,
                                Tertiary
                            )
                        )
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline
                Text(
                    text = "Your Financial Evolution",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.alpha(taglineAlpha)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Loading indicator (optional)
                if (uiState.isLoading) {
                    // Pulsing dots or custom loader can go here
                }
            }

            // Version text at bottom
            Text(
                text = "v1.0.0",
                fontSize = 12.sp,
                color = OnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(taglineAlpha)
            )
        }
    }
}
