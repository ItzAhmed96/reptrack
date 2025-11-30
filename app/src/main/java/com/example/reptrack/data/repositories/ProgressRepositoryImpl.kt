package com.example.reptrack.data.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.data.data_sources.local.ProgressLocalDataSource
import com.example.reptrack.data.data_sources.remote.ProgressRemoteDataSource
import com.example.reptrack.domain.models.ProgressLog
import com.example.reptrack.domain.repositories.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(
    private val progressRemoteDataSource: ProgressRemoteDataSource,
    private val progressLocalDataSource: ProgressLocalDataSource
) : ProgressRepository {

    override suspend fun logProgress(log: ProgressLog): Resource<String> {
        val result = progressRemoteDataSource.logProgress(log)
        if (result is Resource.Success) {
            withContext(Dispatchers.IO) {
                progressLocalDataSource.saveProgressLog(log)
            }
        }
        return result
    }

    override suspend fun getProgressForUser(userId: String): Resource<List<ProgressLog>> {
        val remoteResult = progressRemoteDataSource.getProgressForUser(userId)
        if (remoteResult is Resource.Success) {
            val logs = remoteResult.data ?: emptyList()
            withContext(Dispatchers.IO) {
                logs.forEach { progressLocalDataSource.saveProgressLog(it) }
            }
            return Resource.Success(logs)
        }
        
        val localLogs = withContext(Dispatchers.IO) {
            progressLocalDataSource.getProgressForUser(userId)
        }
        if (localLogs.isNotEmpty()) {
            return Resource.Success(localLogs)
        }
        
        return remoteResult
    }

    override suspend fun getProgressForExercise(userId: String, exerciseId: String): Resource<List<ProgressLog>> {
        val remoteResult = progressRemoteDataSource.getProgressForExercise(userId, exerciseId)
        if (remoteResult is Resource.Success) {
            val logs = remoteResult.data ?: emptyList()
            withContext(Dispatchers.IO) {
                logs.forEach { progressLocalDataSource.saveProgressLog(it) }
            }
            return Resource.Success(logs)
        }
        
        val localLogs = withContext(Dispatchers.IO) {
            progressLocalDataSource.getProgressForExercise(userId, exerciseId)
        }
        if (localLogs.isNotEmpty()) {
            return Resource.Success(localLogs)
        }
        
        return remoteResult
    }
}
