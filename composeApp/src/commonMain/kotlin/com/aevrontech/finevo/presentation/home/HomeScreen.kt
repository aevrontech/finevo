package com.aevrontech.finevo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.presentation.home.tabs.DashboardTab
import com.aevrontech.finevo.presentation.home.tabs.DebtTab
import com.aevrontech.finevo.presentation.home.tabs.ExpenseTab
import com.aevrontech.finevo.presentation.home.tabs.HabitTab
import com.aevrontech.finevo.presentation.home.tabs.SettingsTab
import com.aevrontech.finevo.ui.components.GlassmorphicNavBar
import com.aevrontech.finevo.ui.components.NavBarItem
import com.aevrontech.finevo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

// CompositionLocal to provide sign out handler from HomeScreen level
val LocalSignOutHandler = compositionLocalOf<(() -> Unit)?> { null }

// CompositionLocal to provide parent navigator for screen navigation from tabs
val LocalParentNavigator = compositionLocalOf<Navigator?> { null }

// CompositionLocal to control nav bar visibility from tabs
val LocalSetNavBarVisible = compositionLocalOf<((Boolean) -> Unit)?> { null }

class HomeScreen : Screen {

    // Generate unique key per instance to prevent SlideTransition SaveableStateProvider
    // collision
    override val key: cafe.adriel.voyager.core.screen.ScreenKey =
        "HomeScreen_${java.util.UUID.randomUUID()}"

    @Composable
    override fun Content() {
        // Get navigator for sign out navigation
        val navigator = LocalNavigator.currentOrThrow

        // Nav bar visibility state - controlled by tabs when showing overlays
        var isNavBarVisible by remember { mutableStateOf(true) }

        // Get authViewModel for sign out
        val authViewModel: AuthViewModel = koinViewModel()

        // Track if sign out is in progress to prevent double-click crashes
        var isSigningOut by remember { mutableStateOf(false) }

        // Sign out handler that uses the correct navigator
        val signOutHandler: () -> Unit = {
            if (!isSigningOut) {
                isSigningOut = true
                authViewModel.signOutWithCallback {
                    navigator.replaceAll(LoginScreen())
                }
            }
        }

        CompositionLocalProvider(
            LocalSignOutHandler provides signOutHandler,
            LocalParentNavigator provides navigator,
            LocalSetNavBarVisible provides { visible -> isNavBarVisible = visible }
        ) {
            TabNavigator(
                tab = DashboardTab,
                key = "HomeScreenTabNavigator",
                tabDisposable = {
                    TabDisposable(
                        it,
                        listOf(
                            DashboardTab,
                            ExpenseTab,
                            DebtTab,
                            HabitTab,
                            SettingsTab
                        )
                    )
                }
            ) { _ ->
                val tabNavigator = LocalTabNavigator.current
                val tabs =
                    listOf(
                        DashboardTab,
                        ExpenseTab,
                        DebtTab,
                        HabitTab,
                        SettingsTab
                    )
                val selectedIndex = tabs.indexOf(tabNavigator.current)

                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background
                            )
                ) {
                    // Content area - tabs render their own content including
                    // overlays
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        tabNavigator.current.Content()
                    }

                    // Glassmorphic Floating Navigation Bar - controlled by tabs
                    if (isNavBarVisible) {
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .align(
                                        Alignment
                                            .BottomCenter
                                    )
                                    .navigationBarsPadding()
                        ) {
                            GlassmorphicNavBar(
                                items =
                                    listOf(
                                        NavBarItem(
                                            Icons.Filled
                                                .Home,
                                            "Home"
                                        ),
                                        NavBarItem(
                                            Icons.Filled
                                                .Star,
                                            "Wallet"
                                        ),
                                        NavBarItem(
                                            Icons.Filled
                                                .ShoppingCart,
                                            "Debts"
                                        ),
                                        NavBarItem(
                                            Icons.Filled
                                                .CheckCircle,
                                            "Habits"
                                        ),
                                        NavBarItem(
                                            Icons.Filled
                                                .Settings,
                                            "Settings"
                                        )
                                    ),
                                selectedIndex = selectedIndex,
                                onItemSelected = { index ->
                                    tabNavigator.current =
                                        tabs[index]
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    NavigationBarItem(
        selected = selected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { painter ->
                Icon(painter = painter, contentDescription = tab.options.title)
            }
        },
        label = { Text(text = tab.options.title, fontSize = 11.sp) },
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                indicatorColor = PrimaryContainer
            )
    )
}

// Tabs are now defined in presentation/home/tabs/ package
