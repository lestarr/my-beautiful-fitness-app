package com.fitness.tracker.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LogWithExercise(
    @Embedded val log: Log,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: Exercise
)
