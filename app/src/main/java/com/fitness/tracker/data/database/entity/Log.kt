package com.fitness.tracker.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("exerciseId"), Index("date")]
)
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val exerciseId: Long,
    val weight: Double,
    val reps: Int,
    val date: Long = System.currentTimeMillis()
)
