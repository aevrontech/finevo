package com.aevrontech.finevo.presentation.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aevrontech.finevo.core.util.Platform
import com.aevrontech.finevo.presentation.home.HomeScreen
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.OnPrimary
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class LoginScreen : Screen {

    // Unique key to prevent "key used multiple times" error in Voyager transitions
    override val key: cafe.adriel.voyager.core.screen.ScreenKey = "LoginScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: AuthViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val focusManager = LocalFocusManager.current
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var isLoginMode by remember { mutableStateOf(true) }

        // Navigate on success
        LaunchedEffect(uiState.isLoggedIn) {
            if (uiState.isLoggedIn) {
                navigator.replace(HomeScreen())
            }
        }

        // Show error in Snackbar
        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long,
                        actionLabel = "Dismiss"
                    )
                    viewModel.clearError()
                }
            }
        }

        // Show success message
        LaunchedEffect(uiState.successMessage) {
            uiState.successMessage?.let { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Long
                    )
                    viewModel.clearSuccessMessage()
                }
            }
        }

        // Dashboard gradient for interactive elements
        val dashboardGradient =
            Brush.linearGradient(
                colors =
                    listOf(
                        DashboardGradientStart,
                        DashboardGradientMid,
                        DashboardGradientEnd
                    )
            )

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor =
                            if (uiState.error != null)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor =
                            if (uiState.error != null)
                                MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSurface,
                        actionColor =
                            if (uiState.error != null) Error
                            else DashboardGradientStart,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Logo with dashboard gradient
                    Text(
                        text = "FinEvo",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        style =
                            MaterialTheme.typography.displayMedium.copy(
                                brush = dashboardGradient
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isLoginMode) "Welcome back!" else "Create your account",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                        singleLine = true,
                        isError = uiState.error != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor =
                                    MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = DashboardGradientStart,
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline,
                                focusedLabelColor = DashboardGradientStart,
                                unfocusedLabelColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = DashboardGradientStart,
                                focusedLeadingIconColor = DashboardGradientStart,
                                unfocusedLeadingIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                errorBorderColor = Error
                            ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Lock
                                    else Icons.Default.Person,
                                    contentDescription =
                                        if (passwordVisible) "Hide password"
                                        else "Show password"
                                )
                            }
                        },
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction =
                                    if (isLoginMode) ImeAction.Done
                                    else ImeAction.Next
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                },
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        if (isLoginMode)
                                            viewModel.signIn(email, password)
                                        else viewModel.signUp(email, password)
                                    }
                                }
                            ),
                        singleLine = true,
                        isError = uiState.error != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor =
                                    MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = DashboardGradientStart,
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline,
                                focusedLabelColor = DashboardGradientStart,
                                unfocusedLabelColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = DashboardGradientStart,
                                focusedLeadingIconColor = DashboardGradientStart,
                                unfocusedLeadingIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTrailingIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedTrailingIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                errorBorderColor = Error
                            ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        confirmPasswordVisible = !confirmPasswordVisible
                                    }
                                ) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.Lock
                                        else Icons.Default.Person,
                                        contentDescription =
                                            if (confirmPasswordVisible) "Hide password"
                                            else "Show password"
                                    )
                                }
                            },
                            visualTransformation =
                                if (confirmPasswordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (email.isNotBlank() &&
                                            password.isNotBlank() &&
                                            confirmPassword.isNotBlank()
                                        ) {
                                            if (password != confirmPassword) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "Passwords do not match"
                                                    )
                                                }
                                            } else {
                                                viewModel.signUp(email, password)
                                            }
                                        }
                                    }
                                ),
                            singleLine = true,
                            isError =
                                uiState.error != null ||
                                    (confirmPassword.isNotBlank() &&
                                        password != confirmPassword),
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor =
                                        MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor =
                                        MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = DashboardGradientStart,
                                    unfocusedBorderColor =
                                        MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = DashboardGradientStart,
                                    unfocusedLabelColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = DashboardGradientStart,
                                    focusedLeadingIconColor = DashboardGradientStart,
                                    unfocusedLeadingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTrailingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedTrailingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    errorBorderColor = Error
                                ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                if (email.isNotBlank()) {
                                    viewModel.sendPasswordReset(email)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Please enter your email first"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Forgot password?",
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        brush = dashboardGradient
                                    ),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Inline error display
                    uiState.error?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.errorContainer
                                ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Login/Signup button with dashboard gradient
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.signIn(email, password)
                            } else {
                                if (password != confirmPassword) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Passwords do not match")
                                    }
                                } else {
                                    viewModel.signUp(email, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled =
                            !uiState.isLoading &&
                                email.isNotBlank() &&
                                password.isNotBlank() &&
                                (isLoginMode || confirmPassword.isNotBlank()),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DashboardGradientMid,
                                disabledContainerColor =
                                    DashboardGradientMid.copy(alpha = 0.4f)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = OnPrimary
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) "Sign In" else "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "  or continue with  ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Social login buttons - Google only on Android, both on
                    // iOS
                    val socialLoginHandler = remember { SocialLoginHandler() }
                    val activityContext = com.aevrontech.finevo.core.util.getActivityContext()

                    if (Platform.isAndroid) {
                        // Android: Only Google Sign-In (full width)
                        GoogleSignInButton(
                            onClick = {
                                viewModel.onSocialLoginStarted()
                                scope.launch {
                                    val activity = activityContext
                                    if (activity != null) {
                                        socialLoginHandler.signInWithGoogle(
                                            activity = activity,
                                            onSuccess = { idToken, nonce ->
                                                viewModel.signInWithGoogle(idToken, nonce)
                                            },
                                            onError = { error ->
                                                viewModel.onSocialLoginError(error)
                                            }
                                        )
                                    } else {
                                        viewModel.onSocialLoginError(
                                            "Unable to access activity for sign-in"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isEnabled = !uiState.isLoading
                        )
                    } else {
                        // iOS: Both Google and Apple
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GoogleSignInButton(
                                onClick = {
                                    viewModel.onSocialLoginStarted()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Google Sign-In requires additional setup on iOS"
                                        )
                                        viewModel.clearError()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isEnabled = !uiState.isLoading
                            )

                            AppleSignInButton(
                                onClick = {
                                    viewModel.onSocialLoginStarted()
                                    scope.launch {
                                        socialLoginHandler.signInWithApple(
                                            activity = activityContext ?: Unit,
                                            onSuccess = { idToken, nonce ->
                                                viewModel.signInWithApple(idToken, nonce)
                                            },
                                            onError = { error ->
                                                viewModel.onSocialLoginError(error)
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isEnabled = !uiState.isLoading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Toggle login/signup
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text =
                                if (isLoginMode) "Don't have an account?"
                                else "Already have an account?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                isLoginMode = !isLoginMode
                                viewModel.clearError()
                            }
                        ) {
                            Text(
                                text = if (isLoginMode) "Sign Up" else "Sign In",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        brush = dashboardGradient
                                    ),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/** Google Sign-In button with official branding. */
@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google logo using Canvas with official brand colors
            androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                val googleBlue = Color(0xFF4285F4)
                val googleRed = Color(0xFFEA4335)
                val googleYellow = Color(0xFFFBBC05)
                val googleGreen = Color(0xFF34A853)

                // Official Google "G" logo approximation
                // Blue segment (right side, top-right of G)
                drawArc(color = googleBlue, startAngle = -45f, sweepAngle = 90f, useCenter = true)
                // Green segment (bottom-right of G)
                drawArc(color = googleGreen, startAngle = 45f, sweepAngle = 90f, useCenter = true)
                // Yellow segment (bottom-left of G)
                drawArc(color = googleYellow, startAngle = 135f, sweepAngle = 90f, useCenter = true)
                // Red segment (top-left of G)
                drawArc(color = googleRed, startAngle = 225f, sweepAngle = 90f, useCenter = true)
                // Inner white circle to create the ring effect
                drawCircle(color = Color.White, radius = size.minDimension * 0.35f)
                // Blue horizontal bar for the "G" shape
                drawRect(
                    color = googleBlue,
                    topLeft =
                        androidx.compose.ui.geometry.Offset(
                            size.width * 0.48f,
                            size.height * 0.35f
                        ),
                    size =
                        androidx.compose.ui.geometry.Size(
                            size.width * 0.52f,
                            size.height * 0.3f
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google", fontSize = 14.sp)
        }
    }
}

/** Apple Sign-In button with Apple branding. */
@Composable
private fun AppleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Apple logo using SF Symbol-style text
            Text(
                text = "", // Apple logo character
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Apple", fontSize = 14.sp)
        }
    }
}
