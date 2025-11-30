package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.ProgressLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgressRemoteDataSource(private val firestore: FirebaseFirestore) {

    suspend fun logProgress(log: ProgressLog): Resource<String> {
        return try {
            firestore.collection(Constants.PROGRESS_COLLECTION)
                .document(log.id)
                .set(log)
                .await()
            Resource.Success(log.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log progress")
        }
    }

    suspend fun getProgressForUser(userId: String): Resource<List<ProgressLog>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRESS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val logs = snapshot.toObjects(ProgressLog::class.java)
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch progress")
        }
    }
    
    suspend fun getProgressForExercise(userId: String, exerciseId: String): Resource<List<ProgressLog>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRESS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .await()
            val logs = snapshot.toObjects(ProgressLog::class.java)
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch progress for exercise")
        }
    }
}
