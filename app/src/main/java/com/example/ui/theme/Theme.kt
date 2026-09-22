package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1D08),
    onPrimaryContainer = AccentOrangeLight,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2C251C),
    onSecondaryContainer = AccentAmberLight,
    tertiary = AccentPink,
    background = DarkBaseBackground,
    onBackground = TextPrimaryDark,
    surface = DarkCardSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCardElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorderStroke,
    outlineVariant = DarkBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF9A3412),
    secondary = AccentAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AccentPink,
    background = LightBaseBackground,
    onBackground = TextPrimaryLight,
    surface = LightCardSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCardElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorderStroke,
    outlineVariant = Color(0xFFF3F4F6)
)

@Composable
fun HabitTheme(
    darkTheme: Boolean = true,
    accentColor: Color = AccentOrange,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(
        primary = accentColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content
    )
}
