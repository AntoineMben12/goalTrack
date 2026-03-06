package com.example.goaltrack.ui.screens.goaldetails

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.ui.theme.Dimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    goalId: String,
    onEditGoal: () -> Unit,
    onLogProgress: () -> Unit,
    onBack: () -> Unit,
    viewModel: GoalDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.goal

    val progressAnim by animateFloatAsState(
        targetValue = goal?.progressPercent?.div(100f) ?: 0f,
        animationSpec = tween(1000),
        label = "progress_anim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        goal?.title ?: "Goal Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditGoal) {
                        Icon(Icons.Outlined.Edit, "Edit")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.spacingM, vertical = Dimensions.spacingS),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditGoal,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Goal")
                    }
                    Button(
                        onClick = onLogProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.AddChart, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Progress")
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = Dimensions.spacingM,
                end = Dimensions.spacingM,
                top = Dimensions.spacingM,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingM),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Hero section: circular progress ──────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { progressAnim },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 12.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round,
                                color = goal?.priorityEnum?.color ?: MaterialTheme.colorScheme.primary
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.TrackChanges,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = goal?.categoryEnum?.color ?: MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${goal?.progressPercent?.toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Meta row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            goal?.let {
                                MetaItem(it.priorityEnum.displayLabel, "Priority", it.priorityEnum.color)
                                MetaItem(it.statusEnum.displayLabel, "Status", it.statusEnum.color)
                                MetaItem(
                                    text = if (it.targetDate.isNotBlank()) {
                                        try {
                                            val days = java.time.temporal.ChronoUnit.DAYS.between(
                                                java.time.LocalDate.now(),
                                                java.time.LocalDate.parse(it.targetDate.take(10))
                                            )
                                            "${days}d"
                                        } catch (e: Exception) { "—" }
                                    } else "—",
                                    label = "Remaining",
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            // ── Description ───────────────────────────────────────────────
            goal?.description?.let { desc ->
                item {
                    SectionCard(title = "Description") {
                        Text(desc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ── Milestones ────────────────────────────────────────────────
            if (uiState.milestones.isNotEmpty()) {
                item {
                    SectionCard(title = "Milestones (${uiState.milestones.count { it.isCompleted }}/${uiState.milestones.size})") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.milestones.forEach { milestone ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Checkbox(
                                        checked = milestone.isCompleted,
                                        onCheckedChange = {
                                            viewModel.onToggleMilestone(milestone.id, milestone.isCompleted)
                                        }
                                    )
                                    Text(
                                        text = milestone.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else null,
                                        color = if (milestone.isCompleted)
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Progress History ──────────────────────────────────────────
            if (uiState.progressHistory.isNotEmpty()) {
                item {
                    Text(
                        "Progress History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.progressHistory) { entry ->
                    ProgressEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun MetaItem(text: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun ProgressEntryCard(entry: ProgressUpdate) {
    val formatted = remember(entry.loggedAt) {
        try {
            LocalDateTime.parse(entry.loggedAt.take(19))
                .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
        } catch (e: Exception) { entry.loggedAt.take(10) }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "+${entry.value}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                entry.note?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(
                text = formatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
