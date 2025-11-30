package com.example.reptrack.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.Comment
import com.example.reptrack.domain.models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPostLiked: Boolean = false
)

class PostDetailViewModel(private val postId: String) : ViewModel() {
    private val socialFeedUseCase = AppModule.socialFeedUseCase
    private val authUseCase = AppModule.authUseCase

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    init {
        loadCurrentUser()
        loadPost()
        loadComments()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authUseCase.getCurrentUser()) {
                is Resource.Success -> {
                    currentUserId = result.data?.id ?: ""
                    checkIfLiked()
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun getCurrentUserId(): String = currentUserId

    private fun loadPost() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = socialFeedUseCase.getPost(postId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        post = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            when (val result = socialFeedUseCase.getCommentsForPost(postId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(comments = result.data ?: emptyList())
                    android.util.Log.d("PostDetailVM", "Loaded ${result.data?.size ?: 0} comments")
                }
                is Resource.Error -> {
                    android.util.Log.e("PostDetailVM", "Error loading comments: ${result.message}")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun checkIfLiked() {
        viewModelScope.launch {
            when (val result = socialFeedUseCase.hasUserLiked(postId, currentUserId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isPostLiked = result.data ?: false)
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun addComment(content: String) {
        viewModelScope.launch {
            android.util.Log.d("PostDetailVM", "Adding comment: $content")
            when (val userResult = authUseCase.getCurrentUser()) {
                is Resource.Success -> {
                    val user = userResult.data ?: return@launch
                    val comment = Comment(
                        postId = postId,
                        userId = user.id,
                        userName = user.name,
                        userProfilePicUrl = user.profilePicUrl,
                        content = content
                    )

                    when (val addResult = socialFeedUseCase.addComment(comment)) {
                        is Resource.Success -> {
                            android.util.Log.d("PostDetailVM", "Comment added successfully")
                            // Immediately reload comments
                            loadComments()
                            // Update comment count in post
                            _uiState.value.post?.let { post ->
                                _uiState.value = _uiState.value.copy(
                                    post = post.copy(commentCount = post.commentCount + 1)
                                )
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("PostDetailVM", "Error adding comment: ${addResult.message}")
                            _uiState.value = _uiState.value.copy(error = "Failed to add comment")
                        }
                        is Resource.Loading -> {}
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val isCurrentlyLiked = _uiState.value.isPostLiked
            
            // Optimistic update
            _uiState.value = _uiState.value.copy(isPostLiked = !isCurrentlyLiked)
            _uiState.value.post?.let { post ->
                _uiState.value = _uiState.value.copy(
                    post = post.copy(likeCount = if (isCurrentlyLiked) post.likeCount - 1 else post.likeCount + 1)
                )
            }

            val result = if (isCurrentlyLiked) {
                socialFeedUseCase.unlikePost(postId, currentUserId)
            } else {
                socialFeedUseCase.likePost(postId, currentUserId)
            }

            if (result is Resource.Error) {
                // Revert on error
                _uiState.value = _uiState.value.copy(isPostLiked = isCurrentlyLiked)
                _uiState.value.post?.let { post ->
                    _uiState.value = _uiState.value.copy(
                        post = post.copy(likeCount = if (isCurrentlyLiked) post.likeCount + 1 else post.likeCount - 1)
                    )
                }
            }
        }
    }
}
