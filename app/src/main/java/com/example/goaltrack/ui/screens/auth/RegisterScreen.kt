package com.example.goaltrack.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.ui.theme.GradientEnd
import com.example.goaltrack.ui.theme.GradientMid
import com.example.goaltrack.ui.theme.GradientStart

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onRegisterSuccess() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientMid, GradientEnd)))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(
                                initialOffsetY = { -60 }, // Ensure this is just an Int return
                                animationSpec = tween(600)
                            )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Start tracking your goals today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 700, delayMillis = 200)
                    ) + slideInVertically(
                        initialOffsetY = { 100 }, // This lambda returns the starting pixel offset
                        animationSpec = tween(durationMillis = 700, delayMillis = 200)
                    )
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Your details",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Full Name
                            OutlinedTextField(
                                value = uiState.fullName,
                                onValueChange = viewModel::onFullNameChange,
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Outlined.Person, null) },
                                isError = uiState.fullNameError != null,
                                supportingText = uiState.fullNameError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Username
                            OutlinedTextField(
                                value = uiState.username,
                                onValueChange = viewModel::onUsernameChange,
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null) },
                                isError = uiState.usernameError != null,
                                supportingText = uiState.usernameError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Email
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = viewModel::onEmailChange,
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                                isError = uiState.emailError != null,
                                supportingText = uiState.emailError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Password
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = viewModel::onPasswordChange,
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                                trailingIcon = {
                                    IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                                        Icon(
                                            if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Toggle"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = uiState.passwordError != null,
                                supportingText = uiState.passwordError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Confirm Password
                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = viewModel::onConfirmPasswordChange,
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Outlined.LockOpen, null) },
                                trailingIcon = {
                                    IconButton(onClick = viewModel::onToggleConfirmVisibility) {
                                        Icon(
                                            if (uiState.isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Toggle"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = uiState.confirmPasswordError != null,
                                supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onRegisterClick()
                                }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { focusManager.clearFocus(); viewModel.onRegisterClick() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Create Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 400))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Already have an account?", color = Color.White.copy(alpha = 0.8f))
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Log In", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
