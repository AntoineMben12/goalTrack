package com.example.goaltrack.ui.screens.goaldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.data.model.Milestone
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.domain.repository.GoalsRepository
import com.example.goaltrack.domain.repository.MilestonesRepository
import com.example.goaltrack.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalDetailsUiState(
    val goal: Goal? = null,
    val milestones: List<Milestone> = emptyList(),
    val progressHistory: List<ProgressUpdate> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class GoalDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalsRepository: GoalsRepository,
    private val milestonesRepository: MilestonesRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val goalId: String = checkNotNull(savedStateHandle["goalId"])

    val uiState: StateFlow<GoalDetailsUiState> = combine(
        goalsRepository.getGoalById(goalId),
        milestonesRepository.getMilestonesForGoal(goalId),
        progressRepository.getProgressForGoal(goalId)
    ) { goal, milestones, progress ->
        GoalDetailsUiState(
            goal = goal,
            milestones = milestones,
            progressHistory = progress,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalDetailsUiState())

    fun onDeleteGoal() {
        viewModelScope.launch { goalsRepository.deleteGoal(goalId) }
    }

    fun onMarkComplete() {
        viewModelScope.launch { goalsRepository.markGoalComplete(goalId) }
    }

    fun onToggleMilestone(milestoneId: String, current: Boolean) {
        viewModelScope.launch {
            milestonesRepository.toggleMilestone(milestoneId, !current)
        }
    }

    fun onAddMilestone(milestone: Milestone) {
        viewModelScope.launch { milestonesRepository.addMilestone(milestone) }
    }
}
