package com.example.goaltrack.ui.screens.addeditgoal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.data.model.GoalCategory
import com.example.goaltrack.data.model.Priority
import com.example.goaltrack.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    BackHandler { showUnsavedDialog = true }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = { showUnsavedDialog = false; onBack() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) { Text("Keep Editing") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Goal" else "New Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { showUnsavedDialog = true }) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onSave, enabled = !uiState.isLoading) {
                        Icon(Icons.Filled.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = Dimensions.spacingM,
                top = Dimensions.spacingM,
                end = Dimensions.spacingM,        bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingM),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Title ─────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Goal Title *") },
                    isError = uiState.titleError != null,
                    supportingText = uiState.titleError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Text("${uiState.title.length}/100", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
            }

            // ── Description ───────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Category selector ─────────────────────────────────────────
            item {
                FormSection("Category") {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(GoalCategory.entries.size) { index ->
                            val cat = GoalCategory.entries[index]
                            FilterChip(
                                selected = uiState.category == cat,
                                onClick = { viewModel.onCategoryChange(cat) },
                                label = { Text(cat.displayLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                    selectedLabelColor = cat.color
                                )
                            )
                        }
                    }
                }
            }

            // ── Priority selector ─────────────────────────────────────────
            item {
                FormSection("Priority") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        Priority.entries.forEachIndexed { index, priority ->
                            SegmentedButton(
                                selected = uiState.priority == priority,
                                onClick = { viewModel.onPriorityChange(priority) },
                                shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = priority.color.copy(alpha = 0.2f),
                                    activeContentColor = priority.color
                                )
                            ) { Text(priority.displayLabel) }
                        }
                    }
                }
            }

            // ── Target Value + Unit ───────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.targetValue,
                        onValueChange = viewModel::onTargetValueChange,
                        label = { Text("Target *") },
                        isError = uiState.targetValueError != null,
                        supportingText = uiState.targetValueError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.unit,
                        onValueChange = viewModel::onUnitChange,
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Dates ─────────────────────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.startDate,
                        onValueChange = viewModel::onStartDateChange,
                        label = { Text("Start Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, null, Modifier.size(18.dp)) }
                    )
                    OutlinedTextField(
                        value = uiState.targetDate,
                        onValueChange = viewModel::onTargetDateChange,
                        label = { Text("Target Date") },
                        isError = uiState.targetDateError != null,
                        supportingText = uiState.targetDateError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Event, null, Modifier.size(18.dp)) }
                    )
                }
            }

            // ── Milestones ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Milestones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = viewModel::onAddMilestone) {
                        Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            itemsIndexed(uiState.milestones) { index, milestone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Flag, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = milestone,
                        onValueChange = { viewModel.onMilestoneChange(index, it) },
                        placeholder = { Text("Milestone title…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(onClick = { viewModel.onRemoveMilestone(index) }) {
                        Icon(Icons.Filled.RemoveCircleOutline, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ── Save button ───────────────────────────────────────────────
            item {
                Button(
                    onClick = viewModel::onSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Save, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isEditMode) "Update Goal" else "Create Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}
