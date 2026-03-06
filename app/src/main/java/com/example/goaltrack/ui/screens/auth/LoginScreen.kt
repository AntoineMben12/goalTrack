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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.ui.theme.GradientEnd
import com.example.goaltrack.ui.theme.GradientMid
import com.example.goaltrack.ui.theme.GradientStart

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Show content with slide + fade animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Navigate on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    // Show error snackbar
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientMid, GradientEnd)
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // ── Logo & App Name ────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -80 },
                        animationSpec = tween(600)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Target/bullseye icon drawn in compose
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TrackChanges,
                                contentDescription = "GoalTracker Logo",
                                modifier = Modifier.size(52.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "GoalTracker",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Achieve more, one goal at a time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // ── Form Card ─────────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(
                        initialOffsetY = { 120 },
                        animationSpec = tween(700, delayMillis = 200)
                    )
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Welcome back",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Email
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = viewModel::onEmailChange,
                                label = { Text("Email") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Email, contentDescription = null)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Password
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = viewModel::onPasswordChange,
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                                        Icon(
                                            imageVector = if (uiState.isPasswordVisible)
                                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isPasswordVisible)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.onLoginClick()
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Forgot password
                            Box(modifier = Modifier.fillMaxWidth()) {
                                TextButton(
                                    onClick = { /* TODO: forgot password flow */ },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Text("Forgot Password?", fontSize = 13.sp)
                                }
                            }

                            // Login button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.onLoginClick()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Log In",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Register link ──────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 400))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        TextButton(onClick = onNavigateToRegister) {
                            Text(
                                text = "Sign Up",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
