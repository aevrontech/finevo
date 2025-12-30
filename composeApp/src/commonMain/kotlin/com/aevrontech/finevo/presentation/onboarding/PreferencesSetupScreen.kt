package com.aevrontech.finevo.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.domain.model.CurrencyInfo
import com.aevrontech.finevo.domain.model.CurrencyProvider
import com.aevrontech.finevo.domain.repository.SettingsRepository
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.ui.theme.OnboardingGradientEnd
import com.aevrontech.finevo.ui.theme.OnboardingGradientMid
import com.aevrontech.finevo.ui.theme.OnboardingGradientStart
import com.aevrontech.finevo.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Dark theme preview colors
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnSurface = Color(0xFFE0E0E0)

// Light theme preview colors
private val LightBackground = Color(0xFFF5F5F5)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1A1A1A)

class PreferencesSetupScreen : Screen {

    override val key: cafe.adriel.voyager.core.screen.ScreenKey = "PreferencesSetupScreen"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val settingsRepository: SettingsRepository = koinInject()
        val scope = rememberCoroutineScope()

        // Local state for preferences
        var isDarkMode by remember { mutableStateOf(false) }
        var selectedCurrency by remember {
            mutableStateOf(
                CurrencyProvider.getCurrency("MYR")
                    ?: CurrencyProvider.getAllCurrencies().first()
            )
        }
        var showCurrencyPicker by remember { mutableStateOf(false) }
        var isNavigating by remember { mutableStateOf(false) }
        var currencySearchQuery by remember { mutableStateOf("") }

