package com.example.kancentration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.GameType
import com.example.kancentration.ui.viewmodel.GameUiState

@Composable
fun GameHud(
    uiState: GameUiState,
    onSettingsClick: () -> Unit,
    onDlcStoreClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Row 1: Header & Control Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kancentration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${uiState.settings.dimension.size}×${uiState.settings.dimension.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // DLC Store Button
                IconButton(onClick = onDlcStoreClick, modifier = Modifier.size(36.dp)) {
                    Text(text = "🛒", fontSize = 16.sp)
                }

                // Settings Button
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                    Text(text = "⚙️", fontSize = 16.sp)
                }

                // Restart Button
                IconButton(onClick = onRestartClick, modifier = Modifier.size(36.dp)) {
                    Text(text = "🔄", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: Status & Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (uiState.isTimeAttack) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (uiState.isTimeAttack) Color(0xFFFFB74D) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isTimeAttack) "⏳" else "⏱",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.formattedTimer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isTimeAttack) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Score & Turn Info
            if (uiState.isTwoPlayer) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player 1 Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.activePlayer == 1) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (uiState.activePlayer == 1) 2.dp else 1.dp,
                            if (uiState.activePlayer == 1) Color(0xFF2E7D32) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "P1: ${uiState.player1Score}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (uiState.activePlayer == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.activePlayer == 1) Color(0xFF1B5E20) else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Player 2 Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.activePlayer == 2) Color(0xFFFCE4EC) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (uiState.activePlayer == 2) 2.dp else 1.dp,
                            if (uiState.activePlayer == 2) Color(0xFFC2185B) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "P2: ${uiState.player2Score}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (uiState.activePlayer == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.activePlayer == 2) Color(0xFF880E4F) else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Matches: ${uiState.totalMatches}/${uiState.totalPairs}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Moves: ${uiState.totalMoves}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
