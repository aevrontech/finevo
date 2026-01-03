package com.aevrontech.finevo.presentation.label

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aevrontech.finevo.domain.model.Label
import org.koin.compose.viewmodel.koinViewModel

/** Label Management Screen - accessible from Settings */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelManagementScreen(onNavigateBack: () -> Unit, viewModel: LabelViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Labels", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = onBackgroundColor
                    )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryColor,
                modifier = Modifier.padding(bottom = 130.dp, end = 20.dp)
            ) { Icon(Icons.Default.Add, "Add Label", tint = Color.White) }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        if (uiState.labels.isEmpty() && !uiState.isLoading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No labels yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = onBackgroundColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to create your first label",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onBackgroundColor.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
            var draggingItemOffset by remember { mutableStateOf(0f) }
            val labels = remember { mutableStateListOf<Label>() }

            LaunchedEffect(uiState.labels) {
                if (draggingItemIndex == null) { // Only sync if not dragging
                    labels.clear()
                    labels.addAll(uiState.labels.sortedBy { it.sortOrder })
                }
            }

            var itemHeightPx by remember { mutableStateOf(0f) }
            val density = LocalDensity.current
            val spacingPx = with(density) { 8.dp.toPx() }

            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                labels.forEachIndexed { index, label ->
                    val isDragging = index == draggingItemIndex
                    val translationY = if (isDragging) draggingItemOffset else 0f
                    val zIndex = if (isDragging) 1f else 0f

                    LabelItem(
                        label = label,
                        modifier =
                            Modifier.zIndex(zIndex)
                                .graphicsLayer { this.translationY = translationY }
                                .onGloballyPositioned {
                                    if (index == 0 && itemHeightPx == 0f)
                                        itemHeightPx = it.size.height.toFloat()
                                }
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingItemIndex = index
                                            draggingItemOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            draggingItemOffset += dragAmount.y

                                            // Robust Swap Logic
                                            if (itemHeightPx > 0) {
                                                val stride =
                                                    itemHeightPx + spacingPx
                                                val currentOffset =
                                                    draggingItemOffset

                                                if (currentOffset > stride * 0.5f) {
                                                    // Move Down
                                                    if (index < labels.lastIndex) {
                                                        val nextIndex = index + 1
                                                        val item = labels[index]
                                                        labels.removeAt(index)
                                                        labels.add(nextIndex, item)

                                                        draggingItemIndex =
                                                            nextIndex
                                                        draggingItemOffset -= stride
                                                    }
                                                } else if (currentOffset <
                                                    -stride * 0.5f
                                                ) {
                                                    // Move Up
                                                    if (index > 0) {
                                                        val prevIndex = index - 1
                                                        val item = labels[index]
                                                        labels.removeAt(index)
                                                        labels.add(prevIndex, item)

                                                        draggingItemIndex =
                                                            prevIndex
                                                        draggingItemOffset += stride
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            viewModel.updateLabelOrders(labels)
                                            draggingItemIndex = null
                                            draggingItemOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggingItemIndex = null
                                            draggingItemOffset = 0f
                                            labels.clear()
                                            labels.addAll(
                                                uiState.labels.sortedBy {
                                                    it.sortOrder
                                                }
                                            )
                                        }
                                    )
                                },
                        onEdit = { editingLabel = label },
                        onDelete = { viewModel.checkUsageAndDelete(label) },
                        surfaceColor = surfaceColor,
                        onBackgroundColor = onBackgroundColor
                    )
                }

                Spacer(modifier = Modifier.height(80.dp)) // FAB space
            }
        }
    }

    // Add Label Dialog
    if (showAddDialog) {
        LabelEditDialog(
            label = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color, autoAssign ->
                viewModel.addLabel(name, color, autoAssign)
                showAddDialog = false
            }
        )
    }

    // Edit Label Dialog
    editingLabel?.let { label ->
        LabelEditDialog(
            label = label,
            onDismiss = { editingLabel = null },
            onConfirm = { name, color, autoAssign ->
                viewModel.updateLabel(
                    label.copy(name = name, color = color, autoAssign = autoAssign)
                )
                editingLabel = null
            }
        )
    }

    if (uiState.labelUsageWarning != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWarning() },
            title = { Text("Cannot Delete Label") },
            text = { Text(uiState.labelUsageWarning!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissWarning() }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun LabelItem(
    label: Label,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    surfaceColor: Color,
    onBackgroundColor: Color
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier =
                    Modifier.size(24.dp)
                        .clip(CircleShape)
                        .background(parseColor(label.color))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Label name and auto-assign indicator
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = onBackgroundColor
                )
                if (label.autoAssign) {
                    Text(
                        "Auto-assign enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = onBackgroundColor.copy(alpha = 0.5f)
                    )
                }
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = onBackgroundColor.copy(alpha = 0.6f))
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelEditDialog(
    label: Label?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String, autoAssign: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(label?.name ?: "") }
    var selectedColor by remember { mutableStateOf(label?.color ?: LabelColors.colors.first()) }
    var autoAssign by remember { mutableStateOf(label?.autoAssign ?: false) }

    val isEditing = label != null
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Label" else "New Label") },
        text = {
            Column {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Label Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color picker
                Text(
                    "Color",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabelColors.colors.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = color == selectedColor,
                            onSelect = { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-assign toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto-assign",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Apply to new transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = autoAssign,
                        onCheckedChange = { autoAssign = it },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primaryColor
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedColor, autoAssign) },
                enabled = name.isNotBlank()
            ) { Text(if (isEditing) "Save" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ColorOption(color: String, isSelected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier =
            Modifier.size(36.dp)
                .clip(CircleShape)
                .background(parseColor(color))
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, Color.White, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Parse hex color string to Compose Color */
private fun parseColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        Color(("FF$colorString").toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}
