package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.domain.model.CurrencyProvider
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.category.CategoryManagementScreen
import com.aevrontech.finevo.presentation.home.LocalParentNavigator
import com.aevrontech.finevo.presentation.home.LocalSignOutHandler
import com.aevrontech.finevo.presentation.label.LabelManagementScreen
import com.aevrontech.finevo.presentation.settings.SettingsViewModel
import com.aevrontech.finevo.presentation.settings.UserProfileScreen
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.Expense
import com.aevrontech.finevo.ui.theme.ThemeManager
import org.koin.compose.viewmodel.koinViewModel

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 4u,
                title = "Settings",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Settings
                    )
            )

    @Composable
    override fun Content() {
        SettingsTabContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTabContent() {
    val viewModel: SettingsViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val parentNavigator = LocalParentNavigator.current
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showLabelManagement by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()

    val preferences by viewModel.preferences.collectAsState()
    val allCurrencies = remember { CurrencyProvider.getAllCurrencies() }

    // Local state for the picker, initialized/reset when picker opens
    var selectedCurrency by remember {
        mutableStateOf(
            allCurrencies.find { it.code == preferences.currency } ?: allCurrencies.first()
        )
    }

    // Update local selection when preferences change (initial load)
    LaunchedEffect(preferences.currency) {
        val cur = allCurrencies.find { it.code == preferences.currency }
        if (cur != null) {
            selectedCurrency = cur
        }
    }

    var currencySearchQuery by remember { mutableStateOf("") }
    val filteredCurrencies =
        remember(currencySearchQuery, allCurrencies) {
            if (currencySearchQuery.isBlank()) {
                allCurrencies
            } else {
                allCurrencies.filter {
                    it.code.contains(currencySearchQuery, ignoreCase = true) ||
                        it.displayName.contains(currencySearchQuery, ignoreCase = true) ||
                        it.symbol.contains(currencySearchQuery, ignoreCase = true)
                }
            }
        }

    val signOutHandler = LocalSignOutHandler.current
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    // Show Category Management Screen
    if (showCategoryManagement) {
        CategoryManagementScreen(onBack = { showCategoryManagement = false })
        return
    }

    // Show Label Management Screen
    if (showLabelManagement) {
        LabelManagementScreen(onNavigateBack = { showLabelManagement = false })
        return
    }

    // Currency picker modal bottom sheet
    if (showCurrencyPicker) {
        ModalBottomSheet(
            onDismissRequest = {
                showCurrencyPicker = false
                currencySearchQuery = ""
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(horizontal = 16.dp)
            ) {
                Text(
                    "Select Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = currencySearchQuery,
                    onValueChange = { currencySearchQuery = it },
                    placeholder = { Text("Search currencies...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (currencySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { currencySearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f), // Take available space
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCurrencies) { currency ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable { selectedCurrency = currency }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    currency.symbol,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(40.dp)
                                )
                                Column {
                                    Text(currency.displayName, fontWeight = FontWeight.Medium)
                                    Text(
                                        currency.code,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (currency.code == selectedCurrency.code) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        viewModel.updateCurrency(selectedCurrency.code)
                        showCurrencyPicker = false
                        currencySearchQuery = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                ) { Text("Save Currency", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // User Profile Section
        item {
            Card(
                modifier =
                    Modifier.fillMaxWidth().clickable {
                        parentNavigator?.push(UserProfileScreen())
                    },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier =
                                Modifier.size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush =
                                            Brush.linearGradient(
                                                colors =
                                                    listOf(
                                                        DashboardGradientStart,
                                                        DashboardGradientEnd
                                                    )
                                            )
                                    ),
                            contentAlignment = Alignment.Center
                        ) {
                            val userEmail = authState.user?.email ?: "User"
                            Text(
                                text = userEmail.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = authState.user?.email?.substringBefore("@") ?: "User",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = authState.user?.email ?: "Not signed in",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Dark Mode Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isDarkMode) "🌙" else "☀️", fontSize = 24.sp)
                        Column {
                            Text(
                                "Dark Mode",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { enabled -> ThemeManager.setDarkMode(enabled) },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor =
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        }

        // Currency Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showCurrencyPicker = true },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💰", fontSize = 24.sp)
                        Column {
                            Text(
                                "Currency",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${selectedCurrency.symbol} ${selectedCurrency.displayName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            selectedCurrency.code,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Categories Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showCategoryManagement = true },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📁", fontSize = 24.sp)
                        Column {
                            Text(
                                "Manage Categories",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Add, edit, or delete categories",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Labels Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showLabelManagement = true },
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🏷️", fontSize = 24.sp)
                        Column {
                            Text(
                                "Manage Labels",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Create and customize labels",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Account Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Account",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { signOutHandler?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Expense),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Sign Out") }
                }
            }
        }

        // App Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "FinEvo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Version 1.0.0",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
