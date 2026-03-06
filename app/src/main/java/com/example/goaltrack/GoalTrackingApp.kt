package com.example.goaltrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp to enable Hilt dependency injection
 * across the entire application.
 */
@HiltAndroidApp
class GoalTrackingApp : Application()
