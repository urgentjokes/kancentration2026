package com.example.kancentration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.ui.viewmodel.GameUiState

/**
 * Top HUD Bar maintaining a fixed height to prevent vertical layout shifts,
 * with accessible touch targets (>= 44dp) on all action buttons and dimension controls.
 */
@Composable
fun GameHud(
    uiState: GameUiState,
    onSettingsClick: () -> Unit,
    onDlcStoreClick: () -> Unit,
    onRestartClick: () -> Unit,
    onCycleDimensionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Row 1: Header & Control Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "Kancentration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Cycle board dimension",
                            onClick = { onCycleDimensionClick() }
                        )
                ) {
                    Text(
                        text = "${uiState.settings.dimension.size}×${uiState.settings.dimension.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // DLC Store Button
                IconButton(
                    onClick = onDlcStoreClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(text = "🛒", fontSize = 16.sp)
                }

                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(text = "⚙️", fontSize = 16.sp)
                }

                // Restart Button
                IconButton(
                    onClick = onRestartClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(text = "🔄", fontSize = 16.sp)
                }
            }
        }

        // Row 2: Status & Scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer Badge
            val timerBg = when {
                uiState.isTimerLowWarning -> Color(0xFFFFEBEE)
                uiState.isTimeAttack -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val timerBorder = when {
                uiState.isTimerLowWarning -> Color(0xFFE53935)
                uiState.isTimeAttack -> Color(0xFFFFB74D)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
            val timerText = when {
                uiState.isTimerLowWarning -> Color(0xFFC62828)
                uiState.isTimeAttack -> Color(0xFFE65100)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = timerBg,
                border = BorderStroke(1.dp, timerBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            uiState.isTimerLowWarning -> "🚨"
                            uiState.isTimeAttack -> "⏳"
                            else -> "⏱"
                        },
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.formattedTimer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = timerText
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
