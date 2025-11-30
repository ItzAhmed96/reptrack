package com.example.reptrack.ui.trainee

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

data class TraineeDashboardUiState(
    val programs: List<Program> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TraineeDashboardViewModelFactory(
    private val manageProgramUseCase: ManageProgramUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TraineeDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TraineeDashboardViewModel(manageProgramUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TraineeDashboardViewModel(
    private val manageProgramUseCase: ManageProgramUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraineeDashboardUiState())
    val uiState: StateFlow<TraineeDashboardUiState> = _uiState.asStateFlow()

    init {
        loadPrograms()
    }

    fun loadPrograms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = manageProgramUseCase.getPrograms()
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, programs = result.data ?: emptyList()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load programs") }
            }
        }
    }
}