        // Animated colors for preview
        val previewBgColor by
        animateColorAsState(
            targetValue = if (isDarkMode) DarkBackground else LightBackground,
            animationSpec = tween(300),
            label = "bg_color"
        )
        val previewSurfaceColor by
        animateColorAsState(
            targetValue = if (isDarkMode) DarkSurface else LightSurface,
            animationSpec = tween(300),
            label = "surface_color"
        )
        val previewTextColor by
        animateColorAsState(
            targetValue = if (isDarkMode) DarkOnSurface else LightOnSurface,
            animationSpec = tween(300),
            label = "text_color"
        )

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
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Header
                Text(
                    text = "Set Up Your\nPreferences",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Customize your experience",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Theme Selection Section
                Text(
                    text = "Appearance",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Theme toggle cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Light Mode Card
                    ThemeModeCard(
                        title = "Light",
                        emoji = "☀️",
                        isSelected = !isDarkMode,
                        backgroundColor = LightBackground,
                        textColor = LightOnSurface,
                        onClick = { isDarkMode = false },
                        modifier = Modifier.weight(1f)
                    )

                    // Dark Mode Card
                    ThemeModeCard(
                        title = "Dark",
                        emoji = "🌙",
                        isSelected = isDarkMode,
                        backgroundColor = DarkBackground,
                        textColor = DarkOnSurface,
                        onClick = { isDarkMode = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Preview
                Text(
                    text = "Preview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Mini app preview
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(previewBgColor)
                            .padding(12.dp)
                ) {
                    Column {
                        // Mini header
                        Text(
                            text = "Dashboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = previewTextColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mini card
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(previewSurfaceColor)
                                    .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        Modifier.size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OnboardingGradientMid),
                                    contentAlignment = Alignment.Center
                                ) { Text("💰", fontSize = 18.sp) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Balance",
                                        fontSize = 11.sp,
                                        color = previewTextColor.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "${selectedCurrency.symbol} 24,500",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = previewTextColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mini nav bar
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(previewSurfaceColor)
                                    .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MiniNavItem(Icons.Filled.Home, true, previewTextColor)
                            MiniNavItem(Icons.Filled.Star, false, previewTextColor)
                            MiniNavItem(Icons.Filled.Check, false, previewTextColor)
                            MiniNavItem(Icons.Filled.Settings, false, previewTextColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Currency Selection
                Text(
                    text = "Currency",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { showCurrencyPicker = true }
                            .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedCurrency.symbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedCurrency.code,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = selectedCurrency.displayName,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "Change ›",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Add spacing before Continue button
                Spacer(modifier = Modifier.height(24.dp))

                // Continue Button
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .clickable {
                                if (!isNavigating) {
                                    isNavigating = true
                                    // Save preferences to DB and complete onboarding
                                    scope.launch {
                                        // Save dark mode preference
                                        ThemeManager.setDarkMode(isDarkMode)
                                        settingsRepository.setDarkMode(isDarkMode)
                                        // Save currency preference
                                        settingsRepository.setCurrency(
                                            selectedCurrency.code
                                        )
                                        // Mark onboarding as completed
                                        settingsRepository.setOnboardingCompleted(true)
                                        // Navigate to login
                                        navigator.replace(LoginScreen())
                                    }
                                }
                            },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        color = OnboardingGradientMid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Currency Picker Bottom Sheet with Search
        if (showCurrencyPicker) {
            // Get all currencies and filter by search
            val allCurrencies = remember { CurrencyProvider.getAllCurrencies() }
            val popularCurrencies = remember { CurrencyProvider.getPopularCurrencies() }
            val filteredCurrencies =
                remember(currencySearchQuery) {
                    if (currencySearchQuery.isBlank()) {
                        allCurrencies
                    } else {
                        allCurrencies.filter { it.matchesSearch(currencySearchQuery) }
                    }
                }

            ModalBottomSheet(
                onDismissRequest = {
                    showCurrencyPicker = false
                    currencySearchQuery = ""
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (isDarkMode) DarkSurface else LightSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
                    // Header
                    Text(
                        text = "Select Currency",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) DarkOnSurface else LightOnSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    // Search Box
                    androidx.compose.material3.OutlinedTextField(
                        value = currencySearchQuery,
                        onValueChange = { currencySearchQuery = it },
                        placeholder = {
                            Text(
                                "Search...",
                                color =
                                    if (isDarkMode) DarkOnSurface.copy(alpha = 0.5f)
                                    else LightOnSurface.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint =
                                    if (isDarkMode) DarkOnSurface.copy(alpha = 0.6f)
                                    else LightOnSurface.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            if (currencySearchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    modifier =
                                        Modifier.clickable { currencySearchQuery = "" },
                                    tint =
                                        if (isDarkMode) DarkOnSurface.copy(alpha = 0.6f)
                                        else LightOnSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OnboardingGradientMid,
                                unfocusedBorderColor =
                                    if (isDarkMode) DarkOnSurface.copy(alpha = 0.3f)
                                    else LightOnSurface.copy(alpha = 0.3f),
                                focusedTextColor =
                                    if (isDarkMode) DarkOnSurface
                                    else LightOnSurface,
                                unfocusedTextColor =
                                    if (isDarkMode) DarkOnSurface
                                    else LightOnSurface,
                                cursorColor = OnboardingGradientMid
                            )
                    )

                    // Results count
                    Text(
                        text = "${filteredCurrencies.size} currencies",
                        fontSize = 12.sp,
                        color =
                            if (isDarkMode) DarkOnSurface.copy(alpha = 0.5f)
                            else LightOnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        // Show popular currencies first if no search
                        if (currencySearchQuery.isBlank() && popularCurrencies.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Popular",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color =
                                        if (isDarkMode) DarkOnSurface.copy(alpha = 0.5f)
                                        else LightOnSurface.copy(alpha = 0.5f),
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 24.dp,
                                            vertical = 8.dp
                                        )
                                )
                            }
                            items(popularCurrencies) { currency ->
                                CurrencyListItem(
                                    currency = currency,
                                    isSelected = currency.code == selectedCurrency.code,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        selectedCurrency = currency
                                        showCurrencyPicker = false
                                        currencySearchQuery = ""
                                    }
                                )
                            }
                            item {
                                Text(
                                    text = "All Currencies",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color =
                                        if (isDarkMode) DarkOnSurface.copy(alpha = 0.5f)
                                        else LightOnSurface.copy(alpha = 0.5f),
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 24.dp,
                                            vertical = 8.dp
                                        )
                                )
                            }
                        }

                        items(filteredCurrencies) { currency ->
                            CurrencyListItem(
                                currency = currency,
                                isSelected = currency.code == selectedCurrency.code,
                                isDarkMode = isDarkMode,
                                onClick = {
                                    selectedCurrency = currency
                                    showCurrencyPicker = false
                                    currencySearchQuery = ""
                                }
                            )
                        }

                        // Empty state
                        if (filteredCurrencies.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No currencies found",
                                        fontSize = 16.sp,
                                        color =
                                            if (isDarkMode) DarkOnSurface.copy(alpha = 0.6f)
                                            else LightOnSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Try a different search term",
                                        fontSize = 14.sp,
                                        color =
                                            if (isDarkMode) DarkOnSurface.copy(alpha = 0.4f)
                                            else LightOnSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeCard(
    title: String,
    emoji: String,
    isSelected: Boolean,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier.height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .then(
                    if (isSelected)
                        Modifier.border(
                            width = 3.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                    else Modifier
                )
                .clickable(onClick = onClick)
                .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 24.sp)
                if (isSelected) {
                    Box(
                        modifier =
                            Modifier.size(24.dp)
                                .clip(CircleShape)
                                .background(OnboardingGradientMid),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun MiniNavItem(icon: ImageVector, isSelected: Boolean, textColor: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) OnboardingGradientMid else textColor.copy(alpha = 0.4f),
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun CurrencyListItem(
    currency: CurrencyInfo,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Currency symbol in a box
            Box(
                modifier =
                    Modifier.size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) Color.White.copy(alpha = 0.1f)
                            else Color.Black.copy(alpha = 0.05f)
                        ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) DarkOnSurface else LightOnSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = currency.code,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) DarkOnSurface else LightOnSurface
                )
                Text(
                    text = currency.displayName,
                    fontSize = 13.sp,
                    color =
                        if (isDarkMode) DarkOnSurface.copy(alpha = 0.6f)
                        else LightOnSurface.copy(alpha = 0.6f)
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = OnboardingGradientMid,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
