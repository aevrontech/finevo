package com.aevrontech.finevo.presentation.label

import androidx.compose.ui.graphics.Color

/** Predefined color palette for labels */
object LabelColors {
    val colors =
        listOf(
            "#FF5252", // Red
            "#FF9800", // Orange
            "#FFEB3B", // Yellow
            "#4CAF50", // Green
            "#2196F3", // Blue
            "#9C27B0", // Purple
            "#E91E63", // Pink
            "#00BCD4", // Cyan
            "#795548", // Brown
            "#607D8B", // Grey
            "#3F51B5", // Indigo
            "#009688" // Teal
        )

    fun parse(hex: String): Color {
        return try {
            val colorString = hex.removePrefix("#")
            if (colorString.length == 6) {
                Color(("FF$colorString").toLong(16))
            } else {
                Color.Gray
            }
        } catch (e: Exception) {
            Color.Gray
        }
    }
}
