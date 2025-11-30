package com.example.reptrack.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.Post
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SocialFeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val likedPosts: Set<String> = emptySet()
)

class SocialFeedViewModel : ViewModel() {
    private val socialFeedUseCase = AppModule.socialFeedUseCase
    private val authUseCase = AppModule.authUseCase

    private val _uiState = MutableStateFlow(SocialFeedUiState())
    val uiState: StateFlow<SocialFeedUiState> = _uiState.asStateFlow()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var currentUserId: String = ""

    init {
        loadCurrentUser()
        loadPosts()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authUseCase.getCurrentUser()) {
                is Resource.Success -> {
                    currentUserId = result.data?.id ?: ""
                    loadLikedPosts()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Failed to load user")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun getCurrentUserId(): String = currentUserId

    fun loadPosts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                lastVisibleDocument = null
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            when (val result = socialFeedUseCase.getPosts(lastVisible = if (isRefresh) null else lastVisibleDocument)) {
                is Resource.Success -> {
                    val (newPosts, lastDoc) = result.data ?: (emptyList<Post>() to null)
                    lastVisibleDocument = lastDoc

                    _uiState.value = if (isRefresh) {
                        _uiState.value.copy(
                            posts = newPosts,
                            isRefreshing = false,
                            isLoading = false,
                            error = null,
                            hasMore = newPosts.size >= 20
                        )
                    } else {
                        _uiState.value.copy(
                            posts = _uiState.value.posts + newPosts,
                            isLoading = false,
                            error = null,
                            hasMore = newPosts.size >= 20
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    if (!isRefresh) {
                         _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun loadLikedPosts() {
        viewModelScope.launch {
            val likedPostIds = mutableSetOf<String>()
            _uiState.value.posts.forEach { post ->
                when (val result = socialFeedUseCase.hasUserLiked(post.id, currentUserId)) {
                    is Resource.Success -> {
                        if (result.data == true) {
                            likedPostIds.add(post.id)
                        }
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }
            _uiState.value = _uiState.value.copy(likedPosts = likedPostIds)
        }
    }

    fun createPost(content: String, imageUrl: String?) {
        viewModelScope.launch {
            when (val userResult = authUseCase.getCurrentUser()) {
                is Resource.Success -> {
                    val user = userResult.data ?: return@launch
                    val post = Post(
                        userId = user.id,
                        userName = user.name,
                        userProfilePicUrl = user.profilePicUrl,
                        content = content,
                        imageUrl = imageUrl
                    )

                    when (socialFeedUseCase.createPost(post)) {
                        is Resource.Success -> {
                            loadPosts(isRefresh = true)
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(error = "Failed to create post")
                        }
                        is Resource.Loading -> {}
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "User not found")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val isCurrentlyLiked = _uiState.value.likedPosts.contains(postId)
            
            // Optimistic update
            val updatedLikedPosts = if (isCurrentlyLiked) {
                _uiState.value.likedPosts - postId
            } else {
                _uiState.value.likedPosts + postId
            }
            _uiState.value = _uiState.value.copy(likedPosts = updatedLikedPosts)

            // Update like count optimistically
            val updatedPosts = _uiState.value.posts.map { post ->
                if (post.id == postId) {
                    post.copy(likeCount = if (isCurrentlyLiked) post.likeCount - 1 else post.likeCount + 1)
                } else {
                    post
                }
            }
            _uiState.value = _uiState.value.copy(posts = updatedPosts)

            // Perform actual operation
            val result = if (isCurrentlyLiked) {
                socialFeedUseCase.unlikePost(postId, currentUserId)
            } else {
                socialFeedUseCase.likePost(postId, currentUserId)
            }

            if (result is Resource.Error) {
                // Revert on error
                _uiState.value = _uiState.value.copy(likedPosts = if (isCurrentlyLiked) updatedLikedPosts + postId else updatedLikedPosts - postId)
                val revertedPosts = _uiState.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(likeCount = if (isCurrentlyLiked) post.likeCount + 1 else post.likeCount - 1)
                    } else {
                        post
                    }
                }
                _uiState.value = _uiState.value.copy(posts = revertedPosts)
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (socialFeedUseCase.deletePost(postId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts.filter { it.id != postId }
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Failed to delete post")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun isPostLiked(postId: String): Boolean {
        return _uiState.value.likedPosts.contains(postId)
    }

    // Following System
    fun loadFollowingPosts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                lastVisibleDocument = null
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            // First get list of followed user IDs
            when (val followingResult = socialFeedUseCase.getFollowing(currentUserId)) {
                is Resource.Success -> {
                    val followedIds = followingResult.data ?: emptyList()
                    if (followedIds.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            posts = emptyList(),
                            isLoading = false,
                            isRefreshing = false,
                            hasMore = false
                        )
                        return@launch
                    }

                    // Then fetch posts for these users
                    // Note: Firestore 'in' query is limited to 10 items. For a real app, we'd need a better strategy
                    // (e.g., denormalization or client-side filtering or multiple queries).
                    // For this MVP, we'll just fetch all posts and filter client-side or use a simple query if possible.
                    // Since we don't have a 'getPostsForUsers' in UseCase yet, let's just fetch all posts and filter for now (inefficient but works for MVP)
                    // OR better, let's rely on the 'For You' feed for now and just filter it.
                    
                    // Actually, let's just fetch all posts and filter in memory for this MVP to avoid complex Firestore queries
                    when (val result = socialFeedUseCase.getPosts(limit = 100)) { // Fetch more for filtering
                         is Resource.Success -> {
                            val allPosts = result.data?.first ?: emptyList()
                            val followingPosts = allPosts.filter { it.userId in followedIds }
                            
                            _uiState.value = _uiState.value.copy(
                                posts = followingPosts,
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                                hasMore = false // Simple pagination not supported for client-side filtered list
                            )
                        }
                        is Resource.Error -> {
                             _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {}
                    }
                }
                is Resource.Error -> {
                     _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = followingResult.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    // User Search & Profile
    private val _searchResults = MutableStateFlow<List<com.example.reptrack.domain.models.User>>(emptyList())
    val searchResults: StateFlow<List<com.example.reptrack.domain.models.User>> = _searchResults.asStateFlow()

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = AppModule.userRepository.searchUsers(query)) {
                is Resource.Success -> {
                    _searchResults.value = result.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // Profile Data
    private val _userProfile = MutableStateFlow<com.example.reptrack.domain.models.User?>(null)
    val userProfile: StateFlow<com.example.reptrack.domain.models.User?> = _userProfile.asStateFlow()
    
    private val _isFollowingProfile = MutableStateFlow(false)
    val isFollowingProfile: StateFlow<Boolean> = _isFollowingProfile.asStateFlow()
    
    private val _profileFollowersCount = MutableStateFlow(0)
    val profileFollowersCount: StateFlow<Int> = _profileFollowersCount.asStateFlow()
    
    private val _profileFollowingCount = MutableStateFlow(0)
    val profileFollowingCount: StateFlow<Int> = _profileFollowingCount.asStateFlow()
    
    private val _profilePosts = MutableStateFlow<List<Post>>(emptyList())
    val profilePosts: StateFlow<List<Post>> = _profilePosts.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load User
            when (val result = AppModule.userRepository.getUser(userId)) {
                is Resource.Success -> _userProfile.value = result.data
                is Resource.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                is Resource.Loading -> {}
            }

            // Load Follow Status
            if (currentUserId.isNotEmpty()) {
                when (val result = socialFeedUseCase.isFollowing(currentUserId, userId)) {
                    is Resource.Success -> _isFollowingProfile.value = result.data ?: false
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }

            // Load Stats
            when (val result = socialFeedUseCase.getFollowersCount(userId)) {
                is Resource.Success -> _profileFollowersCount.value = result.data ?: 0
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
            when (val result = socialFeedUseCase.getFollowingCount(userId)) {
                is Resource.Success -> _profileFollowingCount.value = result.data ?: 0
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
            
            // Load User Posts (Client-side filter for MVP)
            when (val result = socialFeedUseCase.getPosts(limit = 100)) {
                is Resource.Success -> {
                    val allPosts = result.data?.first ?: emptyList()
                    _profilePosts.value = allPosts.filter { it.userId == userId }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            // Optimistic update
            _isFollowingProfile.value = true
            _profileFollowersCount.value += 1
            
            when (socialFeedUseCase.followUser(currentUserId, userId)) {
                is Resource.Success -> {} // Success
                is Resource.Error -> {
                    // Revert
                    _isFollowingProfile.value = false
                    _profileFollowersCount.value -= 1
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            // Optimistic update
            _isFollowingProfile.value = false
            _profileFollowersCount.value -= 1
            
            when (socialFeedUseCase.unfollowUser(currentUserId, userId)) {
                is Resource.Success -> {} // Success
                is Resource.Error -> {
                    // Revert
                    _isFollowingProfile.value = true
                    _profileFollowersCount.value += 1
                }
                is Resource.Loading -> {}
            }
        }
    }
}
