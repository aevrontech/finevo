package com.aevrontech.finevo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import com.aevrontech.finevo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        TabNavigator(
            tab = DashboardTab,
            tabDisposable = { TabDisposable(it, listOf(DashboardTab, ExpenseTab, DebtTab, HabitTab, SettingsTab)) }
        ) { tabNavigator ->
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceContainer,
                        contentColor = OnSurface
                    ) {
                        TabNavigationItem(DashboardTab)
                        TabNavigationItem(ExpenseTab)
                        TabNavigationItem(DebtTab)
                        TabNavigationItem(HabitTab)
                        TabNavigationItem(SettingsTab)
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Background)
                ) {
                    CurrentTab()
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
                Icon(
                    painter = painter,
                    contentDescription = tab.options.title
                )
            }
        },
        label = {
            Text(
                text = tab.options.title,
                fontSize = 11.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primary,
            selectedTextColor = Primary,
            unselectedIconColor = OnSurfaceVariant,
            unselectedTextColor = OnSurfaceVariant,
            indicatorColor = PrimaryContainer
        )
    )
}

// ============================================
// TAB DEFINITIONS
// ============================================

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home",
            icon = rememberVectorPainter(Icons.Filled.Home)
        )

    @Composable
    override fun Content() {
        DashboardContent()
    }
}

object ExpenseTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Expenses",
            icon = rememberVectorPainter(Icons.Filled.List) // Changed from Receipt
        )

    @Composable
    override fun Content() {
        ExpenseTabContent()
    }
}

object DebtTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Debts",
            icon = rememberVectorPainter(Icons.Filled.AccountCircle) // Changed from CreditCard for better availability
        )

    @Composable
    override fun Content() {
        DebtTabContent()
    }
}

object HabitTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = "Habits",
            icon = rememberVectorPainter(Icons.Filled.Check) // Changed from CheckCircle
        )

    @Composable
    override fun Content() {
        HabitTabContent()
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 4u,
            title = "Settings",
            icon = rememberVectorPainter(Icons.Filled.Settings)
        )

    @Composable
    override fun Content() {
        SettingsTabContent()
    }
}

@Composable
private fun rememberVectorPainter(imageVector: ImageVector) = 
    androidx.compose.ui.graphics.vector.rememberVectorPainter(imageVector)

// ============================================
// TAB CONTENT
// ============================================

@Composable
private fun DashboardContent() {
    val viewModel: HomeViewModel = koinViewModel()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }

        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GradientStart.copy(alpha = 0.3f),
                                    GradientMid.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Total Balance",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RM 12,345.67",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BalanceItem(
                                label = "Income",
                                amount = "+RM 5,000",
                                color = Income
                            )
                            BalanceItem(
                                label = "Expenses",
                                amount = "-RM 3,200",
                                color = Expense
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Add,
                    label = "Add\nExpense",
                    color = Expense,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.Star,
                    label = "Add\nIncome",
                    color = Income,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.AccountCircle,
                    label = "Pay\nDebt",
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.Check,
                    label = "Log\nHabit",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Coming soon placeholder
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "More features coming soon!",
                        fontSize = 16.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = label,
            color = OnSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = amount,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

// Placeholder contents for other tabs
@Composable
private fun ExpenseTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💰", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Expense Tracker", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("Coming in Phase 2", color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun DebtTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎯", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Debt Payoff Planner", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("Coming in Phase 2", color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun HabitTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🚀", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Habit Tracker", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("Coming in Phase 2", color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚙️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("Coming in Phase 2", color = OnSurfaceVariant)
        }
    }
}
