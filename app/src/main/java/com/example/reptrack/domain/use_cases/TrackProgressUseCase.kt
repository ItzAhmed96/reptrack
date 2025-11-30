package com.example.reptrack.domain.use_cases

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.ProgressLog
import com.example.reptrack.domain.repositories.ProgressRepository

class TrackProgressUseCase(private val repository: ProgressRepository) {
    suspend fun logProgress(log: ProgressLog): Resource<String> {
        return repository.logProgress(log)
    }

    suspend fun getProgressForUser(userId: String): Resource<List<ProgressLog>> {
        return repository.getProgressForUser(userId)
    }
}
