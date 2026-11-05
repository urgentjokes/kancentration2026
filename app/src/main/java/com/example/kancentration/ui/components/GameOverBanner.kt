package com.example.kancentration.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.ui.viewmodel.GameUiState

@Composable
fun GameOverBanner(
    uiState: GameUiState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimeUp = uiState.isTimeExpired || uiState.gameOverMessage.contains("Time's Up", ignoreCase = true)
    val emoji = if (isTimeUp) "🛑" else "🎉"

    val containerColor = if (isTimeUp) Color(0xFFFFF0F0) else Color(0xFFF1F8E9)
    val borderColor = if (isTimeUp) Color(0xFFE53935) else Color(0xFF4CAF50)
    val titleColor = if (isTimeUp) Color(0xFFC62828) else Color(0xFF1B5E20)
    val subtitleColor = if (isTimeUp) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val buttonColor = if (isTimeUp) Color(0xFFC62828) else Color(0xFF2E7D32)

    AnimatedVisibility(
        visible = uiState.isGameOver,
        enter = fadeIn() + slideInVertically(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.5.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Main Header Message
                if (isTimeUp) {
                    Text(
                        text = "$emoji ${uiState.gameOverMessage}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.isTwoPlayer) "$emoji ${uiState.gameOverMessage}" else "$emoji Victory!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Flash cards",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor,
                                textAlign = TextAlign.Center,
                                style = LocalTextStyle.current.copy(
                                    lineHeight = 11.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (uiState.isTwoPlayer) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Player 1: ${uiState.player1Score} matches • Player 2: ${uiState.player2Score} matches",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Play Again pill button aligned to the bottom-right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Play Again",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = LocalTextStyle.current.copy(
                                    lineHeight = 12.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                            Text(
                                text = "🔄",
                                fontSize = 11.sp,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
