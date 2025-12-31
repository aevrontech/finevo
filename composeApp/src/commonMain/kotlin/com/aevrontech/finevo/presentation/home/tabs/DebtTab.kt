package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.domain.model.Debt
import com.aevrontech.finevo.presentation.components.AddDebtDialog
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

object DebtTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 2u,
                title = "Debts",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Add
                    )
            )

    @Composable
    override fun Content() {
        DebtTabContent()
    }
}

@Composable
private fun DebtTabContent() {
    val viewModel: DebtViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Show AddDebt dialog
    if (uiState.showAddDialog) {
        AddDebtDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, type, amount, interest, minPayment, dueDay ->
                viewModel.addDebt(name, type, amount, amount, interest, minPayment, dueDay)
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debts",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                GradientFab(
                    onClick = { viewModel.showAddDialog() },
                    icon = Icons.Filled.Add,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Total Debt Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Debt", color = OnSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RM ${String.format("%.2f", uiState.totalDebt)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.totalDebt > 0) Warning else Income
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "${uiState.debtCount} debts",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            "Min. payment: RM ${String.format("%.0f", uiState.totalMinimumPayment)}/mo",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Debt List
        if (uiState.debts.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No debts tracked", color = OnSurfaceVariant)
                        Text("You're debt-free! 🎉", color = Income, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(uiState.debts.size) { index ->
                val debt = uiState.debts[index]
                DebtItem(debt = debt)
            }
        }
    }
}

@Composable
private fun DebtItem(debt: Debt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = debt.name, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text(
                        text = debt.type.name.replace("_", " "),
                        color = OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${String.format("%.2f", debt.currentBalance)}",
                        color = Warning,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", debt.interestRate)}% APR",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (debt.percentPaid / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Income,
                trackColor = SurfaceContainerHighest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%.1f", debt.percentPaid)}% paid off",
                color = OnSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
