package com.example.goaltrack.ui.screens.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goaltrack.data.model.GoalCategory
import com.example.goaltrack.data.model.GoalStatus
import com.example.goaltrack.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsListScreen(
    onGoalClick: (String) -> Unit,
    onAddGoalClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: GoalsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GoalTracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            if (isSearchVisible) Icons.Filled.SearchOff else Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Outlined.BarChart, "Statistics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGoalClick,
                icon = { Icon(Icons.Filled.Add, "Add Goal") },
                text = { Text("New Goal") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search goals…") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.spacingM, vertical = Dimensions.spacingS),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // ── Status Filter Chips ───────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimensions.spacingM),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == null,
                        onClick = { viewModel.onStatusFilter(null) },
                        label = { Text("All") }
                    )
                }
                items(GoalStatus.entries) { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = {
                            viewModel.onStatusFilter(if (uiState.selectedStatus == status) null else status)
                        },
                        label = { Text(status.displayLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = status.color.copy(alpha = 0.2f),
                            selectedLabelColor = status.color
                        )
                    )
                }
            }

            // ── Category Filter Chips ─────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimensions.spacingM),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategoryFilter(null) },
                        label = { Text("All Categories") }
                    )
                }
                items(GoalCategory.entries) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = {
                            viewModel.onCategoryFilter(if (uiState.selectedCategory == cat) null else cat)
                        },
                        label = { Text(cat.displayLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color.copy(alpha = 0.2f),
                            selectedLabelColor = cat.color
                        )
                    )
                }
            }

            HorizontalDivider()

            // ── Content ───────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.goals.isEmpty() -> {
                    EmptyGoalsState(onAddGoalClick)
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = Dimensions.spacingM,
                                vertical = Dimensions.spacingM
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.goals,
                                key = { it.id }
                            ) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onClick = { onGoalClick(goal.id) },
                                    onEdit = { onGoalClick(goal.id) },
                                    onDelete = { viewModel.onDeleteGoal(goal.id) },
                                    onMarkComplete = { viewModel.onMarkComplete(goal.id) }
                                )
                            }
                            // Bottom FAB padding
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsState(onAddGoalClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.TrackChanges,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No goals yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Create your first goal and start tracking your progress today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onAddGoalClick) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create Your First Goal")
        }
    }
}
