package com.aevrontech.finevo.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.model.UserTier
import com.aevrontech.finevo.presentation.common.FileStorage
import com.aevrontech.finevo.presentation.common.ImagePicker
import com.aevrontech.finevo.presentation.common.ImagePickerResult
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

class UserProfileScreen : Screen {

    override val key: cafe.adriel.voyager.core.screen.ScreenKey = "UserProfileScreen"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: UserProfileViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        // Image picker state
        var showImagePickerSheet by remember { mutableStateOf(false) }
        val imagePicker = remember { ImagePicker() }

        // Handle success/error messages
        LaunchedEffect(uiState.successMessage, uiState.error) {
            uiState.successMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
            uiState.error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color(0xFF667EEA),
                                            Color(0xFF764BA2),
                                            MaterialTheme
                                                .colorScheme
                                                .background
                                        ),
                                    startY = 0f,
                                    endY = 600f
                                )
                        )
            ) {
                // Top app bar overlay
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                    uiState.user != null -> {
                        ProfileContent(
                            user = uiState.user!!,
                            isSaving = uiState.isSaving,
                            pendingAvatarBytes = uiState.pendingAvatarBytes,
                            onAvatarClick = { showImagePickerSheet = true },
                            onUpdateName = { viewModel.updateDisplayName(it) },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    else -> {
                        Text(
                            text = uiState.error ?: "No user data",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }

                // Saving overlay
                if (uiState.isSaving) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Saving...",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Image picker bottom sheet
                if (showImagePickerSheet) {
                    ImagePickerBottomSheet(
                        onDismiss = { showImagePickerSheet = false },
                        onPickFromGallery = { context ->
                            imagePicker.pickFromGallery(context) { result ->
                                handleImageResult(result, viewModel, context)
                                showImagePickerSheet = false
                            }
                        },
                        onCaptureFromCamera = { context ->
                            imagePicker.captureFromCamera(context) { result ->
                                handleImageResult(result, viewModel, context)
                                showImagePickerSheet = false
                            }
                        }
                    )
                }
            }
        }
    }

    private fun handleImageResult(
        result: ImagePickerResult,
        viewModel: UserProfileViewModel,
        context: Any
    ) {
        if (result.isSuccess && result.bytes != null) {
            // Save to local file
            val fileName = "avatar_${Clock.System.now().toEpochMilliseconds()}.jpg"
            val filePath = FileStorage.saveAvatar(context, result.bytes, fileName)

            // Update avatar with bytes and file path
            viewModel.updateAvatar(result.bytes, filePath)
        }
        // Error handling is managed by the ViewModel
    }

    @Composable
    private fun ProfileContent(
        user: User,
        isSaving: Boolean,
        pendingAvatarBytes: ByteArray?,
        onAvatarClick: () -> Unit,
        onUpdateName: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val scrollState = rememberScrollState()
        val focusManager = LocalFocusManager.current

        // Editing state for name
        var isEditingName by remember { mutableStateOf(false) }
        var editedName by remember { mutableStateOf(user.displayName ?: "") }

        // Update edited name when user changes
        LaunchedEffect(user.displayName) { editedName = user.displayName ?: "" }

        Column(
            modifier =
                modifier.fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 56.dp) // Account for custom top bar
                    .verticalScroll(scrollState)
                    .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar with camera button
            Box(contentAlignment = Alignment.BottomEnd) {
                // Avatar circle
                Box(
                    modifier =
                        Modifier.size(130.dp)
                            .clip(CircleShape)
                            .background(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                Color.White.copy(
                                                    alpha = 0.3f
                                                ),
                                                Color.White.copy(
                                                    alpha = 0.1f
                                                )
                                            )
                                    )
                            )
                            .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .clickable(enabled = !isSaving) { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    // Priority: pendingAvatarBytes > user.avatarUrl > initials
                    when {
                        pendingAvatarBytes != null -> {
                            AsyncImage(
                                model = pendingAvatarBytes,
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        user.avatarUrl != null -> {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            // Default avatar with initials
                            Text(
                                text = getInitials(user.displayName, user.email),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Camera button
                Box(
                    modifier =
                        Modifier.size(40.dp)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable(enabled = !isSaving) { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tier Badge
            TierBadge(tier = user.tier)

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Display Name Field
                    Text(
                        text = "Display Name",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingName) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions =
                                    KeyboardActions(
                                        onDone = {
                                            if (editedName.isNotBlank()) {
                                                onUpdateName(editedName)
                                                isEditingName = false
                                                focusManager.clearFocus()
                                            }
                                        }
                                    ),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor =
                                            MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor =
                                            MaterialTheme.colorScheme.outline
                                    ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (editedName.isNotBlank()) {
                                        onUpdateName(editedName)
                                        isEditingName = false
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = !isSaving && editedName.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.displayName ?: "Not set",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = { isEditingName = true }, enabled = !isSaving) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit name",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field (read-only)
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Country Field (read-only)
                    Text(
                        text = "Country",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.country ?: "Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subscription Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Plan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text =
                                user.tier.name.lowercase().replaceFirstChar {
                                    it.uppercase()
                                },
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Status", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Active",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation
        }
    }

    private fun getInitials(displayName: String?, email: String): String {
        return if (!displayName.isNullOrBlank()) {
            displayName
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
        } else {
            email.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        }
    }

    @Composable
    private fun TierBadge(tier: UserTier) {
        val (backgroundColor, textColor) =
            when (tier) {
                UserTier.FREE -> Color(0xFF6B7280) to Color.White
                UserTier.PREMIUM -> Color(0xFFFFD700) to Color.Black
                UserTier.FAMILY -> Color(0xFF667EEA) to Color.White
                UserTier.FAMILY_MEMBER -> Color(0xFF764BA2) to Color.White
            }

        Surface(shape = RoundedCornerShape(20.dp), color = backgroundColor.copy(alpha = 0.9f)) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =
                        when (tier) {
                            UserTier.FREE -> "🆓"
                            UserTier.PREMIUM -> "⭐"
                            UserTier.FAMILY -> "👨‍👩‍👧‍👦"
                            UserTier.FAMILY_MEMBER -> "👤"
                        },
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tier.name.replace("_", " "),
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ImagePickerBottomSheet(
        onDismiss: () -> Unit,
        onPickFromGallery: (context: Any) -> Unit,
        onCaptureFromCamera: (context: Any) -> Unit
    ) {
        val sheetState = rememberModalBottomSheetState()
        val context = androidx.compose.ui.platform.LocalContext.current

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Change Profile Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Photo Library Button
                Button(
                    onClick = { onPickFromGallery(context) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Choose from Gallery",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Button
                Button(
                    onClick = { onCaptureFromCamera(context) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Take Photo",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
