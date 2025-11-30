package com.example.reptrack.data.data_sources.local

import android.content.Context
import com.example.reptrack.common.Constants
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.models.ProgressLog
import com.example.reptrack.domain.models.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class LocalData(
    val users: List<User> = emptyList(),
    val programs: List<Program> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val progressLogs: List<ProgressLog> = emptyList()
)

class LocalDataSource(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, Constants.LOCAL_DATA_FILE)

    fun saveData(data: LocalData) {
        val jsonString = json.encodeToString(data)
        file.writeText(jsonString)
    }

    fun loadData(): LocalData {
        if (!file.exists()) return LocalData()
        return try {
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            LocalData()
        }
    }
    
    // Helper methods to update specific parts could be added here, 
    // but for simplicity we might just read/modify/write in the repository.
}
