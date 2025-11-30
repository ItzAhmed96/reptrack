package com.example.reptrack.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: String = "",
    val programId: String = "",
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val restTime: Int = 0, // in seconds
    val notes: String = ""
)
