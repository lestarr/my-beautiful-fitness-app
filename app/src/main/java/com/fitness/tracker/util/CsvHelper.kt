package com.fitness.tracker.util

import android.content.Context
import android.net.Uri
import com.fitness.tracker.data.database.entity.Exercise
import com.fitness.tracker.data.database.entity.LogWithExercise
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object CsvHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun parseExercisesFromCsv(context: Context, uri: Uri): List<Exercise> {
        val exercises = mutableListOf<Exercise>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                reader.readNext() // Skip header if present

                var line: Array<String>?
                while (reader.readNext().also { line = it } != null) {
                    line?.let {
                        if (it.size >= 2) {
                            exercises.add(
                                Exercise(
                                    name = it[0].trim(),
                                    bodyPart = it[1].trim()
                                )
                            )
                        }
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return exercises
    }

    fun exportExercisesToCsv(context: Context, uri: Uri, exercises: List<Exercise>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = CSVWriter(OutputStreamWriter(outputStream))

                // Write header
                writer.writeNext(arrayOf("Exercise Name", "Body Part"))

                // Write data
                exercises.forEach { exercise ->
                    writer.writeNext(arrayOf(exercise.name, exercise.bodyPart))
                }

                writer.close()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportLogsToFile(context: Context, logs: List<LogWithExercise>, userName: String): File? {
        return try {
            val fileName = "fitness_logs_${userName}_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)

            val writer = CSVWriter(FileWriter(file))

            // Write header
            writer.writeNext(arrayOf("Date", "Exercise", "Body Part", "Weight (kg)", "Reps"))

            // Write data
            logs.forEach { logWithExercise ->
                writer.writeNext(arrayOf(
                    dateFormat.format(Date(logWithExercise.log.date)),
                    logWithExercise.exercise.name,
                    logWithExercise.exercise.bodyPart,
                    logWithExercise.log.weight.toString(),
                    logWithExercise.log.reps.toString()
                ))
            }

            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportLogsToCsv(context: Context, uri: Uri, logs: List<LogWithExercise>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = CSVWriter(OutputStreamWriter(outputStream))

                // Write header
                writer.writeNext(arrayOf("Date", "Exercise", "Body Part", "Weight (kg)", "Reps"))

                // Write data
                logs.forEach { logWithExercise ->
                    writer.writeNext(arrayOf(
                        dateFormat.format(Date(logWithExercise.log.date)),
                        logWithExercise.exercise.name,
                        logWithExercise.exercise.bodyPart,
                        logWithExercise.log.weight.toString(),
                        logWithExercise.log.reps.toString()
                    ))
                }

                writer.close()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
