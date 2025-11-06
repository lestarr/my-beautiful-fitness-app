package com.fitness.tracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.database.entity.LogWithExercise
import com.fitness.tracker.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    exercises: List<Exercise>,
    allLogsGrouped: Map<Long, List<LogWithExercise>>,
    personalRecords: Map<Long, Double>,
    useKg: Boolean,
    onBack: () -> Unit,
    onExerciseClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (exercises.isEmpty() || allLogsGrouped.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No data to display. Start logging your workouts!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(exercises.filter { allLogsGrouped.containsKey(it.id) }) { exercise ->
                    val logs = allLogsGrouped[exercise.id] ?: emptyList()
                    val pr = personalRecords[exercise.id]

                    ExerciseDashboardCard(
                        exercise = exercise,
                        logs = logs,
                        personalRecord = pr,
                        useKg = useKg,
                        onClick = { onExerciseClick(exercise.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDashboardCard(
    exercise: Exercise,
    logs: List<LogWithExercise>,
    personalRecord: Double?,
    useKg: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge
                )
                personalRecord?.let { pr ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            UnitConverter.formatWeight(pr, useKg),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.bodyPart,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Weight progression (last 5 entries)
            Text(
                "Recent Progress",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            logs.take(5).forEach { logWithExercise ->
                WeightProgressionItem(logWithExercise, useKg)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Total logs: ${logs.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeightProgressionItem(
    logWithExercise: LogWithExercise,
    useKg: Boolean
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dateFormat.format(Date(logWithExercise.log.date)),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${UnitConverter.formatWeight(logWithExercise.log.weight, useKg)} × ${logWithExercise.log.reps}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
