package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.OnPrimary
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.Secondary
import com.aevrontech.finevo.ui.theme.SurfaceContainer

/**
 * Professional calculator keypad for entering transaction amounts. Supports basic arithmetic
 * operations with expression evaluation.
 */
@Composable
fun CalculatorKeypad(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1: 7 8 9 ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("7", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "7")
            }
            NumberButton("8", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "8")
            }
            NumberButton("9", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "9")
            }
            OperatorButton("÷", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "÷")
            }
        }

        // Row 2: 4 5 6 ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("4", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "4")
            }
            NumberButton("5", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "5")
            }
            NumberButton("6", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "6")
            }
            OperatorButton("×", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "×")
            }
        }

        // Row 3: 1 2 3 −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("1", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "1")
            }
            NumberButton("2", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "2")
            }
            NumberButton("3", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "3")
            }
            OperatorButton("−", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "−")
            }
        }

        // Row 4: . 0 ⌫ +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton(".", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                // Only add decimal if the current number doesn't have one
                val lastNumber = expression.split(Regex("[÷×−+]")).lastOrNull() ?: ""
                if (!lastNumber.contains(".")) {
                    onExpressionChange(
                        if (expression.isEmpty() || expression.last() in "÷×−+")
                            expression + "0."
                        else expression + "."
                    )
                }
            }
            NumberButton("0", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "0")
            }
            BackspaceButton(Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (expression.isNotEmpty()) {
                    onExpressionChange(expression.dropLast(1))
                }
            }
            OperatorButton("+", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange(expression + "+")
            }
        }

        // Row 5: Clear and Equals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClearButton(Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onExpressionChange("")
            }
            EqualsButton(Modifier.weight(3f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEquals()
            }
        }
    }
}

@Composable
private fun NumberButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainer,
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = OnSurface)
        }
    }
}

@Composable
private fun OperatorButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Secondary.copy(alpha = 0.2f),
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Secondary)
        }
    }
}

@Composable
private fun BackspaceButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Error.copy(alpha = 0.15f),
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "⌫", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Error)
        }
    }
}

@Composable
private fun ClearButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = OnSurfaceVariant.copy(alpha = 0.15f),
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "C",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun EqualsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Primary,
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "=", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
        }
    }
}

/** Evaluates a simple arithmetic expression. Supports: +, −, ×, ÷ */
fun evaluateExpression(expression: String): Double {
    if (expression.isBlank()) return 0.0

    try {
        // Convert display operators to standard operators
        val standardExpr = expression.replace("×", "*").replace("÷", "/").replace("−", "-")

        // Parse and evaluate using simple left-to-right evaluation
        // First handle multiplication and division
        val tokens = tokenize(standardExpr)
        val result = evaluate(tokens)

        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    } catch (e: Exception) {
        return 0.0
    }
}

private fun tokenize(expression: String): MutableList<Any> {
    val tokens = mutableListOf<Any>()
    var currentNumber = StringBuilder()

    for (char in expression) {
        when {
            char.isDigit() || char == '.' -> currentNumber.append(char)
            char in "+-*/" -> {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString().toDouble())
                    currentNumber = StringBuilder()
                }
                tokens.add(char)
            }
        }
    }

    if (currentNumber.isNotEmpty()) {
        tokens.add(currentNumber.toString().toDouble())
    }

    return tokens
}

private fun evaluate(tokens: MutableList<Any>): Double {
    if (tokens.isEmpty()) return 0.0

    // First pass: handle * and /
    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        if (token == '*' || token == '/') {
            val left = tokens[i - 1] as Double
            val right = tokens[i + 1] as Double
            val result = if (token == '*') left * right else left / right
            tokens.removeAt(i + 1)
            tokens.removeAt(i)
            tokens[i - 1] = result
            i--
        }
        i++
    }

    // Second pass: handle + and -
    i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        if (token == '+' || token == '-') {
            val left = tokens[i - 1] as Double
            val right = tokens[i + 1] as Double
            val result = if (token == '+') left + right else left - right
            tokens.removeAt(i + 1)
            tokens.removeAt(i)
            tokens[i - 1] = result
            i--
        }
        i++
    }

    return if (tokens.isNotEmpty() && tokens[0] is Double) tokens[0] as Double else 0.0
}
