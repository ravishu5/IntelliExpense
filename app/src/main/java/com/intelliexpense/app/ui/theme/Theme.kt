package com.intelliexpense.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun IntelliExpenseTheme(
    themeMode: ThemeMode = ThemeMode.CYBER_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val palette = themeMode.getPalette()

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.Black,
            primaryContainer = palette.primaryGlow,
            onPrimaryContainer = palette.primary,
            secondary = palette.secondary,
            onSecondary = Color.White,
            tertiary = palette.tertiary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.cardBorder,
            error = palette.expenseRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primaryGlow,
            onPrimaryContainer = palette.primary,
            secondary = palette.secondary,
            onSecondary = Color.White,
            tertiary = palette.tertiary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.cardBorder,
            error = palette.expenseRed,
            onError = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = palette.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = palette.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !palette.isDark
                insetsController.isAppearanceLightNavigationBars = !palette.isDark
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
