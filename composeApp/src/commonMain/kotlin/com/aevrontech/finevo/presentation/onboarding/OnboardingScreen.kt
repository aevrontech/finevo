package com.aevrontech.finevo.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.ui.theme.Background
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.Secondary
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class OnboardingScreen : Screen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: OnboardingViewModel = koinViewModel()
        val scope = rememberCoroutineScope()

        // Prevent double-click crashes on navigation buttons
        var isNavigating by remember { mutableStateOf(false) }

        val pages =
            listOf(
                OnboardingPage(
                    title = "Track Your Finances",
                    description =
                        "Monitor income, expenses, and budgets with beautiful analytics. Get insights into your spending patterns.",
                    emoji = "💰"
                ),
                OnboardingPage(
                    title = "Crush Your Debt",
                    description =
                        "Create payoff plans with Avalanche or Snowball strategies. Visualize your journey to debt freedom.",
                    emoji = "🎯"
                ),
                OnboardingPage(
                    title = "Build Better Habits",
                    description =
                        "Track daily habits, earn XP, and level up! Stay motivated with streaks and achievements.",
                    emoji = "🚀"
                )
            )

        val pagerState = rememberPagerState(pageCount = { pages.size })

        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Skip button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                viewModel.completeOnboarding()
                                navigator.replace(LoginScreen())
                            }
                        }
                    ) { Text(text = "Skip", color = OnSurfaceVariant) }
                }

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { page -> OnboardingPageContent(pages[page]) }

                // Page indicators
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier =
                                Modifier.padding(4.dp)
                                    .size(if (isSelected) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Primary
                                        else OnSurfaceVariant.copy(alpha = 0.3f)
                                    )
                        )
                    }
                }

                // Bottom buttons
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button (hidden on first page)
                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = OnSurface
                                )
                        ) { Text("Back") }
                    }

                    if (pagerState.currentPage == 0) {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Next/Get Started button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                if (!isNavigating) {
                                    isNavigating = true
                                    viewModel.completeOnboarding()
                                    navigator.replace(LoginScreen())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text =
                                if (pagerState.currentPage < pages.size - 1) "Next"
                                else "Get Started",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji icon
        Text(text = page.emoji, fontSize = 80.sp, modifier = Modifier.padding(bottom = 32.dp))

        // Title with gradient
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.linearGradient(colors = listOf(Primary, Secondary))
                ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

data class OnboardingPage(val title: String, val description: String, val emoji: String)
