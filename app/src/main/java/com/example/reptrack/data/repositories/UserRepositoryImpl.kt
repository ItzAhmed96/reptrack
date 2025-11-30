package com.example.reptrack.data.repositories

import com.example.reptrack.common.Resource
import com.example.reptrack.data.data_sources.remote.AuthDataSource
import com.example.reptrack.domain.models.User
import com.example.reptrack.domain.repositories.UserRepository

class UserRepositoryImpl(
    private val authDataSource: AuthDataSource
) : UserRepository {
    override suspend fun login(email: String, password: String): Resource<User> {
        return authDataSource.login(email, password)
    }

    override suspend fun register(name: String, email: String, password: String, role: String): Resource<User> {
        return authDataSource.register(name, email, password, role)
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return authDataSource.getCurrentUser()
    }

    override suspend fun logout() {
        authDataSource.logout()
    }

    override suspend fun getUser(userId: String): Resource<User> {
        return authDataSource.getUser(userId)
    }

    override suspend fun searchUsers(query: String): Resource<List<User>> {
        return authDataSource.searchUsers(query)
    }
    
    suspend fun updateUser(userId: String, name: String, bio: String, profilePicUrl: String): Resource<Unit> {
        return authDataSource.updateUser(userId, name, bio, profilePicUrl)
    }
}
