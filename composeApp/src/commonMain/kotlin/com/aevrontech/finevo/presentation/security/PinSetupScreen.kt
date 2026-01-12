package com.aevrontech.finevo.presentation.security

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

class PinSetupScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PinSetupViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(state.isComplete) {
            if (state.isComplete) {
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Set PIN") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text =
                        if (state.step == PinSetupStep.ENTER_NEW) "Enter New PIN"
                        else "Confirm PIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < state.currentPin.length
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

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Keypad
                Keypad(onDigit = viewModel::onPinDigit, onBackspace = viewModel::onBackspace)
            }
        }
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        val rows =
            listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf(null, '0', "back")
            )

        rows.forEachIndexed { _, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { _, key ->
                    if (key == "back") {
                        KeypadButton(
                            onClick = onBackspace,
                            icon = Icons.AutoMirrored.Filled.Backspace
                        )
                    } else if (key is Char) {
                        KeypadButton(onClick = { onDigit(key) }, text = key.toString())
                    } else {
                        Spacer(modifier = Modifier.size(72.dp))
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
