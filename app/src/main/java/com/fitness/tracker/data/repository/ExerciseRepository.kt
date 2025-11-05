package com.fitness.tracker.data.repository

import com.fitness.tracker.data.database.dao.ExerciseDao
import com.fitness.tracker.data.database.entity.Exercise
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getAllExercisesGroupedByBodyPart(): Flow<List<Exercise>> =
        exerciseDao.getAllExercisesGroupedByBodyPart()

    suspend fun getExerciseById(exerciseId: Long): Exercise? =
        exerciseDao.getExerciseById(exerciseId)

    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()

    suspend fun insertExercise(exercise: Exercise): Long =
        exerciseDao.insertExercise(exercise)

    suspend fun insertExercises(exercises: List<Exercise>) =
        exerciseDao.insertExercises(exercises)

    suspend fun updateExercise(exercise: Exercise) =
        exerciseDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: Exercise) =
        exerciseDao.deleteExercise(exercise)

    suspend fun deleteAllExercises() = exerciseDao.deleteAllExercises()
}
