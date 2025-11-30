package com.example.reptrack.data.data_sources.local

import android.content.Context
import com.example.reptrack.domain.models.Exercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ExerciseLocalDataSource(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, "reptrack_exercises.json")

    fun saveExercises(exercises: List<Exercise>) {
        val jsonString = json.encodeToString(exercises)
        file.writeText(jsonString)
    }

    fun getExercises(): List<Exercise> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveExercise(exercise: Exercise) {
        val exercises = getExercises().toMutableList()
        val index = exercises.indexOfFirst { it.id == exercise.id }
        if (index != -1) {
            exercises[index] = exercise
        } else {
            exercises.add(exercise)
        }
        saveExercises(exercises)
    }
    
    fun getExercisesForProgram(programId: String): List<Exercise> {
        return getExercises().filter { it.programId == programId }
    }
}
