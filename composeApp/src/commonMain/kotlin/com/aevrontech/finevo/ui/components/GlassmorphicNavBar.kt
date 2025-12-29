package com.aevrontech.finevo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.ui.theme.GlassShadow
import com.aevrontech.finevo.ui.theme.NavBarInactiveIcon

/**
 * Glassmorphic Floating Bottom Navigation Bar
 *
 * Features:
 * - Frosted glass effect with semi-transparent background
 * - Floating pill-shaped design with rounded corners
 * - Lens/Bubble indicator for active tab
 * - Smooth spring animations for tab switching
 */
@Composable
fun GlassmorphicNavBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        // Main glass container
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(66.dp) // Reduced height from 80dp
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(33.dp),
                        ambientColor = GlassShadow,
                        spotColor = GlassShadow
                    )
                    .clip(RoundedCornerShape(33.dp))
                    // Solid white background for light mode visibility
                    .background(Color.White)
                    // Subtle top border for definition
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.White,
                                        Color(
                                            0xFFF8F9FC
                                        ) // Very subtle
                                        // gray at bottom
                                    )
                            ),
                        shape = RoundedCornerShape(33.dp)
                    )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    GlassNavItem(
                        item = item,
                        isSelected = index == selectedIndex,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassNavItem(item: NavBarItem, isSelected: Boolean, onClick: () -> Unit) {
    val scale by
    animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "scale"
    )

    val iconAlpha by
    animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "alpha"
    )

    Column(
        modifier =
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
                .width(56.dp)
                .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with Bubble - fixed size container for centering
        Box(
            modifier =
                Modifier.size(40.dp).graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            // Bubble background (only visible when selected)
            if (isSelected) {
                Box(
                    modifier =
                        Modifier.size(40.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor =
                                    com.aevrontech.finevo.ui
                                        .theme
                                        .HabitGradientStart
                                        .copy(alpha = 0.5f),
                                spotColor =
                                    com.aevrontech.finevo.ui
                                        .theme
                                        .HabitGradientStart
                                        .copy(alpha = 0.3f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                com.aevrontech
                                                    .finevo
                                                    .ui
                                                    .theme
                                                    .HabitGradientStart,
                                                com.aevrontech
                                                    .finevo
                                                    .ui
                                                    .theme
                                                    .HabitGradientEnd
                                            )
                                    )
                            )
                )
            }

            // Icon - centered
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(22.dp).graphicsLayer { alpha = iconAlpha },
                tint = if (isSelected) Color.White else NavBarInactiveIcon
            )
        }

        // Label - always visible
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color =
                if (isSelected) com.aevrontech.finevo.ui.theme.HabitGradientStart
                else NavBarInactiveIcon.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Data class representing a navigation bar item */
data class NavBarItem(val icon: ImageVector, val label: String)
