package com.aevrontech.finevo.presentation.security

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.presentation.home.HomeScreen

class SecurityScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SecurityViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(state.isAuthenticated) {
            if (state.isAuthenticated) {
                navigator.replaceAll(HomeScreen())
            }
        }

        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header / Icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your PIN to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < state.pin.length
                        Box(
                            modifier =
                                Modifier.size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isFilled)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme
                                                .surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color =
                                            if (isFilled)
                                                MaterialTheme.colorScheme
                                                    .primary
                                            else
                                                MaterialTheme.colorScheme
                                                    .outline,
                                        shape = CircleShape
                                    )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Keypad
                Keypad(
                    onDigit = viewModel::onPinDigit,
                    onBackspace = viewModel::onBackspace,
                    onBiometric = viewModel::authenticateBiometric,
                    isBiometricAvailable = state.isBiometricAvailable
                )
            }
        }
    }
}

@Composable
private fun Keypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: () -> Unit,
    isBiometricAvailable: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        val rows =
            listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf(null, '0', "back") // null for placeholder/biometric
            )

        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { colIndex, key ->
                    // Bottom-left key: Biometric Trigger if available
                    if (rowIndex == 3 && colIndex == 0) {
                        if (isBiometricAvailable) {
                            KeypadButton(onClick = onBiometric, icon = Icons.Default.Fingerprint)
                        } else {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                    }
                    // Bottom-right key: Backspace
                    else if (key == "back") {
                        KeypadButton(
                            onClick = onBackspace,
                            icon = Icons.AutoMirrored.Filled.Backspace
                        )
                    }
                    // Digits
                    else if (key is Char) {
                        KeypadButton(onClick = { onDigit(key) }, text = key.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(onClick: () -> Unit, text: String? = null, icon: ImageVector? = null) {
    Box(
        modifier = Modifier.size(72.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
