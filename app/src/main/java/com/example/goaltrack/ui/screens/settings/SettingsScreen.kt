package com.example.goaltrack.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.goaltrack.data.model.AppTheme
import com.example.goaltrack.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) onSignedOut()
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; viewModel.onSignOut() }) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = Dimensions.spacingM,
                vertical = Dimensions.spacingM
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingS),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {

            // ── Profile section ───────────────────────────────────────────
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.padding(Dimensions.spacingM),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        if (uiState.profile?.avatarUrl != null) {
                            AsyncImage(
                                model = uiState.profile!!.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(Dimensions.avatarSizeM).clip(CircleShape)
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(Dimensions.avatarSizeM).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, null, Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                        Column {
                            Text(
                                uiState.profile?.fullName ?: "—",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "@${uiState.profile?.username ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Appearance ────────────────────────────────────────────────
            item { SectionHeader("Appearance") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            AppTheme.entries.forEachIndexed { index, theme ->
                                SegmentedButton(
                                    selected = uiState.settings.themeEnum == theme,
                                    onClick = { viewModel.onThemeChange(theme) },
                                    shape = SegmentedButtonDefaults.itemShape(index, AppTheme.entries.size)
                                ) { Text(theme.displayLabel) }
                            }
                        }
                    }
                }
            }

            // ── Notifications ─────────────────────────────────────────────
            item { SectionHeader("Notifications") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsToggleRow(
                            title = "Enable Notifications",
                            checked = uiState.settings.notificationsEnabled,
                            onCheckedChange = viewModel::onNotificationsToggle
                        )
                        HorizontalDivider()
                        SettingsToggleRow(
                            title = "Weekly Progress Report",
                            checked = uiState.settings.weeklyReportEnabled,
                            onCheckedChange = viewModel::onWeeklyReportToggle,
                            enabled = uiState.settings.notificationsEnabled
                        )
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────────
            item { SectionHeader("About") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsInfoRow("App Version", "1.0.0")
                        HorizontalDivider()
                        SettingsInfoRow("Build", "Release")
                    }
                }
            }

            // ── Sign out button ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(Dimensions.spacingM))
                Button(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isSigning
                ) {
                    if (uiState.isSigning) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Logout, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Box(modifier = Modifier.padding(Dimensions.spacingM)) { content() }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
