package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Like
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LikeRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun likePost(postId: String, userId: String): Resource<Unit> {
        return try {
            val likeId = "${postId}_$userId"
            val like = Like(id = likeId, postId = postId, userId = userId, timestamp = Timestamp.now())
            firestore.collection(Constants.LIKES_COLLECTION).document(likeId).set(like).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like post")
        }
    }

    suspend fun unlikePost(postId: String, userId: String): Resource<Unit> {
        return try {
            val likeId = "${postId}_$userId"
            firestore.collection(Constants.LIKES_COLLECTION).document(likeId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike post")
        }
    }

    suspend fun hasUserLiked(postId: String, userId: String): Resource<Boolean> {
        return try {
            val likeId = "${postId}_$userId"
            val doc = firestore.collection(Constants.LIKES_COLLECTION).document(likeId).get().await()
            Resource.Success(doc.exists())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check like status")
        }
    }

    suspend fun getLikeCount(postId: String): Resource<Int> {
        return try {
            val snapshot = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("postId", postId)
                .get().await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get like count")
        }
    }
}
