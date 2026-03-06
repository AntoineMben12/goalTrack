package com.example.goaltrack.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.data.model.GoalStatistics
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.data.model.StatsPeriod
import com.example.goaltrack.ui.theme.Dimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
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
                horizontal = Dimensions.spacingM,
                vertical = Dimensions.spacingM
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingM),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Period selector ───────────────────────────────────────────
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StatsPeriod.entries.forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = uiState.selectedPeriod == period,
                            onClick = { viewModel.onPeriodChange(period) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriod.entries.size)
                        ) {
                            Text(period.displayLabel, fontSize = 12.sp)
                        }
                    }
                }
            }

            // ── Summary cards ─────────────────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        StatCard("Total Goals", uiState.statistics.totalGoals.toString(),
                            MaterialTheme.colorScheme.primary)
                    }
                    item {
                        StatCard("Active", uiState.statistics.activeGoals.toString(),
                            MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        StatCard("Completed", uiState.statistics.completedGoals.toString(),
                            MaterialTheme.colorScheme.tertiary)
                    }
                    item {
                        StatCard("Rate", "${uiState.statistics.completionRate.toInt()}%",
                            MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ── Canvas Line Chart ─────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recent Activity", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        ProgressSparklineChart(
                            data = uiState.recentActivity.take(7).map { it.value.toFloat() }.reversed(),
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                }
            }

            // ── Goals due this week ───────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Due This Week", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text(
                                "${uiState.statistics.goalsDueThisWeek} goal(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Recent Activity feed ──────────────────────────────────────
            item {
                Text("Recent Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.recentActivity.take(10)) { update ->
                ActivityFeedItem(update)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProgressSparklineChart(data: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val max = data.maxOrNull() ?: 1f
        val min = 0f
        val range = (max - min).coerceAtLeast(1f)
        val step = size.width / (data.size - 1)

        val points = data.mapIndexed { i, v ->
            Offset(
                x = i * step,
                y = size.height - ((v - min) / range * size.height * 0.9f)
            )
        }

        // Fill area
        val path = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(path, fillColor)

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, lineColor, style = Stroke(width = 3.dp.toPx()))

        // Dots
        points.forEach { drawCircle(lineColor, radius = 4.dp.toPx(), center = it) }
    }
}

@Composable
private fun ActivityFeedItem(update: ProgressUpdate) {
    val formatted = remember(update.loggedAt) {
        try {
            LocalDateTime.parse(update.loggedAt.take(19))
                .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
        } catch (e: Exception) { update.loggedAt.take(10) }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "+${update.value}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary
            )
            update.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(formatted, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}
