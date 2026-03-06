package com.example.goaltrack.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a goal in the `goals` Supabase table.
 *
 * @property progressPercent Computed 0f-100f percentage of goal completion.
 */
@Serializable
data class Goal(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("category") val category: String = GoalCategory.OTHER.name,
    @SerialName("priority") val priority: String = Priority.MEDIUM.name,
    @SerialName("status") val status: String = GoalStatus.ACTIVE.name,
    @SerialName("target_value") val targetValue: Double = 100.0,
    @SerialName("current_value") val currentValue: Double = 0.0,
    @SerialName("unit") val unit: String = "%",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("target_date") val targetDate: String = "",
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
) {
    /** Progress as a percentage (0f–100f). */
    val progressPercent: Float
        get() = if (targetValue > 0) {
            ((currentValue / targetValue) * 100f).toFloat().coerceIn(0f, 100f)
        } else 0f

    /** Parsed GoalCategory enum (defaults to OTHER on unknown values). */
    val categoryEnum: GoalCategory
        get() = GoalCategory.entries.find { it.name == category } ?: GoalCategory.OTHER

    /** Parsed Priority enum (defaults to MEDIUM on unknown values). */
    val priorityEnum: Priority
        get() = Priority.entries.find { it.name == priority } ?: Priority.MEDIUM

    /** Parsed GoalStatus enum (defaults to ACTIVE on unknown values). */
    val statusEnum: GoalStatus
        get() = GoalStatus.entries.find { it.name == status } ?: GoalStatus.ACTIVE
}
