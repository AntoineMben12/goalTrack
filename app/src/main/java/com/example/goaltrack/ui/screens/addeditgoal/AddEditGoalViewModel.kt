package com.example.goaltrack.ui.screens.addeditgoal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.data.model.GoalCategory
import com.example.goaltrack.data.model.GoalStatus
import com.example.goaltrack.data.model.Milestone
import com.example.goaltrack.data.model.Priority
import com.example.goaltrack.domain.repository.GoalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditGoalUiState(
    val isEditMode: Boolean = false,
    val title: String = "",
    val description: String = "",
    val category: GoalCategory = GoalCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val targetValue: String = "100",
    val unit: String = "%",
    val startDate: String = LocalDate.now().toString(),
    val targetDate: String = LocalDate.now().plusMonths(1).toString(),
    val milestones: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    // Field errors
    val titleError: String? = null,
    val targetValueError: String? = null,
    val targetDateError: String? = null
)

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val goalId: String? = savedStateHandle["goalId"]

    private val _uiState = MutableStateFlow(AddEditGoalUiState(isEditMode = goalId != null))
    val uiState: StateFlow<AddEditGoalUiState> = _uiState.asStateFlow()

    init {
        goalId?.let { id ->
            viewModelScope.launch {
                goalsRepository.getGoalById(id).firstOrNull()?.let { goal ->
                    _uiState.update {
                        it.copy(
                            title = goal.title,
                            description = goal.description ?: "",
                            category = goal.categoryEnum,
                            priority = goal.priorityEnum,
                            targetValue = goal.targetValue.toString(),
                            unit = goal.unit,
                            startDate = goal.startDate,
                            targetDate = goal.targetDate
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onCategoryChange(v: GoalCategory) = _uiState.update { it.copy(category = v) }
    fun onPriorityChange(v: Priority) = _uiState.update { it.copy(priority = v) }
    fun onTargetValueChange(v: String) = _uiState.update { it.copy(targetValue = v, targetValueError = null) }
    fun onUnitChange(v: String) = _uiState.update { it.copy(unit = v) }
    fun onStartDateChange(v: String) = _uiState.update { it.copy(startDate = v) }
    fun onTargetDateChange(v: String) = _uiState.update { it.copy(targetDate = v, targetDateError = null) }
    fun onAddMilestone() = _uiState.update { it.copy(milestones = it.milestones + "") }
    fun onMilestoneChange(index: Int, v: String) = _uiState.update {
        it.copy(milestones = it.milestones.toMutableList().also { list -> list[index] = v })
    }
    fun onRemoveMilestone(index: Int) = _uiState.update {
        it.copy(milestones = it.milestones.filterIndexed { i, _ -> i != index })
    }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun validate(): Boolean {
        val s = _uiState.value
        var valid = true
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }; valid = false
        }
        if (s.targetValue.toDoubleOrNull() == null || s.targetValue.toDouble() <= 0) {
            _uiState.update { it.copy(targetValueError = "Must be a positive number") }; valid = false
        }
        try {
            val start = LocalDate.parse(s.startDate)
            val target = LocalDate.parse(s.targetDate)
            if (!target.isAfter(start)) {
                _uiState.update { it.copy(targetDateError = "Target date must be after start date") }
                valid = false
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(targetDateError = "Invalid date format") }; valid = false
        }
        return valid
    }

    fun onSave() {
        if (!validate()) return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val goal = Goal(
                id = goalId ?: "",
                title = s.title.trim(),
                description = s.description.ifBlank { null },
                category = s.category.name,
                priority = s.priority.name,
                status = GoalStatus.ACTIVE.name,
                targetValue = s.targetValue.toDouble(),
                unit = s.unit,
                startDate = s.startDate,
                targetDate = s.targetDate
            )
            val result = if (goalId != null) goalsRepository.updateGoal(goal)
            else goalsRepository.createGoal(goal)

            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Save failed") }
                }
            )
        }
    }
}
