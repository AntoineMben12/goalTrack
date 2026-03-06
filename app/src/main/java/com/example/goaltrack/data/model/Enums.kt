package com.example.goaltrack.data.model

import androidx.compose.ui.graphics.Color
import com.example.goaltrack.ui.theme.CategoryCareer
import com.example.goaltrack.ui.theme.CategoryEducation
import com.example.goaltrack.ui.theme.CategoryFinance
import com.example.goaltrack.ui.theme.CategoryHealth
import com.example.goaltrack.ui.theme.CategoryOther
import com.example.goaltrack.ui.theme.CategoryPersonal
import com.example.goaltrack.ui.theme.PriorityHigh
import com.example.goaltrack.ui.theme.PriorityLow
import com.example.goaltrack.ui.theme.PriorityMedium
import com.example.goaltrack.ui.theme.StatusAbandoned
import com.example.goaltrack.ui.theme.StatusActive
import com.example.goaltrack.ui.theme.StatusCompleted
import com.example.goaltrack.ui.theme.StatusPaused

/** Goal categories with display labels and associated brand colors. */
enum class GoalCategory(val displayLabel: String, val color: Color) {
    HEALTH("Health", CategoryHealth),
    CAREER("Career", CategoryCareer),
    FINANCE("Finance", CategoryFinance),
    PERSONAL("Personal", CategoryPersonal),
    EDUCATION("Education", CategoryEducation),
    OTHER("Other", CategoryOther)
}

/** Priority levels with display labels and associated colors. */
enum class Priority(val displayLabel: String, val color: Color) {
    LOW("Low", PriorityLow),
    MEDIUM("Medium", PriorityMedium),
    HIGH("High", PriorityHigh)
}

/** Goal lifecycle statuses with display labels and associated colors. */
enum class GoalStatus(val displayLabel: String, val color: Color) {
    ACTIVE("Active", StatusActive),
    COMPLETED("Completed", StatusCompleted),
    PAUSED("Paused", StatusPaused),
    ABANDONED("Abandoned", StatusAbandoned)
}

/** Theme preference options stored in user settings. */
enum class AppTheme(val displayLabel: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

/** Available unit options for goal target values. */
enum class GoalUnit(val displayLabel: String) {
    PERCENT("%"),
    KILOMETERS("km"),
    KILOGRAMS("kg"),
    PAGES("pages"),
    HOURS("hours"),
    TIMES("times"),
    STEPS("steps"),
    CUSTOM("custom")
}

/** Selectable time periods for the Statistics screen. */
enum class StatsPeriod(val displayLabel: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}
