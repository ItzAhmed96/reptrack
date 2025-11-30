package com.example.reptrack.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutExerciseOverride(
    val sets: Int? = null,
    val reps: Int? = null,
    val restTime: Int? = null
)

@Serializable
data class WorkoutDay(
    val name: String = "",
    val exerciseIds: List<String> = emptyList(),
    val overrides: Map<String, WorkoutExerciseOverride> = emptyMap()
)

@Serializable
data class WorkoutPlan(
    val id: String = "",
    val userId: String = "",  // Deprecated, use creatorId
    val creatorId: String = "",  // User who created this workout
    val name: String = "",
    val description: String = "",
    val daysPerWeek: Int = 0,
    val focusAreas: String = "",
    val restDays: String = "",
    val exercises: String = "",
    val days: List<WorkoutDay> = emptyList(),
    val joinedUserIds: List<String> = emptyList(),  // Users who joined this workout
    val createdAt: Long = System.currentTimeMillis()
)
