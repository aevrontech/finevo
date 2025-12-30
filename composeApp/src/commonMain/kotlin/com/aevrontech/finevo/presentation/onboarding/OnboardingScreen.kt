package com.aevrontech.finevo.presentation.onboarding

// LoginScreen import removed - now navigates to PreferencesSetupScreen
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.ui.theme.OnboardingGradientEnd
import com.aevrontech.finevo.ui.theme.OnboardingGradientMid
import com.aevrontech.finevo.ui.theme.OnboardingGradientStart
import finevo.composeapp.generated.resources.Res
import finevo.composeapp.generated.resources.debt_management
import finevo.composeapp.generated.resources.habit_tracker
import finevo.composeapp.generated.resources.manage_wallet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class OnboardingScreen : Screen {

    override val key: cafe.adriel.voyager.core.screen.ScreenKey = "OnboardingScreen"

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
                    image = Res.drawable.manage_wallet,
                    title = "Manage Your\nFinances",
                    description =
                        "Track income, expenses and budgets with beautiful analytics. Take control of your money."
                ),
                OnboardingPage(
                    image = Res.drawable.debt_management,
                    title = "Crush Your\nDebt",
                    description =
                        "Create payoff plans with smart strategies. Visualize your journey to financial freedom."
                ),
                OnboardingPage(
                    image = Res.drawable.habit_tracker,
                    title = "Build Better\nHabits",
                    description =
                        "Track daily habits, earn XP and level up! Stay motivated with streaks and achievements."
                )
            )

        val pagerState = rememberPagerState(pageCount = { pages.size })

        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        OnboardingGradientStart,
                                        OnboardingGradientMid,
                                        OnboardingGradientEnd
                                    )
                            )
                    )
        ) {
            // Decorative circles in background
            OnboardingBackgroundDecoration()

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                // Pager - takes most of the screen
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { page -> OnboardingPageContent(pages[page]) }

                // Bottom section with indicators and navigation
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                ) {
                    // Page indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by
                            animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                label = "indicator_width"
                            )
                            Box(
                                modifier =
                                    Modifier.padding(horizontal = 4.dp)
                                        .height(8.dp)
                                        .width(width)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) Color.White
                                            else Color.White.copy(alpha = 0.4f)
                                        )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip / Arrow icon button
                        Box(
                            modifier =
                                Modifier.size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        if (!isNavigating) {
                                            isNavigating = true
                                            navigator.replace(PreferencesSetupScreen())
                                        }
                                    },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Skip",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Next / Get Started button
                        Box(
                            modifier =
                                Modifier.height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .clickable {
                                        if (pagerState.currentPage < pages.size - 1) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage + 1
                                                )
                                            }
                                        } else {
                                            if (!isNavigating) {
                                                isNavigating = true
                                                navigator.replace(
                                                    PreferencesSetupScreen()
                                                )
                                            }
                                        }
                                    }
                                    .padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text =
                                    if (pagerState.currentPage < pages.size - 1) "Next"
                                    else "Get Started",
                                color = OnboardingGradientMid,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Large illustration image
        Image(
            painter = painterResource(page.image),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title - Bold white text
        Text(
            text = page.title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Start,
            lineHeight = 44.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description - Lighter white text
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OnboardingBackgroundDecoration() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Large decorative circles (subtle)
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = width * 0.4f,
            center = androidx.compose.ui.geometry.Offset(x = width * 0.85f, y = height * 0.15f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.03f),
            radius = width * 0.35f,
            center = androidx.compose.ui.geometry.Offset(x = width * 0.1f, y = height * 0.4f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.04f),
            radius = width * 0.25f,
            center = androidx.compose.ui.geometry.Offset(x = width * 0.9f, y = height * 0.7f)
        )
    }
}

data class OnboardingPage(val image: DrawableResource, val title: String, val description: String)
