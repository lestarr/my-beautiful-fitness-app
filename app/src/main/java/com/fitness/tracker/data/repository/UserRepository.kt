package com.fitness.tracker.data.repository

import com.fitness.tracker.data.database.dao.UserDao
import com.fitness.tracker.data.database.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(userId: Long): User? = userDao.getUserById(userId)

    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    suspend fun getUserCount(): Int = userDao.getUserCount()
}
