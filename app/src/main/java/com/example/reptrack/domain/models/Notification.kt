package com.example.reptrack.domain.models

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "", // User who receives the notification
    val actorId: String = "", // User who performed the action
    val actorName: String = "",
    val actorProfilePicUrl: String = "",
    val type: String = "", // "like", "comment", "follow"
    val postId: String = "", // For like/comment notifications
    val message: String = "",
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)
