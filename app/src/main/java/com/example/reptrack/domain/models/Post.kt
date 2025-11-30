package com.example.reptrack.domain.models

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfilePicUrl: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val workoutReference: String? = null, // Optional reference to program or exercise
    val timestamp: Timestamp = Timestamp.now(),
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
