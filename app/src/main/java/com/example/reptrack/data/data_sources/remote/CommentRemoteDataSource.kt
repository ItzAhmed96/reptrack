package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class CommentRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun addComment(comment: Comment): Resource<String> {
        return try {
            android.util.Log.d("CommentDataSource", "Attempting to add comment to Firestore")
            val ref = firestore.collection(Constants.COMMENTS_COLLECTION).document()
            val newComment = comment.copy(id = ref.id, timestamp = Timestamp.now())
            
            android.util.Log.d("CommentDataSource", "Comment ID: ${newComment.id}, PostID: ${newComment.postId}")
            ref.set(newComment).await()
            
            android.util.Log.d("CommentDataSource", "Comment added successfully to Firestore!")
            Resource.Success(newComment.id)
        } catch (e: Exception) {
            android.util.Log.e("CommentDataSource", "Failed to add comment: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    suspend fun getCommentsForPost(postId: String): Resource<List<Comment>> {
        return try {
            val snapshot = firestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("postId", postId)
                .get().await()
            
            val comments = snapshot.toObjects(Comment::class.java)
            // Sort in memory instead of using Firestore orderBy
            val sortedComments = comments.sortedBy { it.timestamp.seconds }
            
            android.util.Log.d("CommentDataSource", "Fetched ${sortedComments.size} comments for post $postId")
            Resource.Success(sortedComments)
        } catch (e: Exception) {
            android.util.Log.e("CommentDataSource", "Error fetching comments: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch comments")
        }
    }

    suspend fun deleteComment(commentId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.COMMENTS_COLLECTION).document(commentId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }
}
