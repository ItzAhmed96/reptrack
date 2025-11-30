package com.example.reptrack.data.data_sources.local

import android.content.Context
import com.example.reptrack.domain.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class UserLocalDataSource(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, "reptrack_users.json")

    fun saveUsers(users: List<User>) {
        val jsonString = json.encodeToString(users)
        file.writeText(jsonString)
    }

    fun getUsers(): List<User> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveUser(user: User) {
        val users = getUsers().toMutableList()
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        } else {
            users.add(user)
        }
        saveUsers(users)
    }
    
    fun getUser(userId: String): User? {
        return getUsers().find { it.id == userId }
    }
}
