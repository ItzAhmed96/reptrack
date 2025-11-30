package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val document = firestore.collection(Constants.USERS_COLLECTION).document(uid).get().await()
            val user = document.toObject(User::class.java)
            
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User data not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun register(name: String, email: String, password: String, role: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val user = User(id = uid, name = name, email = email, role = role)
            firestore.collection(Constants.USERS_COLLECTION).document(uid).set(user).await()
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }
    
    suspend fun logout() {
        auth.signOut()
    }
    
    suspend fun getCurrentUser(): Resource<User> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("No user logged in")
        return try {
             val document = firestore.collection(Constants.USERS_COLLECTION).document(uid).get().await()
             val user = document.toObject(User::class.java)
             if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun getUser(userId: String): Resource<User> {
        return try {
            val document = firestore.collection(Constants.USERS_COLLECTION).document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun searchUsers(query: String): Resource<List<User>> {
        return try {
            // Simple search by name prefix
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get().await()
            val users = snapshot.toObjects(User::class.java)
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search users")
        }
    }
}
