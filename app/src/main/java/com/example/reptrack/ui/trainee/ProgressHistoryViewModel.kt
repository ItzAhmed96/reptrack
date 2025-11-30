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

data class ProgressHistoryUiState(
    val history: List<ProgressLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProgressHistoryViewModelFactory(
    private val trackProgressUseCase: TrackProgressUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressHistoryViewModel(trackProgressUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProgressHistoryViewModel(
    private val trackProgressUseCase: TrackProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressHistoryUiState())
    val uiState: StateFlow<ProgressHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = AppModule.auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }
            
            val result = trackProgressUseCase.getProgressForUser(userId)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, history = result.data ?: emptyList()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load history") }
            }
        }
    }
}
