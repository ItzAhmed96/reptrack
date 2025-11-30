package com.example.reptrack.domain.use_cases

import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.User
import com.example.reptrack.domain.repositories.UserRepository

class AuthUseCase(private val repository: UserRepository) {
    suspend fun login(email: String, password: String): Resource<User> {
        return repository.login(email, password)
    }

    suspend fun register(name: String, email: String, password: String, role: String): Resource<User> {
        return repository.register(name, email, password, role)
    }
    
    suspend fun getCurrentUser(): Resource<User> {
        return repository.getCurrentUser()
    }
    
    suspend fun logout() {
        repository.logout()
    }
}
