package com.aevrontech.finevo.presentation.expense

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.Label
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.common.LocationHelper
import com.aevrontech.finevo.presentation.common.LocationMapPreview
import com.aevrontech.finevo.presentation.common.LocationPickerMap
import com.aevrontech.finevo.presentation.label.LabelColors
import com.aevrontech.finevo.presentation.label.LabelPickerBottomSheet
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** Comprehensive transaction entry screen with unified calculator and details. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionType: TransactionType,
    accounts: List<Account>,
    categories: List<Category>,
    selectedAccount: Account?,
    availableLabels: List<Label> = emptyList(),
    editingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm:
        (
        type: TransactionType,
        amount: Double,
        accountId: String?,
        categoryId: String,
        note: String?,
        date: LocalDate,
        time: String,
        location: String?, // Location Name
        locationLat: Double?,
        locationLng: Double?,
        labels: List<String>
    ) -> Unit,
    onAddLabel: (String, String, Boolean) -> Unit = { _, _, _ -> }
) {
    val isEditing = editingTransaction != null

    var expression by remember {
        mutableStateOf(
            if (isEditing) (editingTransaction?.amount ?: 0.0).formatDecimal(2) else ""
        )
    }
    var computedAmount by remember { mutableStateOf(editingTransaction?.amount ?: 0.0) }
    var selectedAccountLocal by remember { mutableStateOf(selectedAccount) }
    var selectedTime by remember {
        mutableStateOf(
            editingTransaction?.time?.let { timeString ->
                try {
                    // Parse "HH:mm" format back to LocalTime
                    val parts = timeString.split(":")
                    if (parts.size >= 2) {
                        LocalTime(parts[0].toInt(), parts[1].toInt())
                    } else {
                        Clock.System.now()
                            .toLocalDateTime(
                                TimeZone.currentSystemDefault()
                            )
                            .time
                    }
                } catch (e: Exception) {
                    Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .time
                }
            }
                ?: Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .time
        )
    }
    var selectedCategory by remember {
        mutableStateOf<Category?>(
            if (isEditing) categories.find { it.id == editingTransaction?.categoryId }
            else null
        )
    }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            editingTransaction?.date
                ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
        )
    }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }
    var showAddLabelDialog by remember { mutableStateOf(false) }
    var selectedLabelIds by remember {
        mutableStateOf(editingTransaction?.labels ?: emptyList())
    }

    // Location State
    var locationLat by remember { mutableStateOf(editingTransaction?.locationLat) }
    var locationLng by remember { mutableStateOf(editingTransaction?.locationLng) }
    var locationName by remember {
        mutableStateOf(editingTransaction?.location)
    } // Could be init from note if structured, but simple for now
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions.values.all { it }
            if (isGranted) {
                coroutineScope.launch {
                    val loc = LocationHelper.getCurrentLocation(context)
                    if (loc != null) {
                        locationLat = loc.latitude
                        locationLng = loc.longitude
                        // Show map picker after getting initial location
                        showMapPicker = true
                    } else {
                        // Even if null, let them pick manually
                        showMapPicker = true
                    }
                }
            } else {
                // Permission denied, still allow manual picking (maybe default to
                // KL)
                showMapPicker = true
            }
        }

    var showNoteDialog by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(editingTransaction?.type ?: transactionType) }

    // Filter categories by type
    val filteredCategories = categories.filter { it.type == type }

    // Select first category if none selected or type switched
    LaunchedEffect(filteredCategories) {
        if (selectedCategory == null || selectedCategory?.type != type) {
            selectedCategory = filteredCategories.firstOrNull()
        }
    }

    val isValid = computedAmount > 0 && selectedCategory != null

    // Theme colors

    val primaryColor = MaterialTheme.colorScheme.primary
    // Colors
    // Colors
    val incomeColor = Color(0xFF069494)
    val expenseColor = Color(0xFFB31B1B) // Cornell Red
    val typeColor = if (type == TransactionType.EXPENSE) expenseColor else incomeColor
    val animatedTypeColor by animateColorAsState(typeColor)

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier.fillMaxSize().background(animatedTypeColor).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Block touches */ },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar (Fixed)
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (pagerState.currentPage == 1) {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Icon(
                    if (pagerState.currentPage == 1)
                        Icons.AutoMirrored.Filled.ArrowBack
                    else Icons.Default.Close,
                    "Back or Close",
                    tint = Color.White
                )
            }

            // Tabs
            if (pagerState.currentPage == 0) {
                Row(
                    modifier =
                        Modifier.background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                            .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeTab("INCOME", type == TransactionType.INCOME) {
                        type = TransactionType.INCOME
                    }
                    TypeTab("EXPENSE", type == TransactionType.EXPENSE) {
                        type = TransactionType.EXPENSE
                    }
                }
            }

            IconButton(
                onClick = {
                    selectedCategory?.let { cat ->
                        onConfirm(
                            type,
                            computedAmount,
                            selectedAccountLocal?.id,
                            cat.id,
                            note.takeIf { it.isNotBlank() },
                            selectedDate,
                            formatTime(selectedTime),
                            locationName,
                            locationLat,
                            locationLng,
                            selectedLabelIds
                        )
                    }
                },
                enabled = isValid
            ) {
                Icon(
                    Icons.Default.Check,
                    "Save",
                    tint =
                        if (isValid) Color.White
                        else Color.White.copy(alpha = 0.3f)
                )
            }
        }

        // 2. Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    // MAIN PAGE: Amount, Selectors, Calculator
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Upper Section (Amount + Selectors)
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment =
                                    Alignment
                                        .CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Bottom
                            ) {
                                // Amount Display
                                Text(
                                    text =
                                        if (expression
                                                .isEmpty()
                                        )
                                            "0"
                                        else expression,
                                    fontSize = 70.sp,
                                    fontWeight =
                                        FontWeight.Light,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                16.dp
                                        ),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text =
                                        selectedAccountLocal
                                            ?.currency
                                            ?: "MYR",
                                    fontSize = 18.sp,
                                    fontWeight =
                                        FontWeight.Medium,
                                    color =
                                        Color.White.copy(
                                            alpha = 0.7f
                                        )
                                )

                                Spacer(Modifier.height(32.dp))

                                // Selectors
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .padding(
                                                horizontal =
                                                    24.dp
                                            ),
                                    horizontalArrangement =
                                        Arrangement
                                            .SpaceBetween
                                ) {
                                    // Account
                                    Column(
                                        horizontalAlignment =
                                            Alignment
                                                .Start,
                                        modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(
                                                    8.dp
                                                )
                                            )
                                                .clickable {
                                                    showAccountPicker =
                                                        true
                                                }
                                                .padding(
                                                    8.dp
                                                )
                                    ) {
                                        Text(
                                            "Account",
                                            fontSize =
                                                12.sp,
                                            color =
                                                Color.White
                                                    .copy(
                                                        alpha =
                                                            0.5f
                                                    )
                                        )
                                        Row(
                                            verticalAlignment =
                                                Alignment
                                                    .CenterVertically
                                        ) {
                                            Text(
                                                selectedAccountLocal
                                                    ?.name
                                                    ?: "Select",
                                                fontSize =
                                                    16.sp,
                                                fontWeight =
                                                    FontWeight
                                                        .Bold,
                                                color =
                                                    Color.White
                                            )
                                            Icon(
                                                Icons.Default
                                                    .KeyboardArrowDown,
                                                null,
                                                tint =
                                                    Color.White,
                                                modifier =
                                                    Modifier.size(
                                                        16.dp
                                                    )
                                            )
                                        }
                                    }

                                    // Category
                                    Column(
                                        horizontalAlignment =
                                            Alignment
                                                .End,
                                        modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(
                                                    8.dp
                                                )
                                            )
                                                .clickable {
                                                    showCategoryPicker =
                                                        true
                                                }
                                                .padding(
                                                    8.dp
                                                )
                                    ) {
                                        Text(
                                            "Category",
                                            fontSize =
                                                12.sp,
                                            color =
                                                Color.White
                                                    .copy(
                                                        alpha =
                                                            0.5f
                                                    )
                                        )
                                        Row(
                                            verticalAlignment =
                                                Alignment
                                                    .CenterVertically
                                        ) {
                                            Text(
                                                selectedCategory
                                                    ?.name
                                                    ?: "Select",
                                                fontSize =
                                                    16.sp,
                                                fontWeight =
                                                    FontWeight
                                                        .Bold,
                                                color =
                                                    Color.White
                                            )
                                            Icon(
                                                Icons.Default
                                                    .KeyboardArrowDown,
                                                null,
                                                tint =
                                                    Color.White,
                                                modifier =
                                                    Modifier.size(
                                                        16.dp
                                                    )
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                            }

                            // Arrow Tab Overlay
                            Box(
                                modifier =
                                    Modifier.align(
                                        Alignment
                                            .CenterEnd
                                    )
                                        .background(
                                            Color.White
                                                .copy(
                                                    alpha =
                                                        0.1f
                                                ),
                                            RoundedCornerShape(
                                                topStart =
                                                    50.dp,
                                                bottomStart =
                                                    50.dp
                                            )
                                        )
                                        .clickable {
                                            scope
                                                .launch {
                                                    pagerState
                                                        .animateScrollToPage(
                                                            1
                                                        )
                                                }
                                        }
                                        .padding(
                                            vertical =
                                                24.dp,
                                            horizontal =
                                                4.dp
                                        )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled
                                        .KeyboardArrowLeft,
                                    "Details",
                                    tint =
                                        Color.White.copy(
                                            alpha = 0.7f
                                        )
                                )
                            }
                        }

                        // Calculator
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .navigationBarsPadding()
                        ) {
                            CalculatorKeypad(
                                expression = expression,
                                onExpressionChange = { newExpr ->
                                    expression = newExpr
                                    computedAmount =
                                        evaluateExpression(
                                            newExpr
                                        )
                                },
                                onEquals = {
                                    if (expression.isNotEmpty()
                                    ) {
                                        val result =
                                            evaluateExpression(
                                                expression
                                            )
                                        expression =
                                            formatAmount(
                                                result
                                            )
                                        computedAmount =
                                            result
                                    }
                                },
                                modifier =
                                    Modifier.padding(
                                        bottom = 24.dp,
                                        top = 8.dp,
                                        start = 12.dp,
                                        end = 12.dp
                                    )
                            )
                        }
                    }
                }
                1 -> {
                    // DETAILS PAGE
                    // DETAILS PAGE
                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme
                                        .background
                                )
                                .padding(24.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                    ) {
                        // Note
                        Text(
                            "Note",
                            style = MaterialTheme.typography.labelSmall,
                            color = onBackgroundColor.copy(alpha = 0.7f)
                        )
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle =
                                MaterialTheme.typography.bodyLarge
                                    .copy(
                                        color =
                                            onBackgroundColor
                                    ),
                            cursorBrush = SolidColor(primaryColor),
                            keyboardOptions =
                                KeyboardOptions(
                                    capitalization =
                                        KeyboardCapitalization
                                            .Sentences
                                ),
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(vertical = 12.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (note.isEmpty()) {
                                        Text(
                                            "Description",
                                            color =
                                                onBackgroundColor
                                                    .copy(
                                                        alpha =
                                                            0.5f
                                                    ),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodyLarge
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        HorizontalDivider(
                            color =
                                if (note.isNotEmpty()) primaryColor
                                else
                                    onBackgroundColor.copy(
                                        alpha = 0.3f
                                    )
                        )

                        Spacer(Modifier.height(24.dp))

                        // Labels
                        Text(
                            "Labels",
                            style = MaterialTheme.typography.labelSmall,
                            color = onBackgroundColor.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            // Add Label Button
                            AssistChip(
                                onClick = {
                                    showLabelPicker = true
                                },
                                label = { Text("Add Label") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        null,
                                        modifier =
                                            Modifier.size(
                                                16.dp
                                            ),
                                        tint = primaryColor
                                    )
                                },
                                colors =
                                    AssistChipDefaults
                                        .assistChipColors(
                                            containerColor =
                                                primaryColor
                                                    .copy(
                                                        alpha =
                                                            0.1f
                                                    ),
                                            labelColor =
                                                primaryColor
                                        )
                            )

                            // Selected Labels
                            selectedLabelIds.forEach { labelId ->
                                val label =
                                    availableLabels.find {
                                        it.id == labelId
                                    }
                                val labelColor =
                                    if (label != null)
                                        LabelColors.parse(
                                            label.color
                                        )
                                    else primaryColor

                                AssistChip(
                                    onClick = {
                                        showLabelPicker =
                                            true
                                    },
                                    label = {
                                        Text(
                                            label?.name
                                                ?: ""
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default
                                                .Close,
                                            "Remove",
                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                                    .clickable {
                                                        selectedLabelIds =
                                                            selectedLabelIds -
                                                                labelId
                                                    }
                                        )
                                    },
                                    colors =
                                        AssistChipDefaults
                                            .assistChipColors(
                                                containerColor =
                                                    labelColor
                                                        .copy(
                                                            alpha =
                                                                0.2f
                                                        ),
                                                labelColor =
                                                    onBackgroundColor
                                            ),
                                    border = null
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp),
                            color = onBackgroundColor.copy(alpha = 0.1f)
                        )

                        Spacer(Modifier.height(24.dp))

                        // Date & Time
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Date
                            Column(
                                modifier =
                                    Modifier.weight(1f)
                                        .clickable {
                                            showDatePicker =
                                                true
                                        }
                            ) {
                                Text(
                                    "Date",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,
                                    color =
                                        onBackgroundColor
                                            .copy(
                                                alpha =
                                                    0.7f
                                            )
                                )
                                Spacer(Modifier.height(4.dp))
                                // Need formatted date
                                Text(
                                    formatDateShort(
                                        selectedDate
                                    ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyLarge,
                                    color = onBackgroundColor,
                                    fontWeight = FontWeight.Bold
                                )
                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            top = 8.dp
                                        ),
                                    color =
                                        onBackgroundColor
                                            .copy(
                                                alpha =
                                                    0.3f
                                            )
                                )
                            }

                            Spacer(Modifier.width(24.dp))

                            // Time
                            Column(
                                modifier =
                                    Modifier.weight(1f)
                                        .clickable {
                                            showTimePicker =
                                                true
                                        }
                            ) {
                                Text(
                                    "Time",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,
                                    color =
                                        onBackgroundColor
                                            .copy(
                                                alpha =
                                                    0.7f
                                            )
                                )
                                Spacer(Modifier.height(4.dp))
                                // Need formatted time
                                Text(
                                    formatTimeDisplay(
                                        selectedTime
                                    ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyLarge,
                                    color = onBackgroundColor,
                                    fontWeight = FontWeight.Bold
                                )
                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            top = 8.dp
                                        ),
                                    color =
                                        onBackgroundColor
                                            .copy(
                                                alpha =
                                                    0.3f
                                            )
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Place
                        Text(
                            "Place",
                            style = MaterialTheme.typography.labelSmall,
                            color = onBackgroundColor.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))

                        AssistChip(
                            onClick = {
                                if (locationLat == null) {
                                    // Request permission first
                                    // to center map on user
                                    locationPermissionLauncher
                                        .launch(
                                            arrayOf(
                                                "android.permission.ACCESS_COARSE_LOCATION",
                                                "android.permission.ACCESS_FINE_LOCATION"
                                            )
                                        )
                                } else {
                                    showMapPicker = true
                                }
                            },
                            label = {
                                Text(
                                    locationName
                                        ?: if (locationLat !=
                                            null
                                        )
                                            "Edit Location"
                                        else "Add Place"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (locationLat != null)
                                        Icons.Filled.Place
                                    else Icons.Default.Add,
                                    null,
                                    modifier =
                                        Modifier.size(
                                            16.dp
                                        ),
                                    tint =
                                        if (locationLat !=
                                            null
                                        )
                                            animatedTypeColor
                                        else
                                            Color.Unspecified
                                )
                            },
                            colors =
                                AssistChipDefaults.assistChipColors(
                                    containerColor =
                                        if (locationLat !=
                                            null
                                        )
                                            animatedTypeColor
                                                .copy(
                                                    alpha =
                                                        0.1f
                                                )
                                        else
                                            Color.Transparent,
                                    labelColor =
                                        if (locationLat !=
                                            null
                                        )
                                            animatedTypeColor
                                        else
                                            Color.Unspecified
                                )
                        )

                        if (showMapPicker) {
                            Dialog(
                                onDismissRequest = {
                                    showMapPicker = false
                                },
                                properties =
                                    DialogProperties(
                                        usePlatformDefaultWidth =
                                            false
                                    ) // Full screen
                            ) {
                                Surface(
                                    modifier =
                                        Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.fillMaxSize()
                                    ) {
                                        var tempLat by remember {
                                            mutableStateOf(
                                                locationLat
                                            )
                                        }
                                        var tempLng by remember {
                                            mutableStateOf(
                                                locationLng
                                            )
                                        }

                                        LocationPickerMap(
                                            initialLat =
                                                locationLat,
                                            initialLng =
                                                locationLng,
                                            onLocationSelected = { lat,
                                                                   lng
                                                ->
                                                tempLat =
                                                    lat
                                                tempLng =
                                                    lng
                                            },
                                            modifier =
                                                Modifier.fillMaxSize()
                                        )

                                        // Confirm Button
                                        // Overlay
                                        Box(
                                            modifier =
                                                Modifier.align(
                                                    Alignment
                                                        .BottomCenter
                                                )
                                                    .padding(
                                                        16.dp
                                                    )
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme
                                                            .colorScheme
                                                            .surface
                                                            .copy(
                                                                alpha =
                                                                    0.9f
                                                            ),
                                                        RoundedCornerShape(
                                                            16.dp
                                                        )
                                                    )
                                                    .padding(
                                                        16.dp
                                                    )
                                        ) {
                                            Column(
                                                horizontalAlignment =
                                                    Alignment
                                                        .CenterHorizontally
                                            ) {
                                                Text(
                                                    "Pinpoint Location",
                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .titleMedium,
                                                    fontWeight =
                                                        FontWeight
                                                            .Bold
                                                )
                                                Spacer(
                                                    Modifier.height(
                                                        8.dp
                                                    )
                                                )
                                                TextButton(
                                                    onClick = {
                                                        if (tempLat !=
                                                            null &&
                                                            tempLng !=
                                                            null
                                                        ) {
                                                            locationLat =
                                                                tempLat
                                                            locationLng =
                                                                tempLng
                                                            // Geocode
                                                            coroutineScope
                                                                .launch {
                                                                    val address =
                                                                        LocationHelper
                                                                            .getAddressFromCoordinates(
                                                                                context,
                                                                                tempLat!!,
                                                                                tempLng!!
                                                                            )
                                                                    locationName =
                                                                        address
                                                                }
                                                            showMapPicker =
                                                                false
                                                        }
                                                    },
                                                    modifier =
                                                        Modifier.fillMaxWidth()
                                                            .background(
                                                                primaryColor,
                                                                RoundedCornerShape(
                                                                    50
                                                                )
                                                            ),
                                                ) {
                                                    Text(
                                                        "Confirm Location",
                                                        color =
                                                            Color.White
                                                    )
                                                }
                                            }
                                        }

                                        // Close Button
                                        IconButton(
                                            onClick = {
                                                showMapPicker =
                                                    false
                                            },
                                            modifier =
                                                Modifier.align(
                                                    Alignment
                                                        .TopStart
                                                )
                                                    .padding(
                                                        16.dp
                                                    )
                                                    .background(
                                                        Color.Black
                                                            .copy(
                                                                alpha =
                                                                    0.5f
                                                            ),
                                                        androidx.compose
                                                            .foundation
                                                            .shape
                                                            .CircleShape
                                                    )
                                        ) {
                                            Icon(
                                                Icons.Default
                                                    .Close,
                                                "Close",
                                                tint =
                                                    Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        val currentLat = locationLat
                        val currentLng = locationLng

                        AnimatedVisibility(
                            visible =
                                currentLat != null &&
                                    currentLng != null
                        ) {
                            if (currentLat != null && currentLng != null
                            ) {
                                Box(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .height(
                                                150.dp
                                            )
                                            .padding(
                                                bottom =
                                                    24.dp
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    12.dp
                                                )
                                            )
                                ) {
                                    LocationMapPreview(
                                        lat = currentLat,
                                        lng = currentLng,
                                        modifier =
                                            Modifier.fillMaxSize()
                                    )
                                    // Add overlay gradient or
                                    // marker icon if needed
                                    // here,
                                    // but osmdroid view handles
                                    // the marker internally for
                                    // now.
                                }
                            }
                        }

                        // Attachments
                        Text(
                            "Attachments",
                            style = MaterialTheme.typography.labelSmall,
                            color = onBackgroundColor.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        // Placeholder
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = animatedTypeColor
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add attachment",
                                color = animatedTypeColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Picker Sheets
    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            // Reusing existing content for category picker
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Select Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                filteredCategories.forEach { category ->
                    Surface(
                        onClick = {
                            selectedCategory = category
                            showCategoryPicker = false
                        },
                        color =
                            if (category == selectedCategory)
                                animatedTypeColor.copy(alpha = 0.1f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(12.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(category.icon, fontSize = 24.sp)
                            Text(
                                category.name,
                                modifier = Modifier.weight(1f)
                            )
                            if (category == selectedCategory)
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = animatedTypeColor
                                )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            // Reusing existing content for account picker
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Select Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                accounts.forEach { account ->
                    Surface(
                        onClick = {
                            selectedAccountLocal = account
                            showAccountPicker = false
                        },
                        color =
                            if (account == selectedAccountLocal)
                                animatedTypeColor.copy(alpha = 0.1f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(12.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(account.icon, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    account.name,
                                    fontWeight =
                                        FontWeight.Medium
                                )
                                Text(
                                    "${account.currency} ${formatAmount(account.balance)}",
                                    fontSize = 12.sp,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }
                            if (account == selectedAccountLocal)
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = animatedTypeColor
                                )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate =
                                Instant.fromEpochMilliseconds(
                                    millis
                                )
                                    .toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    )
                                    .date
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = selectedTime.hour,
                initialMinute = selectedTime.minute
            )
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime =
                    LocalTime(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) { TimePicker(state = timePickerState) }
    }

    if (showNoteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add Note") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Enter note...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Done") }
            }
        )
    }

    if (showLabelPicker) {
        LabelPickerBottomSheet(
            labels = availableLabels,
            selectedLabelIds = selectedLabelIds,
            onLabelSelected = { labelId ->
                selectedLabelIds =
                    if (selectedLabelIds.contains(labelId)) {
                        selectedLabelIds - labelId
                    } else {
                        selectedLabelIds + labelId
                    }
            },
            onAddLabelClick = { showAddLabelDialog = true },
            onDismissRequest = { showLabelPicker = false }
        )
    }

    if (showAddLabelDialog) {
        AddLabelDialog(
            onDismiss = { showAddLabelDialog = false },
            onConfirm = { name, color, autoAssign ->
                onAddLabel(name, color, autoAssign)
                showAddLabelDialog = false
            }
        )
    }
}

@Composable
private fun TypeTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier.clickable(onClick = onClick)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.2f)
                    else Color.Transparent,
                    RoundedCornerShape(50)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White.copy(alpha = if (isSelected) 1f else 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

// Helpers
private fun formatAmount(amount: Double): String {
    return amount.formatDecimal(2)
}

private fun formatDateShort(date: LocalDate): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when {
        date == today -> "Today"
        date == today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> "${date.dayOfMonth} ${date.month.name.take(3)} ${date.year}"
    }
}

private fun formatTime(time: LocalTime): String {
    // Store in 24-hour HH:mm format for consistent parsing
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

private fun formatTimeDisplay(time: LocalTime): String {
    // Display in 12-hour format with AM/PM
    val hour = time.hour
    val minute = time.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "${displayHour}:${minute.toString().padStart(2, '0')} $amPm"
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { content() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddLabelDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String, autoAssign: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(LabelColors.colors.first()) }
    var autoAssign by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Label") },
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
                            color =
                                MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.6f)
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
            ) { Text("Add") }
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
                .background(LabelColors.parse(color))
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
