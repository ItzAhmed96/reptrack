package com.example.reptrack.data.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.data.data_sources.local.ExerciseLocalDataSource
import com.example.reptrack.data.data_sources.local.ProgramLocalDataSource
import com.example.reptrack.data.data_sources.remote.ExerciseRemoteDataSource
import com.example.reptrack.data.data_sources.remote.ProgramRemoteDataSource
import com.example.reptrack.data.data_sources.remote.WorkoutPlanRemoteDataSource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.models.WorkoutDay
import com.example.reptrack.domain.models.WorkoutPlan
import com.example.reptrack.domain.repositories.ProgramRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgramRepositoryImpl(
    private val programRemoteDataSource: ProgramRemoteDataSource,
    private val exerciseRemoteDataSource: ExerciseRemoteDataSource,
    private val workoutPlanRemoteDataSource: WorkoutPlanRemoteDataSource,
    private val programLocalDataSource: ProgramLocalDataSource,
    private val exerciseLocalDataSource: ExerciseLocalDataSource
) : ProgramRepository {

    override suspend fun createProgram(program: Program): Resource<String> {
        // Save to remote
        val result = programRemoteDataSource.createProgram(program)
        if (result is Resource.Success) {
            // Save to local
            withContext(Dispatchers.IO) {
                programLocalDataSource.saveProgram(program)
            }
        }
        return result
    }

    override suspend fun getPrograms(): Resource<List<Program>> {
        // Try remote first
        val remoteResult = programRemoteDataSource.getPrograms()
        if (remoteResult is Resource.Success) {
            val programs = remoteResult.data ?: emptyList()
            withContext(Dispatchers.IO) {
                programLocalDataSource.savePrograms(programs)
            }
            return Resource.Success(programs)
        }
        
        // Fallback to local
        val localPrograms = withContext(Dispatchers.IO) {
            programLocalDataSource.getPrograms()
        }
        if (localPrograms.isNotEmpty()) {
            return Resource.Success(localPrograms)
        }
        
        return remoteResult
    }

    override suspend fun getProgram(programId: String): Resource<Program> {
        val remoteResult = programRemoteDataSource.getProgram(programId)
        if (remoteResult is Resource.Success) {
            val program = remoteResult.data
            if (program != null) {
                withContext(Dispatchers.IO) {
                    programLocalDataSource.saveProgram(program)
                }
                return Resource.Success(program)
            }
        }
        
        val localProgram = withContext(Dispatchers.IO) {
            programLocalDataSource.getProgram(programId)
        }
        if (localProgram != null) {
            return Resource.Success(localProgram)
        }
        
        return Resource.Error("Program not found")
    }

    override suspend fun addExercise(exercise: Exercise): Resource<String> {
        val result = exerciseRemoteDataSource.addExercise(exercise)
        if (result is Resource.Success) {
            withContext(Dispatchers.IO) {
                exerciseLocalDataSource.saveExercise(exercise)
            }
        }
        return result
    }

    override suspend fun getExercisesForProgram(programId: String): Resource<List<Exercise>> {
        val remoteResult = exerciseRemoteDataSource.getExercisesForProgram(programId)
        if (remoteResult is Resource.Success) {
            val exercises = remoteResult.data ?: emptyList()
            withContext(Dispatchers.IO) {
                // We might want to be careful not to overwrite all exercises if we only fetched some,
                // but for simplicity let's save what we got.
                // Ideally we merge or replace specific ones.
                // For now, let's just save them individually to update the list.
                exercises.forEach { exerciseLocalDataSource.saveExercise(it) }
            }
            return Resource.Success(exercises)
        }
        
        val localExercises = withContext(Dispatchers.IO) {
            exerciseLocalDataSource.getExercisesForProgram(programId)
        }
        if (localExercises.isNotEmpty()) {
            return Resource.Success(localExercises)
        }
        
        return remoteResult
    }

    override suspend fun deleteProgram(programId: String): Resource<Unit> {
        // TODO: Implement delete
        return Resource.Success(Unit)
    }

    override suspend fun updateProgram(program: Program): Resource<Unit> {
        // TODO: Implement update
        return Resource.Success(Unit)
    }

    override suspend fun deleteExercise(exerciseId: String): Resource<Unit> {
        // TODO: Implement delete
        return Resource.Success(Unit)
    }

    override suspend fun joinProgram(programId: String, userId: String): Resource<String> {
        val programResult = getProgram(programId)
        if (programResult is Resource.Error) {
            return Resource.Error(programResult.message ?: "Failed to find program")
        }
        val program = programResult.data!!

        val exercisesResult = getExercisesForProgram(programId)
        val exercises = if (exercisesResult is Resource.Success) exercisesResult.data ?: emptyList() else emptyList()

        // Create a basic workout plan structure from the program
        val workoutDay = WorkoutDay(
            name = "Day 1",
            exerciseIds = exercises.map { it.id }
        )

        val workoutPlan = WorkoutPlan(
            userId = userId,
            name = program.name,
            description = program.description,
            daysPerWeek = 1, // Default
            days = listOf(workoutDay)
        )

        return workoutPlanRemoteDataSource.createWorkoutPlan(workoutPlan)
    }
}
