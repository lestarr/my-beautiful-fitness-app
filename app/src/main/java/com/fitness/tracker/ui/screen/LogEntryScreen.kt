package com.fitness.tracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.database.entity.LogWithExercise
import com.fitness.tracker.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    exercise: Exercise,
    lastThreeLogs: List<LogWithExercise>,
    personalRecordWeight: Double?,
    useKg: Boolean,
    onBack: () -> Unit,
    onSaveLog: (Double, Int) -> Unit,
    onEditLog: (LogWithExercise) -> Unit,
    onDeleteLog: (LogWithExercise) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var showNewPrDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.name) },
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
                .padding(16.dp)
        ) {
            // Personal Record
            personalRecordWeight?.let { pr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Personal Record",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                UnitConverter.formatWeight(pr, useKg),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Last 3 Logs
            if (lastThreeLogs.isNotEmpty()) {
                Text(
                    "Recent Logs",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                lastThreeLogs.forEach { logWithExercise ->
                    LogHistoryItem(
                        logWithExercise = logWithExercise,
                        useKg = useKg,
                        onEdit = { onEditLog(logWithExercise) },
                        onDelete = { onDeleteLog(logWithExercise) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Add New Log
            Text(
                "Add New Log",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (${if (useKg) "kg" else "lbs"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val weightValue = weight.toDoubleOrNull()
                    val repsValue = reps.toIntOrNull()

                    if (weightValue != null && repsValue != null) {
                        val weightInKg = if (useKg) weightValue else UnitConverter.lbsToKg(weightValue)
                        onSaveLog(weightInKg, repsValue)
                        weight = ""
                        reps = ""

                        // Check if it's a new PR
                        if (personalRecordWeight == null || weightInKg > personalRecordWeight) {
                            showNewPrDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = weight.toDoubleOrNull() != null && reps.toIntOrNull() != null
            ) {
                Text("Save Log")
            }
        }
    }

    if (showNewPrDialog) {
        AlertDialog(
            onDismissRequest = { showNewPrDialog = false },
            icon = { Icon(Icons.Default.EmojiEvents, null) },
            title = { Text("New Personal Record!") },
            text = { Text("Congratulations! You've set a new personal record!") },
            confirmButton = {
                TextButton(onClick = { showNewPrDialog = false }) {
                    Text("Awesome!")
                }
            }
        )
    }
}

@Composable
fun LogHistoryItem(
    logWithExercise: LogWithExercise,
    useKg: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${UnitConverter.formatWeight(logWithExercise.log.weight, useKg)} × ${logWithExercise.log.reps} reps",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = dateFormat.format(Date(logWithExercise.log.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
