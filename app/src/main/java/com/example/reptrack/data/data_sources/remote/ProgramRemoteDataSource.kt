package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Program
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgramRemoteDataSource(private val firestore: FirebaseFirestore) {

    suspend fun createProgram(program: Program): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.PROGRAMS_COLLECTION).document()
            val newProgram = program.copy(id = ref.id)
            ref.set(newProgram).await()
            Resource.Success(newProgram.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create program")
        }
    }

    suspend fun getPrograms(): Resource<List<Program>> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRAMS_COLLECTION).get().await()
            val programs = snapshot.toObjects(Program::class.java)
            Resource.Success(programs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch programs")
        }
    }
    
    suspend fun getProgram(programId: String): Resource<Program> {
        return try {
            val snapshot = firestore.collection(Constants.PROGRAMS_COLLECTION)
                .document(programId)
                .get()
                .await()
            val program = snapshot.toObject(Program::class.java)
            if (program != null) {
                Resource.Success(program)
            } else {
                Resource.Error("Program not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch program")
        }
    }
}
