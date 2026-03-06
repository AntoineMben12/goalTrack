package com.example.goaltrack.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Represents a progress log entry in the `progress_updates` Supabase table. */
@Serializable
data class ProgressUpdate(
    @SerialName("id") val id: String = "",
    @SerialName("goal_id") val goalId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("value") val value: Double = 0.0,
    @SerialName("note") val note: String? = null,
    @SerialName("logged_at") val loggedAt: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

/** Represents a milestone in the `goal_milestones` Supabase table. */
@Serializable
data class Milestone(
    @SerialName("id") val id: String = "",
    @SerialName("goal_id") val goalId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("target_value") val targetValue: Double = 0.0,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

/** Represents a user profile in the `profiles` Supabase table. */
@Serializable
data class UserProfile(
    @SerialName("id") val id: String = "",
    @SerialName("username") val username: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/** Represents user preferences in the `user_settings` Supabase table. */
@Serializable
data class UserSettings(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("theme") val theme: String = AppTheme.SYSTEM.name,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("reminder_time") val reminderTime: String? = null,
    @SerialName("weekly_report_enabled") val weeklyReportEnabled: Boolean = true,
    @SerialName("language") val language: String = "en",
    @SerialName("updated_at") val updatedAt: String = ""
) {
    val themeEnum: AppTheme
        get() = AppTheme.entries.find { it.name == theme } ?: AppTheme.SYSTEM
}

/** Statistics for a user, sourced from the `goal_statistics` Supabase view. */
@Serializable
data class GoalStatistics(
    @SerialName("total_goals") val totalGoals: Int = 0,
    @SerialName("active_goals") val activeGoals: Int = 0,
    @SerialName("completed_goals") val completedGoals: Int = 0,
    @SerialName("completion_rate") val completionRate: Double = 0.0,
    @SerialName("avg_progress_percent") val avgProgressPercent: Double = 0.0,
    @SerialName("goals_due_this_week") val goalsDueThisWeek: Int = 0
)
