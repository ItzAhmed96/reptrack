package com.example.reptrack.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostDetailViewModelFactory(private val postId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostDetailViewModel(postId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
