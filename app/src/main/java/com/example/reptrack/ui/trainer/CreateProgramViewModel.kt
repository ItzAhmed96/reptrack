package com.example.reptrack.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Program
import com.example.reptrack.domain.use_cases.ManageProgramUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateProgramUiState(
    val name: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class CreateProgramViewModelFactory(
    private val manageProgramUseCase: ManageProgramUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateProgramViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateProgramViewModel(manageProgramUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CreateProgramViewModel(
    private val manageProgramUseCase: ManageProgramUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProgramUiState())
    val uiState: StateFlow<CreateProgramUiState> = _uiState.asStateFlow()
    
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun resetState() {
        _uiState.update { CreateProgramUiState() }
    }

    fun createProgram() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val program = Program(name = state.name, description = state.description)
            val result = manageProgramUseCase.createProgram(program)
            
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to create program") }
            }
        }
    }
}
