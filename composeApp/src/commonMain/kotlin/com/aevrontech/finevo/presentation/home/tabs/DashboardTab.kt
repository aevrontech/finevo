package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.presentation.home.HomeViewModel
import com.aevrontech.finevo.ui.theme.ActionBill
import com.aevrontech.finevo.ui.theme.ActionBillBg
import com.aevrontech.finevo.ui.theme.ActionMode
import com.aevrontech.finevo.ui.theme.ActionModeBg
import com.aevrontech.finevo.ui.theme.ActionTopUp
import com.aevrontech.finevo.ui.theme.ActionTopUpBg
import com.aevrontech.finevo.ui.theme.ActionTransfer
import com.aevrontech.finevo.ui.theme.ActionTransferBg
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.ThemeColors
import org.koin.compose.viewmodel.koinViewModel

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 0u,
                title = "Home",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Home
                    )
            )

    @Composable
    override fun Content() {
        DashboardContent()
    }
}

@Composable
private fun DashboardContent() {
    val viewModel: HomeViewModel = koinViewModel()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.onSurface
            )
        }

        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    DashboardCardBackground()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your available balance",
                                color =
                                    Color.White.copy(
                                        alpha = 0.9f
                                    ),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$ 24,500",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .background(
                                        color =
                                            Color.White
                                                .copy(
                                                    alpha =
                                                        0.15f
                                                ),
                                        shape =
                                            RoundedCornerShape(
                                                16.dp
                                            )
                                    )
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            BalanceItem(
                                label = "Income",
                                amount = "$5,086",
                                color = Color.White,
                                icon =
                                    Icons.Filled
                                        .KeyboardArrowDown
                            )
                            BalanceItem(
                                label = "Expense",
                                amount = "$5,086",
                                color = Color.White,
                                icon = Icons.Filled.KeyboardArrowUp
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions spacer
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Refresh,
                    label = "Transfer",
                    color = ActionTransfer,
                    backgroundColor = ActionTransferBg
                )
                QuickActionButton(
                    icon = Icons.Filled.Add,
                    label = "Top-up",
                    color = ActionTopUp,
                    backgroundColor = ActionTopUpBg
                )
                QuickActionButton(
                    icon = Icons.Filled.DateRange,
                    label = "Bill",
                    color = ActionBill,
                    backgroundColor = ActionBillBg
                )
                QuickActionButton(
                    icon = Icons.Filled.Menu,
                    label = "More",
                    color = ActionMode,
                    backgroundColor = ActionModeBg
                )
            }
        }

        // Coming soon placeholder
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = ThemeColors.surfaceContainer
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🚀", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "More features coming soon!",
                        fontSize = 16.sp,
                        color = ThemeColors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCardBackground() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            DashboardGradientStart,
                            DashboardGradientMid,
                            DashboardGradientEnd
                        ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(width, height)
                )
        )

        // Bubble Groups - Group 1 (Top Left)
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.13f),
            alpha = 0.25f
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.2f),
            alpha = 0.25f
        )
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.21f, height * 0.15f),
            alpha = 0.25f
        )

        // Bubble Groups - Group 2 (Top Right)
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.83f, height * 0.11f),
            alpha = 0.25f
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.89f, height * 0.20f),
            alpha = 0.25f
        )
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.94f, height * 0.14f),
            alpha = 0.25f
        )

        // Bubble Groups - Group 3 (Left Mid)
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.14f, height * 0.39f),
            alpha = 0.15f
        )
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.18f, height * 0.47f),
            alpha = 0.15f
        )

        // Bubble Groups - Group 4 (Right Mid)
        drawCircle(
            color = Color.White,
            radius = 7.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.76f, height * 0.55f),
            alpha = 0.15f
        )
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width * 0.90f, height * 0.58f),
            alpha = 0.15f
        )
    }
}

@Composable
private fun BalanceItem(label: String, amount: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(32.dp)
                    .background(
                        color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = color.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(
                text = amount,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable {}
    ) {
        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = OnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
