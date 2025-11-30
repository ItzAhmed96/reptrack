package com.example.reptrack.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ProgressLog(
    val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val date: Long = 0,
    val weight: Double = 0.0,
    val repsDone: Int = 0,
    val notes: String = ""
)
