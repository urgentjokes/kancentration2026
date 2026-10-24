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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.ui.viewmodel.GameUiState

@Composable
fun GameOverBanner(
    uiState: GameUiState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimeUp = uiState.gameOverMessage.contains("Time's Up", ignoreCase = true)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Main Header Message
                Text(
                    text = "$emoji ${uiState.gameOverMessage}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Detailed stats line
                Text(
                    text = if (uiState.isTwoPlayer) {
                        "Player 1: ${uiState.player1Score} matches • Player 2: ${uiState.player2Score} matches"
                    } else if (isTimeUp) {
                        "Total Moves: ${uiState.totalMoves}"
                    } else {
                        "Moves: ${uiState.totalMoves} • Time: ${uiState.formattedTimer}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )

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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Play Again 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
