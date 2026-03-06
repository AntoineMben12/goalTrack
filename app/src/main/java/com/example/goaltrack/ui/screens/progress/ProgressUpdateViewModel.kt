package com.example.goaltrack.ui.screens.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.domain.repository.GoalsRepository
import com.example.goaltrack.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ProgressUpdateUiState(
    val goal: Goal? = null,
    val newValueInput: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val newValue: Double get() = newValueInput.toDoubleOrNull() ?: 0.0
    val previewProgress: Float
        get() {
            val g = goal ?: return 0f
            return if (g.targetValue > 0)
                (newValue / g.targetValue * 100f).toFloat().coerceIn(0f, 100f)
            else 0f
        }
    val isGoalReached: Boolean get() = previewProgress >= 100f
}

@HiltViewModel
class ProgressUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalsRepository: GoalsRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val goalId: String = checkNotNull(savedStateHandle["goalId"])

    private val _uiState = MutableStateFlow(ProgressUpdateUiState(isLoading = true))
    val uiState: StateFlow<ProgressUpdateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            goalsRepository.getGoalById(goalId).collect { goal ->
                _uiState.update { it.copy(goal = goal, isLoading = false,
                    newValueInput = goal?.currentValue?.toString() ?: "") }
            }
        }
    }

    fun onValueChange(v: String) {
        if (v.isEmpty() || v.toDoubleOrNull() != null) {
            _uiState.update { it.copy(newValueInput = v) }
        }
    }

    fun onNoteChange(note: String) = _uiState.update { it.copy(note = note) }

    fun onSubmit() {
        val state = _uiState.value
        val goal = state.goal ?: return
        val value = state.newValue
        if (value < 0) {
            _uiState.update { it.copy(errorMessage = "Value must be positive") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val update = ProgressUpdate(
                goalId = goalId,
                value = value,
                note = state.note.ifBlank { null },
                loggedAt = Instant.now().toString()
            )
            val result = progressRepository.addProgressUpdate(update)
            if (result.isSuccess) {
                goalsRepository.updateGoalProgress(goalId, value)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
