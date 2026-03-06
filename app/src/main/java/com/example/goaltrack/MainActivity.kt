package com.example.goaltrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.goaltrack.navigation.MainNavHost
import com.example.goaltrack.ui.theme.GoalTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point. Hilt injection is enabled via [AndroidEntryPoint].
 * The entire UI is driven by [MainNavHost] inside [GoalTrackerTheme].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoalTrackerTheme {
                MainNavHost()
            }
        }
    }
}