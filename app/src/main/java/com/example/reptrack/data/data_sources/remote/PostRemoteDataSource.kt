package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PostRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun createPost(post: Post): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.POSTS_COLLECTION).document()
            val newPost = post.copy(id = ref.id, timestamp = Timestamp.now())
            ref.set(newPost).await()
            Resource.Success(newPost.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create post")
        }
    }

    suspend fun getPosts(limit: Int = Constants.POSTS_PAGE_SIZE, lastVisible: DocumentSnapshot? = null): Resource<Pair<List<Post>, DocumentSnapshot?>> {
        return try {
            var query: Query = firestore.collection(Constants.POSTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
            
            lastVisible?.let {
                query = query.startAfter(it)
            }
            
            val snapshot = query.get().await()
            val posts = snapshot.toObjects(Post::class.java)
            val lastDoc = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
            
            Resource.Success(Pair(posts, lastDoc))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch posts")
        }
    }

    suspend fun getPost(postId: String): Resource<Post> {
        return try {
            val doc = firestore.collection(Constants.POSTS_COLLECTION).document(postId).get().await()
            val post = doc.toObject(Post::class.java)
            if (post != null) Resource.Success(post) else Resource.Error("Post not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch post")
        }
    }
    
    suspend fun getPostsForUser(userId: String): Resource<List<Post>> {
        return try {
            android.util.Log.d("PostDataSource", "Fetching posts for user: $userId")
            val snapshot = firestore.collection(Constants.POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val posts = snapshot.toObjects(Post::class.java)
            android.util.Log.d("PostDataSource", "Found ${posts.size} posts for user $userId")
            // Sort in memory instead of using Firestore orderBy
            val sortedPosts = posts.sortedByDescending { it.timestamp.seconds }
            Resource.Success(sortedPosts)
        } catch (e: Exception) {
            android.util.Log.e("PostDataSource", "Error fetching user posts: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch user posts")
        }
    }

    suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.POSTS_COLLECTION).document(postId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }

    suspend fun updatePost(postId: String, content: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.POSTS_COLLECTION).document(postId)
                .update("content", content).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update post")
        }
    }

    suspend fun incrementLikeCount(postId: String, increment: Int): Resource<Unit> {
        return try {
            val postRef = firestore.collection(Constants.POSTS_COLLECTION).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("likeCount") ?: 0
                transaction.update(postRef, "likeCount", currentCount + increment)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update like count")
        }
    }

    suspend fun incrementCommentCount(postId: String, increment: Int): Resource<Unit> {
        return try {
            val postRef = firestore.collection(Constants.POSTS_COLLECTION).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("commentCount") ?: 0
                transaction.update(postRef, "commentCount", currentCount + increment)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update comment count")
        }
    }
}
