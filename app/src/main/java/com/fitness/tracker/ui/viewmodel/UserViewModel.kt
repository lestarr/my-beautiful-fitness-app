package com.fitness.tracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.tracker.data.database.AppDatabase
import com.fitness.tracker.data.database.entity.User
import com.fitness.tracker.data.repository.LogRepository
import com.fitness.tracker.data.repository.UserRepository
import com.fitness.tracker.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val logRepository = LogRepository(database.logDao())
    private val preferencesManager = PreferencesManager(application)

    val allUsers = userRepository.getAllUsers()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = preferencesManager.currentUserId
            if (userId != -1L) {
                _currentUser.value = userRepository.getUserById(userId)
            }
        }
    }

    fun createUser(name: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            val user = User(name = name)
            val id = userRepository.insertUser(user)
            val newUser = user.copy(id = id)

            // If no user is selected, select this one
            if (_currentUser.value == null) {
                setCurrentUser(newUser)
            }
            onSuccess(newUser)
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        preferencesManager.currentUserId = user.id
    }

    fun deleteUser(user: User, onLogsExported: () -> Unit) {
        viewModelScope.launch {
            // Export logs handled by caller
            userRepository.deleteUser(user)

            if (_currentUser.value?.id == user.id) {
                _currentUser.value = null
                preferencesManager.currentUserId = -1L
            }

            onLogsExported()
        }
    }
}
