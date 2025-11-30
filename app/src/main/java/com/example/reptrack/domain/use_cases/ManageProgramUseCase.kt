package com.example.reptrack.domain.use_cases

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.repositories.ProgramRepository

class ManageProgramUseCase(private val repository: ProgramRepository) {
    suspend fun createProgram(program: Program): Resource<String> {
        return repository.createProgram(program)
    }

    suspend fun getPrograms(): Resource<List<Program>> {
        return repository.getPrograms()
    }

    suspend fun getProgram(programId: String): Resource<Program> {
        return repository.getProgram(programId)
    }
    
    suspend fun addExercise(exercise: Exercise): Resource<String> {
        return repository.addExercise(exercise)
    }
    
    suspend fun getExercisesForProgram(programId: String): Resource<List<Exercise>> {
        return repository.getExercisesForProgram(programId)
    }

    suspend fun joinProgram(programId: String, userId: String): Resource<String> {
        return repository.joinProgram(programId, userId)
    }
}
