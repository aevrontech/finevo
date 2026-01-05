package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Account

@Composable
fun DashboardAccountCard(account: Account, onClick: () -> Unit) {
    val accountColor = parseColor(account.color) ?: Color.DarkGray

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize().background(accountColor).drawBehind {
                    // 1. Premium "Spotlight" Highlight (Top-Left)
                    val highlightRadius = this.size.maxDimension * 0.8f
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                center = Offset(0f, 0f),
                                radius = highlightRadius
                            )
                    )

                    // 2. Depth Shadow (Bottom-Right)
                    val shadowRadius = this.size.maxDimension * 0.9f
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                center =
                                    Offset(
                                        this.size.width,
                                        this.size.height
                                    ),
                                radius = shadowRadius
                            )
                    )
                }
        ) {
            // Content
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Icon + Name | Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier.size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) { Text(account.icon, fontSize = 20.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = account.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Box(
                        modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = account.type.displayName,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Balance Section
                Column {
                    Text(text = "Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = account.formattedBalance(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom Row (Simulate card number or chip if needed, user showed dots)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "**** " + account.id.takeLast(4).uppercase(), // Mock 4 digits
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// Helper to parse color string to Color object
fun parseColor(colorString: String): Color? {
    return try {
        if (colorString.startsWith("#")) {
            val color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) {
                // Set alpha to 255 (fully opaque)
                Color(color or 0xFF000000)
            } else {
                Color(color)
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
