package com.fitness.tracker.data.repository

import com.fitness.tracker.data.database.dao.LogDao
import com.fitness.tracker.data.database.entity.Log
import com.fitness.tracker.data.database.entity.LogWithExercise
import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    fun getAllLogsForUser(userId: Long): Flow<List<Log>> =
        logDao.getAllLogsForUser(userId)

    fun getLastThreeLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>> =
        logDao.getLastThreeLogsForExercise(userId, exerciseId)

    fun getAllLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>> =
        logDao.getAllLogsForExercise(userId, exerciseId)

    fun getAllLogsWithExerciseForUser(userId: Long): Flow<List<LogWithExercise>> =
        logDao.getAllLogsWithExerciseForUser(userId)

    suspend fun getLogById(logId: Long): Log? = logDao.getLogById(logId)

    suspend fun getPersonalRecordWeight(userId: Long, exerciseId: Long): Double? =
        logDao.getPersonalRecordWeight(userId, exerciseId)

    suspend fun getPersonalRecordReps(userId: Long, exerciseId: Long, weight: Double): Int? =
        logDao.getPersonalRecordReps(userId, exerciseId, weight)

    suspend fun insertLog(log: Log): Long = logDao.insertLog(log)

    suspend fun updateLog(log: Log) = logDao.updateLog(log)

    suspend fun deleteLog(log: Log) = logDao.deleteLog(log)

    suspend fun deleteAllLogsForUser(userId: Long) =
        logDao.deleteAllLogsForUser(userId)
}
