package com.example.goaltrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryIndigoLight,
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = SecondaryTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = SecondaryTealLight,
    onSecondaryContainer = SecondaryTealDark,
    tertiary = TertiaryAmber,
    onTertiary = OnSurfaceLight,
    tertiaryContainer = TertiaryAmberLight,
    onTertiaryContainer = TertiaryAmberDark,
    error = ErrorCoral,
    onError = SurfaceLight,
    errorContainer = ErrorCoral.copy(alpha = 0.2f),
    onErrorContainer = ErrorCoralDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = Outline,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = PrimaryIndigoDark,
    primaryContainer = PrimaryIndigo,
    onPrimaryContainer = PrimaryIndigoLight,
    secondary = SecondaryTealLight,
    onSecondary = SecondaryTealDark,
    secondaryContainer = SecondaryTeal,
    onSecondaryContainer = SecondaryTealLight,
    tertiary = TertiaryAmberLight,
    onTertiary = TertiaryAmberDark,
    tertiaryContainer = TertiaryAmber,
    onTertiaryContainer = TertiaryAmberLight,
    error = ErrorCoral,
    onError = BackgroundDark,
    errorContainer = ErrorCoral.copy(alpha = 0.25f),
    onErrorContainer = ErrorCoral,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)

/**
 * GoalTracker application theme.
 *
 * Supports:
 * - Dynamic color on Android 12+ (Material You)
 * - Manual light/dark override
 * - Falls back to static indigo/teal palette on older devices
 */
@Composable
fun GoalTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoalTrackerTypography,
        shapes = GoalTrackerShapes,
        content = content
    )
}