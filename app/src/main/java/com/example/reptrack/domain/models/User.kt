package com.example.reptrack.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "trainee", // "trainee" or "trainer"
    val profilePicUrl: String = "", // URL to profile picture
    val bio: String = "" // User bio/description
)
