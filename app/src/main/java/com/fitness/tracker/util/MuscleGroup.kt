package com.fitness.tracker.util

import com.fitness.tracker.data.database.entity.Exercise

/**
 * Enum representing major muscle groups for exercise categorization
 */
enum class MuscleGroup(val displayName: String) {
    LEGS("Legs"),
    CHEST("Chest"),
    BACK("Back"),
    CORE("Core"),
    ARMS("Arms");

    companion object {
        /**
         * Get all muscle group names sorted alphabetically
         */
        fun getAllSorted(): List<String> {
            return entries.map { it.displayName }.sorted()
        }

        /**
         * Find muscle group by display name
         */
        fun fromDisplayName(name: String): MuscleGroup? {
            return entries.find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}

/**
 * Popular exercises pre-filled for each muscle group
 */
object PopularExercises {

    /**
     * Get all popular exercises (4 per muscle group)
     */
    fun getAll(): List<Exercise> {
        return listOf(
            // Legs
            Exercise(name = "Barbell Squat", bodyPart = MuscleGroup.LEGS.displayName),
            Exercise(name = "Leg Press", bodyPart = MuscleGroup.LEGS.displayName),
            Exercise(name = "Lunges", bodyPart = MuscleGroup.LEGS.displayName),
            Exercise(name = "Romanian Deadlift", bodyPart = MuscleGroup.LEGS.displayName),

            // Chest
            Exercise(name = "Barbell Bench Press", bodyPart = MuscleGroup.CHEST.displayName),
            Exercise(name = "Dumbbell Incline Press", bodyPart = MuscleGroup.CHEST.displayName),
            Exercise(name = "Chest Dips", bodyPart = MuscleGroup.CHEST.displayName),
            Exercise(name = "Chest Fly", bodyPart = MuscleGroup.CHEST.displayName),

            // Back
            Exercise(name = "Pull-ups", bodyPart = MuscleGroup.BACK.displayName),
            Exercise(name = "Barbell Row", bodyPart = MuscleGroup.BACK.displayName),
            Exercise(name = "Deadlift", bodyPart = MuscleGroup.BACK.displayName),
            Exercise(name = "Lat Pulldown", bodyPart = MuscleGroup.BACK.displayName),

            // Core
            Exercise(name = "Plank", bodyPart = MuscleGroup.CORE.displayName),
            Exercise(name = "Crunches", bodyPart = MuscleGroup.CORE.displayName),
            Exercise(name = "Russian Twist", bodyPart = MuscleGroup.CORE.displayName),
            Exercise(name = "Leg Raises", bodyPart = MuscleGroup.CORE.displayName),

            // Arms
            Exercise(name = "Barbell Bicep Curl", bodyPart = MuscleGroup.ARMS.displayName),
            Exercise(name = "Tricep Extension", bodyPart = MuscleGroup.ARMS.displayName),
            Exercise(name = "Hammer Curl", bodyPart = MuscleGroup.ARMS.displayName),
            Exercise(name = "Tricep Dips", bodyPart = MuscleGroup.ARMS.displayName)
        )
    }

    /**
     * Get exercises grouped by muscle group, sorted alphabetically within each group
     */
    fun getGrouped(): Map<String, List<Exercise>> {
        return getAll()
            .groupBy { it.bodyPart }
            .mapValues { (_, exercises) -> exercises.sortedBy { it.name } }
            .toSortedMap()
    }
}
