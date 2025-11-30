package com.example.reptrack.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Exercise
import com.example.reptrack.domain.use_cases.ManageProgramUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val isLoadingExercises: Boolean = false,
    val exercisesError: String? = null,
    
    // Add Exercise Dialog State
    val isAddDialogOpen: Boolean = false,
    val newExerciseName: String = "",
    val newExerciseSets: String = "",
    val newExerciseReps: String = "",
    val newExerciseRest: String = "",
    val newExerciseNotes: String = "",
    val isAddingExercise: Boolean = false,
    val addExerciseError: String? = null,
    val isAddSuccess: Boolean = false
)

class ManageExercisesViewModelFactory(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val programId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageExercisesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageExercisesViewModel(manageProgramUseCase, programId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ManageExercisesViewModel(
    private val manageProgramUseCase: ManageProgramUseCase,
    private val programId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageExercisesUiState())
    val uiState: StateFlow<ManageExercisesUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExercises = true, exercisesError = null) }
            val result = manageProgramUseCase.getExercisesForProgram(programId)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoadingExercises = false, exercises = result.data ?: emptyList()) }
            } else {
                _uiState.update { it.copy(isLoadingExercises = false, exercisesError = result.message ?: "Failed to load exercises") }
            }
        }
    }
    
    // Dialog Actions
    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true, isAddSuccess = false, addExerciseError = null) }
    }
    
    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false) }
    }
    
    fun onNewExerciseNameChange(value: String) {
        _uiState.update { it.copy(newExerciseName = value) }
    }
    
    fun onNewExerciseSetsChange(value: String) {
        _uiState.update { it.copy(newExerciseSets = value) }
    }
    
    fun onNewExerciseRepsChange(value: String) {
        _uiState.update { it.copy(newExerciseReps = value) }
    }
    
    fun onNewExerciseRestChange(value: String) {
        _uiState.update { it.copy(newExerciseRest = value) }
    }
    
    fun onNewExerciseNotesChange(value: String) {
        _uiState.update { it.copy(newExerciseNotes = value) }
    }

    fun addExercise() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isAddingExercise = true, addExerciseError = null) }
            
            val exercise = Exercise(
                programId = programId,
                name = state.newExerciseName,
                sets = state.newExerciseSets.toIntOrNull() ?: 0,
                reps = state.newExerciseReps.toIntOrNull() ?: 0,
                restTime = state.newExerciseRest.toIntOrNull() ?: 0,
                notes = state.newExerciseNotes
            )
            
            val result = manageProgramUseCase.addExercise(exercise)
            
            if (result is Resource.Success) {
                _uiState.update { 
                    it.copy(
                        isAddingExercise = false, 
                        isAddSuccess = true, 
                        isAddDialogOpen = false,
                        // Reset fields
                        newExerciseName = "",
                        newExerciseSets = "",
                        newExerciseReps = "",
                        newExerciseRest = "",
                        newExerciseNotes = ""
                    ) 
                }
                loadExercises() // Refresh list
            } else {
                _uiState.update { it.copy(isAddingExercise = false, addExerciseError = result.message ?: "Failed to add exercise") }
            }
        }
    }
}
