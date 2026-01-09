package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.AccountType
import com.aevrontech.finevo.domain.model.CurrencyProvider
import com.aevrontech.finevo.presentation.label.LabelColors

/** Elegant, minimalist screen for creating/editing an account. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountScreen(
    onDismiss: () -> Unit,
    onConfirm:
        (
        name: String,
        balance: Double,
        currency: String,
        type: AccountType,
        color: String) -> Unit,
    defaultCurrency: String = "MYR",
    editingAccount: Account? = null
) {
    val isEditing = editingAccount != null

    var name by remember { mutableStateOf(editingAccount?.name ?: "") }
    var balanceText by remember {
        mutableStateOf(
            if (isEditing) (editingAccount?.balance ?: 0.0).formatDecimal(2) else ""
        )
    }
    var selectedCurrency by remember {
        mutableStateOf(editingAccount?.currency ?: defaultCurrency)
    }
    var selectedType by remember { mutableStateOf(editingAccount?.type ?: AccountType.CASH) }
    var selectedColor by remember { mutableStateOf(editingAccount?.color ?: "#00D9FF") }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    val isValid =
        name.isNotBlank() && (balanceText.isEmpty() || balanceText.toDoubleOrNull() != null)

    // Use centralized Currency enum
    // Use centralized Currency enum
    val colors = LabelColors.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Account" else "New Account",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val balance =
                                balanceText.toDoubleOrNull() ?: 0.0
                            onConfirm(
                                name,
                                balance,
                                selectedCurrency,
                                selectedType,
                                selectedColor
                            )
                        },
                        enabled = isValid
                    ) {
                        Text(
                            "Save",
                            color =
                                if (isValid)
                                    MaterialTheme.colorScheme
                                        .primary
                                else
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ===== ACCOUNT NAME =====
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Account Name",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. My Savings") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor =
                                MaterialTheme.colorScheme
                                    .surfaceContainer,
                            focusedContainerColor =
                                MaterialTheme.colorScheme
                                    .surfaceContainer,
                            unfocusedContainerColor =
                                MaterialTheme.colorScheme
                                    .surfaceContainer
                        ),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization =
                                androidx.compose.ui.text.input
                                    .KeyboardCapitalization
                                    .Sentences
                        )
                )
            }

            // ===== INITIAL BALANCE =====
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Initial Balance",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) { // Currency selector
                    Surface(
                        onClick = { showCurrencyPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 16.dp
                                ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                selectedCurrency,
                                fontWeight = FontWeight.SemiBold,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Balance input
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = {
                            if (it.isEmpty() ||
                                it.matches(
                                    Regex(
                                        "^\\d*\\.?\\d*$"
                                    )
                                )
                            ) {
                                balanceText = it
                            }
                        },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    MaterialTheme.colorScheme
                                        .primary,
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme
                                        .surfaceContainer,
                                focusedContainerColor =
                                    MaterialTheme.colorScheme
                                        .surfaceContainer,
                                unfocusedContainerColor =
                                    MaterialTheme.colorScheme
                                        .surfaceContainer
                            ),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                        singleLine = true
                    )
                }
            }

            // ===== ACCOUNT TYPE =====
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Account Type",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(AccountType.values().toList()) { type ->
                        AccountTypeCard(
                            type = type,
                            isSelected = type == selectedType,
                            onClick = { selectedType = type }
                        )
                    }
                }
            }

            // ===== COLOR PICKER =====
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Color",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        ColorCircle(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Currency picker bottom sheet
    if (showCurrencyPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencyPicker = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Select Currency",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                CurrencyProvider.getAllCurrencies().forEach { currency ->
                    Surface(
                        onClick = {
                            selectedCurrency = currency.code
                            showCurrencyPicker = false
                        },
                        color =
                            if (currency.code == selectedCurrency)
                                MaterialTheme.colorScheme.primary
                                    .copy(alpha = 0.1f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(
                                        vertical = 14.dp,
                                        horizontal = 12.dp
                                    ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    currency.symbol,
                                    fontSize = 16.sp,
                                    fontWeight =
                                        FontWeight.Bold,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurface,
                                    modifier =
                                        Modifier.width(
                                            40.dp
                                        )
                                )
                                Text(
                                    "${currency.code} - ${currency.displayName}",
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurface
                                )
                            }
                            if (currency.code == selectedCurrency) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .primary,
                                    modifier =
                                        Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AccountTypeCard(type: AccountType, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val bgColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surfaceContainer

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(type.icon, fontSize = 28.sp)
            Text(
                type.displayName,
                fontSize = 11.sp,
                fontWeight =
                    if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ColorCircle(color: String, isSelected: Boolean, onClick: () -> Unit) {
    val parsedColor = LabelColors.parse(color)

    Box(
        modifier =
            Modifier.size(40.dp)
                .clip(CircleShape)
                .background(parsedColor)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.onSurface
                        else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
