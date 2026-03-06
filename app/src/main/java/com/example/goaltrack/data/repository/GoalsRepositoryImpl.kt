package com.example.goaltrack.data.repository

import com.example.goaltrack.data.model.Goal
import com.example.goaltrack.domain.repository.GoalsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase-backed implementation of [GoalsRepository].
 * Uses Postgrest to CRUD the `goals` table.
 * Note: Real-time subscriptions use polling via Flow for simplicity;
 * replace with Realtime channel subscription if low-latency sync is needed.
 */
@Singleton
class GoalsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : GoalsRepository {

    private fun currentUserId(): String =
        supabase.auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("User not authenticated")

    override fun getGoals(): Flow<List<Goal>> = flow {
        try {
            val userId = currentUserId()
            val goals = supabase.postgrest["goals"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Goal>()
            emit(goals)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getGoalById(id: String): Flow<Goal?> = flow {
        try {
            val goal = supabase.postgrest["goals"]
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<Goal>()
            emit(goal)
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun createGoal(goal: Goal): Result<Goal> {
        return try {
            val userId = currentUserId()
            val goalWithUser = goal.copy(userId = userId)
            val created = supabase.postgrest["goals"]
                .insert(goalWithUser) { select() }
                .decodeSingle<Goal>()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoal(goal: Goal): Result<Goal> {
        return try {
            val updated = supabase.postgrest["goals"]
                .update(goal) {
                    filter { eq("id", goal.id) }
                    select()
                }
                .decodeSingle<Goal>()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        return try {
            supabase.postgrest["goals"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit> {
        return try {
            supabase.postgrest["goals"]
                .update({
                    set("current_value", newValue)
                }) {
                    filter { eq("id", goalId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markGoalComplete(goalId: String): Result<Unit> {
        return try {
            supabase.postgrest["goals"]
                .update({
                    set("status", "COMPLETED")
                    set("current_value", 100.0)
                }) {
                    filter { eq("id", goalId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
