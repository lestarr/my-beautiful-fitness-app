package com.fitness.tracker.ui.screen

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    exercises: List<Exercise>,
    exerciseCount: Int,
    useGroupedView: Boolean,
    onExerciseClick: (Exercise) -> Unit,
    onAddExercise: () -> Unit,
    onManageExercises: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.Dashboard, "Dashboard")
                    }
                    IconButton(onClick = onManageExercises) {
                        Icon(Icons.Default.Settings, "Manage")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Person, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExercise) {
                Icon(Icons.Default.Add, "Add Exercise")
            }
        }
    ) { padding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No exercises yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddExercise) {
                        Text("Add Your First Exercise")
                    }
                }
            }
        } else {
            if (useGroupedView) {
                GroupedExerciseList(
                    exercises = exercises,
                    onExerciseClick = onExerciseClick,
                    modifier = Modifier.padding(padding)
                )
            } else {
                SimpleExerciseList(
                    exercises = exercises,
                    onExerciseClick = onExerciseClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun SimpleExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(exercises) { exercise ->
            ExerciseListItem(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) }
            )
        }
    }
}

@Composable
fun GroupedExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedExercises = exercises.groupBy { it.bodyPart }

    LazyColumn(modifier = modifier) {
        groupedExercises.forEach { (bodyPart, exerciseList) ->
            item {
                Text(
                    text = bodyPart.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )
            }
            items(exerciseList) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = exercise.bodyPart,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}
