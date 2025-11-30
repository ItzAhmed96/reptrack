package com.example.reptrack.domain.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.ProgressLog
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    suspend fun logProgress(log: ProgressLog): Resource<String>
    suspend fun getProgressForUser(userId: String): Resource<List<ProgressLog>>
    suspend fun getProgressForExercise(userId: String, exerciseId: String): Resource<List<ProgressLog>>
}
