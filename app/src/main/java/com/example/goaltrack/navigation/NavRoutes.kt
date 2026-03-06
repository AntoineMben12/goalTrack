package com.example.goaltrack.navigation

/** Sealed class defining all navigation routes in the app. */
sealed class NavRoutes(val route: String) {
    // ── Auth ──────────────────────────────────────────────────────────────
    data object Login : NavRoutes("login")
    data object Register : NavRoutes("register")

    // ── Main ──────────────────────────────────────────────────────────────
    data object GoalsList : NavRoutes("goals_list")
    data object Statistics : NavRoutes("statistics")
    data object Settings : NavRoutes("settings")

    // ── Goal-specific (with args) ──────────────────────────────────────────
    data object GoalDetails : NavRoutes("goal_details/{goalId}") {
        fun createRoute(goalId: String) = "goal_details/$goalId"
    }
    data object AddEditGoal : NavRoutes("add_edit_goal?goalId={goalId}") {
        fun createRoute(goalId: String? = null) =
            if (goalId != null) "add_edit_goal?goalId=$goalId" else "add_edit_goal"
    }
    data object ProgressUpdate : NavRoutes("progress_update/{goalId}") {
        fun createRoute(goalId: String) = "progress_update/$goalId"
    }
}
