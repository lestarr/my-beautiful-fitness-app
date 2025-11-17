package com.fitness.tracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.tracker.data.database.AppDatabase
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.repository.ExerciseRepository
import com.fitness.tracker.util.PopularExercises
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val exerciseRepository = ExerciseRepository(database.exerciseDao())

    val allExercises = exerciseRepository.getAllExercises()
    val allExercisesGroupedByBodyPart = exerciseRepository.getAllExercisesGroupedByBodyPart()

    private val _exerciseCount = MutableStateFlow(0)
    val exerciseCount: StateFlow<Int> = _exerciseCount.asStateFlow()

    init {
        loadExerciseCount()
        preFillPopularExercises()
    }

    private fun loadExerciseCount() {
        viewModelScope.launch {
            _exerciseCount.value = exerciseRepository.getExerciseCount()
        }
    }

    /**
     * Pre-fill database with popular exercises if it's empty
     */
    private fun preFillPopularExercises() {
        viewModelScope.launch {
            val count = exerciseRepository.getExerciseCount()
            if (count == 0) {
                exerciseRepository.insertExercises(PopularExercises.getAll())
                loadExerciseCount()
            }
        }
    }

    fun createExercise(name: String, bodyPart: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val exercise = Exercise(name = name, bodyPart = bodyPart)
            exerciseRepository.insertExercise(exercise)
            loadExerciseCount()
            onSuccess()
        }
    }

    fun updateExercise(exercise: Exercise, onSuccess: () -> Unit) {
        viewModelScope.launch {
            exerciseRepository.updateExercise(exercise)
            onSuccess()
        }
    }

    fun deleteExercise(exercise: Exercise, onSuccess: () -> Unit) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exercise)
            loadExerciseCount()
            onSuccess()
        }
    }

    fun importExercises(exercises: List<Exercise>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            exerciseRepository.insertExercises(exercises)
            loadExerciseCount()
            onSuccess()
        }
    }
}
