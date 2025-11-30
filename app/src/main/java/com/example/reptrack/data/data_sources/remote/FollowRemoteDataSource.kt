package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun followUser(followerId: String, followedId: String): Resource<Unit> {
        return try {
            val followId = "${followerId}_$followedId"
            val data = hashMapOf(
                "id" to followId,
                "followerId" to followerId,
                "followedId" to followedId,
                "timestamp" to Timestamp.now()
            )
            firestore.collection("follows").document(followId).set(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to follow user")
        }
    }

    suspend fun unfollowUser(followerId: String, followedId: String): Resource<Unit> {
        return try {
            val followId = "${followerId}_$followedId"
            firestore.collection("follows").document(followId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unfollow user")
        }
    }

    suspend fun isFollowing(followerId: String, followedId: String): Resource<Boolean> {
        return try {
            val followId = "${followerId}_$followedId"
            val doc = firestore.collection("follows").document(followId).get().await()
            Resource.Success(doc.exists())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check follow status")
        }
    }

    suspend fun getFollowing(userId: String): Resource<List<String>> {
        return try {
            val snapshot = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get().await()
            val followedIds = snapshot.documents.mapNotNull { it.getString("followedId") }
            Resource.Success(followedIds)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get following list")
        }
    }
    
    suspend fun getFollowersCount(userId: String): Resource<Int> {
        return try {
            val snapshot = firestore.collection("follows")
                .whereEqualTo("followedId", userId)
                .get().await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
             Resource.Error(e.message ?: "Failed to get followers count")
        }
    }

    suspend fun getFollowingCount(userId: String): Resource<Int> {
        return try {
            val snapshot = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get().await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get following count")
        }
    }
}
