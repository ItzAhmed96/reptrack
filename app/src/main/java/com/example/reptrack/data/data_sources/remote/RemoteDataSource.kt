package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.models.ProgressLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    // Programs
    suspend fun createProgram(program: Program): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.PROGRAMS_COLLECTION).document()
            val newProgram = program.copy(id = ref.id)
            ref.set(newProgram).await()
            Resource.Success(newProgram.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create program")
        }
    }

    suspend fun getPrograms(): Resource<List<Program>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRAMS_COLLECTION).get().await()
            val programs = snapshot.toObjects(Program::class.java)
            Resource.Success(programs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch programs")
        }
    }
    
    suspend fun getProgram(programId: String): Resource<Program> {
        return try {
            val doc = firestore.collection(Constants.PROGRAMS_COLLECTION).document(programId).get().await()
            val program = doc.toObject(Program::class.java)
            if (program != null) Resource.Success(program) else Resource.Error("Program not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch program")
        }
    }
    
    // Exercises
    suspend fun addExercise(exercise: Exercise): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.EXERCISES_COLLECTION).document()
            val newExercise = exercise.copy(id = ref.id)
            ref.set(newExercise).await()
            Resource.Success(newExercise.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add exercise")
        }
    }
    
    suspend fun getExercisesForProgram(programId: String): Resource<List<Exercise>> {
        return try {
            val snapshot = firestore.collection(Constants.EXERCISES_COLLECTION)
                .whereEqualTo("programId", programId)
                .get().await()
            val exercises = snapshot.toObjects(Exercise::class.java)
            Resource.Success(exercises)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch exercises")
        }
    }
    
    // Progress
    suspend fun logProgress(log: ProgressLog): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.PROGRESS_LOGS_COLLECTION).document()
            val newLog = log.copy(id = ref.id)
            ref.set(newLog).await()
            Resource.Success(newLog.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log progress")
        }
    }
    
    suspend fun getProgressForUser(userId: String): Resource<List<ProgressLog>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRESS_LOGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get().await()
            val logs = snapshot.toObjects(ProgressLog::class.java)
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch progress")
        }
    }
    
    suspend fun getProgressForExercise(userId: String, exerciseId: String): Resource<List<ProgressLog>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRESS_LOGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("exerciseId", exerciseId)
                .get().await()
            val logs = snapshot.toObjects(ProgressLog::class.java)
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch progress for exercise")
        }
    }
}
