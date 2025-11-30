package com.example.reptrack.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Program(
    val id: String = "",
    val trainerId: String = "",
    val trainerName: String = "",
    val name: String = "",
    val description: String = ""
)
