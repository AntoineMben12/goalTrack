package com.example.goaltrack.domain.repository

import com.example.goaltrack.data.model.Milestone
import com.example.goaltrack.data.model.ProgressUpdate
import kotlinx.coroutines.flow.Flow

/** Progress updates repository interface. */
interface ProgressRepository {
    /** Observes all progress updates for [goalId], newest first. */
    fun getProgressForGoal(goalId: String): Flow<List<ProgressUpdate>>

    /** Adds a new progress log entry. Returns the persisted entry on success. */
    suspend fun addProgressUpdate(update: ProgressUpdate): Result<ProgressUpdate>

    /** Deletes a progress update by [id]. */
    suspend fun deleteProgressUpdate(id: String): Result<Unit>
}

/** Statistics repository interface. */
interface StatisticsRepository {
    /** Observes the `goal_statistics` view for the current user. */
    fun getGoalStatistics(): Flow<com.example.goaltrack.data.model.GoalStatistics>

    /** Observes recent activity (joined progress_updates + goals) for the current user. */
    fun getRecentActivity(): Flow<List<ProgressUpdate>>
}

/** Settings repository interface. */
interface SettingsRepository {
    /** Observes the current user's settings. */
    fun getUserSettings(): Flow<com.example.goaltrack.data.model.UserSettings>

    /** Persists updated settings. Returns the saved [UserSettings] on success. */
    suspend fun updateSettings(settings: com.example.goaltrack.data.model.UserSettings): Result<com.example.goaltrack.data.model.UserSettings>
}

/** Milestones repository interface. */
interface MilestonesRepository {
    /** Observes milestones for a specific [goalId]. */
    fun getMilestonesForGoal(goalId: String): Flow<List<Milestone>>

    /** Adds a milestone to a goal. */
    suspend fun addMilestone(milestone: Milestone): Result<Milestone>

    /** Toggles the completion state of a milestone. */
    suspend fun toggleMilestone(milestoneId: String, isCompleted: Boolean): Result<Unit>

    /** Deletes a milestone by [id]. */
    suspend fun deleteMilestone(id: String): Result<Unit>
}
