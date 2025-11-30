package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseRemoteDataSource(private val firestore: FirebaseFirestore) {

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

    suspend fun getAllExercises(): Resource<List<Exercise>> {
        return try {
            val snapshot = firestore.collection(Constants.EXERCISES_COLLECTION)
                .get()
                .await()
            val exercises = snapshot.toObjects(Exercise::class.java)
            Resource.Success(exercises)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch exercises")
        }
    }
    suspend fun getExercisesForProgram(programId: String): Resource<List<Exercise>> {
        return try {
            val snapshot = firestore.collection(Constants.EXERCISES_COLLECTION)
                .whereEqualTo("programId", programId)
                .get()
                .await()
            val exercises = snapshot.toObjects(Exercise::class.java)
            Resource.Success(exercises)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch exercises")
        }
    }
}
