package com.fitness.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fitness.tracker.data.database.dao.ExerciseDao
import com.fitness.tracker.data.database.dao.LogDao
import com.fitness.tracker.data.database.dao.UserDao
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.database.entity.Log
import com.fitness.tracker.data.database.entity.User

@Database(
    entities = [User::class, Exercise::class, Log::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
