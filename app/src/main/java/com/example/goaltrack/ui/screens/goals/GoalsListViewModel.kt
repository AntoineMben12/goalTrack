package com.example.goaltrack.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.data.model.GoalCategory
import com.example.goaltrack.data.model.GoalStatus
import com.example.goaltrack.domain.repository.GoalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsListUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedStatus: GoalStatus? = null,
    val selectedCategory: GoalCategory? = null,
    val isRefreshing: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GoalsListViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<GoalStatus?>(null)
    private val _selectedCategory = MutableStateFlow<GoalCategory?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    /** Reactive goals list filtered by search query, status, and category. */
    val uiState: StateFlow<GoalsListUiState> = combine(
        goalsRepository.getGoals(),
        _searchQuery,
        _selectedStatus,
        _selectedCategory,
        _isLoading
    ) { goals, query, status, category, loading ->
        val filtered = goals.filter { goal ->
            val matchesQuery = query.isBlank() || goal.title.contains(query, ignoreCase = true)
            val matchesStatus = status == null || goal.statusEnum == status
            val matchesCategory = category == null || goal.categoryEnum == category
            matchesQuery && matchesStatus && matchesCategory
        }
        GoalsListUiState(
            goals = filtered,
            isLoading = loading,
            searchQuery = query,
            selectedStatus = status,
            selectedCategory = category
        )
    }.onStart {
        _isLoading.value = true
    }.onEach {
        _isLoading.value = false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsListUiState())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onStatusFilter(status: GoalStatus?) { _selectedStatus.value = status }
    fun onCategoryFilter(category: GoalCategory?) { _selectedCategory.value = category }

    fun onDeleteGoal(goalId: String) {
        viewModelScope.launch {
            goalsRepository.deleteGoal(goalId)
        }
    }

    fun onMarkComplete(goalId: String) {
        viewModelScope.launch {
            goalsRepository.markGoalComplete(goalId)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            // Re-collect by restarting flow — goals flow emits on next poll
            _isLoading.value = false
        }
    }
}
