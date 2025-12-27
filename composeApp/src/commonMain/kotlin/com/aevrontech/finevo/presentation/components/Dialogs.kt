package com.aevrontech.finevo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.DebtType
import com.aevrontech.finevo.domain.model.HabitFrequency
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.Expense
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Outline
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.SurfaceContainer
import com.aevrontech.finevo.ui.theme.SurfaceContainerHighest
import com.aevrontech.finevo.ui.theme.Warning

/** Parse a hex color string (#RRGGBB or #AARRGGBB) to Compose Color. */
private fun parseHexColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        when (colorString.length) {
            6 -> {
                // Parse as RGB and add full alpha
                val r = colorString.substring(0, 2).toInt(16)
                val g = colorString.substring(2, 4).toInt(16)
                val b = colorString.substring(4, 6).toInt(16)
                Color(red = r, green = g, blue = b, alpha = 255)
            }

            8 -> {
                // Parse as ARGB
                val a = colorString.substring(0, 2).toInt(16)
                val r = colorString.substring(2, 4).toInt(16)
                val g = colorString.substring(4, 6).toInt(16)
                val b = colorString.substring(6, 8).toInt(16)
                Color(red = r, green = g, blue = b, alpha = a)
            }

            else -> Primary
        }
    } catch (e: Exception) {
        Primary
    }
}

/** Dialog for adding a new transaction (income or expense). */
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm:
        (
        type: TransactionType,
        amount: Double,
        categoryId: String,
        description: String?
    ) -> Unit,
    categories: List<Category>,
    initialType: TransactionType = TransactionType.EXPENSE
) {
    var type by remember { mutableStateOf(initialType) }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { it.type == type }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Text(
                    text =
                        if (type == TransactionType.EXPENSE) "Add Expense"
                        else "Add Income",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("Expense") },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    Expense.copy(alpha = 0.2f),
                                selectedLabelColor = Expense
                            ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("Income") },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    Income.copy(alpha = 0.2f),
                                selectedLabelColor = Income
                            ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")))
                            amount = it
                    },
                    label = { Text("Amount (RM)") },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                    leadingIcon = { Text("RM", color = OnSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text("Category", color = OnSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCategories.isEmpty()) {
                    Text(
                        "No categories available",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(filteredCategories.take(8)) { category ->
                            CategoryChip(
                                category = category,
                                isSelected =
                                    selectedCategoryId ==
                                        category.id,
                                onClick = {
                                    selectedCategoryId =
                                        category.id
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline
                        )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            if (parsedAmount != null &&
                                parsedAmount > 0 &&
                                selectedCategoryId
                                    .isNotEmpty()
                            ) {
                                onConfirm(
                                    type,
                                    parsedAmount,
                                    selectedCategoryId,
                                    description.takeIf {
                                        it.isNotBlank()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled =
                            amount.toDoubleOrNull()?.let { it > 0 } ==
                                true &&
                                selectedCategoryId.isNotEmpty(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (type ==
                                        TransactionType
                                            .EXPENSE
                                    )
                                        Expense
                                    else Income
                            )
                    ) { Text("Add") }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) Primary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .clickable(onClick = onClick)
                .padding(8.dp)
    ) {
        Text(text = category.icon, fontSize = 24.sp)
        Text(
            text = category.name,
            fontSize = 10.sp,
            color = if (isSelected) Primary else OnSurfaceVariant,
            maxLines = 1
        )
    }
}

/** Dialog for adding a new debt. */
@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm:
        (
        name: String,
        type: DebtType,
        amount: Double,
        interest: Double,
        minPayment: Double,
        dueDay: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DebtType.CREDIT_CARD) }
    var amount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var minPayment by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("1") }

    val debtTypes =
        listOf(
            DebtType.CREDIT_CARD to "💳 Credit Card",
            DebtType.CAR_LOAN to "🚗 Car Loan",
            DebtType.HOUSING_LOAN to "🏠 Housing Loan",
            DebtType.PERSONAL_LOAN to "💰 Personal Loan",
            DebtType.STUDENT_LOAN to "🎓 Student Loan",
            DebtType.OTHER to "📝 Other"
        )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Add Debt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Debt Name") },
                    placeholder = { Text("e.g., HSBC Credit Card") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Selection
                Text("Type", color = OnSurfaceVariant, fontSize = 12.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(debtTypes.size) { index ->
                        val (type, label) = debtTypes[index]
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Amount & Interest Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")))
                                amount = it
                        },
                        label = { Text("Balance (RM)") },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")))
                                interestRate = it
                        },
                        label = { Text("Interest %") },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Min Payment & Due Day Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = minPayment,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")))
                                minPayment = it
                        },
                        label = { Text("Min Payment") },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = {
                            if (it.matches(Regex("^\\d{0,2}$")) &&
                                (it.toIntOrNull()
                                    ?: 0) <= 31
                            )
                                dueDay = it
                        },
                        label = { Text("Due Day") },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            val parsedInterest =
                                interestRate.toDoubleOrNull() ?: 0.0
                            val parsedMinPayment =
                                minPayment.toDoubleOrNull() ?: 0.0
                            val parsedDueDay = dueDay.toIntOrNull() ?: 1
                            if (name.isNotBlank() &&
                                parsedAmount != null &&
                                parsedAmount > 0
                            ) {
                                onConfirm(
                                    name,
                                    selectedType,
                                    parsedAmount,
                                    parsedInterest,
                                    parsedMinPayment,
                                    parsedDueDay
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled =
                            name.isNotBlank() &&
                                amount.toDoubleOrNull()?.let {
                                    it > 0
                                } == true,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Warning
                            )
                    ) { Text("Add Debt") }
                }
            }
        }
    }
}

/** Dialog for adding a new habit. */
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm:
        (
        name: String,
        icon: String,
        color: String,
        frequency: HabitFrequency,
        xpReward: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💪") }
    var selectedColor by remember { mutableStateOf("#00D9FF") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }

    val icons = listOf("💪", "📚", "🏃", "💧", "😴", "🧘", "💊", "🥗", "✍️", "🎯", "💰", "🚭")
    val colors = listOf("#00D9FF", "#7C4DFF", "#00E5A0", "#FF6B6B", "#FFD93D", "#FF8C00")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Create Habit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g., Drink 8 glasses of water") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selection
                Text("Icon", color = OnSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(icons.size) { index ->
                        val icon = icons[index]
                        Box(
                            modifier =
                                Modifier.size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedIcon ==
                                            icon
                                        )
                                            Primary.copy(
                                                alpha =
                                                    0.2f
                                            )
                                        else
                                            SurfaceContainerHighest
                                    )
                                    .clickable {
                                        selectedIcon = icon
                                    },
                            contentAlignment = Alignment.Center
                        ) { Text(icon, fontSize = 24.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Selection
                Text("Color", color = OnSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors.size) { index ->
                        val color = colors[index]
                        val parsedColor = parseHexColor(color)
                        Box(
                            modifier =
                                Modifier.size(40.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .clickable {
                                        selectedColor =
                                            color
                                    },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier =
                                        Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency
                Text("Frequency", color = OnSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HabitFrequency.values().take(3).forEach { freq ->
                        FilterChip(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            label = {
                                Text(
                                    freq.name.lowercase()
                                        .replaceFirstChar {
                                            it.uppercase()
                                        },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    selectedIcon,
                                    selectedColor,
                                    selectedFrequency,
                                    10
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                    ) { Text("Create") }
                }
            }
        }
    }
}
