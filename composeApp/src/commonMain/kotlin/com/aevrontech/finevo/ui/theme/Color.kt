package com.aevrontech.finevo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FinEvo Futuristic Dark Theme Color Palette
 * 
 * Design Philosophy:
 * - Deep space backgrounds with vibrant neon accents
 * - Electric blues and purples for primary actions
 * - Gradient-ready colors for dynamic UI elements
 * - High contrast for accessibility
 */

// ============================================
// PRIMARY COLORS
// ============================================

// Electric Violet - Main brand color
val Primary = Color(0xFF6C63FF)
val PrimaryLight = Color(0xFF9D97FF)
val PrimaryDark = Color(0xFF5A52D5)
val PrimaryContainer = Color(0xFF1E1B4B)
val OnPrimary = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFFE0E0FF)

// ============================================
// SECONDARY COLORS
// ============================================

// Cyber Cyan - Accent color
val Secondary = Color(0xFF00D9FF)
val SecondaryLight = Color(0xFF6EFFFF)
val SecondaryDark = Color(0xFF00A8CC)
val SecondaryContainer = Color(0xFF003544)
val OnSecondary = Color(0xFF003544)
val OnSecondaryContainer = Color(0xFFB8F3FF)

// ============================================
// TERTIARY COLORS
// ============================================

// Neon Pink - Highlights
val Tertiary = Color(0xFFFF6B9D)
val TertiaryLight = Color(0xFFFFB3C8)
val TertiaryDark = Color(0xFFD4507B)
val TertiaryContainer = Color(0xFF4A1F2E)
val OnTertiary = Color(0xFF4A1F2E)
val OnTertiaryContainer = Color(0xFFFFDAE4)

// ============================================
// BACKGROUND COLORS
// ============================================

// Deep Space - Background colors
val Background = Color(0xFF0A0E1A)
val BackgroundLight = Color(0xFF121829)
val BackgroundElevated = Color(0xFF1A2038)
val Surface = Color(0xFF121829)
val SurfaceVariant = Color(0xFF1A2038)
val SurfaceContainer = Color(0xFF1E2545)
val SurfaceContainerHigh = Color(0xFF252D52)
val SurfaceContainerHighest = Color(0xFF2C365F)
val OnBackground = Color(0xFFE4E6F0)
val OnSurface = Color(0xFFE4E6F0)
val OnSurfaceVariant = Color(0xFFA8ACBF)

// ============================================
// SEMANTIC COLORS
// ============================================

// Success - Income, achievements, positive
val Success = Color(0xFF00E676)
val SuccessDark = Color(0xFF00C853)
val SuccessContainer = Color(0xFF0D3320)
val OnSuccess = Color(0xFF003314)
val OnSuccessContainer = Color(0xFFB8F5D0)

// Error - Expenses, alerts, negative
val Error = Color(0xFFFF5252)
val ErrorDark = Color(0xFFD32F2F)
val ErrorContainer = Color(0xFF3D1F1F)
val OnError = Color(0xFF3D1F1F)
val OnErrorContainer = Color(0xFFFFDADA)

// Warning - Caution, budget alerts
val Warning = Color(0xFFFFB74D)
val WarningDark = Color(0xFFF57C00)
val WarningContainer = Color(0xFF3D2E1F)
val OnWarning = Color(0xFF3D2E1F)
val OnWarningContainer = Color(0xFFFFE0B2)

// Info - Tips, information
val Info = Color(0xFF64B5F6)
val InfoDark = Color(0xFF1976D2)
val InfoContainer = Color(0xFF1F2F3D)
val OnInfo = Color(0xFF1F2F3D)
val OnInfoContainer = Color(0xFFBBDEFB)

// ============================================
// FINANCIAL COLORS
// ============================================

// Income - Green tones
val Income = Color(0xFF00E676)
val IncomeLight = Color(0xFF69F0AE)

// Expense - Red/Orange tones
val Expense = Color(0xFFFF5252)
val ExpenseLight = Color(0xFFFF8A80)

// Debt - Warning tones
val Debt = Color(0xFFFFB74D)
val DebtPaid = Color(0xFF00E676)

// Investment - Blue tones
val Investment = Color(0xFF64B5F6)
val InvestmentGrowth = Color(0xFF00E676)

// ============================================
// HABIT TRACKER COLORS
// ============================================

val HabitStreak = Color(0xFFFFB74D)
val HabitComplete = Color(0xFF00E676)
val HabitMissed = Color(0xFFFF5252)
val HabitSkipped = Color(0xFFA8ACBF)

// ============================================
// GRADIENT COLORS
// ============================================

// Primary gradient (Electric Violet to Cyber Cyan)
val GradientStart = Color(0xFF6C63FF)
val GradientMid = Color(0xFF00D9FF)
val GradientEnd = Color(0xFF00E676)

// Card gradient
val CardGradientStart = Color(0xFF1A2038)
val CardGradientEnd = Color(0xFF252D52)

// Premium gradient
val PremiumGradientStart = Color(0xFFFFD700)
val PremiumGradientMid = Color(0xFFFF6B9D)
val PremiumGradientEnd = Color(0xFF6C63FF)

// ============================================
// OUTLINE & DIVIDER COLORS
// ============================================

val Outline = Color(0xFF3A4066)
val OutlineVariant = Color(0xFF2A3052)
val Divider = Color(0xFF2A3052)

// ============================================
// SCRIM & OVERLAY
// ============================================

val Scrim = Color(0xCC000000)
val Overlay = Color(0x80000000)

// ============================================
// LIGHT THEME COLORS (for future use)
// ============================================

object LightColors {
    val Primary = Color(0xFF5A52D5)
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF1A1C2E)
    val OnSurface = Color(0xFF1A1C2E)
}
