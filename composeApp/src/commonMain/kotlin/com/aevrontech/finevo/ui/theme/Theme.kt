package com.aevrontech.finevo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Custom extended colors for FinEvo that aren't part of Material3
 */
data class ExtendedColors(
    val success: androidx.compose.ui.graphics.Color,
    val successContainer: androidx.compose.ui.graphics.Color,
    val onSuccess: androidx.compose.ui.graphics.Color,
    val onSuccessContainer: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val warningContainer: androidx.compose.ui.graphics.Color,
    val onWarning: androidx.compose.ui.graphics.Color,
    val onWarningContainer: androidx.compose.ui.graphics.Color,
    val info: androidx.compose.ui.graphics.Color,
    val infoContainer: androidx.compose.ui.graphics.Color,
    val income: androidx.compose.ui.graphics.Color,
    val expense: androidx.compose.ui.graphics.Color,
    val debt: androidx.compose.ui.graphics.Color,
    val investment: androidx.compose.ui.graphics.Color,
    val habitStreak: androidx.compose.ui.graphics.Color,
    val habitComplete: androidx.compose.ui.graphics.Color,
    val habitMissed: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientMid: androidx.compose.ui.graphics.Color,
    val gradientEnd: androidx.compose.ui.graphics.Color,
    val cardGradientStart: androidx.compose.ui.graphics.Color,
    val cardGradientEnd: androidx.compose.ui.graphics.Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Success,
        successContainer = SuccessContainer,
        onSuccess = OnSuccess,
        onSuccessContainer = OnSuccessContainer,
        warning = Warning,
        warningContainer = WarningContainer,
        onWarning = OnWarning,
        onWarningContainer = OnWarningContainer,
        info = Info,
        infoContainer = InfoContainer,
        income = Income,
        expense = Expense,
        debt = Debt,
        investment = Investment,
        habitStreak = HabitStreak,
        habitComplete = HabitComplete,
        habitMissed = HabitMissed,
        gradientStart = GradientStart,
        gradientMid = GradientMid,
        gradientEnd = GradientEnd,
        cardGradientStart = CardGradientStart,
        cardGradientEnd = CardGradientEnd
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim
)

private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    background = LightColors.Background,
    surface = LightColors.Surface,
    onBackground = LightColors.OnBackground,
    onSurface = LightColors.OnSurface
    // Light theme is kept minimal since we primarily use dark mode
)

private val FinEvoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun FinEvoTheme(
    darkTheme: Boolean = true, // Default to dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = ExtendedColors(
        success = Success,
        successContainer = SuccessContainer,
        onSuccess = OnSuccess,
        onSuccessContainer = OnSuccessContainer,
        warning = Warning,
        warningContainer = WarningContainer,
        onWarning = OnWarning,
        onWarningContainer = OnWarningContainer,
        info = Info,
        infoContainer = InfoContainer,
        income = Income,
        expense = Expense,
        debt = Debt,
        investment = Investment,
        habitStreak = HabitStreak,
        habitComplete = HabitComplete,
        habitMissed = HabitMissed,
        gradientStart = GradientStart,
        gradientMid = GradientMid,
        gradientEnd = GradientEnd,
        cardGradientStart = CardGradientStart,
        cardGradientEnd = CardGradientEnd
    )

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinEvoTypography,
            shapes = FinEvoShapes,
            content = content
        )
    }
}

/**
 * Access extended colors through this object
 */
object FinEvoTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
