package com.fitness.tracker.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object UserSelection : Screen("user_selection")
    object ExerciseList : Screen("exercise_list")
    object ExerciseManagement : Screen("exercise_management")
    object LogEntry : Screen("log_entry/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "log_entry/$exerciseId"
    }
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
}
