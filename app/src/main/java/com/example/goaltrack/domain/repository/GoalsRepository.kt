package com.example.goaltrack.domain.repository

import com.example.goaltrack.data.model.Goal
import kotlinx.coroutines.flow.Flow

/** Goals repository interface — abstracts Supabase Postgrest CRUD for goals. */
interface GoalsRepository {
    /** Observes all goals for the authenticated user in real-time. */
    fun getGoals(): Flow<List<Goal>>

    /** Observes a single goal by [id]. Emits null if not found. */
    fun getGoalById(id: String): Flow<Goal?>

    /** Creates a new goal. Returns the persisted [Goal] on success. */
    suspend fun createGoal(goal: Goal): Result<Goal>

    /** Updates an existing goal. Returns the updated [Goal] on success. */
    suspend fun updateGoal(goal: Goal): Result<Goal>

    /** Deletes a goal by [id]. */
    suspend fun deleteGoal(id: String): Result<Unit>

    /** Updates only the [currentValue] field for the given [goalId]. */
    suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit>

    /** Marks a goal as COMPLETED and records [completedAt] timestamp. */
    suspend fun markGoalComplete(goalId: String): Result<Unit>
}
