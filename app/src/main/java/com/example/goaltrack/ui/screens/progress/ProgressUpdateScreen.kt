package com.example.goaltrack.ui.screens.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressUpdateScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProgressUpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onSaved() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    val progressAnim by animateFloatAsState(
        targetValue = uiState.previewProgress / 100f,
        animationSpec = tween(400),
        label = "preview_progress"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.spacingM),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingM)
        ) {
            // ── Goal header ───────────────────────────────────────────────
            uiState.goal?.let { goal ->
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Current: ${goal.currentValue} ${goal.unit} · Target: ${goal.targetValue} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Animated circular preview ─────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        CircularProgressIndicator(
                            progress = { progressAnim },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 12.dp,
                            strokeCap = StrokeCap.Round,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "${uiState.previewProgress.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Before/after comparison
                    if (uiState.goal != null) {
                        Text(
                            "Was: ${uiState.goal!!.progressPercent.toInt()}%  →  Will be: ${uiState.previewProgress.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Confetti text when goal reached
                    if (uiState.isGoalReached) {
                        Text(
                            "🎉 Goal Complete!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // ── Value input + slider ──────────────────────────────────────
            OutlinedTextField(
                value = uiState.newValueInput,
                onValueChange = viewModel::onValueChange,
                label = { Text("New Value") },
                suffix = { Text(uiState.goal?.unit ?: "") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Slider alternative
            uiState.goal?.let { goal ->
                Column {
                    Text("Drag to set value", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = uiState.newValue.toFloat().coerceIn(0f, goal.targetValue.toFloat()),
                        onValueChange = { viewModel.onValueChange(it.toInt().toString()) },
                        valueRange = 0f..goal.targetValue.toFloat()
                    )
                }
            }

            // ── Journal note ──────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                placeholder = { Text("How did it go? Any reflections…") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Save button ───────────────────────────────────────────────
            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Progress", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
