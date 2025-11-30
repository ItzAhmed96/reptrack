package com.example.reptrack.data.data_sources.local

import android.content.Context
import com.example.reptrack.domain.models.Program
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProgramLocalDataSource(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file = File(context.filesDir, "reptrack_programs.json")

    fun savePrograms(programs: List<Program>) {
        val jsonString = json.encodeToString(programs)
        file.writeText(jsonString)
    }

    fun getPrograms(): List<Program> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveProgram(program: Program) {
        val programs = getPrograms().toMutableList()
        val index = programs.indexOfFirst { it.id == program.id }
        if (index != -1) {
            programs[index] = program
        } else {
            programs.add(program)
        }
        savePrograms(programs)
    }
    
    fun getProgram(programId: String): Program? {
        return getPrograms().find { it.id == programId }
    }
}
