package com.example.goaltrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.goaltrack.ui.screens.addeditgoal.AddEditGoalScreen
import com.example.goaltrack.ui.screens.auth.LoginScreen
import com.example.goaltrack.ui.screens.auth.LoginViewModel
import com.example.goaltrack.ui.screens.auth.RegisterScreen
import com.example.goaltrack.ui.screens.goaldetails.GoalDetailsScreen
import com.example.goaltrack.ui.screens.goals.GoalsListScreen
import com.example.goaltrack.ui.screens.progress.ProgressUpdateScreen
import com.example.goaltrack.ui.screens.settings.SettingsScreen
import com.example.goaltrack.ui.screens.statistics.StatisticsScreen

/**
 * Root NavHost that wires all screens to their routes.
 * The [LoginViewModel] observes auth state to redirect unauthenticated users to Login.
 */
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val authViewModel: LoginViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Guard: redirect to Login when auth session expires
    LaunchedEffect(authState) {
        if (authState == false) {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {
        // ── Auth ──────────────────────────────────────────────────────────
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.GoalsList.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(NavRoutes.Register.route) }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.GoalsList.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Goals List ────────────────────────────────────────────────────
        composable(NavRoutes.GoalsList.route) {
            GoalsListScreen(
                onGoalClick = { goalId ->
                    navController.navigate(NavRoutes.GoalDetails.createRoute(goalId))
                },
                onAddGoalClick = {
                    navController.navigate(NavRoutes.AddEditGoal.createRoute())
                },
                onStatisticsClick = { navController.navigate(NavRoutes.Statistics.route) },
                onSettingsClick = { navController.navigate(NavRoutes.Settings.route) }
            )
        }

        // ── Goal Details ──────────────────────────────────────────────────
        composable(
            route = NavRoutes.GoalDetails.route,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
            GoalDetailsScreen(
                goalId = goalId,
                onEditGoal = { navController.navigate(NavRoutes.AddEditGoal.createRoute(goalId)) },
                onLogProgress = { navController.navigate(NavRoutes.ProgressUpdate.createRoute(goalId)) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Add / Edit Goal ───────────────────────────────────────────────
        composable(
            route = NavRoutes.AddEditGoal.route,
            arguments = listOf(navArgument("goalId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddEditGoalScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Progress Update ───────────────────────────────────────────────
        composable(
            route = NavRoutes.ProgressUpdate.route,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) {
            ProgressUpdateScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Statistics ────────────────────────────────────────────────────
        composable(NavRoutes.Statistics.route) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }

        // ── Settings ──────────────────────────────────────────────────────
        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
