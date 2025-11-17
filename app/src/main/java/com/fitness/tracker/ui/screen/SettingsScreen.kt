package com.fitness.tracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitness.tracker.data.database.entity.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    useKg: Boolean,
    authEnabled: Boolean,
    tableLogCount: Int,
    onBack: () -> Unit,
    onSwitchUser: () -> Unit,
    onToggleUnit: (Boolean) -> Unit,
    onToggleAuth: (Boolean) -> Unit,
    onTableLogCountChange: (Int) -> Unit,
    onExportUserLogs: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Current User Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Current User",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                currentUser?.name ?: "No user selected",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        TextButton(onClick = onSwitchUser) {
                            Text("Switch")
                        }
                    }
                }
            }

            Divider()

            // Unit Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleUnit(!useKg) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Weight Unit", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (useKg) "Kilograms (kg)" else "Pounds (lbs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = useKg,
                    onCheckedChange = onToggleUnit
                )
            }

            Divider()

            // Table Log Count Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Table View Columns", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Show last $tableLogCount logs per exercise",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = tableLogCount.toFloat(),
                        onValueChange = { onTableLogCountChange(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("10", style = MaterialTheme.typography.bodySmall)
                }
            }

            Divider()

            // Auth Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAuth(!authEnabled) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("App Lock", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Require PIN to access app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = authEnabled,
                    onCheckedChange = onToggleAuth
                )
            }

            Divider()

            // Export User Logs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExportUserLogs)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Export My Logs", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Download all logs as CSV",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
