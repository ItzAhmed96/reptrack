package com.example.reptrack.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.use_cases.ManageProgramUseCase
import com.example.reptrack.domain.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ProgramCreationUiState(
    val name: String = "",
    val description: String = "",
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)



class ProgramCreationViewModelFactory(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramCreationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramCreationViewModel(manageProgramUseCase, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProgramCreationViewModel(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramCreationUiState())
    val uiState: StateFlow<ProgramCreationUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun addExercise(name: String, sets: Int, reps: Int) {
        val exercise = Exercise(
            id = UUID.randomUUID().toString(), // Temporary ID until saved? Or just use it.
            name = name,
            sets = sets,
            reps = reps
        )
        _uiState.update { it.copy(exercises = it.exercises + exercise) }
    }

    fun removeExercise(exercise: Exercise) {
        _uiState.update { it.copy(exercises = it.exercises - exercise) }
    }

    fun saveProgram() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userResult = userRepository.getCurrentUser()
            if (userResult is Resource.Success && userResult.data != null) {
                val trainerId = userResult.data.id
                val trainerName = userResult.data.name
                
                val programId = UUID.randomUUID().toString()
                val program = Program(
                    id = programId,
                    trainerId = trainerId,
                    trainerName = trainerName,
                    name = _uiState.value.name,
                    description = _uiState.value.description
                )
                
                val result = manageProgramUseCase.createProgram(program)
                if (result is Resource.Success) {
                    // Save exercises
                    _uiState.value.exercises.forEach { exercise ->
                        manageProgramUseCase.addExercise(exercise.copy(programId = programId))
                    }
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to save program") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
            }
        }
    }
}
