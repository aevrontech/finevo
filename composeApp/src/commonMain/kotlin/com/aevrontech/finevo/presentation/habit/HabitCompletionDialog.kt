package com.aevrontech.finevo.presentation.habit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aevrontech.finevo.ui.theme.HabitGradientEnd
import com.aevrontech.finevo.ui.theme.HabitGradientStart
import com.aevrontech.finevo.ui.theme.Primary
import kotlinx.coroutines.delay

/** Celebration dialog shown when all habits are completed for the day. */
@Composable
fun HabitCompletionDialog(totalXpEarned: Int, currentStreak: Int, onDismiss: () -> Unit) {
        var showConfetti by remember { mutableStateOf(false) }
        var scaleAnimated by remember { mutableStateOf(false) }

        val scale by
                animateFloatAsState(
                        targetValue = if (scaleAnimated) 1f else 0.5f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                ),
                        label = "scale"
                )

        LaunchedEffect(Unit) {
                scaleAnimated = true
                delay(300)
                showConfetti = true
        }

        Dialog(
                onDismissRequest = onDismiss,
                properties =
                        DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Confetti effect
                        if (showConfetti) {
                                ConfettiEffect()
                        }

                        Card(
                                modifier = Modifier.fillMaxWidth(0.9f).scale(scale),
                                shape = RoundedCornerShape(24.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                                Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        // Trophy/celebration emoji with animated glow
                                        Box(
                                                modifier =
                                                        Modifier.size(100.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                        brush =
                                                                                Brush.radialGradient(
                                                                                        colors =
                                                                                                listOf(
                                                                                                        HabitGradientStart
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.3f
                                                                                                                ),
                                                                                                        HabitGradientEnd
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.1f
                                                                                                                )
                                                                                                )
                                                                                )
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) { Text(text = "🏆", fontSize = 56.sp) }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                                text = "Congratulations!",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                                text =
                                                        "You've completed all your habits for today!",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Stats row
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                                StatItem(
                                                        icon = "⭐",
                                                        value = "+$totalXpEarned XP",
                                                        label = "Earned"
                                                )
                                                StatItem(
                                                        icon = "🔥",
                                                        value = "$currentStreak days",
                                                        label = "Streak"
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Continue button
                                        Button(
                                                onClick = onDismiss,
                                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor = Primary
                                                        )
                                        ) {
                                                Text(
                                                        text = "Continue",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun StatItem(icon: String, value: String, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
}

@Composable
private fun ConfettiEffect() {
        // Simple confetti simulation with emoji particles
        val confettiEmojis = listOf("🎉", "🎊", "✨", "⭐", "🌟")

        repeat(12) { index ->
                val rotation = remember { Animatable(0f) }
                val yOffset = remember { Animatable(-100f) }

                LaunchedEffect(Unit) {
                        // Stagger animation start
                        delay(index * 80L)

                        // Rotation animation
                        rotation.animateTo(
                                targetValue = 360f,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(2000, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                        )
                        )
                }

                LaunchedEffect(Unit) {
                        delay(index * 80L)
                        yOffset.animateTo(targetValue = 800f, animationSpec = tween(3000))
                }

                Box(
                        modifier =
                                Modifier.graphicsLayer {
                                        translationX = (-150f + (index * 50f))
                                        translationY = yOffset.value
                                        rotationZ = rotation.value
                                }
                ) { Text(text = confettiEmojis[index % confettiEmojis.size], fontSize = 24.sp) }
        }
}
