package com.example.goaltrack.data.repository

import com.example.goaltrack.data.model.GoalStatistics
import com.example.goaltrack.data.model.Milestone
import com.example.goaltrack.data.model.ProgressUpdate
import com.example.goaltrack.data.model.UserSettings
import com.example.goaltrack.domain.repository.MilestonesRepository
import com.example.goaltrack.domain.repository.ProgressRepository
import com.example.goaltrack.domain.repository.SettingsRepository
import com.example.goaltrack.domain.repository.StatisticsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// ──────────────────────────────────────────────
// Progress Repository Implementation
// ──────────────────────────────────────────────

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ProgressRepository {

    private fun userId() = supabase.auth.currentSessionOrNull()?.user?.id
        ?: throw IllegalStateException("Not authenticated")

    override fun getProgressForGoal(goalId: String): Flow<List<ProgressUpdate>> = flow {
        try {
            val updates = supabase.postgrest["progress_updates"]
                .select {
                    filter { eq("goal_id", goalId) }
                    order("logged_at", Order.DESCENDING)
                }
                .decodeList<ProgressUpdate>()
            emit(updates)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addProgressUpdate(update: ProgressUpdate): Result<ProgressUpdate> {
        return try {
            val withUser = update.copy(userId = userId())
            val created = supabase.postgrest["progress_updates"]
                .insert(withUser) { select() }
                .decodeSingle<ProgressUpdate>()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProgressUpdate(id: String): Result<Unit> {
        return try {
            supabase.postgrest["progress_updates"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ──────────────────────────────────────────────
// Statistics Repository Implementation
// ──────────────────────────────────────────────

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : StatisticsRepository {

    private fun userId() = supabase.auth.currentSessionOrNull()?.user?.id
        ?: throw IllegalStateException("Not authenticated")

    override fun getGoalStatistics(): Flow<GoalStatistics> = flow {
        try {
            val stats = supabase.postgrest["goal_statistics"]
                .select { filter { eq("user_id", userId()) } }
                .decodeSingleOrNull<GoalStatistics>() ?: GoalStatistics()
            emit(stats)
        } catch (e: Exception) {
            emit(GoalStatistics())
        }
    }

    override fun getRecentActivity(): Flow<List<ProgressUpdate>> = flow {
        try {
            val uid = userId()
            val activity = supabase.postgrest["progress_updates"]
                .select {
                    filter { eq("user_id", uid) }
                    order("logged_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<ProgressUpdate>()
            emit(activity)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

// ──────────────────────────────────────────────
// Settings Repository Implementation
// ──────────────────────────────────────────────

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : SettingsRepository {

    private fun userId() = supabase.auth.currentSessionOrNull()?.user?.id
        ?: throw IllegalStateException("Not authenticated")

    override fun getUserSettings(): Flow<UserSettings> = flow {
        try {
            val uid = userId()
            val settings = supabase.postgrest["user_settings"]
                .select { filter { eq("user_id", uid) } }
                .decodeSingleOrNull<UserSettings>() ?: UserSettings(userId = uid)
            emit(settings)
        } catch (e: Exception) {
            emit(UserSettings())
        }
    }

    override suspend fun updateSettings(settings: UserSettings): Result<UserSettings> {
        return try {
            val updated = supabase.postgrest["user_settings"]
                .upsert(settings) { select() }
                .decodeSingle<UserSettings>()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ──────────────────────────────────────────────
// Milestones Repository Implementation
// ──────────────────────────────────────────────

@Singleton
class MilestonesRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : MilestonesRepository {

    override fun getMilestonesForGoal(goalId: String): Flow<List<Milestone>> = flow {
        try {
            val milestones = supabase.postgrest["goal_milestones"]
                .select { filter { eq("goal_id", goalId) } }
                .decodeList<Milestone>()
            emit(milestones)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addMilestone(milestone: Milestone): Result<Milestone> {
        return try {
            val created = supabase.postgrest["goal_milestones"]
                .insert(milestone) { select() }
                .decodeSingle<Milestone>()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleMilestone(milestoneId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            supabase.postgrest["goal_milestones"]
                .update({ set("is_completed", isCompleted) }) {
                    filter { eq("id", milestoneId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMilestone(id: String): Result<Unit> {
        return try {
            supabase.postgrest["goal_milestones"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
