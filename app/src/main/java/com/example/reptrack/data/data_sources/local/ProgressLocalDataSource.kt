package com.example.reptrack.data.data_sources.local

import android.content.Context
import com.example.reptrack.domain.models.ProgressLog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProgressLocalDataSource(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, "reptrack_progress.json")

    fun saveProgressLogs(logs: List<ProgressLog>) {
        val jsonString = json.encodeToString(logs)
        file.writeText(jsonString)
    }

    fun getProgressLogs(): List<ProgressLog> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveProgressLog(log: ProgressLog) {
        val logs = getProgressLogs().toMutableList()
        val index = logs.indexOfFirst { it.id == log.id }
        if (index != -1) {
            logs[index] = log
        } else {
            logs.add(log)
        }
        saveProgressLogs(logs)
    }
    
    fun getProgressForUser(userId: String): List<ProgressLog> {
        return getProgressLogs().filter { it.userId == userId }
    }
    
    fun getProgressForExercise(userId: String, exerciseId: String): List<ProgressLog> {
        return getProgressLogs().filter { it.userId == userId && it.exerciseId == exerciseId }
    }
}
