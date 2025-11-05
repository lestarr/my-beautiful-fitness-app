package com.fitness.tracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.tracker.data.database.AppDatabase
import com.fitness.tracker.data.database.entity.Log
import com.fitness.tracker.data.database.entity.LogWithExercise
import com.fitness.tracker.data.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val logRepository = LogRepository(database.logDao())

    private val _personalRecordWeight = MutableStateFlow<Double?>(null)
    val personalRecordWeight: StateFlow<Double?> = _personalRecordWeight.asStateFlow()

    fun getLastThreeLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>> {
        return logRepository.getLastThreeLogsForExercise(userId, exerciseId)
    }

    fun getAllLogsForExercise(userId: Long, exerciseId: Long): Flow<List<LogWithExercise>> {
        return logRepository.getAllLogsForExercise(userId, exerciseId)
    }

    fun getAllLogsWithExerciseForUser(userId: Long): Flow<List<LogWithExercise>> {
        return logRepository.getAllLogsWithExerciseForUser(userId)
    }

    fun loadPersonalRecord(userId: Long, exerciseId: Long) {
        viewModelScope.launch {
            _personalRecordWeight.value = logRepository.getPersonalRecordWeight(userId, exerciseId)
        }
    }

    fun createLog(
        userId: Long,
        exerciseId: Long,
        weight: Double,
        reps: Int,
        onSuccess: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val log = Log(
                userId = userId,
                exerciseId = exerciseId,
                weight = weight,
                reps = reps
            )
            logRepository.insertLog(log)

            // Check if it's a new PR
            val currentPR = logRepository.getPersonalRecordWeight(userId, exerciseId)
            val isNewPR = currentPR != null && weight > currentPR

            loadPersonalRecord(userId, exerciseId)
            onSuccess(isNewPR)
        }
    }

    fun updateLog(log: Log, onSuccess: () -> Unit) {
        viewModelScope.launch {
            logRepository.updateLog(log)
            onSuccess()
        }
    }

    fun deleteLog(log: Log, onSuccess: () -> Unit) {
        viewModelScope.launch {
            logRepository.deleteLog(log)
            onSuccess()
        }
    }
}
