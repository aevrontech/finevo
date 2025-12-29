package com.aevrontech.finevo.presentation.habit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.HabitCategoryType
import com.aevrontech.finevo.domain.model.HabitSubCategory
import com.aevrontech.finevo.ui.theme.*

/** First screen of Add Habit flow - Category and sub-category selection */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCategorySelectionScreen(
        onDismiss: () -> Unit,
        onSubCategorySelected: (HabitSubCategory?) -> Unit // null = custom habit
) {
        // Handle Android back button
        BackHandler { onDismiss() }

        var selectedCategory by remember { mutableStateOf(HabitCategoryType.MOST_POPULAR) }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }

        // Get sub-categories based on selection or search
        val subCategories =
                remember(selectedCategory, searchQuery) {
                        if (searchQuery.isNotBlank()) {
                                HabitSubCategory.search(searchQuery)
                        } else {
                                HabitSubCategory.getByCategory(selectedCategory)
                        }
                }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.fillMaxSize()) {
                        // Top App Bar
                        TopAppBar(
                                title = {
                                        if (isSearchActive) {
                                                OutlinedTextField(
                                                        value = searchQuery,
                                                        onValueChange = { searchQuery = it },
                                                        placeholder = { Text("Search habits...") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                OutlinedTextFieldDefaults.colors(
                                                                        focusedBorderColor =
                                                                                Color.Transparent,
                                                                        unfocusedBorderColor =
                                                                                Color.Transparent
                                                                )
                                                )
                                        } else {
                                                Text("New Habit", fontWeight = FontWeight.Bold)
                                        }
                                },
                                navigationIcon = {
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                actions = {
                                        IconButton(
                                                onClick = {
                                                        isSearchActive = !isSearchActive
                                                        if (!isSearchActive) searchQuery = ""
                                                }
                                        ) {
                                                Icon(
                                                        if (isSearchActive) Icons.Filled.Close
                                                        else Icons.Filled.Search,
                                                        contentDescription =
                                                                if (isSearchActive) "Close"
                                                                else "Search"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background
                                        )
                        )

                        // Category Tabs
                        if (!isSearchActive) {
                                CategoryTabRow(
                                        categories = HabitCategoryType.entries,
                                        selectedCategory = selectedCategory,
                                        onCategorySelected = { selectedCategory = it }
                                )

                                // Category description
                                Text(
                                        text = selectedCategory.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 20.dp,
                                                        vertical = 4.dp
                                                ),
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                        text = selectedCategory.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Sub-category List
                        LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(subCategories) { subCategory ->
                                        SubCategoryItem(
                                                subCategory = subCategory,
                                                onClick = { onSubCategorySelected(subCategory) }
                                        )
                                }

                                // Empty state
                                if (subCategories.isEmpty()) {
                                        item {
                                                Box(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(32.dp),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Column(
                                                                horizontalAlignment =
                                                                        Alignment.CenterHorizontally
                                                        ) {
                                                                Text("🔍", fontSize = 48.sp)
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        8.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        "No habits found",
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        // Custom Habit Button
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .navigationBarsPadding()
                                                .padding(horizontal = 20.dp, vertical = 16.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Button(
                                        onClick = { onSubCategorySelected(null) },
                                        modifier =
                                                Modifier.shadow(
                                                        elevation = 8.dp,
                                                        shape = RoundedCornerShape(24.dp),
                                                        ambientColor =
                                                                DashboardGradientStart.copy(
                                                                        alpha = 0.3f
                                                                ),
                                                        spotColor =
                                                                DashboardGradientStart.copy(
                                                                        alpha = 0.3f
                                                                )
                                                ),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = Color.White,
                                                        contentColor = DashboardGradientMid
                                                ),
                                        shape = RoundedCornerShape(24.dp),
                                        border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                        Text(
                                                "Custom Habit",
                                                fontWeight = FontWeight.SemiBold,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = 24.dp,
                                                                vertical = 4.dp
                                                        )
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun CategoryTabRow(
        categories: List<HabitCategoryType>,
        selectedCategory: HabitCategoryType,
        onCategorySelected: (HabitCategoryType) -> Unit
) {
        LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
                items(categories) { category ->
                        CategoryTab(
                                category = category,
                                isSelected = category == selectedCategory,
                                onClick = { onCategorySelected(category) }
                        )
                }
        }
}

@Composable
private fun CategoryTab(category: HabitCategoryType, isSelected: Boolean, onClick: () -> Unit) {
        val backgroundColor by
                animateColorAsState(
                        targetValue = if (isSelected) DashboardGradientMid else Color.White,
                        animationSpec = spring(),
                        label = "categoryColor"
                )
        val contentColor by
                animateColorAsState(
                        targetValue =
                                if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(),
                        label = "categoryContentColor"
                )
        val elevation by
                animateDpAsState(
                        targetValue = if (isSelected) 8.dp else 2.dp,
                        animationSpec = spring(),
                        label = "categoryElevation"
                )

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onClick() }
        ) {
                Box(
                        modifier =
                                Modifier.size(56.dp)
                                        .shadow(
                                                elevation = elevation,
                                                shape = CircleShape,
                                                ambientColor =
                                                        if (isSelected)
                                                                DashboardGradientStart.copy(
                                                                        alpha = 0.4f
                                                                )
                                                        else Color.Gray.copy(alpha = 0.2f),
                                                spotColor =
                                                        if (isSelected)
                                                                DashboardGradientStart.copy(
                                                                        alpha = 0.4f
                                                                )
                                                        else Color.Gray.copy(alpha = 0.2f)
                                        )
                                        .clip(CircleShape)
                                        .background(
                                                if (isSelected) {
                                                        Brush.verticalGradient(
                                                                listOf(
                                                                        DashboardGradientStart,
                                                                        DashboardGradientEnd
                                                                )
                                                        )
                                                } else {
                                                        Brush.verticalGradient(
                                                                listOf(Color.White, Color.White)
                                                        )
                                                }
                                        ),
                        contentAlignment = Alignment.Center
                ) { Text(text = category.icon, fontSize = 24.sp) }
        }
}

@Composable
private fun SubCategoryItem(subCategory: HabitSubCategory, onClick: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Text(text = subCategory.icon, fontSize = 24.sp)
                                Text(
                                        text = subCategory.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                        }

                        Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "Favorite",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFFFB3C8)
                                )
                                Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}
