package com.fitness.tracker.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.database.entity.LogWithExercise
import com.fitness.tracker.util.UnitConverter
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

enum class DashboardView {
    LIST, TABLE, CHART
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    exercises: List<Exercise>,
    allLogsGrouped: Map<Long, List<LogWithExercise>>,
    personalRecords: Map<Long, Double>,
    useKg: Boolean,
    tableLogCount: Int,
    onBack: () -> Unit,
    onExerciseClick: (Long) -> Unit
) {
    var currentView by remember { mutableStateOf(DashboardView.LIST) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // List view button
                    IconButton(onClick = { currentView = DashboardView.LIST }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "List View",
                            tint = if (currentView == DashboardView.LIST)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Table view button
                    IconButton(onClick = { currentView = DashboardView.TABLE }) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = "Table View",
                            tint = if (currentView == DashboardView.TABLE)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Chart view button
                    IconButton(onClick = { currentView = DashboardView.CHART }) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = "Chart View",
                            tint = if (currentView == DashboardView.CHART)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
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
            when (currentView) {
                DashboardView.LIST -> {
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
                DashboardView.TABLE -> {
                    TableView(
                        exercises = exercises,
                        allLogsGrouped = allLogsGrouped,
                        useKg = useKg,
                        tableLogCount = tableLogCount,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
                DashboardView.CHART -> {
                    ChartView(
                        exercises = exercises.filter { allLogsGrouped.containsKey(it.id) },
                        allLogsGrouped = allLogsGrouped,
                        useKg = useKg,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
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

@Composable
fun TableView(
    exercises: List<Exercise>,
    allLogsGrouped: Map<Long, List<LogWithExercise>>,
    useKg: Boolean,
    tableLogCount: Int,
    modifier: Modifier = Modifier
) {
    val exercisesWithLogs = exercises.filter { allLogsGrouped.containsKey(it.id) }
    val groupedExercises = exercisesWithLogs.groupBy { it.bodyPart }.toSortedMap()

    LazyColumn(
        modifier = modifier.padding(8.dp)
    ) {
        groupedExercises.forEach { (bodyPart, exercisesInGroup) ->
            // Group header
            item {
                Text(
                    text = bodyPart,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            // Table for this group
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            // Exercise name column header
                            Text(
                                text = "Exercise",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .width(120.dp)
                                    .padding(4.dp),
                                maxLines = 1
                            )

                            // Log column headers (Last-3, Last-2, Last-1, etc.)
                            repeat(tableLogCount) { index ->
                                Column(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (index == 0) "Latest" else "Last-${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Divider()

                        // Exercise rows
                        exercisesInGroup.forEach { exercise ->
                            val logs = allLogsGrouped[exercise.id]?.take(tableLogCount) ?: emptyList()

                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Exercise name
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .padding(4.dp),
                                    maxLines = 2
                                )

                                // Log data columns
                                repeat(tableLogCount) { index ->
                                    val log = logs.getOrNull(index)
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .padding(horizontal = 2.dp)
                                            .border(
                                                width = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (log != null) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = UnitConverter.formatWeight(log.log.weight, useKg),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${log.log.reps} reps",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "-",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartView(
    exercises: List<Exercise>,
    allLogsGrouped: Map<Long, List<LogWithExercise>>,
    useKg: Boolean,
    modifier: Modifier = Modifier
) {
    // Track which exercises are selected
    val selectedExercises = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            exercises.forEach { exercise ->
                put(exercise.id, true) // All selected by default
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Selection controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Exercise Selection",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Select All / Deselect All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exercises.forEach { exercise ->
                                selectedExercises[exercise.id] = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Select All")
                    }
                    OutlinedButton(
                        onClick = {
                            exercises.forEach { exercise ->
                                selectedExercises[exercise.id] = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deselect All")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Exercise checkboxes
                exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedExercises[exercise.id] ?: false,
                            onCheckedChange = { isChecked ->
                                selectedExercises[exercise.id] = isChecked
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = exercise.bodyPart,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Chart
        val selectedExercisesList = exercises.filter { selectedExercises[it.id] == true }

        if (selectedExercisesList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Select at least one exercise to display chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Weight Progression",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    selectedExercisesList.forEach { exercise ->
                        val logs = allLogsGrouped[exercise.id]?.reversed() ?: emptyList()

                        if (logs.isNotEmpty()) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )

                            // Create chart data
                            val entries = logs.mapIndexed { index, logWithExercise ->
                                val weight = if (useKg) logWithExercise.log.weight
                                else UnitConverter.kgToLbs(logWithExercise.log.weight)
                                entryOf(index.toFloat(), weight.toFloat())
                            }

                            val model = entryModelOf(entries)

                            // Simple chart
                            if (entries.isNotEmpty()) {
                                ProvideChartStyle {
                                    Chart(
                                        chart = lineChart(),
                                        model = model,
                                        startAxis = rememberStartAxis(
                                            title = "Weight (${if (useKg) "kg" else "lbs"})",
                                            titleComponent = textComponent {
                                                color = MaterialTheme.colorScheme.onSurface.toArgb()
                                            }
                                        ),
                                        bottomAxis = rememberBottomAxis(
                                            title = "Session",
                                            titleComponent = textComponent {
                                                color = MaterialTheme.colorScheme.onSurface.toArgb()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
