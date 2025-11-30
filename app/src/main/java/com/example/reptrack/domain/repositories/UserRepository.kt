package com.example.reptrack.domain.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String, role: String): Resource<User>
    suspend fun getCurrentUser(): Resource<User>
    suspend fun logout()
    suspend fun getUser(userId: String): Resource<User>
    suspend fun searchUsers(query: String): Resource<List<User>>
}
