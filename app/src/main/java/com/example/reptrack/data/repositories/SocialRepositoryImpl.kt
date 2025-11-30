package com.example.reptrack.data.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.data.data_sources.remote.CommentRemoteDataSource
import com.example.reptrack.data.data_sources.remote.LikeRemoteDataSource
import com.example.reptrack.data.data_sources.remote.PostRemoteDataSource
import com.example.reptrack.domain.models.Comment
import com.example.reptrack.domain.models.Post
import com.example.reptrack.data.data_sources.remote.FollowRemoteDataSource
import com.google.firebase.firestore.DocumentSnapshot

class SocialRepositoryImpl(
    private val postDataSource: PostRemoteDataSource,
    private val commentDataSource: CommentRemoteDataSource,
    private val likeDataSource: LikeRemoteDataSource,
    private val followDataSource: FollowRemoteDataSource
) {
    // Posts
    suspend fun createPost(post: Post): Resource<String> = postDataSource.createPost(post)
    
    suspend fun getPosts(limit: Int, lastVisible: DocumentSnapshot? = null): Resource<Pair<List<Post>, DocumentSnapshot?>> =
        postDataSource.getPosts(limit, lastVisible)
    
    suspend fun getPost(postId: String): Resource<Post> = postDataSource.getPost(postId)
    
    suspend fun deletePost(postId: String): Resource<Unit> = postDataSource.deletePost(postId)
    
    suspend fun updatePost(postId: String, content: String): Resource<Unit> = postDataSource.updatePost(postId, content)
    
    // Comments
    suspend fun addComment(comment: Comment): Resource<String> {
        val result = commentDataSource.addComment(comment)
        if (result is Resource.Success) {
            // Increment comment count on post
            postDataSource.incrementCommentCount(comment.postId, 1)
        }
        return result
    }
    
    suspend fun getCommentsForPost(postId: String): Resource<List<Comment>> =
        commentDataSource.getCommentsForPost(postId)
    
    suspend fun deleteComment(commentId: String, postId: String): Resource<Unit> {
        val result = commentDataSource.deleteComment(commentId)
        if (result is Resource.Success) {
            // Decrement comment count on post
            postDataSource.incrementCommentCount(postId, -1)
        }
        return result
    }
    
    // Likes
    suspend fun likePost(postId: String, userId: String): Resource<Unit> {
        val result = likeDataSource.likePost(postId, userId)
        if (result is Resource.Success) {
            // Increment like count on post
            postDataSource.incrementLikeCount(postId, 1)
        }
        return result
    }
    
    suspend fun unlikePost(postId: String, userId: String): Resource<Unit> {
        val result = likeDataSource.unlikePost(postId, userId)
        if (result is Resource.Success) {
            // Decrement like count on post
            postDataSource.incrementLikeCount(postId, -1)
        }
        return result
    }
    
    suspend fun hasUserLiked(postId: String, userId: String): Resource<Boolean> =
        likeDataSource.hasUserLiked(postId, userId)
    
    suspend fun getLikeCount(postId: String): Resource<Int> =
        likeDataSource.getLikeCount(postId)

    // Follows
    suspend fun followUser(followerId: String, followedId: String): Resource<Unit> =
        followDataSource.followUser(followerId, followedId)
        
    suspend fun unfollowUser(followerId: String, followedId: String): Resource<Unit> =
        followDataSource.unfollowUser(followerId, followedId)
        
    suspend fun isFollowing(followerId: String, followedId: String): Resource<Boolean> =
        followDataSource.isFollowing(followerId, followedId)
        
    suspend fun getFollowing(userId: String): Resource<List<String>> =
        followDataSource.getFollowing(userId)

    suspend fun getFollowersCount(userId: String): Resource<Int> =
        followDataSource.getFollowersCount(userId)

    suspend fun getFollowingCount(userId: String): Resource<Int> =
        followDataSource.getFollowingCount(userId)
}
