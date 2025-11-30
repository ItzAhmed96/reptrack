package com.example.reptrack.domain.use_cases

import com.example.reptrack.common.Resource
import com.example.reptrack.data.repositories.SocialRepositoryImpl
import com.example.reptrack.domain.models.Comment
import com.example.reptrack.domain.models.Post
import com.google.firebase.firestore.DocumentSnapshot

class SocialFeedUseCase(
    private val repository: SocialRepositoryImpl
) {
    suspend fun createPost(post: Post): Resource<String> = repository.createPost(post)
    
    suspend fun getPosts(limit: Int = 20, lastVisible: DocumentSnapshot? = null): Resource<Pair<List<Post>, DocumentSnapshot?>> =
        repository.getPosts(limit, lastVisible)
    
    suspend fun getPost(postId: String): Resource<Post> = repository.getPost(postId)
    
    suspend fun deletePost(postId: String): Resource<Unit> = repository.deletePost(postId)
    
    suspend fun updatePost(postId: String, content: String): Resource<Unit> = repository.updatePost(postId, content)
    
    suspend fun likePost(postId: String, userId: String): Resource<Unit> = repository.likePost(postId, userId)
    
    suspend fun unlikePost(postId: String, userId: String): Resource<Unit> = repository.unlikePost(postId, userId)
    
    suspend fun hasUserLiked(postId: String, userId: String): Resource<Boolean> = repository.hasUserLiked(postId, userId)
    
    suspend fun addComment(comment: Comment): Resource<String> = repository.addComment(comment)
    
    suspend fun getCommentsForPost(postId: String): Resource<List<Comment>> = repository.getCommentsForPost(postId)
    
    suspend fun deleteComment(commentId: String, postId: String): Resource<Unit> = repository.deleteComment(commentId, postId)

    suspend fun followUser(followerId: String, followedId: String): Resource<Unit> = repository.followUser(followerId, followedId)
    
    suspend fun unfollowUser(followerId: String, followedId: String): Resource<Unit> = repository.unfollowUser(followerId, followedId)
    
    suspend fun isFollowing(followerId: String, followedId: String): Resource<Boolean> = repository.isFollowing(followerId, followedId)
    
    suspend fun getFollowing(userId: String): Resource<List<String>> = repository.getFollowing(userId)

    suspend fun getFollowersCount(userId: String): Resource<Int> = repository.getFollowersCount(userId)

    suspend fun getFollowingCount(userId: String): Resource<Int> = repository.getFollowingCount(userId)
    
    suspend fun getPostsForUser(userId: String): Resource<List<Post>> = repository.getPostsForUser(userId)
}
