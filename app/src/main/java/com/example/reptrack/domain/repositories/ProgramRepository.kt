package com.example.reptrack.domain.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program

interface ProgramRepository {
    suspend fun createProgram(program: Program): Resource<String>
    suspend fun updateProgram(program: Program): Resource<Unit>
    suspend fun deleteProgram(programId: String): Resource<Unit>
    suspend fun getPrograms(): Resource<List<Program>>
    suspend fun getProgram(programId: String): Resource<Program>
    
    suspend fun addExercise(exercise: Exercise): Resource<String>
    suspend fun getExercisesForProgram(programId: String): Resource<List<Exercise>>
    suspend fun joinProgram(programId: String, userId: String): Resource<String>
    suspend fun deleteExercise(exerciseId: String): Resource<Unit>
}
