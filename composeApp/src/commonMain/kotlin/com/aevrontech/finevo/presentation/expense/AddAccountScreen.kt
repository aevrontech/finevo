package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.AccountType
import com.aevrontech.finevo.ui.theme.*

/** Elegant, minimalist screen for creating a new account. */
@OptIn(ExperimentalMaterial3Api::class)
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
        defaultCurrency: String = "MYR"
) {
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }
    var selectedType by remember { mutableStateOf(AccountType.CASH) }
    var selectedColor by remember { mutableStateOf("#00D9FF") }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    val isValid =
            name.isNotBlank() && (balanceText.isEmpty() || balanceText.toDoubleOrNull() != null)

    val currencies = listOf("MYR", "USD", "EUR", "GBP", "SGD", "JPY", "CNY", "AUD", "THB", "IDR")
    val colors =
            listOf(
                    "#00D9FF",
                    "#7C4DFF",
                    "#00E5A0",
                    "#FF6B6B",
                    "#FFD93D",
                    "#FF8C00",
                    "#4ECDC4",
                    "#A855F7"
            )

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("New Account", fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            TextButton(
                                    onClick = {
                                        val balance = balanceText.toDoubleOrNull() ?: 0.0
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
                                        color = if (isValid) Primary else OnSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            },
            containerColor = Surface
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
                        color = OnSurfaceVariant,
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
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = SurfaceContainer,
                                        focusedContainerColor = SurfaceContainer,
                                        unfocusedContainerColor = SurfaceContainer
                                ),
                        singleLine = true
                )
            }

            // ===== INITIAL BALANCE =====
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                        "Initial Balance",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                )
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Currency selector
                    Surface(
                            onClick = { showCurrencyPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.width(80.dp)
                    ) {
                        Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    selectedCurrency,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurface
                            )
                            Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Balance input
                    OutlinedTextField(
                            value = balanceText,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    balanceText = it
                                }
                            },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = SurfaceContainer,
                                            focusedContainerColor = SurfaceContainer,
                                            unfocusedContainerColor = SurfaceContainer
                                    ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                    )
                }
            }

            // ===== ACCOUNT TYPE =====
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                        "Account Type",
                        color = OnSurfaceVariant,
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
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                )
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                containerColor = Surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                        "Select Currency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                currencies.forEach { currency ->
                    Surface(
                            onClick = {
                                selectedCurrency = currency
                                showCurrencyPicker = false
                            },
                            color =
                                    if (currency == selectedCurrency) Primary.copy(alpha = 0.1f)
                                    else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = 14.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getCurrencyName(currency), color = OnSurface)
                            if (currency == selectedCurrency) {
                                Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
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
    val borderColor = if (isSelected) Primary else Color.Transparent
    val bgColor = if (isSelected) Primary.copy(alpha = 0.1f) else SurfaceContainer

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
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Primary else OnSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2
            )
        }
    }
}

@Composable
private fun ColorCircle(color: String, isSelected: Boolean, onClick: () -> Unit) {
    val parsedColor = parseHexColor(color)

    Box(
            modifier =
                    Modifier.size(40.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) OnSurface else Color.Transparent,
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

private fun parseHexColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        when (colorString.length) {
            6 -> {
                val r = colorString.substring(0, 2).toInt(16)
                val g = colorString.substring(2, 4).toInt(16)
                val b = colorString.substring(4, 6).toInt(16)
                Color(red = r, green = g, blue = b, alpha = 255)
            }
            else -> Primary
        }
    } catch (e: Exception) {
        Primary
    }
}

private fun getCurrencyName(code: String): String {
    return when (code) {
        "MYR" -> "🇲🇾 MYR - Malaysian Ringgit"
        "USD" -> "🇺🇸 USD - US Dollar"
        "EUR" -> "🇪🇺 EUR - Euro"
        "GBP" -> "🇬🇧 GBP - British Pound"
        "SGD" -> "🇸🇬 SGD - Singapore Dollar"
        "JPY" -> "🇯🇵 JPY - Japanese Yen"
        "CNY" -> "🇨🇳 CNY - Chinese Yuan"
        "AUD" -> "🇦🇺 AUD - Australian Dollar"
        "THB" -> "🇹🇭 THB - Thai Baht"
        "IDR" -> "🇮🇩 IDR - Indonesian Rupiah"
        else -> code
    }
}
