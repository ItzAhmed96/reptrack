package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.WorkoutPlan
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutPlanRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION).document()
            val newPlan = workoutPlan.copy(id = ref.id)
            ref.set(newPlan).await()
            Resource.Success(newPlan.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create workout plan")
        }
    }

    suspend fun getWorkoutPlansForUser(userId: String): Resource<List<WorkoutPlan>> {
        return try {
            // Get plans where user is creator (check both creatorId and userId for backward compatibility)
            val creatorSnapshot1 = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .whereEqualTo("creatorId", userId)
                .get().await()
            
            val creatorSnapshot2 = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get().await()
            
            val createdPlans = (creatorSnapshot1.toObjects(WorkoutPlan::class.java) + 
                               creatorSnapshot2.toObjects(WorkoutPlan::class.java)).distinctBy { it.id }
            
            // Also get plans user has joined
            val joinedSnapshot = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .whereArrayContains("joinedUserIds", userId)
                .get().await()
            
            val joinedPlans = joinedSnapshot.toObjects(WorkoutPlan::class.java)
            
            val allPlans = (createdPlans + joinedPlans).distinctBy { it.id }
            android.util.Log.d("WorkoutPlanDS", "Found ${createdPlans.size} created and ${joinedPlans.size} joined workouts")
            Resource.Success(allPlans)
        } catch (e: Exception) {
            android.util.Log.e("WorkoutPlanDS", "Error fetching workouts: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch workout plans")
        }
    }

    suspend fun getAllWorkoutPlans(): Resource<List<WorkoutPlan>> {
        return try {
            val snapshot = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .get().await()
            val plans = snapshot.toObjects(WorkoutPlan::class.java)
            Resource.Success(plans)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch workout plans")
        }
    }

    suspend fun joinWorkoutPlan(workoutId: String, userId: String): Resource<Unit> {
        return try {
            android.util.Log.d("WorkoutPlanDS", "Joining workout $workoutId for user $userId")
            firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .document(workoutId)
                .update("joinedUserIds", FieldValue.arrayUnion(userId))
                .await()
            android.util.Log.d("WorkoutPlanDS", "Successfully joined workout!")
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WorkoutPlanDS", "Failed to join workout: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to join workout plan")
        }
    }

    suspend fun unjoinWorkoutPlan(workoutId: String, userId: String): Resource<Unit> {
        return try {
            android.util.Log.d("WorkoutPlanDS", "Unjoining workout $workoutId for user $userId")
            firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .document(workoutId)
                .update("joinedUserIds", FieldValue.arrayRemove(userId))
                .await()
            android.util.Log.d("WorkoutPlanDS", "Successfully unjoined workout!")
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WorkoutPlanDS", "Failed to unjoin workout: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to leave workout plan")
        }
    }

    suspend fun getWorkoutPlanById(workoutId: String): Resource<WorkoutPlan> {
        return try {
            val snapshot = firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .document(workoutId)
                .get()
                .await()
            
            if (snapshot.exists()) {
                val plan = snapshot.toObject(WorkoutPlan::class.java) 
                    ?: return Resource.Error("Failed to parse workout plan")
                Resource.Success(plan)
            } else {
                Resource.Error("Workout plan not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load workout plan")
        }
    }

    suspend fun deleteWorkoutPlan(workoutId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.WORKOUT_PLANS_COLLECTION)
                .document(workoutId)
                .delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete workout plan")
        }
    }
}
