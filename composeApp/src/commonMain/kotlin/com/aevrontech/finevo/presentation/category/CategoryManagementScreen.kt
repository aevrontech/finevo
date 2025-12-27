package com.aevrontech.finevo.presentation.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.OnPrimary
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.Surface
import com.aevrontech.finevo.ui.theme.SurfaceContainer
import org.koin.compose.viewmodel.koinViewModel

/** Category Management Screen - allows users to view, add, edit, and delete categories. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(onBack: () -> Unit) {
    val viewModel: CategoryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = Primary,
                contentColor = OnPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add Category") }
        },
        containerColor = Surface
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Expense/Income Tabs
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == TransactionType.EXPENSE) 0 else 1,
                containerColor = SurfaceContainer,
                contentColor = Primary,
                modifier =
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = uiState.selectedTab == TransactionType.EXPENSE,
                    onClick = { viewModel.setSelectedTab(TransactionType.EXPENSE) },
                    text = { Text("Expense") }
                )
                Tab(
                    selected = uiState.selectedTab == TransactionType.INCOME,
                    onClick = { viewModel.setSelectedTab(TransactionType.INCOME) },
                    text = { Text("Income") }
                )
            }

            // Category List
            val categories =
                if (uiState.selectedTab == TransactionType.EXPENSE) uiState.expenseCategories
                else uiState.incomeCategories

            if (categories.isEmpty() && !uiState.isLoading) {
                EmptyCategoryState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val category = categories[index]
                        CategoryItem(
                            category = category,
                            onEdit = { viewModel.showEditDialog(category) },
                            onDelete = { viewModel.showDeleteConfirmation(category) }
                        )
                    }

                    // Bottom spacer for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (uiState.showAddEditDialog) {
        AddEditCategoryDialog(
            category = uiState.editingCategory,
            type = uiState.selectedTab,
            onDismiss = { viewModel.hideDialog() },
            onConfirm = { name, icon, color ->
                if (uiState.editingCategory != null) {
                    viewModel.updateCategory(uiState.editingCategory!!, name, icon, color)
                } else {
                    viewModel.addCategory(name, icon, color, uiState.selectedTab)
                }
            }
        )
    }

    // Delete Confirmation
    if (uiState.showDeleteConfirmation && uiState.categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Delete Category") },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete \"${uiState.categoryToDelete?.name}\"?"
                    )
                    if (uiState.deleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.deleteError!!, color = Error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCategory(uiState.categoryToDelete!!) },
                    enabled = uiState.deleteError == null
                ) {
                    Text(
                        "Delete",
                        color = if (uiState.deleteError == null) Error else OnSurfaceVariant
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EmptyCategoryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📁", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No categories yet", color = OnSurfaceVariant)
            Text("Tap + to add a category", color = OnSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .combinedClickable(onClick = onEdit, onLongClick = onDelete),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with color background
            Box(
                modifier =
                    Modifier.size(44.dp)
                        .clip(CircleShape)
                        .background(parseColor(category.color).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(category.icon, fontSize = 22.sp) }

            // Name and type badge
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, fontWeight = FontWeight.Medium, color = OnSurface)
                if (category.isDefault && category.userId == null) {
                    Text("System Default", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }

            // Color indicator
            Box(
                modifier =
                    Modifier.size(24.dp)
                        .clip(CircleShape)
                        .background(parseColor(category.color))
            )

            // Edit icon
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditCategoryDialog(
    category: Category?,
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "📁") }
    var color by remember { mutableStateOf(category?.color ?: "#00D9FF") }
    var showIconPicker by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank()

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (category == null)
                    "Add ${if (type == TransactionType.EXPENSE) "Expense" else "Income"} Category"
                else "Edit Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon and Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon selector
                Surface(
                    onClick = { showIconPicker = !showIconPicker },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = parseColor(color).copy(alpha = 0.2f)
                ) { Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 28.sp) } }

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceContainer
                        )
                )
            }

            // Icon Picker
            AnimatedVisibility(visible = showIconPicker) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Icon", fontSize = 14.sp, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    EmojiPicker(
                        selectedEmoji = icon,
                        onEmojiSelected = {
                            icon = it
                            showIconPicker = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Color Section
            Text("Select Color", fontSize = 14.sp, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            ColorPicker(selectedColor = color, onColorSelected = { color = it })

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }
                Button(
                    onClick = { onConfirm(name, icon, color) },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text(if (category == null) "Add" else "Save") }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmojiPicker(selectedEmoji: String, onEmojiSelected: (String) -> Unit) {
    val emojis =
        listOf(
            // Money & Finance
            "💰",
            "💵",
            "💳",
            "🏦",
            "💸",
            "🪙",
            "💎",
            "📊",
            // Food & Dining
            "🍔",
            "🍕",
            "🍜",
            "☕",
            "🍱",
            "🥗",
            "🍰",
            "🍺",
            // Transportation
            "🚗",
            "🚕",
            "🚌",
            "✈️",
            "🚇",
            "🚲",
            "⛽",
            "🅿️",
            // Shopping
            "🛍️",
            "🛒",
            "👗",
            "👠",
            "💄",
            "🎁",
            "📦",
            "🏪",
            // Home & Bills
            "🏠",
            "📄",
            "💡",
            "📱",
            "💻",
            "📺",
            "🛋️",
            "🔌",
            // Entertainment
            "🎬",
            "🎮",
            "🎵",
            "🎪",
            "🎯",
            "🎨",
            "📚",
            "🎤",
            // Health
            "💊",
            "🏥",
            "🩺",
            "💪",
            "🧘",
            "🏃",
            "🦷",
            "👓",
            // Education
            "📚",
            "🎓",
            "✏️",
            "📝",
            "🔬",
            "📖",
            "🎒",
            "🏫",
            // Other
            "⭐",
            "❤️",
            "🔧",
            "🐾",
            "✨",
            "🌈",
            "🎯",
            "📁"
        )

    Column {
        for (row in emojis.chunked(8)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { emoji ->
                    Surface(
                        onClick = { onEmojiSelected(emoji) },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color =
                            if (emoji == selectedEmoji) Primary.copy(alpha = 0.2f)
                            else Color.Transparent
                    ) { Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) } }
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(selectedColor: String, onColorSelected: (String) -> Unit) {
    val colors =
        listOf(
            "#FF5252", // Red
            "#FF9800", // Orange
            "#FFEB3B", // Yellow
            "#4CAF50", // Green
            "#00BCD4", // Cyan
            "#2196F3", // Blue
            "#673AB7", // Purple
            "#E91E63", // Pink
            "#795548", // Brown
            "#607D8B", // Blue Grey
            "#9C27B0", // Deep Purple
            "#00D9FF" // Cyan Accent
        )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        colors.forEach { colorHex ->
            Surface(
                onClick = { onColorSelected(colorHex) },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = parseColor(colorHex),
                border = if (colorHex == selectedColor) BorderStroke(3.dp, OnSurface) else null
            ) {}
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        val colorLong = colorString.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF) / 255f,
            green = ((colorLong shr 8) and 0xFF) / 255f,
            blue = (colorLong and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Primary
    }
}
