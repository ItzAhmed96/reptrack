package com.example.reptrack.domain.models

import com.google.firebase.Timestamp

data class Like(
    val id: String = "", // Composite: "{postId}_{userId}"
    val postId: String = "",
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
