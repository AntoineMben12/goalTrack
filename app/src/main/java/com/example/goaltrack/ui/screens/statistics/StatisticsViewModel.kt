package com.example.goaltrack.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.GoalStatistics
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.data.model.StatsPeriod
import com.example.goaltrack.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val statistics: GoalStatistics = GoalStatistics(),
    val recentActivity: List<ProgressUpdate> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.THIS_WEEK
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.THIS_WEEK)

    val uiState: StateFlow<StatisticsUiState> = combine(
        statisticsRepository.getGoalStatistics(),
        statisticsRepository.getRecentActivity(),
        _selectedPeriod
    ) { stats, activity, period ->
        StatisticsUiState(
            statistics = stats,
            recentActivity = activity,
            isLoading = false,
            selectedPeriod = period
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())

    fun onPeriodChange(period: StatsPeriod) { _selectedPeriod.value = period }
}
