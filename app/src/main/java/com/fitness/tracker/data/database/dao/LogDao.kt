package com.fitness.tracker.data.database.dao

import androidx.room.*
import com.fitness.tracker.data.database.entity.Log
import com.fitness.tracker.data.database.entity.LogWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllLogsForUser(userId: Long): Flow<List<Log>>

    @Transaction
    @Query("SELECT * FROM logs WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY date DESC LIMIT 3")
    fun getLastThreeLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>>

    @Transaction
    @Query("SELECT * FROM logs WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY date DESC LIMIT :limit")
    fun getLastNLogsForExercise(userId: Long, exerciseId: Long, limit: Int): Flow<List<LogWithExercise>>

    @Transaction
    @Query("SELECT * FROM logs WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY date DESC")
    fun getAllLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>>

    @Query("SELECT * FROM logs WHERE id = :logId")
    suspend fun getLogById(logId: Long): Log?

    @Query("SELECT MAX(weight) FROM logs WHERE userId = :userId AND exerciseId = :exerciseId")
    suspend fun getPersonalRecordWeight(userId: Long, exerciseId: Long): Double?

    @Query("SELECT MAX(reps) FROM logs WHERE userId = :userId AND exerciseId = :exerciseId AND weight = :weight")
    suspend fun getPersonalRecordReps(userId: Long, exerciseId: Long, weight: Double): Int?

    @Insert
    suspend fun insertLog(log: Log): Long

    @Update
    suspend fun updateLog(log: Log)

    @Delete
    suspend fun deleteLog(log: Log)

    @Query("DELETE FROM logs WHERE userId = :userId")
    suspend fun deleteAllLogsForUser(userId: Long)

    @Transaction
    @Query("SELECT * FROM logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllLogsWithExerciseForUser(userId: Long): Flow<List<LogWithExercise>>
}
