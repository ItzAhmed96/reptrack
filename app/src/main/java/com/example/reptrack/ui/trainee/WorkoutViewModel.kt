package com.example.reptrack.ui.trainee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.ProgressLog
import com.example.reptrack.domain.use_cases.TrackProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val weight: String = "",
    val reps: String = "",
    val notes: String = "",
    val isLogging: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class WorkoutViewModelFactory(
    private val trackProgressUseCase: TrackProgressUseCase,
    private val exerciseId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(trackProgressUseCase, exerciseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WorkoutViewModel(
    private val trackProgressUseCase: TrackProgressUseCase,
    private val exerciseId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun onWeightChange(value: String) {
        _uiState.update { it.copy(weight = value) }
    }

    fun onRepsChange(value: String) {
        _uiState.update { it.copy(reps = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun logProgress() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLogging = true, error = null) }
            
            val userId = AppModule.auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(isLogging = false, error = "User not logged in") }
                return@launch
            }
            
            val log = ProgressLog(
                userId = userId,
                exerciseId = exerciseId,
                date = System.currentTimeMillis(),
                weight = state.weight.toDoubleOrNull() ?: 0.0,
                repsDone = state.reps.toIntOrNull() ?: 0,
                notes = state.notes
            )
            
            val result = trackProgressUseCase.logProgress(log)
            
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLogging = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLogging = false, error = result.message ?: "Failed to log workout") }
            }
        }
    }
    
    fun resetState() {
        _uiState.update { WorkoutUiState() }
    }
}
