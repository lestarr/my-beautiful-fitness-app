package com.fitness.tracker

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitness.tracker.data.database.AppDatabase
import com.fitness.tracker.data.repository.LogRepository
import com.fitness.tracker.navigation.Screen
import com.fitness.tracker.ui.screen.*
import com.fitness.tracker.ui.theme.FitnessTrackerTheme
import com.fitness.tracker.ui.viewmodel.ExerciseViewModel
import com.fitness.tracker.ui.viewmodel.LogViewModel
import com.fitness.tracker.ui.viewmodel.UserViewModel
import com.fitness.tracker.util.CsvHelper
import com.fitness.tracker.util.EmailHelper
import com.fitness.tracker.util.PreferencesManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferencesManager = PreferencesManager(this)

        setContent {
            FitnessTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitnessTrackerApp(
                        userViewModel = userViewModel,
                        exerciseViewModel = exerciseViewModel,
                        logViewModel = logViewModel,
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}

@Composable
fun FitnessTrackerApp(
    userViewModel: UserViewModel,
    exerciseViewModel: ExerciseViewModel,
    logViewModel: LogViewModel,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val currentUser by userViewModel.currentUser.collectAsState()
    val allUsers by userViewModel.allUsers.collectAsState(initialValue = emptyList())
    val allExercises by exerciseViewModel.allExercises.collectAsState(initialValue = emptyList())
    val allExercisesGrouped by exerciseViewModel.allExercisesGroupedByBodyPart.collectAsState(initialValue = emptyList())
    val exerciseCount by exerciseViewModel.exerciseCount.collectAsState()

    var isAuthenticated by remember { mutableStateOf(!preferencesManager.authEnabled) }
    var useKg by remember { mutableStateOf(preferencesManager.useKg) }

    val startDestination = if (preferencesManager.authEnabled && !isAuthenticated) {
        Screen.Auth.route
    } else if (currentUser == null) {
        Screen.UserSelection.route
    } else {
        Screen.ExerciseList.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screen
        composable(Screen.Auth.route) {
            AuthScreen(
                savedPin = preferencesManager.appPin,
                onAuthenticated = {
                    isAuthenticated = true
                    navController.navigate(
                        if (currentUser == null) Screen.UserSelection.route else Screen.ExerciseList.route
                    ) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onSetupPin = { pin ->
                    preferencesManager.appPin = pin
                    preferencesManager.authEnabled = true
                    isAuthenticated = true
                    navController.navigate(
                        if (currentUser == null) Screen.UserSelection.route else Screen.ExerciseList.route
                    ) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // User Selection Screen
        composable(Screen.UserSelection.route) {
            UserSelectionScreen(
                users = allUsers,
                currentUser = currentUser,
                onUserSelected = { user ->
                    userViewModel.setCurrentUser(user)
                },
                onAddUser = { name ->
                    userViewModel.createUser(name) {}
                },
                onDeleteUser = { user ->
                    scope.launch {
                        val database = AppDatabase.getDatabase(context)
                        val logRepository = LogRepository(database.logDao())
                        val logs = mutableListOf<com.fitness.tracker.data.database.entity.LogWithExercise>()

                        logRepository.getAllLogsWithExerciseForUser(user.id).collect { userLogs ->
                            logs.addAll(userLogs)
                        }

                        // Export and email logs
                        if (logs.isNotEmpty()) {
                            val file = CsvHelper.exportLogsToFile(context, logs, user.name)
                            file?.let {
                                EmailHelper.sendEmailWithAttachment(
                                    context,
                                    "",
                                    "Fitness Logs Export - ${user.name}",
                                    "Please find attached your fitness logs.",
                                    it
                                )
                            }
                        }

                        userViewModel.deleteUser(user) {
                            Toast.makeText(context, "User deleted and logs exported", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onContinue = {
                    navController.navigate(Screen.ExerciseList.route) {
                        popUpTo(Screen.UserSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // Exercise List Screen
        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(
                exercises = if (exerciseCount > 20) allExercisesGrouped else allExercises,
                exerciseCount = exerciseCount,
                useGroupedView = exerciseCount > 20,
                onExerciseClick = { exercise ->
                    navController.navigate(Screen.LogEntry.createRoute(exercise.id))
                },
                onAddExercise = {
                    // Show add dialog through management screen
                    navController.navigate(Screen.ExerciseManagement.route)
                },
                onManageExercises = {
                    navController.navigate(Screen.ExerciseManagement.route)
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Exercise Management Screen
        composable(Screen.ExerciseManagement.route) {
            ExerciseManagementScreen(
                exercises = allExercises,
                onBack = { navController.popBackStack() },
                onAddExercise = { name, bodyPart ->
                    exerciseViewModel.createExercise(name, bodyPart) {
                        Toast.makeText(context, "Exercise added", Toast.LENGTH_SHORT).show()
                    }
                },
                onEditExercise = { exercise ->
                    exerciseViewModel.updateExercise(exercise) {
                        Toast.makeText(context, "Exercise updated", Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteExercise = { exercise ->
                    exerciseViewModel.deleteExercise(exercise) {
                        Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT).show()
                    }
                },
                onImportCsv = { uri ->
                    scope.launch {
                        val exercises = CsvHelper.parseExercisesFromCsv(context, uri)
                        if (exercises.isNotEmpty()) {
                            exerciseViewModel.importExercises(exercises) {
                                Toast.makeText(context, "Imported ${exercises.size} exercises", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No exercises found in file", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onExportCsv = { uri ->
                    scope.launch {
                        val success = CsvHelper.exportExercisesToCsv(context, uri, allExercises)
                        Toast.makeText(
                            context,
                            if (success) "Exercises exported" else "Export failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        // Log Entry Screen
        composable(
            route = Screen.LogEntry.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: return@composable
            val exercise = allExercises.find { it.id == exerciseId } ?: return@composable

            val lastThreeLogs by logViewModel.getLastThreeLogsForExercise(
                currentUser?.id ?: return@composable,
                exerciseId
            ).collectAsState(initialValue = emptyList())

            val personalRecordWeight by logViewModel.personalRecordWeight.collectAsState()

            LaunchedEffect(exerciseId) {
                logViewModel.loadPersonalRecord(currentUser?.id ?: return@LaunchedEffect, exerciseId)
            }

            LogEntryScreen(
                exercise = exercise,
                lastThreeLogs = lastThreeLogs,
                personalRecordWeight = personalRecordWeight,
                useKg = useKg,
                onBack = { navController.popBackStack() },
                onSaveLog = { weight, reps ->
                    logViewModel.createLog(
                        userId = currentUser?.id ?: return@LogEntryScreen,
                        exerciseId = exerciseId,
                        weight = weight,
                        reps = reps
                    ) { isNewPR ->
                        Toast.makeText(
                            context,
                            if (isNewPR) "New PR! Log saved" else "Log saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onEditLog = { logWithExercise ->
                    // Edit functionality can be added via dialog
                    Toast.makeText(context, "Edit functionality - to be implemented via dialog", Toast.LENGTH_SHORT).show()
                },
                onDeleteLog = { logWithExercise ->
                    logViewModel.deleteLog(logWithExercise.log) {
                        Toast.makeText(context, "Log deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            val userId = currentUser?.id ?: return@composable

            // Collect all logs grouped by exercise
            val allLogsForUser by logViewModel.getAllLogsWithExerciseForUser(userId)
                .collectAsState(initialValue = emptyList())

            val logsGroupedByExercise = allLogsForUser.groupBy { it.exercise.id }
            val personalRecords = remember(allLogsForUser) {
                logsGroupedByExercise.mapValues { (_, logs) ->
                    logs.maxOfOrNull { it.log.weight } ?: 0.0
                }
            }

            DashboardScreen(
                exercises = allExercises.filter { logsGroupedByExercise.containsKey(it.id) },
                allLogsGrouped = logsGroupedByExercise,
                personalRecords = personalRecords,
                useKg = useKg,
                onBack = { navController.popBackStack() },
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.LogEntry.createRoute(exerciseId))
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                currentUser = currentUser,
                useKg = useKg,
                authEnabled = preferencesManager.authEnabled,
                onBack = { navController.popBackStack() },
                onSwitchUser = {
                    navController.navigate(Screen.UserSelection.route)
                },
                onToggleUnit = { newUseKg ->
                    useKg = newUseKg
                    preferencesManager.useKg = newUseKg
                },
                onToggleAuth = { enabled ->
                    if (enabled && preferencesManager.appPin == null) {
                        // Navigate to setup PIN
                        navController.navigate(Screen.Auth.route)
                    } else {
                        preferencesManager.authEnabled = enabled
                    }
                },
                onExportUserLogs = {
                    scope.launch {
                        val userId = currentUser?.id ?: return@launch
                        val database = AppDatabase.getDatabase(context)
                        val logRepository = LogRepository(database.logDao())
                        val logs = mutableListOf<com.fitness.tracker.data.database.entity.LogWithExercise>()

                        logRepository.getAllLogsWithExerciseForUser(userId).collect { userLogs ->
                            logs.addAll(userLogs)
                        }

                        if (logs.isNotEmpty()) {
                            val file = CsvHelper.exportLogsToFile(context, logs, currentUser?.name ?: "user")
                            file?.let {
                                EmailHelper.sendEmailWithAttachment(
                                    context,
                                    "",
                                    "My Fitness Logs Export",
                                    "Please find attached your fitness logs.",
                                    it
                                )
                            }
                        } else {
                            Toast.makeText(context, "No logs to export", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}
