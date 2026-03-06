package com.example.goaltrack.ui.screens.goals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.data.model.GoalStatus
import com.example.goaltrack.ui.theme.Dimensions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val progressAnim by animateFloatAsState(
        targetValue = goal.progressPercent / 100f,
        animationSpec = tween(800),
        label = "progress"
    )

    // Check if overdue
    val isOverdue = remember(goal.targetDate) {
        try {
            goal.targetDate.isNotBlank() &&
                    LocalDate.parse(goal.targetDate.take(10)).isBefore(LocalDate.now()) &&
                    goal.statusEnum != GoalStatus.COMPLETED
        } catch (e: Exception) { false }
    }

    val formattedDate = remember(goal.targetDate) {
        try {
            if (goal.targetDate.isBlank()) "No deadline"
            else LocalDate.parse(goal.targetDate.take(10))
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        } catch (e: Exception) { goal.targetDate.take(10) }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete \"${goal.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showContextMenu = true }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // ── Priority accent bar ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(Dimensions.priorityBarWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(goal.priorityEnum.color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Header row ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        // Category chip
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(goal.categoryEnum.displayLabel, fontSize = 11.sp)
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = goal.categoryEnum.color.copy(alpha = 0.12f),
                                labelColor = goal.categoryEnum.color
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = goal.categoryEnum.color.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    // Status chip
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                goal.statusEnum.displayLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = goal.statusEnum.color.copy(alpha = 0.12f),
                            labelColor = goal.statusEnum.color
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = goal.statusEnum.color.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // ── Progress bar ──────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${goal.progressPercent.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = goal.priorityEnum.color
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.progressBarHeight)
                            .clip(RoundedCornerShape(4.dp)),
                        color = goal.priorityEnum.color,
                        trackColor = goal.priorityEnum.color.copy(alpha = 0.15f)
                    )
                }

                // ── Footer row (date) ─────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOverdue) {
                        Text(
                            text = "• Overdue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // ── Long-press context menu ───────────────────────────────────────────
    DropdownMenu(
        expanded = showContextMenu,
        onDismissRequest = { showContextMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Edit") },
            leadingIcon = { Icon(Icons.Filled.Edit, null) },
            onClick = { showContextMenu = false; onEdit() }
        )
        DropdownMenuItem(
            text = { Text("Mark Complete") },
            leadingIcon = { Icon(Icons.Filled.Check, null) },
            onClick = { showContextMenu = false; onMarkComplete() }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
            onClick = { showContextMenu = false; showDeleteDialog = true }
        )
    }
}
