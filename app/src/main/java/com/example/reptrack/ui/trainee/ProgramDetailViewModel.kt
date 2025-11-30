package com.example.reptrack.ui.trainee

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

data class ProgramDetailUiState(
    val program: Program? = null,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)



class ProgramDetailViewModelFactory(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val userRepository: UserRepository,
    private val programId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramDetailViewModel(manageProgramUseCase, userRepository, programId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProgramDetailViewModel(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val userRepository: UserRepository,
    private val programId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramDetailUiState())
    val uiState: StateFlow<ProgramDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val programResult = manageProgramUseCase.getProgram(programId)
            val exercisesResult = manageProgramUseCase.getExercisesForProgram(programId)
            
            if (programResult is Resource.Success && exercisesResult is Resource.Success) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        program = programResult.data, 
                        exercises = exercisesResult.data ?: emptyList()
                    ) 
                }
            } else {
                val errorMsg = programResult.message ?: exercisesResult.message ?: "Failed to load data"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun joinProgram(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userResult = userRepository.getCurrentUser()
            if (userResult is Resource.Success && userResult.data != null) {
                val userId = userResult.data.id
                val result = manageProgramUseCase.joinProgram(programId, userId)
                if (result is Resource.Success) {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to join program") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
            }
        }
    }
}
